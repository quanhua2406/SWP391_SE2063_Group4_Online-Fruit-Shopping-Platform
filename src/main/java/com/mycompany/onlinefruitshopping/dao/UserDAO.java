package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // =========================================================
    // CHECK EMAIL EXISTS
    // =========================================================

    public boolean emailExists(String email) throws SQLException {

        String sql = """
                SELECT user_id
                FROM users
                WHERE email = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    // =========================================================
    // REGISTER USER
    // =========================================================

    public boolean registerUser(
            String fullName,
            String email,
            String passwordHash,
            String phone,
            String address)
            throws SQLException {

        String insertUser = """
                INSERT INTO users
                (
                    full_name,
                    email,
                    password_hash,
                    phone,
                    status
                )
                VALUES (?, ?, ?, ?, 'active')
                """;

        String insertRole = """
                INSERT INTO user_roles
                (
                    user_id,
                    role_id
                )
                SELECT ?, role_id
                FROM roles
                WHERE role_name = 'Customer'
                """;

        Connection conn = null;

        try {

            conn = DBConnection.getConnection();

            conn.setAutoCommit(false);


            // -------------------------------------------------
            // INSERT USER
            // -------------------------------------------------

            int userId;

            try (PreparedStatement ps =
                         conn.prepareStatement(
                                 insertUser,
                                 java.sql.Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, passwordHash);
                ps.setString(4, phone);

                int affected = ps.executeUpdate();

                if (affected == 0) {

                    conn.rollback();

                    return false;
                }


                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (!rs.next()) {

                        conn.rollback();

                        return false;
                    }

                    userId = rs.getInt(1);
                }
            }


            // -------------------------------------------------
            // ASSIGN CUSTOMER ROLE
            // -------------------------------------------------

            try (PreparedStatement ps =
                         conn.prepareStatement(insertRole)) {

                ps.setInt(1, userId);

                int affected = ps.executeUpdate();

                if (affected == 0) {

                    conn.rollback();

                    return false;
                }
            }


            // -------------------------------------------------
            // COMMIT
            // -------------------------------------------------

            conn.commit();

            return true;


        } catch (SQLException e) {

            if (conn != null) {

                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            throw e;


        } finally {

            if (conn != null) {

                try {

                    conn.setAutoCommit(true);

                    conn.close();

                } catch (SQLException ignored) {
                }
            }
        }
    }


    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    public User getUserByEmail(String email)
            throws SQLException {

        String sql = """
                SELECT TOP 1
                    u.user_id,
                    u.full_name,
                    u.email,
                    u.password_hash,
                    u.phone,
                    u.status,
                    r.role_id,
                    r.role_name
                FROM users u
                INNER JOIN user_roles ur
                    ON u.user_id = ur.user_id
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE u.email = ?
                  AND u.status = 'active'
                ORDER BY r.role_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = new User();

                    user.setUserID(
                            rs.getInt("user_id")
                    );

                    user.setFullName(
                            rs.getString("full_name")
                    );

                    user.setEmail(
                            rs.getString("email")
                    );

                    user.setPasswordHash(
                            rs.getString("password_hash")
                    );

                    user.setPhone(
                            rs.getString("phone")
                    );

                    user.setRoleID(
                            rs.getInt("role_id")
                    );

                    user.setRoleName(
                            rs.getString("role_name")
                    );

                    user.setStatus(
                            rs.getString("status")
                    );

                    user.setIsMember(false);

                    return user;
                }
            }
        }

        return null;
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public User getUserByLogin(
            String email,
            String password)
            throws SQLException {

        User user = getUserByEmail(email);

        if (user == null) {
            return null;
        }


        // Check BCrypt password
        if (BCrypt.checkpw(
                password,
                user.getPasswordHash())) {

            return user;
        }

        return null;
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    public User getUserById(int userId)
            throws SQLException {

        String sql = """
                SELECT TOP 1
                    u.user_id,
                    u.full_name,
                    u.email,
                    u.password_hash,
                    u.phone,
                    u.status,
                    r.role_id,
                    r.role_name
                FROM users u
                INNER JOIN user_roles ur
                    ON u.user_id = ur.user_id
                INNER JOIN roles r
                    ON ur.role_id = r.role_id
                WHERE u.user_id = ?
                ORDER BY r.role_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = new User();

                    user.setUserID(
                            rs.getInt("user_id")
                    );

                    user.setFullName(
                            rs.getString("full_name")
                    );

                    user.setEmail(
                            rs.getString("email")
                    );

                    user.setPasswordHash(
                            rs.getString("password_hash")
                    );

                    user.setPhone(
                            rs.getString("phone")
                    );

                    user.setRoleID(
                            rs.getInt("role_id")
                    );

                    user.setRoleName(
                            rs.getString("role_name")
                    );

                    user.setStatus(
                            rs.getString("status")
                    );

                    user.setIsMember(false);

                    return user;
                }
            }
        }

        return null;
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    public boolean updateProfile(
            int userId,
            String fullName,
            String phone)
            throws SQLException {

        String sql = """
                UPDATE users
                SET full_name = ?,
                    phone = ?
                WHERE user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;
        }
    }


    // =========================================================
    // SAVE RESET OTP
    // =========================================================

    public boolean saveResetOTP(
            String email,
            String otp)
            throws SQLException {

        String sql = """
                UPDATE users
                SET reset_otp = ?,
                    reset_otp_expiry =
                        DATEADD(MINUTE, 5, GETDATE())
                WHERE email = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, otp);
            ps.setString(2, email);

            return ps.executeUpdate() > 0;
        }
    }


    // =========================================================
    // VERIFY RESET OTP
    // =========================================================

    public boolean verifyResetOTP(
            String email,
            String otp)
            throws SQLException {

        String sql = """
                SELECT user_id
                FROM users
                WHERE email = ?
                  AND reset_otp = ?
                  AND reset_otp_expiry > GETDATE()
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, otp);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }


    // =========================================================
    // RESET PASSWORD
    // =========================================================

    public boolean resetPassword(
            String email,
            String newPassword)
            throws SQLException {

        String passwordHash =
                BCrypt.hashpw(
                        newPassword,
                        BCrypt.gensalt(12)
                );

        String sql = """
                UPDATE users
                SET password_hash = ?,
                    reset_otp = NULL,
                    reset_otp_expiry = NULL
                WHERE email = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, passwordHash);
            ps.setString(2, email);

            return ps.executeUpdate() > 0;
        }
    }
}