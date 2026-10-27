package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.order.AdminOrderDAO;
import com.mycompany.onlinefruitshopping.service.order.AdminDeliveryService;
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
 * back into the queue or cancel it; the rules live in
 * {@link AdminDeliveryService}.
 */
@WebServlet("/admin/order-issues")
public class AdminOrderIssueServlet extends HttpServlet {

    private AdminOrderDAO orderDAO;
    private AdminDeliveryService deliveryService;

    @Override
    public void init() {
        orderDAO = new AdminOrderDAO();
        deliveryService = new AdminDeliveryService();
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

        Object userId = request.getSession().getAttribute("userId");
        int adminId = userId instanceof Integer ? (Integer) userId : 0;

        try {
            String message = deliveryService.resolveIssue(
                    orderId, resolution, note, adminId);

            response.sendRedirect(request.getContextPath()
                    + "/admin/order-issues?msg=" + message);

        } catch (SQLException e) {
            throw new ServletException("Cannot resolve order #" + orderId, e);
        }
    }
}
