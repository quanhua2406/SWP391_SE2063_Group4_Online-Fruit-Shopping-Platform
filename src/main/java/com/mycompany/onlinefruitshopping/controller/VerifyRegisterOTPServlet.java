package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.RegistrationVerificationDAO;
import com.mycompany.onlinefruitshopping.dao.RegistrationVerificationDAO.PendingRegistration;
import com.mycompany.onlinefruitshopping.dao.UserDAO;

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

    private final RegistrationVerificationDAO verificationDAO =
            new RegistrationVerificationDAO();

    private final UserDAO userDAO =
            new UserDAO();

    // =========================================================
    // GET - SHOW VERIFY OTP PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // User must come from registration flow
        if (session == null
                || session.getAttribute("registerEmail") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/register"
            );
            return;
        }

        request.getRequestDispatcher(
                "/verify-register-otp.jsp"
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

        // -----------------------------------------------------
        // 1. CHECK REGISTER SESSION
        // -----------------------------------------------------

        if (session == null
                || session.getAttribute("registerEmail") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/register"
            );
            return;
        }

        String email =
                (String) session.getAttribute("registerEmail");

        // -----------------------------------------------------
        // 2. GET OTP
        // -----------------------------------------------------

        String otp = request.getParameter("otp");

        if (otp == null) {
            otp = "";
        }

        otp = otp.trim();

        // -----------------------------------------------------
        // 3. VALIDATE OTP FORMAT
        // -----------------------------------------------------

        if (!otp.matches("\\d{6}")) {

            request.setAttribute(
                    "error",
                    "OTP must contain exactly 6 digits."
            );

            request.getRequestDispatcher(
                    "/verify-register-otp.jsp"
            ).forward(request, response);

            return;
        }

        try {

            // -------------------------------------------------
            // 4. VERIFY OTP
            // -------------------------------------------------

            boolean valid =
                    verificationDAO.verifyOTP(
                            email,
                            otp
                    );

            if (!valid) {

                request.setAttribute(
                        "error",
                        "Invalid or expired OTP."
                );

                request.getRequestDispatcher(
                        "/verify-register-otp.jsp"
                ).forward(request, response);

                return;
            }

            // -------------------------------------------------
            // 5. GET PENDING REGISTRATION
            // -------------------------------------------------

            PendingRegistration pending =
                    verificationDAO.getPendingRegistration(
                            email
                    );

            if (pending == null) {

                request.setAttribute(
                        "error",
                        "Registration information was not found."
                );

                request.getRequestDispatcher(
                        "/verify-register-otp.jsp"
                ).forward(request, response);

                return;
            }

            // -------------------------------------------------
            // 6. CHECK EMAIL AGAIN
            // -------------------------------------------------
            // Prevent duplicate registration if the email
            // was registered while OTP verification was pending.

            if (userDAO.emailExists(email)) {

                verificationDAO.deletePendingRegistration(email);

                session.removeAttribute("registerEmail");

                request.setAttribute(
                        "error",
                        "This email is already registered."
                );

                request.getRequestDispatcher(
                        "/register.jsp"
                ).forward(request, response);

                return;
            }

            // -------------------------------------------------
            // 7. CREATE USER
            // -------------------------------------------------
            // passwordHash is already BCrypt hashed
            // inside RegistrationVerificationDAO.

            boolean registered =
                    userDAO.registerUser(
                            pending.getFullName(),
                            pending.getEmail(),
                            pending.getPasswordHash(),
                            pending.getPhone(),
                            pending.getAddress()
                    );

            if (!registered) {

                request.setAttribute(
                        "error",
                        "Unable to create your account. Please try again."
                );

                request.getRequestDispatcher(
                        "/verify-register-otp.jsp"
                ).forward(request, response);

                return;
            }

            // -------------------------------------------------
            // 8. DELETE PENDING REGISTRATION
            // -------------------------------------------------

            verificationDAO.deletePendingRegistration(
                    email
            );

            // -------------------------------------------------
            // 9. CLEAR REGISTER SESSION
            // -------------------------------------------------

            session.removeAttribute(
                    "registerEmail"
            );

            // -------------------------------------------------
            // 10. REGISTRATION SUCCESS
            // -------------------------------------------------

            response.sendRedirect(
                    request.getContextPath()
                            + "/login?registered=success"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Email verification failed.",
                    e
            );
        }
    }
}