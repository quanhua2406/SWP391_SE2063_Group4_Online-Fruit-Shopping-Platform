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
 * Admin product approval / removal from the storefront.
 *
 * <p>Approve publishes a product; hide removes an inappropriate
 * product from the catalogue.</p>
 */
@WebServlet("/admin/product-status")
public class AdminProductStatusServlet extends HttpServlet {

    private final AdminProductDAO dao = new AdminProductDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int productId = parseInt(request.getParameter("productId"), -1);
        String status = request.getParameter("status") == null
                ? ""
                : request.getParameter("status").trim();

        boolean validStatus = "published".equals(status)
                || "hidden".equals(status)
                || "draft".equals(status);

        String result;

        try {
            if (productId <= 0 || !validStatus) {
                result = "invalid";
            } else {
                result = dao.updateProductStatus(productId, status)
                        ? "status_" + status
                        : "failed";
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not change the product status.", e);
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
