package com.analyzer.controllers;

import com.analyzer.Utils;
import com.analyzer.classes.AppData;
import com.analyzer.classes.TableWrapper;
import com.dbutils.common.TableDetail;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.analyzer.AppLogger.logger;

public class SettingsController implements Initializable {
    @FXML
    private RadioButton databaseBtn;

    @FXML
    private ToggleGroup scope;

    @FXML
    private RadioButton schemaBtn;

    @FXML
    private RadioButton tableBtn;

    @FXML
    private TextField noThreads;

    @FXML
    private TextField recordsToScan;

    @FXML
    private ListView<String> objectsToScan;

    private ObservableList<String> listData;
    protected ListProperty<String> listProperty;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Enable multiple item selection mode
        objectsToScan.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listProperty = new SimpleListProperty<>();
        objectsToScan.itemsProperty().bind(listProperty);
        databaseBtnClicked(new ActionEvent());
    }

    @FXML
    private void performDatascan(ActionEvent event) {
        ObservableList<Integer> selectedIndices = objectsToScan.getSelectionModel().getSelectedIndices();

        // Identify the scope of Data scan at the table level - I need to know which DB -> Schema -> Table
        // to be analyzed.
        List<TableWrapper> tablesToBeScanned = new ArrayList<>();

        // If the Data scan needs to be done @ Database level, we need to find all the tables in all the schemas
        // of the selected Databases.
        if (databaseBtn.isSelected()) {
            for (Integer index : selectedIndices) {
                String targetDB = listData.get(index);

                for (String schema : AppData.tables.get(targetDB).keySet()) {
                    for (TableDetail tableDetail : AppData.tables.get(targetDB).get(schema).keySet()) {
                        TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(targetDB).get(schema).get(tableDetail));
                        tablesToBeScanned.add(tableWrapper);
                    }
                }
            }
        }

        // If the Data scan to be performed @ Schema level (that means, not all schemas need to be considered). Only all the
        // tables in selected DB & Schemas to be considered.
        if (schemaBtn.isSelected()) {
            for (Integer index : selectedIndices) {
                logger.debug(listData.get(index));
                String targetDB = listData.get(index).split("\\.")[0];
                String schema = listData.get(index).split("\\.")[1];

                for (TableDetail tableDetail : AppData.tables.get(targetDB).get(schema).keySet()) {
                    TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(targetDB).get(schema).get(tableDetail));
                    tablesToBeScanned.add(tableWrapper);
                }
            }
        }

        // If the Data scan should occur @ Table level, that means, only selected Tables need to be considered.
        if (tableBtn.isSelected()) {
            for (Integer index : selectedIndices) {
                String targetDB = listData.get(index).split("\\.")[0];
                String schema = listData.get(index).split("\\.")[1];
                String table = listData.get(index).split("\\.")[2];

                for (TableDetail tableDetail : AppData.tables.get(targetDB).get(schema).keySet()) {
                    if (tableDetail.getTable().equals(table)) {
                        TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(targetDB).get(schema).get(tableDetail));
                        tablesToBeScanned.add(tableWrapper);
                    }
                }
            }
        }

        logger.debug(tablesToBeScanned);
        Utils.createStage("scan_dashboard.fxml", "theme-1.css", "Dashboard");
    }

    public void databaseBtnClicked(ActionEvent event) {
        List<String> databases = new ArrayList<>(AppData.tables.keySet());
        listData = FXCollections.observableArrayList();
        databases.forEach((db) -> listData.add(db));
        listProperty.set(listData);
    }

    public void schemaBtnClicked(ActionEvent event) {
        List<String> schemas = new ArrayList<>();

        for (String db : AppData.tables.keySet()) {
            for (String schema : AppData.tables.get(db).keySet()) {
                schemas.add(db + "." + schema);
            }
        }

        listData.clear();
        listData = FXCollections.observableArrayList();
        schemas.forEach((schema) -> listData.add(schema));
        listProperty.set(listData);
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

        listData.clear();
        listData = FXCollections.observableArrayList();
        tables.forEach((table) -> listData.add(table));
        listProperty.set(listData);
    }
}
