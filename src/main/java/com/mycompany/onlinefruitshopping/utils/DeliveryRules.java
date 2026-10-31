package com.mycompany.onlinefruitshopping.utils;

/** Quy tắc nghiệp vụ tách riêng để dễ đọc và dễ kiểm thử. */
public class DeliveryRules {
    public static String nextStatus(String current, String action) {
        if ("Assigned".equals(current) && "accept".equals(action)) return "Accepted";
        if ("Assigned".equals(current) && "reject".equals(action)) return "Rejected";
        if ("Accepted".equals(current) && "pickup".equals(action)) return "PickedUp";
        if ("PickedUp".equals(current) && "start".equals(action)) return "Delivering";
        if ("Delivering".equals(current) && "delivered".equals(action)) return "Delivered";
        if ("Delivering".equals(current) && "failed".equals(action)) return "Failed";
        throw new IllegalArgumentException("Thao tác không phù hợp với trạng thái hiện tại. Hãy tải lại trang.");
    }

    public static String orderStatus(String deliveryStatus) {
        switch (deliveryStatus) {
            case "Assigned":
            case "Accepted":
            case "Rejected": return "Confirmed";
            case "PickedUp":
            case "Delivering":
            case "Failed": return "Shipping";
            case "Delivered": return "Completed";
            default: throw new IllegalArgumentException("Trạng thái giao hàng không hợp lệ.");
        }
    }

    public static String canonicalOrderStatus(String deliveryStatus) {
        return switch (deliveryStatus) {
            case "Assigned", "Accepted", "Rejected" -> "preparing";
            case "PickedUp", "Delivering" -> "shipping";
            case "Delivered" -> "delivered";
            case "Failed" -> "failed";
            default -> throw new IllegalArgumentException(
                    "Trạng thái giao hàng không hợp lệ."
            );
        };
    }

    public static String label(String status) {
        if (status == null) return "Chưa có";
        switch (status) {
            case "Assigned": return "Chờ nhận đơn";
            case "Accepted": return "Đã nhận đơn";
            case "Rejected": return "Đã từ chối";
            case "PickedUp": return "Đã lấy hàng";
            case "Delivering": return "Đang giao";
            case "Delivered": return "Giao thành công";
            case "Failed": return "Giao thất bại";
            default: return status;
        }
    }

    public static String validateReason(String nextStatus, String reason) {
        String text = reason == null ? "" : reason.trim();
        if (("Rejected".equals(nextStatus) || "Failed".equals(nextStatus)) && text.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối hoặc giao thất bại.");
        }
        if (text.length() > 255) {
            throw new IllegalArgumentException("Lý do tối đa 255 ký tự.");
        }
        return text;
    }

    public static void validatePayment(
            String method,
            String status,
            boolean collected
    ) {
        if ("paid".equalsIgnoreCase(status)) {
            return;
        }

        if (!"cod".equalsIgnoreCase(method)) {
            throw new IllegalArgumentException(
                    "Đơn thanh toán online chưa được xác nhận Paid. "
                    + "Hãy liên hệ chủ shop."
            );
        }

        if (!collected) {
            throw new IllegalArgumentException(
                    "Bạn cần xác nhận đã thu tiền COD trước khi hoàn tất."
            );
        }
    }
}
