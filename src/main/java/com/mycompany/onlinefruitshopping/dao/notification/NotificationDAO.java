package com.mycompany.onlinefruitshopping.dao.notification;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.model.Notification;

public class NotificationDAO {
    // Nhận Connection từ DAO giao hàng: thông báo và trạng thái cùng commit/rollback.
    public void insert(Connection c, int userId, String title, String message) throws SQLException {
        String sql = "INSERT INTO notifications(user_id, title, content, is_read, channel, created_at) VALUES (?, ?, ?, 0, 'in_app', GETDATE())";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setNString(2, title);
            ps.setNString(3, message != null && message.length() > 255 ? message.substring(0, 255) : message);
            ps.executeUpdate();
        }
    }

    public List<Notification> findByAccount(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT TOP (100) * FROM notifications WHERE user_id = ? ORDER BY notification_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("notification_id"));
                    n.setTitle(rs.getString("title"));
                    String content = rs.getString("content");
                    n.setContent(content);
                    n.setMessage(content);
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    public void markRead(int notificationId, int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1, read_at = GETDATE() WHERE notification_id = ? AND user_id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
