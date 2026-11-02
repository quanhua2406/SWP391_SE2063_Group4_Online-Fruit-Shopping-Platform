package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.ProductImageDAO;
import com.mycompany.onlinefruitshopping.dao.ProductVariantDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductImage;
import com.mycompany.onlinefruitshopping.model.ProductVariant;

import java.sql.SQLException;
import java.util.List;

/**
 * Service Layer xử lý toàn bộ logic nghiệp vụ (Business Logic) liên quan đến Sản phẩm hoa quả (Products).
 *
 * Tuân thủ kiến trúc 3 tầng:
 * - Controller (Servlet): Tiếp nhận request, đọc tham số và điều hướng.
 * - Service: Xử lý nghiệp vụ tìm kiếm, bộ lọc đa tiêu chí, phân trang, gợi ý từ khóa, quản lý quy cách đóng gói, thư viện ảnh.
 * - DAO: Tương tác với cơ sở dữ liệu.
 */
public class ProductService {

    private final ProductDAO productDAO;
    private final ProductVariantDAO variantDAO;
    private final ProductImageDAO imageDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.variantDAO = new ProductVariantDAO();
        this.imageDAO = new ProductImageDAO();
    }

    public ProductService(ProductDAO productDAO, ProductVariantDAO variantDAO, ProductImageDAO imageDAO) {
        this.productDAO = productDAO;
        this.variantDAO = variantDAO;
        this.imageDAO = imageDAO;
    }

    // ------------------------------------------------------------------------
    // KHÁCH HÀNG: CATALOG, TÌM KIẾM, BỘ LỌC VÀ CHI TIẾT
    // ------------------------------------------------------------------------

    public List<Product> searchAndFilterProducts(String keyword, Integer categoryId, List<String> origins,
                                                Double minPrice, Double maxPrice, Boolean inStockOnly,
                                                String sortBy, int page, int pageSize) throws SQLException {
        return productDAO.searchAndFilterProducts(keyword, categoryId, origins, minPrice, maxPrice,
                inStockOnly, sortBy, page, pageSize);
    }

    public int countFilteredProducts(String keyword, Integer categoryId, List<String> origins,
                                     Double minPrice, Double maxPrice, Boolean inStockOnly) throws SQLException {
        return productDAO.countFilteredProducts(keyword, categoryId, origins, minPrice, maxPrice, inStockOnly);
    }

    public List<String> getDistinctOrigins() throws SQLException {
        return productDAO.getDistinctOrigins();
    }

    public List<Product> getSearchSuggestions(String query, int limit) throws SQLException {
        if (query == null || query.trim().length() < 2) {
            return java.util.Collections.emptyList();
        }
        return productDAO.getSearchSuggestions(query.trim(), limit);
    }

    public List<Product> getFeaturedProducts(int limit) throws SQLException {
        return productDAO.getFeaturedProducts(limit);
    }

    public List<Product> getRelatedProducts(int categoryId, int excludeProductId, int limit) throws SQLException {
        return productDAO.getRelatedProducts(categoryId, excludeProductId, limit);
    }

    public Product getProductDetail(int productId) throws SQLException {
        if (productId <= 0) return null;
        return productDAO.getProductById(productId);
    }

    public List<ProductVariant> getProductVariants(int productId) throws SQLException {
        if (productId <= 0) return java.util.Collections.emptyList();
        return variantDAO.getVariantsByProductId(productId);
    }

    public List<ProductImage> getProductImages(int productId) throws SQLException {
        if (productId <= 0) return java.util.Collections.emptyList();
        return imageDAO.getImagesByProductId(productId);
    }

    // ------------------------------------------------------------------------
    // CHỦ CỬA HÀNG (STORE OWNER): QUẢN LÝ SẢN PHẨM, QUY CÁCH VÀ HÌNH ẢNH
    // ------------------------------------------------------------------------

    public int getOrCreateShopIdForUser(int userId, String userName) throws SQLException {
        return productDAO.getOrCreateShopIdForUser(userId, userName);
    }

    public List<Product> getStoreProducts(Integer shopId, String keyword, Integer categoryId, String status) throws SQLException {
        return productDAO.getProducts(shopId, keyword, categoryId, status);
    }

    public boolean addProduct(Product product) throws SQLException {
        if (product == null || product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        return productDAO.addProduct(product);
    }

    public boolean updateProduct(Product product) throws SQLException {
        if (product == null || product.getProductId() <= 0) {
            throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ.");
        }
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        return productDAO.updateProduct(product);
    }

    public boolean deleteProduct(int productId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
        return productDAO.deleteProduct(productId);
    }

    public boolean toggleProductStatus(int productId, Integer shopId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
        return productDAO.toggleProductStatus(productId, shopId);
    }

    // QUẢN LÝ BIẾN THỂ QUY CÁCH
    public ProductVariant getVariantById(int variantId) throws SQLException {
        return variantDAO.getVariantById(variantId);
    }

    public void addVariant(ProductVariant v, double initialStock) throws SQLException {
        if (v == null || v.getProductId() <= 0) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ.");
        }
        if (v.getSku() == null || v.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã SKU không được để trống.");
        }
        if (variantDAO.isSkuExists(v.getSku().trim(), null)) {
            throw new IllegalArgumentException("Mã SKU '" + v.getSku().trim() + "' đã tồn tại.");
        }
        if (v.getPrice() <= 0) {
            throw new IllegalArgumentException("Đơn giá quy cách phải lớn hơn 0.");
        }
        variantDAO.addVariant(v, initialStock);
    }

    public void updateVariant(ProductVariant v, double newStock) throws SQLException {
        if (v == null || v.getVariantId() <= 0) {
            throw new IllegalArgumentException("Biến thể quy cách không hợp lệ.");
        }
        if (v.getSku() == null || v.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã SKU không được để trống.");
        }
        if (variantDAO.isSkuExists(v.getSku().trim(), v.getVariantId())) {
            throw new IllegalArgumentException("Mã SKU '" + v.getSku().trim() + "' đã tồn tại.");
        }
        if (v.getPrice() <= 0) {
            throw new IllegalArgumentException("Đơn giá quy cách phải lớn hơn 0.");
        }
        variantDAO.updateVariant(v, newStock);
    }

    public void deleteVariant(int variantId, int productId) throws SQLException {
        if (variantId <= 0 || productId <= 0) {
            throw new IllegalArgumentException("Thông tin biến thể không hợp lệ.");
        }
        int count = variantDAO.countVariantsByProductId(productId);
        if (count <= 1) {
            throw new IllegalStateException("Mỗi sản phẩm bắt buộc phải giữ lại ít nhất 1 quy cách để bán hàng.");
        }
        if (variantDAO.isVariantInOrders(variantId)) {
            throw new IllegalStateException("Không thể xóa quy cách này vì đã phát sinh trong các đơn hàng thực tế.");
        }
        variantDAO.deleteVariant(variantId);
    }

    // QUẢN LÝ THƯ VIỆN HÌNH ẢNH
    public void addImage(ProductImage img) throws SQLException {
        if (img == null || img.getProductId() <= 0) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ.");
        }
        if (img.getImageUrl() == null || img.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn hình ảnh không được để trống.");
        }
        imageDAO.addImage(img);
    }

    public void deleteImage(int imageId) throws SQLException {
        if (imageId <= 0) {
            throw new IllegalArgumentException("Mã hình ảnh không hợp lệ.");
        }
        imageDAO.deleteImage(imageId);
    }

    public void setPrimaryImage(int productId, int imageId) throws SQLException {
        if (productId <= 0 || imageId <= 0) {
            throw new IllegalArgumentException("Thông tin hình ảnh không hợp lệ.");
        }
        imageDAO.setPrimaryImage(productId, imageId);
    }
}
