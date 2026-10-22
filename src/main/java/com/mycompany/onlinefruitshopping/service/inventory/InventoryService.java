package com.mycompany.onlinefruitshopping.service.inventory;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import jakarta.mail.MessagingException;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business logic behind the shop owner's inventory screens.
 *
 * <p>The controller only reads the request and renders the response;
 * every rule (ownership, bounds, duplicate batch codes, expiry
 * dates ...) and every query orchestration lives here, on top of
 * {@link InventoryDAO}.</p>
 */
public class InventoryService {

    /**
     * Upper bound for any single stock figure, to reject absurd
     * input before it reaches the database.
     */
    private static final double MAX_STOCK_QUANTITY = 1_000_000d;

    /** {@code inventory_adjustments.reason} is NVARCHAR(255). */
    private static final int MAX_REASON_LENGTH = 255;

    private static final Logger LOG =
            Logger.getLogger(InventoryService.class.getName());

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    // =========================================================
    // SHOP
    // =========================================================

    public boolean hasShop(int ownerId) throws SQLException {
        return inventoryDAO.hasShop(ownerId);
    }

    /**
     * Name of the owner's shop, or {@code null} when the owner has
     * no shop yet.
     */
    public String shopName(int ownerId) throws SQLException {

        ProductDAO.ShopInfo shop = productDAO.getShopForOwner(ownerId);

        return shop == null ? null : shop.getShopName();
    }

    // =========================================================
    // VIEW MODELS
    // =========================================================

    /**
     * /store-owner/inventory - View Stock Quantity.
     */
    public static class StockView {

        private final double threshold;
        private final List<String> weightOptions;
        private final Page<Map<String, Object>> variantsPage;

        StockView(double threshold,
                  List<String> weightOptions,
                  Page<Map<String, Object>> variantsPage) {
            this.threshold = threshold;
            this.weightOptions = weightOptions;
            this.variantsPage = variantsPage;
        }

        public double getThreshold() {
            return threshold;
        }

        public List<String> getWeightOptions() {
            return weightOptions;
        }

        public Page<Map<String, Object>> getVariantsPage() {
            return variantsPage;
        }
    }

    /**
     * /store-owner/inventory/update - Update Stock Quantity.
     */
    public static class BatchesView {

        private final Page<Map<String, Object>> batchesPage;

        BatchesView(Page<Map<String, Object>> batchesPage) {
            this.batchesPage = batchesPage;
        }

        public Page<Map<String, Object>> getBatchesPage() {
            return batchesPage;
        }
    }

    /**
     * /store-owner/inventory/low-stock - Low Stock Monitoring.
     */
    public static class LowStockView {

        private final double threshold;
        private final Page<Map<String, Object>> lowStockPage;

        LowStockView(double threshold,
                     Page<Map<String, Object>> lowStockPage) {
            this.threshold = threshold;
            this.lowStockPage = lowStockPage;
        }

        public double getThreshold() {
            return threshold;
        }

        public Page<Map<String, Object>> getLowStockPage() {
            return lowStockPage;
        }
    }

    /**
     * /store-owner/inventory/history - Inventory History.
     */
    public static class AdjustmentsView {

        private final Page<Map<String, Object>> adjustmentsPage;

        AdjustmentsView(Page<Map<String, Object>> adjustmentsPage) {
            this.adjustmentsPage = adjustmentsPage;
        }

        public Page<Map<String, Object>> getAdjustmentsPage() {
            return adjustmentsPage;
        }
    }

    // =========================================================
    // VIEWS (GET)
    // =========================================================

    public StockView loadStock(int userId,
                               String q,
                               String weight,
                               String page) throws SQLException {

        double threshold = inventoryDAO.getLowStockThreshold(userId);

        List<Map<String, Object>> variants =
                inventoryDAO.getVariantsForOwner(userId);

        // ---- View stock: search product/SKU + filter package weight ----
        String search = Filters.text(q);
        String weightFilter = Filters.text(weight);

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map<String, Object> v : variants) {
            if (Filters.any(search, v.get("product_name"), v.get("sku"))
                    && Filters.same(weightFilter,
                            VariantWeights.key(v.get("weight_value")))) {
                rows.add(v);
            }
        }

