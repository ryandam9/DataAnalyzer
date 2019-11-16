package com.analyzer.scanning;

import com.analyzer.classes.AppData;
import com.analyzer.ui.TaskTile;
import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;
import javafx.application.Platform;

import java.sql.Connection;
import java.util.List;

public class DataScanActivity extends com.dbutils.masking.DataScanTask {
    private TaskTile tile;

    public DataScanActivity(int taskId, Connection connection, TableDetail tableDetail, List<ColumnDetail> columnDetails, TaskTile tile) {
        super(taskId, connection, tableDetail, columnDetails, AppData.dbSelection, AppData.prefRecordsToScan, "Y", 10000);
        this.tile = tile;
    }

    public void updateProgressCount() {
        Platform.runLater(() -> tile.getProcessedSoFar().setText("Hello"));
    }
}