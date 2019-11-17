package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.analyzer.classes.DataAnalysisResultRecord;
import com.analyzer.classes.TableToAnalyze;
import com.analyzer.scanning.DataScanActivity;
import com.dbutils.common.DBConnections;
import com.dbutils.masking.DataScanResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;

import static com.analyzer.AppLogger.logger;
import static com.analyzer.Utils.logStackTrace;

public class DashboardController implements Initializable {
    @FXML
    private SplitPane splitPane;

    @FXML
    private Label tablesToBeScanned;

    @FXML
    private Label scannedSoFar;

    @FXML
    private Label poolSize;

    @FXML
    private Label activeCount;

    @FXML
    private Label taskCount;

    @FXML
    private Label completedTasks;

    @FXML
    private BorderPane rightAnchorPane;

    @FXML
    private TableView<TableToAnalyze> tableView;

    @FXML
    private TableColumn<TableToAnalyze, String> sno;

    @FXML
    private TableColumn<TableToAnalyze, String> db;

    @FXML
    private TableColumn<TableToAnalyze, String> schema;

    @FXML
    private TableColumn<TableToAnalyze, String> table;

    @FXML
    private TableColumn<TableToAnalyze, String> recordsToScan;

    @FXML
    private TableColumn<TableToAnalyze, String> soFarScanned;

    @FXML
    private TableColumn<TableToAnalyze, Label> status;

    private List<Connection> connections;
    ObservableList<TableToAnalyze> tableViewRows = FXCollections.observableArrayList();
    private ResultsController resultsController;

    public void setResultsController(ResultsController resultsController) {
        this.resultsController = resultsController;
    }

    private static int columnNo = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Acquire Database connections - One per task
        connections = new ArrayList<>();
        try {
            for (int i = 0; i < AppData.noThreads; i++) {
                Connection connection = DBConnections.getSqlServerConnection(AppData.user, AppData.host, AppData.port);
                connections.add(connection);
            }
        } catch (Exception ex) {
            logStackTrace(ex);
        }

