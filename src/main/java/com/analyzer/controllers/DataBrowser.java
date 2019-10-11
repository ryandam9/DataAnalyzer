package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.analyzer.classes.RefreshQueryResultsTask;
import com.analyzer.ui.TreeViewEntry;
import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;
import com.dbutils.oracle.OracleMetadata;
import com.dbutils.sqlserver.SqlServerMetadata;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

import static com.analyzer.AppLogger.logger;
import static com.analyzer.Utils.logStackTrace;

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

        // Only one entry can be selected at a time
        objectExplorer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // When a table is clicked, its columns should be printed in a List. Each Column is represented as a
        // Check box.
        objectExplorer.getSelectionModel()
                .selectedItemProperty()
                .addListener(new ChangeListener<TreeViewEntry<String>>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends TreeViewEntry<String>> observable,
                            TreeViewEntry<String> old_val,
                            TreeViewEntry<String> new_val) {

                        String db = null;
                        String schema = null;
                        String table = null;

                        TreeViewEntry<String> selectedItem = new_val;

                        String type = selectedItem.getType();
                        String parentItem = selectedItem.getParentItem();

                        switch (type) {
                            case "root":
                                break;

                            case "database":
                                break;

                            case "schema":
                                db = selectedItem.getParentItem();
                                schema = selectedItem.getValue();

                                // If the schema already has children, don't fetch tables again.
                                if (selectedItem.getChildren().size() > 0)
                                    return;

                                Thread t = new Thread(new FetchTableMetadataTask(db, schema, selectedItem));
                                statusMessage.setText("Fetching metadata from the database for schema: " + schema);
                                t.start();
                                break;

                            case "table":
                                db = selectedItem.getParent().getParent().getValue();
                                schema = selectedItem.getParentItem();
                                table = selectedItem.getValue();

                                break;
                        }
                    }
                });

        fetchDBMetadata();
    }

    private void fetchDBMetadata() {
        List<String> databases = new ArrayList<>();
        List<String> schemas = new ArrayList<>();

        Map<String, List<String>> dbSchemas = new HashMap<>();

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

        try {
            // Host name will be the Root of the tree.
            Node serverIcon = new ImageView(new Image(new File("resources/images/" + "server.png").toURI().toURL().toString(), 16, 16, true, true));
            TreeViewEntry rootNode = new TreeViewEntry("root", null, AppData.host, serverIcon);
            rootNode.setExpanded(true);

            List<String> sortedDBs = new ArrayList<>(dbSchemas.keySet());
            Collections.sort(sortedDBs);

            for (String db : sortedDBs) {
                Node databaseIcon = new ImageView(new Image(new File("resources/images/" + "database-green.png").toURI().toURL().toString(), 16, 16, true, true));
                TreeViewEntry dbItem = new TreeViewEntry("database", AppData.host, db, databaseIcon);
                dbItem.setExpanded(true);

                for (String schema : dbSchemas.get(db)) {
                    Node schemaIcon = new ImageView(new Image(new File("resources/images/" + "schema.png").toURI().toURL().toString(), 16, 16, true, true));
                    TreeViewEntry schemaItem = new TreeViewEntry("schema", db, schema, schemaIcon);
                    schemaItem.setExpanded(true);
                    dbItem.getChildren().add(schemaItem);
                }
                rootNode.getChildren().add(dbItem);
            }
            Platform.runLater(() -> objectExplorer.setRoot(rootNode));
        } catch (MalformedURLException ex) {
            logStackTrace(ex);
        }
    }

    class FetchTableMetadataTask extends Task<Integer> {
        private String db;
        private String schema;
        private TreeViewEntry parent;

        private FetchTableMetadataTask(String db, String schema, TreeViewEntry parent) {
            this.db = db;
            this.schema = schema;
            this.parent = parent;
        }

        @Override
        protected Integer call() throws Exception {
            Platform.runLater(() -> progressIndicator.setVisible(true));
            Map<TableDetail, List<ColumnDetail>> tables = new HashMap<>();
            int tableCount = 0;

            switch (AppData.dbSelection) {
                case AppData.ORACLE:
                    tables = OracleMetadata.getAllTables(AppData.initialConnection, db, schema);
                    break;

                case AppData.SQL_SERVER:
                    tables = SqlServerMetadata.getAllTables(AppData.initialConnection, db, schema);
                    break;
            }

            ObservableList<TreeViewEntry<String>> tablesInThisSchema = FXCollections.observableArrayList();

            // To Sort table names
            List<TableDetail> unorderedTables = new ArrayList<>(tables.keySet());
            Collections.sort(unorderedTables, new Comparator<TableDetail>() {
                @Override
                public int compare(TableDetail o1, TableDetail o2) {
                    return o1.getTable().toLowerCase().compareTo(o2.getTable().toLowerCase());
                }
            });

            for (TableDetail tableDetail : unorderedTables) {
                Node tableIcon = new ImageView(new Image(new File("resources/images/" + "table.png").toURI().toURL().toString(), 16, 16, true, true));
                TreeViewEntry tableItem = new TreeViewEntry("table", schema, tableDetail.getTable(), tableIcon);
                tablesInThisSchema.add(tableItem);
                tableCount++;

                // Update the App level Dictionary
                AppData.tables.get(db).get(schema).put(tableDetail, tables.get(tableDetail));

                logger.debug(AppData.tables.get(db).get(schema).get(tableDetail));
            }

            Platform.runLater(() -> parent.getChildren().addAll(tablesInThisSchema));
            return tableCount;
        }

        @Override
        protected void done() {
            super.done();
            Platform.runLater(() -> progressIndicator.setVisible(false));
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
        Task<Long> task = new RefreshQueryResultsTask(AppData.initialConnection, query, results, progressIndicator, statusMessage);
        new Thread(task).start();
    }
}
