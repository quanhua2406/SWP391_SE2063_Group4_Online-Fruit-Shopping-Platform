package com.mycompany.onlinefruitshopping.shopowner.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.shopowner.model.Promotion;
import com.mycompany.onlinefruitshopping.shopowner.model.PromotionPrice;
import com.mycompany.onlinefruitshopping.shopowner.model.PromotionProduct;
import com.mycompany.onlinefruitshopping.shopowner.model.ShopOption;
import com.mycompany.onlinefruitshopping.shopowner.utils.PromotionRules;

/** Mọi thao tác đều kiểm tra ownerId lấy từ session, không tin ownerId trên form. */
public class PromotionDAO {
    private static final String OWNED_PROMOTIONS = """
        SELECT p.*, s.shop_name,
               (SELECT COUNT(*) FROM promotion_products pp
                WHERE pp.promotion_id = p.promotion_id) AS product_count
        FROM promotions p
        JOIN shops s ON s.shop_id = p.shop_id
        JOIN users a ON a.user_id = s.owner_id
        JOIN user_roles ur ON ur.user_id = a.user_id
        JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
        WHERE s.owner_id = ? AND s.status = N'active'
          AND a.status = N'active' AND p.is_deleted = 0
        """;

    public List<ShopOption> findShops(int ownerId) throws SQLException {
        List<ShopOption> list = new ArrayList<>();
        String sql = """
            SELECT s.shop_id, s.shop_name FROM shops s
            JOIN users a ON a.user_id = s.owner_id
            JOIN user_roles ur ON ur.user_id = a.user_id
            JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
            WHERE s.owner_id = ? AND s.status = N'active'
              AND a.status = N'active'
            ORDER BY s.shop_id
            """;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShopOption s = new ShopOption();
                    s.setId(rs.getInt("shop_id"));
                    s.setName(rs.getString("shop_name"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    /** Chỉ liệt kê sản phẩm của các shop mà owner quản lý; trạng thái bán là N'published'. */
    public List<PromotionProduct> findProducts(int ownerId) throws SQLException {
        List<PromotionProduct> list = new ArrayList<>();
        String sql = """
            SELECT pr.product_id, pr.product_name, pr.status
            FROM products pr
            JOIN shops s ON s.shop_id = pr.shop_id
            JOIN users a ON a.user_id = s.owner_id
            JOIN user_roles ur ON ur.user_id = a.user_id
            JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
            WHERE s.owner_id = ? AND s.status = N'active' AND a.status = N'active'
            ORDER BY pr.product_name, pr.product_id
            """;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PromotionProduct p = new PromotionProduct();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("product_name"));
                    p.setActive("published".equals(rs.getString("status")));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public List<Promotion> findAll(int ownerId) throws SQLException {
        List<Promotion> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(OWNED_PROMOTIONS + " ORDER BY p.promotion_id DESC")) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Promotion findById(int ownerId, int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return findById(c, ownerId, id);
        }
    }

    private Promotion findById(Connection c, int ownerId, int id) throws SQLException {
        Promotion p;
        try (PreparedStatement ps = c.prepareStatement(OWNED_PROMOTIONS + " AND p.promotion_id = ?")) {
            ps.setInt(1, ownerId);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                p = map(rs);
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT product_id FROM promotion_products WHERE promotion_id = ? ORDER BY product_id")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) p.getProductIds().add(rs.getInt(1));
            }
        }
        return p;
    }

    /** id = 0: tạo mới; id > 0: cập nhật. Trả về ID đã lưu. */
    public int save(int ownerId, Promotion p) throws SQLException {
        // PromotionService đã kiểm tra đầu vào và loại sản phẩm trùng.
        // Các kiểm tra phụ thuộc dữ liệu hiện tại vẫn thực hiện sau khi khóa bên dưới.
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                lockShop(c, ownerId, p.getShopId());
                if (p.getId() > 0) {
                    Promotion old = requireOwned(c, ownerId, p.getId(), p.getVersion());
                    if (old.getShopId() != p.getShopId())
                        throw new IllegalArgumentException("Không được chuyển chương trình sang cửa hàng khác.");
                }
                validateProducts(c, p);
                if (p.isActive()) {
                    PromotionRules.validateActivation(p, PromotionRules.now());
                    checkOverlap(c, p);
                }
                Timestamp now = Timestamp.valueOf(PromotionRules.now());
                int id = p.getId();
                if (id == 0) {
                    String sql = """
                        INSERT INTO promotions(shop_id, promotion_name, description, discount_type,
                            discount_value, starts_at, ends_at, is_active, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                    try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, p.getShopId());
                        setFields(ps, p, 2);
                        ps.setTimestamp(9, now);
                        ps.setTimestamp(10, now);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("Không lấy được ID khuyến mãi.");
                            id = keys.getInt(1);
                        }
                    }
                } else {
                    String sql = """
                        UPDATE promotions SET promotion_name = ?, description = ?, discount_type = ?,
                            discount_value = ?, starts_at = ?, ends_at = ?, is_active = ?,
                            updated_at = ?, version = version + 1
                        WHERE promotion_id = ? AND shop_id = ? AND is_deleted = 0 AND version = ?
                        """;
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        setFields(ps, p, 1);
                        ps.setTimestamp(8, now);
                        ps.setInt(9, id);
                        ps.setInt(10, p.getShopId());
                        ps.setInt(11, p.getVersion());
                        checkChanged(ps.executeUpdate());
                    }
                    try (PreparedStatement ps = c.prepareStatement("DELETE FROM promotion_products WHERE promotion_id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO promotion_products(promotion_id, product_id) VALUES (?, ?)")) {
                    for (int productId : p.getProductIds()) {
                        ps.setInt(1, id);
                        ps.setInt(2, productId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                // Chỉ khi cả thông tin và danh sách sản phẩm thành công mới ghi lại.
                c.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void setActive(int ownerId, int id, int version, boolean active) throws SQLException {
        changeState(ownerId, id, version, active, false);
    }

    public void delete(int ownerId, int id, int version) throws SQLException {
        // Xóa mềm: không mất dữ liệu cũ, không làm hỏng tham chiếu lịch sử sau này.
        changeState(ownerId, id, version, false, true);
    }

    private void changeState(int ownerId, int id, int version, boolean active, boolean deleted)
            throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Promotion p = requireOwned(c, ownerId, id, version);
                lockShop(c, ownerId, p.getShopId());
                p = requireOwned(c, ownerId, id, version);
                if (active) {
                    PromotionRules.validateActivation(p, PromotionRules.now());
                    validateProducts(c, p);
                    checkOverlap(c, p);
                }
                String sql = """
                    UPDATE promotions SET is_active = ?, is_deleted = ?, updated_at = ?, version = version + 1
                    WHERE promotion_id = ? AND shop_id = ? AND is_deleted = 0 AND version = ?
                    """;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setBoolean(1, active);
                    ps.setBoolean(2, deleted);
                    ps.setTimestamp(3, Timestamp.valueOf(PromotionRules.now()));
                    ps.setInt(4, id);
                    ps.setInt(5, p.getShopId());
                    ps.setInt(6, version);
                    checkChanged(ps.executeUpdate());
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Promotion requireOwned(Connection c, int ownerId, int id, int version) throws SQLException {
        Promotion p = findById(c, ownerId, id);
        if (p == null) throw new IllegalArgumentException("Không tìm thấy chương trình thuộc cửa hàng của bạn.");
        if (p.getVersion() != version) throw new IllegalArgumentException(
                "Dữ liệu đã thay đổi ở lần thao tác khác. Hãy tải lại trang trước khi sửa tiếp.");
        return p;
    }

    private void lockShop(Connection c, int ownerId, int shopId) throws SQLException {
        // Mọi lần tạo/sửa/bật/tắt/xóa của cùng shop đi qua khóa này.
        // Hai yêu cầu đồng thời không thể cùng bỏ qua bước kiểm tra trùng lịch.
        String sql = """
            SELECT s.shop_id FROM shops s WITH (UPDLOCK, HOLDLOCK)
            JOIN users a WITH (HOLDLOCK) ON a.user_id = s.owner_id
            JOIN user_roles ur ON ur.user_id = a.user_id
            JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
            WHERE s.shop_id = ? AND s.owner_id = ? AND s.status = N'active'
              AND a.status = N'active'
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setInt(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Cửa hàng không thuộc quyền quản lý của bạn hoặc đã ngừng hoạt động.");
            }
        }
    }

    private void validateProducts(Connection c, Promotion p) throws SQLException {
        // Sản phẩm phải thuộc đúng shop của chương trình (shop đã khóa và kiểm tra quyền ở lockShop),
        // đang bán và đã có biến thể.
        String sql = """
            SELECT product_id FROM products WITH (HOLDLOCK)
            WHERE product_id = ? AND shop_id = ? AND status = N'published'
              AND EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = products.product_id)
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int productId : p.getProductIds()) {
                ps.setInt(1, productId);
                ps.setInt(2, p.getShopId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException(
                            "Sản phẩm #" + productId + " không thuộc cửa hàng này, ngừng bán hoặc chưa có biến thể. Hãy bỏ chọn sản phẩm này.");
                }
            }
        }
    }

    private void checkOverlap(Connection c, Promotion p) throws SQLException {
        // [bắt đầu, kết thúc): chương trình mới có thể bắt đầu đúng lúc chương trình cũ hết hạn.
        String sql = """
            SELECT TOP (1) other.promotion_id
            FROM promotions other JOIN promotion_products pp ON pp.promotion_id = other.promotion_id
            WHERE other.shop_id = ? AND other.promotion_id <> ?
              AND other.is_active = 1 AND other.is_deleted = 0 AND pp.product_id = ?
              AND other.starts_at < ? AND other.ends_at > ?
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int productId : p.getProductIds()) {
                ps.setInt(1, p.getShopId());
                ps.setInt(2, p.getId());
                ps.setInt(3, productId);
                ps.setTimestamp(4, Timestamp.valueOf(p.getEndsAt()));
                ps.setTimestamp(5, Timestamp.valueOf(p.getStartsAt()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("Sản phẩm #" + productId
                            + " trùng thời gian với chương trình đang bật #" + rs.getInt(1)
                            + ". Hãy đổi lịch, bỏ sản phẩm hoặc tắt chương trình kia.");
                }
            }
        }
    }

