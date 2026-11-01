package com.mycompany.onlinefruitshopping.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class ModuleSql {

    private ModuleSql() {
    }

    public static void prepare(Connection connection) throws SQLException {
        if (!connection.getMetaData()
                .getDatabaseProductName()
                .contains("Microsoft SQL Server")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "SET ANSI_NULLS ON; "
                    + "SET QUOTED_IDENTIFIER ON; "
                    + "SET ANSI_PADDING ON; "
                    + "SET ANSI_WARNINGS ON; "
                    + "SET CONCAT_NULL_YIELDS_NULL ON; "
                    + "SET ARITHABORT ON; "
                    + "SET NUMERIC_ROUNDABORT OFF;"
            );
        }
    }
}
