package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserService();

    // =========================================================
    // GET - SHOW REGISTER PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/register.jsp"
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

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");


        // Handle null values
        if (fullName == null) {
            fullName = "";
        }

        if (email == null) {
            email = "";
        }

        if (password == null) {
            password = "";
        }

        if (confirmPassword == null) {
            confirmPassword = "";
        }

        if (phone == null) {
            phone = "";
        }

        if (address == null) {
            address = "";
        }


        // Trim normal text fields
        fullName = fullName.trim();
        email = email.trim();
        phone = phone.trim();
        address = address.trim();


        // =====================================================
        // VALIDATE FULL NAME
        // =====================================================

        if (fullName.isEmpty()) {

            showError(
                    request,
                    response,
                    "Full name cannot be empty."
            );

            return;
        }


        if (fullName.length() < 2) {

            showError(
                    request,
                    response,
                    "Full name must contain at least 2 characters."
            );

            return;
        }


        if (fullName.length() > 100) {

            showError(
                    request,
                    response,
                    "Full name cannot exceed 100 characters."
            );

            return;
        }


        // Must contain at least one letter
        if (!fullName.matches(".*\\p{L}.*")) {

            showError(
                    request,
                    response,
                    "Full name must contain letters."
            );

            return;
        }


        // Only letters and spaces
        if (!fullName.matches("[\\p{L}\\s]+")) {

            showError(
                    request,
                    response,
                    "Full name can contain only letters and spaces."
            );

            return;
        }


        // =====================================================
        // VALIDATE EMAIL
        // =====================================================

        if (email.isEmpty()) {

            showError(
                    request,
                    response,
                    "Email cannot be empty."
            );

            return;
        }


        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            showError(
                    request,
                    response,
                    "Please enter a valid email address."
            );

            return;
        }


        // =====================================================
        // VALIDATE PASSWORD
        // =====================================================

        if (password.isEmpty()) {

            showError(
                    request,
                    response,
                    "Password cannot be empty."
            );

            return;
        }


        if (password.length() < 8) {

            showError(
                    request,
                    response,
                    "Password must contain at least 8 characters."
            );

            return;
        }


        // At least one letter
        if (!password.matches(".*[A-Za-z].*")) {

            showError(
                    request,
                    response,
                    "Password must contain at least one letter."
            );

            return;
        }


        // At least one number
        if (!password.matches(".*[0-9].*")) {

            showError(
                    request,
                    response,
                    "Password must contain at least one number."
            );

            return;
        }


        // =====================================================
        // CONFIRM PASSWORD
        // =====================================================

        if (confirmPassword.isEmpty()) {

            showError(
                    request,
                    response,
                    "Confirm password cannot be empty."
            );

            return;
        }


        if (!password.equals(confirmPassword)) {

            showError(
                    request,
                    response,
                    "Passwords do not match."
            );

            return;
        }


        // =====================================================
        // VALIDATE PHONE
        // =====================================================

        if (phone.isEmpty()) {

            showError(
                    request,
                    response,
                    "Phone number cannot be empty."
            );

            return;
        }


        if (!phone.matches("\\d+")) {

            showError(
                    request,
                    response,
                    "Phone number can contain only digits."
            );

            return;
        }


        if (phone.length() < 9 ||
                phone.length() > 11) {

            showError(
                    request,
                    response,
                    "Phone number must contain 9 to 11 digits."
            );

            return;
        }


        // =====================================================
        // VALIDATE ADDRESS
        // =====================================================

        if (address.isEmpty()) {

            showError(
                    request,
                    response,
                    "Address cannot be empty."
            );

            return;
        }


        // =====================================================
        // CHECK EMAIL EXISTS
        // =====================================================

        try {

            if (userService.isEmailExists(email)) {

                showError(
                        request,
                        response,
                        "This email is already registered."
                );

                return;
            }


            // =================================================
            // REGISTER
            // =================================================

            boolean registered =
                    userService.registerUser(
                            fullName,
                            email,
                            password,
                            phone,
                            address
                    );


            if (!registered) {

                showError(
                        request,
                        response,
                        "Registration failed. Please try again."
                );

                return;
            }


            // =================================================
            // SUCCESS
            // =================================================

            response.sendRedirect(
                    request.getContextPath()
                            + "/login?registered=success"
            );


        } catch (SQLException e) {

            throw new ServletException(
                    "Registration failed.",
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
            String error)
            throws ServletException, IOException {

        request.setAttribute(
                "error",
                error
        );

        // Keep entered values
        request.setAttribute(
                "fullName",
                request.getParameter("fullName")
        );

        request.setAttribute(
                "email",
                request.getParameter("email")
        );

        request.setAttribute(
                "phone",
                request.getParameter("phone")
        );

        request.setAttribute(
                "address",
                request.getParameter("address")
        );

        request.getRequestDispatcher(
                "/register.jsp"
        ).forward(request, response);
    }
}