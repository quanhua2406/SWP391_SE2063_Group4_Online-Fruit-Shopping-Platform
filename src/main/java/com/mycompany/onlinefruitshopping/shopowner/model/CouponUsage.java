package com.mycompany.onlinefruitshopping.shopowner.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Model chứa dữ liệu cho JSP; DAO dùng setter để gán dữ liệu SQL. */
public class CouponUsage {
    private int id;
    private int orderId;
    private String code;
    private String customerName;
    private String shopName;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime usedAt;
    private String usageStatus;
    private String orderStatus;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public String getUsageStatus() { return usageStatus; }
    public void setUsageStatus(String usageStatus) { this.usageStatus = usageStatus; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getTimeLabel() { return usedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")); }
    public String getStatusLabel() {
        if ("Removed".equals(usageStatus)) return "Đã gỡ — hoàn lượt";
        if ("Cancelled".equals(orderStatus)) return "Đơn hủy — hoàn lượt";
        return "Đã áp dụng";
    }
}
