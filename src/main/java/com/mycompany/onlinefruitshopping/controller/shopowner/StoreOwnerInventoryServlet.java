package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.inventory.LowStockNotifier;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import jakarta.mail.MessagingException;
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
 * Shop owner inventory, split into small pages:
 *
 * <ul>
 *   <li>{@code /store-owner/inventory} - view stock quantity</li>
 *   <li>{@code /store-owner/inventory/update} - update a batch's
 *       stock quantity</li>
 *   <li>{@code /store-owner/inventory/restock} - record a new
 *       restock batch</li>
 *   <li>{@code /store-owner/inventory/low-stock} - threshold and
 *       low stock monitor</li>
 *   <li>{@code /store-owner/inventory/history} - inventory change
 *       log</li>
 * </ul>
 */
@WebServlet(urlPatterns = {
    "/store-owner/inventory",
    "/store-owner/inventory/*"
})
public class StoreOwnerInventoryServlet extends HttpServlet {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Upper bound for any single stock figure, to reject absurd
     * input before it reaches the database.
     */
    private static final double MAX_STOCK_QUANTITY = 1_000_000d;

    // =====================================================
    // ROUTING
    // =====================================================

    /**
     * Which small page is requested, or {@code null} for an unknown
     * sub-path.
     */
    private String viewOf(HttpServletRequest request) {

        String path = request.getPathInfo();

        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "stock";
        }

        if ("/update".equals(path)) {
            return "update";
        }

        if ("/restock".equals(path)) {
            return "restock";
        }

        if ("/low-stock".equals(path)) {
            return "low-stock";
        }

        if ("/history".equals(path)) {
            return "history";
        }

        return null;
    }

    private String jspOf(String view) {

        if ("update".equals(view)) {
            return "/jsp/shopowner/inventory-update.jsp";
        }

        if ("restock".equals(view)) {
            return "/jsp/shopowner/inventory-restock.jsp";
        }

        if ("low-stock".equals(view)) {
            return "/jsp/shopowner/inventory-low-stock.jsp";
        }

        if ("history".equals(view)) {
            return "/jsp/shopowner/inventory-history.jsp";
        }

        return "/jsp/shopowner/inventory.jsp";
    }

    /**
     * The page that owns each action, so a POST message lands where
     * the user was working.
     */
    private String backOf(String action) {

        if ("restock".equals(action)) {
            return "/restock";
        }

        if ("setThreshold".equals(action) || "notifyLowStock".equals(action)) {
            return "/low-stock";
        }

        return "/update";
    }

    // =====================================================
    // GET - ONE SMALL PAGE PER FUNCTION
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        String view = viewOf(request);

        if (view == null) {
            response.sendRedirect(
                    request.getContextPath() + "/store-owner/inventory");
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

            request.setAttribute("shopName",
                    shop == null ? null : shop.getShopName());

            if ("update".equals(view)) {
                loadUpdateView(request, userId);

            } else if ("restock".equals(view)) {
                loadRestockView(request, userId);

            } else if ("low-stock".equals(view)) {
                loadLowStockView(request, userId);

            } else if ("history".equals(view)) {
                loadHistoryView(request, userId);

            } else {
                loadStockView(request, userId);
            }

            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your inventory.", e);
        }

        request.getRequestDispatcher(jspOf(view))
               .forward(request, response);
    }

    /**
     * /store-owner/inventory - View Stock Quantity.
     */
    private void loadStockView(HttpServletRequest request, int userId)
            throws SQLException {

        double threshold = inventoryDAO.getLowStockThreshold(userId);

        List<Map<String, Object>> variants =
                inventoryDAO.getVariantsForOwner(userId);

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
                variantRows,
                Page.parsePage(request.getParameter("vpage"))));

        request.setAttribute("threshold", threshold);
    }

    /**
     * /store-owner/inventory/update - Update Stock Quantity.
     */
    private void loadUpdateView(HttpServletRequest request, int userId)
            throws SQLException {

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
                batchRows,
                Page.parsePage(request.getParameter("bpage"))));
    }

    /**
     * /store-owner/inventory/restock - Restock Products.
     */
    private void loadRestockView(HttpServletRequest request, int userId)
            throws SQLException {

        // Full list: the drop-down must show every variant.
        request.setAttribute("variants",
                inventoryDAO.getVariantsForOwner(userId));
    }

    /**
     * /store-owner/inventory/low-stock - Low Stock Monitoring.
     */
    private void loadLowStockView(HttpServletRequest request, int userId)
            throws SQLException {

        double threshold = inventoryDAO.getLowStockThreshold(userId);

        // ---- Low stock: search product/SKU ----
        String lq = Filters.text(request.getParameter("lq"));

        List<Map<String, Object>> lowRows = new ArrayList<>();

        for (Map<String, Object> v
                : inventoryDAO.getLowStockForOwner(userId, threshold)) {
            if (Filters.any(lq, v.get("product_name"), v.get("sku"))) {
                lowRows.add(v);
            }
        }

        request.setAttribute("threshold", threshold);

        request.setAttribute("lowStockPage", Page.of(
                lowRows,
                Page.parsePage(request.getParameter("lpage"))));
    }

    /**
     * /store-owner/inventory/history - Inventory History.
     */
    private void loadHistoryView(HttpServletRequest request, int userId)
            throws SQLException {

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
                historyRows,
                Page.parsePage(request.getParameter("hpage"))));
    }

    // =====================================================
    // POST - UPDATE, RESTOCK, THRESHOLD, ALERT
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
            } else if ("notifyLowStock".equals(action)) {
                result = handleNotifyLowStock(user);
            } else {
                result = handleUpdate(request, userId);
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to update your inventory.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/store-owner/inventory"
                        + backOf(action)
                        + "?msg=" + result
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

    /**
     * Sends the low stock alert email to the owner's account
     * address. {@code notify-none} when nothing is below the
     * threshold, {@code notify-failed} when the mail could not be
     * sent (for example when SMTP is not configured).
     */
    private String handleNotifyLowStock(User user) {

        try {
            int reported = LowStockNotifier.sendAlert(
                    user.getUserID(), user.getEmail(), null);

            return reported > 0 ? "notified" : "notify-none";

        } catch (MessagingException | SQLException e) {

            getServletContext().log(
                    "Low stock alert failed for owner "
                            + user.getUserID(), e);

            return "notify-failed";
        }
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
