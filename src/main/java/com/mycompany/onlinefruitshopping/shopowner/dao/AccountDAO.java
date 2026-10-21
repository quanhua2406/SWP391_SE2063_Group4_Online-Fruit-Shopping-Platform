package com.mycompany.onlinefruitshopping.shopowner.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;

public class AccountDAO {
    // Cung DB OnlineFruitShopping voi phan SWP391: Accounts -> users + user_roles.
    // Role module la N'ShopOwner' / N'DeliveryStaff' / N'Customer'.
    private static final String SELECT_ACCOUNT = """
        SELECT u.user_id, u.username, u.email, u.full_name, u.status, r.role_name, u.password_hash
        FROM users u
        JOIN user_roles ur ON ur.user_id = u.user_id
        JOIN roles r ON r.role_id = ur.role_id
        WHERE r.role_name IN (N'ShopOwner', N'DeliveryStaff', N'Customer')
        """;

    public Account login(String username, String password) throws SQLException {
        // Dang nhap bang username hoac email; mat khau luu BCrypt trong users.password_hash.
        String sql = SELECT_ACCOUNT + " AND (u.username = ? OR u.email = ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!"active".equalsIgnoreCase(rs.getString("status"))) continue;
                    if (matches(password, rs.getString("password_hash"))) return map(rs);
                }
            }
        }
        return null;
    }

    // Filter goi lai de tai khoan bi khoa khong tiep tuc dung session cu.
    public Account findActive(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ACCOUNT + " AND u.user_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("active".equalsIgnoreCase(rs.getString("status"))) return map(rs);
                }
                return null;
            }
        }
    }

    /**
     * Mat khau BCrypt bat dau bang "$2". Dua kiem tra nay giu du lieu mat khau ro
     * (van gap trong du lieu cu) van dang nhap duoc.
     */
    private boolean matches(String password, String stored) {
        if (stored == null || password == null) return false;
        if (stored.startsWith("$2")) {
            try {
                return BCrypt.checkpw(password, stored);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return stored.equals(password);
    }

    private Account map(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setId(rs.getInt("user_id"));
        String username = rs.getString("username");
        a.setUsername(username != null ? username : rs.getString("email"));
        a.setFullName(rs.getString("full_name"));
        a.setRole(rs.getString("role_name"));
        return a;
    }
}
