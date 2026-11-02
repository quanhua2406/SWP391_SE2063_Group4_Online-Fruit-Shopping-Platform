package com.mycompany.onlinefruitshopping.model;

import java.io.Serializable;
import java.util.Date;
import java.sql.Timestamp;

public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private int     review_id;
    private int     user_id;
    private int     product_id;
    private int     order_id;
    private Integer rating;
    private String  title;
    private String  comment;
    private Date      created_at;
    private String    status;        // published | reported | rejected | deleted
    private Timestamp deletedAt;     // soft-delete timestamp

    // Các trường phụ – lấy qua JOIN hoặc tính toán từ Service, dùng hiển thị trên View
    private String  userName;
    private String  imageUrls;
    private String  productName;
    private String  productImage;
    private boolean editable;
    private long    remainingDays;
    private int     reportCount;     // Số lượng báo cáo của review
    private String  evidenceUrls;    // Ảnh bằng chứng báo cáo vi phạm

    public Review() {
    }

    public Review(int review_id, int user_id, int product_id, int order_id,
                  Integer rating, String comment, Date created_at) {
        this.review_id   = review_id;
        this.user_id     = user_id;
        this.product_id  = product_id;
        this.order_id    = order_id;
        this.rating      = rating;
        this.comment     = comment;
        this.created_at  = created_at;
    }

    public Review(int review_id, int user_id, int product_id, int order_id,
                  Integer rating, String title, String comment, Date created_at) {
        this.review_id   = review_id;
        this.user_id     = user_id;
        this.product_id  = product_id;
        this.order_id    = order_id;
        this.rating      = rating;
        this.title       = title;
        this.comment     = comment;
        this.created_at  = created_at;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public long getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(long remainingDays) {
        this.remainingDays = remainingDays;
    }

    public int getReview_id()       { return review_id; }
    public void setReview_id(int review_id) { this.review_id = review_id; }

    public int getUser_id()         { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public int getProduct_id()      { return product_id; }
    public void setProduct_id(int product_id) { this.product_id = product_id; }

    public int getOrder_id()        { return order_id; }
    public void setOrder_id(int order_id) { this.order_id = order_id; }

    public Integer getRating()      { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getTitle()        { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment()      { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    // Hỗ trợ cả 2 tên gọi: getCreated_at và getCreate_at để tương thích ngược 100%
    public Date getCreated_at()     { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public Date getCreate_at()      { return created_at; }
    public void setCreate_at(Date create_at) { this.created_at = create_at; }

    public String getUserName()     { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getImageUrls()    { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }

    public String getProductName()  { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public String getStatus()         { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getDeletedAt()   { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public String getEvidenceUrls() { return evidenceUrls; }
    public void setEvidenceUrls(String evidenceUrls) { this.evidenceUrls = evidenceUrls; }

    /** Tiện ích: kiểm tra review có đang bị ẩn không */
    public boolean isHidden() {
        return "rejected".equals(status) || "deleted".equals(status);
    }
}
