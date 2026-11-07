package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.dao.notification.NotificationDAO;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import com.mycompany.onlinefruitshopping.utils.ConnectionProvider;
import com.mycompany.onlinefruitshopping.utils.ValidationUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.model.Delivery;
import com.mycompany.onlinefruitshopping.model.DeliveryOrderItem;
import com.mycompany.onlinefruitshopping.utils.DeliveryRules;

public class DeliveryDAO {
    private final ConnectionProvider connections;

    public DeliveryDAO() {
        this(DBConnection::getConnection);
    }

    DeliveryDAO(ConnectionProvider connections) {
        this.connections = java.util.Objects.requireNonNull(connections);
    }

    private static final String SELECT_DELIVERY = """
        SELECT d.delivery_id,da.attempt_id,d.order_id,o.shop_id,da.shipper_id,da.delivery_status,
            da.pickup_time,da.delivered_time,da.note AS delivery_note,s.shop_name,da.pickup_address,
            a.full_name AS shipper_name,da.receiver_name,da.receiver_phone,da.shipping_address,
            o.total_amount,o.status AS order_status,
            UPPER(pay.method) AS payment_method,
            CASE WHEN pay.status=N'paid' THEN N'Paid' ELSE N'Unpaid' END AS payment_status,
            o.note AS order_note
        FROM delivery_attempts da
        JOIN deliveries d ON d.delivery_id=da.delivery_id
        JOIN orders o ON o.order_id=d.order_id
        JOIN shops s ON s.shop_id=o.shop_id
        JOIN users a ON a.user_id=da.shipper_id
        LEFT JOIN payments pay ON pay.order_id=o.order_id
        LEFT JOIN shop_delivery_staff t ON t.shop_id=o.shop_id AND t.shipper_id=da.shipper_id
        WHERE s.status=N'active'
        """;

    // Chỉ nối các điều kiện SQL cố định; dữ liệu người dùng luôn truyền qua dấu ?.
    private String scope(User user) {
        if (user == null || user.getUserID() <= 0) {
            throw new IllegalArgumentException("Người dùng không hợp lệ.");
        }
        return switch (ValidationUtil.normalizeRoleName(user.getRoleName())) {
            case "SHOPOWNER" -> " AND s.owner_id = ?";
            case "DELIVERYSTAFF" -> " AND da.shipper_id = ?";
            case "CUSTOMER" -> " AND o.user_id = ?";
            default -> throw new IllegalArgumentException("Bạn không có quyền xem giao hàng.");
        };
    }

