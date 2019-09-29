package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.analyzer.scanning.DatascanTask;
import com.analyzer.ui.TaskTile;
import com.dbutils.common.DBConnections;
import com.dbutils.masking.DatascanResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
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

import static com.analyzer.Utils.logStackTrace;

public class DashboardController implements Initializable {
    @FXML
    private TextField tablesToBeScanned;

    @FXML
    private TextField scannedSoFar;

    @FXML
    private TextField poolSize;

    @FXML
    private Label activeCount;

    @FXML
    private TextField taskCount;

    @FXML
    private TextField completedTasks;

    @FXML
    private ScrollPane dashboardArea;

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
        TaskTile tile = new TaskTile("", 0L, 0L);
        tile.getStyleClass().add("tile-box");
        return tile;
    }

    private void SubmitTasks() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(AppData.getNoThreads());
        List<Future<DatascanResult>> resultList = new ArrayList<>();
        List<DatascanTask> tasks = new ArrayList<>();

        int tableId = 0;
        int index = 0;
        int currentlyRunningTasks = 0;

        List<Integer> taskStatus = new ArrayList<>();
        for (int i = 0; i < AppData.getNoThreads(); i++)
            taskStatus.set(i, 0);

        boolean allTablesScanned = false;

        while (! allTablesScanned) {
            for (int i = 0; i < AppData.getNoThreads() && tableId < AppData.getTablesTobeScanned().size(); i++) {
                if (taskStatus.get(i) == 0) {
                    DatascanTask task = new DatascanTask(tableId, connections.get(i),
                            AppData.getTablesTobeScanned().get(tableId).getTableDetail(),
                            AppData.getTablesTobeScanned().get(tableId).getColumnDetailList(),
                            tiles.get(i));

                    tasks.add(task);
                    Future<DatascanResult> result = executor.submit(task);
                    resultList.add(result);
                    taskStatus.set(i, 1);
                    tableId++;
                }
            }

            // Wait for the task to be completed.
            boolean breakTheLoop = false;
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException ex) {
                    logStackTrace(ex);
                }
                for (int i = 0; i < taskStatus.size(); i++) {
                    Future<DatascanResult> result = resultList.get(i);
                    if (result.isDone()) {
                        taskStatus.set(i, 0);
                        breakTheLoop = true;
                        break;
                    }
                    tasks.get(i).updateProgressCount();
                }

                if (breakTheLoop)
                    break;
            }

            if (tableId >= AppData.getTablesTobeScanned().size()) {
                int count = 0;

                for(int i = 0; i < taskStatus.size(); i++)
                    if (taskStatus.get(i) != 0)
                        count = 1;

                if (count == 0)
                    allTablesScanned = true;
            }
        }
    }
}