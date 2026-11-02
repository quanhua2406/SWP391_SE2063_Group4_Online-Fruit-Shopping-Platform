package com.mycompany.onlinefruitshopping.controller.payment;

import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.Payment;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.OrderService;
import com.mycompany.onlinefruitshopping.service.PaymentService;
import com.mycompany.onlinefruitshopping.utils.VNPayConfig;
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

/**
 * Servlet điều phối cổng thanh toán trực tuyến VNPay Sandbox & VietQR (Tasks 1, 2, 3, 4).
 * Feature 7: Payment Management - In Charge: Le Duc Cuong (cuongld)
 */
@WebServlet(name = "PaymentServlet", urlPatterns = {
        "/payment/process",
        "/payment/vnpay-return",
        "/payment/vnpay-ipn",
        "/payment/success",
        "/payment/confirm-simulated"
})
public class PaymentServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        try {
            switch (path) {
                case "/payment/process":
                    handlePaymentProcess(request, response);
                    break;
                case "/payment/vnpay-return":
                    handleVNPayReturn(request, response);
                    break;
                case "/payment/vnpay-ipn":
                    handleVNPayIPN(request, response);
                    break;
                case "/payment/success":
                    handlePaymentSuccess(request, response);
                    break;
                case "/payment/confirm-simulated":
                    handleSimulatedPayment(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/orders");
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in PaymentServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Màn hình chuẩn bị và chuyển hướng thanh toán trực tiếp qua cổng VNPay Sandbox (Tasks 1 & 2).
     */
    private void handlePaymentProcess(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr.trim());
            if (orderId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        // Lấy chi tiết đơn hàng
        Order order = orderService.getCustomerOrderDetail(orderId, user.getUserID());
        if (order == null) {
            order = orderService.getOrderById(orderId);
        }
        if (order == null) {
            response.sendRedirect(request.getContextPath() + "/orders?error="
                    + URLEncoder.encode("Đơn hàng không tồn tại.", StandardCharsets.UTF_8));
            return;
        }

        // Validation quyền sở hữu đơn hàng (chỉ khách hàng tạo đơn hoặc Admin mới được thanh toán)
        if (order.getUserId() != user.getUserID() && !"admin".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/orders?error="
                    + URLEncoder.encode("Bạn không có quyền thực hiện thanh toán cho đơn hàng này.", StandardCharsets.UTF_8));
            return;
        }

        // Validation trạng thái đơn: Không được thanh toán đơn đã bị hủy
        if ("cancelled".equalsIgnoreCase(order.getStatus())) {
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&error="
                    + URLEncoder.encode("Đơn hàng này đã bị hủy, không thể tiếp tục thanh toán.", StandardCharsets.UTF_8));
            return;
        }

        // Validation trạng thái thanh toán: Đã thanh toán thì chuyển sang trang thành công
        if ("paid".equalsIgnoreCase(order.getPaymentStatus())) {
            response.sendRedirect(request.getContextPath() + "/payment/success?orderId=" + orderId);
            return;
        }

        // Validation trạng thái đơn đã giao
        if ("delivered".equalsIgnoreCase(order.getStatus())) {
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&msg="
                    + URLEncoder.encode("Đơn hàng đã hoàn tất giao hàng.", StandardCharsets.UTF_8));
            return;
        }

        // Validation số tiền thanh toán
        if (order.getTotalAmount() <= 0) {
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&error="
                    + URLEncoder.encode("Số tiền thanh toán đơn hàng không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }

        // Tạo hoặc lấy bản ghi Payment với trạng thái pending
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null) {
            paymentService.createPayment(orderId, "vnpay", order.getTotalAmount());
            payment = paymentService.getPaymentByOrderId(orderId);
        }

        // Xây dựng VNPay Sandbox Redirect URL
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String returnUrl = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort)
                + contextPath + "/payment/vnpay-return";

