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
import com.mycompany.onlinefruitshopping.model.Coupon;
import com.mycompany.onlinefruitshopping.model.CouponQuote;
import com.mycompany.onlinefruitshopping.model.NotificationType;
import com.mycompany.onlinefruitshopping.model.ShopOwnerNotificationType;
import com.mycompany.onlinefruitshopping.dao.coupon.CouponCheckoutDAO;
import java.math.BigDecimal;

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
    private final NotificationService notificationService;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();
        this.voucherDAO = new VoucherDAO();
        this.addressDAO = new AddressDAO();
        this.notificationService = new NotificationService();
    }

    public OrderService(OrderDAO orderDAO, ProductDAO productDAO, CartDAO cartDAO, VoucherDAO voucherDAO, AddressDAO addressDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
        this.voucherDAO = voucherDAO;
        this.addressDAO = addressDAO;
        this.notificationService = new NotificationService();
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
        if (orderId <= 0) {
            return null;
        }
        return orderDAO.getOrderById(orderId, shopId);
    }

    public Order getOrderById(int orderId) throws SQLException {
        if (orderId <= 0) {
            return null;
        }
        return orderDAO.getOrderById(orderId, 0);
    }

    public void confirmOrder(int orderId, int shopId, int changedBy) throws SQLException {
        if (orderId <= 0 || shopId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc cửa hàng không hợp lệ.");
        }
        boolean updated = orderDAO.updateOrderStatus(orderId, shopId, "preparing", changedBy, "Shop đã duyệt đơn và đang đóng gói.");
        if (updated) {
            try {
                Order order = orderDAO.getOrderById(orderId, shopId);
                if (order != null && order.getUserId() > 0) {
                    notificationService.notifyCustomer(
                            order.getUserId(),
                            "Đơn hàng #" + orderId + " đã được xác nhận",
                            "Đơn hàng #" + orderId + " đã được cửa hàng xác nhận và đang được chuẩn bị.",
                            NotificationType.ORDER_CONFIRMATION,
                            "order",
                            String.valueOf(orderId)
                    );
                }
            } catch (Exception ignored) {}
        }
    }

    public void markOrderReady(int orderId, int shopId, int changedBy) throws SQLException {
        if (orderId <= 0 || shopId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc cửa hàng không hợp lệ.");
        }
        boolean updated = orderDAO.updateOrderStatus(orderId, shopId, "shipping", changedBy, "Shop đã đóng gói xong, bàn giao vận chuyển.");
        if (updated) {
            try {
                Order order = orderDAO.getOrderById(orderId, shopId);
                if (order != null && order.getUserId() > 0) {
                    notificationService.notifyCustomer(
                            order.getUserId(),
                            "Đơn hàng #" + orderId + " đang được giao",
                            "Cửa hàng đã đóng gói xong đơn hàng #" + orderId + " và bàn giao cho đơn vị vận chuyển.",
                            NotificationType.DELIVERY_STATUS,
                            "order",
                            String.valueOf(orderId)
                    );
                }
            } catch (Exception ignored) {}
        }
    }

    public void rejectOrder(int orderId, int shopId, int changedBy, String reason) throws SQLException {
        if (orderId <= 0 || shopId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc cửa hàng không hợp lệ.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối đơn hàng!");
        }
        boolean updated = orderDAO.updateOrderStatus(orderId, shopId, "cancelled", changedBy, "Từ chối: " + reason.trim());
        if (updated) {
            try {
                Order order = orderDAO.getOrderById(orderId, shopId);
                if (order != null && order.getUserId() > 0) {
                    notificationService.notifyCustomer(
                            order.getUserId(),
                            "Đơn hàng #" + orderId + " đã bị từ chối",
                            "Rất tiếc, cửa hàng đã từ chối đơn hàng #" + orderId + ". Lý do: " + reason.trim(),
                            NotificationType.ORDER_CONFIRMATION,
                            "order",
                            String.valueOf(orderId)
                    );
                }
            } catch (Exception ignored) {}
        }
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

    /** Cart checkout supports exactly one shop, because a coupon is always scoped to its shop. */
    public int getCartShopId(List<CartItem> items) throws SQLException {
        if(items==null || items.isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        int shopId=0;
        for(CartItem item:items) {
            int productShopId=productDAO.getShopIdByProductId(item.getProductId());
            if(productShopId<=0) throw new IllegalArgumentException("Không xác định được cửa hàng của sản phẩm trong giỏ.");
            if(shopId!=0 && productShopId!=shopId)
                throw new IllegalArgumentException("Giỏ có sản phẩm của nhiều shop. Vui lòng thanh toán từng cửa hàng để sử dụng mã giảm giá đúng shop.");
            shopId=productShopId;
        }
        return shopId;
    }

    public List<Coupon> getCheckoutCoupons(int customerId,List<CartItem> items) throws SQLException {
        BigDecimal subtotal=BigDecimal.ZERO;
        for(CartItem item:items) subtotal=subtotal.add(BigDecimal.valueOf(item.getItemTotal()));
        return new CouponCheckoutDAO().availableForShop(getCartShopId(items),customerId,subtotal);
    }

    public Map<String, Object> calculateOrderSummary(int customerId,List<CartItem> items,
                                                      String deliveryMethod,String couponCode) throws SQLException {
        double subtotal=0;
        int totalItems=0;
        for(CartItem item:items) { subtotal+=item.getItemTotal(); totalItems+=(int)item.getQuantity(); }
        int shopId=getCartShopId(items);
        double baseShippingFee=subtotal>=500000?0:25000;
        double expressSurcharge="express".equalsIgnoreCase(deliveryMethod)?30000:0;
        double shippingFee=baseShippingFee+expressSurcharge;
        double discount=0;
        String couponMessage="Chưa áp dụng mã giảm giá.";
        String appliedCode=null;
        if(couponCode!=null && !couponCode.isBlank()) {
            CouponQuote quote=new CouponCheckoutDAO().preview(customerId,shopId,couponCode,
                    BigDecimal.valueOf(subtotal));
            discount=quote.getDiscountAmount().doubleValue();
            appliedCode=quote.getCode();
            couponMessage="Áp dụng mã '"+appliedCode+"' thành công (sẽ xác nhận lại khi đặt hàng).";
        }
        Map<String,Object> result=new HashMap<>();
        result.put("subtotal",subtotal); result.put("totalItems",totalItems);
        result.put("shippingFee",shippingFee); result.put("baseShippingFee",baseShippingFee);
        result.put("expressSurcharge",expressSurcharge); result.put("discount",discount);
        result.put("couponCode",appliedCode);result.put("couponMessage",couponMessage);
        result.put("totalAmount",Math.max(0,subtotal+shippingFee-discount));
        result.put("deliveryMethod",deliveryMethod!=null?deliveryMethod:"standard");
        return result;
    }

    /**
     * Xác nhận và tạo đơn hàng (Confirm & Place Order).
     * Bao gồm kiểm tra giỏ hàng, xác thực địa chỉ, tính toán số tiền và trừ tồn kho.
     */
    public int placeOrder(int userId, int addressId, String deliveryMethod, String paymentMethod,
                          String couponCode, String note) throws SQLException {
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

        int shopId=getCartShopId(items);
        // Both preview and final coupon are shop-bound; final validation and usage
        // happen inside OrderDAO's order creation transaction.
        Map<String,Object> summary=calculateOrderSummary(userId,items,deliveryMethod,couponCode);
        double shippingFee=(double)summary.get("shippingFee");
        double subtotal=(double)summary.get("subtotal");
        double totalAmount=(double)summary.get("totalAmount");
        double undiscountedTotal=subtotal+shippingFee;
        int orderId = orderDAO.createOrder(userId,shopId,addressId,null,
                deliveryMethod,paymentMethod,undiscountedTotal,shippingFee,0,items,note,couponCode);

        if (orderId > 0) {
            try {
                // 1. Gửi thông báo xác nhận đặt hàng cho Customer
                notificationService.notifyCustomer(
                        userId,
                        "Đặt hàng thành công #" + orderId,
                        "Đơn hàng #" + orderId + " của bạn đã được tạo thành công với tổng tiền "
                                + String.format("%,.0fđ", totalAmount) + " và đang chờ cửa hàng xác nhận.",
                        NotificationType.ORDER_CONFIRMATION,
                        "order",
                        String.valueOf(orderId)
                );

                // 2. Gửi thông báo Đơn hàng mới cho Shop Owner
                notificationService.notifyShopOwnerByShopId(
                        shopId,
                        "Đơn hàng mới #" + orderId,
                        "Cửa hàng vừa nhận được đơn hàng mới #" + orderId + " (Tổng thanh toán: "
                                + String.format("%,.0fđ", totalAmount) + "). Vui lòng kiểm tra và xác nhận đơn hàng.",
                        ShopOwnerNotificationType.NEW_ORDER,
                        "order",
                        String.valueOf(orderId)
                );
            } catch (Exception ignored) {}
        }

        return orderId;
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
        Order order = orderDAO.getOrderById(orderId, 0);
        boolean cancelled = orderDAO.cancelOrderCustomer(orderId, userId, reason);
        if (cancelled) {
            try {
                String reasonText = (reason != null && !reason.isBlank()) ? " Lý do: " + reason.trim() : "";
                notificationService.notifyCustomer(
                        userId,
                        "Đã hủy đơn hàng #" + orderId,
                        "Bạn đã hủy đơn hàng #" + orderId + " thành công." + reasonText,
                        NotificationType.ORDER_CONFIRMATION,
                        "order",
                        String.valueOf(orderId)
                );
                if (order != null && order.getShopId() > 0) {
                    notificationService.notifyShopOwnerByShopId(
                            order.getShopId(),
                            "Đơn hàng #" + orderId + " đã bị khách hủy",
                            "Khách hàng đã hủy đơn hàng #" + orderId + "." + reasonText,
                            ShopOwnerNotificationType.ORDER_CANCELLED,
                            "order",
                            String.valueOf(orderId)
                    );
                }
            } catch (Exception ignored) {}
        }
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
        boolean assigned = orderDAO.assignDeliveryStaff(orderId, shopId, staffId, assignedByUserId, note);
        if (assigned) {
            try {
                Order order = orderDAO.getOrderById(orderId, shopId);
                String addr = order != null ? order.getRecipientAddress() : "";
                notificationService.notifyDeliveryAssignment(staffId, orderId, addr);
                if (order != null && order.getUserId() > 0) {
                    notificationService.notifyCustomer(
                            order.getUserId(),
                            "Đơn hàng #" + orderId + " đã được giao cho Shipper",
                            "Đơn hàng #" + orderId + " của bạn đã được phân công cho nhân viên giao hàng và đang trên đường vận chuyển.",
                            NotificationType.DELIVERY_STATUS,
                            "order",
                            String.valueOf(orderId)
                    );
                }
            } catch (Exception ignored) {}
        }
    }
}
