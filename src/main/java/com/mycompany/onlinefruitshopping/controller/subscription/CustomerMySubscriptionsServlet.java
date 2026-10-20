package com.mycompany.onlinefruitshopping.controller.subscription;

import com.mycompany.onlinefruitshopping.model.Subscription;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.SubscriptionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Controller phục vụ trang Quản lý gói đăng ký của khách hàng ("Gói Của Tôi"):
 * - Xem danh sách các gói combo đang nhận hoặc đã từng đăng ký: GET /my-subscriptions
 * - Tạm dừng gói (Pause): POST /my-subscriptions?action=pause&id=X
 * - Kích hoạt lại gói (Resume): POST /my-subscriptions?action=resume&id=X
 * - Hủy gói (Cancel): POST /my-subscriptions?action=cancel&id=X
 *
 * Mọi logic nghiệp vụ được ủy quyền cho SubscriptionService.
 */
@WebServlet(name = "CustomerMySubscriptionsServlet", urlPatterns = {
        "/my-subscriptions",
        "/customer/subscriptions"
})
public class CustomerMySubscriptionsServlet extends HttpServlet {

    private final SubscriptionService subscriptionService = new SubscriptionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String statusFilter = trim(request.getParameter("status"));
        if (statusFilter == null || statusFilter.isEmpty()) {
            statusFilter = "all";
        }

        // Lấy danh sách gói theo bộ lọc trạng thái
        List<Subscription> subscriptions = subscriptionService.getCustomerSubscriptions(user.getUserID(), statusFilter);

        // Lấy toàn bộ gói để tính toán số liệu thống kê (dashboard header)
        List<Subscription> allSubs = subscriptionService.getCustomerSubscriptions(user.getUserID(), "all");
        int countActive = 0;
        int countPaused = 0;
        int countCancelled = 0;
        Date nextDeliveryDate = null;

        for (Subscription s : allSubs) {
            if (s.isActive()) {
                countActive++;
                if (s.getNext_delivery_date() != null) {
                    if (nextDeliveryDate == null || s.getNext_delivery_date().before(nextDeliveryDate)) {
                        nextDeliveryDate = s.getNext_delivery_date();
                    }
                }
            } else if (s.isPaused()) {
                countPaused++;
            } else if (s.isCancelled()) {
                countCancelled++;
            }
        }

        request.setAttribute("subscriptions", subscriptions);
        request.setAttribute("currentStatus", statusFilter);
        request.setAttribute("totalCount", allSubs.size());
        request.setAttribute("countActive", countActive);
        request.setAttribute("countPaused", countPaused);
        request.setAttribute("countCancelled", countCancelled);
        request.setAttribute("nextDeliveryDate", nextDeliveryDate);

        request.getRequestDispatcher("/subscription/mySubscriptions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = trim(request.getParameter("action"));
        int subscriptionId = parseSafeInt(request.getParameter("id"), -1);

        HttpSession session = request.getSession();

        if (subscriptionId <= 0 || action == null) {
            session.setAttribute("toastError", "Yêu cầu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-subscriptions");
            return;
        }

        SubscriptionService.ServiceResult<Void> result;

        switch (action.toLowerCase()) {
            case "pause":
                result = subscriptionService.pauseSubscription(subscriptionId, user.getUserID());
                break;
            case "resume":
                result = subscriptionService.resumeSubscription(subscriptionId, user.getUserID());
                break;
            case "cancel":
                result = subscriptionService.cancelSubscription(subscriptionId, user.getUserID());
                break;
            default:
                result = SubscriptionService.ServiceResult.fail("Hành động không được hỗ trợ.");
                break;
        }

        if (result.isSuccess()) {
            session.setAttribute("toastSuccess", result.getMessage());
        } else {
            session.setAttribute("toastError", result.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/my-subscriptions");
    }

    // ─────────────────────────────────────────────────────────────────────
    // AUTH & HELPER
    // ─────────────────────────────────────────────────────────────────────
    private User getAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            if (session == null) session = req.getSession(true);
            session.setAttribute("redirectAfterLogin", req.getContextPath() + "/my-subscriptions");
            session.setAttribute("toastInfo", "Vui lòng đăng nhập để xem các gói đăng ký của bạn.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        return user;
    }

    private String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private int parseSafeInt(String s, int def) {
        if (s == null || s.trim().isEmpty()) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}
