package com.mycompany.onlinefruitshopping.dao.coupon;

import com.mycompany.onlinefruitshopping.service.coupon.CouponService;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.mycompany.onlinefruitshopping.model.Coupon;
import com.mycompany.onlinefruitshopping.model.CouponUsage;
import com.mycompany.onlinefruitshopping.utils.CouponRules;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;

/** Quản lý coupon của chủ shop. Không xóa lịch sử đã dùng. */
public class CouponDAO {
    private final com.mycompany.onlinefruitshopping.utils.ConnectionProvider connections;
    public CouponDAO() { this(DBConnection::getConnection); }
    public CouponDAO(com.mycompany.onlinefruitshopping.utils.ConnectionProvider connections) {
        this.connections = java.util.Objects.requireNonNull(connections);
    }

    private static final String FIELDS = """
        SELECT p.*, s.shop_name,
          (SELECT COUNT(*) FROM coupon_usages u JOIN orders o ON o.order_id = u.order_id
           WHERE u.coupon_id = p.coupon_id AND u.usage_status = 'Applied'
             AND o.status <> N'cancelled') AS used_count,
          (SELECT COUNT(*) FROM coupon_usages u WHERE u.coupon_id = p.coupon_id) AS history_count
        FROM coupons p
        """;
    private static final String OWNER = """
        JOIN shops s ON s.shop_id = p.shop_id
        JOIN users a ON a.user_id = s.owner_id
        JOIN user_roles ur ON ur.user_id = a.user_id
        JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
        WHERE s.owner_id = ? AND s.status = N'active'
          AND a.status = N'active'
        """;

