package com.mycompany.onlinefruitshopping.shopowner.dao;

import com.mycompany.onlinefruitshopping.shopowner.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;
import com.mycompany.onlinefruitshopping.shopowner.model.Delivery;
import com.mycompany.onlinefruitshopping.shopowner.model.OrderItem;
import com.mycompany.onlinefruitshopping.shopowner.utils.DeliveryRules;

public class DeliveryDAO {
    private static final String SELECT_DELIVERY = """
        SELECT d.delivery_id, d.order_id, d.shop_id, d.shipper_id, d.delivery_status,
               d.pickup_time, d.delivered_time, d.note AS delivery_note,
               s.shop_name, s.pickup_address, a.full_name AS shipper_name,
               o.receiver_name, o.receiver_phone, o.shipping_address,
               o.total_amount, o.order_status, o.payment_method, o.payment_status,
               o.note AS order_note
        FROM Deliveries d
        JOIN Orders o ON o.order_id = d.order_id AND o.shop_id = d.shop_id
        JOIN Shops s ON s.shop_id = d.shop_id
        LEFT JOIN Accounts a ON a.account_id = d.shipper_id
        LEFT JOIN ShopDeliveryStaff t ON t.shop_id = d.shop_id AND t.shipper_id = d.shipper_id
        WHERE s.status = N'Active'
        """;

    // Chỉ nối các điều kiện SQL cố định; dữ liệu người dùng luôn truyền qua dấu ?.
    private String scope(Account user) {
        if ("ShopOwner".equals(user.getRole())) return " AND s.owner_account_id = ?";
        if ("Shipper".equals(user.getRole())) return " AND d.shipper_id = ? AND t.status = N'Active'";
        throw new IllegalArgumentException("Bạn không có quyền xem giao hàng.");
    }

