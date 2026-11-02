package com.mycompany.onlinefruitshopping.dao.coupon;

import com.mycompany.onlinefruitshopping.model.Coupon;
import com.mycompany.onlinefruitshopping.model.CouponQuote;
import com.mycompany.onlinefruitshopping.utils.ConnectionProvider;
import com.mycompany.onlinefruitshopping.utils.DBConnection;
import com.mycompany.onlinefruitshopping.utils.CouponRules;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Preview coupons before checkout. No coupon usage is saved until place order commits. */
public class CouponCheckoutDAO {
    private final ConnectionProvider connections;
    public CouponCheckoutDAO() { this(DBConnection::getConnection); }
    public CouponCheckoutDAO(ConnectionProvider connections) { this.connections=java.util.Objects.requireNonNull(connections); }

    public CouponQuote preview(int customerId, int shopId, String code, BigDecimal subtotal) throws SQLException {
        CouponRules.money(subtotal,false);
        try(Connection c=connections.open()) {
            c.setAutoCommit(false);
            try {
                CouponDAO dao=new CouponDAO();
                Coupon coupon=dao.findForUse(c,shopId,CouponRules.normalizeCode(code));
                CouponRules.validateUse(coupon,subtotal,dao.countForCustomer(c,coupon.getId(),customerId),PromotionRules.now());
                BigDecimal discount=CouponRules.discount(coupon,subtotal);
                if(discount.signum()<=0) throw new IllegalArgumentException("Mã không tạo được giá trị giảm hợp lệ.");
                CouponQuote quote=new CouponQuote();
                quote.setCode(coupon.getCode()); quote.setSubtotal(subtotal);
                quote.setDiscountAmount(discount); quote.setFinalAmount(subtotal.subtract(discount));
                c.commit();
                return quote;
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public List<Coupon> availableForShop(int shopId, int customerId, BigDecimal subtotal) throws SQLException {
        // Fetch customer-eligible codes for this shop, never expose other shops' coupons.
        List<Coupon> result=new ArrayList<>();
        String sql="""
            SELECT p.*, s.shop_name,
             (SELECT COUNT(*) FROM coupon_usages u JOIN orders o ON o.order_id=u.order_id
              WHERE u.coupon_id=p.coupon_id AND u.usage_status='Applied' AND o.status<>N'cancelled') AS used_count,
             (SELECT COUNT(*) FROM coupon_usages u WHERE u.coupon_id=p.coupon_id) AS history_count,
             (SELECT COUNT(*) FROM coupon_usages u JOIN orders o ON o.order_id=u.order_id
              WHERE u.coupon_id=p.coupon_id AND u.customer_id=? AND u.usage_status='Applied'
                AND o.status<>N'cancelled') AS customer_uses
            FROM coupons p JOIN shops s ON s.shop_id=p.shop_id
            WHERE p.shop_id=? AND s.status=N'active' AND p.is_active=1 AND p.is_deleted=0
            ORDER BY p.ends_at, p.coupon_id
            """;
        try(Connection c=connections.open(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,customerId);ps.setInt(2,shopId);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    Coupon p=new Coupon();
                    p.setId(rs.getInt("coupon_id")); p.setShopId(rs.getInt("shop_id"));
                    p.setCode(rs.getString("code")); p.setName(rs.getString("coupon_name"));
                    p.setDiscountType(rs.getString("discount_type")); p.setDiscountValue(rs.getBigDecimal("discount_value"));
                    p.setMaxDiscount(rs.getBigDecimal("max_discount")); p.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
                    p.setTotalLimit(rs.getInt("total_limit")); p.setPerCustomerLimit(rs.getInt("per_customer_limit"));
                    p.setStartsAt(rs.getTimestamp("starts_at").toLocalDateTime());
                    p.setEndsAt(rs.getTimestamp("ends_at").toLocalDateTime());
                    p.setActive(rs.getBoolean("is_active")); p.setDeleted(rs.getBoolean("is_deleted"));
                    p.setUsedCount(rs.getInt("used_count"));
                    // Only show a code which can currently be applied by this customer.
                    try { CouponRules.validateUse(p,subtotal,rs.getInt("customer_uses"),PromotionRules.now()); result.add(p); }
                    catch(IllegalArgumentException ignored) { }
                }
            }
        }
        return result;
    }
}
