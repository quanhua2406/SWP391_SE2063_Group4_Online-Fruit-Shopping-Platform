package com.mycompany.onlinefruitshopping.shopowner.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import com.mycompany.onlinefruitshopping.shopowner.model.Coupon;

/** Kiểm tra coupon và tính tiền, không truy cập database. */
public final class CouponRules {
    private CouponRules() { }

    public static String normalizeCode(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9_-]{3,30}"))
            throw new IllegalArgumentException("Mã gồm 3–30 ký tự: chữ A–Z, số, dấu - hoặc dấu _.");
        return normalized;
    }

    public static void validate(Coupon p) {
        normalizeCode(p.getCode());
        if (p.getShopId() <= 0) throw new IllegalArgumentException("Hãy chọn cửa hàng của bạn.");
        if (p.getName() == null || p.getName().isBlank() || p.getName().length() > 150)
            throw new IllegalArgumentException("Tên mã giảm giá phải từ 1 đến 150 ký tự.");
        if (p.getDescription() == null || p.getDescription().length() > 500)
            throw new IllegalArgumentException("Mô tả tối đa 500 ký tự.");
        PromotionRules.validateDiscount(p.getDiscountType(), p.getDiscountValue());
        money(p.getMinOrderAmount(), true);
        if (p.getMaxDiscount() != null) {
            money(p.getMaxDiscount(), false);
            if (!"PERCENT".equals(p.getDiscountType()))
                throw new IllegalArgumentException("Chỉ giảm theo phần trăm mới cần mức giảm tối đa.");
        }
        if (p.getTotalLimit() <= 0 || p.getPerCustomerLimit() <= 0 || p.getPerCustomerLimit() > p.getTotalLimit())
            throw new IllegalArgumentException("Số lượt phải lớn hơn 0; lượt mỗi khách không vượt tổng lượt.");
        if (p.getStartsAt() == null || p.getEndsAt() == null || !p.getStartsAt().isBefore(p.getEndsAt())
                || p.getStartsAt().getYear() < 2000 || p.getEndsAt().getYear() > 9999)
            throw new IllegalArgumentException("Nhập khoảng thời gian hợp lệ, kết thúc sau bắt đầu, năm từ 2000–9999.");
    }

    public static void money(BigDecimal amount, boolean allowZero) {
        if (amount == null || amount.signum() < 0 || (!allowZero && amount.signum() == 0)
                || amount.stripTrailingZeros().scale() > 0
                || amount.compareTo(new BigDecimal("999999999999999999")) > 0)
            throw new IllegalArgumentException("Số tiền phải là số nguyên đồng hợp lệ" + (allowZero ? "." : " và lớn hơn 0."));
    }

    public static void validateUse(Coupon p, BigDecimal subtotal, int usedByCustomer, LocalDateTime now) {
        validate(p);
        if (p.isDeleted() || !p.isActive()) throw new IllegalArgumentException("Mã đã bị tắt hoặc xóa.");
        if (now.isBefore(p.getStartsAt())) throw new IllegalArgumentException("Mã chưa đến thời gian sử dụng.");
        if (!now.isBefore(p.getEndsAt())) throw new IllegalArgumentException("Mã đã hết hạn.");
        money(subtotal, false);
        if (subtotal.compareTo(p.getMinOrderAmount()) < 0)
            throw new IllegalArgumentException("Đơn hàng chưa đạt số tiền tối thiểu của mã.");
        if (p.getUsedCount() >= p.getTotalLimit()) throw new IllegalArgumentException("Mã đã hết lượt sử dụng.");
        if (usedByCustomer >= p.getPerCustomerLimit()) throw new IllegalArgumentException("Bạn đã dùng hết lượt được phép của mã này.");
    }

    public static BigDecimal discount(Coupon p, BigDecimal subtotal) {
        money(subtotal, false);
        BigDecimal amount = subtotal.subtract(PromotionRules.priceAfterDiscount(subtotal,
                p.getDiscountType(), p.getDiscountValue()));
        if (p.getMaxDiscount() != null) amount = amount.min(p.getMaxDiscount());
        return amount.min(subtotal).setScale(0);
    }
}
