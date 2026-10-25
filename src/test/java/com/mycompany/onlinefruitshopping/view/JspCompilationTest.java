package com.mycompany.onlinefruitshopping.view;

import java.nio.file.*;
import java.util.*;
import org.apache.jasper.JspC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class JspCompilationTest {
    @TempDir Path output;
    @Test void migratedViewsAndCustomerDashboardsCompile() throws Exception {
        Path webapp = Path.of("src/main/webapp").toAbsolutePath().normalize();
        Path moved = webapp.resolve("jsp/shopowner/views");
        assertTrue(Files.isDirectory(moved), "Missing migrated views");
        for (String name : List.of("coupon-detail.jsp", "coupon-form.jsp", "coupon-history.jsp",
                "coupons.jsp", "customer-coupons.jsp", "deliveries.jsp", "detail.jsp", "error.jsp",
                "login.jsp", "notifications.jsp", "promotion-detail.jsp", "promotion-form.jsp", "promotions.jsp", "shop.jsp"))
            assertTrue(Files.isRegularFile(moved.resolve(name)), "Missing JSP: " + name);
        List<String> pages = new ArrayList<>();
        try (var files = Files.walk(moved)) {
            files.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".jsp")).sorted()
                    .forEach(p -> pages.add(webapp.relativize(p).toString().replace('\\', '/')));
        }
        pages.add("jsp/customer/customer-dashboard.jsp"); pages.add("customer-dashboard.jsp");
        for (String name : List.of("commission-report", "commission", "create-shop", "store-owner-dashboard",
                "inventory", "inventory-update", "inventory-restock", "inventory-low-stock", "inventory-history",
                "pricing", "promotions")) pages.add("jsp/shopowner/" + name + ".jsp");
        for (String page : pages) assertTrue(Files.isRegularFile(webapp.resolve(page)), page);
        // Check the complete relative include graph, including nested JSPF includes.
        Set<Path> visited = new HashSet<>();
        for (String page : pages) includes(webapp.resolve(page), visited);
        for (String fragment : List.of("header.jspf", "footer.jspf", "delivery-table.jspf", "delivery-filters.jspf", "coupon-usage-table.jspf"))
            assertTrue(visited.contains(moved.resolve(fragment)), "Uncompiled include: " + fragment);
        JspC compiler = new JspC();
        compiler.setUriroot(webapp.toString()); compiler.setOutputDir(output.toString());
        compiler.setClassPath(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
        compiler.setJspFiles(String.join(",", pages));
        compiler.setCompilerSourceVM("17"); compiler.setCompilerTargetVM("17");
        compiler.setCompile(true); compiler.setFailOnError(true); compiler.execute();
        try (var generated = Files.walk(output)) {
            assertTrue(generated.anyMatch(p -> p.toString().endsWith(".class")), "Jasper generated no classes");
        }
        System.out.println("JSP compiled: " + pages.size());
    }
    private void includes(Path page, Set<Path> visited) throws Exception {
        if (!visited.add(page)) return;
        assertTrue(Files.isRegularFile(page), "Missing include: " + page);
        var matcher = java.util.regex.Pattern.compile("<%@\\s*include\\s+file=\"([^\"]+)\"").matcher(Files.readString(page));
        while (matcher.find()) includes(page.getParent().resolve(matcher.group(1)).normalize(), visited);
    }
}
