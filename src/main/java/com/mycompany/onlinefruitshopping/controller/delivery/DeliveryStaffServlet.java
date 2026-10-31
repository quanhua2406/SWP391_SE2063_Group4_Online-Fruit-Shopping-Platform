package com.mycompany.onlinefruitshopping.controller.delivery;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.NotificationService;
import com.mycompany.onlinefruitshopping.utils.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/delivery/dashboard")
public class DeliveryStaffServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("currentRole", "DELIVERYSTAFF");

        User user = WebUtil.user(request);
        if (user != null) {
            int unreadCount = notificationService.getUnreadCount(user.getUserID());
            request.getSession().setAttribute("deliveryUnreadCount", unreadCount);
            request.setAttribute("deliveryUnreadCount", unreadCount);
        }

        WebUtil.createCsrfToken(request.getSession());

        request.getRequestDispatcher(
                "/jsp/delivery/delivery-dashboard.jsp"
        ).forward(request, response);
    }
}
