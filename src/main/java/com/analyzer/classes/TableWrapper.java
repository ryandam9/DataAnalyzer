package com.analyzer.classes;

import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;

import java.util.List;

public class TableWrapper {
    private TableDetail tableDetail;
    private List<ColumnDetail> columnDetailList;

    public TableWrapper(TableDetail tableDetail, List<ColumnDetail> columnDetailList) {
        this.tableDetail = tableDetail;
        this.columnDetailList = columnDetailList;
    }

    public TableDetail getTableDetail() {
        return tableDetail;
    }

    public void setTableDetail(TableDetail tableDetail) {
        this.tableDetail = tableDetail;
    }

    public List<ColumnDetail> getColumnDetailList() {
        return columnDetailList;
    }

    public void setColumnDetailList(List<ColumnDetail> columnDetailList) {
        this.columnDetailList = columnDetailList;
    }

    @Override
    public String toString() {
        return "TableWrapper{" +
                "tableDetail=" + tableDetail +
                ", columnDetailList=" + columnDetailList +
                '}';
    }
}
