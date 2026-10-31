package com.mycompany.onlinefruitshopping.controller.payment;

import com.mycompany.onlinefruitshopping.model.Payment;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.PaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet quản lý xem nhật ký lịch sử giao dịch thanh toán (Task 5: View Payment History).
 * Hỗ trợ Khách hàng xem lịch sử cá nhân và Admin / Store Owner quản lý toàn bộ giao dịch.
 * Feature 7: Payment Management - In Charge: Le Duc Cuong (cuongld)
 */
@WebServlet(name = "PaymentHistoryServlet", urlPatterns = {
        "/payments",
        "/store-owner/payments",
        "/admin/payments"
})
public class PaymentHistoryServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String path = request.getServletPath();
        String roleName = user.getRoleName() != null ? user.getRoleName().toLowerCase() : "";
        boolean isStaffOrAdmin = "admin".equals(roleName) || "store_owner".equals(roleName)
                || path.startsWith("/admin/") || path.startsWith("/store-owner/");

        String statusFilter = request.getParameter("status");
        String methodFilter = request.getParameter("method");
        String searchQuery = request.getParameter("q");

        try {
            List<Payment> paymentList;
            if (isStaffOrAdmin) {
                paymentList = paymentService.getAllPayments(statusFilter, methodFilter, searchQuery);
            } else {
                paymentList = paymentService.getCustomerPayments(user.getUserID());
            }

            request.setAttribute("paymentList", paymentList);
            request.setAttribute("statusFilter", statusFilter != null ? statusFilter : "all");
            request.setAttribute("methodFilter", methodFilter != null ? methodFilter : "all");
            request.setAttribute("searchQuery", searchQuery != null ? searchQuery : "");
            request.setAttribute("isStaffOrAdmin", isStaffOrAdmin);

            request.getRequestDispatcher("/payment-history.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error in PaymentHistoryServlet", e);
        }
    }

    private User getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") instanceof User user) {
            return user;
        }
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + request.getRequestURI());
        return null;
    }
}
