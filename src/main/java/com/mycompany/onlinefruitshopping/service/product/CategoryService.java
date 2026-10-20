package com.mycompany.onlinefruitshopping.service.product;

import com.mycompany.onlinefruitshopping.dao.product.CategoryDAO;
import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic of the administrator's category management:
 * listing, adding, editing and deleting product categories.
 */
public class CategoryService {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * One page of the category table. The {@code categories} list
     * still holds every row, so the parent drop-down above the table
     * can offer every category.
     */
    public static class CategoryListView {

        private final List<Map<String, Object>> categories;
        private final Page<Map<String, Object>> categoriesPage;

        CategoryListView(List<Map<String, Object>> categories,
                         Page<Map<String, Object>> categoriesPage) {
            this.categories = categories;
            this.categoriesPage = categoriesPage;
        }

        public List<Map<String, Object>> getCategories() {
            return categories;
        }

        public Page<Map<String, Object>> getCategoriesPage() {
            return categoriesPage;
        }
    }

    public CategoryListView loadCategories(String q, String page)
            throws SQLException {

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

        // Search filter, applied after the rows are built so the
        // parent drop-down above still lists every category.
        String search = Filters.text(q);

        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            if (Filters.any(search, row.get("category_name"),
                    row.get("parent_name"))) {
                filtered.add(row);
            }
        }

        return new CategoryListView(
                rows, Page.of(filtered, Page.parsePage(page)));
    }

    public String add(String categoryNameRaw, String parentIdRaw)
            throws SQLException {

        String name = trim(categoryNameRaw);
        Integer parentId = parseParent(parentIdRaw);

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

    public String edit(String categoryIdRaw,
                       String categoryNameRaw,
                       String parentIdRaw) throws SQLException {

        int id = parseInt(categoryIdRaw, -1);
        String name = trim(categoryNameRaw);
        Integer parentId = parseParent(parentIdRaw);

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

    public String delete(String categoryIdRaw) throws SQLException {

        int id = parseInt(categoryIdRaw, -1);

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

    // =========================================================
    // HELPERS
    // =========================================================

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
