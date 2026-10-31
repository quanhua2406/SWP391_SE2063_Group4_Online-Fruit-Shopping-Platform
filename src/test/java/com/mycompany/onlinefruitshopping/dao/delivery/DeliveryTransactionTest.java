package com.mycompany.onlinefruitshopping.dao.delivery;

import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Real DAO statements on an isolated H2 database. Does NOT certify SQL Server locking. */
class DeliveryTransactionTest {
    private String url;
    private DeliveryDAO deliveries;
    private AssignmentDAO assignments;

    @BeforeEach void database() throws Exception {
        url = "jdbc:h2:mem:transaction_" + UUID.randomUUID() + ";MODE=MSSQLServer;DB_CLOSE_DELAY=-1";
        deliveries = new DeliveryDAO(() -> DriverManager.getConnection(url));
        assignments = new AssignmentDAO(() -> DriverManager.getConnection(url));
        execute("CREATE TABLE users(user_id INT PRIMARY KEY, full_name VARCHAR(80), status VARCHAR(20))",
                "CREATE TABLE roles(role_id INT PRIMARY KEY, role_name VARCHAR(30))",
                "CREATE TABLE user_roles(user_id INT, role_id INT)",
                "CREATE TABLE shops(shop_id INT PRIMARY KEY, owner_id INT, status VARCHAR(20))",
                "CREATE TABLE shop_delivery_staff(shop_id INT, shipper_id INT, status VARCHAR(20))",
                "CREATE TABLE orders(order_id INT PRIMARY KEY, shop_id INT, user_id INT, order_status VARCHAR(30), payment_method VARCHAR(20), payment_status VARCHAR(20))",
                "CREATE TABLE deliveries(delivery_id INT IDENTITY PRIMARY KEY, shop_id INT, order_id INT, shipper_id INT, delivery_status VARCHAR(30), note NVARCHAR(255), pickup_time TIMESTAMP, delivered_time TIMESTAMP)",
                "CREATE TABLE notifications(notification_id INT IDENTITY PRIMARY KEY, user_id INT, title NVARCHAR(100), message NVARCHAR(255), is_read INT)",
                "INSERT INTO users VALUES(10,'Owner','active'),(20,'Shipper','active'),(21,'Other shipper','active'),(30,'Customer','active'),(11,'Other owner','active')",
                "INSERT INTO roles VALUES(1,'ShopOwner'),(2,'DeliveryStaff'),(3,'Customer')",
                "INSERT INTO user_roles VALUES(10,1),(11,1),(20,2),(21,2),(30,3)",
                "INSERT INTO shops VALUES(1,10,'active'),(2,11,'active')",
                "INSERT INTO shop_delivery_staff VALUES(1,20,'Active'),(1,21,'Active')",
                "INSERT INTO orders VALUES(100,1,30,'Shipping','COD','Unpaid'),(101,1,30,'Confirmed','COD','Unpaid'),(102,2,30,'Confirmed','COD','Unpaid')",
                "INSERT INTO deliveries(shop_id,order_id,shipper_id,delivery_status) VALUES(1,100,20,'Delivering')");
    }

    @Test void codCompletionCommitsAllEffectsAndRejectsReplay() throws Exception {
        deliveries.updateStatus(1,20,"delivered","",true);
        assertEquals("Delivered", scalar("SELECT delivery_status FROM deliveries WHERE delivery_id=1"));
        assertEquals("Completed/Paid", scalar("SELECT order_status + '/' + payment_status FROM orders WHERE order_id=100"));
        assertEquals(1, count("SELECT COUNT(*) FROM deliveries WHERE delivered_time IS NOT NULL"));
        assertEquals(3, count("SELECT COUNT(*) FROM notifications"));
        assertThrows(IllegalArgumentException.class, () -> deliveries.updateStatus(1,20,"delivered","",true));
        assertEquals(3, count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void codWithoutCollectionLeavesEverythingUnchanged() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> deliveries.updateStatus(1,20,"delivered","",false));
        assertInitialDelivery();
    }

    @Test void notificationFailureRollsBackDeliveryPaymentAndEarlierNotifications() throws Exception {
        // Owner notification succeeds; the second notification violates this test-only constraint.
        execute("ALTER TABLE notifications ADD CONSTRAINT fail_customer CHECK(user_id <> 30)");
        assertThrows(SQLException.class, () -> deliveries.updateStatus(1,20,"delivered","",true));
        assertInitialDelivery();
    }

    @Test void inactiveOrUnassignedShipperCannotChangeDelivery() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> deliveries.updateStatus(1,21,"delivered","",true));
        execute("UPDATE users SET status='locked' WHERE user_id=20");
        assertThrows(IllegalArgumentException.class, () -> deliveries.updateStatus(1,20,"delivered","",true));
        assertInitialDelivery();
    }

    @Test void successfulAssignmentIsUniqueAndRetriedAssignmentHasNoSideEffects() throws Exception {
        assignments.assign(10,101,20,"Kho A");
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101 AND shipper_id=20"));
        assertEquals(1,count("SELECT COUNT(*) FROM notifications"));
        assertThrows(IllegalArgumentException.class, () -> assignments.assign(10,101,21,"Kho A"));
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101"));
        assertEquals(1,count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void assignmentNotificationFailureRollsBackInsertedDelivery() throws Exception {
        execute("ALTER TABLE notifications ADD CONSTRAINT fail_shipper CHECK(user_id <> 20)");
        assertThrows(SQLException.class, () -> assignments.assign(10,101,20,"Kho A"));
        assertEquals(0,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101"));
        assertEquals("Confirmed",scalar("SELECT order_status FROM orders WHERE order_id=101"));
        assertEquals(0,count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void assignmentRejectsAnotherShopAndInactiveMembership() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> assignments.assign(10,102,20,"Kho A"));
        execute("UPDATE shop_delivery_staff SET status='Inactive' WHERE shipper_id=20");
        assertThrows(IllegalArgumentException.class, () -> assignments.assign(10,101,20,"Kho A"));
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries"));
        assertEquals(0,count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void acceptedAssignmentCannotThenBeRejected() throws Exception {
        assignments.assign(10,101,20,"Kho A");
        int id=count("SELECT delivery_id FROM deliveries WHERE order_id=101");
        deliveries.updateStatus(id,20,"accept","",false);
        assertThrows(IllegalArgumentException.class, () -> deliveries.updateStatus(id,20,"reject","Cannot deliver",false));
        assertEquals("Accepted",scalar("SELECT delivery_status FROM deliveries WHERE order_id=101"));
        assertEquals(2,count("SELECT COUNT(*) FROM notifications"));
    }

    private void assertInitialDelivery() throws Exception {
        assertEquals("Delivering",scalar("SELECT delivery_status FROM deliveries WHERE delivery_id=1"));
        assertEquals("Shipping/Unpaid",scalar("SELECT order_status + '/' + payment_status FROM orders WHERE order_id=100"));
        assertEquals(0,count("SELECT COUNT(*) FROM deliveries WHERE delivered_time IS NOT NULL"));
        assertEquals(0,count("SELECT COUNT(*) FROM notifications"));
    }
    private void execute(String... statements) throws SQLException {
        try(Connection c=DriverManager.getConnection(url); Statement s=c.createStatement()) {
            for(String sql:statements) s.execute(sql);
        }
    }
    private String scalar(String sql) throws SQLException {
        try(Connection c=DriverManager.getConnection(url); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            assertTrue(r.next());return r.getString(1);
        }
    }
    private int count(String sql) throws SQLException { return Integer.parseInt(scalar(sql)); }
}
