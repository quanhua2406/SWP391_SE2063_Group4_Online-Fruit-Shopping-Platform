
package com.mycompany.onlinefruitshopping.dao.product;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminProductDAO {

    public List<Map<String, Object>> getAllProducts(
            String keyword) throws SQLException {

        List<Map<String, Object>> products = new ArrayList<>();

        String sql = """
            SELECT
                p.product_id,
                p.product_name,
                c.category_name,
                s.shop_name,
                p.origin,
                p.status,
                p.commission_rate,
                p.created_at,
                (
                    SELECT MIN(pv.price)
                    FROM product_variants pv
                    WHERE pv.product_id = p.product_id
                ) AS price,
                (
                    SELECT TOP 1 pi.image_url
                    FROM product_images pi
                    WHERE pi.product_id = p.product_id
                    ORDER BY pi.sort_order, pi.image_id
                ) AS image_url
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN shops s ON p.shop_id = s.shop_id
            WHERE (? IS NULL
                OR p.product_name LIKE ?
                OR CAST(p.product_id AS NVARCHAR(20)) LIKE ?)
            ORDER BY p.product_id DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String search = keyword == null || keyword.isBlank()
                    ? null
                    : "%" + keyword.trim() + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> product =
                            new LinkedHashMap<>();

                    for (int i = 1; i <= count; i++) {
                        product.put(
                                meta.getColumnLabel(i),
                                rs.getObject(i)
                        );
                    }

                    products.add(product);
                }
            }
        }

        return products;
    }

    public List<Map<String, Object>> getCategories()
            throws SQLException {

        List<Map<String, Object>> categories =
                new ArrayList<>();

        String sql = """
            SELECT category_id, category_name
            FROM categories
            ORDER BY category_name
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("category_id", rs.getInt("category_id"));
                item.put("category_name", rs.getString("category_name"));
                categories.add(item);
            }
        }

        return categories;
    }

    public List<Map<String, Object>> getShops()
            throws SQLException {

        List<Map<String, Object>> shops = new ArrayList<>();

        String sql = """
            SELECT shop_id, shop_name
            FROM shops
            WHERE status = 'active'
            ORDER BY shop_name
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("shop_id", rs.getInt("shop_id"));
                item.put("shop_name", rs.getString("shop_name"));
                shops.add(item);
            }
        }

        return shops;
    }

    public boolean createProduct(
            int shopId,
            int categoryId,
            String productName,
            String description,
            String origin,
            String sku,
            String unit,
            BigDecimal weightValue,
            String packagingType,
            BigDecimal price,
            BigDecimal commissionRate,
            String imageUrl) throws SQLException {

        String insertProduct = """
            INSERT INTO products
                (shop_id, category_id, product_name,
                 description, origin, status, commission_rate)
            VALUES (?, ?, ?, ?, ?, 'draft', ?)
            """;

        String insertVariant = """
            INSERT INTO product_variants
                (product_id, sku, unit, weight_value,
                 packaging_type, price)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String insertImage = """
            INSERT INTO product_images
                (product_id, image_url, sort_order)
            VALUES (?, ?, 0)
            """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int productId;

                try (PreparedStatement ps = conn.prepareStatement(
                        insertProduct,
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, shopId);
                    ps.setInt(2, categoryId);
                    ps.setString(3, productName);
                    ps.setString(4, description);
                    ps.setString(5, origin);
                    ps.setBigDecimal(
                            6,
                            commissionRate == null
                                    ? BigDecimal.ZERO
                                    : commissionRate
                    );

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Could not create product.");
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Product ID was not generated.");
                        }

                        productId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps =
                             conn.prepareStatement(insertVariant)) {

                    ps.setInt(1, productId);
                    ps.setString(2, sku);
                    ps.setString(3, unit);

                    if (weightValue == null) {
                        ps.setNull(4, java.sql.Types.DECIMAL);
                    } else {
                        ps.setBigDecimal(4, weightValue);
                    }

                    ps.setString(5, packagingType);
                    ps.setBigDecimal(6, price);
                    ps.executeUpdate();
                }

                if (imageUrl != null && !imageUrl.isBlank()) {
                    try (PreparedStatement ps =
                                 conn.prepareStatement(insertImage)) {

                        ps.setInt(1, productId);
                        ps.setString(2, imageUrl.trim());
                        ps.executeUpdate();
                    }
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

    // =========================================================
    // READ ONE
    // =========================================================

    public Map<String, Object> getProductForEdit(int productId)
            throws SQLException {

        String sql = """
            SELECT p.product_id, p.shop_id, s.shop_name, p.category_id,
                   p.product_name, p.description, p.origin, p.status,
                   p.commission_rate
            FROM products p
            LEFT JOIN shops s ON s.shop_id = p.shop_id
            WHERE p.product_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("product_id", rs.getInt("product_id"));
                item.put("shop_id", rs.getInt("shop_id"));
                item.put("shop_name", rs.getString("shop_name"));
                item.put("category_id", rs.getInt("category_id"));
                item.put("product_name", rs.getString("product_name"));
                item.put("description", rs.getString("description"));
                item.put("origin", rs.getString("origin"));
                item.put("status", rs.getString("status"));
                item.put("commission_rate", rs.getBigDecimal("commission_rate"));
                return item;
            }
        }
    }

    // =========================================================
    // UPDATE / STATUS
    // =========================================================

    public boolean updateProduct(
            int productId,
            int categoryId,
            String productName,
            String description,
            String origin,
            BigDecimal commissionRate,
            String status) throws SQLException {

        String sql = """
            UPDATE products
            SET category_id = ?, product_name = ?, description = ?,
                origin = ?, commission_rate = ?, status = ?
            WHERE product_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.setString(2, productName);
            ps.setString(3, description);
            ps.setString(4, origin);
            ps.setBigDecimal(5,
                    commissionRate == null ? BigDecimal.ZERO : commissionRate);
            ps.setString(6, status);
            ps.setInt(7, productId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateProductStatus(int productId, String status)
            throws SQLException {

        String sql = "UPDATE products SET status = ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Deletes a product and its dependent rows.
     *
     * @return "deleted", "has_orders" (product already sold) or
     *         "failed".
     */
    public String deleteProduct(int productId) throws SQLException {

        String variants = "(SELECT variant_id FROM product_variants "
                + "WHERE product_id = ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (countIn(conn,
                        "SELECT COUNT(*) FROM order_items WHERE variant_id IN "
                        + variants, productId) > 0) {

                    conn.rollback();
                    return "has_orders";
                }

                exec(conn, "DELETE FROM inventory_adjustments WHERE batch_id IN "
                        + "(SELECT batch_id FROM inventory_batches "
                        + "WHERE variant_id IN " + variants + ")", productId);

                exec(conn, "DELETE FROM inventory_batches WHERE variant_id IN "
                        + variants, productId);

                exec(conn, "DELETE FROM cart_items WHERE variant_id IN "
                        + variants, productId);

                exec(conn, "DELETE FROM variant_promotions WHERE variant_id IN "
                        + variants, productId);

                exec(conn, "DELETE FROM subscriptions WHERE variant_id IN "
                        + variants, productId);

                exec(conn, "DELETE FROM reviews WHERE product_id = ?", productId);

                exec(conn, "DELETE FROM product_images WHERE product_id = ?",
                        productId);

                exec(conn, "DELETE FROM product_variants WHERE product_id = ?",
                        productId);

                int deleted = exec(conn,
                        "DELETE FROM products WHERE product_id = ?", productId);

                conn.commit();
                return deleted > 0 ? "deleted" : "failed";

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int exec(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private int countIn(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // =========================================================
    // VARIANTS
    // =========================================================

    public List<Map<String, Object>> getVariantsByProduct(int productId)
            throws SQLException {

        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
            SELECT variant_id, product_id, sku, unit, weight_value,
                   packaging_type, price
            FROM product_variants
            WHERE product_id = ?
            ORDER BY ISNULL(weight_value, 0), variant_id
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    for (int i = 1; i <= count; i++) {
                        item.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(item);
                }
            }
        }

        return list;
    }

    public boolean addVariant(
            int productId,
            String sku,
            String unit,
            BigDecimal weightValue,
            String packagingType,
            BigDecimal price) throws SQLException {

        String sql = """
            INSERT INTO product_variants
                (product_id, sku, unit, weight_value, packaging_type, price)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setString(2, sku);
            ps.setString(3, unit);
            setNullableDecimal(ps, 4, weightValue);
            ps.setString(5, packagingType);
            ps.setBigDecimal(6, price);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateVariant(
            int variantId,
            String sku,
            String unit,
            BigDecimal weightValue,
            String packagingType,
            BigDecimal price) throws SQLException {

        String sql = """
            UPDATE product_variants
            SET sku = ?, unit = ?, weight_value = ?, packaging_type = ?,
                price = ?
            WHERE variant_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sku);
            ps.setString(2, unit);
            setNullableDecimal(ps, 3, weightValue);
            ps.setString(4, packagingType);
            ps.setBigDecimal(5, price);
            ps.setInt(6, variantId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a variant and its stock rows.
     *
     * @return "deleted", "has_orders" or "failed".
     */
    public String deleteVariant(int variantId) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (countIn(conn,
                        "SELECT COUNT(*) FROM order_items WHERE variant_id = ?",
                        variantId) > 0) {

                    conn.rollback();
                    return "has_orders";
                }

                exec(conn, "DELETE FROM inventory_adjustments WHERE batch_id IN "
                        + "(SELECT batch_id FROM inventory_batches "
                        + "WHERE variant_id = ?)", variantId);

                exec(conn, "DELETE FROM inventory_batches WHERE variant_id = ?",
                        variantId);

                exec(conn, "DELETE FROM cart_items WHERE variant_id = ?",
                        variantId);

                exec(conn, "DELETE FROM variant_promotions WHERE variant_id = ?",
                        variantId);

                exec(conn, "DELETE FROM subscriptions WHERE variant_id = ?",
                        variantId);

                int deleted = exec(conn,
                        "DELETE FROM product_variants WHERE variant_id = ?",
                        variantId);

                conn.commit();
                return deleted > 0 ? "deleted" : "failed";

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void setNullableDecimal(PreparedStatement ps, int index,
                                    BigDecimal value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.DECIMAL);
        } else {
            ps.setBigDecimal(index, value);
        }
    }

    public boolean skuExists(String sku, int excludeVariantId)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM product_variants "
                + "WHERE sku = ? AND variant_id <> ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sku);
            ps.setInt(2, excludeVariantId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
