package com.analyzer.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

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
    private AnchorPane dashboardArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
