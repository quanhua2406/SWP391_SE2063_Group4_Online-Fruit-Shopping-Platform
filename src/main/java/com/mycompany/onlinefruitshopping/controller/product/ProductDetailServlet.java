package com.mycompany.onlinefruitshopping.controller.product;

import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductImage;
import com.mycompany.onlinefruitshopping.model.ProductVariant;
import com.mycompany.onlinefruitshopping.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller hiển thị chi tiết sản phẩm.
 * Sử dụng ProductService để lấy thông tin sản phẩm, biến thể quy cách, thư viện ảnh và sản phẩm liên quan.
 */
@WebServlet(urlPatterns = {"/product", "/product-detail"})
public class ProductDetailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ProductDetailServlet.class.getName());
    private final ProductService productService;

    public ProductDetailServlet() {
        this.productService = new ProductService();
    }

    public ProductDetailServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        try {
            Product product = productService.getProductDetail(productId);
            if (product == null || !"published".equalsIgnoreCase(product.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            // Lấy danh sách quy cách đóng gói và tồn kho qua Service
            List<ProductVariant> variants = productService.getProductVariants(productId);

            // Lấy thư viện hình ảnh qua Service
            List<ProductImage> images = productService.getProductImages(productId);

            // Lấy danh sách sản phẩm cùng loại gợi ý qua Service
            List<Product> relatedProducts = productService.getRelatedProducts(product.getCategoryId(), productId, 4);

            request.setAttribute("product", product);
            request.setAttribute("variants", variants);
            request.setAttribute("images", images);
            request.setAttribute("relatedProducts", relatedProducts);

            request.getRequestDispatcher("/product-details.jsp").forward(request, response);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi CSDL khi xem chi tiết sản phẩm ID: " + productId, e);
            throw new ServletException("Lỗi CSDL khi xem chi tiết sản phẩm", e);
        }
    }
}
