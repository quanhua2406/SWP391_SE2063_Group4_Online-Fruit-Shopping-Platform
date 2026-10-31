package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.Notification;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object cho bảng notifications trong SQL Server.
 */
public class NotificationDAO {

    private static final Logger LOGGER = Logger.getLogger(NotificationDAO.class.getName());

    // =========================================================================
    // INSERT NOTIFICATION
    // =========================================================================
    public int insert(Notification n) {
        try (Connection conn = DBConnection.getConnection()) {
            return insert(conn, n);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy kết nối để insert notification", e);
            return -1;
        }
    }

    public int insert(Connection conn, Notification n) throws SQLException {
        String sql = """
                INSERT INTO notifications 
                (user_id, title, content, type, reference_type, reference_id, channel, is_read, is_important, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getContent());
            ps.setString(4, n.getType());
            ps.setString(5, n.getReferenceType());
            ps.setString(6, n.getReferenceId());
            ps.setString(7, n.getChannel() != null ? n.getChannel() : "in_app");
            ps.setBoolean(8, n.isRead());
            ps.setBoolean(9, n.isImportant());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        n.setNotificationId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    // =========================================================================
    // GET NOTIFICATIONS WITH FILTER & PAGINATION
    // =========================================================================
    public List<Notification> getNotifications(int userId, String groupFilter, Boolean isRead, Boolean isImportant, int page, int pageSize) {
        List<Notification> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT notification_id, user_id, title, content, type, 
                       reference_type, reference_id, channel, is_read, 
                       is_important, created_at, read_at
                FROM notifications
                WHERE user_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(userId);

        applyFilters(sql, params, groupFilter, isRead, isImportant);

        // Sort by newest first
        sql.append(" ORDER BY created_at DESC ");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");

        int offset = Math.max(0, (page - 1) * pageSize);
        params.add(offset);
        params.add(pageSize);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi query getNotifications cho userId: " + userId, e);
        }

        return list;
    }

    // =========================================================================
    // COUNT TOTAL NOTIFICATIONS WITH FILTER
    // =========================================================================
    public int countNotifications(int userId, String groupFilter, Boolean isRead, Boolean isImportant) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM notifications WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        applyFilters(sql, params, groupFilter, isRead, isImportant);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi countNotifications cho userId: " + userId, e);
        }
        return 0;
    }

    // =========================================================================
    // COUNT UNREAD NOTIFICATIONS (QUẢ CHUÔNG HEADER)
    // =========================================================================
    public int countUnreadByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi countUnreadByUserId: " + userId, e);
        }
        return 0;
    }

    public int countImportantUnreadByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0 AND is_important = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi countImportantUnreadByUserId: " + userId, e);
        }
        return 0;
    }

    // =========================================================================
    // COUNT UNREAD PER GROUP (CHO SIDEBAR BADGES)
    // =========================================================================
    public Map<String, Integer> countUnreadPerGroup(int userId) {
        Map<String, Integer> counts = new HashMap<>();
        String sql = """
                SELECT type, COUNT(*) as cnt
                FROM notifications
                WHERE user_id = ? AND is_read = 0
                GROUP BY type
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    int cnt = rs.getInt("cnt");

                    // Lưu cả theo type cụ thể lẫn theo group để mọi giao diện (Customer, ShopOwner, Admin, Delivery) đọc được
                    counts.put(type, counts.getOrDefault(type, 0) + cnt);

                    String group = switch (type) {
                        // Customer groups
                        case "order_confirmation" -> "order";
                        case "delivery_status" -> "delivery";
                        case "payment_success", "payment_failed" -> "payment";
                        case "promotion" -> "promotion";
                        case "subscription" -> "subscription";
                        // Shop Owner groups
                        case "new_order" -> "new_order";
                        case "payment_received" -> "payment";
                        case "low_stock" -> "low_stock";
                        // Admin & Shared
                        case "reported_review" -> "reported_review";
                        case "critical_low_stock" -> "critical_low_stock";
                        case "refund_request" -> "refund";
                        case "order_cancelled" -> "cancelled";
                        case "new_shop_registration" -> "new_shop";
                        case "system_alert" -> "system_alert";
                        // Delivery Staff
                        case "new_delivery_assignment", "assignment" -> "new_delivery_assignment";
                        case "delivery_status_update", "status" -> "delivery_status_update";
                        default -> type;
                    };
                    counts.put(group, counts.getOrDefault(group, 0) + cnt);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi countUnreadPerGroup cho userId: " + userId, e);
        }
        return counts;
    }

    // =========================================================================
    // GET NOTIFICATION BY ID
    // =========================================================================
    public Notification getById(int notificationId, int userId) {
        String sql = """
                SELECT notification_id, user_id, title, content, type, 
                       reference_type, reference_id, channel, is_read, 
                       is_important, created_at, read_at
                FROM notifications
                WHERE notification_id = ? AND user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNotification(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi getById notification: " + notificationId, e);
        }
        return null;
    }

    // =========================================================================
    // MARK ONE AS READ
    // =========================================================================
    public boolean markAsRead(int notificationId, int userId) {
        String sql = """
                UPDATE notifications
                SET is_read = 1, read_at = GETDATE()
                WHERE notification_id = ? AND user_id = ? AND is_read = 0
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi markAsRead: " + notificationId, e);
        }
        return false;
    }

    // =========================================================================
    // MARK ALL AS READ
    // =========================================================================
    public int markAllAsRead(int userId) {
        String sql = """
                UPDATE notifications
                SET is_read = 1, read_at = GETDATE()
                WHERE user_id = ? AND is_read = 0
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi markAllAsRead cho userId: " + userId, e);
        }
        return 0;
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    private void applyFilters(StringBuilder sql, List<Object> params, String groupFilter, Boolean isRead, Boolean isImportant) {
        if (groupFilter != null && !groupFilter.isBlank() && !groupFilter.equalsIgnoreCase("all")) {
            switch (groupFilter.toLowerCase()) {
                // Customer filters
                case "order" -> sql.append(" AND type = 'order_confirmation' ");
                case "delivery" -> sql.append(" AND (type = 'delivery_status' OR type = 'delivery_status_update') ");
                case "payment" -> sql.append(" AND (type = 'payment_success' OR type = 'payment_failed' OR type = 'payment_received') ");
                case "promotion" -> sql.append(" AND type = 'promotion' ");
                case "subscription" -> sql.append(" AND type = 'subscription' ");
                // Shop Owner filters
                case "new_order" -> sql.append(" AND type = 'new_order' ");
                case "low_stock" -> sql.append(" AND (type = 'low_stock' OR type = 'critical_low_stock') ");
                // Admin & Shared filters
                case "reported_review" -> sql.append(" AND type = 'reported_review' ");
                case "critical_low_stock" -> sql.append(" AND type = 'critical_low_stock' ");
                case "refund", "refund_request" -> sql.append(" AND type = 'refund_request' ");
                case "cancelled", "order_cancelled" -> sql.append(" AND type = 'order_cancelled' ");
                case "new_shop", "new_shop_registration" -> sql.append(" AND type = 'new_shop_registration' ");
                case "system_alert" -> sql.append(" AND type = 'system_alert' ");
                // Delivery Staff filters
                case "assignment", "new_delivery_assignment" -> sql.append(" AND type = 'new_delivery_assignment' ");
                case "status", "delivery_status_update" -> sql.append(" AND type = 'delivery_status_update' ");
                default -> {
                    sql.append(" AND type = ? ");
                    params.add(groupFilter);
                }
            }
        }

        if (isRead != null) {
            sql.append(" AND is_read = ? ");
            params.add(isRead ? 1 : 0);
        }

        if (isImportant != null) {
            sql.append(" AND is_important = ? ");
            params.add(isImportant ? 1 : 0);
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        n.setType(rs.getString("type"));
        n.setReferenceType(rs.getString("reference_type"));
        n.setReferenceId(rs.getString("reference_id"));
        n.setChannel(rs.getString("channel"));
        n.setRead(rs.getBoolean("is_read"));
        n.setImportant(rs.getBoolean("is_important"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        n.setReadAt(rs.getTimestamp("read_at"));
        return n;
    }
}
