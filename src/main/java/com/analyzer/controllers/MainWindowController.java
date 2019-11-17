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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.analyzer.AppLogger.logger;
import static com.analyzer.Utils.logStackTrace;

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
    private TabPane tabPane;

    @FXML
    private Tab settingsTab;

    @FXML
    private Tab dashboardTab;

    @FXML
    private Tab reportTab;

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
    private VBox buttonBar;

    @FXML
    private Button showScopeBtn;

    @FXML
    private Button datascanBtn;

    private ListProperty<String> databasesListProperty = new SimpleListProperty<>();
    private ListView<String> databasesListView;

    private ListProperty<String> schemasListProperty = new SimpleListProperty<>();
    private ListView<String> schemasListView;

    private ListProperty<String> tablesListProperty = new SimpleListProperty<>();
    private ListView<String> tablesListView;

    private ListProperty<String> scopeListProperty = new SimpleListProperty<>();
    private ListView<String> scopeListView;

    private ProgressIndicator progressIndicator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        prefRecordsToScan.setText("100000");
        prefParallelTaskCount.setText("10");

        HBox.setHgrow(msgBar, Priority.ALWAYS);            // Only contains a Label to show status messages.

        // Create a Progress Indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(70, 70);
        progressIndicator.setVisible(false);
        buttonBar.getChildren().add(progressIndicator);

        // Don't enable the "Perform Data Scan" button initially. Only when a DB/Schema/table(s) is selected and
        // we have gathered all tables metadata, enable this button.
        datascanBtn.setDisable(true);

        // This step gathers the names of databases and all the schemas in those databases, and stores in Application level map.
        // There could be thousands of tables in a Schema ! It is not a good idea to gather metadata of all those
        // tables at this point.
        fetchDBMetadata();

        // Create a list view to show databases and bind its Items property with a List Property.
        databasesListView = new ListView<>();
        databasesListView.itemsProperty().bind(databasesListProperty);
        databasesListView.prefHeightProperty().bind(databasesVBox.heightProperty());
        databasesVBox.getChildren().add(databasesListView);

        // Only one database can be selected for scanning at a time
        databasesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // When a database is clicked, its Schemas should be rendered in the Schema window.
        databasesListView.setOnMouseClicked(event -> {
            AppData.userSelectionDB = databasesListView.getSelectionModel().getSelectedItem();
            AppData.userSelectionSchema = null;
            AppData.userSelectionTables.clear();

            // When a Database selection is made, show schemas only. Clear any previously shown tables !
            tablesListProperty.clear();
            showSchemas();
        });

        // Schemas
        schemasListView = new ListView<>();
        schemasListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        schemasListView.itemsProperty().bind(schemasListProperty);
        schemasListView.prefHeightProperty().bind(schemasVBox.heightProperty());
        schemasVBox.getChildren().add(schemasListView);

        // When a schema is clicked, its tables should be rendered in the tables window.
        schemasListView.setOnMouseClicked(event -> {
            AppData.userSelectionSchema = schemasListView.getSelectionModel().getSelectedItem();
            AppData.userSelectionTables.clear();
            showTables();
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

    /**
     * Reads Database names from the Application level Map, sorts them and updates the backend of Database Listview
     */
    private void showDatabases() {
        List<String> databases = new ArrayList<>(AppData.tables.keySet());  // Get list of databases
        databases.sort(Comparator.comparing(String::toLowerCase));

        ObservableList<String> listData = FXCollections.observableArrayList();
        listData.addAll(databases);

        databasesListProperty.clear();
        databasesListProperty.set(listData);
    }

    /**
     * This is called when the User clicks on any of Database list view. Shows all the schemas in the Selected DB.
     */
    private void showSchemas() {
        String db = AppData.userSelectionDB;
        ObservableList<String> listData = FXCollections.observableArrayList();
        listData.addAll(AppData.tables.get(db).keySet());     // Get the schema names
        listData.sort(Comparator.comparing(String::toLowerCase));

        schemasListProperty.clear();
        schemasListProperty.set(listData);
    }

    /**
     *
     */
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
        Thread t = new Thread(new FetchSchemaListInADatabaseTask());
        t.start();
    }

    class FetchSchemaListInADatabaseTask extends Task<Integer> {
        @Override
        protected Integer call() throws Exception {
            List<String> databases = new ArrayList<>();
            List<String> schemas = new ArrayList<>();
            Platform.runLater(() -> progressIndicator.setVisible(true));
            int noSchemas = 0;

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
                        Platform.runLater(() -> message.setText("Gathering schema names in DB: " + db));
                        try {
                            // Make an entry for the Database
                            AppData.tables.put(db, new HashMap<>());
                            schemas = OracleMetadata.getAllSchemas(AppData.initialConnection, db);

                            // Update the application level dictionary with all the Schemas.
                            for (String schema : schemas) {
                                AppData.tables.get(db).put(schema, new HashMap<>());
                                noSchemas++;
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
                        Platform.runLater(() -> message.setText("Gathering schema names in DB: " + db));
                        try {
                            // Update application level dictionary for later use
                            AppData.tables.put(db, new HashMap<>());
                            schemas = SqlServerMetadata.getAllSchemas(AppData.initialConnection, db);

                            // Update the application level dictionary with all the Schemas.
                            for (String schema : schemas) {
                                AppData.tables.get(db).put(schema, new HashMap<>());
                                noSchemas++;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
            }
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                message.setText("");
            });

            showDatabases();
            return noSchemas;
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
            Platform.runLater(() -> progressIndicator.setVisible(true));
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

            return tableCount;
        }

        @Override
        protected void done() {
            super.done();
            Platform.runLater(() -> progressIndicator.setVisible(false));
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
        unorderedTables.sort(Comparator.comparing(o -> o.getTable().toLowerCase()));

        // Type of table entry is "TableDetail". Just get the table name as a String
        ObservableList<String> listData = FXCollections.observableArrayList();
        unorderedTables.forEach(tableDetail -> listData.add(tableDetail.getTable()));

        // Show on UI
        Platform.runLater(() -> {
            tablesListProperty.clear();
            tablesListProperty.set(listData);
        });
    }

    @FXML
    private void performDataScan(ActionEvent event) {
        ResultsController resultsController = null;
        DashboardController dashboardController = null;

        try {
            URL url = new File("resources/ui/results.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            BorderPane borderPane = (BorderPane) loader.load();
            borderPane.prefWidthProperty().bind(databaseBtn.getScene().widthProperty());
            borderPane.prefHeightProperty().bind(databaseBtn.getScene().heightProperty());
            reportTab.setContent(borderPane);

            resultsController = loader.getController();
        } catch (Exception ex) {
            logStackTrace(ex);
        }

        try {
            URL url = new File("resources/ui/scan_dashboard.fxml").toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            BorderPane borderPane = (BorderPane) loader.load();
            dashboardController = loader.getController();

            borderPane.prefWidthProperty().bind(databaseBtn.getScene().widthProperty());
            borderPane.prefHeightProperty().bind(databaseBtn.getScene().heightProperty());
            dashboardTab.setContent(borderPane);
            tabPane.getSelectionModel().select(dashboardTab);

            dashboardController.setResultsController(resultsController);
        } catch (Exception ex) {
            logStackTrace(ex);
        }
    }

    /**
     * This method is called when "Show Scope" button is clicked. It tries to find the scope of Data scan. The three
     * options are:
     * <pre>
     *      a. Database level
     *      b. Schema level
     *      c. Table level
     * </pre>
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
    private void showScope(ActionEvent event) {
        message.setText("");            // Clear previously shown messages

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
                int tableCount = 0;

                for (TableDetail tableDetail : AppData.tables.get(db).get(schema).keySet()) {
                    TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(db).get(schema).get(tableDetail));
                    AppData.tablesTobeScanned.add(tableWrapper);
                    tableCount++;
                    listData.add("[" + tableCount + "] " + db + " : " + schema + " : " + tableDetail.getTable());
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
                ObservableList<String> listData = FXCollections.observableArrayList();
                int tableCount = 0;

                for (Integer index : selectedIndices) {
                    TableDetail tableDetail = new TableDetail(db, schema, tablesListProperty.get(index), "BASE TABLE");
                    TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(db).get(schema).get(tableDetail));
                    AppData.tablesTobeScanned.add(tableWrapper);
                    tableCount++;
                    listData.add("[" + tableCount + "] " + db + " : " + schema + " : " + tableDetail.getTable());
                }

                // Show the list
                scopeListProperty.clear();
                scopeListProperty.set(listData);
            }
        }

        datascanBtn.setDisable(false);
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
                    Platform.runLater(() -> message.setText("Gather table list for schema: " + this.db + "." + schema));
                    Thread t = new Thread(new FetchTablesListInASchemaTask(this.db, schema, false));
                    t.start();
                    t.join();
                }
            }

            ObservableList<String> listData = FXCollections.observableArrayList();

            List<String> schemas = new ArrayList<>(AppData.tables.get(this.db).keySet());
            schemas.sort(Comparator.comparing(o -> o.toLowerCase()));

            for (String schema : schemas) {
                List<TableDetail> tables = new ArrayList<>(AppData.tables.get(this.db).get(schema).keySet());
                tables.sort(Comparator.comparing(o -> o.getTable().toLowerCase()));

                for (TableDetail tableDetail : tables) {
                    TableWrapper tableWrapper = new TableWrapper(tableDetail, AppData.tables.get(this.db).get(schema).get(tableDetail));
                    AppData.tablesTobeScanned.add(tableWrapper);
                    tableCount++;
                    listData.add("[" + tableCount + "] " + this.db + " : " + schema + " : " + tableDetail.getTable());
                }
            }

            Platform.runLater(() -> {
                // Show the list
                progressIndicator.setVisible(false);
                message.setText("");
                scopeListProperty.clear();
                scopeListProperty.set(listData);
                datascanBtn.setDisable(false);
            });
            return tableCount;
        }
    }
}