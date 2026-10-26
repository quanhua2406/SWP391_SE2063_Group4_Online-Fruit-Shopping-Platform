package com.mycompany.onlinefruitshopping.dao.report;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
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
 *       platform (broken down by product, by commission
 *       rate and by order status);</li>
 *   <li>the <b>admin</b> sees the fee collected from every
 *       shop.</li>
 * </ul>
 *
 * <p><b>Fee basis:</b> commission is counted on
 * {@code Completed} orders only (the status the delivery flow sets
 * once an order is delivered) - the money the platform has actually
 * earned. Every report can additionally be narrowed down with a
 * date range on {@code orders.order_date} ({@code from}/{@code to},
 * inclusive) and an order-status slice; the "Fee by Order Status"
 * table then shows the statuses outside that basis for reference.</p>
 *
 * <p>Orders with the status {@code Cancelled} never count towards
 * the fee; their value is reported separately as refunds in the fee
 * breakdown. Revenue always uses the historical purchase price
 * ({@code order_items.price}), never the current variant price.</p>
 */
public class CommissionReportDAO {

    /**
     * Shopee-style service fee (% of the paid revenue) the platform
     * charges on top of the commission. Display-only: it does not
     * change how the commission itself is calculated.
     */
    public static final BigDecimal SERVICE_FEE_RATE =
            new BigDecimal("2");

    /**
     * Every status an order can have in {@code orders.order_status}
     * (see DeliveryRules.orderStatus and AssignmentDAO).
     */
    public static final List<String> STATUSES = List.of(
            "Pending", "Confirmed", "Shipping", "Completed", "Cancelled");

    /** The status meaning "the money is actually earned". */
    public static final String DELIVERED_STATUS = "Completed";

    /** The statuses whose value is reported as refunds. */
    public static final List<String> REFUND_STATUSES =
            List.of("Cancelled", "Failed");

    // =========================================================
    // ORDER SCOPE (date range + status slice)
    // =========================================================

    /**
     * Normalises an order-status filter: anything outside the known
     * statuses means "no slice" (empty string is also accepted).
     */
    public static String normalizeStatus(String value) {

        String status = value == null ? "" : value.trim();

        if (status.isEmpty()) {
            return "";
        }

        for (String known : STATUSES) {
            if (known.equalsIgnoreCase(status)) {
                return known;
            }
        }

        return "";
    }

    /**
     * Normalises a {@code yyyy-MM-dd} date filter. Invalid or empty
     * input is ignored (returns an empty string) so a typo can
     * never break the report.
     */
    public static String normalizeDate(String value) {

        String date = value == null ? "" : value.trim();

        if (date.isEmpty()) {
            return "";
        }

        try {
            LocalDate.parse(date);
            return date;
        } catch (RuntimeException e) {
            return "";
        }
    }

    /**
     * The SQL fragment deciding which order lines count towards the
     * fee, plus the parameters that go with it.
     */
    private static final class Scope {

        private final String statusSql;
        private final List<Object> statusParams = new ArrayList<>();
        private final String dateSql;
        private final List<Object> dateParams = new ArrayList<>();

        Scope(String from, String to, String status) {

            if (status == null || status.isEmpty()) {
                // Default fee basis: money actually earned.
                statusSql = "o.order_status = N'" + DELIVERED_STATUS + "'";
            } else {
                String known = normalizeStatus(status);
                statusSql = "o.order_status = ?";
                statusParams.add(known.isEmpty() ? status : known);
            }

            StringBuilder dates = new StringBuilder();

            if (from != null && !from.isEmpty()) {
                dates.append(" AND o.order_date >= ?");
                dateParams.add(startOfDay(from));
            }

            if (to != null && !to.isEmpty()) {
                dates.append(" AND o.order_date < ?");
                dateParams.add(startOfDay(
                        LocalDate.parse(to).plusDays(1).toString()));
            }

            dateSql = dates.toString();
        }

        /** Condition counting towards the fee. */
        String counted() {
            return statusSql + dateSql;
        }

        List<Object> countedParams() {

            List<Object> all = new ArrayList<>(statusParams);
            all.addAll(dateParams);
            return all;
        }

        /** Condition covering the whole date range (all statuses). */
        String datesOnly() {
            return dateSql.isEmpty() ? "1 = 1" : "1 = 1" + dateSql;
        }

        List<Object> dateOnlyParams() {
            return dateParams;
        }

