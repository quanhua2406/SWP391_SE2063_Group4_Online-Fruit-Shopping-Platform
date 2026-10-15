
package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.AdminProductDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

            request.setAttribute("products", products);
            request.setAttribute(
                    "keyword",
                    keyword == null ? "" : keyword
            );

            request.getRequestDispatcher(
                    "/admin-products.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load products from database.",
                    e
            );
        }
    }
}
