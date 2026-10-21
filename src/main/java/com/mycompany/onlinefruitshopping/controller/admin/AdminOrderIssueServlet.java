package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.OrderDAO;
import com.mycompany.onlinefruitshopping.dao.order.AdminOrderDAO;
import com.mycompany.onlinefruitshopping.utils.Page;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handle Order Issues (task 19.0): failed and cancelled orders,
 * plus orders that have been stuck in an open status for more than
 * three days. The administrator can put a failed or delayed order
 * back into the queue or cancel it, using the shared
 * {@code OrderDAO.updateOrderStatus} (shop id 0 means "any shop").
 */
@WebServlet("/admin/order-issues")
public class AdminOrderIssueServlet extends HttpServlet {

    private AdminOrderDAO orderDAO;
    private OrderDAO orderShopDAO;

    @Override
    public void init() {
        orderDAO = new AdminOrderDAO();
        orderShopDAO = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String search = AdminWeb.trim(request.getParameter("search"));

        try {
            List<Map<String, Object>> all = orderDAO.findIssueOrders(search);
            Page<Map<String, Object>> slice = Page.of(all, AdminWeb.intParam(request.getParameter("page"), 1));

            request.setAttribute("orders", slice);
            request.setAttribute("search", search);
            request.setAttribute("issueMessage", AdminWeb.trim(request.getParameter("msg")));
            request.setAttribute("pagerBase",
                    AdminWeb.pagerBase(request, "/admin/order-issues", "search"));

            request.getRequestDispatcher("/jsp/admin/admin-order-issues.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Cannot load order issues", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int orderId = AdminWeb.intParam(request.getParameter("orderId"), 0);
        String resolution = AdminWeb.trim(request.getParameter("resolution"));
        String note = AdminWeb.trim(request.getParameter("note"));
        String message = "invalid";

        // Reopen a problem order, or cancel it outright.
        String newStatus = "cancel".equals(resolution) ? "cancelled"
                : "reopen".equals(resolution) ? "preparing"
                : "";

        Object userId = request.getSession().getAttribute("userId");
        int adminId = userId instanceof Integer ? (Integer) userId : 0;

        try {
            if (orderId > 0 && !newStatus.isEmpty()) {
                boolean updated = orderShopDAO.updateOrderStatus(orderId, 0, newStatus, adminId,
                        note.isEmpty() ? "Handled by admin" : note);

                message = updated
                        ? ("cancel".equals(resolution) ? "cancelled" : "reopened")
                        : "failed";
            }

            response.sendRedirect(request.getContextPath()
                    + "/admin/order-issues?msg=" + message);

        } catch (IllegalStateException | IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/order-issues?msg=terminal");

        } catch (SQLException e) {
            throw new ServletException("Cannot resolve order #" + orderId, e);
        }
    }
}
