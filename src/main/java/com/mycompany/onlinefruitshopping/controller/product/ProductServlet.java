package com.mycompany.onlinefruitshopping.controller.product;

import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.CategoryService;
import com.mycompany.onlinefruitshopping.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller Quản trị Sản phẩm của Chủ Shop / Admin.
 * Tuân thủ mô hình 3 tầng: Sử dụng ProductService và CategoryService để xử lý nghiệp vụ.
 */
@WebServlet(urlPatterns = {"/store-owner/products", "/admin/products"})
public class ProductServlet extends HttpServlet {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductServlet() {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
    }

    public ProductServlet(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
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
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    handleDelete(request, response, user);
                    break;
                case "toggleStatus":
                    handleToggleStatus(request, response, user);
                    break;
                case "list":
                default:
                    showProductList(request, response, user);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in ProductServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "add":
                    processAddProduct(request, response, user);
                    break;
                case "edit":
                    processEditProduct(request, response);
                    break;
                default:
                    response.sendRedirect(request.getRequestURI());
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in ProductServlet", e);
        }
    }

    // =========================================================
    // 1. PRODUCT LIST
    // =========================================================
    private void showProductList(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String catParam = request.getParameter("categoryId");
        String status = request.getParameter("status");

        Integer categoryId = null;
        if (catParam != null && !catParam.isBlank()) {
            try {
                categoryId = Integer.parseInt(catParam);
            } catch (NumberFormatException ignored) {}
        }

        Integer shopId = null;
        String role = (user.getRoleName() != null) ? user.getRoleName().toUpperCase() : "";
        if (role.contains("SHOP") || role.contains("OWNER")) {
            shopId = productService.getOrCreateShopIdForUser(user.getUserID(), user.getFullName());
        }

        List<Product> products = productService.getStoreProducts(shopId, keyword, categoryId, status);
        List<Category> categories = categoryService.getAllCategories();

        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("selectedCategory", categoryId);
        request.setAttribute("selectedStatus", status != null ? status : "all");

        request.getRequestDispatcher("/products.jsp").forward(request, response);
    }

    // =========================================================
    // 2. ADD PRODUCT FORM & PROCESS
    // =========================================================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        List<Category> categories = categoryService.getAllCategories();
        request.setAttribute("categories", categories);
        request.getRequestDispatcher("/add-product.jsp").forward(request, response);
    }

    private void processAddProduct(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {

        String productName = request.getParameter("productName");
        String categoryIdStr = request.getParameter("categoryId");
        String origin = request.getParameter("origin");
        String priceStr = request.getParameter("price");
        String unit = request.getParameter("unit");
        String packagingType = request.getParameter("packagingType");
        String stockStr = request.getParameter("stock");
        String imageUrl = request.getParameter("imageUrl");
        String description = request.getParameter("description");
        String status = request.getParameter("status");

        String error = null;
        int categoryId = 0;
        double price = 0;
        double stock = 0;

        if (productName == null || productName.trim().isEmpty()) {
            error = "Vui lòng nhập tên sản phẩm hoa quả!";
        } else {
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (Exception e) {
                error = "Vui lòng chọn danh mục hợp lệ!";
            }

            if (error == null) {
                try {
                    price = Double.parseDouble(priceStr);
                    if (price < 0) error = "Giá bán không được nhỏ hơn 0!";
                } catch (Exception e) {
                    error = "Giá bán không hợp lệ!";
                }
            }

            if (error == null && stockStr != null && !stockStr.isBlank()) {
                try {
                    stock = Double.parseDouble(stockStr);
                    if (stock < 0) error = "Số lượng tồn kho không được âm!";
                } catch (Exception e) {
                    error = "Số lượng tồn kho không hợp lệ!";
                }
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("productName", productName);
            request.setAttribute("categoryId", categoryIdStr);
            request.setAttribute("origin", origin);
            request.setAttribute("price", priceStr);
            request.setAttribute("unit", unit);
            request.setAttribute("packagingType", packagingType);
            request.setAttribute("stock", stockStr);
            request.setAttribute("imageUrl", imageUrl);
            request.setAttribute("description", description);
            request.setAttribute("categories", categoryService.getAllCategories());
            request.getRequestDispatcher("/add-product.jsp").forward(request, response);
            return;
        }

        int shopId = productService.getOrCreateShopIdForUser(user.getUserID(), user.getFullName());

        Product p = new Product();
        p.setShopId(shopId);
        p.setCategoryId(categoryId);
        p.setProductName(productName.trim());
        p.setDescription(description != null ? description.trim() : "");
        p.setOrigin(origin != null ? origin.trim() : "");
        p.setStatus((status != null && !status.isBlank()) ? status : "published");
        p.setPrice(price);
        p.setUnit((unit != null && !unit.isBlank()) ? unit : "kg");
        p.setPackagingType((packagingType != null && !packagingType.isBlank()) ? packagingType.trim() : "Mặc định");
        p.setStock(stock);
        p.setImageUrl((imageUrl != null && !imageUrl.isBlank()) ? imageUrl.trim() : "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500");

        productService.addProduct(p);
        response.sendRedirect(request.getRequestURI() + "?success=added");
    }

    // =========================================================
    // 3. EDIT PRODUCT FORM & PROCESS
    // =========================================================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect(request.getRequestURI());
            return;
        }

        int id = Integer.parseInt(idStr);
        Product product = productService.getProductDetail(id);
        if (product == null) {
            response.sendRedirect(request.getRequestURI() + "?error=notfound");
            return;
        }

        request.setAttribute("product", product);
        request.setAttribute("categories", categoryService.getAllCategories());
        request.getRequestDispatcher("/edit-product.jsp").forward(request, response);
    }

    private void processEditProduct(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String idStr = request.getParameter("id");
        int productId = Integer.parseInt(idStr);
        String productName = request.getParameter("productName");
        String categoryIdStr = request.getParameter("categoryId");
        String origin = request.getParameter("origin");
        String priceStr = request.getParameter("price");
        String unit = request.getParameter("unit");
        String packagingType = request.getParameter("packagingType");
        String stockStr = request.getParameter("stock");
        String imageUrl = request.getParameter("imageUrl");
        String description = request.getParameter("description");
        String status = request.getParameter("status");

        String error = null;
        int categoryId = 0;
        double price = 0;
        double stock = 0;

        if (productName == null || productName.trim().isEmpty()) {
            error = "Tên sản phẩm không được để trống!";
        } else {
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (Exception e) {
                error = "Danh mục không hợp lệ!";
            }

            if (error == null) {
                try {
                    price = Double.parseDouble(priceStr);
                    if (price < 0) error = "Giá bán không được nhỏ hơn 0!";
                } catch (Exception e) {
                    error = "Giá bán không hợp lệ!";
                }
            }

            if (error == null && stockStr != null && !stockStr.isBlank()) {
                try {
                    stock = Double.parseDouble(stockStr);
                    if (stock < 0) error = "Số lượng tồn kho không được âm!";
                } catch (Exception e) {
                    error = "Số lượng tồn kho không hợp lệ!";
                }
            }
        }

        if (error != null) {
            Product p = productService.getProductDetail(productId);
            request.setAttribute("error", error);
            request.setAttribute("product", p);
            request.setAttribute("categories", categoryService.getAllCategories());
            request.getRequestDispatcher("/edit-product.jsp").forward(request, response);
            return;
        }

        Product p = new Product();
        p.setProductId(productId);
        p.setCategoryId(categoryId);
        p.setProductName(productName.trim());
        p.setDescription(description != null ? description.trim() : "");
        p.setOrigin(origin != null ? origin.trim() : "");
        p.setStatus((status != null && !status.isBlank()) ? status : "published");
        p.setPrice(price);
        p.setUnit((unit != null && !unit.isBlank()) ? unit : "kg");
        p.setPackagingType((packagingType != null && !packagingType.isBlank()) ? packagingType.trim() : "Mặc định");
        p.setStock(stock);
        p.setImageUrl((imageUrl != null && !imageUrl.isBlank()) ? imageUrl.trim() : "");

        productService.updateProduct(p);
        response.sendRedirect(request.getRequestURI() + "?success=updated");
    }

    // =========================================================
    // 4. DELETE PRODUCT
    // =========================================================
    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, IOException {

        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isBlank()) {
            int productId = Integer.parseInt(idStr);
            productService.deleteProduct(productId);
        }
        response.sendRedirect(request.getRequestURI() + "?success=deleted");
    }

    // =========================================================
    // 5. TOGGLE PRODUCT STATUS (ACTIVE / HIDDEN)
    // =========================================================
    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect(request.getRequestURI() + "?error=invalid_id");
            return;
        }

        try {
            int productId = Integer.parseInt(idStr);
            Integer shopId = null;
            String role = (user.getRoleName() != null) ? user.getRoleName().toUpperCase() : "";
            if (role.contains("SHOP") || role.contains("OWNER")) {
                shopId = productService.getOrCreateShopIdForUser(user.getUserID(), user.getFullName());
            }

            boolean success = productService.toggleProductStatus(productId, shopId);
            if (success) {
                response.sendRedirect(request.getRequestURI() + "?success=status");
            } else {
                response.sendRedirect(request.getRequestURI() + "?error=notfound");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getRequestURI() + "?error=invalid_id");
        }
    }

    // =========================================================
    // HELPER: AUTH CHECK
    // =========================================================
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
