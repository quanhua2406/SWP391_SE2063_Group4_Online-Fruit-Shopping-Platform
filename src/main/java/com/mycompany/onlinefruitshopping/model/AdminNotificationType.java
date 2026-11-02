package com.mycompany.onlinefruitshopping.model;

/**
 * Các loại thông báo dành riêng cho Admin (Hệ thống & Kiểm duyệt).
 * Quy tắc: Các loại REPORTED_REVIEW, CRITICAL_LOW_STOCK, REFUND_REQUEST bắt buộc mặc định isImportant = true.
 */
public enum AdminNotificationType {
    REPORTED_REVIEW("reported_review", "Đánh giá bị báo cáo", "🚩", "Xem & Kiểm duyệt", "reported_review", true),
    CRITICAL_LOW_STOCK("critical_low_stock", "Tồn kho nghiêm trọng", "⚠️", "Kiểm tra tồn kho", "critical_low_stock", true),
    REFUND_REQUEST("refund_request", "Yêu cầu hoàn tiền", "🔄", "Xử lý hoàn tiền", "refund", true),
    ORDER_CANCELLED("order_cancelled", "Đơn hàng bị hủy", "❌", "Chi tiết đơn hàng", "cancelled", false),
    NEW_SHOP_REGISTRATION("new_shop_registration", "Đăng ký cửa hàng mới", "🏪", "Xét duyệt cửa hàng", "new_shop", false),
    SYSTEM_ALERT("system_alert", "Cảnh báo hệ thống", "🔔", "Xem chi tiết", "system_alert", false);

    private final String code;
    private final String displayName;
    private final String defaultIcon;
    private final String defaultActionText;
    private final String groupCategory; // Phục vụ lọc tab sidebar
    private final boolean important;    // Quy tắc nghiệp vụ bắt buộc

    AdminNotificationType(String code, String displayName, String defaultIcon,
                          String defaultActionText, String groupCategory, boolean important) {
        this.code = code;
        this.displayName = displayName;
        this.defaultIcon = defaultIcon;
        this.defaultActionText = defaultActionText;
        this.groupCategory = groupCategory;
        this.important = important;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public String getDefaultActionText() {
        return defaultActionText;
    }

    public String getGroupCategory() {
        return groupCategory;
    }

    public boolean isImportant() {
        return important;
    }

    /**
     * Tìm Enum theo mã code, nếu không tìm thấy trả về null
     */
    public static AdminNotificationType fromCode(String code) {
        if (code == null) return null;
        for (AdminNotificationType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
