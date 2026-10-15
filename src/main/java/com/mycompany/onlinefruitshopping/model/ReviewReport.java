package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model ánh xạ bảng review_reports.
 * Mỗi bản ghi = 1 lần user báo cáo 1 review.
 */
public class ReviewReport implements Serializable {
    private static final long serialVersionUID = 1L;

    // ── Các trường chính (ánh xạ cột DB) ─────────────────────────────────
    private int       reportId;
    private int       reviewId;
    private int       reporterId;      // user_id của người báo cáo
    private String    reason;          // spam | inappropriate_language | fake_review | other
    private String    note;            // ghi chú thêm (có thể null)
    private Timestamp createdAt;

    // ── Trường phụ – dùng khi JOIN để hiển thị ────────────────────────────
    private String    reporterName;    // tên người báo cáo
    private String    reviewComment;   // nội dung review bị báo cáo
    private int       reviewRating;    // số sao của review bị báo cáo

    // ── Danh sách lý do hợp lệ (dùng validate phía Java) ─────────────────
    public static final String REASON_SPAM           = "spam";
    public static final String REASON_INAPPROPRIATE  = "inappropriate_language";
    public static final String REASON_FAKE           = "fake_review";
    public static final String REASON_OTHER          = "other";

    // ── Constructors ──────────────────────────────────────────────────────
    public ReviewReport() {}

    public ReviewReport(int reviewId, int reporterId, String reason, String note) {
        this.reviewId   = reviewId;
        this.reporterId = reporterId;
        this.reason     = reason;
        this.note       = note;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public int getReportId()              { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getReviewId()              { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getReporterId()                  { return reporterId; }
    public void setReporterId(int reporterId)   { this.reporterId = reporterId; }

    public String getReason()                   { return reason; }
    public void setReason(String reason)        { this.reason = reason; }

    public String getNote()                     { return note; }
    public void setNote(String note)            { this.note = note; }

    public Timestamp getCreatedAt()             { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getReporterName()             { return reporterName; }
    public void setReporterName(String name)    { this.reporterName = name; }

    public String getReviewComment()            { return reviewComment; }
    public void setReviewComment(String comment){ this.reviewComment = comment; }

    public int getReviewRating()                { return reviewRating; }
    public void setReviewRating(int rating)     { this.reviewRating = rating; }

    // ── Tiện ích ──────────────────────────────────────────────────────────
    /** Kiểm tra reason có hợp lệ không */
    public static boolean isValidReason(String reason) {
        return REASON_SPAM.equals(reason)
            || REASON_INAPPROPRIATE.equals(reason)
            || REASON_FAKE.equals(reason)
            || REASON_OTHER.equals(reason);
    }

    /** Lấy nhãn tiếng Việt của reason để hiển thị trên UI */
    public String getReasonLabel() {
        if (reason == null) return "";
        switch (reason) {
            case REASON_SPAM:          return "Spam";
            case REASON_INAPPROPRIATE: return "Ngôn ngữ không phù hợp";
            case REASON_FAKE:          return "Đánh giá giả mạo";
            case REASON_OTHER:         return "Lý do khác";
            default:                   return reason;
        }
    }
}
