package com.mycompany.onlinefruitshopping.shopowner.service;

import com.mycompany.onlinefruitshopping.shopowner.dao.AccountDAO;
import com.mycompany.onlinefruitshopping.shopowner.dao.AssignmentDAO;
import com.mycompany.onlinefruitshopping.shopowner.dao.CouponApplicationDAO;
import com.mycompany.onlinefruitshopping.shopowner.dao.CouponDAO;
import com.mycompany.onlinefruitshopping.shopowner.dao.DeliveryDAO;
import com.mycompany.onlinefruitshopping.shopowner.dao.NotificationDAO;
import com.mycompany.onlinefruitshopping.shopowner.dao.PromotionDAO;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;
import com.mycompany.onlinefruitshopping.shopowner.model.Coupon;
import com.mycompany.onlinefruitshopping.shopowner.model.CouponQuote;
import com.mycompany.onlinefruitshopping.shopowner.model.Promotion;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Kiểm tra validation và ranh giới lưu dữ liệu, không cần SQL Server. */
class ServiceValidationTest {
    private Account account(int id, String role) {
        Account account = new Account();
        account.setId(id);
        account.setRole(role);
        return account;
    }

    private Promotion promotion(PromotionService service) {
        Promotion promotion = service.createDraft();
        promotion.setName("Giảm giá cuối tuần");
        promotion.setShopId(2);
        promotion.setProductIds(List.of(5, 7));
        return promotion;
    }

    private Coupon coupon(CouponService service) {
        Coupon coupon = service.createDraft();
        coupon.setCode(" SAVE10 ");
        coupon.setName("Ưu đãi khách hàng");
        coupon.setShopId(2);
        coupon.setTotalLimit(100);
        coupon.setPerCustomerLimit(2);
        return coupon;
    }

