package com.mycompany.onlinefruitshopping.service.shop;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.dao.product.ProductDAO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Business logic of the shop owner's shop itself: creating it and
 * summarising it on the dashboard.
 *
 * <p>Only one shop per owner is allowed.</p>
 */
public class ShopService {

    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * True when the owner already has a shop.
     */
    public boolean hasShop(int ownerId) throws SQLException {
        return productDAO.getShopForOwner(ownerId) != null;
    }

    /**
     * Creates the shop linked to the owner's account.
     *
     * @return {@code true} when the shop was created, {@code false}
     *         when the owner already has one (or someone raced them
     *         to it)
     */
    public boolean createShop(int ownerId,
                              String shopName,
                              String description) throws SQLException {

        return productDAO.createShopForOwner(ownerId, shopName, description);
    }

    /**
     * Validates the create-shop form.
     *
     * @return the message to show, or {@code null} when the input is
     *         valid
     */
    public String validateShop(String shopName, String description) {

        if (shopName == null || shopName.isBlank()) {
            return "Shop name cannot be empty.";
        }

        if (shopName.length() < 2 || shopName.length() > 150) {
            return "Shop name must be between 2 and 150 characters.";
        }

        if (description != null && description.length() > 255) {
            return "Description cannot exceed 255 characters.";
        }

        return null;
    }

    /**
     * The dashboard summary: whether the owner has a shop, its name
     * and a small set of stock statistics.
     */
    public static class Dashboard {

        private final boolean hasShop;
        private final String shopName;
        private final Map<String, Object> stats;

        Dashboard(boolean hasShop,
                  String shopName,
                  Map<String, Object> stats) {
            this.hasShop = hasShop;
            this.shopName = shopName;
            this.stats = stats;
        }

        public boolean isHasShop() {
            return hasShop;
        }

        public String getShopName() {
            return shopName;
        }

        public Map<String, Object> getStats() {
            return stats;
        }
    }

    public Dashboard loadDashboard(int ownerId) throws SQLException {

        ProductDAO.ShopInfo shop = productDAO.getShopForOwner(ownerId);

        Map<String, Object> stats = Collections.emptyMap();

        if (shop != null) {
            stats = inventoryDAO.getDashboardStats(ownerId);
        }

        return new Dashboard(shop != null,
                shop == null ? null : shop.getShopName(),
                stats);
    }
}
