package com.mycompany.onlinefruitshopping.controller.subscription;

import com.mycompany.onlinefruitshopping.dao.AddressDAO;
import com.mycompany.onlinefruitshopping.model.Address;
import com.mycompany.onlinefruitshopping.model.Subscription;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlan;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.SubscriptionService;
import com.mycompany.onlinefruitshopping.utils.VNPayConfig;

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
 * - Xử lý callback thanh toán VNPay Sandbox: GET /subscription/vnpay-return
 *
 * Mọi logic nghiệp vụ được ủy quyền cho SubscriptionService.
 */
@WebServlet(name = "CustomerSubscriptionServlet", urlPatterns = {
        "/subscriptions",
        "/subscription/plans",
        "/subscription/detail",
        "/subscription/subscribe",
        "/subscription/vnpay-return"
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
        } else if ("/subscription/vnpay-return".equals(servletPath)) {
            handleVNPayReturn(request, response);
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

        request.getRequestDispatcher("/jsp/subscription/browsePlans.jsp").forward(request, response);
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
        request.getRequestDispatcher("/jsp/subscription/planDetail.jsp").forward(request, response);
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

        request.getRequestDispatcher("/jsp/subscription/subscribeCheckout.jsp").forward(request, response);
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
        String deliveryTimeSlot = trim(request.getParameter("deliveryTimeSlot"));
        String ripenessPreference = trim(request.getParameter("ripenessPreference"));
        String fruitPreferences = trim(request.getParameter("fruitPreferences"));
        String backupPhone = trim(request.getParameter("backupPhone"));
        String rawNotes = trim(request.getParameter("notes"));
        String paymentMethod = trim(request.getParameter("paymentMethod"));
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = "COD";
        }
        boolean isVNPay = "VNPAY".equalsIgnoreCase(paymentMethod);

        // Lưu địa chỉ mới nếu người dùng nhập trực tiếp (khi chưa có sẵn trong sổ địa chỉ)
        String newAddressLine = trim(request.getParameter("addressLine"));
        String addressSummary = null;
        if (newAddressLine != null && !newAddressLine.isEmpty()) {
            try {
                String recipient = trim(request.getParameter("recipientName"));
                String phone = trim(request.getParameter("phone"));
                String finalRecipient = recipient != null && !recipient.isEmpty() ? recipient : user.getFullName();
                String finalPhone = phone != null && !phone.isEmpty() ? phone : user.getPhone();
                Address newAddr = new Address();
                newAddr.setUserId(user.getUserID());
                newAddr.setRecipientName(finalRecipient);
                newAddr.setPhone(finalPhone);
                newAddr.setAddressLine(newAddressLine);
                newAddr.setWard("");
                newAddr.setDistrict("");
                newAddr.setProvince("");
                newAddr.setDefault(true);
                addressDAO.addAddress(newAddr);
                addressSummary = finalRecipient + " (" + finalPhone + ") - " + newAddressLine;
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Không thể lưu địa chỉ mới của user khi đăng ký gói: " + user.getUserID(), e);
            }
        } else {
            int selectedAddressId = parseSafeInt(request.getParameter("selectedAddressId"), -1);
            if (selectedAddressId > 0) {
                try {
                    List<Address> userAddresses = addressDAO.getAddressesByUserId(user.getUserID());
                    for (Address a : userAddresses) {
                        if (a.getAddressId() == selectedAddressId) {
                            addressSummary = a.getRecipientName() + " (" + a.getPhone() + ") - "
                                    + a.getAddressLine()
                                    + (a.getWard() != null && !a.getWard().isEmpty() ? ", " + a.getWard() : "")
                                    + (a.getDistrict() != null && !a.getDistrict().isEmpty() ? ", " + a.getDistrict() : "")
                                    + (a.getProvince() != null && !a.getProvince().isEmpty() ? ", " + a.getProvince() : "");
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Tổng hợp ghi chú chi tiết
        StringBuilder sbNotes = new StringBuilder();
        if (addressSummary != null && !addressSummary.isEmpty()) {
            sbNotes.append("📍 Địa chỉ nhận: ").append(addressSummary).append("\n");
        }
        if (isVNPay) {
            sbNotes.append("💳 Thanh toán: Chờ xác nhận qua cổng VNPay Sandbox\n");
        } else {
            sbNotes.append("💳 Thanh toán: Thanh toán khi nhận hàng mỗi kỳ (COD)\n");
        }
        if (deliveryTimeSlot != null && !deliveryTimeSlot.isEmpty()) {
            sbNotes.append("⏰ Khung giờ: ").append(deliveryTimeSlot).append("\n");
        }
        if (ripenessPreference != null && !ripenessPreference.isEmpty()) {
            sbNotes.append("🥑 Độ chín: ").append(ripenessPreference).append("\n");
        }
        if (fruitPreferences != null && !fruitPreferences.isEmpty()) {
            sbNotes.append("🚫 Dị ứng/Tránh: ").append(fruitPreferences).append("\n");
        }
        if (backupPhone != null && !backupPhone.isEmpty()) {
            sbNotes.append("📞 SĐT phụ: ").append(backupPhone).append("\n");
        }
        if (rawNotes != null && !rawNotes.isEmpty()) {
            sbNotes.append("📝 Ghi chú: ").append(rawNotes);
        }
        String combinedNotes = sbNotes.toString().trim();
        if (combinedNotes.isEmpty()) combinedNotes = null;

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
                subscriptionService.subscribeToComboPlan(user.getUserID(), planId, firstDeliveryDate, combinedNotes);

        if (result.isSuccess()) {
            int subId = result.getData() != null ? result.getData() : -1;

            // Nếu khách chọn Ví điện tử / VNPay -> chuyển hướng sang cổng VNPay Sandbox
            if (isVNPay && subId > 0) {
                SubscriptionPlan plan = subscriptionService.getActivePlanDetail(planId);
                double amount = (plan != null && plan.getPrice() != null) ? plan.getPrice().doubleValue() : 0;

                if (amount > 0) {
                    String scheme = request.getScheme();
                    String serverName = request.getServerName();
                    int serverPort = request.getServerPort();
                    String contextPath = request.getContextPath();
                    String returnUrl = scheme + "://" + serverName
                            + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort)
                            + contextPath + "/subscription/vnpay-return";

                    String clientIp = request.getHeader("X-FORWARDED-FOR");
                    if (clientIp == null || clientIp.isBlank()) {
                        clientIp = request.getRemoteAddr();
                    }

                    String orderInfo = "Thanh toan goi dinh ky #" + subId;
                    String vnpayUrl = VNPayConfig.buildPaymentUrl(returnUrl, subId, amount, orderInfo, clientIp);
                    response.sendRedirect(vnpayUrl);
                    return;
                }
            }

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
            request.setAttribute("selectedTimeSlot", deliveryTimeSlot);
            request.setAttribute("selectedRipeness", ripenessPreference);
            request.setAttribute("fruitPreferences", fruitPreferences);
            request.setAttribute("backupPhone", backupPhone);
            request.setAttribute("notes", rawNotes);
            request.setAttribute("selectedPaymentMethod", paymentMethod);
            request.getRequestDispatcher("/jsp/subscription/subscribeCheckout.jsp").forward(request, response);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. XỬ LÝ CALLBACK TRẢ VỀ TỪ CỔNG VNPAY SANDBOX (GET /subscription/vnpay-return)
    // ─────────────────────────────────────────────────────────────────────
    private void handleVNPayReturn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTxnRef = request.getParameter("vnp_TxnRef");
        String vnpAmountStr = request.getParameter("vnp_Amount");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
        String vnpBankCode = request.getParameter("vnp_BankCode");

        int subId = 0;
        if (vnpTxnRef != null && !vnpTxnRef.isBlank()) {
            try {
                if (vnpTxnRef.contains("_")) {
                    subId = Integer.parseInt(vnpTxnRef.split("_")[0]);
                } else {
                    subId = Integer.parseInt(vnpTxnRef);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (subId <= 0) {
            session.setAttribute("toastError", "Mã giao dịch gói đăng ký phản hồi từ VNPay không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-subscriptions");
            return;
        }

        Subscription sub = subscriptionService.getCustomerSubscriptionDetail(subId, user.getUserID());
        if (sub == null) {
            session.setAttribute("toastError", "Không tìm thấy gói đăng ký tương ứng.");
            response.sendRedirect(request.getContextPath() + "/my-subscriptions");
            return;
        }

        // 1. Kiểm tra chữ ký bảo mật Checksum HMAC-SHA512 từ VNPay
        boolean isValidChecksum = VNPayConfig.verifyCallback(request.getParameterMap());
        if (!isValidChecksum) {
            subscriptionService.cancelUnpaidVNPaySubscription(subId, user.getUserID(), "97");
            session.setAttribute("toastError", "Chữ ký bảo mật VNPay không hợp lệ (Sai mã băm Checksum).");
            response.sendRedirect(request.getContextPath() + "/my-subscriptions");
            return;
        }

        // 2. Kiểm tra số tiền thanh toán
        if (vnpAmountStr != null && !vnpAmountStr.isBlank() && sub.getPrice_snapshot() != null) {
            try {
                long vnpAmount = Long.parseLong(vnpAmountStr) / 100;
                long expectedAmount = Math.round(sub.getPrice_snapshot().doubleValue());
                if (vnpAmount != expectedAmount) {
                    subscriptionService.cancelUnpaidVNPaySubscription(subId, user.getUserID(), "04");
                    session.setAttribute("toastError", "Số tiền thanh toán phản hồi từ VNPay không khớp với giá trị gói.");
                    response.sendRedirect(request.getContextPath() + "/my-subscriptions");
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        // 3. Xử lý kết quả giao dịch
        if ("00".equals(vnpResponseCode)) {
            SubscriptionService.ServiceResult<Subscription> confirmRes =
                    subscriptionService.confirmVNPaySubscriptionPayment(
                            subId,
                            user.getUserID(),
                            vnpTransactionNo != null && !vnpTransactionNo.isBlank() ? vnpTransactionNo : vnpTxnRef,
                            vnpBankCode != null ? vnpBankCode : "VNPAY"
                    );
            session.setAttribute("toastSuccess", confirmRes.getMessage());
            response.sendRedirect(request.getContextPath() + "/my-subscriptions?success=1");
        } else {
            // Khách hàng hủy giao dịch trên VNPay Sandbox (code 24) hoặc thất bại -> Hủy gói để khách có thể đăng ký lại
            subscriptionService.cancelUnpaidVNPaySubscription(subId, user.getUserID(), vnpResponseCode);
            String cancelMsg = "24".equals(vnpResponseCode)
                    ? "Bạn đã hủy thanh toán trên cổng VNPay. Vui lòng thử lại hoặc chọn phương thức thanh toán khác."
                    : "Giao dịch thanh toán VNPay không thành công (Mã lỗi: " + vnpResponseCode + "). Vui lòng thử lại.";
            session.setAttribute("toastError", cancelMsg);
            if (sub.getPlan_id() != null && sub.getPlan_id() > 0) {
                response.sendRedirect(request.getContextPath() + "/subscription/subscribe?planId=" + sub.getPlan_id());
            } else {
                response.sendRedirect(request.getContextPath() + "/my-subscriptions");
            }
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
