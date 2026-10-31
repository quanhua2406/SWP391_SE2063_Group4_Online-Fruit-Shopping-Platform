package com.mycompany.onlinefruitshopping.model;

/**
 * Enum định nghĩa các loại thông báo dành riêng cho Delivery Staff (Nhân viên giao hàng).
 */
public enum DeliveryNotificationType {
    NEW_DELIVERY_ASSIGNMENT("new_delivery_assignment", "Đơn hàng mới được phân công", "🚚", true, "Xem & Nhận đơn"),
    DELIVERY_STATUS_UPDATE("delivery_status_update", "Cập nhật trạng thái giao hàng", "📍", false, "Xem chi tiết");

    private final String code;
    private final String displayName;
    private final String icon;
    private final boolean defaultImportant;
    private final String defaultActionText;

    DeliveryNotificationType(String code, String displayName, String icon, boolean defaultImportant, String defaultActionText) {
        this.code = code;
        this.displayName = displayName;
        this.icon = icon;
        this.defaultImportant = defaultImportant;
        this.defaultActionText = defaultActionText;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isDefaultImportant() {
        return defaultImportant;
    }

    public String getDefaultActionText() {
        return defaultActionText;
    }

    public static DeliveryNotificationType fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (DeliveryNotificationType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        String lower = code.toLowerCase();
        if (lower.contains("assign")) return NEW_DELIVERY_ASSIGNMENT;
        if (lower.contains("status") || lower.contains("deliver")) return DELIVERY_STATUS_UPDATE;
        return null;
    }
}
