package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.report.CommissionReportDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Admin "commission report": how much platform fee is collected
 * from every shop, broken down per shop and per commission rate.
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
            List<Map<String, Object>> shopReport =
                    reportDAO.getShopReport();

            List<Map<String, Object>> rateReport =
                    reportDAO.getRateReportAll();

            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalFee = BigDecimal.ZERO;

            for (Map<String, Object> row : shopReport) {
                totalRevenue = totalRevenue.add(decimal(row.get("revenue")));
                totalFee = totalFee.add(decimal(row.get("platform_fee")));
            }

            request.setAttribute("shopReport", shopReport);
            request.setAttribute("rateReport", rateReport);
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("totalFee", totalFee);
            request.setAttribute("totalEarning",
                    totalRevenue.subtract(totalFee));

            if ("csv".equalsIgnoreCase(request.getParameter("format"))) {
                writeCsv(response, shopReport, totalRevenue, totalFee);
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
     * Streams the per-shop commission report as a CSV download.
     */
    private void writeCsv(HttpServletResponse response,
                          List<Map<String, Object>> rows,
                          BigDecimal totalRevenue,
                          BigDecimal totalFee)
            throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"commission-report-by-shop.csv\"");

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
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
