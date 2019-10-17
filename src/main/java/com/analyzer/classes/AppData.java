package com.analyzer.classes;

import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppData {
    public static String dbSelection;
    public static String user;
    public static String password;
    public static String host;
    public static String db;
    public static String port;
    public static Connection initialConnection;
    public static int prefParallelTaskCount;
    public static int prefRecordsToScan;

    // Constants
    public static final String ORACLE = "Oracle";
    public static final String SQL_SERVER = "SQL Server";
    public static final String MySQL = "MySQL";
    public static final String DYNAMO_DB = "Dynamo DB";
    public static final String DB2 = "DB2";

    public static Map<String, Map<String, Map<TableDetail, List<ColumnDetail>>>> tables = new HashMap<>();
    public static List<TableWrapper> tablesTobeScanned = new ArrayList<>();
    public static int noThreads = 10;

    public static String userSelectionDB;
    public static String userSelectionSchema;
    public static List<String> userSelectionTables = new ArrayList<>();
}
