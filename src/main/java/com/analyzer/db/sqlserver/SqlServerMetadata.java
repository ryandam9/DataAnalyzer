package com.analyzer.db.sqlserver;

import com.analyzer.DBConnections;
import com.analyzer.Utils;
import com.analyzer.db.TableDetail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.analyzer.Utils.logStackTrace;

public class SqlServerMetadata {
    private static Map<String, String> sqls;

    static {
        try {
            sqls = Utils.fetchQueries("sqlserver");
        } catch (Exception ex) {
            logStackTrace(ex);
        }
    }

    public List<String> getAllDatabases(Connection connection, String dbServer) {
        String query = sqls.get("get_db_names");
        ResultSet resultSet = null;
        List<String> databases = new ArrayList<>();

        try {
            resultSet = DBConnections.execReadOnlyQuery(connection, query);

            while (resultSet.next()) {
                String database = resultSet.getString(1);
                databases.add(database);
            }
        } catch (Exception e) {
            logStackTrace(e);
        }

        return databases;
    }
}
