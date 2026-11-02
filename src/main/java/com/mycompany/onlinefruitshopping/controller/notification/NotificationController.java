package com.mycompany.onlinefruitshopping.controller.notification;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Controller định tuyến thông báo cho các giao diện kế thừa:
 * - Điều hướng Delivery Staff sang /delivery/notifications
 * - Điều hướng Shop Owner sang /store-owner/notifications
 */
@WebServlet("/shopowner/notifications")
public class NotificationController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = WebUtil.user(request);
        if (user != null) {
            String role = user.getRoleName();
            if (role != null && (role.equalsIgnoreCase("DeliveryStaff")
                    || role.equalsIgnoreCase("DELIVERY_STAFF")
                    || role.equalsIgnoreCase("DELIVERY")
                    || role.equalsIgnoreCase("DELIVERYSTAFF"))) {
                response.sendRedirect(request.getContextPath() + "/delivery/notifications");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/store-owner/notifications");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if ("readAll".equalsIgnoreCase(action)) {
            User user = WebUtil.user(request);
            if (user != null) {
                String role = user.getRoleName();
                if (role != null && (role.equalsIgnoreCase("DeliveryStaff")
                        || role.equalsIgnoreCase("DELIVERY_STAFF")
                        || role.equalsIgnoreCase("DELIVERY")
                        || role.equalsIgnoreCase("DELIVERYSTAFF"))) {
                    response.sendRedirect(request.getContextPath() + "/delivery/notifications");
                    return;
                }
            }
            response.sendRedirect(request.getContextPath() + "/store-owner/notifications");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/store-owner/notifications");
    }
}
