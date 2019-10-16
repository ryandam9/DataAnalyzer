package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.analyzer.classes.TableWrapper;
import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;
import com.dbutils.oracle.OracleMetadata;
import com.dbutils.sqlserver.SqlServerMetadata;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;

import static com.analyzer.AppLogger.logger;

public class MainWindowController implements Initializable {
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
    private VBox scopeBox;

    @FXML
    private ColumnConstraints gridColumn1;

    @FXML
    private ColumnConstraints gridColumn2;

    @FXML
    private ColumnConstraints gridColumn3;

    @FXML
    private HBox statusBar;                 // Entire Status Bar, that holds Msg bar and Button bar.

    @FXML
    private HBox msgBar;

    @FXML
    private Label message;

    @FXML
    private HBox buttonBar;

    @FXML
    private Button showScopeBtn;

    @FXML
    private Button datascanBtn;

    ListProperty<String> databasesListProperty = new SimpleListProperty<>();
    ListView<String> databasesListView;

    ListProperty<String> schemasListProperty = new SimpleListProperty<>();
    ListView<String> schemasListView;

    ListProperty<String> tablesListProperty = new SimpleListProperty<>();
    ListView<String> tablesListView;

    ListProperty<String> scopeListProperty = new SimpleListProperty<>();
    ListView<String> scopeListView;

