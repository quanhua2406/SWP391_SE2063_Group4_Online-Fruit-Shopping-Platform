package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;

/**
 * Model đại diện cho thông báo (Notification) trong hệ thống Online Fruit Shopping.
 * Ánh xạ tới bảng `notifications` trong cơ sở dữ liệu.
 * Hỗ trợ đầy đủ các thuộc tính lưu trữ CSDL và các thuộc tính hỗ trợ hiển thị giao diện (UI helpers).
 */
public class Notification {

    // =========================================================================
    // DATABASE COLUMNS
    // =========================================================================
    private int notificationId;
    private int userId;
    private String title;
    private String content;
    private String type;
    private String referenceType;
    private String referenceId;
    private String channel = "in_app";
    private boolean isRead = false;
    private boolean isImportant = false;
    private Timestamp createdAt;
    private Timestamp readAt;

    // =========================================================================
    // UI DISPLAY HELPER ATTRIBUTES
    // =========================================================================
    private String timeAgo;
    private String actionText;
    private String actionUrl;
    private String icon;
    private String typeDisplayName;
    private String productImageUrl;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public Notification() {
        this.channel = "in_app";
        this.isRead = false;
        this.isImportant = false;
    }

    public Notification(int notificationId, int userId, String title, String content, String type,
                        String referenceType, String referenceId, String channel,
                        boolean isRead, boolean isImportant, Timestamp createdAt, Timestamp readAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.channel = channel;
        this.isRead = isRead;
        this.isImportant = isImportant;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    // =========================================================================
    // GETTERS & SETTERS (DATABASE COLUMNS)
    // =========================================================================

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    // Alias compatibility for getId / setId
    public int getId() {
        return notificationId;
    }

    public void setId(int id) {
        this.notificationId = id;
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

    // Alias compatibility for getMessage / setMessage
    public String getMessage() {
        return content;
    }

    public void setMessage(String message) {
        this.content = message;
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

    public boolean getIsRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public boolean getIsImportant() {
        return isImportant;
    }

    public void setImportant(boolean isImportant) {
        this.isImportant = isImportant;
    }

    public void setIsImportant(boolean isImportant) {
        this.isImportant = isImportant;
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

    // =========================================================================
    // GETTERS & SETTERS (UI HELPERS)
    // =========================================================================

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
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

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }
}
