package com.mycompany.onlinefruitshopping.service.report;

import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.report.CommissionReportDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Business logic of the commission reports.
 *
 * <p>A report shows how much platform fee is collected: the shop
 * owner sees the fee their own shop pays, the administrator sees
 * the fee of every shop. The basis is the <b>Completed</b> orders
 * only, with a Shopee-style fee breakdown (service fee on top of
 * the product commission).</p>
 */
public class CommissionReportService {

    private final CommissionReportDAO reportDAO = new CommissionReportDAO();
    private final ProductDAO productDAO = new ProductDAO();

    // =========================================================
    // VIEW MODELS
    // =========================================================

    /**
     * The shop owner's commission report.
     */
    public static class OwnerReport {

        private final String from;
        private final String to;
        private final String status;
        private final String section;
        private final String shopName;
        private final List<Map<String, Object>> productReport;
        private final List<Map<String, Object>> rateReport;
        private final List<Map<String, Object>> statusReport;
        private final Map<String, Object> breakdown;
        private final List<Product> products;
        private final BigDecimal totalRevenue;
        private final BigDecimal totalFee;
        private final BigDecimal serviceFee;
        private final BigDecimal netPayout;
        private final BigDecimal totalEarning;

        OwnerReport(String from,
                    String to,
                    String status,
                    String section,
                    String shopName,
                    List<Map<String, Object>> productReport,
                    List<Map<String, Object>> rateReport,
                    List<Map<String, Object>> statusReport,
                    Map<String, Object> breakdown,
                    List<Product> products,
                    BigDecimal totalRevenue,
                    BigDecimal totalFee,
                    BigDecimal serviceFee,
                    BigDecimal netPayout,
                    BigDecimal totalEarning) {
            this.from = from;
            this.to = to;
            this.status = status;
            this.section = section;
            this.shopName = shopName;
            this.productReport = productReport;
            this.rateReport = rateReport;
            this.statusReport = statusReport;
            this.breakdown = breakdown;
            this.products = products;
            this.totalRevenue = totalRevenue;
            this.totalFee = totalFee;
            this.serviceFee = serviceFee;
            this.netPayout = netPayout;
            this.totalEarning = totalEarning;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getStatus() {
            return status;
        }

        public String getSection() {
            return section;
        }

        public String getShopName() {
            return shopName;
        }

        public List<Map<String, Object>> getProductReport() {
            return productReport;
        }

        public List<Map<String, Object>> getRateReport() {
            return rateReport;
        }

        public List<Map<String, Object>> getStatusReport() {
            return statusReport;
        }

        public Map<String, Object> getBreakdown() {
            return breakdown;
        }

        public List<Product> getProducts() {
            return products;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public BigDecimal getTotalFee() {
            return totalFee;
        }

        public BigDecimal getServiceFee() {
            return serviceFee;
        }

        public BigDecimal getNetPayout() {
            return netPayout;
        }

        public BigDecimal getTotalEarning() {
            return totalEarning;
        }
    }

    /**
     * The administrator's commission report.
     */
    public static class AdminReport {

        private final String from;
        private final String to;
        private final String status;
        private final String section;
        private final List<Map<String, Object>> shopReport;
        private final List<Map<String, Object>> rateReport;
        private final Map<String, Object> breakdown;
        private final BigDecimal totalRevenue;
        private final BigDecimal totalFee;
        private final BigDecimal serviceFee;
        private final BigDecimal netPayout;
        private final BigDecimal totalEarning;

        AdminReport(String from,
                    String to,
                    String status,
                    String section,
                    List<Map<String, Object>> shopReport,
                    List<Map<String, Object>> rateReport,
                    Map<String, Object> breakdown,
                    BigDecimal totalRevenue,
                    BigDecimal totalFee,
                    BigDecimal serviceFee,
                    BigDecimal netPayout,
                    BigDecimal totalEarning) {
            this.from = from;
            this.to = to;
            this.status = status;
            this.section = section;
            this.shopReport = shopReport;
            this.rateReport = rateReport;
            this.breakdown = breakdown;
            this.totalRevenue = totalRevenue;
            this.totalFee = totalFee;
            this.serviceFee = serviceFee;
            this.netPayout = netPayout;
            this.totalEarning = totalEarning;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getStatus() {
            return status;
        }

        public String getSection() {
            return section;
        }

        public List<Map<String, Object>> getShopReport() {
            return shopReport;
        }

        public List<Map<String, Object>> getRateReport() {
            return rateReport;
        }

        public Map<String, Object> getBreakdown() {
            return breakdown;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public BigDecimal getTotalFee() {
            return totalFee;
        }

        public BigDecimal getServiceFee() {
            return serviceFee;
        }

        public BigDecimal getNetPayout() {
            return netPayout;
        }

        public BigDecimal getTotalEarning() {
            return totalEarning;
        }
    }

    // =========================================================
    // OWNER REPORT
    // =========================================================

    /**
     * Loads the whole owner report in one go.
     *
     * @return the report, or {@code null} when the owner has no shop
     *         yet (the controller then sends them to create one)
     */
    public OwnerReport loadOwnerReport(int userId,
                                       String from,
                                       String to,
                                       String status,
                                       String section)
            throws SQLException {

        String shopName = ownerShopName(userId);

        if (shopName == null) {
            return null;
        }

        // ---- Report filters (date range + order status) ----
        String rangeFrom = CommissionReportDAO.normalizeDate(from);
        String rangeTo = CommissionReportDAO.normalizeDate(to);
        String statusFilter =
                CommissionReportDAO.normalizeStatus(status);

        // Friendly behaviour: an inverted range is swapped.
        if (!rangeFrom.isEmpty() && !rangeTo.isEmpty()
                && rangeFrom.compareTo(rangeTo) > 0) {
            String swap = rangeFrom;
            rangeFrom = rangeTo;
            rangeTo = swap;
        }

        String sectionFilter = normalizeSection(section,
                "overview", "products", "schedule", "rates", "status");

        List<Map<String, Object>> productReport =
                reportDAO.getProductReportForOwner(
                        userId, rangeFrom, rangeTo, statusFilter);

        List<Map<String, Object>> rateReport =
                reportDAO.getRateReportForOwner(
                        userId, rangeFrom, rangeTo, statusFilter);

        List<Map<String, Object>> statusReport =
                reportDAO.getStatusReportForOwner(
                        userId, rangeFrom, rangeTo, statusFilter);

        Map<String, Object> breakdown =
                reportDAO.getFeeBreakdownForOwner(
                        userId, rangeFrom, rangeTo, statusFilter);

        List<Product> products = Collections.emptyList();

        try {
            products = productDAO.getProductsByOwner(userId);
        } catch (SQLException ignored) {
            // keep the report even when the price list fails
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO;

        for (Map<String, Object> row : productReport) {
            totalRevenue = totalRevenue.add(decimal(row.get("revenue")));
            totalFee = totalFee.add(decimal(row.get("platform_fee")));
        }

        BigDecimal serviceFee = serviceFee(totalRevenue);

        return new OwnerReport(
                rangeFrom, rangeTo, statusFilter, sectionFilter, shopName,
                productReport, rateReport, statusReport, breakdown, products,
                totalRevenue, totalFee, serviceFee,
                totalRevenue.subtract(serviceFee).subtract(totalFee),
                totalRevenue.subtract(totalFee));
    }

    /**
     * The shop owner's products and the commission the admin keeps
     * on each one.
     *
     * @return the view, or {@code null} when the owner has no shop
     */
    public static class OwnerProductsView {

        private final String shopName;
        private final List<Product> products;
        private final Page<Product> productsPage;

        OwnerProductsView(String shopName,
                          List<Product> products,
                          Page<Product> productsPage) {
            this.shopName = shopName;
            this.products = products;
            this.productsPage = productsPage;
        }

        public String getShopName() {
            return shopName;
        }

        public List<Product> getProducts() {
            return products;
        }

        public Page<Product> getProductsPage() {
            return productsPage;
        }
    }

    public OwnerProductsView loadOwnerProducts(int userId,
                                               String q,
                                               String status,
                                               String page)
            throws SQLException {

        String shopName = ownerShopName(userId);

        if (shopName == null) {
            return null;
        }

        List<Product> products = Collections.emptyList();

        try {
            products = productDAO.getProductsByOwner(userId);
        } catch (SQLException ignored) {
            // keep an empty list rather than failing the page
        }

        String search = Filters.text(q);
        String statusFilter = Filters.text(status);

        List<Product> filtered = new ArrayList<>();

        for (Product p : products) {
            if (Filters.any(search, p.getProductName(), p.getSku())
                    && Filters.same(statusFilter, p.getStatus())) {
                filtered.add(p);
            }
        }

        return new OwnerProductsView(
                shopName, products, Page.of(filtered, Page.parsePage(page)));
    }

    // =========================================================
    // ADMIN REPORT
    // =========================================================

    public AdminReport loadAdminReport(String from,
                                       String to,
                                       String status,
                                       String section)
            throws SQLException {

        // ---- Report filters (date range + order status) ----
        String rangeFrom = CommissionReportDAO.normalizeDate(from);
        String rangeTo = CommissionReportDAO.normalizeDate(to);
        String statusFilter =
                CommissionReportDAO.normalizeStatus(status);

        // Friendly behaviour: an inverted range is swapped.
        if (!rangeFrom.isEmpty() && !rangeTo.isEmpty()
                && rangeFrom.compareTo(rangeTo) > 0) {
            String swap = rangeFrom;
            rangeFrom = rangeTo;
            rangeTo = swap;
        }

        String sectionFilter = normalizeSection(section,
                "overview", "shops", "rates");

        List<Map<String, Object>> shopReport =
                reportDAO.getShopReport(rangeFrom, rangeTo, statusFilter);

        List<Map<String, Object>> rateReport =
                reportDAO.getRateReportAll(rangeFrom, rangeTo, statusFilter);

        Map<String, Object> breakdown =
                reportDAO.getFeeBreakdown(rangeFrom, rangeTo, statusFilter);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO;

        for (Map<String, Object> row : shopReport) {
            totalRevenue = totalRevenue.add(decimal(row.get("revenue")));
            totalFee = totalFee.add(decimal(row.get("platform_fee")));
        }

        BigDecimal serviceFee = serviceFee(totalRevenue);

        return new AdminReport(
                rangeFrom, rangeTo, statusFilter, sectionFilter,
                shopReport, rateReport, breakdown,
                totalRevenue, totalFee, serviceFee,
                totalRevenue.subtract(serviceFee).subtract(totalFee),
                totalRevenue.subtract(totalFee));
    }

    // =========================================================
    // CSV EXPORTS
    // =========================================================

    /**
     * Builds the per-product commission report of one shop as a CSV
     * download, including the active filters and the fee breakdown.
     */
    public String buildOwnerCsv(OwnerReport report) {

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
        csv.append("Shop,").append(csv(report.shopName)).append('\n');

        appendFilterLine(csv, report.from, report.to, report.status);

        csv.append("Product,Quantity Sold,Revenue,Commission %,"
                + "Platform Fee,Shop Earning\n");

        for (Map<String, Object> row : report.productReport) {
            csv.append(csv(row.get("product_name"))).append(',')
               .append(csv(row.get("sold_qty"))).append(',')
               .append(csv(row.get("revenue"))).append(',')
               .append(csv(row.get("commission_rate"))).append(',')
               .append(csv(row.get("platform_fee"))).append(',')
               .append(csv(row.get("shop_earning"))).append('\n');
        }

        csv.append("TOTAL,,")
           .append(csv(report.totalRevenue)).append(',')
           .append(',')
           .append(csv(report.totalFee)).append(',')
           .append(csv(report.totalRevenue.subtract(report.totalFee)))
           .append('\n');

        appendBreakdown(csv, report.totalRevenue, report.totalFee,
                report.serviceFee, report.breakdown,
                "Net payout to shop,");

        return csv.toString();
    }

    /**
     * Builds the per-shop commission report of every shop as a CSV
     * download, including the active filters and the fee breakdown.
     */
    public String buildAdminCsv(AdminReport report) {

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');

        appendFilterLine(csv, report.from, report.to, report.status);

        csv.append("Shop,Products,Quantity Sold,Revenue,"
                + "Avg. Commission %,Platform Fee,Paid To Shop\n");

        for (Map<String, Object> row : report.shopReport) {
            csv.append(csv(row.get("shop_name"))).append(',')
               .append(csv(row.get("product_count"))).append(',')
               .append(csv(row.get("sold_qty"))).append(',')
               .append(csv(row.get("revenue"))).append(',')
               .append(csv(row.get("average_rate"))).append(',')
               .append(csv(row.get("platform_fee"))).append(',')
               .append(csv(row.get("shop_earning"))).append('\n');
        }

        csv.append("TOTAL,,,")
           .append(csv(report.totalRevenue)).append(',')
           .append(',')
           .append(csv(report.totalFee)).append(',')
           .append(csv(report.totalRevenue.subtract(report.totalFee)))
           .append('\n');

        appendBreakdown(csv, report.totalRevenue, report.totalFee,
                report.serviceFee, report.breakdown,
                "Paid to shops (net),");

        return csv.toString();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String ownerShopName(int userId) throws SQLException {

        ProductDAO.ShopInfo shop = productDAO.getShopForOwner(userId);

        return shop == null ? null : shop.getShopName();
    }

    /**
     * The platform service fee: a percentage of the paid revenue.
     */
    private BigDecimal serviceFee(BigDecimal totalRevenue) {

        return totalRevenue.multiply(CommissionReportDAO.SERVICE_FEE_RATE)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private static void appendFilterLine(StringBuilder csv,
                                         String from,
                                         String to,
                                         String status) {

        if (!from.isEmpty() || !to.isEmpty() || !status.isEmpty()) {
            csv.append("Filter,\"")
               .append(from.isEmpty() ? "..." : from)
               .append(" to ")
               .append(to.isEmpty() ? "..." : to)
               .append("; status: ")
               .append(status.isEmpty()
                       ? CommissionReportDAO.DELIVERED_STATUS + " (fee basis)"
                       : status)
               .append("\"\n");
        }
    }

    private static void appendBreakdown(StringBuilder csv,
                                        BigDecimal totalRevenue,
                                        BigDecimal totalFee,
                                        BigDecimal serviceFee,
                                        Map<String, Object> breakdown,
                                        String netLabel) {

        csv.append('\n');
        csv.append("Fee Breakdown\n");
        csv.append("Gross sales,").append(csv(breakdown.get("gross_sales"))).append('\n');
        csv.append("Promotional discount,")
           .append(csv(breakdown.get("promotional_discount"))).append('\n');
        csv.append("Refunds (cancelled/failed),")
           .append(csv(breakdown.get("refunds"))).append('\n');
        csv.append("Paid revenue (commission basis),")
           .append(csv(totalRevenue)).append('\n');
        csv.append("Service fee (")
           .append(CommissionReportDAO.SERVICE_FEE_RATE.toPlainString())
           .append(" %),").append(csv(serviceFee)).append('\n');
        csv.append("Platform commission,").append(csv(totalFee)).append('\n');
        csv.append(netLabel)
           .append(csv(totalRevenue.subtract(serviceFee).subtract(totalFee)))
           .append('\n');
    }

    private static String csv(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString();

        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return '"' + text.replace("\"", "\"\"") + '"';
        }

        return text;
    }

    private static String normalizeSection(String value, String... allowed) {
        String section = value == null ? "" : value.trim().toLowerCase();

        for (String s : allowed) {
            if (s.equals(section)) {
                return section;
            }
        }

        return allowed[0];
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());
    }
}