    private ProgressIndicator progressIndicator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        HBox.setHgrow(msgBar, Priority.ALWAYS);
        HBox.setHgrow(buttonBar, Priority.SOMETIMES);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);
        progressIndicator.setVisible(false);
        buttonBar.getChildren().add(0, progressIndicator);

        // Don't enable the "Perform Data Scan" button initially. Only when a DB/Schema/table(s) is selected and
        // we have gathered all tables metadata, enable this button.
        datascanBtn.setDisable(true);

        // This step gathers the Database and all the schemas in the Databases and stores in Application level map.
        fetchDBMetadata();

        // Create a list view to show databases and bind its Items property with a List Property.
        databasesListView = new ListView<>();
        databasesListView.itemsProperty().bind(databasesListProperty);
        databasesListView.prefHeightProperty().bind(databasesVBox.heightProperty());
        databasesVBox.getChildren().add(databasesListView);
        showDatabases();

        // Only one database can be selected for scanning at a time
        databasesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // When a database is clicked, its Schemas should be rendered in the Schema window.
        databasesListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String selectedItem = databasesListView.getSelectionModel().getSelectedItem();
                AppData.userSelectionDB = selectedItem;
                AppData.userSelectionSchema = null;
                AppData.userSelectionTables.removeAll(AppData.userSelectionTables);

                // When a Database selection is made, show schemas only. Clear any previously shown tables !
                tablesListProperty.clear();
                showSchemas();
            }
        });

        // Schemas
        schemasListView = new ListView<>();
        schemasListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        schemasListView.itemsProperty().bind(schemasListProperty);
        schemasListView.prefHeightProperty().bind(schemasVBox.heightProperty());
        schemasVBox.getChildren().add(schemasListView);

        // When a schema is clicked, its tables should be rendered in the tables window.
        schemasListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String selectedItem = schemasListView.getSelectionModel().getSelectedItem();
                AppData.userSelectionSchema = selectedItem;
                AppData.userSelectionTables.removeAll(AppData.userSelectionTables);
                showTables();
            }
        });

        // Tables
        tablesListView = new ListView<>();
        tablesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tablesListView.itemsProperty().bind(tablesListProperty);
        tablesListView.prefHeightProperty().bind(tablesVBox.heightProperty());
        tablesVBox.getChildren().add(tablesListView);

        // This listview is to show the table list that will be scanned.
        scopeListView = new ListView<>();
        scopeListView.itemsProperty().bind(scopeListProperty);
        scopeListView.prefHeightProperty().bind(scopeBox.heightProperty());
        scopeBox.getChildren().add(scopeListView);
    }

    private void showDatabases() {
        List<String> databases = new ArrayList<>(AppData.tables.keySet());  // Get list of databases
        Collections.sort(databases, (o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));

        ObservableList<String> listData = FXCollections.observableArrayList();
        databasesListProperty.clear();
        databases.forEach((db) -> listData.add(db));
        databasesListProperty.set(listData);
    }

    private void showSchemas() {
        String db = AppData.userSelectionDB;
        ObservableList<String> listData = FXCollections.observableArrayList();

        for (String schema : AppData.tables.get(db).keySet()) {
            listData.add(schema);
        }

        Collections.sort(listData, (o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));

        schemasListProperty.clear();
        schemasListProperty.set(listData);
    }

    private void showTables() {
        String db = AppData.userSelectionDB;
        String schema = AppData.userSelectionSchema;

        // If the tables don't already exist in the map, run the background thread to fetch the tables.
        if (AppData.tables.get(db).get(schema).keySet().size() == 0) {
            Thread t = new Thread(new FetchTablesListInASchemaTask(db, schema, true));
            t.start();
        } else {
            // If the table list is already gathered, just show them.
            sortAndShowTables(db, schema);
        }
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

    @FXML
    private void databaseBtnClicked(ActionEvent event) {
    }

    @FXML
    private void schemaBtnClicked(ActionEvent event) {
    }

    @FXML
    private void tableBtnClicked(ActionEvent event) {
    }

    /**
     * This class fetches tables that exist in a Selected DB & Schema. Given that there could be thousands of tables
     * exist in a schema, It is implemented as JavaFX Task, so that, its body is executed in the background.
     */
    class FetchTablesListInASchemaTask extends Task<Integer> {
        private String db;
        private String schema;
        private boolean showTables;

        public FetchTablesListInASchemaTask(String db, String schema, boolean showTables) {
            this.db = db;
            this.schema = schema;
            this.showTables = showTables;
        }

        @Override
        protected Integer call() throws Exception {
//            Platform.runLater(() -> progressIndicator.setVisible(true));
            Map<TableDetail, List<ColumnDetail>> tablesMap = new HashMap<>();
            int tableCount = 0;

            switch (AppData.dbSelection) {
                case AppData.ORACLE:
                    tablesMap = OracleMetadata.getAllTables(AppData.initialConnection, this.db, this.schema);
                    break;

                case AppData.SQL_SERVER:
                    tablesMap = SqlServerMetadata.getAllTables(AppData.initialConnection, this.db, this.schema);
                    break;
            }

            // Update the App level Dictionary
            synchronized (this) {
                for (TableDetail tableDetail : tablesMap.keySet()) {
                    AppData.tables.get(this.db).get(this.schema).put(tableDetail, tablesMap.get(tableDetail));
                    logger.debug(AppData.tables.get(this.db).get(this.schema).get(tableDetail));
                    tableCount++;
                }

                if (showTables)
                    sortAndShowTables(this.db, this.schema);
            }

//            Platform.runLater(() -> parent.getChildren().addAll(tablesInThisSchema));
            return tableCount;
        }

        @Override
        protected void done() {
            super.done();
//            Platform.runLater(() -> progressIndicator.setVisible(false));
        }
    }

    /**
     * Shows the tables that exist in a given database and schema
     *
     * @param db
     * @param schema
     */
    private void sortAndShowTables(String db, String schema) {
        // Get the table list from app level map
        List<TableDetail> unorderedTables = new ArrayList<>(AppData.tables.get(db).get(schema).keySet());

        // Sort the tables on table name
        Collections.sort(unorderedTables, new Comparator<TableDetail>() {
            @Override
            public int compare(TableDetail o1, TableDetail o2) {
                return o1.getTable().toLowerCase().compareTo(o2.getTable().toLowerCase());
            }
        });

        // Type of table entry is "TableDetail". Just get the table name as a String
        ObservableList<String> listData = FXCollections.observableArrayList();
        unorderedTables.forEach(tableDetail -> listData.add(tableDetail.getTable()));

        // Show on UI
        Platform.runLater(() -> {
            tablesListProperty.clear();
            tablesListProperty.set(listData);
        });
    }

    /**
     * This method is called when "Perform Data Scan" button is clicked. It tries to find the scope of Data scan. The three
     * options are:
     * a. Database level
     * b. Schema level
     * c. Table level
     * <p>
     * It identifies the scope by reading the radio buttons and validates the user selections done so far and shows
     * warning messages accordingly (For Instance, if the "Database" radio button is checked and no database is
     * selected, Data scan cannot be done !!).
     * <p>
     * Based on the selection, it finally creates the tables to be scanned list, that includes Database + Schema + Table + Column
     * details.
     *
     * @param event
     */
    @FXML
    private void performDataScan(ActionEvent event) {

    }

    @FXML
    private void showScope(ActionEvent event) {
        String db = AppData.userSelectionDB;
        String schema = AppData.userSelectionSchema;

        if (databaseBtn.isSelected()) {
            // If user clicked on the "Show Scope" button without selection a DB, show this message !
            if (db == null) {
                message.setText("Select a Database to perform Data scan @ DB level !");
                return;
            } else {
                progressIndicator.setVisible(true);
                Thread t = new Thread(new GatherDatascanScopeTask(db));
                t.start();
            }
        }

        if (schemaBtn.isSelected()) {
            if (schema == null) {
                message.setText("Select a schema in DB: " + db + " to perform Data scan @ Schema level !");
                return;
            } else {
                message.setText("Datascan @ Schema Level: " + db + "." + schema);
                ObservableList<String> listData = FXCollections.observableArrayList();

                for (TableDetail tableDetail : AppData.tables.get(db).get(schema).keySet()) {
                    TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(db).get(schema).get(tableDetail));
                    AppData.tablesTobeScanned.add(tableWrapper);
                    listData.add("[" + db + "][" + schema + "][" + tableDetail.getTable() + "]");
                }

                // Show the list
                scopeListProperty.clear();
                scopeListProperty.set(listData);
            }
        }

        if (tableBtn.isSelected()) {
            if (db == null || schema == null) {
                message.setText("Select a DB and a Schema first !");
                return;
            }

            ObservableList<Integer> selectedIndices = tablesListView.getSelectionModel().getSelectedIndices();

            if (selectedIndices.size() == 0) {
                message.setText("Select one/more tables in DB: " + db + " Schema: " + schema + " to perform Data scan @ table level !");
                return;
            } else {
                StringBuilder msg = new StringBuilder();
                ObservableList<String> listData = FXCollections.observableArrayList();
                msg.append("Datascan @ Table level: " + db + "." + schema);

                for (Integer index : selectedIndices) {
                    msg.append("\n").append("\t" + tablesListProperty.get(index));

                    TableDetail tableDetail = new TableDetail(db, schema, tablesListProperty.get(index), "BASE TABLE");
                    TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(db).get(schema).get(tableDetail));
                    AppData.tablesTobeScanned.add(tableWrapper);
                    listData.add("[" + db + "][" + schema + "][" + tableDetail.getTable() + "]");
                }

                // Show the list
                scopeListProperty.clear();
                scopeListProperty.set(listData);
            }
        }
    }

    /**
     * Prepares actual scope of Data scan. For instance, if scope is "database", it means, all tables in all schemas
     * of the selected database needs to be scanned. In order to do that, we need to gather columns of all the tables.
     * This class does that.
     * <p>
     * If the scope is "Schema" or "table", we have already gathered the list.
     */
    class GatherDatascanScopeTask extends Task<Integer> {
        private String db;

        public GatherDatascanScopeTask(String db) {
            this.db = db;
        }

        @Override
        protected Integer call() throws Exception {
            int tableCount = 0;
            // This step gather metadata of all schemas for which there is no entry in the App level map.
            for (String schema : AppData.tables.get(this.db).keySet()) {
                if (AppData.tables.get(this.db).get(schema).keySet().size() == 0) {
                    Thread t = new Thread(new FetchTablesListInASchemaTask(this.db, schema, false));
                    t.start();
                    t.join();
                }
            }

            ObservableList<String> listData = FXCollections.observableArrayList();

            for (String schema : AppData.tables.get(this.db).keySet()) {
                for (TableDetail tableDetail : AppData.tables.get(this.db).get(schema).keySet()) {
                    TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(this.db).get(schema).get(tableDetail));
                    AppData.tablesTobeScanned.add(tableWrapper);
                    tableCount++;
                    listData.add("[" + tableCount + "]. " + this.db + " : " + schema + " : " + tableDetail.getTable());
                }
            }

            Platform.runLater(() -> {
                // Show the list
                progressIndicator.setVisible(false);
                scopeListProperty.clear();
                scopeListProperty.set(listData);
            });
            return tableCount;
        }
    }
}