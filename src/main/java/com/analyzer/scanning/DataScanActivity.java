package com.analyzer.scanning;

import com.analyzer.classes.AppData;
import com.analyzer.classes.TableToAnalyze;
import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;

import java.sql.Connection;
import java.util.List;

public class DataScanActivity extends com.dbutils.masking.DataScanTask {
    private TableToAnalyze tableToAnalyze;

    public DataScanActivity(int taskId, Connection connection, TableDetail tableDetail, List<ColumnDetail> columnDetails, TableToAnalyze tableToAnalyze) {
        super(taskId, connection, tableDetail, columnDetails, AppData.dbSelection, AppData.prefRecordsToScan, "Y", 10000);
        this.tableToAnalyze = tableToAnalyze;
    }

//    public void updateProgressCount() {
//        Platform.runLater(() -> tile.getProcessedSoFar().setText("Hello"));
//    }
}