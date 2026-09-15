package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.UserDAO;

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

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // User must verify OTP first
        if (session == null
                || !Boolean.TRUE.equals(
                        session.getAttribute("resetVerified"))) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }

        String email =
                (String) session.getAttribute("resetEmail");

        if (email == null || email.isBlank()) {

            session.invalidate();

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }

        request.setAttribute("email", email);

        request.getRequestDispatcher(
                "/reset-password.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        // Must verify OTP first
        if (session == null
                || !Boolean.TRUE.equals(
                        session.getAttribute("resetVerified"))) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }

        String email =
                (String) session.getAttribute("resetEmail");

        String newPassword =
                request.getParameter("newPassword");

        String confirmPassword =
                request.getParameter("confirmPassword");

        newPassword =
                newPassword == null
                        ? ""
                        : newPassword;

        confirmPassword =
                confirmPassword == null
                        ? ""
                        : confirmPassword;

        // Validate password
        if (!newPassword.equals(confirmPassword)) {

            request.setAttribute(
                    "error",
                    "Password and Confirm Password do not match."
            );

            request.setAttribute(
                    "email",
                    email
            );

            request.getRequestDispatcher(
                    "/reset-password.jsp"
            ).forward(request, response);

            return;
        }

        try {

            boolean success =
                    userDAO.resetPassword(
                            email,
                            newPassword
                    );

            if (success) {

                // Reset process completed
                session.removeAttribute("resetVerified");
                session.removeAttribute("resetEmail");

                response.sendRedirect(
                        request.getContextPath()
                                + "/login"
                );

                return;

            } else {

                request.setAttribute(
                        "error",
                        "Unable to reset password."
                );

                request.setAttribute(
                        "email",
                        email
                );

                request.getRequestDispatcher(
                        "/reset-password.jsp"
                ).forward(request, response);
            }

        } catch (Exception e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "System error. Please try again later."
            );

            request.setAttribute(
                    "email",
                    email
            );

            request.getRequestDispatcher(
                    "/reset-password.jsp"
            ).forward(request, response);
        }
    }
}