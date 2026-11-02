package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.review.ReviewDAO;
import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.PendingReviewItem;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service Layer xử lý toàn bộ nghiệp vụ (Business Logic) liên quan đến Đánh giá sản phẩm (Reviews).
 *
 * Tách biệt rõ ràng:
 * - Controller (Servlet): Chỉ nhận request HTTP, kiểm tra Session, đọc tham số và điều hướng.
 * - Service: Xử lý logic nghiệp vụ, validation, quy định 7 ngày, quyền sở hữu, tính toán phân trang.
 * - DAO: Chỉ tương tác trực tiếp với Database.
 *
 * @author Admin
 */
public class ReviewService {

    private static final Logger LOG = Logger.getLogger(ReviewService.class.getName());

    /**
     * Thời hạn tối đa cho phép chỉnh sửa đánh giá: 7 ngày (tính bằng mili-giây).
     */
    public static final long EDIT_LIMIT_MILLIS = 7L * 24 * 60 * 60 * 1000L;

    private final ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
    }

    public ReviewService(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    // ----------------------------------------------------------------
    // 1. NGHIỆP VỤ XEM ĐÁNH GIÁ (VIEW REVIEWS)
    // ----------------------------------------------------------------

    /**
     * Lấy mảng thống kê điểm số và phân bố sao của sản phẩm.
     */
    public double[] getRatingStats(int productId) {
        if (productId <= 0) {
            return new double[]{0.0, 0, 0, 0, 0, 0, 0};
        }
        return reviewDAO.getRatingStats(productId);
    }

    /**
     * Lấy danh sách đánh giá có phân trang và lọc theo sao / có ảnh.
     * Tự động tính toán logic nghiệp vụ: tính khả dụng sửa (hạn 7 ngày) và số ngày còn lại.
     */
    public List<Review> getReviews(int productId, int ratingFilter, int page, int pageSize) {
        if (productId <= 0) {
            return new java.util.ArrayList<>();
        }
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 5;
        List<Review> list = reviewDAO.getReviewsByProduct(productId, ratingFilter, page, pageSize);
        if (list != null) {
            for (Review r : list) {
                r.setEditable(isReviewEditable(r));
                r.setRemainingDays(calculateRemainingDays(r));
            }
        }
        return list;
    }

    /**
     * Đếm tổng số đánh giá theo bộ lọc.
     */
    public int countReviews(int productId, int ratingFilter) {
        if (productId <= 0) return 0;
        return reviewDAO.countReviewsByProduct(productId, ratingFilter);
    }

    /**
     * Tính toán tổng số trang dựa trên số bản ghi và kích thước trang.
     */
    public int calculateTotalPages(int totalCount, int pageSize) {
        if (pageSize <= 0) pageSize = 5;
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        return Math.max(1, totalPages);
    }

    /**
     * Lấy tên sản phẩm thực tế từ Database.
     */
    public String getProductName(int productId) {
        if (productId <= 0) return null;
        String name = reviewDAO.getProductNameById(productId);
        return (name != null && !name.trim().isEmpty()) ? name : ("Sản phẩm #" + productId);
    }

    /**
     * Lấy ảnh đại diện sản phẩm từ Database.
     */
    public String getProductImage(int productId) {
        if (productId <= 0) return null;
        return reviewDAO.getProductImageById(productId);
    }

    // ----------------------------------------------------------------
    // 2. NGHIỆP VỤ GỬI ĐÁNH GIÁ MỚI (CREATE REVIEW)
    // ----------------------------------------------------------------

    /**
     * Kiểm tra dữ liệu đánh giá cơ bản (số sao và nội dung bình luận).
     */
    public String validateReviewInput(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            return "Vui lòng chọn số sao đánh giá (1–5 sao).";
        }
        if (comment == null || comment.trim().length() < 20) {
            return "Nội dung đánh giá phải có ít nhất 20 ký tự.";
        }
        if (comment.trim().length() > 1000) {
            return "Nội dung đánh giá không được vượt quá 1000 ký tự.";
        }
        return null;
    }

    /**
     * Kiểm tra dữ liệu đầu vào khi gửi đánh giá mới (bao gồm sản phẩm và đơn hàng).
     * @return Chuỗi thông báo lỗi nếu không hợp lệ, null nếu hợp lệ.
     */
    public String validateReviewInput(int productId, int orderId, int rating, String comment) {
        if (productId <= 0) {
            return "Thông tin sản phẩm không hợp lệ.";
        }
        if (orderId <= 0) {
            return "Không tìm thấy đơn hàng đã giao thành công (status: delivered) của bạn cho sản phẩm này.";
        }
        return validateReviewInput(rating, comment);
    }

    /**
     * Kiểm tra điều kiện nghiệp vụ trước khi người dùng gửi đánh giá:
     * 1. Đơn hàng phải ở trạng thái 'delivered' và thuộc về user.
     * 2. Người dùng chưa từng đánh giá sản phẩm này trong đơn hàng này.
     *
     * @return Thông báo lỗi nếu vi phạm, null nếu đủ điều kiện.
     */
    public String checkEligibilityToReview(int userId, int orderId, int productId) {
        if (!reviewDAO.hasOrderDelivered(userId, orderId)) {
            return "Bạn chỉ có thể đánh giá sau khi đơn hàng đã được giao thành công (status: delivered).";
        }
        if (reviewDAO.hasUserReviewedOrder(userId, orderId, productId)) {
            return "Bạn đã gửi đánh giá cho sản phẩm này trong đơn hàng này rồi.";
        }
        return null;
    }

    /**
     * Tự động tìm mã đơn hàng hợp lệ đã giao của user đối với sản phẩm này.
     */
    public int findDeliveredOrderId(int userId, int productId) {
        if (userId <= 0 || productId <= 0) return -1;
        return reviewDAO.findDeliveredOrderId(userId, productId);
    }

    /**
     * Thêm đánh giá mới vào hệ thống.
     * @return ID đánh giá mới tạo, hoặc -1 nếu thất bại.
     */
    public int addReview(Review review) {
        if (review == null) return -1;
        return reviewDAO.insertReview(review);
    }

    // ----------------------------------------------------------------
    // 3. NGHIỆP VỤ CHỈNH SỬA ĐÁNH GIÁ (EDIT REVIEW)
    // ----------------------------------------------------------------

    public static class EditPermissionResult {
        public boolean isSuccess;
        public String errorMessage;
        public Review review;

        public EditPermissionResult(boolean isSuccess, String errorMessage, Review review) {
            this.isSuccess = isSuccess;
            this.errorMessage = errorMessage;
            this.review = review;
        }
    }

    /**
     * Logic nghiệp vụ: Kiểm tra xem đánh giá có còn trong hạn 7 ngày cho phép sửa hay không.
     * @param review đối tượng đánh giá cần kiểm tra
     * @return true nếu trong vòng 7 ngày kể từ lúc tạo, ngược lại false
     */
    public boolean isReviewEditable(Review review) {
        if (review == null || review.getCreated_at() == null) {
            return false;
        }
        long diffMillis = System.currentTimeMillis() - review.getCreated_at().getTime();
        return diffMillis >= 0 && diffMillis <= EDIT_LIMIT_MILLIS;
    }

    /**
     * Logic nghiệp vụ: Tính số ngày còn lại được phép chỉnh sửa đánh giá.
     * @param review đối tượng đánh giá
     * @return số ngày còn lại (tối thiểu 1 ngày nếu còn trong hạn, 0 nếu hết hạn)
     */
    public long calculateRemainingDays(Review review) {
        if (review == null || review.getCreated_at() == null) {
            return 0;
        }
        long diffMillis = System.currentTimeMillis() - review.getCreated_at().getTime();
        long remainMillis = EDIT_LIMIT_MILLIS - diffMillis;
        if (remainMillis <= 0) {
            return 0;
        }
        return Math.max(1, (remainMillis / (1000L * 60 * 60 * 24)) + 1);
    }

    /**
     * Lấy đánh giá cho form chỉnh sửa và kiểm tra đầy đủ các điều kiện nghiệp vụ:
     * 1. Đánh giá có tồn tại trong hệ thống.
     * 2. Người dùng hiện tại có phải chính chủ đánh giá (Authorization).
     * 3. Đánh giá còn trong thời hạn 7 ngày cho phép sửa hay không.
     */
    public EditPermissionResult checkAndGetReviewForEdit(int reviewId, int userId) {
        if (reviewId <= 0) {
            return new EditPermissionResult(false, "Mã đánh giá không hợp lệ.", null);
        }

        Review review = reviewDAO.getReviewById(reviewId);
        if (review == null) {
            return new EditPermissionResult(false, "Không tìm thấy đánh giá cần chỉnh sửa.", null);
        }

        // Quyền tác giả
        if (review.getUser_id() != userId) {
            return new EditPermissionResult(false, "AUTH_ERROR", review);
        }

        // Tính toán trạng thái nghiệp vụ và inject vào Model Review
        boolean editable = isReviewEditable(review);
        long remaining = calculateRemainingDays(review);
        review.setEditable(editable);
        review.setRemainingDays(remaining);

        // Kiểm tra thời hạn 7 ngày
        if (!editable) {
            return new EditPermissionResult(false, "EXPIRED", review);
        }

        // Bổ sung thông tin sản phẩm hiển thị nếu chưa có
        if (review.getProductName() == null || review.getProductName().trim().isEmpty()) {
            String pName = reviewDAO.getProductNameById(review.getProduct_id());
            review.setProductName(pName != null ? pName : ("Sản phẩm #" + review.getProduct_id()));
        }
        if (review.getProductImage() == null || review.getProductImage().trim().isEmpty()) {
            String pImg = reviewDAO.getProductImageById(review.getProduct_id());
            review.setProductImage(pImg != null ? pImg : "https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=200&q=80");
        }

        return new EditPermissionResult(true, null, review);
    }

    /**
     * Cập nhật nội dung đánh giá sau khi kiểm tra lại quyền và hạn 7 ngày trên server.
     */
    public boolean updateReview(Review updatedReview, int userId) {
        if (updatedReview == null || userId <= 0) return false;

        // Đối chiếu lại với dữ liệu gốc trong DB
        Review original = reviewDAO.getReviewById(updatedReview.getReview_id());
        if (original == null) return false;

        // Kiểm tra quyền sở hữu & thời hạn 7 ngày
        if (original.getUser_id() != userId || !isReviewEditable(original)) {
            return false;
        }

        updatedReview.setUser_id(userId);
        return reviewDAO.updateReview(updatedReview);
    }

    // ----------------------------------------------------------------
    // 4. NGHIỆP VỤ XÓA ĐÁNH GIÁ (DELETE REVIEW)
    // ----------------------------------------------------------------
    public boolean deleteReview(int reviewId, int userId, String webappRoot) {
        if (reviewId <= 0 || userId <= 0) return false;

        Review review = reviewDAO.getReviewById(reviewId);
        if (review == null || review.getUser_id() != userId) {
            return false;
        }

        // Dọn dẹp ảnh vật lý nếu có
        deletePhysicalImages(review.getImageUrls(), webappRoot);

        // Xóa trong CSDL
        return reviewDAO.deleteReview(reviewId, userId);
    }

    public void deletePhysicalImages(String imageUrls, String webappRoot) {
        if (imageUrls == null || imageUrls.trim().isEmpty() || webappRoot == null) {
            return;
        }
        String[] paths = imageUrls.split(",");
        for (String path : paths) {
            path = path.trim();
            if (!path.isEmpty()) {
                try {
                    File file = new File(webappRoot, path);
                    if (file.exists() && file.isFile()) {
                        file.delete();
                    }
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Không thể xóa file ảnh: " + path, ex);
                }
            }
        }
    }

    // ----------------------------------------------------------------
    // 5. NGHIỆP VỤ CUSTOMER DASHBOARD - MY REVIEWS
    // ----------------------------------------------------------------

    public List<Review> getReviewsByUser(int userId, int page, int pageSize) {
        if (userId <= 0) return new java.util.ArrayList<>();
        return reviewDAO.getReviewsByUserId(userId, page, pageSize);
    }

    public int countReviewsByUser(int userId) {
        if (userId <= 0) return 0;
        return reviewDAO.countReviewsByUserId(userId);
    }

    public List<PendingReviewItem> getPendingReviewsByUser(int userId) {
        if (userId <= 0) return new java.util.ArrayList<>();
        return reviewDAO.getPendingReviewsByUserId(userId);
    }
}
