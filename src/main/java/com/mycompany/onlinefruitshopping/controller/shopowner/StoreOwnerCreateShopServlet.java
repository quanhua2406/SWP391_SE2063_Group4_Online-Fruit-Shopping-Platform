package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.shop.ShopService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Lets a shop owner create the shop linked to their account.
 *
 * <p>Only one shop per owner is allowed: if a shop already exists
 * the user is sent straight back to the dashboard.</p>
 */
@WebServlet("/store-owner/create-shop")
public class StoreOwnerCreateShopServlet extends HttpServlet {

    private final ShopService shopService;
    public StoreOwnerCreateShopServlet() { this(new ShopService()); }
    StoreOwnerCreateShopServlet(ShopService service) { this.shopService = service; }

    // =====================================================
    // GET - SHOW CREATE SHOP FORM
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            if (shopService.hasShop(user.getUserID())) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/dashboard?shop=exists"
                );
                return;
            }
        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to check your shop.", e
            );
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/create-shop.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - CREATE SHOP
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        String shopName = trim(request.getParameter("shopName"));
        String description = trim(request.getParameter("description"));

        String error = shopService.validateShop(shopName, description);

        if (error != null) {
            showForm(request, response, shopName, description, error);
            return;
        }

        try {
            boolean created = shopService.createShop(
                    user.getUserID(), shopName, description);

            if (!created) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/dashboard?shop=exists"
                );
                return;
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to create your shop.", e
            );
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/store-owner/dashboard?shop=created"
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void showForm(HttpServletRequest request,
                           HttpServletResponse response,
                           String shopName,
                           String description,
                           String error)
            throws ServletException, IOException {

        request.setAttribute("error", error);
        request.setAttribute("shopName", shopName);
        request.setAttribute("description", description);

        request.getRequestDispatcher(
                "/jsp/shopowner/create-shop.jsp"
        ).forward(request, response);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
