package com.mycompany.onlinefruitshopping.dao.review;

import com.mycompany.onlinefruitshopping.model.ReviewReport;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewReportDAO {

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
    // 2. Tạo báo cáo mới
    // ─────────────────────────────────────────────────────────────────────

    public int createReport(ReviewReport report) throws SQLException {
        String sql = "INSERT INTO review_reports (review_id, reporter_id, reason, note) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, report.getReviewId());
            ps.setInt(2, report.getReporterId());
            ps.setString(3, report.getReason());
            ps.setString(4, report.getNote());   // có thể null

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
        String sql = "SELECT rr.report_id, rr.review_id, rr.reporter_id, "
                   + "       rr.reason, rr.note, rr.created_at, "
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
                    r.setNote(rs.getString("note"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setReporterName(rs.getString("reporter_name"));
                    list.add(r);
                }
            }
        }
        return list;
    }
}
