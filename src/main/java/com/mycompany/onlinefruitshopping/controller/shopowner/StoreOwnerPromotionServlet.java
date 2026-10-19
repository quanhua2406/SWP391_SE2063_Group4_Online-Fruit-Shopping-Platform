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
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Promotion management: set / edit / remove the promotional price of a
 * product variant for a limited period of time.
 *
 * <p>Promotions live apart from the normal prices on purpose: the
 * selling price of a variant is managed once in
 * {@code /store-owner/pricing}, while a promotion is a temporary,
 * dated offer on top of it.</p>
 *
 * <p>Every promotion is only a request: it is stored as
 * {@code pending} and becomes effective once an administrator
 * approves it in {@code /admin/promotion-approvals}.</p>
 */
@WebServlet("/store-owner/promotions")
public class StoreOwnerPromotionServlet extends HttpServlet {

    /**
     * Upper bound accepted for a price, to keep obviously wrong input
     * (or an accidental extra digit) out of the database.
     */
    private static final double MAX_PRICE = 1_000_000_000d;

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    // =====================================================
    // GET - PROMOTION LIST
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
            String approval = Filters.text(request.getParameter("approval"));

            List<Map<String, Object>> variants =
                    inventoryDAO.getVariantsForOwner(user.getUserID());

            // The counters describe the whole shop, so they are
            // computed before the search filter is applied.
            int pending = 0;
            int approved = 0;
            int rejected = 0;

            for (Map<String, Object> v : variants) {
                String status = v.get("promo_status") == null
                        ? ""
                        : v.get("promo_status").toString();

                if ("approved".equals(status)) {
                    approved++;
                } else if ("rejected".equals(status)) {
                    rejected++;
                } else if ("pending".equals(status)) {
                    pending++;
                }
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (Map<String, Object> v : variants) {
                if (Filters.any(q, v.get("product_name"), v.get("sku"))
                        && Filters.same(weight,
                                VariantWeights.key(v.get("weight_value")))
                        && Filters.same(approval, v.get("promo_status"))) {
                    rows.add(v);
                }
            }

            request.setAttribute("variantCount", variants.size());
            request.setAttribute("pendingCount", pending);
            request.setAttribute("approvedCount", approved);
            request.setAttribute("rejectedCount", rejected);

            request.setAttribute("weightOptions",
                    VariantWeights.options(variants));

            request.setAttribute("variantsPage", Page.of(
                    rows, Page.parsePage(request.getParameter("page"))));

            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your promotions.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/promotions.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - SET OR REMOVE A PROMOTION
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

        String action = request.getParameter("action");
        String result;

        try {
            if ("removePromo".equals(action)) {
                result = removePromotion(request, user.getUserID());
            } else {
                result = savePromotion(request, user.getUserID());
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to save the promotion.", e);
        }

        // Keep the current search / filter / page while redirecting,
        // so saving one row does not send the owner back to page 1.
        String url = QueryUrl.keep(request,
                request.getContextPath() + "/store-owner/promotions",
                "q", "weight", "approval", "page") + "msg=" + result;

        response.sendRedirect(url);
    }

    private String savePromotion(HttpServletRequest request, int ownerId)
            throws SQLException {

        int variantId = parseInt(request.getParameter("variantId"), -1);

        BigDecimal promoPrice = parseMoney(
                request.getParameter("promoPrice"));

        Date startDate = parseDate(request.getParameter("startDate"));
        Date endDate = parseDate(request.getParameter("endDate"));

        if (variantId <= 0 || promoPrice == null
                || startDate == null || endDate == null) {
            return "promo-invalid";
        }

        if (promoPrice.compareTo(BigDecimal.valueOf(MAX_PRICE)) > 0) {
            return "promo-invalid";
        }

        if (endDate.before(startDate)) {
            return "promo-invalid";
        }

        Date today = Date.valueOf(LocalDate.now());

        if (endDate.before(today)) {
            return "promo-expired";
        }

        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) {
            return "forbidden";
        }

        Double price = inventoryDAO.getVariantPrice(variantId);

        if (price == null) {
            return "failed";
        }

        if (promoPrice.compareTo(BigDecimal.valueOf(price)) >= 0) {
            return "promo-not-discount";
        }

        return inventoryDAO.setPromotion(
                variantId, promoPrice, startDate, endDate, ownerId)
                ? "promo-set" : "failed";
    }

    private String removePromotion(HttpServletRequest request, int ownerId)
            throws SQLException {

        int variantId = parseInt(request.getParameter("variantId"), -1);

        if (variantId <= 0) {
            return "invalid";
        }

        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) {
            return "forbidden";
        }

        // Removing is idempotent: the promotion is gone either way.
        inventoryDAO.removePromotion(variantId, ownerId);
        return "promo-removed";
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

    private BigDecimal parseMoney(String value) {
        try {
            return new BigDecimal(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseDate(String value) {
        try {
            return Date.valueOf(value == null ? "" : value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
