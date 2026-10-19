package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Read-only view of the shop owner's products and the platform
 * commission the admin keeps on each one.
 */
@WebServlet({"/store-owner/products", "/store-owner/commission"})
public class StoreOwnerCommissionServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

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

            List<Product> products = Collections.emptyList();

            try {
                products = productDAO.getProductsByOwner(user.getUserID());
            } catch (SQLException ignored) {
                // keep an empty list rather than failing the page
            }

            String q = Filters.text(request.getParameter("q"));
            String status = Filters.text(request.getParameter("status"));

            List<Product> filtered = new ArrayList<>();

            for (Product p : products) {
                if (Filters.any(q, p.getProductName(), p.getSku())
                        && Filters.same(status, p.getStatus())) {
                    filtered.add(p);
                }
            }

            request.setAttribute("shopName", shop.getShopName());
            request.setAttribute("products", products);
            request.setAttribute("productsPage", Page.of(
                    filtered, Page.parsePage(request.getParameter("page"))));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your products and commission.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/commission.jsp"
        ).forward(request, response);
    }
}
