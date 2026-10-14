package com.mycompany.onlinefruitshopping.model;

import java.sql.Timestamp;

public class OrderStatusHistory {

    private int historyId;
    private int orderId;
    private String status;
    private int changedBy;
    private String changedByName;
    private Timestamp changedAt;

    public OrderStatusHistory() {
    }

    public OrderStatusHistory(int historyId, int orderId, String status, int changedBy, Timestamp changedAt) {
        this.historyId = historyId;
        this.orderId = orderId;
        this.status = status;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }
}
