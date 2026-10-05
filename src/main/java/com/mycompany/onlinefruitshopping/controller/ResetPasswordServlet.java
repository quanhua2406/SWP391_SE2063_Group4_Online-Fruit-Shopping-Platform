package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.UserDAO;
import com.mycompany.onlinefruitshopping.utils.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();


    // =========================================================
    // GET - SHOW RESET PASSWORD PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);


        // -----------------------------------------------------
        // CHECK RESET EMAIL
        // -----------------------------------------------------

        if (session == null
                || session.getAttribute("resetEmail") == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }


        // -----------------------------------------------------
        // CHECK OTP VERIFIED
        // -----------------------------------------------------

        Boolean resetVerified =
                session.getAttribute("resetVerified") != null
                        ? (Boolean) session.getAttribute("resetVerified")
                        : false;


        if (!resetVerified) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/verify-otp"
            );

            return;
        }


        request.getRequestDispatcher(
                "/reset-password.jsp"
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


        HttpSession session =
                request.getSession(false);


        // -----------------------------------------------------
        // CHECK SESSION
        // -----------------------------------------------------

        if (session == null
                || session.getAttribute("resetEmail") == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }


        // -----------------------------------------------------
        // CHECK OTP VERIFIED
        // -----------------------------------------------------

        Boolean resetVerified =
                session.getAttribute("resetVerified") != null
                        ? (Boolean) session.getAttribute("resetVerified")
                        : false;


        if (!resetVerified) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/verify-otp"
            );

            return;
        }


        // -----------------------------------------------------
        // GET EMAIL
        // -----------------------------------------------------

        String email =
                (String) session.getAttribute(
                        "resetEmail"
                );


        // -----------------------------------------------------
        // GET PASSWORD
        // -----------------------------------------------------

        String password =
                request.getParameter("password");


        String confirmPassword =
                request.getParameter(
                        "confirmPassword"
                );


        if (password == null) {
            password = "";
        }

        if (confirmPassword == null) {
            confirmPassword = "";
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


        if (!password.matches(".*[A-Za-z].*")) {

            showError(
                    request,
                    response,
                    "Password must contain at least one letter."
            );

            return;
        }


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
        // RESET PASSWORD
        // =====================================================

        try {

            boolean reset =
                    userDAO.resetPassword(
                            email,
                            password
                    );


            if (!reset) {

                showError(
                        request,
                        response,
                        "Unable to reset password."
                );

                return;
            }


            // -------------------------------------------------
            // CLEAR RESET SESSION
            // -------------------------------------------------

            session.removeAttribute(
                    "resetEmail"
            );

            session.removeAttribute(
                    "resetVerified"
            );


            // -------------------------------------------------
            // GO TO LOGIN
            // -------------------------------------------------

            response.sendRedirect(
                    request.getContextPath()
                            + "/login?reset=success"
            );


        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    request,
                    response,
                    "Unable to reset password."
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

        request.getRequestDispatcher(
                "/reset-password.jsp"
        ).forward(request, response);
    }
}