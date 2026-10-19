
package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO;
import com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO.UserInfo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/admin/user-management")
public class AdminUserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private AdminUserDAO userDAO;

    private static final String LIST_JSP = "/jsp/admin/admin-users.jsp";
    private static final String DETAIL_JSP = "/jsp/admin/admin-user-detail.jsp";
    private static final String EDIT_JSP = "/WEB-INF/admin-user-edit.jsp";

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "ADMIN",
            "CUSTOMER",
            "SHOPOWNER",
            "DELIVERYSTAFF",
            "GUEST"
    );

    @Override
    public void init() throws ServletException {
        userDAO = new AdminUserDAO();
    }

    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = trim(request.getParameter("action"));

        try {
            if ("detail".equalsIgnoreCase(action)) {
                showDetail(request, response);

            } else if ("edit".equalsIgnoreCase(action)) {
                showEdit(request, response);

            } else if ("search".equalsIgnoreCase(action)
                    || request.getParameter("keyword") != null
                    || request.getParameter("role") != null) {
                searchUsers(request, response);

            } else {
                loadUsers(request, response);
            }

        } catch (SQLException e) {
            log("Failed to retrieve user data.", e);
            throw new ServletException(
                    "Unable to retrieve user data.", e
            );
        }
    }

    // =====================================================
    // SHOW USER DETAIL
    // =====================================================

    private void showDetail(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        Integer userId = parseUserId(request, response);

        if (userId == null) {
            return;
        }

        UserInfo user = userDAO.getUserById(userId);

        if (user == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found."
            );
            return;
        }

        request.setAttribute("user", user);

        request.getRequestDispatcher(DETAIL_JSP)
                .forward(request, response);
    }

    // =====================================================
    // SHOW EDIT FORM
    // =====================================================

    private void showEdit(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        Integer userId = parseUserId(request, response);

        if (userId == null) {
            return;
        }

        UserInfo user = userDAO.getUserById(userId);

        if (user == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found."
            );
            return;
        }

        request.setAttribute("user", user);

        request.getRequestDispatcher(EDIT_JSP)
                .forward(request, response);
    }

    // =====================================================
    // LOAD ALL USERS
    // =====================================================

    private void loadUsers(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        List<UserInfo> users = userDAO.getAllUsers();

        request.setAttribute("users", users);
        request.setAttribute("keyword", "");
        request.setAttribute("selectedRole", "ALL");

        request.getRequestDispatcher(LIST_JSP)
                .forward(request, response);
    }

    // =====================================================
    // SEARCH AND FILTER
    // =====================================================

    private void searchUsers(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        String keyword = trim(request.getParameter("keyword"));

        String role = trim(request.getParameter("role"))
                .toUpperCase(Locale.ROOT);

        if (role.isEmpty()) {
            role = "ALL";
        }

        List<UserInfo> users;

        if ("ALL".equals(role)) {

            if (keyword.isEmpty()) {
                users = userDAO.getAllUsers();
            } else {
                users = userDAO.searchByEmail(keyword);
            }

        } else if (ALLOWED_ROLES.contains(role)) {

            if (keyword.isEmpty()) {
                users = userDAO.filterByRole(role);
            } else {
                users = userDAO.searchByEmailAndRole(
                        keyword, role
                );
            }

        } else {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid role."
            );
            return;
        }

        request.setAttribute("users", users);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedRole", role);

        request.getRequestDispatcher(LIST_JSP)
                .forward(request, response);
    }

    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = trim(request.getParameter("action"));

        try {
            if ("update".equalsIgnoreCase(action)) {
                updateUser(request, response);

            } else if ("block".equalsIgnoreCase(action)) {
                changeStatus(request, response, "locked");

            } else if ("unblock".equalsIgnoreCase(action)) {
                changeStatus(request, response, "active");

            } else {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid action."
                );
            }

        } catch (SQLException e) {
            log("Failed to update user.", e);
            throw new ServletException(
                    "Unable to update user.", e
            );
        }
    }

    // =====================================================
    // UPDATE USER
    // =====================================================

    private void updateUser(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        Integer userId = parseUserId(request, response);

        if (userId == null) {
            return;
        }

        UserInfo user = userDAO.getUserById(userId);

        if (user == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found."
            );
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));

        // Validate full name.
        if (fullName.isEmpty()) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Full Name is required."
            );
            return;
        }

        if (fullName.length() < 2 || fullName.length() > 100) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Full Name must contain 2 to 100 characters."
            );
            return;
        }

        if (!fullName.matches("[\\p{L}\\p{M} .'-]+")) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Full Name contains invalid characters."
            );
            return;
        }

        // Validate email.
        if (email.isEmpty()) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Email is required."
            );
            return;
        }

        String emailRegex =
                "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@"
                + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}"
                + "[A-Za-z0-9])?(?:\\.[A-Za-z0-9]"
                + "(?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$";

        if (email.length() > 254 || !email.matches(emailRegex)) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Please enter a valid email address."
            );
            return;
        }

        // Check whether another account already uses this email.
        if (userDAO.emailExists(email, userId)) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "This email is already used by another account."
            );
            return;
        }

        // Validate phone number.
        if (phone.isEmpty()) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Phone is required."
            );
            return;
        }

        if (!phone.matches("^0[0-9]{9,10}$")) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Phone must start with 0 and contain 10 or 11 digits."
            );
            return;
        }

        // Update database.
        boolean updated = userDAO.updateUser(
                userId, fullName, email, phone
        );

        if (!updated) {
            forwardEditWithError(
                    request, response, user,
                    fullName, email, phone,
                    "Update failed. Please try again."
            );
            return;
        }

        // Redirect after a successful update.
        response.sendRedirect(
                request.getContextPath()
                + "/admin/user-management?action=detail&userId="
                + userId
                + "&message=updated"
        );
    }

    // =====================================================
    // RETURN TO EDIT FORM WITH ERROR
    // =====================================================

    private void forwardEditWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            UserInfo user,
            String fullName,
            String email,
            String phone,
            String error
    ) throws ServletException, IOException {

        request.setAttribute("user", user);
        request.setAttribute("formFullName", fullName);
        request.setAttribute("formEmail", email);
        request.setAttribute("formPhone", phone);
        request.setAttribute("error", error);

        request.getRequestDispatcher(EDIT_JSP)
                .forward(request, response);
    }

    // =====================================================
    // BLOCK / UNBLOCK USER
    // =====================================================

    private void changeStatus(
            HttpServletRequest request,
            HttpServletResponse response,
            String status
    ) throws ServletException, IOException, SQLException {

        Integer userId = parseUserId(request, response);

        if (userId == null) {
            return;
        }

        UserInfo user = userDAO.getUserById(userId);

        if (user == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found."
            );
            return;
        }

        // Protect admin accounts from being blocked.
        if (userDAO.isAdminUser(userId)) {
            redirectToDetail(
                    request, response, userId, "admin-protected"
            );
            return;
        }

        boolean updated = userDAO.updateStatus(userId, status);

        String message = updated
                ? "status-updated"
                : "status-failed";

        redirectToDetail(request, response, userId, message);
    }

    // =====================================================
    // REDIRECT TO DETAIL
    // =====================================================

    private void redirectToDetail(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String message
    ) throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/admin/user-management?action=detail&userId="
                + userId
                + "&message=" + message
        );
    }

    // =====================================================
    // PARSE USER ID
    // =====================================================

    private Integer parseUserId(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String value = request.getParameter("userId");

        if (value == null || value.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing user ID."
            );
            return null;
        }

        try {
            int userId = Integer.parseInt(value);

            if (userId <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid user ID."
                );
                return null;
            }

            return userId;

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid user ID."
            );
            return null;
        }
    }

    // =====================================================
    // STRING HELPER
    // =====================================================

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
