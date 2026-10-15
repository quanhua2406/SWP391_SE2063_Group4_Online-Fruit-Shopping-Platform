
package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShopOwnerRequestDAO {

    // =========================================================
    // CUSTOMER INFORMATION
    // =========================================================

    public static class CustomerInfo {

        private final int userId;
        private final String fullName;
        private final String email;
        private final String phone;

        public CustomerInfo(
                int userId,
                String fullName,
                String email,
                String phone) {

            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
        }

        public int getUserId() {
            return userId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }

    // =========================================================
    // CUSTOMER REQUEST INFORMATION
    // =========================================================

    public static class RequestInfo {

        private final int requestId;
        private final String shopName;
        private final String description;
        private final String contactPhone;
        private final String address;
        private final String status;
        private final String adminNote;
        private final String submittedAt;
        private final String reviewedAt;

        public RequestInfo(
                int requestId,
                String shopName,
                String description,
                String contactPhone,
                String address,
                String status,
                String adminNote,
                String submittedAt,
                String reviewedAt) {

            this.requestId = requestId;
            this.shopName = shopName;
            this.description = description;
            this.contactPhone = contactPhone;
            this.address = address;
            this.status = status;
            this.adminNote = adminNote;
            this.submittedAt = submittedAt;
            this.reviewedAt = reviewedAt;
        }

        public int getRequestId() {
            return requestId;
        }

        public String getShopName() {
            return shopName;
        }

        public String getDescription() {
            return description;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public String getAddress() {
            return address;
        }

        public String getStatus() {
            return status;
        }

        public String getAdminNote() {
            return adminNote;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }

        public String getReviewedAt() {
            return reviewedAt;
        }
    }

    // =========================================================
    // ADMIN REQUEST INFORMATION
    // =========================================================

    public static class AdminRequestInfo {

        private final int requestId;
        private final int userId;
        private final String fullName;
        private final String email;
        private final String shopName;
        private final String description;
        private final String contactPhone;
        private final String address;
        private final String status;
        private final String adminNote;
        private final String submittedAt;

        public AdminRequestInfo(
                int requestId,
                int userId,
                String fullName,
                String email,
                String shopName,
                String description,
                String contactPhone,
                String address,
                String status,
                String adminNote,
                String submittedAt) {

            this.requestId = requestId;
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.shopName = shopName;
            this.description = description;
            this.contactPhone = contactPhone;
            this.address = address;
            this.status = status;
            this.adminNote = adminNote;
            this.submittedAt = submittedAt;
        }

        public int getRequestId() {
            return requestId;
        }

        public int getUserId() {
            return userId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getShopName() {
            return shopName;
        }

        public String getDescription() {
            return description;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public String getAddress() {
            return address;
        }

        public String getStatus() {
            return status;
        }

        public String getAdminNote() {
            return adminNote;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }
    }

    // =========================================================
    // FIND ACTIVE CUSTOMER BY EMAIL
    // =========================================================

    public CustomerInfo getActiveCustomerByEmail(String email)
            throws SQLException {

        String sql =
                "SELECT TOP 1 " +
                "u.user_id, u.full_name, u.email, u.phone " +
                "FROM dbo.users u " +
                "INNER JOIN dbo.user_roles ur " +
                "ON u.user_id = ur.user_id " +
                "INNER JOIN dbo.roles r " +
                "ON ur.role_id = r.role_id " +
                "WHERE LOWER(u.email) = LOWER(?) " +
                "AND UPPER(u.status) = 'ACTIVE' " +
                "AND UPPER(r.role_name) = 'CUSTOMER'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerInfo(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone")
                    );
                }
            }
        }

        return null;
    }

    // =========================================================
    // GET LATEST REQUEST OF A CUSTOMER
    // =========================================================

    public RequestInfo getLatestRequest(int userId)
            throws SQLException {

        String sql =
                "SELECT TOP 1 " +
                "request_id, proposed_shop_name, " +
                "business_description, contact_phone, " +
                "shop_address, status, admin_note, " +
                "submitted_at, reviewed_at " +
                "FROM dbo.shop_owner_requests " +
                "WHERE user_id = ? " +
                "ORDER BY submitted_at DESC, request_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new RequestInfo(
                            rs.getInt("request_id"),
                            rs.getString("proposed_shop_name"),
                            rs.getString("business_description"),
                            rs.getString("contact_phone"),
                            rs.getString("shop_address"),
                            rs.getString("status"),
                            rs.getString("admin_note"),
                            rs.getString("submitted_at"),
                            rs.getString("reviewed_at")
                    );
                }
            }
        }

        return null;
    }

    // =========================================================
    // CHECK WHETHER CUSTOMER HAS AN OPEN REQUEST
    // =========================================================

    public boolean hasOpenRequest(int userId)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                "FROM dbo.shop_owner_requests " +
                "WHERE user_id = ? " +
                "AND status IN ('PENDING', 'APPROVED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // =========================================================
    // SUBMIT SHOP OWNER REQUEST
    // =========================================================

    public void submitRequest(
            int userId,
            String shopName,
            String description,
            String contactPhone,
            String address) throws SQLException {

        String checkSql =
                "SELECT COUNT(*) " +
                "FROM dbo.shop_owner_requests WITH (UPDLOCK, HOLDLOCK) " +
                "WHERE user_id = ? " +
                "AND status IN ('PENDING', 'APPROVED')";

        String insertSql =
                "INSERT INTO dbo.shop_owner_requests " +
                "(user_id, proposed_shop_name, business_description, " +
                "contact_phone, shop_address, status, submitted_at) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING', SYSDATETIME())";

        try (Connection conn = DBConnection.getConnection()) {

            boolean oldAutoCommit = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                try (PreparedStatement checkPs =
                             conn.prepareStatement(checkSql)) {

                    checkPs.setInt(1, userId);

                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException(
                                    "An active shop owner request already exists."
                            );
                        }
                    }
                }

                try (PreparedStatement insertPs =
                             conn.prepareStatement(insertSql)) {

                    insertPs.setInt(1, userId);
                    insertPs.setString(2, shopName);
                    insertPs.setString(3, description);
                    insertPs.setString(4, contactPhone);
                    insertPs.setString(5, address);

                    insertPs.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }

    // =========================================================
    // GET ALL REQUESTS FOR ADMIN
    // =========================================================

    public List<AdminRequestInfo> getAllRequests()
            throws SQLException {

        List<AdminRequestInfo> requests = new ArrayList<>();

        String sql =
                "SELECT r.request_id, r.user_id, " +
                "u.full_name, u.email, " +
                "r.proposed_shop_name, r.business_description, " +
                "r.contact_phone, r.shop_address, r.status, " +
                "r.admin_note, r.submitted_at " +
                "FROM dbo.shop_owner_requests r " +
                "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                "ORDER BY CASE WHEN r.status = 'PENDING' " +
                "THEN 0 ELSE 1 END, r.submitted_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                requests.add(new AdminRequestInfo(
                        rs.getInt("request_id"),
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("proposed_shop_name"),
                        rs.getString("business_description"),
                        rs.getString("contact_phone"),
                        rs.getString("shop_address"),
                        rs.getString("status"),
                        rs.getString("admin_note"),
                        rs.getString("submitted_at")
                ));
            }
        }

        return requests;
    }

    // =========================================================
    // ADMIN REVIEWS A REQUEST
    // =========================================================

    public boolean reviewRequest(
            int requestId,
            String newStatus,
            String adminNote) throws SQLException {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Request status is required."
            );
        }

        String status = newStatus.trim().toUpperCase();

        if (!"APPROVED".equals(status)
                && !"REJECTED".equals(status)) {

            throw new IllegalArgumentException(
                    "Request status must be APPROVED or REJECTED."
            );
        }

        String sql =
                "UPDATE dbo.shop_owner_requests " +
                "SET status = ?, admin_note = ?, " +
                "reviewed_at = SYSDATETIME() " +
                "WHERE request_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, adminNote);
            ps.setInt(3, requestId);

            return ps.executeUpdate() == 1;
        }
    }
}
