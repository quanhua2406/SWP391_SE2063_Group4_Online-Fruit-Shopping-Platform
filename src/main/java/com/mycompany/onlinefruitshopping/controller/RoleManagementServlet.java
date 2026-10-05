package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.RoleDAO;
import com.mycompany.onlinefruitshopping.dao.RoleDAO.Role;
import com.mycompany.onlinefruitshopping.dao.RoleDAO.UserRoleInfo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/roles")
public class RoleManagementServlet extends HttpServlet {

    private final RoleDAO roleDAO = new RoleDAO();

    // =========================================================
    // GET
    // =========================================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            // Load all users by default
            List<UserRoleInfo> users =
                    roleDAO.getAllUsersWithRoles();

            // Load all available roles
            List<Role> roles =
                    roleDAO.getAllRoles();

            request.setAttribute("users", users);
            request.setAttribute("roles", roles);

            request.getRequestDispatcher(
                    "/role-management.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load role management page.",
                    e
            );
        }
    }

    // =========================================================
    // POST
    // =========================================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(
                    request.getContextPath() + "/admin/roles"
            );
            return;
        }

        try {

            // =================================================
            // SEARCH
            // =================================================
            if ("search".equals(action)) {

                String keyword =
                        request.getParameter("email");

                if (keyword == null) {
                    keyword = "";
                }

                keyword = keyword.trim();

                List<UserRoleInfo> users;

                if (keyword.isEmpty()) {

                    // Empty search -> show all users
                    users = roleDAO.getAllUsersWithRoles();

                } else {

                    // Search by keyword inside email
                    users = roleDAO.searchUsersByEmail(keyword);
                }

                List<Role> roles =
                        roleDAO.getAllRoles();

                request.setAttribute("users", users);
                request.setAttribute("roles", roles);
                request.setAttribute("searchEmail", keyword);

                request.getRequestDispatcher(
                        "/role-management.jsp"
                ).forward(request, response);

                return;
            }

            // =================================================
            // UPDATE ROLE
            // =================================================
            if ("update".equals(action)) {

                String userIdParam =
                        request.getParameter("userId");

                String roleIdParam =
                        request.getParameter("roleId");

                if (userIdParam == null
                        || roleIdParam == null) {

                    request.setAttribute(
                            "error",
                            "Invalid user or role."
                    );

                    loadPage(request, response);
                    return;
                }

                int userId;
                int roleId;

                try {

                    userId =
                            Integer.parseInt(userIdParam);

                    roleId =
                            Integer.parseInt(roleIdParam);

                } catch (NumberFormatException e) {

                    request.setAttribute(
                            "error",
                            "Invalid user or role."
                    );

                    loadPage(request, response);
                    return;
                }

                // Check selected role exists
                if (!roleDAO.roleExists(roleId)) {

                    request.setAttribute(
                            "error",
                            "Selected role does not exist."
                    );

                    loadPage(request, response);
                    return;
                }
if (roleDAO.isAdminUser(userId)) {

    request.setAttribute(
            "error",
            "Admin role cannot be changed."
    );

    loadPage(request, response);
    return;
}
                boolean updated =
                        roleDAO.updateUserRole(
                                userId,
                                roleId
                        );

                if (updated) {

                    request.setAttribute(
                            "success",
                            "User role updated successfully."
                    );

                } else {

                    request.setAttribute(
                            "error",
                            "Unable to update user role."
                    );
                }

                // Reload users after update
                List<UserRoleInfo> users =
                        roleDAO.getAllUsersWithRoles();

                List<Role> roles =
                        roleDAO.getAllRoles();

                request.setAttribute(
                        "users",
                        users
                );

                request.setAttribute(
                        "roles",
                        roles
                );

                request.getRequestDispatcher(
                        "/role-management.jsp"
                ).forward(request, response);

                return;
            }

            // Unknown action
            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/roles"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Role management operation failed.",
                    e
            );
        }
    }

    // =========================================================
    // LOAD PAGE
    // =========================================================
    private void loadPage(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        List<UserRoleInfo> users =
                roleDAO.getAllUsersWithRoles();

        List<Role> roles =
                roleDAO.getAllRoles();

        request.setAttribute("users", users);
        request.setAttribute("roles", roles);

        request.getRequestDispatcher(
                "/role-management.jsp"
        ).forward(request, response);
    }
}