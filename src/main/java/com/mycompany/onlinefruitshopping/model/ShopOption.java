package com.mycompany.onlinefruitshopping.model;

/** Một lựa chọn cửa hàng trong form của chủ shop. */
public class ShopOption {
    private int id;
    private String name;
    private String pickupAddress;
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
}
