package com.mycompany.onlinefruitshopping.controller.shopowner;

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

/**
 * Lets a shop owner create the shop linked to their account.
 *
 * <p>Only one shop per owner is allowed: if a shop already exists
 * the user is sent straight back to the dashboard.</p>
 */
@WebServlet("/store-owner/create-shop")
public class StoreOwnerCreateShopServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    // =====================================================
    // GET - SHOW CREATE SHOP FORM
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User user = currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            if (productDAO.getShopForOwner(user.getUserID()) != null) {
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

        User user = currentUser(request, response);

        if (user == null) {
            return;
        }

        String shopName = trim(request.getParameter("shopName"));
        String description = trim(request.getParameter("description"));

        String error = validate(shopName, description);

        if (error != null) {
            showForm(request, response, shopName, description, error);
            return;
        }

        try {
            boolean created = productDAO.createShopForOwner(
                    user.getUserID(),
                    shopName,
                    description
            );

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

    private User currentUser(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("user") instanceof User) {

            return (User) session.getAttribute("user");
        }

        response.sendRedirect(
                request.getContextPath() + "/login"
        );

        return null;
    }

    private String validate(String shopName, String description) {

        if (shopName == null || shopName.isBlank()) {
            return "Shop name cannot be empty.";
        }

        if (shopName.length() < 2 || shopName.length() > 150) {
            return "Shop name must be between 2 and 150 characters.";
        }

        if (description != null && description.length() > 255) {
            return "Description cannot exceed 255 characters.";
        }

        return null;
    }

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
