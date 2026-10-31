package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.Payment;
import com.mycompany.onlinefruitshopping.model.Refund;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public PaymentDAO() {
        ensurePaymentTables();
    }

    public boolean createOrUpdatePayment(int orderId, String method, double amount, String status, String transactionRef) throws SQLException {
        ensurePaymentTables();
        String sql = """
            IF EXISTS (SELECT 1 FROM payments WHERE order_id = ?)
            BEGIN
                UPDATE payments 
                SET method = ?, amount = ?, status = ?, transaction_ref = ?,
                    paid_at = CASE WHEN ? = 'paid' THEN GETDATE() ELSE paid_at END
                WHERE order_id = ?;
            END
            ELSE
            BEGIN
                INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
                VALUES (?, ?, ?, ?, ?, CASE WHEN ? = 'paid' THEN GETDATE() ELSE NULL END);
            END
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String payStatus = (status != null && !status.isBlank()) ? status.toLowerCase() : "pending";
            String payMethod = (method != null && !method.isBlank()) ? method.toLowerCase() : "cod";

            ps.setInt(1, orderId);
            ps.setString(2, payMethod);
            ps.setDouble(3, amount);
            ps.setString(4, payStatus);
            ps.setString(5, transactionRef);
            ps.setString(6, payStatus);
            ps.setInt(7, orderId);

            ps.setInt(8, orderId);
            ps.setString(9, payMethod);
            ps.setDouble(10, amount);
            ps.setString(11, payStatus);
            ps.setString(12, transactionRef);
            ps.setString(13, payStatus);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePaymentStatus(int orderId, String status, String transactionRef) throws SQLException {
        String sql = "UPDATE payments SET status = ?, transaction_ref = ?, "
                + "paid_at = CASE WHEN ? = 'paid' THEN GETDATE() ELSE paid_at END "
                + "WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String payStatus = status.toLowerCase();
            ps.setString(1, payStatus);
            ps.setString(2, transactionRef);
            ps.setString(3, payStatus);
            ps.setInt(4, orderId);

            return ps.executeUpdate() > 0;
        }
    }

    public Payment getPaymentByOrderId(int orderId) throws SQLException {
        String sql = "SELECT p.payment_id, p.order_id, p.method, p.amount, p.status, p.transaction_ref, p.paid_at, "
                + "o.user_id, u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone, "
                + "o.shop_id, s.shop_name, o.created_at AS order_created_at "
                + "FROM payments p "
                + "INNER JOIN orders o ON p.order_id = o.order_id "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN shops s ON o.shop_id = s.shop_id "
                + "WHERE p.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPayment(rs);
                }
            }
        }
        return null;
    }

    public List<Payment> getPaymentsByUserId(int userId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.order_id, p.method, p.amount, p.status, p.transaction_ref, p.paid_at, "
                + "o.user_id, u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone, "
                + "o.shop_id, s.shop_name, o.created_at AS order_created_at "
                + "FROM payments p "
                + "INNER JOIN orders o ON p.order_id = o.order_id "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN shops s ON o.shop_id = s.shop_id "
                + "WHERE o.user_id = ? "
                + "ORDER BY p.payment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        }
        return list;
    }

    public List<Payment> getAllPayments(String statusFilter, String methodFilter, String search) throws SQLException {
        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.payment_id, p.order_id, p.method, p.amount, p.status, p.transaction_ref, p.paid_at, "
                + "o.user_id, u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone, "
                + "o.shop_id, s.shop_name, o.created_at AS order_created_at "
                + "FROM payments p "
                + "INNER JOIN orders o ON p.order_id = o.order_id "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN shops s ON o.shop_id = s.shop_id "
                + "WHERE 1=1 "
        );

        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
            sql.append("AND p.status = ? ");
        }
        if (methodFilter != null && !methodFilter.isBlank() && !methodFilter.equalsIgnoreCase("all")) {
            sql.append("AND p.method = ? ");
        }
        if (search != null && !search.isBlank()) {
            sql.append("AND (CAST(p.order_id AS VARCHAR) LIKE ? OR p.transaction_ref LIKE ? OR u.full_name LIKE ? OR u.phone LIKE ?) ");
        }

        sql.append("ORDER BY p.payment_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
                ps.setString(idx++, statusFilter.trim().toLowerCase());
            }
            if (methodFilter != null && !methodFilter.isBlank() && !methodFilter.equalsIgnoreCase("all")) {
                ps.setString(idx++, methodFilter.trim().toLowerCase());
            }
            if (search != null && !search.isBlank()) {
                String kw = "%" + search.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        }
        return list;
    }

    // =========================================================
    // REFUND MANAGEMENT (TASKS 6 & 7)
    // =========================================================

    public int createRefundRequest(int paymentId, double amount, String reason) throws SQLException {
        ensurePaymentTables();
        String sql = "INSERT INTO refunds (payment_id, amount, status, reason, requested_at) VALUES (?, ?, 'requested', ?, GETDATE())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, paymentId);
            ps.setDouble(2, amount);
            ps.setString(3, reason);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Refund> getRefundsByUserId(int userId) throws SQLException {
        List<Refund> list = new ArrayList<>();
        String sql = "SELECT r.refund_id, r.payment_id, r.processed_by, r.amount, r.status, r.reason, r.requested_at, r.processed_at, "
                + "p.order_id, p.method AS payment_method, o.user_id, u.full_name AS customer_name, u.phone AS customer_phone, "
                + "pu.full_name AS processed_by_name "
                + "FROM refunds r "
                + "INNER JOIN payments p ON r.payment_id = p.payment_id "
                + "INNER JOIN orders o ON p.order_id = o.order_id "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN users pu ON r.processed_by = pu.user_id "
                + "WHERE o.user_id = ? "
                + "ORDER BY r.refund_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRefund(rs));
                }
            }
        }
        return list;
    }

    public List<Refund> getAllRefunds(String statusFilter) throws SQLException {
        List<Refund> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.refund_id, r.payment_id, r.processed_by, r.amount, r.status, r.reason, r.requested_at, r.processed_at, "
                + "p.order_id, p.method AS payment_method, o.user_id, u.full_name AS customer_name, u.phone AS customer_phone, "
                + "pu.full_name AS processed_by_name "
                + "FROM refunds r "
                + "INNER JOIN payments p ON r.payment_id = p.payment_id "
                + "INNER JOIN orders o ON p.order_id = o.order_id "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN users pu ON r.processed_by = pu.user_id "
                + "WHERE 1=1 "
        );

        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
            sql.append("AND r.status = ? ");
        }
        sql.append("ORDER BY r.refund_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
                ps.setString(1, statusFilter.trim().toLowerCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRefund(rs));
                }
            }
        }
        return list;
    }

    public boolean updateRefundStatus(int refundId, String newStatus, int processedByUserId) throws SQLException {
        String sql = "UPDATE refunds SET status = ?, processed_by = ?, processed_at = GETDATE() WHERE refund_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.toLowerCase());
            ps.setInt(2, processedByUserId);
            ps.setInt(3, refundId);
            return ps.executeUpdate() > 0;
        }
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setMethod(rs.getString("method"));
        p.setAmount(rs.getDouble("amount"));
        p.setStatus(rs.getString("status"));
        p.setTransactionRef(rs.getString("transaction_ref"));
        p.setPaidAt(rs.getTimestamp("paid_at"));

        p.setUserId(rs.getInt("user_id"));
        p.setCustomerName(rs.getString("customer_name"));
        p.setCustomerEmail(rs.getString("customer_email"));
        p.setCustomerPhone(rs.getString("customer_phone"));
        p.setShopId(rs.getInt("shop_id"));
        p.setShopName(rs.getString("shop_name"));
        p.setOrderCreatedAt(rs.getTimestamp("order_created_at"));
        return p;
    }

    private Refund mapRefund(ResultSet rs) throws SQLException {
        Refund r = new Refund();
        r.setRefundId(rs.getInt("refund_id"));
        r.setPaymentId(rs.getInt("payment_id"));
        int procBy = rs.getInt("processed_by");
        if (!rs.wasNull()) r.setProcessedBy(procBy);
        r.setAmount(rs.getDouble("amount"));
        r.setStatus(rs.getString("status"));
        r.setReason(rs.getString("reason"));
        r.setRequestedAt(rs.getTimestamp("requested_at"));
        r.setProcessedAt(rs.getTimestamp("processed_at"));

        r.setOrderId(rs.getInt("order_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setCustomerPhone(rs.getString("customer_phone"));
        r.setPaymentMethod(rs.getString("payment_method"));
        r.setProcessedByName(rs.getString("processed_by_name"));
        return r;
    }

    private void ensurePaymentTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            String createPayments = """
                IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'payments')
                BEGIN
                    CREATE TABLE payments (
                        payment_id       INT IDENTITY(1,1) PRIMARY KEY,
                        order_id         INT NOT NULL UNIQUE,
                        method           NVARCHAR(20) NOT NULL,
                        amount           DECIMAL(12,2) NOT NULL,
                        status           NVARCHAR(20) NOT NULL DEFAULT 'pending',
                        transaction_ref  NVARCHAR(100),
                        paid_at          DATETIME2 NULL,
                        FOREIGN KEY (order_id) REFERENCES orders(order_id)
                    );
                END
                """;
            st.execute(createPayments);

            String createRefunds = """
                IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'refunds')
                BEGIN
                    CREATE TABLE refunds (
                        refund_id      INT IDENTITY(1,1) PRIMARY KEY,
                        payment_id     INT NOT NULL,
                        processed_by   INT NULL,
                        amount         DECIMAL(12,2) NOT NULL,
                        status         NVARCHAR(20) NOT NULL DEFAULT 'requested',
                        reason         NVARCHAR(255),
                        requested_at   DATETIME2 NOT NULL DEFAULT GETDATE(),
                        processed_at   DATETIME2 NULL,
                        FOREIGN KEY (payment_id) REFERENCES payments(payment_id),
                        FOREIGN KEY (processed_by) REFERENCES users(user_id)
                    );
                END
                """;
            st.execute(createRefunds);

        } catch (SQLException e) {
            System.err.println("Warning: ensurePaymentTables: " + e.getMessage());
        }
    }
}
