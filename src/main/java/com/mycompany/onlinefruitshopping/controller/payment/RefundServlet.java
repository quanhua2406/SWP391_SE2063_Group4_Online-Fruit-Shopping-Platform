package com.mycompany.onlinefruitshopping.controller.payment;

import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.Payment;
import com.mycompany.onlinefruitshopping.model.Refund;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.OrderService;
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
 * Servlet xử lý yêu cầu hoàn tiền và phê duyệt hoàn tiền (Tasks 6 & 7: Process Refunds & Refund Approval).
 * Feature 7: Payment Management - In Charge: Le Duc Cuong (cuongld)
 */
@WebServlet(name = "RefundServlet", urlPatterns = {
        "/refunds",
        "/refunds/request",
        "/admin/refunds",
        "/store-owner/refunds"
})
public class RefundServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String path = request.getServletPath();

        try {
            switch (path) {
                case "/refunds/request":
                    showRefundRequestForm(request, response, user);
                    break;
                case "/admin/refunds":
                case "/store-owner/refunds":
                    showAdminRefunds(request, response, user);
                    break;
                case "/refunds":
                default:
                    showCustomerRefunds(request, response, user);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in RefundServlet doGet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String path = request.getServletPath();

        try {
            switch (path) {
                case "/refunds/request":
                    handleCreateRefundRequest(request, response, user);
                    break;
                case "/admin/refunds":
                case "/store-owner/refunds":
                    handleProcessRefund(request, response, user);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/refunds");
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in RefundServlet doPost", e);
        }
    }

    private void showRefundRequestForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {

        String orderIdStr = request.getParameter("orderId");
        int orderId = 0;
        if (orderIdStr != null && !orderIdStr.isBlank()) {
            try {
                orderId = Integer.parseInt(orderIdStr);
            } catch (NumberFormatException ignored) {}
        }

        if (orderId <= 0) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        int checkUserId = "admin".equalsIgnoreCase(user.getRoleName()) || "store_owner".equalsIgnoreCase(user.getRoleName()) ? 0 : user.getUserID();
        Order order = orderService.getOrderDetail(orderId, checkUserId);
        Payment payment = paymentService.getPaymentByOrderId(orderId);

        if (order == null || payment == null) {
            response.sendRedirect(request.getContextPath() + "/orders?error=order_not_found");
            return;
        }

        request.setAttribute("order", order);
        request.setAttribute("payment", payment);
        request.getRequestDispatcher("/refund-request.jsp").forward(request, response);
    }

    private void handleCreateRefundRequest(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {

        String orderIdStr = request.getParameter("orderId");
        int orderId = 0;
        if (orderIdStr != null && !orderIdStr.isBlank()) {
            try {
                orderId = Integer.parseInt(orderIdStr);
            } catch (NumberFormatException ignored) {}
        }

        String reasonCategory = request.getParameter("reasonCategory");
        String reasonNote = request.getParameter("reasonNote");

        StringBuilder fullReason = new StringBuilder();
        if (reasonCategory != null && !reasonCategory.isBlank()) {
            fullReason.append("[").append(reasonCategory.trim()).append("] ");
        }
        if (reasonNote != null && !reasonNote.isBlank()) {
            fullReason.append(reasonNote.trim());
        }

        try {
            paymentService.requestRefund(user.getUserID(), orderId, fullReason.toString());
            response.sendRedirect(request.getContextPath() + "/refunds?success=requested");
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("errorMessage", e.getMessage());
            showRefundRequestForm(request, response, user);
        }
    }

    private void showCustomerRefunds(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {

        List<Refund> refundList = paymentService.getCustomerRefunds(user.getUserID());
        request.setAttribute("refundList", refundList);
        request.setAttribute("isCustomerView", true);
        request.getRequestDispatcher("/refund-request.jsp").forward(request, response);
    }

    private void showAdminRefunds(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {

        String role = user.getRoleName() != null ? user.getRoleName().toLowerCase() : "";
        if (!"admin".equals(role) && !"store_owner".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        String statusFilter = request.getParameter("status");
        List<Refund> refundList = paymentService.getAllRefunds(statusFilter);

        request.setAttribute("refundList", refundList);
        request.setAttribute("statusFilter", statusFilter != null ? statusFilter : "all");
        request.getRequestDispatcher("/admin-refunds.jsp").forward(request, response);
    }

    private void handleProcessRefund(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, IOException {

        String role = user.getRoleName() != null ? user.getRoleName().toLowerCase() : "";
        if (!"admin".equals(role) && !"store_owner".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        String action = request.getParameter("action");
        String refundIdStr = request.getParameter("refundId");
        int refundId = 0;
        if (refundIdStr != null && !refundIdStr.isBlank()) {
            try {
                refundId = Integer.parseInt(refundIdStr);
            } catch (NumberFormatException ignored) {}
        }

        if (refundId > 0) {
            if ("approve".equalsIgnoreCase(action)) {
                paymentService.approveRefund(refundId, user.getUserID());
            } else if ("reject".equalsIgnoreCase(action)) {
                paymentService.rejectRefund(refundId, user.getUserID());
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/refunds?success=processed");
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
