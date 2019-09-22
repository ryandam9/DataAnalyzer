package com.analyzer.controllers;

import com.dbutils.common.DBConnections;
import com.dbutils.common.TableDetail;
import com.dbutils.sqlserver.SqlServerMetadata;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
        String query = queryText.getText();
        ResultSet resultSet;
        ObservableList<ObservableList<String>> queryResultData = FXCollections.observableArrayList();

        // Clear existing query results
        if (results.getColumns().size() > 0)
            results.getColumns().clear();

        try {
            resultSet = DBConnections.execReadOnlyQuery(connection, query);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            // Setup Header
            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(resultSet.getMetaData().getColumnName(i + 1));

                col.setCellValueFactory(
                        new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                                try {
                                    return new SimpleStringProperty(param.getValue().get(j).toString());
                                } catch (Exception e) {
                                    return null;
                                }
                            }
                        });

                results.getColumns().addAll(col);
                System.out.println("Column [" + i + "] ");
            }

            // Fetch Data from Result Set
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    row.add(resultSet.getString(i));
                }
                System.out.println("Row [1] added " + row);
                queryResultData.add(row);
            }

            // Set Table View with data
            results.setItems(queryResultData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
