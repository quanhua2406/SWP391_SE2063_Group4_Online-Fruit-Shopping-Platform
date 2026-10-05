package com.mycompany.onlinefruitshopping.model;

import java.util.Date;

/**
 *
 * @author Quan
 */
public class Subscription {
    private int subscription_id;
    private int user_id;
    private int variant_id;
    private String frequency;
    private Date next_delivery_date;
    private String status;
    private Date created_at;

    public Subscription() {
    }

    public Subscription(int subscription_id, int user_id, int variant_id, String frequency, Date next_delivery_date, String status, Date created_at) {
        this.subscription_id = subscription_id;
        this.user_id = user_id;
        this.variant_id = variant_id;
        this.frequency = frequency;
        this.next_delivery_date = next_delivery_date;
        this.status = status;
        this.created_at = created_at;
    }

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
}
