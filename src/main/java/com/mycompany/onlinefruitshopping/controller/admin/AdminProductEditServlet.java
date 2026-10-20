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
 * Admin product editing (details, category, commission and status).
 */
@WebServlet("/admin/product-edit")
public class AdminProductEditServlet extends HttpServlet {

    private final AdminProductService productService =
            new AdminProductService();

    // =====================================================
    // GET - EDIT FORM
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int productId = parseInt(request.getParameter("id"), -1);

        if (productId <= 0) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/product-management?message=invalid"
            );
            return;
        }

        try {
            if (productService.getProductForEdit(productId) == null) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/product-management?message=notfound"
                );
                return;
            }

            showForm(request, response, productId);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load the product.", e);
        }
    }

    // =====================================================
    // POST - SAVE
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int productId = parseInt(request.getParameter("productId"), -1);

        try {
            AdminProductService.SaveResult result =
                    productService.updateProduct(
                            request.getParameter("productId"),
                            request.getParameter("categoryId"),
                            request.getParameter("productName"),
                            request.getParameter("description"),
                            request.getParameter("origin"),
                            request.getParameter("status"),
                            request.getParameter("commissionRate"));

            if (result.isSuccess()) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/product-management?message=updated"
                );
                return;
            }

            request.setAttribute("error", result.getError());
            showForm(request, response, productId);

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not update the product.", e);
        }
    }

    private void showForm(HttpServletRequest request,
                          HttpServletResponse response,
                          int productId)
            throws ServletException, IOException {

        try {
            request.setAttribute("product",
                    productService.getProductForEdit(productId));
            request.setAttribute("categories",
                    productService.getCategories());

            request.getRequestDispatcher(
                    "/jsp/admin/admin-product-edit.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to reload the product form.", e);
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(
                    value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
