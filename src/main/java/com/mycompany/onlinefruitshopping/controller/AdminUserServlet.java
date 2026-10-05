package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.AdminUserDAO;
import com.mycompany.onlinefruitshopping.dao.AdminUserDAO.UserInfo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/user-management")
public class AdminUserServlet extends HttpServlet {

    private AdminUserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new AdminUserDAO();
    }

    // =========================================================
    // GET
    // =========================================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        loadAllUsers(request, response);
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

        String action =
                request.getParameter("action");

        if (action == null || action.isBlank()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/user-management"
            );

            return;
        }

        try {

            // =================================================
            // SEARCH + FILTER
            // =================================================
            if ("search".equals(action)) {

                String keyword =
                        request.getParameter("keyword");

                String role =
                        request.getParameter("role");

                if (keyword == null) {
                    keyword = "";
                }

                if (role == null || role.isBlank()) {
                    role = "ALL";
                }

                keyword = keyword.trim();
                role = role.trim().toUpperCase();

                List<UserInfo> users;

                // ALL ROLES
                if ("ALL".equals(role)) {

                    if (keyword.isEmpty()) {

                        users =
                                userDAO.getAllUsers();

                    } else {

                        users =
                                userDAO.searchByEmail(
                                        keyword
                                );
                    }

                // CUSTOMER
                } else if ("CUSTOMER".equals(role)) {

                    if (keyword.isEmpty()) {

                        users =
                                userDAO.filterByRole(
                                        "CUSTOMER"
                                );

                    } else {

                        users =
                                userDAO.searchByEmailAndRole(
                                        keyword,
                                        "CUSTOMER"
                                );
                    }

                // STORE OWNER
                } else if ("SHOPOWNER".equals(role)) {

                    if (keyword.isEmpty()) {

                        users =
                                userDAO.filterByRole(
                                        "SHOPOWNER"
                                );

                    } else {

                        users =
                                userDAO.searchByEmailAndRole(
                                        keyword,
                                        "SHOPOWNER"
                                );
                    }

                // DELIVERY STAFF
                } else if ("DELIVERYSTAFF".equals(role)) {

                    if (keyword.isEmpty()) {

                        users =
                                userDAO.filterByRole(
                                        "DELIVERYSTAFF"
                                );

                    } else {

                        users =
                                userDAO.searchByEmailAndRole(
                                        keyword,
                                        "DELIVERYSTAFF"
                                );
                    }

                // ADMIN
                } else if ("ADMIN".equals(role)) {

                    if (keyword.isEmpty()) {

                        users =
                                userDAO.filterByRole(
                                        "ADMIN"
                                );

                    } else {

                        users =
                                userDAO.searchByEmailAndRole(
                                        keyword,
                                        "ADMIN"
                                );
                    }

                // GUEST
                } else if ("GUEST".equals(role)) {

                    if (keyword.isEmpty()) {

                        users =
                                userDAO.filterByRole(
                                        "GUEST"
                                );

                    } else {

                        users =
                                userDAO.searchByEmailAndRole(
                                        keyword,
                                        "GUEST"
                                );
                    }

                } else {

                    users =
                            userDAO.getAllUsers();

                    role = "ALL";
                }

                request.setAttribute(
                        "users",
                        users
                );

                request.setAttribute(
                        "keyword",
                        keyword
                );

                request.setAttribute(
                        "selectedRole",
                        role
                );

                request.getRequestDispatcher(
                        "/admin-users.jsp"
                ).forward(
                        request,
                        response
                );

                return;
            }

            // =================================================
            // UPDATE
            // =================================================
            if ("update".equals(action)) {

                String userIdParam =
                        request.getParameter("userId");

                String fullName =
                        request.getParameter("fullName");

                String email =
                        request.getParameter("email");

                String phone =
                        request.getParameter("phone");

                int userId =
                        Integer.parseInt(userIdParam);

                if (fullName == null) {
                    fullName = "";
                }

                if (email == null) {
                    email = "";
                }

                if (phone == null) {
                    phone = "";
                }

                fullName = fullName.trim();
                email = email.trim();
                phone = phone.trim();

                if (userDAO.emailExists(
                        email,
                        userId)) {

                    request.setAttribute(
                            "error",
                            "This email is already used by another account."
                    );

                    loadAllUsers(
                            request,
                            response
                    );

                    return;
                }

                boolean updated =
                        userDAO.updateUser(
                                userId,
                                fullName,
                                email,
                                phone
                        );

                if (updated) {

                    request.setAttribute(
                            "success",
                            "User information updated successfully."
                    );

                } else {

                    request.setAttribute(
                            "error",
                            "Unable to update user."
                    );
                }

                loadAllUsers(
                        request,
                        response
                );

                return;
            }

            // =================================================
            // BLOCK
            // =================================================
            if ("block".equals(action)) {

                int userId =
                        Integer.parseInt(
                                request.getParameter("userId")
                        );

                // Không cho block Admin
                if (userDAO.isAdminUser(userId)) {

                    request.setAttribute(
                            "error",
                            "Admin account cannot be blocked."
                    );

                    loadAllUsers(
                            request,
                            response
                    );

                    return;
                }

                boolean updated =
                        userDAO.updateStatus(
                                userId,
                                "locked"
                        );

                if (updated) {

                    request.setAttribute(
                            "success",
                            "Account blocked successfully."
                    );

                } else {

                    request.setAttribute(
                            "error",
                            "Unable to block account."
                    );
                }

                loadAllUsers(
                        request,
                        response
                );

                return;
            }

            // =================================================
            // UNBLOCK
            // =================================================
            if ("unblock".equals(action)) {

                int userId =
                        Integer.parseInt(
                                request.getParameter("userId")
                        );

                // Không cho thay đổi Admin
                if (userDAO.isAdminUser(userId)) {

                    request.setAttribute(
                            "error",
                            "Admin account status cannot be changed."
                    );

                    loadAllUsers(
                            request,
                            response
                    );

                    return;
                }

                boolean updated =
                        userDAO.updateStatus(
                                userId,
                                "active"
                        );

                if (updated) {

                    request.setAttribute(
                            "success",
                            "Account unblocked successfully."
                    );

                } else {

                    request.setAttribute(
                            "error",
                            "Unable to unblock account."
                    );
                }

                loadAllUsers(
                        request,
                        response
                );

                return;
            }

        } catch (NumberFormatException e) {

            throw new ServletException(
                    "Invalid user ID.",
                    e
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Database error in User Administration.",
                    e
            );
        }
    }

    // =========================================================
    // LOAD ALL USERS
    // =========================================================
    private void loadAllUsers(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            List<UserInfo> users =
                    userDAO.getAllUsers();

            request.setAttribute(
                    "users",
                    users
            );

            request.setAttribute(
                    "keyword",
                    ""
            );

            request.setAttribute(
                    "selectedRole",
                    "ALL"
            );

            request.getRequestDispatcher(
                    "/admin-users.jsp"
            ).forward(
                    request,
                    response
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load users.",
                    e
            );
        }
    }
}