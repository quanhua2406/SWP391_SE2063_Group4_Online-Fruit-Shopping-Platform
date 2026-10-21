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
 * Manage Delivery Assignments (task 19.0): the shop owner assigns
 * a shipper when the order is prepared; this screen lets the
 * administrator step in and reassign â€” or cancel â€” an assignment
 * "if needed" (see the project tracking).
 */
@WebServlet("/admin/delivery-assign")
public class AdminDeliveryAssignServlet extends HttpServlet {

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
            List<Map<String, Object>> all = orderDAO.findAssignableOrders(search);
            Page<Map<String, Object>> slice = Page.of(all, AdminWeb.intParam(request.getParameter("page"), 1));

            request.setAttribute("orders", slice);
            request.setAttribute("staff", orderDAO.findDeliveryStaff());
            request.setAttribute("search", search);
            request.setAttribute("assignMessage", AdminWeb.trim(request.getParameter("msg")));
            request.setAttribute("pagerBase",
                    AdminWeb.pagerBase(request, "/admin/delivery-assign", "search"));

            request.getRequestDispatcher("/jsp/admin/admin-delivery-assign.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Cannot load assignments", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int orderId = AdminWeb.intParam(request.getParameter("orderId"), 0);
        int staffId = AdminWeb.intParam(request.getParameter("staffId"), 0);
        int deliveryId = AdminWeb.intParam(request.getParameter("deliveryId"), 0);
        String action = AdminWeb.trim(request.getParameter("action"));
        String note = AdminWeb.trim(request.getParameter("note"));

        try {
            String message = deliveryService.applyAssignment(
                    action, orderId, deliveryId, staffId, note);

            response.sendRedirect(request.getContextPath()
                    + "/admin/delivery-assign?msg=" + message);

        } catch (SQLException e) {
            throw new ServletException("Cannot update assignment for order #" + orderId, e);
        }
    }
}
