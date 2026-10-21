package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.user.RoleDAO.Role;
import com.mycompany.onlinefruitshopping.dao.user.RoleDAO.UserRoleInfo;
import com.mycompany.onlinefruitshopping.service.user.RoleManagementService;
import com.mycompany.onlinefruitshopping.service.user.RoleManagementService.UpdateResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Role management (feature 16.0): view accounts, search them and
 * change the role of an account. All rules live in
 * {@link RoleManagementService}.
 */
@WebServlet("/admin/roles")
public class RoleManagementServlet extends HttpServlet {

    private final RoleManagementService roleService =
            new RoleManagementService();

    // =========================================================
    // GET
    // =========================================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            request.setAttribute("users", roleService.listUsers());
            request.setAttribute("roles", roleService.listRoles());

            request.getRequestDispatcher(
                    "/jsp/admin/role-management.jsp"
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

                String keyword = request.getParameter("email");

                List<UserRoleInfo> users = roleService.searchUsers(keyword);

                request.setAttribute("users", users);
                request.setAttribute("roles", roleService.listRoles());
                request.setAttribute(
                        "searchEmail",
                        keyword == null ? "" : keyword.trim()
                );

                request.getRequestDispatcher(
                        "/jsp/admin/role-management.jsp"
                ).forward(request, response);

                return;
            }

            // =================================================
            // UPDATE ROLE
            // =================================================
            if ("update".equals(action)) {

                UpdateResult result = roleService.updateUserRole(
                        request.getParameter("userId"),
                        request.getParameter("roleId")
                );

                if (result.isSuccess()) {
                    request.setAttribute("success", result.getMessage());
                } else {
                    request.setAttribute("error", result.getMessage());
                }

                request.setAttribute("users", roleService.listUsers());
                request.setAttribute("roles", roleService.listRoles());

                request.getRequestDispatcher(
                        "/jsp/admin/role-management.jsp"
                ).forward(request, response);

                return;
            }

            // Unknown action
            response.sendRedirect(
                    request.getContextPath() + "/admin/roles"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Role management operation failed.",
                    e
            );
        }
    }
}
