package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.review.ReviewDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.NotificationService;
import com.mycompany.onlinefruitshopping.service.report.AdminReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private NotificationService notificationService;
    private ReviewDAO reviewDAO;
    private AdminReportService reportService;

    @Override
    public void init() {
        this.notificationService = new NotificationService();
        this.reviewDAO = new ReviewDAO();
        this.reportService = new AdminReportService();
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

        // Nạp KPI và Top sản phẩm bán chạy cho Sales Dashboard (Task 26)
        try {
            Map<String, Object> kpi = reportService.getDashboardKPIs();
            request.setAttribute("kpi", kpi);

            List<Map<String, Object>> topFruits = reportService.getTopSellingFruits(5);
            request.setAttribute("topFruits", topFruits);
        } catch (Exception e) {
            System.err.println("Warning: AdminDashboardServlet unable to load report KPIs: " + e.getMessage());
        }

        request.getRequestDispatcher("/jsp/admin/admin-dashboard.jsp")
                .forward(request, response);
    }
}
