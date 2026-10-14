package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.OrderItem;
import com.mycompany.onlinefruitshopping.model.OrderStatusHistory;
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
}
