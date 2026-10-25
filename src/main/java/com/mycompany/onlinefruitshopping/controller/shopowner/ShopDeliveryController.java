package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.service.delivery.DeliveryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.DeliveryList;
import com.mycompany.onlinefruitshopping.utils.WebUtil;

@WebServlet("/shopowner/shop/deliveries")
public class ShopDeliveryController extends HttpServlet {
    private final DeliveryService deliveryService = new DeliveryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = WebUtil.user(request);
        try {
            DeliveryList.prepareList(request, deliveryService.findAll(user));
            request.setAttribute("orders", deliveryService.findAvailableOrders(user));
            request.setAttribute("staff", deliveryService.findActiveStaff(user));
            request.getRequestDispatcher("/jsp/shopowner/views/shop.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(400);
        } catch (SQLException e) {
            log("Không tải được giao hàng của shop", e);
            response.sendError(503);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int orderId = WebUtil.positiveId(request.getParameter("orderId"));
            int shipperId = WebUtil.positiveId(request.getParameter("shipperId"));
            // ownerId lấy từ session; người dùng không tự chọn quyền sở hữu bằng form.
            deliveryService.assign(WebUtil.user(request), orderId, shipperId);
            WebUtil.flash(request, "success", "Đã phân công và tạo thông báo cho shipper.");
        } catch (IllegalArgumentException e) {
            WebUtil.flash(request, "error", e.getMessage());
        } catch (SQLException e) {
            log("Không phân công được", e);
            WebUtil.flash(request, "error", "Chưa phân công được. Hãy tải lại trang và thử lại; nếu còn lỗi, xem log Tomcat.");
        }
        
        response.sendRedirect(request.getContextPath() + "/shopowner/shop/deliveries");
    }
}
