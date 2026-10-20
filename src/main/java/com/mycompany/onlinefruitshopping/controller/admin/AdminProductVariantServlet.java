package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.service.product.AdminProductService;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Admin management of the variants of a product.
 *
 * <p>A variant is one selling package, so it is defined by its weight
 * (half a kilo, one kilo, two kilos ...) together with its own SKU,
 * packaging label and price.</p>
 */
@WebServlet("/admin/product-variants")
public class AdminProductVariantServlet extends HttpServlet {

    private final AdminProductService productService =
            new AdminProductService();

    // =====================================================
    // GET - VARIANT LIST
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int productId = parseInt(request.getParameter("productId"), -1);

        if (productId <= 0) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/product-management?message=invalid"
            );
            return;
        }

        try {
            AdminProductService.VariantListView view =
                    productService.loadVariants(
                            productId,
                            request.getParameter("q"),
                            request.getParameter("weight"),
                            request.getParameter("page"));

            if (view.getProduct() == null) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/product-management?message=notfound"
                );
                return;
            }

            request.setAttribute("product", view.getProduct());
            request.setAttribute("variants", view.getVariants());
            request.setAttribute("variantsPage", view.getVariantsPage());
            request.setAttribute("weightOptions", view.getWeightOptions());
            request.setAttribute("message", request.getParameter("message"));

            // Keep the search and filters when moving between pages.
            request.setAttribute("pagerUrl", QueryUrl.keep(
                    request,
                    request.getContextPath() + "/admin/product-variants",
                    "productId",
                    "q",
                    "weight"
            ));

            request.getRequestDispatcher(
                    "/jsp/admin/admin-product-variants.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load product variants.", e);
        }
    }

    // =====================================================
    // POST - ADD / EDIT / DELETE
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = trim(request.getParameter("action"));
        int productId = parseInt(request.getParameter("productId"), -1);
        String result;

        try {
            if ("add".equals(action)) {
                result = productService.addVariant(
                        productId,
                        request.getParameter("sku"),
                        request.getParameter("price"),
                        request.getParameter("weightValue"),
                        request.getParameter("packagingType"));

            } else if ("edit".equals(action)) {
                result = productService.updateVariant(
                        request.getParameter("variantId"),
                        request.getParameter("sku"),
                        request.getParameter("price"),
                        request.getParameter("weightValue"),
                        request.getParameter("packagingType"));

            } else if ("delete".equals(action)) {
                result = productService.deleteVariant(
                        request.getParameter("variantId"));

            } else {
                result = "failed";
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Could not save the variant.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/admin/product-variants?productId=" + productId
                        + "&message=" + result
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

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
