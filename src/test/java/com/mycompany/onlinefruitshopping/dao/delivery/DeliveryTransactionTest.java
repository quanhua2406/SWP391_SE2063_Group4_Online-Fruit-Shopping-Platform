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
        var db = new com.mycompany.onlinefruitshopping.support.ModuleDatabase();
        url = db.url;
        deliveries = new DeliveryDAO(db::open);
        assignments = new AssignmentDAO(db::open);
    }

    @Test void codCompletionCommitsAllEffectsAndRejectsReplay() throws Exception {
        deliveries.updateStatus(1,20,"delivered","",true);
        assertEquals("Delivered", scalar("SELECT delivery_status FROM delivery_attempts WHERE attempt_id=1"));
        assertEquals("delivered/paid", scalar("SELECT o.status + '/' + p.status FROM orders o JOIN payments p ON p.order_id=o.order_id WHERE o.order_id=100"));
        assertEquals(1, count("SELECT COUNT(*) FROM delivery_attempts WHERE delivered_time IS NOT NULL"));
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
        assignments.assign(10,101,20,"Shop pickup");
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101 AND staff_id=20"));
        assertEquals(1,count("SELECT COUNT(*) FROM notifications"));
        assertThrows(IllegalArgumentException.class, () -> assignments.assign(10,101,21,"Shop pickup"));
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101"));
        assertEquals(1,count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void assignmentNotificationFailureRollsBackInsertedDelivery() throws Exception {
        execute("ALTER TABLE notifications ADD CONSTRAINT fail_shipper CHECK(user_id <> 20)");
        assertThrows(SQLException.class, () -> assignments.assign(10,101,20,"Shop pickup"));
        assertEquals(0,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101"));
        assertEquals("preparing",scalar("SELECT status FROM orders WHERE order_id=101"));
        assertEquals(0,count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void assignmentRejectsAnotherShopAndInactiveMembership() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> assignments.assign(10,102,20,"Shop pickup"));
        execute("UPDATE shop_delivery_staff SET status='Inactive' WHERE shipper_id=20");
        assertThrows(IllegalArgumentException.class, () -> assignments.assign(10,101,20,"Shop pickup"));
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries"));
        assertEquals(0,count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void acceptedAssignmentCannotThenBeRejected() throws Exception {
        assignments.assign(10,101,20,"Shop pickup");
        int id=count("SELECT MAX(attempt_id) FROM delivery_attempts");
        deliveries.updateStatus(id,20,"accept","",false);
        assertThrows(IllegalArgumentException.class, () -> deliveries.updateStatus(id,20,"reject","Cannot deliver",false));
        assertEquals("Accepted",scalar("SELECT a.delivery_status FROM delivery_attempts a JOIN deliveries d ON d.delivery_id=a.delivery_id WHERE d.order_id=101"));
        assertEquals(2,count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void rejectedAttemptClosesAndReassignmentKeepsOneBaseDelivery() throws Exception {
        assignments.assign(10,101,20,"Shop pickup");
        int old=count("SELECT MAX(attempt_id) FROM delivery_attempts");
        deliveries.updateStatus(old,20,"reject","Vehicle broken",false);
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101 AND staff_id IS NULL"));
        assignments.assign(10,101,21,"New pickup");
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101"));
        assertEquals(2,count("SELECT COUNT(*) FROM delivery_attempts a JOIN deliveries d ON d.delivery_id=a.delivery_id WHERE d.order_id=101"));
        assertThrows(IllegalArgumentException.class,()->deliveries.updateStatus(old,20,"accept","",false));
    }
    @Test void assignmentCapturesAddressAndAvailableListUsesCanonicalColumns() throws Exception {
        assertEquals(1,assignments.findAvailableOrders(10).size());
        assertEquals(2,assignments.findActiveStaff(10).size());
        assignments.assign(10,101,20,"Shop pickup");
        execute("UPDATE user_addresses SET address_line='Changed' WHERE address_id=1");
        assertTrue(scalar("SELECT shipping_address FROM delivery_attempts WHERE attempt_id=2").contains("1 Test street"));
        assertTrue(assignments.findAvailableOrders(10).isEmpty());
    }
    @Test void onlinePendingOrWrongAmountCannotComplete() throws Exception {
        execute("UPDATE payments SET method='vnpay' WHERE order_id=100");
        assertThrows(IllegalArgumentException.class,()->deliveries.updateStatus(1,20,"delivered","",true));
        execute("UPDATE payments SET status='paid',amount=1 WHERE order_id=100");
        assertThrows(IllegalArgumentException.class,()->deliveries.updateStatus(1,20,"delivered","",true));
        assertEquals("Delivering",scalar("SELECT delivery_status FROM delivery_attempts WHERE attempt_id=1"));
    }

    @Test void pickupStartFailureAndRetrySynchronizeCanonicalStates() throws Exception {
        assignments.assign(10,101,20,"Shop pickup");
        int id=count("SELECT MAX(attempt_id) FROM delivery_attempts");
        deliveries.updateStatus(id,20,"accept","",false);
        deliveries.updateStatus(id,20,"pickup","",false);
        assertEquals("shipping",scalar("SELECT status FROM orders WHERE order_id=101"));
        assertEquals("picked_up",scalar("SELECT status FROM deliveries WHERE order_id=101"));
        deliveries.updateStatus(id,20,"start","",false);
        deliveries.updateStatus(id,20,"failed","Customer unavailable",false);
        assertEquals("failed",scalar("SELECT status FROM orders WHERE order_id=101"));
        assertEquals(1,count("SELECT COUNT(*) FROM delivery_attempts WHERE attempt_id="+id+" AND pickup_time IS NOT NULL AND closed_at IS NOT NULL"));
        assignments.assign(10,101,21,"Retry pickup");
        assertEquals("preparing",scalar("SELECT status FROM orders WHERE order_id=101"));
        assertEquals(1,count("SELECT COUNT(*) FROM deliveries WHERE order_id=101"));
    }

    private void assertInitialDelivery() throws Exception {
        assertEquals("Delivering",scalar("SELECT delivery_status FROM delivery_attempts WHERE attempt_id=1"));
        assertEquals("shipping/pending",scalar("SELECT o.status + '/' + p.status FROM orders o JOIN payments p ON p.order_id=o.order_id WHERE o.order_id=100"));
        assertEquals(0,count("SELECT COUNT(*) FROM delivery_attempts WHERE delivered_time IS NOT NULL"));
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
