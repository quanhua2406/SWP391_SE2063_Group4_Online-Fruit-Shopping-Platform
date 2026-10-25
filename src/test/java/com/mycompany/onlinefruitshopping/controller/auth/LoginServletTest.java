package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.dao.CartDAO;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginServletTest {
    @Test void successfulLoginRotatesAuthenticationMergesCartAndHonorsSavedRedirect() throws Exception {
        ServletHarness h = request();
        for (String key : List.of("userId", "roleName", "shopUser", "shopId", "shopcsrf", "csrfToken"))
            h.sessionAttributes.put(key, "old");
        List<CartItem> cart = List.of(new CartItem());
        h.sessionAttributes.put("sessionCart", cart);
        h.sessionAttributes.put("redirectAfterLogin", "/fruit/cart");
        User user = ServletHarness.user(25, "Customer");
        RecordingCart dao = new RecordingCart();
        new LoginServlet(success(user), dao).doPost(h.request, h.response);
        assertSame(user, h.sessionAttributes.get("user"));
        assertEquals(1, h.sessionIdChanges);
        assertNotEquals("old", h.sessionAttributes.get("csrfToken"));
        assertInstanceOf(String.class, h.sessionAttributes.get("csrfToken"));
        for (String key : List.of("userId", "roleName", "shopUser", "shopId", "shopcsrf", "redirectAfterLogin", "sessionCart"))
            assertFalse(h.sessionAttributes.containsKey(key), key);
        assertEquals(25, dao.owner); assertSame(cart, dao.merged); assertEquals(1, dao.calls);
        assertEquals(3, h.sessionAttributes.get("cartCount"));
        assertEquals("/fruit/cart", h.redirect);
    }
    @Test void cartFailureKeepsGuestCartAndStillUsesRoleDashboard() throws Exception {
        ServletHarness h = request();
        List<CartItem> cart = List.of(new CartItem()); h.sessionAttributes.put("sessionCart", cart);
        RecordingCart dao = new RecordingCart(); dao.fail = true;
        new LoginServlet(success(ServletHarness.user(25, "ShopOwner")), dao).doPost(h.request, h.response);
        assertSame(cart, h.sessionAttributes.get("sessionCart"));
        assertEquals("/fruit/store-owner/dashboard", h.redirect);
        assertEquals(1, h.sessionIdChanges); assertEquals(1, dao.calls);
    }
    private ServletHarness request() {
        ServletHarness h = new ServletHarness(); h.hasSession = true;
        h.parameters.put("email", " user@example.com "); h.parameters.put("password", " secret ");
        h.parameters.put("role", "Customer"); return h;
    }
    private LoginService success(User user) {
        return new LoginService() {
            @Override public LoginResult login(String email, String password, String role) {
                assertEquals("user@example.com", email); assertEquals(" secret ", password);
                return new LoginResult(Status.SUCCESS, user, "ok");
            }
        };
    }
    private static class RecordingCart extends CartDAO {
        int owner, calls; List<CartItem> merged; boolean fail;
        @Override public void mergeSessionCart(int id, List<CartItem> items) {
            calls++; if (fail) throw new IllegalStateException("offline"); owner = id; merged = items;
        }
        @Override public int getCartCount(int id) { assertEquals(25, id); return 3; }
    }
}