        String clientIp = request.getHeader("X-FORWARDED-FOR");
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = request.getRemoteAddr();
        }

        String vnpayUrl = paymentService.processPayment(orderId, "vnpay", returnUrl, clientIp);

        if (vnpayUrl == null || vnpayUrl.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&error="
                    + URLEncoder.encode("Không thể kết nối đến cổng VNPay Sandbox. Vui lòng thử lại sau.", StandardCharsets.UTF_8));
            return;
        }

        // Chuyển hướng trực tiếp thẳng sang cổng VNPay Sandbox để thanh toán
        response.sendRedirect(vnpayUrl);
    }

    /**
     * Nhận callback phản hồi từ cổng thanh toán VNPay (Task 3).
     * Khi khách hàng hoàn tất hoặc quay lại / hủy giao dịch từ VNPay Sandbox.
     */
    private void handleVNPayReturn(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTxnRef = request.getParameter("vnp_TxnRef");
        String vnpAmountStr = request.getParameter("vnp_Amount");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
        String vnpBankCode = request.getParameter("vnp_BankCode");

        int orderId = 0;
        if (vnpTxnRef != null && !vnpTxnRef.isBlank()) {
            try {
                if (vnpTxnRef.contains("_")) {
                    orderId = Integer.parseInt(vnpTxnRef.split("_")[0]);
                } else {
                    orderId = Integer.parseInt(vnpTxnRef);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (orderId <= 0) {
            response.sendRedirect(request.getContextPath() + "/orders?error="
                    + URLEncoder.encode("Mã đơn hàng phản hồi từ VNPay không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            response.sendRedirect(request.getContextPath() + "/orders?error=not_found");
            return;
        }

        // 1. Kiểm tra chữ ký bảo mật Checksum HMAC-SHA512
        boolean isValidChecksum = VNPayConfig.verifyCallback(request.getParameterMap());
        if (!isValidChecksum) {
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&error="
                    + URLEncoder.encode("Chữ ký bảo mật VNPay không hợp lệ (Sai mã băm Checksum).", StandardCharsets.UTF_8));
            return;
        }

        // 2. Kiểm tra tính toàn vẹn số tiền thanh toán (Amount Tampering Validation)
        if (vnpAmountStr != null && !vnpAmountStr.isBlank()) {
            try {
                long vnpAmount = Long.parseLong(vnpAmountStr) / 100;
                long expectedAmount = Math.round(order.getTotalAmount());
                if (vnpAmount != expectedAmount) {
                    paymentService.verifyPaymentCallback(orderId, "04", vnpTxnRef);
                    response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&error="
                            + URLEncoder.encode("Số tiền thanh toán phản hồi từ VNPay không khớp với giá trị đơn hàng.", StandardCharsets.UTF_8));
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        // 3. Xử lý kết quả giao dịch
        if ("00".equals(vnpResponseCode)) {
            // Xác thực hợp lệ và thanh toán thành công
            paymentService.verifyPaymentCallback(orderId, "00", vnpTransactionNo != null ? vnpTransactionNo : vnpTxnRef);
            response.sendRedirect(request.getContextPath() + "/payment/success?orderId=" + orderId
                    + "&txnRef=" + (vnpTransactionNo != null ? vnpTransactionNo : vnpTxnRef)
                    + "&bankCode=" + (vnpBankCode != null ? vnpBankCode : "VNPAY"));
        } else {
            // Khách hàng hủy giao dịch trên VNPay Sandbox (code 24) hoặc giao dịch thất bại
            // Cập nhật trạng thái thanh toán sang failed
            paymentService.verifyPaymentCallback(orderId, vnpResponseCode != null ? vnpResponseCode : "24", vnpTxnRef);

            // Tự động hủy đơn hàng và ghi nhận lịch sử trạng thái nếu đơn đang trong tiến trình chờ
            if ("pending_confirmation".equalsIgnoreCase(order.getStatus()) || "preparing".equalsIgnoreCase(order.getStatus())) {
                try {
                    String cancelReason = "24".equals(vnpResponseCode)
                            ? "Khách hàng hủy thanh toán trên cổng VNPay Sandbox"
                            : "Giao dịch VNPay không thành công (Mã lỗi: " + vnpResponseCode + ")";
                    orderService.cancelCustomerOrder(orderId, order.getUserId(), cancelReason);
                } catch (Exception ignored) {}
            }

            String msg = "Giao dịch thanh toán trên VNPay đã kết thúc. Đơn hàng #" + orderId + " đã được tự động hủy thành công.";
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&msg="
                    + URLEncoder.encode(msg, StandardCharsets.UTF_8));
        }
    }

    /**
     * Xử lý IPN ngầm từ VNPay Server gửi sang (Task 3: IPN Verification).
     */
    private void handleVNPayIPN(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTxnRef = request.getParameter("vnp_TxnRef");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");

        int orderId = 0;
        if (vnpTxnRef != null && !vnpTxnRef.isBlank()) {
            try {
                if (vnpTxnRef.contains("_")) {
                    orderId = Integer.parseInt(vnpTxnRef.split("_")[0]);
                } else {
                    orderId = Integer.parseInt(vnpTxnRef);
                }
            } catch (NumberFormatException ignored) {}
        }

        boolean isValid = VNPayConfig.verifyCallback(request.getParameterMap());
        if (isValid) {
            paymentService.verifyPaymentCallback(orderId, vnpResponseCode, vnpTransactionNo);
            response.getWriter().print("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
        } else {
            response.getWriter().print("{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}");
        }
    }

    /**
     * Mô phỏng xác nhận thanh toán thành công (dành cho Demo và kiểm thử VietQR) (Tasks 2 & 3).
     */
    private void handleSimulatedPayment(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String orderIdStr = request.getParameter("orderId");
        int orderId = 0;
        if (orderIdStr != null && !orderIdStr.isBlank()) {
            try {
                orderId = Integer.parseInt(orderIdStr);
            } catch (NumberFormatException ignored) {}
        }

        if (orderId > 0) {
            String fakeTxn = "SIM_" + System.currentTimeMillis();
            paymentService.verifyPaymentCallback(orderId, "00", fakeTxn);
            response.sendRedirect(request.getContextPath() + "/payment/success?orderId=" + orderId + "&txnRef=" + fakeTxn + "&bankCode=VIETQR");
        } else {
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }

    /**
     * Màn hình thông báo thanh toán thành công và hóa đơn xác nhận (Task 4: Payment Confirmation).
     */
    private void handlePaymentSuccess(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String orderIdStr = request.getParameter("orderId");
        int orderId = 0;
        if (orderIdStr != null && !orderIdStr.isBlank()) {
            try {
                orderId = Integer.parseInt(orderIdStr);
            } catch (NumberFormatException ignored) {}
        }

        if (orderId <= 0) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        Order order = orderService.getCustomerOrderDetail(orderId, user.getUserID());
        if (order == null) {
            order = orderService.getOrderById(orderId);
        }
        Payment payment = paymentService.getPaymentByOrderId(orderId);

        request.setAttribute("order", order);
        request.setAttribute("payment", payment);
        request.setAttribute("txnRef", request.getParameter("txnRef"));
        request.setAttribute("bankCode", request.getParameter("bankCode"));

        request.getRequestDispatcher("/jsp/payment/payment-success.jsp").forward(request, response);
    }

    private User getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") instanceof User user) {
            return user;
        }
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + request.getRequestURI());
        return null;
    }
}
