
package com.mycompany.onlinefruitshopping.dao;

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
            String imageUrl) throws SQLException {

        String insertProduct = """
            INSERT INTO products
                (shop_id, category_id, product_name,
                 description, origin, status)
            VALUES (?, ?, ?, ?, ?, 'draft')
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
}
