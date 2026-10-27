package com.mycompany.onlinefruitshopping.utils;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {
    @Test void signInRetainsGuestCartAndReplacesOnlyAuthenticationState() {
        ServletHarness h = new ServletHarness("/login", "POST");
        Object cart = new Object();
        h.sessionValues.put("sessionCart", cart);
        for (String name : new String[]{"shopUser", "userId", "roleName", "shopcsrf", "shopId"}) {
            h.sessionValues.put(name, "stale");
        }
        h.sessionValues.put("csrfToken", "old-token");
        User user = ServletHarness.user(42, "Customer");
        UserSession.signIn(h.session(), user);
        assertSame(user, UserSession.current(h.request));
        assertSame(cart, h.sessionValues.get("sessionCart"));
        assertNotEquals("old-token", h.sessionValues.get("csrfToken"));
        for (String name : new String[]{"shopUser", "userId", "roleName", "shopcsrf", "shopId"}) {
            assertFalse(h.sessionValues.containsKey(name), name);
        }
    }

    @Test void anonymousLookupDoesNotCreateSession() {
        ServletHarness h = new ServletHarness("/home", "GET");
        assertNull(UserSession.current(h.request));
        assertFalse(h.hasSession);
    }

    @Test void wrongPrincipalTypeIsTreatedAsGuest() {
        ServletHarness h = new ServletHarness("/home", "GET");
        h.session().setAttribute("user", "obsolete-principal");
        assertNull(UserSession.current(h.request));
    }

    @Test void refreshingProfileKeepsTheValidatedLoginRole() {
        ServletHarness h = new ServletHarness("/profile", "GET").as(42, "Customer");
        UserSession.current(h.request).setRoleID(3);
        User profile = ServletHarness.user(42, "Admin");
        profile.setFullName("Updated name");
        UserSession.refresh(h.session(), profile);
        assertEquals("Customer", UserSession.current(h.request).getRoleName());
        assertEquals(3, UserSession.current(h.request).getRoleID());
        assertEquals("Updated name", UserSession.current(h.request).getFullName());
    }

    @Test void profileCannotReplaceAnotherUsersSession() {
        ServletHarness h = new ServletHarness("/profile", "GET").as(42, "Customer");
        assertThrows(IllegalArgumentException.class,
                () -> UserSession.refresh(h.session(), ServletHarness.user(99, "Admin")));
        assertEquals(42, UserSession.current(h.request).getUserID());
    }
}
