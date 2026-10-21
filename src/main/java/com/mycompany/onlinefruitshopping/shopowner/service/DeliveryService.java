package com.mycompany.onlinefruitshopping.shopowner.service;

import com.mycompany.onlinefruitshopping.shopowner.dao.AssignmentDAO;
import com.mycompany.onlinefruitshopping.shopowner.dao.DeliveryDAO;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;
import com.mycompany.onlinefruitshopping.shopowner.model.Delivery;
import com.mycompany.onlinefruitshopping.shopowner.model.OrderItem;
import com.mycompany.onlinefruitshopping.shopowner.model.OrderOption;
import com.mycompany.onlinefruitshopping.shopowner.model.Staff;
import com.mycompany.onlinefruitshopping.shopowner.utils.DeliveryRules;
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

    public List<Delivery> findAll(Account user) throws SQLException {
        ServiceChecks.requireRole(user, "ShopOwner", "Shipper");
        return deliveryDAO.findAll(user);
    }

    public Delivery findById(int deliveryId, Account user) throws SQLException {
        ServiceChecks.requireRole(user, "ShopOwner", "Shipper");
        ServiceChecks.positiveId(deliveryId);
        return deliveryDAO.findById(deliveryId, user);
    }

    public List<OrderItem> findItems(int deliveryId, Account user) throws SQLException {
        ServiceChecks.requireRole(user, "ShopOwner", "Shipper");
        ServiceChecks.positiveId(deliveryId);
        return deliveryDAO.findItems(deliveryId, user);
    }

    public List<OrderOption> findAvailableOrders(Account owner) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        return assignmentDAO.findAvailableOrders(owner.getId());
    }

    public List<Staff> findActiveStaff(Account owner) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        return assignmentDAO.findActiveStaff(owner.getId());
    }

    public void assign(Account owner, int orderId, int shipperId) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        ServiceChecks.positiveId(orderId);
        ServiceChecks.positiveId(shipperId);
        // DAO kiểm tra lại shop/shipper, khóa đơn và tạo thông báo trong cùng transaction.
        assignmentDAO.assign(owner.getId(), orderId, shipperId);
    }

    public void updateStatus(int deliveryId, Account shipper, String action,
                             String reason, boolean collected) throws SQLException {
        ServiceChecks.requireRole(shipper, "Shipper");
        ServiceChecks.positiveId(deliveryId);
        if (action == null || !List.of("accept", "reject", "pickup", "start", "delivered", "failed").contains(action)) {
            throw new IllegalArgumentException("Thao tác giao hàng không hợp lệ.");
        }
        String reasonStatus = "reject".equals(action) ? "Rejected" : "failed".equals(action) ? "Failed" : "";
        String cleanReason = DeliveryRules.validateReason(reasonStatus, reason);
        // Trạng thái hiện tại và thanh toán phải đọc/kiểm tra sau khi khóa dữ liệu trong DAO.
        deliveryDAO.updateStatus(deliveryId, shipper.getId(), action, cleanReason, collected);
    }
}
