package com.analyzer.classes;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

public class TableToAnalyze {
    private String sno;
    private String db;
    private String schema;
    private String table;
    private String recordsToScan;
    private String soFarScanned;
    private Node status;

    public TableToAnalyze(String sno, String db, String schema, String table, String recordsToScan, String soFarScanned, String status) {
        this.sno = sno;
        this.db = db;
        this.schema = schema;
        this.table = table;
        this.recordsToScan = recordsToScan;
        this.soFarScanned = soFarScanned;

        HBox hBox = new HBox(10);

        // Create a Progress Indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(15, 15);
        progressIndicator.setVisible(false);

        Label statusMsg = new Label(status);
        statusMsg.setStyle("-fx-background-color: #7D2996; -fx-text-fill: white");

        hBox.getChildren().addAll(progressIndicator, statusMsg);

        this.status = hBox;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getRecordsToScan() {
        return recordsToScan;
    }

    public void setRecordsToScan(String recordsToScan) {
        this.recordsToScan = recordsToScan;
    }

    public String getSoFarScanned() {
        return soFarScanned;
    }

    public void setSoFarScanned(String soFarScanned) {
        this.soFarScanned = soFarScanned;
    }

    public Node getStatus() {
        return status;
    }

    public void setStatus(Node status) {
        this.status = status;
    }
}
