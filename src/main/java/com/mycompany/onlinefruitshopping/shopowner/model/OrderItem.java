package com.mycompany.onlinefruitshopping.shopowner.model;

/** Model chứa dữ liệu; getter để JSP đọc, setter để DAO gán giá trị. */
public class OrderItem {
    private String productName;
    private String variantName;
    private java.math.BigDecimal price;
    private int quantity;

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public java.math.BigDecimal getSubtotal() {
        return price.multiply(java.math.BigDecimal.valueOf(quantity));
    }
}
