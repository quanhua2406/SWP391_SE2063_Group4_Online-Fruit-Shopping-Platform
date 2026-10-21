package com.mycompany.onlinefruitshopping.shopowner.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.mycompany.onlinefruitshopping.shopowner.model.Promotion;

/** Quy tắc nghiệp vụ đặt một chỗ để Servlet/DAO không phải viết lại. */
public final class PromotionRules {
    private PromotionRules() { }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).withNano(0);
    }

    public static void validate(Promotion p) {
        if (p.getName() == null || p.getName().isBlank() || p.getName().length() > 150)
            throw new IllegalArgumentException("Tên chương trình phải từ 1 đến 150 ký tự.");
        if (p.getDescription() == null || p.getDescription().length() > 500)
            throw new IllegalArgumentException("Mô tả không được vượt quá 500 ký tự.");
        validateDiscount(p.getDiscountType(), p.getDiscountValue());
        if (p.getStartsAt() == null || p.getEndsAt() == null
                || !p.getStartsAt().isBefore(p.getEndsAt()))
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        if (p.getStartsAt().getYear() < 2000 || p.getEndsAt().getYear() > 9999)
            throw new IllegalArgumentException("Năm áp dụng phải nằm trong khoảng 2000–9999.");
        if (p.getShopId() <= 0)
            throw new IllegalArgumentException("Hãy chọn cửa hàng của bạn.");
        if (p.getProductIds() == null || p.getProductIds().isEmpty())
            throw new IllegalArgumentException("Hãy chọn ít nhất một sản phẩm.");
        if (p.getProductIds().stream().anyMatch(id -> id == null || id <= 0))
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
    }

    public static void validateDiscount(String type, BigDecimal value) {
        if (!"PERCENT".equals(type) && !"FIXED".equals(type))
            throw new IllegalArgumentException("Loại giảm giá phải là phần trăm hoặc số tiền.");
        if (value == null || value.signum() <= 0 || value.stripTrailingZeros().scale() > 2
                || value.compareTo(new BigDecimal("9999999999999999.99")) > 0)
            throw new IllegalArgumentException("Mức giảm phải lớn hơn 0 và có tối đa 2 chữ số thập phân.");
        if ("PERCENT".equals(type) && value.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Phần trăm giảm không được vượt quá 100%.");
        if ("FIXED".equals(type) && value.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Số tiền giảm phải là số nguyên đồng.");
    }

    public static void validateActivation(Promotion p, LocalDateTime now) {
        validate(p);
        if (!p.getEndsAt().isAfter(now))
            throw new IllegalArgumentException("Chương trình đã hết hạn. Hãy sửa thời gian trước khi bật.");
    }

    public static boolean isEffective(Promotion p, LocalDateTime now) {
        return p.isActive() && p.getStartsAt() != null && p.getEndsAt() != null
                && !now.isBefore(p.getStartsAt()) && now.isBefore(p.getEndsAt());
    }

    public static String status(Promotion p, LocalDateTime now) {
        if (!p.isActive()) return "Đang tắt";
        if (now.isBefore(p.getStartsAt())) return "Chưa đến giờ";
        if (!now.isBefore(p.getEndsAt())) return "Đã hết hạn";
        return "Đang áp dụng";
    }

    public static BigDecimal priceAfterDiscount(BigDecimal price, String type, BigDecimal value) {
        validateDiscount(type, value);
        if (price == null || price.signum() < 0 || price.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Giá gốc phải là số nguyên đồng không âm.");
        BigDecimal discount = "PERCENT".equals(type)
                ? price.multiply(value).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)
                : value;
        // Giảm theo mỗi đơn vị biến thể; không để số tiền phải trả âm.
        return price.subtract(discount.min(price)).setScale(0, RoundingMode.HALF_UP);
    }
}
