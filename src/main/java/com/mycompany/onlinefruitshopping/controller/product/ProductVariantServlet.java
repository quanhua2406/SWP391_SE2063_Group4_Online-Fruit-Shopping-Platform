package com.mycompany.onlinefruitshopping.controller.product;

import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductVariant;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.ProductService;
import com.mycompany.onlinefruitshopping.service.pricing.PricingService;
import com.mycompany.onlinefruitshopping.service.product.ProductVariantService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller Quản lý Biến thể (Product Variants) của shop owner.
 * Tuân thủ mô hình 3 tầng: nghiệp vụ nằm trong ProductService,
 * PricingService và ProductVariantService.
 */
@WebServlet("/store-owner/variants")
public class ProductVariantServlet extends HttpServlet {

    private final ProductService productService;
    private final PricingService pricingService = new PricingService();
    private final ProductVariantService variantService = new ProductVariantService();

    public ProductVariantServlet() {
        this.productService = new ProductService();
    }

    public ProductVariantServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        try {
            switch (action) {
                case "delete":
                    handleDeleteVariant(request, response);
                    break;
                case "list":
                default:
                    showVariantList(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in ProductVariantServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if (action == null) {
            action = "add";
        }

        try {
            switch (action) {
                case "add":
                    handleAddVariant(request, response);
                    break;
                case "edit":
                    handleEditVariant(request, response);
                    break;
                default:
                    response.sendRedirect(request.getRequestURI());
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in ProductVariantServlet", e);
        }
    }

    // =========================================================
    // 1. SHOW VARIANTS (ALL SHOP VARIANTS, OR ONE PRODUCT)
    // =========================================================
    private void showVariantList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String q = trim(request.getParameter("q"));
        String productIdStr = request.getParameter("productId");

        // In shop-wide view, productId acts as an optional filter. In single-product
        // context links, productId is the target product. We treat it as single-product
        // context only when it resolves to an owned/existing product and no explicit
        // shop-wide filter intent is implied by other params? To keep behavior simple:
        // if productId is present and valid, prefer single-product context; provide
        // filter UI via shop-wide mode (productId omitted or "0"/"all") and q.
        if (productIdStr == null || productIdStr.isBlank() || "0".equals(productIdStr) || "all".equalsIgnoreCase(productIdStr)) {
            showShopVariants(request, response, q, parseOptionalInt(productIdStr));
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            redirectInvalid(request, response, "Mã sản phẩm không hợp lệ.");
            return;
        }

        // Allow using productId as filter in shop-wide mode by passing view=shop.
        if ("shop".equalsIgnoreCase(request.getParameter("view"))) {
            showShopVariants(request, response, q, productId);
            return;
        }

        Product product = productService.getProductDetail(productId);
        if (product == null) {
            redirectInvalid(request, response, "Không tìm thấy sản phẩm.");
            return;
        }

        List<ProductVariant> variants = productService.getProductVariants(productId);
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            variants = variants.stream().filter(v ->
                    containsIgnoreCase(v.getSku(), needle)
                    || containsIgnoreCase(v.getPackagingType(), needle)
                    || containsIgnoreCase(v.getUnit(), needle)
                    || containsIgnoreCase(v.getQualityGrade(), needle)
            ).collect(java.util.stream.Collectors.toList());
        }

        request.setAttribute("q", q);
        request.setAttribute("product", product);
        request.setAttribute("variants", variants);
        request.setAttribute("qualityGrades", variantService.qualityGrades());
        request.getRequestDispatcher("/jsp/shopowner/product-variants.jsp").forward(request, response);
    }

    private void showShopVariants(HttpServletRequest request, HttpServletResponse response,
                                  String q, Integer filterProductId)
            throws SQLException, ServletException, IOException {

        int ownerId = currentUserId(request);

        request.setAttribute("q", q);
        request.setAttribute("filterProductId", filterProductId == null ? 0 : filterProductId);
        request.setAttribute("variants", variantService.shopVariants(ownerId, q, filterProductId));
        request.setAttribute("shopProducts", variantService.shopProducts(ownerId));
        request.setAttribute("qualityGrades", variantService.qualityGrades());
        request.getRequestDispatcher("/jsp/shopowner/product-variants.jsp").forward(request, response);
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            int v = Integer.parseInt(value);
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean containsIgnoreCase(String value, String needleLower) {
        return value != null && value.toLowerCase().contains(needleLower);
    }

    // =========================================================
    // 2. ADD PRODUCT VARIANT
    // =========================================================
    private void handleAddVariant(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String productIdStr = request.getParameter("productId");
        String sku = request.getParameter("sku");
        String packagingType = request.getParameter("packagingType");
        String unit = request.getParameter("unit");
        String weightStr = request.getParameter("weightValue");
        String stockStr = request.getParameter("stock");

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            redirectInvalid(request, response, "Mã sản phẩm không hợp lệ.");
            return;
        }

        if (!variantService.productBelongsToOwner(productId, currentUserId(request))) {
            redirectWithError(request, response, productId, "Bạn không có quyền thêm quy cách cho sản phẩm này.");
            return;
        }

        if (weightStr == null || weightStr.isBlank()) {
            redirectWithError(request, response, productId, "Vui lòng nhập khối lượng quy cách (kg).");
            return;
        }

        Double weightValue;
        try {
            weightValue = Double.parseDouble(weightStr);
        } catch (Exception e) {
            redirectWithError(request, response, productId, "Khối lượng quy cách không hợp lệ!");
            return;
        }
        if (weightValue.isNaN() || weightValue.isInfinite() || weightValue <= 0) {
            redirectWithError(request, response, productId, "Khối lượng quy cách phải lớn hơn 0!");
            return;
        }

        double stock = 0;
        if (stockStr != null && !stockStr.isBlank()) {
            try {
                stock = Double.parseDouble(stockStr);
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Số lượng tồn kho không hợp lệ!");
                return;
            }
        }

        String grade = pricingService.normalizeGrade(request.getParameter("qualityGrade"));
        double price;
        try {
            price = pricingService.requireAutoVariantPrice(productId, weightValue, grade).doubleValue();
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, productId, e.getMessage());
            return;
        }

        ProductVariant v = new ProductVariant();
        v.setProductId(productId);
        v.setSku(sku != null ? sku.trim().toUpperCase() : "");
        v.setPackagingType(packagingType != null ? packagingType.trim() : "");
        v.setUnit((unit != null && !unit.isBlank()) ? unit : "kg");
        v.setWeightValue(weightValue);
        v.setQualityGrade(grade);
        v.setPrice(price);

        try {
            productService.addVariant(v, stock);
            response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=added");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, productId, e.getMessage());
        }
    }

