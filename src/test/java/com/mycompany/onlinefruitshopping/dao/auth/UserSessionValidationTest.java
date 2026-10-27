package com.mycompany.onlinefruitshopping.dao.auth;

import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserSessionValidationTest {
    private String url;
    private UserDAO dao;

    @BeforeEach void database() throws SQLException {
        url = "jdbc:h2:mem:auth_" + UUID.randomUUID() + ";MODE=MSSQLServer;DB_CLOSE_DELAY=-1";
        dao = new UserDAO(() -> DriverManager.getConnection(url));
        try (Connection connection = DriverManager.getConnection(url); Statement sql = connection.createStatement()) {
            sql.execute("CREATE TABLE users(user_id INT PRIMARY KEY, status VARCHAR(20))");
            sql.execute("CREATE TABLE roles(role_id INT PRIMARY KEY, role_name VARCHAR(40))");
            sql.execute("CREATE TABLE user_roles(user_id INT, role_id INT)");
            sql.execute("INSERT INTO users VALUES (1, 'active'), (2, 'blocked')");
            sql.execute("INSERT INTO roles VALUES (1, 'Customer'), (2, 'Shop Owner'), (3, 'Admin')");
            sql.execute("INSERT INTO user_roles VALUES (1, 1), (1, 2), (2, 1)");
        }
    }

    @Test void selectedRoleIsCheckedInsteadOfArbitrarilyUsingTheFirstRole() throws SQLException {
        assertTrue(dao.isActiveWithRole(1, "CUSTOMER"));
        assertTrue(dao.isActiveWithRole(1, "SHOP_OWNER"));
        assertFalse(dao.isActiveWithRole(1, "Admin"));
    }

    @Test void blockedAndMissingAccountsFail() throws SQLException {
        assertFalse(dao.isActiveWithRole(2, "Customer"));
        assertFalse(dao.isActiveWithRole(999, "Customer"));
    }

    @Test void removingSelectedRoleTakesEffectOnNextValidation() throws SQLException {
        assertTrue(dao.isActiveWithRole(1, "ShopOwner"));
        try (Connection connection = DriverManager.getConnection(url); Statement sql = connection.createStatement()) {
            sql.execute("DELETE FROM user_roles WHERE user_id=1 AND role_id=2");
        }
        assertFalse(dao.isActiveWithRole(1, "ShopOwner"));
        assertTrue(dao.isActiveWithRole(1, "Customer"));
    }

    @Test void invalidInputCannotBecomeAPermissiveQuery() throws SQLException {
        assertFalse(dao.isActiveWithRole(0, "Customer"));
        assertFalse(dao.isActiveWithRole(1, null));
        assertFalse(dao.isActiveWithRole(1, "Customer' OR '1'='1"));
    }
}
