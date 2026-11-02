package com.mycompany.onlinefruitshopping.service.product;

import com.mycompany.onlinefruitshopping.dao.product.AdminProductDAO;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Business logic of the administrator's product administration:
 * listing, creating, editing, publishing / hiding, deleting and
 * managing the variants of a product.
 *
 * <p>All database work is delegated to {@link AdminProductDAO}; the
 * validation and the message decisions stay here.</p>
 */
public class AdminProductService {

    /**
     * Upper bound for a selling price, to reject absurd input before
     * it reaches the database.
     */
    private static final BigDecimal MAX_PRICE =
            new BigDecimal("1000000000");

    /** {@code products.origin} is NVARCHAR(100). */
    private static final int MAX_ORIGIN = 100;

    /** {@code product_variants.packaging_type} is NVARCHAR(50). */
    private static final int MAX_PACKAGING_TYPE = 50;

    private final AdminProductDAO productDAO = new AdminProductDAO();

    // =========================================================
    // VIEW MODELS
    // =========================================================

    /**
     * One page of the product list, with the data the filter bar
     * needs.
     */
    public static class ProductListView {

        private final List<Map<String, Object>> products;
        private final Page<Map<String, Object>> productsPage;
        private final TreeSet<String> categoryOptions;
        private final String keyword;

        ProductListView(List<Map<String, Object>> products,
                        Page<Map<String, Object>> productsPage,
                        TreeSet<String> categoryOptions,
                        String keyword) {
            this.products = products;
            this.productsPage = productsPage;
            this.categoryOptions = categoryOptions;
            this.keyword = keyword;
        }

        public List<Map<String, Object>> getProducts() {
            return products;
        }

        public Page<Map<String, Object>> getProductsPage() {
            return productsPage;
        }

        public TreeSet<String> getCategoryOptions() {
            return categoryOptions;
        }

        public String getKeyword() {
            return keyword;
        }
    }

    /**
     * One page of the variants of a product.
     */
    public static class VariantListView {

        private final Map<String, Object> product;
        private final List<Map<String, Object>> variants;
        private final Page<Map<String, Object>> variantsPage;
        private final List<String> weightOptions;

        VariantListView(Map<String, Object> product,
                        List<Map<String, Object>> variants,
                        Page<Map<String, Object>> variantsPage,
                        List<String> weightOptions) {
            this.product = product;
            this.variants = variants;
            this.variantsPage = variantsPage;
            this.weightOptions = weightOptions;
        }

        public Map<String, Object> getProduct() {
            return product;
        }

        public List<Map<String, Object>> getVariants() {
            return variants;
        }

        public Page<Map<String, Object>> getVariantsPage() {
            return variantsPage;
        }

        public List<String> getWeightOptions() {
            return weightOptions;
        }
    }

    /**
     * The outcome of a save: {@code success}, or {@code error} holding
     * the message shown back on the form.
     */
    public static class SaveResult {

        private final boolean success;
        private final String error;

        private SaveResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        static SaveResult ok() {
            return new SaveResult(true, null);
        }

        static SaveResult error(String message) {
            return new SaveResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }

    // =========================================================
    // LIST
    // =========================================================

    public ProductListView loadProducts(String keyword,
                                        String status,
                                        String category,
                                        String page) throws SQLException {

        List<Map<String, Object>> products =
                productDAO.getAllProducts(keyword);

        // Category options for the filter come from the loaded rows,
        // so no extra query is needed.
        TreeSet<String> categoryOptions = new TreeSet<>();

        for (Map<String, Object> row : products) {
            if (row.get("category_name") != null) {
                categoryOptions.add(row.get("category_name").toString());
            }
        }

        String statusFilter = Filters.text(status);
        String categoryFilter = Filters.text(category);

        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> row : products) {
            if (Filters.same(statusFilter, row.get("status"))
                    && Filters.same(categoryFilter, row.get("category_name"))) {
                filtered.add(row);
            }
        }

        return new ProductListView(
                products,
                Page.of(filtered, Page.parsePage(page)),
                categoryOptions,
                keyword == null ? "" : keyword);
    }

    // =========================================================
    // FORM DATA
    // =========================================================

    public List<Map<String, Object>> getCategories() throws SQLException {
        return productDAO.getCategories();
    }

    public List<Map<String, Object>> getShops() throws SQLException {
        return productDAO.getShops();
    }

    public Map<String, Object> getProductForEdit(int productId)
            throws SQLException {

        return productDAO.getProductForEdit(productId);
    }

    // =========================================================
    // CREATE / UPDATE
    // =========================================================

