package com.mycompany.onlinefruitshopping.shopowner.dao;

import com.mycompany.onlinefruitshopping.shopowner.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.shopowner.model.Coupon;
import com.mycompany.onlinefruitshopping.shopowner.model.CouponOrder;
import com.mycompany.onlinefruitshopping.shopowner.model.CouponQuote;
import com.mycompany.onlinefruitshopping.shopowner.utils.CouponRules;
import com.mycompany.onlinefruitshopping.shopowner.utils.PromotionRules;

/** Khách áp dụng mã vào đơn Pending/Unpaid của chính mình. */
public class CouponApplicationDAO {
    private static final String ORDER_FIELDS = """
        SELECT o.*, s.shop_name, cp.code AS coupon_code FROM Orders o
        """;
    private static final String ORDER_JOINS = """
        JOIN Shops s WITH (HOLDLOCK) ON s.shop_id = o.shop_id
        JOIN Accounts a WITH (HOLDLOCK) ON a.account_id = o.account_id
        JOIN Roles r ON r.role_id = a.role_id
        JOIN Accounts owner WITH (HOLDLOCK) ON owner.account_id = s.owner_account_id
        LEFT JOIN Coupons cp ON cp.coupon_id = o.coupon_id
        WHERE o.account_id = ? AND a.status = N'Active' AND r.role_name = N'Customer'
          AND s.status = N'Active' AND owner.status = N'Active'
        """;

    public List<CouponOrder> findOrders(int customerId) throws SQLException {
        List<CouponOrder> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                ORDER_FIELDS + ORDER_JOINS + " ORDER BY o.order_id DESC")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public CouponQuote preview(int customerId, int orderId, String code) throws SQLException {
        return applyOrPreview(customerId, orderId, code, false);
    }

    public CouponQuote apply(int customerId, int orderId, String code) throws SQLException {
        return applyOrPreview(customerId, orderId, code, true);
    }

    private CouponQuote applyOrPreview(int customerId, int orderId, String code, boolean save) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                CouponOrder order = lockOrder(c, customerId, orderId);
                requireEditable(order);
                if (order.getVoucherId() != null) throw new IllegalArgumentException("Đơn đang có voucher cũ; không cộng dồn với coupon mới.");
                if (order.getCouponId() != null) throw new IllegalArgumentException("Đơn đã có mã. Hãy gỡ mã hiện tại trước khi áp dụng mã khác.");
                BigDecimal subtotal = readSubtotal(c, orderId);
                // Không tự sửa một đơn có dữ liệu tổng tiền không nhất quán.
                if (subtotal.compareTo(order.getTotalAmount()) != 0)
                    throw new IllegalArgumentException("Tổng tiền đơn chưa khớp chi tiết. Vui lòng liên hệ cửa hàng.");

                CouponDAO coupons = new CouponDAO();
                Coupon coupon = coupons.findForUse(c, order.getShopId(), code);
                int customerUses = coupons.countForCustomer(c, coupon.getId(), customerId);
                CouponRules.validateUse(coupon, subtotal, customerUses, PromotionRules.now());
                BigDecimal discount = CouponRules.discount(coupon, subtotal);
                if (discount.signum() == 0) throw new IllegalArgumentException("Mức giảm sau làm tròn bằng 0 đ, mã không được áp dụng.");
                CouponQuote quote = new CouponQuote();
                quote.setCode(coupon.getCode()); quote.setSubtotal(subtotal);
                quote.setDiscountAmount(discount); quote.setFinalAmount(subtotal.subtract(discount));

