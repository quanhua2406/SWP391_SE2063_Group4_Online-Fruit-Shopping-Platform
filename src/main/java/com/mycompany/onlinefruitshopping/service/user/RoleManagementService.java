package com.mycompany.onlinefruitshopping.service.user;

import com.mycompany.onlinefruitshopping.dao.user.RoleDAO;
import com.mycompany.onlinefruitshopping.dao.user.RoleDAO.Role;
import com.mycompany.onlinefruitshopping.dao.user.RoleDAO.UserRoleInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic of role management (feature 16.0): listing accounts
 * with their role and changing the role of an account.
 *
 * <p>The controller only reads the request and renders the response.
 * The rules (identifiers must be numbers, the role must exist, an
 * admin account's role is fixed ...) live here.</p>
 */
public class RoleManagementService {

    private final RoleDAO roleDAO = new RoleDAO();

    /** All accounts together with their current role. */
    public List<UserRoleInfo> listUsers() throws SQLException {
        return roleDAO.getAllUsersWithRoles();
    }

    /** Every role the administrator may assign. */
    public List<Role> listRoles() throws SQLException {
        return roleDAO.getAllRoles();
    }

    /**
     * Searches accounts by a keyword inside the email, or lists them
     * all when the keyword is empty.
     */
    public List<UserRoleInfo> searchUsers(String keywordRaw)
            throws SQLException {

        String keyword = trim(keywordRaw);

        return keyword.isEmpty()
                ? roleDAO.getAllUsersWithRoles()
                : roleDAO.searchUsersByEmail(keyword);
    }

    /** Outcome of a role change, carrying the message to display. */
    public static class UpdateResult {

        private final boolean success;
        private final String message;

        private UpdateResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Changes the role of one account.
     *
     * @return the outcome and the message to show
     */
    public UpdateResult updateUserRole(String userIdRaw, String roleIdRaw)
            throws SQLException {

        if (userIdRaw == null || roleIdRaw == null) {
            return new UpdateResult(false, "Invalid user or role.");
        }

        int userId;
        int roleId;

        try {
            userId = Integer.parseInt(userIdRaw.trim());
            roleId = Integer.parseInt(roleIdRaw.trim());
        } catch (NumberFormatException e) {
            return new UpdateResult(false, "Invalid user or role.");
        }

        if (userId <= 0 || roleId <= 0) {
            return new UpdateResult(false, "Invalid user or role.");
        }

        if (!roleDAO.roleExists(roleId)) {
            return new UpdateResult(false, "Selected role does not exist.");
        }

        if (roleDAO.isAdminUser(userId)) {
            return new UpdateResult(false, "Admin role cannot be changed.");
        }

        boolean updated = roleDAO.updateUserRole(userId, roleId);

        return updated
                ? new UpdateResult(true, "User role updated successfully.")
                : new UpdateResult(false, "Unable to update user role.");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