    public List<Coupon> findAll(int ownerId) throws SQLException {
        List<Coupon> result = new ArrayList<>();
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(
                FIELDS + OWNER + " AND p.is_deleted = 0 ORDER BY p.coupon_id DESC")) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(map(rs)); }
        }
        return result;
    }

    public Coupon findById(int ownerId, int id) throws SQLException {
        try (Connection c = connections.open()) { return findById(c, ownerId, id); }
    }

    private Coupon findById(Connection c, int ownerId, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(FIELDS + OWNER + " AND p.coupon_id = ? AND p.is_deleted = 0")) {
            ps.setInt(1, ownerId); ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public int save(int ownerId, Coupon p) throws SQLException {
        // CouponService đã chuẩn hóa mã và kiểm tra đầu vào.
        // Quota/lịch sử/quyền sở hữu vẫn được kiểm tra trong transaction.
        try (Connection c = connections.open()) {
            c.setAutoCommit(false);
            try {
                lockShop(c, ownerId, p.getShopId());
                Coupon old = null;
                if (p.getId() > 0) {
                    lockCoupon(c, p.getId(), p.getShopId());
                    old = requireOwned(c, ownerId, p.getId(), p.getVersion());
                    if (old.getShopId() != p.getShopId()) throw new IllegalArgumentException("Không được chuyển mã sang shop khác.");
                    if (old.getHistoryCount() > 0 && !samePolicy(old, p))
                        throw new IllegalArgumentException("Mã đã có lịch sử dùng: chỉ sửa tên, mô tả hoặc bật/tắt. Hãy tạo mã mới để đổi điều kiện.");
                }
                if (p.isActive() && (old == null || !old.isActive() || old.getHistoryCount() == 0)) checkNotExpired(p);
                String duplicate = "SELECT coupon_id FROM coupons WHERE shop_id = ? AND code = ? AND coupon_id <> ?";
                try (PreparedStatement ps = c.prepareStatement(duplicate)) {
                    ps.setInt(1, p.getShopId()); ps.setString(2, p.getCode()); ps.setInt(3, p.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new IllegalArgumentException("Mã đã tồn tại ở shop này, kể cả mã đã xóa. Hãy chọn mã khác.");
                    }
                }
                int id = p.getId();
                Timestamp now = Timestamp.valueOf(PromotionRules.now());
                if (id == 0) {
                    String sql = """
                        INSERT INTO coupons(code, coupon_name, description, discount_type, discount_value,
                          max_discount, min_order_amount, total_limit, per_customer_limit, starts_at, ends_at,
                          is_active, shop_id, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                    try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        fields(ps, p); ps.setInt(13, p.getShopId()); ps.setTimestamp(14, now); ps.setTimestamp(15, now);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("Không lấy được ID coupon.");
                            id = keys.getInt(1);
                        }
                    }
                } else {
                    String sql = """
                        UPDATE coupons SET code = ?, coupon_name = ?, description = ?, discount_type = ?,
                          discount_value = ?, max_discount = ?, min_order_amount = ?, total_limit = ?,
                          per_customer_limit = ?, starts_at = ?, ends_at = ?, is_active = ?,
                          updated_at = ?, version = version + 1
                        WHERE coupon_id = ? AND shop_id = ? AND is_deleted = 0 AND version = ?
                        """;
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        fields(ps, p); ps.setTimestamp(13, now); ps.setInt(14, id);
                        ps.setInt(15, p.getShopId()); ps.setInt(16, p.getVersion());
                        if (ps.executeUpdate() != 1) throw new SQLException("Coupon đã thay đổi.");
                    }
                }
                c.commit();
                return id;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    public void changeState(int ownerId, int id, int version, boolean active, boolean deleted) throws SQLException {
        try (Connection c = connections.open()) {
            c.setAutoCommit(false);
            try {
                Coupon before = requireOwned(c, ownerId, id, version);
                lockShop(c, ownerId, before.getShopId());
                lockCoupon(c, id, before.getShopId());
                Coupon p = requireOwned(c, ownerId, id, version);
                if (deleted) active = false;
                if (active) checkNotExpired(p);
                try (PreparedStatement ps = c.prepareStatement("""
                        UPDATE coupons SET is_active = ?, is_deleted = ?, updated_at = ?, version = version + 1
                        WHERE coupon_id = ? AND shop_id = ? AND version = ? AND is_deleted = 0
                        """)) {
                    ps.setBoolean(1, active); ps.setBoolean(2, deleted);
                    ps.setTimestamp(3, Timestamp.valueOf(PromotionRules.now()));
                    ps.setInt(4, id); ps.setInt(5, p.getShopId()); ps.setInt(6, version);
                    if (ps.executeUpdate() != 1) throw new SQLException("Coupon đã thay đổi.");
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    /** Dùng trong transaction áp dụng mã; shopId lấy từ đơn đã kiểm tra quyền. */
    Coupon findForUse(Connection c, int shopId, String code) throws SQLException {
        // Tìm ID từ mã, sau đó dùng cùng một hàm khóa với thao tác sửa/xóa/gỡ.
        // Chỉ đếm lượt sau khi đã lấy khóa; không đọc quota trước rồi mới khóa.
        int id;
        String normalized = CouponRules.normalizeCode(code);
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT coupon_id FROM coupons WHERE shop_id = ? AND code = ?")) {
            ps.setInt(1, shopId); ps.setString(2, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Mã không tồn tại tại cửa hàng của đơn này.");
                id = rs.getInt(1);
            }
        }
        lockCoupon(c, id, shopId);
        String sql = FIELDS + " JOIN shops s ON s.shop_id = p.shop_id WHERE p.coupon_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Mã không tồn tại tại cửa hàng của đơn này.");
                Coupon p = map(rs);
                if (!normalized.equals(p.getCode()))
                    throw new IllegalArgumentException("Mã vừa được thay đổi. Hãy kiểm tra lại mã giảm giá.");
                return p;
            }
        }
    }

    static void lockCoupon(Connection c, int id, int shopId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT coupon_id FROM coupons WITH (UPDLOCK, HOLDLOCK) WHERE coupon_id = ? AND shop_id = ?")) {
            ps.setInt(1, id); ps.setInt(2, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy mã thuộc cửa hàng này.");
            }
        }
    }

    int countForCustomer(Connection c, int couponId, int customerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT COUNT(*) FROM coupon_usages u JOIN orders o ON o.order_id = u.order_id
                WHERE u.coupon_id = ? AND u.customer_id = ? AND u.usage_status = 'Applied'
                  AND o.status <> N'cancelled'
                """)) {
            ps.setInt(1, couponId); ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    public List<CouponUsage> findUsage(int ownerId, Integer couponId) throws SQLException {
        List<CouponUsage> list = new ArrayList<>();
        String sql = """
            SELECT u.*, a.full_name, s.shop_name, o.status AS order_status FROM coupon_usages u
            JOIN coupons p ON p.coupon_id = u.coupon_id
            JOIN shops s ON s.shop_id = u.shop_id
            JOIN users owner ON owner.user_id = s.owner_id
            JOIN user_roles ur ON ur.user_id = owner.user_id
            JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
            JOIN users a ON a.user_id = u.customer_id
            JOIN orders o ON o.order_id = u.order_id
            WHERE s.owner_id = ? AND s.status = N'active'
              AND owner.status = N'active'
            """;
        if (couponId != null) sql += " AND u.coupon_id = ?";
        sql += " ORDER BY u.used_at DESC, u.usage_id DESC";
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId); if (couponId != null) ps.setInt(2, couponId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CouponUsage u = new CouponUsage();
                    u.setId(rs.getInt("usage_id")); u.setOrderId(rs.getInt("order_id"));
                    u.setCode(rs.getString("code_snapshot")); u.setShopName(rs.getString("shop_name"));
                    u.setCustomerName(rs.getString("full_name")); u.setSubtotal(rs.getBigDecimal("subtotal"));
                    u.setDiscountAmount(rs.getBigDecimal("discount_amount")); u.setFinalAmount(rs.getBigDecimal("final_amount"));
                    u.setUsedAt(rs.getTimestamp("used_at").toLocalDateTime());
                    u.setUsageStatus(rs.getString("usage_status")); u.setOrderStatus(rs.getString("order_status"));
                    list.add(u);
                }
            }
        }
        return list;
    }

    private Coupon requireOwned(Connection c, int ownerId, int id, int version) throws SQLException {
        Coupon p = findById(c, ownerId, id);
        if (p == null) throw new IllegalArgumentException("Không tìm thấy mã thuộc shop của bạn.");
        if (p.getVersion() != version) throw new IllegalArgumentException("Dữ liệu đã thay đổi. Hãy tải lại trang trước khi sửa tiếp.");
        return p;
    }

    private void lockShop(Connection c, int ownerId, int shopId) throws SQLException {
        String sql = """
            SELECT s.shop_id FROM shops s WITH (UPDLOCK, HOLDLOCK)
            JOIN users a WITH (HOLDLOCK) ON a.user_id = s.owner_id
            JOIN user_roles ur ON ur.user_id = a.user_id
            JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
            WHERE s.shop_id = ? AND s.owner_id = ? AND s.status = N'active'
              AND a.status = N'active'
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId); ps.setInt(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Cửa hàng không thuộc quyền quản lý của bạn hoặc đã ngừng hoạt động.");
            }
        }
    }

    private void checkNotExpired(Coupon p) {
        if (!p.getEndsAt().isAfter(PromotionRules.now())) throw new IllegalArgumentException("Mã đã hết hạn, không thể bật.");
    }

    private boolean samePolicy(Coupon a, Coupon b) {
        return a.getCode().equals(b.getCode()) && a.getDiscountType().equals(b.getDiscountType())
                && a.getDiscountValue().compareTo(b.getDiscountValue()) == 0
                && Objects.equals(a.getMaxDiscount(), b.getMaxDiscount())
                && a.getMinOrderAmount().compareTo(b.getMinOrderAmount()) == 0
                && a.getTotalLimit() == b.getTotalLimit() && a.getPerCustomerLimit() == b.getPerCustomerLimit()
                && a.getStartsAt().equals(b.getStartsAt()) && a.getEndsAt().equals(b.getEndsAt());
    }

    private void fields(PreparedStatement ps, Coupon p) throws SQLException {
        ps.setString(1, p.getCode()); ps.setNString(2, p.getName()); ps.setNString(3, p.getDescription());
        ps.setString(4, p.getDiscountType()); ps.setBigDecimal(5, p.getDiscountValue());
        if (p.getMaxDiscount() == null) ps.setNull(6, Types.DECIMAL); else ps.setBigDecimal(6, p.getMaxDiscount());
        ps.setBigDecimal(7, p.getMinOrderAmount()); ps.setInt(8, p.getTotalLimit()); ps.setInt(9, p.getPerCustomerLimit());
        ps.setTimestamp(10, Timestamp.valueOf(p.getStartsAt())); ps.setTimestamp(11, Timestamp.valueOf(p.getEndsAt()));
        ps.setBoolean(12, p.isActive());
    }

    private Coupon map(ResultSet rs) throws SQLException {
        Coupon p = new Coupon();
        p.setId(rs.getInt("coupon_id")); p.setShopId(rs.getInt("shop_id")); p.setShopName(rs.getString("shop_name"));
        p.setCode(rs.getString("code")); p.setName(rs.getString("coupon_name")); p.setDescription(rs.getString("description"));
        p.setDiscountType(rs.getString("discount_type")); p.setDiscountValue(rs.getBigDecimal("discount_value"));
        p.setMaxDiscount(rs.getBigDecimal("max_discount")); p.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        p.setTotalLimit(rs.getInt("total_limit")); p.setPerCustomerLimit(rs.getInt("per_customer_limit"));
        p.setStartsAt(rs.getTimestamp("starts_at").toLocalDateTime()); p.setEndsAt(rs.getTimestamp("ends_at").toLocalDateTime());
        p.setActive(rs.getBoolean("is_active")); p.setDeleted(rs.getBoolean("is_deleted")); p.setVersion(rs.getInt("version"));
        p.setUsedCount(rs.getInt("used_count")); p.setHistoryCount(rs.getInt("history_count"));
        return p;
    }
}
