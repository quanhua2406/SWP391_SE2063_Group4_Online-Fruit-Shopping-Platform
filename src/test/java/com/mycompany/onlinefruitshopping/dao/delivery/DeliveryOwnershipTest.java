package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.model.Delivery;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Executes the actual read queries, including actor predicates and prepared parameters. */
class DeliveryOwnershipTest {
    private DeliveryDAO dao;
    private String url;

    @BeforeEach void database() throws SQLException {
        url = "jdbc:h2:mem:delivery_" + UUID.randomUUID() + ";MODE=MSSQLServer;DB_CLOSE_DELAY=-1";
        dao = new DeliveryDAO(() -> DriverManager.getConnection(url));
        try (Connection connection = DriverManager.getConnection(url); Statement sql = connection.createStatement()) {
            sql.execute("CREATE TABLE users(user_id INT PRIMARY KEY, full_name VARCHAR(100))");
            sql.execute("CREATE TABLE shops(shop_id INT PRIMARY KEY, owner_id INT, shop_name VARCHAR(100), status VARCHAR(20))");
            sql.execute("CREATE TABLE orders(order_id INT PRIMARY KEY, shop_id INT, user_id INT, total_amount DECIMAL(12,2), status VARCHAR(30))");
            sql.execute("CREATE TABLE deliveries(delivery_id INT PRIMARY KEY, order_id INT, staff_id INT, status VARCHAR(30))");
            sql.execute("CREATE TABLE shop_delivery_staff(shop_id INT, shipper_id INT, status VARCHAR(20))");
            sql.execute("CREATE TABLE products(product_id INT PRIMARY KEY, product_name VARCHAR(100))");
            sql.execute("CREATE TABLE product_variants(variant_id INT PRIMARY KEY, product_id INT, sku VARCHAR(30))");
            sql.execute("CREATE TABLE order_items(order_item_id INT PRIMARY KEY, order_id INT, variant_id INT, unit_price DECIMAL(12,2), quantity DECIMAL(10,2))");
            sql.execute("INSERT INTO users VALUES (201, 'Shipper A'), (202, 'Shipper B')");
            sql.execute("INSERT INTO shops VALUES (1,101,'Shop A','active'), (2,102,'Shop B','active'), (3,101,'Closed shop','suspended')");
            sql.execute("INSERT INTO shop_delivery_staff VALUES (1,201,'Active'), (2,202,'Active'), (2,201,'Inactive'), (3,201,'Active')");
            sql.execute("INSERT INTO orders(order_id,shop_id,user_id,total_amount) VALUES (1,1,1001,100),(2,2,1002,200),(3,1,1002,300),(4,3,1001,400),(5,2,1001,500)");
            sql.execute("INSERT INTO deliveries VALUES(1,1,201,'assigned'),(2,2,202,'assigned'),(3,3,201,'assigned'),(4,4,201,'assigned'),(5,5,201,'assigned')");
            sql.execute("CREATE TABLE payments(order_id INT, method VARCHAR(20), status VARCHAR(20))");
            sql.execute("CREATE TABLE delivery_attempts(attempt_id INT PRIMARY KEY,delivery_id INT,shipper_id INT,delivery_status VARCHAR(30),pickup_time TIMESTAMP,delivered_time TIMESTAMP,note VARCHAR(255),pickup_address VARCHAR(1000),receiver_name VARCHAR(100),receiver_phone VARCHAR(20),shipping_address VARCHAR(1000))");
            sql.execute("INSERT INTO delivery_attempts(attempt_id,delivery_id,shipper_id,delivery_status) SELECT delivery_id+10,delivery_id,staff_id,'Assigned' FROM deliveries");
            sql.execute("INSERT INTO products VALUES (1,'Apple')");
            sql.execute("INSERT INTO product_variants VALUES (1,1,'APPLE-1KG')");
            sql.execute("INSERT INTO order_items VALUES (1,1,1,50,0.5),(2,2,1,50,4)");
        }
    }

    @Test void customerReadsOnlyTheirOwnOrdersAcrossShops() throws SQLException {
        User customer = ServletHarness.user(1001, "CUSTOMER");
        assertEquals(List.of(5, 1), dao.findAll(customer).stream().map(Delivery::getId).toList());
        assertNotNull(dao.findById(11, customer));
        assertNull(dao.findById(12, customer));
        assertNull(dao.findById(13, customer));
    }

    @Test void ownerCannotReadAnotherShopsDelivery() throws SQLException {
        User owner = ServletHarness.user(101, "shop_owner");
        assertEquals(List.of(3, 1), dao.findAll(owner).stream().map(Delivery::getId).toList());
        assertNull(dao.findById(12, owner));
    }

    @Test void shipperNeedsBothAssignmentAndActiveShopMembership() throws SQLException {
        User shipper = ServletHarness.user(201, "Delivery Staff");
        assertEquals(List.of(3, 1), dao.findAll(shipper).stream().map(Delivery::getId).toList());
        assertNull(dao.findById(12, shipper));
        assertNull(dao.findById(15, shipper));
    }

    @Test void itemsCannotBeFetchedByGuessingAnotherDeliveryId() throws SQLException {
        User customer = ServletHarness.user(1001, "Customer");
        assertThrows(IllegalArgumentException.class, () -> dao.findItems(12, customer));
        assertEquals("Apple", dao.findItems(11, customer).get(0).getProductName());
        assertEquals(0, new java.math.BigDecimal("25").compareTo(dao.findItems(11, customer).get(0).getSubtotal()));
    }

    @Test void itemQueryRechecksAssignmentAfterInitialAccessCheck() throws SQLException {
        java.util.concurrent.atomic.AtomicInteger opens = new java.util.concurrent.atomic.AtomicInteger();
        DeliveryDAO changing = new DeliveryDAO(() -> {
            if (opens.incrementAndGet() == 2) {
                try (Connection c = DriverManager.getConnection(url); Statement statement = c.createStatement()) {
                    statement.executeUpdate("UPDATE shop_delivery_staff SET status='Inactive' WHERE shop_id=1 AND shipper_id=201");
                }
            }
            return DriverManager.getConnection(url);
        });
        assertTrue(changing.findItems(11, ServletHarness.user(201, "DeliveryStaff")).isEmpty());
    }

    @Test void inactiveShopIsExcludedForEveryReader() throws SQLException {
        for (User user : List.of(ServletHarness.user(1001, "Customer"),
                ServletHarness.user(101, "ShopOwner"), ServletHarness.user(201, "DeliveryStaff"))) {
            assertNull(dao.findById(14, user));
        }
    }

    @Test void unsupportedOrMissingActorIsRejectedBeforeAQuery() {
        assertThrows(IllegalArgumentException.class, () -> dao.findAll(ServletHarness.user(1, "Admin")));
        assertThrows(IllegalArgumentException.class, () -> dao.findAll(ServletHarness.user(0, "Customer")));
        assertThrows(IllegalArgumentException.class, () -> dao.findAll(null));
    }
}
