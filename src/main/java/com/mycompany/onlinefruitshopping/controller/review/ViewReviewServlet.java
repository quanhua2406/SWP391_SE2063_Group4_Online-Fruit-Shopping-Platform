package com.mycompany.onlinefruitshopping.controller.review;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.mycompany.onlinefruitshopping.service.ReviewService;
import com.mycompany.onlinefruitshopping.model.Review;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * ViewReviewServlet – Controller hiển thị danh sách đánh giá cho một sản phẩm.
 * Gọi ReviewService để xử lý logic nghiệp vụ và lấy dữ liệu.
 *
 * @author Admin
 */
@WebServlet(name = "ViewReviewServlet", urlPatterns = {"/ViewReviewServlet", "/view-review", "/reviews", "/review"})
public class ViewReviewServlet extends HttpServlet {

    private static final int PAGE_SIZE = 5; // Số đánh giá mỗi trang
    private ReviewService reviewService;

    @Override
    public void init() {
        this.reviewService = new ReviewService();
    }

    // ----------------------------------------------------------------
    // GET – hiển thị trang danh sách đánh giá
    // ----------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // ---- 1. Đọc và chuẩn hóa tham số HTTP -------------------------
        int productId = parseIntSafe(request.getParameter("productId"), 0);
        int filter    = parseIntSafe(request.getParameter("filter"),    0);
        int page      = parseIntSafe(request.getParameter("page"),      1);

        boolean reviewSent    = "1".equals(request.getParameter("reviewSent"));
        boolean reviewUpdated = "1".equals(request.getParameter("reviewUpdated"));
        boolean reviewDeleted = "1".equals(request.getParameter("reviewDeleted"));
        boolean expired       = "1".equals(request.getParameter("expired"));
        boolean authError     = "1".equals(request.getParameter("authError"));
        boolean deleteError   = "1".equals(request.getParameter("deleteError"));

        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        if (page < 1) page = 1;
        if (filter != -1 && (filter < 0 || filter > 5)) {
            filter = 0;
        }

        // ---- 2. Gọi Service xử lý logic và truy vấn ------------------
        double[] stats = reviewService.getRatingStats(productId);
        int totalCount = reviewService.countReviews(productId, filter);
        int totalPages = reviewService.calculateTotalPages(totalCount, PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        List<Review> reviewList = reviewService.getReviews(productId, filter, page, PAGE_SIZE);
        String productName = reviewService.getProductName(productId);
        String productImage = reviewService.getProductImage(productId);

        // ---- Đặt attribute vào request và forward ---------------------
        request.setAttribute("reviewList",    reviewList);
        request.setAttribute("stats",         stats);
        request.setAttribute("productId",     productId);
        request.setAttribute("productName",   productName);
        request.setAttribute("productImage",  productImage);
        request.setAttribute("currentPage",   page);
        request.setAttribute("totalPages",    totalPages);
        request.setAttribute("totalCount",    totalCount);
        request.setAttribute("filter",        filter);
        request.setAttribute("reviewSent",    reviewSent);
        request.setAttribute("reviewUpdated", reviewUpdated);
        request.setAttribute("reviewDeleted", reviewDeleted);
        request.setAttribute("expired",       expired);
        request.setAttribute("authError",     authError);
        request.setAttribute("deleteError",   deleteError);

        request.getRequestDispatcher("/review/viewProductReview.jsp")
                .forward(request, response);
    }

    // ----------------------------------------------------------------
    // Helper: parseInt với giá trị mặc định nếu lỗi
    // ----------------------------------------------------------------

    private int parseIntSafe(String s, int defaultVal) {
        if (s == null || s.isBlank()) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}

