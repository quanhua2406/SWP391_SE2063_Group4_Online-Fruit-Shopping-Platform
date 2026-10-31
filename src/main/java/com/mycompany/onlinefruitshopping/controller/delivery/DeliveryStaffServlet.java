package com.mycompany.onlinefruitshopping.controller.delivery;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.NotificationService;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/delivery/dashboard")
public class DeliveryStaffServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Integer userId = null;
            Object userObj = session.getAttribute("user");
            if (userObj instanceof User u) {
                userId = u.getUserID();
            } else {
                Object shopUserObj = session.getAttribute("shopUser");
                if (shopUserObj instanceof Account acc) {
                    userId = acc.getId();
                } else {
                    userId = (Integer) session.getAttribute("userId");
                }
            }

            if (userId != null && userId > 0) {
                int unreadCount = notificationService.getUnreadCount(userId);
                session.setAttribute("deliveryUnreadCount", unreadCount);
                request.setAttribute("deliveryUnreadCount", unreadCount);
            }
        }

        request.getRequestDispatcher("/jsp/delivery/delivery-dashboard.jsp")
                .forward(request, response);
    }
}