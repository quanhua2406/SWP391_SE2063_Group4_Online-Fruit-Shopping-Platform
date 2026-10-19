
package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.product.AdminProductDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/admin/product-add")
public class AdminProductAddServlet extends HttpServlet {

    private final AdminProductDAO dao = new AdminProductDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        showForm(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String productName = trim(request.getParameter("productName"));
        String description = trim(request.getParameter("description"));
        String origin = trim(request.getParameter("origin"));
        String sku = trim(request.getParameter("sku"));
        String weightText = trim(request.getParameter("weightValue"));
        String packagingType = trim(request.getParameter("packagingType"));
        String priceText = trim(request.getParameter("price"));
        String commissionText = trim(request.getParameter("commissionRate"));
        String imageUrl = trim(request.getParameter("imageUrl"));

        try {
            String shopText = trim(request.getParameter("shopId"));
            String categoryText = trim(request.getParameter("categoryId"));

            if (shopText.isEmpty() || categoryText.isEmpty()
                    || productName.isEmpty() || sku.isEmpty()
                    || priceText.isEmpty() || weightText.isEmpty()) {

                request.setAttribute(
                        "error",
                        "Please fill in all required fields."
                );
                showForm(request, response);
                return;
            }

            int shopId = Integer.parseInt(shopText);
            int categoryId = Integer.parseInt(categoryText);
            BigDecimal price = new BigDecimal(priceText);

            BigDecimal commissionRate = commissionText.isEmpty()
                    ? BigDecimal.ZERO
                    : new BigDecimal(commissionText);

            // The first variant of the product: its weight defines the
            // package that is sold.
            BigDecimal weightValue = new BigDecimal(weightText);

            if (shopId <= 0
                    || categoryId <= 0
                    || productName.length() > 150
                    || sku.length() > 50
                    || price.compareTo(BigDecimal.ZERO) <= 0
                    || commissionRate.compareTo(BigDecimal.ZERO) < 0
                    || commissionRate.compareTo(
                            new BigDecimal("100")) > 0
                    || weightValue.compareTo(BigDecimal.ZERO) <= 0) {

                request.setAttribute(
                        "error",
                        "Shop, category, product name, SKU, price, "
                        + "weight or commission (0-100) is invalid."
                );
                showForm(request, response);
                return;
            }

            boolean created = dao.createProduct(
                    shopId,
                    categoryId,
                    productName,
                    description.isEmpty() ? null : description,
                    origin.isEmpty() ? null : origin,
                    sku,
                    "kg",
                    weightValue,
                    packagingType.isEmpty() ? null : packagingType,
                    price,
                    commissionRate,
                    imageUrl.isEmpty() ? null : imageUrl
            );

            if (created) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/product-management?message=created"
                );
                return;
            }

            request.setAttribute(
                    "error",
                    "Product could not be created."
            );
            showForm(request, response);

        } catch (NumberFormatException e) {
            request.setAttribute(
                    "error",
                    "Shop, category, price, weight or commission has an invalid format."
            );
            showForm(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not save product. Please check the database schema and constraints.",
                    e
            );
        }
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            request.setAttribute("categories", dao.getCategories());
            request.setAttribute("shops", dao.getShops());

            request.getRequestDispatcher(
                    "/jsp/admin/admin-product-form.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not load product categories or shops.",
                    e
            );
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
