package com.mycompany.onlinefruitshopping.controller.order;

import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controller Quản trị Đơn hàng của Chủ Cửa Hàng (Store Owner).
 * Feature 6: Order Management - In Charge: Le Duc Cuong (cuongld)
 *
 * Tuân thủ mô hình 3 tầng (3-Layer Architecture):
 * Servlet chỉ đóng vai trò tiếp nhận HTTP Request, xác thực quyền và gọi OrderService để xử lý.
 */
@WebServlet("/store-owner/orders")
public class StoreOwnerOrderServlet extends HttpServlet {

    private final OrderService orderService;

    public StoreOwnerOrderServlet() {
        this.orderService = new OrderService();
    }

    public StoreOwnerOrderServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        try {
            int shopId = orderService.getOrCreateShopIdForUser(user.getUserID(), user.getFullName());

            // Tự động tạo dữ liệu mẫu nếu shop chưa có đơn hàng nào để tiện kiểm thử
            orderService.seedSampleOrdersIfEmpty(shopId);

            String action = request.getParameter("action");
            if ("detail".equalsIgnoreCase(action)) {
                handleViewDetail(request, response, shopId);
                return;
            }

            String status = request.getParameter("status");
            if (status == null || status.isBlank()) {
                status = "all";
            }

            String searchQuery = request.getParameter("q");

            List<Order> orders = orderService.getOrdersByShop(shopId, status, searchQuery);
            Map<String, Integer> counts = orderService.countOrdersByStatus(shopId);
            List<User> availableShippers = orderService.getAvailableDeliveryStaff();

            request.setAttribute("orders", orders);
            request.setAttribute("counts", counts);
            request.setAttribute("currentStatus", status);
            request.setAttribute("searchQuery", searchQuery);
            request.setAttribute("availableShippers", availableShippers);

            request.getRequestDispatcher("/store-owner-orders.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Lỗi CSDL khi truy vấn danh sách đơn hàng", e);
        }
    }

    private void handleViewDetail(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws SQLException, ServletException, IOException {

        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.isBlank()) {
            redirectWithError(request, response, "Mã đơn hàng không hợp lệ.");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr.trim());
            Order order = orderService.getOrderDetail(orderId, shopId);
            if (order == null) {
                redirectWithError(request, response, "Không tìm thấy thông tin đơn hàng hoặc bạn không có quyền xem đơn này.");
                return;
            }

            String status = request.getParameter("status");
            if (status == null || status.isBlank()) status = "all";

            List<Order> orders = orderService.getOrdersByShop(shopId, status, null);
            Map<String, Integer> counts = orderService.countOrdersByStatus(shopId);
            List<User> availableShippers = orderService.getAvailableDeliveryStaff();

            request.setAttribute("orders", orders);
            request.setAttribute("counts", counts);
            request.setAttribute("currentStatus", status);
            request.setAttribute("selectedOrder", order);
            request.setAttribute("availableShippers", availableShippers);

            request.getRequestDispatcher("/store-owner-orders.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            redirectWithError(request, response, "Định dạng mã đơn hàng không đúng.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        try {
            int shopId = orderService.getOrCreateShopIdForUser(user.getUserID(), user.getFullName());
            String action = request.getParameter("action");
            String orderIdStr = request.getParameter("orderId");

            if (orderIdStr == null || orderIdStr.isBlank()) {
                redirectWithError(request, response, "Mã đơn hàng không hợp lệ.");
                return;
            }

            int orderId = Integer.parseInt(orderIdStr.trim());

            if ("confirm".equalsIgnoreCase(action)) {
                // Xác nhận đơn thông qua OrderService
                orderService.confirmOrder(orderId, shopId, user.getUserID());
                redirectWithSuccess(request, response, "Xác nhận đơn hàng #" + orderId + " thành công! Trạng thái chuyển sang Đang chuẩn bị.", "preparing");

            } else if ("ready".equalsIgnoreCase(action)) {
                // Đóng gói xong / Sẵn sàng giao thông qua OrderService
                orderService.markOrderReady(orderId, shopId, user.getUserID());
                redirectWithSuccess(request, response, "Đơn hàng #" + orderId + " đã sẵn sàng và chuyển sang Đang giao hàng!", "shipping");

            } else if ("reject".equalsIgnoreCase(action)) {
                // Từ chối đơn thông qua OrderService
                String reason = request.getParameter("reason");
                orderService.rejectOrder(orderId, shopId, user.getUserID(), reason);
                redirectWithSuccess(request, response, "Đã từ chối đơn hàng #" + orderId + " thành công.", "cancelled");

            } else if ("assign-shipper".equalsIgnoreCase(action)) {
                // Phân công shipper thông qua OrderService (Task 12)
                String staffIdStr = request.getParameter("staffId");
                if (staffIdStr == null || staffIdStr.isBlank()) {
                    redirectWithError(request, response, "Vui lòng chọn nhân viên giao hàng.");
                    return;
                }
                int staffId = Integer.parseInt(staffIdStr.trim());
                String shipperNote = request.getParameter("shipperNote");
                orderService.assignDeliveryStaff(orderId, shopId, staffId, user.getUserID(), shipperNote);
                redirectWithSuccess(request, response, "Đã phân công Shipper giao đơn hàng #" + orderId + " thành công!", "shipping");

            } else {
                redirectWithError(request, response, "Thao tác không được hỗ trợ.");
            }

        } catch (NumberFormatException e) {
            redirectWithError(request, response, "Mã đơn hàng phải là số hợp lệ.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectWithError(request, response, e.getMessage());
        } catch (SQLException e) {
            redirectWithError(request, response, "Lỗi hệ thống CSDL: " + e.getMessage());
        }
    }

    private User getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return (User) session.getAttribute("user");
    }

    private void redirectWithSuccess(HttpServletRequest request, HttpServletResponse response, String message, String status)
            throws IOException {
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String targetStatus = (status != null && !status.isBlank()) ? status : "all";
        response.sendRedirect(request.getContextPath() + "/store-owner/orders?status=" + targetStatus + "&successMessage=" + encodedMsg);
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/store-owner/orders?errorMessage=" + encodedMsg);
    }
}
