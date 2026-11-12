package com.mycompany.onlinefruitshopping.dao.promotion;

import com.mycompany.onlinefruitshopping.dao.CartDAO;
import com.mycompany.onlinefruitshopping.dao.OrderDAO;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.support.ModuleDatabase;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PromotionPricingTest {
    ModuleDatabase db;
    final LocalDateTime noon = LocalDateTime.of(2026, 11, 12, 12, 0);

    @BeforeEach void setup() throws Exception { db = new ModuleDatabase(); }

    void campaign(String type, String discount, LocalDateTime start, LocalDateTime end) throws Exception {
        try (Connection c = db.open(); PreparedStatement ps = c.prepareStatement("""
            INSERT INTO promotions(promotion_id,shop_id,promotion_name,discount_type,discount_value,
                                   starts_at,ends_at,is_active) VALUES(1,1,'Offer',?,?,?, ?,1)
            """)) {
            ps.setString(1,type); ps.setBigDecimal(2,new BigDecimal(discount));
            ps.setTimestamp(3,Timestamp.valueOf(start)); ps.setTimestamp(4,Timestamp.valueOf(end));
            ps.executeUpdate();
        }
        db.exec("INSERT INTO promotion_products VALUES(1,1)");
    }

    BigDecimal price(int variant, LocalDateTime time) throws Exception {
        return new BigDecimal(db.scalar("SELECT price FROM " + PromotionPricing.customerVariants(time)
                + " priced WHERE variant_id=" + variant));
    }

    void assertPrice(String expected, int variant, LocalDateTime time) throws Exception {
        assertEquals(0,new BigDecimal(expected).compareTo(price(variant,time)));
    }

    @Test void percentageAppliesToSelectedProductsAndAllTheirVariantsOnly() throws Exception {
        db.exec("INSERT INTO product_variants(variant_id,product_id,sku,price,unit) VALUES(4,1,'ORANGE2',150005,'piece')");
        campaign("PERCENT","10",noon,noon.plusDays(1));
        assertPrice("90000",1,noon); assertPrice("135004",4,noon);
        assertPrice("100000",2,noon); // other shop
        assertEquals("100000.00",db.scalar("SELECT price FROM product_variants WHERE variant_id=1"));
        assertEquals("50000.00",db.scalar("SELECT unit_price FROM order_items WHERE order_id=103"));
    }

    @Test void startIsInclusiveEndIsExclusiveAndDisabledOrDeletedNeverApplies() throws Exception {
        campaign("PERCENT","10",noon,noon.plusHours(1));
        assertPrice("100000",1,noon.minusSeconds(1)); assertPrice("90000",1,noon);
        assertPrice("100000",1,noon.plusHours(1));
        db.exec("UPDATE promotions SET is_active=0"); assertPrice("100000",1,noon);
        db.exec("UPDATE promotions SET is_deleted=1"); assertPrice("100000",1,noon);
    }

    @Test void fixedDiscountCannotMakePriceNegative() throws Exception {
        campaign("FIXED","200000",noon,noon.plusDays(1)); assertPrice("0",1,noon);
    }

    @Test void approvedVariantUsesBestPriceWithoutStackingAndIncludesEndDate() throws Exception {
        campaign("PERCENT","10",noon,noon.plusDays(2));
        db.exec("INSERT INTO variant_promotions(variant_id,promo_price,start_date,end_date,status) VALUES(1,80000,'2026-11-12','2026-11-12','pending')");
        assertPrice("90000",1,noon);
        db.exec("UPDATE variant_promotions SET status='approved'");
        assertPrice("80000",1,noon); assertPrice("80000",1,noon.withHour(23));
        assertPrice("90000",1,noon.plusDays(1));
        db.exec("UPDATE variant_promotions SET promo_price=120000"); assertPrice("90000",1,noon);
        db.exec("UPDATE variant_promotions SET status='rejected',promo_price=70000"); assertPrice("90000",1,noon);
    }

    @Test void wrongShopHiddenProductOrInactiveShopCannotSupplyCampaignDiscount() throws Exception {
        campaign("PERCENT","10",noon,noon.plusDays(1));
        db.exec("UPDATE promotions SET shop_id=2"); assertPrice("100000",1,noon);
        db.exec("UPDATE promotions SET shop_id=1", "UPDATE products SET hidden_by_admin=1 WHERE product_id=1");
        assertPrice("100000",1,noon);
        db.exec("UPDATE products SET hidden_by_admin=0 WHERE product_id=1", "UPDATE shops SET status='suspended' WHERE shop_id=1");
        assertPrice("100000",1,noon);
    }

    @Test void cartAndGuestRefreshReadEffectivePrices() throws Exception {
        LocalDateTime now=PromotionRules.now();
        campaign("FIXED","25000",now.minusDays(1),now.plusDays(1));
        db.exec("INSERT INTO carts(cart_id,user_id) VALUES(1,30)","INSERT INTO cart_items(cart_id,variant_id,quantity) VALUES(1,1,2)");
        CartDAO cart=new CartDAO(db::open);
        assertEquals(75000,cart.getVariantDetailForCart(1).getUnitPrice());
        List<CartItem> items=cart.getCartItemsByCartId(1);
        assertEquals(75000,items.get(0).getUnitPrice()); assertEquals(150000,items.get(0).getItemTotal());
        try(Connection c=db.open()) { PromotionPricing.validateCheckoutPrices(c,1,items); }
        db.exec("UPDATE promotions SET is_active=0");
        try(Connection c=db.open()) {
            assertThrows(IllegalArgumentException.class,()->PromotionPricing.validateCheckoutPrices(c,1,items));
        }
        assertEquals(100000,cart.getVariantDetailForCart(1).getUnitPrice());
    }

    @Test void savedOrderAndPaymentUseDiscountedSnapshotAndRejectStaleCheckout() throws Exception {
        LocalDateTime now=PromotionRules.now();
        campaign("PERCENT","10",now.minusDays(1),now.plusDays(1));
        db.exec("INSERT INTO carts(cart_id,user_id) VALUES(1,30)","INSERT INTO cart_items(cart_id,variant_id,quantity) VALUES(1,1,2)");
        List<CartItem> items=new CartDAO(db::open).getCartItemsByCartId(1);
        OrderDAO orders=new OrderDAO(db::open);
        int id=orders.createOrder(30,1,1,null,"standard","cod",205000,25000,0,items,null);
        assertEquals("90000.00",db.scalar("SELECT unit_price FROM order_items WHERE order_id="+id));
        assertEquals("205000.00",db.scalar("SELECT total_amount FROM orders WHERE order_id="+id));
        assertEquals("205000.00",db.scalar("SELECT amount FROM payments WHERE order_id="+id));
        assertEquals(0,db.count("SELECT COUNT(*) FROM cart_items WHERE cart_id=1"));
        db.exec("UPDATE promotions SET is_active=0");
        int count=db.count("SELECT COUNT(*) FROM orders");
        assertThrows(IllegalArgumentException.class,()->orders.createOrder(30,1,1,null,
                "standard","cod",205000,25000,0,items,null));
        assertEquals(count,db.count("SELECT COUNT(*) FROM orders"));
        assertEquals("90000.00",db.scalar("SELECT unit_price FROM order_items WHERE order_id="+id));
    }

    @Test void checkoutRejectsWrongShopAndForgedPrice() throws Exception {
        CartItem item=new CartItem(); item.setVariantId(1); item.setUnitPrice(1);
        try(Connection c=db.open()) {
            assertThrows(IllegalArgumentException.class,()->PromotionPricing.validateCheckoutPrices(c,1,List.of(item)));
            item.setUnitPrice(100000);
            assertThrows(IllegalArgumentException.class,()->PromotionPricing.validateCheckoutPrices(c,2,List.of(item)));
            PromotionPricing.validateCheckoutPrices(c,1,List.of(item));
        }
    }
}
