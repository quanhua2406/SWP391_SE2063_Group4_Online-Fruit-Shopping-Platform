package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.AddressDAO;
import com.mycompany.onlinefruitshopping.dao.CartDAO;
import com.mycompany.onlinefruitshopping.dao.OrderDAO;
import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.dao.VoucherDAO;
import com.mycompany.onlinefruitshopping.model.Address;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.Order;
import com.mycompany.onlinefruitshopping.model.OrderItem;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.model.Voucher;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Layer xử lý toàn bộ logic nghiệp vụ (Business Logic) liên quan đến Quản lý Đơn hàng (Order Management).
 * Feature 6: Order Management - In Charge: Le Duc Cuong (cuongld)
 *
 * Tuân thủ kiến trúc 3 tầng:
 * - Controller (Servlet): Tiếp nhận HTTP request, đọc tham số và điều hướng.
 * - Service: Xử lý quy tắc nghiệp vụ, tính toán tổng tiền, chiết khấu voucher, phí vận chuyển theo phương thức,
 *            kiểm tra tồn kho và trạng thái bán, quy trình duyệt / hủy / mua lại / phân công vận chuyển.
 * - DAO: Tương tác với cơ sở dữ liệu.
 */
public class OrderService {

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;
    private final VoucherDAO voucherDAO;
    private final AddressDAO addressDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();
        this.voucherDAO = new VoucherDAO();
        this.addressDAO = new AddressDAO();
    }

    public OrderService(OrderDAO orderDAO, ProductDAO productDAO, CartDAO cartDAO, VoucherDAO voucherDAO, AddressDAO addressDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
        this.voucherDAO = voucherDAO;
        this.addressDAO = addressDAO;
    }

    // =========================================================
    // STORE OWNER METHODS (EXISTING)
    // =========================================================

    public int getOrCreateShopIdForUser(int userId, String fullName) throws SQLException {
        return productDAO.getOrCreateShopIdForUser(userId, fullName);
    }

    public void seedSampleOrdersIfEmpty(int shopId) throws SQLException {
        orderDAO.seedSampleOrdersIfEmpty(shopId);
    }

    public List<Order> getOrdersByShop(int shopId, String status, String searchQuery) throws SQLException {
        if (status == null || status.isBlank()) {
            status = "all";
        }
        return orderDAO.getOrdersByShop(shopId, status, searchQuery);
    }

    public Map<String, Integer> countOrdersByStatus(int shopId) throws SQLException {
        return orderDAO.countOrdersByStatus(shopId);
    }

    public Order getOrderDetail(int orderId, int shopId) throws SQLException {
        if (orderId <= 0 || shopId <= 0) {
            return null;
        }
        return orderDAO.getOrderById(orderId, shopId);
    }

    public void confirmOrder(int orderId, int shopId, int changedBy) throws SQLException {
        if (orderId <= 0 || shopId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc cửa hàng không hợp lệ.");
        }
        orderDAO.updateOrderStatus(orderId, shopId, "preparing", changedBy, "Shop đã duyệt đơn và đang đóng gói.");
    }

    public void markOrderReady(int orderId, int shopId, int changedBy) throws SQLException {
        if (orderId <= 0 || shopId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc cửa hàng không hợp lệ.");
        }
        orderDAO.updateOrderStatus(orderId, shopId, "shipping", changedBy, "Shop đã đóng gói xong, bàn giao vận chuyển.");
    }

    public void rejectOrder(int orderId, int shopId, int changedBy, String reason) throws SQLException {
        if (orderId <= 0 || shopId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc cửa hàng không hợp lệ.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối đơn hàng!");
        }
        orderDAO.updateOrderStatus(orderId, shopId, "cancelled", changedBy, "Từ chối: " + reason.trim());
    }

    // =========================================================
    // CUSTOMER CHECKOUT & PLACE ORDER (TASKS 1 - 6)
    // =========================================================

    public List<Address> getUserAddresses(int userId) throws SQLException {
        return addressDAO.getAddressesByUserId(userId);
    }

    public Address getDefaultAddress(int userId) throws SQLException {
        return addressDAO.getDefaultAddress(userId);
    }

    public int createQuickAddress(int userId, String recipientName, String phone,
                                  String addressLine, String province, String district, String ward) throws SQLException {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setRecipientName(recipientName);
        addr.setPhone(phone);
        addr.setAddressLine(addressLine);
        addr.setProvince(province);
        addr.setDistrict(district);
        addr.setWard(ward);
        addr.setDefault(false);
        addressDAO.addAddress(addr);

        List<Address> list = addressDAO.getAddressesByUserId(userId);
        if (!list.isEmpty()) {
            return list.get(0).getAddressId();
        }
        return 0;
    }

    public List<CartItem> getActiveCartItems(int userId) throws SQLException {
        return cartDAO.getCartItemsByUserId(userId);
    }

    public Map<String, Object> calculateOrderSummary(List<CartItem> items, String deliveryMethod, String voucherCode) throws SQLException {
        double subtotal = 0;
        int totalItems = 0;
        for (CartItem item : items) {
            subtotal += item.getItemTotal();
            totalItems += (int) item.getQuantity();
        }

        // Tính phí vận chuyển (Delivery Method)
        // Standard: 25.000 ₫ (Miễn phí nếu subtotal >= 500.000 ₫)
        // Express: +30.000 ₫
        double baseShippingFee = subtotal >= 500000 ? 0 : 25000;
        double expressSurcharge = "express".equalsIgnoreCase(deliveryMethod) ? 30000 : 0;
        double shippingFee = baseShippingFee + expressSurcharge;

        // Áp dụng voucher
        double discount = 0;
        Voucher appliedVoucher = null;
        String voucherMessage = null;

        if (voucherCode != null && !voucherCode.isBlank()) {
            appliedVoucher = voucherDAO.getVoucherByCode(voucherCode.trim());
            if (appliedVoucher != null && appliedVoucher.isValid()) {
                discount = appliedVoucher.calculateDiscount(subtotal);
                voucherMessage = "Áp dụng mã '" + appliedVoucher.getCode() + "' thành công!";
            } else {
                voucherMessage = "Mã khuyến mãi không hợp lệ hoặc đã hết hạn.";
            }
        }

        double totalAmount = subtotal + shippingFee - discount;
        if (totalAmount < 0) totalAmount = 0;

        Map<String, Object> result = new HashMap<>();
        result.put("subtotal", subtotal);
        result.put("totalItems", totalItems);
        result.put("shippingFee", shippingFee);
        result.put("baseShippingFee", baseShippingFee);
        result.put("expressSurcharge", expressSurcharge);
        result.put("discount", discount);
        result.put("voucher", appliedVoucher);
        result.put("voucherMessage", voucherMessage);
        result.put("totalAmount", totalAmount);
        result.put("deliveryMethod", deliveryMethod != null ? deliveryMethod : "standard");
        return result;
    }

    public Voucher validateAndGetVoucher(String code, double subtotal) throws SQLException {
        if (code == null || code.isBlank()) return null;
        Voucher voucher = voucherDAO.getVoucherByCode(code.trim());
        if (voucher != null && voucher.isValid()) {
            return voucher;
        }
        return null;
    }

    public List<Voucher> getActiveVouchers() throws SQLException {
        return voucherDAO.getActiveVouchers();
    }

    /**
     * Xác nhận và tạo đơn hàng (Confirm & Place Order).
     * Bao gồm kiểm tra giỏ hàng, xác thực địa chỉ, tính toán số tiền và trừ tồn kho.
     */
    public int placeOrder(int userId, int addressId, String deliveryMethod, String paymentMethod,
                          String voucherCode, String note) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Yêu cầu đăng nhập trước khi thanh toán.");
        }
        if (addressId <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn hoặc nhập địa chỉ nhận hàng.");
        }

        List<CartItem> items = cartDAO.getCartItemsByUserId(userId);
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống, không thể đặt hàng.");
        }

        // Kiểm tra tồn kho khả dụng
        for (CartItem item : items) {
            if (!item.isActiveProduct()) {
                throw new IllegalStateException("Sản phẩm '" + item.getProductName() + "' đã tạm ngừng bán.");
            }
            if (item.getQuantity() > item.getStockQuantity()) {
                throw new IllegalStateException("Sản phẩm '" + item.getProductName() + " - " + item.getPackagingType() 
                        + "' chỉ còn " + item.getStockQuantity() + " sản phẩm trong kho.");
            }
        }

        // Lấy shop_id từ sản phẩm đầu tiên (trong nền tảng hoa quả, các sản phẩm liên kết với shop)
        int shopId = 1; // Default shop
        for (CartItem item : items) {
            int itemShopId = productDAO.getShopIdByProductId(item.getProductId());
            if (itemShopId > 0) {
                shopId = itemShopId;
                break;
            }
        }

        // Tính toán các khoản tiền
        Map<String, Object> summary = calculateOrderSummary(items, deliveryMethod, voucherCode);
        double totalAmount = (double) summary.get("totalAmount");
        double shippingFee = (double) summary.get("shippingFee");
        double discount = (double) summary.get("discount");
        Voucher voucher = (Voucher) summary.get("voucher");
        Integer voucherId = voucher != null ? voucher.getVoucherId() : null;

        // Tạo đơn hàng trong Transaction an toàn
        return orderDAO.createOrder(userId, shopId, addressId, voucherId,
                deliveryMethod, paymentMethod, totalAmount, shippingFee, discount, items, note);
    }

    // =========================================================
    // CUSTOMER ORDER TRACKING & HISTORY (TASKS 7 - 11)
    // =========================================================

    public List<Order> getCustomerOrders(int userId, String statusFilter, String searchQuery) throws SQLException {
        if (userId <= 0) return List.of();
        return orderDAO.getOrdersByUser(userId, statusFilter, searchQuery);
    }

    public Order getCustomerOrderDetail(int orderId, int userId) throws SQLException {
        if (orderId <= 0 || userId <= 0) return null;
        return orderDAO.getOrderByIdForCustomer(orderId, userId);
    }

    public void cancelCustomerOrder(int orderId, int userId, String reason) throws SQLException {
        if (orderId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng không hợp lệ.");
        }
        orderDAO.cancelOrderCustomer(orderId, userId, reason);
    }

    public int reorder(int orderId, int userId) throws SQLException {
        if (orderId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng không hợp lệ.");
        }
        List<OrderItem> pastItems = orderDAO.getPastOrderItems(orderId, userId);
        if (pastItems.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy các mặt hàng trong đơn hàng cũ.");
        }

        int count = 0;
        for (OrderItem item : pastItems) {
            int qty = (int) Math.round(item.getQuantity());
            if (qty <= 0) qty = 1;
            cartDAO.addToCart(userId, item.getVariantId(), qty);
            count++;
        }
        return count;
    }

    // =========================================================
    // DELIVERY STAFF ASSIGNMENT (TASK 12)
    // =========================================================

    public List<User> getAvailableDeliveryStaff() throws SQLException {
        return orderDAO.getAvailableDeliveryStaff();
    }

    public void assignDeliveryStaff(int orderId, int shopId, int staffId, int assignedByUserId, String note) throws SQLException {
        if (orderId <= 0 || shopId <= 0 || staffId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc nhân viên giao hàng không hợp lệ.");
        }
        orderDAO.assignDeliveryStaff(orderId, shopId, staffId, assignedByUserId, note);
    }
}
