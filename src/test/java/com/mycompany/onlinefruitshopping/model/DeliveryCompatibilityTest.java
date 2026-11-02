package com.mycompany.onlinefruitshopping.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryCompatibilityTest {
    @Test void moduleAndAdminIdentifiersReferToTheSameValues() {
        Delivery d = new Delivery();
        d.setDeliveryId(4); d.setStaffId(20); d.setStaffName("A");
        assertEquals(4,d.getId()); assertEquals(20,d.getShipperId()); assertEquals("A",d.getShipperName());
        d.setId(5); d.setShipperId(21); d.setShipperName("B");
        assertEquals(5,d.getDeliveryId()); assertEquals(21,d.getStaffId()); assertEquals("B",d.getStaffName());
        d.setProofImageUrl("proof.jpg"); d.setStaffPhone("123");
        assertEquals("proof.jpg",d.getProofImageUrl()); assertEquals("123",d.getStaffPhone());
    }
}
