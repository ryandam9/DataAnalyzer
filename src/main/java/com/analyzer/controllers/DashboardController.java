package com.analyzer.controllers;

import com.analyzer.ui.TaskTile;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

import java.net.URL;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create a Tile pane
        TilePane tilePane = new TilePane(Orientation.HORIZONTAL);
        tilePane.setHgap(10.0);
        tilePane.setVgap(10.0);
        tilePane.setPadding(new Insets(20, 20, 20, 20));
        tilePane.setPrefColumns(4);
        tilePane.setMaxWidth(Region.USE_PREF_SIZE);

        TaskTile tile = new TaskTile("Task12345", 12345L, 123L);
        tilePane.getChildren().addAll(tile);
        tile.getStyleClass().add("tile-box");

        dashboardArea.setContent(tilePane);
        dashboardArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        dashboardArea.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    }
}
