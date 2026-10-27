package com.mycompany.onlinefruitshopping.controller.product;

import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.CategoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller Quản lý Danh mục hoa quả.
 * Tuân thủ mô hình 3 tầng: Sử dụng CategoryService để xử lý toàn bộ logic nghiệp vụ và ràng buộc toàn vẹn.
 */
@WebServlet(urlPatterns = {"/store-owner/categories", "/admin/categories"})
public class CategoryServlet extends HttpServlet {

    private final CategoryService categoryService;

    public CategoryServlet() {
        this.categoryService = new CategoryService();
    }

    public CategoryServlet(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        try {
            switch (action) {
                case "delete":
                    handleDeleteCategory(request, response);
                    break;
                case "list":
                default:
                    showCategoryList(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in CategoryServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if (action == null) {
            action = "add";
        }

        try {
            switch (action) {
                case "add":
                    handleAddCategory(request, response);
                    break;
                case "edit":
                    handleEditCategory(request, response);
                    break;
                default:
                    response.sendRedirect(request.getRequestURI());
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in CategoryServlet", e);
        }
    }

    // =========================================================
    // 1. SHOW CATEGORY LIST
    // =========================================================
    private void showCategoryList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        List<Category> categories = categoryService.getAllCategoriesWithProductCount();
        List<Category> parentOptions = categoryService.getAllCategories();

        request.setAttribute("categories", categories);
        request.setAttribute("parentOptions", parentOptions);
        request.getRequestDispatcher("/categories.jsp").forward(request, response);
    }

    // =========================================================
    // 2. ADD CATEGORY (VIA SERVICE)
    // =========================================================
    private void handleAddCategory(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String categoryName = request.getParameter("categoryName");
        String parentIdStr = request.getParameter("parentId");

        Integer parentId = null;
        if (parentIdStr != null && !parentIdStr.isBlank()) {
            try {
                int pid = Integer.parseInt(parentIdStr);
                if (pid > 0) parentId = pid;
            } catch (NumberFormatException ignored) {}
        }

        Category cat = new Category();
        cat.setCategoryName(categoryName != null ? categoryName.trim() : null);
        cat.setParentId(parentId);

        try {
            categoryService.addCategory(cat);
            response.sendRedirect(request.getRequestURI() + "?success=added");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, e.getMessage());
        }
    }

    // =========================================================
    // 3. EDIT CATEGORY (VIA SERVICE)
    // =========================================================
    private void handleEditCategory(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String idStr = request.getParameter("categoryId");
        String categoryName = request.getParameter("categoryName");
        String parentIdStr = request.getParameter("parentId");

        if (idStr == null || idStr.isBlank()) {
            redirectWithError(request, response, "Mã danh mục không hợp lệ!");
            return;
        }

        int categoryId;
        try {
            categoryId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            redirectWithError(request, response, "Mã danh mục không hợp lệ!");
            return;
        }

        Integer parentId = null;
        if (parentIdStr != null && !parentIdStr.isBlank()) {
            try {
                int pid = Integer.parseInt(parentIdStr);
                if (pid > 0) parentId = pid;
            } catch (NumberFormatException ignored) {}
        }

        Category cat = new Category();
        cat.setCategoryId(categoryId);
        cat.setCategoryName(categoryName != null ? categoryName.trim() : null);
        cat.setParentId(parentId);

        try {
            categoryService.updateCategory(cat);
            response.sendRedirect(request.getRequestURI() + "?success=updated");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, e.getMessage());
        }
    }

    // =========================================================
    // 4. DELETE CATEGORY (VIA SERVICE)
    // =========================================================
    private void handleDeleteCategory(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            redirectWithError(request, response, "Mã danh mục không hợp lệ!");
            return;
        }

        int categoryId;
        try {
            categoryId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            redirectWithError(request, response, "Mã danh mục không hợp lệ!");
            return;
        }

        try {
            categoryService.deleteCategory(categoryId);
            response.sendRedirect(request.getRequestURI() + "?success=deleted");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectWithError(request, response, e.getMessage());
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getRequestURI() + "?errorMsg=" + encoded);
    }

    private User getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return (User) session.getAttribute("user");
    }
}
