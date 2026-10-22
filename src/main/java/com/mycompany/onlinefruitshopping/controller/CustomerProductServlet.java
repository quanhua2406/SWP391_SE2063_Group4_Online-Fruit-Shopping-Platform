package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.CategoryDAO;
import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.model.Category;
import com.mycompany.onlinefruitshopping.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/products", "/catalog"})
public class CustomerProductServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CustomerProductServlet.class.getName());
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Parse search & filter parameters
        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.isBlank()) {
            keyword = request.getParameter("q"); // Also accept 'q' for query
        }
        if (keyword != null) {
            keyword = keyword.trim();
        }

        Integer categoryId = null;
        String catParam = request.getParameter("category");
        if (catParam == null || catParam.isBlank()) {
            catParam = request.getParameter("categoryId");
        }
        if (catParam != null && !catParam.isBlank()) {
            try {
                categoryId = Integer.parseInt(catParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Multi origins filter
        String[] originArr = request.getParameterValues("origin");
        List<String> origins = new ArrayList<>();
        if (originArr != null) {
            for (String o : originArr) {
                if (o != null && !o.trim().isEmpty()) {
                    origins.add(o.trim());
                }
            }
        }

        // Price range presets or custom min/max
        String priceRange = request.getParameter("priceRange");
        Double minPrice = null;
        Double maxPrice = null;

        if (priceRange != null && !priceRange.isBlank()) {
            switch (priceRange.trim()) {
                case "under100":
                    maxPrice = 100000.0;
                    break;
                case "100to300":
                    minPrice = 100000.0;
                    maxPrice = 300000.0;
                    break;
                case "300to600":
                    minPrice = 300000.0;
                    maxPrice = 600000.0;
                    break;
                case "above600":
                    minPrice = 600000.0;
                    break;
            }
        }

        // Custom min/max override if specified
        String minPriceStr = request.getParameter("minPrice");
        if (minPriceStr != null && !minPriceStr.isBlank()) {
            try {
                minPrice = Double.parseDouble(minPriceStr.trim().replace(",", "").replace(".", ""));
            } catch (NumberFormatException ignored) {}
        }
        String maxPriceStr = request.getParameter("maxPrice");
        if (maxPriceStr != null && !maxPriceStr.isBlank()) {
            try {
                maxPrice = Double.parseDouble(maxPriceStr.trim().replace(",", "").replace(".", ""));
            } catch (NumberFormatException ignored) {}
        }

        // Availability / In stock only
        String inStockParam = request.getParameter("inStock");
        Boolean inStockOnly = null;
        if ("true".equalsIgnoreCase(inStockParam) || "1".equals(inStockParam) || "on".equalsIgnoreCase(inStockParam)) {
            inStockOnly = true;
        }

        // Sort by
        String sortBy = request.getParameter("sortBy");
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "newest";
        }

        // Pagination
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int pageSize = 12;
        String pageSizeStr = request.getParameter("pageSize");
        if (pageSizeStr != null && !pageSizeStr.isBlank()) {
            try {
                pageSize = Math.max(1, Integer.parseInt(pageSizeStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        try {
            List<Product> products = productDAO.searchAndFilterProducts(
                    keyword, categoryId, origins, minPrice, maxPrice, inStockOnly, sortBy, page, pageSize);

            int totalProducts = productDAO.countFilteredProducts(
                    keyword, categoryId, origins, minPrice, maxPrice, inStockOnly);

            int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
            if (totalPages < 1) totalPages = 1;

            // Categories with product count
            List<Category> categories = categoryDAO.getAllCategoriesWithProductCount();

            // Distinct origins for faceted filter
            List<String> distinctOrigins = productDAO.getDistinctOrigins();

            // Build query string without page for pagination links
            StringBuilder qs = new StringBuilder();
            if (keyword != null && !keyword.isEmpty()) {
                qs.append("&keyword=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
            }
            if (categoryId != null && categoryId > 0) {
                qs.append("&category=").append(categoryId);
            }
            for (String org : origins) {
                qs.append("&origin=").append(URLEncoder.encode(org, StandardCharsets.UTF_8));
            }
            if (priceRange != null && !priceRange.isEmpty()) {
                qs.append("&priceRange=").append(URLEncoder.encode(priceRange, StandardCharsets.UTF_8));
            }
            if (minPriceStr != null && !minPriceStr.isEmpty()) {
                qs.append("&minPrice=").append(URLEncoder.encode(minPriceStr, StandardCharsets.UTF_8));
            }
            if (maxPriceStr != null && !maxPriceStr.isEmpty()) {
                qs.append("&maxPrice=").append(URLEncoder.encode(maxPriceStr, StandardCharsets.UTF_8));
            }
            if (inStockOnly != null && inStockOnly) {
                qs.append("&inStock=true");
            }
            if (sortBy != null && !sortBy.isEmpty()) {
                qs.append("&sortBy=").append(URLEncoder.encode(sortBy, StandardCharsets.UTF_8));
            }
            String baseQueryString = qs.toString();

            // Request attributes
            request.setAttribute("products", products);
            request.setAttribute("totalProducts", totalProducts);
            request.setAttribute("currentPage", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("categories", categories);
            request.setAttribute("distinctOrigins", distinctOrigins);

            request.setAttribute("selectedKeyword", keyword);
            request.setAttribute("selectedCategoryId", categoryId);
            request.setAttribute("selectedOrigins", origins);
            request.setAttribute("selectedPriceRange", priceRange);
            request.setAttribute("selectedMinPrice", minPriceStr);
            request.setAttribute("selectedMaxPrice", maxPriceStr);
            request.setAttribute("selectedInStock", inStockOnly);
            request.setAttribute("selectedSortBy", sortBy);
            request.setAttribute("baseQueryString", baseQueryString);

            request.getRequestDispatcher("/product-catalog.jsp").forward(request, response);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi truy vấn sản phẩm trong CustomerProductServlet", e);
            throw new ServletException("Database error in CustomerProductServlet", e);
        }
    }
}
