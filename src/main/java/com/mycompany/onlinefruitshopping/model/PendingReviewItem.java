package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Model biểu diễn sản phẩm từ đơn hàng đã giao thành công mà khách hàng chưa đánh giá.
 */
public class PendingReviewItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int orderId;
    private Date orderDate;
    private int productId;
    private String productName;
    private String productImage;
    private double price;

    public PendingReviewItem() {
    }

    public PendingReviewItem(int orderId, Date orderDate, int productId, String productName, String productImage, double price) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.price = price;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
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

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
