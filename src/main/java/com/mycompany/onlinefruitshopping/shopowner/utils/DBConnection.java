package com.mycompany.onlinefruitshopping.shopowner.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility class.
 *
 * <p>Cau hinh duoc doc theo thu tu uu tien:
 * <ol>
 *   <li> Bien moi truong: {@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD}.</li>
 *   <li> File {@code db.properties} tren classpath (xem {@code db.properties.example}).</li>
 *   <li> Gia tri mac dinh localhost cho {@code DB_URL} va {@code DB_USER}.</li>
 * </ol>
 *
 * <p>Mat khau khong duoc phep de trong source. Tao file
 * {@code src/main/resources/db.properties} (da bi .gitignore) hoac dat bien
 * {@code DB_PASSWORD} truoc khi chay.
 */
public class DBConnection {

    private static final String DEFAULT_DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=FruitShoppingShopOwnerDB;encrypt=true;trustServerCertificate=true";
    private static final String DEFAULT_DB_USER = "sa";

    private static final Properties FILE_CONFIG = loadFileConfig();

    private DBConnection() {
    }

    private static Properties loadFileConfig() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.err.println("DBConnection: khong doc duoc db.properties - " + e.getMessage());
        }
        return props;
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String fileValue(String key) {
        String value = FILE_CONFIG.getProperty(key);
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String resolve(String envName, String fileKey, String fallback) {
        String value = env(envName);
        if (value != null) {
            return value;
        }
        value = fileValue(fileKey);
        return value != null ? value : fallback;
    }

    private static String resolvePassword() {
        // Mat khau co the intentionally rong, nen chi bo qua khi khoa khong ton tai.
        String value = System.getenv("DB_PASSWORD");
        if (value != null) {
            return value;
        }
        return FILE_CONFIG.getProperty("db.password");
    }

    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if configuration is missing or connection fails
     */
    public static Connection getConnection() throws SQLException {
        String password = resolvePassword();
        if (password == null) {
            throw new SQLException("Chua cau hinh mat khau ket noi database. "
                    + "Dat bien moi truong DB_PASSWORD hoac tao src/main/resources/db.properties "
                    + "theo mau db.properties.example.");
        }
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(
                    resolve("DB_URL", "db.url", DEFAULT_DB_URL),
                    resolve("DB_USER", "db.user", DEFAULT_DB_USER),
                    password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found", e);
        }
    }

    /**
     * Close database connection
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
