package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.CategoryDAO;
import com.mycompany.onlinefruitshopping.model.Category;

import java.sql.SQLException;
import java.util.List;

/**
 * Service Layer xử lý toàn bộ logic nghiệp vụ (Business Logic) liên quan đến Danh mục hoa quả (Categories).
 *
 * Tuân thủ kiến trúc 3 tầng:
 * - Servlet (Controller): Tiếp nhận request, đọc tham số và điều hướng.
 * - Service: Xử lý nghiệp vụ (kiểm tra tên danh mục trùng lặp, ràng buộc không được xóa nếu có sản phẩm/danh mục con).
 * - DAO: Tương tác với cơ sở dữ liệu.
 */
public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    public List<Category> getAllCategoriesWithProductCount() throws SQLException {
        return categoryDAO.getAllCategoriesWithProductCount();
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        if (categoryId <= 0) return null;
        return categoryDAO.getCategoryById(categoryId);
    }

    public void addCategory(Category category) throws SQLException {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }
        String name = category.getCategoryName().trim();
        if (categoryDAO.isCategoryNameExists(name, null)) {
            throw new IllegalArgumentException("Tên danh mục '" + name + "' đã tồn tại trong hệ thống.");
        }
        categoryDAO.addCategory(category);
    }

    public void updateCategory(Category category) throws SQLException {
        if (category == null || category.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Mã danh mục không hợp lệ.");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }
        String name = category.getCategoryName().trim();
        if (categoryDAO.isCategoryNameExists(name, category.getCategoryId())) {
            throw new IllegalArgumentException("Tên danh mục '" + name + "' đã tồn tại trong hệ thống.");
        }
        if (category.getParentId() != null && category.getParentId().equals(category.getCategoryId())) {
            throw new IllegalArgumentException("Danh mục không thể làm danh mục cha của chính nó.");
        }
        categoryDAO.updateCategory(category);
    }

    public void deleteCategory(int categoryId) throws SQLException {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Mã danh mục không hợp lệ.");
        }
        int productCount = categoryDAO.countProductsByCategoryId(categoryId);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục này vì đang có " + productCount + " sản phẩm liên kết.");
        }
        int subCategoryCount = categoryDAO.countSubCategories(categoryId);
        if (subCategoryCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục này vì đang có " + subCategoryCount + " danh mục con trực thuộc.");
        }
        categoryDAO.deleteCategory(categoryId);
    }
}
