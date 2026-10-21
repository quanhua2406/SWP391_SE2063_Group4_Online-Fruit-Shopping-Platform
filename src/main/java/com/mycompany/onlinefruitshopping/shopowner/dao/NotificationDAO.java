package com.mycompany.onlinefruitshopping.shopowner.dao;

import com.mycompany.onlinefruitshopping.shopowner.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.shopowner.model.Notification;

public class NotificationDAO {
    // Nhận Connection từ DAO giao hàng: thông báo và trạng thái cùng commit/rollback.
    public void insert(Connection c, int accountId, String title, String message) throws SQLException {
        String sql = "INSERT INTO Notifications(account_id, title, message, is_read) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setNString(2, title);
            ps.setNString(3, message.length() > 255 ? message.substring(0, 255) : message);
            ps.executeUpdate();
        }
    }

    public List<Notification> findByAccount(int accountId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT TOP (100) * FROM Notifications WHERE account_id = ? ORDER BY notification_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("notification_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    public void markRead(int notificationId, int accountId) throws SQLException {
        String sql = "UPDATE Notifications SET is_read = 1 WHERE notification_id = ? AND account_id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }
}
