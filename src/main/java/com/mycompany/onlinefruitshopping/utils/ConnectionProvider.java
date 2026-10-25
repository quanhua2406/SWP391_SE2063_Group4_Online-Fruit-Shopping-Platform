package com.mycompany.onlinefruitshopping.utils;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionProvider {
    Connection open() throws SQLException;
}