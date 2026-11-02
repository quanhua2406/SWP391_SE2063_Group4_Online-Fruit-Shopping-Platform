package com.mycompany.onlinefruitshopping.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;

public class Promotion {
    private int id;
    private int shopId;
    private String shopName;
    private String name = "";
    private String description = "";
    private String discountType = "PERCENT";
    private BigDecimal discountValue;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private boolean active;
    private int version;
    private int productCount;
    private List<Integer> productIds = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }
    public List<Integer> getProductIds() { return productIds; }
    public void setProductIds(List<Integer> productIds) { this.productIds = productIds; }

    // Các getter này giúp JSP hiển thị ngày mà không cần viết Java trong JSP.
    public String getStartInput() { return format(startsAt, "yyyy-MM-dd'T'HH:mm"); }
    public String getEndInput() { return format(endsAt, "yyyy-MM-dd'T'HH:mm"); }
    public String getStartLabel() { return format(startsAt, "dd/MM/yyyy HH:mm"); }
    public String getEndLabel() { return format(endsAt, "dd/MM/yyyy HH:mm"); }
    public String getStatusLabel() { return PromotionRules.status(this, PromotionRules.now()); }
    private String format(LocalDateTime date, String pattern) {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern(pattern));
    }
}
