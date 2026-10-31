package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;

public class Delivery {

    private int deliveryId;
    private int orderId;
    private int staffId;
    private String status; // 'assigned', 'picked_up', 'delivering', 'delivered', 'failed'
    private String proofImageUrl;
    private String note;
    private Timestamp createdAt;

    // Joined staff info
    private String staffName;
    private String staffPhone;

    public Delivery() {
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProofImageUrl() {
        return proofImageUrl;
    }

    public void setProofImageUrl(String proofImageUrl) {
        this.proofImageUrl = proofImageUrl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffPhone() {
        return staffPhone;
    }

    public void setStaffPhone(String staffPhone) {
        this.staffPhone = staffPhone;
    }

    // Shared delivery views; legacy Admin getters above remain compatible.
    private int shopId;
    private String shopName;
    private String pickupAddress;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private java.math.BigDecimal totalAmount;
    private String orderStatus;
    private String paymentMethod;
    private String paymentStatus;
    private String orderNote;
    private java.sql.Timestamp pickupTime;
    private java.sql.Timestamp deliveredTime;
    public int getId() { return deliveryId; }
    public void setId(int deliveryId) { this.deliveryId = deliveryId; }
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public int getShipperId() { return staffId; }
    public void setShipperId(int staffId) { this.staffId = staffId; }
    public String getShipperName() { return staffName; }
    public void setShipperName(String staffName) { this.staffName = staffName; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getOrderNote() { return orderNote; }
    public void setOrderNote(String orderNote) { this.orderNote = orderNote; }
    public java.sql.Timestamp getPickupTime() { return pickupTime; }
    public void setPickupTime(java.sql.Timestamp pickupTime) { this.pickupTime = pickupTime; }
    public java.sql.Timestamp getDeliveredTime() { return deliveredTime; }
    public void setDeliveredTime(java.sql.Timestamp deliveredTime) { this.deliveredTime = deliveredTime; }
    public String getStatusLabel() { return com.mycompany.onlinefruitshopping.utils.DeliveryRules.label(status); }
}
