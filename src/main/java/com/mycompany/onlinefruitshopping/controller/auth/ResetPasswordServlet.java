package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.service.auth.PasswordResetService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final PasswordResetService passwordResetService =
            new PasswordResetService();

    // =========================================================
    // GET - SHOW RESET PASSWORD PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isResetAllowed(request)) {

            redirectToStart(request, response);
            return;
        }

        request.getRequestDispatcher(
                "/jsp/auth/reset-password.jsp"
        ).forward(request, response);
    }

    // =========================================================
    // POST - RESET PASSWORD
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!isResetAllowed(request)) {

            redirectToStart(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        String email = (String) session.getAttribute("resetEmail");

        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");

        password = password == null ? "" : password;
        confirmPassword = confirmPassword == null
                ? ""
                : confirmPassword;

        // -----------------------------------------------------
        // VALIDATE (LOGIC IN SERVICE)
        // -----------------------------------------------------

        String error =
                passwordResetService.validateNewPassword(
                        password,
                        confirmPassword
                );

        if (error != null) {

            showError(request, response, error);
            return;
        }

        // -----------------------------------------------------
        // RESET
        // -----------------------------------------------------

        try {

            boolean reset =
                    passwordResetService.resetPassword(
                            email,
                            password
                    );

            if (!reset) {

                showError(request, response,
                        "Unable to reset password.");
                return;
            }

            session.removeAttribute("resetEmail");
            session.removeAttribute("resetVerified");

            response.sendRedirect(
                    request.getContextPath()
                            + "/login?reset=success"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to reset password.",
                    e
            );
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * The reset form is only reachable after a successful OTP check.
     */
    private boolean isResetAllowed(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("resetEmail") == null) {
            return false;
        }

        return Boolean.TRUE.equals(
                session.getAttribute("resetVerified")
        );
    }

    private void redirectToStart(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        boolean hasResetEmail = session != null
                && session.getAttribute("resetEmail") != null;

        String target = hasResetEmail
                ? "/verify-otp"
                : "/forgot-password";

        response.sendRedirect(
                request.getContextPath() + target
        );
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);

        request.getRequestDispatcher(
                "/jsp/auth/reset-password.jsp"
        ).forward(request, response);
    }
}
