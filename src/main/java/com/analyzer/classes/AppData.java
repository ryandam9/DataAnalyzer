package com.analyzer.classes;

import com.dbutils.common.ColumnDetail;
import com.dbutils.common.TableDetail;

import java.util.List;
import java.util.Map;

public class AppData {
    public static Map<String, Map<String, Map<TableDetail, List<ColumnDetail>>>> tables;
    private static String user;
    private static String password;
    private static String host;
    private static String db;
    private static String port;
    private static List<TableWrapper> tablesTobeScanned;
    private static int noThreads;

    public static Map<String, Map<String, Map<TableDetail, List<ColumnDetail>>>> getTables() {
        return tables;
    }

    public static void setTables(Map<String, Map<String, Map<TableDetail, List<ColumnDetail>>>> tables) {
        AppData.tables = tables;
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        AppData.user = user;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        AppData.password = password;
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        AppData.host = host;
    }

    public static String getDb() {
        return db;
    }

    public static void setDb(String db) {
        AppData.db = db;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        AppData.port = port;
    }

    public static List<TableWrapper> getTablesTobeScanned() {
        return tablesTobeScanned;
    }

    public static void setTablesTobeScanned(List<TableWrapper> tablesTobeScanned) {
        AppData.tablesTobeScanned = tablesTobeScanned;
    }

    public static int getNoThreads() {
        return noThreads;
    }

    public static void setNoThreads(int noThreads) {
        AppData.noThreads = noThreads;
    }
}
