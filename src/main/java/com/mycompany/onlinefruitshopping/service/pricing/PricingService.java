package com.mycompany.onlinefruitshopping.service.pricing;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for the shop owner's normal selling prices.
 *
 * <p>A variant with a weight is priced automatically when its product
 * has a price per kg: {@code price = price_per_kg x weight x factor},
 * where the factor belongs to the variant's quality grade. Other
 * variants keep a manually set price.</p>
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

    private static final BigDecimal MAX_PRICE_PER_KG = new BigDecimal("100000000");

    /** Automatic prices are rounded to the nearest thousand dong. */
    private static final BigDecimal ROUNDING_UNIT = new BigDecimal("1000");

    private static final int HISTORY_LIMIT = 30;

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean hasShop(int ownerId) throws SQLException {
        return inventoryDAO.hasShop(ownerId);
    }

    /**
     * One page of the owner's price list, plus the data the screen needs.
     */
    public static class PriceListView {

        private final Page<Map<String, Object>> variantsPage;
        private final List<String> weightOptions;
        private final List<Map<String, Object>> qualityGrades;
        private final List<Map<String, Object>> products;
        private final List<Map<String, Object>> history;

        PriceListView(Page<Map<String, Object>> variantsPage,
                      List<String> weightOptions,
                      List<Map<String, Object>> qualityGrades,
                      List<Map<String, Object>> products,
                      List<Map<String, Object>> history) {
            this.variantsPage = variantsPage;
            this.weightOptions = weightOptions;
            this.qualityGrades = qualityGrades;
            this.products = products;
            this.history = history;
        }

        public Page<Map<String, Object>> getVariantsPage() {
            return variantsPage;
        }

        public List<String> getWeightOptions() {
            return weightOptions;
        }

        public List<Map<String, Object>> getQualityGrades() {
            return qualityGrades;
        }

        public List<Map<String, Object>> getProducts() {
            return products;
        }

        public List<Map<String, Object>> getHistory() {
            return history;
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
        List<Map<String, Object>> products =
                inventoryDAO.getProductsForPricing(ownerId);

        Map<Integer, BigDecimal> kgByProduct = new HashMap<>();
        for (Map<String, Object> product : products) {
            kgByProduct.put((Integer) product.get("product_id"),
                    (BigDecimal) product.get("price_per_kg"));
        }

        for (Map<String, Object> v : all) {
            BigDecimal kg = kgByProduct.get((Integer) v.get("product_id"));
            v.put("auto", isAuto(kg, (BigDecimal) v.get("weight_value")));
        }

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
                VariantWeights.options(all),
                inventoryDAO.getQualityGrades(),
                products,
                inventoryDAO.getPriceHistoryForOwner(ownerId, HISTORY_LIMIT));
    }

    /**
     * Sets the manual selling price of one variant.
     *
     * <p>A promotion must stay cheaper than the normal price, so the
     * normal price cannot be dropped below a running promotion. An
     * automatically priced variant cannot be set by hand.</p>
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

        Map<String, Object> variant = inventoryDAO.getVariantForPricing(variantId);

        if (variant != null && isAuto(
                (BigDecimal) variant.get("price_per_kg"),
                (BigDecimal) variant.get("weight_value"))) {
            return "auto-priced";
        }

        BigDecimal newPrice = BigDecimal.valueOf(price);
        BigDecimal promo = inventoryDAO.getLivePromoPrice(variantId);

        if (promo != null && newPrice.compareTo(promo) <= 0) {
            return "promo-conflict";
        }

        return inventoryDAO.savePriceWithHistory(variantId, newPrice, "manual", ownerId)
                ? "updated" : "failed";
    }

    /**
     * Sets the quality grade of one variant. If the variant is priced
     * automatically, its price is recalculated with the new factor.
     */
    public String saveGrade(int ownerId,
                            String variantIdRaw,
                            String gradeRaw) throws SQLException {

        int variantId = parseInt(variantIdRaw, -1);
        String grade = trim(gradeRaw);

        Map<String, BigDecimal> factors = factors();

        if (variantId <= 0 || !factors.containsKey(grade)) {
            return "invalid-grade";
        }

        if (!inventoryDAO.variantBelongsToOwner(variantId, ownerId)) {
            return "forbidden";
        }

        Map<String, Object> variant = inventoryDAO.getVariantForPricing(variantId);
        if (variant == null) {
            return "forbidden";
        }

        BigDecimal newPrice = null;

        if (isAuto((BigDecimal) variant.get("price_per_kg"),
                (BigDecimal) variant.get("weight_value"))) {

            newPrice = calculate(
                    (BigDecimal) variant.get("price_per_kg"),
                    (BigDecimal) variant.get("weight_value"),
                    factors.get(grade));

            BigDecimal promo = inventoryDAO.getLivePromoPrice(variantId);
            if (promo != null && newPrice.compareTo(promo) <= 0) {
                return "promo-conflict";
            }
        }

        return inventoryDAO.saveGradeWithHistory(variantId, grade, newPrice, ownerId)
                ? "grade-saved" : "failed";
    }

    /**
     * Sets the price per kg of a product and recalculates the price of
     * every variant that has a weight.
     */
    public String savePricePerKg(int ownerId,
                                 String productIdRaw,
                                 String pricePerKgRaw) throws SQLException {

        int productId = parseInt(productIdRaw, -1);
        BigDecimal pricePerKg = parseBigDecimal(pricePerKgRaw);

        if (productId <= 0
                || pricePerKg == null
                || pricePerKg.signum() <= 0
                || pricePerKg.compareTo(MAX_PRICE_PER_KG) > 0) {
            return "invalid-kg";
        }

        if (!inventoryDAO.productBelongsToOwner(productId, ownerId)) {
            return "forbidden";
        }

        Map<String, BigDecimal> factors = factors();
        List<InventoryDAO.PriceChange> changes = new ArrayList<>();

        for (Map<String, Object> variant
                : inventoryDAO.getVariantsForPricingByProduct(productId)) {

            BigDecimal weight = (BigDecimal) variant.get("weight_value");
            if (weight == null || weight.signum() <= 0) {
                continue;
            }

            int variantId = (Integer) variant.get("variant_id");
            BigDecimal factor = factors.get((String) variant.get("quality_grade"));
            if (factor == null) {
                return "invalid-grade";
            }

            BigDecimal newPrice = calculate(pricePerKg, weight, factor);

            BigDecimal promo = inventoryDAO.getLivePromoPrice(variantId);
            if (promo != null && newPrice.compareTo(promo) <= 0) {
                return "promo-conflict";
            }

            BigDecimal oldPrice = (BigDecimal) variant.get("price");
            if (oldPrice == null || oldPrice.compareTo(newPrice) != 0) {
                changes.add(new InventoryDAO.PriceChange(variantId, oldPrice, newPrice));
            }
        }

        return inventoryDAO.saveProductPricePerKg(productId, pricePerKg, changes, ownerId)
                ? "kg-saved" : "failed";
    }

    /**
     * The automatic price of a variant, or {@code null} when the variant
     * is priced by hand (the product has no price per kg, or the
     * variant has no weight).
     */
    public BigDecimal autoPrice(int productId, Double weight, String grade)
            throws SQLException {

        BigDecimal pricePerKg = inventoryDAO.getProductPricePerKg(productId);
        BigDecimal factor = factors().get(grade);

        if (pricePerKg == null || weight == null || weight <= 0 || factor == null) {
            return null;
        }

        return calculate(pricePerKg, BigDecimal.valueOf(weight), factor);
    }

    /** The grade as submitted if it exists, otherwise the default grade "1". */
    public String normalizeGrade(String grade) throws SQLException {
        return grade != null && factors().containsKey(grade) ? grade : "1";
    }

    /** The automatic price when the variant has one, otherwise the entered price. */
    public BigDecimal resolveVariantPrice(int productId,
                                          Double weight,
                                          String grade,
                                          double enteredPrice) throws SQLException {

        BigDecimal auto = autoPrice(productId, weight, grade);

        return auto != null ? auto : BigDecimal.valueOf(enteredPrice);
    }

    /** Records a price change made on the variant form. */
    public void recordVariantPriceChange(int variantId,
                                         double oldPrice,
                                         double newPrice,
                                         int changedBy) throws SQLException {

        if (Double.compare(oldPrice, newPrice) != 0) {
            inventoryDAO.recordPriceHistory(variantId,
                    BigDecimal.valueOf(oldPrice),
                    BigDecimal.valueOf(newPrice),
                    "variant_form",
                    changedBy);
        }
    }

    /** Quality grade factors by grade code. */
    public Map<String, BigDecimal> factors() throws SQLException {
        Map<String, BigDecimal> factors = new HashMap<>();
        for (Map<String, Object> grade : inventoryDAO.getQualityGrades()) {
            factors.put((String) grade.get("grade"), (BigDecimal) grade.get("factor"));
        }
        return factors;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean isAuto(BigDecimal pricePerKg, BigDecimal weight) {
        return pricePerKg != null && weight != null && weight.signum() > 0;
    }

    private BigDecimal calculate(BigDecimal pricePerKg,
                                 BigDecimal weight,
                                 BigDecimal factor) {
        BigDecimal raw = pricePerKg.multiply(weight).multiply(factor);

        return raw.divide(ROUNDING_UNIT, 0, RoundingMode.HALF_UP)
                .multiply(ROUNDING_UNIT);
    }

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

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(trim(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