        return new StockView(
                threshold,
                VariantWeights.options(variants),
                Page.of(rows, Page.parsePage(page)));
    }

    public BatchesView loadBatches(int userId,
                                   String q,
                                   String status,
                                   String page) throws SQLException {

        String search = Filters.text(q);
        String statusFilter = Filters.text(status);

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map<String, Object> b : inventoryDAO.getBatchesForOwner(userId)) {
            if (Filters.any(search, b.get("batch_code"),
                            b.get("product_name"), b.get("sku"))
                    && Filters.same(statusFilter, b.get("status"))) {
                rows.add(b);
            }
        }

        return new BatchesView(Page.of(rows, Page.parsePage(page)));
    }

    /**
     * Full variant list: the restock drop-down must show every
     * variant of the shop.
     */
    public List<Map<String, Object>> loadRestockVariants(int userId)
            throws SQLException {

        return inventoryDAO.getVariantsForOwner(userId);
    }

    public LowStockView loadLowStock(int userId,
                                     String q,
                                     String page) throws SQLException {

        double threshold = inventoryDAO.getLowStockThreshold(userId);

        // ---- Low stock: search product/SKU ----
        String search = Filters.text(q);

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map<String, Object> v
                : inventoryDAO.getLowStockForOwner(userId, threshold)) {
            if (Filters.any(search, v.get("product_name"), v.get("sku"))) {
                rows.add(v);
            }
        }

        return new LowStockView(
                threshold, Page.of(rows, Page.parsePage(page)));
    }

    public AdjustmentsView loadHistory(int userId,
                                       String q,
                                       String type,
                                       String page) throws SQLException {

        String search = Filters.text(q);
        String typeFilter = Filters.text(type);

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map<String, Object> a
                : inventoryDAO.getAdjustmentsForOwner(userId)) {
            if (Filters.any(search, a.get("product_name"), a.get("sku"),
                            a.get("batch_code"), a.get("reason"))
                    && Filters.same(typeFilter, a.get("adjustment_type"))) {
                rows.add(a);
            }
        }

        return new AdjustmentsView(Page.of(rows, Page.parsePage(page)));
    }

    // =========================================================
    // ACTIONS (POST)
    // =========================================================

    /**
     * Records a new restock batch.
     *
     * @return a message code understood by the inventory screens
     */
    public String restock(int userId,
                          String variantIdRaw,
                          String quantityRaw,
                          String batchCodeRaw,
                          String expiryRaw,
                          String reasonRaw) throws SQLException {

        int variantId = parseInt(variantIdRaw, -1);
        double quantity = parseDouble(quantityRaw, -1);
        String batchCode = trim(batchCodeRaw);
        String reason = trim(reasonRaw);

        if (variantId <= 0 || quantity <= 0 || batchCode.isEmpty()) {
            return "invalid";
        }

        if (quantity > MAX_STOCK_QUANTITY) {
            return "invalid";
        }

        if (batchCode.length() > 50 || reason.length() > MAX_REASON_LENGTH) {
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
            expiry = Date.valueOf(trim(expiryRaw));
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

    /**
     * Sets a batch quantity to an absolute value.
     */
    public String updateBatch(int userId,
                              String batchIdRaw,
                              String quantityRaw,
                              String reasonRaw) throws SQLException {

        int batchId = parseInt(batchIdRaw, -1);
        double quantity = parseDouble(quantityRaw, -1);
        String reason = trim(reasonRaw);

        if (batchId <= 0 || quantity < 0) {
            return "invalid";
        }

        if (quantity > MAX_STOCK_QUANTITY) {
            return "invalid";
        }

        if (reason.length() > MAX_REASON_LENGTH) {
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
    public String setThreshold(int userId, String thresholdRaw)
            throws SQLException {

        double threshold = parseDouble(thresholdRaw, -1);

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
     * sent (for example when SMTP is not configured). A failed
     * alert never breaks the business transaction.
     */
    public String notifyLowStock(User user) {

        try {
            int reported = LowStockNotifier.sendAlert(
                    user.getUserID(), user.getEmail(), null);

            return reported > 0 ? "notified" : "notify-none";

        } catch (MessagingException | SQLException e) {

            LOG.log(Level.WARNING,
                    "Low stock alert failed for owner "
                            + user.getUserID(), e);

            return "notify-failed";
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            double parsed = Double.parseDouble(trim(value));

            // Reject NaN / infinity so no absurd figure can be stored.
            if (Double.isNaN(parsed) || Double.isInfinite(parsed)) {
                return fallback;
            }

            return parsed;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
