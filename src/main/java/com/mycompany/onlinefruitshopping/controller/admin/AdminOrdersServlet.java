package com.mycompany.onlinefruitshopping.controller.admin;

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
 * Monitor Orders (task 19.0): every order across all shops with
 * status counts, free-text search, status filter and pagination.
 */
@WebServlet("/admin/orders")
public class AdminOrdersServlet extends HttpServlet {

    private AdminOrderDAO orderDAO;

    @Override
    public void init() {
        orderDAO = new AdminOrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String status = AdminWeb.trim(request.getParameter("status"));
        String search = AdminWeb.trim(request.getParameter("search"));

        try {
            List<Map<String, Object>> all = orderDAO.findOrders(status, search);
            Page<Map<String, Object>> slice = Page.of(all, AdminWeb.intParam(request.getParameter("page"), 1));

            request.setAttribute("orders", slice);
            request.setAttribute("statusCounts", orderDAO.countOrdersByStatus());
            request.setAttribute("status", status);
            request.setAttribute("search", search);
            request.setAttribute("pagerBase",
                    AdminWeb.pagerBase(request, "/admin/orders", "status", "search"));

            request.getRequestDispatcher("/jsp/admin/admin-orders.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Cannot load orders", e);
        }
    }
}
