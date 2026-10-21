package com.mycompany.onlinefruitshopping.shopowner.dao;

import com.mycompany.onlinefruitshopping.shopowner.utils.DBConnection;
import java.sql.*;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;

public class AccountDAO {
    private static final String SELECT_ACCOUNT = """
        SELECT a.account_id, a.username, a.full_name, r.role_name, a.password
        FROM Accounts a JOIN Roles r ON r.role_id = a.role_id
        WHERE a.status = N'Active'
          AND r.role_name IN (N'ShopOwner', N'Shipper', N'Customer')
        """;

    public Account login(String username, String password) throws SQLException {
        // Database đính kèm lưu mật khẩu demo dạng rõ (123456).
        // So sánh mật khẩu bằng Java để phân biệt hoa/thường, không theo collation SQL.
        String sql = SELECT_ACCOUNT + " AND a.username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("password").equals(password)) return map(rs);
            }
        }
        return null;
    }

    // Filter gọi lại để tài khoản bị khóa không tiếp tục dùng session cũ.
    public Account findActive(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ACCOUNT + " AND a.account_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setId(rs.getInt("account_id"));
        a.setUsername(rs.getString("username"));
        a.setFullName(rs.getString("full_name"));
        a.setRole(rs.getString("role_name"));
        return a;
    }
}
