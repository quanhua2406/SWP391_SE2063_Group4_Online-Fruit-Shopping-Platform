package com.mycompany.onlinefruitshopping.dao.coupon;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.model.Coupon;
import com.mycompany.onlinefruitshopping.model.CouponOrder;
import com.mycompany.onlinefruitshopping.model.CouponQuote;
import com.mycompany.onlinefruitshopping.utils.CouponRules;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;

/** Coupon chỉ áp dụng cho đơn pending_confirmation và payment pending của khách. */
public class CouponApplicationDAO {
    private final com.mycompany.onlinefruitshopping.utils.ConnectionProvider connections;
    public CouponApplicationDAO() { this(DBConnection::getConnection); }
    public CouponApplicationDAO(com.mycompany.onlinefruitshopping.utils.ConnectionProvider connections) {
        this.connections = java.util.Objects.requireNonNull(connections);
    }

    private static final String ORDER_FIELDS = """
        SELECT o.order_id,o.shop_id,o.voucher_id,o.total_amount,s.shop_name,
            o.status AS order_status,pay.status AS payment_status,
            usage.coupon_id,usage.code_snapshot AS coupon_code,
            COALESCE(usage.discount_amount,0) AS coupon_discount,usage.subtotal AS coupon_subtotal
        FROM orders o
        """;
    private static final String ORDER_JOINS = """
        JOIN shops s WITH(HOLDLOCK) ON s.shop_id=o.shop_id
        JOIN users a WITH(HOLDLOCK) ON a.user_id=o.user_id
        JOIN user_roles ur ON ur.user_id=a.user_id
        JOIN roles r ON r.role_id=ur.role_id AND r.role_name=N'Customer'
        JOIN users owner WITH(HOLDLOCK) ON owner.user_id=s.owner_id
        LEFT JOIN payments pay WITH(UPDLOCK,HOLDLOCK) ON pay.order_id=o.order_id
        LEFT JOIN coupon_usages usage WITH(UPDLOCK,HOLDLOCK)
            ON usage.order_id=o.order_id AND usage.usage_status=N'Applied'
        WHERE o.user_id=? AND a.status=N'active' AND s.status=N'active' AND owner.status=N'active'
        """;

