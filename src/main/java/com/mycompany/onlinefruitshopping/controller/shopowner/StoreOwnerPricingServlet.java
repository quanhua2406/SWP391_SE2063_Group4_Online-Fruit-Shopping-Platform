package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.model.User;
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
 * Product &amp; variant pricing: set / edit the normal selling price of
 * each variant of the owner's shop.
 *
 * <p>Temporary offers are handled by
 * {@link StoreOwnerPromotionServlet} instead.</p>
 */
@WebServlet("/store-owner/pricing")
public class StoreOwnerPricingServlet extends HttpServlet {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    // =====================================================
    // GET - PRICE LIST
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            if (!inventoryDAO.hasShop(user.getUserID())) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/create-shop"
                );
                return;
            }

            String q = Filters.text(request.getParameter("q"));
            String weight = Filters.text(request.getParameter("weight"));

            List<Map<String, Object>> all =
                    inventoryDAO.getVariantsForOwner(user.getUserID());

            List<Map<String, Object>> rows = new ArrayList<>();

            for (Map<String, Object> v : all) {
                if (Filters.any(q, v.get("product_name"), v.get("sku"))
                        && Filters.same(weight,
                                VariantWeights.key(v.get("weight_value")))) {
                    rows.add(v);
                }
            }

            request.setAttribute("variantsPage", Page.of(
                    rows, Page.parsePage(request.getParameter("page"))));

            request.setAttribute("weightOptions",
                    VariantWeights.options(all));

            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your prices.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/pricing.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - UPDATE A PRICE
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        String result;

        try {
            result = savePrice(request, user.getUserID());

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to save the price.", e);
        }

        // Keep the current search / filter / page while redirecting,
        // so saving one row does not send the owner back to page 1.
        String url = QueryUrl.keep(request,
                request.getContextPath() + "/store-owner/pricing",
                "q", "weight", "page") + "msg=" + result;

        response.sendRedirect(url);
    }

    private String savePrice(HttpServletRequest request, int ownerId)
            throws SQLException {

        int variantId = parseInt(request.getParameter("variantId"), -1);
        double price = parseDouble(request.getParameter("price"), -1);

        if (variantId <= 0 || price < 0) {
            return "invalid";
        }

        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) {
            return "forbidden";
        }

        // A promotion must stay cheaper than the normal price, so the
        // normal price cannot be dropped below a running promotion.
        BigDecimal promo = inventoryDAO.getLivePromoPrice(variantId);

        if (promo != null
                && BigDecimal.valueOf(price).compareTo(promo) <= 0) {
            return "promo-conflict";
        }

        return inventoryDAO.updateVariantPrice(variantId, price)
                ? "updated" : "failed";
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
