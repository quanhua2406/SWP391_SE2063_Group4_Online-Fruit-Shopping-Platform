package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.report.CommissionReportDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Admin "commission report": how much platform fee is collected
 * from every shop, broken down per shop and per commission rate,
 * with a Shopee-style fee breakdown.
 *
 * <p>The report can be narrowed down with a date range
 * ({@code from}/{@code to}, inclusive) and an order-status slice
 * ({@code status}); both are kept on the CSV export link.</p>
 */
@WebServlet("/admin/commission-report")
public class AdminCommissionReportServlet extends HttpServlet {

    private final CommissionReportDAO reportDAO = new CommissionReportDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            // ---- Report filters (date range + order status) ----
            String from =
                    CommissionReportDAO.normalizeDate(request.getParameter("from"));
            String to =
                    CommissionReportDAO.normalizeDate(request.getParameter("to"));
            String status =
                    CommissionReportDAO.normalizeStatus(request.getParameter("status"));

            // Friendly behaviour: an inverted range is swapped.
            if (!from.isEmpty() && !to.isEmpty() && from.compareTo(to) > 0) {
                String swap = from;
                from = to;
                to = swap;
            }

            request.setAttribute("from", from);
            request.setAttribute("to", to);
            request.setAttribute("status", status);

            List<Map<String, Object>> shopReport =
                    reportDAO.getShopReport(from, to, status);

            List<Map<String, Object>> rateReport =
                    reportDAO.getRateReportAll(from, to, status);

            Map<String, Object> breakdown =
                    reportDAO.getFeeBreakdown(from, to, status);

            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalFee = BigDecimal.ZERO;

            for (Map<String, Object> row : shopReport) {
                totalRevenue = totalRevenue.add(decimal(row.get("revenue")));
                totalFee = totalFee.add(decimal(row.get("platform_fee")));
            }

            BigDecimal serviceFee =
                    totalRevenue.multiply(
                            CommissionReportDAO.SERVICE_FEE_RATE)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            request.setAttribute("shopReport", shopReport);
            request.setAttribute("rateReport", rateReport);
            request.setAttribute("breakdown", breakdown);
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("totalFee", totalFee);
            request.setAttribute("serviceFee", serviceFee);
            request.setAttribute("netPayout",
                    totalRevenue.subtract(serviceFee).subtract(totalFee));
            request.setAttribute("totalEarning",
                    totalRevenue.subtract(totalFee));

            if ("csv".equalsIgnoreCase(request.getParameter("format"))) {
                writeCsv(response, shopReport, totalRevenue, totalFee,
                        serviceFee, breakdown, from, to, status);
                return;
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load the commission report.", e);
        }

        request.getRequestDispatcher(
                "/jsp/admin/admin-commission-report.jsp"
        ).forward(request, response);
    }

    /**
     * Streams the per-shop commission report as a CSV download,
     * including the active filters and the fee breakdown.
     */
    private void writeCsv(HttpServletResponse response,
                          List<Map<String, Object>> rows,
                          BigDecimal totalRevenue,
                          BigDecimal totalFee,
                          BigDecimal serviceFee,
                          Map<String, Object> breakdown,
                          String from,
                          String to,
                          String status)
            throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"commission-report-by-shop.csv\"");

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');

        if (!from.isEmpty() || !to.isEmpty() || !status.isEmpty()) {
            csv.append("Filter,\"")
               .append(from.isEmpty() ? "..." : from)
               .append(" to ")
               .append(to.isEmpty() ? "..." : to)
               .append("; status: ")
               .append(status.isEmpty() ? "delivered (fee basis)" : status)
               .append("\"\n");
        }

        csv.append("Shop,Products,Quantity Sold,Revenue,"
                + "Avg. Commission %,Platform Fee,Paid To Shop\n");

        for (Map<String, Object> row : rows) {
            csv.append(csv(row.get("shop_name"))).append(',')
               .append(csv(row.get("product_count"))).append(',')
               .append(csv(row.get("sold_qty"))).append(',')
               .append(csv(row.get("revenue"))).append(',')
               .append(csv(row.get("average_rate"))).append(',')
               .append(csv(row.get("platform_fee"))).append(',')
               .append(csv(row.get("shop_earning"))).append('\n');
        }

        csv.append("TOTAL,,,")
           .append(csv(totalRevenue)).append(',')
           .append(',')
           .append(csv(totalFee)).append(',')
           .append(csv(totalRevenue.subtract(totalFee))).append('\n');

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
        csv.append("Paid to shops (net),")
           .append(csv(totalRevenue.subtract(serviceFee).subtract(totalFee)))
           .append('\n');

        response.getWriter().write(csv.toString());
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

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());
    }
}
