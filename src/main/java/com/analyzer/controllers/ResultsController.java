package com.analyzer.controllers;

import com.analyzer.classes.DataAnalysisResultRecord;
import com.analyzer.classes.TableToAnalyze;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ResultsController implements Initializable {
    @FXML
    private ListView categoriesListView;

    @FXML
    private TableView<DataAnalysisResultRecord> resultsTableView;

    @FXML
    private TableColumn<DataAnalysisResultRecord, String> sno;

    @FXML
    private TableColumn<DataAnalysisResultRecord, String> database;

    @FXML
    private TableColumn<DataAnalysisResultRecord, String> schema;

    @FXML
    private TableColumn<DataAnalysisResultRecord, String> table;

    @FXML
    private TableColumn<DataAnalysisResultRecord, String> column;

    @FXML
    private TableColumn<DataAnalysisResultRecord, String> comments;

    ObservableList<DataAnalysisResultRecord> tableViewRows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sno.setCellValueFactory(new PropertyValueFactory<>("sno"));
        database.setCellValueFactory(new PropertyValueFactory<>("db"));
        schema.setCellValueFactory(new PropertyValueFactory<>("schema"));
        table.setCellValueFactory(new PropertyValueFactory<>("table"));
        column.setCellValueFactory(new PropertyValueFactory<>("column"));
        comments.setCellValueFactory(new PropertyValueFactory<>("comments"));

        resultsTableView.setItems(tableViewRows);
    }

    public void addRow(DataAnalysisResultRecord dataAnalysisResultRecord) {
        this.tableViewRows.add(dataAnalysisResultRecord);
    }
}
