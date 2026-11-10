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
    private static volatile boolean schemaInitialized = false;

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found!", e);
        }

        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, "Passw0rd@123");
        } catch (SQLException e) {
            conn = DriverManager.getConnection(URL, USER, "123");
        }

        if (!schemaInitialized) {
            initSchema(conn);
        }

        return conn;
    }

    private static synchronized void initSchema(Connection conn) {
        if (schemaInitialized) return;
        try (java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("IF COL_LENGTH('products', 'price_per_kg') IS NULL ALTER TABLE products ADD price_per_kg DECIMAL(12,2) NULL;");
            stmt.execute("IF COL_LENGTH('product_variants', 'quality_grade') IS NULL ALTER TABLE product_variants ADD quality_grade VARCHAR(5) NOT NULL CONSTRAINT DF_pv_quality_grade DEFAULT ('1') WITH VALUES;");
            stmt.execute("IF OBJECT_ID('quality_grade_factors', 'U') IS NULL "
                    + "BEGIN "
                    + "CREATE TABLE quality_grade_factors (grade VARCHAR(5) NOT NULL PRIMARY KEY, label NVARCHAR(50) NOT NULL, factor DECIMAL(4,2) NOT NULL); "
                    + "INSERT INTO quality_grade_factors (grade, label, factor) VALUES "
                    + "('1', N'Loại 1 (Xuất khẩu / Tuyệt hảo)', 1.00), "
                    + "('2', N'Loại 2 (Tiêu chuẩn / Phổ thông)', 0.85), "
                    + "('3', N'Loại 3 (Tiết kiệm)', 0.70); "
                    + "END;");
            stmt.execute("IF OBJECT_ID('variant_price_history', 'U') IS NULL "
                    + "BEGIN "
                    + "CREATE TABLE variant_price_history (history_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY, variant_id INT NOT NULL, old_price DECIMAL(12,2) NOT NULL, new_price DECIMAL(12,2) NOT NULL, changed_at DATETIME2 NOT NULL DEFAULT GETDATE(), changed_by INT NULL); "
                    + "END;");
            stmt.execute("IF OBJECT_ID('commission_settlements', 'U') IS NULL "
                    + "BEGIN "
                    + "CREATE TABLE commission_settlements ("
                    + " settlement_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,"
                    + " shop_id INT NOT NULL,"
                    + " period_month CHAR(7) NOT NULL,"
                    + " amount DECIMAL(12,0) NOT NULL,"
                    + " issued_date DATE NOT NULL,"
                    + " due_date DATE NOT NULL,"
                    + " status NVARCHAR(10) NOT NULL CONSTRAINT DF_commission_settlement_status DEFAULT ('unpaid'),"
                    + " shop_locked BIT NOT NULL CONSTRAINT DF_commission_settlement_locked DEFAULT (0),"
                    + " transaction_ref NVARCHAR(100) NULL,"
                    + " paid_at DATETIME2 NULL,"
                    + " CONSTRAINT UQ_commission_settlement UNIQUE (shop_id, period_month),"
                    + " CONSTRAINT FK_commission_settlement_shop FOREIGN KEY (shop_id) REFERENCES shops(shop_id),"
                    + " CONSTRAINT CK_commission_settlement_status CHECK (status IN ('unpaid','paid'))"
                    + "); "
                    + "END;");
            stmt.execute("IF COL_LENGTH('products', 'hidden_by_commission') IS NULL "
                    + "ALTER TABLE products ADD hidden_by_commission BIT NOT NULL CONSTRAINT DF_products_hidden_by_commission DEFAULT (0);");
            schemaInitialized = true;
        } catch (Exception ignored) {
            // Không chặn ứng dụng nếu DB in-memory hoặc quyền hạn chế
        }
    }
}
