package com.mycompany.onlinefruitshopping.service.coupon;

import com.mycompany.onlinefruitshopping.service.common.ServiceChecks;

import com.mycompany.onlinefruitshopping.dao.coupon.CouponDAO;
import com.mycompany.onlinefruitshopping.model.Coupon;
import com.mycompany.onlinefruitshopping.model.CouponUsage;
import com.mycompany.onlinefruitshopping.utils.CouponRules;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Nghiệp vụ quản lý mã giảm giá của Shop Owner. */
public class CouponService {
    private final CouponDAO couponDAO;

    public CouponService() { this(new CouponDAO()); }

    CouponService(CouponDAO couponDAO) { this.couponDAO = couponDAO; }

    public Coupon createDraft() {
        Coupon coupon = new Coupon();
        coupon.setDiscountType("FIXED");
        coupon.setDiscountValue(new BigDecimal("20000"));
        coupon.setMinOrderAmount(new BigDecimal("100000"));
        coupon.setStartsAt(PromotionRules.now().withSecond(0));
        coupon.setEndsAt(coupon.getStartsAt().plusDays(7));
        return coupon;
    }

    public List<Coupon> findAll(int ownerId) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        return couponDAO.findAll(ownerId);
    }

    public Coupon findById(int ownerId, int id) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        ServiceChecks.positiveId(id);
        return couponDAO.findById(ownerId, id);
    }

    public List<CouponUsage> findUsage(int ownerId, Integer couponId) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        if (couponId != null) ServiceChecks.positiveId(couponId);
        return couponDAO.findUsage(ownerId, couponId);
    }

    public void validateShopChange(Coupon coupon, int shopId) {
        ServiceChecks.positiveId(shopId);
        if (coupon.getId() > 0 && coupon.getShopId() != shopId) {
            throw new IllegalArgumentException("Không được chuyển mã sang shop khác.");
        }
    }

    public int save(int ownerId, Coupon coupon) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        if (coupon == null || coupon.getId() < 0) {
            throw new IllegalArgumentException("Thông tin mã giảm giá không hợp lệ.");
        }
        ServiceChecks.version(coupon.getVersion());
        coupon.setCode(CouponRules.normalizeCode(coupon.getCode()));
        CouponRules.validate(coupon);
        return couponDAO.save(ownerId, coupon);
    }

    public void changeState(int ownerId, int id, int version, boolean active, boolean deleted)
            throws SQLException {
        ServiceChecks.positiveId(ownerId);
        ServiceChecks.positiveId(id);
        ServiceChecks.version(version);
        couponDAO.changeState(ownerId, id, version, active, deleted);
    }
}
