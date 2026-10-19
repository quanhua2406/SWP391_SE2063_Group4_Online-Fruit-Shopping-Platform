package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;

/**
 * Model representing an item in the shopping cart.
 * Feature 5: Shopping Cart Management - In Charge: Le Duc Cuong (cuongld)
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private int cartItemId;
    private int cartId;
    private int variantId;
    private int productId;
    private String productName;
    private String packagingType;
    private String unit;
    private String sku;
    private double unitPrice;
    private int quantity;
    private String imageUrl;
    private double stockQuantity;
    private boolean activeProduct;
    private String origin;

    public CartItem() {
        this.quantity = 1;
        this.activeProduct = true;
    }

    public CartItem(int cartItemId, int cartId, int variantId, int productId, String productName,
                    String packagingType, String unit, String sku, double unitPrice,
                    int quantity, String imageUrl, double stockQuantity,
                    boolean activeProduct, String origin) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.variantId = variantId;
        this.productId = productId;
        this.productName = productName;
        this.packagingType = packagingType;
        this.unit = unit;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
        this.activeProduct = activeProduct;
        this.origin = origin;
    }

    public double getItemTotal() {
        return this.unitPrice * this.quantity;
    }

    public boolean isAvailable() {
        return this.activeProduct && this.stockQuantity > 0;
    }

    public boolean hasSufficientStock() {
        return this.quantity <= this.stockQuantity && this.stockQuantity > 0;
    }

    // Getters and Setters
    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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

    public boolean isActiveProduct() {
        return activeProduct;
    }

    public void setActiveProduct(boolean activeProduct) {
        this.activeProduct = activeProduct;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
