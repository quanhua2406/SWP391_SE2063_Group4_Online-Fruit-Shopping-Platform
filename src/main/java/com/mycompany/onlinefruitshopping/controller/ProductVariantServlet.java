package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.ProductVariantDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductVariant;
import com.mycompany.onlinefruitshopping.model.User;

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

@WebServlet(urlPatterns = {"/store-owner/variants", "/admin/variants"})
public class ProductVariantServlet extends HttpServlet {

    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final ProductDAO productDAO = new ProductDAO();

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

        Product product = productDAO.getProductById(productId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=notfound");
            return;
        }

        List<ProductVariant> variants = variantDAO.getVariantsByProductId(productId);

        request.setAttribute("product", product);
        request.setAttribute("variants", variants);
        request.getRequestDispatcher("/product-variants.jsp").forward(request, response);
    }

    // =========================================================
    // 2. ADD PRODUCT VARIANT (WITH VALIDATION)
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

        // Validation 1: SKU
        if (sku == null || sku.trim().isEmpty()) {
            redirectWithError(request, response, productId, "Mã SKU không được để trống!");
            return;
        }
        String trimmedSku = sku.trim().toUpperCase();
        if (trimmedSku.length() < 3 || trimmedSku.length() > 50) {
            redirectWithError(request, response, productId, "Mã SKU phải từ 3 đến 50 ký tự!");
            return;
        }
        if (variantDAO.isSkuExists(trimmedSku, null)) {
            redirectWithError(request, response, productId, "Mã SKU '" + trimmedSku + "' đã tồn tại!");
            return;
        }

        // Validation 2: Packaging Type
        if (packagingType == null || packagingType.trim().isEmpty()) {
            redirectWithError(request, response, productId, "Quy cách đóng gói không được để trống (Ví dụ: Hộp 500g, Thùng 5kg)!");
            return;
        }
        String trimmedPackaging = packagingType.trim();

        // Validation 3: Unit
        if (unit == null || (!unit.equals("kg") && !unit.equals("box") && !unit.equals("piece"))) {
            unit = "kg";
        }

        // Validation 4: Price
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

        // Validation 5: Weight Value (optional)
        Double weightValue = null;
        if (weightStr != null && !weightStr.isBlank()) {
            try {
                double w = Double.parseDouble(weightStr);
                if (w < 0) {
                    redirectWithError(request, response, productId, "Khối lượng quy cách không được âm!");
                    return;
                }
                weightValue = w;
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Khối lượng quy cách không hợp lệ!");
                return;
            }
        }

        // Validation 6: Stock
        double stock = 0;
        if (stockStr != null && !stockStr.isBlank()) {
            try {
                stock = Double.parseDouble(stockStr);
                if (stock < 0) {
                    redirectWithError(request, response, productId, "Số lượng tồn kho không được âm!");
                    return;
                }
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Số lượng tồn kho không hợp lệ!");
                return;
            }
        }

        ProductVariant v = new ProductVariant();
        v.setProductId(productId);
        v.setSku(trimmedSku);
        v.setPackagingType(trimmedPackaging);
        v.setUnit(unit);
        v.setWeightValue(weightValue);
        v.setPrice(price);

        variantDAO.addVariant(v, stock);
        response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=added");
    }

    // =========================================================
    // 3. EDIT PRODUCT VARIANT (WITH VALIDATION)
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

        int variantId;
        int productId;
        try {
            variantId = Integer.parseInt(variantIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        if (sku == null || sku.trim().isEmpty()) {
            redirectWithError(request, response, productId, "Mã SKU không được để trống!");
            return;
        }
        String trimmedSku = sku.trim().toUpperCase();
        if (variantDAO.isSkuExists(trimmedSku, variantId)) {
            redirectWithError(request, response, productId, "Mã SKU '" + trimmedSku + "' đã trùng với quy cách khác!");
            return;
        }

        if (packagingType == null || packagingType.trim().isEmpty()) {
            redirectWithError(request, response, productId, "Quy cách đóng gói không được để trống!");
            return;
        }

        if (unit == null || (!unit.equals("kg") && !unit.equals("box") && !unit.equals("piece"))) {
            unit = "kg";
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
                double w = Double.parseDouble(weightStr);
                if (w < 0) {
                    redirectWithError(request, response, productId, "Khối lượng quy cách không được âm!");
                    return;
                }
                weightValue = w;
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Khối lượng quy cách không hợp lệ!");
                return;
            }
        }

        double stock = 0;
        if (stockStr != null && !stockStr.isBlank()) {
            try {
                stock = Double.parseDouble(stockStr);
                if (stock < 0) {
                    redirectWithError(request, response, productId, "Số lượng tồn kho không được âm!");
                    return;
                }
            } catch (Exception e) {
                redirectWithError(request, response, productId, "Số lượng tồn kho không hợp lệ!");
                return;
            }
        }

        ProductVariant v = new ProductVariant();
        v.setVariantId(variantId);
        v.setProductId(productId);
        v.setSku(trimmedSku);
        v.setPackagingType(packagingType.trim());
        v.setUnit(unit);
        v.setWeightValue(weightValue);
        v.setPrice(price);

        variantDAO.updateVariant(v, stock);
        response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=updated");
    }

    // =========================================================
    // 4. DELETE PRODUCT VARIANT (WITH STRICT VALIDATION)
    // =========================================================
    private void handleDeleteVariant(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String variantIdStr = request.getParameter("variantId");
        String productIdStr = request.getParameter("productId");

        int variantId;
        int productId;
        try {
            variantId = Integer.parseInt(variantIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        // Strict Check 1: A product must keep at least 1 packaging variant
        int totalVariants = variantDAO.countVariantsByProductId(productId);
        if (totalVariants <= 1) {
            redirectWithError(request, response, productId, "Không thể xóa quy cách này vì sản phẩm bắt buộc phải có ít nhất 1 quy cách đóng gói!");
            return;
        }

        // Strict Check 2: Check if variant is referenced in order items
        if (variantDAO.isVariantInOrders(variantId)) {
            redirectWithError(request, response, productId, "Không thể xóa quy cách này vì đã phát sinh trong các đơn hàng đã đặt của khách!");
            return;
        }

        variantDAO.deleteVariant(variantId);
        response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=deleted");
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
