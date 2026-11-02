package com.mycompany.onlinefruitshopping.controller.auth;

import com.mycompany.onlinefruitshopping.service.auth.PasswordResetService;
import com.mycompany.onlinefruitshopping.service.auth.PasswordResetService.RequestStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    /**
     * Shown for both "account not found" and "OTP sent" cases so the
     * screen never reveals whether an email is registered.
     */
    private static final String GENERIC_SUCCESS =
            "Nếu email này đã được đăng ký, OTP đã được gửi đến email đó.";

    private final PasswordResetService passwordResetService =
            new PasswordResetService();

    // =========================================================
    // GET - SHOW FORGOT PASSWORD PAGE
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/jsp/auth/forgot-password.jsp"
        ).forward(request, response);
    }

    // =========================================================
    // POST - REQUEST OTP
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");

        email = email == null ? "" : email.trim();

        request.setAttribute("email", email);

        try {

            RequestStatus status =
                    passwordResetService.requestReset(email);

            switch (status) {

                case INVALID_EMAIL ->

                    showError(request, response,
                            "Vui lòng nhập địa chỉ email hợp lệ.");

                case ACCOUNT_NOT_FOUND ->

                    // Do not reveal that the account is missing.
                    showSuccess(request, response, GENERIC_SUCCESS);

                case SEND_FAILED ->

                    showError(request, response,
                            "Không thể gửi OTP. Vui lòng thử lại.");

                case OTP_SENT -> {

                    HttpSession session = request.getSession();

                    session.setAttribute("resetEmail", email);
                    session.removeAttribute("resetVerified");

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/verify-otp"
                    );
                }
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể tạo yêu cầu đặt lại mật khẩu.",
                    e
            );
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);

        request.getRequestDispatcher(
                "/jsp/auth/forgot-password.jsp"
        ).forward(request, response);
    }

    private void showSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("success", message);

        request.getRequestDispatcher(
                "/jsp/auth/forgot-password.jsp"
        ).forward(request, response);
    }
}
