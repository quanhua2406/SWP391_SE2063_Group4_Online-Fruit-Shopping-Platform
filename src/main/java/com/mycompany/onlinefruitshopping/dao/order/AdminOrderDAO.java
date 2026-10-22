package com.mycompany.onlinefruitshopping.dao.order;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin-side order and delivery queries (feature 19.0).
 *
 * <p>Unlike {@code OrderDAO}, which always scopes its queries to a
 * single shop, every method here looks at <b>all</b> shops so the
 * administrator can monitor the whole platform. Rows are returned
 * as maps keyed by column label; pagination is applied in the
 * servlet with the shared {@code Page} helper.</p>
 */
public class AdminOrderDAO {

    /**
     * Columns shared by every order listing. The scalar subqueries
     * attach the current delivery assignment (staff name + status)
     * and a line-item count so each table needs a single round
     * trip.
     */
    private static final String ORDER_SELECT =
            "SELECT o.order_id, o.status, o.total_amount, o.created_at, "
            + "o.user_id AS customer_id, cu.full_name AS customer_name, "
            + "cu.email AS customer_email, cu.phone AS customer_phone, "
            + "o.shop_id, s.shop_name, "
            + "a.recipient_name, a.phone AS recipient_phone, "
            + "a.address_line, a.ward, a.district, a.province, "
            + "(SELECT COUNT(*) FROM order_items oi WHERE oi.order_id = o.order_id) AS item_count, "
            + "(SELECT TOP 1 d.delivery_id FROM deliveries d WHERE d.order_id = o.order_id) AS delivery_id, "
            + "(SELECT TOP 1 d.staff_id FROM deliveries d WHERE d.order_id = o.order_id) AS assigned_staff_id, "
            + "(SELECT TOP 1 su.full_name FROM deliveries d "
            + " INNER JOIN users su ON su.user_id = d.staff_id "
            + " WHERE d.order_id = o.order_id) AS assigned_staff_name, "
            + "(SELECT TOP 1 d.status FROM deliveries d WHERE d.order_id = o.order_id) AS delivery_status "
            + "FROM orders o "
            + "INNER JOIN users cu ON cu.user_id = o.user_id "
            + "LEFT JOIN shops s ON s.shop_id = o.shop_id "
            + "LEFT JOIN user_addresses a ON a.address_id = o.address_id";

    /**
     * Order statuses an administrator may still act on (not yet
     * finished). Used by the delivery reassignment screen.
     */
    private static final String OPEN_STATUS_LIST =
            "('pending_confirmation', 'preparing', 'shipping')";

    // =====================================================
    // Orders
    // =====================================================

