package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUserDAO {

    // =========================================================
    // GET ALL USERS
    // =========================================================
    public List<UserInfo> getAllUsers() throws SQLException {

        String sql = """
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                u.phone,
                u.status,
                ISNULL(r.role_name, 'GUEST') AS role_name
            FROM users u
            OUTER APPLY (
                SELECT TOP 1
                    r.role_name
                FROM user_roles ur
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE ur.user_id = u.user_id
                ORDER BY r.role_id
            ) r
            ORDER BY u.user_id
            """;

        return executeUserQuery(sql);
    }

    // =========================================================
    // SEARCH BY EMAIL
    // =========================================================
    public List<UserInfo> searchByEmail(
            String keyword) throws SQLException {

        String sql = """
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                u.phone,
                u.status,
                ISNULL(r.role_name, 'GUEST') AS role_name
            FROM users u
            OUTER APPLY (
                SELECT TOP 1
                    r.role_name
                FROM user_roles ur
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE ur.user_id = u.user_id
                ORDER BY r.role_id
            ) r
            WHERE u.email LIKE ?
            ORDER BY u.user_id
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {

                return mapUsers(rs);
            }
        }
    }

    // =========================================================
    // FILTER BY ROLE
    // =========================================================
    public List<UserInfo> filterByRole(
            String roleName) throws SQLException {

        String sql = """
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                u.phone,
                u.status,
                ISNULL(r.role_name, 'GUEST') AS role_name
            FROM users u
            OUTER APPLY (
                SELECT TOP 1
                    r.role_name
                FROM user_roles ur
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE ur.user_id = u.user_id
                ORDER BY r.role_id
            ) r
            WHERE UPPER(ISNULL(r.role_name, 'GUEST')) = UPPER(?)
            ORDER BY u.user_id
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roleName);

            try (ResultSet rs = ps.executeQuery()) {

                return mapUsers(rs);
            }
        }
    }

    // =========================================================
    // SEARCH + FILTER ROLE
    // =========================================================
    public List<UserInfo> searchByEmailAndRole(
            String keyword,
            String roleName) throws SQLException {

        String sql = """
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                u.phone,
                u.status,
                ISNULL(r.role_name, 'GUEST') AS role_name
            FROM users u
            OUTER APPLY (
                SELECT TOP 1
                    r.role_name
                FROM user_roles ur
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE ur.user_id = u.user_id
                ORDER BY r.role_id
            ) r
            WHERE u.email LIKE ?
              AND UPPER(ISNULL(r.role_name, 'GUEST')) = UPPER(?)
            ORDER BY u.user_id
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, roleName);

            try (ResultSet rs = ps.executeQuery()) {

                return mapUsers(rs);
            }
        }
    }

    // =========================================================
    // COMMON QUERY EXECUTOR
    // =========================================================
    private List<UserInfo> executeUserQuery(
            String sql) throws SQLException {

        List<UserInfo> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            users = mapUsers(rs);
        }

        return users;
    }

    // =========================================================
    // MAP RESULT
    // =========================================================
    private List<UserInfo> mapUsers(
            ResultSet rs) throws SQLException {

        List<UserInfo> users = new ArrayList<>();

        while (rs.next()) {

            UserInfo user = new UserInfo();

            user.setUserId(
                    rs.getInt("user_id")
            );

            user.setFullName(
                    rs.getString("full_name")
            );

            user.setEmail(
                    rs.getString("email")
            );

            user.setPhone(
                    rs.getString("phone")
            );

            user.setStatus(
                    rs.getString("status")
            );

            user.setRoleName(
                    rs.getString("role_name")
            );

            users.add(user);
        }

        return users;
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================
    public UserInfo getUserById(
            int userId) throws SQLException {

        String sql = """
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                u.phone,
                u.status,
                ISNULL(r.role_name, 'GUEST') AS role_name
            FROM users u
            OUTER APPLY (
                SELECT TOP 1
                    r.role_name
                FROM user_roles ur
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE ur.user_id = u.user_id
                ORDER BY r.role_id
            ) r
            WHERE u.user_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    UserInfo user = new UserInfo();

                    user.setUserId(
                            rs.getInt("user_id")
                    );

                    user.setFullName(
                            rs.getString("full_name")
                    );

                    user.setEmail(
                            rs.getString("email")
                    );

                    user.setPhone(
                            rs.getString("phone")
                    );

                    user.setStatus(
                            rs.getString("status")
                    );

                    user.setRoleName(
                            rs.getString("role_name")
                    );

                    return user;
                }
            }
        }

        return null;
    }

    // =========================================================
    // UPDATE USER INFORMATION
    // =========================================================
    public boolean updateUser(
            int userId,
            String fullName,
            String email,
            String phone) throws SQLException {

        String sql = """
            UPDATE users
            SET
                full_name = ?,
                email = ?,
                phone = ?
            WHERE user_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // CHECK DUPLICATE EMAIL
    // =========================================================
    public boolean emailExists(
            String email,
            int userId) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM users
            WHERE email = ?
              AND user_id <> ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // =========================================================
    // UPDATE STATUS
    // active -> locked
    // locked -> active
    // =========================================================
    public boolean updateStatus(
            int userId,
            String status) throws SQLException {

        String sql = """
            UPDATE users
            SET status = ?
            WHERE user_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // CHECK ADMIN
    // =========================================================
    public boolean isAdminUser(
            int userId) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM user_roles ur
            INNER JOIN roles r
                ON ur.role_id = r.role_id
            WHERE ur.user_id = ?
              AND UPPER(r.role_name) = 'ADMIN'
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // =========================================================
    // USER INFO
    // =========================================================
    public static class UserInfo {

        private int userId;
        private String fullName;
        private String email;
        private String phone;
        private String status;
        private String roleName;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }
}