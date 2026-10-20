package com.mycompany.onlinefruitshopping.controller.review;


import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.service.ReviewService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EditReviewServlet – Xử lý luồng CHỈNH SỬA ĐÁNH GIÁ SẢN PHẨM (Edit Product Review).
 *
 * URL patterns: /EditReviewServlet, /edit-review
 *
 * Yêu cầu nghiệp vụ:
 *  - GET:
 *      1. Kiểm tra session đăng nhập.
 *      2. Tải đánh giá theo reviewId.
 *      3. Xác thực quyền tác giả (Authorization: session.userId == review.user_id).
 *      4. Kiểm tra thời hạn chỉnh sửa trong vòng 7 ngày kể từ ngày đăng (create_at).
 *      5. Pre-fill dữ liệu cũ sang view editProductReview.jsp.
 *  - POST:
 *      1. Kiểm tra xác thực & quyền sở hữu trên server.
 *      2. Kiểm tra điều kiện <= 7 ngày.
 *      3. Validate tham số: số sao (1-5), tiêu đề, nội dung nhận xét.
 *      4. Quản lý ảnh: giữ lại các ảnh cũ chưa bị xóa + upload ảnh mới (tối đa 5 ảnh).
 *      5. Cập nhật vào CSDL qua ReviewDAO.updateReview.
 *      6. Chuyển hướng về ViewReviewServlet kèm thông báo thành công.
 *
 * @author Admin
 */
