package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    // =========================================================
    // GET - SHOW LOGIN PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already logged in,
        // redirect to home
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("user") != null) {

            response.sendRedirect(
                    request.getContextPath() + "/home"
            );

            return;
        }

        request.getRequestDispatcher(
                "/login.jsp"
        ).forward(request, response);
    }


    // =========================================================
    // POST - LOGIN
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // =====================================================
        // GET FORM DATA
        // =====================================================

        String email = request.getParameter("email");
        String password = request.getParameter("password");


        // Handle null
        if (email == null) {
            email = "";
        }

        if (password == null) {
            password = "";
        }


        // Remove spaces from email
        email = email.trim();


        // =====================================================
        // VALIDATE EMAIL
        // =====================================================

        if (email.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Email cannot be empty."
            );

            request.setAttribute(
                    "email",
                    email
            );

            request.getRequestDispatcher(
                    "/login.jsp"
            ).forward(request, response);

            return;
        }


        // =====================================================
        // VALIDATE EMAIL FORMAT
        // =====================================================

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            request.setAttribute(
                    "error",
                    "Please enter a valid email address."
            );

            request.setAttribute(
                    "email",
                    email
            );

            request.getRequestDispatcher(
                    "/login.jsp"
            ).forward(request, response);

            return;
        }


        // =====================================================
        // VALIDATE PASSWORD
        // =====================================================

        if (password.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Password cannot be empty."
            );

            request.setAttribute(
                    "email",
                    email
            );

            request.getRequestDispatcher(
                    "/login.jsp"
            ).forward(request, response);

            return;
        }


        // =====================================================
        // LOGIN
        // =====================================================

        try {

            User user =
                    userService.getUserByLogin(
                            email,
                            password
                    );


            // =================================================
            // LOGIN FAILED
            // =================================================

            if (user == null) {

                request.setAttribute(
                        "error",
                        "Invalid email or password."
                );

                request.setAttribute(
                        "email",
                        email
                );

                request.getRequestDispatcher(
                        "/login.jsp"
                ).forward(request, response);

                return;
            }


            // =================================================
            // LOGIN SUCCESS
            // =================================================

            HttpSession session =
                    request.getSession(true);


            // Store user
            session.setAttribute(
                    "user",
                    user
            );
            session.setAttribute(
                    "userId",
                    user.getUserID()
            );


            // Store role
            session.setAttribute(
                    "roleName",
                    user.getRoleName()
            );


            // Check redirectAfterLogin if user was previously trying to access checkout or my-subscriptions
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                session.removeAttribute("redirectAfterLogin");
                response.sendRedirect(redirectUrl);
            } else {
                // Redirect to home
                response.sendRedirect(
                        request.getContextPath()
                                + "/home"
                );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Login failed.",
                    e
            );
        }
    }
}