    public List<PromotionPrice> previewPrices(int ownerId, int id) throws SQLException {
        List<PromotionPrice> list = new ArrayList<>();
        // Đọc lại chương trình qua kiểm tra chủ sở hữu, không nhận mức giảm từ URL.
        try (Connection c = DBConnection.getConnection()) {
            Promotion p = findById(c, ownerId, id);
            if (p == null) throw new IllegalArgumentException("Không tìm thấy chương trình thuộc cửa hàng của bạn.");
            String sql = """
                SELECT pr.product_name, pr.status, v.sku AS variant_name, v.price
                FROM promotion_products pp JOIN products pr ON pr.product_id = pp.product_id
                JOIN product_variants v ON v.product_id = pr.product_id
                WHERE pp.promotion_id = ? ORDER BY pr.product_name, v.variant_id
                """;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        PromotionPrice row = new PromotionPrice();
                        row.setProductName(rs.getString("product_name"));
                        row.setVariantName(rs.getString("variant_name"));
                        row.setProductActive("published".equals(rs.getString("status")));
                        row.setOriginalPrice(rs.getBigDecimal("price"));
                        row.setFinalPrice(PromotionRules.priceAfterDiscount(row.getOriginalPrice(),
                                p.getDiscountType(), p.getDiscountValue()));
                        list.add(row);
                    }
                }
            }
        }
        return list;
    }

    private void setFields(PreparedStatement ps, Promotion p, int start) throws SQLException {
        ps.setNString(start, p.getName());
        ps.setNString(start + 1, p.getDescription());
        ps.setString(start + 2, p.getDiscountType());
        ps.setBigDecimal(start + 3, p.getDiscountValue());
        ps.setTimestamp(start + 4, Timestamp.valueOf(p.getStartsAt()));
        ps.setTimestamp(start + 5, Timestamp.valueOf(p.getEndsAt()));
        ps.setBoolean(start + 6, p.isActive());
    }

    private void checkChanged(int rows) throws SQLException {
        if (rows != 1) throw new SQLException("Không cập nhật được đúng một chương trình.");
    }

    private Promotion map(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setId(rs.getInt("promotion_id"));
        p.setShopId(rs.getInt("shop_id"));
        p.setShopName(rs.getString("shop_name"));
        p.setName(rs.getString("promotion_name"));
        p.setDescription(rs.getString("description"));
        p.setDiscountType(rs.getString("discount_type"));
        p.setDiscountValue(rs.getBigDecimal("discount_value"));
        p.setStartsAt(rs.getTimestamp("starts_at").toLocalDateTime());
        p.setEndsAt(rs.getTimestamp("ends_at").toLocalDateTime());
        p.setActive(rs.getBoolean("is_active"));
        p.setVersion(rs.getInt("version"));
        p.setProductCount(rs.getInt("product_count"));
        return p;
    }
}
