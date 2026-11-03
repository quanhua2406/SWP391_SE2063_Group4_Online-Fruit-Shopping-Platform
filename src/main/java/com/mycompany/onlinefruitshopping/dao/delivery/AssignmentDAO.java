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
            SELECT o.order_id,o.shop_id,s.shop_name,
                COALESCE(NULLIF(o.receiver_name,N''),ua.recipient_name) AS receiver_name,
                COALESCE(NULLIF(o.shipping_address,N''),CONCAT_WS(N', ',NULLIF(ua.address_line,N''),
                    NULLIF(ua.ward,N''),NULLIF(ua.district,N''),NULLIF(ua.province,N''))) AS shipping_address,o.total_amount
            FROM orders o JOIN shops s ON s.shop_id=o.shop_id
            LEFT JOIN user_addresses ua ON ua.address_id=o.address_id AND ua.user_id=o.user_id
            LEFT JOIN deliveries d ON d.delivery_id=(SELECT MAX(x.delivery_id) FROM deliveries x WHERE x.order_id=o.order_id)
            WHERE s.owner_id=? AND s.status=N'active' AND o.status IN(N'preparing',N'failed')
              AND (d.delivery_id IS NULL OR d.status IN(N'failed',N'rejected')
                   OR (d.status=N'assigned' AND COALESCE(d.shipper_id,d.staff_id) IS NULL))
              AND NOT EXISTS (SELECT 1 FROM deliveries x WHERE x.order_id=o.order_id
                  AND (x.status IN(N'accepted',N'picked_up',N'delivering',N'delivered')
                       OR (x.status=N'assigned' AND COALESCE(x.shipper_id,x.staff_id) IS NOT NULL)))
              AND NOT EXISTS (SELECT 1 FROM delivery_attempts a JOIN deliveries x ON x.delivery_id=a.delivery_id
                  WHERE x.order_id=o.order_id AND a.closed_at IS NULL)
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
            SELECT a.user_id, a.full_name, t.shop_id,
                COALESCE(cfg.is_available,1) AS is_available,COALESCE(cfg.assignment_enabled,1) AS assignment_enabled,
                (SELECT COUNT(*) FROM deliveries da WHERE COALESCE(da.shipper_id,da.staff_id)=t.shipper_id
                    AND da.status IN(N'assigned',N'accepted',N'picked_up',N'delivering')) AS open_delivery_count
            FROM shop_delivery_staff t
            LEFT JOIN shop_delivery_settings cfg ON cfg.shop_id=t.shop_id AND cfg.shipper_id=t.shipper_id
            JOIN shops s ON s.shop_id = t.shop_id
            JOIN users a ON a.user_id = t.shipper_id
            JOIN user_roles ur ON ur.user_id = a.user_id
            JOIN roles r ON r.role_id = ur.role_id AND r.role_name = N'DeliveryStaff'
            WHERE s.owner_id = ? AND s.status = N'active'
              AND t.status = N'Active' AND COALESCE(cfg.leave_requested,0)=0 AND COALESCE(cfg.is_available,1)=1 AND COALESCE(cfg.assignment_enabled,1)=1 AND a.status = N'active'
              AND (SELECT COUNT(*) FROM deliveries busy WHERE COALESCE(busy.shipper_id,busy.staff_id)=t.shipper_id
                  AND busy.status IN(N'assigned',N'accepted',N'picked_up',N'delivering')) < 3
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
                    staff.setAvailable(rs.getBoolean("is_available"));
                    staff.setAssignmentEnabled(rs.getBoolean("assignment_enabled"));
                    staff.setOpenDeliveryCount(rs.getInt("open_delivery_count"));
                    staff.setBusy(staff.getOpenDeliveryCount() >= 3);
                    list.add(staff);
                }
            }
        }
        return list;
    }

    // An optional form address overrides shops.pickup_address for this assignment snapshot.
    public void assign(int ownerId,int orderId,int shipperId,String pickupAddress) throws SQLException {
        String pickup=pickupAddress==null ? "" : pickupAddress.trim();
        if(ownerId<=0 || orderId<=0 || shipperId<=0 || pickup.length()>500)
            throw new IllegalArgumentException("Chọn đơn, shipper và nhập địa chỉ lấy hàng (tối đa 500 ký tự).");
        try(Connection c=connections.open()) {
            com.mycompany.onlinefruitshopping.utils.ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                int shopId; String receiver,phone,address,state;
                try(PreparedStatement ps=c.prepareStatement("""
                    SELECT o.shop_id,o.user_id,o.status,s.pickup_address,
                        COALESCE(NULLIF(o.receiver_name,N''),ua.recipient_name) AS recipient_name,
                        COALESCE(NULLIF(o.receiver_phone,N''),ua.phone) AS phone,
                        COALESCE(NULLIF(o.shipping_address,N''),CONCAT_WS(N', ',NULLIF(ua.address_line,N''),
                            NULLIF(ua.ward,N''),NULLIF(ua.district,N''),NULLIF(ua.province,N''))) AS shipping_address
                    FROM orders o WITH(UPDLOCK,HOLDLOCK)
                    JOIN shops s ON s.shop_id=o.shop_id
                    LEFT JOIN user_addresses ua WITH(HOLDLOCK) ON ua.address_id=o.address_id AND ua.user_id=o.user_id
                    WHERE o.order_id=? AND s.owner_id=? AND s.status=N'active'
                    """)) {
                    ps.setInt(1,orderId); ps.setInt(2,ownerId);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new IllegalArgumentException("Đơn không thuộc cửa hàng đang hoạt động của bạn.");
                        shopId=rs.getInt("shop_id"); state=rs.getString("status");
                        if(pickup.isEmpty()) pickup=rs.getString("pickup_address");
                        receiver=rs.getString("recipient_name");phone=rs.getString("phone");address=rs.getString("shipping_address");
                    }
                }
                if(pickup==null || pickup.isBlank() || pickup.length()>500)
                    throw new IllegalArgumentException("Nhập địa chỉ lấy hàng hoặc lưu địa chỉ shop trước khi phân công (tối đa 500 ký tự).");
                requireActor(c,ownerId,"ShopOwner");
                // Khóa hàng users theo shipper để tuần tự hóa mọi thao tác phân công cùng shipper.
                try(PreparedStatement lock=c.prepareStatement("SELECT user_id FROM users WITH (UPDLOCK,HOLDLOCK) WHERE user_id=? AND status=N'active'")) {
                    lock.setInt(1,shipperId); try(ResultSet rs=lock.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("Tài khoản shipper không hoạt động."); }
                }
                requireAssignableTeamMember(c,shopId,shipperId);
                // Giữ giới hạn 3 đơn, kể cả các đơn được tạo bởi module khác. Khóa users phía trên
                // tuần tự hóa các lần phân công đồng thời cho cùng một shipper.
                try(PreparedStatement busy=c.prepareStatement("SELECT COUNT(*) FROM deliveries WITH (UPDLOCK,HOLDLOCK) WHERE COALESCE(shipper_id,staff_id)=? AND status IN(N'assigned',N'accepted',N'picked_up',N'delivering')")) {
                    busy.setInt(1,shipperId);
                    try(ResultSet rs=busy.executeQuery()) {
                        rs.next();
                        int openCount=rs.getInt(1);
                        if(openCount >= 3) throw new IllegalArgumentException("Shipper đã đủ tải (3/3 đơn đang giao). Chọn shipper khác hoặc chờ shipper hoàn tất đơn.");
                    }
                }
                if(state==null || !List.of("preparing","failed").contains(state))
                    throw new IllegalArgumentException("Chỉ phân công đơn đã xác nhận hoặc cần giao lại.");
                if(receiver==null || receiver.isBlank() || phone==null || phone.isBlank() || address==null || address.isBlank())
                    throw new IllegalArgumentException("Đơn chưa có đủ tên, điện thoại và địa chỉ nhận hàng.");
                int deliveryId=0; String deliveryState=null; int oldStaff=0;
                try(PreparedStatement ps=c.prepareStatement("SELECT delivery_id,status,COALESCE(shipper_id,staff_id) FROM deliveries WITH(UPDLOCK,HOLDLOCK) WHERE order_id=? ORDER BY delivery_id DESC")) {
                    ps.setInt(1,orderId);
                    try(ResultSet rs=ps.executeQuery()) { while(rs.next()) {
                        String existing=rs.getString(2);
                        int staff=rs.getInt(3);
                        if(!List.of("failed","rejected").contains(existing) && !("assigned".equals(existing) && staff==0))
                            throw new IllegalArgumentException("Đơn đã có người giao hoặc đã lấy/giao hàng.");
                        if(deliveryId==0) {deliveryId=rs.getInt(1);deliveryState=existing;oldStaff=staff;}
                    }}
                }
                try(PreparedStatement ps=c.prepareStatement("SELECT a.attempt_id FROM delivery_attempts a WITH(UPDLOCK,HOLDLOCK) JOIN deliveries d ON d.delivery_id=a.delivery_id WHERE d.order_id=? AND a.closed_at IS NULL")) {
                    ps.setInt(1,orderId);
                    try(ResultSet rs=ps.executeQuery()) {if(rs.next()) throw new IllegalArgumentException("Đơn còn lần phân công đang mở.");}
                }
                if(deliveryId>0) {
                    if(!List.of("failed","rejected").contains(deliveryState) && !("assigned".equals(deliveryState) && oldStaff==0))
                        throw new IllegalArgumentException("Đơn đã có người giao hoặc đã lấy/giao hàng.");
                    try(PreparedStatement ps=c.prepareStatement("SELECT attempt_id FROM delivery_attempts WITH(UPDLOCK,HOLDLOCK) WHERE delivery_id=? AND closed_at IS NULL")) {
                        ps.setInt(1,deliveryId);
                        try(ResultSet rs=ps.executeQuery()) {if(rs.next()) throw new IllegalArgumentException("Đơn còn lần phân công đang mở.");}
                    }
                    try(PreparedStatement ps=c.prepareStatement("UPDATE deliveries SET staff_id=?,shipper_id=?,shop_id=?,status=N'assigned',delivery_status=N'Assigned',note=N'Chờ shipper nhận',proof_image_url=NULL,pickup_time=NULL,delivered_time=NULL WHERE delivery_id=?")) {
                        ps.setInt(1,shipperId);ps.setInt(2,shipperId);ps.setInt(3,shopId);ps.setInt(4,deliveryId);ps.executeUpdate();
                    }
                } else {
                    try(PreparedStatement ps=c.prepareStatement("INSERT INTO deliveries(order_id,staff_id,shipper_id,shop_id,status,delivery_status,note) VALUES(?,?,?,?,N'assigned',N'Assigned',N'Chờ shipper nhận')",Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1,orderId);ps.setInt(2,shipperId);ps.setInt(3,shipperId);ps.setInt(4,shopId);ps.executeUpdate();
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
                try(PreparedStatement ps=c.prepareStatement("UPDATE orders SET status=N'preparing',order_status=N'preparing' WHERE order_id=?")) {
                    ps.setInt(1,orderId);ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO order_status_history(order_id,status,changed_by) VALUES(?,N'preparing',?)")) {
                    ps.setInt(1,orderId);ps.setInt(2,ownerId);ps.executeUpdate();
                }
                new NotificationDAO().insert(c,shipperId,"Đơn giao mới","Bạn được phân công đơn #"+orderId+". Hãy nhận hoặc từ chối đơn.");
                c.commit();
            } catch(SQLException e) {
                c.rollback();
                if (e.getErrorCode()==2601 || e.getErrorCode()==2627) {
                    throw new IllegalArgumentException("Đơn hàng vừa được phân công ở thao tác khác. Vui lòng tải lại danh sách.",e);
                }
                throw e;
            } catch(RuntimeException e) { c.rollback(); throw e; }
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

    public static void requireAssignableTeamMember(Connection c,int shopId,int shipperId) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("""
            SELECT u.user_id FROM shop_delivery_staff t WITH (UPDLOCK,HOLDLOCK)
            LEFT JOIN shop_delivery_settings cfg WITH (HOLDLOCK) ON cfg.shop_id=t.shop_id AND cfg.shipper_id=t.shipper_id
            JOIN users u WITH (HOLDLOCK) ON u.user_id=t.shipper_id
            JOIN user_roles ur ON ur.user_id=u.user_id JOIN roles r ON r.role_id=ur.role_id
            WHERE t.shop_id=? AND t.shipper_id=? AND t.status=N'Active'
              AND COALESCE(cfg.leave_requested,0)=0 AND COALESCE(cfg.is_available,1)=1 AND COALESCE(cfg.assignment_enabled,1)=1
              AND u.status=N'active' AND r.role_name=N'DeliveryStaff'
            """)) {
            ps.setInt(1,shopId);ps.setInt(2,shipperId);
            try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("Shipper không sẵn sàng nhận đơn hoặc không thuộc đội của shop."); }
        }
    }

    /** Cho phép shipper đã gửi leave_requested hoàn tất attempt đang mở trước đó. */
    public static void requireCurrentAttemptMember(Connection c,int shopId,int shipperId) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("""
            SELECT u.user_id FROM shop_delivery_staff t WITH (HOLDLOCK)
            JOIN users u WITH (HOLDLOCK) ON u.user_id=t.shipper_id
            JOIN user_roles ur ON ur.user_id=u.user_id JOIN roles r ON r.role_id=ur.role_id
            WHERE t.shop_id=? AND t.shipper_id=? AND t.status=N'Active'
              AND u.status=N'active' AND r.role_name=N'DeliveryStaff'
            """)) {
            ps.setInt(1,shopId);ps.setInt(2,shipperId);
            try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("Không tìm thấy quyền xử lý lần giao hàng này."); }
        }
    }

    /** Giữ alias cũ cho các caller khác; chỉ dùng khi cần kiểm tra thành viên đang Active. */
    public static void requireTeamMember(Connection c,int shopId,int shipperId) throws SQLException {
        requireAssignableTeamMember(c,shopId,shipperId);
    }
}
