package com.mycompany.onlinefruitshopping.model;

import java.math.BigDecimal;

/** Mß╗Öt d├▓ng xem thß╗¡ gi├í, kh├┤ng ghi ─æ├¿ gi├í sß║ún phß║⌐m hoß║╖c chi tiß║┐t ─æ╞ín c┼⌐. */
public class PromotionPrice {
    private String productName;
    private String variantName;
    private boolean productActive;
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public boolean isProductActive() { return productActive; }
    public void setProductActive(boolean productActive) { this.productActive = productActive; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public BigDecimal getDiscountAmount() { return originalPrice.subtract(finalPrice); }
}
