package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.review.ReviewDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private NotificationService notificationService;
    private ReviewDAO reviewDAO;

    @Override
    public void init() {
        this.notificationService = new NotificationService();
        this.reviewDAO = new ReviewDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("user");
            if (userObj instanceof User) {
                User u = (User) userObj;
                int unread = notificationService.getUnreadCount(u.getUserID());
                session.setAttribute("adminUnreadCount", unread);
            }
        }

        try {
            int reportedReviewsCount = reviewDAO.countReviewsForAdmin("reported");
            request.setAttribute("reportedReviewsCount", reportedReviewsCount);
        } catch (Exception ignored) {}

        request.getRequestDispatcher("/jsp/admin/admin-dashboard.jsp")
                .forward(request, response);
    }
}
