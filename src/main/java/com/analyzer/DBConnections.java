package com.analyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.analyzer.Utils.logStackTrace;

public class DBConnections {
    public static Connection getOracleConnection(String user, String password, String host, String db, String port) throws Exception {
        String connectionUrl = "jdbc:oracle:thin:@" + host + ":" + port + "/" + db;
        return getDbConnection(connectionUrl);
    }

    public static Connection getSqlServerConnection(String user, String dbServer, String port) throws Exception {
        String connectionUrl =
                "jdbc:sqlserver://" + dbServer + ":" + port + ";";

        connectionUrl += "integratedSecurity=true;";
        connectionUrl += "user=" + user + ";";

        return getDbConnection(connectionUrl);
    }

    private static Connection getDbConnection(String connectionUrl) throws Exception {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(connectionUrl);
        } catch (Exception ex) {
            logStackTrace(ex);
            throw new IllegalArgumentException(ex.getMessage());
        }

        return connection;
    }

    /**
     * Executes a SELECT Query
     *
     * @param connection DB Connection object
     * @param query Query to be executed
     * @return Result set
     * @throws Exception
     */
    public static ResultSet execReadOnlyQuery(Connection connection, String query) throws Exception {
        ResultSet resultSet = null;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
        } catch (Exception ex) {
            throw ex;
        }

        return resultSet;
    }
}