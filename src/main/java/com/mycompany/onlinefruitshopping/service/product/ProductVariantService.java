package com.mycompany.onlinefruitshopping.service.product;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.model.ProductVariant;

import java.math.BigDecimal;
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
            v.setVariantId((Integer) row.get("variant_id"));
            v.setProductId((Integer) row.get("product_id"));
            v.setProductName((String) row.get("product_name"));
            v.setSku((String) row.get("sku"));
            v.setUnit((String) row.get("unit"));

            BigDecimal weight = (BigDecimal) row.get("weight_value");
            if (weight != null) {
                v.setWeightValue(weight.doubleValue());
            }

            v.setPackagingType((String) row.get("packaging_type"));
            v.setQualityGrade((String) row.get("quality_grade"));
            v.setPrice(((BigDecimal) row.get("price")).doubleValue());
            v.setStock(((BigDecimal) row.get("stock")).doubleValue());
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
}
