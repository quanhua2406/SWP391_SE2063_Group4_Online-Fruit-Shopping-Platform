package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, parent_id, category_name FROM categories ORDER BY category_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category cat = new Category();
                cat.setCategoryId(rs.getInt("category_id"));
                int parentId = rs.getInt("parent_id");
                if (!rs.wasNull()) {
                    cat.setParentId(parentId);
                }
                cat.setCategoryName(rs.getString("category_name"));
                list.add(cat);
            }
        }
        return list;
    }

    public List<Category> getAllCategoriesWithProductCount() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = """
                SELECT c.category_id, c.parent_id, c.category_name, p_parent.category_name AS parent_name,
                       COUNT(p.product_id) AS product_count
                FROM categories c
                LEFT JOIN categories p_parent ON c.parent_id = p_parent.category_id
                LEFT JOIN products p ON c.category_id = p.category_id
                GROUP BY c.category_id, c.parent_id, c.category_name, p_parent.category_name
                ORDER BY c.category_id ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category cat = new Category();
                cat.setCategoryId(rs.getInt("category_id"));
                int parentId = rs.getInt("parent_id");
                if (!rs.wasNull()) {
                    cat.setParentId(parentId);
                }
                cat.setCategoryName(rs.getString("category_name"));
                cat.setParentName(rs.getString("parent_name"));
                cat.setProductCount(rs.getInt("product_count"));
                list.add(cat);
            }
        }
        return list;
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        String sql = "SELECT category_id, parent_id, category_name FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category cat = new Category();
                    cat.setCategoryId(rs.getInt("category_id"));
                    int parentId = rs.getInt("parent_id");
                    if (!rs.wasNull()) {
                        cat.setParentId(parentId);
                    }
                    cat.setCategoryName(rs.getString("category_name"));
                    return cat;
                }
            }
        }
        return null;
    }

    public boolean isCategoryNameExists(String name, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(TRIM(category_name)) = LOWER(TRIM(?))";
        if (excludeId != null) {
            sql += " AND category_id != ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int countProductsByCategoryId(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countSubCategories(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE parent_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (parent_id, category_name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (category.getParentId() != null && category.getParentId() > 0) {
                ps.setInt(1, category.getParentId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, category.getCategoryName().trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET parent_id = ?, category_name = ? WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (category.getParentId() != null && category.getParentId() > 0) {
                ps.setInt(1, category.getParentId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, category.getCategoryName().trim());
            ps.setInt(3, category.getCategoryId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        }
    }
}
