package com.mycompany.onlinefruitshopping.dao.shop;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the administrator's shop-owner management (18.0):
 * listing the shops together with their owner, and blocking /
 * unblocking a shop.
 */
public class AdminShopDAO {

    // =========================================================
    // LIST / SEARCH
    // =========================================================

    /**
     * Counters shown on the dashboard card of the shop management
     * screen: every shop by status and how many products are on sale.
     */
    public ShopStatistics getStatistics() throws SQLException {

        String sql = """
            SELECT
                (SELECT COUNT(*) FROM shops) AS total_shops,
                (SELECT COUNT(*) FROM shops
                  WHERE status = 'active') AS active_shops,
                (SELECT COUNT(*) FROM shops
                  WHERE status = 'suspended') AS suspended_shops,
                (SELECT COUNT(*) FROM shops
                  WHERE status = 'pending') AS pending_shops,
                (SELECT COUNT(*) FROM products
                  WHERE status = 'published') AS published_products,
                (SELECT COUNT(*) FROM products
                  WHERE hidden_by_admin = 1) AS hidden_products
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ShopStatistics stats = new ShopStatistics();

            if (rs.next()) {
                stats.setTotalShops(rs.getInt("total_shops"));
                stats.setActiveShops(rs.getInt("active_shops"));
                stats.setSuspendedShops(rs.getInt("suspended_shops"));
                stats.setPendingShops(rs.getInt("pending_shops"));
                stats.setPublishedProducts(rs.getInt("published_products"));
                stats.setHiddenProducts(rs.getInt("hidden_products"));
            }

            return stats;
        }
    }

    /**
     * Every shop with its owner and a few counters, optionally filtered
     * by a keyword (shop name or owner email) and / or a status.
     */
    public List<ShopInfo> searchShops(String keyword, String status)
            throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT
                s.shop_id,
                s.shop_name,
                s.description,
                s.status,
                s.created_at,
                u.user_id,
                u.full_name,
                u.email,
                u.status AS owner_status,
                (SELECT COUNT(*) FROM products p
                  WHERE p.shop_id = s.shop_id) AS product_count,
                (SELECT COUNT(*) FROM products p
                  WHERE p.shop_id = s.shop_id
                    AND p.status = 'published') AS published_count
            FROM shops s
            JOIN users u ON u.user_id = s.owner_id
            WHERE 1 = 1
            """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (s.shop_name LIKE ? OR u.email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND LOWER(s.status) = LOWER(?)");
            params.add(status.trim());
        }

        sql.append(" ORDER BY s.shop_id");

        List<ShopInfo> shops = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shops.add(map(rs));
                }
            }
        }

        return shops;
    }

    public ShopInfo getShopById(int shopId) throws SQLException {

        String sql = """
            SELECT
                s.shop_id,
                s.shop_name,
                s.description,
                s.status,
                s.created_at,
                u.user_id,
                u.full_name,
                u.email,
                u.status AS owner_status,
                (SELECT COUNT(*) FROM products p
                  WHERE p.shop_id = s.shop_id) AS product_count,
                (SELECT COUNT(*) FROM products p
                  WHERE p.shop_id = s.shop_id
                    AND p.status = 'published') AS published_count
            FROM shops s
            JOIN users u ON u.user_id = s.owner_id
            WHERE s.shop_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, shopId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // =========================================================
    // BLOCK / UNBLOCK
    // =========================================================

    /**
     * Blocks the shop and its owner in one transaction:
     * the owner account is locked, the shop becomes {@code suspended}
     * and every published product is hidden.
     *
     * <p>The hidden products are flagged with {@code hidden_by_admin}
     * so that {@link #unblockShop(int, int)} restores only them and
     * leaves the products the shop had hidden by itself alone.</p>
     */
    public boolean blockShop(int shopId, int ownerId) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Suspend the shop.
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE shops SET status = 'suspended' "
                                + "WHERE shop_id = ?")) {
                    ps.setInt(1, shopId);
                    ps.executeUpdate();
                }

                // Lock the owner (a shop owner account is never an admin).
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET status = 'locked' "
                                + "WHERE user_id = ? AND status <> 'locked'")) {
                    ps.setInt(1, ownerId);
                    ps.executeUpdate();
                }

                // Hide the products that are currently on sale, and
                // remember that the block is what hid them.
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET status = 'hidden', "
                                + "hidden_by_admin = 1 "
                                + "WHERE shop_id = ? AND status = 'published'")) {
                    ps.setInt(1, shopId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * The reverse of {@link #blockShop(int, int)}: the owner is
     * reactivated, the shop is active again and the products that the
     * block hid are published again.
     *
     * <p>Only the products flagged {@code hidden_by_admin} are
     * restored; a product the shop had hidden on its own stays
     * hidden.</p>
     */
    public boolean unblockShop(int shopId, int ownerId) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE shops SET status = 'active' "
                                + "WHERE shop_id = ?")) {
                    ps.setInt(1, shopId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET status = 'active' "
                                + "WHERE user_id = ?")) {
                    ps.setInt(1, ownerId);
                    ps.executeUpdate();
                }

                // Publish again only what the block took off sale.
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET status = 'published', "
                                + "hidden_by_admin = 0 "
                                + "WHERE shop_id = ? "
                                + "AND status = 'hidden' "
                                + "AND hidden_by_admin = 1")) {
                    ps.setInt(1, shopId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // =========================================================
    // MAP
    // =========================================================

    private ShopInfo map(ResultSet rs) throws SQLException {

        ShopInfo shop = new ShopInfo();

        shop.setShopId(rs.getInt("shop_id"));
        shop.setShopName(rs.getString("shop_name"));
        shop.setDescription(rs.getString("description"));
        shop.setStatus(rs.getString("status"));
        shop.setCreatedAt(rs.getTimestamp("created_at"));
        shop.setOwnerId(rs.getInt("user_id"));
        shop.setOwnerName(rs.getString("full_name"));
        shop.setOwnerEmail(rs.getString("email"));
        shop.setOwnerStatus(rs.getString("owner_status"));
        shop.setProductCount(rs.getInt("product_count"));
        shop.setPublishedCount(rs.getInt("published_count"));

        return shop;
    }

    // =========================================================
    // SHOP INFO
    // =========================================================

    public static class ShopInfo {

        private int shopId;
        private String shopName;
        private String description;
        private String status;
        private java.sql.Timestamp createdAt;
        private int ownerId;
        private String ownerName;
        private String ownerEmail;
        private String ownerStatus;
        private int productCount;
        private int publishedCount;

        public int getShopId() {
            return shopId;
        }

        public void setShopId(int shopId) {
            this.shopId = shopId;
        }

        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public java.sql.Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.sql.Timestamp createdAt) {
            this.createdAt = createdAt;
        }

        public int getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(int ownerId) {
            this.ownerId = ownerId;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public String getOwnerEmail() {
            return ownerEmail;
        }

        public void setOwnerEmail(String ownerEmail) {
            this.ownerEmail = ownerEmail;
        }

        public String getOwnerStatus() {
            return ownerStatus;
        }

        public void setOwnerStatus(String ownerStatus) {
            this.ownerStatus = ownerStatus;
        }

        public int getProductCount() {
            return productCount;
        }

        public void setProductCount(int productCount) {
            this.productCount = productCount;
        }

        public int getPublishedCount() {
            return publishedCount;
        }

        public void setPublishedCount(int publishedCount) {
            this.publishedCount = publishedCount;
        }
    }

    // =========================================================
    // SHOP STATISTICS
    // =========================================================

    /**
     * Counters of the whole platform, used by the dashboard card of
     * the shop management screen.
     */
    public static class ShopStatistics {

        private int totalShops;
        private int activeShops;
        private int suspendedShops;
        private int pendingShops;
        private int publishedProducts;
        private int hiddenProducts;

        public int getTotalShops() {
            return totalShops;
        }

        public void setTotalShops(int totalShops) {
            this.totalShops = totalShops;
        }

        public int getActiveShops() {
            return activeShops;
        }

        public void setActiveShops(int activeShops) {
            this.activeShops = activeShops;
        }

        public int getSuspendedShops() {
            return suspendedShops;
        }

        public void setSuspendedShops(int suspendedShops) {
            this.suspendedShops = suspendedShops;
        }

        public int getPendingShops() {
            return pendingShops;
        }

        public void setPendingShops(int pendingShops) {
            this.pendingShops = pendingShops;
        }

        public int getPublishedProducts() {
            return publishedProducts;
        }

        public void setPublishedProducts(int publishedProducts) {
            this.publishedProducts = publishedProducts;
        }

        public int getHiddenProducts() {
            return hiddenProducts;
        }

        public void setHiddenProducts(int hiddenProducts) {
            this.hiddenProducts = hiddenProducts;
        }
    }
}
