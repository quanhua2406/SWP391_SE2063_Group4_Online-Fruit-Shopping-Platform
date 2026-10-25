package com.mycompany.onlinefruitshopping.filter;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccessPolicyTest {
    @Test void deliveryReadersAndWriterAreSeparated() {
        for (String role : List.of("CUSTOMER", "SHOPOWNER", "DELIVERYSTAFF")) {
            for (String path : List.of("/shopowner/delivery/list", "/shopowner/delivery/detail"))
                assertTrue(AccessPolicy.permits(path, "GET", role));
            assertEquals(role.equals("DELIVERYSTAFF"), AccessPolicy.permits("/shopowner/delivery/update", "POST", role));
            assertFalse(AccessPolicy.permits("/shopowner/delivery/update", "GET", role));
        }
    }
    @Test void roleBoundariesAndUnknownRoutesDeny() {
        assertFalse(AccessPolicy.permits("/shopowner/shop/coupons", "GET", "CUSTOMER"));
        assertFalse(AccessPolicy.permits("/shopowner/customer/coupons", "GET", "SHOPOWNER"));
        for (String role : List.of("ADMIN", "CUSTOMER", "SHOPOWNER", "DELIVERYSTAFF")) {
            assertEquals(role.equals("ADMIN"), AccessPolicy.permits("/admin/users", "GET", role));
            assertFalse(AccessPolicy.permits("/unknown", "GET", role));
        }
        assertFalse(AccessPolicy.isPublic("/unknown", "GET"));
    }
    @Test void legacyLoginIsPublicButInternalViewsAreNot() {
        for (String method : List.of("GET", "HEAD", "POST")) {
            for (String path : List.of("/shopowner", "/shopowner/login"))
                assertTrue(AccessPolicy.isPublic(path, method));
            for (String path : List.of("/jsp/shopowner/views", "/jsp/shopowner/views/detail.jsp")) {
                assertTrue(AccessPolicy.isInternalView(path));
                assertFalse(AccessPolicy.isPublic(path, method));
                assertTrue(AccessPolicy.allowedRoles(path, method).isEmpty());
            }
        }
        assertFalse(AccessPolicy.isPublic("/shopowner/login", "DELETE"));
    }
    @Test void csrfScopeMatchesProtectedWrites() {
        for (String path : List.of("/logout", "/shopowner/logout", "/shopowner/shop/coupons", "/shopowner/delivery/update")) {
            assertTrue(AccessPolicy.requiresCsrf(path, "POST"));
            assertFalse(AccessPolicy.requiresCsrf(path, "GET"));
            assertFalse(AccessPolicy.requiresCsrf(path, "HEAD"));
        }
        assertFalse(AccessPolicy.requiresCsrf("/shopowner/login", "POST"));
        assertFalse(AccessPolicy.requiresCsrf("/cart", "POST"));
    }
}
