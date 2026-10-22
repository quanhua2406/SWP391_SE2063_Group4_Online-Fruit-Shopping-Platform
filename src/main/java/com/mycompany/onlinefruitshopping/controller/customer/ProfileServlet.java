package com.mycompany.onlinefruitshopping.controller.customer;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.user.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserService userService = new UserService();


    // =========================================================
    // GET
    // View Profile / Show Edit Profile
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        if (session == null ||
                session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }


        User sessionUser =
                (User) session.getAttribute("user");


        try {

            // Get newest information from database
            User user =
                    userService.getUserById(
                            sessionUser.getUserID()
                    );


            if (user == null) {

                session.invalidate();

                response.sendRedirect(
                        request.getContextPath() + "/login"
                );

                return;
            }


            // Update session with latest user information
            session.setAttribute(
                    "user",
                    user
            );

            session.setAttribute(
                    "roleName",
                    user.getRoleName()
            );


            // -------------------------------------------------
            // CHECK ACTION
            // -------------------------------------------------

            String action =
                    request.getParameter("action");


            if ("edit".equalsIgnoreCase(action)) {

                request.setAttribute(
                        "user",
                        user
                );

                request.getRequestDispatcher(
                        "/jsp/customer/edit-profile.jsp"
                ).forward(request, response);

            } else {

                request.setAttribute(
                        "user",
                        user
                );

                request.getRequestDispatcher(
                        "/jsp/customer/profile.jsp"
                ).forward(request, response);
            }


        } catch (SQLException e) {

            throw new ServletException(
                    "Cannot load profile.",
                    e
            );
        }
    }


    // =========================================================
    // POST
    // Update Profile
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");


        HttpSession session =
                request.getSession(false);


        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        if (session == null ||
                session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }


        User sessionUser =
                (User) session.getAttribute("user");


        // -----------------------------------------------------
        // GET FORM DATA
        // -----------------------------------------------------

        String fullName =
                request.getParameter("fullName");

        String phone =
                request.getParameter("phone");


        // Handle null
        if (fullName == null) {
            fullName = "";
        }

        if (phone == null) {
            phone = "";
        }


        fullName = fullName.trim();
        phone = phone.trim();


        // -----------------------------------------------------
        // VALIDATE FULL NAME
        // -----------------------------------------------------

        if (fullName.isEmpty()) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Full name cannot be empty."
            );

            return;
        }


        // At least 2 characters
        if (fullName.length() < 2) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Full name must contain at least 2 characters."
            );

            return;
        }


        // Maximum 100 characters
        if (fullName.length() > 100) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Full name cannot exceed 100 characters."
            );

            return;
        }


        // Must contain at least one letter
        if (!fullName.matches(".*\\p{L}.*")) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Full name must contain letters."
            );

            return;
        }


        // Only letters and spaces
        if (!fullName.matches("[\\p{L}\\s]+")) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Full name can contain only letters and spaces."
            );

            return;
        }


        // -----------------------------------------------------
        // VALIDATE PHONE
        // -----------------------------------------------------

        if (phone.isEmpty()) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Phone number cannot be empty."
            );

            return;
        }


        // Only digits
        if (!phone.matches("\\d+")) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Phone number can contain only digits."
            );

            return;
        }


        // 9 - 11 digits
        if (phone.length() < 9 ||
                phone.length() > 11) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Phone number must contain 9 to 11 digits."
            );

            return;
        }


        // -----------------------------------------------------
        // UPDATE DATABASE
        // -----------------------------------------------------

        try {

            boolean updated =
                    userService.updateProfile(
                            sessionUser.getUserID(),
                            fullName,
                            phone
                    );


            if (!updated) {

                showEditError(
                        request,
                        response,
                        sessionUser,
                        "Failed to update profile."
                );

                return;
            }


            // -------------------------------------------------
            // GET UPDATED USER
            // -------------------------------------------------

            User updatedUser =
                    userService.getUserById(
                            sessionUser.getUserID()
                    );


            if (updatedUser != null) {

                session.setAttribute(
                        "user",
                        updatedUser
                );

                session.setAttribute(
                        "roleName",
                        updatedUser.getRoleName()
                );
            }


            // -------------------------------------------------
            // SUCCESS
            // -------------------------------------------------

            response.sendRedirect(
                    request.getContextPath()
                            + "/profile?success=updated"
            );


        } catch (SQLException e) {

            throw new ServletException(
                    "Cannot update profile.",
                    e
            );
        }
    }


    // =========================================================
    // SHOW EDIT ERROR
    // =========================================================

    private void showEditError(
            HttpServletRequest request,
            HttpServletResponse response,
            User user,
            String error)
            throws ServletException, IOException {

        request.setAttribute(
                "user",
                user
        );

        request.setAttribute(
                "error",
                error
        );

        request.getRequestDispatcher(
                "/jsp/customer/edit-profile.jsp"
        ).forward(request, response);
    }
}