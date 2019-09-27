package com.analyzer.scanning;

import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;
import com.dbutils.masking.DataScanTask;

import java.sql.Connection;
import java.util.List;

public class DatascanTask extends DataScanTask {
    public DatascanTask(Connection connection, TableDetail tableDetail, List<ColumnDetail> columnDetails) {
        super(connection, tableDetail, columnDetails);
    }
}