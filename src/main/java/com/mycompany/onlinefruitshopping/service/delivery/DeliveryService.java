package com.mycompany.onlinefruitshopping.service.delivery;

import com.mycompany.onlinefruitshopping.service.common.ServiceChecks;

import com.mycompany.onlinefruitshopping.dao.delivery.AssignmentDAO;
import com.mycompany.onlinefruitshopping.dao.delivery.DeliveryDAO;
import com.mycompany.onlinefruitshopping.dao.delivery.ShipperTeamDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.model.Delivery;
import com.mycompany.onlinefruitshopping.model.DeliveryOrderItem;
import com.mycompany.onlinefruitshopping.model.OrderOption;
import com.mycompany.onlinefruitshopping.model.Staff;
import com.mycompany.onlinefruitshopping.model.ShopDeliveryRequest;
import com.mycompany.onlinefruitshopping.model.ShopOption;
import com.mycompany.onlinefruitshopping.utils.DeliveryRules;
import java.sql.SQLException;
import java.util.List;

/** Điều phối xem, phân công và cập nhật giao hàng. Không phụ thuộc Servlet/JSP. */
public class DeliveryService {
    private final DeliveryDAO deliveryDAO;
    private final AssignmentDAO assignmentDAO;
    private final ShipperTeamDAO shipperTeamDAO = new ShipperTeamDAO();

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

    public void assign(User owner,int orderId,int shipperId,String pickupAddress) throws SQLException {
        ServiceChecks.requireRole(owner,"ShopOwner");
        ServiceChecks.positiveId(orderId); ServiceChecks.positiveId(shipperId);
        assignmentDAO.assign(owner.getUserID(),orderId,shipperId,pickupAddress);
    }




    // Shipper team membership
    public List<ShopOption> findActiveShops(User shipper) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        return shipperTeamDAO.findActiveShops();
    }

    public Staff getMyMembership(User shipper) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        return shipperTeamDAO.getMyMembership(shipper.getUserID());
    }

    public List<ShopDeliveryRequest> getMyRequests(User shipper) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        return shipperTeamDAO.getMyRequests(shipper.getUserID());
    }

    public void applyToShop(User shipper, int shopId) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        ServiceChecks.positiveId(shopId);
        shipperTeamDAO.applyToShop(shipper.getUserID(), shopId);
    }

    public void withdrawRequest(User shipper, int requestId) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        ServiceChecks.positiveId(requestId);
        shipperTeamDAO.withdrawRequest(shipper.getUserID(), requestId);
    }

    public void setAvailability(User shipper, boolean available) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        shipperTeamDAO.setAvailability(shipper.getUserID(), available);
    }

    public void requestLeave(User shipper) throws SQLException {
        ServiceChecks.requireRole(shipper, "DeliveryStaff");
        shipperTeamDAO.requestLeave(shipper.getUserID());
    }

    // Shop owner manages public pickup address; only members of the ShopOwner role.
    public ShopOption getOwnerShop(User owner) throws SQLException {
        ServiceChecks.requireRole(owner,"ShopOwner");
        return shipperTeamDAO.findOwnerShop(owner.getUserID());
    }

    public void updateShopPickupAddress(User owner,int shopId,String address) throws SQLException {
        ServiceChecks.requireRole(owner,"ShopOwner");
        ServiceChecks.positiveId(shopId);
        shipperTeamDAO.updatePickupAddress(owner.getUserID(),shopId,address);
    }

    // Shop owner team management
    public List<ShopDeliveryRequest> getTeamRequests(User owner) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        return shipperTeamDAO.getRequests(owner.getUserID());
    }

    public List<Staff> getTeamMembers(User owner) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        return shipperTeamDAO.getTeamMembers(owner.getUserID());
    }

    public void reviewTeamRequest(User owner,int requestId,boolean approved,String reason) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        ServiceChecks.positiveId(requestId);
        shipperTeamDAO.reviewRequest(owner.getUserID(),requestId,approved,reason);
    }

    public void setTeamAssignmentEnabled(User owner,int shipperId,boolean enabled) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        ServiceChecks.positiveId(shipperId);
        shipperTeamDAO.setAssignmentEnabled(owner.getUserID(),shipperId,enabled);
    }

    public void terminateTeamMember(User owner,int shipperId) throws SQLException {
        ServiceChecks.requireRole(owner, "ShopOwner");
        ServiceChecks.positiveId(shipperId);
        shipperTeamDAO.terminateMember(owner.getUserID(),shipperId);
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
