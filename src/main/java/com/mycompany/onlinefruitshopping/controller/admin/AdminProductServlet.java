
package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.product.AdminProductDAO;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@WebServlet("/admin/product-management")
public class AdminProductServlet extends HttpServlet {

    private final AdminProductDAO productDAO = new AdminProductDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String keyword = request.getParameter("keyword");

        try {
            List<Map<String, Object>> products =
                    productDAO.getAllProducts(keyword);

            // Category options for the filter come from the loaded
            // rows, so no extra query is needed.
            TreeSet<String> categoryOptions = new TreeSet<>();

            for (Map<String, Object> row : products) {
                if (row.get("category_name") != null) {
                    categoryOptions.add(row.get("category_name").toString());
                }
            }

            String status = Filters.text(request.getParameter("status"));
            String category = Filters.text(request.getParameter("category"));

            List<Map<String, Object>> filtered = new ArrayList<>();

            for (Map<String, Object> row : products) {
                if (Filters.same(status, row.get("status"))
                        && Filters.same(category, row.get("category_name"))) {
                    filtered.add(row);
                }
            }

            request.setAttribute("products", products);
            request.setAttribute("productsPage", Page.of(
                    filtered, Page.parsePage(request.getParameter("page"))));
            request.setAttribute("categoryOptions", categoryOptions);
            request.setAttribute(
                    "keyword",
                    keyword == null ? "" : keyword
            );

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
