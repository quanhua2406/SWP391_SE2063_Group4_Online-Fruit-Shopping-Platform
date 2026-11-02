package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTO chứa toàn bộ dữ liệu chi tiết cho màn hình kiểm duyệt báo cáo đánh giá
 * (Admin Review Report Moderation Detail View).
 */
public class ReviewReportDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // ── 1. Report Info ──
    private int reportId;
    private int reviewId;
    private int reporterId;
    private String reporterName;
    private String reporterShopName;
    private Date reportDate;
    private String reason;
    private String reasonLabel;
    private String sellerDescription;
    private List<String> evidenceImages = new ArrayList<>();

    // ── 2. Original Review ──
    private int rating;
    private String reviewComment;
    private int reviewerId;
    private String reviewerName;
    private Date reviewDate;
    private String reviewStatus;
    private List<String> reviewImages = new ArrayList<>();

    // ── 3. Order Details ──
    private int orderId;
    private String orderFormattedId;
    private int productId;
    private String productName;
    private String variant;
    private double price;
    private Date orderDate;
    private String orderStatus;
    private Date deliveredDate;

    // ── 4. Internal Notes & History ──
    private String internalNotes;

    // ── 5. Chat History Item ──
    public static class ChatMessageItem implements Serializable {
        private String sender;
        private String senderType; // "shop" or "user"
        private String time;
        private String message;

        public ChatMessageItem() {}

        public ChatMessageItem(String sender, String senderType, String time, String message) {
            this.sender = sender;
            this.senderType = senderType;
            this.time = time;
            this.message = message;
        }

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }

        public String getSenderType() { return senderType; }
        public void setSenderType(String senderType) { this.senderType = senderType; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    private List<ChatMessageItem> chatHistory = new ArrayList<>();

    public ReviewReportDetailDTO() {}

    // ── Getters and Setters ──
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterShopName() { return reporterShopName; }
    public void setReporterShopName(String reporterShopName) { this.reporterShopName = reporterShopName; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReasonLabel() { return reasonLabel; }
    public void setReasonLabel(String reasonLabel) { this.reasonLabel = reasonLabel; }

    public String getSellerDescription() { return sellerDescription; }
    public void setSellerDescription(String sellerDescription) { this.sellerDescription = sellerDescription; }

    public List<String> getEvidenceImages() { return evidenceImages; }
    public void setEvidenceImages(List<String> evidenceImages) { this.evidenceImages = evidenceImages; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public int getReviewerId() { return reviewerId; }
    public void setReviewerId(int reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public Date getReviewDate() { return reviewDate; }
    public void setReviewDate(Date reviewDate) { this.reviewDate = reviewDate; }

    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public List<String> getReviewImages() { return reviewImages; }
    public void setReviewImages(List<String> reviewImages) { this.reviewImages = reviewImages; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getOrderFormattedId() { return orderFormattedId; }
    public void setOrderFormattedId(String orderFormattedId) { this.orderFormattedId = orderFormattedId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public Date getDeliveredDate() { return deliveredDate; }
    public void setDeliveredDate(Date deliveredDate) { this.deliveredDate = deliveredDate; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public List<ChatMessageItem> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<ChatMessageItem> chatHistory) { this.chatHistory = chatHistory; }
}

