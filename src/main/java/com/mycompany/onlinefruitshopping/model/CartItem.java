package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;

public class CartItem implements Serializable {

    private int cartItemId;
    private int variantId;
    private String productName;
    private String packagingType;
    private String unit;
    private double unitPrice;
    private double quantity;
    private String imageUrl;
    private double stockQuantity;

    public CartItem() {
    }

    public CartItem(int cartItemId, int variantId, String productName, String packagingType, 
                    String unit, double unitPrice, double quantity, String imageUrl, double stockQuantity) {
        this.cartItemId = cartItemId;
        this.variantId = variantId;
        this.productName = productName;
        this.packagingType = packagingType;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(double stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getItemTotal() {
        return this.unitPrice * this.quantity;
    }
}
