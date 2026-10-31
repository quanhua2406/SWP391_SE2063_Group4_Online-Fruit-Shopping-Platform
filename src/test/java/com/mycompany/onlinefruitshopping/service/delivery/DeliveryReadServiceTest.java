package com.mycompany.onlinefruitshopping.service.delivery;

import com.mycompany.onlinefruitshopping.dao.delivery.AssignmentDAO;
import com.mycompany.onlinefruitshopping.dao.delivery.DeliveryDAO;
import com.mycompany.onlinefruitshopping.model.DeliveryOrderItem;
import com.mycompany.onlinefruitshopping.model.Delivery;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.support.ServletHarness;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryReadServiceTest {
    @Test void customerCanReadAndTheActorReachesEveryDaoRead() throws Exception {
        User customer = ServletHarness.user(42, "Customer");
        int[] calls = {0};
        DeliveryDAO dao = new DeliveryDAO() {
            @Override public List<Delivery> findAll(User user) {
                assertSame(customer, user); calls[0]++; return List.of();
            }
            @Override public Delivery findById(int id, User user) {
                assertSame(customer, user); calls[0]++; return new Delivery();
            }
            @Override public List<DeliveryOrderItem> findItems(int id, User user) {
                assertSame(customer, user); calls[0]++; return List.of();
            }
        };
        DeliveryService service = new DeliveryService(dao, new AssignmentDAO());
        service.findAll(customer);
        service.findById(1, customer);
        service.findItems(1, customer);
        assertEquals(3, calls[0]);
    }

    @Test void customerCannotAssignDeliveriesOrEnumerateStaff() {
        DeliveryService service = new DeliveryService();
        User customer = ServletHarness.user(42, "Customer");
        assertThrows(IllegalArgumentException.class, () -> service.assign(customer, 1, 201));
        assertThrows(IllegalArgumentException.class, () -> service.findAvailableOrders(customer));
        assertThrows(IllegalArgumentException.class, () -> service.findActiveStaff(customer));
    }
}
