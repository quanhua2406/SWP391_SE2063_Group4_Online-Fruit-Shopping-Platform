
package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.service.product.AdminProductService;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Admin product list: search, status / category filters and one page
 * of products at a time.
 */
@WebServlet("/admin/product-management")
public class AdminProductServlet extends HttpServlet {

    private final AdminProductService productService =
            new AdminProductService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            AdminProductService.ProductListView view =
                    productService.loadProducts(
                            request.getParameter("keyword"),
                            request.getParameter("status"),
                            request.getParameter("category"),
                            request.getParameter("page"));

            request.setAttribute("products", view.getProducts());
            request.setAttribute("productsPage", view.getProductsPage());
            request.setAttribute("categoryOptions", view.getCategoryOptions());
            request.setAttribute("keyword", view.getKeyword());

            // Keep the search and filters when moving between pages.
            request.setAttribute("pagerUrl", QueryUrl.keep(
                    request,
                    request.getContextPath() + "/admin/product-management",
                    "keyword",
                    "status",
                    "category"
            ));

            request.getRequestDispatcher(
                    "/jsp/admin/admin-products.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load products from database.",
                    e
            );
        }
    }
}