        private static Timestamp startOfDay(String date) {

            return Timestamp.from(LocalDate.parse(date)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

    // =========================================================
    // SHOP OWNER - FEE PER PRODUCT
    // =========================================================

    public List<Map<String, Object>> getProductReportForOwner(
            int ownerId, String from, String to, String status)
            throws SQLException {

        Scope scope = new Scope(from, to, status);

        String sql =
                "SELECT p.product_id, p.product_name, "
                + "       ISNULL(p.commission_rate, 0) AS commission_rate, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity * oi.price ELSE 0 END), 0) AS revenue "
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

            int index = bind(ps, 1, scope.countedParams());
            index = bind(ps, index, scope.countedParams());
            ps.setInt(index, ownerId);

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

    public List<Map<String, Object>> getRateReportForOwner(
            int ownerId, String from, String to, String status)
            throws SQLException {

        Scope scope = new Scope(from, to, status);

        String sql =
                "SELECT ISNULL(p.commission_rate, 0) AS commission_rate, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity * oi.price ELSE 0 END), 0) AS revenue "
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

            int index = bind(ps, 1, scope.countedParams());
            index = bind(ps, index, scope.countedParams());
            ps.setInt(index, ownerId);

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

    public List<Map<String, Object>> getShopReport(
            String from, String to, String status)
            throws SQLException {

        Scope scope = new Scope(from, to, status);

        String sql =
                "SELECT s.shop_id, s.shop_name, "
                + "       COUNT(DISTINCT p.product_id) AS product_count, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity * oi.price ELSE 0 END), 0) AS revenue, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity * oi.price "
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
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = bind(ps, 1, scope.countedParams());
            index = bind(ps, index, scope.countedParams());
            bind(ps, index, scope.countedParams());

            try (ResultSet rs = ps.executeQuery()) {
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
        }

        return rows;
    }

    // =========================================================
    // ADMIN - FEE COLLECTED BY COMMISSION RATE
    // =========================================================

    public List<Map<String, Object>> getRateReportAll(
            String from, String to, String status)
            throws SQLException {

        Scope scope = new Scope(from, to, status);

        String sql =
                "SELECT ISNULL(p.commission_rate, 0) AS commission_rate, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity ELSE 0 END), 0) AS sold_qty, "
                + "       ISNULL(SUM(CASE WHEN " + scope.counted()
                + "             THEN oi.quantity * oi.price ELSE 0 END), 0) AS revenue "
                + "FROM products p "
                + "LEFT JOIN product_variants v ON v.product_id = p.product_id "
                + "LEFT JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "LEFT JOIN orders o ON o.order_id = oi.order_id "
                + "GROUP BY p.commission_rate "
                + "ORDER BY commission_rate";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = bind(ps, 1, scope.countedParams());
            bind(ps, index, scope.countedParams());

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
    // REVENUE BY ORDER STATUS (reference table)
    // =========================================================

    /**
     * Revenue and fee grouped by order status, so both sides can
     * tell "money already delivered" (fee due) apart from "orders
     * still being processed" and from refunded orders.
     *
     * <p>This table is a reference view: it lists every status
     * inside the selected date range (and the selected status
     * slice, when one was chosen).</p>
     */
    public List<Map<String, Object>> getStatusReportForOwner(
            int ownerId, String from, String to, String status)
            throws SQLException {

        Scope scope = new Scope(from, to, status);

        StringBuilder where = new StringBuilder("WHERE s.owner_id = ?");

        where.append(scope.dateSql);

        if (status != null && !status.isEmpty()) {
            where.append(" AND o.order_status = ?");
        }

        String sql =
                "SELECT o.order_status AS status, "
                + "       ISNULL(SUM(oi.quantity), 0) AS sold_qty, "
                + "       ISNULL(SUM(oi.quantity * oi.price), 0) AS revenue, "
                + "       ISNULL(SUM(oi.quantity * oi.price "
                + "                  * ISNULL(p.commission_rate, 0) / 100.0), 0) AS platform_fee "
                + "FROM products p "
                + "JOIN shops s ON s.shop_id = p.shop_id "
                + "JOIN product_variants v ON v.product_id = p.product_id "
                + "JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "JOIN orders o ON o.order_id = oi.order_id "
                + where
                + " GROUP BY o.order_status "
                + "ORDER BY o.order_status";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;

            ps.setInt(index++, ownerId);
            index = bind(ps, index, scope.dateOnlyParams());

            if (status != null && !status.isEmpty()) {
                ps.setString(index, status);
            }

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
    // FEE BREAKDOWN (Shopee-style settlement)
    // =========================================================

    /**
     * Settlement figures for one shop in the selected period:
     * gross sales, promotional discount, refunds (cancelled /
     * failed orders) and the paid revenue the commission is based
     * on. The service fee and the net payout are derived from this
     * in the servlet.
     */
    public Map<String, Object> getFeeBreakdownForOwner(
            int ownerId, String from, String to, String status)
            throws SQLException {

        Scope scope = new Scope(from, to, status);

        String sql = expandBreakdown(SELECT_BREAKDOWN, scope)
                + "FROM products p "
                + "JOIN shops s ON s.shop_id = p.shop_id "
                + "JOIN product_variants v ON v.product_id = p.product_id "
                + "JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "JOIN orders o ON o.order_id = oi.order_id "
                + "WHERE s.owner_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = bindBreakdownParams(ps, scope);
            ps.setInt(index, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return readBreakdown(rs);
            }
        }
    }

    /**
     * The same settlement figures across every shop (admin view).
     */
    public Map<String, Object> getFeeBreakdown(
            String from, String to, String status)
            throws SQLException {

        Scope scope = new Scope(from, to, status);

        String sql = expandBreakdown(SELECT_BREAKDOWN, scope)
                + "FROM products p "
                + "JOIN product_variants v ON v.product_id = p.product_id "
                + "JOIN order_items oi ON oi.variant_id = v.variant_id "
                + "JOIN orders o ON o.order_id = oi.order_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = bindBreakdownParams(ps, scope);

            try (ResultSet rs = ps.executeQuery()) {
                return readBreakdown(rs);
            }
        }
    }

    private static final String SELECT_BREAKDOWN =
            "SELECT "
            + "  ISNULL(SUM(CASE WHEN {counted} "
            + "        THEN oi.quantity * oi.price END), 0) AS revenue, "
            + "  ISNULL(SUM(CASE WHEN {counted} THEN "
            + "        CASE WHEN v.price > oi.price "
            + "             THEN oi.quantity * (v.price - oi.price) "
            + "             ELSE 0 END END), 0) AS promotional_discount, "
            + "  ISNULL(SUM(CASE WHEN {dates} "
            + "        AND o.order_status IN (N'" + String.join("', N'", REFUND_STATUSES) + "') "
            + "        THEN oi.quantity * oi.price END), 0) AS refunds, "
            + "  ISNULL(SUM(CASE WHEN {dates} "
            + "        THEN oi.quantity * oi.price END), 0) AS gross_sales ";

    private static int bindBreakdownParams(PreparedStatement ps,
                                           Scope scope)
            throws SQLException {

        int index = bind(ps, 1, scope.countedParams());
        index = bind(ps, index, scope.countedParams());
        index = bind(ps, index, scope.dateOnlyParams());
        return bind(ps, index, scope.dateOnlyParams());
    }

    /**
     * Replaces the {@code {counted}} / {@code {dates}} placeholders
     * of the breakdown template with the real conditions.
     */
    private static String expandBreakdown(String template, Scope scope) {

        return template
                .replace("{counted}", scope.counted())
                .replace("{dates}", scope.datesOnly());
    }

    private static Map<String, Object> readBreakdown(ResultSet rs)
            throws SQLException {

        Map<String, Object> row = new LinkedHashMap<>();

        if (rs.next()) {
            row.put("revenue", decimal(rs.getBigDecimal("revenue")));
            row.put("promotional_discount",
                    decimal(rs.getBigDecimal("promotional_discount")));
            row.put("refunds", decimal(rs.getBigDecimal("refunds")));
            row.put("gross_sales", decimal(rs.getBigDecimal("gross_sales")));
        }

        return row;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * Binds every value starting at the given parameter index and
     * returns the next free index.
     */
    private static int bind(PreparedStatement ps,
                            int index,
                            List<Object> values)
            throws SQLException {

        for (Object value : values) {

            if (value instanceof Timestamp) {
                ps.setTimestamp(index, (Timestamp) value);
            } else if (value instanceof BigDecimal) {
                ps.setBigDecimal(index, (BigDecimal) value);
            } else {
                ps.setString(index, value.toString());
            }

            index++;
        }

        return index;
    }

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
