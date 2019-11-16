package com.analyzer.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TaskTile extends HBox {
    private Label taskId;
    private Label taskName;
    private Label totalRecordsToScan;
    private Label processedSoFar;
    private ProgressIndicator progressIndicator;
    private VBox vBox;

    public TaskTile(String taskId, String taskName, String totalRecordsToScan, String processedSoFar) {
        this.getStyleClass().add("tile-box");

        this.taskId = createLabel(taskId);
        this.taskName = createLabel(taskName);
        this.totalRecordsToScan = createLabel(totalRecordsToScan);
        this.processedSoFar = createLabel(processedSoFar);

        // Progress Indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("progress-indicator");
        progressIndicator.setVisible(false);

        vBox = new VBox();
        vBox.setPrefSize(100, 30);
        vBox.setMaxSize(100, 30);
        vBox.setMinSize(100, 30);
        vBox.setPadding(new Insets(5, 5, 5, 5));

        getChildren().addAll(this.taskName, this.totalRecordsToScan, this.processedSoFar, this.progressIndicator, this.vBox);
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

    public Label getTaskId() {
        return taskId;
    }

    public void setTaskId(Label taskId) {
        this.taskId = taskId;
    }

    public VBox getvBox() {
        return vBox;
    }

    public void setvBox(VBox vBox) {
        this.vBox = vBox;
    }
}