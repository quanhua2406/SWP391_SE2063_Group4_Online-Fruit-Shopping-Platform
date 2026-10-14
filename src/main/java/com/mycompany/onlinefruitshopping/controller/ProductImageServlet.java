package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.ProductImageDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductImage;
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

@WebServlet(urlPatterns = {"/store-owner/product-images", "/admin/product-images"})
public class ProductImageServlet extends HttpServlet {

    private final ProductImageDAO imageDAO = new ProductImageDAO();
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

        Product product = productDAO.getProductById(productId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=notfound");
            return;
        }

        List<ProductImage> images = imageDAO.getImagesByProductId(productId);

        request.setAttribute("product", product);
        request.setAttribute("images", images);
        request.getRequestDispatcher("/product-images.jsp").forward(request, response);
    }

    // =========================================================
    // 2. ADD PRODUCT IMAGE (WITH URL VALIDATION)
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

        // Validation 1: Blank check
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            redirectWithError(request, response, productId, "Đường dẫn hình ảnh không được để trống!");
            return;
        }

        String trimmedUrl = imageUrl.trim();

        // Validation 2: URL format check
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://") && !trimmedUrl.startsWith("/")) {
            redirectWithError(request, response, productId, "Đường dẫn ảnh phải bắt đầu bằng http://, https:// hoặc đường dẫn hợp lệ!");
            return;
        }

        // Validation 3: Length check
        if (trimmedUrl.length() > 255) {
            redirectWithError(request, response, productId, "Đường dẫn ảnh quá dài (tối đa 255 ký tự)!");
            return;
        }

        int sortOrder = 2; // Default secondary image
        if (sortOrderStr != null && !sortOrderStr.isBlank()) {
            try {
                int so = Integer.parseInt(sortOrderStr);
                if (so >= 1) {
                    sortOrder = so;
                }
            } catch (NumberFormatException ignored) {}
        }

        ProductImage img = new ProductImage();
        img.setProductId(productId);
        img.setImageUrl(trimmedUrl);
        img.setSortOrder(sortOrder);

        imageDAO.addImage(img);
        response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=added");
    }

    // =========================================================
    // 3. SET PRIMARY IMAGE
    // =========================================================
    private void handleSetPrimary(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String imageIdStr = request.getParameter("imageId");
        String productIdStr = request.getParameter("productId");

        int imageId;
        int productId;
        try {
            imageId = Integer.parseInt(imageIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        imageDAO.setPrimaryImage(productId, imageId);
        response.sendRedirect(request.getRequestURI() + "?productId=" + productId + "&success=primary");
    }

    // =========================================================
    // 4. DELETE IMAGE (WITH VALIDATION)
    // =========================================================
    private void handleDeleteImage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String imageIdStr = request.getParameter("imageId");
        String productIdStr = request.getParameter("productId");

        int imageId;
        int productId;
        try {
            imageId = Integer.parseInt(imageIdStr);
            productId = Integer.parseInt(productIdStr);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/store-owner/products?error=invalid_id");
            return;
        }

        // Validation: A product should maintain at least 1 image
        int totalImages = imageDAO.countImagesByProductId(productId);
        if (totalImages <= 1) {
            redirectWithError(request, response, productId, "Không thể xóa hình ảnh duy nhất này! Sản phẩm cần ít nhất 1 hình ảnh đại diện.");
            return;
        }

        imageDAO.deleteImage(imageId);
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
