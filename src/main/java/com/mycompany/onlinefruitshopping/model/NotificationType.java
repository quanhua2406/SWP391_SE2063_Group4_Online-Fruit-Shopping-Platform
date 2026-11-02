package com.mycompany.onlinefruitshopping.model;

/**
 * Các loại thông báo dành riêng cho Customer.
 */
public enum NotificationType {
    ORDER_CONFIRMATION("order_confirmation", "Xác nhận đơn hàng", "📦", "Xem đơn hàng", "order"),
    DELIVERY_STATUS("delivery_status", "Trạng thái giao hàng", "🚚", "Theo dõi đơn hàng", "delivery"),
    ORDER_DELIVERED("order_delivered", "Giao hàng thành công", "🚚", "Xem đơn hàng", "delivery"),
    PAYMENT_SUCCESS("payment_success", "Thanh toán thành công", "💳", "Chi tiết đơn hàng", "payment"),
    PAYMENT_FAILED("payment_failed", "Thanh toán thất bại", "⚠️", "Thử thanh toán lại", "payment"),
    REFUND_UPDATE("refund_update", "Hoàn tiền", "💳", "Xem chi tiết", "payment"),
    PROMOTION("promotion", "Khuyến mãi & Ưu đãi", "🏷️", "Xem ưu đãi", "promotion"),
    SUBSCRIPTION("subscription", "Gói đăng ký định kỳ", "🔄", "Xem gói đăng ký", "subscription"),
    SUBSCRIPTION_REMINDER("subscription_reminder", "Gói đăng ký định kỳ", "🔄", "Xem gói đăng ký", "subscription");

    private final String code;
    private final String displayName;
    private final String defaultIcon;
    private final String defaultActionText;
    private final String groupCategory; // Phục vụ lọc tab: order, delivery, payment, promotion, subscription

    NotificationType(String code, String displayName, String defaultIcon, String defaultActionText, String groupCategory) {
        this.code = code;
        this.displayName = displayName;
        this.defaultIcon = defaultIcon;
        this.defaultActionText = defaultActionText;
        this.groupCategory = groupCategory;
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

    /**
     * Tìm Enum theo mã code, nếu không khớp chính xác sẽ tìm linh hoạt theo từ khóa
     */
    public static NotificationType fromCode(String code) {
        if (code == null) return null;
        for (NotificationType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        String lower = code.toLowerCase();
        if (lower.contains("deliver")) return DELIVERY_STATUS;
        if (lower.contains("refund")) return REFUND_UPDATE;
        if (lower.contains("payment")) return PAYMENT_SUCCESS;
        if (lower.contains("order")) return ORDER_CONFIRMATION;
        if (lower.contains("subscription")) return SUBSCRIPTION;
        if (lower.contains("promo") || lower.contains("coupon")) return PROMOTION;
        return null;
    }
}
