package com.mycompany.onlinefruitshopping.controller.notification;

import com.mycompany.onlinefruitshopping.service.notification.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import com.mycompany.onlinefruitshopping.utils.WebUtil;

@WebServlet("/shopowner/notifications")
public class NotificationController extends HttpServlet {
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("notifications", notificationService.findByAccount(WebUtil.user(request).getId()));
            request.getRequestDispatcher("/jsp/shopowner/views/notifications.jsp").forward(request, response);
        } catch (SQLException e) {
            log("Không tải được thông báo", e);
            response.sendError(503);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = WebUtil.positiveId(request.getParameter("id"));
            notificationService.markRead(id, WebUtil.user(request).getId());
        } catch (IllegalArgumentException e) {
            WebUtil.flash(request, "error", e.getMessage());
        } catch (SQLException e) {
            log("Không đánh dấu được thông báo", e);
            WebUtil.flash(request, "error", "Chưa cập nhật được thông báo. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/shopowner/notifications");
    }
}
