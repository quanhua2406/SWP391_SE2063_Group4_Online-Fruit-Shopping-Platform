package com.mycompany.onlinefruitshopping.dao.review;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.mycompany.onlinefruitshopping.utils.DBConnection;
import com.mycompany.onlinefruitshopping.model.Review;
import com.mycompany.onlinefruitshopping.model.PendingReviewItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ReviewDAO extends DBConnection {

    private static final Logger LOG = Logger.getLogger(ReviewDAO.class.getName());

    // Cache metadata của bảng reviews trong SQL Server
    private static volatile String dateColumnName = null;
    private static volatile Boolean hasTitleColumn = null;
    private static volatile Boolean hasImageUrlsColumn = null;
    Connection connection;

    public ReviewDAO() {
        super();
        try {
            this.connection = DBConnection.getConnection();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to initialize ReviewDAO connection initially", e);
        }
    }

    private synchronized Connection getValidConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DBConnection.getConnection();
        }
        return connection;
    }

    /**
     * Tự động kiểm tra cấu trúc các cột của bảng reviews trong CSDL thực tế.
     * Xác định:
     *  - Tên cột ngày: 'created_at' hay 'create_at'
     *  - Cột 'title' có tồn tại không
     *  - Cột 'image_urls' có tồn tại không
     */
    public synchronized void checkColumns() {
        if (dateColumnName != null && hasTitleColumn != null && hasImageUrlsColumn != null) {
            return;
        }
        if (connection == null) {
            dateColumnName = "created_at";
            hasTitleColumn = false;
            hasImageUrlsColumn = false;
            return;
        }
        String checkSql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'reviews'";
        try (PreparedStatement ps = getValidConnection().prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            boolean foundCreatedAt = false;
            boolean foundCreateAt = false;
            boolean foundTitle = false;
            boolean foundImageUrls = false;
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME").toLowerCase();
                if ("created_at".equals(col)) foundCreatedAt = true;
                if ("create_at".equals(col)) foundCreateAt = true;
                if ("title".equals(col)) foundTitle = true;
                if ("image_urls".equals(col)) foundImageUrls = true;
            }
            if (foundCreatedAt) {
                dateColumnName = "created_at";
            } else if (foundCreateAt) {
                dateColumnName = "create_at";
            } else {
                dateColumnName = "created_at";
            }
            hasTitleColumn = foundTitle;
            hasImageUrlsColumn = foundImageUrls;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Lỗi kiểm tra INFORMATION_SCHEMA cho bảng reviews", e);
            if (dateColumnName == null) dateColumnName = "created_at";
            if (hasTitleColumn == null) hasTitleColumn = false;
            if (hasImageUrlsColumn == null) hasImageUrlsColumn = false;
        }
    }

    // ----------------------------------------------------------------
    // 1. LẤY DANH SÁCH ĐÁNH GIÁ (có phân trang + lọc sao)
    // ----------------------------------------------------------------

    /**
     * Lấy danh sách đánh giá cho một sản phẩm.
     * Sử dụng LEFT JOIN users để không bao giờ bị mất đánh giá nếu user_id có vấn đề.
     *
     * @param productId    ID sản phẩm
     * @param ratingFilter 0 = tất cả, 1-5 = lọc theo sao, -1 = chỉ lấy có ảnh
     * @param page         Trang hiện tại (bắt đầu từ 1)
     * @param pageSize     Số đánh giá mỗi trang
     * @return Danh sách Review
     */
    public List<Review> getReviewsByProduct(int productId, int ratingFilter, int page, int pageSize) {
        List<Review> list = new ArrayList<>();
        checkColumns();
        String dateCol = (dateColumnName != null) ? dateColumnName : "created_at";
        boolean hasTitle = Boolean.TRUE.equals(hasTitleColumn);
        boolean hasImages = Boolean.TRUE.equals(hasImageUrlsColumn);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.review_id, r.user_id, r.product_id, r.order_id, ");
        if (hasTitle) {
            sql.append("r.title, ");
        }
        sql.append("r.rating, r.comment, r.").append(dateCol).append(" AS review_date, ");
        if (hasImages) {
            sql.append("r.image_urls, ");
        }
        sql.append("u.full_name AS user_name ");
        sql.append("FROM   reviews r ");
        sql.append("LEFT JOIN users u ON r.user_id = u.user_id ");
        sql.append("WHERE  r.product_id = ? AND (r.status IS NULL OR r.status = 'published' OR r.status = 'reported') ");

        if (ratingFilter >= 1 && ratingFilter <= 5) {
            sql.append("AND r.rating = ? ");
        } else if (ratingFilter == -1) {
            if (hasImages) {
                sql.append("AND r.image_urls IS NOT NULL AND r.image_urls <> '' ");
            } else {
                // Nếu DB chưa có cột image_urls thì lọc có ảnh trả về danh sách rỗng
                return list;
            }
        }

        sql.append("ORDER BY r.").append(dateCol).append(" DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, productId);
            if (ratingFilter >= 1 && ratingFilter <= 5) {
                ps.setInt(idx++, ratingFilter);
            }
            int offset = Math.max(0, (page - 1) * pageSize);
            ps.setInt(idx++, offset);
            ps.setInt(idx,   pageSize);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Review r = new Review();
                r.setReview_id(rs.getInt("review_id"));
                r.setUser_id(rs.getInt("user_id"));
                r.setProduct_id(rs.getInt("product_id"));
                r.setOrder_id(rs.getInt("order_id"));
                if (hasTitle) {
                    r.setTitle(rs.getString("title"));
                }
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));

                Timestamp ts = rs.getTimestamp("review_date");
                if (ts != null) {
                    r.setCreated_at(new java.util.Date(ts.getTime()));
                }

                if (hasImages) {
                    r.setImageUrls(rs.getString("image_urls"));
                }

                String uName = rs.getString("user_name");
                if (uName == null || uName.trim().isEmpty()) {
                    uName = "Khách hàng";
                }
                r.setUserName(uName);

                list.add(r);
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "getReviewsByProduct error", ex);
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 2. ĐẾM TỔNG SỐ ĐÁNH GIÁ (dùng cho phân trang)
    // ----------------------------------------------------------------

    /**
     * Đếm tổng số review của sản phẩm theo bộ lọc.
     *
     * @param productId    ID sản phẩm
     * @param ratingFilter 0 = tất cả, 1-5 = lọc theo sao, -1 = chỉ có ảnh
     * @return Tổng số bản ghi
     */
    public int countReviewsByProduct(int productId, int ratingFilter) {
        checkColumns();
        boolean hasImages = Boolean.TRUE.equals(hasImageUrlsColumn);

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM reviews r WHERE r.product_id = ? AND (r.status IS NULL OR r.status = 'published' OR r.status = 'reported') "
        );
        if (ratingFilter >= 1 && ratingFilter <= 5) {
            sql.append("AND r.rating = ? ");
        } else if (ratingFilter == -1) {
            if (hasImages) {
                sql.append("AND r.image_urls IS NOT NULL AND r.image_urls <> '' ");
            } else {
                return 0;
            }
        }

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql.toString())) {
            ps.setInt(1, productId);
            if (ratingFilter >= 1 && ratingFilter <= 5) {
                ps.setInt(2, ratingFilter);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "countReviewsByProduct error", ex);
        }
        return 0;
    }

    // ----------------------------------------------------------------
    // 3. THỐNG KÊ ĐIỂM TRUNG BÌNH & PHÂN BỐ SAO
    // ----------------------------------------------------------------

    /**
     * Trả về mảng thống kê điểm số cho sản phẩm.
     *
     * @param productId ID sản phẩm
     * @return double[7]:
     *   [0] = avgRating,
     *   [1] = totalCount,
     *   [2] = count5star,
     *   [3] = count4star,
     *   [4] = count3star,
     *   [5] = count2star,
     *   [6] = count1star
     */
    public double[] getRatingStats(int productId) {
        double[] stats = {0.0, 0, 0, 0, 0, 0, 0};
        String sql =
                "SELECT " +
                        "  AVG(CAST(rating AS FLOAT))                         AS avg_rating, " +
                        "  COUNT(*)                                           AS total, " +
                        "  SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END)       AS c5, " +
                        "  SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END)       AS c4, " +
                        "  SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END)       AS c3, " +
                        "  SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END)       AS c2, " +
                        "  SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END)       AS c1 " +
                        "FROM reviews " +
                        "WHERE product_id = ? AND (status IS NULL OR status = 'published' OR status = 'reported')";

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats[0] = rs.getDouble("avg_rating");
                stats[1] = rs.getInt("total");
                stats[2] = rs.getInt("c5");
                stats[3] = rs.getInt("c4");
                stats[4] = rs.getInt("c3");
                stats[5] = rs.getInt("c2");
                stats[6] = rs.getInt("c1");
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "getRatingStats error", ex);
        }
        return stats;
    }

    // ----------------------------------------------------------------
    // 4. KIỂM TRA NGƯỜI DÙNG ĐÃ ĐÁNH GIÁ ĐƠN HÀNG NÀY CHƯA
    // ----------------------------------------------------------------

    /**
     * Kiểm tra user đã gửi review cho order+product này chưa.
     * Khớp với CONSTRAINT uq_review_per_order_product UNIQUE (order_id, product_id).
     */
    public boolean hasUserReviewedOrder(int userId, int orderId, int productId) {
        String sql =
                "SELECT TOP 1 1 FROM reviews " +
                        "WHERE user_id = ? AND order_id = ? AND product_id = ?";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, orderId);
            ps.setInt(3, productId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "hasUserReviewedOrder error", ex);
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 5. KIỂM TRA ĐƠN HÀNG ĐÃ ĐƯỢC GIAO THÀNH CÔNG
    // ----------------------------------------------------------------

    /**
     * Điều kiện nghiệp vụ: Chỉ cho phép đánh giá khi đơn hàng có
     * trạng thái 'delivered' và thuộc về đúng user_id.
     */
    public boolean hasOrderDelivered(int userId, int orderId) {
        String sql =
                "SELECT TOP 1 1 FROM orders " +
                        "WHERE order_id = ? AND user_id = ? AND LOWER(status) = 'delivered'";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "hasOrderDelivered error", ex);
        }
        return false;
    }

    /**
     * Tự động tìm đơn hàng gần nhất đã giao thành công của user chứa sản phẩm này
     * mà chưa được đánh giá.
     */
    public int findDeliveredOrderId(int userId, int productId) {
        // 1. Tìm qua liên kết chi tiết đơn hàng (order_items -> product_variants)
        String sql =
                "SELECT TOP 1 o.order_id " +
                        "FROM orders o " +
                        "JOIN order_items oi ON o.order_id = oi.order_id " +
                        "JOIN product_variants pv ON oi.variant_id = pv.variant_id " +
                        "WHERE o.user_id = ? AND pv.product_id = ? AND LOWER(o.status) = 'delivered' " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM reviews r WHERE r.order_id = o.order_id AND r.product_id = ? " +
                        "  ) " +
                        "ORDER BY o.order_id DESC";

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("order_id");
            }
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "findDeliveredOrderId specific search error", ex);
        }

        // 2. Dự phòng: tìm đơn hàng delivered bất kỳ của user chưa review cho sản phẩm này
        String fallbackSql =
                "SELECT TOP 1 o.order_id " +
                        "FROM orders o " +
                        "WHERE o.user_id = ? AND LOWER(o.status) = 'delivered' " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM reviews r WHERE r.order_id = o.order_id AND r.product_id = ? " +
                        "  ) " +
                        "ORDER BY o.order_id DESC";

        try (PreparedStatement ps = getValidConnection().prepareStatement(fallbackSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("order_id");
            }
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "findDeliveredOrderId fallback error", ex);
        }

        return -1;
    }

    // ----------------------------------------------------------------
    // 6. THÊM ĐÁNH GIÁ MỚI VÀO DATABASE
    // ----------------------------------------------------------------

    /**
     * INSERT một review mới vào bảng reviews.
     * Tự động điều chỉnh theo các cột có sẵn trong CSDL.
     *
     * @param review Object Review đã được điền đầy đủ
     * @return review_id mới sinh ra, hoặc -1 nếu lỗi
     */
    public int insertReview(Review review) {
        checkColumns();
        String dateCol = (dateColumnName != null) ? dateColumnName : "created_at";
        boolean hasTitle = Boolean.TRUE.equals(hasTitleColumn);
        boolean hasImages = Boolean.TRUE.equals(hasImageUrlsColumn);

        StringBuilder cols = new StringBuilder("user_id, product_id, order_id, rating, comment, ").append(dateCol);
        StringBuilder vals = new StringBuilder("?, ?, ?, ?, ?, ?");

        boolean insertTitle = hasTitle && review.getTitle() != null && !review.getTitle().trim().isEmpty();
        boolean insertImages = hasImages && review.getImageUrls() != null && !review.getImageUrls().trim().isEmpty();

        if (insertTitle) {
            cols.append(", title");
            vals.append(", ?");
        }
        if (insertImages) {
            cols.append(", image_urls");
            vals.append(", ?");
        }

        String commentToSave = review.getComment();
        // Nếu DB không có cột title riêng nhưng user nhập title, lưu title vào đầu comment
        if (!hasTitle && review.getTitle() != null && !review.getTitle().trim().isEmpty()) {
            commentToSave = "[" + review.getTitle().trim() + "] " + (commentToSave != null ? commentToSave : "");
        }

        String sql = "INSERT INTO reviews (" + cols.toString() + ") OUTPUT INSERTED.review_id VALUES (" + vals.toString() + ")";

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, review.getUser_id());
            ps.setInt(idx++, review.getProduct_id());
            ps.setInt(idx++, review.getOrder_id());
            ps.setInt(idx++, review.getRating());
            ps.setString(idx++, commentToSave);
            ps.setTimestamp(idx++, new Timestamp(System.currentTimeMillis()));

            if (insertTitle) {
                ps.setString(idx++, review.getTitle().trim());
            }
            if (insertImages) {
                ps.setString(idx++, review.getImageUrls().trim());
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "insertReview error", ex);
        }
        return -1;
    }

    // ----------------------------------------------------------------
    // 7. LẤY CHI TIẾT 1 ĐÁNH GIÁ THEO ID (dùng cho form Edit/Delete)
    // ----------------------------------------------------------------

    /**
     * Lấy đánh giá theo review_id, kèm thông tin người dùng và sản phẩm.
     *
     * @param reviewId ID của đánh giá
     * @return Đối tượng Review hoặc null nếu không tìm thấy
     */
    public Review getReviewById(int reviewId) {
        checkColumns();
        String dateCol = (dateColumnName != null) ? dateColumnName : "created_at";
        boolean hasTitle = Boolean.TRUE.equals(hasTitleColumn);
        boolean hasImages = Boolean.TRUE.equals(hasImageUrlsColumn);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.review_id, r.user_id, r.product_id, r.order_id, ");
        if (hasTitle) {
            sql.append("r.title, ");
        }
        sql.append("r.rating, r.comment, r.").append(dateCol).append(" AS review_date, ");
        if (hasImages) {
            sql.append("r.image_urls, ");
        }
        sql.append("u.full_name AS user_name, ");
        sql.append("p.product_name ");
        sql.append("FROM   reviews r ");
        sql.append("LEFT JOIN users u ON r.user_id = u.user_id ");
        sql.append("LEFT JOIN products p ON r.product_id = p.product_id ");
        sql.append("WHERE  r.review_id = ?");

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql.toString())) {
            ps.setInt(1, reviewId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Review r = new Review();
                r.setReview_id(rs.getInt("review_id"));
                r.setUser_id(rs.getInt("user_id"));
                r.setProduct_id(rs.getInt("product_id"));
                r.setOrder_id(rs.getInt("order_id"));
                if (hasTitle) {
                    r.setTitle(rs.getString("title"));
                }
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));

                Timestamp ts = rs.getTimestamp("review_date");
                if (ts != null) {
                    r.setCreated_at(new java.util.Date(ts.getTime()));
                }

                if (hasImages) {
                    r.setImageUrls(rs.getString("image_urls"));
                }

                String uName = rs.getString("user_name");
                r.setUserName(uName != null ? uName : "Khách hàng");
                r.setProductName(rs.getString("product_name"));
                return r;
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "getReviewById error", ex);
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 8. CẬP NHẬT ĐÁNH GIÁ (EDIT REVIEW)
    // ----------------------------------------------------------------

    /**
     * Cập nhật đánh giá của khách hàng (số sao, tiêu đề, bình luận, ảnh).
     * Bắt buộc có điều kiện WHERE review_id = ? AND user_id = ? để bảo mật.
     */
    public boolean updateReview(Review review) {
        checkColumns();
        boolean hasTitle = Boolean.TRUE.equals(hasTitleColumn);
        boolean hasImages = Boolean.TRUE.equals(hasImageUrlsColumn);

        StringBuilder setClause = new StringBuilder("rating = ?, comment = ?");
        if (hasTitle) {
            setClause.append(", title = ?");
        }
        if (hasImages) {
            setClause.append(", image_urls = ?");
        }
        String sql = "UPDATE reviews SET " + setClause.toString() + " WHERE review_id = ? AND user_id = ?";

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, review.getRating());

            String commentToSave = review.getComment();
            if (!hasTitle && review.getTitle() != null && !review.getTitle().trim().isEmpty()) {
                commentToSave = "[" + review.getTitle().trim() + "] " + (commentToSave != null ? commentToSave : "");
            }
            ps.setString(idx++, commentToSave);

            if (hasTitle) {
                ps.setString(idx++, review.getTitle());
            }
            if (hasImages) {
                ps.setString(idx++, review.getImageUrls());
            }
            ps.setInt(idx++, review.getReview_id());
            ps.setInt(idx++, review.getUser_id());

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "updateReview error", ex);
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 9. XÓA ĐÁNH GIÁ (DELETE REVIEW)
    // ----------------------------------------------------------------

    /**
     * Xóa đánh giá của khách hàng.
     * Bắt buộc có điều kiện WHERE review_id = ? AND user_id = ? để đảm bảo
     * chỉ chính chủ đánh giá mới có quyền xóa.
     */
    public boolean deleteReview(int reviewId, int userId) {
        String sql = "DELETE FROM reviews WHERE review_id = ? AND user_id = ?";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "deleteReview error", ex);
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 10. HELPER TRUY VẤN THÔNG TIN SẢN PHẨM TỪ DATABASE
    // ----------------------------------------------------------------

    /**
     * Lấy tên sản phẩm theo product_id từ bảng products trong CSDL.
     */
    public String getProductNameById(int productId) {
        String sql = "SELECT product_name FROM products WHERE product_id = ?";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("product_name");
            }
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "getProductNameById error", ex);
        }
        return null;
    }

    /**
     * Lấy ảnh đại diện sản phẩm từ bảng product_images.
     */
    public String getProductImageById(int productId) {
        String sql = "SELECT TOP 1 image_url FROM product_images WHERE product_id = ? ORDER BY sort_order ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("image_url");
            }
        } catch (SQLException ex) {
            LOG.log(Level.FINE, "getProductImageById error", ex);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // REVIEW MODERATION METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Cập nhật status của review.
     * @param reviewId  ID của review
     * @param newStatus  "published" | "reported" | "rejected" | "deleted"
     * @return true nếu update thành công
     */
    public boolean updateStatus(int reviewId, String newStatus) {
        String sql = "UPDATE reviews SET status = ? WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "updateStatus error", e);
            return false;
        }
    }

    /**
     * Admin duyệt review: status -> published, xóa deleted_at nếu có.
     */
    public boolean approveReview(int reviewId) {
        String sql = "UPDATE reviews SET status = 'published', deleted_at = NULL "
                   + "WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "approveReview error", e);
            return false;
        }
    }

    /**
     * Admin từ chối review: status -> rejected (ẩn khỏi public).
     */
    public boolean rejectReview(int reviewId) {
        return updateStatus(reviewId, "rejected");
    }

    /**
     * Admin soft-delete review: status -> deleted, ghi thời gian xóa.
     */
    public boolean softDeleteReview(int reviewId) {
        String sql = "UPDATE reviews SET status = 'deleted', deleted_at = GETDATE() "
                   + "WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "softDeleteReview error", e);
            return false;
        }
    }

    /**
     * Lấy danh sách review bị báo cáo cho trang Admin.
     * JOIN với review_reports để đếm số lần báo cáo.
     * @param status  null = lấy tất cả, hoặc "reported" / "rejected" / "deleted"
     * @param page    trang hiện tại (bắt đầu từ 1)
     * @param pageSize số review mỗi trang
     */
    public List<Review> getReviewsForAdmin(String status, int page, int pageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.review_id, r.user_id, r.product_id, r.order_id, ");
        sb.append("       r.rating, r.title, r.comment, r.status, r.created_at, r.deleted_at, ");
        sb.append("       u.full_name AS user_name, p.product_name, ");
        sb.append("       COUNT(rr.report_id) AS report_count ");
        sb.append("FROM reviews r ");
        sb.append("LEFT JOIN users u ON u.user_id = r.user_id ");
        sb.append("LEFT JOIN products p ON p.product_id = r.product_id ");
        sb.append("LEFT JOIN review_reports rr ON rr.review_id = r.review_id ");
        if (status != null && !status.isEmpty()) {
            sb.append("WHERE r.status = ? ");
        }
        sb.append("GROUP BY r.review_id, r.user_id, r.product_id, r.order_id, ");
        sb.append("         r.rating, r.title, r.comment, r.status, r.created_at, r.deleted_at, ");
        sb.append("         u.full_name, p.product_name ");
        sb.append("ORDER BY r.created_at DESC ");
        sb.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<Review> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (status != null && !status.isEmpty()) {
                ps.setString(idx++, status);
            }
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review rev = new Review();
                    rev.setReview_id(rs.getInt("review_id"));
                    rev.setUser_id(rs.getInt("user_id"));
                    rev.setProduct_id(rs.getInt("product_id"));
                    rev.setOrder_id(rs.getInt("order_id"));
                    rev.setRating(rs.getInt("rating"));
                    rev.setTitle(rs.getString("title"));
                    rev.setComment(rs.getString("comment"));
                    rev.setStatus(rs.getString("status"));
                    rev.setCreated_at(rs.getTimestamp("created_at"));
                    rev.setDeletedAt(rs.getTimestamp("deleted_at"));
                    rev.setUserName(rs.getString("user_name"));
                    rev.setProductName(rs.getString("product_name"));
                    rev.setReportCount(rs.getInt("report_count"));
                    list.add(rev);
                }
            }
            populateEvidenceForAdmin(conn, list);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getReviewsForAdmin error", e);
        }
        return list;
    }

    private void populateEvidenceForAdmin(Connection conn, List<Review> list) {
        if (list == null || list.isEmpty()) return;
        boolean hasEvidenceCol = false;
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "review_reports", "evidence_urls")) {
                hasEvidenceCol = rs.next();
            }
        } catch (SQLException ignored) {}

        for (Review r : list) {
            if (r.getReportCount() <= 0) continue;
            List<String> urls = new ArrayList<>();
            if (hasEvidenceCol) {
                String sql = "SELECT evidence_urls, note FROM review_reports WHERE review_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, r.getReview_id());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String u = rs.getString("evidence_urls");
                            if (u != null && !u.isBlank()) {
                                urls.add(u);
                            } else {
                                String note = rs.getString("note");
                                if (note != null && note.contains("[Bằng chứng: ")) {
                                    int start = note.indexOf("[Bằng chứng: ") + 13;
                                    int end = note.indexOf("]", start);
                                    if (end > start) urls.add(note.substring(start, end).trim());
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "Error reading evidence_urls", e);
                }
            } else {
                String sql = "SELECT note FROM review_reports WHERE review_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, r.getReview_id());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String note = rs.getString("note");
                            if (note != null && note.contains("[Bằng chứng: ")) {
                                int start = note.indexOf("[Bằng chứng: ") + 13;
                                int end = note.indexOf("]", start);
                                if (end > start) urls.add(note.substring(start, end).trim());
                            }
                        }
                    }
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "Error reading fallback evidence note", e);
                }
            }
            if (!urls.isEmpty()) {
                r.setEvidenceUrls(String.join(",", urls));
            }
        }
    }

    /**
     * Đếm tổng số review theo status (dùng phân trang).
     */
    public int countReviewsForAdmin(String status) {
        String sql = (status != null && !status.isEmpty())
            ? "SELECT COUNT(*) FROM reviews WHERE status = ?"
            : "SELECT COUNT(*) FROM reviews";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (status != null && !status.isEmpty()) {
                ps.setString(1, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "countReviewsForAdmin error", e);
            return 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CUSTOMER MY REVIEWS METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách đánh giá do một khách hàng cụ thể viết (phân trang).
     */
    public List<Review> getReviewsByUserId(int userId, int page, int pageSize) {
        List<Review> list = new ArrayList<>();
        checkColumns();
        String dateCol = (dateColumnName != null) ? dateColumnName : "created_at";
        boolean hasTitle = Boolean.TRUE.equals(hasTitleColumn);
        boolean hasImages = Boolean.TRUE.equals(hasImageUrlsColumn);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.review_id, r.user_id, r.product_id, r.order_id, ");
        if (hasTitle) {
            sql.append("r.title, ");
        }
        sql.append("r.rating, r.comment, r.status, r.").append(dateCol).append(" AS review_date, ");
        if (hasImages) {
            sql.append("r.image_urls, ");
        }
        sql.append("p.product_name, (SELECT TOP 1 pi.image_url FROM product_images pi WHERE pi.product_id = r.product_id ORDER BY pi.sort_order ASC, pi.image_id ASC) AS product_image ");
        sql.append("FROM reviews r ");
        sql.append("LEFT JOIN products p ON r.product_id = p.product_id ");
        sql.append("WHERE r.user_id = ? AND (r.status IS NULL OR r.status <> 'deleted') ");
        sql.append("ORDER BY r.").append(dateCol).append(" DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql.toString())) {
            int offset = Math.max(0, (page - 1) * pageSize);
            ps.setInt(1, userId);
            ps.setInt(2, offset);
            ps.setInt(3, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setReview_id(rs.getInt("review_id"));
                    r.setUser_id(rs.getInt("user_id"));
                    r.setProduct_id(rs.getInt("product_id"));
                    r.setOrder_id(rs.getInt("order_id"));
                    if (hasTitle) {
                        r.setTitle(rs.getString("title"));
                    }
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setStatus(rs.getString("status"));

                    Timestamp ts = rs.getTimestamp("review_date");
                    if (ts != null) {
                        r.setCreated_at(new java.util.Date(ts.getTime()));
                    }

                    if (hasImages) {
                        r.setImageUrls(rs.getString("image_urls"));
                    }

                    r.setProductName(rs.getString("product_name"));
                    String pImg = rs.getString("product_image");
                    if (pImg == null || pImg.isBlank()) {
                        pImg = getProductImageById(r.getProduct_id());
                    }
                    r.setProductImage(pImg);

                    // Tính khả năng sửa (trong vòng 7 ngày)
                    if (r.getCreated_at() != null) {
                        long diffMs = System.currentTimeMillis() - r.getCreated_at().getTime();
                        long diffDays = diffMs / (1000L * 60 * 60 * 24);
                        r.setEditable(diffDays <= 7);
                        r.setRemainingDays(Math.max(0, 7 - diffDays));
                    } else {
                        r.setEditable(true);
                        r.setRemainingDays(7);
                    }

                    list.add(r);
                }
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "getReviewsByUserId error", ex);
        }
        return list;
    }

    /**
     * Đếm tổng số đánh giá khách hàng đã viết.
     */
    public int countReviewsByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND (status IS NULL OR status <> 'deleted')";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "countReviewsByUserId error", ex);
        }
        return 0;
    }

    /**
     * Lấy danh sách sản phẩm từ các đơn hàng đã giao thành công của user mà chưa được đánh giá.
     */
    public List<PendingReviewItem> getPendingReviewsByUserId(int userId) {
        List<PendingReviewItem> list = new ArrayList<>();
        String sql =
            "SELECT DISTINCT " +
            "  o.order_id, o.created_at AS order_date, " +
            "  p.product_id, p.product_name, oi.unit_price AS price, (SELECT TOP 1 pi.image_url FROM product_images pi WHERE pi.product_id = p.product_id ORDER BY pi.sort_order ASC, pi.image_id ASC) AS image_url " +
            "FROM orders o " +
            "JOIN order_items oi ON o.order_id = oi.order_id " +
            "JOIN product_variants pv ON oi.variant_id = pv.variant_id " +
            "JOIN products p ON pv.product_id = p.product_id " +
            "WHERE o.user_id = ? AND LOWER(o.status) = 'delivered' " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM reviews r " +
            "      WHERE r.order_id = o.order_id AND r.product_id = p.product_id " +
            "  ) " +
            "ORDER BY o.order_id DESC";

        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PendingReviewItem item = new PendingReviewItem();
                    item.setOrderId(rs.getInt("order_id"));
                    Timestamp ts = rs.getTimestamp("order_date");
                    if (ts != null) {
                        item.setOrderDate(new java.util.Date(ts.getTime()));
                    }
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    String img = rs.getString("image_url");
                    if (img == null || img.isBlank()) {
                        img = getProductImageById(item.getProductId());
                    }
                    item.setProductImage(img);
                    item.setPrice(rs.getDouble("price"));
                    list.add(item);
                }
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "getPendingReviewsByUserId error", ex);
        }
        return list;
    }
}

