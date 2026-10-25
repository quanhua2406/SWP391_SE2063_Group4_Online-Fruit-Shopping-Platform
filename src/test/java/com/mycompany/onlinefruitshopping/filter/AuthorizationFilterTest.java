package com.mycompany.onlinefruitshopping.filter;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.annotation.WebFilter;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthorizationFilterTest {
    private final Validator validator = new Validator();
    private final AuthorizationFilter filter = new AuthorizationFilter(validator);
    private void run(ServletHarness h, int status, int calls) throws Exception {
        filter.doFilter(h.request, h.response, h.chain);
        assertEquals(status, h.status); assertEquals(calls, h.chainCalls);
    }
    @Test void guestsRedirectOnReadAndGet401OnWrite() throws Exception {
        ServletHarness h = new ServletHarness(); run(h, 302, 0);
        assertEquals("/fruit/login", h.redirect);
        assertEquals("/fruit/profile", h.sessionAttributes.get("redirectAfterLogin"));
        h = new ServletHarness(); h.path = "/shopowner/delivery/update"; h.method = "POST";
        run(h, 401, 0); assertNull(h.redirect);
    }
    @Test void guestCartRemainsPublic() throws Exception {
        ServletHarness h = new ServletHarness(); h.path = "/cart"; h.method = "POST";
        run(h, 200, 1); assertEquals(0, validator.calls);
    }
    @Test void validSessionNormalizesRole() throws Exception {
        ServletHarness h = new ServletHarness(); h.login("shop_owner"); run(h, 200, 1);
        assertEquals("SHOPOWNER", h.attributes.get("currentRole"));
        assertEquals(1, validator.calls);
        assertEquals("no-store", h.responseHeaders.get("Cache-Control"));
    }
    @Test void legacySessionIsNotAuthentication() throws Exception {
        ServletHarness h = new ServletHarness(); h.hasSession = true;
        h.sessionAttributes.put("shopUser", ServletHarness.user(10, "Customer"));
        run(h, 302, 0); assertEquals("/fruit/login", h.redirect); assertEquals(0, validator.calls);
    }
    @Test void wrongRoleDenied() throws Exception {
        ServletHarness h = new ServletHarness(); h.login("Customer"); h.path = "/admin/users";
        run(h, 403, 0);
    }
    @Test void missingAndIncorrectTokensDenied() throws Exception {
        for (String token : new String[]{null, "wrong"}) {
            ServletHarness h = write(); h.parameters.put("csrfToken", token); run(h, 403, 0);
        }
    }
    @Test void formAndHeaderTokensAccepted() throws Exception {
        ServletHarness h = write(); h.parameters.put("csrfToken", "expected"); run(h, 200, 1);
        h = write(); h.headers.put("X-CSRF-Token", "expected"); run(h, 200, 1);
    }
    @Test void customerTokenDoesNotGrantWriteRole() throws Exception {
        ServletHarness h = write(); h.login("Customer"); h.parameters.put("csrfToken", "expected");
        run(h, 403, 0);
    }
    @Test void revokedSessionInvalidates() throws Exception {
        validator.valid = false;
        ServletHarness h = new ServletHarness(); h.login("Customer"); run(h, 302, 0);
        assertTrue(h.invalidated); assertEquals("/fruit/login", h.redirect); assertEquals(1, validator.calls);
    }
    @Test void databaseFailureFailsClosed() throws Exception {
        validator.fail = true;
        ServletHarness h = new ServletHarness(); h.login("Customer"); run(h, 503, 0);
        assertEquals(1, validator.calls); assertFalse(h.invalidated);
    }
    @Test void directJspDeniedBeforeAuthenticationAndOnlyRequestDispatched() throws Exception {
        ServletHarness h = new ServletHarness(); h.path = "/jsp/shopowner/views/detail.jsp";
        run(h, 404, 0); assertEquals(0, validator.calls); assertEquals(0, h.sessionsCreated);
        assertArrayEquals(new DispatcherType[]{DispatcherType.REQUEST},
                AuthorizationFilter.class.getAnnotation(WebFilter.class).dispatcherTypes());
    }
    private ServletHarness write() {
        ServletHarness h = new ServletHarness(); h.login("DeliveryStaff");
        h.path = "/shopowner/delivery/update"; h.method = "POST";
        h.sessionAttributes.put("csrfToken", "expected"); return h;
    }
    private static class Validator extends LoginService {
        boolean valid = true, fail; int calls;
        @Override public boolean isSessionValid(User user) throws SQLException {
            calls++; if (fail) throw new SQLException("offline"); return valid;
        }
    }
}
