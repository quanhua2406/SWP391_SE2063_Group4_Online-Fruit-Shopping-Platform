package com.mycompany.onlinefruitshopping.controller.customer;

import com.mycompany.onlinefruitshopping.service.coupon.CouponApplicationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import com.mycompany.onlinefruitshopping.utils.WebUtil;

@WebServlet("/shopowner/customer/coupons")
public class CustomerCouponController extends HttpServlet {
    private final CouponApplicationService applicationService = new CouponApplicationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        show(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (!"preview".equals(action) && !"apply".equals(action) && !"remove".equals(action)) {
            response.sendError(400); return;
        }
        try {
            int orderId = WebUtil.positiveId(request.getParameter("orderId"));
            request.setAttribute("selectedOrderId", orderId);
            int customerId = WebUtil.user(request).getUserID();

            if ("preview".equals(action)) {
                request.setAttribute("quote", applicationService.preview(customerId, orderId, request.getParameter("code")));
                show(request, response);
                return;
            }
            if ("apply".equals(action)) {
                applicationService.apply(customerId, orderId, request.getParameter("code"));
                WebUtil.flash(request, "success", "Đã áp dụng mã cho đơn #" + orderId + ", cập nhật tổng tiền và ghi lịch sử sử dụng.");
            } else {
                applicationService.remove(customerId, orderId);
                WebUtil.flash(request, "success", "Đã gỡ mã, khôi phục tổng tiền trước giảm và trả lại lượt sử dụng.");
            }
            response.sendRedirect(request.getContextPath() + "/shopowner/customer/coupons");
        } catch (IllegalArgumentException e) {
            response.setStatus(400); request.setAttribute("formError", e.getMessage()); show(request, response);
        } catch (SQLException e) { log("Không xử lý được mã trên đơn", e); response.sendError(503); }
    }

    private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("orders", applicationService.findOrders(WebUtil.user(request).getUserID()));
            request.getRequestDispatcher("/jsp/shopowner/views/customer-coupons.jsp").forward(request, response);
        } catch (SQLException e) { log("Không tải được đơn/coupon. Kiểm tra migration 05", e); response.sendError(503); }
    }
}
