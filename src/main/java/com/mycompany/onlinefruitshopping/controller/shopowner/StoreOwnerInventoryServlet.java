package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shop owner inventory: view stock, update stock quantity and
 * restock products.
 */
@WebServlet("/store-owner/inventory")
public class StoreOwnerInventoryServlet extends HttpServlet {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Upper bound for any single stock figure, to reject absurd
     * input before it reaches the database.
     */
    private static final double MAX_STOCK_QUANTITY = 1_000_000d;

    // =====================================================
    // GET - STOCK OVERVIEW
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

            ProductDAO.ShopInfo shop =
                    productDAO.getShopForOwner(user.getUserID());

            int userId = user.getUserID();

            double threshold = inventoryDAO.getLowStockThreshold(userId);

            List<Map<String, Object>> variants =
                    inventoryDAO.getVariantsForOwner(userId);

            request.setAttribute("shopName",
                    shop == null ? null : shop.getShopName());

            // Full list: the restock drop-down must show every variant.
            request.setAttribute("variants", variants);

            // ---- View stock: search product/SKU + filter package weight ----
            String vq = Filters.text(request.getParameter("vq"));
            String vweight = Filters.text(request.getParameter("vweight"));

            List<Map<String, Object>> variantRows = new ArrayList<>();

            for (Map<String, Object> v : variants) {
                if (Filters.any(vq, v.get("product_name"), v.get("sku"))
                        && Filters.same(vweight,
                                VariantWeights.key(v.get("weight_value")))) {
                    variantRows.add(v);
                }
            }

            request.setAttribute("weightOptions",
                    VariantWeights.options(variants));

            request.setAttribute("variantsPage", Page.of(
                    variantRows, Page.parsePage(request.getParameter("vpage"))));

            // ---- Update stock: search batch/product/SKU + filter status ----
            String bq = Filters.text(request.getParameter("bq"));
            String bstatus = Filters.text(request.getParameter("bstatus"));

            List<Map<String, Object>> batchRows = new ArrayList<>();

            for (Map<String, Object> b
                    : inventoryDAO.getBatchesForOwner(userId)) {
                if (Filters.any(bq, b.get("batch_code"),
                                b.get("product_name"), b.get("sku"))
                        && Filters.same(bstatus, b.get("status"))) {
                    batchRows.add(b);
                }
            }

            request.setAttribute("batchesPage", Page.of(
                    batchRows, Page.parsePage(request.getParameter("bpage"))));

            // ---- Low stock: search product/SKU ----
            String lq = Filters.text(request.getParameter("lq"));

            List<Map<String, Object>> lowRows = new ArrayList<>();

            for (Map<String, Object> v
                    : inventoryDAO.getLowStockForOwner(userId, threshold)) {
                if (Filters.any(lq, v.get("product_name"), v.get("sku"))) {
                    lowRows.add(v);
                }
            }

            request.setAttribute("lowStockPage", Page.of(
                    lowRows, Page.parsePage(request.getParameter("lpage"))));

            // ---- History: search + filter by movement type ----
            String hq = Filters.text(request.getParameter("hq"));
            String htype = Filters.text(request.getParameter("htype"));

            List<Map<String, Object>> historyRows = new ArrayList<>();

            for (Map<String, Object> a
                    : inventoryDAO.getAdjustmentsForOwner(userId)) {
                if (Filters.any(hq, a.get("product_name"), a.get("sku"),
                                a.get("batch_code"), a.get("reason"))
                        && Filters.same(htype, a.get("adjustment_type"))) {
                    historyRows.add(a);
                }
            }

            request.setAttribute("adjustmentsPage", Page.of(
                    historyRows, Page.parsePage(request.getParameter("hpage"))));

            request.setAttribute("threshold", threshold);
            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your inventory.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/inventory.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - UPDATE STOCK OR RESTOCK
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

        String action = trim(request.getParameter("action"));
        int userId = user.getUserID();
        String result;

        try {
            if ("restock".equals(action)) {
                result = handleRestock(request, userId);
            } else if ("setThreshold".equals(action)) {
                result = handleSetThreshold(request, userId);
            } else {
                result = handleUpdate(request, userId);
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to update your inventory.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/store-owner/inventory?msg=" + result
        );
    }

    private String handleRestock(HttpServletRequest request, int userId)
            throws SQLException {

        int variantId = parseInt(request.getParameter("variantId"), -1);
        double quantity = parseDouble(request.getParameter("quantity"), -1);
        String batchCode = trim(request.getParameter("batchCode"));
        String expiryRaw = trim(request.getParameter("expiryDate"));
        String reason = trim(request.getParameter("reason"));

        if (variantId <= 0 || quantity <= 0 || batchCode.isEmpty()) {
            return "invalid";
        }

        if (quantity > MAX_STOCK_QUANTITY) {
            return "invalid";
        }

        if (batchCode.length() > 50) {
            return "invalid";
        }

        if (!inventoryDAO.variantBelongsToOwner(variantId, userId)) {
            return "forbidden";
        }

        if (inventoryDAO.batchCodeExists(variantId, batchCode)) {
            return "duplicate";
        }

        Date expiry;
        try {
            expiry = Date.valueOf(expiryRaw);
        } catch (IllegalArgumentException e) {
            return "invalid";
        }

        if (expiry.before(new Date(System.currentTimeMillis()))) {
            return "expired";
        }

        if (reason.isEmpty()) {
            reason = "Restock " + batchCode;
        }

        boolean ok = inventoryDAO.restock(
                variantId, batchCode, quantity, expiry, userId, reason);

        return ok ? "restocked" : "failed";
    }

    private String handleUpdate(HttpServletRequest request, int userId)
            throws SQLException {

        int batchId = parseInt(request.getParameter("batchId"), -1);
        double quantity = parseDouble(request.getParameter("quantity"), -1);
        String reason = trim(request.getParameter("reason"));

        if (batchId <= 0 || quantity < 0) {
            return "invalid";
        }

        if (quantity > MAX_STOCK_QUANTITY) {
            return "invalid";
        }

        if (!inventoryDAO.batchBelongsToOwner(batchId, userId)) {
            return "forbidden";
        }

        if (reason.isEmpty()) {
            reason = "Manual stock update";
        }

        boolean ok = inventoryDAO.setBatchQuantity(
                batchId, quantity, userId, reason);

        return ok ? "updated" : "failed";
    }

    /**
     * Stores the shop's own "low stock" safety threshold.
     */
    private String handleSetThreshold(HttpServletRequest request, int userId)
            throws SQLException {

        double threshold = parseDouble(request.getParameter("threshold"), -1);

        if (threshold < 0 || threshold > MAX_STOCK_QUANTITY) {
            return "invalid";
        }

        boolean ok = inventoryDAO.updateLowStockThreshold(userId, threshold);

        return ok ? "threshold-saved" : "failed";
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

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(trim(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
