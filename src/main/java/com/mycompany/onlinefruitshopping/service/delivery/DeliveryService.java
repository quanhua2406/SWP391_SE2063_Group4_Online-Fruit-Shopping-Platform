package com.mycompany.onlinefruitshopping.service.delivery;

import com.mycompany.onlinefruitshopping.service.common.ServiceChecks;

import com.mycompany.onlinefruitshopping.dao.delivery.AssignmentDAO;
import com.mycompany.onlinefruitshopping.dao.delivery.DeliveryDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.model.Delivery;
import com.mycompany.onlinefruitshopping.model.DeliveryOrderItem;
import com.mycompany.onlinefruitshopping.model.OrderOption;
import com.mycompany.onlinefruitshopping.model.Staff;
import com.mycompany.onlinefruitshopping.utils.DeliveryRules;
import java.sql.SQLException;
import java.util.List;

/** Điều phối xem, phân công và cập nhật giao hàng. Không phụ thuộc Servlet/JSP. */
public class DeliveryService {
    private final DeliveryDAO deliveryDAO;
    private final AssignmentDAO assignmentDAO;

    public DeliveryService() {
        this(new DeliveryDAO(), new AssignmentDAO());
    }

    DeliveryService(DeliveryDAO deliveryDAO, AssignmentDAO assignmentDAO) {
        this.deliveryDAO = deliveryDAO;
        this.assignmentDAO = assignmentDAO;
    }

    public List<Delivery> findAll(User user) throws SQLException {
        ServiceChecks.requireRole(user, "SHOPOWNER", "DELIVERYSTAFF", "CUSTOMER");
        return deliveryDAO.findAll(user);
    }

    public Delivery findById(int deliveryId, User user) throws SQLException {
        ServiceChecks.requireRole(user, "SHOPOWNER", "DELIVERYSTAFF", "CUSTOMER");
        ServiceChecks.positiveId(deliveryId);
        return deliveryDAO.findById(deliveryId, user);
    }

    public List<DeliveryOrderItem> findItems(int deliveryId, User user) throws SQLException {
        ServiceChecks.requireRole(user, "SHOPOWNER", "DELIVERYSTAFF", "CUSTOMER");
        ServiceChecks.positiveId(deliveryId);
        return deliveryDAO.findItems(deliveryId, user);
    }

    public List<OrderOption> findAvailableOrders(User owner) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        return assignmentDAO.findAvailableOrders(owner.getUserID());
    }

    public List<Staff> findActiveStaff(User owner) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        return assignmentDAO.findActiveStaff(owner.getUserID());
    }

    public void assign(User owner, int orderId, int shipperId) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        ServiceChecks.positiveId(orderId);
        ServiceChecks.positiveId(shipperId);
        // DAO kiểm tra lại shop/shipper, khóa đơn và tạo thông báo trong cùng transaction.
        assignmentDAO.assign(owner.getUserID(), orderId, shipperId);
    }

    public void updateStatus(int deliveryId, User shipper, String action,
                             String reason, boolean collected) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        ServiceChecks.positiveId(deliveryId);
        if (action == null || !List.of("accept", "reject", "pickup", "start", "delivered", "failed").contains(action)) {
            throw new IllegalArgumentException("Thao tác giao hàng không hợp lệ.");
        }
        String reasonStatus = "reject".equals(action) ? "Rejected" : "failed".equals(action) ? "Failed" : "";
        String cleanReason = DeliveryRules.validateReason(reasonStatus, reason);
        // Trạng thái hiện tại và thanh toán phải đọc/kiểm tra sau khi khóa dữ liệu trong DAO.
        deliveryDAO.updateStatus(deliveryId, shipper.getUserID(), action, cleanReason, collected);
    }
}
