package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.service.product.CategoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Admin category management: view, add, edit and delete product
 * categories.
 */
@WebServlet("/admin/category-management")
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryService categoryService = new CategoryService();

    // =====================================================
    // GET - CATEGORY LIST
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            CategoryService.CategoryListView view =
                    categoryService.loadCategories(
                            request.getParameter("q"),
                            request.getParameter("page"));

            request.setAttribute("categories", view.getCategories());
            request.setAttribute("categoriesPage",
                    view.getCategoriesPage());
            request.setAttribute("message", request.getParameter("message"));

            request.getRequestDispatcher(
                    "/jsp/admin/admin-categories.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load categories.", e);
        }
    }

    // =====================================================
    // POST - ADD / EDIT / DELETE
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = trim(request.getParameter("action"));
        String result;

        try {
            if ("add".equals(action)) {
                result = categoryService.add(
                        request.getParameter("categoryName"),
                        request.getParameter("parentId"));

            } else if ("edit".equals(action)) {
                result = categoryService.edit(
                        request.getParameter("categoryId"),
                        request.getParameter("categoryName"),
                        request.getParameter("parentId"));

            } else if ("delete".equals(action)) {
                result = categoryService.delete(
                        request.getParameter("categoryId"));

            } else {
                result = "failed";
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to save category.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/admin/categories?message=" + result
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
