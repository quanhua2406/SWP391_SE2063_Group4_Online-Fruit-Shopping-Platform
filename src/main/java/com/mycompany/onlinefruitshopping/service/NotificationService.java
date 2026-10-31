package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.NotificationDAO;
import com.mycompany.onlinefruitshopping.model.AdminNotificationType;
import com.mycompany.onlinefruitshopping.model.DeliveryNotificationType;
import com.mycompany.onlinefruitshopping.model.Notification;
import com.mycompany.onlinefruitshopping.model.NotificationType;
import com.mycompany.onlinefruitshopping.model.ShopOwnerNotificationType;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service Layer xử lý nghiệp vụ thông báo (Notification)
 * Hỗ trợ toàn diện cả 4 Role: Customer, Shop Owner, Admin, Delivery Staff.
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
    // 1. TẠO THÔNG BÁO CHO ADMIN
    // =========================================================================
    
    public int createNotification(int userId, String title, String content,
                                  AdminNotificationType type, String referenceType, String referenceId) {
        if (userId <= 0 || title == null || title.isBlank() || content == null || content.isBlank() || type == null) {
            LOGGER.warning("Dữ liệu tạo thông báo Admin không hợp lệ (userId=" + userId + ", title=" + title + ")");
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
        n.setImportant(type.isImportant());

        return notificationDAO.insert(n);
    }

    public int notifyAdmin(int adminUserId, String title, String content,
                           AdminNotificationType type, String referenceType, String referenceId) {
        return createNotification(adminUserId, title, content, type, referenceType, referenceId);
    }

    // =========================================================================
    // 2. TẠO THÔNG BÁO CHO CUSTOMER
    // =========================================================================

    public int createCustomerNotification(int userId, String title, String content,
                                          NotificationType type, String referenceType, String referenceId) {
        if (userId <= 0 || title == null || title.isBlank() || content == null || content.isBlank() || type == null) {
            LOGGER.warning("Dữ liệu tạo thông báo Customer không hợp lệ (userId=" + userId + ", title=" + title + ")");
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
        n.setImportant(false);

        return notificationDAO.insert(n);
    }

    public int notifyCustomer(int customerUserId, String title, String content,
                              NotificationType type, String referenceType, String referenceId) {
        return createCustomerNotification(customerUserId, title, content, type, referenceType, referenceId);
    }

    public List<Notification> getNotifications(int userId, String groupFilter, Boolean isRead, int page, int pageSize) {
        if (userId <= 0) return List.of();
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        List<Notification> list = notificationDAO.getNotifications(userId, groupFilter, isRead, null, page, pageSize);
        for (Notification n : list) {
            enrichNotificationDisplay(n);
        }
        return list;
    }

    public int countNotifications(int userId, String groupFilter, Boolean isRead) {
        if (userId <= 0) return 0;
        return notificationDAO.countNotifications(userId, groupFilter, isRead, null);
    }

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

    // =========================================================================
    // 3. TẠO THÔNG BÁO CHO SHOP OWNER
    // =========================================================================

    public int createShopOwnerNotification(int userId, String title, String content,
                                           ShopOwnerNotificationType type,
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
        n.setImportant(type.isImportant());

        return notificationDAO.insert(n);
    }

    public int notifyShopOwner(int shopOwnerUserId, String title, String content,
                               ShopOwnerNotificationType type,
                               String referenceType, String referenceId) {
        return createShopOwnerNotification(shopOwnerUserId, title, content, type, referenceType, referenceId);
    }

    public List<Notification> getShopOwnerNotifications(int userId, String groupFilter, Boolean isRead, Boolean isImportant, int page, int pageSize) {
        if (userId <= 0) return List.of();
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

    public String resolveShopOwnerTargetUrl(Notification n, String contextPath) {
        if (n == null) {
            return contextPath + "/store-owner/notifications";
        }

        String refType = n.getReferenceType() != null ? n.getReferenceType().toLowerCase() : "";
        String refId = n.getReferenceId() != null ? n.getReferenceId().trim() : "";
        String type = n.getType() != null ? n.getType().toLowerCase() : "";

        if ("low_stock".equals(type) || "product".equals(refType)) {
            return contextPath + "/store-owner/products";
        }

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
    // 4. TẠO THÔNG BÁO CHO DELIVERY STAFF
    // =========================================================================

    public int createDeliveryNotification(int staffUserId, String title, String content,
                                          DeliveryNotificationType type, String referenceId) {
        if (staffUserId <= 0 || title == null || title.isBlank() || content == null || content.isBlank() || type == null) {
            LOGGER.warning("Dữ liệu thông báo Delivery Staff không hợp lệ (staffUserId=" + staffUserId + ", title=" + title + ")");
            return -1;
        }

        Notification n = new Notification();
        n.setUserId(staffUserId);
        n.setTitle(title.trim());
        n.setContent(content.trim());
        n.setType(type.getCode());
        n.setReferenceType("delivery");
        n.setReferenceId(referenceId);
        n.setChannel("in_app");
        n.setRead(false);
        n.setImportant(type.isDefaultImportant());

        return notificationDAO.insert(n);
    }

    public int notifyDeliveryAssignment(int staffUserId, int orderId, String deliveryAddress) {
        String title = "Đơn hàng mới được phân công #" + orderId;
        String content = "Bạn vừa được phân công giao đơn hàng #" + orderId 
                + (deliveryAddress != null && !deliveryAddress.isBlank() ? ". Địa chỉ: " + deliveryAddress : "");
        return createDeliveryNotification(staffUserId, title, content, 
                DeliveryNotificationType.NEW_DELIVERY_ASSIGNMENT, String.valueOf(orderId));
    }

    public int notifyDeliveryStatusUpdate(int staffUserId, int orderId, String statusText) {
        String title = "Cập nhật trạng thái đơn hàng #" + orderId;
        String content = "Trạng thái đơn hàng #" + orderId + " đã cập nhật: " + statusText;
        return createDeliveryNotification(staffUserId, title, content, 
                DeliveryNotificationType.DELIVERY_STATUS_UPDATE, String.valueOf(orderId));
    }

    public List<Notification> getDeliveryNotifications(int userId, String typeFilter, Boolean isRead, int page, int pageSize) {
        if (userId <= 0) return List.of();
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        List<Notification> list = notificationDAO.getNotifications(userId, typeFilter, isRead, null, page, pageSize);
        for (Notification n : list) {
            enrichNotificationDisplay(n);
        }
        return list;
    }

    public String resolveDeliveryTargetUrl(Notification n, String contextPath) {
        if (n == null) {
            return contextPath + "/delivery/notifications";
        }
        String refId = n.getReferenceId() != null ? n.getReferenceId().trim() : "";
        if (!refId.isEmpty()) {
            return contextPath + "/shopowner/delivery/detail?id=" + refId;
        }
        return contextPath + "/shopowner/delivery/list";
    }

    // =========================================================================
    // 5. CHUNG CHO ADMIN
    // =========================================================================

    public List<Notification> getAdminNotifications(int userId, String groupFilter, Boolean isRead, Boolean isImportant, int page, int pageSize) {
        if (userId <= 0) return List.of();
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        List<Notification> list = notificationDAO.getNotifications(userId, groupFilter, isRead, isImportant, page, pageSize);
        for (Notification n : list) {
            enrichNotificationDisplay(n);
        }
        return list;
    }

    public int countAdminNotifications(int userId, String groupFilter, Boolean isRead, Boolean isImportant) {
        if (userId <= 0) return 0;
        return notificationDAO.countNotifications(userId, groupFilter, isRead, isImportant);
    }

    public String resolveAdminTargetUrl(Notification n, String contextPath) {
        if (n == null) {
            return contextPath + "/admin/notifications";
        }

        String refType = n.getReferenceType() != null ? n.getReferenceType().toLowerCase() : "";
        String refId = n.getReferenceId() != null ? n.getReferenceId().trim() : "";
        String type = n.getType() != null ? n.getType().toLowerCase() : "";

        if ("reported_review".equals(type) || "review".equals(refType)) {
            if (!refId.isEmpty()) {
                return contextPath + "/admin/reviews?status=reported&reviewId=" + refId;
            }
            return contextPath + "/admin/reviews?status=reported";
        }

        if ("critical_low_stock".equals(type) || "product".equals(refType)) {
            return contextPath + "/admin/products";
        }

        if ("refund_request".equals(type) || "refund".equals(refType)) {
            return contextPath + "/admin/orders?status=refund_pending";
        }
        if ("order_cancelled".equals(type) || "order".equals(refType)) {
            if (!refId.isEmpty()) {
                return contextPath + "/admin/orders?action=detail&orderId=" + refId;
            }
            return contextPath + "/admin/orders?status=cancelled";
        }

        if ("new_shop_registration".equals(type) || "shop".equals(refType)) {
            return contextPath + "/admin/shops?status=pending";
        }

        if ("system_alert".equals(type)) {
            return contextPath + "/admin/dashboard";
        }

        return contextPath + "/admin/notifications";
    }

    // =========================================================================
    // 6. TIỆN ÍCH CHUNG: ĐỌC, ĐẾM, PHÂN TRANG
    // =========================================================================

    public int calculateTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0 || pageSize <= 0) return 1;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public int getUnreadCount(int userId) {
        if (userId <= 0) return 0;
        return notificationDAO.countUnreadByUserId(userId);
    }

    public int getImportantUnreadCount(int userId) {
        if (userId <= 0) return 0;
        return notificationDAO.countImportantUnreadByUserId(userId);
    }

    public Map<String, Integer> getUnreadCountPerGroup(int userId) {
        if (userId <= 0) return Map.of();
        return notificationDAO.countUnreadPerGroup(userId);
    }

    public boolean markAsRead(int notificationId, int userId) {
        if (notificationId <= 0 || userId <= 0) return false;
        return notificationDAO.markAsRead(notificationId, userId);
    }

    public int markAllAsRead(int userId) {
        if (userId <= 0) return 0;
        return notificationDAO.markAllAsRead(userId);
    }

    public Notification getNotificationById(int notificationId, int userId) {
        if (notificationId <= 0 || userId <= 0) return null;
        Notification n = notificationDAO.getById(notificationId, userId);
        if (n != null) {
            enrichNotificationDisplay(n);
        }
        return n;
    }

    // =========================================================================
    // 7. PRIVATE HELPER ENRICH DISPLAY
    // =========================================================================

    private void enrichNotificationDisplay(Notification n) {
        String typeCode = n.getType();

        // 1. Thử Admin
        AdminNotificationType adminType = AdminNotificationType.fromCode(typeCode);
        if (adminType != null) {
            n.setIcon(adminType.getDefaultIcon());
            n.setTypeDisplayName(adminType.getDisplayName());
            n.setActionText(adminType.getDefaultActionText());
            n.setTimeAgo(calculateTimeAgo(n.getCreatedAt()));
            return;
        }

        // 2. Thử Delivery
        DeliveryNotificationType deliveryType = DeliveryNotificationType.fromCode(typeCode);
        if (deliveryType != null) {
            n.setIcon(deliveryType.getIcon());
            n.setTypeDisplayName(deliveryType.getDisplayName());
            n.setActionText(deliveryType.getDefaultActionText());
            n.setTimeAgo(calculateTimeAgo(n.getCreatedAt()));
            return;
        }

        // 3. Thử Shop Owner
        ShopOwnerNotificationType shopType = ShopOwnerNotificationType.fromCode(typeCode);
        if (shopType != null) {
            n.setIcon(shopType.getDefaultIcon());
            n.setTypeDisplayName(shopType.getDisplayName());
            n.setActionText(shopType.getDefaultActionText());
            n.setTimeAgo(calculateTimeAgo(n.getCreatedAt()));
            return;
        }

        // 4. Thử Customer
        NotificationType custType = NotificationType.fromCode(typeCode);
        if (custType != null) {
            n.setIcon(custType.getDefaultIcon());
            n.setTypeDisplayName(custType.getDisplayName());
            n.setActionText(custType.getDefaultActionText());
            n.setTimeAgo(calculateTimeAgo(n.getCreatedAt()));
            return;
        }

        // Fallback mặc định
        n.setIcon("🔔");
        n.setTypeDisplayName("Thông báo");
        n.setActionText("Xem chi tiết");
        n.setTimeAgo(calculateTimeAgo(n.getCreatedAt()));
    }

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
