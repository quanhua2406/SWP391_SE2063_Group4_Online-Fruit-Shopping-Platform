package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.product.CategoryDAO;
import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin category management: view, add, edit and delete product
 * categories.
 */
@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    // =====================================================
    // GET - CATEGORY LIST
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            List<Category> categories = categoryDAO.getAllCategories();

            Map<Integer, String> names = new HashMap<>();
            for (Category category : categories) {
                names.put(category.getCategoryId(), category.getCategoryName());
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (Category category : categories) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("category_id", category.getCategoryId());
                row.put("category_name", category.getCategoryName());
                row.put("parent_name", category.getParentId() == null
                        ? null
                        : names.get(category.getParentId()));
                row.put("product_count",
                        categoryDAO.countProducts(category.getCategoryId()));
                row.put("child_count",
                        categoryDAO.countChildren(category.getCategoryId()));
                rows.add(row);
            }

            request.setAttribute("categories", rows);

            // Search filter, applied after the rows are built so the
            // parent drop-down above still lists every category.
            String q = Filters.text(request.getParameter("q"));

            List<Map<String, Object>> filtered = new ArrayList<>();

            for (Map<String, Object> row : rows) {
                if (Filters.any(q, row.get("category_name"),
                                row.get("parent_name"))) {
                    filtered.add(row);
                }
            }

            request.setAttribute("categoriesPage", Page.of(
                    filtered, Page.parsePage(request.getParameter("page"))));
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
                result = add(request);
            } else if ("edit".equals(action)) {
                result = edit(request);
            } else if ("delete".equals(action)) {
                result = delete(request);
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

    private String add(HttpServletRequest request) throws SQLException {

        String name = trim(request.getParameter("categoryName"));
        Integer parentId = parseParent(request.getParameter("parentId"));

        if (name.isEmpty() || name.length() > 100) {
            return "invalid";
        }

        if (categoryDAO.categoryNameExists(name, 0)) {
            return "duplicate";
        }

        if (parentId != null && categoryDAO.getCategoryById(parentId) == null) {
            return "invalid";
        }

        return categoryDAO.createCategory(name, parentId)
                ? "created" : "failed";
    }

    private String edit(HttpServletRequest request) throws SQLException {

        int id = parseInt(request.getParameter("categoryId"), -1);
        String name = trim(request.getParameter("categoryName"));
        Integer parentId = parseParent(request.getParameter("parentId"));

        if (id <= 0 || name.isEmpty() || name.length() > 100) {
            return "invalid";
        }

        if (parentId != null && parentId == id) {
            return "invalid";
        }

        if (categoryDAO.categoryNameExists(name, id)) {
            return "duplicate";
        }

        return categoryDAO.updateCategory(id, name, parentId)
                ? "updated" : "failed";
    }

    private String delete(HttpServletRequest request) throws SQLException {

        int id = parseInt(request.getParameter("categoryId"), -1);

        if (id <= 0) {
            return "invalid";
        }

        if (categoryDAO.countProducts(id) > 0) {
            return "in_use";
        }

        if (categoryDAO.countChildren(id) > 0) {
            return "has_children";
        }

        return categoryDAO.deleteCategory(id) ? "deleted" : "failed";
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Integer parseParent(String value) {
        String text = trim(value);

        if (text.isEmpty()) {
            return null;
        }

        try {
            int id = Integer.parseInt(text);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
