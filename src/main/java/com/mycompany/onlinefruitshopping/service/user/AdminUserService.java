package com.mycompany.onlinefruitshopping.service.user;

import com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO;
import com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO.UserInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Business logic of the administrator's user management
 * (feature 18.0 / 2.0): listing, searching, editing, blocking and
 * unblocking accounts.
 *
 * <p>The controller only reads the request and renders the response.
 * Every rule (field formats, duplicate email, admin accounts must not
 * be blocked ...) and the choice of the message code live here, on
 * top of {@link AdminUserDAO}.</p>
 */
public class AdminUserService {

    /** Roles the administrator may filter by. */
    public static final Set<String> ALLOWED_ROLES = Set.of(
            "ADMIN",
            "CUSTOMER",
            "SHOPOWNER",
            "DELIVERYSTAFF",
            "GUEST"
    );

    /** {@code users.full_name} is NVARCHAR(100). */
    private static final int MAX_FULL_NAME = 100;

    /** {@code users.email} is NVARCHAR(150). */
    private static final int MAX_EMAIL = 150;

    /** {@code users.phone} is NVARCHAR(20). */
    private static final int MAX_PHONE = 20;

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@"
            + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
            + "[A-Za-z0-9])?(?:\\.[A-Za-z0-9]"
            + "(?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$";

    private static final String PHONE_REGEX = "^0[0-9]{9,10}$";

    private final AdminUserDAO userDAO = new AdminUserDAO();

    // =========================================================
    // RESULT TYPES
    // =========================================================

    /**
     * Outcome of an edit: either {@code success}, or a validation
     * {@code error} message to show back on the form.
     */
    public static class UpdateResult {

        private final boolean success;
        private final String error;

        private UpdateResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        static UpdateResult ok() {
            return new UpdateResult(true, null);
        }

        public static UpdateResult error(String message) {
            return new UpdateResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }

    /** The values kept on the form when validation fails. */
    public static class UserForm {

        private final String fullName;
        private final String email;
        private final String phone;

        public UserForm(String fullName, String email, String phone) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }

    // =========================================================
    // LIST / READ
    // =========================================================

    public List<UserInfo> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public UserInfo getUserById(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }

    /**
     * Filters the account list by keyword and role.
     *
     * @return the matching accounts, or {@code null} when the role is
     *         not a known role
     */
    public List<UserInfo> searchUsers(String keywordRaw, String roleRaw)
            throws SQLException {

        String keyword = trim(keywordRaw);
        String role = trim(roleRaw).toUpperCase(Locale.ROOT);

        if (role.isEmpty()) {
            role = "ALL";
        }

        if ("ALL".equals(role)) {
            return keyword.isEmpty()
                    ? userDAO.getAllUsers()
                    : userDAO.searchByEmail(keyword);
        }

        if (!ALLOWED_ROLES.contains(role)) {
            return null;
        }

        return keyword.isEmpty()
                ? userDAO.filterByRole(role)
                : userDAO.searchByEmailAndRole(keyword, role);
    }

    // =========================================================
    // EDIT
    // =========================================================

    /**
     * Validates and saves the editable fields of an account.
     *
     * <p>The caller must have checked that the account exists; a
     * non-existing id makes the update fail and come back as a
     * generic error.</p>
     */
    public UpdateResult updateUser(int userId,
                                   UserForm form) throws SQLException {

        String fullName = trim(form.getFullName());
        String email = trim(form.getEmail());
        String phone = trim(form.getPhone());

        // ---- Full name ----
        if (fullName.isEmpty()) {
            return UpdateResult.error("Full Name is required.");
        }

        if (fullName.length() < 2 || fullName.length() > MAX_FULL_NAME) {
            return UpdateResult.error(
                    "Full Name must contain 2 to 100 characters.");
        }

        if (!fullName.matches("[\\p{L}\\p{M} .'-]+")) {
            return UpdateResult.error(
                    "Full Name contains invalid characters.");
        }

        // ---- Email ----
        if (email.isEmpty()) {
            return UpdateResult.error("Email is required.");
        }

        if (email.length() > MAX_EMAIL || !email.matches(EMAIL_REGEX)) {
            return UpdateResult.error(
                    "Please enter a valid email address.");
        }

        if (userDAO.emailExists(email, userId)) {
            return UpdateResult.error(
                    "This email is already used by another account.");
        }

        // ---- Phone ----
        if (phone.isEmpty()) {
            return UpdateResult.error("Phone is required.");
        }

        if (phone.length() > MAX_PHONE || !phone.matches(PHONE_REGEX)) {
            return UpdateResult.error(
                    "Phone must start with 0 and contain 10 or 11 digits.");
        }

        boolean updated = userDAO.updateUser(userId, fullName, email, phone);

        return updated
                ? UpdateResult.ok()
                : UpdateResult.error("Update failed. Please try again.");
    }

    // =========================================================
    // BLOCK / UNBLOCK
    // =========================================================

    /**
     * Changes an account status.
     *
     * @param status {@code locked} (block) or {@code active} (unblock)
     * @return a message code understood by the user screens
     */
    public String changeStatus(int userId, String status) throws SQLException {

        String normalized = trim(status);

        if (!"locked".equals(normalized) && !"active".equals(normalized)) {
            return "invalid";
        }

        // Admin accounts can never be blocked.
        if (userDAO.isAdminUser(userId)) {
            return "admin-protected";
        }

        return userDAO.updateStatus(userId, normalized)
                ? "status-updated"
                : "status-failed";
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
