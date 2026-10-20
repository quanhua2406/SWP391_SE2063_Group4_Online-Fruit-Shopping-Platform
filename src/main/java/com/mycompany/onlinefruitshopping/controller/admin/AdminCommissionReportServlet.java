package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.service.report.CommissionReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

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

    private final CommissionReportService reportService =
            new CommissionReportService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            CommissionReportService.AdminReport report =
                    reportService.loadAdminReport(
                            request.getParameter("from"),
                            request.getParameter("to"),
                            request.getParameter("status"),
                            request.getParameter("section"));

            if ("csv".equalsIgnoreCase(request.getParameter("format"))) {
                writeCsv(response, report);
                return;
            }

            request.setAttribute("from", report.getFrom());
            request.setAttribute("to", report.getTo());
            request.setAttribute("status", report.getStatus());
            request.setAttribute("section", report.getSection());
            request.setAttribute("shopReport", report.getShopReport());
            request.setAttribute("rateReport", report.getRateReport());
            request.setAttribute("breakdown", report.getBreakdown());
            request.setAttribute("totalRevenue", report.getTotalRevenue());
            request.setAttribute("totalFee", report.getTotalFee());
            request.setAttribute("serviceFee", report.getServiceFee());
            request.setAttribute("netPayout", report.getNetPayout());
            request.setAttribute("totalEarning", report.getTotalEarning());

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
                          CommissionReportService.AdminReport report)
            throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"commission-report-by-shop.csv\"");

        response.getWriter().write(reportService.buildAdminCsv(report));
    }
}
