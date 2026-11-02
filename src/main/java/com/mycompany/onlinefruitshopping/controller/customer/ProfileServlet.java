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
                    "Không thể tải thông tin hồ sơ.",
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
                    "Họ và tên không được để trống."
            );

            return;
        }


        // At least 2 characters
        if (fullName.length() < 2) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Họ và tên phải có ít nhất 2 ký tự."
            );

            return;
        }


        // Maximum 100 characters
        if (fullName.length() > 100) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Họ và tên không được vượt quá 100 ký tự."
            );

            return;
        }


        // Must contain at least one letter
        if (!fullName.matches(".*\\p{L}.*")) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Họ và tên phải chứa chữ cái."
            );

            return;
        }


        // Only letters and spaces
        if (!fullName.matches("[\\p{L}\\s]+")) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Họ và tên chỉ được chứa chữ cái và khoảng trắng."
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
                    "Số điện thoại không được để trống."
            );

            return;
        }


        // Only digits
        if (!phone.matches("\\d+")) {

            showEditError(
                    request,
                    response,
                    sessionUser,
                    "Số điện thoại chỉ được chứa chữ số."
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
                    "Số điện thoại phải gồm 9 đến 11 chữ số."
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
                        "Cập nhật hồ sơ thất bại."
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
                    "Không thể cập nhật hồ sơ.",
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