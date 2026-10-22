package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Entity Notification đại diện cho bảng notifications trong SQL Server.
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    // Database fields
    private int notificationId;
    private int userId;
    private String title;
    private String content;
    private String type;            // order_confirmation, delivery_status, etc.
    private String referenceType;   // order, delivery, payment, promotion, subscription
    private String referenceId;     // id đơn hàng, id gói sub, v.v.
    private String channel;         // in_app, email, both (mặc định in_app)
    private boolean isRead;
    private boolean isImportant;
    private Timestamp createdAt;
    private Timestamp readAt;

    // View helper fields (được tính toán hoặc enrich thêm từ Service)
    private String actionUrl;
    private String actionText;
    private String icon;
    private String typeDisplayName;
    private String timeAgo;
    private String productImageUrl;

    public Notification() {
        this.channel = "in_app";
        this.isRead = false;
        this.isImportant = false;
    }

    public Notification(int userId, String title, String content, String type, 
                        String referenceType, String referenceId) {
        this();
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    // --- Getters and Setters ---

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getReadAt() {
        return readAt;
    }

    public void setReadAt(Timestamp readAt) {
        this.readAt = readAt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTypeDisplayName() {
        return typeDisplayName;
    }

    public void setTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
