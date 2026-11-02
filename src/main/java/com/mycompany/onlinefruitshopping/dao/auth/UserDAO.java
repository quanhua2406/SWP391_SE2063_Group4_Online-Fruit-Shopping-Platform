package com.mycompany.onlinefruitshopping.dao.auth;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final com.mycompany.onlinefruitshopping.utils.ConnectionProvider connections;

    public UserDAO() { this(DBConnection::getConnection); }

    UserDAO(com.mycompany.onlinefruitshopping.utils.ConnectionProvider connections) {
        this.connections = java.util.Objects.requireNonNull(connections);
    }

    /** Revalidate the principal for the delivery/coupon/promotion module. */
    public boolean isActiveWithRole(int userId, String roleName) throws SQLException {
        String role = com.mycompany.onlinefruitshopping.utils.ValidationUtil.normalizeRoleName(roleName);
        if (userId <= 0 || role.isEmpty()) return false;
        String sql = """
            SELECT 1
            FROM users u
            JOIN user_roles ur ON ur.user_id = u.user_id
            JOIN roles r ON r.role_id = ur.role_id
            WHERE u.user_id = ? AND LOWER(u.status) = 'active'
              AND REPLACE(REPLACE(UPPER(r.role_name), ' ', ''), '_', '') = ?
            """;
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, role);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

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

    /**
     * Checks whether a role with the given name exists.
     * Matching ignores case, spaces and underscores, so
     * {@code ShopOwner}, {@code SHOP_OWNER} and {@code Shop Owner}
     * all match the same role row.
     */
    public boolean roleExistsByName(String roleName)
            throws SQLException {

        String sql = """
                SELECT role_id
                FROM roles
                WHERE REPLACE(REPLACE(UPPER(role_name), ' ', ''), '_', '') = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,
                    com.mycompany.onlinefruitshopping.utils
                            .ValidationUtil
                            .normalizeRoleName(roleName));

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
            String address,
            String roleName)
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
                WHERE REPLACE(REPLACE(UPPER(role_name), ' ', ''), '_', '') = ?
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
            // ASSIGN THE CHOSEN ROLE
            // -------------------------------------------------

            try (PreparedStatement ps =
                         conn.prepareStatement(insertRole)) {

                ps.setInt(1, userId);
                ps.setString(2,
                        com.mycompany.onlinefruitshopping.utils
                                .ValidationUtil
                                .normalizeRoleName(roleName));

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
// GET USER BY EMAIL FOR LOGIN
// =========================================================

public User getUserByEmailForLogin(String email)
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
    // CLEAR RESET OTP (CONSUME)
    // =========================================================

    /**
     * Removes the stored reset OTP so it cannot be used again
     * (OTP is valid for a single successful verification only).
     */
    public boolean clearResetOTP(String email)
            throws SQLException {

        String sql = """
                UPDATE users
                SET reset_otp = NULL,
                    reset_otp_expiry = NULL
                WHERE email = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            return ps.executeUpdate() > 0;
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
    public List<User> getAllUsers() throws SQLException {

    List<User> users = new ArrayList<>();

    String sql = """
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                u.password_hash,
                u.phone,
                u.status,
                r.role_id,
                r.role_name
            FROM users u
            LEFT JOIN user_roles ur
                ON u.user_id = ur.user_id
            LEFT JOIN roles r
                ON ur.role_id = r.role_id
            ORDER BY u.user_id DESC
            """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            User user = new User();

            user.setUserID(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setStatus(rs.getString("status"));
            user.setRoleID(rs.getInt("role_id"));
            user.setRoleName(rs.getString("role_name"));

            users.add(user);
        }
    }

    return users;
}
}
