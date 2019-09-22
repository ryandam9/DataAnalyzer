package com.analyzer.controllers;

import com.analyzer.classes.RefreshQueryResultsTask;
import com.dbutils.common.TableDetail;
import com.dbutils.sqlserver.SqlServerMetadata;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.analyzer.AppLogger.logger;

public class DataBrowser implements Initializable {
    @FXML
    private Button executeBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private TreeView objectExplorer;

    @FXML
    private TextArea queryText;

    @FXML
    private TableView results;

    @FXML
    private HBox statusBar;

    private Connection connection;
    private String dbType;
    private ProgressIndicator progressIndicator;
    private Label statusMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create a Progress Indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);
        progressIndicator.setVisible(false);

        // Status message
        statusMessage = new Label();
        statusMessage.getStyleClass().add("label-level-3");

        statusBar.getChildren().addAll(progressIndicator, statusMessage);
    }

    public void setupParameters(String host, Connection connection, String dbType) {
        this.connection = connection;
        this.dbType = dbType;

        Map<String, Map<String, List<TableDetail>>> tables;

        if (dbType.equals("SQL Server")) {
            tables = SqlServerMetadata.getAllTables(connection);

            List<String> databaseNames = new ArrayList(tables.keySet());
            logger.debug(databaseNames);

            // Host name will be the Root of the tree.
            TreeItem<String> rootNode = new TreeItem<String>(host);
            rootNode.setExpanded(true);

            // Add Database names.
            databaseNames.forEach((String db) -> {
                TreeItem<String> dbItem = new TreeItem<>(db);

                for (String schema : tables.get(db).keySet()) {
                    TreeItem<String> schemaItem = new TreeItem<>(schema);

                    for (TableDetail table : tables.get(db).get(schema)) {
                        TreeItem<String> tableItem = new TreeItem<>(table.getTable());
                        schemaItem.getChildren().add(tableItem);
                    }

                    dbItem.getChildren().add(schemaItem);
                }

                rootNode.getChildren().add(dbItem);
            });

            objectExplorer.setRoot(rootNode);
        }
    }

    @FXML
    private void executeQuery(ActionEvent event) {
        progressIndicator.setVisible(true);
        statusMessage.setText("");

        String query = queryText.getText();
        ResultSet resultSet;
        ObservableList<ObservableList<String>> queryResultData = FXCollections.observableArrayList();

        // Clear existing query results
        if (results.getColumns().size() > 0)
            results.getColumns().clear();

        // Execute the Query in the Background
        Task<Long> task = new RefreshQueryResultsTask(connection, query, results, progressIndicator, statusMessage);
        new Thread(task).start();
    }
}
