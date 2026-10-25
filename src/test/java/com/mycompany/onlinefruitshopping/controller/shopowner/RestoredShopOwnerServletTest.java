package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.service.shop.ShopService;
import com.mycompany.onlinefruitshopping.service.inventory.InventoryService;
import com.mycompany.onlinefruitshopping.service.pricing.PricingService;
import com.mycompany.onlinefruitshopping.service.promotion.VariantPromotionService;
import com.mycompany.onlinefruitshopping.service.report.CommissionReportService;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RestoredShopOwnerServletTest {
    private ServletHarness owner() { ServletHarness h = new ServletHarness(); h.login("ShopOwner"); return h; }
    private void get(HttpServlet servlet, ServletHarness h) throws Exception { servlet.service(h.request, h.response); }
    private void post(HttpServlet servlet, ServletHarness h) throws Exception { h.method = "POST"; servlet.service(h.request, h.response); }

    @Test void sevenMappingsAreDistinctAndGuestsAndWrongRolesCannotEnter() throws Exception {
        Map<HttpServlet, Set<String>> routes = new LinkedHashMap<>();
        routes.put(new StoreOwnerCommissionReportServlet(), Set.of("/store-owner/commission-report"));
        routes.put(new StoreOwnerCommissionServlet(), Set.of("/store-owner/commission"));
        routes.put(new StoreOwnerCreateShopServlet(), Set.of("/store-owner/create-shop"));
        routes.put(new StoreOwnerDashboardServlet(), Set.of("/store-owner/dashboard"));
        routes.put(new StoreOwnerInventoryServlet(), Set.of("/store-owner/inventory", "/store-owner/inventory/*"));
        routes.put(new StoreOwnerPricingServlet(), Set.of("/store-owner/pricing"));
        routes.put(new StoreOwnerPromotionServlet(), Set.of("/store-owner/promotions"));
        Set<String> all = new HashSet<>();
        for (var entry : routes.entrySet()) {
            WebServlet annotation = entry.getKey().getClass().getAnnotation(WebServlet.class);
            Set<String> actual = new HashSet<>(List.of(annotation.value())); actual.addAll(List.of(annotation.urlPatterns()));
            assertEquals(entry.getValue(), actual);
            for (String url : actual) assertTrue(all.add(url));
            ServletHarness h = new ServletHarness(); get(entry.getKey(), h);
            assertEquals("/fruit/login", h.redirect); assertNull(h.forwarded);
            h = owner(); h.login("Customer"); get(entry.getKey(), h);
            assertEquals(403, h.status); assertNull(h.forwarded);
        }
    }

    @Test void createShopValidatesAndUsesAuthenticatedOwner() throws Exception {
        int[] calls = {0};
        ShopService service = new ShopService() {
            @Override public boolean hasShop(int id) { assertEquals(10, id); return false; }
            @Override public boolean createShop(int id, String name, String description) {
                assertEquals(10, id); assertEquals("Fruit", name); assertEquals("Fresh", description); calls[0]++; return true;
            }
        };
        var servlet = new StoreOwnerCreateShopServlet(service);
        ServletHarness h = owner(); get(servlet, h); assertEquals("/jsp/shopowner/create-shop.jsp", h.forwarded);
        h = owner(); post(servlet, h); assertNotNull(h.attributes.get("error")); assertEquals(0, calls[0]);
        h = owner(); h.parameters.put("shopName", " Fruit "); h.parameters.put("description", " Fresh ");
        post(servlet, h); assertEquals(1, calls[0]); assertEquals("/fruit/store-owner/dashboard?shop=created", h.redirect);
    }

    @Test void dashboardForwardsLoadedSummary() throws Exception {
        var constructor = ShopService.Dashboard.class.getDeclaredConstructor(boolean.class, String.class, Map.class);
        constructor.setAccessible(true);
        var summary = constructor.newInstance(true, "Fruit", Map.of("total", 4));
        ServletHarness h = owner();
        get(new StoreOwnerDashboardServlet(new ShopService() {
            @Override public Dashboard loadDashboard(int id) { assertEquals(10, id); return summary; }
        }), h);
        assertEquals("/jsp/shopowner/store-owner-dashboard.jsp", h.forwarded);
        assertEquals("Fruit", h.attributes.get("shopName")); assertEquals(true, h.attributes.get("hasShop"));
        assertEquals(summary.getStats(), h.attributes.get("stats"));
    }

    @Test void reportsPassFiltersAndRedirectWhenShopIsAbsent() throws Exception {
        int[] calls = {0, 0};
        CommissionReportService service = new CommissionReportService() {
            @Override public OwnerProductsView loadOwnerProducts(int id, String q, String status, String page) {
                assertEquals(10, id); assertEquals("apple", q); calls[0]++; return null;
            }
            @Override public OwnerReport loadOwnerReport(int id, String from, String to, String status, String section) {
                assertEquals(10, id); assertEquals("2026-01-01", from); assertEquals("product", section); calls[1]++; return null;
            }
        };
        ServletHarness h = owner(); h.parameters.put("q", "apple"); get(new StoreOwnerCommissionServlet(service), h);
        assertEquals("/fruit/store-owner/create-shop", h.redirect);
        h = owner(); h.parameters.put("from", "2026-01-01"); h.parameters.put("section", "product");
        get(new StoreOwnerCommissionReportServlet(service), h); assertEquals("/fruit/store-owner/create-shop", h.redirect);
        assertArrayEquals(new int[]{1,1}, calls);
    }

    @Test void pricingAndPromotionWritesKeepOwnerAndFilterRedirect() throws Exception {
        int[] calls = {0, 0, 0};
        var pricing = new StoreOwnerPricingServlet(new PricingService() {
            @Override public String savePrice(int id, String variant, String price) {
                assertEquals(10, id); assertEquals("7", variant); assertEquals("90", price); calls[0]++; return "saved";
            }
        });
        var promotions = new StoreOwnerPromotionServlet(new VariantPromotionService() {
            @Override public String savePromotion(int id, String variant, String price, String from, String to) {
                assertEquals(10, id); assertEquals("7", variant); assertEquals("80", price); calls[1]++; return "promo-set";
            }
            @Override public String removePromotion(int id, String variant) {
                assertEquals(10, id); assertEquals("7", variant); calls[2]++; return "promo-removed";
            }
        });
        ServletHarness h = owner(); h.parameters.put("variantId", "7"); h.parameters.put("price", "90"); h.parameters.put("q", "apple");
        post(pricing, h); assertTrue(h.redirect.contains("q=apple")); assertTrue(h.redirect.endsWith("msg=saved"));
        h = owner(); h.parameters.put("variantId", "7"); h.parameters.put("promoPrice", "80");
        post(promotions, h); assertTrue(h.redirect.endsWith("msg=promo-set"));
        h.parameters.put("action", "removePromo"); post(promotions, h); assertTrue(h.redirect.endsWith("msg=promo-removed"));
        assertArrayEquals(new int[]{1,1,1}, calls);
    }

    @Test void inventoryDispatchesAllWriteActionsAndRejectsUnknownReadPath() throws Exception {
        List<String> calls = new ArrayList<>();
        var servlet = new StoreOwnerInventoryServlet(new InventoryService() {
            @Override public String updateBatch(int id, String batch, String qty, String reason) { assertEquals(10,id); calls.add("update"); return "ok"; }
            @Override public String restock(int id, String variant, String qty, String batch, String expiry, String reason) { assertEquals(10,id); calls.add("restock"); return "ok"; }
            @Override public String setThreshold(int id, String threshold) { assertEquals(10,id); calls.add("setThreshold"); return "ok"; }
            @Override public String notifyLowStock(com.mycompany.onlinefruitshopping.model.User user) { assertEquals(10,user.getUserID()); calls.add("notifyLowStock"); return "ok"; }
        });
        for (String action : List.of("update", "restock", "setThreshold", "notifyLowStock")) {
            ServletHarness h = owner(); h.parameters.put("action", action); post(servlet, h);
            String suffix = action.equals("restock") ? "restock" : action.equals("update") ? "update" : "low-stock";
            assertEquals("/fruit/store-owner/inventory/" + suffix + "?msg=ok", h.redirect);
        }
        assertEquals(List.of("update", "restock", "setThreshold", "notifyLowStock"), calls);
        ServletHarness h = owner(); h.pathInfo = "/unknown"; get(servlet, h);
        assertEquals("/fruit/store-owner/inventory", h.redirect);
    }
}
