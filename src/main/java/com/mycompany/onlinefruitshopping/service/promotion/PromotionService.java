package com.mycompany.onlinefruitshopping.service.promotion;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Business logic for the promotions a shop owner sets on the
 * variants of their shop.
 *
 * <p>A promotion is a temporary, dated offer on top of the normal
 * price. Every promotion is only a request: it is stored as
 * {@code pending} and becomes effective once an administrator
 * approves it (see {@link PromotionReviewService}).</p>
 */
public class PromotionService {

    /**
     * Upper bound accepted for a price, to keep obviously wrong input
     * (or an accidental extra digit) out of the database.
     */
    private static final double MAX_PRICE = 1_000_000_000d;

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean hasShop(int ownerId) throws SQLException {
        return inventoryDAO.hasShop(ownerId);
    }

    /**
     * One page of the owner's promotion list together with the shop
     * wide counters.
     */
    public static class PromotionListView {

        private final int variantCount;
        private final int pendingCount;
        private final int approvedCount;
        private final int rejectedCount;
        private final List<String> weightOptions;
        private final Page<Map<String, Object>> variantsPage;

        PromotionListView(int variantCount,
                          int pendingCount,
                          int approvedCount,
                          int rejectedCount,
                          List<String> weightOptions,
                          Page<Map<String, Object>> variantsPage) {
            this.variantCount = variantCount;
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.rejectedCount = rejectedCount;
            this.weightOptions = weightOptions;
            this.variantsPage = variantsPage;
        }

        public int getVariantCount() {
            return variantCount;
        }

        public int getPendingCount() {
            return pendingCount;
        }

        public int getApprovedCount() {
            return approvedCount;
        }

        public int getRejectedCount() {
            return rejectedCount;
        }

        public List<String> getWeightOptions() {
            return weightOptions;
        }

        public Page<Map<String, Object>> getVariantsPage() {
            return variantsPage;
        }
    }

    public PromotionListView loadPromotions(int ownerId,
                                            String q,
                                            String weight,
                                            String approval,
                                            String page) throws SQLException {

        String search = Filters.text(q);
        String weightFilter = Filters.text(weight);
        String approvalFilter = Filters.text(approval);

        List<Map<String, Object>> variants =
                inventoryDAO.getVariantsForOwner(ownerId);

        // The counters describe the whole shop, so they are computed
        // before the search filter is applied.
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
            if (Filters.any(search, v.get("product_name"), v.get("sku"))
                    && Filters.same(weightFilter,
                            VariantWeights.key(v.get("weight_value")))
                    && Filters.same(approvalFilter, v.get("promo_status"))) {
                rows.add(v);
            }
        }

        return new PromotionListView(
                variants.size(), pending, approved, rejected,
                VariantWeights.options(variants),
                Page.of(rows, Page.parsePage(page)));
    }

    /**
     * Submits (inserts or replaces) the promotion of one variant.
     *
     * @return a message code understood by the promotion screen
     */
    public String savePromotion(int ownerId,
                                String variantIdRaw,
                                String promoPriceRaw,
                                String startDateRaw,
                                String endDateRaw) throws SQLException {

        int variantId = parseInt(variantIdRaw, -1);
        BigDecimal promoPrice = parseMoney(promoPriceRaw);
        Date startDate = parseDate(startDateRaw);
        Date endDate = parseDate(endDateRaw);

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

    /**
     * Removes the promotion of one variant. Removing is idempotent:
     * the promotion is gone either way.
     */
    public String removePromotion(int ownerId, String variantIdRaw)
            throws SQLException {

        int variantId = parseInt(variantIdRaw, -1);

        if (variantId <= 0) {
            return "invalid";
        }

        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) {
            return "forbidden";
        }

        inventoryDAO.removePromotion(variantId, ownerId);
        return "promo-removed";
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

    private BigDecimal parseMoney(String value) {
        try {
            return new BigDecimal(trim(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseDate(String value) {
        try {
            return Date.valueOf(trim(value));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
