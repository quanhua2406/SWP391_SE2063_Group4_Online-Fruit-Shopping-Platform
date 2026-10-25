package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryOwnershipTest {
    private Connection anchor;
    private DeliveryDAO dao;
    @BeforeEach void setup() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MSSQLServer";
        anchor = DriverManager.getConnection(url);
        dao = new DeliveryDAO(() -> DriverManager.getConnection(url));
        try (Statement s = anchor.createStatement()) {
            s.execute("CREATE TABLE users(user_id INT PRIMARY KEY, full_name VARCHAR(100))");
            s.execute("CREATE TABLE shops(shop_id INT PRIMARY KEY, owner_id INT, shop_name VARCHAR(100), pickup_address VARCHAR(100), status VARCHAR(30))");
            s.execute("CREATE TABLE shop_delivery_staff(shop_id INT, shipper_id INT, status VARCHAR(30))");
            s.execute("CREATE TABLE orders(order_id INT PRIMARY KEY, shop_id INT, user_id INT, receiver_name VARCHAR(100), receiver_phone VARCHAR(30), shipping_address VARCHAR(100), total_amount DECIMAL(18,2), order_status VARCHAR(30), payment_method VARCHAR(30), payment_status VARCHAR(30), note VARCHAR(100))");
            s.execute("CREATE TABLE deliveries(delivery_id INT PRIMARY KEY, order_id INT, shop_id INT, shipper_id INT, delivery_status VARCHAR(30), pickup_time TIMESTAMP, delivered_time TIMESTAMP, note VARCHAR(100))");
            s.execute("CREATE TABLE products(product_id INT PRIMARY KEY, product_name VARCHAR(100))");
            s.execute("CREATE TABLE product_variants(variant_id INT PRIMARY KEY, product_id INT, sku VARCHAR(100))");
            s.execute("CREATE TABLE order_items(order_item_id INT PRIMARY KEY, order_id INT, variant_id INT, price DECIMAL(18,2), quantity INT)");
            s.execute("INSERT INTO users VALUES(1,'Customer A'),(2,'Customer B'),(11,'Owner A'),(12,'Owner B'),(21,'Shipper A'),(22,'Shipper B')");
            s.execute("INSERT INTO shops VALUES(1,11,'Shop A','A','active'),(2,12,'Shop B','B','active')");
            s.execute("INSERT INTO shop_delivery_staff VALUES(1,21,'Active'),(2,22,'Active'),(2,21,'Inactive')");
            s.execute("INSERT INTO orders(order_id,shop_id,user_id,receiver_name,total_amount) VALUES(101,1,1,'A',100),(102,2,2,'B',200),(103,2,2,'B',300)");
            s.execute("INSERT INTO deliveries(delivery_id,order_id,shop_id,shipper_id,delivery_status) VALUES(201,101,1,21,'Assigned'),(202,102,2,22,'Assigned'),(203,103,2,21,'Assigned')");
            s.execute("INSERT INTO products VALUES(1,'Apple'),(2,'Orange')");
            s.execute("INSERT INTO product_variants VALUES(1,1,'APPLE'),(2,2,'ORANGE')");
            s.execute("INSERT INTO order_items VALUES(1,101,1,100,1),(2,102,2,100,2),(3,103,2,100,3)");
        }
    }
    @AfterEach void close() throws Exception { anchor.close(); }
    private User user(int id, String role) { return ServletHarness.user(id, role); }
    private void only(User user, Integer... ids) throws Exception {
        assertEquals(List.of(ids), dao.findAll(user).stream().map(d -> d.getId()).toList());
    }
    @Test void customersSeeOnlyOwnOrdersAndItems() throws Exception {
        User a = user(1, "customer"); only(a, 201); only(user(2, "CUSTOMER"), 203, 202);
        assertNotNull(dao.findById(201, a));
        var items = dao.findItems(201, a); assertEquals(1, items.size());
        assertEquals("Apple", items.get(0).getProductName()); assertEquals("APPLE", items.get(0).getVariantName());
        assertEquals(1, items.get(0).getQuantity());
        assertEquals(0, items.get(0).getPrice().compareTo(new java.math.BigDecimal("100")));
        assertNull(dao.findById(202, a));
        assertThrows(IllegalArgumentException.class, () -> dao.findItems(202, a));
    }
    @Test void ownersSeeOnlyTheirShops() throws Exception {
        User a = user(11, "shop_owner"); only(a, 201); only(user(12, "ShopOwner"), 203, 202);
        assertNotNull(dao.findById(201, a)); assertEquals(1, dao.findItems(201, a).size());
        assertNull(dao.findById(202, a));
        assertThrows(IllegalArgumentException.class, () -> dao.findItems(202, a));
    }
    @Test void shippersMustBeAssignedAndActiveAtThatShop() throws Exception {
        User a = user(21, "delivery_staff"); only(a, 201); only(user(22, "DeliveryStaff"), 202);
        assertNotNull(dao.findById(201, a)); assertEquals(1, dao.findItems(201, a).size());
        for (int id : List.of(202, 203)) {
            assertNull(dao.findById(id, a));
            assertThrows(IllegalArgumentException.class, () -> dao.findItems(id, a));
        }
        try (Statement s = anchor.createStatement()) { s.executeUpdate("UPDATE shop_delivery_staff SET status='Inactive' WHERE shop_id=1 AND shipper_id=21"); }
        only(a); assertNull(dao.findById(201, a));
        assertThrows(IllegalArgumentException.class, () -> dao.findItems(201, a));
    }
    @Test void invalidPrincipalsAndDeliveryIdsAreRejected() {
        for (User u : Arrays.asList(null, user(0, "Customer"), user(-1, "Customer"), user(1, "Admin"), user(1, null))) {
            assertThrows(IllegalArgumentException.class, () -> dao.findAll(u));
            assertThrows(IllegalArgumentException.class, () -> dao.findById(201, u));
            assertThrows(IllegalArgumentException.class, () -> dao.findItems(201, u));
        }
        assertThrows(IllegalArgumentException.class, () -> dao.findById(0, user(1, "Customer")));
    }
    @Test void mismatchedOrderShopAndInactiveShopAreNotVisible() throws Exception {
        try (Statement s = anchor.createStatement()) { s.executeUpdate("UPDATE deliveries SET shop_id=2 WHERE delivery_id=201"); }
        assertNull(dao.findById(201, user(12, "ShopOwner")));
        assertThrows(IllegalArgumentException.class, () -> dao.findItems(201, user(1, "Customer")));
        try (Statement s = anchor.createStatement()) { s.executeUpdate("UPDATE shops SET status='inactive' WHERE shop_id=2"); }
        only(user(12, "ShopOwner"));
    }
}
