package com.mycompany.onlinefruitshopping.service.notification;

import com.mycompany.onlinefruitshopping.dao.notification.NotificationDAO;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    @Test void markingNotificationKeepsAccountScopeAndRejectsBadId() throws SQLException {
        RecordingNotificationDAO dao = new RecordingNotificationDAO();
        NotificationService service = new NotificationService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.markRead(0, 10));
        assertEquals(0, dao.calls);
        service.markRead(3, 10);
        assertEquals(3, dao.notificationId);
        assertEquals(10, dao.accountId);
    }

    private static class RecordingNotificationDAO extends NotificationDAO {
        int calls, notificationId, accountId;
        @Override public void markRead(int notificationId, int accountId) {
            calls++;
            this.notificationId = notificationId;
            this.accountId = accountId;
        }
    }
}
