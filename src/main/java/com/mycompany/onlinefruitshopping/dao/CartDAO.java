package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Shopping Cart Management.
 * Feature 5: Shopping Cart Management - In Charge: Le Duc Cuong (cuongld)
 * Handles cart persistence, variant stock checking, dynamic pricing, and validation.
 */
public class CartDAO {

    public static class CartResult {
        private boolean success;
        private String message;
        private int adjustedQuantity;
        private int cartItemCount;

        public CartResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public CartResult(boolean success, String message, int adjustedQuantity) {
            this.success = success;
            this.message = message;
            this.adjustedQuantity = adjustedQuantity;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getAdjustedQuantity() {
            return adjustedQuantity;
        }

        public int getCartItemCount() {
            return cartItemCount;
        }

        public void setCartItemCount(int cartItemCount) {
            this.cartItemCount = cartItemCount;
        }
    }

    /**
     * Self-healing: Creates carts and cart_items tables if they do not exist.
     */
    public void ensureTablesExist() {
        String sql = """
            IF OBJECT_ID('carts', 'U') IS NULL
            BEGIN
                CREATE TABLE carts (
                    cart_id INT IDENTITY(1,1) PRIMARY KEY,
                    user_id INT NOT NULL UNIQUE,
                    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                );
            END;

            IF OBJECT_ID('cart_items', 'U') IS NULL
            BEGIN
                CREATE TABLE cart_items (
                    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
                    cart_id INT NOT NULL,
                    variant_id INT NOT NULL,
                    quantity DECIMAL(10,2) NOT NULL DEFAULT 1,
                    CONSTRAINT uq_cart_variant UNIQUE (cart_id, variant_id),
                    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
                    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
                );
            END;
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Table creation might already be done or handled by DBA
        }
    }

    /**
     * Gets existing cart_id or creates a new one for the user.
     */
    public int getOrCreateCartId(int userId) throws SQLException {
        ensureTablesExist();
        String selectSql = "SELECT cart_id FROM carts WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cart_id");
                }
            }
        }

        String insertSql = "INSERT INTO carts (user_id, created_at) VALUES (?, GETDATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Fetches live variant detail including current price, active state and available stock.
     */
    public CartItem getVariantDetailForCart(int variantId) throws SQLException {
        ensureTablesExist();
        String sql = """
            SELECT pv.variant_id, pv.product_id, pv.sku, pv.unit, pv.packaging_type, pv.price,
                   p.product_name, p.origin, p.status,
                   COALESCE((
                       SELECT SUM(ib.quantity)
                       FROM inventory_batches ib
                       WHERE ib.variant_id = pv.variant_id AND ib.status = 'active'
                   ), 0) AS stock_quantity,
                   (
                       SELECT TOP 1 pi.image_url
                       FROM product_images pi
                       WHERE pi.product_id = p.product_id
                       ORDER BY pi.sort_order ASC, pi.image_id ASC
                   ) AS image_url
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            WHERE pv.variant_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setSku(rs.getString("sku"));
                    item.setUnit(rs.getString("unit"));
                    item.setPackagingType(rs.getString("packaging_type"));
                    item.setUnitPrice(rs.getDouble("price"));
                    item.setProductName(rs.getString("product_name"));
                    item.setOrigin(rs.getString("origin"));
                    String status = rs.getString("status");
                    item.setActiveProduct("published".equalsIgnoreCase(status) || "active".equalsIgnoreCase(status));
                    item.setStockQuantity(rs.getDouble("stock_quantity"));
                    item.setImageUrl(rs.getString("image_url"));
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all cart items for a user with up-to-date prices, stock, and images.
     */
    public List<CartItem> getCartItemsByUserId(int userId) throws SQLException {
        int cartId = getOrCreateCartId(userId);
        return getCartItemsByCartId(cartId);
    }

    public List<CartItem> getCartItemsByCartId(int cartId) throws SQLException {
        ensureTablesExist();
        List<CartItem> items = new ArrayList<>();
        String sql = """
            SELECT ci.cart_item_id, ci.cart_id, ci.variant_id, CAST(ci.quantity AS INT) AS quantity,
                   pv.product_id, pv.sku, pv.unit, pv.packaging_type, pv.price AS unit_price,
                   p.product_name, p.origin, p.status,
                   COALESCE((
                       SELECT SUM(ib.quantity)
                       FROM inventory_batches ib
                       WHERE ib.variant_id = pv.variant_id AND ib.status = 'active'
                   ), 0) AS stock_quantity,
                   (
                       SELECT TOP 1 pi.image_url
                       FROM product_images pi
                       WHERE pi.product_id = p.product_id
                       ORDER BY pi.sort_order ASC, pi.image_id ASC
                   ) AS image_url
            FROM cart_items ci
            JOIN product_variants pv ON ci.variant_id = pv.variant_id
            JOIN products p ON pv.product_id = p.product_id
            WHERE ci.cart_id = ?
            ORDER BY ci.cart_item_id DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getInt("cart_item_id"));
                    item.setCartId(rs.getInt("cart_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSku(rs.getString("sku"));
                    item.setUnit(rs.getString("unit"));
                    item.setPackagingType(rs.getString("packaging_type"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setProductName(rs.getString("product_name"));
                    item.setOrigin(rs.getString("origin"));
                    String status = rs.getString("status");
                    item.setActiveProduct("published".equalsIgnoreCase(status) || "active".equalsIgnoreCase(status));
                    item.setStockQuantity(rs.getDouble("stock_quantity"));
                    item.setImageUrl(rs.getString("image_url"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Adds an item to the user's cart with rigorous validation.
     * Validates: Product is active, variant exists, stock > 0, caps quantity if exceeding stock.
     */
    public CartResult addToCart(int userId, int variantId, int requestedQuantity) throws SQLException {
        if (requestedQuantity <= 0) {
            return new CartResult(false, "Số lượng thêm vào giỏ hàng phải lớn hơn 0.");
        }

        CartItem variant = getVariantDetailForCart(variantId);
        if (variant == null) {
            return new CartResult(false, "Quy cách sản phẩm không tồn tại trong hệ thống.");
        }

        if (!variant.isActiveProduct()) {
            return new CartResult(false, "Sản phẩm này hiện đang tạm ngừng kinh doanh.");
        }

        int availableStock = (int) variant.getStockQuantity();
        if (availableStock <= 0) {
            return new CartResult(false, "Sản phẩm '" + variant.getProductName() + " - " + variant.getPackagingType() + "' hiện đã hết hàng.");
        }

        int cartId = getOrCreateCartId(userId);

        // Check if item already exists in cart
        String checkSql = "SELECT cart_item_id, CAST(quantity AS INT) AS quantity FROM cart_items WHERE cart_id = ? AND variant_id = ?";
        int existingQty = 0;
        int cartItemId = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cartItemId = rs.getInt("cart_item_id");
                    existingQty = rs.getInt("quantity");
                }
            }
        }

        int targetQty = existingQty + requestedQuantity;
        boolean wasCapped = false;

        if (targetQty > availableStock) {
            targetQty = availableStock;
            wasCapped = true;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (cartItemId > 0) {
                String updateSql = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, targetQty);
                    ps.setInt(2, cartItemId);
                    ps.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO cart_items (cart_id, variant_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, cartId);
                    ps.setInt(2, variantId);
                    ps.setInt(3, targetQty);
                    ps.executeUpdate();
                }
            }
        }

        String msg;
        if (wasCapped) {
            msg = "Đã thêm vào giỏ hàng. Do tồn kho chỉ còn " + availableStock + " sản phẩm nên số lượng đã được tự động điều chỉnh về " + targetQty + ".";
        } else {
            msg = "Đã thêm thành công " + requestedQuantity + " " + variant.getPackagingType() + " (" + variant.getProductName() + ") vào giỏ hàng!";
        }

        CartResult result = new CartResult(true, msg, targetQty);
        result.setCartItemCount(getCartCount(userId));
        return result;
    }

    /**
     * Updates quantity of an item in the cart.
     * If newQuantity <= 0, the item is removed.
     * If newQuantity > stock, quantity is capped to available stock.
     */
    public CartResult updateQuantity(int userId, int variantId, int newQuantity) throws SQLException {
        int cartId = getOrCreateCartId(userId);

        if (newQuantity <= 0) {
            removeItem(userId, variantId);
            CartResult res = new CartResult(true, "Đã xóa sản phẩm khỏi giỏ hàng.", 0);
            res.setCartItemCount(getCartCount(userId));
            return res;
        }

        CartItem variant = getVariantDetailForCart(variantId);
        if (variant == null) {
            removeItem(userId, variantId);
            return new CartResult(false, "Sản phẩm không còn tồn tại trong hệ thống.");
        }

        int availableStock = (int) variant.getStockQuantity();
        boolean wasCapped = false;
        int finalQuantity = newQuantity;

        if (finalQuantity > availableStock) {
            finalQuantity = availableStock;
            wasCapped = true;
        }

        if (finalQuantity <= 0) {
            removeItem(userId, variantId);
            CartResult res = new CartResult(false, "Sản phẩm này hiện đã hết hàng trong kho.", 0);
            res.setCartItemCount(getCartCount(userId));
            return res;
        }

        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND variant_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, finalQuantity);
            ps.setInt(2, cartId);
            ps.setInt(3, variantId);
            ps.executeUpdate();
        }

        String msg = wasCapped
                ? "Số lượng tối đa có sẵn trong kho là " + availableStock + " sản phẩm."
                : "Đã cập nhật số lượng thành công.";

        CartResult res = new CartResult(true, msg, finalQuantity);
        res.setCartItemCount(getCartCount(userId));
        return res;
    }

    /**
     * Removes a specific item from the cart.
     */
    public boolean removeItem(int userId, int variantId) throws SQLException {
        int cartId = getOrCreateCartId(userId);
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND variant_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, variantId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Clears all items in the user's cart.
     */
    public boolean clearCart(int userId) throws SQLException {
        int cartId = getOrCreateCartId(userId);
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            return ps.executeUpdate() >= 0;
        }
    }

    /**
     * Gets the total number of items (sum of quantities) in user's cart.
     */
    public int getCartCount(int userId) {
        try {
            int cartId = getOrCreateCartId(userId);
            String sql = "SELECT COALESCE(SUM(CAST(quantity AS INT)), 0) FROM cart_items WHERE cart_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, cartId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            // return 0 on error
        }
        return 0;
    }

    /**
     * Merges a guest's session cart items into the user's database cart upon login.
     */
    public void mergeSessionCart(int userId, List<CartItem> sessionCart) {
        if (sessionCart == null || sessionCart.isEmpty()) {
            return;
        }
        for (CartItem item : sessionCart) {
            try {
                addToCart(userId, item.getVariantId(), item.getQuantity());
            } catch (SQLException e) {
                // Continue merging other items if one fails
            }
        }
    }
}
