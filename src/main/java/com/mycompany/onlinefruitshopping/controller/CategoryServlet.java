package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.CategoryDAO;
import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.model.User;

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

@WebServlet(urlPatterns = {"/store-owner/categories", "/admin/categories"})
public class CategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

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

        List<Category> categories = categoryDAO.getAllCategoriesWithProductCount();
        List<Category> parentOptions = categoryDAO.getAllCategories();

        request.setAttribute("categories", categories);
        request.setAttribute("parentOptions", parentOptions);
        request.getRequestDispatcher("/categories.jsp").forward(request, response);
    }

    // =========================================================
    // 2. ADD CATEGORY (WITH VALIDATION)
    // =========================================================
    private void handleAddCategory(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String categoryName = request.getParameter("categoryName");
        String parentIdStr = request.getParameter("parentId");

        // Validation 1: Blank or empty check
        if (categoryName == null || categoryName.trim().isEmpty()) {
            redirectWithError(request, response, "Tên danh mục không được để trống!");
            return;
        }

        String trimmedName = categoryName.trim();

        // Validation 2: Length check
        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            redirectWithError(request, response, "Tên danh mục phải từ 2 đến 100 ký tự!");
            return;
        }

        // Validation 3: Duplicate name check (case-insensitive)
        if (categoryDAO.isCategoryNameExists(trimmedName, null)) {
            redirectWithError(request, response, "Danh mục '" + trimmedName + "' đã tồn tại trong hệ thống!");
            return;
        }

        Integer parentId = null;
        if (parentIdStr != null && !parentIdStr.isBlank()) {
            try {
                int pid = Integer.parseInt(parentIdStr);
                if (pid > 0) {
                    parentId = pid;
                }
            } catch (NumberFormatException ignored) {}
        }

        Category cat = new Category();
        cat.setCategoryName(trimmedName);
        cat.setParentId(parentId);

        categoryDAO.addCategory(cat);
        response.sendRedirect(request.getRequestURI() + "?success=added");
    }

    // =========================================================
    // 3. EDIT CATEGORY (WITH VALIDATION)
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

        Category existing = categoryDAO.getCategoryById(categoryId);
        if (existing == null) {
            redirectWithError(request, response, "Không tìm thấy danh mục cần cập nhật!");
            return;
        }

        if (categoryName == null || categoryName.trim().isEmpty()) {
            redirectWithError(request, response, "Tên danh mục không được để trống!");
            return;
        }

        String trimmedName = categoryName.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            redirectWithError(request, response, "Tên danh mục phải từ 2 đến 100 ký tự!");
            return;
        }

        if (categoryDAO.isCategoryNameExists(trimmedName, categoryId)) {
            redirectWithError(request, response, "Tên danh mục '" + trimmedName + "' đã trùng với danh mục khác!");
            return;
        }

        Integer parentId = null;
        if (parentIdStr != null && !parentIdStr.isBlank()) {
            try {
                int pid = Integer.parseInt(parentIdStr);
                if (pid > 0) {
                    // Prevent circular reference: parentId cannot be self
                    if (pid == categoryId) {
                        redirectWithError(request, response, "Danh mục không thể làm danh mục cha của chính nó!");
                        return;
                    }
                    parentId = pid;
                }
            } catch (NumberFormatException ignored) {}
        }

        Category cat = new Category();
        cat.setCategoryId(categoryId);
        cat.setCategoryName(trimmedName);
        cat.setParentId(parentId);

        categoryDAO.updateCategory(cat);
        response.sendRedirect(request.getRequestURI() + "?success=updated");
    }

    // =========================================================
    // 4. DELETE CATEGORY (WITH STRICT RELATIONAL VALIDATION)
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

        Category cat = categoryDAO.getCategoryById(categoryId);
        if (cat == null) {
            redirectWithError(request, response, "Không tìm thấy danh mục cần xóa!");
            return;
        }

        // Relational Check 1: Check if any products are assigned to this category
        int productCount = categoryDAO.countProductsByCategoryId(categoryId);
        if (productCount > 0) {
            redirectWithError(request, response, "Không thể xóa danh mục '" + cat.getCategoryName() 
                    + "' vì đang có " + productCount + " sản phẩm trực thuộc! Vui lòng chuyển hoặc xóa sản phẩm trước.");
            return;
        }

        // Relational Check 2: Check if any subcategories belong to this category
        int subCount = categoryDAO.countSubCategories(categoryId);
        if (subCount > 0) {
            redirectWithError(request, response, "Không thể xóa danh mục '" + cat.getCategoryName() 
                    + "' vì đang chứa " + subCount + " danh mục con!");
            return;
        }

        categoryDAO.deleteCategory(categoryId);
        response.sendRedirect(request.getRequestURI() + "?success=deleted");
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
