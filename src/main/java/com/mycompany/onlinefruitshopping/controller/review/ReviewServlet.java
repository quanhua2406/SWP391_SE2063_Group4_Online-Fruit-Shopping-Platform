package com.mycompany.onlinefruitshopping.controller.review;


import com.mycompany.onlinefruitshopping.service.ReviewService;
import com.mycompany.onlinefruitshopping.model.Review;

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
 * ReviewServlet – Controller tiếp nhận form gửi đánh giá sản phẩm mới.
 * Ủy quyền toàn bộ kiểm tra nghiệp vụ và lưu trữ cho ReviewService.
 *
 * @author Admin
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {"/ReviewServlet", "/write-review"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,        // 1 MB
        maxFileSize       = 5 * 1024 * 1024,    // 5 MB / ảnh
        maxRequestSize    = 26 * 1024 * 1024    // 26 MB tổng
)
public class ReviewServlet extends HttpServlet {

    private static final Logger LOG       = Logger.getLogger(ReviewServlet.class.getName());
    private static final int    MAX_FILES = 5;

    private static final List<String> ALLOWED_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/webp");

    private ReviewService reviewService;

    @Override
    public void init() {
        this.reviewService = new ReviewService();
    }

    // ----------------------------------------------------------------
    // GET – mở form viết đánh giá
    // ----------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productIdStr = request.getParameter("productId");
        if (productIdStr == null || productIdStr.isBlank()) {
            productIdStr = request.getParameter("product_id");
        }
        if (productIdStr == null || productIdStr.isBlank()) {
            productIdStr = request.getParameter("id");
        }
        int productId = parseIntSafe(productIdStr, 0);
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }
        request.setAttribute("productId", productId);
        String redirect = request.getParameter("redirect");
        request.setAttribute("redirect", redirect);
        request.getRequestDispatcher("/review/writeProductReview.jsp").forward(request, response);
    }

    // ----------------------------------------------------------------
    // POST – nhận form gửi đánh giá
    // ----------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // ---- BƯỚC 1: Kiểm tra đăng nhập (Session) ---------------------
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

        // ---- BƯỚC 2: Đọc tham số HTTP --------------------------------
        String pidStr = request.getParameter("product_id");
        if (pidStr == null || pidStr.isBlank()) pidStr = request.getParameter("productId");
        if (pidStr == null || pidStr.isBlank()) pidStr = request.getParameter("id");
        int productId = parseIntSafe(pidStr, 0);

        String oidStr = request.getParameter("order_id");
        if (oidStr == null || oidStr.isBlank()) oidStr = request.getParameter("orderId");
        int orderId   = parseIntSafe(oidStr, 0);

        int rating    = parseIntSafe(request.getParameter("rating"),     0);
        String comment= request.getParameter("comment");

        // Nếu chưa có order_id, tự động tìm đơn hàng đã giao thành công của user
        if (orderId <= 0) {
            orderId = reviewService.findDeliveredOrderId(userId, productId);
        }

        // ---- BƯỚC 3: Gọi ReviewService kiểm tra hợp lệ dữ liệu --------
        String validationError = reviewService.validateReviewInput(productId, orderId, rating, comment);
        if (validationError != null) {
            forwardToFormWithError(request, response, validationError, productId, orderId);
            return;
        }

        // ---- BƯỚC 4: Gọi ReviewService kiểm tra điều kiện nghiệp vụ ---
        String eligibilityError = reviewService.checkEligibilityToReview(userId, orderId, productId);
        if (eligibilityError != null) {
            forwardToFormWithError(request, response, eligibilityError, productId, orderId);
            return;
        }

        // ---- BƯỚC 5: Xử lý upload ảnh vật lý -------------------------
        String imageUrlsCsv = null;
        try {
            imageUrlsCsv = saveUploadedImages(request, productId, orderId);
        } catch (IllegalArgumentException ex) {
            forwardToFormWithError(request, response, ex.getMessage(), productId, orderId);
            return;
        }

        // ---- BƯỚC 6: Gọi ReviewService lưu đánh giá -------------------
        Review review = new Review();
        review.setUser_id(userId);
        review.setProduct_id(productId);
        review.setOrder_id(orderId);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        review.setImageUrls(imageUrlsCsv);

        int newId = reviewService.addReview(review);
        if (newId <= 0) {
            forwardToFormWithError(request, response,
                    "Có lỗi xảy ra khi lưu đánh giá. Vui lòng thử lại sau.", productId, orderId);
            return;
        }

        // ---- BƯỚC 7: Điều hướng thông báo thành công ----
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            response.sendRedirect(request.getContextPath() + redirect + (redirect.contains("?") ? "&" : "?") + "reviewSent=1");
            return;
        }
        response.sendRedirect(request.getContextPath()
                + "/product?id=" + productId + "&reviewSent=1#tab-reviews");
    }

    private int parseIntSafe(String s, int defaultVal) {
        if (s == null || s.isBlank()) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    // ----------------------------------------------------------------
    // Helper: lưu file upload, trả về chuỗi URL cách dấu phẩy
    // ----------------------------------------------------------------

    /**
     * Lưu các file ảnh được upload vào thư mục:
     *   [webapp_root]/uploads/reviews/
     *
     * @return Chuỗi URL tương đối, cách nhau dấu phẩy (VD: "uploads/reviews/xxx.jpg,...")
     *         hoặc null nếu không có file nào được upload.
     * @throws IllegalArgumentException nếu file vi phạm giới hạn (loại/số lượng)
     */
    private String saveUploadedImages(HttpServletRequest request, int productId, int orderId)
            throws IOException, ServletException {

        List<String> savedPaths = new ArrayList<>();

        // Thư mục lưu ảnh trên server (tạo nếu chưa tồn tại)
        String uploadDir = getServletContext().getRealPath("") + File.separator
                + "uploads" + File.separator + "reviews";
        Files.createDirectories(Paths.get(uploadDir));

        List<Part> imageParts = new ArrayList<>(request.getParts().stream()
                .filter(p -> "reviewImages".equals(p.getName())
                        && p.getSize() > 0)
                .toList());

        // Kiểm tra số lượng ảnh
        if (imageParts.size() > MAX_FILES) {
            throw new IllegalArgumentException(
                    "Chỉ được upload tối đa " + MAX_FILES + " ảnh. Vui lòng xoá bớt.");
        }

        for (Part part : imageParts) {
            // Kiểm tra Content-Type
            String contentType = part.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Định dạng ảnh không hợp lệ. Chỉ chấp nhận JPG, PNG, WEBP.");
            }

            // Kiểm tra dung lượng (tối đa 5 MB / ảnh – thực ra @MultipartConfig đã giới hạn)
            if (part.getSize() > 5L * 1024 * 1024) {
                throw new IllegalArgumentException(
                        "Mỗi ảnh không được vượt quá 5 MB.");
            }

            // Lấy phần mở rộng từ Content-Type
            String ext = contentType.contains("png") ? ".png"
                    : contentType.contains("webp") ? ".webp" : ".jpg";

            // Tạo tên file duy nhất (UUID) để tránh trùng lặp
            String fileName = "rv_" + productId + "_" + orderId + "_"
                    + UUID.randomUUID().toString().replace("-", "") + ext;

            String filePath = uploadDir + File.separator + fileName;
            try (InputStream in = part.getInputStream()) {
                Files.copy(in, Paths.get(filePath));
            }

            // URL tương đối từ context root (phục vụ <img src="...">)
            savedPaths.add("uploads/reviews/" + fileName);
        }

        return savedPaths.isEmpty() ? null : String.join(",", savedPaths);
    }

    // ----------------------------------------------------------------
    // Helper: forward về form với thông báo lỗi
    // ----------------------------------------------------------------

    private void forwardToFormWithError(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String errorMsg, int productId, int orderId)
            throws ServletException, IOException {
        LOG.log(Level.WARNING, "Review form validation error: {0}", errorMsg);
        request.setAttribute("errorMsg",  errorMsg);
        request.setAttribute("productId", productId);
        request.setAttribute("orderId",   orderId);
        request.getRequestDispatcher("/review/writeProductReview.jsp")
                .forward(request, response);
    }
}

