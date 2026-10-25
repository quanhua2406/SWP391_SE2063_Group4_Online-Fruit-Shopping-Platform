package com.mycompany.onlinefruitshopping.service.coupon;

import com.mycompany.onlinefruitshopping.dao.coupon.CouponDAO;
import com.mycompany.onlinefruitshopping.model.Coupon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CouponServiceTest {
    private Coupon coupon(CouponService service) {
        Coupon c = service.createDraft();
        c.setCode(" SAVE10 "); c.setName("Ưu đãi khách hàng"); c.setShopId(2);
        c.setTotalLimit(100); c.setPerCustomerLimit(2);
        return c;
    }
    @Test void existingCouponCannotChangeShopDuringFormMapping() {
        CouponService service = new CouponService(new RecordingDAO());
        Coupon c = coupon(service); c.setId(9);
        assertThrows(IllegalArgumentException.class, () -> service.validateShopChange(c, 99));
        assertEquals(2, c.getShopId());
    }
    @Test void couponValidatesQuotasAndNormalizesCodeBeforeSave() throws Exception {
        RecordingDAO dao = new RecordingDAO();
        CouponService service = new CouponService(dao);
        Coupon c = coupon(service); c.setPerCustomerLimit(101);
        assertThrows(IllegalArgumentException.class, () -> service.save(10, c));
        assertEquals(0, dao.calls);
        c.setPerCustomerLimit(2);
        assertEquals(9, service.save(10, c));
        assertEquals("SAVE10", dao.saved.getCode());
    }
    @Test void couponCannotPassNegativeVersionToWrite() {
        RecordingDAO dao = new RecordingDAO();
        CouponService service = new CouponService(dao);
        Coupon c = coupon(service); c.setVersion(-1);
        assertThrows(IllegalArgumentException.class, () -> service.save(10, c));
        assertEquals(0, dao.calls);
    }
    private static class RecordingDAO extends CouponDAO {
        int calls; Coupon saved;
        @Override public int save(int ownerId, Coupon coupon) {
            calls++; saved = coupon; return 9;
        }
    }
}