    /**
     * Creates a product together with its first variant. The weight
     * of that first variant defines the package that is sold.
     */
    public SaveResult createProduct(String shopIdRaw,
                                    String categoryIdRaw,
                                    String productNameRaw,
                                    String descriptionRaw,
                                    String originRaw,
                                    String skuRaw,
                                    String weightValueRaw,
                                    String packagingTypeRaw,
                                    String priceRaw,
                                    String commissionRateRaw,
                                    String imageUrlRaw) throws SQLException {

        String productName = trim(productNameRaw);
        String description = trim(descriptionRaw);
        String origin = trim(originRaw);
        String sku = trim(skuRaw);
        String weightText = trim(weightValueRaw);
        String packagingType = trim(packagingTypeRaw);
        String priceText = trim(priceRaw);
        String commissionText = trim(commissionRateRaw);
        String imageUrl = trim(imageUrlRaw);
        String shopText = trim(shopIdRaw);
        String categoryText = trim(categoryIdRaw);

        if (shopText.isEmpty() || categoryText.isEmpty()
                || productName.isEmpty() || sku.isEmpty()
                || priceText.isEmpty() || weightText.isEmpty()) {

            return SaveResult.error(
                    "Please fill in all required fields.");
        }

        int shopId;
        int categoryId;
        BigDecimal price;
        BigDecimal commissionRate;
        BigDecimal weightValue;

        try {
            shopId = Integer.parseInt(shopText);
            categoryId = Integer.parseInt(categoryText);
            price = new BigDecimal(priceText);
            commissionRate = commissionText.isEmpty()
                    ? AdminProductDAO.DEFAULT_COMMISSION_RATE
                    : new BigDecimal(commissionText);
            weightValue = new BigDecimal(weightText);

        } catch (NumberFormatException e) {
            return SaveResult.error(
                    "Shop, category, price, weight or commission has "
                    + "an invalid format.");
        }

        if (shopId <= 0
                || categoryId <= 0
                || productName.length() > 150
                || sku.length() > 50
                || price.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(MAX_PRICE) > 0
                || commissionRate.compareTo(BigDecimal.ZERO) < 0
                || commissionRate.compareTo(new BigDecimal("100")) > 0
                || weightValue.compareTo(BigDecimal.ZERO) <= 0
                || weightValue.compareTo(MAX_PRICE) > 0
                || origin.length() > MAX_ORIGIN
                || packagingType.length() > MAX_PACKAGING_TYPE) {

            return SaveResult.error(
                    "Shop, category, product name, SKU, price, "
                    + "weight or commission (0-100) is invalid.");
        }

        // The chosen shop and category must really exist before the
        // product (and its first variant) is written.
        if (!productDAO.shopExists(shopId)
                || !productDAO.categoryExists(categoryId)) {

            return SaveResult.error(
                    "The selected shop or category does not exist.");
        }

        boolean created = productDAO.createProduct(
                shopId,
                categoryId,
                productName,
                description.isEmpty() ? null : description,
                origin.isEmpty() ? null : origin,
                sku,
                "kg",
                weightValue,
                packagingType.isEmpty() ? null : packagingType,
                price,
                commissionRate,
                imageUrl.isEmpty() ? null : imageUrl);

        return created
                ? SaveResult.ok()
                : SaveResult.error("Product could not be created.");
    }

    /**
     * Updates the editable fields of a product.
     */
    public SaveResult updateProduct(String productIdRaw,
                                    String categoryIdRaw,
                                    String productNameRaw,
                                    String descriptionRaw,
                                    String originRaw,
                                    String statusRaw,
                                    String commissionRateRaw)
            throws SQLException {

        int productId = parseInt(productIdRaw, -1);
        int categoryId = parseInt(categoryIdRaw, -1);

        String productName = trim(productNameRaw);
        String description = trim(descriptionRaw);
        String origin = trim(originRaw);
        String status = trim(statusRaw);
        String commissionText = trim(commissionRateRaw);

        BigDecimal commissionRate;

        try {
            commissionRate = commissionText.isEmpty()
                    ? AdminProductDAO.DEFAULT_COMMISSION_RATE
                    : new BigDecimal(commissionText);

        } catch (NumberFormatException e) {
            return SaveResult.error("Commission has an invalid format.");
        }

        boolean validStatus = "draft".equals(status)
                || "published".equals(status)
                || "hidden".equals(status);

        if (productId <= 0
                || categoryId <= 0
                || productName.isEmpty()
                || productName.length() > 150
                || origin.length() > MAX_ORIGIN
                || commissionRate.compareTo(BigDecimal.ZERO) < 0
                || commissionRate.compareTo(new BigDecimal("100")) > 0
                || !validStatus) {

            return SaveResult.error(
                    "Category, product name, status and commission "
                    + "(0-100) are invalid.");
        }

        // The category must really exist before the product is saved.
        if (!productDAO.categoryExists(categoryId)) {
            return SaveResult.error(
                    "The selected category does not exist.");
        }

        boolean updated = productDAO.updateProduct(
                productId,
                categoryId,
                productName,
                description.isEmpty() ? null : description,
                origin.isEmpty() ? null : origin,
                commissionRate,
                status);

        return updated
                ? SaveResult.ok()
                : SaveResult.error("Product could not be updated.");
    }

