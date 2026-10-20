package com.mycompany.onlinefruitshopping.controller.review;


import com.mycompany.onlinefruitshopping.service.ReviewService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * DeleteReviewServlet – Xử lý chức năng XÓA ĐÁNH GIÁ SẢN PHẨM (Delete Product Review).
 *
 * URL patterns: /DeleteReviewServlet, /delete-review
 *
 * @author Admin
 */
@WebServlet(name = "DeleteReviewServlet", urlPatterns = {"/DeleteReviewServlet", "/delete-review"})
public class DeleteReviewServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DeleteReviewServlet.class.getName());
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra đăng nhập
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            com.mycompany.onlinefruitshopping.model.User u =
                    (com.mycompany.onlinefruitshopping.model.User) request.getSession().getAttribute("user");
            if (u != null) {
                userId = u.getUserID();
            }
        }
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. Đọc tham số
        int reviewId  = parseIntSafe(request.getParameter("review_id"), 0);
        int productId = parseIntSafe(request.getParameter("product_id"), 0);

        if (reviewId <= 0) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        // 3. Gọi ReviewService xử lý toàn bộ logic: kiểm tra quyền tác giả, dọn dẹp file ảnh đĩa và xóa bản ghi DB
        String webappRoot = getServletContext().getRealPath("");
        boolean success = reviewService.deleteReview(reviewId, userId, webappRoot);

        // 4. Chuyển hướng về trang danh sách đánh giá kèm thông báo
        if (success) {
            response.sendRedirect(request.getContextPath()
                    + "/ViewReviewServlet?productId=" + productId + "&reviewDeleted=1");
        } else {
            response.sendRedirect(request.getContextPath()
                    + "/ViewReviewServlet?productId=" + productId + "&authError=1");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cho phép chuyển tiếp từ GET nếu có tham số
        doPost(request, response);
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

