package com.mycompany.onlinefruitshopping.dao.coupon;

import com.mycompany.onlinefruitshopping.model.Coupon;
import com.mycompany.onlinefruitshopping.model.CouponQuote;
import com.mycompany.onlinefruitshopping.support.ModuleDatabase;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;
import java.math.BigDecimal;
import java.sql.Connection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Regression: checkout preview never burns a coupon; a single transaction saves order+payment+usage. */
class CheckoutCouponTest {
    @Test void previewAndAtomicApply() throws Exception {
        ModuleDatabase db=new ModuleDatabase();
        Coupon coupon=new Coupon();coupon.setShopId(1);coupon.setCode("SHOP10");coupon.setName("Shop coupon");
        coupon.setDiscountValue(BigDecimal.TEN);
        coupon.setStartsAt(PromotionRules.now().minusDays(1));
        coupon.setEndsAt(PromotionRules.now().plusDays(1));coupon.setActive(true);
        new CouponDAO(db::open).save(10,coupon);
        CouponCheckoutDAO checkout=new CouponCheckoutDAO(db::open);
        CouponQuote quote=checkout.preview(30,1,"shop10",new BigDecimal("50000"));
        assertEquals(0,new BigDecimal("5000").compareTo(quote.getDiscountAmount()));
        assertEquals(0,db.count("SELECT COUNT(*) FROM coupon_usages"));
        assertEquals(1,checkout.availableForShop(1,30,new BigDecimal("50000")).size());
        assertTrue(checkout.availableForShop(2,30,new BigDecimal("50000")).isEmpty());
        assertThrows(IllegalArgumentException.class,()->checkout.preview(30,2,"SHOP10",new BigDecimal("50000")));
        CouponApplicationDAO application=new CouponApplicationDAO(db::open);
        try(Connection c=db.open()) {
            c.setAutoCommit(false);
            application.applyDuringCheckout(c,30,103,"SHOP10");
            c.rollback();
        }
        assertEquals("75000.00",new BigDecimal(db.scalar("SELECT total_amount FROM orders WHERE order_id=103")).setScale(2).toPlainString());
        assertEquals(0,db.count("SELECT COUNT(*) FROM coupon_usages"));
        try(Connection c=db.open()) {
            c.setAutoCommit(false);
            application.applyDuringCheckout(c,30,103,"SHOP10");
            c.commit();
        }
        assertEquals(0,new BigDecimal("70000").compareTo(new BigDecimal(db.scalar("SELECT total_amount FROM orders WHERE order_id=103"))));
        assertEquals(0,new BigDecimal("70000").compareTo(new BigDecimal(db.scalar("SELECT amount FROM payments WHERE order_id=103"))));
        assertEquals(1,db.count("SELECT COUNT(*) FROM coupon_usages WHERE usage_status='Applied'"));
        assertTrue(checkout.availableForShop(1,30,new BigDecimal("50000")).isEmpty());
    }
}
