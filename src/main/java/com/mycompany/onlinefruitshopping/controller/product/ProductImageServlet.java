package com.mycompany.onlinefruitshopping.controller.product;

import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductImage;
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
 * Controller Quản lý Thư viện Hình ảnh Sản phẩm.
 * Tuân thủ mô hình 3 tầng: Sử dụng ProductService để xử lý nghiệp vụ.
 */
@WebServlet(urlPatterns = {"/store-owner/product-images", "/admin/product-images"})
public class ProductImageServlet extends HttpServlet {

    private final ProductService productService;

    public ProductImageServlet() {
        this.productService = new ProductService();
    }

    public ProductImageServlet(ProductService productService) {
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
                    handleDeleteImage(request, response);
                    break;
                case "setPrimary":
                    handleSetPrimary(request, response);
                    break;
                case "list":
                default:
                    showImageList(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in ProductImageServlet", e);
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
                    handleAddImage(request, response);
                    break;
                default:
                    response.sendRedirect(request.getRequestURI());
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in ProductImageServlet", e);
        }
    }

    // =========================================================
    // 1. SHOW IMAGES FOR PRODUCT
    // =========================================================
    private void showImageList(HttpServletRequest request, HttpServletResponse response)
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

        List<ProductImage> images = productService.getProductImages(productId);

        request.setAttribute("product", product);
        request.setAttribute("images", images);
        request.getRequestDispatcher("/product-images.jsp").forward(request, response);
    }

    // =========================================================
    // 2. ADD PRODUCT IMAGE
    // =========================================================
    private void handleAddImage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String productIdStr = request.getParameter("productId");
        String imageUrl = request.getParameter("imageUrl");
        String sortOrderStr = request.getParameter("sortOrder");

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        int sortOrder = 1;
        if (sortOrderStr != null && !sortOrderStr.isBlank()) {
            try {
                int s = Integer.parseInt(sortOrderStr);
                if (s >= 1) sortOrder = s;
            } catch (NumberFormatException ignored) {}
        }

        ProductImage img = new ProductImage();
        img.setProductId(productId);
        img.setImageUrl(imageUrl != null ? imageUrl.trim() : null);
        img.setSortOrder(sortOrder);

        try {
            productService.addImage(img);
            response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=added");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, productId, e.getMessage());
        }
    }

    // =========================================================
    // 3. DELETE PRODUCT IMAGE
    // =========================================================
    private void handleDeleteImage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String imageIdStr = request.getParameter("id");
        String productIdStr = request.getParameter("productId");

        if (imageIdStr == null || productIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        int imageId, productId;
        try {
            imageId = Integer.parseInt(imageIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        try {
            productService.deleteImage(imageId);
            response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=deleted");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, productId, e.getMessage());
        }
    }

    // =========================================================
    // 4. SET PRIMARY IMAGE
    // =========================================================
    private void handleSetPrimary(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String imageIdStr = request.getParameter("id");
        String productIdStr = request.getParameter("productId");

        if (imageIdStr == null || productIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        int imageId, productId;
        try {
            imageId = Integer.parseInt(imageIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        try {
            productService.setPrimaryImage(productId, imageId);
            response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=setprimary");
        } catch (IllegalArgumentException e) {
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
