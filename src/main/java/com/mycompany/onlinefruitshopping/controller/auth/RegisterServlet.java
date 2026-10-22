package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.service.auth.RegistrationService;

import jakarta.mail.MessagingException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(RegisterServlet.class.getName());

    private final RegistrationService registrationService =
            new RegistrationService();

    // =========================================================
    // GET - SHOW REGISTER PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/jsp/auth/register.jsp"
        ).forward(request, response);
    }

    // =========================================================
    // POST - REGISTER
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // -----------------------------------------------------
        // GET FORM DATA
        // -----------------------------------------------------

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String password = nullToEmpty(request.getParameter("password"));
        String confirmPassword =
                nullToEmpty(request.getParameter("confirmPassword"));
        String phone = trim(request.getParameter("phone"));
        String address = trim(request.getParameter("address"));
        String role = trim(request.getParameter("role"));

        // -----------------------------------------------------
        // VALIDATE (LOGIC IN SERVICE)
        // -----------------------------------------------------

        String error =
                registrationService.validateRegistration(
                        fullName,
                        email,
                        password,
                        confirmPassword,
                        phone,
                        address,
                        role
                );

        if (error != null) {
            showError(request, response, error);
            return;
        }

        try {

            if (registrationService.isEmailRegistered(email)) {

                showError(request, response,
                        "This email is already registered.");

                return;
            }

            // The client can only offer Customer / Shop Owner /
            // Delivery Staff; the service still verifies the role
            // is a real, self-registerable role before continuing.

            if (!registrationService.roleExists(role)) {

                showError(request, response,
                        "The selected role is not available.");

                return;
            }

            // -------------------------------------------------
            // STORE PENDING + SEND OTP
            // -------------------------------------------------

            registrationService.startRegistration(
                    fullName,
                    email,
                    password,
                    phone,
                    address
            );

            HttpSession session = request.getSession();

            session.setAttribute("registerEmail", email);
            session.setAttribute("registerRole", role);

            response.sendRedirect(
                    request.getContextPath()
                            + "/verify-register-otp"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Registration failed.",
                    e
            );

        } catch (MessagingException e) {

            LOGGER.log(Level.SEVERE,
                    "Unable to send registration OTP", e);

            showError(request, response,
                    "Unable to send the OTP email. Please try again.");
        }
    }

    // =========================================================
    // SHOW ERROR
    // =========================================================

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String error)
            throws ServletException, IOException {

        request.setAttribute("error", error);
        request.setAttribute("fullName", request.getParameter("fullName"));
        request.setAttribute("email", request.getParameter("email"));
        request.setAttribute("phone", request.getParameter("phone"));
        request.setAttribute("address", request.getParameter("address"));
        request.setAttribute("role", request.getParameter("role"));

        request.getRequestDispatcher(
                "/jsp/auth/register.jsp"
        ).forward(request, response);
    }

    private String trim(String value) {

        return value == null ? "" : value.trim();
    }

    private String nullToEmpty(String value) {

        return value == null ? "" : value;
    }
}
