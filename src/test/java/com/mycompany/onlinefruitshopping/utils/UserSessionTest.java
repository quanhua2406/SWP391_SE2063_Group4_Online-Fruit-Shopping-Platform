package com.mycompany.onlinefruitshopping.utils;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {
    @Test void signInClearsLegacyStateRotatesTokenAndKeepsGuestCart() {
        ServletHarness h = new ServletHarness(); h.hasSession = true;
        for (String key : List.of("shopUser", "userId", "roleName", "shopId", "shopcsrf", "csrfToken"))
            h.sessionAttributes.put(key, "old");
        Object cart = new Object(); h.sessionAttributes.put("sessionCart", cart);
        User u = ServletHarness.user(10, "Customer"); UserSession.signIn(h.session, u);
        assertSame(u, h.sessionAttributes.get("user")); assertSame(u, UserSession.current(h.session));
        for (String key : List.of("shopUser", "userId", "roleName", "shopId", "shopcsrf"))
            assertFalse(h.sessionAttributes.containsKey(key));
        assertInstanceOf(String.class, h.sessionAttributes.get("csrfToken"));
        assertNotEquals("old", h.sessionAttributes.get("csrfToken"));
        assertSame(cart, h.sessionAttributes.get("sessionCart"));
    }
    @Test void currentNeverCreatesSessionAndRejectsWrongPrincipalType() {
        ServletHarness h = new ServletHarness();
        assertNull(UserSession.current(h.request)); assertEquals(0, h.sessionsCreated);
        h.hasSession = true; h.sessionAttributes.put("user", "not a User");
        assertNull(UserSession.current(h.request));
    }
    @Test void refreshPreservesVerifiedRoleAndRejectsDifferentIdentity() {
        ServletHarness h = new ServletHarness(); h.login("Customer");
        User original = UserSession.current(h.session); original.setRoleID(4);
        User profile = ServletHarness.user(10, "Admin"); profile.setRoleID(1); profile.setFullName("Updated");
        UserSession.refresh(h.session, profile);
        assertSame(profile, UserSession.current(h.session));
        assertEquals("Updated", profile.getFullName()); assertEquals("Customer", profile.getRoleName());
        assertEquals(4, profile.getRoleID());
        assertThrows(IllegalArgumentException.class, () -> UserSession.refresh(h.session, ServletHarness.user(20, "Admin")));
        assertSame(profile, UserSession.current(h.session));
    }
}
