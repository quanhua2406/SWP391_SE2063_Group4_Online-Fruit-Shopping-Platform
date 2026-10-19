package com.mycompany.onlinefruitshopping.dao.inventory;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventory &amp; pricing data access for shop owners.
 *
 * <p>Covers the shop-owner side of "Inventory &amp; Pricing
 * Management": viewing stock, updating stock, restocking, low
 * stock monitoring, inventory history and product / variant
 * pricing.</p>
 *
 * <p>Every query is scoped to the owner's shop through
 * {@code products.shop_id}, so a shop owner can only touch their
 * own inventory.</p>
 */
public class InventoryDAO {

    /**
     * Default "low stock" safety threshold. Used when a shop has not
     * configured its own value in {@code shops.low_stock_threshold}.
     */
    public static final double LOW_STOCK_THRESHOLD = 10.0;

    // =========================================================
    // SHOP RESOLUTION
    // =========================================================

    private Integer findShopId(Connection conn, int ownerId)
            throws SQLException {

        String sql = "SELECT shop_id FROM shops WHERE owner_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shop_id");
                }
            }
        }

        return null;
    }

    public boolean hasShop(int ownerId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findShopId(conn, ownerId) != null;
        }
    }

    // =========================================================
    // LOW STOCK THRESHOLD (PER SHOP CONFIGURATION)
    // =========================================================

    /**
     * Returns the shop's low-stock threshold, or the default
     * {@link #LOW_STOCK_THRESHOLD} when the shop has no value set.
     */
    public double getLowStockThreshold(int ownerId) throws SQLException {

        String sql = """
                SELECT ISNULL(low_stock_threshold, ?) AS threshold
                FROM shops
                WHERE owner_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, BigDecimal.valueOf(LOW_STOCK_THRESHOLD));
            ps.setInt(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal value = rs.getBigDecimal("threshold");
                    return value == null ? LOW_STOCK_THRESHOLD : value.doubleValue();
                }
            }
        }

        return LOW_STOCK_THRESHOLD;
    }

    /**
     * Stores a new low-stock threshold for the shop. Negative values
     * are rejected.
     */
    public boolean updateLowStockThreshold(int ownerId, double threshold)
            throws SQLException {

        if (threshold < 0) {
            return false;
        }

        String sql = """
                UPDATE shops
                SET low_stock_threshold = ?
                WHERE owner_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, BigDecimal.valueOf(threshold));
            ps.setInt(2, ownerId);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // DASHBOARD SUMMARY
    // =========================================================

    /**
     * Small aggregate used by the shop-owner dashboard: how many
     * products / variants the shop has, the total active stock and
     * how many variants are low or out of stock.
     */
    public Map<String, Object> getDashboardStats(int ownerId)
            throws SQLException {

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("productCount", 0);
        stats.put("variantCount", 0);
        stats.put("totalStock", BigDecimal.ZERO);
        stats.put("lowStockCount", 0);
        stats.put("outOfStockCount", 0);

        try (Connection conn = DBConnection.getConnection()) {

            Integer shopId = findShopId(conn, ownerId);
            if (shopId == null) {
                return stats;
            }

            String countSql = """
                    SELECT
                        (SELECT COUNT(*)
                         FROM products p
                         WHERE p.shop_id = ?) AS product_count,
                        (SELECT COUNT(*)
                         FROM product_variants v
                         JOIN products p ON p.product_id = v.product_id
                         WHERE p.shop_id = ?) AS variant_count,
                        (SELECT ISNULL(SUM(b.quantity), 0)
                         FROM inventory_batches b
                         JOIN product_variants v ON v.variant_id = b.variant_id
                         JOIN products p ON p.product_id = v.product_id
                         WHERE p.shop_id = ?
                           AND b.status = 'active') AS total_stock
                    """;

            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setInt(1, shopId);
                ps.setInt(2, shopId);
                ps.setInt(3, shopId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("productCount", rs.getInt("product_count"));
                        stats.put("variantCount", rs.getInt("variant_count"));
                        stats.put("totalStock", rs.getBigDecimal("total_stock"));
                    }
                }
            }

            double threshold = getLowStockThreshold(ownerId);

            String stockSql = """
                    SELECT
                        ISNULL((
                            SELECT SUM(b.quantity)
                            FROM inventory_batches b
                            WHERE b.variant_id = v.variant_id
                              AND b.status = 'active'
                        ), 0) AS stock
                    FROM product_variants v
                    JOIN products p ON p.product_id = v.product_id
                    WHERE p.shop_id = ?
                    """;

            int low = 0;
            int out = 0;

            try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                ps.setInt(1, shopId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BigDecimal stock = rs.getBigDecimal("stock");
                        double value = stock == null ? 0 : stock.doubleValue();

                        if (value <= 0) {
                            out++;
                        } else if (value <= threshold) {
                            low++;
                        }
                    }
                }
            }

            stats.put("lowStockCount", low);
            stats.put("outOfStockCount", out);
        }

        return stats;
    }

    // =========================================================
    // STOCK BY VARIANT (VIEW STOCK QUANTITY)
    // =========================================================

    public List<Map<String, Object>> getVariantsForOwner(int ownerId)
            throws SQLException {

        String sql = """
                SELECT v.variant_id,
                       v.product_id,
                       p.product_name,
                       v.sku,
                       v.unit,
                       v.weight_value,
                       v.packaging_type,
                       v.price,
                       pr.promo_price,
                       pr.start_date AS promo_start,
                       pr.end_date AS promo_end,
                       pr.status AS promo_status,
                       pr.review_note AS promo_note,
                       ISNULL((
                           SELECT SUM(b.quantity)
                           FROM inventory_batches b
                           WHERE b.variant_id = v.variant_id
                             AND b.status = 'active'
                       ), 0) AS stock
                FROM product_variants v
                JOIN products p ON p.product_id = v.product_id
                LEFT JOIN variant_promotions pr
                       ON pr.variant_id = v.variant_id
                WHERE p.shop_id = ?
                ORDER BY p.product_name ASC,
                         ISNULL(v.weight_value, 0) ASC,
                         v.variant_id ASC
                """;

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            Integer shopId = findShopId(conn, ownerId);
            if (shopId == null) {
                return list;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    list.addAll(mapVariants(rs));
                }
            }
        }

        return list;
    }

    // =========================================================
    // LOW STOCK MONITORING
    // =========================================================

    public List<Map<String, Object>> getLowStockForOwner(int ownerId,
                                                         double threshold)
            throws SQLException {

        String sql = """
                SELECT t.* FROM (
                    SELECT v.variant_id,
                           v.product_id,
                           p.product_name,
                           v.sku,
                           v.unit,
                           v.weight_value,
                           v.packaging_type,
                           v.price,
                           pr.promo_price,
                           pr.start_date AS promo_start,
                           pr.end_date AS promo_end,
                           pr.status AS promo_status,
                           pr.review_note AS promo_note,
                           ISNULL((
                               SELECT SUM(b.quantity)
                               FROM inventory_batches b
                               WHERE b.variant_id = v.variant_id
                                 AND b.status = 'active'
                           ), 0) AS stock
                    FROM product_variants v
                    JOIN products p ON p.product_id = v.product_id
                    LEFT JOIN variant_promotions pr
                           ON pr.variant_id = v.variant_id
                    WHERE p.shop_id = ?
                ) t
                WHERE t.stock <= ?
                ORDER BY t.stock ASC, t.product_name ASC
                """;

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            Integer shopId = findShopId(conn, ownerId);
            if (shopId == null) {
                return list;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, shopId);
                ps.setBigDecimal(2, BigDecimal.valueOf(threshold));
                try (ResultSet rs = ps.executeQuery()) {
                    list.addAll(mapVariants(rs));
                }
            }
        }

        return list;
    }

    private List<Map<String, Object>> mapVariants(ResultSet rs)
            throws SQLException {

        List<Map<String, Object>> list = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("variant_id", rs.getInt("variant_id"));
            item.put("product_id", rs.getInt("product_id"));
            item.put("product_name", rs.getString("product_name"));
            item.put("sku", rs.getString("sku"));
            item.put("unit", rs.getString("unit"));
            item.put("weight_value", rs.getBigDecimal("weight_value"));
            item.put("packaging_type", rs.getString("packaging_type"));
            item.put("price", rs.getBigDecimal("price"));
            item.put("promo_price", rs.getBigDecimal("promo_price"));
            item.put("promo_start", rs.getDate("promo_start"));
            item.put("promo_end", rs.getDate("promo_end"));
            item.put("promo_status", rs.getString("promo_status"));
            item.put("promo_note", rs.getString("promo_note"));
            item.put("stock", rs.getBigDecimal("stock"));
            list.add(item);
        }

        return list;
    }

    // =========================================================
    // BATCHES (VIEW / UPDATE STOCK QUANTITY)
    // =========================================================

    public List<Map<String, Object>> getBatchesForOwner(int ownerId)
            throws SQLException {

        String sql = """
                SELECT b.batch_id,
                       b.variant_id,
                       p.product_name,
                       v.sku,
                       v.unit,
                       b.batch_code,
                       b.quantity,
                       b.expiry_date,
                       b.received_date,
                       b.status
                FROM inventory_batches b
                JOIN product_variants v ON v.variant_id = b.variant_id
                JOIN products p ON p.product_id = v.product_id
                WHERE p.shop_id = ?
                ORDER BY b.received_date DESC, b.batch_id DESC
                """;

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            Integer shopId = findShopId(conn, ownerId);
            if (shopId == null) {
                return list;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("batch_id", rs.getInt("batch_id"));
                        item.put("variant_id", rs.getInt("variant_id"));
                        item.put("product_name", rs.getString("product_name"));
                        item.put("sku", rs.getString("sku"));
                        item.put("unit", rs.getString("unit"));
                        item.put("batch_code", rs.getString("batch_code"));
                        item.put("quantity", rs.getBigDecimal("quantity"));
                        item.put("expiry_date", rs.getDate("expiry_date"));
                        item.put("received_date", rs.getDate("received_date"));
                        item.put("status", rs.getString("status"));
                        list.add(item);
                    }
                }
            }
        }

        return list;
    }

    public boolean batchBelongsToOwner(int batchId, int ownerId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM inventory_batches b
                JOIN product_variants v ON v.variant_id = b.variant_id
                JOIN products p ON p.product_id = v.product_id
                WHERE b.batch_id = ?
                  AND p.shop_id = (SELECT shop_id FROM shops WHERE owner_id = ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, batchId);
            ps.setInt(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean variantBelongsToOwner(int variantId, int ownerId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM product_variants v
                JOIN products p ON p.product_id = v.product_id
                WHERE v.variant_id = ?
                  AND p.shop_id = (SELECT shop_id FROM shops WHERE owner_id = ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            ps.setInt(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Sets a batch quantity to an absolute value and records the
     * change in the inventory adjustment log.
     */
    public boolean setBatchQuantity(int batchId,
                                    double newQuantity,
                                    int userId,
                                    String reason)
            throws SQLException {

        if (newQuantity < 0) {
            return false;
        }

        String selectSql =
                "SELECT quantity FROM inventory_batches WITH (UPDLOCK, ROWLOCK) "
                + "WHERE batch_id = ?";

        String updateSql =
                "UPDATE inventory_batches SET quantity = ? WHERE batch_id = ?";

        String logSql = """
                INSERT INTO inventory_adjustments
                    (batch_id, adjustment_type, quantity, reason, created_by, created_at)
                VALUES (?, ?, ?, ?, ?, GETDATE())
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            BigDecimal oldQuantity;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, batchId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    oldQuantity = rs.getBigDecimal("quantity");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, BigDecimal.valueOf(newQuantity));
                ps.setInt(2, batchId);

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            double delta = newQuantity - oldQuantity.doubleValue();
            String type = delta >= 0 ? "import" : "export";

            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, batchId);
                ps.setString(2, type);
                ps.setBigDecimal(3, BigDecimal.valueOf(Math.abs(delta)));
                ps.setString(4, reason);
                ps.setInt(5, userId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================
    // RESTOCK (ADD A NEW BATCH)
    // =========================================================

    /**
     * True when the same variant already has a batch with this code.
     * Used to keep batch codes meaningful inside a variant.
     */
    public boolean batchCodeExists(int variantId, String batchCode)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM inventory_batches
                WHERE variant_id = ?
                  AND batch_code = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            ps.setString(2, batchCode);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean restock(int variantId,
                           String batchCode,
                           double quantity,
                           Date expiryDate,
                           int userId,
                           String reason)
            throws SQLException {

        if (quantity <= 0) {
            return false;
        }

        String insertBatch = """
                INSERT INTO inventory_batches
                    (variant_id, batch_code, quantity, expiry_date, received_date, status)
                VALUES (?, ?, ?, ?, CAST(GETDATE() AS DATE), 'active')
                """;

        String logSql = """
                INSERT INTO inventory_adjustments
                    (batch_id, adjustment_type, quantity, reason, created_by, created_at)
                VALUES (?, 'import', ?, ?, ?, GETDATE())
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int batchId;
            try (PreparedStatement ps = conn.prepareStatement(
                    insertBatch, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, variantId);
                ps.setString(2, batchCode);
                ps.setBigDecimal(3, BigDecimal.valueOf(quantity));
                ps.setDate(4, expiryDate);

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        batchId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, batchId);
                ps.setBigDecimal(2, BigDecimal.valueOf(quantity));
                ps.setString(3, reason);
                ps.setInt(4, userId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================
    // STOCK DEDUCTION FOR AN ORDER
    // =========================================================

    /**
     * Deducts every line of an order from the shop's active batches
     * (FIFO by expiry date, then by batch id) and records an
     * <b>export</b> adjustment for each batch touched.
     *
     * <p>This is the bridge between the <i>Order Management</i>
     * module and the inventory: the order module calls it once an
     * order is confirmed, so that "View Stock", "Low Stock
     * Monitoring" and "Inventory History" stay in sync with real
     * sales. The whole order is applied inside a single
     * transaction - if any line cannot be fully covered, nothing is
     * changed and {@code false} is returned.</p>
     *
     * @param orderId the order to deduct
     * @param userId  the user recorded as author of the adjustment
     * @return {@code true} when the whole order was deducted
     */
    public boolean deductStockForOrder(int orderId, int userId)
            throws SQLException {

        String orderCheckSql = """
                SELECT 1
                FROM orders o
                WHERE o.order_id = ?
                  AND o.shop_id = (SELECT shop_id FROM shops WHERE owner_id = ?)
                """;

        String itemsSql = """
                SELECT variant_id, batch_id, quantity
                FROM order_items
                WHERE order_id = ?
                ORDER BY order_item_id
                """;

        String lockedBatchSql = """
                SELECT quantity FROM inventory_batches WITH (UPDLOCK, ROWLOCK)
                WHERE batch_id = ?
                """;

        String takeBatchesSql = """
                SELECT batch_id, quantity
                FROM inventory_batches WITH (UPDLOCK, ROWLOCK)
                WHERE variant_id = ?
                  AND status = 'active'
                  AND quantity > 0
                ORDER BY CASE WHEN expiry_date IS NULL THEN 1 ELSE 0 END,
                         expiry_date ASC,
                         batch_id ASC
                """;

        String deductSql =
                "UPDATE inventory_batches SET quantity = quantity - ? WHERE batch_id = ?";

        String logSql = """
                INSERT INTO inventory_adjustments
                    (batch_id, adjustment_type, quantity, reason, created_by, created_at)
                VALUES (?, 'export', ?, ?, ?, GETDATE())
                """;

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) The order must belong to this owner's shop.
            try (PreparedStatement ps = conn.prepareStatement(orderCheckSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2) Read the order lines.
            List<int[]> lines = new ArrayList<>();
            List<BigDecimal> lineQty = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(itemsSql)) {
                ps.setInt(1, orderId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int batchId = rs.getInt("batch_id");
                        lines.add(new int[]{
                            rs.getInt("variant_id"),
                            rs.wasNull() ? -1 : batchId
                        });
                        lineQty.add(rs.getBigDecimal("quantity"));
                    }
                }
            }

            if (lines.isEmpty()) {
                conn.rollback();
                return false;
            }

            // 3) Deduct each line, preferring the batch recorded on
            //    the order line and then walking active batches FIFO.
            for (int i = 0; i < lines.size(); i++) {

                int variantId = lines.get(i)[0];
                int preferredBatch = lines.get(i)[1];
                BigDecimal remaining = lineQty.get(i);

                if (preferredBatch > 0) {
                    remaining = takeFromBatch(
                            conn, preferredBatch, remaining,
                            orderId, userId, lockedBatchSql,
                            deductSql, logSql);
                }

                if (remaining.signum() > 0) {
                    remaining = takeFromVariant(
                            conn, variantId, remaining,
                            orderId, userId, takeBatchesSql,
                            deductSql, logSql);
                }

                if (remaining.signum() > 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Takes as much as possible from one batch and returns the
     * quantity that is still outstanding.
     */
    private BigDecimal takeFromBatch(Connection conn,
                                     int batchId,
                                     BigDecimal requested,
                                     int orderId,
                                     int userId,
                                     String lockedBatchSql,
                                     String deductSql,
                                     String logSql)
            throws SQLException {

        if (requested.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal available;

        try (PreparedStatement ps = conn.prepareStatement(lockedBatchSql)) {
            ps.setInt(1, batchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return requested;
                }

                available = rs.getBigDecimal("quantity");
            }
        }

        if (available == null || available.signum() <= 0) {
            return requested;
        }

        BigDecimal taken = available.min(requested);
        writeDeduction(conn, batchId, taken, orderId, userId, deductSql, logSql);

        return requested.subtract(taken);
    }

    /**
     * Takes as much as possible from the variant's active batches,
     * oldest expiry first, and returns the outstanding quantity.
     */
    private BigDecimal takeFromVariant(Connection conn,
                                       int variantId,
                                       BigDecimal requested,
                                       int orderId,
                                       int userId,
                                       String takeBatchesSql,
                                       String deductSql,
                                       String logSql)
            throws SQLException {

        List<int[]> batches = new ArrayList<>();
        List<BigDecimal> quantities = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(takeBatchesSql)) {
            ps.setInt(1, variantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batches.add(new int[]{rs.getInt("batch_id")});
                    quantities.add(rs.getBigDecimal("quantity"));
                }
            }
        }

        BigDecimal remaining = requested;

        for (int i = 0; i < batches.size() && remaining.signum() > 0; i++) {

            BigDecimal available = quantities.get(i);

            if (available == null || available.signum() <= 0) {
                continue;
            }

            BigDecimal taken = available.min(remaining);

            writeDeduction(conn, batches.get(i)[0], taken,
                    orderId, userId, deductSql, logSql);

            remaining = remaining.subtract(taken);
        }

        return remaining;
    }

    private void writeDeduction(Connection conn,
                                int batchId,
                                BigDecimal quantity,
                                int orderId,
                                int userId,
                                String deductSql,
                                String logSql)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(deductSql)) {
            ps.setBigDecimal(1, quantity);
            ps.setInt(2, batchId);

            if (ps.executeUpdate() == 0) {
                throw new SQLException("Batch " + batchId + " vanished.");
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(logSql)) {
            ps.setInt(1, batchId);
            ps.setBigDecimal(2, quantity);
            ps.setString(3, "Order #" + orderId);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    // =========================================================
    // INVENTORY HISTORY
    // =========================================================

    public List<Map<String, Object>> getAdjustmentsForOwner(int ownerId)
            throws SQLException {

        String sql = """
                SELECT a.adjustment_id,
                       a.adjustment_type,
                       a.quantity,
                       a.reason,
                       a.created_at,
                       a.created_by,
                       b.batch_code,
                       p.product_name,
                       v.sku
                FROM inventory_adjustments a
                JOIN inventory_batches b ON b.batch_id = a.batch_id
                JOIN product_variants v ON v.variant_id = b.variant_id
                JOIN products p ON p.product_id = v.product_id
                WHERE p.shop_id = ?
                ORDER BY a.created_at DESC, a.adjustment_id DESC
                """;

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            Integer shopId = findShopId(conn, ownerId);
            if (shopId == null) {
                return list;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("adjustment_id", rs.getInt("adjustment_id"));
                        item.put("adjustment_type", rs.getString("adjustment_type"));
                        item.put("quantity", rs.getBigDecimal("quantity"));
                        item.put("reason", rs.getString("reason"));
                        item.put("created_at", rs.getTimestamp("created_at"));
                        item.put("created_by", rs.getInt("created_by"));
                        item.put("batch_code", rs.getString("batch_code"));
                        item.put("product_name", rs.getString("product_name"));
                        item.put("sku", rs.getString("sku"));
                        list.add(item);
                    }
                }
            }
        }

        return list;
    }

    // =========================================================
    // PRICING (SET / EDIT PRODUCT & VARIANT PRICE)
    // =========================================================

    public boolean updateVariantPrice(int variantId, double price)
            throws SQLException {

        if (price < 0) {
            return false;
        }

        String sql = "UPDATE product_variants SET price = ? WHERE variant_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, BigDecimal.valueOf(price));
            ps.setInt(2, variantId);

            return ps.executeUpdate() > 0;
        }
    }

    public Double getVariantPrice(int variantId) throws SQLException {

        String sql = "SELECT price FROM product_variants WHERE variant_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        }

        return null;
    }

    // =========================================================
    // PROMOTIONAL PRICE (SET / EDIT PROMOTION)
    // =========================================================

    /**
     * Submits (inserts or replaces) the promotion of one variant.
     *
     * <p>The promotion always enters the approval workflow as
     * {@code pending}, so a first submission and an edit both wait
     * for an administrator.</p>
     *
     * <p>The ownership check and the write run inside a single
     * transaction, so a shop owner can only change the promotion of
     * a variant that belongs to their own shop.</p>
     *
     * @return {@code true} when the promotion was saved,
     *         {@code false} when the input is invalid or the variant
     *         does not belong to the owner.
     */
    public boolean setPromotion(int variantId,
                                BigDecimal promoPrice,
                                Date startDate,
                                Date endDate,
                                int ownerId)
            throws SQLException {

        if (promoPrice == null || promoPrice.signum() <= 0
                || startDate == null || endDate == null
                || endDate.before(startDate)) {
            return false;
        }

        String owned = """
                SELECT 1
                FROM product_variants v
                JOIN products p ON p.product_id = v.product_id
                WHERE v.variant_id = ?
                  AND p.shop_id = (SELECT shop_id FROM shops WHERE owner_id = ?)
                """;

        String delete = "DELETE FROM variant_promotions WHERE variant_id = ?";

        String insert = """
                INSERT INTO variant_promotions
                    (variant_id, promo_price, start_date, end_date, status,
                     reviewed_by, reviewed_at, review_note,
                     created_at, updated_at)
                VALUES (?, ?, ?, ?, 'pending',
                        NULL, NULL, NULL,
                        SYSDATETIME(), SYSDATETIME())
                """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(owned)) {
                    ps.setInt(1, variantId);
                    ps.setInt(2, ownerId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(delete)) {
                    ps.setInt(1, variantId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setInt(1, variantId);
                    ps.setBigDecimal(2, promoPrice);
                    ps.setDate(3, startDate);
                    ps.setDate(4, endDate);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Removes the promotion of a variant owned by the given shop
     * owner.
     */
    public boolean removePromotion(int variantId, int ownerId)
            throws SQLException {

        String sql = """
                DELETE FROM variant_promotions
                WHERE variant_id = ?
                  AND variant_id IN (
                      SELECT v.variant_id
                      FROM product_variants v
                      JOIN products p ON p.product_id = v.product_id
                      WHERE p.shop_id =
                          (SELECT shop_id FROM shops WHERE owner_id = ?)
                  )
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            ps.setInt(2, ownerId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @return the promotional price of a variant when the promotion
     *         has been approved and is still running or scheduled,
     *         or {@code null} when the variant has none, the
     *         promotion is waiting for review / was refused, or it
     *         already expired.
     */
    public BigDecimal getLivePromoPrice(int variantId) throws SQLException {

        String sql = """
                SELECT promo_price
                FROM variant_promotions
                WHERE variant_id = ?
                  AND status = 'approved'
                  AND end_date >= CAST(GETDATE() AS DATE)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("promo_price");
                }
            }
        }

        return null;
    }

    /**
     * The price that should be charged for a variant: the promotion
     * when it has been approved and is running today, otherwise the
     * normal price.
     *
     * <p>Order / cart / checkout code should call this instead of
     * reading {@code product_variants.price} directly.</p>
     */
    public BigDecimal getEffectivePrice(int variantId) throws SQLException {

        String sql = """
                SELECT v.price,
                       CASE WHEN CAST(GETDATE() AS DATE) BETWEEN pr.start_date
                                                            AND pr.end_date
                            THEN pr.promo_price
                       END AS active_promo
                FROM product_variants v
                LEFT JOIN variant_promotions pr
                       ON pr.variant_id = v.variant_id
                      AND pr.status = 'approved'
                WHERE v.variant_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal promo = rs.getBigDecimal("active_promo");
                    return promo != null ? promo : rs.getBigDecimal("price");
                }
            }
        }

        return null;
    }

    // =========================================================
    // PROMOTION APPROVAL (ADMIN REVIEW)
    // =========================================================

    /**
     * Every promotion of every shop, newest request first, with
     * enough product / shop information for the approval table.
     */
    public List<Map<String, Object>> getPromotionsForAdmin()
            throws SQLException {

        String sql = """
                SELECT pr.promo_id,
                       pr.variant_id,
                       pr.promo_price,
                       pr.start_date,
                       pr.end_date,
                       pr.status,
                       pr.review_note,
                       pr.created_at,
                       pr.updated_at,
                       pr.reviewed_at,
                       v.sku,
                       v.unit,
                       v.price,
                       p.product_name,
                       s.shop_name,
                       reviewer.full_name AS reviewer_name
                FROM variant_promotions pr
                JOIN product_variants v ON v.variant_id = pr.variant_id
                JOIN products p ON p.product_id = v.product_id
                JOIN shops s ON s.shop_id = p.shop_id
                LEFT JOIN users reviewer ON reviewer.user_id = pr.reviewed_by
                ORDER BY CASE pr.status WHEN 'pending' THEN 0 ELSE 1 END,
                         pr.updated_at DESC
                """;

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("promo_id", rs.getInt("promo_id"));
                item.put("variant_id", rs.getInt("variant_id"));
                item.put("promo_price", rs.getBigDecimal("promo_price"));
                item.put("start_date", rs.getDate("start_date"));
                item.put("end_date", rs.getDate("end_date"));
                item.put("status", rs.getString("status"));
                item.put("review_note", rs.getString("review_note"));
                item.put("updated_at", rs.getTimestamp("updated_at"));
                item.put("reviewed_at", rs.getTimestamp("reviewed_at"));
                item.put("sku", rs.getString("sku"));
                item.put("unit", rs.getString("unit"));
                item.put("price", rs.getBigDecimal("price"));
                item.put("product_name", rs.getString("product_name"));
                item.put("shop_name", rs.getString("shop_name"));
                item.put("reviewer_name", rs.getString("reviewer_name"));
                list.add(item);
            }
        }

        return list;
    }

    /**
     * One promotion, used by the admin controller to check the
     * request before it is approved or refused.
     *
     * @return the row, or {@code null} when the promotion is gone.
     */
    public Map<String, Object> getPromotionForReview(int promoId)
            throws SQLException {

        String sql = """
                SELECT pr.promo_id,
                       pr.promo_price,
                       pr.start_date,
                       pr.end_date,
                       pr.status,
                       v.price,
                       v.sku,
                       p.product_name,
                       s.shop_name
                FROM variant_promotions pr
                JOIN product_variants v ON v.variant_id = pr.variant_id
                JOIN products p ON p.product_id = v.product_id
                JOIN shops s ON s.shop_id = p.shop_id
                WHERE pr.promo_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, promoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("promo_id", rs.getInt("promo_id"));
                item.put("promo_price", rs.getBigDecimal("promo_price"));
                item.put("start_date", rs.getDate("start_date"));
                item.put("end_date", rs.getDate("end_date"));
                item.put("status", rs.getString("status"));
                item.put("price", rs.getBigDecimal("price"));
                item.put("sku", rs.getString("sku"));
                item.put("product_name", rs.getString("product_name"));
                item.put("shop_name", rs.getString("shop_name"));
                return item;
            }
        }
    }

    /**
     * Approves or refuses a promotion that is still waiting for
     * review.
     *
     * <p>Only {@code pending} rows are touched, so two admins
     * reviewing the same request at the same time cannot overwrite
     * each other's decision.</p>
     *
     * @return {@code true} when the decision was stored,
     *         {@code false} when the promotion was already reviewed.
     */
    public boolean reviewPromotion(int promoId,
                                   String status,
                                   String note,
                                   int adminId)
            throws SQLException {

        if (!"approved".equals(status) && !"rejected".equals(status)) {
            return false;
        }

        String sql = """
                UPDATE variant_promotions
                SET status = ?,
                    review_note = ?,
                    reviewed_by = ?,
                    reviewed_at = SYSDATETIME(),
                    updated_at = SYSDATETIME()
                WHERE promo_id = ?
                  AND status = 'pending'
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, (note == null || note.isBlank()) ? null : note.trim());
            ps.setInt(3, adminId);
            ps.setInt(4, promoId);

            return ps.executeUpdate() > 0;
        }
    }
}
