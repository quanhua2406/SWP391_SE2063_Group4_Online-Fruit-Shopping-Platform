package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.subscription.SubscriptionPlanDAO;
import com.mycompany.onlinefruitshopping.model.Subscription;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlan;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlanItem;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service Layer xử lý toàn bộ nghiệp vụ (Business Logic) liên quan đến Subscription & Subscription Plans.
 *
 * Tách biệt rõ ràng:
 * - Controller (Servlet): Nhận HTTP request, kiểm tra Session/Auth, đọc tham số và điều hướng.
 * - Service: Xử lý nghiệp vụ, kiểm tra ràng buộc, xác thực dữ liệu, bảo đảm an toàn khi cập nhật/xóa.
 * - DAO: Tương tác trực tiếp với Database.
 */
public class SubscriptionService {

    private static final Logger LOG = Logger.getLogger(SubscriptionService.class.getName());

    private final SubscriptionPlanDAO planDAO;

    public SubscriptionService() {
        this.planDAO = new SubscriptionPlanDAO();
    }

    public SubscriptionService(SubscriptionPlanDAO planDAO) {
        this.planDAO = planDAO;
    }

    // ═════════════════════════════════════════════════════════════════════
    // RESULT WRAPPER
    // ═════════════════════════════════════════════════════════════════════
    public static class ServiceResult<T> {
        private final boolean success;
        private final String message;
        private final T data;
        private final List<String> errors;

        public ServiceResult(boolean success, String message, T data, List<String> errors) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public static <T> ServiceResult<T> ok(String message, T data) {
            return new ServiceResult<>(true, message, data, null);
        }

        public static <T> ServiceResult<T> ok(String message) {
            return new ServiceResult<>(true, message, null, null);
        }

        public static <T> ServiceResult<T> fail(String message, List<String> errors) {
            return new ServiceResult<>(false, message, null, errors);
        }

        public static <T> ServiceResult<T> fail(String message) {
            List<String> errs = new ArrayList<>();
            errs.add(message);
            return new ServiceResult<>(false, message, null, errs);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public List<String> getErrors() { return errors; }
    }

    // ═════════════════════════════════════════════════════════════════════
    // 1. SHOP OWNER RESOLVER
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Lấy shop_id tương ứng của chủ shop (ownerId).
     * @return shop_id nếu tồn tại và active, ngược lại -1
     */
    public int getShopIdByOwner(int ownerId) {
        if (ownerId <= 0) return -1;
        String sql = "SELECT TOP 1 shop_id FROM shops WHERE owner_id = ? ORDER BY CASE WHEN LOWER(status) = 'active' THEN 1 ELSE 2 END, shop_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shop_id");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getShopIdByOwner query error", e);
        }

