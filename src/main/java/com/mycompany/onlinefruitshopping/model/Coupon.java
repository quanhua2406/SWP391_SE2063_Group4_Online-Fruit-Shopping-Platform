package com.mycompany.onlinefruitshopping.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Model chß╗⌐a dß╗» liß╗çu cho JSP; DAO d├╣ng setter ─æß╗â g├ín dß╗» liß╗çu SQL. */
public class Coupon {
    private int id;
    private int shopId;
    private String shopName;
    private String code = "";
    private String name = "";
    private String description = "";
    private String discountType = "PERCENT";
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
    private int totalLimit = 100;
    private int perCustomerLimit = 1;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private boolean active;
    private boolean deleted;
    private int version;
    private int usedCount;
    private int historyCount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public int getTotalLimit() { return totalLimit; }
    public void setTotalLimit(int totalLimit) { this.totalLimit = totalLimit; }
    public int getPerCustomerLimit() { return perCustomerLimit; }
    public void setPerCustomerLimit(int perCustomerLimit) { this.perCustomerLimit = perCustomerLimit; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public int getHistoryCount() { return historyCount; }
    public void setHistoryCount(int historyCount) { this.historyCount = historyCount; }

    public String getStartInput() { return format(startsAt, "yyyy-MM-dd'T'HH:mm"); }
    public String getEndInput() { return format(endsAt, "yyyy-MM-dd'T'HH:mm"); }
    public String getStartLabel() { return format(startsAt, "dd/MM/yyyy HH:mm"); }
    public String getEndLabel() { return format(endsAt, "dd/MM/yyyy HH:mm"); }
    public int getRemaining() { return Math.max(0, totalLimit - usedCount); }
    public String getStatusLabel() {
        LocalDateTime now = com.mycompany.onlinefruitshopping.utils.PromotionRules.now();
        if (deleted) return "─É├ú x├│a";
        if (!active) return "─Éang tß║»t";
        if (now.isBefore(startsAt)) return "Ch╞░a ─æß║┐n giß╗¥";
        if (!now.isBefore(endsAt)) return "─É├ú hß║┐t hß║ín";
        if (getRemaining() == 0) return "Hß║┐t l╞░ß╗út";
        return "─Éang ├íp dß╗Ñng";
    }
    private String format(LocalDateTime time, String pattern) {
        return time == null ? "" : time.format(DateTimeFormatter.ofPattern(pattern));
    }
}
