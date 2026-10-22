package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.Delivery;
import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.OrderItem;
import com.mycompany.onlinefruitshopping.model.OrderStatusHistory;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    public List<Order> getOrdersByShop(int shopId, String statusFilter, String searchQuery) throws SQLException {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.order_id, o.user_id, o.shop_id, o.address_id, o.voucher_id, o.status, o.total_amount, o.created_at, "
                + "u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone, "
                + "a.recipient_name, a.phone AS recipient_phone, "
                + "a.address_line, a.ward, a.district, a.province "
                + "FROM orders o "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN user_addresses a ON o.address_id = a.address_id "
                + "WHERE o.shop_id = ? "
        );

        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
            sql.append("AND o.status = ? ");
        }

        if (searchQuery != null && !searchQuery.isBlank()) {
            sql.append("AND (u.full_name LIKE ? OR a.recipient_name LIKE ? OR a.phone LIKE ? OR CAST(o.order_id AS VARCHAR) LIKE ?) ");
        }

        sql.append("ORDER BY o.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            ps.setInt(paramIndex++, shopId);

            if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
                ps.setString(paramIndex++, statusFilter.trim());
            }

            if (searchQuery != null && !searchQuery.isBlank()) {
                String keyword = "%" + searchQuery.trim() + "%";
                ps.setString(paramIndex++, keyword);
                ps.setString(paramIndex++, keyword);
                ps.setString(paramIndex++, keyword);
                ps.setString(paramIndex++, keyword);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrderRow(rs);
                    list.add(order);
                }
            }
        }

        // Bổ sung số lượng và danh sách mặt hàng cho từng đơn hàng
        for (Order order : list) {
            order.setItems(getOrderItems(order.getOrderId()));
        }

        return list;
    }

    public Order getOrderById(int orderId, int shopId) throws SQLException {
        String sql = "SELECT o.order_id, o.user_id, o.shop_id, o.address_id, o.voucher_id, o.status, o.total_amount, o.created_at, "
                + "u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone, "
                + "a.recipient_name, a.phone AS recipient_phone, "
                + "a.address_line, a.ward, a.district, a.province "
                + "FROM orders o "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN user_addresses a ON o.address_id = a.address_id "
                + "WHERE o.order_id = ? " + (shopId > 0 ? "AND o.shop_id = ?" : "");

        Order order = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            if (shopId > 0) {
                ps.setInt(2, shopId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    order = mapOrderRow(rs);
                }
            }
        }

        if (order != null) {
            order.setItems(getOrderItems(orderId));
            order.setHistoryList(getOrderHistory(orderId));
        }

        return order;
    }

    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.variant_id, oi.batch_id, oi.quantity, oi.unit_price, "
                + "p.product_name, "
                + "pv.packaging_type, pv.unit, pv.weight_value, "
                + "(SELECT TOP 1 pi.image_url FROM product_images pi WHERE pi.product_id = p.product_id ORDER BY pi.sort_order ASC, pi.image_id ASC) AS image_url "
                + "FROM order_items oi "
                + "INNER JOIN product_variants pv ON oi.variant_id = pv.variant_id "
                + "INNER JOIN products p ON pv.product_id = p.product_id "
                + "WHERE oi.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    int batchId = rs.getInt("batch_id");
                    if (!rs.wasNull()) {
                        item.setBatchId(batchId);
                    }
                    item.setQuantity(rs.getDouble("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setProductName(rs.getString("product_name"));
                    item.setPackagingType(rs.getString("packaging_type"));
                    item.setUnit(rs.getString("unit"));
                    item.setWeightValue(rs.getDouble("weight_value"));
                    item.setImageUrl(rs.getString("image_url"));

                    list.add(item);
                }
            }
        }
        return list;
    }

    public List<OrderStatusHistory> getOrderHistory(int orderId) throws SQLException {
        List<OrderStatusHistory> list = new ArrayList<>();
        String sql = "SELECT h.history_id, h.order_id, h.status, h.changed_by, h.changed_at, "
                + "u.full_name AS changed_by_name "
                + "FROM order_status_history h "
                + "LEFT JOIN users u ON h.changed_by = u.user_id "
                + "WHERE h.order_id = ? "
                + "ORDER BY h.changed_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory h = new OrderStatusHistory();
                    h.setHistoryId(rs.getInt("history_id"));
                    h.setOrderId(rs.getInt("order_id"));
                    h.setStatus(rs.getString("status"));
                    h.setChangedBy(rs.getInt("changed_by"));
                    h.setChangedByName(rs.getString("changed_by_name"));
                    h.setChangedAt(rs.getTimestamp("changed_at"));
                    list.add(h);
                }
            }
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, int shopId, String newStatus, int changedByUserId, String note) throws SQLException {
        // Kiểm tra tính hợp lệ của trạng thái mới theo DB constraint
        if (!List.of("pending_confirmation", "preparing", "shipping", "delivered", "cancelled", "failed").contains(newStatus)) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + newStatus);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Kiểm tra đơn hàng có tồn tại và thuộc shop không
            String checkSql = "SELECT status FROM orders WHERE order_id = ?" + (shopId > 0 ? " AND shop_id = ?" : "");
            String currentStatus = null;
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, orderId);
                if (shopId > 0) {
                    checkPs.setInt(2, shopId);
                }
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("status");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Kiểm tra ràng buộc chuyển trạng thái hợp lệ
            if ("cancelled".equals(currentStatus) || "delivered".equals(currentStatus)) {
                throw new IllegalStateException("Đơn hàng đã ở trạng thái kết thúc (" + currentStatus + "), không thể thay đổi.");
            }

            // 2. Cập nhật trạng thái trong bảng orders
            String updateSql = "UPDATE orders SET status = ? WHERE order_id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setString(1, newStatus);
                updatePs.setInt(2, orderId);
                updatePs.executeUpdate();
            }

            // 3. Ghi vết vào bảng order_status_history
            String historyStatus = newStatus;
            if (note != null && !note.isBlank()) {
                historyStatus = newStatus + " (" + note.trim() + ")";
                if (historyStatus.length() > 50) {
                    historyStatus = historyStatus.substring(0, 50);
                }
            }

            String historySql = "INSERT INTO order_status_history (order_id, status, changed_by, changed_at) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement histPs = conn.prepareStatement(historySql)) {
                histPs.setInt(1, orderId);
                histPs.setString(2, historyStatus);
                histPs.setInt(3, changedByUserId);
                histPs.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public Map<String, Integer> countOrdersByStatus(int shopId) throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("all", 0);
        counts.put("pending_confirmation", 0);
        counts.put("preparing", 0);
        counts.put("shipping", 0);
        counts.put("delivered", 0);
        counts.put("cancelled", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM orders WHERE shop_id = ? GROUP BY status";
        int total = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int cnt = rs.getInt("cnt");
                    counts.put(status, cnt);
                    total += cnt;
                }
            }
        }
        counts.put("all", total);
        return counts;
    }

    public void seedSampleOrdersIfEmpty(int shopId) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM orders WHERE shop_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Đã có đơn hàng, không cần seed
                }
            }
        }

        // Tạo dữ liệu mẫu kết nối bảng users, user_addresses, product_variants
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Tìm user customer và address mẫu
            int customerUserId = 0;
            String userSql = "SELECT TOP 1 user_id FROM users WHERE email <> 'trung@fruit.com'";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(userSql)) {
                if (rs.next()) {
                    customerUserId = rs.getInt(1);
                }
            }

            if (customerUserId == 0) {
                // Tạo một customer mẫu nếu chưa có
                String insertUser = "INSERT INTO users (full_name, email, phone, password_hash, status) "
                        + "VALUES (N'Nguyễn Văn An', 'customer@fruit.com', '0988888888', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'active')";
                try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) customerUserId = rs.getInt(1);
                    }
                }
            }

            int addressId = 0;
            String addrSql = "SELECT TOP 1 address_id FROM user_addresses WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(addrSql)) {
                ps.setInt(1, customerUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) addressId = rs.getInt(1);
                }
            }

            if (addressId == 0) {
                String insertAddr = "INSERT INTO user_addresses (user_id, recipient_name, phone, address_line, ward, district, province, is_default) "
                        + "VALUES (?, N'Nguyễn Văn An', '0988888888', N'Số 123 Đường Nguyễn Trãi', N'Phường Bến Thành', N'Quận 1', N'TP. Hồ Chí Minh', 1)";
                try (PreparedStatement ps = conn.prepareStatement(insertAddr, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerUserId);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) addressId = rs.getInt(1);
                    }
                }
            }

            // Lấy 2 variant mẫu của shop để tạo đơn
            List<Integer> variantIds = new ArrayList<>();
            List<Double> variantPrices = new ArrayList<>();
            String varSql = "SELECT TOP 2 pv.variant_id, pv.price FROM product_variants pv "
                    + "INNER JOIN products p ON pv.product_id = p.product_id WHERE p.shop_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(varSql)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        variantIds.add(rs.getInt("variant_id"));
                        variantPrices.add(rs.getDouble("price"));
                    }
                }
            }

            if (!variantIds.isEmpty()) {
                // Đơn 1: pending_confirmation (Đang chờ duyệt)
                insertOrderWithItems(conn, customerUserId, shopId, addressId, "pending_confirmation", 
                        variantIds.get(0), 2.0, variantPrices.get(0));

                // Đơn 2: preparing (Đang chuẩn bị hàng)
                if (variantIds.size() > 1) {
                    insertOrderWithItems(conn, customerUserId, shopId, addressId, "preparing", 
                            variantIds.get(1), 1.0, variantPrices.get(1));
                }

                // Đơn 3: shipping (Đang giao hàng)
                insertOrderWithItems(conn, customerUserId, shopId, addressId, "shipping", 
                        variantIds.get(0), 3.0, variantPrices.get(0));
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private void insertOrderWithItems(Connection conn, int userId, int shopId, int addressId, 
                                      String status, int variantId, double qty, double unitPrice) throws SQLException {
        double total = qty * unitPrice + 25000; // subtotal + ship
        String insertOrder = "INSERT INTO orders (user_id, shop_id, address_id, status, total_amount, created_at) "
                + "VALUES (?, ?, ?, ?, ?, DATEADD(minute, -30, GETDATE()))";
        int orderId = 0;
        try (PreparedStatement ps = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, shopId);
            ps.setInt(3, addressId);
            ps.setString(4, status);
            ps.setDouble(5, total);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) orderId = rs.getInt(1);
            }
        }

        if (orderId > 0) {
            String insertItem = "INSERT INTO order_items (order_id, variant_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                ps.setInt(1, orderId);
                ps.setInt(2, variantId);
                ps.setDouble(3, qty);
                ps.setDouble(4, unitPrice);
                ps.executeUpdate();
            }

            String insertHistory = "INSERT INTO order_status_history (order_id, status, changed_by, changed_at) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(insertHistory)) {
                ps.setInt(1, orderId);
                ps.setString(2, status);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
        }
    }

    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setShopId(rs.getInt("shop_id"));
        order.setAddressId(rs.getInt("address_id"));
        int voucherId = rs.getInt("voucher_id");
        if (!rs.wasNull()) {
            order.setVoucherId(voucherId);
        }
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setCreatedAt(rs.getTimestamp("created_at"));

        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerEmail(rs.getString("customer_email"));
        order.setCustomerPhone(rs.getString("customer_phone"));

        String recName = rs.getString("recipient_name");
        order.setRecipientName(recName != null && !recName.isBlank() ? recName : rs.getString("customer_name"));

        String recPhone = rs.getString("recipient_phone");
        order.setRecipientPhone(recPhone != null && !recPhone.isBlank() ? recPhone : rs.getString("customer_phone"));

        StringBuilder addr = new StringBuilder();
        String line = rs.getString("address_line");
        String ward = rs.getString("ward");
        String dist = rs.getString("district");
        String prov = rs.getString("province");
        if (line != null && !line.isBlank()) addr.append(line);
        if (ward != null && !ward.isBlank()) addr.append(addr.length() > 0 ? ", " : "").append(ward);
        if (dist != null && !dist.isBlank()) addr.append(addr.length() > 0 ? ", " : "").append(dist);
        if (prov != null && !prov.isBlank()) addr.append(addr.length() > 0 ? ", " : "").append(prov);
        order.setRecipientAddress(addr.toString());

        return order;
    }

    // =========================================================
    // CUSTOMER ORDER METHODS (FEATURE 6: 12 TASKS)
    // =========================================================

    public List<Order> getOrdersByUser(int userId, String statusFilter, String searchQuery) throws SQLException {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.order_id, o.user_id, o.shop_id, o.address_id, o.voucher_id, o.status, o.total_amount, o.created_at, "
                + "u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone, "
                + "a.recipient_name, a.phone AS recipient_phone, "
                + "a.address_line, a.ward, a.district, a.province, "
                + "s.shop_name, "
                + "p.method AS payment_method, "
                + "d.staff_id AS delivery_staff_id, du.full_name AS delivery_staff_name, du.phone AS delivery_staff_phone, d.status AS delivery_status "
                + "FROM orders o "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN user_addresses a ON o.address_id = a.address_id "
                + "LEFT JOIN shops s ON o.shop_id = s.shop_id "
                + "LEFT JOIN payments p ON o.order_id = p.order_id "
                + "LEFT JOIN deliveries d ON o.order_id = d.order_id "
                + "LEFT JOIN users du ON d.staff_id = du.user_id "
                + "WHERE o.user_id = ? "
        );

        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
            sql.append("AND o.status = ? ");
        }

        if (searchQuery != null && !searchQuery.isBlank()) {
            sql.append("AND (CAST(o.order_id AS VARCHAR) LIKE ? OR s.shop_name LIKE ? OR EXISTS ("
                    + "  SELECT 1 FROM order_items oi2 "
                    + "  JOIN product_variants pv2 ON oi2.variant_id = pv2.variant_id "
                    + "  JOIN products p2 ON pv2.product_id = p2.product_id "
                    + "  WHERE oi2.order_id = o.order_id AND p2.product_name LIKE ?"
                    + ")) ");
        }

        sql.append("ORDER BY o.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            ps.setInt(paramIndex++, userId);

            if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("all")) {
                ps.setString(paramIndex++, statusFilter.trim());
            }

            if (searchQuery != null && !searchQuery.isBlank()) {
                String kw = "%" + searchQuery.trim() + "%";
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setShopName(rs.getString("shop_name"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    int dStaffId = rs.getInt("delivery_staff_id");
                    if (!rs.wasNull()) {
                        order.setDeliveryStaffId(dStaffId);
                        order.setDeliveryStaffName(rs.getString("delivery_staff_name"));
                        order.setDeliveryStaffPhone(rs.getString("delivery_staff_phone"));
                        order.setDeliveryStatus(rs.getString("delivery_status"));
                    }
                    list.add(order);
                }
            }
        }

        for (Order order : list) {
            order.setItems(getOrderItems(order.getOrderId()));
        }

        return list;
    }

    public Order getOrderByIdForCustomer(int orderId, int userId) throws SQLException {
        String sql = "SELECT o.order_id, o.user_id, o.shop_id, o.address_id, o.voucher_id, o.status, o.total_amount, o.created_at, "
                + "u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone, "
                + "a.recipient_name, a.phone AS recipient_phone, "
                + "a.address_line, a.ward, a.district, a.province, "
                + "s.shop_name, "
                + "v.code AS voucher_code, "
                + "p.method AS payment_method, "
                + "d.staff_id AS delivery_staff_id, du.full_name AS delivery_staff_name, du.phone AS delivery_staff_phone, d.status AS delivery_status "
                + "FROM orders o "
                + "INNER JOIN users u ON o.user_id = u.user_id "
                + "LEFT JOIN user_addresses a ON o.address_id = a.address_id "
                + "LEFT JOIN shops s ON o.shop_id = s.shop_id "
                + "LEFT JOIN vouchers v ON o.voucher_id = v.voucher_id "
                + "LEFT JOIN payments p ON o.order_id = p.order_id "
                + "LEFT JOIN deliveries d ON o.order_id = d.order_id "
                + "LEFT JOIN users du ON d.staff_id = du.user_id "
                + "WHERE o.order_id = ? AND o.user_id = ?";

        Order order = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    order = mapOrderRow(rs);
                    order.setShopName(rs.getString("shop_name"));
                    order.setVoucherCode(rs.getString("voucher_code"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    int dStaffId = rs.getInt("delivery_staff_id");
                    if (!rs.wasNull()) {
                        order.setDeliveryStaffId(dStaffId);
                        order.setDeliveryStaffName(rs.getString("delivery_staff_name"));
                        order.setDeliveryStaffPhone(rs.getString("delivery_staff_phone"));
                        order.setDeliveryStatus(rs.getString("delivery_status"));
                    }
                }
            }
        }

        if (order != null) {
            order.setItems(getOrderItems(orderId));
            order.setHistoryList(getOrderHistory(orderId));
        }

        return order;
    }

    public int createOrder(int userId, int shopId, int addressId, Integer voucherId,
                           String deliveryMethod, String paymentMethod,
                           double totalAmount, double shippingFee, double discountAmount,
                           List<CartItem> items, String note) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Tạo bản ghi đơn hàng
            String insertOrderSql = "INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at) "
                    + "VALUES (?, ?, ?, ?, 'pending_confirmation', ?, GETDATE())";
            int orderId = 0;
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, shopId);
                ps.setInt(3, addressId);
                if (voucherId != null && voucherId > 0) {
                    ps.setInt(4, voucherId);
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setDouble(5, totalAmount);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            if (orderId <= 0) {
                conn.rollback();
                throw new SQLException("Không thể khởi tạo mã đơn hàng trong CSDL.");
            }

            // 2. Chèn danh sách order_items (snapshot số lượng & đơn giá)
            String insertItemSql = "INSERT INTO order_items (order_id, variant_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                for (CartItem item : items) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getVariantId());
                    ps.setDouble(3, item.getQuantity());
                    ps.setDouble(4, item.getUnitPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 3. Ghi lịch sử khởi tạo đơn hàng
            String historyStatus = "pending_confirmation";
            if (note != null && !note.isBlank()) {
                historyStatus = "pending_confirmation (Ghi chú: " + note.trim() + ")";
                if (historyStatus.length() > 50) historyStatus = historyStatus.substring(0, 50);
            }
            String insertHistSql = "INSERT INTO order_status_history (order_id, status, changed_by, changed_at) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(insertHistSql)) {
                ps.setInt(1, orderId);
                ps.setString(2, historyStatus);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }

            // 4. Khởi tạo bản ghi thanh toán
            ensurePaymentsTable(conn);
            String payMethod = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod.toLowerCase() : "cod";
            if (!List.of("cod", "vnpay", "momo", "card").contains(payMethod)) {
                payMethod = "cod";
            }
            String insertPaySql = "INSERT INTO payments (order_id, method, amount, status) VALUES (?, ?, ?, 'pending')";
            try (PreparedStatement ps = conn.prepareStatement(insertPaySql)) {
                ps.setInt(1, orderId);
                ps.setString(2, payMethod);
                ps.setDouble(3, totalAmount);
                ps.executeUpdate();
            }

            // 5. Nếu dùng voucher, trừ usage_limit
            if (voucherId != null && voucherId > 0) {
                String updateVoucherSql = "UPDATE vouchers SET usage_limit = usage_limit - 1 WHERE voucher_id = ? AND usage_limit > 0";
                try (PreparedStatement ps = conn.prepareStatement(updateVoucherSql)) {
                    ps.setInt(1, voucherId);
                    ps.executeUpdate();
                }
            }

            // 6. Xóa các sản phẩm đã mua khỏi giỏ hàng
            String clearCartSql = "DELETE ci FROM cart_items ci "
                    + "INNER JOIN carts c ON ci.cart_id = c.cart_id "
                    + "WHERE c.user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(clearCartSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            conn.commit();
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean cancelOrderCustomer(int orderId, int userId, String reason) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra trạng thái hiện tại
            String checkSql = "SELECT status FROM orders WHERE order_id = ? AND user_id = ?";
            String currentStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("status");
                    } else {
                        conn.rollback();
                        return false; // Không tìm thấy đơn hoặc không đúng chủ sở hữu
                    }
                }
            }

            if (!"pending_confirmation".equalsIgnoreCase(currentStatus) && !"preparing".equalsIgnoreCase(currentStatus)) {
                throw new IllegalStateException("Đơn hàng đang ở trạng thái '" + currentStatus + "', không thể tự hủy.");
            }

            // Cập nhật trạng thái sang cancelled
            String updateSql = "UPDATE orders SET status = 'cancelled' WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            // Ghi log vào order_status_history
            String histStatus = "cancelled (Khách hủy" + (reason != null && !reason.isBlank() ? ": " + reason.trim() : "") + ")";
            if (histStatus.length() > 50) histStatus = histStatus.substring(0, 50);

            String histSql = "INSERT INTO order_status_history (order_id, status, changed_by, changed_at) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(histSql)) {
                ps.setInt(1, orderId);
                ps.setString(2, histStatus);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<OrderItem> getPastOrderItems(int orderId, int userId) throws SQLException {
        Order order = getOrderByIdForCustomer(orderId, userId);
        if (order == null) return new ArrayList<>();
        return order.getItems();
    }

    // =========================================================
    // DELIVERY STAFF ASSIGNMENT (TASK 12)
    // =========================================================

    public List<User> getAvailableDeliveryStaff() throws SQLException {
        List<User> list = new ArrayList<>();
        ensureDeliveryStaffAndTables();

        String sql = "SELECT u.user_id, u.full_name, u.email, u.phone, u.status "
                + "FROM users u "
                + "INNER JOIN user_roles ur ON u.user_id = ur.user_id "
                + "INNER JOIN roles r ON ur.role_id = r.role_id "
                + "WHERE r.role_name = 'DeliveryStaff' AND u.status = 'active'";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();
                u.setUserID(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setStatus(rs.getString("status"));
                u.setRoleName("DeliveryStaff");
                list.add(u);
            }
        }
        return list;
    }

    public boolean assignDeliveryStaff(int orderId, int shopId, int staffId, int assignedByUserId, String note) throws SQLException {
        ensureDeliveryStaffAndTables();

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra đơn hàng thuộc shop
            String checkSql = "SELECT status FROM orders WHERE order_id = ? AND shop_id = ?";
            String currentStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("status");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Lấy tên shipper
            String staffName = "Shipper #" + staffId;
            String staffSql = "SELECT full_name FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(staffSql)) {
                ps.setInt(1, staffId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) staffName = rs.getString("full_name");
                }
            }

            // Ghi hoặc cập nhật bản ghi trong bảng deliveries
            String delSql = """
                IF EXISTS (SELECT 1 FROM deliveries WHERE order_id = ?)
                BEGIN
                    UPDATE deliveries SET staff_id = ?, status = 'assigned', note = ?, created_at = GETDATE()
                    WHERE order_id = ?;
                END
                ELSE
                BEGIN
                    INSERT INTO deliveries (order_id, staff_id, status, note, created_at)
                    VALUES (?, ?, 'assigned', ?, GETDATE());
                END
                """;
            try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, staffId);
                ps.setString(3, note != null ? note : "Phân công vận chuyển");
                ps.setInt(4, orderId);
                ps.setInt(5, orderId);
                ps.setInt(6, staffId);
                ps.setString(7, note != null ? note : "Phân công vận chuyển");
                ps.executeUpdate();
            }

            // Cập nhật trạng thái đơn sang shipping nếu đang là preparing
            if ("preparing".equalsIgnoreCase(currentStatus) || "pending_confirmation".equalsIgnoreCase(currentStatus)) {
                String updateOrder = "UPDATE orders SET status = 'shipping' WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateOrder)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }
            }

            // Ghi log lịch sử
            String histStatus = "shipping (Gán shipper: " + staffName + ")";
            if (histStatus.length() > 50) histStatus = histStatus.substring(0, 50);

            String histSql = "INSERT INTO order_status_history (order_id, status, changed_by, changed_at) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(histSql)) {
                ps.setInt(1, orderId);
                ps.setString(2, histStatus);
                ps.setInt(3, assignedByUserId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    private void ensurePaymentsTable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            String sql = """
                IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'payments')
                BEGIN
                    CREATE TABLE payments (
                        payment_id       INT IDENTITY(1,1) PRIMARY KEY,
                        order_id         INT NOT NULL UNIQUE,
                        method           NVARCHAR(20) NOT NULL,
                        amount           DECIMAL(12,2) NOT NULL,
                        status           NVARCHAR(20) NOT NULL DEFAULT 'pending',
                        transaction_ref  NVARCHAR(100),
                        paid_at          DATETIME2 NULL,
                        FOREIGN KEY (order_id) REFERENCES orders(order_id)
                    );
                END
                """;
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Warning: ensurePaymentsTable: " + e.getMessage());
        }
    }

    private void ensureDeliveryStaffAndTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            // Đảm bảo có bảng deliveries
            String createDeliveriesSql = """
                IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'deliveries')
                BEGIN
                    CREATE TABLE deliveries (
                        delivery_id      INT IDENTITY(1,1) PRIMARY KEY,
                        order_id         INT NOT NULL UNIQUE,
                        staff_id         INT NOT NULL,
                        status           NVARCHAR(20) NOT NULL DEFAULT 'assigned',
                        proof_image_url  NVARCHAR(255) NULL,
                        note             NVARCHAR(255),
                        created_at       DATETIME2 NOT NULL DEFAULT GETDATE(),
                        FOREIGN KEY (order_id) REFERENCES orders(order_id),
                        FOREIGN KEY (staff_id) REFERENCES users(user_id)
                    );
                END
                """;
            st.execute(createDeliveriesSql);

            // Đảm bảo có role DeliveryStaff
            String checkRole = "SELECT role_id FROM roles WHERE role_name = 'DeliveryStaff'";
            int deliveryRoleId = 4;
            try (ResultSet rs = st.executeQuery(checkRole)) {
                if (rs.next()) {
                    deliveryRoleId = rs.getInt(1);
                } else {
                    st.execute("INSERT INTO roles (role_id, role_name) VALUES (4, 'DeliveryStaff')");
                }
            }

            // Đảm bảo có ít nhất 1 tài khoản Shipper để phân công
            String checkShipper = "SELECT COUNT(*) FROM user_roles WHERE role_id = " + deliveryRoleId;
            try (ResultSet rs = st.executeQuery(checkShipper)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Tạo tài khoản shipper mẫu
                    String insertShipper = """
                        INSERT INTO users (full_name, email, phone, password_hash, status, created_at)
                        VALUES (N'Trần Văn Giao (Shipper Hà Nội)', 'shipper@fruit.com', '0933333333', 
                                '$2a$10$jpPtGqvfoOhlgqcBUSo8/u178eEN9hFiRLIFEF3GbSoL0AF.Qnfkm', 'active', GETDATE());
                        """;
                    st.execute(insertShipper);

                    String mapRole = "INSERT INTO user_roles (user_id, role_id) "
                            + "SELECT user_id, " + deliveryRoleId + " FROM users WHERE email = 'shipper@fruit.com'";
                    st.execute(mapRole);
                }
            }

        } catch (SQLException e) {
            System.err.println("Warning: ensureDeliveryStaffAndTables: " + e.getMessage());
        }
    }
}
