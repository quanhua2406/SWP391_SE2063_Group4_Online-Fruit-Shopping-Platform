package com.mycompany.onlinefruitshopping.service.coupon;

import com.mycompany.onlinefruitshopping.service.common.ServiceChecks;

import com.mycompany.onlinefruitshopping.dao.coupon.CouponApplicationDAO;
import com.mycompany.onlinefruitshopping.model.CouponOrder;
import com.mycompany.onlinefruitshopping.model.CouponQuote;
import com.mycompany.onlinefruitshopping.utils.CouponRules;
import java.sql.SQLException;
import java.util.List;

/** Khách kiểm tra, áp dụng hoặc gỡ mã trên đơn của mình. */
public class CouponApplicationService {
    private final CouponApplicationDAO applicationDAO;

    public CouponApplicationService() { this(new CouponApplicationDAO()); }

    CouponApplicationService(CouponApplicationDAO applicationDAO) { this.applicationDAO = applicationDAO; }

    public List<CouponOrder> findOrders(int customerId) throws SQLException {
        ServiceChecks.positiveId(customerId);
        return applicationDAO.findOrders(customerId);
    }

    public CouponQuote preview(int customerId, int orderId, String code) throws SQLException {
        ServiceChecks.positiveId(customerId);
        ServiceChecks.positiveId(orderId);
        return applicationDAO.preview(customerId, orderId, CouponRules.normalizeCode(code));
    }

    public CouponQuote apply(int customerId, int orderId, String code) throws SQLException {
        ServiceChecks.positiveId(customerId);
        ServiceChecks.positiveId(orderId);
        // DAO giữ nguyên transaction: khóa đơn/mã, kiểm tra quota, cập nhật tiền và lịch sử.
        return applicationDAO.apply(customerId, orderId, CouponRules.normalizeCode(code));
    }

    public void remove(int customerId, int orderId) throws SQLException {
        ServiceChecks.positiveId(customerId);
        ServiceChecks.positiveId(orderId);
        applicationDAO.remove(customerId, orderId);
    }
}
