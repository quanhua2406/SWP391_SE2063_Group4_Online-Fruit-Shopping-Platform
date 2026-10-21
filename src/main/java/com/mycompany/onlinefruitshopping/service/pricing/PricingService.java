package com.mycompany.onlinefruitshopping.service.pricing;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Business logic for the shop owner's normal selling prices.
 *
 * <p>Temporary offers are handled by
 * {@code service.promotion.PromotionService} instead.</p>
 */
public class PricingService {

    /**
     * Upper bound for a selling price, to reject absurd input before
     * it reaches the database.
     */
    private static final double MAX_PRICE = 1_000_000_000d;

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean hasShop(int ownerId) throws SQLException {
        return inventoryDAO.hasShop(ownerId);
    }

    /**
     * One page of the owner's price list.
     */
    public static class PriceListView {

        private final Page<Map<String, Object>> variantsPage;
        private final List<String> weightOptions;

        PriceListView(Page<Map<String, Object>> variantsPage,
                      List<String> weightOptions) {
            this.variantsPage = variantsPage;
            this.weightOptions = weightOptions;
        }

        public Page<Map<String, Object>> getVariantsPage() {
            return variantsPage;
        }

        public List<String> getWeightOptions() {
            return weightOptions;
        }
    }

    public PriceListView loadPrices(int ownerId,
                                    String q,
                                    String weight,
                                    String page) throws SQLException {

        String search = Filters.text(q);
        String weightFilter = Filters.text(weight);

        List<Map<String, Object>> all =
                inventoryDAO.getVariantsForOwner(ownerId);

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map<String, Object> v : all) {
            if (Filters.any(search, v.get("product_name"), v.get("sku"))
                    && Filters.same(weightFilter,
                            VariantWeights.key(v.get("weight_value")))) {
                rows.add(v);
            }
        }

        return new PriceListView(
                Page.of(rows, Page.parsePage(page)),
                VariantWeights.options(all));
    }

    /**
     * Sets the normal selling price of one variant.
     *
     * <p>A promotion must stay cheaper than the normal price, so the
     * normal price cannot be dropped below a running promotion.</p>
     *
     * @return a message code understood by the pricing screen
     */
    public String savePrice(int ownerId,
                            String variantIdRaw,
                            String priceRaw) throws SQLException {

        int variantId = parseInt(variantIdRaw, -1);
        double price = parseDouble(priceRaw, -1);

        // A selling price must be a real, positive amount; NaN and
        // infinity are rejected here too so they can never be stored.
        if (variantId <= 0
                || Double.isNaN(price)
                || Double.isInfinite(price)
                || price <= 0
                || price > MAX_PRICE) {
            return "invalid";
        }

        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) {
            return "forbidden";
        }

        BigDecimal promo = inventoryDAO.getLivePromoPrice(variantId);

        if (promo != null
                && BigDecimal.valueOf(price).compareTo(promo) <= 0) {
            return "promo-conflict";
        }

        return inventoryDAO.updateVariantPrice(variantId, price)
                ? "updated" : "failed";
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
            return Double.parseDouble(trim(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
