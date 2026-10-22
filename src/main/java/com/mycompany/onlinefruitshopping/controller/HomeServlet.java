package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlan;
import com.mycompany.onlinefruitshopping.service.CategoryService;
import com.mycompany.onlinefruitshopping.service.ProductService;
import com.mycompany.onlinefruitshopping.service.SubscriptionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller Trang chủ.
 * Tuân thủ mô hình 3 tầng: Sử dụng SubscriptionService, ProductService, CategoryService.
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HomeServlet.class.getName());
    private final SubscriptionService subscriptionService = new SubscriptionService();
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Nạp các gói combo đang active để render động trên banner & grid trang chủ qua SubscriptionService
            List<SubscriptionPlan> featuredPlans = subscriptionService.getActivePlansForCustomer(null, null, null, 1, 4);
            request.setAttribute("featuredPlans", featuredPlans);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Không thể tải danh sách gói combo cho trang chủ", e);
        }

        try {
            // Nạp danh sách trái cây tươi nổi bật qua ProductService
            List<Product> featuredProducts = productService.getFeaturedProducts(8);
            request.setAttribute("featuredProducts", featuredProducts);

            // Nạp danh mục trái cây qua CategoryService
            List<Category> categories = categoryService.getAllCategoriesWithProductCount();
            request.setAttribute("categories", categories);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Không thể tải danh sách sản phẩm nổi bật cho trang chủ", e);
        }

        request.getRequestDispatcher("/home.jsp")
                .forward(request, response);
    }
}
