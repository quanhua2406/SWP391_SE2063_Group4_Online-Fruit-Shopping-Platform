package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {

    // =========================================================
    // GET ALL ROLES
    // =========================================================
    public List<Role> getAllRoles() throws SQLException {

        List<Role> roles = new ArrayList<>();

        String sql = """
                SELECT role_id, role_name
                FROM roles
                ORDER BY role_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Role role = new Role();

                role.setRoleId(rs.getInt("role_id"));
                role.setRoleName(rs.getString("role_name"));

                roles.add(role);
            }
        }

        return roles;
    }

    // =========================================================
    // GET ALL USERS WITH THEIR ROLES
    // =========================================================
    public List<UserRoleInfo> getAllUsersWithRoles()
            throws SQLException {

        List<UserRoleInfo> users = new ArrayList<>();

        String sql = """
                SELECT
                    u.user_id,
                    u.full_name,
                    u.email,
                    u.phone,
                    u.status,
                    r.role_id,
                    r.role_name
                FROM users u
                LEFT JOIN user_roles ur
                    ON u.user_id = ur.user_id
                LEFT JOIN roles r
                    ON ur.role_id = r.role_id
                ORDER BY u.user_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                UserRoleInfo user = mapUserRoleInfo(rs);

                users.add(user);
            }
        }

        return users;
    }

    // =========================================================
    // SEARCH USERS BY EMAIL KEYWORD
    // =========================================================
    public List<UserRoleInfo> searchUsersByEmail(String keyword)
            throws SQLException {

        List<UserRoleInfo> users = new ArrayList<>();

        String sql = """
                SELECT
                    u.user_id,
                    u.full_name,
                    u.email,
                    u.phone,
                    u.status,
                    r.role_id,
                    r.role_name
                FROM users u
                LEFT JOIN user_roles ur
                    ON u.user_id = ur.user_id
                LEFT JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE u.email LIKE ?
                ORDER BY u.user_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    UserRoleInfo user = mapUserRoleInfo(rs);

                    users.add(user);
                }
            }
        }

        return users;
    }

    // =========================================================
    // UPDATE USER ROLE
    // =========================================================
    public boolean updateUserRole(int userId, int roleId)
            throws SQLException {

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();

            conn.setAutoCommit(false);

            // Remove current role
            String deleteSql = """
                    DELETE FROM user_roles
                    WHERE user_id = ?
                    """;

            try (PreparedStatement ps =
                         conn.prepareStatement(deleteSql)) {

                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // Assign new role
            String insertSql = """
                    INSERT INTO user_roles (user_id, role_id)
                    VALUES (?, ?)
                    """;

            try (PreparedStatement ps =
                         conn.prepareStatement(insertSql)) {

                ps.setInt(1, userId);
                ps.setInt(2, roleId);

                ps.executeUpdate();
            }

            conn.commit();

            return true;

        } catch (SQLException e) {

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            throw e;

        } finally {

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }

    // =========================================================
    // CHECK ROLE EXISTS
    // =========================================================
    public boolean roleExists(int roleId)
            throws SQLException {

        String sql = """
                SELECT role_id
                FROM roles
                WHERE role_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roleId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // =========================================================
    // MAP RESULTSET -> UserRoleInfo
    // =========================================================
    private UserRoleInfo mapUserRoleInfo(ResultSet rs)
            throws SQLException {

        UserRoleInfo user = new UserRoleInfo();

        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getString("status"));

        int roleId = rs.getInt("role_id");

        if (rs.wasNull()) {
            user.setRoleId(0);
        } else {
            user.setRoleId(roleId);
        }

        user.setRoleName(rs.getString("role_name"));

        return user;
    }

    // =========================================================
    // ROLE CLASS
    // =========================================================
    public static class Role {

        private int roleId;
        private String roleName;

        public int getRoleId() {
            return roleId;
        }

        public void setRoleId(int roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
        
    

    }

    // =========================================================
    // USER ROLE INFO CLASS
    // =========================================================
    public static class UserRoleInfo {

        private int userId;
        private String fullName;
        private String email;
        private String phone;
        private String status;
        private int roleId;
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

        public int getRoleId() {
            return roleId;
        }

        public void setRoleId(int roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
        
    }
    public boolean isAdminUser(int userId) throws SQLException {

    String sql = """
            SELECT 1
            FROM users u
            INNER JOIN user_roles ur
                ON u.user_id = ur.user_id
            INNER JOIN roles r
                ON ur.role_id = r.role_id
            WHERE u.user_id = ?
              AND UPPER(r.role_name) = 'ADMIN'
            """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}
}