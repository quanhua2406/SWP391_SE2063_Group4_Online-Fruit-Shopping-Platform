package com.mycompany.onlinefruitshopping.shopowner.model;

/** Model chứa dữ liệu; getter để JSP đọc, setter để DAO gán giá trị. */
public class Delivery {
    private int id;
    private int orderId;
    private int shopId;
    private String shopName;
    private String pickupAddress;
    private int shipperId;
    private String shipperName;
    private String status;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private java.math.BigDecimal totalAmount;
    private String orderStatus;
    private String paymentMethod;
    private String paymentStatus;
    private String orderNote;
    private String note;
    private java.sql.Timestamp pickupTime;
    private java.sql.Timestamp deliveredTime;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public int getShipperId() { return shipperId; }
    public void setShipperId(int shipperId) { this.shipperId = shipperId; }

    public String getShipperName() { return shipperName; }
    public void setShipperName(String shipperName) { this.shipperName = shipperName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public java.sql.Timestamp getPickupTime() { return pickupTime; }
    public void setPickupTime(java.sql.Timestamp pickupTime) { this.pickupTime = pickupTime; }

    public java.sql.Timestamp getDeliveredTime() { return deliveredTime; }
    public void setDeliveredTime(java.sql.Timestamp deliveredTime) { this.deliveredTime = deliveredTime; }

    public String getStatusLabel() { return com.mycompany.onlinefruitshopping.shopowner.utils.DeliveryRules.label(status); }
}