    /**
     * Every order on the platform, newest first, optionally
     * filtered by status and free-text search (customer name,
     * email, phone, shop name or order id).
     */
    public List<Map<String, Object>> findOrders(String status, String search) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(ORDER_SELECT).append(" WHERE 1 = 1");

        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.status = ?");
        }

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (cu.full_name LIKE ? OR cu.email LIKE ?")
                .append(" OR cu.phone LIKE ? OR ISNULL(s.shop_name, '') LIKE ?")
                .append(" OR CAST(o.order_id AS NVARCHAR(12)) LIKE ?)");
        }

        sql.append(" ORDER BY o.order_id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int i = 0;

            if (status != null && !status.isEmpty()) {
                ps.setString(++i, status);
            }

            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                for (int k = 0; k < 5; k++) {
                    ps.setString(++i, like);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(row(rs));
                }
            }
        }

        return rows;
    }

    /**
     * One full order row (all shops). Returns {@code null} when the
     * order does not exist.
     */
    public Map<String, Object> getOrderDetail(int orderId) throws SQLException {

        String sql = ORDER_SELECT + " WHERE o.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? row(rs) : null;
            }
        }
    }

    /**
     * Number of orders per status across the whole platform.
     */
    public Map<String, Integer> countOrdersByStatus() throws SQLException {

        Map<String, Integer> counts = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT status, COUNT(*) AS cnt FROM orders GROUP BY status");
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("cnt"));
            }
        }

        return counts;
    }

    // =====================================================
    // Deliveries
    // =====================================================

    /**
     * All delivery rows joined with their order, shipper, customer
     * and address. Optionally filtered by delivery status and by a
     * free-text search over delivery id, order id, shipper and
     * customer.
     */
    public List<Map<String, Object>> findDeliveries(String status, String search) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT d.delivery_id, d.status, d.note, d.proof_image_url, d.created_at, "
                + "d.order_id, d.staff_id, "
                + "o.status AS order_status, o.total_amount, o.created_at AS order_created_at, "
                + "su.full_name AS staff_name, su.email AS staff_email, su.phone AS staff_phone, "
                + "cu.full_name AS customer_name, cu.phone AS customer_phone, "
                + "s.shop_name, "
                + "a.recipient_name, a.address_line, a.ward, a.district, a.province "
                + "FROM deliveries d "
                + "INNER JOIN orders o ON o.order_id = d.order_id "
                + "INNER JOIN users su ON su.user_id = d.staff_id "
                + "INNER JOIN users cu ON cu.user_id = o.user_id "
                + "LEFT JOIN shops s ON s.shop_id = o.shop_id "
                + "LEFT JOIN user_addresses a ON a.address_id = o.address_id "
                + "WHERE 1 = 1");

        if (status != null && !status.isEmpty()) {
            sql.append(" AND d.status = ?");
        }

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (CAST(d.delivery_id AS NVARCHAR(12)) LIKE ?")
                .append(" OR CAST(d.order_id AS NVARCHAR(12)) LIKE ?")
                .append(" OR su.full_name LIKE ? OR cu.full_name LIKE ?")
                .append(" OR ISNULL(s.shop_name, '') LIKE ?)");
        }

        sql.append(" ORDER BY d.delivery_id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int i = 0;

            if (status != null && !status.isEmpty()) {
                ps.setString(++i, status);
            }

            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                for (int k = 0; k < 5; k++) {
                    ps.setString(++i, like);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(row(rs));
                }
            }
        }

        return rows;
    }

    /**
     * Number of deliveries per delivery status.
     */
    public Map<String, Integer> countDeliveriesByStatus() throws SQLException {

        Map<String, Integer> counts = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT status, COUNT(*) AS cnt FROM deliveries GROUP BY status");
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("cnt"));
            }
        }

        return counts;
    }

    /**
     * The (most recent) delivery assigned to the given order, or
     * {@code null} when the order has no assignment yet.
     */
    public Map<String, Object> getDeliveryByOrder(int orderId) throws SQLException {

        String sql =
                "SELECT TOP 1 d.delivery_id, d.status, d.staff_id, d.note, d.created_at, "
                + "su.full_name AS staff_name, su.email AS staff_email, su.phone AS staff_phone "
                + "FROM deliveries d "
                + "INNER JOIN users su ON su.user_id = d.staff_id "
                + "WHERE d.order_id = ? "
                + "ORDER BY d.delivery_id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? row(rs) : null;
            }
        }
    }

    // =====================================================
    // Delivery staff
    // =====================================================

    /**
     * Every account holding the DeliveryStaff role together with
     * how many deliveries are active / total for that shipper.
     */
    public List<Map<String, Object>> findDeliveryStaff() throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        String sql =
                "SELECT u.user_id, u.full_name, u.email, u.phone, u.status, u.created_at, "
                + "(SELECT COUNT(*) FROM deliveries d WHERE d.staff_id = u.user_id) AS total_deliveries, "
                + "(SELECT COUNT(*) FROM deliveries d WHERE d.staff_id = u.user_id "
                + " AND d.status IN ('assigned', 'picked_up', 'delivering')) AS active_deliveries "
                + "FROM users u "
                + "WHERE EXISTS (SELECT 1 FROM user_roles ur "
                + "  INNER JOIN roles r ON r.role_id = ur.role_id "
                + "  WHERE ur.user_id = u.user_id AND r.role_name = 'DeliveryStaff') "
                + "ORDER BY u.full_name";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(row(rs));
            }
        }

        return rows;
    }

    /**
     * Whether the given account currently holds the DeliveryStaff
     * role. Used to guard the block / reassignment actions.
     */
    public boolean isDeliveryStaff(int userId) throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM user_roles ur "
                + "INNER JOIN roles r ON r.role_id = ur.role_id "
                + "WHERE ur.user_id = ? AND r.role_name = 'DeliveryStaff'";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // =====================================================
    // Reassignment (admin intervention)
    // =====================================================

    /**
     * Orders that can still receive a delivery assignment (not yet
     * delivered or cancelled), newest first. The scalar subqueries
     * from {@code ORDER_SELECT} already expose the current staff.
     */
    public List<Map<String, Object>> findAssignableOrders(String search) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(ORDER_SELECT)
                .append(" WHERE o.status IN ").append(OPEN_STATUS_LIST);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (cu.full_name LIKE ? OR cu.email LIKE ?")
                .append(" OR cu.phone LIKE ? OR ISNULL(s.shop_name, '') LIKE ?")
                .append(" OR CAST(o.order_id AS NVARCHAR(12)) LIKE ?)");
        }

        sql.append(" ORDER BY o.order_id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int i = 0;

            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                for (int k = 0; k < 5; k++) {
                    ps.setString(++i, like);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(row(rs));
                }
            }
        }

        return rows;
    }

    /**
     * Create the delivery assignment for an order that has none
     * yet. The status check and the "no assignment yet" check live
     * inside the statement, so a terminal order or a double
     * assignment simply inserts nothing and the method returns
     * {@code false}.
     */
    public boolean assignDelivery(int orderId, int staffId, String note) throws SQLException {

        String sql =
                "INSERT INTO deliveries (order_id, staff_id, status, note, created_at) "
                + "SELECT ?, ?, 'assigned', ?, GETDATE() "
                + "WHERE EXISTS (SELECT 1 FROM orders WHERE order_id = ? AND status IN " + OPEN_STATUS_LIST + ") "
                + "AND NOT EXISTS (SELECT 1 FROM deliveries WHERE order_id = ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, staffId);
            ps.setString(3, note);
            ps.setInt(4, orderId);
            ps.setInt(5, orderId);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Point an existing assignment at a different shipper. A failed
     * delivery is put back into the queue ({@code assigned}); an
     * in-progress status is kept. Delivered assignments cannot be
     * changed.
     */
    public boolean reassignDelivery(int deliveryId, int staffId, String note) throws SQLException {

        String sql =
                "UPDATE deliveries SET staff_id = ?, "
                + "status = CASE WHEN status = 'failed' THEN 'assigned' ELSE status END, "
                + "note = ? "
                + "WHERE delivery_id = ? AND status <> 'delivered'";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            ps.setString(2, note);
            ps.setInt(3, deliveryId);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Remove an assignment that must not go ahead (admin
     * intervention). Tracking points are removed first to satisfy
     * the foreign key; delivered assignments are left untouched.
     */
    public boolean unassignDelivery(int deliveryId) throws SQLException {

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement locPs = conn.prepareStatement(
                    "DELETE FROM delivery_locations WHERE delivery_id = ?")) {
                locPs.setInt(1, deliveryId);
                locPs.executeUpdate();
            }

            int removed;

            try (PreparedStatement delPs = conn.prepareStatement(
                    "DELETE FROM deliveries WHERE delivery_id = ? AND status <> 'delivered'")) {
                delPs.setInt(1, deliveryId);
                removed = delPs.executeUpdate();
            }

            conn.commit();
            return removed == 1;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                conn.close();
            }
        }
    }

    // =====================================================
    // Order issues
    // =====================================================

    /**
     * Orders that need admin attention: failed or cancelled
     * orders, plus orders that have been stuck in an open status
     * for more than three days (delayed).
     */
    public List<Map<String, Object>> findIssueOrders(String search) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT t.*, "
                + "DATEDIFF(DAY, t.created_at, GETDATE()) AS age_days, "
                + "CASE WHEN t.status IN ('failed', 'cancelled') "
                + " THEN 'Failed or cancelled order' "
                + " ELSE 'Delayed: over 3 days in current stage' END AS issue_type "
                + "FROM (").append(ORDER_SELECT)
                .append(" WHERE (o.status IN ('failed', 'cancelled') ")
                .append(" OR (o.status IN ").append(OPEN_STATUS_LIST)
                .append(" AND o.created_at < DATEADD(DAY, -3, GETDATE())))");

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (cu.full_name LIKE ? OR cu.email LIKE ?")
                .append(" OR cu.phone LIKE ? OR ISNULL(s.shop_name, '') LIKE ?")
                .append(" OR CAST(o.order_id AS NVARCHAR(12)) LIKE ?)");
        }

        sql.append(") t ORDER BY t.order_id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int i = 0;

            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                for (int k = 0; k < 5; k++) {
                    ps.setString(++i, like);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(row(rs));
                }
            }
        }

        return rows;
    }

    // =====================================================
    // Payments (order detail screen)
    // =====================================================

    /**
     * Payment records belonging to an order, oldest first.
     */
    public List<Map<String, Object>> getPayments(int orderId) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        String sql =
                "SELECT payment_id, method, amount, status, transaction_ref, paid_at "
                + "FROM payments WHERE order_id = ? ORDER BY payment_id";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(row(rs));
                }
            }
        }

        return rows;
    }

    // =====================================================
    // Helpers
    // =====================================================

    /**
     * Copy the current result row into a map keyed by column
     * label, so new queries do not need new model classes.
     */
    private Map<String, Object> row(ResultSet rs) throws SQLException {

        Map<String, Object> map = new LinkedHashMap<>();
        ResultSetMetaData md = rs.getMetaData();

        for (int i = 1; i <= md.getColumnCount(); i++) {
            map.put(md.getColumnLabel(i), rs.getObject(i));
        }

        return map;
    }
}
