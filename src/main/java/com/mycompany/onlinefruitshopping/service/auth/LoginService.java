package com.mycompany.onlinefruitshopping.service.auth;

import com.mycompany.onlinefruitshopping.dao.auth.UserDAO;
import com.mycompany.onlinefruitshopping.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class LoginService {

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

    private final UserDAO userDAO;

    public LoginService() {
        this(new UserDAO());
    }

    LoginService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public LoginResult login(String email,
                             String password,
                             String requestedRole) {

        try {
            if (email == null || password == null || requestedRole == null) {
                return new LoginResult(Status.INVALID_CREDENTIALS, null,
                        "Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ email, máº­t kháº©u vÃ  vai trÃ².");
            }

            email = email.trim().toLowerCase();

            if (email.isEmpty() || password.isEmpty()) {
                return new LoginResult(Status.INVALID_CREDENTIALS, null,
                        "Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ email, máº­t kháº©u vÃ  vai trÃ².");
            }

            String normalizedRequestedRole =
                    normalizeRole(requestedRole);

            if (normalizedRequestedRole.isEmpty()) {
                return new LoginResult(Status.INVALID_CREDENTIALS, null,
                        "Vui lÃ²ng chá»n vai trÃ² há»£p lá»‡.");
            }

            User user = userDAO.findByEmail(email);

            if (user == null) {
                return new LoginResult(Status.INVALID_CREDENTIALS, null,
                        "Email hoáº·c máº­t kháº©u khÃ´ng Ä‘Ãºng.");
            }

            if (user.getPasswordHash() == null
                    || !BCrypt.checkpw(password, user.getPasswordHash())) {
                return new LoginResult(Status.INVALID_CREDENTIALS, null,
                        "Email hoáº·c máº­t kháº©u khÃ´ng Ä‘Ãºng.");
            }

            if (user.getStatus() != null
                    && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                return new LoginResult(Status.ACCOUNT_LOCKED, null,
                        "TÃ i khoáº£n Ä‘Ã£ bá»‹ khÃ³a hoáº·c chÆ°a Ä‘Æ°á»£c kÃ­ch hoáº¡t.");
            }

            String actualRole = normalizeRole(user.getRoleName());

            if (!actualRole.equals(normalizedRequestedRole)) {
                return new LoginResult(Status.ROLE_MISMATCH, null,
                        "TÃ i khoáº£n nÃ y khÃ´ng thuá»™c vai trÃ² Ä‘Ã£ chá»n.");
            }

            if (user.getRoleName() == null || user.getRoleName().isBlank()) {
                user.setRoleName(actualRole);
            }

            return new LoginResult(Status.SUCCESS, user, "ÄÄƒng nháº­p thÃ nh cÃ´ng.");
        } catch (SQLException e) {
            throw new RuntimeException("KhÃ´ng thá»ƒ Ä‘Äƒng nháº­p.", e);
        }
    }

    public static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        String normalized = role.trim()
                .replace(" ", "")
                .replace("_", "")
                .toUpperCase();

        if ("DELIVERY".equals(normalized)
                || "SHIPPER".equals(normalized)) {
            return "DELIVERYSTAFF";
        }

        return normalized;
    }

    public static boolean roleMatches(String role1, String role2) {
        String r1 = normalizeRole(role1);
        String r2 = normalizeRole(role2);

        if (r1.isEmpty() || r2.isEmpty()) {
            return false;
        }

        if (r1.equals(r2)) {
            return true;
        }

        boolean r1IsDelivery = "DELIVERYSTAFF".equals(r1);
        boolean r2IsDelivery = "DELIVERYSTAFF".equals(r2);

        return r1IsDelivery && r2IsDelivery;
    }

    public static String dashboardPath(String role) {

        return switch (normalizeRole(role)) {
            case "ADMIN" -> "/admin/dashboard";
            case "SHOPOWNER" -> "/store-owner/dashboard";
            case "DELIVERYSTAFF" -> "/delivery/dashboard";
            case "CUSTOMER" -> "/customer/dashboard";
            default -> "/home";
        };
    }

    public boolean isSessionValid(User user) throws SQLException {
        return user != null
                && userDAO.isActiveWithRole(
                        user.getUserID(),
                        user.getRoleName()
                );
    }
}