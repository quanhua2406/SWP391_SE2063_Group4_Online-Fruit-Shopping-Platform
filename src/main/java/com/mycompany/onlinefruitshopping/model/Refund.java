package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;

public class Refund {

    private int refundId;
    private int paymentId;
    private Integer processedBy;
    private double amount;
    private String status; // 'requested', 'approved', 'rejected', 'completed'
    private String reason;
    private Timestamp requestedAt;
    private Timestamp processedAt;

    // Joined details
    private int orderId;
    private int userId;
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private String processedByName;

    public Refund() {
    }

    public String getStatusDisplayName() {
        if (status == null) return "Chờ xét duyệt";
        switch (status.toLowerCase()) {
            case "requested":
                return "Chờ xét duyệt";
            case "approved":
                return "Đã chấp thuận";
            case "rejected":
                return "Đã từ chối";
            case "completed":
                return "Đã hoàn tiền thành công";
            default:
                return status;
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-warning";
        switch (status.toLowerCase()) {
            case "requested":
                return "badge-warning";
            case "approved":
                return "badge-info";
            case "rejected":
                return "badge-danger";
            case "completed":
                return "badge-success";
            default:
                return "badge-secondary";
        }
    }

    public String getFormattedAmount() {
        return String.format("%,.0f ₫", amount);
    }

    public int getRefundId() {
        return refundId;
    }

    public void setRefundId(int refundId) {
        this.refundId = refundId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getProcessedByName() {
        return processedByName;
    }

    public void setProcessedByName(String processedByName) {
        this.processedByName = processedByName;
    }
}
