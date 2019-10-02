package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.analyzer.scanning.DatascanTask;
import com.analyzer.ui.TaskTile;
import com.dbutils.common.DBConnections;
import com.dbutils.masking.DatascanResult;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

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
    private TextField tablesToBeScanned;

    @FXML
    private TextField scannedSoFar;

    @FXML
    private TextField poolSize;

    @FXML
    private TextField activeCount;

    @FXML
    private TextField taskCount;

    @FXML
    private TextField completedTasks;

    @FXML
    private ScrollPane dashboardArea;

    @FXML
    public Button showButton;

    private List<Connection> connections;
    private List<TaskTile> tiles = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Acquire Database connections - One per task
        connections = new ArrayList<>();
        try {
            for (int i = 0; i < AppData.getNoThreads(); i++) {
                Connection connection = DBConnections.getSqlServerConnection(AppData.getUser(), AppData.getHost(), AppData.getPort());
                connections.add(connection);
            }
        } catch (Exception ex) {
            logStackTrace(ex);
        }

        // Create a Tile pane
        TilePane tilePane = new TilePane(Orientation.HORIZONTAL);
        tilePane.setHgap(10.0);
        tilePane.setVgap(10.0);
        tilePane.setPadding(new Insets(20, 20, 20, 20));
        tilePane.setPrefColumns(4);
        tilePane.setMaxWidth(Region.USE_PREF_SIZE);

        dashboardArea.setContent(tilePane);
        dashboardArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        dashboardArea.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Create a tile for each task
        for (int i = 0; i < AppData.getNoThreads(); i++) {
            TaskTile tile = createTile();
            tilePane.getChildren().addAll(tile);
            tiles.add(tile);
        }
    }

    private TaskTile createTile() {
        TaskTile tile = new TaskTile("", "", "");
        tile.getStyleClass().add("tile-box");
        return tile;
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        showButton.setDisable(true);
        new Thread(new PerformDatascan()).start();
    }

    /**
     * This is the main Controller that submits background tasks to perform Data scan - One task per table.
     * This is also a Background thread, which in turn uses Java Executor framework to submit tasks. Since this is a
     * inner class, it has access to the UI elements that are defined in the outer class.
     */
    private class PerformDatascan extends Task<Integer> {
        private ThreadPoolExecutor executor;

        private PerformDatascan() {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(AppData.getNoThreads());
        }

        @Override
        protected Integer call() {
            try {
                List<Future<DatascanResult>> resultList = new ArrayList<>();    // holds results of Data scan
                List<DatascanTask> tasks = new ArrayList<>();

                int tableId = 0;
                int maxParallelTasks = AppData.getNoThreads();

                // If the tables to be scanned is less than Max threads specified, update the max tasks to be the number
                // of tables to be scanned.
                if (AppData.getTablesTobeScanned().size() < AppData.getNoThreads())
                    maxParallelTasks = AppData.getTablesTobeScanned().size();

                Future<?>[] currentlyRunningTasks;
                currentlyRunningTasks = new Future<?>[maxParallelTasks];

                // This tracks the status of Tasks:
                //      1 --> Task is running;
                //      0 --> Task is completed, and a new one can be Submitted.
                List<Integer> taskStatus = new ArrayList<>();
                for (int i = 0; i < maxParallelTasks; i++)
                    taskStatus.add(0);

                boolean allTablesScanned = false;

                logger.debug("Number of tables to be scanned: " + AppData.getTablesTobeScanned().size());
                logger.debug("Max parallel tasks: " + maxParallelTasks);

                // Loop until all tables are scanned and all tasks are completed.
                while (!allTablesScanned) {
                    for (int i = 0; i < maxParallelTasks && tableId < AppData.getTablesTobeScanned().size(); i++) {
                        TaskTile t = tiles.get(i);

                        if (taskStatus.get(i) == 0) {
                            String tableName = AppData.getTablesTobeScanned().get(tableId).getTableDetail().getTable();
                            logger.debug("Available position to submit task: " + i);
                            logger.debug("Table to be analyzed: " + tableName);
                            logger.debug("Table Id to be submitted: " + tableId);

                            DatascanTask task = new DatascanTask(tableId, connections.get(i),
                                    AppData.getTablesTobeScanned().get(tableId).getTableDetail(),
                                    AppData.getTablesTobeScanned().get(tableId).getColumnDetailList(),
                                    t);

                            Platform.runLater(() -> {
                                t.setProgressIndicator(true);
                                t.getTaskName().setText("Table being analyzed\n:    " + tableName);
                                t.getTotalRecordsToScan().setText("999999999");
                            });

                            tasks.add(task);
                            Future<DatascanResult> result = executor.submit(task);   // Submits a task via a Thread.
                            resultList.add(result);                                  // Holds results of all Submitted tasks. Need this to generate a final report.
                            currentlyRunningTasks[i] = result;                    // Holds only the currently running tasks. need this to monitor their progress.
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
                            Future<DatascanResult> result = (Future<DatascanResult>) (currentlyRunningTasks[i]);

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
                                tablesToBeScanned.setText(String.valueOf(AppData.getTablesTobeScanned().size()));
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
                    if (tableId >= AppData.getTablesTobeScanned().size()) {
                        int count = 0;

                        for (int i = 0; i < taskStatus.size(); i++)
                            if (taskStatus.get(i) != 0)
                                count = 1;

                        if (count == 0) {
                            logger.debug("Task Status table: " + taskStatus);
                            logger.debug("All tasks completed. TableId: " + tableId + " >= " + AppData.getTablesTobeScanned().size());
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