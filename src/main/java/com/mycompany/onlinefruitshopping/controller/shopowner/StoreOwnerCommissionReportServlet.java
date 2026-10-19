package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.report.CommissionReportDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Shop-owner "commission report": how much fee the shop has to
 * pay the platform, broken down by product and by commission
 * rate.
 */
@WebServlet("/store-owner/commission-report")
public class StoreOwnerCommissionReportServlet extends HttpServlet {

    private final CommissionReportDAO reportDAO = new CommissionReportDAO();
    private final ProductDAO productDAO = new ProductDAO();

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
            ProductDAO.ShopInfo shop =
                    productDAO.getShopForOwner(user.getUserID());

            if (shop == null) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/create-shop"
                );
                return;
            }

            int userId = user.getUserID();

            List<Map<String, Object>> productReport =
                    reportDAO.getProductReportForOwner(userId);

            List<Map<String, Object>> rateReport =
                    reportDAO.getRateReportForOwner(userId);

            List<Map<String, Object>> statusReport =
                    reportDAO.getStatusReportForOwner(userId);

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

            request.setAttribute("shopName", shop.getShopName());
            request.setAttribute("productReport", productReport);
            request.setAttribute("rateReport", rateReport);
            request.setAttribute("statusReport", statusReport);
            request.setAttribute("products", products);
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("totalFee", totalFee);
            request.setAttribute("totalEarning",
                    totalRevenue.subtract(totalFee));

            if ("csv".equalsIgnoreCase(request.getParameter("format"))) {
                writeCsv(response, shop.getShopName(), productReport,
                        totalRevenue, totalFee);
                return;
            }

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
                          String shopName,
                          List<Map<String, Object>> rows,
                          BigDecimal totalRevenue,
                          BigDecimal totalFee)
            throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"commission-report.csv\"");

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
        csv.append("Shop,").append(csv(shopName)).append('\n');
        csv.append("Product,Quantity Sold,Revenue,Commission %,Platform Fee,Shop Earning\n");

        for (Map<String, Object> row : rows) {
            csv.append(csv(row.get("product_name"))).append(',')
               .append(csv(row.get("sold_qty"))).append(',')
               .append(csv(row.get("revenue"))).append(',')
               .append(csv(row.get("commission_rate"))).append(',')
               .append(csv(row.get("platform_fee"))).append(',')
               .append(csv(row.get("shop_earning"))).append('\n');
        }

        csv.append("TOTAL,,")
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
