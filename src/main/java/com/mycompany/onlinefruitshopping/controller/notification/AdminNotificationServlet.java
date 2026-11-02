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
 * Controller xử lý tính năng Thông báo dành riêng cho Admin (Hệ thống & Kiểm duyệt).
 * Chuẩn mô hình MVC, thuần túy JSP/JSTL, không dùng JS/JSON.
 */
@WebServlet(name = "AdminNotificationServlet", urlPatterns = {"/admin/notifications"})
public class AdminNotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminNotificationServlet.class.getName());
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

        User admin = getAuthenticatedAdmin(request, response);
        if (admin == null) return;

        int userId = admin.getUserID();
        String action = request.getParameter("action");

        // 1. Trường hợp bấm vào xem 1 thông báo -> Đánh dấu đã đọc và chuyển đến trang quản lý liên quan
        if ("read".equalsIgnoreCase(action)) {
            handleReadAndRedirect(request, response, userId);
            return;
        }

        // 2. Lọc theo tab danh mục
        String tab = request.getParameter("tab");
        if (tab == null || tab.isBlank()) {
            tab = "all";
        }

        // Lọc trạng thái đọc/chưa đọc
        String filterRead = request.getParameter("filterRead");
        Boolean isReadCondition = null;
        if ("unread".equalsIgnoreCase(filterRead)) {
            isReadCondition = false;
        } else if ("read".equalsIgnoreCase(filterRead)) {
            isReadCondition = true;
        }

        // Lọc thông báo khẩn cấp (is_important)
        String filterImportant = request.getParameter("filterImportant");
        Boolean isImportantCondition = null;
        if ("important".equalsIgnoreCase(filterImportant)) {
            isImportantCondition = true;
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

        int totalItems = notificationService.countAdminNotifications(userId, tab, isReadCondition, isImportantCondition);
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

        // Tính toán khoảng trang hiển thị (Sliding Window: startPage đến endPage)
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);
        int fromItem = totalItems > 0 ? (page - 1) * pageSize + 1 : 0;
        int toItem = Math.min(totalItems, page * pageSize);

        // Lấy dữ liệu qua Service
        List<Notification> notificationList = notificationService.getAdminNotifications(
                userId, tab, isReadCondition, isImportantCondition, page, pageSize);
        int unreadCount = notificationService.getUnreadCount(userId);
        int importantUnreadCount = notificationService.getImportantUnreadCount(userId);
        Map<String, Integer> unreadPerGroup = notificationService.getUnreadCountPerGroup(userId);

        // Lưu unreadCount vào session để quả chuông trên Header của Admin hiển thị
        request.getSession().setAttribute("adminUnreadCount", unreadCount);

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
        request.setAttribute("filterImportant", filterImportant != null ? filterImportant : "all");
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("importantUnreadCount", importantUnreadCount);
        request.setAttribute("unreadPerGroup", unreadPerGroup);

        request.getRequestDispatcher("/jsp/admin/admin-notifications.jsp").forward(request, response);
    }

    // =========================================================================
    // POST: Đánh dấu tất cả đã đọc (Mark All As Read)
    // =========================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = getAuthenticatedAdmin(request, response);
        if (admin == null) return;

        int userId = admin.getUserID();
        String action = request.getParameter("action");
        String currentTab = request.getParameter("tab");
        if (currentTab == null || currentTab.isBlank()) {
            currentTab = "all";
        }

        String pageSizeStr = request.getParameter("pageSize");
        String pageSizeQuery = (pageSizeStr != null && !pageSizeStr.isBlank()) ? "&pageSize=" + pageSizeStr : "";
        String filterRead = request.getParameter("filterRead");
        String filterReadQuery = (filterRead != null && !filterRead.isBlank()) ? "&filterRead=" + filterRead : "";
        String filterImportant = request.getParameter("filterImportant");
        String filterImportantQuery = (filterImportant != null && !filterImportant.isBlank()) ? "&filterImportant=" + filterImportant : "";

        if ("readAll".equalsIgnoreCase(action)) {
            int updated = notificationService.markAllAsRead(userId);
            request.getSession().setAttribute("adminUnreadCount", 0);
            request.getSession().setAttribute("flashMessage", "Đã đánh dấu tất cả thông báo hệ thống là đã đọc!");
        }

        // Redirect lại danh sách theo đúng tab và pageSize hiện tại (chuẩn PRG pattern)
        response.sendRedirect(request.getContextPath() + "/admin/notifications?tab=" + currentTab 
                + pageSizeQuery + filterReadQuery + filterImportantQuery);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================
    
    /**
     * Xử lý khi Admin bấm vào một thông báo cụ thể:
     * 1. Cập nhật is_read = true trong DB
     * 2. Xác định đích đến (actionUrl) của Admin
     * 3. Redirect sang trang đích
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
                request.getSession().setAttribute("adminUnreadCount", updatedUnread);

                // Chuyển hướng đến URL đích quản trị tương ứng
                String targetUrl = notificationService.resolveAdminTargetUrl(n, request.getContextPath());
                response.sendRedirect(targetUrl);
                return;
            }
        }

        // Fallback
        response.sendRedirect(request.getContextPath() + "/admin/notifications");
    }

    /**
     * Kiểm tra quyền Admin từ Session
     */
    private User getAuthenticatedAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        if (!"admin".equalsIgnoreCase(user.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang quản trị này.");
            return null;
        }

        return user;
    }
}
