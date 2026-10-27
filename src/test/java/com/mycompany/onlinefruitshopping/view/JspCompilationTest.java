package com.mycompany.onlinefruitshopping.view;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.jasper.JspC;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Compile changed views and their includes, without executing JSP code or accessing SQL Server. */
class JspCompilationTest {
    @Test void movedViewsAndUpdatedSessionReadersCompile() throws Exception {
        Path webapp = Path.of("src/main/webapp").toAbsolutePath();
        Path moved = webapp.resolve("jsp/shopowner/views");
        List<String> pages = new ArrayList<>();
        try (var files = Files.list(moved)) {
            files.filter(path -> path.toString().endsWith(".jsp"))
                    .sorted().forEach(path -> pages.add(webapp.relativize(path).toString().replace('\\', '/')));
        }
        assertEquals(14, pages.size());
        pages.addAll(List.of("jsp/customer/customer-dashboard.jsp", "customer-dashboard.jsp",
                "jsp/delivery/delivery-dashboard.jsp", "jsp/shopowner/store-owner-dashboard.jsp",
                "jsp/admin/admin-delivery-assign.jsp", "jsp/admin/admin-delivery-staff.jsp"));
        Path output = Path.of("target/jsp-compilation").toAbsolutePath();
        Files.createDirectories(output);

        JspC compiler = new JspC();
        compiler.setUriroot(webapp.toString());
        compiler.setOutputDir(output.toString());
        compiler.setClassPath(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
        compiler.setJspFiles(String.join(",", pages));
        compiler.setCompilerSourceVM("17");
        compiler.setCompilerTargetVM("17");
        compiler.setCompile(true);
        compiler.setFailOnError(true);
        compiler.execute();
        try (var generated = Files.walk(output)) {
            assertTrue(generated.anyMatch(path -> path.toString().endsWith(".class")));
        }
    }
}
