package com.mycompany.onlinefruitshopping.model;

/** Model chứa dữ liệu; getter để JSP đọc, setter để DAO gán giá trị. */
public class Staff {
    private int id;
    private int shopId;
    private String fullName;
    private String status;
    private String shopName;
    private boolean available;
    private boolean assignmentEnabled;
    private boolean busy;
    private int currentOrderId;
    private int openDeliveryCount;
    private boolean leaveRequested;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public boolean isAvailable() { return available; }
    public boolean getAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public boolean isAssignmentEnabled() { return assignmentEnabled; }
    public boolean getAssignmentEnabled() { return assignmentEnabled; }
    public void setAssignmentEnabled(boolean assignmentEnabled) { this.assignmentEnabled = assignmentEnabled; }

    public boolean isBusy() { return busy; }
    public boolean getBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }

    public int getOpenDeliveryCount() { return openDeliveryCount; }
    public void setOpenDeliveryCount(int openDeliveryCount) { this.openDeliveryCount = openDeliveryCount; }

    public int getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(int currentOrderId) { this.currentOrderId = currentOrderId; }

    public boolean isLeaveRequested() { return leaveRequested; }
    public boolean getLeaveRequested() { return leaveRequested; }
    public void setLeaveRequested(boolean leaveRequested) { this.leaveRequested = leaveRequested; }
}
