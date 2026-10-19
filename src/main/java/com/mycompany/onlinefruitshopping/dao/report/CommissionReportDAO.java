package com.mycompany.onlinefruitshopping.dao.report;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Commission ("fee paid to the platform") reporting.
 *
 * <p>The platform keeps a percentage of every sale
 * ({@code products.commission_rate}). This DAO aggregates the
 * really earned money from {@code order_items} so both sides can
 * see how much fee is owed / collected:</p>
 *
 * <ul>
 *   <li>the <b>shop owner</b> sees the fee they must pay the
 *       platform (broken down by product and by commission
 *       rate);</li>
 *   <li>the <b>admin</b> sees the fee collected from every
 *       shop.</li>
 * </ul>
 *
 * <p>Orders with the status {@code cancelled} or {@code failed}
 * are ignored.</p>
 */
public class CommissionReportDAO {

    private static final String PAID_STATUS =
            "o.status NOT IN ('cancelled', 'failed')";

    // =========================================================
    // SHOP OWNER - FEE PER PRODUCT
    // =========================================================

    public List<Map<String, Object>> getProductReportForOwner(int ownerId)
            throws SQLException {

        String sql =
                "SELECT p.product_id, p.product_name, "
                + "       ISNULL(p.commission_rate, 0) AS commission_rate, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity * oi.unit_price ELSE 0 END), 0) AS revenue "
                + "FROM products p "
                + "JOIN shops s ON s.shop_id = p.shop_id "
                + "LEFT JOIN product_variants v ON v.product_id = p.product_id "
                + "LEFT JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "LEFT JOIN orders o ON o.order_id = oi.order_id "
                + "WHERE s.owner_id = ? "
                + "GROUP BY p.product_id, p.product_name, p.commission_rate "
                + "ORDER BY p.product_name";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal revenue =
                            decimal(rs.getBigDecimal("revenue"));
                    BigDecimal rate =
                            decimal(rs.getBigDecimal("commission_rate"));

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("product_id", rs.getInt("product_id"));
                    row.put("product_name", rs.getString("product_name"));
                    row.put("commission_rate", rate);
                    row.put("sold_qty", decimal(rs.getBigDecimal("sold_qty")));
                    row.put("revenue", revenue);
                    row.put("platform_fee", fee(revenue, rate));
                    row.put("shop_earning",
                            revenue.subtract(fee(revenue, rate)));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    // =========================================================
    // SHOP OWNER - FEE BY COMMISSION RATE
    // =========================================================

    public List<Map<String, Object>> getRateReportForOwner(int ownerId)
            throws SQLException {

        String sql =
                "SELECT ISNULL(p.commission_rate, 0) AS commission_rate, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity * oi.unit_price ELSE 0 END), 0) AS revenue "
                + "FROM products p "
                + "JOIN shops s ON s.shop_id = p.shop_id "
                + "LEFT JOIN product_variants v ON v.product_id = p.product_id "
                + "LEFT JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "LEFT JOIN orders o ON o.order_id = oi.order_id "
                + "WHERE s.owner_id = ? "
                + "GROUP BY p.commission_rate "
                + "ORDER BY commission_rate";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal revenue =
                            decimal(rs.getBigDecimal("revenue"));
                    BigDecimal rate =
                            decimal(rs.getBigDecimal("commission_rate"));

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("commission_rate", rate);
                    row.put("sold_qty", decimal(rs.getBigDecimal("sold_qty")));
                    row.put("revenue", revenue);
                    row.put("platform_fee", fee(revenue, rate));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    // =========================================================
    // ADMIN - FEE COLLECTED PER SHOP
    // =========================================================

    public List<Map<String, Object>> getShopReport() throws SQLException {

        String sql =
                "SELECT s.shop_id, s.shop_name, "
                + "       COUNT(DISTINCT p.product_id) AS product_count, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity * oi.unit_price ELSE 0 END), 0) AS revenue, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity * oi.unit_price "
                + "                  * ISNULL(p.commission_rate, 0) / 100.0 "
                + "             ELSE 0 END), 0) AS platform_fee "
                + "FROM shops s "
                + "LEFT JOIN products p ON p.shop_id = s.shop_id "
                + "LEFT JOIN product_variants v ON v.product_id = p.product_id "
                + "LEFT JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "LEFT JOIN orders o ON o.order_id = oi.order_id "
                + "GROUP BY s.shop_id, s.shop_name "
                + "ORDER BY s.shop_name";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BigDecimal revenue =
                        decimal(rs.getBigDecimal("revenue"));
                BigDecimal fee =
                        decimal(rs.getBigDecimal("platform_fee"));

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("shop_id", rs.getInt("shop_id"));
                row.put("shop_name", rs.getString("shop_name"));
                row.put("product_count", rs.getInt("product_count"));
                row.put("sold_qty", decimal(rs.getBigDecimal("sold_qty")));
                row.put("revenue", revenue);
                row.put("platform_fee", fee);
                row.put("shop_earning", revenue.subtract(fee));
                row.put("average_rate", percent(revenue, fee));
                rows.add(row);
            }
        }

        return rows;
    }

