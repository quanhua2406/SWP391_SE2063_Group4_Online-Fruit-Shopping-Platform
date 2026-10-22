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
 * Admin product approval / removal from the storefront.
 *
 * <p>Approve publishes a product; hide removes an inappropriate
 * product from the catalogue.</p>
 */
@WebServlet("/admin/product-status")
public class AdminProductStatusServlet extends HttpServlet {

    private final AdminProductService productService =
            new AdminProductService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String result;

        try {
            result = productService.changeStatus(
                    request.getParameter("productId"),
                    request.getParameter("status"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not change the product status.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/admin/product-management?message=" + result
        );
    }
}
