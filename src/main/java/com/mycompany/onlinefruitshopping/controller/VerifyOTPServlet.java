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


    // =========================================================
    // GET - SHOW VERIFY OTP PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        // -----------------------------------------------------
        // CHECK RESET SESSION
        // -----------------------------------------------------

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


    // =========================================================
    // POST - VERIFY OTP
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
        // GET EMAIL FROM SESSION
        // -----------------------------------------------------

        String email =
                (String) session.getAttribute(
                        "resetEmail"
                );


        // -----------------------------------------------------
        // GET OTP
        // -----------------------------------------------------

        String otp =
                request.getParameter("otp");


        if (otp == null) {
            otp = "";
        }

        otp = otp.trim();


        // -----------------------------------------------------
        // VALIDATE OTP EMPTY
        // -----------------------------------------------------

        if (otp.isEmpty()) {

            request.setAttribute(
                    "error",
                    "OTP cannot be empty."
            );

            request.getRequestDispatcher(
                    "/verify-otp.jsp"
            ).forward(request, response);

            return;
        }


        // -----------------------------------------------------
        // VALIDATE OTP FORMAT
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // VERIFY OTP
        // -----------------------------------------------------

        try {

            boolean verified =
                    userDAO.verifyResetOTP(
                            email,
                            otp
                    );


            if (!verified) {

                request.setAttribute(
                        "error",
                        "Invalid or expired OTP."
                );

                request.getRequestDispatcher(
                        "/verify-otp.jsp"
                ).forward(request, response);

                return;
            }


            // -------------------------------------------------
            // OTP VERIFIED
            // -------------------------------------------------

            session.setAttribute(
                    "resetVerified",
                    true
            );


            // -------------------------------------------------
            // GO TO RESET PASSWORD
            // -------------------------------------------------

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