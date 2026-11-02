package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Model anh xa bang subscription_plans.
 * Moi plan = 1 combo goi dang ky do Shop Owner tao.
 */
public class SubscriptionPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    // Constants status
    public static final String STATUS_DRAFT    = "draft";
    public static final String STATUS_ACTIVE   = "active";
    public static final String STATUS_INACTIVE = "inactive";

    // Constants billing cycle
    public static final String CYCLE_WEEKLY    = "weekly";
    public static final String CYCLE_MONTHLY   = "monthly";
    public static final String CYCLE_QUARTERLY = "quarterly";
    public static final String CYCLE_YEARLY    = "yearly";

    // Database fields
    private int        planId;
    private int        shopId;
    private String     name;
    private String     description;
    private BigDecimal price;
    private String     billingCycle;
    private int        durationDays;
    private String     benefits;         // JSON string
    private String     status;
    private int        createdBy;
    private Timestamp  createdAt;
    private Timestamp  updatedAt;
    private Timestamp  deletedAt;

    // Join fields
    private String shopName;
    private String createdByName;
    private int    subscriberCount;      // so subscriber dang active
    private List<SubscriptionPlanItem> items; // danh sach san pham trong combo

    // Constructors
    public SubscriptionPlan() {}

    // Getters & Setters
    public int getPlanId()                        { return planId; }
    public void setPlanId(int planId)             { this.planId = planId; }

    public int getShopId()                        { return shopId; }
    public void setShopId(int shopId)             { this.shopId = shopId; }

    public String getName()                       { return name; }
    public void setName(String name)              { this.name = name; }

    public String getDescription()                { return description; }
    public void setDescription(String desc)       { this.description = desc; }

    public BigDecimal getPrice()                  { return price; }
    public void setPrice(BigDecimal price)        { this.price = price; }

    public String getBillingCycle()               { return billingCycle; }
    public void setBillingCycle(String cycle)     { this.billingCycle = cycle; }

    public int getDurationDays()                  { return durationDays; }
    public void setDurationDays(int days)         { this.durationDays = days; }

    public String getBenefits()                   { return benefits; }
    public void setBenefits(String benefits)      { this.benefits = benefits; }

    public String getStatus()                     { return status; }
    public void setStatus(String status)          { this.status = status; }

    public int getCreatedBy()                     { return createdBy; }
    public void setCreatedBy(int createdBy)       { this.createdBy = createdBy; }

    public Timestamp getCreatedAt()               { return createdAt; }
    public void setCreatedAt(Timestamp t)         { this.createdAt = t; }

    public Timestamp getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(Timestamp t)         { this.updatedAt = t; }

    public Timestamp getDeletedAt()               { return deletedAt; }
    public void setDeletedAt(Timestamp t)         { this.deletedAt = t; }

    public String getShopName()                   { return shopName; }
    public void setShopName(String shopName)      { this.shopName = shopName; }

    public String getCreatedByName()              { return createdByName; }
    public void setCreatedByName(String name)     { this.createdByName = name; }

    public int getSubscriberCount()               { return subscriberCount; }
    public void setSubscriberCount(int count)     { this.subscriberCount = count; }

    public List<SubscriptionPlanItem> getItems()  { return items; }
    public void setItems(List<SubscriptionPlanItem> items) { this.items = items; }
    public int getItemCount()                     { return items != null ? items.size() : 0; }

    // Helpers
    public boolean isActive()   { return STATUS_ACTIVE.equals(status); }
    public boolean isDraft()    { return STATUS_DRAFT.equals(status); }
    public boolean isDeleted()  { return deletedAt != null; }
    public boolean hasActiveSubscribers() { return subscriberCount > 0; }

    /** Nhan tieng Viet cho billing cycle */
    public String getBillingCycleLabel() {
        if (billingCycle == null) return "";
        switch (billingCycle) {
            case CYCLE_WEEKLY:    return "H\u00e0ng tu\u1ea7n";
            case CYCLE_MONTHLY:   return "H\u00e0ng th\u00e1ng";
            case CYCLE_QUARTERLY: return "H\u00e0ng qu\u00fd (3 th\u00e1ng)";
            case CYCLE_YEARLY:    return "H\u00e0ng n\u0103m";
            default: return billingCycle;
        }
    }

    /** Nhan tieng Viet cho status */
    public String getStatusLabel() {
        if (status == null) return "";
        switch (status) {
            case STATUS_DRAFT:    return "B\u1ea3n nh\u00e1p";
            case STATUS_ACTIVE:   return "\u0110ang ho\u1ea1t \u0111\u1ed9ng";
            case STATUS_INACTIVE: return "Ng\u1eebng ho\u1ea1t \u0111\u1ed9ng";
            default: return status;
        }
    }

    /** Validate billing cycle hop le */
    public static boolean isValidCycle(String cycle) {
        return CYCLE_WEEKLY.equals(cycle) || CYCLE_MONTHLY.equals(cycle)
            || CYCLE_QUARTERLY.equals(cycle) || CYCLE_YEARLY.equals(cycle);
    }
}
