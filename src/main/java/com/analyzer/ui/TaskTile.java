package com.analyzer.ui;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

public class TaskTile extends VBox {
    private Label taskName;
    private Label totalRecordsToScan;
    private Label processedSoFar;
    private ProgressIndicator progressIndicator;

    public TaskTile(String taskName, String totalRecordsToScan, String processedSoFar) {
        this.getStyleClass().add("tile-box");

        this.taskName = createLabel(taskName);
        this.totalRecordsToScan = createLabel(totalRecordsToScan);
        this.processedSoFar = createLabel(processedSoFar);

        // Progress Indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("progress-indicator");
        progressIndicator.setVisible(false);

        getChildren().addAll(this.taskName, this.totalRecordsToScan, this.processedSoFar, this.progressIndicator);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("tile-label");
        return label;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProcessedSoFar(String processedSoFar) {
        this.processedSoFar = createLabel(processedSoFar);
    }

    public void setProgressIndicator(boolean visibility) {
        this.progressIndicator.setVisible(visibility);
    }

    public Label getTaskName() {
        return taskName;
    }

    public void setTaskName(Label taskName) {
        this.taskName = taskName;
    }

    public Label getTotalRecordsToScan() {
        return totalRecordsToScan;
    }

    public void setTotalRecordsToScan(Label totalRecordsToScan) {
        this.totalRecordsToScan = totalRecordsToScan;
    }

    public Label getProcessedSoFar() {
        return processedSoFar;
    }

    public void setProcessedSoFar(Label processedSoFar) {
        this.processedSoFar = processedSoFar;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }
}