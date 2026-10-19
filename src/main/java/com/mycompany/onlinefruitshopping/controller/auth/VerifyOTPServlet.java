package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.service.auth.PasswordResetService;
import com.mycompany.onlinefruitshopping.service.auth.RegistrationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/verify-otp")
public class VerifyOTPServlet extends HttpServlet {

    private final PasswordResetService passwordResetService =
            new PasswordResetService();

    // =========================================================
    // GET - SHOW VERIFY OTP PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("resetEmail") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/forgot-password"
            );
            return;
        }

        request.getRequestDispatcher(
                "/jsp/auth/verify-otp.jsp"
        ).forward(request, response);
    }

    // =========================================================
    // POST - VERIFY OTP
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("resetEmail") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/forgot-password"
            );
            return;
        }

        String email = (String) session.getAttribute("resetEmail");

        String otp = request.getParameter("otp");

        otp = otp == null ? "" : otp.trim();

        if (otp.isEmpty()) {

            showError(request, response, "OTP cannot be empty.");
            return;
        }

        if (!RegistrationService.isValidOtp(otp)) {

            showError(request, response,
                    "OTP must contain exactly 6 digits.");
            return;
        }

        try {

            boolean verified =
                    passwordResetService.verifyOtp(email, otp);

            if (!verified) {

                showError(request, response,
                        "Invalid or expired OTP.");
                return;
            }

            // -------------------------------------------------
            // OTP VERIFIED -> ALLOW PASSWORD RESET
            // -------------------------------------------------

            session.setAttribute("resetVerified", true);

            response.sendRedirect(
                    request.getContextPath() + "/reset-password"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to verify OTP.",
                    e
            );
        }
    }

    // =========================================================
    // SHOW ERROR
    // =========================================================

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);

        request.getRequestDispatcher(
                "/jsp/auth/verify-otp.jsp"
        ).forward(request, response);
    }
}
