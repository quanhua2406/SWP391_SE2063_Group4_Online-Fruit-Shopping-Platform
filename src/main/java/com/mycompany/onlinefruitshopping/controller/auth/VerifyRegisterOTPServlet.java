package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.service.auth.RegistrationService;
import com.mycompany.onlinefruitshopping.service.auth.RegistrationService.VerifyStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/verify-register-otp")
public class VerifyRegisterOTPServlet extends HttpServlet {

    private final RegistrationService registrationService =
            new RegistrationService();

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
                || session.getAttribute("registerEmail") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/register"
            );
            return;
        }

        request.getRequestDispatcher(
                "/jsp/auth/verify-register-otp.jsp"
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
                || session.getAttribute("registerEmail") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/register"
            );
            return;
        }

        String email =
                (String) session.getAttribute("registerEmail");

        String role =
                (String) session.getAttribute("registerRole");

        if (role == null || role.isBlank()) {

            response.sendRedirect(
                    request.getContextPath() + "/register"
            );
            return;
        }

        String otp = request.getParameter("otp");

        otp = otp == null ? "" : otp.trim();

        if (!RegistrationService.isValidOtp(otp)) {

            showOtpError(request, response,
                    "OTP must contain exactly 6 digits.");
            return;
        }

        try {

            VerifyStatus status =
                    registrationService.completeRegistration(
                            email,
                            otp,
                            role
                    );

            switch (status) {

                case SUCCESS -> {

                    session.removeAttribute("registerEmail");
                    session.removeAttribute("registerRole");

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/login?registered=success"
                    );
                }

                case EMAIL_ALREADY_REGISTERED -> {

                    session.removeAttribute("registerEmail");
                    session.removeAttribute("registerRole");

                    request.setAttribute(
                            "error",
                            "This email is already registered."
                    );

                    request.getRequestDispatcher(
                            "/jsp/auth/register.jsp"
                    ).forward(request, response);
                }

                case INVALID_ROLE -> {

                    session.removeAttribute("registerEmail");
                    session.removeAttribute("registerRole");

                    response.sendRedirect(
                            request.getContextPath() + "/register"
                    );
                }

                case INVALID_OTP ->

                    showOtpError(request, response,
                            "Invalid or expired OTP.");

                case PENDING_NOT_FOUND ->

                    showOtpError(request, response,
                            "Registration information was not found.");

                case CREATE_FAILED ->

                    showOtpError(request, response,
                            "Unable to create your account. "
                                    + "Please try again.");
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Email verification failed.",
                    e
            );
        }
    }

    // =========================================================
    // SHOW OTP ERROR
    // =========================================================

    private void showOtpError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);

        request.getRequestDispatcher(
                "/jsp/auth/verify-register-otp.jsp"
        ).forward(request, response);
    }
}
