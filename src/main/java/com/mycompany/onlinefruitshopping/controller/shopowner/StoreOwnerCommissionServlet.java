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
 * Read-only view of the shop owner's products and the platform
 * commission the admin keeps on each one.
 */
@WebServlet({"/store-owner/products", "/store-owner/commission"})
public class StoreOwnerCommissionServlet extends HttpServlet {

    private final CommissionReportService reportService =
            new CommissionReportService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            CommissionReportService.OwnerProductsView view =
                    reportService.loadOwnerProducts(
                            user.getUserID(),
                            request.getParameter("q"),
                            request.getParameter("status"),
                            request.getParameter("page"));

            if (view == null) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/create-shop"
                );
                return;
            }

            request.setAttribute("shopName", view.getShopName());
            request.setAttribute("products", view.getProducts());
            request.setAttribute("productsPage", view.getProductsPage());

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your products and commission.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/commission.jsp"
        ).forward(request, response);
    }
}