        // Auto-create shop if none exists yet for this store owner
        String createSql = "INSERT INTO shops (owner_id, shop_name, description, status) VALUES (?, ?, ?, 'active')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(createSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ownerId);
            ps.setString(2, "Cửa hàng của Shop Owner #" + ownerId);
            ps.setString(3, "Cửa hàng trái cây tươi sạch phân phối trực tiếp.");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "autoCreateShop error for owner " + ownerId, e);
        }

        return -1;
    }

    // ═════════════════════════════════════════════════════════════════════
    // 2. NGHIỆP VỤ XEM DANH SÁCH GÓI (VIEW PLANS)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách gói đăng ký của shop có phân trang và bộ lọc.
     */
    public List<SubscriptionPlan> getPlansByShop(int shopId, String status, int page, int pageSize) {
        if (shopId <= 0) return new ArrayList<>();
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        return planDAO.getPlansByShop(shopId, status, page, pageSize);
    }

    /**
     * Đếm tổng số gói của shop theo bộ lọc trạng thái.
     */
    public int countPlansByShop(int shopId, String status) {
        if (shopId <= 0) return 0;
        return planDAO.countPlansByShop(shopId, status);
    }

    /**
     * Tính tổng số trang dựa trên số bản ghi và kích thước trang.
     */
    public int calculateTotalPages(int totalCount, int pageSize) {
        if (pageSize <= 0) pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        return Math.max(1, totalPages);
    }

    /**
     * Lấy chi tiết gói đăng ký kèm danh sách sản phẩm trong combo.
     * Kiểm tra quyền sở hữu gói theo shopId.
     */
    public SubscriptionPlan getPlanDetails(int planId, int shopId) {
        if (planId <= 0 || shopId <= 0) return null;
        SubscriptionPlan plan = planDAO.getPlanById(planId);
        if (plan == null || plan.getShopId() != shopId) {
            return null;
        }
        // Nạp danh sách items thuộc combo
        List<SubscriptionPlanItem> items = planDAO.getItemsByPlanId(planId);
        plan.setItems(items);
        return plan;
    }

    /**
     * Lấy danh sách biến thể sản phẩm đang active của shop để chọn vào combo.
     */
    public List<SubscriptionPlanItem> getAvailableVariantsByShop(int shopId) {
        if (shopId <= 0) return new ArrayList<>();
        return planDAO.getAvailableVariantsByShop(shopId);
    }

    // ═════════════════════════════════════════════════════════════════════
    // 3. VALIDATION DỮ LIỆU GÓI ĐĂNG KÝ
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Kiểm tra tính hợp lệ của gói đăng ký.
     * @return Danh sách thông báo lỗi nếu có, rỗng nếu hợp lệ.
     */
    public List<String> validatePlan(SubscriptionPlan p) {
        List<String> errors = new ArrayList<>();
        if (p == null) {
            errors.add("Dữ liệu gói đăng ký không được rỗng.");
            return errors;
        }

        if (p.getName() == null || p.getName().trim().isEmpty()) {
            errors.add("Tên gói đăng ký không được để trống.");
        } else if (p.getName().trim().length() > 100) {
            errors.add("Tên gói đăng ký không được vượt quá 100 ký tự.");
        }

        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Giá gói phải là số không âm.");
        }

        if (!SubscriptionPlan.isValidCycle(p.getBillingCycle())) {
            errors.add("Chu kỳ thanh toán không hợp lệ (weekly, monthly, quarterly, yearly).");
        }

        if (p.getDurationDays() <= 0) {
            errors.add("Số ngày hiệu lực phải lớn hơn 0.");
        }

        String status = p.getStatus();
        if (status != null && !status.isEmpty()) {
            if (!SubscriptionPlan.STATUS_DRAFT.equals(status) &&
                    !SubscriptionPlan.STATUS_ACTIVE.equals(status) &&
                    !SubscriptionPlan.STATUS_INACTIVE.equals(status)) {
                errors.add("Trạng thái gói không hợp lệ (draft, active, inactive).");
            }
        }

        return errors;
    }

    // ═════════════════════════════════════════════════════════════════════
    // 4. NGHIỆP VỤ TẠO GÓI MỚI (CREATE PLAN)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Tạo gói đăng ký mới kèm các sản phẩm trong combo.
     */
    public ServiceResult<Integer> createPlan(SubscriptionPlan plan, List<SubscriptionPlanItem> items) {
        List<String> errors = validatePlan(plan);
        if (!errors.isEmpty()) {
            return ServiceResult.fail("Dữ liệu nhập vào chưa hợp lệ.", errors);
        }

        int newId = planDAO.createPlan(plan);
        if (newId <= 0) {
            return ServiceResult.fail("Lỗi hệ thống: Không thể tạo gói đăng ký vào cơ sở dữ liệu.");
        }

        // Lưu danh sách sản phẩm combo nếu có
        if (items != null && !items.isEmpty()) {
            boolean itemsSaved = planDAO.insertItems(newId, items);
            if (!itemsSaved) {
                LOG.log(Level.WARNING, "Không thể lưu sản phẩm combo cho planId: " + newId);
            }
        }

        return ServiceResult.ok("Tạo gói đăng ký thành công!", newId);
    }

    // ═════════════════════════════════════════════════════════════════════
    // 5. NGHIỆP VỤ CẬP NHẬT GÓI (UPDATE PLAN)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Cập nhật thông tin gói đăng ký và danh sách sản phẩm trong combo.
     */
    public ServiceResult<Void> updatePlan(int planId, int shopId, SubscriptionPlan updatedPlan, List<SubscriptionPlanItem> items) {
        if (planId <= 0 || shopId <= 0 || updatedPlan == null) {
            return ServiceResult.fail("Yêu cầu không hợp lệ.");
        }

        SubscriptionPlan existing = planDAO.getPlanById(planId);
        if (existing == null || existing.getShopId() != shopId) {
            return ServiceResult.fail("Gói đăng ký không tồn tại hoặc bạn không có quyền chỉnh sửa.");
        }

        updatedPlan.setPlanId(planId);
        updatedPlan.setShopId(shopId);
        List<String> errors = validatePlan(updatedPlan);
        if (!errors.isEmpty()) {
            return ServiceResult.fail("Dữ liệu cập nhật chưa hợp lệ.", errors);
        }

        boolean ok = planDAO.updatePlan(updatedPlan);
        if (!ok) {
            return ServiceResult.fail("Không thể cập nhật gói đăng ký. Vui lòng thử lại sau.");
        }

        // Cập nhật items combo: xóa cũ, thêm mới
        planDAO.deleteItemsByPlanId(planId);
        if (items != null && !items.isEmpty()) {
            planDAO.insertItems(planId, items);
        }

        return ServiceResult.ok("Đã cập nhật gói đăng ký thành công.", null);
    }

    // ═════════════════════════════════════════════════════════════════════
    // 6. BẬT / TẮT TRẠNG THÁI (ACTIVATE / DEACTIVATE)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Chuyển đổi trạng thái giữa Active và Inactive.
     * @return ServiceResult chứa nhãn trạng thái mới
     */
    public ServiceResult<String> togglePlanStatus(int planId, int shopId) {
        if (planId <= 0 || shopId <= 0) {
            return ServiceResult.fail("Yêu cầu không hợp lệ.");
        }

        SubscriptionPlan plan = planDAO.getPlanById(planId);
        if (plan == null || plan.getShopId() != shopId) {
            return ServiceResult.fail("Gói đăng ký không tồn tại hoặc bạn không có quyền thao tác.");
        }

        String newStatus = SubscriptionPlan.STATUS_ACTIVE.equals(plan.getStatus())
                ? SubscriptionPlan.STATUS_INACTIVE
                : SubscriptionPlan.STATUS_ACTIVE;

        boolean ok = planDAO.updateStatus(planId, newStatus);
        if (!ok) {
            return ServiceResult.fail("Không thể thay đổi trạng thái gói đăng ký.");
        }

        String msg = SubscriptionPlan.STATUS_ACTIVE.equals(newStatus)
                ? "Đã kích hoạt gói đăng ký."
                : "Đã tạm dừng gói đăng ký.";
        return ServiceResult.ok(msg, newStatus);
    }

    // ═════════════════════════════════════════════════════════════════════
    // 7. XÓA GÓI AN TOÀN (SOFT DELETE)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Nghiệp vụ xóa gói đăng ký:
     * Quy tắc bắt buộc: KHÔNG ĐƯỢC XÓA nếu gói đang có khách hàng đăng ký active.
     */
    public ServiceResult<Void> deletePlan(int planId, int shopId) {
        if (planId <= 0 || shopId <= 0) {
            return ServiceResult.fail("Yêu cầu không hợp lệ.");
        }

        SubscriptionPlan plan = planDAO.getPlanById(planId);
        if (plan == null || plan.getShopId() != shopId) {
            return ServiceResult.fail("Gói đăng ký không tồn tại hoặc bạn không có quyền thao tác.");
        }

        int activeSubscribers = planDAO.countActiveSubscribers(planId);
        if (activeSubscribers > 0) {
            return ServiceResult.fail("Không thể xóa gói đăng ký đang có " + activeSubscribers + " khách hàng đăng ký.");
        }

        boolean ok = planDAO.softDeletePlan(planId);
        if (!ok) {
            return ServiceResult.fail("Không thể xóa gói đăng ký. Vui lòng thử lại sau.");
        }

        return ServiceResult.ok("Đã xóa gói đăng ký thành công.", null);
    }

    // ═════════════════════════════════════════════════════════════════════
    // 8. NGHIỆP VỤ KHÁCH HÀNG XEM GÓI COMBO (BROWSE PLANS)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách gói combo đang active cho khách hàng xem.
     */
    public List<SubscriptionPlan> getActivePlansForCustomer(Integer shopId, String billingCycle, String keyword, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 9;
        return planDAO.getActivePlansForCustomer(shopId, billingCycle, keyword, page, pageSize);
    }

    /**
     * Đếm tổng số gói combo đang active phù hợp bộ lọc.
     */
    public int countActivePlansForCustomer(Integer shopId, String billingCycle, String keyword) {
        return planDAO.countActivePlansForCustomer(shopId, billingCycle, keyword);
    }

    /**
     * Lấy danh sách shops cho dropdown bộ lọc của khách.
     */
    public List<java.util.Map<String, Object>> getActiveShopsForFilter() {
        return planDAO.getActiveShopsForFilter();
    }

    /**
     * Lấy chi tiết gói active cho khách chuẩn bị đăng ký.
     */
    public SubscriptionPlan getActivePlanDetail(int planId) {
        if (planId <= 0) return null;
        SubscriptionPlan plan = planDAO.getPlanById(planId);
        if (plan == null || !SubscriptionPlan.STATUS_ACTIVE.equals(plan.getStatus()) || plan.getDeletedAt() != null) {
            return null;
        }
        return plan;
    }

    /**
     * Tính tổng giá trị gốc của các sản phẩm trong combo và phần trăm tiết kiệm.
     * @return Map chứa: originalPrice (BigDecimal), savingsPercent (int), savingsAmount (BigDecimal)
     */
    public java.util.Map<String, Object> calculatePlanSavings(SubscriptionPlan plan) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        BigDecimal original = BigDecimal.ZERO;
        if (plan != null && plan.getItems() != null) {
            for (SubscriptionPlanItem it : plan.getItems()) {
                BigDecimal itemPrice = BigDecimal.valueOf(it.getUnitPrice());
                BigDecimal qty = BigDecimal.valueOf(it.getQuantity());
                original = original.add(itemPrice.multiply(qty));
            }
        }
        map.put("originalPrice", original);

        BigDecimal planPrice = plan != null && plan.getPrice() != null ? plan.getPrice() : BigDecimal.ZERO;
        BigDecimal savingsAmount = original.subtract(planPrice);
        if (savingsAmount.compareTo(BigDecimal.ZERO) < 0) {
            savingsAmount = BigDecimal.ZERO;
        }
        map.put("savingsAmount", savingsAmount);

        int savingsPercent = 0;
        if (original.compareTo(BigDecimal.ZERO) > 0 && savingsAmount.compareTo(BigDecimal.ZERO) > 0) {
            savingsPercent = (int) Math.round((savingsAmount.doubleValue() / original.doubleValue()) * 100);
        }
        map.put("savingsPercent", savingsPercent);

        return map;
    }

    // ═════════════════════════════════════════════════════════════════════
    // 9. NGHIỆP VỤ KHÁCH HÀNG ĐĂNG KÝ GÓI (SUBSCRIBE COMBO)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Nghiệp vụ đăng ký gói combo:
     * - Kiểm tra user hợp lệ
     * - Kiểm tra gói tồn tại, đang active, chưa bị xóa
     * - Kiểm tra gói có sản phẩm trong combo
     * - Kiểm tra ngày giao hàng đầu tiên (phải từ hôm nay trở đi)
     * - Kiểm tra khách hàng không bị trùng gói active cùng plan (tránh double charge)
     * - Tính toán ngày giao hàng tiếp theo và ngày kết thúc kỳ gói
     * - Lưu snapshot giá gói lúc đăng ký
     */
    public ServiceResult<Integer> subscribeToComboPlan(int userId, int planId, Date firstDeliveryDate, String notes) {
        if (userId <= 0) {
            return ServiceResult.fail("Vui lòng đăng nhập để thực hiện đăng ký gói.");
        }
        if (planId <= 0) {
            return ServiceResult.fail("Gói đăng ký không hợp lệ.");
        }

        SubscriptionPlan plan = planDAO.getPlanById(planId);
        if (plan == null || plan.getDeletedAt() != null) {
            return ServiceResult.fail("Gói đăng ký không tồn tại hoặc đã ngừng cung cấp.");
        }
        if (!SubscriptionPlan.STATUS_ACTIVE.equals(plan.getStatus())) {
            return ServiceResult.fail("Gói đăng ký hiện đang tạm dừng, không tiếp nhận đăng ký mới.");
        }

        List<SubscriptionPlanItem> items = planDAO.getItemsByPlanId(planId);
        if (items == null || items.isEmpty()) {
            return ServiceResult.fail("Gói combo này hiện chưa có sản phẩm nào. Vui lòng chọn gói khác.");
        }

        // Kiểm tra xem khách đã có gói active của plan này chưa
        int activeCount = planDAO.countActiveSubscriptionsOfUser(userId, planId);
        if (activeCount > 0) {
            return ServiceResult.fail("Bạn hiện đã có một gói đăng ký đang hoạt động cho combo này. Hãy kiểm tra mục 'Gói Của Tôi'.");
        }

        // Kiểm tra ngày giao hàng
        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);

        if (firstDeliveryDate == null) {
            // Mặc định ngày mai nếu không chọn
            Calendar defaultDate = Calendar.getInstance();
            defaultDate.add(Calendar.DAY_OF_MONTH, 1);
            firstDeliveryDate = defaultDate.getTime();
        } else if (firstDeliveryDate.before(calToday.getTime())) {
            return ServiceResult.fail("Ngày giao hàng đầu tiên không thể là ngày trong quá khứ.");
        }

        // Tính ngày kết thúc kỳ: firstDeliveryDate + durationDays
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(firstDeliveryDate);
        calEnd.add(Calendar.DAY_OF_MONTH, plan.getDurationDays());
        Date endDate = calEnd.getTime();

        Subscription sub = new Subscription();
        sub.setUser_id(userId);
        sub.setPlan_id(planId);
        sub.setIs_custom(false);
        sub.setPrice_snapshot(plan.getPrice());
        sub.setFrequency(plan.getBillingCycle());
        sub.setNext_delivery_date(firstDeliveryDate);
        sub.setEnd_date(endDate);
        sub.setStatus(Subscription.STATUS_ACTIVE);
        // Để thỏa mãn FK NOT NULL variant_id trong DB, lấy variant đầu tiên của combo
        sub.setVariant_id(items.get(0).getVariantId());

        int subId = planDAO.createSubscription(sub);
        if (subId <= 0) {
            return ServiceResult.fail("Không thể tạo gói đăng ký do lỗi hệ thống. Vui lòng thử lại sau.");
        }

        return ServiceResult.ok("Đăng ký gói combo '" + plan.getName() + "' thành công!", subId);
    }

    // ═════════════════════════════════════════════════════════════════════
    // 10. NGHIỆP VỤ QUẢN LÝ GÓI CỦA KHÁCH HÀNG (MY SUBSCRIPTIONS)
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách các gói subscription của khách hàng (kèm thống kê).
     */
    public List<Subscription> getCustomerSubscriptions(int userId, String status) {
        if (userId <= 0) return new ArrayList<>();
        return planDAO.getSubscriptionsByCustomer(userId, status);
    }

    /**
     * Lấy chi tiết gói subscription của khách hàng.
     */
    public Subscription getCustomerSubscriptionDetail(int subscriptionId, int userId) {
        if (subscriptionId <= 0 || userId <= 0) return null;
        return planDAO.getCustomerSubscriptionById(subscriptionId, userId);
    }

    /**
     * Nghiệp vụ tạm dừng gói:
     * - Chỉ áp dụng cho gói đang 'active'
     */
    public ServiceResult<Void> pauseSubscription(int subscriptionId, int userId) {
        if (subscriptionId <= 0 || userId <= 0) {
            return ServiceResult.fail("Yêu cầu không hợp lệ.");
        }

        Subscription sub = planDAO.getCustomerSubscriptionById(subscriptionId, userId);
        if (sub == null) {
            return ServiceResult.fail("Gói đăng ký không tồn tại hoặc không thuộc tài khoản của bạn.");
        }

        if (!Subscription.STATUS_ACTIVE.equalsIgnoreCase(sub.getStatus())) {
            return ServiceResult.fail("Chỉ có thể tạm dừng gói đang hoạt động.");
        }

        boolean ok = planDAO.updateSubscriptionStatus(subscriptionId, userId, Subscription.STATUS_PAUSED);
        if (!ok) {
            return ServiceResult.fail("Không thể tạm dừng gói đăng ký. Vui lòng thử lại sau.");
        }

        return ServiceResult.ok("Đã tạm dừng gói đăng ký thành công.", null);
    }

    /**
     * Nghiệp vụ kích hoạt lại gói (Resume):
     * - Chỉ áp dụng cho gói đang 'paused'
     * - Tự động cập nhật lại ngày giao tiếp theo nếu ngày cũ đã qua
     */
    public ServiceResult<Void> resumeSubscription(int subscriptionId, int userId) {
        if (subscriptionId <= 0 || userId <= 0) {
            return ServiceResult.fail("Yêu cầu không hợp lệ.");
        }

        Subscription sub = planDAO.getCustomerSubscriptionById(subscriptionId, userId);
        if (sub == null) {
            return ServiceResult.fail("Gói đăng ký không tồn tại hoặc không thuộc tài khoản của bạn.");
        }

        if (!Subscription.STATUS_PAUSED.equalsIgnoreCase(sub.getStatus())) {
            return ServiceResult.fail("Chỉ có thể kích hoạt lại gói đang bị tạm dừng.");
        }

        boolean ok;
        Date now = new Date();
        if (sub.getNext_delivery_date() == null || sub.getNext_delivery_date().before(now)) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            ok = planDAO.resumeSubscriptionWithNewDate(subscriptionId, userId, cal.getTime());
        } else {
            ok = planDAO.updateSubscriptionStatus(subscriptionId, userId, Subscription.STATUS_ACTIVE);
        }
        if (!ok) {
            return ServiceResult.fail("Không thể kích hoạt lại gói đăng ký. Vui lòng thử lại sau.");
        }

        return ServiceResult.ok("Đã kích hoạt lại gói đăng ký thành công. Lịch giao hàng sẽ tiếp tục bình thường.", null);
    }

    /**
     * Nghiệp vụ hủy gói đăng ký (Cancel):
     * - Không thể hủy nếu gói đã bị hủy trước đó
     */
    public ServiceResult<Void> cancelSubscription(int subscriptionId, int userId) {
        if (subscriptionId <= 0 || userId <= 0) {
            return ServiceResult.fail("Yêu cầu không hợp lệ.");
        }

        Subscription sub = planDAO.getCustomerSubscriptionById(subscriptionId, userId);
        if (sub == null) {
            return ServiceResult.fail("Gói đăng ký không tồn tại hoặc không thuộc tài khoản của bạn.");
        }

        if (Subscription.STATUS_CANCELLED.equalsIgnoreCase(sub.getStatus())) {
            return ServiceResult.fail("Gói đăng ký này đã được hủy trước đó.");
        }

        boolean ok = planDAO.updateSubscriptionStatus(subscriptionId, userId, Subscription.STATUS_CANCELLED);
        if (!ok) {
            return ServiceResult.fail("Không thể hủy gói đăng ký. Vui lòng thử lại sau.");
        }

        return ServiceResult.ok("Đã hủy gói đăng ký thành công. Bạn sẽ không nhận thêm đơn giao định kỳ từ kỳ tiếp theo.", null);
    }
}