    // =========================================================
    // 3. EDIT PRODUCT VARIANT
    // =========================================================
    private void handleEditVariant(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String variantIdStr = request.getParameter("variantId");
        String productIdStr = request.getParameter("productId");
        String sku = request.getParameter("sku");
        String packagingType = request.getParameter("packagingType");
        String unit = request.getParameter("unit");
        String weightStr = request.getParameter("weightValue");
        String stockStr = request.getParameter("stock");

        int variantId, productId;
        try {
            variantId = Integer.parseInt(variantIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            redirectInvalid(request, response, "Mã biến thể hoặc sản phẩm không hợp lệ.");
            return;
        }

        if (!variantService.variantBelongsToOwner(variantId, currentUserId(request))) {
            redirectWithError(request, response, productId, "Bạn không có quyền sửa quy cách này.");
            return;
        }

        if (weightStr == null || weightStr.isBlank()) {
            redirectWithError(request, response, productId, "Vui lòng nhập khối lượng quy cách (kg).");
            return;
        }

        Double weightValue;
        try {
            weightValue = Double.parseDouble(weightStr);
        } catch (Exception e) {
            redirectWithError(request, response, productId, "Khối lượng quy cách không hợp lệ!");
            return;
        }
        if (weightValue.isNaN() || weightValue.isInfinite() || weightValue <= 0) {
            redirectWithError(request, response, productId, "Khối lượng quy cách phải lớn hơn 0!");
            return;
        }

        double stock = 0;
        if (stockStr != null && !stockStr.isBlank()) {
            try {
                stock = Double.parseDouble(stockStr);
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Số lượng tồn kho không hợp lệ!");
                return;
            }
        }

        String grade = pricingService.normalizeGrade(request.getParameter("qualityGrade"));
        double price;
        try {
            price = pricingService.requireAutoVariantPrice(productId, weightValue, grade).doubleValue();
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, productId, e.getMessage());
            return;
        }

        ProductVariant before = productService.getVariantById(variantId);

        ProductVariant v = new ProductVariant();
        v.setVariantId(variantId);
        v.setProductId(productId);
        v.setSku(sku != null ? sku.trim().toUpperCase() : "");
        v.setPackagingType(packagingType != null ? packagingType.trim() : "");
        v.setUnit((unit != null && !unit.isBlank()) ? unit : "kg");
        v.setWeightValue(weightValue);
        v.setQualityGrade(grade);
        v.setPrice(price);

        try {
            productService.updateVariant(v, stock, currentUserId(request));
            if (before != null) {
                pricingService.recordVariantPriceChange(variantId, before.getPrice(), price, currentUserId(request));
            }
            response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=updated");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, productId, e.getMessage());
        }
    }

    // =========================================================
    // 4. DELETE PRODUCT VARIANT
    // =========================================================
    private void handleDeleteVariant(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String variantIdStr = request.getParameter("variantId");
        if (variantIdStr == null) {
            variantIdStr = request.getParameter("id");
        }
        String productIdStr = request.getParameter("productId");

        if (variantIdStr == null) {
            redirectInvalid(request, response, "Mã biến thể không hợp lệ.");
            return;
        }

        int variantId;
        try {
            variantId = Integer.parseInt(variantIdStr);
        } catch (NumberFormatException e) {
            redirectInvalid(request, response, "Mã biến thể không hợp lệ.");
            return;
        }

        int productId = 0;
        if (productIdStr != null && !productIdStr.isBlank()) {
            try { productId = Integer.parseInt(productIdStr); } catch (NumberFormatException ignored) {}
        }
        if (productId <= 0) {
            ProductVariant existing = productService.getVariantById(variantId);
            if (existing != null) {
                productId = existing.getProductId();
            }
        }

        if (!variantService.variantBelongsToOwner(variantId, currentUserId(request))) {
            redirectWithError(request, response, productId, "Bạn không có quyền xóa quy cách này.");
            return;
        }

        try {
            productService.deleteVariant(variantId, productId);
            if (productId > 0) {
                response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=deleted");
            } else {
                response.sendRedirect(request.getRequestURI() + "?success=deleted");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (productId > 0) {
                redirectWithError(request, response, productId, e.getMessage());
            } else {
                redirectInvalid(request, response, e.getMessage());
            }
        }
    }

    private void redirectInvalid(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/store-owner/variants?errorMsg=" + encoded);
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, int productId, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&errorMsg=" + encoded);
    }

    private int currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        return user.getUserID();
    }

    private User getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return (User) session.getAttribute("user");
    }
}
