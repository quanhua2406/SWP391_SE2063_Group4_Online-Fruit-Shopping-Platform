package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/verify-otp")
public class VerifyOTPServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();


    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("resetEmail") == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }

        request.getRequestDispatcher(
                "/verify-otp.jsp"
        ).forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");


        // =====================================================
        // SESSION
        // =====================================================

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }


        // =====================================================
        // GET EMAIL FROM SESSION
        // =====================================================

        String email =
                (String) session.getAttribute(
                        "resetEmail"
                );

        if (email == null || email.isBlank()) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/forgot-password"
            );

            return;
        }


        // =====================================================
        // GET OTP
        // =====================================================

        String otp =
                request.getParameter("otp");

        otp = otp == null
                ? ""
                : otp.trim();


        // =====================================================
        // VALIDATE OTP FORMAT
        // =====================================================

        if (!otp.matches("\\d{6}")) {

            request.setAttribute(
                    "error",
                    "OTP must contain exactly 6 digits."
            );

            request.getRequestDispatcher(
                    "/verify-otp.jsp"
            ).forward(request, response);

            return;
        }


        try {

            // =================================================
            // VERIFY
            // =================================================

            boolean valid =
                    userDAO.verifyResetOTP(
                            email,
                            otp
                    );


            if (!valid) {

                request.setAttribute(
                        "error",
                        "Invalid or expired OTP."
                );

                request.getRequestDispatcher(
                        "/verify-otp.jsp"
                ).forward(request, response);

                return;
            }


            // =================================================
            // OTP SUCCESS
            // =================================================

            session.setAttribute(
                    "resetVerified",
                    true
            );


            response.sendRedirect(
                    request.getContextPath()
                            + "/reset-password"
            );

        } catch (Exception e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to verify OTP."
            );

            request.getRequestDispatcher(
                    "/verify-otp.jsp"
            ).forward(request, response);
        }
    }
}