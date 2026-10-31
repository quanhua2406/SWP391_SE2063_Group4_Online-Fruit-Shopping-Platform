package com.mycompany.onlinefruitshopping.controller.customer;

import com.mycompany.onlinefruitshopping.model.PendingReviewItem;
import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.ReviewService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller phục vụ trang "My Reviews" trong Customer Dashboard.
 * Cho phép khách hàng quản lý tất cả các đánh giá đã gửi và xem sản phẩm chờ đánh giá.
 */
@WebServlet(name = "CustomerReviewsServlet", urlPatterns = {"/customer/reviews", "/my-reviews"})
public class CustomerReviewsServlet extends HttpServlet {

    private static final int PAGE_SIZE = 6;
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra đăng nhập
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            User u = (User) request.getSession().getAttribute("user");
            if (u != null) {
                userId = u.getUserID();
            }
        }
        if (userId == null || userId <= 0) {
            String redirectUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            response.sendRedirect(request.getContextPath() + "/login?redirect=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
            return;
        }

        // 2. Đọc tab và trang
        String tab = request.getParameter("tab");
        if (tab == null || (!"reviewed".equals(tab) && !"pending".equals(tab))) {
            tab = "reviewed";
        }

        int page = parseIntSafe(request.getParameter("page"), 1);
        if (page < 1) page = 1;

        // 3. Lấy dữ liệu đánh giá của khách hàng
        int totalReviewed = reviewService.countReviewsByUser(userId);
        int totalPages = reviewService.calculateTotalPages(totalReviewed, PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        List<Review> reviewedList = reviewService.getReviewsByUser(userId, page, PAGE_SIZE);
        List<PendingReviewItem> pendingList = reviewService.getPendingReviewsByUser(userId);

        // 4. Nhận các cờ phản hồi toast
        boolean reviewSent    = "1".equals(request.getParameter("reviewSent"));
        boolean reviewUpdated = "1".equals(request.getParameter("reviewUpdated"));
        boolean reviewDeleted = "1".equals(request.getParameter("reviewDeleted"));
        boolean deleteError   = "1".equals(request.getParameter("deleteError"));
        boolean expired       = "1".equals(request.getParameter("expired"));
        boolean authError     = "1".equals(request.getParameter("authError"));

        request.setAttribute("currentTab", tab);
        request.setAttribute("reviewedList", reviewedList);
        request.setAttribute("pendingList", pendingList);
        request.setAttribute("totalReviewed", totalReviewed);
        request.setAttribute("totalPending", pendingList != null ? pendingList.size() : 0);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("reviewSent", reviewSent);
        request.setAttribute("reviewUpdated", reviewUpdated);
        request.setAttribute("reviewDeleted", reviewDeleted);
        request.setAttribute("deleteError", deleteError);
        request.setAttribute("expired", expired);
        request.setAttribute("authError", authError);

        request.getRequestDispatcher("/jsp/customer/my-reviews.jsp").forward(request, response);
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
