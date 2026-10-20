package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import com.mycompany.onlinefruitshopping.service.auth.LoginService.LoginResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final LoginService loginService = new LoginService();

    // =========================================================
    // GET - SHOW LOGIN PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Already logged in -> go to the role dashboard
        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("user") != null) {

            User loggedIn =
                    (User) session.getAttribute("user");

            response.sendRedirect(
                    request.getContextPath()
                            + LoginService.dashboardPath(
                                    loggedIn.getRoleName())
            );

            return;
        }

        request.getRequestDispatcher(
                "/jsp/auth/login.jsp"
        ).forward(request, response);
    }

    // =========================================================
    // POST - LOGIN
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String role = trim(request.getParameter("role"));

        if (password == null) {
            password = "";
        }

        // -----------------------------------------------------
        // BASIC VALIDATION
        // -----------------------------------------------------

        if (email.isEmpty()) {
            showError(request, response, email, role,
                    "Email cannot be empty.");
            return;
        }

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            showError(request, response, email, role,
                    "Please enter a valid email address.");
            return;
        }

        if (password.isEmpty()) {
            showError(request, response, email, role,
                    "Password cannot be empty.");
            return;
        }

        if (role.isEmpty()) {
            showError(request, response, email, role,
                    "Please choose who you are logging in as.");
            return;
        }

        // -----------------------------------------------------
        // AUTHENTICATE (LOGIC IN SERVICE)
        // -----------------------------------------------------

        try {

            LoginResult result =
                    loginService.login(email, password, role);

            if (!result.isSuccess()) {

                showError(request, response, email, role,
                        result.getMessage());

                return;
            }

            User user = result.getUser();

            // -------------------------------------------------
            // LOGIN SUCCESS
            // -------------------------------------------------

            HttpSession session = request.getSession(true);

            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserID());
            session.setAttribute("roleName", user.getRoleName());

            // Merge guest session cart into database cart if present
            try {
                com.mycompany.onlinefruitshopping.dao.CartDAO cartDAO =
                        new com.mycompany.onlinefruitshopping.dao.CartDAO();
                @SuppressWarnings("unchecked")
                java.util.List<com.mycompany.onlinefruitshopping.model.CartItem> guestCart =
                        (java.util.List<com.mycompany.onlinefruitshopping.model.CartItem>)
                                session.getAttribute("sessionCart");
                if (guestCart != null && !guestCart.isEmpty()) {
                    cartDAO.mergeSessionCart(user.getUserID(), guestCart);
                    session.removeAttribute("sessionCart");
                }
                session.setAttribute("cartCount",
                        cartDAO.getCartCount(user.getUserID()));
            } catch (Exception ignored) {
            }

            // If the user was previously trying to access a protected page
            String redirectUrl =
                    (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                session.removeAttribute("redirectAfterLogin");
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(
                        request.getContextPath()
                                + LoginService.dashboardPath(
                                        user.getRoleName())
                );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Login failed.",
                    e
            );
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String email,
            String role,
            String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);
        request.setAttribute("email", email);
        request.setAttribute("selectedRole", role);

        request.getRequestDispatcher(
                "/jsp/auth/login.jsp"
        ).forward(request, response);
    }

    private String trim(String value) {

        return value == null ? "" : value.trim();
    }
}
