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
 * Controller xử lý tính năng Thông báo dành riêng cho Shop Owner.
 * Tách biệt hoàn toàn với Customer Notification theo chuẩn MVC, thuần túy JSP/JSTL, không dùng JS/JSON.
 */
@WebServlet(name = "ShopOwnerNotificationServlet", urlPatterns = {"/store-owner/notifications", "/shop-owner/notifications"})
public class ShopOwnerNotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ShopOwnerNotificationServlet.class.getName());
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

        HttpSession session = request.getSession(false);
        User user = getAuthenticatedStoreOwner(session);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = user.getUserID();
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

        // Lọc thông báo quan trọng (is_important)
        String filterImportant = request.getParameter("filterImportant");
        Boolean isImportantCondition = null;
        if ("important".equalsIgnoreCase(filterImportant)) {
            isImportantCondition = true;
        }

        // 3. Phân trang linh hoạt (Dynamic Pagination, không cố định)
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

        int totalItems = notificationService.countShopOwnerNotifications(userId, tab, isReadCondition, isImportantCondition);
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
        List<Notification> notificationList = notificationService.getShopOwnerNotifications(
                userId, tab, isReadCondition, isImportantCondition, page, pageSize);
        int unreadCount = notificationService.getUnreadCount(userId);
        int importantUnreadCount = notificationService.getImportantUnreadCount(userId);
        Map<String, Integer> unreadPerGroup = notificationService.getUnreadCountPerGroup(userId);

        // Lưu unreadCount vào session để quả chuông trên Header của Shop Owner hiển thị
        session.setAttribute("shopOwnerUnreadCount", unreadCount);

        // Đẩy dữ liệu sang View JSP riêng của Shop Owner
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

        request.getRequestDispatcher("/notification/shop-owner-notifications.jsp").forward(request, response);
    }

    // =========================================================================
    // POST: Đánh dấu tất cả đã đọc (Mark All As Read)
    // =========================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User user = getAuthenticatedStoreOwner(session);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = user.getUserID();
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
            session.setAttribute("shopOwnerUnreadCount", 0);
            session.setAttribute("flashMessage", "Đã đánh dấu tất cả thông báo của cửa hàng là đã đọc!");
        }

        // Redirect lại danh sách theo đúng tab và pageSize hiện tại (chuẩn PRG pattern)
        response.sendRedirect(request.getContextPath() + "/store-owner/notifications?tab=" + currentTab 
                + pageSizeQuery + filterReadQuery + filterImportantQuery);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================
    
    /**
     * Xử lý khi Shop Owner bấm vào một thông báo cụ thể:
     * 1. Cập nhật is_read = true trong DB
     * 2. Xác định đích đến (actionUrl) của Shop Owner (Orders, Products/Stock)
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
                request.getSession().setAttribute("shopOwnerUnreadCount", updatedUnread);

                // Chuyển hướng đến URL đích dành cho Shop Owner
                String targetUrl = notificationService.resolveShopOwnerTargetUrl(n, request.getContextPath());
                response.sendRedirect(targetUrl);
                return;
            }
        }

        // Fallback nếu không tìm thấy thông báo
        response.sendRedirect(request.getContextPath() + "/store-owner/notifications");
    }

    /**
     * Lấy User của Shop Owner từ Session
     */
    private User getAuthenticatedStoreOwner(HttpSession session) {
        if (session == null) return null;
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User u) {
            return u;
        }
        return null;
    }
}
