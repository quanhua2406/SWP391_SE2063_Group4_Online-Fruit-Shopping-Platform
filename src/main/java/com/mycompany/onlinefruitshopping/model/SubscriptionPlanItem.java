package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;

public class SubscriptionPlanItem implements Serializable {
    private static final long serialVersionUID = 1L;

    // ── Trường DB ───────────────────────────────────────────────────────
    private int    itemId;
    private int    planId;
    private int    variantId;
    private int    quantity;

    // ── Trường phụ – JOIN với product_variants / products ───────────────
    private String productName;
    private String variantName;   // tên biến thể (vd: "500g", "1kg")
    private String imageUrl;
    private double unitPrice;     // giá đơn vị của variant

    // ── Constructors ─────────────────────────────────────────────────────
    public SubscriptionPlanItem() {}

    public SubscriptionPlanItem(int planId, int variantId, int quantity) {
        this.planId    = planId;
        this.variantId = variantId;
        this.quantity  = quantity;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public int getItemId()                        { return itemId; }
    public void setItemId(int itemId)             { this.itemId = itemId; }

    public int getPlanId()                        { return planId; }
    public void setPlanId(int planId)             { this.planId = planId; }

    public int getVariantId()                     { return variantId; }
    public void setVariantId(int variantId)       { this.variantId = variantId; }

    public int getQuantity()                      { return quantity; }
    public void setQuantity(int quantity)         { this.quantity = quantity; }

    public String getProductName()                { return productName; }
    public void setProductName(String name)       { this.productName = name; }

    public String getVariantName()                { return variantName; }
    public void setVariantName(String name)       { this.variantName = name; }

    public String getImageUrl()                   { return imageUrl; }
    public void setImageUrl(String url)           { this.imageUrl = url; }

    public double getUnitPrice()                  { return unitPrice; }
    public void setUnitPrice(double price)        { this.unitPrice = price; }

    public String getUnit()                       { return variantName != null ? variantName : ""; }
    public void setUnit(String unit)              { this.variantName = unit; }
}

