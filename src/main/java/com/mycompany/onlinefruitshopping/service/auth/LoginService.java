package com.mycompany.onlinefruitshopping.service.auth;

import com.mycompany.onlinefruitshopping.dao.auth.UserDAO;
import com.mycompany.onlinefruitshopping.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * Handles authentication logic for the login screen.
 *
 * <p>The login screen lets the user choose which role they want to sign in
 * as (Customer, Shop Owner, Delivery Staff, Admin). Login only succeeds when
 * the credentials are valid AND the account really owns the selected role.
 */
public class LoginService {
    public boolean isSessionValid(User user) throws SQLException {
        return user != null && userDAO.isActiveWithRole(user.getUserID(), user.getRoleName());
    }


    // =========================================================
    // RESULT TYPES
    // =========================================================

    public enum Status {
        SUCCESS,
        INVALID_CREDENTIALS,
        ACCOUNT_LOCKED,
        ROLE_MISMATCH
    }

    public static class LoginResult {

        private final Status status;
        private final User user;
        private final String message;

        public LoginResult(Status status,
                           User user,
                           String message) {
            this.status = status;
            this.user = user;
            this.message = message;
        }

        public Status getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }
    }

    // =========================================================
    // FIELDS
    // =========================================================

    private final UserDAO userDAO = new UserDAO();

    // =========================================================
    // LOGIN
    // =========================================================

    public LoginResult login(String email,
                             String password,
                             String selectedRole)
            throws SQLException {

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {

            return new LoginResult(
                    Status.INVALID_CREDENTIALS,
                    null,
                    "Email hoặc mật khẩu không đúng."
            );
        }

        // -----------------------------------------------------
        // Find account (including locked accounts)
        // -----------------------------------------------------

        User user =
                userDAO.getUserByEmailForLogin(email.trim());

        boolean passwordMatches = false;

        if (user != null && user.getPasswordHash() != null) {
            try {
                passwordMatches = BCrypt.checkpw(
                        password,
                        user.getPasswordHash());
            } catch (IllegalArgumentException e) {
                // Fallback: password in DB is raw (not BCrypt-hashed yet)
                passwordMatches =
                        password.equals(user.getPasswordHash());
            }
        }

        if (user == null || !passwordMatches) {

            return new LoginResult(
                    Status.INVALID_CREDENTIALS,
                    null,
                    "Email hoặc mật khẩu không đúng."
            );
        }

        // -----------------------------------------------------
        // Blocked account
        // -----------------------------------------------------

        if (!"active".equalsIgnoreCase(user.getStatus())) {

            return new LoginResult(
                    Status.ACCOUNT_LOCKED,
                    null,
                    "Tài khoản của bạn đã bị khóa."
            );
        }

        // -----------------------------------------------------
        // Role must match the selected login type
        // -----------------------------------------------------

        if (!roleMatches(
                user.getRoleName(),
                selectedRole)) {

            return new LoginResult(
                    Status.ROLE_MISMATCH,
                    null,
                    "Tài khoản này được đăng ký với vai trò "
                            + displayRole(user.getRoleName())
                            + ". Vui lòng chọn đúng "
                            + "loại đăng nhập."
            );
        }

        return new LoginResult(
                Status.SUCCESS,
                user,
                null
        );
    }

    // =========================================================
    // ROLE HELPERS
    // =========================================================

    /**
     * Normalizes a role name so that values like {@code ShopOwner},
     * {@code SHOP_OWNER} and {@code Shop Owner} all compare equal.
     */
    public static String normalizeRole(String role) {

        return com.mycompany.onlinefruitshopping.utils
                .ValidationUtil.normalizeRoleName(role);
    }

    public static boolean roleMatches(String userRole,
                                      String selectedRole) {

        String normalizedUser = normalizeRole(userRole);
        String normalizedSelected = normalizeRole(selectedRole);

        return !normalizedUser.isEmpty()
                && normalizedUser.equals(normalizedSelected);
    }

    /**
     * Friendly label used inside error messages.
     */
    public static String displayRole(String role) {

        String normalized = normalizeRole(role);

        return switch (normalized) {
            case "CUSTOMER" -> "Khách hàng";
            case "SHOPOWNER" -> "Chủ cửa hàng";
            case "DELIVERYSTAFF" -> "Nhân viên giao hàng";
            case "ADMIN" -> "Quản trị viên";
            default -> role == null ? "người dùng" : role;
        };
    }

    /**
     * Landing page for a role after a successful login.
     */
    public static String dashboardPath(String role) {

        return switch (normalizeRole(role)) {
            case "ADMIN" -> "/admin/dashboard";
            case "SHOPOWNER" -> "/store-owner/dashboard";
            case "DELIVERYSTAFF" -> "/delivery/dashboard";
            case "CUSTOMER" -> "/customer/dashboard";
            default -> "/home";
        };
    }
}
