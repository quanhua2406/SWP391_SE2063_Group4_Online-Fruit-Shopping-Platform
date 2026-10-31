package com.mycompany.onlinefruitshopping.controller.review;

import com.mycompany.onlinefruitshopping.dao.NotificationDAO;
import com.mycompany.onlinefruitshopping.dao.review.ReviewDAO;
import com.mycompany.onlinefruitshopping.dao.review.ReviewReportDAO;
import com.mycompany.onlinefruitshopping.model.Notification;
import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.ReviewReportDetailDTO;
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
 * Servlet dành riêng cho Admin quản lý (moderate) reviews & báo cáo vi phạm.
 *
 * GET  /admin/reviews                  → danh sách review (lọc theo status)
 * GET  /admin/reviews/detail           → trang chi tiết báo cáo đánh giá không phù hợp
 * POST /admin/reviews/approve          → duyệt review (published) / Bác bỏ báo cáo
 * POST /admin/reviews/reject-report    → Bác bỏ báo cáo, giữ review hiển thị (published)
 * POST /admin/reviews/hide             → Ẩn đánh giá (rejected)
 * POST /admin/reviews/reject           → từ chối / ẩn review (rejected)
 * POST /admin/reviews/delete           → soft-delete review (deleted)
 * POST /admin/reviews/request-evidence → gửi thông báo yêu cầu thêm bằng chứng
 * POST /admin/reviews/save-notes       → lưu ghi chú nội bộ ban quản trị
 */
@WebServlet("/admin/reviews/*")
public class AdminReviewServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final ReviewDAO       reviewDAO       = new ReviewDAO();
    private final ReviewReportDAO reportDAO       = new ReviewReportDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // ─────────────────────────────────────────────────────────────────────
    // GET: Danh sách review & Chi tiết báo cáo cho Admin
    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) return;

        String pathInfo = request.getPathInfo();

        // ── GET /admin/reviews/detail hoặc /report: Trang chi tiết báo cáo ──
        if ("/detail".equals(pathInfo) || "/report".equals(pathInfo)) {
            String revIdStr = request.getParameter("reviewId");
            if (revIdStr == null || revIdStr.isBlank()) {
                revIdStr = request.getParameter("id");
            }

            int reviewId = -1;
            try {
                if (revIdStr != null) reviewId = Integer.parseInt(revIdStr);
            } catch (NumberFormatException ignored) {}

            if (reviewId <= 0) {
                response.sendRedirect(request.getContextPath() + "/admin/reviews?status=reported");
                return;
            }

            ReviewReportDetailDTO detail = reportDAO.getReportDetailByReviewId(reviewId);
            request.setAttribute("detail", detail);
            request.getRequestDispatcher("/admin/review/reviewReportDetail.jsp")
                   .forward(request, response);
            return;
        }

        // ── GET /admin/reviews: Danh sách kiểm duyệt tổng quan ───────────
        String status   = request.getParameter("status");   // null | reported | rejected | deleted
        String pageStr  = request.getParameter("page");
        int page = 1;
        try { if (pageStr != null) page = Integer.parseInt(pageStr); } catch (NumberFormatException ignored) {}
        if (page < 1) page = 1;

        List<Review> reviews = reviewDAO.getReviewsForAdmin(status, page, PAGE_SIZE);
        int total    = reviewDAO.countReviewsForAdmin(status);
        int totalAll = reviewDAO.countReviewsForAdmin(null);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

        request.setAttribute("reviews",    reviews);
        request.setAttribute("total",      total);
        request.setAttribute("totalAll",   totalAll);
        request.setAttribute("page",       page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("status",     status);

        request.getRequestDispatcher("/admin/review/reviewModeration.jsp")
               .forward(request, response);
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST: Xử lý hành động Admin (hide / delete / reject-report / request-evidence / save-notes)
    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) return;

        String pathInfo  = request.getPathInfo();
        String idStr     = request.getParameter("reviewId");
        String redirect  = request.getParameter("redirect");
        String backUrl   = (redirect != null && !redirect.isBlank())
                ? redirect
                : request.getContextPath() + "/admin/reviews";

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

        boolean success = false;
        String  toastMsg = "";

        if ("/approve".equals(pathInfo) || "/reject-report".equals(pathInfo)) {
            // ── Bác bỏ báo cáo & Duyệt/giữ lại review (published) ─────────
            success  = reviewDAO.approveReview(reviewId);
            toastMsg = success ? "Đã bác bỏ báo cáo và giữ lại đánh giá." : "Không thể duyệt đánh giá.";

        } else if ("/hide".equals(pathInfo) || "/reject".equals(pathInfo)) {
            // ── Ẩn đánh giá (rejected) ────────────────────────────────────
            success  = reviewDAO.rejectReview(reviewId);
            toastMsg = success ? "Đã ẩn đánh giá khỏi trang sản phẩm." : "Không thể ẩn đánh giá.";

        } else if ("/delete".equals(pathInfo)) {
            // ── Xóa đánh giá vĩnh viễn/soft-delete (deleted) ──────────────
            success  = reviewDAO.softDeleteReview(reviewId);
            toastMsg = success ? "Đã xóa đánh giá thành công." : "Không thể xóa đánh giá.";

        } else if ("/request-evidence".equals(pathInfo)) {
            // ── Yêu cầu thêm bằng chứng (gửi notification đến reporter) ──
            String repIdStr = request.getParameter("reporterId");
            int reporterId = 0;
            try {
                if (repIdStr != null) reporterId = Integer.parseInt(repIdStr);
            } catch (NumberFormatException ignored) {}

            if (reporterId > 0) {
                Notification n = new Notification();
                n.setUserId(reporterId);
                n.setTitle("Yêu cầu bổ sung bằng chứng báo cáo đánh giá #RV-" + reviewId);
                n.setContent("Ban quản trị yêu cầu bạn bổ sung thêm bằng chứng hoặc hình ảnh chứng minh vi phạm của đánh giá #" + reviewId + ".");
                n.setType("system");
                n.setReferenceType("review_report");
                n.setReferenceId(String.valueOf(reviewId));
                n.setImportant(true);
                n.setRead(false);
                notificationDAO.insert(n);
                success = true;
                toastMsg = "Đã gửi thông báo yêu cầu bổ sung bằng chứng tới người báo cáo.";
            } else {
                success = true;
                toastMsg = "Đã ghi nhận yêu cầu thêm bằng chứng cho báo cáo này.";
            }

        } else if ("/save-notes".equals(pathInfo)) {
            // ── Lưu ghi chú nội bộ của ban quản trị ─────────────────────────
            String reportIdStr = request.getParameter("reportId");
            String notes = request.getParameter("internalNotes");
            int reportId = 0;
            try {
                if (reportIdStr != null) reportId = Integer.parseInt(reportIdStr);
            } catch (NumberFormatException ignored) {}

            if (reportId > 0) {
                success = reportDAO.updateAdminNotes(reportId, notes != null ? notes.trim() : "");
                toastMsg = success ? "Đã lưu ghi chú nội bộ thành công." : "Không thể lưu ghi chú.";
            } else {
                success = true;
                toastMsg = "Ghi chú nội bộ đã được cập nhật.";
            }

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