    // =========================================================
    // STATUS / DELETE
    // =========================================================

    public String deleteProduct(String productIdRaw) throws SQLException {

        int productId = parseInt(productIdRaw, -1);

        return productId <= 0 ? "invalid" : productDAO.deleteProduct(productId);
    }

    public String changeStatus(String productIdRaw, String statusRaw)
            throws SQLException {

        int productId = parseInt(productIdRaw, -1);
        String status = statusRaw == null ? "" : statusRaw.trim();

        boolean validStatus = "published".equals(status)
                || "hidden".equals(status)
                || "draft".equals(status);

        if (productId <= 0 || !validStatus) {
            return "invalid";
        }

        return productDAO.updateProductStatus(productId, status)
                ? "status_" + status
                : "failed";
    }

    // =========================================================
    // VARIANTS
    // =========================================================

    /**
     * Loads the variants of a product. When the product does not
     * exist, {@link VariantListView#getProduct()} is {@code null} and
     * the controller sends the admin back to the product list.
     */
    public VariantListView loadVariants(int productId,
                                        String q,
                                        String weight,
                                        String page) throws SQLException {

        Map<String, Object> product = productDAO.getProductForEdit(productId);

        if (product == null) {
            return new VariantListView(null, List.of(), Page.of(List.of(), 1),
                    List.of());
        }

        String search = Filters.text(q);
        String weightFilter = Filters.text(weight);

        List<Map<String, Object>> variants =
                productDAO.getVariantsByProduct(productId);

        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> v : variants) {
            if (Filters.any(search, v.get("sku"), v.get("packaging_type"))
                    && Filters.same(weightFilter,
                            VariantWeights.key(v.get("weight_value")))) {
                filtered.add(v);
            }
        }

        return new VariantListView(
                product,
                variants,
                Page.of(filtered, Page.parsePage(page)),
                VariantWeights.options(variants));
    }

    public String addVariant(int productId,
                             String skuRaw,
                             String priceRaw,
                             String weightRaw,
                             String packagingTypeRaw) throws SQLException {

        if (productId <= 0) {
            return "invalid";
        }

        String sku = trim(skuRaw);
        BigDecimal price = parseDecimal(priceRaw);
        BigDecimal weight = parseDecimal(weightRaw);

        // A variant is one selling package, so its weight is what
        // identifies it and is therefore mandatory.
        if (sku.isEmpty() || sku.length() > 50
                || price == null
                || price.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(MAX_PRICE) > 0
                || weight == null
                || weight.compareTo(BigDecimal.ZERO) <= 0
                || weight.compareTo(MAX_PRICE) > 0
                || trim(packagingTypeRaw).length() > MAX_PACKAGING_TYPE) {

            return "invalid";
        }

        if (productDAO.skuExists(sku, 0)) {
            return "duplicate";
        }

        boolean ok = productDAO.addVariant(
                productId,
                sku,
                VariantWeights.UNIT,
                weight,
                trimOrNull(packagingTypeRaw),
                price);

        return ok ? "created" : "failed";
    }

    public String updateVariant(String variantIdRaw,
                                String skuRaw,
                                String priceRaw,
                                String weightRaw,
                                String packagingTypeRaw) throws SQLException {

        int variantId = parseInt(variantIdRaw, -1);

        String sku = trim(skuRaw);
        BigDecimal price = parseDecimal(priceRaw);
        BigDecimal weight = parseDecimal(weightRaw);

        if (variantId <= 0
                || sku.isEmpty() || sku.length() > 50
                || price == null
                || price.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(MAX_PRICE) > 0
                || weight == null
                || weight.compareTo(BigDecimal.ZERO) <= 0
                || weight.compareTo(MAX_PRICE) > 0
                || trim(packagingTypeRaw).length() > MAX_PACKAGING_TYPE) {

            return "invalid";
        }

        if (productDAO.skuExists(sku, variantId)) {
            return "duplicate";
        }

        boolean ok = productDAO.updateVariant(
                variantId,
                sku,
                VariantWeights.UNIT,
                weight,
                trimOrNull(packagingTypeRaw),
                price);

        return ok ? "updated" : "failed";
    }

    public String deleteVariant(String variantIdRaw) throws SQLException {

        int variantId = parseInt(variantIdRaw, -1);

        return variantId <= 0 ? "invalid" : productDAO.deleteVariant(variantId);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private BigDecimal parseDecimal(String value) {
        String text = trim(value);

        if (text.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimOrNull(String value) {
        String text = trim(value);
        return text.isEmpty() ? null : text;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
