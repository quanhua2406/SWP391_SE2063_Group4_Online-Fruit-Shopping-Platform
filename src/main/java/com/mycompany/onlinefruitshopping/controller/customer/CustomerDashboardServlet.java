package com.mycompany.onlinefruitshopping.controller.customer;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/customer/dashboard")
public class CustomerDashboardServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                int unreadCount = notificationService.getUnreadCount(user.getUserID());
                session.setAttribute("unreadNotificationCount", unreadCount);
                request.setAttribute("unreadNotificationCount", unreadCount);
            }
        }

        request.getRequestDispatcher("/jsp/customer/customer-dashboard.jsp")
                .forward(request, response);
    }
}