package com.mycompany.onlinefruitshopping.dao.product;

import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // =========================================================
    // READ
    // =========================================================

    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, parent_id, category_name FROM categories ORDER BY category_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapCategory(rs));
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
                    return mapCategory(rs);
                }
            }
        }
        return null;
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category cat = new Category();
        cat.setCategoryId(rs.getInt("category_id"));

        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            cat.setParentId(parentId);
        }

        cat.setCategoryName(rs.getString("category_name"));
        return cat;
    }

    // =========================================================
    // CREATE / UPDATE / DELETE
    // =========================================================

    public boolean createCategory(String name, Integer parentId)
            throws SQLException {

        String sql = "INSERT INTO categories (parent_id, category_name) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            if (parentId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, parentId);
            }

            ps.setString(2, name);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateCategory(int categoryId, String name, Integer parentId)
            throws SQLException {

        String sql = "UPDATE categories SET category_name = ?, parent_id = ? WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            if (parentId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, parentId);
            }

            ps.setInt(3, categoryId);

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

    // =========================================================
    // CONSTRAINTS
    // =========================================================

    public int countProducts(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        return count(sql, categoryId);
    }

    public int countChildren(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE parent_id = ?";
        return count(sql, categoryId);
    }

    private int count(String sql, int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean categoryNameExists(String name, int excludeId)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM categories "
                + "WHERE category_name = ? AND category_id <> ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Follows the parent chain upwards from {@code categoryId} and
     * reports whether {@code possibleAncestorId} is reached. Used to
     * stop a category from being made a child of one of its own
     * descendants (which would create a cycle in the tree).
     */
    public boolean isDescendantOf(int categoryId, int possibleAncestorId)
            throws SQLException {

        String sql = "SELECT parent_id FROM categories WHERE category_id = ?";

        int current = categoryId;
        // The tree is tiny; a hard cap keeps a pre-existing cycle from
        // looping forever.
        for (int guard = 0; guard < 100 && current > 0; guard++) {

            if (current == possibleAncestorId) {
                return true;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, current);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    int parent = rs.getInt("parent_id");
                    current = rs.wasNull() ? 0 : parent;
                }
            }
        }

        return false;
    }
}
