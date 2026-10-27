package com.mycompany.onlinefruitshopping.filter;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.annotation.WebFilter;
import java.sql.SQLException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthorizationFilterTest {
    private final SessionChecker checker = new SessionChecker();
    private final AuthorizationFilter filter = new AuthorizationFilter(checker);

    private void run(ServletHarness h) throws Exception {
        filter.doFilter(h.request, h.response, h.chain);
    }

    @Test void guestIsRedirectedAndInternalGetTargetIsRemembered() throws Exception {
        ServletHarness h = new ServletHarness("/shopowner/delivery/detail", "GET");
        h.query = "id=17";
        run(h);
        assertEquals(302, h.status);
        assertEquals("/OnlineFruitShopping/login", h.redirect);
        assertEquals("/OnlineFruitShopping/shopowner/delivery/detail?id=17",
                h.sessionValues.get("redirectAfterLogin"));
        assertEquals(0, h.calls);
    }

    @Test void guestWriteReturns401WithoutSavingPostAsAGet() throws Exception {
        ServletHarness h = new ServletHarness("/shopowner/delivery/update", "POST");
        run(h);
        assertEquals(401, h.status);
        assertFalse(h.sessionValues.containsKey("redirectAfterLogin"));
        assertEquals(0, h.calls);
    }

    @Test void guestCartWorksWithoutQueryingAccounts() throws Exception {
        ServletHarness h = new ServletHarness("/cart", "GET");
        run(h);
        assertEquals(1, h.calls);
        assertEquals(0, checker.calls);
    }

    @Test void customerReadsDeliveriesUsingOnlyTheCommonUser() throws Exception {
        ServletHarness h = new ServletHarness("/shopowner/delivery/list", "GET").as(11, "Customer");
        Object user = h.sessionValues.get("user");
        run(h);
        assertEquals(1, h.calls);
        assertEquals("CUSTOMER", h.attributes.get("currentRole"));
        assertSame(user, h.sessionValues.get("user"));
        assertFalse(h.sessionValues.containsKey("shopUser"));
        assertNotNull(h.sessionValues.get("csrfToken"));
    }

    @Test void legacyShopUserAloneCannotAuthenticate() throws Exception {
        ServletHarness h = new ServletHarness("/shopowner/notifications", "GET");
        h.session().setAttribute("shopUser", ServletHarness.user(11, "Customer"));
        run(h);
        assertEquals(302, h.status);
        assertEquals(0, h.calls);
    }

    @Test void customerCannotUpdateEvenWithAValidToken() throws Exception {
        ServletHarness h = new ServletHarness("/shopowner/delivery/update", "POST").as(11, "Customer");
        h.sessionValues.put("csrfToken", "known-token");
        h.parameters.put("csrfToken", "known-token");
        run(h);
        assertEquals(403, h.status);
        assertEquals(0, h.calls);
    }

    @Test void shipperWriteRejectsMissingAndIncorrectCsrf() throws Exception {
        for (String supplied : new String[]{null, "wrong-token"}) {
            ServletHarness h = new ServletHarness("/shopowner/delivery/update", "POST").as(21, "DeliveryStaff");
            h.sessionValues.put("csrfToken", "known-token");
            if (supplied != null) h.parameters.put("csrfToken", supplied);
            run(h);
            assertEquals(403, h.status);
            assertEquals(0, h.calls);
        }
    }

    @Test void shipperWriteAcceptsFormOrHeaderToken() throws Exception {
        for (boolean header : new boolean[]{false, true}) {
            ServletHarness h = new ServletHarness("/shopowner/delivery/update", "POST").as(21, "delivery_staff");
            h.sessionValues.put("csrfToken", "known-token");
            if (header) h.headers.put("X-CSRF-Token", "known-token");
            else h.parameters.put("csrfToken", "known-token");
            run(h);
            assertEquals(1, h.calls);
            assertEquals("DELIVERYSTAFF", h.attributes.get("currentRole"));
        }
    }

    @Test void modulePostLogoutRequiresCsrf() throws Exception {
        ServletHarness h = new ServletHarness("/shopowner/logout", "POST").as(21, "DeliveryStaff");
        run(h);
        assertEquals(403, h.status);
        assertEquals(0, h.calls);
    }

    @Test void blockedOrRevokedAccountInvalidatesTheSession() throws Exception {
        checker.active = false;
        ServletHarness h = new ServletHarness("/shopowner/shop/coupons", "GET").as(31, "ShopOwner");
        run(h);
        assertTrue(h.invalidated);
        assertEquals(302, h.status);
        assertEquals(0, h.calls);
    }

    @Test void modulePostRevalidatesRevokedAccount() throws Exception {
        checker.active = false;
        ServletHarness h = new ServletHarness("/shopowner/notifications", "POST").as(11, "Customer");
        run(h);
        assertTrue(h.invalidated);
        assertEquals(401, h.status);
        assertEquals(0, h.calls);
    }

    @Test void databaseFailureDoesNotGrantAccess() throws Exception {
        checker.failure = true;
        ServletHarness h = new ServletHarness("/shopowner/shop/deliveries", "GET").as(31, "ShopOwner");
        run(h);
        assertEquals(503, h.status);
        assertEquals(0, h.calls);
    }

    @Test void migratedJspCannotBeOpenedDirectlyEvenByOwner() throws Exception {
        ServletHarness h = new ServletHarness("/jsp/shopowner/views/deliveries.jsp", "GET").as(31, "ShopOwner");
        run(h);
        assertEquals(404, h.status);
        assertEquals(0, h.calls);
        assertEquals(0, checker.calls);
    }

    @Test void assetsDoNotCreateASessionOrQueryAccounts() throws Exception {
        ServletHarness h = new ServletHarness("/assets/freshfruit-base.css", "GET");
        run(h);
        assertEquals(1, h.calls);
        assertFalse(h.hasSession);
        assertEquals(0, checker.calls);
    }

    @Test void unknownEndpointDoesNotFallThrough() throws Exception {
        ServletHarness h = new ServletHarness("/shopowner/unclassified-sensitive-action", "POST").as(1, "Admin");
        run(h);
        assertEquals(403, h.status);
        assertEquals(0, h.calls);
    }

    @Test void filterDoesNotReclassifyInternalForwardsAsOwnerPages() {
        WebFilter annotation = AuthorizationFilter.class.getAnnotation(WebFilter.class);
        assertArrayEquals(new DispatcherType[]{DispatcherType.REQUEST}, annotation.dispatcherTypes());
        assertFalse(Arrays.asList(annotation.dispatcherTypes()).contains(DispatcherType.FORWARD));
    }

    @Test void teammateRoutesKeepExistingPublicAndRoleBehavior() throws Exception {
        for (String path : new String[]{"/home", "/cart/add", "/api/products/search-suggestions"}) {
            ServletHarness h = new ServletHarness(path, "GET");
            run(h);
            assertEquals(1, h.calls);
        }
        ServletHarness denied = new ServletHarness("/admin/dashboard", "GET").as(11, "Customer");
        run(denied);
        assertEquals(403, denied.status);
        assertEquals(0, denied.calls);
        ServletHarness allowed = new ServletHarness("/admin/dashboard", "GET").as(1, "Admin");
        run(allowed);
        assertEquals(1, allowed.calls);
        assertEquals(0, checker.calls);
    }

    private static class SessionChecker extends LoginService {
        boolean active = true;
        boolean failure;
        int calls;
        @Override public boolean isSessionValid(User user) throws SQLException {
            calls++;
            if (failure) throw new SQLException("Test connection failure");
            return active;
        }
    }
}
