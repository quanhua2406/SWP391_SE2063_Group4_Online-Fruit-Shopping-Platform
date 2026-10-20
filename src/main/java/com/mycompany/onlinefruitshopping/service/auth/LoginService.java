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
                    "Invalid email or password."
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
                    "Invalid email or password."
            );
        }

        // -----------------------------------------------------
        // Blocked account
        // -----------------------------------------------------

        if ("locked".equalsIgnoreCase(user.getStatus())) {

            return new LoginResult(
                    Status.ACCOUNT_LOCKED,
                    null,
                    "Your account has been blocked."
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
                    "This account is registered as a "
                            + displayRole(user.getRoleName())
                            + ". Please choose the correct "
                            + "login type."
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
            case "CUSTOMER" -> "Customer";
            case "SHOPOWNER" -> "Shop Owner";
            case "DELIVERYSTAFF" -> "Delivery Staff";
            case "ADMIN" -> "Admin";
            default -> role == null ? "user" : role;
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
