package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.analyzer.classes.TableToAnalyze;
import com.analyzer.scanning.DataScanActivity;
import com.analyzer.ui.TaskTile;
import com.dbutils.common.DBConnections;
import com.dbutils.masking.DataScanResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private TableColumn<TableToAnalyze, String> status;

    private List<Connection> connections;
    private List<TaskTile> tiles = new ArrayList<>();
    ObservableList<TableToAnalyze> list = FXCollections.observableArrayList();

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

        list.clear();

        // Create a tile for each table to be Scanned.
        for (int i = 0; i < AppData.tablesTobeScanned.size(); i++) {
            TableToAnalyze row = new TableToAnalyze(Integer.toString(i),
                    AppData.tablesTobeScanned.get(i).getTableDetail().getDb(),
                    AppData.tablesTobeScanned.get(i).getTableDetail().getSchema(),
                    AppData.tablesTobeScanned.get(i).getTableDetail().getTable(),
                    "",
                    "",
                    "NOT STARTED");

            tableView.getItems().add(row);
        }

//        SplitPane.setResizableWithParent(rightAnchorPane, true);

        //new Thread(new PerformDatascan()).start();
    }

    /**
     * This is the main Controller that submits background tasks to perform Data scan - One task per table.
     * This is also a Background thread, which in turn uses Java Executor framework to submit tasks. Since this is a
     * inner class, it has access to the UI elements that are defined in the outer class.
     */
    private class PerformDatascan extends Task<Integer> {
        private ThreadPoolExecutor executor;

        private PerformDatascan() {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(AppData.noThreads);
        }

        @Override
        protected Integer call() {
            try {
                List<Future<DataScanResult>> resultList = new ArrayList<>();    // holds results of Data scan
                List<DataScanActivity> tasks = new ArrayList<>();

                int tableId = 0;
                int maxParallelTasks = AppData.noThreads;

                // If the tables to be scanned is less than Max threads specified, update the max tasks to be the number
                // of tables to be scanned.
                if (AppData.tablesTobeScanned.size() < AppData.noThreads)
                    maxParallelTasks = AppData.tablesTobeScanned.size();

                Future<?>[] currentlyRunningTasks;
                currentlyRunningTasks = new Future<?>[maxParallelTasks];

                // This tracks the status of Tasks:
                //      1 --> Task is running;
                //      0 --> Task is completed, and a new one can be Submitted.
                List<Integer> taskStatus = new ArrayList<>();
                for (int i = 0; i < maxParallelTasks; i++)
                    taskStatus.add(0);

                boolean allTablesScanned = false;

                logger.debug("Number of tables to be scanned: " + AppData.tablesTobeScanned.size());
                logger.debug("Max parallel tasks: " + maxParallelTasks);

                // Loop until all tables are scanned and all tasks are completed.
                while (!allTablesScanned) {
                    for (int i = 0; i < maxParallelTasks && tableId < AppData.tablesTobeScanned.size(); i++) {
                        TaskTile t = tiles.get(i);

                        if (taskStatus.get(i) == 0) {
                            String tableName = AppData.tablesTobeScanned.get(tableId).getTableDetail().getTable();
                            logger.debug("Available position to submit task: " + i);
                            logger.debug("Table to be analyzed: " + tableName);
                            logger.debug("Table Id to be submitted: " + tableId);

                            DataScanActivity task = new DataScanActivity(tableId, connections.get(i),
                                    AppData.tablesTobeScanned.get(tableId).getTableDetail(),
                                    AppData.tablesTobeScanned.get(tableId).getColumnDetailList(),
                                    t);

                            Platform.runLater(() -> {
                                t.setProgressIndicator(true);
                                t.getTaskName().setText("Table being analyzed\n:    " + tableName);
                                t.getTotalRecordsToScan().setText("999999999");
                            });

                            tasks.add(task);
                            Future<DataScanResult> result = executor.submit(task);   // Submits a task via a Thread.
                            resultList.add(result);                                  // Holds results of all Submitted tasks. Need this to generate a final report.
                            currentlyRunningTasks[i] = result;                       // Holds only the currently running tasks. need this to monitor their progress.
                            taskStatus.set(i, 1);       // Change the status from 0 to 1.
                            tableId++;
                        }
                    }

                    logger.debug("Task Status table: " + taskStatus);

                    // Wait until at least one task is completed.
                    boolean breakTheLoop = false;
                    while (true) {
                        // Check all the currently running tasks and check if they're completed !!
                        for (int i = 0; i < taskStatus.size(); i++) {
                            Future<DataScanResult> result = (Future<DataScanResult>) (currentlyRunningTasks[i]);

                            if (result.isDone()) {
                                taskStatus.set(i, 0);            // Task at this place holder is free.
                                breakTheLoop = true;
                            }
                            tasks.get(i).updateProgressCount();
                        }

                        // If none of the currently running tasks is completed, wait for some time !
                        if (breakTheLoop == false) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(50);
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

                        for (int i = 0; i < taskStatus.size(); i++)
                            if (taskStatus.get(i) != 0)
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

            for (TaskTile t : tiles) {
                t.getProgressIndicator().setVisible(false);
            }
        }
    }
}