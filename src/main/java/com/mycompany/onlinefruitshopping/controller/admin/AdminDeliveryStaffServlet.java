package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.order.AdminOrderDAO;
import com.mycompany.onlinefruitshopping.service.order.AdminDeliveryService;
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
 * Manage Delivery Staff (task 19.0): the administrator sees every
 * delivery-staff account with its workload and can lock or unlock
 * the account. Blocking reuses the same {@code users.status}
 * column as the general user-management screen; the rules live in
 * {@link AdminDeliveryService}.
 */
@WebServlet("/admin/delivery-staff")
public class AdminDeliveryStaffServlet extends HttpServlet {

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

        try {
            request.setAttribute("staff", orderDAO.findDeliveryStaff());
            request.setAttribute("assignMessage", AdminWeb.trim(request.getParameter("msg")));

            request.getRequestDispatcher("/jsp/admin/admin-delivery-staff.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Cannot load delivery staff", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int staffId = AdminWeb.intParam(request.getParameter("staffId"), 0);
        String action = AdminWeb.trim(request.getParameter("action"));

        try {
            String message = deliveryService.changeStaffStatus(staffId, action);

            response.sendRedirect(request.getContextPath()
                    + "/admin/delivery-staff?msg=" + message);

        } catch (SQLException e) {
            throw new ServletException("Cannot update delivery staff #" + staffId, e);
        }
    }
}
