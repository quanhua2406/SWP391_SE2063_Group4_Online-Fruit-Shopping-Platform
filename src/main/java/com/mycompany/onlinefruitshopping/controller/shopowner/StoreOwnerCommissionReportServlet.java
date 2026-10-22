package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.report.CommissionReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Shop-owner "commission report": how much fee the shop has to
 * pay the platform, broken down by product, by commission rate and
 * by order status, with a Shopee-style fee breakdown.
 *
 * <p>The report can be narrowed down with a date range
 * ({@code from}/{@code to}, inclusive) and an order-status slice
 * ({@code status}); both are kept on the CSV export link.</p>
 */
@WebServlet("/store-owner/commission-report")
public class StoreOwnerCommissionReportServlet extends HttpServlet {

    private final CommissionReportService reportService =
            new CommissionReportService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            CommissionReportService.OwnerReport report =
                    reportService.loadOwnerReport(
                            user.getUserID(),
                            request.getParameter("from"),
                            request.getParameter("to"),
                            request.getParameter("status"),
                            request.getParameter("section"));

            if (report == null) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/create-shop"
                );
                return;
            }

            if ("csv".equalsIgnoreCase(request.getParameter("format"))) {
                writeCsv(response, report);
                return;
            }

            request.setAttribute("from", report.getFrom());
            request.setAttribute("to", report.getTo());
            request.setAttribute("status", report.getStatus());
            request.setAttribute("section", report.getSection());
            request.setAttribute("shopName", report.getShopName());
            request.setAttribute("productReport", report.getProductReport());
            request.setAttribute("rateReport", report.getRateReport());
            request.setAttribute("statusReport", report.getStatusReport());
            request.setAttribute("breakdown", report.getBreakdown());
            request.setAttribute("products", report.getProducts());
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
                "/jsp/shopowner/commission-report.jsp"
        ).forward(request, response);
    }

    /**
     * Streams the per-product commission report as a CSV download.
     */
    private void writeCsv(HttpServletResponse response,
                          CommissionReportService.OwnerReport report)
            throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"commission-report.csv\"");

        response.getWriter().write(reportService.buildOwnerCsv(report));
    }
}
