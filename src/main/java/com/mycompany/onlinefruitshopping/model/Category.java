package com.mycompany.onlinefruitshopping.model;

public class Category {

    private int categoryId;
    private Integer parentId;
    private String categoryName;

    public Category() {
    }

    public Category(int categoryId, Integer parentId, String categoryName) {
        this.categoryId = categoryId;
        this.parentId = parentId;
        this.categoryName = categoryName;
    }

    public Category(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
