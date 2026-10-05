package com.mycompany.onlinefruitshopping.model;

public class User {

    private int userID;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String roleName;
    private int roleID;
    private String status;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public User() {
    }

    public User(
            int userID,
            String fullName,
            String email,
            String passwordHash,
            String phone,
            int roleID,
            String roleName,
            String status) {

        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.roleID = roleID;
        this.roleName = roleName;
        this.status = status;
    }


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}