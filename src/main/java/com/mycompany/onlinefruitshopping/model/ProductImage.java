package com.mycompany.onlinefruitshopping.model;

public class ProductImage {

    private int imageId;
    private int productId;
    private String imageUrl;
    private int sortOrder;

    public ProductImage() {
    }

    public ProductImage(int imageId, int productId, String imageUrl, int sortOrder) {
        this.imageId = imageId;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
