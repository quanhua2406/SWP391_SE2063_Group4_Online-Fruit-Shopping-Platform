package com.mycompany.onlinefruitshopping.controller.notification;

import com.mycompany.onlinefruitshopping.model.DeliveryNotificationType;
import com.mycompany.onlinefruitshopping.model.Notification;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.NotificationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller phục vụ riêng cho Delivery Staff (Nhân viên giao hàng).
 * Quản lý danh sách thông báo, lọc theo loại, lọc theo trạng thái đọc,
 * phân trang động và đánh dấu đã đọc.
 */
@WebServlet(name = "DeliveryNotificationServlet", urlPatterns = {
        "/delivery/notifications",
        "/delivery/notifications/unread-count"
})
public class DeliveryNotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DeliveryNotificationServlet.class.getName());
    private static final int DEFAULT_PAGE_SIZE = 8;

    private final NotificationService notificationService;

    public DeliveryNotificationServlet() {
        this.notificationService = new NotificationService();
    }

    public DeliveryNotificationServlet(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. Xác thực quyền Delivery Staff
        Integer staffUserId = getAuthenticatedStaffUserId(request, response);
        if (staffUserId == null) {
            return; // Đã xử lý redirect hoặc báo lỗi 403
        }

        String servletPath = request.getServletPath();

        // 2. Endpoint unread-count (trả về số lượng tin chưa đọc dạng plain text)
        if ("/delivery/notifications/unread-count".equals(servletPath)) {
            int unreadCount = notificationService.getUnreadCount(staffUserId);
            request.getSession().setAttribute("deliveryUnreadCount", unreadCount);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(String.valueOf(unreadCount));
            return;
        }

        // 3. Xử lý hành động đọc thông báo và chuyển hướng nhanh
        String action = request.getParameter("action");
        if ("read".equalsIgnoreCase(action)) {
            handleMarkOneReadAndRedirect(request, response, staffUserId);
            return;
        }

        // 4. Lấy tham số bộ lọc & Phân trang động
        String typeFilter = request.getParameter("type");
        if (typeFilter == null || typeFilter.isBlank()) {
            typeFilter = "all";
        }

        String filterRead = request.getParameter("filterRead");
        Boolean isReadBool = null;
        if ("unread".equalsIgnoreCase(filterRead)) {
            isReadBool = false;
        } else if ("read".equalsIgnoreCase(filterRead)) {
            isReadBool = true;
        }

        // Xử lý Dynamic Pagination
        int pageSize = DEFAULT_PAGE_SIZE;
        String pageSizeStr = request.getParameter("pageSize");
        if (pageSizeStr != null && !pageSizeStr.isBlank()) {
            try {
                int parsed = Integer.parseInt(pageSizeStr.trim());
                if (parsed > 0 && parsed <= 100) {
                    pageSize = parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                int parsed = Integer.parseInt(pageStr.trim());
                if (parsed > 0) {
                    page = parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // 5. Query dữ liệu từ CSDL
        int totalItems = notificationService.countAdminNotifications(staffUserId, typeFilter, isReadBool, null);
        int totalPages = notificationService.calculateTotalPages(totalItems, pageSize);

        if (page > totalPages && totalPages > 0) {
            page = totalPages;
        }

        List<Notification> notificationList = notificationService.getDeliveryNotifications(
                staffUserId, typeFilter, isReadBool, page, pageSize);

        // Gán link điều hướng chi tiết cho từng card
        String contextPath = request.getContextPath();
        for (Notification n : notificationList) {
            n.setActionUrl(notificationService.resolveDeliveryTargetUrl(n, contextPath));
        }

        // Tính toán thanh phân trang trượt (Sliding window 5 trang)
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, startPage + 4);
        if (endPage - startPage < 4 && startPage > 1) {
            startPage = Math.max(1, endPage - 4);
        }

        int fromItem = totalItems == 0 ? 0 : (page - 1) * pageSize + 1;
        int toItem = Math.min(page * pageSize, totalItems);

        // Đếm số lượng chưa đọc
        int unreadCount = notificationService.getUnreadCount(staffUserId);
        Map<String, Integer> unreadPerGroup = notificationService.getUnreadCountPerGroup(staffUserId);

        // Cập nhật số chưa đọc vào session để quả chuông trên navbar luôn hiển thị đúng
        request.getSession().setAttribute("deliveryUnreadCount", unreadCount);

        // 6. Truyền dữ liệu sang View JSP
        request.setAttribute("notifications", notificationList);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("startPage", startPage);
        request.setAttribute("endPage", endPage);
        request.setAttribute("fromItem", fromItem);
        request.setAttribute("toItem", toItem);
        request.setAttribute("pageSizeOptions", List.of(5, 8, 10, 15));
        request.setAttribute("currentType", typeFilter);
        request.setAttribute("filterRead", filterRead != null ? filterRead : "all");
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("unreadPerGroup", unreadPerGroup);

        request.getRequestDispatcher("/jsp/delivery/delivery-notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Integer staffUserId = getAuthenticatedStaffUserId(request, response);
        if (staffUserId == null) {
            return;
        }

        String action = request.getParameter("action");
        String currentType = request.getParameter("type");
        if (currentType == null || currentType.isBlank()) {
            currentType = "all";
        }

        String pageSizeStr = request.getParameter("pageSize");
        String pageSizeQuery = (pageSizeStr != null && !pageSizeStr.isBlank()) ? "&pageSize=" + pageSizeStr : "";
        String filterRead = request.getParameter("filterRead");
        String filterReadQuery = (filterRead != null && !filterRead.isBlank()) ? "&filterRead=" + filterRead : "";

        // 1. Đánh dấu tất cả đã đọc
        if ("readAll".equalsIgnoreCase(action)) {
            notificationService.markAllAsRead(staffUserId);
            request.getSession().setAttribute("deliveryUnreadCount", 0);
            response.sendRedirect(request.getContextPath() + "/delivery/notifications?type="
                    + currentType + filterReadQuery + pageSizeQuery);
            return;
        }

        // 2. Đánh dấu một thông báo đã đọc
        if ("read".equalsIgnoreCase(action)) {
            handleMarkOneReadAndRedirect(request, response, staffUserId);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/delivery/notifications");
    }

    private void handleMarkOneReadAndRedirect(HttpServletRequest request, HttpServletResponse response, int staffUserId)
            throws IOException {
        String idStr = request.getParameter("id");
        String targetUrl = request.getParameter("redirect");

        if (idStr != null && !idStr.isBlank()) {
            try {
                int notiId = Integer.parseInt(idStr.trim());
                notificationService.markAsRead(notiId, staffUserId);

                if (targetUrl == null || targetUrl.isBlank()) {
                    Notification n = notificationService.getNotificationById(notiId, staffUserId);
                    targetUrl = notificationService.resolveDeliveryTargetUrl(n, request.getContextPath());
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Cập nhật lại số chưa đọc trong session
        int newUnread = notificationService.getUnreadCount(staffUserId);
        request.getSession().setAttribute("deliveryUnreadCount", newUnread);

        if (targetUrl != null && !targetUrl.isBlank()) {
            response.sendRedirect(targetUrl);
        } else {
            response.sendRedirect(request.getContextPath() + "/delivery/notifications");
        }
    }

    /**
     * Xác thực người dùng hiện tại là Delivery Staff
     */
    private Integer getAuthenticatedStaffUserId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        // 1. Kiểm tra session "user"
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User u) {
            String role = u.getRoleName();
            if (role != null && (role.equalsIgnoreCase("DeliveryStaff")
                    || role.equalsIgnoreCase("DELIVERY_STAFF")
                    || role.equalsIgnoreCase("DELIVERY")
                    || role.equalsIgnoreCase("DELIVERYSTAFF"))) {
                return u.getUserID();
            }
        }

        // 2. Fallback kiểm tra userId trực tiếp nếu role là DeliveryStaff
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId != null && role != null && (role.equalsIgnoreCase("DeliveryStaff")
                || role.equalsIgnoreCase("DELIVERY_STAFF")
                || role.equalsIgnoreCase("DELIVERY")
                || role.equalsIgnoreCase("DELIVERYSTAFF"))) {
            return userId;
        }

        // Không có quyền truy cập
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang thông báo giao hàng.");
        return null;
    }
}
