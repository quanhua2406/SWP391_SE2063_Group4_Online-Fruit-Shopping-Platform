package com.mycompany.onlinefruitshopping.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Model chß╗⌐a dß╗» liß╗çu cho JSP; DAO d├╣ng setter ─æß╗â g├ín dß╗» liß╗çu SQL. */
public class CouponQuote {
    private String code;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
}
