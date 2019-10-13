package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.dbutils.common.TableDetail;
import com.dbutils.oracle.OracleMetadata;
import com.dbutils.sqlserver.SqlServerMetadata;
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
import java.util.*;

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

    ListProperty<String> databasesListProperty = new SimpleListProperty<>();
    ListView<String> databasesListView = new ListView<>();

    ListProperty<String> schemasListProperty = new SimpleListProperty<>();
    ListProperty<String> tablesListProperty = new SimpleListProperty<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fetchDBMetadata();

        ListView<String> databasesListView = new ListView<>();
        databasesListView.itemsProperty().bind(databasesListProperty);
        databasesListView.prefHeightProperty().bind(databasesVBox.heightProperty());
        databasesListView.setStyle("-fx-background-insets: 0 ;");            // Remove Listview border.
        databasesVBox.getChildren().add(databasesListView);

        // Schemas
        ListView<String> schemasListView = new ListView<>();
        schemasListView.itemsProperty().bind(schemasListProperty);
        schemasListView.prefHeightProperty().bind(schemasVBox.heightProperty());
        schemasListView.setStyle("-fx-background-insets: 0 ;");            // Remove Listview border.
        schemasVBox.getChildren().add(schemasListView);

        // Tables
        ListView<String> tablesListView = new ListView<>();
        tablesListView.itemsProperty().bind(tablesListProperty);
        tablesListView.prefHeightProperty().bind(tablesVBox.heightProperty());
        tablesListView.setStyle("-fx-background-insets: 0 ;");            // Remove Listview border.
        tablesVBox.getChildren().add(tablesListView);

        databaseBtnClicked(new ActionEvent());
    }

    @FXML
    private void databaseBtnClicked(ActionEvent event) {
        List<String> databases = new ArrayList<>(AppData.tables.keySet());
        ObservableList<String> listData = FXCollections.observableArrayList();
        databasesListProperty.clear();
        databases.forEach((db) -> listData.add(db));
        databasesListProperty.set(listData);
    }

    public void schemaBtnClicked(ActionEvent event) {
        ObservableList<String> listData = FXCollections.observableArrayList();

        for (String db : AppData.tables.keySet()) {
            for (String schema : AppData.tables.get(db).keySet()) {
                listData.add(db + "." + schema);
            }
        }

        schemasListProperty.clear();
        schemasListProperty.set(listData);
    }

    public void tableBtnClicked(ActionEvent event) {
        List<String> tables = new ArrayList<>();

        for (String db : AppData.tables.keySet()) {
            for (String schema : AppData.tables.get(db).keySet()) {
                for (TableDetail tableDetail : AppData.tables.get(db).get(schema).keySet()) {
                    tables.add(db + "." + schema + "." + tableDetail.getTable());
                }
            }
        }
    }

    @FXML
    private void performDataScan(ActionEvent event) {
    }

    /**
     * Fetches Database names, schemas names from the Database and updates the global "AppData.tables" dictionary.
     * Also, updates the Database and Schemas list views.
     */
    private void fetchDBMetadata() {
        List<String> databases = new ArrayList<>();
        List<String> schemas = new ArrayList<>();

        Map<String, List<String>> dbSchemas = new HashMap<>();      // Database + List of schemas in it.

        switch (AppData.dbSelection) {
            case AppData.ORACLE:
                // First get all Database names
                try {
                    databases = OracleMetadata.getAllDatabases(AppData.initialConnection);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // For each DB, fetch the Schemas.
                for (String db : databases) {
                    try {
                        // Update application level dictionary for later use
                        AppData.tables.put(db, new HashMap<>());

                        schemas = OracleMetadata.getAllSchemas(AppData.initialConnection, db);
                        dbSchemas.put(db, schemas);

                        // Update the application level dictionary with all the Schemas.
                        for (String schema : schemas) {
                            AppData.tables.get(db).put(schema, new HashMap<>());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;

            case AppData.SQL_SERVER:
                // First get all Database names
                try {
                    databases = SqlServerMetadata.getAllDatabases(AppData.initialConnection);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // For each DB, fetch the Schemas.
                for (String db : databases) {
                    try {
                        // Update application level dictionary for later use
                        AppData.tables.put(db, new HashMap<>());

                        schemas = SqlServerMetadata.getAllSchemas(AppData.initialConnection, db);
                        dbSchemas.put(db, schemas);

                        // Update the application level dictionary with all the Schemas.
                        for (String schema : schemas) {
                            AppData.tables.get(db).put(schema, new HashMap<>());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
        }
    }
}