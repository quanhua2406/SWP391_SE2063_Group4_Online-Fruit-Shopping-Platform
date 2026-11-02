package com.mycompany.onlinefruitshopping.utils;

import java.sql.Connection;
import java.sql.SQLException;

/** Supplies a fresh connection; the caller closes it with try-with-resources. */
@FunctionalInterface
public interface ConnectionProvider {
    Connection open() throws SQLException;
}
