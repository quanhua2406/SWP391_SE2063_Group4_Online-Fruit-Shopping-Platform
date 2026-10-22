package com.mycompany.onlinefruitshopping.shopowner.service;

import com.mycompany.onlinefruitshopping.shopowner.dao.NotificationDAO;
import com.mycompany.onlinefruitshopping.shopowner.model.Notification;
import java.sql.SQLException;
import java.util.List;

/** Xem và đánh dấu đã đọc thông báo theo tài khoản đang đăng nhập. */
public class NotificationService {
    private final NotificationDAO notificationDAO;

    public NotificationService() { this(new NotificationDAO()); }

    NotificationService(NotificationDAO notificationDAO) { this.notificationDAO = notificationDAO; }

    public List<Notification> findByAccount(int accountId) throws SQLException {
        ServiceChecks.positiveId(accountId);
        return notificationDAO.findByAccount(accountId);
    }

    public void markRead(int notificationId, int accountId) throws SQLException {
        ServiceChecks.positiveId(notificationId);
        ServiceChecks.positiveId(accountId);
        notificationDAO.markRead(notificationId, accountId);
    }
}
