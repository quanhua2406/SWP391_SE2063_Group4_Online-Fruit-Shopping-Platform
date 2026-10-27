package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.dao.notification.NotificationDAO;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import com.mycompany.onlinefruitshopping.utils.ConnectionProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.model.OrderOption;
import com.mycompany.onlinefruitshopping.model.Staff;

/** Các truy vấn phân công chỉ dành cho chủ shop. */
public class AssignmentDAO {
    private final ConnectionProvider connections;

    public AssignmentDAO() { this(DBConnection::getConnection); }

    AssignmentDAO(ConnectionProvider connections) {
        this.connections = java.util.Objects.requireNonNull(connections);
    }

    public List<OrderOption> findAvailableOrders(int ownerId) throws SQLException {
        List<OrderOption> list = new ArrayList<>();
        String sql = """
            SELECT o.order_id, o.shop_id, s.shop_name, o.receiver_name,
                   o.shipping_address, o.total_amount
            FROM orders o JOIN shops s ON s.shop_id = o.shop_id
            WHERE s.owner_id = ? AND s.status = N'active'
              AND (o.order_status = N'Confirmed' OR (o.order_status = N'Shipping'
                   AND EXISTS (SELECT 1 FROM deliveries d WHERE d.order_id = o.order_id
                               AND d.delivery_status = N'Failed')))
              AND NOT EXISTS (
                  SELECT 1 FROM deliveries d WHERE d.order_id = o.order_id AND
                  (d.delivery_status IS NULL
                   OR d.delivery_status NOT IN (N'Failed', N'Rejected', N'Assigned')
                   OR (d.delivery_status = N'Assigned' AND d.shipper_id IS NOT NULL))
              )
            ORDER BY o.order_id DESC
            """;
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderOption o = new OrderOption();
                    o.setId(rs.getInt("order_id"));
                    o.setShopId(rs.getInt("shop_id"));
                    o.setShopName(rs.getString("shop_name"));
                    o.setReceiverName(rs.getString("receiver_name"));
                    o.setShippingAddress(rs.getString("shipping_address"));
                    o.setTotalAmount(rs.getBigDecimal("total_amount"));
                    list.add(o);
                }
            }
        }
        return list;
    }

    public List<Staff> findActiveStaff(int ownerId) throws SQLException {
        List<Staff> list = new ArrayList<>();
        String sql = """
            SELECT a.user_id, a.full_name, t.shop_id
            FROM shop_delivery_staff t
            JOIN shops s ON s.shop_id = t.shop_id
            JOIN users a ON a.user_id = t.shipper_id
            JOIN user_roles ur ON ur.user_id = a.user_id
            JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'DeliveryStaff'
            WHERE s.owner_id = ? AND s.status = N'active'
              AND t.status = N'Active' AND a.status = N'active'
            ORDER BY t.shop_id, a.full_name
            """;
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Staff staff = new Staff();
                    staff.setId(rs.getInt("user_id"));
                    staff.setShopId(rs.getInt("shop_id"));
                    staff.setFullName(rs.getString("full_name"));
                    list.add(staff);
                }
            }
        }
        return list;
    }

    public void assign(int ownerId, int orderId, int shipperId) throws SQLException {
        try (Connection c = connections.open()) {
            c.setAutoCommit(false);
            try {
                // Không nhận shop_id từ form. Lấy shop thật của đơn trong database.
                String orderSql = """
                    SELECT o.shop_id, o.order_status
                    FROM orders o WITH (UPDLOCK, HOLDLOCK)
                    JOIN shops s WITH (HOLDLOCK) ON s.shop_id = o.shop_id
                    JOIN users a WITH (HOLDLOCK) ON a.user_id = s.owner_id
                    JOIN user_roles ur ON ur.user_id = a.user_id
                    JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'ShopOwner'
                    WHERE o.order_id = ? AND s.owner_id = ? AND s.status = N'active'
                      AND a.status = N'active'
                    """;
                int shopId;
                String orderStatus;
                try (PreparedStatement ps = c.prepareStatement(orderSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, ownerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Đơn hàng không thuộc shop của bạn.");
                        shopId = rs.getInt("shop_id");
                        orderStatus = rs.getString("order_status");
                    }
                }
                if (!"Confirmed".equals(orderStatus) && !"Shipping".equals(orderStatus)) {
                    throw new IllegalArgumentException("Chỉ phân công đơn đã xác nhận hoặc đơn giao thất bại.");
                }

                String staffSql = """
                    SELECT a.user_id FROM shop_delivery_staff t WITH (HOLDLOCK)
                    JOIN users a WITH (HOLDLOCK) ON a.user_id = t.shipper_id
                    JOIN user_roles ur ON ur.user_id = a.user_id
                    JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'DeliveryStaff'
                    WHERE t.shop_id = ? AND t.shipper_id = ? AND t.status = N'Active'
                      AND a.status = N'active'
                    """;
                try (PreparedStatement ps = c.prepareStatement(staffSql)) {
                    ps.setInt(1, shopId);
                    ps.setInt(2, shipperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("DeliveryStaff phải đang hoạt động và thuộc đúng đội của shop.");
                    }
                }

                // Giữ các lần giao Failed/Rejected để xem lại lịch sử.
                // Bản ghi Assigned chưa có shipper (nếu có) sẽ được dùng lại.
                int unassignedId = 0;
                boolean hasFailed = false;
                String attemptsSql = """
                    SELECT delivery_id, delivery_status, shipper_id
                    FROM deliveries WITH (UPDLOCK, HOLDLOCK) WHERE order_id = ?
                    """;
                try (PreparedStatement ps = c.prepareStatement(attemptsSql)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String status = rs.getString("delivery_status");
                            if ("Failed".equals(status)) {
                                hasFailed = true;
                            } else if ("Rejected".equals(status)) {
                                // Lần giao cũ đã kết thúc, có thể phân công lần mới.
                            } else if ("Assigned".equals(status) && rs.getObject("shipper_id") == null && unassignedId == 0) {
                                unassignedId = rs.getInt("delivery_id");
                            } else {
                                throw new IllegalArgumentException("Đơn đã có người giao hoặc đã hoàn tất. Hãy tải lại trang.");
                            }
                        }
                    }
                }
                if ("Shipping".equals(orderStatus) && !hasFailed) {
                    throw new IllegalArgumentException("Đơn đang giao không được phân công lại.");
                }

                if (unassignedId == 0) {
                    String insert = """
                        INSERT INTO deliveries(shop_id, order_id, shipper_id, delivery_status, note)
                        VALUES (?, ?, ?, N'Assigned', N'Đã phân công; chờ shipper nhận đơn.')
                        """;
                    try (PreparedStatement ps = c.prepareStatement(insert)) {
                        ps.setInt(1, shopId);
                        ps.setInt(2, orderId);
                        ps.setInt(3, shipperId);
                        ps.executeUpdate();
                    }
                } else {
                    String update = """
                        UPDATE deliveries SET shipper_id = ?, delivery_status = N'Assigned',
                            pickup_time = NULL, delivered_time = NULL,
                            note = N'Đã phân công; chờ shipper nhận đơn.'
                        WHERE delivery_id = ? AND shipper_id IS NULL
                        """;
                    try (PreparedStatement ps = c.prepareStatement(update)) {
                        ps.setInt(1, shipperId);
                        ps.setInt(2, unassignedId);
                        if (ps.executeUpdate() != 1) throw new SQLException("Phân công đã thay đổi.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET order_status = N'Confirmed' WHERE order_id = ?")) {
                    ps.setInt(1, orderId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Đơn hàng đã thay đổi.");
                }
                new NotificationDAO().insert(c, shipperId, "Bạn có đơn giao hàng mới",
                        "Bạn được phân công đơn #" + orderId + ". Vào Giao hàng của tôi để xem địa chỉ và nhận hoặc từ chối đơn.");
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
