package com.mycompany.onlinefruitshopping.model;

/**
 * Các loại thông báo dành riêng cho Shop Owner.
 * Quy tắc: Các loại NEW_ORDER, LOW_STOCK, REFUND_REQUEST bắt buộc mặc định isImportant = true.
 */
public enum ShopOwnerNotificationType {
    NEW_ORDER("new_order", "Đơn hàng mới", "🛒", "Xem đơn hàng", "new_order", true),
    PAYMENT_RECEIVED("payment_received", "Đã nhận thanh toán", "💰", "Chi tiết đơn hàng", "payment", false),
    LOW_STOCK("low_stock", "Cảnh báo tồn kho", "⚠️", "Cập nhật tồn kho", "low_stock", true),
    REFUND_REQUEST("refund_request", "Yêu cầu hoàn tiền", "🔄", "Xử lý hoàn tiền", "refund", true),
    ORDER_CANCELLED("order_cancelled", "Đơn hàng đã hủy", "❌", "Xem chi tiết", "cancelled", false);

    private final String code;
    private final String displayName;
    private final String defaultIcon;
    private final String defaultActionText;
    private final String groupCategory; // Phục vụ lọc tab sidebar
    private final boolean important;    // Quy tắc nghiệp vụ

    ShopOwnerNotificationType(String code, String displayName, String defaultIcon, 
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
    public static ShopOwnerNotificationType fromCode(String code) {
        if (code == null) return null;
        for (ShopOwnerNotificationType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
