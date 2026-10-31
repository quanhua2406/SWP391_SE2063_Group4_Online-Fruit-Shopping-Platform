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

    public AssignmentDAO(ConnectionProvider connections) {
        this.connections = java.util.Objects.requireNonNull(connections);
    }

    public List<OrderOption> findAvailableOrders(int ownerId) throws SQLException {
        List<OrderOption> list = new ArrayList<>();
        String sql = """
            SELECT o.order_id,o.shop_id,s.shop_name,ua.recipient_name AS receiver_name,
                CONCAT_WS(N', ',NULLIF(ua.address_line,N''),NULLIF(ua.ward,N''),
                    NULLIF(ua.district,N''),NULLIF(ua.province,N'')) AS shipping_address,o.total_amount
            FROM orders o JOIN shops s ON s.shop_id=o.shop_id
            LEFT JOIN user_addresses ua ON ua.address_id=o.address_id AND ua.user_id=o.user_id
            LEFT JOIN deliveries d ON d.order_id=o.order_id
            WHERE s.owner_id=? AND s.status=N'active' AND o.status IN(N'preparing',N'failed')
              AND (d.delivery_id IS NULL OR d.status=N'failed' OR (d.status=N'assigned' AND d.staff_id IS NULL))
              AND NOT EXISTS (SELECT 1 FROM delivery_attempts a WHERE a.delivery_id=d.delivery_id AND a.closed_at IS NULL)
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

    // The new form supplies the pickup address because shops has no pickup_address column.
    public void assign(int ownerId,int orderId,int shipperId,String pickupAddress) throws SQLException {
        String pickup=pickupAddress==null ? "" : pickupAddress.trim();
        if(ownerId<=0 || orderId<=0 || shipperId<=0 || pickup.isEmpty() || pickup.length()>1000)
            throw new IllegalArgumentException("Chọn đơn, shipper và nhập địa chỉ lấy hàng (tối đa 1000 ký tự).");
        try(Connection c=connections.open()) {
            com.mycompany.onlinefruitshopping.utils.ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                int shopId; String receiver,phone,address,state;
                try(PreparedStatement ps=c.prepareStatement("""
                    SELECT o.shop_id,o.user_id,o.status,ua.recipient_name,ua.phone,
                        CONCAT_WS(N', ',NULLIF(ua.address_line,N''),NULLIF(ua.ward,N''),
                            NULLIF(ua.district,N''),NULLIF(ua.province,N'')) AS shipping_address
                    FROM orders o WITH(UPDLOCK,HOLDLOCK)
                    JOIN shops s ON s.shop_id=o.shop_id
                    LEFT JOIN user_addresses ua WITH(HOLDLOCK) ON ua.address_id=o.address_id AND ua.user_id=o.user_id
                    WHERE o.order_id=? AND s.owner_id=? AND s.status=N'active'
                    """)) {
                    ps.setInt(1,orderId); ps.setInt(2,ownerId);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new IllegalArgumentException("Đơn không thuộc cửa hàng đang hoạt động của bạn.");
                        shopId=rs.getInt("shop_id"); state=rs.getString("status");
                        receiver=rs.getString("recipient_name");phone=rs.getString("phone");address=rs.getString("shipping_address");
                    }
                }
                requireActor(c,ownerId,"ShopOwner"); requireTeamMember(c,shopId,shipperId);
                if(state==null || !List.of("preparing","failed").contains(state))
                    throw new IllegalArgumentException("Chỉ phân công đơn đã xác nhận hoặc cần giao lại.");
                if(receiver==null || receiver.isBlank() || phone==null || phone.isBlank() || address==null || address.isBlank())
                    throw new IllegalArgumentException("Đơn chưa có đủ tên, điện thoại và địa chỉ nhận hàng.");
                int deliveryId=0; String deliveryState=null; int oldStaff=0;
                try(PreparedStatement ps=c.prepareStatement("SELECT delivery_id,status,staff_id FROM deliveries WITH(UPDLOCK,HOLDLOCK) WHERE order_id=?")) {
                    ps.setInt(1,orderId);
                    try(ResultSet rs=ps.executeQuery()) { if(rs.next()) {
                        deliveryId=rs.getInt(1);deliveryState=rs.getString(2);oldStaff=rs.getInt(3);
                    }}
                }
                if(deliveryId>0) {
                    if(!"failed".equals(deliveryState) && !("assigned".equals(deliveryState) && oldStaff==0))
                        throw new IllegalArgumentException("Đơn đã có người giao hoặc đã lấy/giao hàng.");
                    try(PreparedStatement ps=c.prepareStatement("SELECT attempt_id FROM delivery_attempts WITH(UPDLOCK,HOLDLOCK) WHERE delivery_id=? AND closed_at IS NULL")) {
                        ps.setInt(1,deliveryId);
                        try(ResultSet rs=ps.executeQuery()) {if(rs.next()) throw new IllegalArgumentException("Đơn còn lần phân công đang mở.");}
                    }
                    try(PreparedStatement ps=c.prepareStatement("UPDATE deliveries SET staff_id=?,status=N'assigned',note=N'Chờ shipper nhận',proof_image_url=NULL WHERE delivery_id=?")) {
                        ps.setInt(1,shipperId);ps.setInt(2,deliveryId);ps.executeUpdate();
                    }
                } else {
                    try(PreparedStatement ps=c.prepareStatement("INSERT INTO deliveries(order_id,staff_id,status,note) VALUES(?,?,N'assigned',N'Chờ shipper nhận')",Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1,orderId);ps.setInt(2,shipperId);ps.executeUpdate();
                        try(ResultSet rs=ps.getGeneratedKeys()) {if(!rs.next()) throw new SQLException("Không lấy được delivery_id."); deliveryId=rs.getInt(1);}
                    }
                }
                try(PreparedStatement ps=c.prepareStatement("""
                    INSERT INTO delivery_attempts(delivery_id,shipper_id,assigned_by,receiver_name,
                        receiver_phone,shipping_address,pickup_address,note)
                    VALUES(?,?,?,?,?,?,?,N'Chờ shipper nhận')
                    """)) {
                    ps.setInt(1,deliveryId);ps.setInt(2,shipperId);ps.setInt(3,ownerId);
                    ps.setNString(4,receiver);ps.setNString(5,phone);ps.setNString(6,address);ps.setNString(7,pickup);ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement("UPDATE orders SET status=N'preparing' WHERE order_id=?")) {
                    ps.setInt(1,orderId);ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO order_status_history(order_id,status,changed_by) VALUES(?,N'preparing',?)")) {
                    ps.setInt(1,orderId);ps.setInt(2,ownerId);ps.executeUpdate();
                }
                new NotificationDAO().insert(c,shipperId,"Đơn giao mới","Bạn được phân công đơn #"+orderId+". Hãy nhận hoặc từ chối đơn.");
                c.commit();
            } catch(SQLException | RuntimeException e) {c.rollback();throw e;}
        }
    }

    public static void requireActor(Connection c,int actorId,String role) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("""
            SELECT u.user_id FROM users u WITH (HOLDLOCK)
            JOIN user_roles ur ON ur.user_id=u.user_id JOIN roles r ON r.role_id=ur.role_id
            WHERE u.user_id=? AND u.status=N'active' AND r.role_name=?
            """)) {
            ps.setInt(1,actorId);ps.setNString(2,role);
            try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("Tài khoản không có quyền thực hiện thao tác này."); }
        }
    }

    public static void requireTeamMember(Connection c,int shopId,int shipperId) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("""
            SELECT u.user_id FROM shop_delivery_staff t WITH (HOLDLOCK)
            JOIN users u WITH (HOLDLOCK) ON u.user_id=t.shipper_id
            JOIN user_roles ur ON ur.user_id=u.user_id JOIN roles r ON r.role_id=ur.role_id
            WHERE t.shop_id=? AND t.shipper_id=? AND t.status=N'Active'
              AND u.status=N'active' AND r.role_name=N'DeliveryStaff'
            """)) {
            ps.setInt(1,shopId);ps.setInt(2,shipperId);
            try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("Shipper phải đang hoạt động và thuộc đội của đúng shop."); }
        }
    }
}
