package com.mycompany.onlinefruitshopping.utils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        Properties config = loadConfiguration();
        String url = requiredText(config, "db.url");
        String user = requiredText(config, "db.user");
        String password = config.getProperty("db.password");
        if (password == null || password.isEmpty()) {
            throw new SQLException("Thiếu cấu hình db.password.");
        }
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy SQL Server JDBC Driver.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    static Properties loadConfiguration() throws SQLException {
        String configuredPath = System.getProperty("fruitshop.db.config");
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv("FRUITSHOP_DB_CONFIG");
        }
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new SQLException("Chưa khai báo fruitshop.db.config hoặc FRUITSHOP_DB_CONFIG.");
        }
        Properties config = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of(configuredPath), StandardCharsets.UTF_8)) {
            config.load(reader);
            return config;
        } catch (IOException | IllegalArgumentException e) {
            throw new SQLException("Không đọc được file cấu hình database.", e);
        }
    }

    private static String requiredText(Properties config, String key) throws SQLException {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) throw new SQLException("Thiếu cấu hình " + key + ".");
        return value.trim();
    }
}
