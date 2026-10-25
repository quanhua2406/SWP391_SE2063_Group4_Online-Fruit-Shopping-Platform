package com.mycompany.onlinefruitshopping.controller.delivery;

import com.mycompany.onlinefruitshopping.service.delivery.DeliveryService;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.model.Delivery;
import com.mycompany.onlinefruitshopping.utils.DeliveryList;
import com.mycompany.onlinefruitshopping.utils.WebUtil;

@WebServlet(urlPatterns = {"/shopowner/delivery/list", "/shopowner/delivery/detail", "/shopowner/delivery/update"})
public class DeliveryController extends HttpServlet {
    private final DeliveryService deliveryService = new DeliveryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = WebUtil.user(request);

        try {
            if ("/shopowner/delivery/list".equals(request.getServletPath())) {
                DeliveryList.prepareList(request, deliveryService.findAll(user));
                request.getRequestDispatcher("/jsp/shopowner/views/deliveries.jsp").forward(request, response);
            } else if ("/shopowner/delivery/detail".equals(request.getServletPath())) {
                int id = WebUtil.positiveId(request.getParameter("id"));
                Delivery delivery = deliveryService.findById(id, user);
                if (delivery == null) {
                    response.sendError(404);
                    return;
                }
                request.setAttribute("delivery", delivery);
                request.setAttribute("items", deliveryService.findItems(id, user));
                request.getRequestDispatcher("/jsp/shopowner/views/detail.jsp").forward(request, response);
            } else {
                response.sendError(405); // Cập nhật phải dùng POST, không cập nhật bằng đường dẫn GET.
            }
        } catch (IllegalArgumentException e) {
            response.sendError(400);
        } catch (SQLException e) {
            log("Không tải được giao hàng", e);
            response.sendError(503);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/shopowner/delivery/update".equals(request.getServletPath())) {
            response.sendError(405);
            return;
        }
        User user = WebUtil.user(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!LoginService.roleMatches(user.getRoleName(), "DELIVERYSTAFF")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int id = 0;
        try {
            id = WebUtil.positiveId(request.getParameter("id"));
            String action = request.getParameter("action");
            String reason = request.getParameter("reason");
            boolean collected = "yes".equals(request.getParameter("collected"));
            deliveryService.updateStatus(id, user, action, reason, collected);
            WebUtil.flash(request, "success", "Đã cập nhật giao hàng và thông báo cho chủ shop.");
        } catch (IllegalArgumentException e) {
            WebUtil.flash(request, "error", e.getMessage());
        } catch (SQLException e) {
            log("Không cập nhật được giao hàng", e);
            WebUtil.flash(request, "error", "Chưa lưu được thay đổi. Hãy tải lại trang và thử lại; nếu còn lỗi, xem log Tomcat.");
        }
        String path = id > 0 ? "/shopowner/delivery/detail?id=" + id : "/shopowner/delivery/list";
        response.sendRedirect(request.getContextPath() + path);
    }
}
