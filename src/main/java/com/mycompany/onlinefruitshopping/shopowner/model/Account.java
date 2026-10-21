package com.mycompany.onlinefruitshopping.shopowner.model;

/** Model chứa dữ liệu; getter để JSP đọc, setter để DAO gán giá trị. */
public class Account {
    private int id;
    private String username;
    private String fullName;
    private String role;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
