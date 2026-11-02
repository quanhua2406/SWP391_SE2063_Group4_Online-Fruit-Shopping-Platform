package com.mycompany.onlinefruitshopping.dao.report;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Data Access Object phục vụ Feature 4: Reporting & Dashboard (Admin).
 * Đảm nhiệm:
 * 1. KPI tổng quan cho Sales Dashboard.
 * 2. Báo cáo Bán hàng (Sales Reports) đa tiêu chí (ngày, shop, danh mục).
 * 3. Báo cáo Doanh thu & Chiết khấu & Phí sàn (Revenue Reports).
 * 4. Thống kê Sản phẩm (Top bán chạy, bán chậm, rating đánh giá).
 * 5. Báo cáo Tồn kho & Cảnh báo sắp hết hàng (Inventory Reports).
 * 6. Thống kê Đơn hàng theo trạng thái (Order Statistics).
 * 7. Thống kê Khách hàng & Hội viên & Loyalty Points (Customer Statistics).
 */
public class AdminReportDAO {

    // =========================================================================
    // 1. KPI TỔNG QUAN DASHBOARD (TASK 26: VIEW SALES DASHBOARD)
    // =========================================================================
    public Map<String, Object> getDashboardKPIs() throws SQLException {
        Map<String, Object> kpis = new LinkedHashMap<>();

        String sql = """
            SELECT 
                (SELECT ISNULL(SUM(total_amount), 0) FROM orders WHERE status = 'delivered') AS total_revenue,
                (SELECT COUNT(*) FROM orders) AS total_orders,
                (SELECT COUNT(*) FROM orders WHERE status = 'delivered') AS completed_orders,
                (SELECT COUNT(*) FROM orders WHERE status = 'pending_confirmation') AS pending_orders,
                (SELECT COUNT(*) FROM shops WHERE status = 'active') AS active_shops,
                (SELECT COUNT(*) FROM users u 
                 JOIN user_roles ur ON u.user_id = ur.user_id 
                 JOIN roles r ON ur.role_id = r.role_id 
                 WHERE r.role_name = 'Customer' AND u.status = 'active') AS active_customers,
                (SELECT COUNT(*) FROM products WHERE status = 'published') AS published_products,
                (SELECT ISNULL(SUM(quantity), 0) FROM inventory_batches WHERE status = 'active') AS total_stock
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                kpis.put("totalRevenue", rs.getDouble("total_revenue"));
                kpis.put("totalOrders", rs.getInt("total_orders"));
                kpis.put("completedOrders", rs.getInt("completed_orders"));
                kpis.put("pendingOrders", rs.getInt("pending_orders"));
                kpis.put("activeShops", rs.getInt("active_shops"));
                kpis.put("activeCustomers", rs.getInt("active_customers"));
                kpis.put("publishedProducts", rs.getInt("published_products"));
                kpis.put("totalStock", rs.getDouble("total_stock"));
            }
        }
        return kpis;
    }

    // =========================================================================
    // 2. TOP HOA QUẢ BÁN CHẠY (TASK 26 & TASK 29: PRODUCT STATISTICS)
    // =========================================================================
    public List<Map<String, Object>> getTopSellingFruits(int limit) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) 
                p.product_id,
                p.product_name,
                p.origin,
                c.category_name,
                s.shop_name,
                ISNULL(SUM(oi.quantity), 0) AS total_quantity,
                ISNULL(SUM(oi.quantity * oi.unit_price), 0) AS total_sales,
                COUNT(DISTINCT o.order_id) AS order_count
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.order_id
            JOIN product_variants pv ON oi.variant_id = pv.variant_id
            JOIN products p ON pv.product_id = p.product_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN shops s ON p.shop_id = s.shop_id
            WHERE o.status = 'delivered'
            GROUP BY p.product_id, p.product_name, p.origin, c.category_name, s.shop_name
            ORDER BY total_quantity DESC, total_sales DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productId", rs.getInt("product_id"));
                    item.put("productName", rs.getString("product_name"));
                    item.put("origin", rs.getString("origin"));
                    item.put("categoryName", rs.getString("category_name") != null ? rs.getString("category_name") : "Chưa phân loại");
                    item.put("shopName", rs.getString("shop_name") != null ? rs.getString("shop_name") : "FreshFruit Shop");
                    item.put("totalQuantity", rs.getDouble("total_quantity"));
                    item.put("totalSales", rs.getDouble("total_sales"));
                    item.put("orderCount", rs.getInt("order_count"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 3. BÁO CÁO BÁN HÀNG THEO KHOẢNG THỜI GIAN, SHOP, CATEGORY (TASK 27)
    // =========================================================================
    public List<Map<String, Object>> getSalesReport(LocalDate startDate, LocalDate endDate, Integer shopId, Integer categoryId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT 
                p.product_id,
                p.product_name,
                c.category_name,
                s.shop_name,
                pv.packaging_type,
                pv.unit,
                ISNULL(SUM(oi.quantity), 0) AS quantity_sold,
                ISNULL(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue,
                COUNT(DISTINCT o.order_id) AS total_orders
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.order_id
            JOIN product_variants pv ON oi.variant_id = pv.variant_id
            JOIN products p ON pv.product_id = p.product_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN shops s ON p.shop_id = s.shop_id
            WHERE o.status = 'delivered'
            """);

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            sql.append(" AND CAST(o.created_at AS DATE) >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND CAST(o.created_at AS DATE) <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }
        if (shopId != null && shopId > 0) {
            sql.append(" AND p.shop_id = ?");
            params.add(shopId);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        sql.append("""
            GROUP BY p.product_id, p.product_name, c.category_name, s.shop_name, pv.packaging_type, pv.unit
            ORDER BY total_revenue DESC, quantity_sold DESC
            """);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("productId", rs.getInt("product_id"));
                    row.put("productName", rs.getString("product_name"));
                    row.put("categoryName", rs.getString("category_name") != null ? rs.getString("category_name") : "N/A");
                    row.put("shopName", rs.getString("shop_name") != null ? rs.getString("shop_name") : "N/A");
                    row.put("packagingType", rs.getString("packaging_type") != null ? rs.getString("packaging_type") : "Tiêu chuẩn");
                    row.put("unit", rs.getString("unit") != null ? rs.getString("unit") : "kg");
                    row.put("quantitySold", rs.getDouble("quantity_sold"));
                    row.put("totalRevenue", rs.getDouble("total_revenue"));
                    row.put("totalOrders", rs.getInt("total_orders"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 4. BÁO CÁO DOANH THU & CHI TIẾT PHÍ NỀN TẢNG (TASK 28: REVENUE REPORTS)
    // =========================================================================
    public Map<String, Object> getRevenueReport(LocalDate startDate, LocalDate endDate, Integer shopId) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();

        // 4.1 Doanh thu gộp, giảm giá, hoàn tiền
        StringBuilder sql = new StringBuilder("""
            SELECT 
                ISNULL(SUM(CASE WHEN o.status = 'delivered' THEN o.total_amount ELSE 0 END), 0) AS gross_revenue,
                ISNULL(SUM(CASE WHEN o.status = 'delivered' THEN ISNULL(v.discount_value, 0) ELSE 0 END), 0) AS total_vouchers,
                ISNULL((SELECT SUM(r.amount) FROM refunds r WHERE r.status = 'approved'), 0) AS total_refunds
            FROM orders o
            LEFT JOIN vouchers v ON o.voucher_id = v.voucher_id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        if (startDate != null) {
            sql.append(" AND CAST(o.created_at AS DATE) >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND CAST(o.created_at AS DATE) <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }
        if (shopId != null && shopId > 0) {
            sql.append(" AND o.shop_id = ?");
            params.add(shopId);
        }

        double grossRevenue = 0;
        double totalVouchers = 0;
        double totalRefunds = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    grossRevenue = rs.getDouble("gross_revenue");
                    totalVouchers = rs.getDouble("total_vouchers");
                    totalRefunds = rs.getDouble("total_refunds");
                }
            }
        }

        // Tính hoa hồng nền tảng (Platform fee) dựa trên commission_rate của sản phẩm
        StringBuilder commSql = new StringBuilder("""
            SELECT 
                ISNULL(SUM(oi.quantity * oi.unit_price * (ISNULL(p.commission_rate, 5.0) / 100.0)), 0) AS platform_commission
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.order_id
            JOIN product_variants pv ON oi.variant_id = pv.variant_id
            JOIN products p ON pv.product_id = p.product_id
            WHERE o.status = 'delivered'
            """);

        List<Object> commParams = new ArrayList<>();
        if (startDate != null) {
            commSql.append(" AND CAST(o.created_at AS DATE) >= ?");
            commParams.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            commSql.append(" AND CAST(o.created_at AS DATE) <= ?");
            commParams.add(java.sql.Date.valueOf(endDate));
        }
        if (shopId != null && shopId > 0) {
            commSql.append(" AND p.shop_id = ?");
            commParams.add(shopId);
        }

        double platformCommission = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(commSql.toString())) {
            for (int i = 0; i < commParams.size(); i++) {
                ps.setObject(i + 1, commParams.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    platformCommission = rs.getDouble("platform_commission");
                }
            }
        }

        // Doanh thu thuần sàn = Hoa hồng nhận được - Hoàn tiền chi trả
        double netPlatformRevenue = Math.max(0, platformCommission - totalRefunds);
        double profitMargin = grossRevenue > 0 ? (netPlatformRevenue / grossRevenue) * 100.0 : 0.0;

        result.put("grossRevenue", grossRevenue);
        result.put("totalVouchers", totalVouchers);
        result.put("totalRefunds", totalRefunds);
        result.put("platformCommission", platformCommission);
        result.put("netPlatformRevenue", netPlatformRevenue);
        result.put("profitMargin", BigDecimal.valueOf(profitMargin).setScale(2, RoundingMode.HALF_UP).doubleValue());

        // 4.2 Phân bổ theo phương thức thanh toán
        StringBuilder payMethodSql = new StringBuilder("""
            SELECT 
                ISNULL(p.method, 'cod') AS pay_method,
                COUNT(o.order_id) AS order_count,
                ISNULL(SUM(o.total_amount), 0) AS total_amount
            FROM orders o
            LEFT JOIN payments p ON o.order_id = p.order_id
            WHERE o.status = 'delivered'
            """);
        List<Object> payParams = new ArrayList<>();
        if (startDate != null) {
            payMethodSql.append(" AND CAST(o.created_at AS DATE) >= ?");
            payParams.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            payMethodSql.append(" AND CAST(o.created_at AS DATE) <= ?");
            payParams.add(java.sql.Date.valueOf(endDate));
        }
        payMethodSql.append(" GROUP BY ISNULL(p.method, 'cod')");

        List<Map<String, Object>> paymentBreakdown = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(payMethodSql.toString())) {
            for (int i = 0; i < payParams.size(); i++) {
                ps.setObject(i + 1, payParams.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    String m = rs.getString("pay_method");
                    item.put("methodKey", m);
                    item.put("methodName", "vnpay".equalsIgnoreCase(m) ? "VNPay Sandbox" : ("cod".equalsIgnoreCase(m) ? "Tiền mặt (COD)" : m.toUpperCase()));
                    item.put("orderCount", rs.getInt("order_count"));
                    item.put("totalAmount", rs.getDouble("total_amount"));
                    paymentBreakdown.add(item);
                }
            }
        }
        result.put("paymentBreakdown", paymentBreakdown);

        return result;
    }

    // =========================================================================
    // 5. THỐNG KÊ SẢN PHẨM & ĐÁNH GIÁ (TASK 29: PRODUCT STATISTICS)
    // =========================================================================
    public Map<String, Object> getProductStatistics(int limit) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        int topLimit = limit > 0 ? limit : 6;

        // Top bán chạy
        map.put("topSelling", getTopSellingFruits(topLimit));

        // Sản phẩm bán chậm (ít lượt bán hoặc 0 lượt bán)
        List<Map<String, Object>> slowMoving = new ArrayList<>();
        String slowSql = """
            SELECT TOP (?)
                p.product_id,
                p.product_name,
                c.category_name,
                s.shop_name,
                ISNULL(SUM(oi.quantity), 0) AS total_sold,
                ISNULL((SELECT SUM(ib.quantity) FROM inventory_batches ib 
                        JOIN product_variants pv ON ib.variant_id = pv.variant_id 
                        WHERE pv.product_id = p.product_id AND ib.status = 'active'), 0) AS current_stock
            FROM products p
            LEFT JOIN product_variants pv ON p.product_id = pv.product_id
            LEFT JOIN order_items oi ON pv.variant_id = oi.variant_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN shops s ON p.shop_id = s.shop_id
            WHERE p.status = 'published'
            GROUP BY p.product_id, p.product_name, c.category_name, s.shop_name
            ORDER BY total_sold ASC, current_stock DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(slowSql)) {
            ps.setInt(1, topLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productId", rs.getInt("product_id"));
                    item.put("productName", rs.getString("product_name"));
                    item.put("categoryName", rs.getString("category_name") != null ? rs.getString("category_name") : "N/A");
                    item.put("shopName", rs.getString("shop_name") != null ? rs.getString("shop_name") : "N/A");
                    item.put("totalSold", rs.getDouble("total_sold"));
                    item.put("currentStock", rs.getDouble("current_stock"));
                    slowMoving.add(item);
                }
            }
        }
        map.put("slowMoving", slowMoving);

        // Thống kê đánh giá sản phẩm (Ratings)
        List<Map<String, Object>> ratingStats = new ArrayList<>();
        String rateSql = """
            SELECT TOP (?)
                p.product_id,
                p.product_name,
                ISNULL(AVG(CAST(r.rating AS FLOAT)), 0.0) AS avg_rating,
                COUNT(r.review_id) AS total_reviews
            FROM products p
            LEFT JOIN reviews r ON p.product_id = r.product_id AND r.status = 'published'
            WHERE p.status = 'published'
            GROUP BY p.product_id, p.product_name
            HAVING COUNT(r.review_id) > 0
            ORDER BY avg_rating DESC, total_reviews DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(rateSql)) {
            ps.setInt(1, topLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productId", rs.getInt("product_id"));
                    item.put("productName", rs.getString("product_name"));
                    double avg = rs.getDouble("avg_rating");
                    item.put("avgRating", BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
                    item.put("totalReviews", rs.getInt("total_reviews"));
                    ratingStats.add(item);
                }
            }
        }
        map.put("ratings", ratingStats);

        return map;
    }

    // =========================================================================
    // 6. BÁO CÁO TỒN KHO & CẢNH BÁO (TASK 30: INVENTORY REPORTS)
    // =========================================================================
    public Map<String, Object> getInventoryReports() throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();

        // 6.1 Tổng tồn kho và số biến thể
        String totalSql = """
            SELECT 
                (SELECT COUNT(*) FROM product_variants) AS total_variants,
                (SELECT ISNULL(SUM(quantity), 0) FROM inventory_batches WHERE status = 'active') AS total_stock_quantity
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(totalSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                map.put("totalVariants", rs.getInt("total_variants"));
                map.put("totalStockQuantity", rs.getDouble("total_stock_quantity"));
            }
        }

        // 6.2 Cảnh báo sắp hết hàng (Low Stock) dựa theo ngưỡng của shop hoặc <= 10
        String lowStockSql = """
            SELECT 
                p.product_id,
                p.product_name,
                pv.variant_id,
                pv.sku,
                pv.packaging_type,
                pv.unit,
                s.shop_name,
                ISNULL(s.low_stock_threshold, 10) AS threshold,
                ISNULL(SUM(ib.quantity), 0) AS current_stock
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            LEFT JOIN shops s ON p.shop_id = s.shop_id
            LEFT JOIN inventory_batches ib ON pv.variant_id = ib.variant_id AND ib.status = 'active'
            WHERE p.status = 'published'
            GROUP BY p.product_id, p.product_name, pv.variant_id, pv.sku, pv.packaging_type, pv.unit, s.shop_name, s.low_stock_threshold
            HAVING ISNULL(SUM(ib.quantity), 0) > 0 AND ISNULL(SUM(ib.quantity), 0) <= ISNULL(s.low_stock_threshold, 10)
            ORDER BY current_stock ASC
            """;
        List<Map<String, Object>> lowStockList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(lowStockSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("productId", rs.getInt("product_id"));
                item.put("productName", rs.getString("product_name"));
                item.put("variantId", rs.getInt("variant_id"));
                item.put("sku", rs.getString("sku"));
                item.put("packagingType", rs.getString("packaging_type"));
                item.put("unit", rs.getString("unit"));
                item.put("shopName", rs.getString("shop_name"));
                item.put("threshold", rs.getDouble("threshold"));
                item.put("currentStock", rs.getDouble("current_stock"));
                lowStockList.add(item);
            }
        }
        map.put("lowStockItems", lowStockList);

        // 6.3 Hết hàng (Out of stock = 0)
        String outStockSql = """
            SELECT 
                p.product_id,
                p.product_name,
                pv.variant_id,
                pv.sku,
                pv.packaging_type,
                pv.unit,
                s.shop_name
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            LEFT JOIN shops s ON p.shop_id = s.shop_id
            WHERE p.status = 'published'
              AND NOT EXISTS (
                  SELECT 1 FROM inventory_batches ib 
                  WHERE ib.variant_id = pv.variant_id AND ib.status = 'active' AND ib.quantity > 0
              )
            ORDER BY p.product_name ASC
            """;
        List<Map<String, Object>> outStockList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(outStockSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("productId", rs.getInt("product_id"));
                item.put("productName", rs.getString("product_name"));
                item.put("variantId", rs.getInt("variant_id"));
                item.put("sku", rs.getString("sku"));
                item.put("packagingType", rs.getString("packaging_type"));
                item.put("unit", rs.getString("unit"));
                item.put("shopName", rs.getString("shop_name"));
                outStockList.add(item);
            }
        }
        map.put("outOfStockItems", outStockList);

