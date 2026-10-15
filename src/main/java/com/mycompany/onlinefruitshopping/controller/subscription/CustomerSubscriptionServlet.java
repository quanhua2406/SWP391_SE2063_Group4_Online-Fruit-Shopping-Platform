package com.mycompany.onlinefruitshopping.controller.subscription;

import com.mycompany.onlinefruitshopping.dao.AddressDAO;
import com.mycompany.onlinefruitshopping.model.Address;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlan;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.SubscriptionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ luồng khách hàng (Customer):
 * - Khám phá các gói combo đăng ký định kỳ: GET /subscriptions hoặc /subscription/plans
 * - Xem chi tiết gói combo: GET /subscription/detail?id=X
 * - Trang xác nhận & thanh toán gói: GET /subscription/subscribe?planId=X
 * - Xử lý đăng ký gói: POST /subscription/subscribe
 *
 * Mọi logic nghiệp vụ được ủy quyền cho SubscriptionService.
 */
@WebServlet(name = "CustomerSubscriptionServlet", urlPatterns = {
        "/subscriptions",
        "/subscription/plans",
        "/subscription/detail",
        "/subscription/subscribe"
})
public class CustomerSubscriptionServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CustomerSubscriptionServlet.class.getName());
    private static final int PAGE_SIZE = 9; // 3x3 grid cards

    private final SubscriptionService subscriptionService = new SubscriptionService();
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        if ("/subscription/subscribe".equals(servletPath)) {
            handleShowSubscribeCheckout(request, response);
        } else if ("/subscription/detail".equals(servletPath)) {
            handleShowPlanDetail(request, response);
        } else {
            handleBrowsePlans(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String servletPath = request.getServletPath();

        if ("/subscription/subscribe".equals(servletPath)) {
            handleSubmitSubscribe(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/subscriptions");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1. DUYỆT DANH SÁCH GÓI COMBO CHO KHÁCH HÀNG
    // ─────────────────────────────────────────────────────────────────────
    private void handleBrowsePlans(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer shopId = parseOptionalInt(request.getParameter("shopId"));
        String cycle = trim(request.getParameter("cycle"));
        String keyword = trim(request.getParameter("q"));
        int page = parseSafeInt(request.getParameter("page"), 1);

        List<SubscriptionPlan> plans = subscriptionService.getActivePlansForCustomer(shopId, cycle, keyword, page, PAGE_SIZE);
        int totalPlans = subscriptionService.countActivePlansForCustomer(shopId, cycle, keyword);
        int totalPages = subscriptionService.calculateTotalPages(totalPlans, PAGE_SIZE);

        // Tính toán thông tin tiết kiệm cho từng plan
        Map<Integer, Map<String, Object>> savingsMap = new HashMap<>();
        for (SubscriptionPlan p : plans) {
            savingsMap.put(p.getPlanId(), subscriptionService.calculatePlanSavings(p));
        }

        // Lấy danh sách shop để làm bộ lọc
        List<Map<String, Object>> shopList = subscriptionService.getActiveShopsForFilter();

        request.setAttribute("plans", plans);
        request.setAttribute("savingsMap", savingsMap);
        request.setAttribute("shopList", shopList);
        request.setAttribute("selectedShopId", shopId);
        request.setAttribute("selectedCycle", cycle);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalPlans", totalPlans);

        request.getRequestDispatcher("/subscription/browsePlans.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. CHI TIẾT GÓI COMBO
    // ─────────────────────────────────────────────────────────────────────
    private void handleShowPlanDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            idParam = request.getParameter("planId");
        }
        int planId = parseSafeInt(idParam, -1);
        if (planId <= 0) {
            response.sendRedirect(request.getContextPath() + "/subscriptions");
            return;
        }

        SubscriptionPlan plan = subscriptionService.getActivePlanDetail(planId);
        if (plan == null) {
            request.getSession().setAttribute("toastError", "Gói đăng ký không tồn tại hoặc đã tạm dừng.");
            response.sendRedirect(request.getContextPath() + "/subscriptions");
            return;
        }

        Map<String, Object> savings = subscriptionService.calculatePlanSavings(plan);

        request.setAttribute("plan", plan);
        request.setAttribute("savings", savings);
        request.getRequestDispatcher("/subscription/planDetail.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. HIỂN THỊ TRANG XÁC NHẬN ĐĂNG KÝ (CHECKOUT)
    // ─────────────────────────────────────────────────────────────────────
    private void handleShowSubscribeCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String idParam = request.getParameter("planId");
        if (idParam == null || idParam.trim().isEmpty()) {
            idParam = request.getParameter("id");
        }
        int planId = parseSafeInt(idParam, -1);
        if (planId <= 0) {
            response.sendRedirect(request.getContextPath() + "/subscriptions");
            return;
        }

        // Nếu chưa đăng nhập, lưu lại URL để redirect sau khi đăng nhập xong
        if (user == null) {
            if (session == null) session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/subscription/subscribe?planId=" + planId);
            session.setAttribute("toastInfo", "Vui lòng đăng nhập để tiến hành đăng ký gói trái cây định kỳ.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        SubscriptionPlan plan = subscriptionService.getActivePlanDetail(planId);
        if (plan == null) {
            session.setAttribute("toastError", "Gói combo này hiện không khả dụng để đăng ký.");
            response.sendRedirect(request.getContextPath() + "/subscriptions");
            return;
        }

        Map<String, Object> savings = subscriptionService.calculatePlanSavings(plan);

        // Nạp địa chỉ giao hàng của người dùng (nếu có)
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = addressDAO.getAddressesByUserId(user.getUserID());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Không thể tải danh sách địa chỉ của user: " + user.getUserID(), e);
        }

        // Tính ngày giao hàng mặc định (ngày mai)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String defaultDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        String minDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        request.setAttribute("plan", plan);
        request.setAttribute("savings", savings);
        request.setAttribute("addresses", addresses);
        request.setAttribute("defaultDate", defaultDateStr);
        request.setAttribute("minDate", minDateStr);

        request.getRequestDispatcher("/subscription/subscribeCheckout.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. XỬ LÝ SUBMIT ĐĂNG KÝ GÓI (POST)
    // ─────────────────────────────────────────────────────────────────────
    private void handleSubmitSubscribe(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int planId = parseSafeInt(request.getParameter("planId"), -1);
        String deliveryDateStr = trim(request.getParameter("deliveryDate"));
        String notes = trim(request.getParameter("notes"));

        // Lưu địa chỉ mới nếu người dùng nhập trực tiếp (khi chưa có sẵn trong sổ địa chỉ)
        String newAddressLine = trim(request.getParameter("addressLine"));
        if (newAddressLine != null && !newAddressLine.isEmpty()) {
            try {
                String recipient = trim(request.getParameter("recipientName"));
                String phone = trim(request.getParameter("phone"));
                Address newAddr = new Address();
                newAddr.setUserId(user.getUserID());
                newAddr.setRecipientName(recipient != null && !recipient.isEmpty() ? recipient : user.getFullName());
                newAddr.setPhone(phone != null && !phone.isEmpty() ? phone : user.getPhone());
                newAddr.setAddressLine(newAddressLine);
                newAddr.setWard("");
                newAddr.setDistrict("");
                newAddr.setProvince("");
                newAddr.setDefault(true);
                addressDAO.addAddress(newAddr);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Không thể lưu địa chỉ mới của user khi đăng ký gói: " + user.getUserID(), e);
            }
        }

        Date firstDeliveryDate = null;
        if (deliveryDateStr != null && !deliveryDateStr.isEmpty()) {
            try {
                firstDeliveryDate = new SimpleDateFormat("yyyy-MM-dd").parse(deliveryDateStr);
            } catch (ParseException e) {
                // Sẽ được validate tại Service
            }
        }

        // Gọi service xử lý toàn bộ logic nghiệp vụ
        SubscriptionService.ServiceResult<Integer> result =
                subscriptionService.subscribeToComboPlan(user.getUserID(), planId, firstDeliveryDate, notes);

        if (result.isSuccess()) {
            session.setAttribute("toastSuccess", result.getMessage());
            response.sendRedirect(request.getContextPath() + "/my-subscriptions?success=1");
        } else {
            // Có lỗi nghiệp vụ -> trả về form checkout kèm thông báo lỗi
            request.setAttribute("errorMessage", result.getMessage());
            if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                request.setAttribute("errorList", result.getErrors());
            }

            SubscriptionPlan plan = subscriptionService.getActivePlanDetail(planId);
            if (plan != null) {
                request.setAttribute("plan", plan);
                request.setAttribute("savings", subscriptionService.calculatePlanSavings(plan));
                try {
                    request.setAttribute("addresses", addressDAO.getAddressesByUserId(user.getUserID()));
                } catch (Exception ignored) {}
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            String defaultDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            String minDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            request.setAttribute("defaultDate", defaultDateStr);
            request.setAttribute("minDate", minDateStr);
            request.setAttribute("selectedDate", deliveryDateStr);
            request.setAttribute("notes", notes);
            request.getRequestDispatcher("/subscription/subscribeCheckout.jsp").forward(request, response);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TIỆN ÍCH HELPER
    // ─────────────────────────────────────────────────────────────────────
    private String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private int parseSafeInt(String s, int def) {
        if (s == null || s.trim().isEmpty()) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private Integer parseOptionalInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