    @Test void loginValidatesBeforeQueryAndDoesNotTrimPassword() throws SQLException {
        RecordingAccountDAO dao = new RecordingAccountDAO();
        AccountService service = new AccountService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.login(" ", "secret"));
        assertThrows(IllegalArgumentException.class, () -> service.login("shop", null));
        assertEquals(0, dao.calls);
        service.login(" shop ", " secret ");
        assertEquals("shop", dao.username);
        assertEquals(" secret ", dao.password);
    }

    @Test void customerAndShopOwnerCannotUpdateDeliveryAsShipper() {
        RecordingDeliveryDAO dao = new RecordingDeliveryDAO();
        DeliveryService service = new DeliveryService(dao, new RecordingAssignmentDAO());
        for (String role : List.of("Customer", "ShopOwner")) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.updateStatus(3, account(10, role), "accept", "", false));
        }
        assertEquals(0, dao.calls);
    }

    @Test void invalidActionAndMissingFailureReasonNeverReachWrite() {
        RecordingDeliveryDAO dao = new RecordingDeliveryDAO();
        DeliveryService service = new DeliveryService(dao, new RecordingAssignmentDAO());
        Account shipper = account(10, "DeliveryStaff");
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, null, "", false));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, "delete", "", false));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, "failed", " ", false));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, "reject", null, false));
        assertEquals(0, dao.calls);
    }

    @Test void validDeliveryActionKeepsActorAndCodConfirmation() throws SQLException {
        RecordingDeliveryDAO dao = new RecordingDeliveryDAO();
        DeliveryService service = new DeliveryService(dao, new RecordingAssignmentDAO());
        service.updateStatus(3, account(10, "DeliveryStaff"), "delivered", " Đã thu tiền ", true);
        assertEquals(1, dao.calls);
        assertEquals(10, dao.shipperId);
        assertEquals("Đã thu tiền", dao.reason);
        assertTrue(dao.collected);
    }

    @Test void assignmentRequiresShopOwnerAndPositiveIds() throws SQLException {
        RecordingAssignmentDAO dao = new RecordingAssignmentDAO();
        DeliveryService service = new DeliveryService(new RecordingDeliveryDAO(), dao);
        assertThrows(IllegalArgumentException.class, () -> service.assign(account(10, "DeliveryStaff"), 3, 20));
        assertThrows(IllegalArgumentException.class, () -> service.assign(account(10, "ShopOwner"), 0, 20));
        assertEquals(0, dao.calls);
        service.assign(account(10, "ShopOwner"), 3, 20);
        assertEquals(1, dao.calls);
        assertEquals(10, dao.ownerId);
    }

    @Test void promotionRejectsInvalidDiscountBeforeSaveAndDeduplicatesProducts() throws SQLException {
        RecordingPromotionDAO dao = new RecordingPromotionDAO();
        PromotionService service = new PromotionService(dao);
        Promotion promotion = promotion(service);
        promotion.setDiscountValue(new BigDecimal("101"));
        assertThrows(IllegalArgumentException.class, () -> service.save(10, promotion));
        assertEquals(0, dao.calls);
        promotion.setDiscountValue(new BigDecimal("10"));
        promotion.setProductIds(List.of(5, 7, 5));
        assertEquals(8, service.save(10, promotion));
        assertEquals(List.of(5, 7), dao.saved.getProductIds());
        assertEquals(10, dao.ownerId);
    }

    @Test void existingPromotionAndCouponCannotChangeShopDuringFormMapping() {
        PromotionService promotions = new PromotionService(new RecordingPromotionDAO());
        Promotion promotion = promotion(promotions);
        promotion.setId(8);
        assertThrows(IllegalArgumentException.class, () -> promotions.validateShopChange(promotion, 99));
        assertEquals(2, promotion.getShopId());
        CouponService coupons = new CouponService(new RecordingCouponDAO());
        Coupon coupon = coupon(coupons);
        coupon.setId(9);
        assertThrows(IllegalArgumentException.class, () -> coupons.validateShopChange(coupon, 99));
        assertEquals(2, coupon.getShopId());
    }

    @Test void couponValidatesQuotasAndNormalizesCodeBeforeSave() throws SQLException {
        RecordingCouponDAO dao = new RecordingCouponDAO();
        CouponService service = new CouponService(dao);
        Coupon coupon = coupon(service);
        coupon.setPerCustomerLimit(101);
        assertThrows(IllegalArgumentException.class, () -> service.save(10, coupon));
        assertEquals(0, dao.calls);
        coupon.setPerCustomerLimit(2);
        assertEquals(9, service.save(10, coupon));
        assertEquals("SAVE10", dao.saved.getCode());
    }

    @Test void couponCannotPassNegativeVersionToWrite() {
        RecordingCouponDAO dao = new RecordingCouponDAO();
        CouponService service = new CouponService(dao);
        Coupon coupon = coupon(service);
        coupon.setVersion(-1);
        assertThrows(IllegalArgumentException.class, () -> service.save(10, coupon));
        assertEquals(0, dao.calls);
    }

    @Test void applyRejectsMalformedCodeBeforeTransactionAndPreservesCustomerScope() throws SQLException {
        RecordingApplicationDAO dao = new RecordingApplicationDAO();
        CouponApplicationService service = new CouponApplicationService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.apply(10, 3, "<bad>"));
        assertThrows(IllegalArgumentException.class, () -> service.apply(0, 3, "SAVE10"));
        assertEquals(0, dao.calls);
        service.apply(10, 3, " save10 ");
        assertEquals("SAVE10", dao.code);
        assertEquals(10, dao.customerId);
        assertEquals(3, dao.orderId);
    }

    @Test void sqlFailurePropagatesWithoutRetryingCouponApplication() {
        RecordingApplicationDAO dao = new RecordingApplicationDAO();
        dao.fail = true;
        CouponApplicationService service = new CouponApplicationService(dao);
        assertThrows(SQLException.class, () -> service.apply(10, 3, "SAVE10"));
        assertEquals(1, dao.calls);
    }

    @Test void markingNotificationKeepsAccountScopeAndRejectsBadId() throws SQLException {
        RecordingNotificationDAO dao = new RecordingNotificationDAO();
        NotificationService service = new NotificationService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.markRead(0, 10));
        assertEquals(0, dao.calls);
        service.markRead(3, 10);
        assertEquals(3, dao.notificationId);
        assertEquals(10, dao.accountId);
    }

    private static class RecordingAccountDAO extends AccountDAO {
        int calls;
        String username, password;
        @Override public Account login(String username, String password) {
            calls++;
            this.username = username;
            this.password = password;
            return null;
        }
    }

    private static class RecordingDeliveryDAO extends DeliveryDAO {
        int calls, shipperId;
        String reason;
        boolean collected;
        @Override public void updateStatus(int id, int shipperId, String action, String reason, boolean collected) {
            calls++;
            this.shipperId = shipperId;
            this.reason = reason;
            this.collected = collected;
        }
    }

    private static class RecordingAssignmentDAO extends AssignmentDAO {
        int calls, ownerId;
        @Override public void assign(int ownerId, int orderId, int shipperId) {
            calls++;
            this.ownerId = ownerId;
        }
    }

    private static class RecordingPromotionDAO extends PromotionDAO {
        int calls, ownerId;
        Promotion saved;
        @Override public int save(int ownerId, Promotion promotion) {
            calls++;
            this.ownerId = ownerId;
            this.saved = promotion;
            return 8;
        }
    }

    private static class RecordingCouponDAO extends CouponDAO {
        int calls;
        Coupon saved;
        @Override public int save(int ownerId, Coupon coupon) {
            calls++;
            this.saved = coupon;
            return 9;
        }
    }

    private static class RecordingApplicationDAO extends CouponApplicationDAO {
        int calls, customerId, orderId;
        String code;
        boolean fail;
        @Override public CouponQuote apply(int customerId, int orderId, String code) throws SQLException {
            calls++;
            if (fail) throw new SQLException("Database error during transaction");
            this.customerId = customerId;
            this.orderId = orderId;
            this.code = code;
            return new CouponQuote();
        }
    }

    private static class RecordingNotificationDAO extends NotificationDAO {
        int calls, notificationId, accountId;
        @Override public void markRead(int notificationId, int accountId) {
            calls++;
            this.notificationId = notificationId;
            this.accountId = accountId;
        }
    }
}
