
package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.AdminUserCreationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet("/admin/user-add")
public class AdminUserAddServlet extends HttpServlet {

    private AdminUserCreationDAO userDAO;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{9,11}$");

    @Override
    public void init() {
        userDAO = new AdminUserCreationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/admin-user-add.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = value(request, "fullName").trim();
        String email = value(request, "email").trim();
        String password = value(request, "password");
        String confirmPassword = value(request, "confirmPassword");
        String phone = value(request, "phone").trim();
        String role = value(request, "role").trim();

        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("role", role);

        if (fullName.isBlank() || email.isBlank()
                || password.isBlank() || confirmPassword.isBlank()
                || role.isBlank()) {
            showError(request, response, "Please complete all required fields.");
            return;
        }

        if (fullName.length() > 100) {
            showError(request, response, "Full name must not exceed 100 characters.");
            return;
        }

        if (email.length() > 150 || !EMAIL_PATTERN.matcher(email).matches()) {
            showError(request, response, "Please enter a valid email address.");
            return;
        }

        if (password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !password.matches("(?s).*[A-Za-z].*")
                || !password.matches("(?s).*[0-9].*")) {
            showError(request, response,
                    "Password must contain at least 8 characters, a letter and a number, and be no more than 72 UTF-8 bytes.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(request, response, "Passwords do not match.");
            return;
        }

        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showError(request, response, "Phone number must contain 9 to 11 digits.");
            return;
        }

        if (!isAllowedRole(role)) {
            showError(request, response, "Please select a valid role.");
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                showError(request, response, "This email is already registered.");
                return;
            }

            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));

            userDAO.createUser(
                    fullName,
                    email,
                    passwordHash,
                    phone.isEmpty() ? null : phone,
                    role
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/user-management?message=created"
            );

        } catch (IllegalArgumentException e) {
            showError(request, response,
                    "The selected role is not configured in the database.");
        } catch (SQLException e) {
            getServletContext().log("Failed to create user.", e);
            showError(request, response,
                    "Could not create account. Check the email and database role configuration.");
        }
    }

    private String value(HttpServletRequest request, String name) {
        String result = request.getParameter(name);
        return result == null ? "" : result;
    }

    private boolean isAllowedRole(String role) {
        return switch (role.toUpperCase()) {
            case "CUSTOMER", "SHOPOWNER", "SHOP_OWNER",
                 "DELIVERYSTAFF", "DELIVERY_STAFF", "ADMIN", "GUEST" -> true;
            default -> false;
        };
    }

    private void showError(HttpServletRequest request,
                           HttpServletResponse response,
                           String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);

        request.getRequestDispatcher("/WEB-INF/admin-user-add.jsp")
               .forward(request, response);
    }
}
