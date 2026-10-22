package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.service.product.AdminProductService;

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

    private final AdminProductService productService =
            new AdminProductService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String result;

        try {
            result = productService.deleteProduct(
                    request.getParameter("productId"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not delete the product.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/admin/product-management?message=" + result
        );
    }
}
