package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;

public class Payment {

    private int paymentId;
    private int orderId;
    private String method; // 'vnpay', 'momo', 'card', 'cod'
    private double amount;
    private String status; // 'pending', 'paid', 'failed'
    private String transactionRef;
    private Timestamp paidAt;

    // Joined details
    private int userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private int shopId;
    private String shopName;
    private Timestamp orderCreatedAt;

    public Payment() {
    }

    public Payment(int paymentId, int orderId, String method, double amount, String status, String transactionRef, Timestamp paidAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.transactionRef = transactionRef;
        this.paidAt = paidAt;
    }

    public String getStatusDisplayName() {
        if (status == null) return "Chờ thanh toán";
        switch (status.toLowerCase()) {
            case "paid":
                return "Đã thanh toán";
            case "pending":
                return "Chờ thanh toán";
            case "failed":
                return "Thanh toán thất bại";
            default:
                return status;
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-warning";
        switch (status.toLowerCase()) {
            case "paid":
                return "badge-success";
            case "pending":
                return "badge-warning";
            case "failed":
                return "badge-danger";
            default:
                return "badge-secondary";
        }
    }

    public String getMethodDisplayName() {
        if (method == null) return "COD";
        switch (method.toLowerCase()) {
            case "vnpay":
                return "VNPAY (Thẻ / QR / E-Wallet)";
            case "momo":
                return "MoMo E-Wallet";
            case "card":
                return "Thẻ Tín Dụng / Ghi Nợ";
            case "cod":
                return "Tiền mặt khi nhận hàng (COD)";
            default:
                return method.toUpperCase();
        }
    }

    public String getFormattedAmount() {
        return String.format("%,.0f ₫", amount);
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public Timestamp getOrderCreatedAt() {
        return orderCreatedAt;
    }

    public void setOrderCreatedAt(Timestamp orderCreatedAt) {
        this.orderCreatedAt = orderCreatedAt;
    }
}
