package com.mycompany.onlinefruitshopping.service.promotion;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Business logic behind the administrator's promotion review queue.
 *
 * <p>A promotion submitted by a shop owner stays {@code pending}
 * until it is approved here; approving makes the promotional price
 * effective inside its start / end date range, refusing keeps the
 * variant on its normal price and shows the reason back to the shop
 * owner.</p>
 */
public class PromotionReviewService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * The review queue, with counters describing every request (not
     * only the rows the current filter keeps).
     */
    public static class ReviewQueue {

        private final int pendingCount;
        private final int approvedCount;
        private final int rejectedCount;
        private final int totalCount;
        private final Page<Map<String, Object>> promotionsPage;

        ReviewQueue(int pendingCount,
                    int approvedCount,
                    int rejectedCount,
                    int totalCount,
                    Page<Map<String, Object>> promotionsPage) {
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.rejectedCount = rejectedCount;
            this.totalCount = totalCount;
            this.promotionsPage = promotionsPage;
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

        public int getTotalCount() {
            return totalCount;
        }

        public Page<Map<String, Object>> getPromotionsPage() {
            return promotionsPage;
        }
    }

    public ReviewQueue loadQueue(String status,
                                 String q,
                                 String page) throws SQLException {

        String statusFilter = Filters.text(status);
        String search = Filters.text(q);

        List<Map<String, Object>> all =
                inventoryDAO.getPromotionsForAdmin();

        int pending = 0;
        int approved = 0;
        int rejected = 0;

        for (Map<String, Object> row : all) {
            String value = row.get("status") == null
                    ? ""
                    : row.get("status").toString();

            if ("approved".equals(value)) {
                approved++;
            } else if ("rejected".equals(value)) {
                rejected++;
            } else {
                pending++;
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map<String, Object> row : all) {
            if (Filters.same(statusFilter, row.get("status"))
                    && Filters.any(search,
                            row.get("product_name"),
                            row.get("sku"),
                            row.get("shop_name"))) {
                rows.add(row);
            }
        }

        return new ReviewQueue(pending, approved, rejected, all.size(),
                Page.of(rows, Page.parsePage(page)));
    }

    /**
     * Approves a pending promotion.
     *
     * <p>A promotion whose time window is already over cannot be
     * made effective, so it is refused instead of approved. The
     * offer must also still be a discount: the shop owner may have
     * lowered the normal price while the request waited.</p>
     *
     * @return a message code understood by the review screen
     */
    public String approve(int adminId, String promoIdRaw)
            throws SQLException {

        int promoId = parseInt(promoIdRaw, -1);

        if (promoId <= 0) {
            return "invalid";
        }

        Map<String, Object> promo =
                inventoryDAO.getPromotionForReview(promoId);

        if (promo == null) {
            return "invalid";
        }

        if (!"pending".equals(String.valueOf(promo.get("status")))) {
            return "not-pending";
        }

        Date endDate = (Date) promo.get("end_date");

        if (endDate != null
                && endDate.toLocalDate().isBefore(LocalDate.now())) {
            return "approve-expired";
        }

        BigDecimal promoPrice = (BigDecimal) promo.get("promo_price");
        BigDecimal price = (BigDecimal) promo.get("price");

        if (promoPrice == null || price == null
                || promoPrice.compareTo(price) >= 0) {
            return "approve-not-discount";
        }

        return inventoryDAO.reviewPromotion(
                promoId, "approved", null, adminId)
                ? "approved" : "not-pending";
    }

    /**
     * Refuses a pending promotion, optionally with a short reason.
     */
    public String reject(int adminId,
                         String promoIdRaw,
                         String note) throws SQLException {

        int promoId = parseInt(promoIdRaw, -1);

        if (promoId <= 0) {
            return "invalid";
        }

        if (note != null && note.length() > 255) {
            note = note.substring(0, 255);
        }

        return inventoryDAO.reviewPromotion(
                promoId, "rejected", note, adminId)
                ? "rejected" : "not-pending";
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
