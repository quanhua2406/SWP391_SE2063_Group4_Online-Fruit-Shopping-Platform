package com.mycompany.onlinefruitshopping.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Model chß╗⌐a dß╗» liß╗çu cho JSP; DAO d├╣ng setter ─æß╗â g├ín dß╗» liß╗çu SQL. */
public class CouponOrder {
    private int id;
    private int shopId;
    private String shopName;
    private String orderStatus;
    private String paymentStatus;
    private Integer voucherId;
    private Integer couponId;
    private String couponCode;
    private BigDecimal totalAmount;
    private BigDecimal couponDiscount;
    private BigDecimal couponSubtotal;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }
    public Integer getCouponId() { return couponId; }
    public void setCouponId(Integer couponId) { this.couponId = couponId; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getCouponDiscount() { return couponDiscount; }
    public void setCouponDiscount(BigDecimal couponDiscount) { this.couponDiscount = couponDiscount; }
    public BigDecimal getCouponSubtotal() { return couponSubtotal; }
    public void setCouponSubtotal(BigDecimal couponSubtotal) { this.couponSubtotal = couponSubtotal; }

    public boolean isEditable() { return "Pending".equals(orderStatus) && "Unpaid".equals(paymentStatus); }
}
