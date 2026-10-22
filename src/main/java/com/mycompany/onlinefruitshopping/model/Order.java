package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int orderId;
    private int userId;
    private int shopId;
    private int addressId;
    private Integer voucherId;
    private String status;
    private double totalAmount;
    private Timestamp createdAt;

    // Joined customer and address details
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String shopName;

    // Checkout & Payment details
    private String voucherCode;
    private double discountAmount;
    private double shippingFee;
    private String deliveryMethod = "Standard";
    private String paymentMethod = "COD";
    private String note;

    // Delivery staff assignment
    private Integer deliveryStaffId;
    private String deliveryStaffName;
    private String deliveryStaffPhone;
    private String deliveryStatus;

    // Associated items and history
    private List<OrderItem> items = new ArrayList<>();
    private List<OrderStatusHistory> historyList = new ArrayList<>();

    public Order() {
    }

    public Order(int orderId, int userId, int shopId, int addressId, Integer voucherId, String status, double totalAmount, Timestamp createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.shopId = shopId;
        this.addressId = addressId;
        this.voucherId = voucherId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public String getStatusDisplayName() {
        if (status == null) return "Chưa xác định";
        switch (status) {
            case "pending_confirmation":
                return "Chờ xác nhận";
            case "preparing":
                return "Đang đóng gói";
            case "shipping":
                return "Đang giao hàng";
            case "delivered":
                return "Đã giao thành công";
            case "cancelled":
                return "Đã hủy";
            case "failed":
                return "Giao thất bại";
            default:
                return status;
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case "pending_confirmation":
                return "badge-warning";
            case "preparing":
                return "badge-info";
            case "shipping":
                return "badge-primary";
            case "delivered":
                return "badge-success";
            case "cancelled":
            case "failed":
                return "badge-danger";
            default:
                return "badge-secondary";
        }
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        int count = 0;
        for (OrderItem item : items) {
            count += (int) item.getQuantity();
        }
        return count;
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

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public boolean isCancelable() {
        return "pending_confirmation".equalsIgnoreCase(status) || "preparing".equalsIgnoreCase(status);
    }

    public String getFormattedTotalAmount() {
        return String.format("%,.0f ₫", totalAmount);
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getDeliveryStaffId() {
        return deliveryStaffId;
    }

    public void setDeliveryStaffId(Integer deliveryStaffId) {
        this.deliveryStaffId = deliveryStaffId;
    }

    public String getDeliveryStaffName() {
        return deliveryStaffName;
    }

    public void setDeliveryStaffName(String deliveryStaffName) {
        this.deliveryStaffName = deliveryStaffName;
    }

    public String getDeliveryStaffPhone() {
        return deliveryStaffPhone;
    }

    public void setDeliveryStaffPhone(String deliveryStaffPhone) {
        this.deliveryStaffPhone = deliveryStaffPhone;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public List<OrderStatusHistory> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<OrderStatusHistory> historyList) {
        this.historyList = historyList;
    }
}
