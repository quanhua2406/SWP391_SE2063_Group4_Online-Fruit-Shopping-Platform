package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.ProductImageDAO;
import com.mycompany.onlinefruitshopping.dao.ProductVariantDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductImage;
import com.mycompany.onlinefruitshopping.model.ProductVariant;

import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.ReviewService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/product", "/product-detail"})
public class ProductDetailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ProductDetailServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final ProductImageDAO imageDAO = new ProductImageDAO();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            idParam = request.getParameter("productId");
        }
        if (idParam == null || idParam.isBlank()) {
            idParam = request.getParameter("product_id");
        }
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        try {
            Product product = productDAO.getProductById(productId);
            if (product == null || !"published".equalsIgnoreCase(product.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            // Get all packaging variants with live stock
            List<ProductVariant> variants = variantDAO.getVariantsByProductId(productId);

            // Get product image gallery
            List<ProductImage> images = imageDAO.getImagesByProductId(productId);

            // Get related products (same category)
            List<Product> relatedProducts = productDAO.getRelatedProducts(product.getCategoryId(), productId, 4);

            // ---- Review & Rating Mapping ----
            double[] stats = reviewService.getRatingStats(productId);
            double avgRating = stats[0];
            int totalReviews = (int) stats[1];

            int filter = parseIntSafe(request.getParameter("filter"), 0);
            if (filter != -1 && (filter < 0 || filter > 5)) {
                filter = 0;
            }
            int page = parseIntSafe(request.getParameter("page"), 1);
            if (page < 1) page = 1;
            int pageSize = 5;

            int filteredReviewCount = reviewService.countReviews(productId, filter);
            int totalReviewPages = reviewService.calculateTotalPages(filteredReviewCount, pageSize);
            if (page > totalReviewPages) page = totalReviewPages;

            List<Review> reviewList = reviewService.getReviews(productId, filter, page, pageSize);

            // Check if logged-in user can write a review (has completed delivery)
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            if (userId == null) {
                User u = (User) request.getSession().getAttribute("user");
                if (u != null) userId = u.getUserID();
            }
            boolean canReview = false;
            int eligibleOrderId = -1;
            if (userId != null && userId > 0) {
                eligibleOrderId = reviewService.findDeliveredOrderId(userId, productId);
                canReview = (eligibleOrderId > 0);
            }

            // Action notification flags
            boolean reviewSent = "1".equals(request.getParameter("reviewSent"));
            boolean reviewUpdated = "1".equals(request.getParameter("reviewUpdated"));
            boolean reviewDeleted = "1".equals(request.getParameter("reviewDeleted"));
            boolean expired = "1".equals(request.getParameter("expired"));
            boolean authError = "1".equals(request.getParameter("authError"));
            boolean deleteError = "1".equals(request.getParameter("deleteError"));

            request.setAttribute("product", product);
            request.setAttribute("variants", variants);
            request.setAttribute("images", images);
            request.setAttribute("relatedProducts", relatedProducts);

            // Rating & Review attributes
            request.setAttribute("stats", stats);
            request.setAttribute("avgRating", avgRating);
            request.setAttribute("totalReviews", totalReviews);
            request.setAttribute("reviewList", reviewList);
            request.setAttribute("reviewFilter", filter);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalReviewPages);
            request.setAttribute("totalCount", filteredReviewCount);
            request.setAttribute("canReview", canReview);
            request.setAttribute("eligibleOrderId", eligibleOrderId);
            request.setAttribute("currentUserId", userId);

            // Toast flags
            request.setAttribute("reviewSent", reviewSent);
            request.setAttribute("reviewUpdated", reviewUpdated);
            request.setAttribute("reviewDeleted", reviewDeleted);
            request.setAttribute("expired", expired);
            request.setAttribute("authError", authError);
            request.setAttribute("deleteError", deleteError);

            request.getRequestDispatcher("/product-details.jsp").forward(request, response);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi nạp chi tiết sản phẩm id: " + productId, e);
            throw new ServletException("Database error in ProductDetailServlet", e);
        }
    }

    private int parseIntSafe(String s, int defaultVal) {
        if (s == null || s.isBlank()) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
