package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.ProductVariant;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDAO {

    public List<ProductVariant> getVariantsByProductId(int productId) throws SQLException {
        List<ProductVariant> list = new ArrayList<>();
        String sql = """
                SELECT pv.variant_id, pv.product_id, p.product_name, pv.sku, pv.unit,
                       pv.weight_value, pv.packaging_type, pv.quality_grade, pv.price, pv.created_at,
                       ISNULL(SUM(ib.quantity), 0) AS stock
                FROM product_variants pv
                JOIN products p ON pv.product_id = p.product_id
                LEFT JOIN inventory_batches ib ON pv.variant_id = ib.variant_id AND ib.status = 'active'
                WHERE pv.product_id = ?
                GROUP BY pv.variant_id, pv.product_id, p.product_name, pv.sku, pv.unit,
                         pv.weight_value, pv.packaging_type, pv.quality_grade, pv.price, pv.created_at
                ORDER BY pv.variant_id ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductVariant v = new ProductVariant();
                    v.setVariantId(rs.getInt("variant_id"));
                    v.setProductId(rs.getInt("product_id"));
                    v.setProductName(rs.getString("product_name"));
                    v.setSku(rs.getString("sku"));
                    v.setUnit(rs.getString("unit"));
                    double weight = rs.getDouble("weight_value");
                    if (!rs.wasNull()) {
                        v.setWeightValue(weight);
                    }
                    v.setPackagingType(rs.getString("packaging_type"));
                    v.setQualityGrade(rs.getString("quality_grade"));
                    v.setPrice(rs.getDouble("price"));
                    v.setCreatedAt(rs.getTimestamp("created_at"));
                    v.setStock(rs.getDouble("stock"));
                    list.add(v);
                }
            }
        }
        if (list.isEmpty() && productId > 0) {
            createDefaultVariantIfMissing(productId);
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ProductVariant v = new ProductVariant();
                        v.setVariantId(rs.getInt("variant_id"));
                        v.setProductId(rs.getInt("product_id"));
                        v.setProductName(rs.getString("product_name"));
                        v.setSku(rs.getString("sku"));
                        v.setUnit(rs.getString("unit"));
                        double weight = rs.getDouble("weight_value");
                        if (!rs.wasNull()) {
                            v.setWeightValue(weight);
                        }
                        v.setPackagingType(rs.getString("packaging_type"));
                        String qg = rs.getString("quality_grade");
                        v.setQualityGrade(qg != null ? qg : "1");
                        v.setPrice(rs.getDouble("price"));
                        v.setCreatedAt(rs.getTimestamp("created_at"));
                        v.setStock(rs.getDouble("stock"));
                        list.add(v);
                    }
                }
            } catch (SQLException ignored) {}
        }
        return list;
    }

    private void createDefaultVariantIfMissing(int productId) {
        String checkProd = "SELECT product_name FROM products WHERE product_id = ?";
        String insVar = "INSERT INTO product_variants (product_id, sku, unit, packaging_type, price, quality_grade) VALUES (?, ?, 'kg', N'Gói tiêu chuẩn (1kg)', 50000, '1')";
        String insBatch = "INSERT INTO inventory_batches (variant_id, batch_code, quantity, expiry_date, received_date, status) VALUES (?, ?, 50, DATEADD(day, 30, GETDATE()), CAST(GETDATE() AS DATE), 'active')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkProd)) {
            psCheck.setInt(1, productId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    String sku = "SKU-P" + productId + "-STD";
                    int varId = 0;
                    try (PreparedStatement psV = conn.prepareStatement(insVar, Statement.RETURN_GENERATED_KEYS)) {
                        psV.setInt(1, productId);
                        psV.setString(2, sku);
                        psV.executeUpdate();
                        try (ResultSet rsGen = psV.getGeneratedKeys()) {
                            if (rsGen.next()) {
                                varId = rsGen.getInt(1);
                            }
                        }
                    }
                    if (varId > 0) {
                        try (PreparedStatement psB = conn.prepareStatement(insBatch)) {
                            psB.setInt(1, varId);
                            psB.setString(2, "BATCH-AUTO-" + varId);
                            psB.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException ignored) {
        }
    }

    public ProductVariant getVariantById(int variantId) throws SQLException {
        String sql = """
                SELECT pv.variant_id, pv.product_id, p.product_name, pv.sku, pv.unit,
                       pv.weight_value, pv.packaging_type, pv.quality_grade, pv.price, pv.created_at,
                       ISNULL(SUM(ib.quantity), 0) AS stock
                FROM product_variants pv
                JOIN products p ON pv.product_id = p.product_id
                LEFT JOIN inventory_batches ib ON pv.variant_id = ib.variant_id AND ib.status = 'active'
                WHERE pv.variant_id = ?
                GROUP BY pv.variant_id, pv.product_id, p.product_name, pv.sku, pv.unit,
                         pv.weight_value, pv.packaging_type, pv.quality_grade, pv.price, pv.created_at
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductVariant v = new ProductVariant();
                    v.setVariantId(rs.getInt("variant_id"));
                    v.setProductId(rs.getInt("product_id"));
                    v.setProductName(rs.getString("product_name"));
                    v.setSku(rs.getString("sku"));
                    v.setUnit(rs.getString("unit"));
                    double weight = rs.getDouble("weight_value");
                    if (!rs.wasNull()) {
                        v.setWeightValue(weight);
                    }
                    v.setPackagingType(rs.getString("packaging_type"));
                    v.setQualityGrade(rs.getString("quality_grade"));
                    v.setPrice(rs.getDouble("price"));
                    v.setCreatedAt(rs.getTimestamp("created_at"));
                    v.setStock(rs.getDouble("stock"));
                    return v;
                }
            }
        }
        return null;
    }

    public boolean isSkuExists(String sku, Integer excludeVariantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_variants WHERE LOWER(TRIM(sku)) = LOWER(TRIM(?))";
        if (excludeVariantId != null) {
            sql += " AND variant_id != ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            if (excludeVariantId != null) {
                ps.setInt(2, excludeVariantId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int countVariantsByProductId(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_variants WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean isVariantInOrders(int variantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_items WHERE variant_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean addVariant(ProductVariant v, double initialStock) throws SQLException {
        String insVariant = """
                INSERT INTO product_variants (product_id, sku, unit, weight_value, packaging_type, price, quality_grade)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String insBatch = """
                INSERT INTO inventory_batches (variant_id, batch_code, quantity, expiry_date, received_date, status)
                VALUES (?, ?, ?, ?, ?, 'active')
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int variantId = 0;
            try (PreparedStatement psV = conn.prepareStatement(insVariant, Statement.RETURN_GENERATED_KEYS)) {
                psV.setInt(1, v.getProductId());
                psV.setString(2, v.getSku().trim().toUpperCase());
                psV.setString(3, v.getUnit());
                if (v.getWeightValue() != null && v.getWeightValue() > 0) {
                    psV.setBigDecimal(4, BigDecimal.valueOf(v.getWeightValue()));
                } else {
                    psV.setNull(4, Types.DECIMAL);
                }
                psV.setString(5, v.getPackagingType().trim());
                psV.setBigDecimal(6, BigDecimal.valueOf(v.getPrice()));
                psV.setString(7, v.getQualityGrade() == null ? "1" : v.getQualityGrade());
                psV.executeUpdate();

                try (ResultSet keys = psV.getGeneratedKeys()) {
                    if (keys.next()) {
                        variantId = keys.getInt(1);
                    }
                }
            }

            if (variantId > 0 && initialStock > 0) {
                try (PreparedStatement psB = conn.prepareStatement(insBatch)) {
                    psB.setInt(1, variantId);
                    psB.setString(2, "BATCH-" + v.getSku().trim().toUpperCase());
                    psB.setBigDecimal(3, BigDecimal.valueOf(initialStock));
                    psB.setDate(4, Date.valueOf(LocalDate.now().plusMonths(3)));
                    psB.setDate(5, Date.valueOf(LocalDate.now()));
                    psB.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateVariant(ProductVariant v, double newStock) throws SQLException {
        String updVariant = """
                UPDATE product_variants
                SET sku = ?, unit = ?, weight_value = ?, packaging_type = ?, price = ?, quality_grade = ?
                WHERE variant_id = ?
                """;

        String updBatch = """
                UPDATE inventory_batches
                SET quantity = ?
                WHERE variant_id = ? AND status = 'active'
                """;

        String insBatch = """
                INSERT INTO inventory_batches (variant_id, batch_code, quantity, expiry_date, received_date, status)
                VALUES (?, ?, ?, ?, ?, 'active')
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psV = conn.prepareStatement(updVariant)) {
                psV.setString(1, v.getSku().trim().toUpperCase());
                psV.setString(2, v.getUnit());
                if (v.getWeightValue() != null && v.getWeightValue() > 0) {
                    psV.setBigDecimal(3, BigDecimal.valueOf(v.getWeightValue()));
                } else {
                    psV.setNull(3, Types.DECIMAL);
                }
                psV.setString(4, v.getPackagingType().trim());
                psV.setBigDecimal(5, BigDecimal.valueOf(v.getPrice()));
                psV.setString(6, v.getQualityGrade() == null ? "1" : v.getQualityGrade());
                psV.setInt(7, v.getVariantId());
                psV.executeUpdate();
            }

            // Update or add stock batch
            int batchUpdated = 0;
            try (PreparedStatement psB = conn.prepareStatement(updBatch)) {
                psB.setBigDecimal(1, BigDecimal.valueOf(newStock));
                psB.setInt(2, v.getVariantId());
                batchUpdated = psB.executeUpdate();
            }

            if (batchUpdated == 0 && newStock > 0) {
                try (PreparedStatement psInsB = conn.prepareStatement(insBatch)) {
                    psInsB.setInt(1, v.getVariantId());
                    psInsB.setString(2, "BATCH-" + v.getSku().trim().toUpperCase());
                    psInsB.setBigDecimal(3, BigDecimal.valueOf(newStock));
                    psInsB.setDate(4, Date.valueOf(LocalDate.now().plusMonths(3)));
                    psInsB.setDate(5, Date.valueOf(LocalDate.now()));
                    psInsB.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deleteVariant(int variantId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Delete adjustments and batches
            String delAdj = "DELETE FROM inventory_adjustments WHERE batch_id IN (SELECT batch_id FROM inventory_batches WHERE variant_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(delAdj)) {
                ps.setInt(1, variantId);
                ps.executeUpdate();
            }

            String delBatches = "DELETE FROM inventory_batches WHERE variant_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delBatches)) {
                ps.setInt(1, variantId);
                ps.executeUpdate();
            }

            // 2. Delete cart items for this variant
            String delCart = "DELETE FROM cart_items WHERE variant_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delCart)) {
                ps.setInt(1, variantId);
                ps.executeUpdate();
            }

            // 3. Delete variant
            String delV = "DELETE FROM product_variants WHERE variant_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delV)) {
                ps.setInt(1, variantId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
