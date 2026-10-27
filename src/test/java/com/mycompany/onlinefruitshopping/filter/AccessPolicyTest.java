package com.mycompany.onlinefruitshopping.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class AccessPolicyTest {
    @ParameterizedTest
    @CsvSource({
        "/admin/dashboard,GET,Admin,true",
        "/admin/dashboard,GET,Customer,false",
        "/store-owner/products,POST,SHOP_OWNER,true",
        "/store-owner/products,POST,Customer,false",
        "/customer/dashboard,GET,Customer,true",
        "/delivery/dashboard,GET,DeliveryStaff,true",
        "/shopowner/shop/coupons,POST,Shop Owner,true",
        "/shopowner/shop/coupons,POST,Customer,false",
        "/shopowner/shop/deliveries,POST,DeliveryStaff,false",
        "/shopowner/customer/coupons,POST,Customer,true",
        "/shopowner/customer/coupons,POST,ShopOwner,false",
        "/shopowner/delivery/list,GET,Customer,true",
        "/shopowner/delivery/list,GET,ShopOwner,true",
        "/shopowner/delivery/list,GET,DeliveryStaff,true",
        "/shopowner/delivery/detail,HEAD,Customer,true",
        "/shopowner/delivery/detail,POST,Customer,false",
        "/shopowner/delivery/update,POST,DeliveryStaff,true",
        "/shopowner/delivery/update,GET,DeliveryStaff,false",
        "/shopowner/delivery/update,POST,Customer,false",
        "/shopowner/delivery/update,POST,ShopOwner,false",
        "/shopowner/delivery/update,POST,Admin,false",
        "/shopowner/notifications,POST,Customer,true",
        "/shopowner/logout,POST,DeliveryStaff,true",
        "/shopowner/logout,GET,DeliveryStaff,false",
        "/profile,GET,ShopOwner,true",
        "/addresses,GET,ShopOwner,false",
        "/subscription/subscribe,POST,Customer,true",
        "/subscription/subscribe,POST,DeliveryStaff,false",
        "/review/report,POST,ShopOwner,true",
        "/delete-review,GET,Customer,false",
        "/delete-review,POST,Customer,true",
        "/test-db,GET,Customer,false",
        "/unknown,GET,Admin,false",
        "/jsp/shopowner/views/detail.jsp,GET,ShopOwner,false"
    })
    void enforcesRoleAndMethod(String path, String method, String role, boolean expected) {
        assertEquals(expected, AccessPolicy.permits(path, method, role));
    }

    @Test void storefrontAndGuestCartRemainPublic() {
        for (String path : new String[]{"/", "/home", "/products", "/catalog", "/product", "/product-detail",
                "/api/products/search-suggestions", "/cart", "/subscriptions", "/subscription/detail",
                "/ViewReviewServlet", "/login", "/shopowner/login"}) {
            assertTrue(AccessPolicy.isPublic(path, "GET"), path);
        }
        assertTrue(AccessPolicy.isPublic("/cart/add", "POST"));
        assertFalse(AccessPolicy.isPublic("/subscription/subscribe", "POST"));
        assertFalse(AccessPolicy.isPublic("/shopowner/delivery/list", "GET"));
        assertFalse(AccessPolicy.isPublic("/products", "POST"));
    }

    @Test void javascriptOrJspUnderAnUploadDirectoryIsNotAnImage() {
        assertFalse(AccessPolicy.isStatic("/uploads/reviews/untrusted.jsp", "GET"));
        assertFalse(AccessPolicy.isStatic("/uploads/reviews/untrusted.js", "GET"));
        assertTrue(AccessPolicy.isStatic("/uploads/reviews/photo.jpg", "GET"));
    }
}
