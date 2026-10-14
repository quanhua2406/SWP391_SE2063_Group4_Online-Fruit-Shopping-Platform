package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Model ánh xạ bảng subscriptions.
 * Lưu bản ghi đăng ký của khách hàng (gói combo có sẵn hoặc tự tạo).
 */
public class Subscription implements Serializable {
    private static final long serialVersionUID = 1L;

    // Trạng thái gói
    public static final String STATUS_ACTIVE    = "active";
    public static final String STATUS_PAUSED    = "paused";
    public static final String STATUS_CANCELLED = "cancelled";

    // Trường DB chính
    private int        subscription_id;
    private int        user_id;
    private int        variant_id;
    private String     frequency;            // weekly, monthly...
    private Date       next_delivery_date;
    private String     status;               // active, paused, cancelled
    private Date       created_at;

    // Trường mở rộng cho combo subscription
    private Integer    plan_id;              // nullable, null = custom plan
    private boolean    is_custom;            // 0 = combo có sẵn, 1 = custom
    private BigDecimal price_snapshot;       // giá gói lúc đăng ký
    private Date       end_date;             // ngày kết thúc kỳ gói

    // Trường phụ (JOIN để hiển thị trên UI)
    private String     planName;
    private String     planDescription;
    private String     shopName;
    private int        shopId;
    private int        durationDays;
    private String     benefits;
    private String     customerName;
    private String     customerEmail;
    private String     customerPhone;
    private String     productName;
    private String     variantName;
    private List<SubscriptionPlanItem> items; // Danh sách các loại trái cây trong combo

    public Subscription() {
    }

    public Subscription(int subscription_id, int user_id, int variant_id, String frequency,
                        Date next_delivery_date, String status, Date created_at) {
        this.subscription_id    = subscription_id;
        this.user_id            = user_id;
        this.variant_id         = variant_id;
        this.frequency          = frequency;
        this.next_delivery_date = next_delivery_date;
        this.status             = status;
        this.created_at         = created_at;
    }

    // ── Getters & Setters DB ──────────────────────────────────────────────
    public int getSubscription_id() {
        return subscription_id;
    }

    public void setSubscription_id(int subscription_id) {
        this.subscription_id = subscription_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(int variant_id) {
        this.variant_id = variant_id;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Date getNext_delivery_date() {
        return next_delivery_date;
    }

    public void setNext_delivery_date(Date next_delivery_date) {
        this.next_delivery_date = next_delivery_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Integer getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(Integer plan_id) {
        this.plan_id = plan_id;
    }

    public boolean isIs_custom() {
        return is_custom;
    }

    public void setIs_custom(boolean is_custom) {
        this.is_custom = is_custom;
    }

    public BigDecimal getPrice_snapshot() {
        return price_snapshot;
    }

    public void setPrice_snapshot(BigDecimal price_snapshot) {
        this.price_snapshot = price_snapshot;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    // ── Getters & Setters Display ─────────────────────────────────────────
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public List<SubscriptionPlanItem> getItems() {
        return items;
    }

    public void setItems(List<SubscriptionPlanItem> items) {
        this.items = items;
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ── Helper UI Methods ─────────────────────────────────────────────────
    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    public boolean isPaused() {
        return STATUS_PAUSED.equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equalsIgnoreCase(status);
    }

    public String getStatusBadgeClass() {
        if (isActive()) return "badge-active";
        if (isPaused()) return "badge-paused";
        if (isCancelled()) return "badge-cancelled";
        return "badge-default";
    }

    public String getStatusLabel() {
        if (isActive()) return "Đang hoạt động";
        if (isPaused()) return "Tạm dừng";
        if (isCancelled()) return "Đã hủy";
        return status != null ? status : "Không xác định";
    }

    public String getFrequencyLabel() {
        if ("weekly".equalsIgnoreCase(frequency)) return "Hàng tuần";
        if ("monthly".equalsIgnoreCase(frequency)) return "Hàng tháng";
        if ("quarterly".equalsIgnoreCase(frequency)) return "Hàng quý (3 tháng)";
        if ("yearly".equalsIgnoreCase(frequency)) return "Hàng năm";
        return frequency != null ? frequency : "";
    }
}
