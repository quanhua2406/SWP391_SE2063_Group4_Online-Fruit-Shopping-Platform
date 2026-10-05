package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.UserDAO;
import com.mycompany.onlinefruitshopping.utils.EmailService;
import com.mycompany.onlinefruitshopping.utils.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    private static final SecureRandom RANDOM =
            new SecureRandom();


    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/forgot-password.jsp"
        ).forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");

        email = email == null
                ? ""
                : email.trim();

        request.setAttribute(
                "email",
                email
        );


        // =====================================================
        // VALIDATE EMAIL
        // =====================================================

        if (!ValidationUtil.isValidEmail(email)) {

            request.setAttribute(
                    "error",
                    "Please enter a valid Gmail address."
            );

            request.getRequestDispatcher(
                    "/forgot-password.jsp"
            ).forward(request, response);

            return;
        }


        try {

            // =================================================
            // CHECK EMAIL
            // =================================================

            boolean exists =
                    userDAO.emailExists(email);


            /*
             *
             */

            if (!exists) {

                request.setAttribute(
                        "success",
                        "If this Gmail is registered, an OTP has been sent to your email."
                );

                request.getRequestDispatcher(
                        "/forgot-password.jsp"
                ).forward(request, response);

                return;
            }


            // =================================================
            // GENERATE OTP
            // =================================================

            String otp = generateOTP();


            // =================================================
            // SAVE OTP
            // =================================================

            boolean saved =
                    userDAO.saveResetOTP(
                            email,
                            otp
                    );

            if (!saved) {

                request.setAttribute(
                        "error",
                        "Unable to create reset request."
                );

                request.getRequestDispatcher(
                        "/forgot-password.jsp"
                ).forward(request, response);

                return;
            }


            // =================================================
            // SEND OTP
            // =================================================

            EmailService.sendOTP(
                    email,
                    otp
            );


            // =================================================
            // SESSION
            // =================================================

            HttpSession session =
                    request.getSession();

            session.setAttribute(
                    "resetEmail",
                    email
            );

            session.removeAttribute(
                    "resetVerified"
            );


            // =================================================
            // GO TO VERIFY OTP
            // =================================================

            response.sendRedirect(
                    request.getContextPath()
                            + "/verify-otp"
            );

        } catch (Exception e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to send OTP: "
                            + e.getMessage()
            );

            request.getRequestDispatcher(
                    "/forgot-password.jsp"
            ).forward(request, response);
        }
    }


    private String generateOTP() {

        int number =
                100000
                        + RANDOM.nextInt(900000);

        return String.valueOf(number);
    }
}