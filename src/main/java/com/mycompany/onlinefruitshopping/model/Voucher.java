package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;

public class Voucher {

    private int voucherId;
    private String code;
    private String discountType; // "percent" | "fixed_amount"
    private double discountValue;
    private Timestamp startDate;
    private Timestamp endDate;
    private Integer usageLimit;
    private String status; // "active" | "expired" | "disabled"

    public Voucher() {
    }

    public Voucher(int voucherId, String code, String discountType, double discountValue,
                   Timestamp startDate, Timestamp endDate, Integer usageLimit, String status) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.usageLimit = usageLimit;
        this.status = status;
    }

    public double calculateDiscount(double subtotal) {
        if (subtotal <= 0) return 0;
        double discount = 0;
        if ("percent".equalsIgnoreCase(discountType)) {
            discount = subtotal * (discountValue / 100.0);
        } else if ("fixed_amount".equalsIgnoreCase(discountType)) {
            discount = discountValue;
        }
        if (discount > subtotal) {
            discount = subtotal;
        }
        return Math.round(discount);
    }

    public boolean isValid() {
        if (!"active".equalsIgnoreCase(status)) return false;
        long now = System.currentTimeMillis();
        if (startDate != null && now < startDate.getTime()) return false;
        if (endDate != null && now > endDate.getTime()) return false;
        if (usageLimit != null && usageLimit <= 0) return false;
        return true;
    }

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
