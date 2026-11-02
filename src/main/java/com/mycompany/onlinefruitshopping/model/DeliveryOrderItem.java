package com.mycompany.onlinefruitshopping.model;

import java.math.BigDecimal;

public class DeliveryOrderItem {

    private String productName;
    private String variantName;
    private BigDecimal price;
    private BigDecimal quantity;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = BigDecimal.valueOf(quantity);
    }

    public BigDecimal getSubtotal() {
        return price.multiply(quantity);
    }
}
