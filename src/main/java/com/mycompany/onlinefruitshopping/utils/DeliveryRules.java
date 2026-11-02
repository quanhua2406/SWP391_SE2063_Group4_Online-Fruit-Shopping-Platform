package com.mycompany.onlinefruitshopping.utils;

/** Quy tắc nghiệp vụ tách riêng để dễ đọc và dễ kiểm thử. */
public class DeliveryRules {
    public static String nextStatus(String current, String action) {
        if ("Assigned".equalsIgnoreCase(current) && "accept".equalsIgnoreCase(action)) return "Accepted";
        if ("Assigned".equalsIgnoreCase(current) && "reject".equalsIgnoreCase(action)) return "Rejected";
        if ("Accepted".equalsIgnoreCase(current) && "pickup".equalsIgnoreCase(action)) return "PickedUp";
        if ("PickedUp".equalsIgnoreCase(current) && "start".equalsIgnoreCase(action)) return "Delivering";
        if ("Delivering".equalsIgnoreCase(current) && "delivered".equalsIgnoreCase(action)) return "Delivered";
        if ("Delivering".equalsIgnoreCase(current) && "failed".equalsIgnoreCase(action)) return "Failed";
        throw new IllegalArgumentException("Thao tác không phù hợp với trạng thái hiện tại. Hãy tải lại trang.");
    }

    public static String orderStatus(String deliveryStatus) {
        if (deliveryStatus == null) throw new IllegalArgumentException("Trạng thái giao hàng không hợp lệ.");
        switch (deliveryStatus.toLowerCase()) {
            case "assigned":
            case "accepted":
            case "rejected": return "Confirmed";
            case "pickedup":
            case "delivering":
            case "failed": return "Shipping";
            case "delivered": return "Completed";
            default: throw new IllegalArgumentException("Trạng thái giao hàng không hợp lệ.");
        }
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

    public static void validatePayment(String method, String status, boolean collected) {
        if ("Paid".equals(status)) return;
        if (!"COD".equals(method)) {
            throw new IllegalArgumentException("Đơn thanh toán online chưa được xác nhận Paid. Hãy liên hệ chủ shop.");
        }
        if (!collected) {
            throw new IllegalArgumentException("Bạn cần xác nhận đã thu tiền COD trước khi hoàn tất.");
        }
    }
}
