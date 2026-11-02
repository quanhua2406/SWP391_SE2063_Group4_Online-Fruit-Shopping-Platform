package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.NotificationService;
import com.mycompany.onlinefruitshopping.service.shop.ShopService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Shop-owner dashboard: the shop itself plus a small stock summary,
 * and one card per inventory / pricing / promotion / report module.
 */
@WebServlet("/store-owner/dashboard")
public class StoreOwnerDashboardServlet extends HttpServlet {

    private final ShopService shopService = new ShopService();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        User user = null;

        if (session != null
                && session.getAttribute("user") instanceof User) {

            user = (User) session.getAttribute("user");
        }

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        // Đếm thông báo chưa đọc của Shop Owner
        int unreadCount = notificationService.getUnreadCount(user.getUserID());
        session.setAttribute("shopOwnerUnreadCount", unreadCount);
        request.setAttribute("shopOwnerUnreadCount", unreadCount);

        ShopService.Dashboard dashboard;

        try {
            dashboard = shopService.loadDashboard(user.getUserID());

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your shop.",
                    e
            );
        }

        request.setAttribute("ownerName", user.getFullName());
        request.setAttribute("hasShop", dashboard.isHasShop());
        request.setAttribute("shopName", dashboard.getShopName());
        request.setAttribute("shopStatus", request.getParameter("shop"));
        request.setAttribute("stats", dashboard.getStats());

        request.getRequestDispatcher(
                "/jsp/shopowner/store-owner-dashboard.jsp"
        ).forward(request, response);
    }
}
