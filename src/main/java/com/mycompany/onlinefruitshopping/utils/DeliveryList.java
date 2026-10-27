package com.mycompany.onlinefruitshopping.utils;

import com.mycompany.onlinefruitshopping.dao.delivery.DeliveryDAO;

import jakarta.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.mycompany.onlinefruitshopping.model.Delivery;

/**
 * Lọc trên danh sách đã được DeliveryDAO giới hạn quyền theo tài khoản.
 */
public class DeliveryList {

    private static final List<String> STATUSES = List.of(
            "Assigned", "Accepted", "PickedUp", "Delivering", "Delivered", "Rejected", "Failed");

    private final String keyword;
    private final String status;
    private final int shopId;
    private final int shipperId;

    public DeliveryList(String keyword, String status, String shopId, String shipperId) {
        this.keyword = clean(keyword);
        this.status = clean(status);
        this.shopId = parseId(shopId);
        this.shipperId = parseId(shipperId);
        if (this.keyword.length() > 100) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm tối đa 100 ký tự.");
        }
        if (!this.status.isEmpty() && !STATUSES.contains(this.status)) {
            throw new IllegalArgumentException("Trạng thái lọc không hợp lệ.");
        }
    }

    /**
     * Các điều kiện được kết hợp: một lần giao phải khớp tất cả điều kiện đã
     * chọn.
     */
    public List<Delivery> apply(List<Delivery> deliveries) {
        List<Delivery> result = new ArrayList<>();
        String query = normalize(keyword);
        for (Delivery delivery : deliveries) {
            if (!status.isEmpty() && !status.equals(delivery.getStatus())) {
                continue;
            }
            if (shopId > 0 && shopId != delivery.getShopId()) {
                continue;
            }
            if (shipperId > 0 && shipperId != delivery.getShipperId()) {
                continue;
            }
            if (!matchesKeyword(delivery, query)) {
                continue;
            }
            result.add(delivery);
        }
        return result;
    }

    /**
     * Dùng chung cho trang chủ shop và trang giao hàng của shipper.
     */
    public static void prepareList(HttpServletRequest request, List<Delivery> allDeliveries) {
        DeliveryList filter = new DeliveryList(request.getParameter("keyword"),
                request.getParameter("status"), request.getParameter("shopId"),
                request.getParameter("shipperId"));

        // Lấy lựa chọn từ toàn bộ dữ liệu được phép xem, trước khi lọc.
        Map<Integer, String> shops = new LinkedHashMap<>();
        Map<Integer, String> shippers = new LinkedHashMap<>();
        for (Delivery delivery : allDeliveries) {
            if (delivery.getShopId() > 0) {
                shops.putIfAbsent(delivery.getShopId(), delivery.getShopName());
            }
            if (delivery.getShipperId() > 0) {
                String name = clean(delivery.getShipperName());
                shippers.putIfAbsent(delivery.getShipperId(), name.isEmpty() ? "Nhân viên" : name);
            }
        }
        Map<String, String> statuses = new LinkedHashMap<>();
        for (String value : STATUSES) {
            statuses.put(value, DeliveryRules.label(value));
        }

        request.setAttribute("deliveryFilter", filter);
        request.setAttribute("deliveryShops", shops);
        request.setAttribute("deliveryShippers", shippers);
        request.setAttribute("deliveryStatuses", statuses);
        request.setAttribute("deliveryTotal", allDeliveries.size());
        request.setAttribute("deliveries", filter.apply(allDeliveries));
    }

    private boolean matchesKeyword(Delivery delivery, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String text = "#" + delivery.getId() + " #" + delivery.getOrderId() + " "
                + clean(delivery.getReceiverName()) + " " + clean(delivery.getReceiverPhone()) + " "
                + clean(delivery.getShippingAddress());
        if (normalize(text).contains(query)) {
            return true;
        }

        // Cho phép tìm số điện thoại có hoặc không có dấu cách/dấu gạch.
        String phoneQuery = query.replaceAll("[\\s.()+-]", "");
        return phoneQuery.matches("[0-9]{3,}")
                && clean(delivery.getReceiverPhone()).replaceAll("[^0-9]", "").contains(phoneQuery);
    }

    private static int parseId(String value) {
        String text = clean(value);
        if (text.isEmpty()) {
            return 0;
        }
        try {
            int id = Integer.parseInt(text);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã cửa hàng hoặc nhân viên không hợp lệ.");
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.strip();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(clean(value).toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd').replaceAll("\\s+", " ");
    }

    public String getKeyword() {
        return keyword;
    }

    public String getStatus() {
        return status;
    }

    public int getShopId() {
        return shopId;
    }

    public int getShipperId() {
        return shipperId;
    }

    public boolean isActive() {
        return !keyword.isEmpty() || !status.isEmpty() || shopId > 0 || shipperId > 0;
    }
}
