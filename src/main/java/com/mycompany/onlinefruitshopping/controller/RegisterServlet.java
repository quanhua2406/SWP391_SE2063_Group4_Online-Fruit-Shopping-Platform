package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.service.UserService;
import com.mycompany.onlinefruitshopping.utils.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/register.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim();
        password = password == null ? "" : password;
        confirmPassword = confirmPassword == null ? "" : confirmPassword;
        phone = phone == null ? "" : phone.trim();
        address = address == null ? "" : address.trim();

        // Keep entered data when validation fails
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("address", address);

        // =========================
        // 1. Full Name
        // =========================
        if (!ValidationUtil.isValidFullName(fullName)) {

            request.setAttribute(
                    "error",
                    "Full name is invalid. Please enter 2-100 letters and spaces only."
            );

            request.getRequestDispatcher("/register.jsp")
                    .forward(request, response);

            return;
        }

        // =========================
        // 2. Email
        // =========================
        if (!ValidationUtil.isValidEmail(email)) {

            request.setAttribute(
                    "error",
                    "Please enter a valid Gmail address."
            );

            request.getRequestDispatcher("/register.jsp")
                    .forward(request, response);

            return;
        }

        // =========================
        // 3. Password
        // =========================
        if (!ValidationUtil.isValidPassword(password)) {

            request.setAttribute(
                    "error",
                    "Password must contain at least 8 characters, including at least 1 letter and 1 number."
            );

            request.getRequestDispatcher("/register.jsp")
                    .forward(request, response);

            return;
        }

        // =========================
        // 4. Confirm Password
        // =========================
        if (!password.equals(confirmPassword)) {

            request.setAttribute(
                    "error",
                    "Password and Confirm Password do not match."
            );

            request.getRequestDispatcher("/register.jsp")
                    .forward(request, response);

            return;
        }

        // =========================
        // 5. Phone
        // =========================
        if (!ValidationUtil.isValidPhone(phone)) {

            request.setAttribute(
                    "error",
                    "Phone must contain only 10-11 digits and start with 0."
            );

            request.getRequestDispatcher("/register.jsp")
                    .forward(request, response);

            return;
        }

        // =========================
        // 6. Address
        // =========================
        if (!ValidationUtil.isValidAddress(address)) {

            request.setAttribute(
                    "error",
                    "Address cannot exceed 255 characters."
            );

            request.getRequestDispatcher("/register.jsp")
                    .forward(request, response);

            return;
        }

        try {

            // =========================
            // 7. Check duplicate email
            // =========================
            if (userService.isEmailExists(email)) {

                request.setAttribute(
                        "error",
                        "This email is already registered."
                );

                request.getRequestDispatcher("/register.jsp")
                        .forward(request, response);

                return;
            }

            // =========================
            // 8. Register user
            // =========================
            boolean success = userService.registerUser(
                    fullName,
                    email,
                    password,
                    phone,
                    address
            );

            if (success) {

                // Registration successful
                request.setAttribute(
                        "success",
                        "Account created successfully! Please login to continue."
                );

                request.getRequestDispatcher(
                        "/login.jsp"
                ).forward(request, response);

                return;
            }

            // Register failed
            request.setAttribute(
                    "error",
                    "Registration failed. Please try again."
            );

            request.getRequestDispatcher(
                    "/register.jsp"
            ).forward(request, response);

        } catch (Exception e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "System error. Please try again later."
            );

            request.getRequestDispatcher(
                    "/register.jsp"
            ).forward(request, response);
        }
    }
}