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

/** Dated variant-price requests, approved through PromotionReviewService. */
public class VariantPromotionService {
    private static final double MAX_PRICE = 1_000_000_000d;
    private final InventoryDAO inventoryDAO;

    public VariantPromotionService() { this(new InventoryDAO()); }
    VariantPromotionService(InventoryDAO inventoryDAO) {
        this.inventoryDAO = java.util.Objects.requireNonNull(inventoryDAO);
    }

    public boolean hasShop(int ownerId) throws SQLException {
        return inventoryDAO.hasShop(ownerId);
    }

    public static class PromotionListView {
        private final int variantCount, pendingCount, approvedCount, rejectedCount;
        private final List<String> weightOptions;
        private final Page<Map<String, Object>> variantsPage;

        PromotionListView(int variantCount, int pendingCount, int approvedCount,
                int rejectedCount, List<String> weightOptions,
                Page<Map<String, Object>> variantsPage) {
            this.variantCount = variantCount;
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.rejectedCount = rejectedCount;
            this.weightOptions = weightOptions;
            this.variantsPage = variantsPage;
        }
        public int getVariantCount() { return variantCount; }
        public int getPendingCount() { return pendingCount; }
        public int getApprovedCount() { return approvedCount; }
        public int getRejectedCount() { return rejectedCount; }
        public List<String> getWeightOptions() { return weightOptions; }
        public Page<Map<String, Object>> getVariantsPage() { return variantsPage; }
    }

    public PromotionListView loadPromotions(int ownerId, String q, String weight,
            String approval, String page) throws SQLException {
        String search = Filters.text(q);
        String weightFilter = Filters.text(weight);
        String approvalFilter = Filters.text(approval);
        List<Map<String, Object>> variants = inventoryDAO.getVariantsForOwner(ownerId);
        int pending = 0, approved = 0, rejected = 0;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> v : variants) {
            String status = v.get("promo_status") == null ? "" : v.get("promo_status").toString();
            if ("approved".equals(status)) approved++;
            else if ("rejected".equals(status)) rejected++;
            else if ("pending".equals(status)) pending++;
            if (Filters.any(search, v.get("product_name"), v.get("sku"))
                    && Filters.same(weightFilter, VariantWeights.key(v.get("weight_value")))
                    && Filters.same(approvalFilter, v.get("promo_status"))) rows.add(v);
        }
        return new PromotionListView(variants.size(), pending, approved, rejected,
                VariantWeights.options(variants), Page.of(rows, Page.parsePage(page)));
    }

    public String savePromotion(int ownerId, String variantIdRaw, String promoPriceRaw,
            String startDateRaw, String endDateRaw) throws SQLException {
        int variantId = parseInt(variantIdRaw);
        BigDecimal promoPrice = parseMoney(promoPriceRaw);
        Date startDate = parseDate(startDateRaw), endDate = parseDate(endDateRaw);
        if (variantId <= 0 || promoPrice == null || startDate == null || endDate == null)
            return "promo-invalid";
        if (promoPrice.compareTo(BigDecimal.valueOf(MAX_PRICE)) > 0) return "promo-invalid";
        if (endDate.before(startDate)) return "promo-invalid";
        if (endDate.before(Date.valueOf(LocalDate.now()))) return "promo-expired";
        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) return "forbidden";
        Double price = inventoryDAO.getVariantPrice(variantId);
        if (price == null) return "failed";
        if (promoPrice.compareTo(BigDecimal.valueOf(price)) >= 0) return "promo-not-discount";
        return inventoryDAO.setPromotion(variantId, promoPrice, startDate, endDate, ownerId)
                ? "promo-set" : "failed";
    }

    public String removePromotion(int ownerId, String variantIdRaw) throws SQLException {
        int variantId = parseInt(variantIdRaw);
        if (variantId <= 0) return "invalid";
        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) return "forbidden";
        inventoryDAO.removePromotion(variantId, ownerId);
        return "promo-removed";
    }
    private int parseInt(String value) {
        try { return Integer.parseInt(trim(value)); }
        catch (NumberFormatException e) { return -1; }
    }
    private BigDecimal parseMoney(String value) {
        try { return new BigDecimal(trim(value)); }
        catch (NumberFormatException e) { return null; }
    }
    private Date parseDate(String value) {
        try { return Date.valueOf(trim(value)); }
        catch (IllegalArgumentException e) { return null; }
    }
    private String trim(String value) { return value == null ? "" : value.trim(); }
}
