package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.OrderDAO;
import com.mycompany.onlinefruitshopping.dao.order.AdminOrderDAO;
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
 * View Order Details (task 19.0): full order breakdown for the
 * administrator â€” customer, shop, address, items, payment records,
 * delivery assignment and status history.
 */
@WebServlet("/admin/order-detail")
public class AdminOrderDetailServlet extends HttpServlet {

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

        int orderId = AdminWeb.intParam(request.getParameter("orderId"), 0);

        if (orderId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin/orders?msg=badorder");
            return;
        }

        try {
            Map<String, Object> order = orderDAO.getOrderDetail(orderId);

            if (order == null) {
                response.sendRedirect(request.getContextPath() + "/admin/orders?msg=notfound");
                return;
            }

            request.setAttribute("order", order);
            request.setAttribute("items", orderShopDAO.getOrderItems(orderId));
            request.setAttribute("history", orderShopDAO.getOrderHistory(orderId));
            request.setAttribute("payments", orderDAO.getPayments(orderId));
            request.setAttribute("delivery", orderDAO.getDeliveryByOrder(orderId));

            request.getRequestDispatcher("/jsp/admin/admin-order-detail.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Cannot load order #" + orderId, e);
        }
    }
}
