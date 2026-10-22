package com.mycompany.onlinefruitshopping.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=OnlineFruitShopping;"
            + "encrypt=true;"
            + "trustServerCertificate=true";

    private static final String USER = "sa";

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found!", e);
        }

        try {
            return DriverManager.getConnection(URL, USER, "Passw0rd@123");
        } catch (SQLException e) {
            return DriverManager.getConnection(URL, USER, "123");
        }
    }
}