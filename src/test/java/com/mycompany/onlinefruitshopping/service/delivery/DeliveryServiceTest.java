package com.mycompany.onlinefruitshopping.service.delivery;

import com.mycompany.onlinefruitshopping.dao.delivery.AssignmentDAO;
import com.mycompany.onlinefruitshopping.dao.delivery.DeliveryDAO;
import com.mycompany.onlinefruitshopping.model.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {
    private User user(int id, String role) {
        User u = new User(); u.setUserID(id); u.setRoleName(role); u.setStatus("active"); return u;
    }
    @Test void customerAndShopOwnerCannotUpdateDeliveryAsShipper() {
        RecordingDeliveryDAO dao = new RecordingDeliveryDAO();
        DeliveryService service = new DeliveryService(dao, new RecordingAssignmentDAO());
        for (String role : List.of("Customer", "ShopOwner")) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.updateStatus(3, user(10, role), "accept", "", false));
        }
        assertEquals(0, dao.calls);
    }
    @Test void invalidActionAndMissingFailureReasonNeverReachWrite() {
        RecordingDeliveryDAO dao = new RecordingDeliveryDAO();
        DeliveryService service = new DeliveryService(dao, new RecordingAssignmentDAO());
        User shipper = user(10, "DeliveryStaff");
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, null, "", false));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, "delete", "", false));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, "failed", " ", false));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(3, shipper, "reject", null, false));
        assertEquals(0, dao.calls);
    }
    @Test void validDeliveryActionKeepsActorAndCodConfirmation() throws Exception {
        RecordingDeliveryDAO dao = new RecordingDeliveryDAO();
        DeliveryService service = new DeliveryService(dao, new RecordingAssignmentDAO());
        service.updateStatus(3, user(10, "DeliveryStaff"), "delivered", " Đã thu tiền ", true);
        assertEquals(1, dao.calls); assertEquals(10, dao.shipperId);
        assertEquals("Đã thu tiền", dao.reason); assertTrue(dao.collected);
    }
    @Test void assignmentRequiresShopOwnerAndPositiveIds() throws Exception {
        RecordingAssignmentDAO dao = new RecordingAssignmentDAO();
        DeliveryService service = new DeliveryService(new RecordingDeliveryDAO(), dao);
        assertThrows(IllegalArgumentException.class, () -> service.assign(user(10, "DeliveryStaff"), 3, 20));
        assertThrows(IllegalArgumentException.class, () -> service.assign(user(10, "ShopOwner"), 0, 20));
        assertEquals(0, dao.calls);
        service.assign(user(10, "ShopOwner"), 3, 20);
        assertEquals(1, dao.calls); assertEquals(10, dao.ownerId);
    }
    private static class RecordingDeliveryDAO extends DeliveryDAO {
        int calls, shipperId; String reason; boolean collected;
        @Override public void updateStatus(int id, int shipperId, String action, String reason, boolean collected) {
            calls++; this.shipperId = shipperId; this.reason = reason; this.collected = collected;
        }
    }
    private static class RecordingAssignmentDAO extends AssignmentDAO {
        int calls, ownerId;
        @Override public void assign(int ownerId, int orderId, int shipperId) {
            calls++; this.ownerId = ownerId;
        }
    }
}
