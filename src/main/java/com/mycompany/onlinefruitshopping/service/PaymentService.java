package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.OrderDAO;
import com.mycompany.onlinefruitshopping.dao.PaymentDAO;
import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.Payment;
import com.mycompany.onlinefruitshopping.model.Refund;
import com.mycompany.onlinefruitshopping.utils.VNPayConfig;

import java.sql.SQLException;
import java.util.List;

/**
 * Service Layer xử lý logic nghiệp vụ Quản lý Thanh toán & Hoàn tiền (Payment & Refund Management).
 * Feature 7: Payment Management - In Charge: Le Duc Cuong (cuongld)
 */
public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.orderDAO = new OrderDAO();
    }

    public PaymentService(PaymentDAO paymentDAO, OrderDAO orderDAO) {
        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
    }

    /**
     * Khởi tạo giao dịch thanh toán (Task 2: Create Payment).
     */
    public boolean createPayment(int orderId, String method, double amount) throws SQLException {
        if (orderId <= 0 || amount <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc số tiền thanh toán không hợp lệ.");
        }
        String txnRef = "TXN_" + orderId + "_" + System.currentTimeMillis();
        return paymentDAO.createOrUpdatePayment(orderId, method, amount, "pending", txnRef);
    }

    /**
     * Xử lý giao dịch thanh toán và tạo URL điều hướng tới cổng thanh toán (Task 3: Process Payment).
     */
    public String processPayment(int orderId, String method, String returnUrl, String ipAddress) throws SQLException {
        Payment p = paymentDAO.getPaymentByOrderId(orderId);
        double amount = p != null ? p.getAmount() : 0;
        if (amount <= 0) {
            Order order = orderDAO.getOrderById(orderId, 0);
            if (order != null) {
                amount = order.getTotalAmount();
                createPayment(orderId, method, amount);
            } else {
                throw new IllegalStateException("Không tìm thấy thông tin đơn hàng #" + orderId);
            }
        }

        if ("vnpay".equalsIgnoreCase(method)) {
            String orderInfo = "Thanh toan don hang hoa qua #" + orderId;
            return VNPayConfig.buildPaymentUrl(returnUrl, orderId, amount, orderInfo, ipAddress);
        }

        return null;
    }

    /**
     * Xác thực kết quả giao dịch từ cổng thanh toán (Task 4: Verify Payment Status).
     */
    public boolean verifyPaymentCallback(int orderId, String vnpResponseCode, String txnRef) throws SQLException {
        if (orderId <= 0) return false;

        // Mã "00" đại diện cho giao dịch thành công của VNPay
        boolean isSuccess = "00".equals(vnpResponseCode);
        String status = isSuccess ? "paid" : "failed";

        boolean updated = paymentDAO.updatePaymentStatus(orderId, status, txnRef != null ? txnRef : "VNP_" + orderId);
        if (isSuccess) {
            // Cập nhật trạng thái đơn sang preparing (Đã thanh toán, sẵn sàng chuẩn bị hàng)
            orderDAO.updateOrderStatus(orderId, 0, "preparing", 1, "Thanh toán thành công qua VNPay (Mã GD: " + txnRef + ")");
        }
        return isSuccess;
    }

    /**
     * Lấy thông tin thanh toán theo mã đơn hàng.
     */
    public Payment getPaymentByOrderId(int orderId) throws SQLException {
        return paymentDAO.getPaymentByOrderId(orderId);
    }

    /**
     * Lịch sử giao dịch thanh toán của khách hàng (Task 5: View Payment History).
     */
    public List<Payment> getCustomerPayments(int userId) throws SQLException {
        if (userId <= 0) return List.of();
        return paymentDAO.getPaymentsByUserId(userId);
    }

    /**
     * Toàn bộ lịch sử giao dịch dành cho Admin và Shop Owner (Task 5: View Payment History).
     */
    public List<Payment> getAllPayments(String status, String method, String search) throws SQLException {
        return paymentDAO.getAllPayments(status, method, search);
    }

    /**
     * Gửi yêu cầu hoàn tiền (Task 6: Request Refund).
     */
    public int requestRefund(int userId, int orderId, String reason) throws SQLException {
        if (userId <= 0 || orderId <= 0) {
            throw new IllegalArgumentException("Thông tin yêu cầu hoàn tiền không hợp lệ.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp lý do hoàn tiền cụ thể.");
        }

        Payment payment = paymentDAO.getPaymentByOrderId(orderId);
        if (payment == null) {
            throw new IllegalStateException("Không tìm thấy thông tin giao dịch của đơn hàng #" + orderId);
        }

        if (!"paid".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException("Đơn hàng chưa được thanh toán thành công, không thể gửi yêu cầu hoàn tiền.");
        }

        return paymentDAO.createRefundRequest(payment.getPaymentId(), payment.getAmount(), reason.trim());
    }

    /**
     * Xem danh sách các yêu cầu hoàn tiền của khách hàng.
     */
    public List<Refund> getCustomerRefunds(int userId) throws SQLException {
        if (userId <= 0) return List.of();
        return paymentDAO.getRefundsByUserId(userId);
    }

    /**
     * Danh sách toàn bộ yêu cầu hoàn tiền dành cho Admin / Shop Owner duyệt.
     */
    public List<Refund> getAllRefunds(String status) throws SQLException {
        return paymentDAO.getAllRefunds(status);
    }

    /**
     * Phê duyệt hoàn tiền (Task 7: Approve Refund).
     */
    public boolean approveRefund(int refundId, int processedByUserId) throws SQLException {
        if (refundId <= 0) throw new IllegalArgumentException("Mã yêu cầu hoàn tiền không hợp lệ.");
        return paymentDAO.updateRefundStatus(refundId, "approved", processedByUserId);
    }

    /**
     * Từ chối hoàn tiền (Task 7: Reject Refund).
     */
    public boolean rejectRefund(int refundId, int processedByUserId) throws SQLException {
        if (refundId <= 0) throw new IllegalArgumentException("Mã yêu cầu hoàn tiền không hợp lệ.");
        return paymentDAO.updateRefundStatus(refundId, "rejected", processedByUserId);
    }
}
