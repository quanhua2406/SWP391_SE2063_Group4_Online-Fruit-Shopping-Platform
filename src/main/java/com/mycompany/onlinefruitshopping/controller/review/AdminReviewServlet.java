package com.mycompany.onlinefruitshopping.controller.review;

import com.mycompany.onlinefruitshopping.dao.review.ReviewDAO;
import com.mycompany.onlinefruitshopping.dao.review.ReviewReportDAO;
import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.ReviewReport;
import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Servlet dành riêng cho Admin quản lý (moderate) reviews.
 *
 * GET  /admin/reviews          → danh sách review (lọc theo status)
 * POST /admin/reviews/approve  → duyệt review (published)
 * POST /admin/reviews/reject   → từ chối review (rejected)
 * POST /admin/reviews/delete   → soft-delete review (deleted)
 */
@WebServlet("/admin/reviews/*")
public class AdminReviewServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final ReviewDAO       reviewDAO = new ReviewDAO();
    private final ReviewReportDAO reportDAO = new ReviewReportDAO();

    // ─────────────────────────────────────────────────────────────────────
    // GET: Danh sách review cho Admin
    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) return;

        String status   = request.getParameter("status");   // null | reported | rejected | deleted
        String pageStr  = request.getParameter("page");
        int page = 1;
        try { if (pageStr != null) page = Integer.parseInt(pageStr); } catch (NumberFormatException ignored) {}
        if (page < 1) page = 1;

        List<Review> reviews = reviewDAO.getReviewsForAdmin(status, page, PAGE_SIZE);
        int total    = reviewDAO.countReviewsForAdmin(status);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

        request.setAttribute("reviews",    reviews);
        request.setAttribute("total",      total);
        request.setAttribute("page",       page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("status",     status);

        request.getRequestDispatcher("/admin/review/reviewModeration.jsp")
               .forward(request, response);
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST: Xử lý hành động Admin (approve / reject / delete)
    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) return;

        String pathInfo  = request.getPathInfo();   // /approve | /reject | /delete
        String idStr     = request.getParameter("reviewId");
        String backUrl   = request.getContextPath() + "/admin/reviews";

        if (idStr == null) {
            sendError(request, response, backUrl, "Thiếu reviewId.");
            return;
        }

        int reviewId;
        try {
            reviewId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendError(request, response, backUrl, "reviewId không hợp lệ.");
            return;
        }

        // Kiểm tra review tồn tại
        Review review = reviewDAO.getReviewById(reviewId);
        if (review == null) {
            sendError(request, response, backUrl, "Đánh giá không tồn tại.");
            return;
        }

        boolean success;
        String  toastMsg;

        if ("/approve".equals(pathInfo)) {
            // ── Duyệt review ──────────────────────────────────────────────
            success  = reviewDAO.approveReview(reviewId);
            toastMsg = success ? "Đã duyệt đánh giá thành công." : "Không thể duyệt đánh giá.";

        } else if ("/reject".equals(pathInfo)) {
            // ── Từ chối review ────────────────────────────────────────────
            success  = reviewDAO.rejectReview(reviewId);
            toastMsg = success ? "Đã từ chối đánh giá." : "Không thể từ chối đánh giá.";

        } else if ("/delete".equals(pathInfo)) {
            // ── Soft-delete review ────────────────────────────────────────
            success  = reviewDAO.softDeleteReview(reviewId);
            toastMsg = success ? "Đã xóa đánh giá thành công." : "Không thể xóa đánh giá.";

        } else {
            sendError(request, response, backUrl, "Hành động không hợp lệ.");
            return;
        }

        HttpSession session = request.getSession();
        if (success) {
            session.setAttribute("toast_success", toastMsg);
        } else {
            session.setAttribute("toast_error", toastMsg);
        }
        response.sendRedirect(backUrl);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helper: kiểm tra quyền Admin
    // ─────────────────────────────────────────────────────────────────────
    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!"admin".equalsIgnoreCase(user.getRoleName())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập.");
            return false;
        }
        return true;
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp,
                           String backUrl, String message) throws IOException {
        req.getSession().setAttribute("toast_error", message);
        resp.sendRedirect(backUrl);
    }
}
