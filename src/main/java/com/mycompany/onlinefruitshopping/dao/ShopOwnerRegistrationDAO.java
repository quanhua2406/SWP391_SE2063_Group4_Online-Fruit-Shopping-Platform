
package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ShopOwnerRegistrationDAO {

    // =========================================================
    // 1. THONG TIN KHACH HANG
    // =========================================================
    public static class CustomerInfo {

        private int userId;
        private String fullName;
        private String email;
        private String phone;

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
    // 2. THONG TIN YEU CAU DANG KY SHOP OWNER
    // =========================================================
    public static class RequestInfo {

        private int requestId;
        private String shopName;
        private String status;
        private String adminNote;

        public RequestInfo(
                int requestId,
                String shopName,
                String status,
                String adminNote) {
            this.requestId = requestId;
            this.shopName = shopName;
            this.status = status;
            this.adminNote = adminNote;
        }

        public int getRequestId() {
            return requestId;
        }

        public String getShopName() {
            return shopName;
        }

        public String getStatus() {
            return status;
        }

        public String getAdminNote() {
            return adminNote;
        }
    }

    // =========================================================
    // 3. LAY THONG TIN CUSTOMER THEO EMAIL
    // =========================================================
    public CustomerInfo getCustomerByEmail(String email)
            throws SQLException {

        String sql = """
            SELECT TOP 1
                u.user_id,
                u.full_name,
                u.email,
                u.phone
            FROM dbo.users u
            INNER JOIN dbo.user_roles ur
                ON u.user_id = ur.user_id
            INNER JOIN dbo.roles r
                ON ur.role_id = r.role_id
            WHERE u.email = ?
              AND r.role_name = 'Customer'
              AND u.status = 'ACTIVE'
            """;

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
    // 4. LAY YEU CAU DANG KY MOI NHAT
    // =========================================================
    public RequestInfo getLatestRequest(int userId)
            throws SQLException {

        String sql = """
            SELECT TOP 1
                request_id,
                proposed_shop_name,
                status,
                admin_note
            FROM dbo.shop_owner_requests
            WHERE user_id = ?
            ORDER BY submitted_at DESC, request_id DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new RequestInfo(
                            rs.getInt("request_id"),
                            rs.getString("proposed_shop_name"),
                            rs.getString("status"),
                            rs.getString("admin_note")
                    );
                }
            }
        }

        return null;
    }

    // =========================================================
    // 5. KIEM TRA CUSTOMER CO YEU CAU DANG CHO DUYET KHONG
    // =========================================================
    public boolean hasOpenRequest(int userId)
            throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM dbo.shop_owner_requests
            WHERE user_id = ?
              AND status = 'PENDING'
            """;

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
    // 6. GUI YEU CAU DANG KY SHOP OWNER
    // =========================================================
    public boolean submitRequest(
            int userId,
            String shopName,
            String description,
            String shopPhone,
            String shopEmail,
            String shopAddress,
            String city,
            String businessType) throws SQLException {

        if (shopName == null || shopName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Shop name is required."
            );
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Business description is required."
            );
        }

        if (shopPhone == null || shopPhone.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Shop phone is required."
            );
        }

        if (shopAddress == null || shopAddress.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Shop address is required."
            );
        }

        String checkSql = """
            SELECT COUNT(*)
            FROM dbo.shop_owner_requests
            WHERE user_id = ?
              AND status = 'PENDING'
            """;

        String insertSql = """
            INSERT INTO dbo.shop_owner_requests
                (
                    user_id,
                    proposed_shop_name,
                    business_description,
                    contact_phone,
                    shop_address,
                    status,
                    submitted_at
                )
            VALUES (?, ?, ?, ?, ?, 'PENDING', SYSDATETIME())
            """;

        try (Connection conn = DBConnection.getConnection()) {

            boolean oldAutoCommit = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                // Kiem tra yeu cau dang cho duyet
                try (PreparedStatement checkPs =
                             conn.prepareStatement(checkSql)) {

                    checkPs.setInt(1, userId);

                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // Them yeu cau moi voi status PENDING
                try (PreparedStatement insertPs =
                             conn.prepareStatement(
                                     insertSql,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    insertPs.setInt(1, userId);
                    insertPs.setString(2, shopName.trim());
                    insertPs.setString(3, description.trim());
                    insertPs.setString(4, shopPhone.trim());
                    insertPs.setString(5, shopAddress.trim());

                    int affectedRows = insertPs.executeUpdate();

                    if (affectedRows == 0) {
                        conn.rollback();
                        return false;
                    }

                    conn.commit();
                    return true;
                }

            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;

            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }
}
