package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.CartDAO;
import com.mycompany.onlinefruitshopping.dao.CartDAO.CartResult;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service Layer xử lý toàn bộ logic nghiệp vụ (Business Logic) của Giỏ hàng (Shopping Cart).
 *
 * Tuân thủ kiến trúc 3 tầng chuẩn:
 * - Servlet (Controller): Tiếp nhận HTTP request, đọc tham số và gọi Service.
 * - Service: Xử lý nghiệp vụ (kiểm tra tồn kho, tự động điều chỉnh số lượng (capping), tính toán tạm tính,
 *            áp dụng chính sách miễn phí vận chuyển 500.000đ, quản lý giỏ hàng vãng lai Session,
 *            gộp giỏ hàng khi đăng nhập).
 * - DAO: Tương tác với cơ sở dữ liệu.
 */
public class CartService {

    public static final double FREE_SHIPPING_THRESHOLD = 500000.0;
    public static final double STANDARD_SHIPPING_FEE = 25000.0;

    private final CartDAO cartDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
    }

    public CartService(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    /**
     * DTO chứa dữ liệu tóm tắt đơn hàng và giỏ hàng phục vụ hiển thị.
     */
    public static class CartSummary {
        private List<CartItem> items = new ArrayList<>();
        private double subtotal;
        private double shippingFee;
        private double grandTotal;
        private double freeShippingThreshold = FREE_SHIPPING_THRESHOLD;
        private double freeShippingRemaining;
        private int totalCount;
        private List<String> warnings = new ArrayList<>();
        private boolean hasUnavailableItem;

        public List<CartItem> getItems() {
            return items;
        }

        public void setItems(List<CartItem> items) {
            this.items = items;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(double subtotal) {
            this.subtotal = subtotal;
        }

        public double getShippingFee() {
            return shippingFee;
        }

        public void setShippingFee(double shippingFee) {
            this.shippingFee = shippingFee;
        }

        public double getGrandTotal() {
            return grandTotal;
        }

        public void setGrandTotal(double grandTotal) {
            this.grandTotal = grandTotal;
        }

        public double getFreeShippingThreshold() {
            return freeShippingThreshold;
        }

        public void setFreeShippingThreshold(double freeShippingThreshold) {
            this.freeShippingThreshold = freeShippingThreshold;
        }

        public double getFreeShippingRemaining() {
            return freeShippingRemaining;
        }

        public void setFreeShippingRemaining(double freeShippingRemaining) {
            this.freeShippingRemaining = freeShippingRemaining;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public boolean isHasUnavailableItem() {
            return hasUnavailableItem;
        }

        public void setHasUnavailableItem(boolean hasUnavailableItem) {
            this.hasUnavailableItem = hasUnavailableItem;
        }
    }

    /**
     * Lấy dữ liệu giỏ hàng hoàn chỉnh và tính toán tổng chi phí, phí ship, cảnh báo tồn kho.
     * Hỗ trợ cả thành viên đã đăng nhập và khách vãng lai (Session).
     */
    public CartSummary getCartSummary(User currentUser, List<CartItem> sessionCart) throws SQLException {
        CartSummary summary = new CartSummary();
        List<CartItem> items;
        List<String> warnings = new ArrayList<>();

        if (currentUser != null) {
            items = cartDAO.getCartItemsByUserId(currentUser.getUserID());
        } else {
            items = (sessionCart != null) ? sessionCart : new ArrayList<>();
            validateAndRefreshSessionCart(items, warnings);
        }

        double subtotal = 0;
        int totalCount = 0;
        boolean hasUnavailableItem = false;

        for (CartItem item : items) {
            if (!item.isActiveProduct()) {
                hasUnavailableItem = true;
            }
            if (item.getQuantity() > item.getStockQuantity()) {
                warnings.add("Sản phẩm '" + item.getProductName() + " - " + item.getPackagingType() +
                        "' chỉ còn " + (int) item.getStockQuantity() + " sản phẩm trong kho.");
            }
            subtotal += item.getItemTotal();
            totalCount += item.getQuantity();
        }

        if (hasUnavailableItem) {
            warnings.add("Một số sản phẩm trong giỏ hàng hiện đã tạm ngừng kinh doanh.");
        }

        double shippingFee = (subtotal >= FREE_SHIPPING_THRESHOLD || items.isEmpty()) ? 0 : STANDARD_SHIPPING_FEE;
        double grandTotal = subtotal + shippingFee;
        double freeShippingRemaining = Math.max(0, FREE_SHIPPING_THRESHOLD - subtotal);

        summary.setItems(items);
        summary.setSubtotal(subtotal);
        summary.setShippingFee(shippingFee);
        summary.setGrandTotal(grandTotal);
        summary.setFreeShippingThreshold(FREE_SHIPPING_THRESHOLD);
        summary.setFreeShippingRemaining(freeShippingRemaining);
        summary.setTotalCount(totalCount);
        summary.setWarnings(warnings);
        summary.setHasUnavailableItem(hasUnavailableItem);

        return summary;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng với đầy đủ kiểm tra nghiệp vụ:
     * - Tồn tại biến thể quy cách
     * - Trạng thái kinh doanh
     * - Tồn kho khả dụng
     * - Tự động điều chỉnh số lượng nếu vượt tồn kho (Stock capping)
     */
    public CartResult addToCart(User currentUser, List<CartItem> sessionCart, int variantId, int requestedQuantity)
            throws SQLException {

        if (requestedQuantity <= 0) {
            requestedQuantity = 1;
        }

        if (variantId <= 0) {
            return new CartResult(false, "Quy cách đóng gói không hợp lệ.", 0);
        }

        if (currentUser != null) {
            return cartDAO.addToCart(currentUser.getUserID(), variantId, requestedQuantity);
        } else {
            CartItem liveVariant = cartDAO.getVariantDetailForCart(variantId);
            if (liveVariant == null) {
                return new CartResult(false, "Quy cách sản phẩm không tồn tại.", calculateSessionCartCount(sessionCart));
            }
            if (!liveVariant.isActiveProduct()) {
                return new CartResult(false, "Sản phẩm tạm thời ngừng kinh doanh.", calculateSessionCartCount(sessionCart));
            }

            int availableStock = (int) liveVariant.getStockQuantity();
            if (availableStock <= 0) {
                return new CartResult(false, "Sản phẩm hiện đã hết hàng trong kho.", calculateSessionCartCount(sessionCart));
            }

            if (sessionCart == null) {
                sessionCart = new ArrayList<>();
            }

            CartItem existingItem = null;
            for (CartItem item : sessionCart) {
                if (item.getVariantId() == variantId) {
                    existingItem = item;
                    break;
                }
            }

            int targetQty = requestedQuantity;
            boolean wasCapped = false;

            if (existingItem != null) {
                targetQty = existingItem.getQuantity() + requestedQuantity;
                if (targetQty > availableStock) {
                    targetQty = availableStock;
                    wasCapped = true;
                }
                existingItem.setQuantity(targetQty);
                existingItem.setUnitPrice(liveVariant.getUnitPrice());
                existingItem.setStockQuantity(availableStock);
            } else {
                if (targetQty > availableStock) {
                    targetQty = availableStock;
                    wasCapped = true;
                }
                liveVariant.setQuantity(targetQty);
                sessionCart.add(liveVariant);
            }

            int totalCount = calculateSessionCartCount(sessionCart);
            String msg = wasCapped
                    ? "Đã thêm vào giỏ. Do tồn kho chỉ còn " + availableStock + " sản phẩm nên số lượng đã điều chỉnh về " + targetQty + "."
                    : "Đã thêm thành công " + requestedQuantity + " " + liveVariant.getPackagingType() + " (" + liveVariant.getProductName() + ") vào giỏ hàng!";

            CartResult res = new CartResult(true, msg, targetQty);
            res.setCartItemCount(totalCount);
            return res;
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng.
     */
    public CartResult updateQuantity(User currentUser, List<CartItem> sessionCart, int variantId, int newQuantity)
            throws SQLException {

        if (currentUser != null) {
            return cartDAO.updateQuantity(currentUser.getUserID(), variantId, newQuantity);
        } else {
            if (sessionCart == null) {
                return new CartResult(false, "Không tìm thấy sản phẩm trong giỏ hàng.", 0);
            }

            CartItem targetItem = null;
            for (CartItem item : sessionCart) {
                if (item.getVariantId() == variantId) {
                    targetItem = item;
                    break;
                }
            }

            if (targetItem == null) {
                return new CartResult(false, "Không tìm thấy sản phẩm trong giỏ hàng.", calculateSessionCartCount(sessionCart));
            }

            if (newQuantity <= 0) {
                sessionCart.remove(targetItem);
                int count = calculateSessionCartCount(sessionCart);
                CartResult res = new CartResult(true, "Đã xóa sản phẩm khỏi giỏ hàng.", 0);
                res.setCartItemCount(count);
                return res;
            } else {
                CartItem live = cartDAO.getVariantDetailForCart(variantId);
                int stock = (live != null) ? (int) live.getStockQuantity() : 0;
                boolean wasCapped = false;
                if (newQuantity > stock) {
                    newQuantity = stock;
                    wasCapped = true;
                }
                targetItem.setQuantity(newQuantity);
                if (live != null) {
                    targetItem.setUnitPrice(live.getUnitPrice());
                    targetItem.setStockQuantity(stock);
                }

                int totalCount = calculateSessionCartCount(sessionCart);
                String msg = wasCapped
                        ? "Số lượng tối đa có sẵn trong kho là " + stock + " sản phẩm."
                        : "Đã cập nhật số lượng thành công.";

                CartResult res = new CartResult(true, msg, newQuantity);
                res.setCartItemCount(totalCount);
                return res;
            }
        }
    }

    /**
     * Xóa một mặt hàng khỏi giỏ.
     */
    public int removeItem(User currentUser, List<CartItem> sessionCart, int variantId) throws SQLException {
        if (currentUser != null) {
            cartDAO.removeItem(currentUser.getUserID(), variantId);
            return cartDAO.getCartCount(currentUser.getUserID());
        } else {
            if (sessionCart != null) {
                sessionCart.removeIf(item -> item.getVariantId() == variantId);
            }
            return calculateSessionCartCount(sessionCart);
        }
    }

    /**
     * Dọn sạch toàn bộ giỏ hàng.
     */
    public void clearCart(User currentUser, List<CartItem> sessionCart) throws SQLException {
        if (currentUser != null) {
            cartDAO.clearCart(currentUser.getUserID());
        } else {
            if (sessionCart != null) {
                sessionCart.clear();
            }
        }
    }

    /**
     * Lấy tổng số lượng mặt hàng trong giỏ.
     */
    public int getCartCount(User currentUser, List<CartItem> sessionCart) {
        if (currentUser != null) {
            return cartDAO.getCartCount(currentUser.getUserID());
        } else {
            return calculateSessionCartCount(sessionCart);
        }
    }

    /**
     * Gộp giỏ hàng vãng lai (Session) vào giỏ hàng CSDL khi khách hàng đăng nhập.
     */
    public void mergeSessionCart(int userId, List<CartItem> sessionCart) {
        if (sessionCart != null && !sessionCart.isEmpty()) {
            cartDAO.mergeSessionCart(userId, sessionCart);
        }
    }

    /**
     * Tính tổng số lượng hàng trong Session Cart.
     */
    public int calculateSessionCartCount(List<CartItem> cart) {
        int count = 0;
        if (cart != null) {
            for (CartItem item : cart) {
                count += item.getQuantity();
            }
        }
        return count;
    }

    /**
     * Đồng bộ và làm mới thông tin giá bán và tồn kho mới nhất cho giỏ hàng session từ CSDL.
     */
    public void validateAndRefreshSessionCart(List<CartItem> cart, List<String> warnings) {
        if (cart == null) return;
        Iterator<CartItem> it = cart.iterator();
        while (it.hasNext()) {
            CartItem item = it.next();
            try {
                CartItem live = cartDAO.getVariantDetailForCart(item.getVariantId());
                if (live == null || !live.isActiveProduct()) {
                    if (warnings != null) {
                        warnings.add("Sản phẩm '" + item.getProductName() + "' đã ngừng bán.");
                    }
                    item.setActiveProduct(false);
                } else {
                    item.setUnitPrice(live.getUnitPrice());
                    item.setStockQuantity(live.getStockQuantity());
                    item.setImageUrl(live.getImageUrl());
                    if (item.getQuantity() > live.getStockQuantity()) {
                        item.setQuantity(Math.max(1, (int) live.getStockQuantity()));
                    }
                }
            } catch (SQLException e) {
                // Bỏ qua lỗi kết nối tạm thời
            }
        }
    }
}
