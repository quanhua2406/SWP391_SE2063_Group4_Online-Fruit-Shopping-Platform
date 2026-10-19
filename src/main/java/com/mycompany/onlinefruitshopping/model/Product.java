package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;

public class Product {

    private int productId;
    private int shopId;
    private String shopName;
    private int categoryId;
    private String categoryName;
    private String productName;
    private String description;
    private String origin;
    private String status;           // 'published', 'hidden', 'draft'
    private double price;
    private String unit;             // 'kg', 'box', 'piece'
    private String packagingType;    // e.g. "Túi 1kg", "Hộp 500g"
    private String sku;
    private double stock;
    private double commissionRate;   // platform commission % (0 - 100)
    private String imageUrl;
    private Timestamp createdAt;

    public Product() {
        this.status = "published";
        this.unit = "kg";
    }

    public Product(int productId, int shopId, int categoryId, String productName,
                   String description, String origin, String status, double price,
                   String unit, String packagingType, double stock, String imageUrl) {
        this.productId = productId;
        this.shopId = shopId;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.origin = origin;
        this.status = status;
        this.price = price;
        this.unit = unit;
        this.packagingType = packagingType;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    /**
     * Platform commission percentage (0 - 100) kept on each sale
     * of this product.
     */
    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    /**
     * Amount the platform keeps per unit, based on the price and
     * the commission rate.
     */
    public double getPlatformCommission() {
        return price * commissionRate / 100.0;
    }

    /**
     * Amount the shop owner receives per unit after commission.
     */
    public double getShopEarning() {
        return price - getPlatformCommission();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}