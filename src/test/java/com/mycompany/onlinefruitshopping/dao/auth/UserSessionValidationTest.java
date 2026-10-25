package com.mycompany.onlinefruitshopping.dao.auth;

import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class UserSessionValidationTest {
    private Connection anchor;
    private UserDAO dao;
    @BeforeEach void setup() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MSSQLServer";
        anchor = DriverManager.getConnection(url);
        dao = new UserDAO(() -> DriverManager.getConnection(url));
        try (Statement s = anchor.createStatement()) {
            s.execute("CREATE TABLE users(user_id INT PRIMARY KEY, status VARCHAR(30))");
            s.execute("CREATE TABLE roles(role_id INT PRIMARY KEY, role_name VARCHAR(50))");
            s.execute("CREATE TABLE user_roles(user_id INT, role_id INT)");
            s.execute("INSERT INTO users VALUES(1,'active'),(2,'locked')");
            s.execute("INSERT INTO roles VALUES(1,'Customer'),(2,'Shop_Owner'),(3,'DeliveryStaff')");
            s.execute("INSERT INTO user_roles VALUES(1,1),(1,2),(2,1)");
        }
    }
    @AfterEach void close() throws Exception { anchor.close(); }
    @Test void activeUserAndEveryAssignedRoleAreRecognized() throws Exception {
        assertTrue(dao.isActiveWithRole(1, "CUSTOMER"));
        assertTrue(dao.isActiveWithRole(1, "shop owner"));
        assertFalse(dao.isActiveWithRole(1, "DeliveryStaff"));
    }
    @Test void lockedMissingAndInvalidInputsDenied() throws Exception {
        assertFalse(dao.isActiveWithRole(2, "Customer"));
        assertFalse(dao.isActiveWithRole(999, "Customer"));
        assertFalse(dao.isActiveWithRole(0, "Customer"));
        assertFalse(dao.isActiveWithRole(-1, "Customer"));
        assertFalse(dao.isActiveWithRole(1, null));
        assertFalse(dao.isActiveWithRole(1, " "));
    }
    @Test void revokedRoleIsDetectedOnNextCheck() throws Exception {
        assertTrue(dao.isActiveWithRole(1, "Customer"));
        try (Statement s = anchor.createStatement()) { s.executeUpdate("DELETE FROM user_roles WHERE user_id=1 AND role_id=1"); }
        assertFalse(dao.isActiveWithRole(1, "Customer"));
        assertTrue(dao.isActiveWithRole(1, "ShopOwner"));
    }
}
