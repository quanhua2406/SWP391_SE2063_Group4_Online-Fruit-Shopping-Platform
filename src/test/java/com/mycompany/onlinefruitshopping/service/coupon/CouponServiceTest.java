package com.mycompany.onlinefruitshopping.service.coupon;

import com.mycompany.onlinefruitshopping.dao.coupon.CouponDAO;
import com.mycompany.onlinefruitshopping.model.Coupon;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CouponServiceTest {
    private Coupon coupon(CouponService service) {
        Coupon coupon = service.createDraft();
        coupon.setCode(" SAVE10 ");
        coupon.setName("Ưu đãi khách hàng");
        coupon.setShopId(2);
        coupon.setTotalLimit(100);
        coupon.setPerCustomerLimit(2);
        return coupon;
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

    private static class RecordingCouponDAO extends CouponDAO {
        int calls;
        Coupon saved;
        @Override public int save(int ownerId, Coupon coupon) {
            calls++;
            this.saved = coupon;
            return 9;
        }
    }

    @Test void existingCouponCannotChangeShopDuringFormMapping() {
        CouponService service = new CouponService(new RecordingCouponDAO());
        Coupon coupon = coupon(service);
        coupon.setId(9);
        assertThrows(IllegalArgumentException.class, () -> service.validateShopChange(coupon, 99));
        assertEquals(2, coupon.getShopId());
    }
}
