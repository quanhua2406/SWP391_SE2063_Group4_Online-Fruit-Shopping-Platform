package com.mycompany.onlinefruitshopping.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=OnlineFruitShopping;encrypt=true;trustServerCertificate=true";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found!", e);
        }
        String url = setting("fruitshop.db.url", "FRUITSHOP_DB_URL", DEFAULT_URL);
        String user = setting("fruitshop.db.user", "FRUITSHOP_DB_USER", "sa");
        String password = setting("fruitshop.db.password", "FRUITSHOP_DB_PASSWORD", null);
        if (password == null || password.isEmpty()) {
            throw new SQLException("Set FRUITSHOP_DB_PASSWORD or -Dfruitshop.db.password before starting Tomcat.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String property, String environment, String fallback) {
        String value = System.getProperty(property);
        if (value == null) value = System.getenv(environment);
        return value == null ? fallback : value;
    }
}
