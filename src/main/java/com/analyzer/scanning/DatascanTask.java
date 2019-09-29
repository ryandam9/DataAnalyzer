package com.analyzer.scanning;

import com.analyzer.ui.TaskTile;
import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;
import com.dbutils.masking.DataScanTask;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.util.List;

public class DatascanTask extends DataScanTask {
    private TaskTile tile;

    public DatascanTask(int taskId, Connection connection, TableDetail tableDetail, List<ColumnDetail> columnDetails, TaskTile tile) {
        super(taskId, connection, tableDetail, columnDetails);
        this.tile = tile;
    }

    public void updateProgressCount() {
        Platform.runLater(() -> tile.setProcessedSoFar(new Label("Hello")));
    }
}