@WebServlet(name = "EditReviewServlet", urlPatterns = {"/EditReviewServlet", "/edit-review"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,        // 1 MB
        maxFileSize       = 5 * 1024 * 1024,    // 5 MB / ảnh
        maxRequestSize    = 26 * 1024 * 1024    // 26 MB tổng
)
public class EditReviewServlet extends HttpServlet {

    private static final Logger LOG       = Logger.getLogger(EditReviewServlet.class.getName());
    private static final int    MAX_FILES = 5;

    private final ReviewService reviewService = new ReviewService();

    /** Các định dạng ảnh được phép upload. */
    private static final List<String> ALLOWED_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/webp");

    // ----------------------------------------------------------------
    // GET: Hiển thị form chỉnh sửa kèm dữ liệu pre-fill
    // ----------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
            String redirectUrl = request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            response.sendRedirect(request.getContextPath() + "/login?redirect="
                    + java.net.URLEncoder.encode(redirectUrl, "UTF-8"));
            return;
        }

        // 2. Đọc tham số reviewId
        int reviewId = parseIntSafe(request.getParameter("reviewId"), 0);

        // 3. Gọi Service kiểm tra quyền tác giả và thời hạn 7 ngày
        ReviewService.EditPermissionResult result = reviewService.checkAndGetReviewForEdit(reviewId, userId);
        if (!result.isSuccess) {
            if ("AUTH_ERROR".equals(result.errorMessage) && result.review != null) {
                response.sendRedirect(request.getContextPath()
                        + "/ViewReviewServlet?productId=" + result.review.getProduct_id() + "&authError=1");
            } else if ("EXPIRED".equals(result.errorMessage) && result.review != null) {
                response.sendRedirect(request.getContextPath()
                        + "/ViewReviewServlet?productId=" + result.review.getProduct_id() + "&expired=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }
            return;
        }

        // 4. Chuyển tiếp sang view editProductReview.jsp với dữ liệu đã chuẩn bị từ Service
        request.setAttribute("review", result.review);
        request.getRequestDispatcher("/review/editProductReview.jsp").forward(request, response);
    }

    // ----------------------------------------------------------------
    // POST: Lưu thay đổi đánh giá
    // ----------------------------------------------------------------

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

        // 2. Đọc các tham số cơ bản
        int reviewId  = parseIntSafe(request.getParameter("review_id"), 0);
        int rating    = parseIntSafe(request.getParameter("rating"), 0);
        String title  = request.getParameter("title");
        String comment= request.getParameter("comment");

        // 3. Kiểm tra quyền sở hữu & tính hợp lệ thông qua Service
        ReviewService.EditPermissionResult perm = reviewService.checkAndGetReviewForEdit(reviewId, userId);
        if (!perm.isSuccess) {
            if ("EXPIRED".equals(perm.errorMessage) && perm.review != null) {
                response.sendRedirect(request.getContextPath()
                        + "/ViewReviewServlet?productId=" + perm.review.getProduct_id() + "&expired=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }
            return;
        }

        Review originalReview = perm.review;

        // 4. Validate nghiệp vụ nội dung nhận xét qua Service
        String errorMsg = reviewService.validateReviewInput(rating, comment);

        // 5. Xử lý danh sách ảnh: Ảnh cũ giữ lại + Ảnh mới upload
        String[] keptImages = request.getParameterValues("keptImages");
        List<String> finalImageList = new ArrayList<>();
        if (keptImages != null) {
            for (String img : keptImages) {
                String cleanImg = img.trim();
                if (!cleanImg.isEmpty() && originalReview.getImageUrls() != null
                        && originalReview.getImageUrls().contains(cleanImg)) {
                    finalImageList.add(cleanImg);
                }
            }
        }

        List<Part> newFileParts = new ArrayList<>();
        try {
            for (Part part : request.getParts()) {
                if ("newImages".equals(part.getName()) && part.getSize() > 0) {
                    newFileParts.add(part);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error reading multipart request parts", ex);
        }

        if (errorMsg == null && (finalImageList.size() + newFileParts.size() > MAX_FILES)) {
            errorMsg = "Tổng số ảnh (ảnh cũ và ảnh mới) tối đa là 5 ảnh. Vui lòng xóa bớt ảnh trước khi lưu.";
        }

        if (errorMsg != null) {
            originalReview.setRating(rating);
            originalReview.setTitle(title != null ? title.trim() : "");
            originalReview.setComment(comment != null ? comment.trim() : "");

            request.setAttribute("review", originalReview);
            request.setAttribute("errorMsg", errorMsg);
            request.getRequestDispatcher("/review/editProductReview.jsp").forward(request, response);
            return;
        }

        // 6. Upload ảnh mới
        try {
            List<String> newUploadedPaths = saveNewUploadedImages(newFileParts, originalReview.getProduct_id(), originalReview.getOrder_id());
            finalImageList.addAll(newUploadedPaths);
        } catch (IllegalArgumentException ex) {
            originalReview.setRating(rating);
            originalReview.setTitle(title != null ? title.trim() : "");
            originalReview.setComment(comment != null ? comment.trim() : "");

            request.setAttribute("review", originalReview);
            request.setAttribute("errorMsg", ex.getMessage());
            request.getRequestDispatcher("/review/editProductReview.jsp").forward(request, response);
            return;
        }

        // 7. Gán dữ liệu mới và cập nhật qua Service
        originalReview.setRating(rating);
        originalReview.setTitle(title != null ? title.trim() : "");
        originalReview.setComment(comment != null ? comment.trim() : "");
        originalReview.setImageUrls(finalImageList.isEmpty() ? null : String.join(",", finalImageList));

        boolean success = reviewService.updateReview(originalReview, userId);

        if (success) {
            response.sendRedirect(request.getContextPath()
                    + "/ViewReviewServlet?productId=" + originalReview.getProduct_id() + "&reviewUpdated=1");
        } else {
            request.setAttribute("review", originalReview);
            request.setAttribute("errorMsg", "Không thể cập nhật đánh giá. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/review/editProductReview.jsp").forward(request, response);
        }
    }

    // ----------------------------------------------------------------
    // Helper Methods
    // ----------------------------------------------------------------

    /**
     * Lưu các file ảnh mới upload vào thư mục webapp_root/uploads/reviews/
     */
    private List<String> saveNewUploadedImages(List<Part> parts, int productId, int orderId)
            throws IOException {
        List<String> savedPaths = new ArrayList<>();
        if (parts == null || parts.isEmpty()) return savedPaths;

        String uploadDir = getServletContext().getRealPath("") + File.separator
                + "uploads" + File.separator + "reviews";
        Files.createDirectories(Paths.get(uploadDir));

        for (Part part : parts) {
            String contentType = part.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException("Định dạng ảnh không hợp lệ (" + part.getSubmittedFileName() + "). Chỉ hỗ trợ JPG, PNG, WEBP.");
            }

            if (part.getSize() > 5L * 1024 * 1024) {
                throw new IllegalArgumentException("Ảnh " + part.getSubmittedFileName() + " vượt quá dung lượng tối đa 5 MB.");
            }

            String ext = contentType.contains("png") ? ".png"
                    : contentType.contains("webp") ? ".webp" : ".jpg";

            String fileName = "rv_" + productId + "_" + orderId + "_"
                    + UUID.randomUUID().toString().replace("-", "") + ext;

            String filePath = uploadDir + File.separator + fileName;
            try (InputStream in = part.getInputStream()) {
                Files.copy(in, Paths.get(filePath));
            }

            savedPaths.add("uploads/reviews/" + fileName);
        }
        return savedPaths;
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
