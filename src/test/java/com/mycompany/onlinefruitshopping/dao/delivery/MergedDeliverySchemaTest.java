package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.support.ModuleDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MergedDeliverySchemaTest {
    ModuleDatabase db;
    AssignmentDAO assignments;
    DeliveryDAO deliveries;

    @BeforeEach void setup() throws Exception {
        db=new ModuleDatabase();
        assignments=new AssignmentDAO(db::open);
        deliveries=new DeliveryDAO(db::open);
    }

    @Test void assignmentUsesOrderSnapshotAndSavedShopAddress() throws Exception {
        db.exec("UPDATE shops SET pickup_address='Saved shop address' WHERE shop_id=1",
                "UPDATE orders SET receiver_name='Snapshot receiver',receiver_phone='09999',shipping_address='Snapshot destination' WHERE order_id=101");
        assignments.assign(10,101,20,"");
        assertEquals("Snapshot destination",db.scalar("SELECT shipping_address FROM delivery_attempts WHERE attempt_id=2"));
        assertEquals("Snapshot receiver",db.scalar("SELECT receiver_name FROM delivery_attempts WHERE attempt_id=2"));
        assertEquals("Saved shop address",db.scalar("SELECT pickup_address FROM delivery_attempts WHERE attempt_id=2"));
        assertEquals(1,db.count("SELECT COUNT(*) FROM deliveries WHERE order_id=101 AND shop_id=1 AND staff_id=20 AND shipper_id=20 AND status='assigned' AND delivery_status='Assigned'"));
    }

    @Test void acceptsRejectsAndCompletionWriteCanonicalColumns() throws Exception {
        assignments.assign(10,101,20,"Pickup");
        deliveries.updateStatus(2,20,"reject","Vehicle unavailable",false);
        assertEquals("rejected",db.scalar("SELECT status FROM deliveries WHERE order_id=101"));
        assertEquals(1,db.count("SELECT COUNT(*) FROM deliveries WHERE order_id=101 AND staff_id IS NULL AND shipper_id IS NULL"));
        assignments.assign(10,101,21,"Pickup");
        deliveries.updateStatus(3,21,"accept","",false);
        assertEquals("accepted",db.scalar("SELECT status FROM deliveries WHERE order_id=101"));
        deliveries.updateStatus(3,21,"pickup","",false);
        assertEquals(1,db.count("SELECT COUNT(*) FROM deliveries WHERE order_id=101 AND pickup_time IS NOT NULL"));
        deliveries.updateStatus(3,21,"start","",false);
        deliveries.updateStatus(3,21,"delivered","",true);
        assertEquals(1,db.count("SELECT COUNT(*) FROM deliveries WHERE order_id=101 AND status='delivered' AND delivery_status='Delivered' AND delivered_time IS NOT NULL"));
        assertEquals(1,db.count("SELECT COUNT(*) FROM orders WHERE order_id=101 AND status='delivered' AND order_status='delivered' AND payment_status='paid' AND payment_method='cod'"));
    }

    @Test void multipleOldDeliveryRowsDoNotDuplicateOrChooseArbitraryDelivery() throws Exception {
        db.exec("INSERT INTO deliveries(order_id,staff_id,status) VALUES(101,21,'failed'),(101,21,'rejected')");
        assertEquals(1,assignments.findAvailableOrders(10).size());
        assignments.assign(10,101,20,"Pickup");
        assertEquals("failed",db.scalar("SELECT status FROM deliveries WHERE delivery_id=2"));
        assertEquals("assigned",db.scalar("SELECT status FROM deliveries WHERE delivery_id=3"));
        assertEquals(1,db.count("SELECT COUNT(*) FROM delivery_attempts WHERE delivery_id=3"));
        assertTrue(assignments.findAvailableOrders(10).isEmpty());
    }

    @Test void olderActiveDeliveryCannotBeHiddenByNewerFailedRow() throws Exception {
        db.exec("INSERT INTO deliveries(order_id,staff_id,status) VALUES(101,21,'assigned'),(101,21,'failed')");
        assertTrue(assignments.findAvailableOrders(10).isEmpty());
        assertThrows(IllegalArgumentException.class,()->assignments.assign(10,101,20,"Pickup"));
        assertEquals(0,db.count("SELECT COUNT(*) FROM notifications"));
    }

    @Test void threeCanonicalDeliveriesFillCapacityEvenWithoutAttemptRows() throws Exception {
        db.exec("INSERT INTO deliveries(order_id,shipper_id,status) VALUES(102,20,'assigned'),(103,20,'accepted')");
        assertFalse(assignments.findActiveStaff(10).stream().anyMatch(s->s.getId()==20));
        assertThrows(IllegalArgumentException.class,()->assignments.assign(10,101,20,"Pickup"));
        assertEquals(3,new ShipperTeamDAO(db::open).getMyMembership(20).getOpenDeliveryCount());
    }

    @Test void pickupAddressHonorsMergedLengthAndRequiredFallback() throws Exception {
        assertThrows(IllegalArgumentException.class,()->assignments.assign(10,101,20,"x".repeat(501)));
        assertThrows(IllegalArgumentException.class,()->assignments.assign(10,101,20,""));
        assignments.assign(10,101,20,"x".repeat(500));
        assertEquals(500,db.scalar("SELECT pickup_address FROM delivery_attempts WHERE attempt_id=2").length());
    }
}
