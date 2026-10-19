package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.product.AdminProductDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Admin product removal.
 */
@WebServlet("/admin/product-delete")
public class AdminProductDeleteServlet extends HttpServlet {

    private final AdminProductDAO dao = new AdminProductDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int productId = parseInt(request.getParameter("productId"), -1);
        String result;

        try {
            if (productId <= 0) {
                result = "invalid";
            } else {
                result = dao.deleteProduct(productId);
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not delete the product.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/admin/product-management?message=" + result
        );
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
