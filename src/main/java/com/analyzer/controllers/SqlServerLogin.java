package com.analyzer.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SqlServerLogin implements Initializable {
    @FXML
    private TextField userId;

    @FXML
    private TextField dbServer;

    @FXML
    private TextField port;

    @FXML
    private Button connectBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void connectToDb(ActionEvent event) {

    }
}