    public List<Delivery> findAll(User user) throws SQLException {
        List<Delivery> list = new ArrayList<>();
        String sql = SELECT_DELIVERY + scope(user) + " ORDER BY da.attempt_id DESC";
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getUserID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Delivery findById(int id, User user) throws SQLException {
        String sql = SELECT_DELIVERY + scope(user) + " AND da.attempt_id = ?";
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getUserID());
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<DeliveryOrderItem> findItems(int deliveryId, User user) throws SQLException {
        // Kiểm tra quyền cả khi đọc sản phẩm, không chỉ ở trang danh sách.
        Delivery delivery = findById(deliveryId, user);
        if (delivery == null) throw new IllegalArgumentException("Không tìm thấy giao hàng thuộc quyền của bạn.");
        List<DeliveryOrderItem> list = new ArrayList<>();
        String sql = """
            SELECT p.product_name,v.sku AS variant_name,od.unit_price AS price,od.quantity
            FROM delivery_attempts da
            JOIN deliveries d ON d.delivery_id=da.delivery_id
            JOIN orders o ON o.order_id=d.order_id
            JOIN shops s ON s.shop_id=o.shop_id
            LEFT JOIN shop_delivery_staff t ON t.shop_id=o.shop_id AND t.shipper_id=da.shipper_id
            JOIN order_items od ON od.order_id=o.order_id
            JOIN product_variants v ON v.variant_id=od.variant_id
            JOIN products p ON p.product_id=v.product_id
            WHERE s.status=N'active' AND da.attempt_id=?
            """ + scope(user) + " ORDER BY od.order_item_id";
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, deliveryId);
            ps.setInt(2, user.getUserID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DeliveryOrderItem item = new DeliveryOrderItem();
                    item.setProductName(rs.getString("product_name"));
                    item.setVariantName(rs.getString("variant_name"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setQuantity(rs.getBigDecimal("quantity"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /** The ID passed from the module UI is attempt_id, never delivery_id. */
    public void updateStatus(int attemptId,int shipperId,String action,String reason,boolean collected) throws SQLException {
        try(Connection c=connections.open()) {
            c.setAutoCommit(false);
            try {
                int deliveryId,orderId,shopId,customerId,ownerId; String orderStatus,baseStatus;
                try(PreparedStatement ps=c.prepareStatement("""
                    SELECT d.delivery_id, o.order_id, o.shop_id, o.user_id, o.status, s.owner_id, d.status
                    FROM deliveries d WITH(UPDLOCK,HOLDLOCK)
                    JOIN orders o WITH(UPDLOCK,HOLDLOCK) ON o.order_id = d.order_id
                    JOIN shops s ON s.shop_id = o.shop_id
                    WHERE (d.delivery_id = ? OR d.order_id = ?) AND d.staff_id = ?
                    """)) {
                    ps.setInt(1,attemptId);ps.setInt(2,attemptId);ps.setInt(3,shipperId);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new IllegalArgumentException("Không tìm thấy đơn giao hàng được phân công cho bạn.");
                        deliveryId=rs.getInt(1);orderId=rs.getInt(2);shopId=rs.getInt(3);
                        customerId=rs.getInt(4);orderStatus=rs.getString(5);ownerId=rs.getInt(6);baseStatus=rs.getString(7);
                    }
                }
                String oldStatus = switch(baseStatus != null ? baseStatus.toLowerCase() : "assigned") {
                    case "assigned" -> "Assigned";
                    case "accepted" -> "Accepted";
                    case "rejected" -> "Rejected";
                    case "picked_up" -> "PickedUp";
                    case "delivering" -> "Delivering";
                    case "delivered" -> "Delivered";
                    case "failed" -> "Failed";
                    default -> "Assigned";
                };
                String next=DeliveryRules.nextStatus(oldStatus,action);
                String clean=DeliveryRules.validateReason(next,reason);
                String note=clean.isEmpty()?DeliveryRules.label(next):clean;
                if("Delivered".equals(next)) {
                    String method="cod",paymentStatus="pending";
                    try(PreparedStatement ps=c.prepareStatement("""
                        SELECT p.method,p.status FROM payments p WITH(UPDLOCK,HOLDLOCK) WHERE p.order_id=?
                        """)) {
                        ps.setInt(1,orderId);
                        try(ResultSet rs=ps.executeQuery()) {
                            if(rs.next()) {
                                method=rs.getString(1);paymentStatus=rs.getString(2);
                            }
                        }
                    }
                    if("cod".equalsIgnoreCase(method) && "pending".equalsIgnoreCase(paymentStatus)) {
                        try(PreparedStatement ps=c.prepareStatement("UPDATE payments SET status=N'paid',paid_at=GETDATE() WHERE order_id=?")) {
                            ps.setInt(1,orderId);ps.executeUpdate();
                        }
                    }
                }
                try(PreparedStatement ps=c.prepareStatement("""
                    UPDATE deliveries SET status=?, note=?
                    WHERE delivery_id=? AND staff_id=?
                    """)) {
                    ps.setNString(1,baseStatus(next));ps.setNString(2,note);ps.setInt(3,deliveryId);ps.setInt(4,shipperId);
                    ps.executeUpdate();
                }
                String targetOrderStatus = switch(next) {
                    case "Delivered" -> "delivered";
                    case "Failed" -> "failed";
                    case "Rejected" -> "preparing";
                    default -> "shipping";
                };
                try(PreparedStatement ps=c.prepareStatement("UPDATE orders SET status=? WHERE order_id=?")) {
                    ps.setNString(1,targetOrderStatus);ps.setInt(2,orderId);
                    ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO order_status_history(order_id,status,changed_by,changed_at) VALUES(?,?,?,GETDATE())")) {
                    ps.setInt(1,orderId);ps.setNString(2,targetOrderStatus);ps.setInt(3,shipperId);ps.executeUpdate();
                }
                NotificationDAO notifications=new NotificationDAO();
                String message="Đơn #"+orderId+": "+note;
                notifications.insert(c,ownerId,"Cập nhật giao hàng #"+orderId,message,"new_order","order",String.valueOf(orderId));
                if("Delivered".equals(next)) {
                    notifications.insert(c,customerId,"Giao hàng thành công #"+orderId,"Đơn hàng #"+orderId+" đã được giao thành công. Hãy để lại đánh giá nhé!","order_delivered","order",String.valueOf(orderId));
                    notifications.insert(c,shipperId,"Đã giao đơn hàng #"+orderId,message,"delivery_status_update","delivery",String.valueOf(orderId));
                } else if("Failed".equals(next)) {
                    notifications.insert(c,customerId,"Giao hàng không thành công #"+orderId,message,"delivery_status","order",String.valueOf(orderId));
                    notifications.insert(c,shipperId,"Đã ghi nhận kết quả giao hàng #"+orderId,message,"delivery_status_update","delivery",String.valueOf(orderId));
                } else {
                    notifications.insert(c,customerId,"Cập nhật giao hàng #"+orderId,message,"delivery_status","order",String.valueOf(orderId));
                }
                c.commit();
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
        }
    }

    private static String baseStatus(String state) {
        return switch(state) {
            case "Assigned" -> "assigned";
            case "Accepted" -> "accepted";
            case "Rejected" -> "rejected";
            case "PickedUp" -> "picked_up";
            case "Delivering" -> "delivering";
            case "Delivered" -> "delivered";
            case "Failed" -> "failed";
            default -> throw new IllegalArgumentException("Trạng thái giao hàng không hợp lệ.");
        };
    }

    private Delivery map(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setId(rs.getInt("delivery_id"));
        d.setAttemptId(rs.getInt("attempt_id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setShopId(rs.getInt("shop_id"));
        d.setStatus(rs.getString("delivery_status"));
        d.setShopName(rs.getString("shop_name"));
        d.setPickupAddress(rs.getString("pickup_address"));
        d.setShipperId(rs.getInt("shipper_id"));
        d.setShipperName(rs.getString("shipper_name"));
        d.setReceiverName(rs.getString("receiver_name"));
        d.setReceiverPhone(rs.getString("receiver_phone"));
        d.setShippingAddress(rs.getString("shipping_address"));
        d.setTotalAmount(rs.getBigDecimal("total_amount"));
        d.setOrderStatus(rs.getString("order_status"));
        d.setPaymentMethod(rs.getString("payment_method"));
        d.setPaymentStatus(rs.getString("payment_status"));
        d.setOrderNote(rs.getString("order_note"));
        d.setNote(rs.getString("delivery_note"));
        d.setPickupTime(rs.getTimestamp("pickup_time"));
        d.setDeliveredTime(rs.getTimestamp("delivered_time"));
        return d;
    }
}