    // =========================================================
    // ADMIN - FEE COLLECTED BY COMMISSION RATE
    // =========================================================

    public List<Map<String, Object>> getRateReportAll()
            throws SQLException {

        String sql =
                "SELECT ISNULL(p.commission_rate, 0) AS commission_rate, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + PAID_STATUS
                + "             THEN oi.quantity * oi.unit_price ELSE 0 END), 0) AS revenue "
                + "FROM products p "
                + "LEFT JOIN product_variants v ON v.product_id = p.product_id "
                + "LEFT JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "LEFT JOIN orders o ON o.order_id = oi.order_id "
                + "GROUP BY p.commission_rate "
                + "ORDER BY commission_rate";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BigDecimal revenue =
                        decimal(rs.getBigDecimal("revenue"));
                BigDecimal rate =
                        decimal(rs.getBigDecimal("commission_rate"));

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("commission_rate", rate);
                row.put("sold_qty", decimal(rs.getBigDecimal("sold_qty")));
                row.put("revenue", revenue);
                row.put("platform_fee", fee(revenue, rate));
                rows.add(row);
            }
        }

        return rows;
    }

    // =========================================================
    // SHOP OWNER - REVENUE BY ORDER STATUS
    // =========================================================

    /**
     * Revenue and fee grouped by order status, so the shop owner
     * can tell "money already delivered" (fee due) apart from
     * "orders still being processed" (provisional fee).
     */
    public List<Map<String, Object>> getStatusReportForOwner(int ownerId)
            throws SQLException {

        String sql =
                "SELECT o.status, "
                + "       ISNULL(SUM(oi.quantity), 0) AS sold_qty, "
                + "       ISNULL(SUM(oi.quantity * oi.unit_price), 0) AS revenue, "
                + "       ISNULL(SUM(oi.quantity * oi.unit_price "
                + "                  * ISNULL(p.commission_rate, 0) / 100.0), 0) AS platform_fee "
                + "FROM products p "
                + "JOIN shops s ON s.shop_id = p.shop_id "
                + "JOIN product_variants v ON v.product_id = p.product_id "
                + "JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "JOIN orders o ON o.order_id = oi.order_id "
                + "WHERE s.owner_id = ? "
                + "GROUP BY o.status "
                + "ORDER BY o.status";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("status", rs.getString("status"));
                    row.put("sold_qty",
                            decimal(rs.getBigDecimal("sold_qty")));
                    row.put("revenue",
                            decimal(rs.getBigDecimal("revenue")));
                    row.put("platform_fee",
                            decimal(rs.getBigDecimal("platform_fee")));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static BigDecimal decimal(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal fee(BigDecimal revenue, BigDecimal rate) {
        return revenue.multiply(rate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal percent(BigDecimal revenue, BigDecimal fee) {
        if (revenue == null || revenue.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return fee.multiply(new BigDecimal("100"))
                .divide(revenue, 2, RoundingMode.HALF_UP);
    }
}
