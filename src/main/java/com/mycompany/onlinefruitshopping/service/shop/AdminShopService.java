package com.mycompany.onlinefruitshopping.service.shop;

import com.mycompany.onlinefruitshopping.dao.shop.AdminShopDAO;
import com.mycompany.onlinefruitshopping.dao.shop.AdminShopDAO.ShopInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Business logic of the administrator's shop-owner management (18.0):
 * viewing the shops and blocking / unblocking a shop.
 *
 * <p>Blocking a shop is more than locking one account: the shop is
 * suspended and its products are taken off sale. All those decisions,
 * plus the input validation, live here on top of {@link AdminShopDAO}.
 * The controller only reads the request and renders the response.</p>
 */
public class AdminShopService {

    /** Statuses a shop may be filtered by. */
    public static final Set<String> ALLOWED_STATUSES = Set.of(
            "active",
            "suspended",
            "pending"
    );

    private final AdminShopDAO shopDAO = new AdminShopDAO();

    // =========================================================
    // LIST / READ
    // =========================================================

    /**
     * Platform-wide counters for the dashboard card (every shop by
     * status, and how many products are on sale).
     */
    public com.mycompany.onlinefruitshopping.dao.shop.AdminShopDAO.ShopStatistics
            getStatistics() throws SQLException {
        return shopDAO.getStatistics();
    }

    /**
     * Filters the shop list.
     *
     * @param status {@code null} / blank / {@code all} means "no status
     *               filter"; any other value must be a known status
     * @return the matching shops, or {@code null} when the status is
     *         not recognised (the controller answers 400)
     */
    public List<ShopInfo> searchShops(String keyword, String status)
            throws SQLException {

        String normalized = trim(status).toLowerCase(Locale.ROOT);

        if (normalized.isEmpty() || "all".equals(normalized)) {
            normalized = null;
        } else if (!ALLOWED_STATUSES.contains(normalized)) {
            return null;
        }

        return shopDAO.searchShops(trim(keyword), normalized);
    }

    public ShopInfo getShop(int shopId) throws SQLException {
        return shopId <= 0 ? null : shopDAO.getShopById(shopId);
    }

    // =========================================================
    // BLOCK / UNBLOCK
    // =========================================================

    /**
     * Blocks a shop: the owner account is locked, the shop is suspended
     * and its published products are hidden.
     *
     * @return a message code understood by the shop screen
     */
    public String blockShop(String shopIdRaw) throws SQLException {

        ShopInfo shop = loadShopOrNull(shopIdRaw);

        if (shop == null) {
            return "notfound";
        }

        if ("suspended".equalsIgnoreCase(shop.getStatus())) {
            return "already-blocked";
        }

        return shopDAO.blockShop(shop.getShopId(), shop.getOwnerId())
                ? "blocked"
                : "failed";
    }

    /**
     * Unblocks a shop: the owner is reactivated, the shop is active
     * again and the products that the block hid are published again.
     */
    public String unblockShop(String shopIdRaw) throws SQLException {

        ShopInfo shop = loadShopOrNull(shopIdRaw);

        if (shop == null) {
            return "notfound";
        }

        if (!"suspended".equalsIgnoreCase(shop.getStatus())) {
            return "not-blocked";
        }

        return shopDAO.unblockShop(shop.getShopId(), shop.getOwnerId())
                ? "unblocked"
                : "failed";
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ShopInfo loadShopOrNull(String shopIdRaw) throws SQLException {

        int shopId = parseInt(shopIdRaw, -1);

        return shopId <= 0 ? null : shopDAO.getShopById(shopId);
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