    public List<Delivery> findAll(Account user) throws SQLException {
        List<Delivery> list = new ArrayList<>();
        String sql = SELECT_DELIVERY + scope(user) + " ORDER BY d.delivery_id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Delivery findById(int id, Account user) throws SQLException {
        String sql = SELECT_DELIVERY + scope(user) + " AND d.delivery_id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<OrderItem> findItems(int deliveryId, Account user) throws SQLException {
        // Kiểm tra quyền cả khi đọc sản phẩm, không chỉ ở trang danh sách.
        Delivery delivery = findById(deliveryId, user);
        if (delivery == null) throw new IllegalArgumentException("Không tìm thấy giao hàng thuộc quyền của bạn.");
        List<OrderItem> list = new ArrayList<>();
        String sql = """
            SELECT p.product_name, v.variant_name, od.price, od.quantity
            FROM OrderDetails od
            JOIN ProductVariants v ON v.variant_id = od.variant_id
            JOIN Products p ON p.product_id = v.product_id
            WHERE od.order_id = ? ORDER BY od.order_detail_id
            """;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delivery.getOrderId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setProductName(rs.getString("product_name"));
                    item.setVariantName(rs.getString("variant_name"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setQuantity(rs.getInt("quantity"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /** Một transaction: kiểm tra quyền -> kiểm tra trạng thái -> cập nhật -> thông báo. */
    public void updateStatus(int deliveryId, int shipperId, String action,
                             String reason, boolean collected) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Khóa đơn trước. Các thao tác phân công cũng khóa đơn theo cách này.
                // Hai lần bấm đồng thời sẽ không cùng thay đổi một đơn hàng.
                String orderSql = """
                    SELECT o.order_id, o.order_status, o.account_id, o.payment_method,
                           o.payment_status, s.owner_account_id
                    FROM Orders o WITH (UPDLOCK, HOLDLOCK)
                    JOIN Shops s ON s.shop_id = o.shop_id
                    WHERE o.order_id = (
                        SELECT order_id FROM Deliveries WHERE delivery_id = ? AND shipper_id = ?
                    ) AND s.status = N'Active'
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
                        customerId = rs.getInt("account_id");
                        ownerId = rs.getInt("owner_account_id");
                        orderStatus = rs.getString("order_status");
                        paymentMethod = rs.getString("payment_method");
                        paymentStatus = rs.getString("payment_status");
                    }
                }

                String oldStatus;
                String deliverySql = """
                    SELECT d.delivery_status, d.note
                    FROM Deliveries d WITH (UPDLOCK, HOLDLOCK)
                    JOIN ShopDeliveryStaff t WITH (HOLDLOCK)
                      ON t.shop_id = d.shop_id AND t.shipper_id = d.shipper_id
                    JOIN Accounts a WITH (HOLDLOCK) ON a.account_id = d.shipper_id
                    JOIN Roles r ON r.role_id = a.role_id
                    WHERE d.delivery_id = ? AND d.shipper_id = ?
                      AND t.status = N'Active' AND a.status = N'Active' AND r.role_name = N'Shipper'
                    """;
                try (PreparedStatement ps = c.prepareStatement(deliverySql)) {
                    ps.setInt(1, deliveryId);
                    ps.setInt(2, shipperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Bạn không còn là shipper đang hoạt động của shop.");
                        oldStatus = rs.getString("delivery_status");
                    }
                }

                String nextStatus = DeliveryRules.nextStatus(oldStatus, action);
                String cleanReason = DeliveryRules.validateReason(nextStatus, reason);
                if (!DeliveryRules.orderStatus(oldStatus).equals(orderStatus)) {
                    throw new IllegalArgumentException("Trạng thái đơn hàng đã thay đổi. Hãy liên hệ chủ shop.");
                }
                if ("Delivered".equals(nextStatus)) {
                    DeliveryRules.validatePayment(paymentMethod, paymentStatus, collected);
                }

                String updateDelivery = """
                    UPDATE Deliveries SET delivery_status = ?, note = ?,
                        pickup_time = CASE WHEN ? = N'PickedUp' THEN GETDATE() ELSE pickup_time END,
                        delivered_time = CASE WHEN ? = N'Delivered' THEN GETDATE() ELSE delivered_time END
                    WHERE delivery_id = ? AND shipper_id = ? AND delivery_status = ?
                    """;
                try (PreparedStatement ps = c.prepareStatement(updateDelivery)) {
                    ps.setNString(1, nextStatus);
                    ps.setNString(2, cleanReason.isEmpty() ? DeliveryRules.label(nextStatus) + "." : cleanReason);
                    ps.setNString(3, nextStatus);
                    ps.setNString(4, nextStatus);
                    ps.setInt(5, deliveryId);
                    ps.setInt(6, shipperId);
                    ps.setNString(7, oldStatus);
                    if (ps.executeUpdate() != 1) throw new SQLException("Giao hàng đã thay đổi, hãy thử lại.");
                }

                String updateOrder = """
                    UPDATE Orders SET order_status = ?,
                        payment_status = CASE WHEN ? = N'Delivered' AND payment_method = N'COD'
                                              THEN N'Paid' ELSE payment_status END
                    WHERE order_id = ?
                    """;
                try (PreparedStatement ps = c.prepareStatement(updateOrder)) {
                    ps.setNString(1, DeliveryRules.orderStatus(nextStatus));
                    ps.setNString(2, nextStatus);
                    ps.setInt(3, orderId);
                    ps.executeUpdate();
                }

                NotificationDAO notifications = new NotificationDAO();
                String message = "Đơn #" + orderId + ": " + DeliveryRules.label(nextStatus)
                        + (cleanReason.isEmpty() ? "." : ". Lý do: " + cleanReason);
                notifications.insert(c, ownerId, "Cập nhật giao hàng", message);
                if ("Delivered".equals(nextStatus) || "Failed".equals(nextStatus)) {
                    notifications.insert(c, customerId, "Kết quả giao hàng", message);
                    notifications.insert(c, shipperId, "Đã ghi nhận kết quả giao hàng", message);
                }
                c.commit();
            } catch (SQLException | IllegalArgumentException e) {
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
