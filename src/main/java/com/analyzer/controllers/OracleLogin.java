package com.analyzer.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class OracleLogin implements Initializable {
    @FXML
    private TextField userId;

    @FXML
    private PasswordField password;

    @FXML
    private TextField host;

    @FXML
    private TextField port;

    @FXML
    private RadioButton serviceRadioBtn;

    @FXML
    private RadioButton sidRadioBtn;

    @FXML
    private TextField serviceOrSid;

    @FXML
    private Button connectBtn;

    @FXML
    private VBox statusBox;

    @FXML
    private Label status;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void connectToDb(ActionEvent event) {
    }
}
