package com.mycompany.onlinefruitshopping.dao.review;

import com.mycompany.onlinefruitshopping.model.ReviewReport;
import com.mycompany.onlinefruitshopping.model.ReviewReportDetailDTO;
import com.mycompany.onlinefruitshopping.model.ReviewReportDetailDTO.ChatMessageItem;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviewReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReviewReportDAO.class.getName());

    /** Cached flag – kiểm tra một lần duy nhất xem cột evidence_urls có tồn tại không */
    private static Boolean evidenceColExists = null;
    private static Boolean adminNotesColExists = null;

    // ─────────────────────────────────────────────────────────────────────
    // Helper: kiểm tra cột evidence_urls trong bảng review_reports
    // ─────────────────────────────────────────────────────────────────────
    public synchronized boolean hasEvidenceColumn() {
        if (evidenceColExists != null) return evidenceColExists;
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "review_reports", "evidence_urls")) {
                if (rs.next()) {
                    evidenceColExists = true;
                    return true;
                }
            }
            // Thử tự động thêm cột evidence_urls nếu DB chưa có
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE review_reports ADD evidence_urls NVARCHAR(1000) NULL");
                evidenceColExists = true;
                LOGGER.info("Successfully added evidence_urls column to review_reports");
                return true;
            } catch (SQLException ex) {
                LOGGER.log(Level.INFO, "Could not auto-add evidence_urls column: " + ex.getMessage());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Cannot check evidence_urls column", e);
        }
        evidenceColExists = false;
        return false;
    }

    public synchronized boolean hasAdminNotesColumn() {
        if (adminNotesColExists != null) return adminNotesColExists;
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "review_reports", "admin_notes")) {
                if (rs.next()) {
                    adminNotesColExists = true;
                    return true;
                }
            }
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE review_reports ADD admin_notes NVARCHAR(1000) NULL");
                adminNotesColExists = true;
                LOGGER.info("Successfully added admin_notes column to review_reports");
                return true;
            } catch (SQLException ex) {
                LOGGER.log(Level.INFO, "Could not auto-add admin_notes column: " + ex.getMessage());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Cannot check admin_notes column", e);
        }
        adminNotesColExists = false;
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1. Kiểm tra user đã báo cáo review này chưa
    // ─────────────────────────────────────────────────────────────────────
    public boolean hasReported(int reviewId, int reporterId) throws SQLException {
        String sql = "SELECT 1 FROM review_reports "
                   + "WHERE review_id = ? AND reporter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. Tạo báo cáo mới (hỗ trợ evidence_urls nếu cột tồn tại)
    // ─────────────────────────────────────────────────────────────────────
    public int createReport(ReviewReport report) throws SQLException {
        boolean colExists = hasEvidenceColumn();
        boolean hasEvidence = report.getEvidenceUrls() != null && !report.getEvidenceUrls().isBlank();
        boolean useEvidence = colExists && hasEvidence;

        String note = report.getNote();
        // Nếu DB không có cột evidence_urls mà có ảnh bằng chứng, ghi đính kèm vào note để không mất dữ liệu
        if (!colExists && hasEvidence) {
            String evidenceNote = "[Bằng chứng: " + report.getEvidenceUrls() + "]";
            note = (note != null && !note.isBlank()) ? note + "\n" + evidenceNote : evidenceNote;
        }

        String sql = useEvidence
                ? "INSERT INTO review_reports (review_id, reporter_id, reason, note, evidence_urls) VALUES (?, ?, ?, ?, ?)"
                : "INSERT INTO review_reports (review_id, reporter_id, reason, note) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, report.getReviewId());
            ps.setInt(2, report.getReporterId());
            ps.setString(3, report.getReason());
            ps.setString(4, note);   // có thể null hoặc chứa note + bằng chứng fallback
            if (useEvidence) {
                ps.setString(5, report.getEvidenceUrls());
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. Đếm số lần báo cáo của 1 review
    // ─────────────────────────────────────────────────────────────────────
    public int countReportsByReviewId(int reviewId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM review_reports WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. Lấy danh sách báo cáo theo review_id (cho Admin xem chi tiết)
    // ─────────────────────────────────────────────────────────────────────
    public List<ReviewReport> getReportsByReviewId(int reviewId) throws SQLException {
        boolean useEvidence = hasEvidenceColumn();
        String evidenceSel  = useEvidence ? ", rr.evidence_urls" : "";
        String sql = "SELECT rr.report_id, rr.review_id, rr.reporter_id, "
                   + "       rr.reason, rr.note, rr.created_at" + evidenceSel + ", "
                   + "       u.full_name AS reporter_name "
                   + "FROM review_reports rr "
                   + "JOIN users u ON u.user_id = rr.reporter_id "
                   + "WHERE rr.review_id = ? "
                   + "ORDER BY rr.created_at DESC";
        List<ReviewReport> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReviewReport r = new ReviewReport();
                    r.setReportId(rs.getInt("report_id"));
                    r.setReviewId(rs.getInt("review_id"));
                    r.setReporterId(rs.getInt("reporter_id"));
                    r.setReason(rs.getString("reason"));
                    String noteVal = rs.getString("note");
                    r.setNote(noteVal);
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setReporterName(rs.getString("reporter_name"));
                    if (useEvidence) {
                        r.setEvidenceUrls(rs.getString("evidence_urls"));
                    } else if (noteVal != null && noteVal.contains("[Bằng chứng: ")) {
                        // Trích xuất fallback từ note nếu cột DB chưa có
                        int start = noteVal.indexOf("[Bằng chứng: ") + 13;
                        int end = noteVal.indexOf("]", start);
                        if (end > start) {
                            r.setEvidenceUrls(noteVal.substring(start, end).trim());
                        }
                    }
                    list.add(r);
                }
            }
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. Cập nhật ghi chú nội bộ (internal notes) của ban quản trị
    // ─────────────────────────────────────────────────────────────────────
    public boolean updateAdminNotes(int reportId, String notes) {
        if (!hasAdminNotesColumn()) return false;
        String sql = "UPDATE review_reports SET admin_notes = ? WHERE report_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notes);
            ps.setInt(2, reportId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi updateAdminNotes", e);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 6. Lấy toàn bộ thông tin chi tiết báo cáo phục vụ Admin Moderation Dashboard
    // ─────────────────────────────────────────────────────────────────────
    public ReviewReportDetailDTO getReportDetailByReviewId(int reviewId) {
        ReviewReportDetailDTO dto = new ReviewReportDetailDTO();
        dto.setReviewId(reviewId);

        boolean hasEvidence = hasEvidenceColumn();
        boolean hasAdminNotes = hasAdminNotesColumn();

        // 1. Đọc báo cáo mới nhất của review
        String reportSql = "SELECT TOP 1 rr.report_id, rr.reporter_id, rr.reason, rr.note, rr.created_at, "
                + (hasEvidence ? "rr.evidence_urls, " : "")
                + (hasAdminNotes ? "rr.admin_notes, " : "")
                + "u.full_name AS reporter_name, s.shop_name "
                + "FROM review_reports rr "
                + "LEFT JOIN users u ON u.user_id = rr.reporter_id "
                + "LEFT JOIN shops s ON s.owner_id = rr.reporter_id "
                + "WHERE rr.review_id = ? "
                + "ORDER BY rr.created_at DESC";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(reportSql)) {
                ps.setInt(1, reviewId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dto.setReportId(rs.getInt("report_id"));
                        dto.setReporterId(rs.getInt("reporter_id"));
                        String repName = rs.getString("reporter_name");
                        String shopName = rs.getString("shop_name");
                        if (shopName != null && !shopName.isBlank()) {
                            dto.setReporterName(shopName + " (ID: " + rs.getInt("reporter_id") + ")");
                            dto.setReporterShopName(shopName);
                        } else {
                            dto.setReporterName((repName != null ? repName : "Người dùng") + " (ID: " + rs.getInt("reporter_id") + ")");
                            dto.setReporterShopName(repName != null ? repName : "Cửa hàng");
                        }

                        dto.setReportDate(rs.getTimestamp("created_at"));
                        String reasonCode = rs.getString("reason");
                        dto.setReason(reasonCode);
                        dto.setReasonLabel(mapReasonLabel(reasonCode));

                        String note = rs.getString("note");
                        dto.setSellerDescription(cleanNote(note));

                        if (hasAdminNotes) {
                            dto.setInternalNotes(rs.getString("admin_notes"));
                        }

                        // Lấy ảnh bằng chứng
                        List<String> evidenceList = new ArrayList<>();
                        if (hasEvidence) {
                            String evUrls = rs.getString("evidence_urls");
                            if (evUrls != null && !evUrls.isBlank()) {
                                Arrays.stream(evUrls.split(","))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .forEach(evidenceList::add);
                            }
                        }
                        // Fallback trích xuất từ note nếu chưa có
                        if (evidenceList.isEmpty() && note != null && note.contains("[Bằng chứng: ")) {
                            int start = note.indexOf("[Bằng chứng: ") + 13;
                            int end = note.indexOf("]", start);
                            if (end > start) {
                                Arrays.stream(note.substring(start, end).split(","))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .forEach(evidenceList::add);
                            }
                        }
                        dto.setEvidenceImages(evidenceList);
                    }
                }
            }

            // 2. Đọc thông tin đánh giá gốc (Original Review) và sản phẩm
            boolean hasReviewImages = false;
            try {
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rsCol = meta.getColumns(null, null, "reviews", "image_urls")) {
                    hasReviewImages = rsCol.next();
                }
            } catch (SQLException ignored) {}

            String revSql = "SELECT r.review_id, r.user_id, r.product_id, r.order_id, r.rating, r.comment, r.status, r.created_at, "
                    + (hasReviewImages ? "r.image_urls, " : "")
                    + "u.full_name AS reviewer_name, "
                    + "p.product_name, (SELECT TOP 1 pv.packaging_type FROM product_variants pv WHERE pv.product_id = p.product_id ORDER BY pv.variant_id ASC) AS packaging_type, (SELECT TOP 1 pv.price FROM product_variants pv WHERE pv.product_id = p.product_id ORDER BY pv.variant_id ASC) AS price, (SELECT TOP 1 pi.image_url FROM product_images pi WHERE pi.product_id = p.product_id ORDER BY pi.sort_order ASC, pi.image_id ASC) AS product_image, s.shop_name "
                    + "FROM reviews r "
                    + "LEFT JOIN users u ON u.user_id = r.user_id "
                    + "LEFT JOIN products p ON p.product_id = r.product_id "
                    + "LEFT JOIN shops s ON s.shop_id = p.shop_id "
                    + "WHERE r.review_id = ?";

            int orderId = 0;
            try (PreparedStatement ps = conn.prepareStatement(revSql)) {
                ps.setInt(1, reviewId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dto.setRating(rs.getInt("rating"));
                        dto.setReviewComment(rs.getString("comment"));
                        dto.setReviewerId(rs.getInt("user_id"));
                        String reviewerFull = rs.getString("reviewer_name");
                        dto.setReviewerName(maskUserName(reviewerFull, rs.getInt("user_id")));
                        dto.setReviewDate(rs.getTimestamp("created_at"));
                        dto.setReviewStatus(rs.getString("status"));

                        dto.setProductId(rs.getInt("product_id"));
                        dto.setProductName(rs.getString("product_name"));
                        dto.setVariant(rs.getString("packaging_type") != null ? rs.getString("packaging_type") : "Tiêu chuẩn");
                        dto.setPrice(rs.getDouble("price"));

                        if (dto.getReporterShopName() == null || dto.getReporterShopName().isBlank()) {
                            String shp = rs.getString("shop_name");
                            dto.setReporterShopName(shp != null ? shp : "FreshFruit Shop");
                        }

                        orderId = rs.getInt("order_id");
                        dto.setOrderId(orderId);
                        dto.setOrderFormattedId(orderId > 0 ? "#ORD-2026-" + String.format("%06d", orderId) : "#ORD-N/A");

                        // Ảnh đánh giá (product images in review)
                        List<String> revImgs = new ArrayList<>();
                        if (hasReviewImages) {
                            String imgs = rs.getString("image_urls");
                            if (imgs != null && !imgs.isBlank()) {
                                Arrays.stream(imgs.split(","))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .forEach(revImgs::add);
                            }
                        }
                        if (revImgs.isEmpty() && rs.getString("product_image") != null) {
                            revImgs.add(rs.getString("product_image"));
                        }
                        dto.setReviewImages(revImgs);
                    }
                }
            }

            // 3. Đọc thông tin đơn hàng (Order Details)
            if (orderId > 0) {
                String ordSql = "SELECT order_id, status, total_amount, created_at FROM orders WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(ordSql)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dto.setOrderStatus(rs.getString("status"));
                            dto.setOrderDate(rs.getTimestamp("created_at"));
                            if (dto.getPrice() <= 0) {
                                dto.setPrice(rs.getDouble("total_amount"));
                            }
                        }
                    }
                }

                // Ngày giao hàng từ order_status_history
                String histSql = "SELECT TOP 1 changed_at FROM order_status_history WHERE order_id = ? AND status = 'delivered' ORDER BY changed_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(histSql)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dto.setDeliveredDate(rs.getTimestamp("changed_at"));
                        } else if (dto.getOrderDate() != null) {
                            // Dự phòng mốc giao sau 2-3 ngày
                            long delivMillis = dto.getOrderDate().getTime() + (2L * 24 * 3600 * 1000);
                            dto.setDeliveredDate(new Date(delivMillis));
                        }
                    }
                }
            }

            // 4. Khởi tạo danh sách Chat History Preview (3 tin nhắn gần nhất)
            buildChatHistoryPreview(dto);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi getReportDetailByReviewId", e);
        }

        return dto;
    }

    private String mapReasonLabel(String code) {
        if (code == null) return "N/A";
        switch (code) {
            case "inappropriate_language":
                return "Inappropriate language / Offensive content";
            case "spam":
                return "Spam / Quảng cáo trái phép";
            case "fake_review":
                return "Fake review / Đánh giá giả mạo";
            case "other":
                return "Other violation / Lý do khác";
            default:
                return code;
        }
    }

    private String cleanNote(String note) {
        if (note == null) return "";
        if (note.contains("[Bằng chứng: ")) {
            int start = note.indexOf("[Bằng chứng: ");
            return note.substring(0, start).trim();
        }
        return note.trim();
    }

    private String maskUserName(String name, int id) {
        if (name == null || name.isBlank()) return "User***" + (id % 100);
        if (name.length() <= 4) return name.charAt(0) + "***" + (id % 100);
        return name.substring(0, Math.min(4, name.length())) + "***" + (id % 100);
    }

    private void buildChatHistoryPreview(ReviewReportDetailDTO dto) {
        List<ChatMessageItem> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date baseDate = dto.getReviewDate() != null ? dto.getReviewDate() : (dto.getOrderDate() != null ? dto.getOrderDate() : new Date());

        long time1 = baseDate.getTime() - (18L * 3600 * 1000);
        long time2 = time1 + (4L * 60 * 1000);
        long time3 = time2 + (3L * 60 * 1000);

        String shopName = (dto.getReporterShopName() != null && !dto.getReporterShopName().isBlank())
                ? dto.getReporterShopName() : "Shop";
        String userName = dto.getReviewerName();

        list.add(new ChatMessageItem(
                shopName, "shop", sdf.format(new Date(time1)),
                "Chào bạn, sản phẩm bạn nhận được có bị hỏng hay gặp lỗi gì không ạ?"
        ));
        list.add(new ChatMessageItem(
                userName, "user", sdf.format(new Date(time2)),
                "Trái cây bị dập nát nhiều quá, không dùng được gì cả, rất thất vọng!"
        ));
        list.add(new ChatMessageItem(
                shopName, "shop", sdf.format(new Date(time3)),
                "Shop rất xin lỗi vì trải nghiệm không tốt này của bạn. Chúng mình có thể hỗ trợ gửi bù hoặc đổi mới sản phẩm cho bạn ngay nhé?"
        ));

        dto.setChatHistory(list);
    }
}
