package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}