        sno.setCellValueFactory(new PropertyValueFactory<>("sno"));
        db.setCellValueFactory(new PropertyValueFactory<>("db"));
        schema.setCellValueFactory(new PropertyValueFactory<>("schema"));
        table.setCellValueFactory(new PropertyValueFactory<>("table"));
        recordsToScan.setCellValueFactory(new PropertyValueFactory<>("recordsToScan"));
        soFarScanned.setCellValueFactory(new PropertyValueFactory<>("soFarScanned"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableViewRows.clear();

        // Create a tile for each table to be Scanned.
        for (int i = 0; i < AppData.tablesTobeScanned.size(); i++) {
            TableToAnalyze row = new TableToAnalyze(Integer.toString(i + 1),
                    AppData.tablesTobeScanned.get(i).getTableDetail().getDb(),
                    AppData.tablesTobeScanned.get(i).getTableDetail().getSchema(),
                    AppData.tablesTobeScanned.get(i).getTableDetail().getTable(),
                    "",
                    "",
                    "NOT STARTED");

            tableViewRows.add(row);
        }

        tableView.setItems(tableViewRows);
        new Thread(new PerformDatascan()).start();
    }

    /**
     * This is the main Controller that submits background tasks to perform Data scan - One task per table.
     * This is also a Background thread, which in turn uses Java Executor framework to submit tasks. Since this is a
     * inner class, it has access to the UI elements that are defined in the outer class.
     */
    private class PerformDatascan extends Task<Integer> {
        private ThreadPoolExecutor executor;
        private List<Future<DataScanResult>> resultList = new ArrayList<>();    // holds results of Data scan

        private PerformDatascan() {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(AppData.noThreads);
        }

        @Override
        protected Integer call() {
            try {
                int maxParallelTasks = AppData.noThreads;

                // If the tables to be scanned is less than Max threads specified, update the max tasks to be the number
                // of tables to be scanned.
                if (AppData.tablesTobeScanned.size() < AppData.noThreads)
                    maxParallelTasks = AppData.tablesTobeScanned.size();

                Future<?>[] currentlyRunningTasks = new Future<?>[maxParallelTasks];
                int tableId = 0;
                boolean allTablesScanned = false;
                List<DataScanActivity> tasks = new ArrayList<>();

                // This tracks the status of Tasks:
                //      1 --> Task is running;
                //      0 --> Task is completed, and a new one can be Submitted.
                List<Integer> taskStatus = new ArrayList<>();
                for (int i = 0; i < maxParallelTasks; i++)
                    taskStatus.add(0);

                logger.debug("Number of tables to be scanned: " + AppData.tablesTobeScanned.size());
                logger.debug("Max parallel tasks: " + maxParallelTasks);

                List<Integer> alreadyNoted = new ArrayList<>();

                // Loop until all tables are scanned and all tasks are completed.
                while (!allTablesScanned) {
                    for (int i = 0; i < maxParallelTasks && tableId < AppData.tablesTobeScanned.size(); i++) {
                        if (taskStatus.get(i) == 0) {
                            String tableName = AppData.tablesTobeScanned.get(tableId).getTableDetail().getTable();
                            logger.debug("Available position to submit task: " + i);
                            logger.debug("Table to be analyzed: " + tableName);
                            logger.debug("Table Id to be submitted: " + tableId);
                            TableToAnalyze tableToAnalyze = tableViewRows.get(tableId);

                            DataScanActivity task = new DataScanActivity(
                                    tableId,
                                    connections.get(i),
                                    AppData.tablesTobeScanned.get(tableId).getTableDetail(),
                                    AppData.tablesTobeScanned.get(tableId).getColumnDetailList(),
                                    tableToAnalyze);

                            tasks.add(task);
                            Future<DataScanResult> result = executor.submit(task);   // Submits a task via a Thread.
                            resultList.add(result);                                  // Holds results of all Submitted tasks. Need this to generate a final report.
                            currentlyRunningTasks[i] = result;                       // Holds only the currently running tasks. need this to monitor their progress.
                            taskStatus.set(i, 1);       // Change the status from 0 to 1.
                            tableId++;

                            Platform.runLater(() -> {
                                ((ProgressIndicator) (((HBox) (tableToAnalyze.getStatus())).getChildren().get(0))).setVisible(true);
                                ((Label) (((HBox) (tableToAnalyze.getStatus())).getChildren().get(1))).setText("RUNNING");
                                ((Label) (((HBox) (tableToAnalyze.getStatus())).getChildren().get(1))).setStyle("-fx-background-color: #065FD4; -fx-text-fill: white");
                            });
                        }
                    }

                    // Wait until at least one task is completed. The loop breaks only if a currently running task is
                    // completed ! otherwise, it waits indefinitely !
                    boolean breakTheLoop = false;
                    while (true) {
                        // Check all the currently running tasks and check if they're completed !!
                        for (int i = 0; i < taskStatus.size(); i++) {
                            Future<DataScanResult> result = (Future<DataScanResult>) (currentlyRunningTasks[i]);

                            if (result.isDone()) {
                                if (!alreadyNoted.contains(result.get().getTaskId())) {
                                    logger.info(String.format("Data analysis for table with Task ID: %d is completed\n%s", result.get().getTaskId(), result.get().toString()));
                                    alreadyNoted.add(result.get().getTaskId());
                                    taskStatus.set(i, 0);            // Task at this place holder is free.
                                    breakTheLoop = true;

                                    Platform.runLater(() -> {
                                        try {
                                            int completedTableId = result.get().getTaskId();
                                            TableToAnalyze TableCompleted = tableViewRows.get(completedTableId);
                                            ((ProgressIndicator) (((HBox) (TableCompleted.getStatus())).getChildren().get(0))).setVisible(false);
                                            ((Label) (((HBox) (TableCompleted.getStatus())).getChildren().get(1))).setText("COMPLETED");
                                            ((Label) (((HBox) (TableCompleted.getStatus())).getChildren().get(1))).setStyle("-fx-background-color: #005D57; -fx-text-fill: white");
                                        } catch (InterruptedException ex) {
                                            logStackTrace(ex);
                                        } catch (ExecutionException ex) {
                                            logStackTrace(ex);
                                        }
                                    });

                                    for (String line : result.get().toString().split("\n")) {
                                        if (line.split(":").length == 6) {
                                            columnNo++;
                                            String db = line.split(":")[1].trim();
                                            String schema = line.split(":")[2].trim();
                                            String table = line.split(":")[3].trim();
                                            String column = line.split(":")[4].trim();
                                            String comments = line.split(":")[5].trim();

                                            DataAnalysisResultRecord dataAnalysisResultRecord = new
                                                    DataAnalysisResultRecord(Integer.toString(columnNo), db, schema, table, column, "", "", comments);

                                            resultsController.addRow(dataAnalysisResultRecord);
                                        }
                                    }
                                    break;
                                }
                            }
                        }

                        // If none of the currently running tasks is completed, wait for some time !
                        if (!breakTheLoop) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(1000);
                            } catch (InterruptedException ex) {
                                logStackTrace(ex);
                            }
                        }

                        if (breakTheLoop) {
                            // Update UI Elements
                            int tablesDone = tableId;
                            Platform.runLater(() -> {
                                tablesToBeScanned.setText(String.valueOf(AppData.tablesTobeScanned.size()));
                                scannedSoFar.setText(String.valueOf(tablesDone));
                                poolSize.setText(String.valueOf(executor.getPoolSize()));
                                activeCount.setText(String.valueOf(executor.getActiveCount()));
                                taskCount.setText(String.valueOf(executor.getTaskCount()));
                                completedTasks.setText(String.valueOf(executor.getCompletedTaskCount()));
                            });
                            break;
                        }
                    }

                    // If this condition is satisfied, it means, all tables have submitted for processing. Ensure that
                    // any currently running tasks are completed.
                    if (tableId >= AppData.tablesTobeScanned.size()) {
                        int count = 0;

                        for (Integer status : taskStatus)
                            if (status != 0)
                                count = 1;

                        if (count == 0) {
                            logger.debug("Task Status table: " + taskStatus);
                            logger.debug("All tasks completed. TableId: " + tableId + " >= " + AppData.tablesTobeScanned.size());
                            logger.debug("All tables have been scanned !!!");
                            allTablesScanned = true;
                        }
                    }
                }
            } catch (Exception ex) {
                logStackTrace(ex);
                System.exit(1);
            }

            try {
                for (Future<DataScanResult> result : resultList)
                    logger.info(result.get());
            } catch (Exception e) {
                logStackTrace(e);
                executor.shutdown();
                System.exit(1);
            }

            return 0;
        }

        @Override
        public void succeeded() {
            super.succeeded();
            executor.shutdown();
        }

        @Override
        protected void done() {
            super.done();

//            for (TaskTile t : tiles) {
//                t.getProgressIndicator().setVisible(false);
//            }
        }
    }
}