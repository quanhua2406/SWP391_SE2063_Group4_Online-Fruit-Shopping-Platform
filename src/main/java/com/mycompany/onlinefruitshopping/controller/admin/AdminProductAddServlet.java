
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
 * Admin "add product" form: creates a product together with its
 * first selling package (variant).
 */
@WebServlet("/admin/product-add")
public class AdminProductAddServlet extends HttpServlet {

    private final AdminProductService productService =
            new AdminProductService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        showForm(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            AdminProductService.SaveResult result =
                    productService.createProduct(
                            request.getParameter("shopId"),
                            request.getParameter("categoryId"),
                            request.getParameter("productName"),
                            request.getParameter("description"),
                            request.getParameter("origin"),
                            request.getParameter("sku"),
                            request.getParameter("weightValue"),
                            request.getParameter("packagingType"),
                            request.getParameter("price"),
                            request.getParameter("commissionRate"),
                            request.getParameter("imageUrl"));

            if (result.isSuccess()) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/product-management?message=created"
                );
                return;
            }

            request.setAttribute("error", result.getError());
            showForm(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not save product. Please check the database schema and constraints.",
                    e
            );
        }
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            request.setAttribute("categories", productService.getCategories());
            request.setAttribute("shops", productService.getShops());

            request.getRequestDispatcher(
                    "/jsp/admin/admin-product-form.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not load product categories or shops.",
                    e
            );
        }
    }
}
