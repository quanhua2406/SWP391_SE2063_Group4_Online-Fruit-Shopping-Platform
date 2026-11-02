package com.mycompany.onlinefruitshopping.controller.review;

import com.mycompany.onlinefruitshopping.dao.review.ReviewDAO;
import com.mycompany.onlinefruitshopping.dao.review.ReviewReportDAO;
import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.ReviewReport;
import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet cho phép Customer / Shop Owner báo cáo (report) một review.
 * URL: POST /review/report
 *
 * Params (multipart/form-data):
 *   - reviewId       (int)
 *   - reason         (spam | inappropriate_language | fake_review | other)
 *   - note           (String, tuỳ chọn)
 *   - redirect       (String, tuỳ chọn) – URL quay lại sau khi xử lý
 *   - evidenceImages (File[], tuỳ chọn) – tối đa 3 ảnh bằng chứng
 */
@WebServlet("/review/report")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB – ghi vào disk khi vượt ngưỡng
    maxFileSize       = 5 * 1024 * 1024,  // 5 MB / file
    maxRequestSize    = 16 * 1024 * 1024  // 16 MB toàn request
)
public class ReportReviewServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReportReviewServlet.class.getName());

    /** Thư mục lưu ảnh bằng chứng (tương đối với webapp root) */
    private static final String UPLOAD_DIR    = "uploads" + File.separator + "reports";
    private static final int    MAX_IMAGES    = 3;
    private static final List<String> ALLOWED  = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final ReviewDAO       reviewDAO = new ReviewDAO();
    private final ReviewReportDAO reportDAO = new ReviewReportDAO();

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

            // ── 7. Xử lý upload ảnh bằng chứng (tuỳ chọn) ───────────────
            String evidenceUrls = handleEvidenceUploads(request);

            // ── 8. Lưu báo cáo ───────────────────────────────────────────
            ReviewReport report = new ReviewReport(
                    reviewId,
                    currentUser.getUserID(),
                    reason,
                    (note != null && !note.isBlank()) ? note.trim() : null,
                    evidenceUrls
            );
            int newId = reportDAO.createReport(report);
            if (newId < 0) {
                sendError(request, response, backUrl, "Không thể lưu báo cáo. Vui lòng thử lại.");
                return;
            }

            // ── 9. Cập nhật status review -> reported ─────────────────────
            if ("published".equals(review.getStatus())) {
                reviewDAO.updateStatus(reviewId, "reported");
            }

            // ── 10. Thành công ─────────────────────────────────────────────
            session.setAttribute("toast_success", "Báo cáo đã được ghi nhận. Cảm ơn bạn!");
            response.sendRedirect(backUrl);

        } catch (SQLException e) {
            getServletContext().log("ReportReviewServlet DB error", e);
            sendError(request, response, backUrl, "Lỗi hệ thống. Vui lòng thử lại sau.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Xử lý upload ảnh bằng chứng, trả về chuỗi CSV các đường dẫn ảnh
    // (relative URL, e.g. "uploads/reports/rp_uuid1.jpg,uploads/reports/rp_uuid2.png")
    // Trả về null nếu không có ảnh hoặc xảy ra lỗi
    // ─────────────────────────────────────────────────────────────────────
    private String handleEvidenceUploads(HttpServletRequest request) {
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        List<String> savedUrls = new ArrayList<>();
        try {
            int count = 0;
            for (Part part : request.getParts()) {
                if (!"evidenceImages".equals(part.getName())) continue;
                if (count >= MAX_IMAGES) break;

                String contentType = part.getContentType();
                if (contentType == null || !ALLOWED.contains(contentType.toLowerCase())) continue;
                if (part.getSize() == 0) continue;

                // Xác định extension từ content-type
                String ext = contentType.contains("png")  ? ".png"
                           : contentType.contains("webp") ? ".webp"
                           : contentType.contains("gif")  ? ".gif"
                           : ".jpg";

                String fileName = "rp_" + UUID.randomUUID().toString().replace("-", "") + ext;
                String filePath = uploadPath + File.separator + fileName;

                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, Paths.get(filePath));
                    savedUrls.add("uploads/reports/" + fileName);
                    count++;
                }
            }
        } catch (IOException | ServletException e) {
            LOGGER.log(Level.WARNING, "Evidence upload failed (report still saved)", e);
        }

        return savedUrls.isEmpty() ? null : String.join(",", savedUrls);
    }

    /** Đặt thông báo lỗi vào session và redirect về trang trước. */
    private void sendError(HttpServletRequest req, HttpServletResponse resp,
                           String backUrl, String message) throws IOException {
        req.getSession().setAttribute("toast_error", message);
        resp.sendRedirect(backUrl);
    }
}
