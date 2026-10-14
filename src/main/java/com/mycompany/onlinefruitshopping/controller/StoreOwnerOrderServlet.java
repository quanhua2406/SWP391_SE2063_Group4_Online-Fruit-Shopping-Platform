package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.OrderDAO;
import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.User;

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

@WebServlet("/store-owner/orders")
public class StoreOwnerOrderServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        try {
            int shopId = productDAO.getOrCreateShopIdForUser(user.getUserID(), user.getFullName());

            // Tự động tạo dữ liệu mẫu nếu shop chưa có đơn hàng nào để tiện kiểm thử
            orderDAO.seedSampleOrdersIfEmpty(shopId);

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

            List<Order> orders = orderDAO.getOrdersByShop(shopId, status, searchQuery);
            Map<String, Integer> counts = orderDAO.countOrdersByStatus(shopId);

            request.setAttribute("orders", orders);
            request.setAttribute("counts", counts);
            request.setAttribute("currentStatus", status);
            request.setAttribute("searchQuery", searchQuery);

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
            Order order = orderDAO.getOrderById(orderId, shopId);
            if (order == null) {
                redirectWithError(request, response, "Không tìm thấy thông tin đơn hàng hoặc bạn không có quyền xem đơn này.");
                return;
            }

            String status = request.getParameter("status");
            if (status == null || status.isBlank()) status = "all";

            List<Order> orders = orderDAO.getOrdersByShop(shopId, status, null);
            Map<String, Integer> counts = orderDAO.countOrdersByStatus(shopId);

            request.setAttribute("orders", orders);
            request.setAttribute("counts", counts);
            request.setAttribute("currentStatus", status);
            request.setAttribute("selectedOrder", order);

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
            int shopId = productDAO.getOrCreateShopIdForUser(user.getUserID(), user.getFullName());
            String action = request.getParameter("action");
            String orderIdStr = request.getParameter("orderId");

            if (orderIdStr == null || orderIdStr.isBlank()) {
                redirectWithError(request, response, "Mã đơn hàng không hợp lệ.");
                return;
            }

            int orderId = Integer.parseInt(orderIdStr.trim());

            if ("confirm".equalsIgnoreCase(action)) {
                // Xác nhận đơn: pending_confirmation -> preparing
                orderDAO.updateOrderStatus(orderId, shopId, "preparing", user.getUserID(), "Shop đã duyệt đơn và đang đóng gói.");
                redirectWithSuccess(request, response, "Xác nhận đơn hàng #" + orderId + " thành công! Trạng thái chuyển sang Đang chuẩn bị.", "preparing");

            } else if ("ready".equalsIgnoreCase(action)) {
                // Đóng gói xong / Sẵn sàng giao: preparing -> shipping
                orderDAO.updateOrderStatus(orderId, shopId, "shipping", user.getUserID(), "Shop đã đóng gói xong, bàn giao vận chuyển.");
                redirectWithSuccess(request, response, "Đơn hàng #" + orderId + " đã sẵn sàng và chuyển sang Đang giao hàng!", "shipping");

            } else if ("reject".equalsIgnoreCase(action)) {
                // Từ chối đơn: chuyển sang cancelled với lý do bắt buộc
                String reason = request.getParameter("reason");
                if (reason == null || reason.trim().isEmpty()) {
                    redirectWithError(request, response, "Vui lòng nhập lý do từ chối đơn hàng!");
                    return;
                }

                orderDAO.updateOrderStatus(orderId, shopId, "cancelled", user.getUserID(), "Từ chối: " + reason.trim());
                redirectWithSuccess(request, response, "Đã từ chối đơn hàng #" + orderId + " thành công.", "cancelled");

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