                if (save) {
                    prepareWrite(c);
                    String update = """
                        UPDATE Orders SET coupon_id = ?, coupon_subtotal = ?, coupon_discount = ?, total_amount = ?
                        WHERE order_id = ? AND account_id = ? AND order_status = N'Pending'
                          AND payment_status = N'Unpaid' AND coupon_id IS NULL AND voucher_id IS NULL
                        """;
                    try (PreparedStatement ps = c.prepareStatement(update)) {
                        ps.setInt(1, coupon.getId()); ps.setBigDecimal(2, subtotal); ps.setBigDecimal(3, discount);
                        ps.setBigDecimal(4, quote.getFinalAmount()); ps.setInt(5, orderId); ps.setInt(6, customerId);
                        if (ps.executeUpdate() != 1) throw new SQLException("Đơn đã thay đổi.");
                    }
                    String insert = """
                        INSERT INTO CouponUsages(coupon_id, shop_id, order_id, customer_id, code_snapshot,
                            subtotal, discount_amount, final_amount, used_at, usage_status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Applied')
                        """;
                    try (PreparedStatement ps = c.prepareStatement(insert)) {
                        ps.setInt(1, coupon.getId()); ps.setInt(2, order.getShopId()); ps.setInt(3, orderId);
                        ps.setInt(4, customerId); ps.setString(5, coupon.getCode()); ps.setBigDecimal(6, subtotal);
                        ps.setBigDecimal(7, discount); ps.setBigDecimal(8, quote.getFinalAmount());
                        ps.setTimestamp(9, Timestamp.valueOf(PromotionRules.now())); ps.executeUpdate();
                    }
                }
                // Preview không INSERT, không UPDATE và không giữ chỗ lượt sử dụng.
                c.commit();
                return quote;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    public void remove(int customerId, int orderId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                CouponOrder order = lockOrder(c, customerId, orderId);
                requireEditable(order);
                if (order.getCouponId() == null) throw new IllegalArgumentException("Đơn không có coupon để gỡ.");
                CouponDAO.lockCoupon(c, order.getCouponId(), order.getShopId());
                // Có thể gỡ cả mã đã bị owner tắt/xóa; không kiểm tra hạn dùng lúc gỡ.
                if (order.getCouponSubtotal() == null) throw new SQLException("Thiếu giá trước giảm của đơn.");
                prepareWrite(c);
                try (PreparedStatement ps = c.prepareStatement("""
                        UPDATE CouponUsages SET usage_status = 'Removed', removed_at = ?
                        WHERE order_id = ? AND coupon_id = ? AND customer_id = ? AND usage_status = 'Applied'
                        """)) {
                    ps.setTimestamp(1, Timestamp.valueOf(PromotionRules.now())); ps.setInt(2, orderId);
                    ps.setInt(3, order.getCouponId()); ps.setInt(4, customerId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Lịch sử coupon chưa khớp đơn.");
                }
                try (PreparedStatement ps = c.prepareStatement("""
                        UPDATE Orders SET total_amount = ?, coupon_id = NULL, coupon_discount = 0, coupon_subtotal = NULL
                        WHERE order_id = ? AND account_id = ? AND coupon_id = ?
                        """)) {
                    ps.setBigDecimal(1, order.getCouponSubtotal()); ps.setInt(2, orderId);
                    ps.setInt(3, customerId); ps.setInt(4, order.getCouponId());
                    if (ps.executeUpdate() != 1) throw new SQLException("Đơn đã thay đổi.");
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    private CouponOrder lockOrder(Connection c, int customerId, int orderId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                ORDER_FIELDS + " WITH (UPDLOCK, HOLDLOCK) " + ORDER_JOINS + " AND o.order_id = ?")) {
            ps.setInt(1, customerId); ps.setInt(2, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy đơn thuộc tài khoản của bạn tại cửa hàng đang hoạt động.");
                return map(rs);
            }
        }
    }

    private void requireEditable(CouponOrder order) {
        if (!order.isEditable()) throw new IllegalArgumentException("Chỉ dùng hoặc gỡ mã khi đơn đang chờ xác nhận và chưa thanh toán.");
    }

    private BigDecimal readSubtotal(Connection c, int orderId) throws SQLException {
        BigDecimal subtotal = BigDecimal.ZERO;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT price, quantity FROM OrderDetails WITH (HOLDLOCK) WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal price = rs.getBigDecimal("price");
                    int quantity = rs.getInt("quantity");
                    CouponRules.money(price, true);
                    if (quantity <= 0) throw new IllegalArgumentException("Số lượng sản phẩm trong đơn không hợp lệ.");
                    subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(quantity)));
                }
            }
        }
        CouponRules.money(subtotal, false);
        return subtotal;
    }

    private void prepareWrite(Connection c) throws SQLException {
        // Các SET này đáp ứng điều kiện ghi vào bảng có filtered index của SQL Server.
        try (Statement s = c.createStatement()) {
            s.execute("SET ANSI_NULLS ON; SET QUOTED_IDENTIFIER ON; SET ANSI_PADDING ON; "
                    + "SET ANSI_WARNINGS ON; SET CONCAT_NULL_YIELDS_NULL ON; SET ARITHABORT ON; SET NUMERIC_ROUNDABORT OFF;");
        }
    }

    private CouponOrder map(ResultSet rs) throws SQLException {
        CouponOrder o = new CouponOrder();
        o.setId(rs.getInt("order_id")); o.setShopId(rs.getInt("shop_id")); o.setShopName(rs.getString("shop_name"));
        o.setOrderStatus(rs.getString("order_status")); o.setPaymentStatus(rs.getString("payment_status"));
        o.setVoucherId((Integer) rs.getObject("voucher_id")); o.setCouponId((Integer) rs.getObject("coupon_id"));
        o.setCouponCode(rs.getString("coupon_code")); o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setCouponDiscount(rs.getBigDecimal("coupon_discount")); o.setCouponSubtotal(rs.getBigDecimal("coupon_subtotal"));
        return o;
    }
}