        // 6.4 Lô hàng cận hạn sử dụng (trong vòng 14 ngày tới)
        String expSql = """
            SELECT TOP 10
                ib.batch_id,
                ib.batch_code,
                p.product_name,
                pv.packaging_type,
                ib.quantity,
                ib.expiry_date,
                DATEDIFF(day, GETDATE(), ib.expiry_date) AS days_left
            FROM inventory_batches ib
            JOIN product_variants pv ON ib.variant_id = pv.variant_id
            JOIN products p ON pv.product_id = p.product_id
            WHERE ib.status = 'active' 
              AND ib.expiry_date IS NOT NULL 
              AND ib.expiry_date >= CAST(GETDATE() AS DATE)
              AND ib.expiry_date <= DATEADD(day, 14, CAST(GETDATE() AS DATE))
            ORDER BY ib.expiry_date ASC
            """;
        List<Map<String, Object>> expiringBatches = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(expSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("batchId", rs.getInt("batch_id"));
                item.put("batchCode", rs.getString("batch_code"));
                item.put("productName", rs.getString("product_name"));
                item.put("packagingType", rs.getString("packaging_type"));
                item.put("quantity", rs.getDouble("quantity"));
                item.put("expiryDate", rs.getDate("expiry_date"));
                item.put("daysLeft", rs.getInt("days_left"));
                expiringBatches.add(item);
            }
        }
        map.put("expiringBatches", expiringBatches);

        return map;
    }

    // =========================================================================
    // 7. THỐNG KÊ ĐƠN HÀNG (TASK 31: ORDER STATISTICS)
    // =========================================================================
    public Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                status,
                COUNT(*) AS count_orders,
                ISNULL(SUM(total_amount), 0) AS subtotal_amount
            FROM orders
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        if (startDate != null) {
            sql.append(" AND CAST(created_at AS DATE) >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND CAST(created_at AS DATE) <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }
        sql.append(" GROUP BY status");

        int totalOrders = 0;
        int completedCount = 0;
        int cancelledCount = 0;
        int pendingCount = 0;
        int shippingCount = 0;
        int preparingCount = 0;
        int failedCount = 0;

        List<Map<String, Object>> statusList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String st = rs.getString("status");
                    int cnt = rs.getInt("count_orders");
                    double amount = rs.getDouble("subtotal_amount");

                    totalOrders += cnt;
                    if ("delivered".equalsIgnoreCase(st)) completedCount += cnt;
                    else if ("cancelled".equalsIgnoreCase(st)) cancelledCount += cnt;
                    else if ("pending_confirmation".equalsIgnoreCase(st)) pendingCount += cnt;
                    else if ("shipping".equalsIgnoreCase(st)) shippingCount += cnt;
                    else if ("preparing".equalsIgnoreCase(st)) preparingCount += cnt;
                    else if ("failed".equalsIgnoreCase(st)) failedCount += cnt;

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("status", st);
                    row.put("statusName", formatStatusName(st));
                    row.put("count", cnt);
                    row.put("amount", amount);
                    statusList.add(row);
                }
            }
        }

        double completionRate = totalOrders > 0 ? ((double) completedCount / totalOrders) * 100.0 : 0.0;
        double cancellationRate = totalOrders > 0 ? ((double) cancelledCount / totalOrders) * 100.0 : 0.0;

        map.put("totalOrders", totalOrders);
        map.put("completedCount", completedCount);
        map.put("cancelledCount", cancelledCount);
        map.put("pendingCount", pendingCount);
        map.put("shippingCount", shippingCount);
        map.put("preparingCount", preparingCount);
        map.put("failedCount", failedCount);
        map.put("completionRate", BigDecimal.valueOf(completionRate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        map.put("cancellationRate", BigDecimal.valueOf(cancellationRate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        map.put("statusList", statusList);

        return map;
    }

    // =========================================================================
    // 8. THỐNG KÊ KHÁCH HÀNG (TASK 32: CUSTOMER STATISTICS)
    // =========================================================================
    public Map<String, Object> getCustomerStatistics() throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();

        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM users u JOIN user_roles ur ON u.user_id = ur.user_id JOIN roles r ON ur.role_id = r.role_id WHERE r.role_name = 'Customer') AS total_customers,
                (SELECT COUNT(*) FROM users u JOIN user_roles ur ON u.user_id = ur.user_id JOIN roles r ON ur.role_id = r.role_id WHERE r.role_name = 'Customer' AND u.created_at >= DATEADD(day, -7, GETDATE())) AS new_customers_7d,
                (SELECT COUNT(*) FROM users u JOIN user_roles ur ON u.user_id = ur.user_id JOIN roles r ON ur.role_id = r.role_id WHERE r.role_name = 'Customer' AND u.created_at >= DATEADD(day, -30, GETDATE())) AS new_customers_30d,
                (SELECT COUNT(DISTINCT user_id) FROM orders) AS active_ordering_customers,
                (SELECT COUNT(*) FROM users u JOIN user_roles ur ON u.user_id = ur.user_id JOIN roles r ON ur.role_id = r.role_id WHERE r.role_name = 'Customer' AND u.is_member = 1) AS member_customers,
                (SELECT ISNULL(SUM(points), 0) FROM loyalty_points WHERE type = 'earn') AS total_points_earned,
                (SELECT ISNULL(SUM(points), 0) FROM loyalty_points WHERE type = 'redeem') AS total_points_redeemed
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                map.put("totalCustomers", rs.getInt("total_customers"));
                map.put("newCustomers7d", rs.getInt("new_customers_7d"));
                map.put("newCustomers30d", rs.getInt("new_customers_30d"));
                map.put("activeOrderingCustomers", rs.getInt("active_ordering_customers"));
                map.put("memberCustomers", rs.getInt("member_customers"));
                map.put("totalPointsEarned", rs.getInt("total_points_earned"));
                map.put("totalPointsRedeemed", rs.getInt("total_points_redeemed"));
            }
        }

        // Top 5 khách hàng chi tiêu nhiều nhất (Top Spenders)
        String topCustSql = """
            SELECT TOP 5
                u.user_id,
                u.full_name,
                u.email,
                u.phone,
                u.is_member,
                COUNT(o.order_id) AS order_count,
                ISNULL(SUM(o.total_amount), 0) AS total_spent
            FROM users u
            JOIN orders o ON u.user_id = o.user_id
            WHERE o.status = 'delivered'
            GROUP BY u.user_id, u.full_name, u.email, u.phone, u.is_member
            ORDER BY total_spent DESC
            """;

        List<Map<String, Object>> topCustomers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(topCustSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", rs.getInt("user_id"));
                item.put("fullName", rs.getString("full_name"));
                item.put("email", rs.getString("email"));
                item.put("phone", rs.getString("phone"));
                item.put("isMember", rs.getBoolean("is_member"));
                item.put("orderCount", rs.getInt("order_count"));
                item.put("totalSpent", rs.getDouble("total_spent"));
                topCustomers.add(item);
            }
        }
        map.put("topCustomers", topCustomers);

        return map;
    }

    // Helper lấy danh sách cửa hàng cho dropdown bộ lọc
    public List<Map<String, Object>> getActiveShops() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT shop_id, shop_name FROM shops WHERE status = 'active' ORDER BY shop_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("shopId", rs.getInt("shop_id"));
                map.put("shopName", rs.getString("shop_name"));
                list.add(map);
            }
        }
        return list;
    }

    // Helper lấy danh sách danh mục cho dropdown bộ lọc
    public List<Map<String, Object>> getAllCategories() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("categoryId", rs.getInt("category_id"));
                map.put("categoryName", rs.getString("category_name"));
                list.add(map);
            }
        }
        return list;
    }

    // Helper format status name in Vietnamese
    private String formatStatusName(String status) {
        if (status == null) return "Không xác định";
        return switch (status.toLowerCase()) {
            case "pending_confirmation" -> "Chờ xác nhận";
            case "preparing" -> "Đang đóng gói";
            case "shipping" -> "Đang giao hàng";
            case "delivered" -> "Giao thành công";
            case "cancelled" -> "Đã hủy đơn";
            case "failed" -> "Giao thất bại";
            default -> status;
        };
    }
}
