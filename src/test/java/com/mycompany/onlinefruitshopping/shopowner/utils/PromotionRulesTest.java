package com.mycompany.onlinefruitshopping.shopowner.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.mycompany.onlinefruitshopping.shopowner.model.Promotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PromotionRulesTest {
    private Promotion sample() {
        Promotion p = new Promotion();
        p.setName("Tuần lễ trái cây");
        p.setShopId(1);
        p.setDiscountType("PERCENT");
        p.setDiscountValue(new BigDecimal("10"));
        p.setStartsAt(LocalDateTime.of(2030, 1, 1, 0, 0));
        p.setEndsAt(LocalDateTime.of(2030, 1, 8, 0, 0));
        p.setProductIds(List.of(1, 2));
        p.setActive(true);
        return p;
    }

    @Test void percentAndFixedDiscountsUseWholeDong() {
        assertEquals(new BigDecimal("90000"), PromotionRules.priceAfterDiscount(new BigDecimal("100000"), "PERCENT", new BigDecimal("10")));
        assertEquals(new BigDecimal("80000"), PromotionRules.priceAfterDiscount(new BigDecimal("100000"), "FIXED", new BigDecimal("20000")));
        assertEquals(new BigDecimal("9900"), PromotionRules.priceAfterDiscount(new BigDecimal("10001"), "PERCENT", new BigDecimal("1.01")));
    }

    @Test void discountCannotMakePriceNegative() {
        assertEquals(BigDecimal.ZERO, PromotionRules.priceAfterDiscount(new BigDecimal("10000"), "FIXED", new BigDecimal("20000")));
        assertEquals(BigDecimal.ZERO, PromotionRules.priceAfterDiscount(new BigDecimal("10000"), "PERCENT", new BigDecimal("100")));
        assertEquals(BigDecimal.ZERO, PromotionRules.priceAfterDiscount(BigDecimal.ZERO, "PERCENT", new BigDecimal("10")));
    }

    @Test void invalidDiscountsAndPricesAreRejected() {
        for (String value : List.of("0", "-1", "100.01", "0.001", "10000000000000000"))
            assertThrows(IllegalArgumentException.class, () -> PromotionRules.validateDiscount("PERCENT", new BigDecimal(value)));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validateDiscount("FIXED", new BigDecimal("100.5")));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validateDiscount("OTHER", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validateDiscount("PERCENT", null));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.priceAfterDiscount(new BigDecimal("-1"), "PERCENT", BigDecimal.TEN));
    }

    @Test void effectivePeriodIncludesStartButExcludesEnd() {
        Promotion p = sample();
        assertFalse(PromotionRules.isEffective(p, p.getStartsAt().minusSeconds(1)));
        assertTrue(PromotionRules.isEffective(p, p.getStartsAt()));
        assertTrue(PromotionRules.isEffective(p, p.getEndsAt().minusSeconds(1)));
        assertFalse(PromotionRules.isEffective(p, p.getEndsAt()));
        assertEquals("Chưa đến giờ", PromotionRules.status(p, p.getStartsAt().minusSeconds(1)));
        assertEquals("Đang áp dụng", PromotionRules.status(p, p.getStartsAt()));
        assertEquals("Đã hết hạn", PromotionRules.status(p, p.getEndsAt()));
    }

    @Test void disabledProgramDoesNotBecomeEffectiveAtStart() {
        Promotion p = sample();
        p.setActive(false);
        assertFalse(PromotionRules.isEffective(p, p.getStartsAt()));
        assertEquals("Đang tắt", PromotionRules.status(p, p.getStartsAt()));
    }

    @Test void activationAllowsFuturePeriodButRejectsExpiredPeriod() {
        Promotion p = sample();
        assertDoesNotThrow(() -> PromotionRules.validateActivation(p, p.getStartsAt().minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validateActivation(p, p.getEndsAt()));
    }

    @Test void invalidPeriodsAreRejected() {
        Promotion p = sample();
        p.setEndsAt(p.getStartsAt());
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
        p.setEndsAt(p.getStartsAt().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
        p.setStartsAt(null);
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
    }

    @Test void productAndShopAreRequired() {
        Promotion p = sample();
        p.setProductIds(List.of());
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
        p.setProductIds(List.of(-1));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
        p.setProductIds(List.of(1));
        p.setShopId(0);
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
    }

    @Test void textLengthsMatchDatabase() {
        Promotion p = sample();
        p.setName(" ");
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
        p.setName("x".repeat(151));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
        p.setName("Tên hợp lệ");
        p.setDescription("x".repeat(501));
        assertThrows(IllegalArgumentException.class, () -> PromotionRules.validate(p));
        p.setDescription("");
        assertDoesNotThrow(() -> PromotionRules.validate(p));
    }
}
