package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.dao.notification.NotificationDAO;
import com.mycompany.onlinefruitshopping.model.ShopDeliveryRequest;
import com.mycompany.onlinefruitshopping.model.ShopOption;
import com.mycompany.onlinefruitshopping.model.Staff;
import com.mycompany.onlinefruitshopping.utils.ConnectionProvider;
import com.mycompany.onlinefruitshopping.utils.DBConnection;
import com.mycompany.onlinefruitshopping.utils.ModuleSql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Nghiệp vụ xin vào shop và quản lý đội shipper. Không chứa chức năng Admin.
 */
public class ShipperTeamDAO {

    private final ConnectionProvider connections;

    public ShipperTeamDAO() {
        this(DBConnection::getConnection);
    }

    public ShipperTeamDAO(ConnectionProvider connections) {
        this.connections = java.util.Objects.requireNonNull(connections);
    }

    private static void lockShipper(Connection c, int shipperId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT user_id FROM users WITH (UPDLOCK,HOLDLOCK) WHERE user_id=? AND status=N'active'")) {
            ps.setInt(1, shipperId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Tài khoản shipper không tồn tại hoặc đã bị khóa.");
                }
            }
        }
        AssignmentDAO.requireActor(c, shipperId, "DeliveryStaff");
    }

    /** Settings are separate because the merged membership table has only four columns. */
    private static void ensureSettings(Connection c,int shipperId) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("""
            INSERT INTO shop_delivery_settings(shop_id,shipper_id)
            SELECT t.shop_id,t.shipper_id FROM shop_delivery_staff t
            WHERE t.shipper_id=? AND NOT EXISTS(SELECT 1 FROM shop_delivery_settings cfg
                WHERE cfg.shop_id=t.shop_id AND cfg.shipper_id=t.shipper_id)
            """)) {
            ps.setInt(1,shipperId);ps.executeUpdate();
        }
    }

    public List<ShopOption> findActiveShops() throws SQLException {
    List<ShopOption> list = new ArrayList<>();

    String sql = """
        SELECT
            shop_id,
            shop_name,
            pickup_address
        FROM shops
        WHERE status = N'active'
        ORDER BY shop_name
        """;

    try (Connection c = connections.open();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            ShopOption shop = new ShopOption();

            shop.setId(rs.getInt("shop_id"));
            shop.setName(rs.getString("shop_name"));
            shop.setPickupAddress(
                rs.getString("pickup_address")
            );

            list.add(shop);
        }
    }

    return list;
}

    /**
     * ShopOwner manages the address shown to prospective shippers.
     */
    public ShopOption findOwnerShop(int ownerId) throws SQLException {

    String sql = """
        SELECT TOP 1
            shop_id,
            shop_name,
            pickup_address
        FROM shops
        WHERE owner_id = ?
        ORDER BY shop_id
        """;

    try (Connection c = connections.open();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, ownerId);

        try (ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            ShopOption shop = new ShopOption();

            shop.setId(rs.getInt("shop_id"));
            shop.setName(rs.getString("shop_name"));
            shop.setPickupAddress(
                rs.getString("pickup_address")
            );

            return shop;
        }
    }
}

    public void updatePickupAddress(
        int ownerId,
        int shopId,
        String address
) throws SQLException {

    if (address == null
        || address.trim().length() < 5
        || address.trim().length() > 500) {

    throw new IllegalArgumentException(
        "Địa chỉ phải từ 5 đến 500 ký tự."
    );
}

    String sql = """
        UPDATE shops
        SET pickup_address = ?
        WHERE shop_id = ?
          AND owner_id = ?
        """;

    try (Connection c = connections.open();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, address.trim());
        ps.setInt(2, shopId);
        ps.setInt(3, ownerId);

        int affected = ps.executeUpdate();

        if (affected == 0) {
            throw new IllegalArgumentException(
                "Shop không tồn tại hoặc bạn không có quyền chỉnh sửa"
            );
        }
    }
}

    public void applyToShop(int shipperId, int shopId) throws SQLException {
        if (shopId <= 0) {
            throw new IllegalArgumentException("Cửa hàng không hợp lệ.");
        }
        try (Connection c = connections.open()) {
            ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                lockShipper(c, shipperId);
                ensureSettings(c, shipperId);
                int ownerId;
                try (PreparedStatement ps = c.prepareStatement("SELECT owner_id FROM shops WITH (UPDLOCK,HOLDLOCK) WHERE shop_id=? AND status=N'active'")) {
                    ps.setInt(1, shopId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Shop không tồn tại hoặc đã ngừng hoạt động.");
                        }
                        ownerId = rs.getInt("owner_id");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT shop_id FROM shop_delivery_staff WITH (UPDLOCK,HOLDLOCK) WHERE shipper_id=? AND status<>N'Inactive'")) {
                    ps.setInt(1, shipperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException("Bạn đã thuộc một shop hoặc đang chờ hoàn tất nghỉ shop.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT request_id FROM shop_delivery_requests WITH (UPDLOCK,HOLDLOCK) WHERE shipper_id=? AND status=N'Pending'")) {
                    ps.setInt(1, shipperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException("Bạn đang có một yêu cầu chờ xử lý.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO shop_delivery_requests(shipper_id,shop_id,status) VALUES(?,?,N'Pending')")) {
                    ps.setInt(1, shipperId);
                    ps.setInt(2, shopId);
                    ps.executeUpdate();
                }
                new NotificationDAO().insert(c, ownerId, "Yêu cầu tham gia đội shipper", "Shipper #" + shipperId + " đã gửi yêu cầu tham gia shop.");
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void withdrawRequest(int shipperId, int requestId) throws SQLException {
        try (Connection c = connections.open()) {
            ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                lockShipper(c, shipperId);
                ensureSettings(c, shipperId);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE shop_delivery_requests SET status=N'Withdrawn' WHERE request_id=? AND shipper_id=? AND status=N'Pending'")) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, shipperId);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Yêu cầu không tồn tại hoặc đã được xử lý.");
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void setAvailability(int shipperId, boolean available) throws SQLException {
        try (Connection c = connections.open()) {
            ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                lockShipper(c, shipperId);
                ensureSettings(c, shipperId);
                try (PreparedStatement ps = c.prepareStatement("UPDATE shop_delivery_settings SET is_available=? WHERE shipper_id=? AND leave_requested=0 AND EXISTS(SELECT 1 FROM shop_delivery_staff t WHERE t.shop_id=shop_delivery_settings.shop_id AND t.shipper_id=shop_delivery_settings.shipper_id AND t.status=N'Active')")) {
                    ps.setBoolean(1, available);
                    ps.setInt(2, shipperId);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Bạn chưa thuộc đội shipper đang hoạt động của shop.");
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void requestLeave(int shipperId) throws SQLException {
        try (Connection c = connections.open()) {
            ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                lockShipper(c, shipperId);
                ensureSettings(c, shipperId);
                int shopId;
                try (PreparedStatement ps = c.prepareStatement("SELECT t.shop_id FROM shop_delivery_staff t WITH (UPDLOCK,HOLDLOCK) JOIN shop_delivery_settings cfg ON cfg.shop_id=t.shop_id AND cfg.shipper_id=t.shipper_id WHERE t.shipper_id=? AND t.status=N'Active' AND cfg.leave_requested=0")) {
                    ps.setInt(1, shipperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Bạn không thuộc đội shipper đang hoạt động.");
                        }
                        shopId = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE shop_delivery_settings SET leave_requested=1,is_available=0,assignment_enabled=0 WHERE shipper_id=? AND leave_requested=0 AND EXISTS(SELECT 1 FROM shop_delivery_staff t WHERE t.shop_id=shop_delivery_settings.shop_id AND t.shipper_id=shop_delivery_settings.shipper_id AND t.status=N'Active')")) {
                    ps.setInt(1, shipperId);
                    ps.executeUpdate();
                }
                finalizeLeaveIfNoOpenAttempt(c, shipperId);
                int ownerId;
                try (PreparedStatement ps = c.prepareStatement("SELECT owner_id FROM shops WHERE shop_id=?")) {
                    ps.setInt(1, shopId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Không tìm thấy chủ shop.");
                        }
                        ownerId = rs.getInt(1);
                    }
                }
                new NotificationDAO().insert(c, ownerId, "Shipper xin nghỉ", "Shipper #" + shipperId + " đã gửi yêu cầu nghỉ shop.");
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Được gọi trong transaction cập nhật trạng thái giao hàng sau khi attempt
     * đóng.
     */
    public static void finalizeLeaveIfNoOpenAttempt(Connection c, int shipperId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            UPDATE shop_delivery_staff SET status=N'Inactive'
            WHERE shipper_id=? AND status=N'Active'
              AND EXISTS(SELECT 1 FROM shop_delivery_settings cfg WHERE cfg.shop_id=shop_delivery_staff.shop_id
                  AND cfg.shipper_id=shop_delivery_staff.shipper_id AND cfg.leave_requested=1)
              AND NOT EXISTS(SELECT 1 FROM deliveries d WHERE COALESCE(d.shipper_id,d.staff_id)=?
                  AND d.status IN(N'assigned',N'accepted',N'picked_up',N'delivering'))
            """)) {
            ps.setInt(1, shipperId);
            ps.setInt(2, shipperId);
            ps.executeUpdate();
        }
    }

    public List<ShopDeliveryRequest> getRequests(int ownerId) throws SQLException {
        List<ShopDeliveryRequest> list = new ArrayList<>();
        String sql = """
    SELECT
        r.request_id,
        r.shipper_id,
        r.shop_id,
        r.status,
        r.requested_at,s.shop_name,r.reviewed_at,r.review_note,

        u.full_name,
        u.email,
        u.phone,
        u.status AS account_status

    FROM shop_delivery_requests r

    JOIN shops s
        ON s.shop_id = r.shop_id

    JOIN users u
        ON u.user_id = r.shipper_id

    JOIN user_roles ur
        ON ur.user_id = u.user_id

    JOIN roles ro
        ON ro.role_id = ur.role_id

    WHERE s.owner_id = ?
      AND r.status = N'Pending'
      AND ro.role_name = N'DeliveryStaff'

    ORDER BY r.requested_at
    """;
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShopDeliveryRequest r = new ShopDeliveryRequest();
                    r.setRequestId(rs.getInt("request_id"));
                    r.setShipperId(rs.getInt("shipper_id"));
                    r.setShopId(rs.getInt("shop_id"));
                    r.setStatus(rs.getString("status"));
                    r.setRequestedAt(rs.getTimestamp("requested_at"));
                    r.setShipperName(rs.getString("full_name"));
                    r.setShopName(rs.getString("shop_name"));
                    r.setShipperEmail(rs.getString("email"));
                    r.setShipperPhone(rs.getString("phone"));
                    r.setAccountStatus(rs.getString("account_status"));
                    r.setReviewedAt(rs.getTimestamp("reviewed_at"));
                    r.setReviewNote(rs.getString("review_note"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    public List<Staff> getTeamMembers(int ownerId) throws SQLException {
        List<Staff> list = new ArrayList<>();
        String sql = """
            SELECT t.shipper_id,t.shop_id,t.status,COALESCE(cfg.leave_requested,0) AS leave_requested,
                COALESCE(cfg.is_available,1) AS is_available,COALESCE(cfg.assignment_enabled,1) AS assignment_enabled,u.full_name,
                (SELECT COUNT(*) FROM deliveries d WHERE COALESCE(d.shipper_id,d.staff_id)=t.shipper_id
                 AND d.status IN(N'assigned',N'accepted',N'picked_up',N'delivering')) AS open_delivery_count,
                (SELECT TOP (1) d.order_id FROM deliveries d WHERE COALESCE(d.shipper_id,d.staff_id)=t.shipper_id
                 AND d.status IN(N'assigned',N'accepted',N'picked_up',N'delivering') ORDER BY d.delivery_id DESC) AS current_order_id
            FROM shop_delivery_staff t
            LEFT JOIN shop_delivery_settings cfg ON cfg.shop_id=t.shop_id AND cfg.shipper_id=t.shipper_id
            JOIN shops s ON s.shop_id=t.shop_id JOIN users u ON u.user_id=t.shipper_id
            WHERE s.owner_id=? AND t.status<>N'Inactive' ORDER BY u.full_name
            """;
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Staff x = new Staff();
                    x.setId(rs.getInt("shipper_id"));
                    x.setShopId(rs.getInt("shop_id"));
                    x.setStatus(rs.getString("status"));
                    x.setFullName(rs.getString("full_name"));
                    x.setAvailable(rs.getBoolean("is_available"));
                    x.setLeaveRequested(rs.getBoolean("leave_requested"));
                    x.setAssignmentEnabled(rs.getBoolean("assignment_enabled"));
                    x.setOpenDeliveryCount(rs.getInt("open_delivery_count"));
                    x.setBusy(x.getOpenDeliveryCount() >= 3);
                    x.setCurrentOrderId(rs.getInt("current_order_id"));
                    list.add(x);
                }
            }
        }
        return list;
    }

    public Staff getMyMembership(int shipperId) throws SQLException {
        String sql = """
            SELECT TOP (1) t.shipper_id,t.shop_id,t.status,COALESCE(cfg.leave_requested,0) AS leave_requested,
                COALESCE(cfg.is_available,1) AS is_available,COALESCE(cfg.assignment_enabled,1) AS assignment_enabled,
                s.shop_name,
                (SELECT COUNT(*) FROM deliveries d WHERE COALESCE(d.shipper_id,d.staff_id)=t.shipper_id
                 AND d.status IN(N'assigned',N'accepted',N'picked_up',N'delivering')) AS open_delivery_count,
                (SELECT TOP (1) d.order_id FROM deliveries d WHERE COALESCE(d.shipper_id,d.staff_id)=t.shipper_id
                 AND d.status IN(N'assigned',N'accepted',N'picked_up',N'delivering') ORDER BY d.delivery_id DESC) AS current_order_id
            FROM shop_delivery_staff t
            LEFT JOIN shop_delivery_settings cfg ON cfg.shop_id=t.shop_id AND cfg.shipper_id=t.shipper_id
            JOIN shops s ON s.shop_id=t.shop_id
            WHERE t.shipper_id=? AND t.status<>N'Inactive'
            ORDER BY t.shipper_id
            """;
        try (Connection c = connections.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shipperId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Staff x = new Staff();
                x.setId(rs.getInt("shipper_id"));
                x.setShopId(rs.getInt("shop_id"));
                x.setStatus(rs.getString("status"));
                x.setShopName(rs.getString("shop_name"));
                x.setLeaveRequested(rs.getBoolean("leave_requested"));
                x.setAvailable(rs.getBoolean("is_available"));
                x.setAssignmentEnabled(rs.getBoolean("assignment_enabled"));
                x.setOpenDeliveryCount(rs.getInt("open_delivery_count"));
                x.setBusy(x.getOpenDeliveryCount() >= 3);
                x.setCurrentOrderId(rs.getInt("current_order_id"));
                return x;
            }
        }
    }

    
public List<ShopDeliveryRequest> getMyRequests(int shipperId)
        throws SQLException {

    List<ShopDeliveryRequest> list = new ArrayList<>();

    String sql = """
        SELECT
            r.request_id,
            r.shipper_id,
            r.shop_id,
            s.shop_name,
            r.status,
            r.requested_at,
            r.reviewed_at,
            r.review_note,

            u.full_name,
            u.email,
            u.phone,
            u.status AS account_status

        FROM shop_delivery_requests r

        JOIN shops s
            ON s.shop_id = r.shop_id

        JOIN users u
            ON u.user_id = r.shipper_id

        WHERE r.shipper_id = ?

        ORDER BY r.request_id DESC
        """;

    try (Connection c = connections.open();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1, shipperId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ShopDeliveryRequest r =
                    new ShopDeliveryRequest();

                r.setRequestId(
                    rs.getInt("request_id")
                );

                r.setShipperId(
                    rs.getInt("shipper_id")
                );

                r.setShopId(
                    rs.getInt("shop_id")
                );

                r.setStatus(
                    rs.getString("status")
                );

                r.setRequestedAt(
                    rs.getTimestamp("requested_at")
                );

                r.setShipperName(
                    rs.getString("full_name")
                );

                r.setShipperEmail(
                    rs.getString("email")
                );

                r.setShipperPhone(
                    rs.getString("phone")
                );

                r.setAccountStatus(
                    rs.getString("account_status")
                );

                r.setShopName(rs.getString("shop_name"));
                r.setReviewedAt(rs.getTimestamp("reviewed_at"));
                r.setReviewNote(rs.getString("review_note"));
                list.add(r);
            }
        }
    }

    return list;
}


    public void reviewRequest(int ownerId, int requestId, boolean approved, String reason) throws SQLException {
        String clean = reason == null ? "" : reason.trim();
        if (!approved && clean.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        }
        if (clean.length() > 255) {
            throw new IllegalArgumentException("Lý do tối đa 255 ký tự.");
        }
        try (Connection c = connections.open()) {
            ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                AssignmentDAO.requireActor(c, ownerId, "ShopOwner");
                int shipperId, shopId;
                // Read request identity, then serialize every membership mutation on the shipper row.
                try (PreparedStatement ps = c.prepareStatement("SELECT shipper_id,shop_id FROM shop_delivery_requests WHERE request_id=? AND status=N'Pending'")) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Yêu cầu không tồn tại hoặc đã được xử lý.");
                        }
                        shipperId = rs.getInt(1);
                        shopId = rs.getInt(2);
                    }
                }
                lockShipper(c, shipperId);
                ensureSettings(c, shipperId);
                try (PreparedStatement ps = c.prepareStatement("SELECT request_id FROM shop_delivery_requests r WITH (UPDLOCK,HOLDLOCK) JOIN shops s ON s.shop_id=r.shop_id WHERE r.request_id=? AND r.shop_id=? AND s.owner_id=? AND s.status=N'active' AND r.status=N'Pending'")) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, shopId);
                    ps.setInt(3, ownerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Yêu cầu không thuộc shop của bạn hoặc đã được xử lý.");
                        }
                    }
                }
                if (approved) {
                    try (PreparedStatement ps = c.prepareStatement("SELECT shop_id FROM shop_delivery_staff WITH (UPDLOCK,HOLDLOCK) WHERE shipper_id=? AND status<>N'Inactive'")) {
                        ps.setInt(1, shipperId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                throw new IllegalArgumentException("Shipper đã thuộc một shop hoặc đang chờ hoàn tất nghỉ shop.");
                            }
                        }
                    }
                    int changed;
                    try(PreparedStatement ps=c.prepareStatement("UPDATE shop_delivery_staff SET status=N'Active',joined_at=SYSDATETIME() WHERE shop_id=? AND shipper_id=? AND status=N'Inactive'")) {
                        ps.setInt(1,shopId);ps.setInt(2,shipperId);changed=ps.executeUpdate();
                    }
                    if(changed==0) {
                        try(PreparedStatement ps=c.prepareStatement("INSERT INTO shop_delivery_staff(shop_id,shipper_id,status) VALUES(?,?,N'Active')")) {
                            ps.setInt(1,shopId);ps.setInt(2,shipperId);ps.executeUpdate();
                        }
                    }
                    ensureSettings(c,shipperId);
                    try(PreparedStatement ps=c.prepareStatement("UPDATE shop_delivery_settings SET is_available=1,assignment_enabled=1,leave_requested=0 WHERE shop_id=? AND shipper_id=?")) {
                        ps.setInt(1,shopId);ps.setInt(2,shipperId);ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE shop_delivery_requests SET status=?,reviewed_at=SYSDATETIME(),reviewed_by=?,review_note=? WHERE request_id=? AND status=N'Pending'")) {
                    ps.setNString(1, approved ? "Approved" : "Rejected");
                    ps.setInt(2, ownerId);
                    ps.setNString(3, clean.isEmpty() ? null : clean);
                    ps.setInt(4, requestId);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Yêu cầu vừa được xử lý ở nơi khác.");
                    }
                }
                new NotificationDAO().insert(c, shipperId, "Kết quả yêu cầu vào shop", approved ? "Shop đã chấp nhận yêu cầu tham gia của bạn." : "Shop đã từ chối yêu cầu: " + clean);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void setAssignmentEnabled(int ownerId, int shipperId, boolean enabled) throws SQLException {
        try (Connection c = connections.open()) {
            ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                AssignmentDAO.requireActor(c, ownerId, "ShopOwner");
                try (PreparedStatement lock = c.prepareStatement("SELECT user_id FROM users WITH (UPDLOCK,HOLDLOCK) WHERE user_id=?")) {
                    lock.setInt(1, shipperId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Không tìm thấy tài khoản shipper.");
                        }
                    }
                }
                ensureSettings(c,shipperId);
                try (PreparedStatement ps = c.prepareStatement("""
                    UPDATE shop_delivery_settings SET assignment_enabled=?
                    WHERE EXISTS(SELECT 1 FROM shop_delivery_staff t JOIN shops s ON s.shop_id=t.shop_id
                        WHERE s.owner_id=? AND t.shipper_id=shop_delivery_settings.shipper_id
                          AND t.shop_id=shop_delivery_settings.shop_id AND t.status=N'Active')
                      AND shipper_id=? AND leave_requested=0
                    """)) {
                    ps.setBoolean(1, enabled);
                    ps.setInt(2, ownerId);
                    ps.setInt(3, shipperId);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Không tìm thấy shipper đang hoạt động thuộc shop của bạn.");
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void terminateMember(int ownerId, int shipperId) throws SQLException {
        try (Connection c = connections.open()) {
            ModuleSql.prepare(c);
            c.setAutoCommit(false);
            try {
                AssignmentDAO.requireActor(c, ownerId, "ShopOwner");
                try (PreparedStatement lock = c.prepareStatement("SELECT user_id FROM users WITH (UPDLOCK,HOLDLOCK) WHERE user_id=?")) {
                    lock.setInt(1, shipperId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Không tìm thấy tài khoản shipper.");
                        }
                    }
                }
                ensureSettings(c,shipperId);
                try (PreparedStatement ps = c.prepareStatement("""
                    UPDATE shop_delivery_settings SET leave_requested=1,is_available=0,assignment_enabled=0
                    WHERE EXISTS(SELECT 1 FROM shop_delivery_staff t JOIN shops s ON s.shop_id=t.shop_id
                        WHERE s.owner_id=? AND t.shipper_id=shop_delivery_settings.shipper_id
                          AND t.shop_id=shop_delivery_settings.shop_id AND t.status=N'Active')
                      AND shipper_id=? AND leave_requested=0
                    """)) {
                    ps.setInt(1, ownerId);
                    ps.setInt(2, shipperId);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Không tìm thấy shipper đang hoạt động thuộc shop của bạn.");
                    }
                }
                ShipperTeamDAO.finalizeLeaveIfNoOpenAttempt(c, shipperId);
                new NotificationDAO().insert(c, shipperId, "Shop ngừng hợp tác", "Shop đã ngừng phân công đơn mới. Bạn vẫn có thể hoàn tất đơn đang mở.");
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
