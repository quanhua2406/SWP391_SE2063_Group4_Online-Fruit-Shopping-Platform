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
import java.sql.SQLException;

/**
 * Servlet cho phép Customer / Shop Owner báo cáo (report) một review.
 * URL: POST /review/report
 *
 * Params:
 *   - reviewId  (int)
 *   - reason    (spam | inappropriate_language | fake_review | other)
 *   - note      (String, tuỳ chọn)
 *   - redirect  (String, tuỳ chọn) – URL quay lại sau khi xử lý
 */
@WebServlet("/review/report")
public class ReportReviewServlet extends HttpServlet {

    private final ReviewDAO       reviewDAO       = new ReviewDAO();
    private final ReviewReportDAO reportDAO       = new ReviewReportDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Kiểm tra đăng nhập ────────────────────────────────────────
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // ── 2. Đọc params ─────────────────────────────────────────────────
        String reviewIdStr = request.getParameter("reviewId");
        String reason      = request.getParameter("reason");
        String note        = request.getParameter("note");
        String redirect    = request.getParameter("redirect");

        // Fallback redirect
        String backUrl = (redirect != null && !redirect.isBlank())
                ? redirect
                : request.getContextPath() + "/";

        // ── 3. Validate params ────────────────────────────────────────────
        if (reviewIdStr == null || reason == null) {
            sendError(request, response, backUrl, "Thiếu thông tin báo cáo.");
            return;
        }

        int reviewId;
        try {
            reviewId = Integer.parseInt(reviewIdStr);
        } catch (NumberFormatException e) {
            sendError(request, response, backUrl, "reviewId không hợp lệ.");
            return;
        }

        if (!ReviewReport.isValidReason(reason)) {
            sendError(request, response, backUrl, "Lý do báo cáo không hợp lệ.");
            return;
        }

        // ── 4. Kiểm tra review tồn tại & không bị xóa ────────────────────
        Review review = reviewDAO.getReviewById(reviewId);
        if (review == null || "deleted".equals(review.getStatus())) {
            sendError(request, response, backUrl, "Đánh giá không tồn tại.");
            return;
        }

        // ── 5. Không tự báo cáo review của chính mình ────────────────────
        if (review.getUser_id() == currentUser.getUserID()) {
            sendError(request, response, backUrl, "Bạn không thể báo cáo đánh giá của chính mình.");
            return;
        }

        try {
            // ── 6. Kiểm tra đã báo cáo chưa ─────────────────────────────
            if (reportDAO.hasReported(reviewId, currentUser.getUserID())) {
                sendError(request, response, backUrl, "Bạn đã báo cáo đánh giá này rồi.");
                return;
            }

            // ── 7. Lưu báo cáo ───────────────────────────────────────────
            ReviewReport report = new ReviewReport(
                    reviewId,
                    currentUser.getUserID(),
                    reason,
                    (note != null && !note.isBlank()) ? note.trim() : null
            );
            int newId = reportDAO.createReport(report);
            if (newId < 0) {
                sendError(request, response, backUrl, "Không thể lưu báo cáo. Vui lòng thử lại.");
                return;
            }

            // ── 8. Cập nhật status review -> reported ─────────────────────
            if ("published".equals(review.getStatus())) {
                reviewDAO.updateStatus(reviewId, "reported");
            }

            // ── 9. Thành công ─────────────────────────────────────────────
            session.setAttribute("toast_success", "Báo cáo đã được ghi nhận. Cảm ơn bạn!");
            response.sendRedirect(backUrl);

        } catch (SQLException e) {
            getServletContext().log("ReportReviewServlet DB error", e);
            sendError(request, response, backUrl, "Lỗi hệ thống. Vui lòng thử lại sau.");
        }
    }

    /** Đặt thông báo lỗi vào session và redirect về trang trước. */
    private void sendError(HttpServletRequest req, HttpServletResponse resp,
                           String backUrl, String message) throws IOException {
        req.getSession().setAttribute("toast_error", message);
        resp.sendRedirect(backUrl);
    }
}
