package com.mycompany.onlinefruitshopping.controller.notification;

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
 * Controller xử lý tính năng Thông báo (Notification) dành cho Customer.
 * Sử dụng mô hình thuần túy Servlet + JSP/JSTL, không dùng JavaScript/JSON.
 */
@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications", "/customer/notifications"})
public class NotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());
    private static final int DEFAULT_PAGE_SIZE = 8;

    private NotificationService notificationService;

    @Override
    public void init() {
        this.notificationService = new NotificationService();
    }

    // =========================================================================
    // GET: Hiển thị danh sách thông báo hoặc Xử lý Đọc & Chuyển hướng
    // =========================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Integer userId = getLoggedInUserId(session);

        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        // 1. Trường hợp bấm vào xem 1 thông báo -> Đánh dấu đã đọc và chuyển trang
        if ("read".equalsIgnoreCase(action)) {
            handleReadAndRedirect(request, response, userId);
            return;
        }

        // 2. Trường hợp hiển thị danh sách thông báo
        String tab = request.getParameter("tab");
        if (tab == null || tab.isBlank()) {
            tab = "all";
        }

        String filterRead = request.getParameter("filterRead");
        Boolean isReadCondition = null;
        if ("unread".equalsIgnoreCase(filterRead)) {
            isReadCondition = false;
        } else if ("read".equalsIgnoreCase(filterRead)) {
            isReadCondition = true;
        }

        // 3. Phân trang linh hoạt (Dynamic Pagination)
        int pageSize = DEFAULT_PAGE_SIZE;
        String pageSizeStr = request.getParameter("pageSize");
        if (pageSizeStr != null && !pageSizeStr.isBlank()) {
            try {
                int parsed = Integer.parseInt(pageSizeStr);
                if (parsed > 0 && parsed <= 100) {
                    pageSize = parsed;
                }
            } catch (NumberFormatException ignored) {}
        }

        int totalItems = notificationService.countNotifications(userId, tab, isReadCondition);
        int totalPages = notificationService.calculateTotalPages(totalItems, pageSize);

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
                if (totalPages > 0 && page > totalPages) page = totalPages;
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }

        // Tính toán khoảng trang hiển thị (Sliding Window Pagination)
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);
        int fromItem = totalItems > 0 ? (page - 1) * pageSize + 1 : 0;
        int toItem = Math.min(totalItems, page * pageSize);

        // Lấy dữ liệu qua Service
        List<Notification> notificationList = notificationService.getNotifications(
                userId, tab, isReadCondition, page, pageSize);
        int unreadCount = notificationService.getUnreadCount(userId);
        Map<String, Integer> unreadPerGroup = notificationService.getUnreadCountPerGroup(userId);

        // Lưu unreadCount vào session để quả chuông trên Header mọi trang đều có thể truy cập
        session.setAttribute("unreadNotificationCount", unreadCount);

        // Đẩy dữ liệu sang View JSP
        request.setAttribute("notifications", notificationList);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("startPage", startPage);
        request.setAttribute("endPage", endPage);
        request.setAttribute("fromItem", fromItem);
        request.setAttribute("toItem", toItem);
        request.setAttribute("pageSizeOptions", List.of(5, 8, 10, 15, 20));
        request.setAttribute("currentTab", tab);
        request.setAttribute("filterRead", filterRead != null ? filterRead : "all");
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("unreadPerGroup", unreadPerGroup);

        request.getRequestDispatcher("/notifications.jsp").forward(request, response);
    }

    // =========================================================================
    // POST: Đánh dấu tất cả đã đọc (Mark All As Read)
    // =========================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Integer userId = getLoggedInUserId(session);

        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String currentTab = request.getParameter("tab");
        if (currentTab == null || currentTab.isBlank()) {
            currentTab = "all";
        }

        String pageSizeStr = request.getParameter("pageSize");
        String pageSizeQuery = (pageSizeStr != null && !pageSizeStr.isBlank()) ? "&pageSize=" + pageSizeStr : "";

        if ("readAll".equalsIgnoreCase(action)) {
            int updated = notificationService.markAllAsRead(userId);
            session.setAttribute("unreadNotificationCount", 0);
            session.setAttribute("flashMessage", "Đã đánh dấu tất cả thông báo là đã đọc!");
        }

        // Redirect lại danh sách theo đúng tab và pageSize hiện tại (chuẩn PRG pattern)
        response.sendRedirect(request.getContextPath() + "/notifications?tab=" + currentTab + pageSizeQuery);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================
    
    /**
     * Xử lý khi khách hàng bấm vào một thông báo cụ thể:
     * 1. Cập nhật is_read = true trong DB
     * 2. Xác định đích đến (actionUrl)
     * 3. Redirect người dùng sang trang đích
     */
    private void handleReadAndRedirect(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String idStr = request.getParameter("id");
        int notificationId = 0;
        try {
            notificationId = Integer.parseInt(idStr);
        } catch (NumberFormatException ignored) {}

        if (notificationId > 0) {
            Notification n = notificationService.getNotificationById(notificationId, userId);
            if (n != null) {
                // Đánh dấu đã đọc
                notificationService.markAsRead(notificationId, userId);

                // Cập nhật lại số đếm trên session
                int updatedUnread = notificationService.getUnreadCount(userId);
                request.getSession().setAttribute("unreadNotificationCount", updatedUnread);

                // Chuyển hướng đến URL đích tương ứng
                String targetUrl = notificationService.resolveTargetUrl(n, request.getContextPath());
                response.sendRedirect(targetUrl);
                return;
            }
        }

        // Fallback nếu không tìm thấy thông báo
        response.sendRedirect(request.getContextPath() + "/notifications");
    }

    /**
     * Lấy userId an toàn từ Session
     */
    private Integer getLoggedInUserId(HttpSession session) {
        if (session == null) return null;
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            User u = (User) session.getAttribute("user");
            if (u != null) {
                userId = u.getUserID();
            }
        }
        return userId;
    }
}
