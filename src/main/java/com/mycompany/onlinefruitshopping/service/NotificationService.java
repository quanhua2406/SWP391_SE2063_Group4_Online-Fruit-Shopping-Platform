package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.NotificationDAO;
import com.mycompany.onlinefruitshopping.model.Notification;
import com.mycompany.onlinefruitshopping.model.NotificationType;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service Layer xử lý nghiệp vụ thông báo (Notification) dành cho Customer.
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    // =========================================================================
    // 1. TẠO THÔNG BÁO (DÙNG CHO CÁC MODULE KHÁC GỌI VÍ DỤ ORDER, PAYMENT, SUB)
    // =========================================================================
    
    /**
     * Phương thức tiện ích để các module khác gọi tạo thông báo cho khách hàng
     *
     * @param userId        ID khách hàng nhận thông báo
     * @param title         Tiêu đề thông báo
     * @param content       Nội dung chi tiết
     * @param type          Enum loại thông báo
     * @param referenceType Loại đối tượng tham chiếu (order, subscription, promotion, delivery...)
     * @param referenceId   ID tham chiếu (ví dụ ID đơn hàng, ID gói...)
     * @return ID thông báo vừa tạo, hoặc -1 nếu thất bại
     */
    public int createNotification(int userId, String title, String content, 
                                  NotificationType type, String referenceType, String referenceId) {
        if (userId <= 0 || title == null || title.isBlank() || content == null || content.isBlank() || type == null) {
            LOGGER.warning("Dữ liệu tạo thông báo không hợp lệ (userId=" + userId + ", title=" + title + ")");
            return -1;
        }

        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title.trim());
        n.setContent(content.trim());
        n.setType(type.getCode());
        n.setReferenceType(referenceType);
        n.setReferenceId(referenceId);
        n.setChannel("in_app");
        n.setRead(false);
        n.setImportant(type == NotificationType.ORDER_CONFIRMATION || type == NotificationType.PAYMENT_FAILED);

        return notificationDAO.insert(n);
    }

    /**
     * Phương thức tạo thông báo chi tiết
     */
    public int createNotification(int userId, String title, String content, String typeStr,
                                  String referenceType, String referenceId, String channel, boolean isImportant) {
        if (userId <= 0 || title == null || title.isBlank() || content == null || content.isBlank()) {
            return -1;
        }

        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title.trim());
        n.setContent(content.trim());
        n.setType(typeStr != null ? typeStr.trim() : "other");
        n.setReferenceType(referenceType);
        n.setReferenceId(referenceId);
        n.setChannel(channel != null ? channel : "in_app");
        n.setRead(false);
        n.setImportant(isImportant);

        return notificationDAO.insert(n);
    }

    // =========================================================================
    // 2. LẤY DANH SÁCH THÔNG BÁO CỦA USER (CÓ ENRICH DỮ LIỆU HIỂN THỊ)
    // =========================================================================
    public List<Notification> getNotifications(int userId, String groupFilter, Boolean isRead, int page, int pageSize) {
        if (userId <= 0) {
            return List.of();
        }
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        List<Notification> list = notificationDAO.getNotifications(userId, groupFilter, isRead, page, pageSize);

        // Enrich thông tin hiển thị cho View JSP
        for (Notification n : list) {
            enrichNotificationDisplay(n);
        }

        return list;
    }

    /**
     * Đếm tổng số lượng thông báo theo bộ lọc
     */
    public int countNotifications(int userId, String groupFilter, Boolean isRead) {
        if (userId <= 0) return 0;
        return notificationDAO.countNotifications(userId, groupFilter, isRead);
    }

    /**
     * Tính tổng số trang (phục vụ phân trang trên JSP)
     */
    public int calculateTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0 || pageSize <= 0) return 1;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * Đếm số lượng thông báo chưa đọc cho quả chuông Header
     */
    public int getUnreadCount(int userId) {
        if (userId <= 0) return 0;
        return notificationDAO.countUnreadByUserId(userId);
    }

    /**
     * Đếm số lượng chưa đọc cho từng tab sidebar
     */
    public Map<String, Integer> getUnreadCountPerGroup(int userId) {
        if (userId <= 0) return Map.of();
        return notificationDAO.countUnreadPerGroup(userId);
    }

    // =========================================================================
    // 3. ĐÁNH DẤU ĐÃ ĐỌC (MARK AS READ)
    // =========================================================================
    
    /**
     * Đánh dấu 1 thông báo đã đọc
     */
    public boolean markAsRead(int notificationId, int userId) {
        if (notificationId <= 0 || userId <= 0) return false;
        return notificationDAO.markAsRead(notificationId, userId);
    }

    /**
     * Đánh dấu toàn bộ thông báo của khách hàng là đã đọc
     */
    public int markAllAsRead(int userId) {
        if (userId <= 0) return 0;
        return notificationDAO.markAllAsRead(userId);
    }

    /**
     * Lấy thông báo theo ID (kiểm tra quyền sở hữu)
     */
    public Notification getNotificationById(int notificationId, int userId) {
        if (notificationId <= 0 || userId <= 0) return null;
        Notification n = notificationDAO.getById(notificationId, userId);
        if (n != null) {
            enrichNotificationDisplay(n);
        }
        return n;
    }

    /**
     * Xác định URL điều hướng khi khách hàng bấm vào thông báo hoặc nút hành động
     */
    public String resolveTargetUrl(Notification n, String contextPath) {
        if (n == null) {
            return contextPath + "/notifications";
        }

        String refType = n.getReferenceType() != null ? n.getReferenceType().toLowerCase() : "";
        String refId = n.getReferenceId() != null ? n.getReferenceId().trim() : "";

        return switch (refType) {
            case "order", "delivery", "payment" -> {
                if (!refId.isEmpty()) {
                    yield contextPath + "/customer/dashboard?tab=orders&orderId=" + refId;
                }
                yield contextPath + "/customer/dashboard";
            }
            case "subscription" -> contextPath + "/my-subscriptions";
            case "promotion" -> contextPath + "/home";
            default -> contextPath + "/notifications";
        };
    }

    /**
     * Phương thức tiện ích để các module khác gọi tạo thông báo cho Shop Owner
     * Quy định nghiệp vụ: new_order, low_stock, refund_request bắt buộc có isImportant = true.
     */
    public int createNotification(int userId, String title, String content,
                                  com.mycompany.onlinefruitshopping.model.ShopOwnerNotificationType type,
                                  String referenceType, String referenceId) {
        if (userId <= 0 || title == null || title.isBlank() || content == null || content.isBlank() || type == null) {
            LOGGER.warning("Dữ liệu tạo thông báo Shop Owner không hợp lệ (userId=" + userId + ", title=" + title + ")");
            return -1;
        }

        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title.trim());
        n.setContent(content.trim());
        n.setType(type.getCode());
        n.setReferenceType(referenceType);
        n.setReferenceId(referenceId);
        n.setChannel("in_app");
        n.setRead(false);
        n.setImportant(type.isImportant()); // Tự động gắn cờ theo enum

        return notificationDAO.insert(n);
    }

    public int notifyShopOwner(int shopOwnerUserId, String title, String content,
                               com.mycompany.onlinefruitshopping.model.ShopOwnerNotificationType type,
                               String referenceType, String referenceId) {
        return createNotification(shopOwnerUserId, title, content, type, referenceType, referenceId);
    }

    // =========================================================================
    // 2. LẤY DANH SÁCH THÔNG BÁO CHO SHOP OWNER (CÓ LỌC IS_IMPORTANT)
    // =========================================================================
    public List<Notification> getShopOwnerNotifications(int userId, String groupFilter, Boolean isRead, Boolean isImportant, int page, int pageSize) {
        if (userId <= 0) {
            return List.of();
        }
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        List<Notification> list = notificationDAO.getNotifications(userId, groupFilter, isRead, isImportant, page, pageSize);

        for (Notification n : list) {
            enrichNotificationDisplay(n);
        }

        return list;
    }

    public int countShopOwnerNotifications(int userId, String groupFilter, Boolean isRead, Boolean isImportant) {
        if (userId <= 0) return 0;
        return notificationDAO.countNotifications(userId, groupFilter, isRead, isImportant);
    }

    public int getImportantUnreadCount(int userId) {
        if (userId <= 0) return 0;
        return notificationDAO.countImportantUnreadByUserId(userId);
    }

    /**
     * Phân giải URL điều hướng dành riêng cho Shop Owner
     */
    public String resolveShopOwnerTargetUrl(Notification n, String contextPath) {
        if (n == null) {
            return contextPath + "/store-owner/notifications";
        }

        String refType = n.getReferenceType() != null ? n.getReferenceType().toLowerCase() : "";
        String refId = n.getReferenceId() != null ? n.getReferenceId().trim() : "";
        String type = n.getType() != null ? n.getType().toLowerCase() : "";

        // Nếu là cảnh báo tồn kho -> dẫn đến trang sản phẩm
        if ("low_stock".equals(type) || "product".equals(refType)) {
            return contextPath + "/store-owner/products";
        }

        // Nếu là đơn hàng, thanh toán, hoàn tiền, hủy đơn -> dẫn đến trang chi tiết đơn hàng
        if ("order".equals(refType) || "new_order".equals(type) || "refund_request".equals(type) ||
            "order_cancelled".equals(type) || "payment_received".equals(type)) {
            if (!refId.isEmpty()) {
                return contextPath + "/store-owner/orders?action=detail&orderId=" + refId;
            }
            return contextPath + "/store-owner/orders";
        }

        return contextPath + "/store-owner/notifications";
    }

    // =========================================================================
    // 4. PRIVATE HELPER ENRICH DISPLAY
    // =========================================================================
    private void enrichNotificationDisplay(Notification n) {
        // Kiểm tra loại thông báo của Shop Owner trước
        com.mycompany.onlinefruitshopping.model.ShopOwnerNotificationType shopType =
                com.mycompany.onlinefruitshopping.model.ShopOwnerNotificationType.fromCode(n.getType());

        if (shopType != null) {
            n.setIcon(shopType.getDefaultIcon());
            n.setTypeDisplayName(shopType.getDisplayName());
            n.setActionText(shopType.getDefaultActionText());
        } else {
            // Kiểm tra loại thông báo của Customer
            NotificationType custType = NotificationType.fromCode(n.getType());
            if (custType != null) {
                n.setIcon(custType.getDefaultIcon());
                n.setTypeDisplayName(custType.getDisplayName());
                n.setActionText(custType.getDefaultActionText());
            } else {
                n.setIcon("🔔");
                n.setTypeDisplayName("Thông báo");
                n.setActionText("Xem chi tiết");
            }
        }

        // Định dạng thời gian tương đối timeAgo
        n.setTimeAgo(calculateTimeAgo(n.getCreatedAt()));
    }

    /**
     * Tính thời gian hiển thị tương đối: "Vừa xong", "5 phút trước", "Hôm qua", v.v.
     */
    private String calculateTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "";

        long diffMillis = System.currentTimeMillis() - timestamp.getTime();
        long diffSeconds = diffMillis / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;

        if (diffMinutes < 1) {
            return "Vừa xong";
        } else if (diffMinutes < 60) {
            return diffMinutes + " phút trước";
        } else if (diffHours < 24) {
            return diffHours + " giờ trước";
        } else if (diffDays == 1) {
            return "Hôm qua";
        } else if (diffDays < 7) {
            return diffDays + " ngày trước";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(new Date(timestamp.getTime()));
        }
    }
}
