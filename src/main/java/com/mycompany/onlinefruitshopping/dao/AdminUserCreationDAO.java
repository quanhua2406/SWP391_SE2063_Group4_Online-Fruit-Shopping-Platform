
package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class AdminUserCreationDAO {

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean createUser(String fullName,
                              String email,
                              String passwordHash,
                              String phone,
                              String requestedRole) throws SQLException {

        String roleName = normalizeRole(requestedRole);

        if (roleName == null) {
            throw new IllegalArgumentException("Invalid role.");
        }

        String findRoleSql =
                "SELECT role_id FROM roles " +
                "WHERE UPPER(REPLACE(role_name, '_', '')) = ?";

        String insertUserSql =
                "INSERT INTO users " +
                "(full_name, email, password_hash, phone, status) " +
                "VALUES (?, ?, ?, ?, 'active')";

        String insertUserRoleSql =
                "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            boolean oldAutoCommit = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                int roleId;

                try (PreparedStatement ps = conn.prepareStatement(findRoleSql)) {
                    ps.setString(1, roleName);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Role not found: " + roleName
                            );
                        }

                        roleId = rs.getInt("role_id");
                    }
                }

                int userId;

                try (PreparedStatement ps = conn.prepareStatement(
                        insertUserSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, fullName);
                    ps.setString(2, email);
                    ps.setString(3, passwordHash);

                    if (phone == null || phone.isBlank()) {
                        ps.setNull(4, Types.VARCHAR);
                    } else {
                        ps.setString(4, phone);
                    }

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Unable to create user.");
                    }

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Unable to get user ID.");
                        }

                        userId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps =
                             conn.prepareStatement(insertUserRoleSql)) {

                    ps.setInt(1, userId);
                    ps.setInt(2, roleId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {
                    // Connection is closing.
                }
            }
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        return switch (role.trim().toUpperCase()) {
            case "CUSTOMER" -> "CUSTOMER";
            case "SHOPOWNER", "SHOP_OWNER" -> "SHOPOWNER";
            case "DELIVERYSTAFF", "DELIVERY_STAFF" -> "DELIVERYSTAFF";
            case "ADMIN" -> "ADMIN";
            case "GUEST" -> "GUEST";
            default -> null;
        };
    }
}
