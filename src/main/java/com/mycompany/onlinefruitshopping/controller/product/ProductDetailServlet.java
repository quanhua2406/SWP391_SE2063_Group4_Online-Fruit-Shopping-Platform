package com.mycompany.onlinefruitshopping.controller.product;

import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductImage;
import com.mycompany.onlinefruitshopping.model.ProductVariant;
import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.ProductService;
import com.mycompany.onlinefruitshopping.service.ReviewService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller hiển thị chi tiết sản phẩm.
 * Sử dụng ProductService để lấy thông tin sản phẩm, biến thể quy cách, thư viện ảnh và sản phẩm liên quan.
 * Sử dụng ReviewService để nạp toàn bộ dữ liệu đánh giá, phân bổ điểm sao và quyền đánh giá của khách hàng.
 */
@WebServlet(urlPatterns = {"/product", "/product-detail"})
public class ProductDetailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ProductDetailServlet.class.getName());
    private static final int REVIEW_PAGE_SIZE = 5;

    private final ProductService productService;
    private final ReviewService reviewService;

    public ProductDetailServlet() {
        this.productService = new ProductService();
        this.reviewService = new ReviewService();
    }

    public ProductDetailServlet(ProductService productService, ReviewService reviewService) {
        this.productService = productService;
        this.reviewService = reviewService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            idParam = request.getParameter("productId");
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

        // Đọc tham số lọc review và phân trang
        int filter = parseIntSafe(request.getParameter("filter"), 0);
        int reviewPage = parseIntSafe(request.getParameter("page"), 1);
        if (reviewPage < 1) reviewPage = 1;
        if (filter != -1 && (filter < 0 || filter > 5)) {
            filter = 0;
        }

        boolean reviewSent    = "1".equals(request.getParameter("reviewSent"));
        boolean reviewUpdated = "1".equals(request.getParameter("reviewUpdated"));
        boolean reviewDeleted = "1".equals(request.getParameter("reviewDeleted"));
        boolean expired       = "1".equals(request.getParameter("expired"));
        boolean authError     = "1".equals(request.getParameter("authError"));
        boolean deleteError   = "1".equals(request.getParameter("deleteError"));

        try {
            Product product = productService.getProductDetail(productId);
            if (product == null || !"published".equalsIgnoreCase(product.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            // 1. Lấy danh sách quy cách đóng gói và tồn kho qua Service
            List<ProductVariant> variants = productService.getProductVariants(productId);

            // 2. Lấy thư viện hình ảnh qua Service
            List<ProductImage> images = productService.getProductImages(productId);

            // 3. Lấy danh sách sản phẩm cùng loại gợi ý qua Service
            List<Product> relatedProducts = productService.getRelatedProducts(product.getCategoryId(), productId, 4);

            // 4. Lấy dữ liệu đánh giá sản phẩm qua ReviewService
            double[] stats = reviewService.getRatingStats(productId);
            double avgRating = (stats != null && stats.length > 0) ? stats[0] : 0.0;
            int totalReviews = (stats != null && stats.length > 1) ? (int) stats[1] : 0;

            int filteredCount = reviewService.countReviews(productId, filter);
            int totalReviewPages = reviewService.calculateTotalPages(filteredCount, REVIEW_PAGE_SIZE);
            if (reviewPage > totalReviewPages) reviewPage = totalReviewPages;

            List<Review> reviewList = reviewService.getReviews(productId, filter, reviewPage, REVIEW_PAGE_SIZE);

            // 5. Kiểm tra quyền đánh giá của user hiện tại
            Integer currentUserId = null;
            HttpSession session = request.getSession(false);
            if (session != null) {
                currentUserId = (Integer) session.getAttribute("userId");
                if (currentUserId == null) {
                    Object userObj = session.getAttribute("user");
                    if (userObj instanceof User) {
                        currentUserId = ((User) userObj).getUserID();
                    }
                }
            }

            boolean canReview = false;
            int eligibleOrderId = 0;
            if (currentUserId != null && currentUserId > 0) {
                eligibleOrderId = reviewService.findDeliveredOrderId(currentUserId, productId);
                if (eligibleOrderId > 0) {
                    canReview = true;
                }
            }

            // Gắn thông tin sản phẩm
            request.setAttribute("product", product);
            request.setAttribute("variants", variants);
            request.setAttribute("images", images);
            request.setAttribute("relatedProducts", relatedProducts);

            // Gắn thông tin đánh giá
            request.setAttribute("reviewList", reviewList);
            request.setAttribute("stats", stats);
            request.setAttribute("avgRating", avgRating);
            request.setAttribute("totalReviews", totalReviews);
            request.setAttribute("reviewFilter", filter);
            request.setAttribute("currentPage", reviewPage);
            request.setAttribute("totalPages", totalReviewPages);
            request.setAttribute("currentUserId", currentUserId);
            request.setAttribute("canReview", canReview);
            request.setAttribute("eligibleOrderId", eligibleOrderId);

            // Cờ thông báo trạng thái
            request.setAttribute("reviewSent", reviewSent);
            request.setAttribute("reviewUpdated", reviewUpdated);
            request.setAttribute("reviewDeleted", reviewDeleted);
            request.setAttribute("expired", expired);
            request.setAttribute("authError", authError);
            request.setAttribute("deleteError", deleteError);

            request.getRequestDispatcher("/product-details.jsp").forward(request, response);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi CSDL khi xem chi tiết sản phẩm ID: " + productId, e);
            throw new ServletException("Lỗi CSDL khi xem chi tiết sản phẩm", e);
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
