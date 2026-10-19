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
import java.util.Map;

/**
 * Admin product editing (details, category, commission and status).
 */
@WebServlet("/admin/product-edit")
public class AdminProductEditServlet extends HttpServlet {

    private final AdminProductDAO dao = new AdminProductDAO();

    // =====================================================
    // GET - EDIT FORM
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int productId = parseInt(request.getParameter("id"), -1);

        if (productId <= 0) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/product-management?message=invalid"
            );
            return;
        }

        try {
            Map<String, Object> product = dao.getProductForEdit(productId);

            if (product == null) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/product-management?message=notfound"
                );
                return;
            }

            request.setAttribute("product", product);
            request.setAttribute("categories", dao.getCategories());

            request.getRequestDispatcher(
                    "/jsp/admin/admin-product-edit.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load the product.", e);
        }
    }

    // =====================================================
    // POST - SAVE
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int productId = parseInt(request.getParameter("productId"), -1);
        int categoryId = parseInt(request.getParameter("categoryId"), -1);

        String productName = trim(request.getParameter("productName"));
        String description = trim(request.getParameter("description"));
        String origin = trim(request.getParameter("origin"));
        String status = trim(request.getParameter("status"));
        String commissionText = trim(request.getParameter("commissionRate"));

        try {
            BigDecimal commissionRate = commissionText.isEmpty()
                    ? AdminProductDAO.DEFAULT_COMMISSION_RATE
                    : new BigDecimal(commissionText);

            boolean validStatus = "draft".equals(status)
                    || "published".equals(status)
                    || "hidden".equals(status);

            if (productId <= 0
                    || categoryId <= 0
                    || productName.isEmpty()
                    || productName.length() > 150
                    || commissionRate.compareTo(BigDecimal.ZERO) < 0
                    || commissionRate.compareTo(new BigDecimal("100")) > 0
                    || !validStatus) {

                request.setAttribute(
                        "error",
                        "Category, product name, status and commission "
                        + "(0-100) are invalid."
                );
                showForm(request, response, productId);
                return;
            }

            boolean updated = dao.updateProduct(
                    productId,
                    categoryId,
                    productName,
                    description.isEmpty() ? null : description,
                    origin.isEmpty() ? null : origin,
                    commissionRate,
                    status
            );

            if (updated) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/product-management?message=updated"
                );
                return;
            }

            request.setAttribute("error", "Product could not be updated.");
            showForm(request, response, productId);

        } catch (NumberFormatException e) {
            request.setAttribute(
                    "error",
                    "Commission has an invalid format."
            );
            showForm(request, response, productId);

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not update the product.", e);
        }
    }

    private void showForm(HttpServletRequest request,
                          HttpServletResponse response,
                          int productId)
            throws ServletException, IOException {

        try {
            request.setAttribute("product", dao.getProductForEdit(productId));
            request.setAttribute("categories", dao.getCategories());

            request.getRequestDispatcher(
                    "/jsp/admin/admin-product-edit.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to reload the product form.", e);
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
