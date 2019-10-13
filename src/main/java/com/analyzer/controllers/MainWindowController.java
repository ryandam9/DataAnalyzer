package com.analyzer.controllers;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    @FXML
    private Button datascanBtn;

    @FXML
    private RadioButton databaseBtn;

    @FXML
    private RadioButton schemaBtn;

    @FXML
    private RadioButton tableBtn;

    @FXML
    private TextField prefParallelTaskCount;

    @FXML
    private TextField prefRecordsToScan;

    @FXML
    private AnchorPane databasesAnchorPane;

    @FXML
    private VBox databasesVBox;

    @FXML
    private AnchorPane schemasAnchorPane;

    @FXML
    private VBox schemasVBox;

    @FXML
    private VBox tablesVBox;

    @FXML
    private Label statusMessage;

    @FXML
    private ColumnConstraints gridColumn1;

    @FXML
    private ColumnConstraints gridColumn2;

    @FXML
    private ColumnConstraints gridColumn3;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> list = FXCollections.observableArrayList();
        ListProperty<String> databasesListProperty = new SimpleListProperty<>();
        ListProperty<String> schemasListProperty = new SimpleListProperty<>();
        ListProperty<String> tablesListProperty = new SimpleListProperty<>();

        for(int i = 0; i < 200; i++)
            list.add("String " + i);

        databasesListProperty.set(list);
        ListView<String> databasesListView = new ListView<>();
        databasesListView.itemsProperty().bind(databasesListProperty);
        databasesListView.prefHeightProperty().bind(databasesVBox.heightProperty());
        databasesListView.setStyle("-fx-background-insets: 0 ;");            // Remove Listview border.
        databasesVBox.getChildren().add(databasesListView);

        // Schemas
        schemasListProperty.set(list);
        ListView<String> schemasListView = new ListView<>();
        schemasListView.itemsProperty().bind(schemasListProperty);
        schemasListView.prefHeightProperty().bind(schemasVBox.heightProperty());
        schemasListView.setStyle("-fx-background-insets: 0 ;");            // Remove Listview border.
        schemasVBox.getChildren().add(schemasListView);

        // Tables
        tablesListProperty.set(list);
        ListView<String> tablesListView = new ListView<>();
        tablesListView.itemsProperty().bind(tablesListProperty);
        tablesListView.prefHeightProperty().bind(tablesVBox.heightProperty());
        tablesListView.setStyle("-fx-background-insets: 0 ;");            // Remove Listview border.
        tablesVBox.getChildren().add(tablesListView);
    }

    @FXML
    private void databaseBtnClicked(ActionEvent event) {
    }

    @FXML
    private void schemaBtnClicked(ActionEvent event) {
    }

    @FXML
    private void tableBtnClicked(ActionEvent event) {
    }

    @FXML
    private void performDataScan(ActionEvent event) {
    }


}
