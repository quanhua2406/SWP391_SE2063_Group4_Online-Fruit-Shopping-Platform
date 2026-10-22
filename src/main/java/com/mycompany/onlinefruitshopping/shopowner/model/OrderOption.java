package com.mycompany.onlinefruitshopping.shopowner.model;

/** Model chứa dữ liệu; getter để JSP đọc, setter để DAO gán giá trị. */
public class OrderOption {
    private int id;
    private int shopId;
    private String shopName;
    private String receiverName;
    private String shippingAddress;
    private java.math.BigDecimal totalAmount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
