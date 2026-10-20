package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.ProductImageDAO;
import com.mycompany.onlinefruitshopping.dao.ProductVariantDAO;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.ProductImage;
import com.mycompany.onlinefruitshopping.model.ProductVariant;

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

@WebServlet(urlPatterns = {"/product", "/product-detail"})
public class ProductDetailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ProductDetailServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final ProductImageDAO imageDAO = new ProductImageDAO();

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
            Product product = productDAO.getProductById(productId);
            if (product == null || !"published".equalsIgnoreCase(product.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            // Get all packaging variants with live stock
            List<ProductVariant> variants = variantDAO.getVariantsByProductId(productId);

            // Get product image gallery
            List<ProductImage> images = imageDAO.getImagesByProductId(productId);

            // Get related products (same category)
            List<Product> relatedProducts = productDAO.getRelatedProducts(product.getCategoryId(), productId, 4);

            request.setAttribute("product", product);
            request.setAttribute("variants", variants);
            request.setAttribute("images", images);
            request.setAttribute("relatedProducts", relatedProducts);

            request.getRequestDispatcher("/product-details.jsp").forward(request, response);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi nạp chi tiết sản phẩm id: " + productId, e);
            throw new ServletException("Database error in ProductDetailServlet", e);
        }
    }
}
