package com.mycompany.onlinefruitshopping.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.mycompany.onlinefruitshopping.model.Coupon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CouponRulesTest {
    private Coupon coupon() {
        Coupon c = new Coupon();
        c.setShopId(1); c.setCode("SHOP20K"); c.setName("Giảm giá");
        c.setDiscountType("FIXED"); c.setDiscountValue(new BigDecimal("20000"));
        c.setMinOrderAmount(new BigDecimal("100000"));
        c.setStartsAt(LocalDateTime.of(2030, 1, 1, 0, 0)); c.setEndsAt(c.getStartsAt().plusDays(7));
        c.setActive(true);
        return c;
    }

    @Test void normalizeCodeWithoutAcceptingSpecialInput() {
        assertEquals("SHOP20K", CouponRules.normalizeCode(" shop20k "));
        for (String s : new String[] {null, "a", "GIẢM20K", "CODE SPACE", "' OR 1=1", "A".repeat(31)})
            assertThrows(IllegalArgumentException.class, () -> CouponRules.normalizeCode(s));
    }

    @Test void fixedDiscountUsesWholeOrderAndNeverGoesBelowZero() {
        Coupon c = coupon();
        assertEquals(new BigDecimal("20000"), CouponRules.discount(c, new BigDecimal("140000")));
        assertEquals(new BigDecimal("10000"), CouponRules.discount(c, new BigDecimal("10000")));
    }

    @Test void percentCapAndRounding() {
        Coupon c = coupon(); c.setDiscountType("PERCENT"); c.setDiscountValue(new BigDecimal("10"));
        c.setMaxDiscount(new BigDecimal("30000"));
        assertEquals(new BigDecimal("30000"), CouponRules.discount(c, new BigDecimal("500000")));
        assertEquals(new BigDecimal("10000"), CouponRules.discount(c, new BigDecimal("100001")));
    }

    @Test void boundaryMinSpendIsAccepted() {
        Coupon c = coupon();
        assertDoesNotThrow(() -> CouponRules.validateUse(c, new BigDecimal("100000"), 0, c.getStartsAt()));
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validateUse(c, new BigDecimal("99999"), 0, c.getStartsAt()));
    }

    @Test void effectivePeriodHasExclusiveEnd() {
        Coupon c = coupon();
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validateUse(c, new BigDecimal("100000"), 0, c.getStartsAt().minusSeconds(1)));
        assertDoesNotThrow(() -> CouponRules.validateUse(c, new BigDecimal("100000"), 0, c.getEndsAt().minusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validateUse(c, new BigDecimal("100000"), 0, c.getEndsAt()));
    }

    @Test void disabledAndDeletedCodesCannotBeUsed() {
        Coupon c = coupon(); c.setActive(false);
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validateUse(c, new BigDecimal("100000"), 0, c.getStartsAt()));
        c.setActive(true); c.setDeleted(true);
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validateUse(c, new BigDecimal("100000"), 0, c.getStartsAt()));
    }

    @Test void bothTotalAndCustomerQuotaAreChecked() {
        Coupon c = coupon(); c.setUsedCount(100);
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validateUse(c, new BigDecimal("100000"), 0, c.getStartsAt()));
        c.setUsedCount(0);
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validateUse(c, new BigDecimal("100000"), 1, c.getStartsAt()));
    }

    @Test void quotasAndMoneyConfigurationMustBeValid() {
        Coupon c = coupon(); c.setTotalLimit(0);
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validate(c));
        c.setTotalLimit(1); c.setPerCustomerLimit(2);
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validate(c));
        c.setPerCustomerLimit(1); c.setMinOrderAmount(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validate(c));
        c.setMinOrderAmount(BigDecimal.ZERO); c.setMaxDiscount(BigDecimal.TEN);
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validate(c)); // FIXED không dùng trần %
        c.setDiscountType("PERCENT"); c.setDiscountValue(new BigDecimal("101"));
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validate(c));
    }

    @Test void invalidNamePeriodAndFractionalMoneyAreRejected() {
        Coupon c = coupon(); c.setName(" ");
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validate(c));
        c.setName("Coupon"); c.setEndsAt(c.getStartsAt());
        assertThrows(IllegalArgumentException.class, () -> CouponRules.validate(c));
        assertThrows(IllegalArgumentException.class, () -> CouponRules.money(new BigDecimal("0.5"), true));
        assertThrows(IllegalArgumentException.class, () -> CouponRules.money(new BigDecimal("1000000000000000000"), true));
    }
}