    public List<CouponOrder> findOrders(int customerId) throws SQLException {
        List<CouponOrder> list = new ArrayList<>();
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(
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
        try (Connection c = connections.open()) {
            prepareWrite(c);
            c.setAutoCommit(false);
            try {
                CouponOrder order = lockOrder(c, customerId, orderId);
                requireEditable(order);
                requirePendingPayment(c, orderId);
                if (order.getVoucherId() != null) throw new IllegalArgumentException("Đơn đang có voucher cũ; không cộng dồn với coupon mới.");
                if (order.getCouponId() != null) throw new IllegalArgumentException("Đơn đã có mã. Hãy gỡ mã hiện tại trước khi áp dụng mã khác.");
                BigDecimal subtotal = readSubtotal(c, orderId);
                // Coupon giảm trên tiền hàng; giữ lại phí giao hàng/phụ phí đã có trong tổng đơn.
                if (subtotal.compareTo(order.getTotalAmount()) > 0)
                    throw new IllegalArgumentException("Tổng tiền đơn chưa khớp chi tiết. Vui lòng liên hệ cửa hàng.");

                CouponDAO coupons = new CouponDAO();
                Coupon coupon = coupons.findForUse(c, order.getShopId(), code);
                int customerUses = coupons.countForCustomer(c, coupon.getId(), customerId);
                CouponRules.validateUse(coupon, subtotal, customerUses, PromotionRules.now());
                BigDecimal discount = CouponRules.discount(coupon, subtotal);
                if (discount.signum() == 0) throw new IllegalArgumentException("Mức giảm sau làm tròn bằng 0 đ, mã không được áp dụng.");
                CouponQuote quote = new CouponQuote();
                quote.setCode(coupon.getCode()); quote.setSubtotal(order.getTotalAmount());
                quote.setDiscountAmount(discount); quote.setFinalAmount(order.getTotalAmount().subtract(discount));

                if (save) {
                    updateTotal(c,customerId,orderId,order.getTotalAmount(),quote.getFinalAmount());
                    String insert = """
                        INSERT INTO coupon_usages(coupon_id, shop_id, order_id, customer_id, code_snapshot,
                            subtotal, discount_amount, final_amount, used_at, usage_status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Applied')
                        """;
                    try (PreparedStatement ps = c.prepareStatement(insert)) {
                        ps.setInt(1, coupon.getId()); ps.setInt(2, order.getShopId()); ps.setInt(3, orderId);
                        ps.setInt(4, customerId); ps.setString(5, coupon.getCode()); ps.setBigDecimal(6, order.getTotalAmount());
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
        try (Connection c = connections.open()) {
            prepareWrite(c);
            c.setAutoCommit(false);
            try {
                CouponOrder order = lockOrder(c, customerId, orderId);
                requireEditable(order);
                requirePendingPayment(c, orderId);
                if (order.getCouponId() == null) throw new IllegalArgumentException("Đơn không có coupon để gỡ.");
                CouponDAO.lockCoupon(c, order.getCouponId(), order.getShopId());
                // Có thể gỡ cả mã đã bị owner tắt/xóa; không kiểm tra hạn dùng lúc gỡ.
                if (order.getCouponSubtotal() == null) throw new SQLException("Thiếu giá trước giảm của đơn.");
                try (PreparedStatement ps = c.prepareStatement("""
                        UPDATE coupon_usages SET usage_status = 'Removed', removed_at = ?
                        WHERE order_id = ? AND coupon_id = ? AND customer_id = ? AND usage_status = 'Applied'
                        """)) {
                    ps.setTimestamp(1, Timestamp.valueOf(PromotionRules.now())); ps.setInt(2, orderId);
                    ps.setInt(3, order.getCouponId()); ps.setInt(4, customerId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Lịch sử coupon chưa khớp đơn.");
                }
                if(order.getCouponSubtotal().subtract(order.getCouponDiscount()).compareTo(order.getTotalAmount())!=0)
                    throw new IllegalArgumentException("Tổng đơn đã đổi sau khi dùng mã; cần kiểm tra trước khi gỡ.");
                updateTotal(c,customerId,orderId,order.getTotalAmount(),order.getCouponSubtotal());
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
                "SELECT unit_price AS price, quantity FROM order_items WITH (HOLDLOCK) WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal price = rs.getBigDecimal("price");
                    BigDecimal quantity = rs.getBigDecimal("quantity");
                    CouponRules.money(price, true);
                    if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Số lượng sản phẩm trong đơn không hợp lệ.");
                    subtotal = subtotal.add(price.multiply(quantity));
                }
            }
        }
        CouponRules.money(subtotal, false);
        return subtotal;
    }

    private void requirePendingPayment(Connection c,int orderId) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("""
            SELECT p.status,p.amount,p.transaction_ref,o.total_amount
            FROM payments p WITH(UPDLOCK,HOLDLOCK) JOIN orders o ON o.order_id=p.order_id
            WHERE p.order_id=?
            """)) {
            ps.setInt(1,orderId);
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next() || !"pending".equals(rs.getString(1)))
                    throw new IllegalArgumentException("Đơn phải có thanh toán pending trước khi dùng/gỡ mã.");
                if(rs.getBigDecimal(2)==null || rs.getBigDecimal(4)==null || rs.getBigDecimal(2).compareTo(rs.getBigDecimal(4))!=0)
                    throw new IllegalArgumentException("Số tiền thanh toán chưa khớp tổng đơn.");
                if(rs.getString(3)!=null && !rs.getString(3).isBlank())
                    throw new IllegalArgumentException("Đơn đã mở giao dịch thanh toán; cần xử lý giao dịch đó trước khi đổi mã.");
            }
        }
    }

    private void updateTotal(Connection c,int customerId,int orderId,BigDecimal oldAmount,BigDecimal newAmount) throws SQLException {
        // orders.total_amount / payments.amount are decimal(12,2), narrower than coupon values.
        if(newAmount.signum()<0 || newAmount.compareTo(new BigDecimal("9999999999.99"))>0 || newAmount.scale()>2)
            throw new IllegalArgumentException("Tổng tiền vượt giới hạn của database nhóm.");
        try(PreparedStatement ps=c.prepareStatement("""
            UPDATE orders SET total_amount=? WHERE order_id=? AND user_id=?
              AND status=N'pending_confirmation' AND voucher_id IS NULL AND total_amount=?
            """)) {
            ps.setBigDecimal(1,newAmount);ps.setInt(2,orderId);ps.setInt(3,customerId);ps.setBigDecimal(4,oldAmount);
            if(ps.executeUpdate()!=1) throw new SQLException("Đơn đã thay đổi.");
        }
        try(PreparedStatement ps=c.prepareStatement("""
            UPDATE payments SET amount=? WHERE order_id=? AND status=N'pending' AND amount=?
              AND (transaction_ref IS NULL OR LTRIM(RTRIM(transaction_ref))=N'')
            """)) {
            ps.setBigDecimal(1,newAmount);ps.setInt(2,orderId);ps.setBigDecimal(3,oldAmount);
            if(ps.executeUpdate()!=1) throw new SQLException("Thanh toán đã thay đổi. Thao tác coupon được hoàn tác.");
        }
    }

    private void prepareWrite(Connection c) throws SQLException {
        com.mycompany.onlinefruitshopping.utils.ModuleSql.prepare(c);
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
