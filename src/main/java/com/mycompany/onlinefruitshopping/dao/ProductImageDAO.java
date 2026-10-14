package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.ProductImage;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductImageDAO {

    public List<ProductImage> getImagesByProductId(int productId) throws SQLException {
        List<ProductImage> list = new ArrayList<>();
        String sql = "SELECT image_id, product_id, image_url, sort_order FROM product_images WHERE product_id = ? ORDER BY sort_order ASC, image_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductImage img = new ProductImage();
                    img.setImageId(rs.getInt("image_id"));
                    img.setProductId(rs.getInt("product_id"));
                    img.setImageUrl(rs.getString("image_url"));
                    img.setSortOrder(rs.getInt("sort_order"));
                    list.add(img);
                }
            }
        }
        return list;
    }

    public ProductImage getImageById(int imageId) throws SQLException {
        String sql = "SELECT image_id, product_id, image_url, sort_order FROM product_images WHERE image_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductImage img = new ProductImage();
                    img.setImageId(rs.getInt("image_id"));
                    img.setProductId(rs.getInt("product_id"));
                    img.setImageUrl(rs.getString("image_url"));
                    img.setSortOrder(rs.getInt("sort_order"));
                    return img;
                }
            }
        }
        return null;
    }

    public int countImagesByProductId(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_images WHERE product_id = ?";
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

    public boolean addImage(ProductImage img) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, image_url, sort_order) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, img.getProductId());
            ps.setString(2, img.getImageUrl().trim());
            ps.setInt(3, img.getSortOrder());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteImage(int imageId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE image_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean setPrimaryImage(int productId, int imageId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Step 1: Set all other images of this product to sortOrder > 1
            String sqlDemote = "UPDATE product_images SET sort_order = 2 WHERE product_id = ? AND sort_order = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlDemote)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            // Step 2: Set the selected image to sortOrder = 1
            String sqlPromote = "UPDATE product_images SET sort_order = 1 WHERE image_id = ? AND product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlPromote)) {
                ps.setInt(1, imageId);
                ps.setInt(2, productId);
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
