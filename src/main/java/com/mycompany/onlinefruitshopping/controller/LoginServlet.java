package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.UserService;
import com.mycompany.onlinefruitshopping.utils.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/login.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // =========================================
        // 1. Get data from form
        // =========================================

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        email = email == null ? "" : email.trim();
        password = password == null ? "" : password;

        // Keep email when login fails
        request.setAttribute("email", email);

        // =========================================
        // 2. Validate email
        // =========================================

        if (!ValidationUtil.isValidEmail(email)) {

            request.setAttribute(
                    "error",
                    "Please enter a valid Gmail address."
            );

            request.getRequestDispatcher(
                    "/login.jsp"
            ).forward(request, response);

            return;
        }

        // =========================================
        // 3. Validate password
        // =========================================

        if (password.isBlank()) {

            request.setAttribute(
                    "error",
                    "Please enter your password."
            );

            request.getRequestDispatcher(
                    "/login.jsp"
            ).forward(request, response);

            return;
        }

        try {

            // =========================================
            // 4. Authenticate user
            // =========================================

            User user = userService.getUserByLogin(
                    email,
                    password
            );

            // Invalid email/password
            if (user == null) {

                request.setAttribute(
                        "error",
                        "Invalid Gmail or password."
                );

                request.getRequestDispatcher(
                        "/login.jsp"
                ).forward(request, response);

                return;
            }

            // =========================================
            // 5. Check account status
            // =========================================

            if (user.getStatus() != null
                    && !"ACTIVE".equalsIgnoreCase(
                            user.getStatus())) {

                request.setAttribute(
                        "error",
                        "Your account is not active."
                );

                request.getRequestDispatcher(
                        "/login.jsp"
                ).forward(request, response);

                return;
            }

            // =========================================
            // 6. Create session
            // =========================================

            HttpSession session =
                    request.getSession();

            session.setAttribute(
                    "user",
                    user
            );

            session.setAttribute(
                    "role",
                    user.getRoleName()
            );

            // =========================================
            // 7. Redirect by role
            // =========================================

            String role = user.getRoleName();

            if (role == null || role.isBlank()) {

                session.invalidate();

                request.setAttribute(
                        "error",
                        "Invalid user role."
                );

                request.getRequestDispatcher(
                        "/login.jsp"
                ).forward(request, response);

                return;
            }

            switch (role.toUpperCase()) {

                case "CUSTOMER":

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/customer/dashboard"
                    );

                    break;

                case "SHOP_OWNER":

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/store-owner/dashboard"
                    );

                    break;

                case "DELIVERY_STAFF":

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/delivery/dashboard"
                    );

                    break;

                case "ADMIN":

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/admin/dashboard"
                    );

                    break;

                default:

                    session.invalidate();

                    request.setAttribute(
                            "error",
                            "Invalid user role."
                    );

                    request.getRequestDispatcher(
                            "/login.jsp"
                    ).forward(request, response);

                    break;
            }

        } catch (Exception e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "System error. Please try again later."
            );

            request.getRequestDispatcher(
                    "/login.jsp"
            ).forward(request, response);
        }
    }
}