package com.mycompany.onlinefruitshopping.dao.coupon;
import com.mycompany.onlinefruitshopping.support.ModuleDatabase;
import com.mycompany.onlinefruitshopping.model.*;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;
import java.math.BigDecimal;
import java.sql.SQLException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class CouponDatabaseTest {
 ModuleDatabase db; CouponDAO coupons; CouponApplicationDAO applications; int couponId;
 @BeforeEach void setup() throws Exception {
  db=new ModuleDatabase();coupons=new CouponDAO(db::open);applications=new CouponApplicationDAO(db::open);
  Coupon p=new Coupon();p.setShopId(1);p.setCode("TEST10");p.setName("Test coupon");p.setDiscountValue(BigDecimal.TEN);
  p.setStartsAt(PromotionRules.now().minusDays(1));p.setEndsAt(PromotionRules.now().plusDays(1));p.setActive(true);
  couponId=coupons.save(10,p);
 }
 @Test void previewApplyRemoveKeepFeeAndSynchronizePayment() throws Exception {
  CouponQuote preview=applications.preview(30,103,"TEST10");
  assertMoney("70000",preview.getFinalAmount()); // 0.5 * 100000 goods + 25000 fee - 5000 discount
  assertEquals(0,db.count("SELECT COUNT(*) FROM coupon_usages"));
  assertMoney("75000",new BigDecimal(db.scalar("SELECT amount FROM payments WHERE order_id=103")));
  applications.apply(30,103,"TEST10");
  assertMoney("70000",new BigDecimal(db.scalar("SELECT total_amount FROM orders WHERE order_id=103")));
  assertMoney("70000",new BigDecimal(db.scalar("SELECT amount FROM payments WHERE order_id=103")));
  assertEquals(couponId,applications.findOrders(30).stream().filter(o->o.getId()==103).findFirst().orElseThrow().getCouponId());
  assertEquals(1,coupons.findAll(10).get(0).getUsedCount());
  assertEquals(1,coupons.findUsage(10,couponId).size());
  assertThrows(IllegalArgumentException.class,()->applications.apply(30,103,"TEST10"));
  applications.remove(30,103);
  assertEquals("Removed",db.scalar("SELECT usage_status FROM coupon_usages"));
  assertMoney("75000",new BigDecimal(db.scalar("SELECT total_amount FROM orders WHERE order_id=103")));
  assertMoney("75000",new BigDecimal(db.scalar("SELECT amount FROM payments WHERE order_id=103")));
  assertEquals(0,coupons.findAll(10).get(0).getUsedCount());
 }
 @Test void usageFailureRollsBackBothAmounts() throws Exception {
  db.exec("ALTER TABLE coupon_usages ADD CONSTRAINT fail_insert CHECK(discount_amount=0)");
  assertThrows(SQLException.class,()->applications.apply(30,103,"TEST10"));
  assertMoney("75000",new BigDecimal(db.scalar("SELECT total_amount FROM orders WHERE order_id=103")));
  assertMoney("75000",new BigDecimal(db.scalar("SELECT amount FROM payments WHERE order_id=103")));
 }
 @Test void otherCustomerPaidTransactionAndVoucherAreRejected() throws Exception {
  assertThrows(IllegalArgumentException.class,()->applications.apply(31,103,"TEST10"));
  db.exec("UPDATE payments SET status='paid' WHERE order_id=103");
  assertThrows(IllegalArgumentException.class,()->applications.apply(30,103,"TEST10"));
  db.exec("UPDATE payments SET status='pending',transaction_ref='gateway-ref' WHERE order_id=103");
  assertThrows(IllegalArgumentException.class,()->applications.apply(30,103,"TEST10"));
  db.exec("UPDATE payments SET transaction_ref=NULL WHERE order_id=103","UPDATE orders SET voucher_id=1 WHERE order_id=103");
  assertThrows(IllegalArgumentException.class,()->applications.apply(30,103,"TEST10"));
 }
 @Test void expiredInactiveAndDifferentShopCouponsAreRejected() throws Exception {
  db.exec("UPDATE coupons SET shop_id=2");
  assertThrows(IllegalArgumentException.class,()->applications.apply(30,103,"TEST10"));
  db.exec("UPDATE coupons SET shop_id=1,is_active=0");
  assertThrows(IllegalArgumentException.class,()->applications.apply(30,103,"TEST10"));
  db.exec("UPDATE coupons SET is_active=1,ends_at=DATEADD(DAY,-2,CURRENT_TIMESTAMP),starts_at=DATEADD(DAY,-3,CURRENT_TIMESTAMP)");
  assertThrows(IllegalArgumentException.class,()->applications.apply(30,103,"TEST10"));
 }
 @Test void ownerScopeVersionAndHistoryPolicyAreEnforced() throws Exception {
  assertNull(coupons.findById(11,couponId));
  applications.apply(30,103,"TEST10");
  Coupon changed=coupons.findById(10,couponId);changed.setDiscountValue(new BigDecimal("20"));
  assertThrows(IllegalArgumentException.class,()->coupons.save(10,changed));
  coupons.changeState(10,couponId,0,false,false);
  assertThrows(IllegalArgumentException.class,()->coupons.changeState(10,couponId,0,true,false));
  applications.remove(30,103); // Disabled coupon can still be removed from an editable order.
 }
 private void assertMoney(String expected,BigDecimal actual){assertEquals(0,new BigDecimal(expected).compareTo(actual));}
}
