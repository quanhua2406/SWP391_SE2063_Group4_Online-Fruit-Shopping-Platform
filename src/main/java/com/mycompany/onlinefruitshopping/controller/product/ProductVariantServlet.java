package com.mycompany.onlinefruitshopping.controller.product;

import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductVariant;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.ProductService;

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
 * Controller Quản lý Quy cách đóng gói biến thể (Product Variants).
 * Tuân thủ mô hình 3 tầng: Sử dụng ProductService để xử lý nghiệp vụ.
 */
@WebServlet(urlPatterns = {"/store-owner/variants", "/admin/variants"})
public class ProductVariantServlet extends HttpServlet {

    private final ProductService productService;

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
    // 1. SHOW VARIANT LIST FOR A PRODUCT
    // =========================================================
    private void showVariantList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String productIdStr = request.getParameter("productId");
        if (productIdStr == null || productIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        Product product = productService.getProductDetail(productId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=notfound");
            return;
        }

        List<ProductVariant> variants = productService.getProductVariants(productId);

        request.setAttribute("product", product);
        request.setAttribute("variants", variants);
        request.getRequestDispatcher("/product-variants.jsp").forward(request, response);
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
        String priceStr = request.getParameter("price");
        String stockStr = request.getParameter("stock");

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                redirectWithError(request, response, productId, "Đơn giá không được nhỏ hơn 0!");
                return;
            }
        } catch (Exception e) {
            redirectWithError(request, response, productId, "Đơn giá không hợp lệ!");
            return;
        }

        Double weightValue = null;
        if (weightStr != null && !weightStr.isBlank()) {
            try {
                weightValue = Double.parseDouble(weightStr);
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Khối lượng quy cách không hợp lệ!");
                return;
            }
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

        ProductVariant v = new ProductVariant();
        v.setProductId(productId);
        v.setSku(sku != null ? sku.trim().toUpperCase() : "");
        v.setPackagingType(packagingType != null ? packagingType.trim() : "");
        v.setUnit((unit != null && !unit.isBlank()) ? unit : "kg");
        v.setWeightValue(weightValue);
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
        String priceStr = request.getParameter("price");
        String stockStr = request.getParameter("stock");

        int variantId, productId;
        try {
            variantId = Integer.parseInt(variantIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                redirectWithError(request, response, productId, "Đơn giá không được nhỏ hơn 0!");
                return;
            }
        } catch (Exception e) {
            redirectWithError(request, response, productId, "Đơn giá không hợp lệ!");
            return;
        }

        Double weightValue = null;
        if (weightStr != null && !weightStr.isBlank()) {
            try {
                weightValue = Double.parseDouble(weightStr);
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Khối lượng quy cách không hợp lệ!");
                return;
            }
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

        ProductVariant v = new ProductVariant();
        v.setVariantId(variantId);
        v.setProductId(productId);
        v.setSku(sku != null ? sku.trim().toUpperCase() : "");
        v.setPackagingType(packagingType != null ? packagingType.trim() : "");
        v.setUnit((unit != null && !unit.isBlank()) ? unit : "kg");
        v.setWeightValue(weightValue);
        v.setPrice(price);

        try {
            productService.updateVariant(v, stock);
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

        String variantIdStr = request.getParameter("id");
        String productIdStr = request.getParameter("productId");

        if (variantIdStr == null || productIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        int variantId, productId;
        try {
            variantId = Integer.parseInt(variantIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        try {
            productService.deleteVariant(variantId, productId);
            response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=deleted");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectWithError(request, response, productId, e.getMessage());
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, int productId, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&errorMsg=" + encoded);
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
