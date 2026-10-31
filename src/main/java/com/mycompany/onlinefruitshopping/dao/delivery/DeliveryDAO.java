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
        SELECT d.delivery_id, d.order_id, o.shop_id, d.staff_id AS shipper_id, d.status AS delivery_status,
               d.created_at AS pickup_time, d.created_at AS delivered_time, d.note AS delivery_note,
               s.shop_name, COALESCE(s.description, s.shop_name) AS pickup_address, a.full_name AS shipper_name,
               COALESCE(ua.recipient_name, u.full_name) AS receiver_name,
               COALESCE(ua.phone, u.phone) AS receiver_phone,
               CONCAT_WS(', ', ua.address_line, ua.ward, ua.district, ua.province) AS shipping_address,
               o.total_amount, o.status AS order_status,
               COALESCE(pay.method, 'COD') AS payment_method,
               COALESCE(pay.status, 'Pending') AS payment_status,
               d.note AS order_note
        FROM deliveries d
        JOIN orders o ON o.order_id = d.order_id
        LEFT JOIN shops s ON s.shop_id = o.shop_id
        LEFT JOIN users a ON a.user_id = d.staff_id
        LEFT JOIN users u ON u.user_id = o.user_id
        LEFT JOIN user_addresses ua ON ua.address_id = o.address_id
        LEFT JOIN payments pay ON pay.order_id = o.order_id
        WHERE 1=1
        """;

    private String scope(User user) {
        if (user == null || user.getUserID() <= 0) {
            throw new IllegalArgumentException("Người dùng không hợp lệ.");
        }
        return switch (ValidationUtil.normalizeRoleName(user.getRoleName())) {
            case "SHOPOWNER" -> " AND s.owner_id = ?";
            case "DELIVERYSTAFF" -> " AND d.staff_id = ?";
            case "CUSTOMER" -> " AND o.user_id = ?";
            default -> throw new IllegalArgumentException("Bạn không có quyền xem giao hàng.");
        };
    }

    public List<Delivery> findAll(User user) throws SQLException {
        List<Delivery> list = new ArrayList<>();
        String sql = SELECT_DELIVERY + scope(user) + " ORDER BY d.delivery_id DESC";
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getUserID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Delivery findById(int id, User user) throws SQLException {
        // 1. Thử tìm theo delivery_id
        String sql = SELECT_DELIVERY + scope(user) + " AND d.delivery_id = ?";
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getUserID());
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        // 2. Fallback tìm theo order_id nếu mã id trong thông báo là order_id
        String sqlOrder = SELECT_DELIVERY + scope(user) + " AND d.order_id = ?";
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sqlOrder)) {
            ps.setInt(1, user.getUserID());
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<DeliveryOrderItem> findItems(int deliveryId, User user) throws SQLException {
        Delivery delivery = findById(deliveryId, user);
        if (delivery == null) throw new IllegalArgumentException("Không tìm thấy giao hàng thuộc quyền của bạn.");
        List<DeliveryOrderItem> list = new ArrayList<>();
        String sql = """
            SELECT p.product_name, v.sku AS variant_name, oi.unit_price AS price, oi.quantity
            FROM deliveries d
            JOIN orders o ON o.order_id = d.order_id
            JOIN order_items oi ON oi.order_id = o.order_id
            JOIN product_variants v ON v.variant_id = oi.variant_id
            JOIN products p ON p.product_id = v.product_id
            WHERE d.delivery_id = ?
            ORDER BY oi.order_item_id
            """;
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delivery.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DeliveryOrderItem item = new DeliveryOrderItem();
                    item.setProductName(rs.getString("product_name"));
                    item.setVariantName(rs.getString("variant_name"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setQuantity(rs.getBigDecimal("quantity") != null ? rs.getBigDecimal("quantity").intValue() : 1);
                    list.add(item);
                }
            }
        }
        return list;
    }

    public void updateStatus(int deliveryId, int shipperId, String action,
                             String reason, boolean collected) throws SQLException {
        try (Connection c = connections.open()) {
            c.setAutoCommit(false);
            try {
                String orderSql = """
                    SELECT o.order_id, o.status AS order_status, o.user_id,
                           COALESCE(pay.method, 'COD') AS payment_method,
                           COALESCE(pay.status, 'Pending') AS payment_status,
                           s.owner_id
                    FROM orders o WITH (UPDLOCK, HOLDLOCK)
                    LEFT JOIN shops s ON s.shop_id = o.shop_id
                    LEFT JOIN payments pay ON pay.order_id = o.order_id
                    WHERE o.order_id = (
                        SELECT order_id FROM deliveries WHERE delivery_id = ? AND staff_id = ?
                    )
                    """;
                int orderId;
                int customerId;
                int ownerId;
                String orderStatus;
                String paymentMethod;
                String paymentStatus;
                try (PreparedStatement ps = c.prepareStatement(orderSql)) {
                    ps.setInt(1, deliveryId);
                    ps.setInt(2, shipperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy đơn được phân công cho bạn.");
                        orderId = rs.getInt("order_id");
                        customerId = rs.getInt("user_id");
                        ownerId = rs.getInt("owner_id");
                        orderStatus = rs.getString("order_status");
                        paymentMethod = rs.getString("payment_method");
                        paymentStatus = rs.getString("payment_status");
                    }
                }

                String oldStatus;
                String deliverySql = """
                    SELECT d.status AS delivery_status, d.note
                    FROM deliveries d WITH (UPDLOCK, HOLDLOCK)
                    WHERE d.delivery_id = ? AND d.staff_id = ?
                    """;
                try (PreparedStatement ps = c.prepareStatement(deliverySql)) {
                    ps.setInt(1, deliveryId);
                    ps.setInt(2, shipperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy giao hàng thuộc quyền shipper của bạn.");
                        oldStatus = rs.getString("delivery_status");
                    }
                }

                String nextStatus = DeliveryRules.nextStatus(oldStatus, action);
                String cleanReason = DeliveryRules.validateReason(nextStatus, reason);

                String updateDelivery = """
                    UPDATE deliveries SET status = ?, note = ?
                    WHERE delivery_id = ? AND staff_id = ?
                    """;
                try (PreparedStatement ps = c.prepareStatement(updateDelivery)) {
                    ps.setNString(1, nextStatus);
                    ps.setNString(2, cleanReason.isEmpty() ? DeliveryRules.label(nextStatus) + "." : cleanReason);
                    ps.setInt(3, deliveryId);
                    ps.setInt(4, shipperId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Giao hàng đã thay đổi, hãy thử lại.");
                }

                String updateOrder = """
                    UPDATE orders SET status = ?
                    WHERE order_id = ?
                    """;
                try (PreparedStatement ps = c.prepareStatement(updateOrder)) {
                    ps.setNString(1, DeliveryRules.orderStatus(nextStatus));
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }

                NotificationDAO notifications = new NotificationDAO();
                String message = "Đơn #" + orderId + ": " + DeliveryRules.label(nextStatus)
                        + (cleanReason.isEmpty() ? "." : ". Lý do: " + cleanReason);
                if (ownerId > 0) {
                    notifications.insert(c, ownerId, "Cập nhật giao hàng", message);
                }
                if ("Delivered".equalsIgnoreCase(nextStatus) || "Failed".equalsIgnoreCase(nextStatus)) {
                    notifications.insert(c, customerId, "Kết quả giao hàng", message);
                    notifications.insert(c, shipperId, "Đã ghi nhận kết quả giao hàng", message);
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Delivery map(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setId(rs.getInt("delivery_id"));
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
