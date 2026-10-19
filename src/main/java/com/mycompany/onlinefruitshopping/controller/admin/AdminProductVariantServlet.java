package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.product.AdminProductDAO;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin management of the variants of a product.
 *
 * <p>A variant is one selling package, so it is defined by its weight
 * (half a kilo, one kilo, two kilos ...) together with its own SKU,
 * packaging label and price.</p>
 */
@WebServlet("/admin/product-variants")
public class AdminProductVariantServlet extends HttpServlet {

    private final AdminProductDAO dao = new AdminProductDAO();

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
            Map<String, Object> product = dao.getProductForEdit(productId);

            if (product == null) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/product-management?message=notfound"
                );
                return;
            }

            request.setAttribute("product", product);

            String q = Filters.text(request.getParameter("q"));
            String weight = Filters.text(request.getParameter("weight"));

            List<Map<String, Object>> variants =
                    dao.getVariantsByProduct(productId);

            List<Map<String, Object>> filtered = new ArrayList<>();

            for (Map<String, Object> v : variants) {
                if (Filters.any(q, v.get("sku"), v.get("packaging_type"))
                        && Filters.same(weight,
                                VariantWeights.key(v.get("weight_value")))) {
                    filtered.add(v);
                }
            }

            request.setAttribute("variants", variants);
            request.setAttribute("variantsPage", Page.of(
                    filtered, Page.parsePage(request.getParameter("page"))));
            request.setAttribute("message", request.getParameter("message"));

            request.setAttribute("weightOptions",
                    VariantWeights.options(variants));

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
                result = add(request, productId);
            } else if ("edit".equals(action)) {
                result = edit(request);
            } else if ("delete".equals(action)) {
                result = delete(request);
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

    private String add(HttpServletRequest request, int productId)
            throws SQLException {

        if (productId <= 0) {
            return "invalid";
        }

        String sku = trim(request.getParameter("sku"));

        BigDecimal price = parseDecimal(request.getParameter("price"));
        BigDecimal weight = parseDecimal(request.getParameter("weightValue"));

        // A variant is one selling package, so its weight is what
        // identifies it and is therefore mandatory.
        if (sku.isEmpty() || sku.length() > 50
                || price == null
                || price.compareTo(BigDecimal.ZERO) <= 0
                || weight == null
                || weight.compareTo(BigDecimal.ZERO) <= 0) {

            return "invalid";
        }

        if (dao.skuExists(sku, 0)) {
            return "duplicate";
        }

        boolean ok = dao.addVariant(
                productId,
                sku,
                VariantWeights.UNIT,
                weight,
                trimOrNull(request.getParameter("packagingType")),
                price
        );

        return ok ? "created" : "failed";
    }

    private String edit(HttpServletRequest request) throws SQLException {

        int variantId = parseInt(request.getParameter("variantId"), -1);

        String sku = trim(request.getParameter("sku"));

        BigDecimal price = parseDecimal(request.getParameter("price"));
        BigDecimal weight = parseDecimal(request.getParameter("weightValue"));

        if (variantId <= 0
                || sku.isEmpty() || sku.length() > 50
                || price == null
                || price.compareTo(BigDecimal.ZERO) <= 0
                || weight == null
                || weight.compareTo(BigDecimal.ZERO) <= 0) {

            return "invalid";
        }

        if (dao.skuExists(sku, variantId)) {
            return "duplicate";
        }

        boolean ok = dao.updateVariant(
                variantId,
                sku,
                VariantWeights.UNIT,
                weight,
                trimOrNull(request.getParameter("packagingType")),
                price
        );

        return ok ? "updated" : "failed";
    }

    private String delete(HttpServletRequest request) throws SQLException {

        int variantId = parseInt(request.getParameter("variantId"), -1);

        if (variantId <= 0) {
            return "invalid";
        }

        return dao.deleteVariant(variantId);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private BigDecimal parseDecimal(String value) {
        String text = trim(value);

        if (text.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimOrNull(String value) {
        String text = trim(value);
        return text.isEmpty() ? null : text;
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
