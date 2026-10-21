package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO.UserInfo;
import com.mycompany.onlinefruitshopping.service.user.AdminUserService;
import com.mycompany.onlinefruitshopping.service.user.AdminUserService.UpdateResult;
import com.mycompany.onlinefruitshopping.service.user.AdminUserService.UserForm;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * User administration (feature 18.0 / 2.0): view, search, edit,
 * block and unblock accounts. All rules live in
 * {@link AdminUserService}.
 */
@WebServlet("/admin/user-management")
public class AdminUserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AdminUserService userService = new AdminUserService();

    private static final String LIST_JSP = "/jsp/admin/admin-users.jsp";
    private static final String DETAIL_JSP = "/jsp/admin/admin-user-detail.jsp";
    private static final String EDIT_JSP = "/WEB-INF/admin-user-edit.jsp";

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

        UserInfo user = userService.getUserById(userId);

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

        UserInfo user = userService.getUserById(userId);

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

        List<UserInfo> users = userService.getAllUsers();

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
        String role = trim(request.getParameter("role"));

        List<UserInfo> users = userService.searchUsers(keyword, role);

        if (users == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid role."
            );
            return;
        }

        request.setAttribute("users", users);
        request.setAttribute("keyword", keyword);
        request.setAttribute(
                "selectedRole",
                role.isEmpty() ? "ALL" : role.toUpperCase(java.util.Locale.ROOT)
        );

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

        UserInfo user = userService.getUserById(userId);

        if (user == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found."
            );
            return;
        }

        UserForm form = new UserForm(
                request.getParameter("fullName"),
                request.getParameter("email"),
                request.getParameter("phone")
        );

        UpdateResult result = userService.updateUser(userId, form);

        if (!result.isSuccess()) {
            forwardEditWithError(request, response, user, form, result.getError());
            return;
        }

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
            UserForm form,
            String error
    ) throws ServletException, IOException {

        request.setAttribute("user", user);
        request.setAttribute("formFullName", form.getFullName());
        request.setAttribute("formEmail", form.getEmail());
        request.setAttribute("formPhone", form.getPhone());
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

        UserInfo user = userService.getUserById(userId);

        if (user == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found."
            );
            return;
        }

        String message = userService.changeStatus(userId, status);

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
