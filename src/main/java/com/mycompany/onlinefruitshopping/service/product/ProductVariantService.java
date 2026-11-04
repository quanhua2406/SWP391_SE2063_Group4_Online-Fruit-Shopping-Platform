package com.mycompany.onlinefruitshopping.service.product;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.model.ProductVariant;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data for the shop owner's variant screen: the variants of the whole
 * shop, the shop's products and the quality grades.
 */
public class ProductVariantService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public List<ProductVariant> shopVariants(int ownerId) throws SQLException {

        List<ProductVariant> list = new ArrayList<>();

        for (Map<String, Object> row : inventoryDAO.getVariantsForOwner(ownerId)) {

            ProductVariant v = new ProductVariant();
            Object varIdObj = row.get("variant_id");
            if (varIdObj instanceof Number n) {
                v.setVariantId(n.intValue());
            }
            Object prodIdObj = row.get("product_id");
            if (prodIdObj instanceof Number n) {
                v.setProductId(n.intValue());
            }
            v.setProductName((String) row.get("product_name"));
            v.setSku((String) row.get("sku"));
            v.setUnit((String) row.get("unit"));

            Object weight = row.get("weight_value");
            if (weight instanceof Number n) {
                v.setWeightValue(n.doubleValue());
            }

            v.setPackagingType((String) row.get("packaging_type"));
            Object grade = row.get("quality_grade");
            v.setQualityGrade(grade != null ? grade.toString() : "1");
            v.setPrice(toDouble(row.get("price")));
            v.setStock(toDouble(row.get("stock")));
            list.add(v);
        }

        return list;
    }

    public List<Map<String, Object>> shopProducts(int ownerId) throws SQLException {
        return inventoryDAO.getProductsForPricing(ownerId);
    }

    public List<Map<String, Object>> qualityGrades() throws SQLException {
        return inventoryDAO.getQualityGrades();
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }
}
