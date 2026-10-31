package com.mycompany.onlinefruitshopping.controller.order;

import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller Quản lý Đơn hàng phía Khách hàng (Customer Order Management & Tracking).
 * Feature 6: Tasks 7 - 11 (View History, View Details, Cancel Order, Re-order, Track Status).
 *
 * Tuân thủ mô hình 3 tầng:
 * - Servlet điều phối, gọi OrderService xử lý logic.
 */
@WebServlet(urlPatterns = {"/orders", "/orders/detail", "/orders/cancel", "/orders/reorder", "/orders/track"})
public class CustomerOrderServlet extends HttpServlet {

    private final OrderService orderService;

    public CustomerOrderServlet() {
        this.orderService = new OrderService();
    }

    public CustomerOrderServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String path = request.getServletPath();
        String action = request.getParameter("action");

        try {
            if ("/orders/detail".equals(path) || "detail".equalsIgnoreCase(action)
                    || "/orders/track".equals(path) || "track".equalsIgnoreCase(action)) {
                handleViewDetail(request, response, user.getUserID());
            } else {
                handleListOrders(request, response, user.getUserID());
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in CustomerOrderServlet doGet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String path = request.getServletPath();
        String action = request.getParameter("action");
        if (action == null) {
            if (path.endsWith("cancel")) action = "cancel";
            else if (path.endsWith("reorder")) action = "reorder";
        }

        try {
            switch (action != null ? action : "") {
                case "cancel":
                    handleCancelOrder(request, response, user.getUserID());
                    break;
                case "reorder":
                    handleReorder(request, response, user.getUserID());
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/orders");
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in CustomerOrderServlet doPost", e);
        }
    }

    private void handleListOrders(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {

        String status = request.getParameter("status");
        if (status == null || status.isBlank()) {
            status = "all";
        }
        String searchQuery = request.getParameter("q");

        List<Order> orders = orderService.getCustomerOrders(userId, status, searchQuery);

        request.setAttribute("orders", orders);
        request.setAttribute("currentStatus", status);
        request.setAttribute("searchQuery", searchQuery);

        request.getRequestDispatcher("/customer-orders.jsp").forward(request, response);
    }

    private void handleViewDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {

        String idStr = request.getParameter("id");
        int orderId = 0;
        if (idStr != null) {
            try {
                orderId = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {}
        }

        if (orderId <= 0) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        Order order = orderService.getCustomerOrderDetail(orderId, userId);
        if (order == null) {
            response.sendRedirect(request.getContextPath() + "/orders?error=not_found");
            return;
        }

        request.setAttribute("order", order);
        request.getRequestDispatcher("/customer-order-detail.jsp").forward(request, response);
    }

    private void handleCancelOrder(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, IOException {

        String idStr = request.getParameter("orderId");
        int orderId = 0;
        if (idStr != null) {
            try {
                orderId = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {}
        }

        String reason = request.getParameter("cancelReason");
        if (reason == null || reason.isBlank()) {
            reason = "Khách hàng yêu cầu hủy đơn";
        }

        try {
            orderService.cancelCustomerOrder(orderId, userId, reason);
            String successMsg = URLEncoder.encode("Đã hủy đơn hàng #" + orderId + " thành công!", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&msg=" + successMsg);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            String errorMsg = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&error=" + errorMsg);
        }
    }

    private void handleReorder(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, IOException {

        String idStr = request.getParameter("orderId");
        int orderId = 0;
        if (idStr != null) {
            try {
                orderId = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            int itemsCount = orderService.reorder(orderId, userId);
            String msg = URLEncoder.encode("Đã thêm lại " + itemsCount + " sản phẩm từ đơn hàng #" + orderId + " vào giỏ hàng!", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/cart?msg=" + msg);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            String errorMsg = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/orders?error=" + errorMsg);
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
