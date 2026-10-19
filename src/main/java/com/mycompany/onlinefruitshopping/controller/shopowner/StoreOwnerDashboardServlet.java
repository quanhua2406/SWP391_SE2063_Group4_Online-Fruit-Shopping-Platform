package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;
import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@WebServlet("/store-owner/dashboard")
public class StoreOwnerDashboardServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        User user = null;

        if (session != null
                && session.getAttribute("user") instanceof User) {

            user = (User) session.getAttribute("user");
        }

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        ProductDAO.ShopInfo shop = null;
        Map<String, Object> stats = Collections.emptyMap();

        try {
            shop = productDAO.getShopForOwner(user.getUserID());

            if (shop != null) {
                stats = inventoryDAO.getDashboardStats(user.getUserID());
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your shop.",
                    e
            );
        }

        String shopStatus = request.getParameter("shop");

        request.setAttribute("ownerName", user.getFullName());
        request.setAttribute("hasShop", shop != null);
        request.setAttribute("shopName", shop == null ? null : shop.getShopName());
        request.setAttribute("shopStatus", shopStatus);
        request.setAttribute("stats", stats);

        request.getRequestDispatcher(
                "/jsp/shopowner/store-owner-dashboard.jsp"
        ).forward(request, response);
    }
}
