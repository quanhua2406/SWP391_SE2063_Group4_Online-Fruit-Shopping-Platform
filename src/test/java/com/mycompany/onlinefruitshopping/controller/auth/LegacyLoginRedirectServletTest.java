package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.support.ServletHarness;
import jakarta.servlet.annotation.WebServlet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LegacyLoginRedirectServletTest {
    @Test void registersOnlyLegacyUrls() {
        assertArrayEquals(new String[]{"/shopowner", "/shopowner/login"},
                LegacyLoginRedirectServlet.class.getAnnotation(WebServlet.class).urlPatterns());
    }
    @Test void getRedirectsWithoutSession() throws Exception {
        ServletHarness h = new ServletHarness();
        new LegacyLoginRedirectServlet().doGet(h.request, h.response);
        assertEquals(302, h.status); assertEquals("/fruit/login", h.redirect);
        assertEquals(0, h.sessionsCreated); assertTrue(h.sessionAttributes.isEmpty());
    }
    @Test void postUsesSeeOtherWithoutSession() throws Exception {
        ServletHarness h = new ServletHarness();
        new LegacyLoginRedirectServlet().doPost(h.request, h.response);
        assertEquals(303, h.status); assertEquals("/fruit/login", h.responseHeaders.get("Location"));
        assertEquals(0, h.sessionsCreated); assertTrue(h.sessionAttributes.isEmpty());
    }
}
