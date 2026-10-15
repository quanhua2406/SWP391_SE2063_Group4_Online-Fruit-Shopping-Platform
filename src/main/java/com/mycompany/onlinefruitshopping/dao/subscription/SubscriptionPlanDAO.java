package com.mycompany.onlinefruitshopping.dao.subscription;

import com.mycompany.onlinefruitshopping.model.Subscription;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlan;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlanItem;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý toàn bộ thao tác DB cho subscription_plans và subscription_plan_items.
 */
public class SubscriptionPlanDAO {

    private static final Logger LOG = Logger.getLogger(SubscriptionPlanDAO.class.getName());

    // ═══════════════════════════════════════════════════════════════════
    // QUERY — Danh sách plans
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách plans của 1 shop (Shop Owner quản lý).
     * @param shopId   ID của shop
     * @param status   null = tất cả, hoặc "draft"/"active"/"inactive"
     * @param page     trang hiện tại (bắt đầu từ 1)
     * @param pageSize số plan mỗi trang
     */
    public List<SubscriptionPlan> getPlansByShop(int shopId, String status, int page, int pageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, ");
        sb.append("       sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, ");
        sb.append("       sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, ");
        sb.append("       u.full_name AS created_by_name, ");
        sb.append("       COUNT(sub.subscription_id) AS subscriber_count ");
        sb.append("FROM subscription_plans sp ");
        sb.append("LEFT JOIN users u ON u.user_id = sp.created_by ");
        sb.append("LEFT JOIN subscriptions sub ON sub.plan_id = sp.plan_id AND sub.status = 'active' ");
        sb.append("WHERE sp.shop_id = ? AND sp.deleted_at IS NULL ");
        if (status != null && !status.isEmpty()) {
            sb.append("AND sp.status = ? ");
        }
        sb.append("GROUP BY sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, ");
        sb.append("         sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, ");
        sb.append("         sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, u.full_name ");
        sb.append("ORDER BY sp.created_at DESC ");
        sb.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<SubscriptionPlan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            ps.setInt(idx++, shopId);
            if (status != null && !status.isEmpty()) ps.setString(idx++, status);
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getPlansByShop error", e);
        }
        return list;
    }

    /** Đếm tổng plan của shop (dùng phân trang) */
    public int countPlansByShop(int shopId, String status) {
        String sql = "SELECT COUNT(*) FROM subscription_plans "
                   + "WHERE shop_id = ? AND deleted_at IS NULL"
                   + (status != null && !status.isEmpty() ? " AND status = ?" : "");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            if (status != null && !status.isEmpty()) ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "countPlansByShop error", e);
            return 0;
        }
    }

    /**
     * Admin xem tất cả plans của mọi shop.
     */
    public List<SubscriptionPlan> getAllPlans(String status, int page, int pageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, ");
        sb.append("       sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, ");
        sb.append("       sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, ");
        sb.append("       sh.shop_name, u.full_name AS created_by_name, ");
        sb.append("       COUNT(sub.subscription_id) AS subscriber_count ");
        sb.append("FROM subscription_plans sp ");
        sb.append("LEFT JOIN shops sh ON sh.shop_id = sp.shop_id ");
        sb.append("LEFT JOIN users u  ON u.user_id  = sp.created_by ");
        sb.append("LEFT JOIN subscriptions sub ON sub.plan_id = sp.plan_id AND sub.status = 'active' ");
        sb.append("WHERE sp.deleted_at IS NULL ");
        if (status != null && !status.isEmpty()) sb.append("AND sp.status = ? ");
        sb.append("GROUP BY sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, ");
        sb.append("         sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, ");
        sb.append("         sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, ");
        sb.append("         sh.shop_name, u.full_name ");
        sb.append("ORDER BY sp.created_at DESC ");
        sb.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<SubscriptionPlan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (status != null && !status.isEmpty()) ps.setString(idx++, status);
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getAllPlans error", e);
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════════════════
    // QUERY — Chi tiết plan
    // ═══════════════════════════════════════════════════════════════════

    /** Lấy 1 plan theo ID (kèm items) */
    public SubscriptionPlan getPlanById(int planId) {
        String sql = "SELECT sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, "
                   + "       sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, "
                   + "       sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, "
                   + "       sh.shop_name, u.full_name AS created_by_name, "
                   + "       COUNT(sub.subscription_id) AS subscriber_count "
                   + "FROM subscription_plans sp "
                   + "LEFT JOIN shops sh ON sh.shop_id = sp.shop_id "
                   + "LEFT JOIN users u  ON u.user_id  = sp.created_by "
                   + "LEFT JOIN subscriptions sub ON sub.plan_id = sp.plan_id AND sub.status = 'active' "
                   + "WHERE sp.plan_id = ? AND sp.deleted_at IS NULL "
                   + "GROUP BY sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, "
                   + "         sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, "
                   + "         sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, "
                   + "         sh.shop_name, u.full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SubscriptionPlan plan = mapRow(rs);
                    plan.setItems(getItemsByPlanId(planId));
                    return plan;
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getPlanById error", e);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CREATE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tạo plan mới, trả về plan_id mới tạo hoặc -1 nếu lỗi.
     */
    public int createPlan(SubscriptionPlan plan) {
        String sql = "INSERT INTO subscription_plans "
                   + "(shop_id, name, description, price, billing_cycle, duration_days, benefits, status, created_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, plan.getShopId());
            ps.setString(2, plan.getName().trim());
            ps.setString(3, plan.getDescription());
            ps.setBigDecimal(4, plan.getPrice());
            ps.setString(5, plan.getBillingCycle());
            ps.setInt(6, plan.getDurationDays());
            ps.setString(7, plan.getBenefits());
            ps.setString(8, plan.getStatus() != null ? plan.getStatus() : SubscriptionPlan.STATUS_DRAFT);
            ps.setInt(9, plan.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "createPlan error", e);
        }
        return -1;
    }

    // ═══════════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════════

    /** Cập nhật thông tin plan (không ảnh hưởng subscriber cũ) */
    public boolean updatePlan(SubscriptionPlan plan) {
        String sql = "UPDATE subscription_plans "
                   + "SET name = ?, description = ?, price = ?, billing_cycle = ?, "
                   + "    duration_days = ?, benefits = ?, updated_at = GETDATE() "
                   + "WHERE plan_id = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getName().trim());
            ps.setString(2, plan.getDescription());
            ps.setBigDecimal(3, plan.getPrice());
            ps.setString(4, plan.getBillingCycle());
            ps.setInt(5, plan.getDurationDays());
            ps.setString(6, plan.getBenefits());
            ps.setInt(7, plan.getPlanId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "updatePlan error", e);
            return false;
        }
    }

    /** Cập nhật status (draft/active/inactive) */
    public boolean updateStatus(int planId, String newStatus) {
        String sql = "UPDATE subscription_plans SET status = ?, updated_at = GETDATE() "
                   + "WHERE plan_id = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, planId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "updateStatus error", e);
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // DELETE (Soft)
    // ═══════════════════════════════════════════════════════════════════

    /** Soft delete plan — chỉ được xóa nếu không có subscriber active */
    public boolean softDeletePlan(int planId) {
        if (countActiveSubscribers(planId) > 0) return false;
        String sql = "UPDATE subscription_plans SET deleted_at = GETDATE(), status = 'inactive' "
                   + "WHERE plan_id = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "softDeletePlan error", e);
            return false;
        }
    }

    /** Đếm số subscriber đang active của plan */
    public int countActiveSubscribers(int planId) {
        String sql = "SELECT COUNT(*) FROM subscriptions WHERE plan_id = ? AND status = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "countActiveSubscribers error", e);
            return 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // PLAN ITEMS (sản phẩm trong combo)
    // ═══════════════════════════════════════════════════════════════════

    /** Lấy danh sách items của plan */
    public List<SubscriptionPlanItem> getItemsByPlanId(int planId) {
        String sql = "SELECT spi.item_id, spi.plan_id, spi.variant_id, spi.quantity, "
                   + "       p.product_name, pv.sku AS variant_name, "
                   + "       pv.price AS unit_price "
                   + "FROM subscription_plan_items spi "
                   + "JOIN product_variants pv ON pv.variant_id = spi.variant_id "
                   + "JOIN products p ON p.product_id = pv.product_id "
                   + "WHERE spi.plan_id = ?";
        List<SubscriptionPlanItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SubscriptionPlanItem item = new SubscriptionPlanItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setPlanId(rs.getInt("plan_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setProductName(rs.getString("product_name"));
                    item.setVariantName(rs.getString("variant_name"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getItemsByPlanId error", e);
        }
        return list;
    }

    /** Xóa toàn bộ items của plan (dùng khi update items) */
    public boolean deleteItemsByPlanId(int planId) {
        String sql = "DELETE FROM subscription_plan_items WHERE plan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "deleteItemsByPlanId error", e);
            return false;
        }
    }

    /** Thêm danh sách items vào plan (batch insert) */
    public boolean insertItems(int planId, List<SubscriptionPlanItem> items) {
        if (items == null || items.isEmpty()) return true;
        String sql = "INSERT INTO subscription_plan_items (plan_id, variant_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SubscriptionPlanItem item : items) {
                ps.setInt(1, planId);
                ps.setInt(2, item.getVariantId());
                ps.setInt(3, item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "insertItems error", e);
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // PRODUCT VARIANTS (Cho dropdown chọn sản phẩm combo)
    // ═══════════════════════════════════════════════════════════════════

    /** Lấy danh sách các biến thể sản phẩm đang active/published của shop để chọn vào combo */
    public List<SubscriptionPlanItem> getAvailableVariantsByShop(int shopId) {
        String sql = "SELECT pv.variant_id, p.product_name, pv.sku AS variant_name, pv.price AS unit_price "
                   + "FROM product_variants pv "
                   + "JOIN products p ON p.product_id = pv.product_id "
                   + "WHERE p.shop_id = ? AND (p.status IS NULL OR p.status IN ('published', 'active')) "
                   + "ORDER BY p.product_name ASC, pv.sku ASC";
        List<SubscriptionPlanItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SubscriptionPlanItem item = new SubscriptionPlanItem();
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setVariantName(rs.getString("variant_name"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getAvailableVariantsByShop error", e);
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CUSTOMER — Danh sách plans công khai (status = 'active')
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Khách hàng xem danh sách gói đăng ký combo đang active trên hệ thống.
     * Hỗ trợ tìm kiếm theo tên/mô tả, lọc theo shop, chu kỳ thanh toán, phân trang.
     */
    public List<SubscriptionPlan> getActivePlansForCustomer(Integer shopId, String billingCycle, String keyword, int page, int pageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, ");
        sb.append("       sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, ");
        sb.append("       sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, ");
        sb.append("       sh.shop_name, u.full_name AS created_by_name, ");
        sb.append("       COUNT(sub.subscription_id) AS subscriber_count ");
        sb.append("FROM subscription_plans sp ");
        sb.append("JOIN shops sh ON sh.shop_id = sp.shop_id AND sh.status = 'active' ");
        sb.append("LEFT JOIN users u ON u.user_id = sp.created_by ");
        sb.append("LEFT JOIN subscriptions sub ON sub.plan_id = sp.plan_id AND sub.status = 'active' ");
        sb.append("WHERE sp.status = 'active' AND sp.deleted_at IS NULL ");

        List<Object> params = new ArrayList<>();
        if (shopId != null && shopId > 0) {
            sb.append("AND sp.shop_id = ? ");
            params.add(shopId);
        }
        if (billingCycle != null && !billingCycle.trim().isEmpty() && !"all".equalsIgnoreCase(billingCycle)) {
            sb.append("AND sp.billing_cycle = ? ");
            params.add(billingCycle.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (sp.name LIKE ? OR sp.description LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        sb.append("GROUP BY sp.plan_id, sp.shop_id, sp.name, sp.description, sp.price, ");
        sb.append("         sp.billing_cycle, sp.duration_days, sp.benefits, sp.status, ");
        sb.append("         sp.created_by, sp.created_at, sp.updated_at, sp.deleted_at, ");
        sb.append("         sh.shop_name, u.full_name ");
        sb.append("ORDER BY sp.created_at DESC ");
        sb.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        int offset = Math.max(0, (page - 1) * pageSize);
        params.add(offset);
        params.add(pageSize);

        List<SubscriptionPlan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SubscriptionPlan plan = mapRow(rs);
                    plan.setItems(getItemsByPlanId(plan.getPlanId()));
                    list.add(plan);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getActivePlansForCustomer error", e);
        }
        return list;
    }

    /** Đếm tổng số plan active phù hợp với bộ lọc cho khách */
    public int countActivePlansForCustomer(Integer shopId, String billingCycle, String keyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) FROM subscription_plans sp ");
        sb.append("JOIN shops sh ON sh.shop_id = sp.shop_id AND sh.status = 'active' ");
        sb.append("WHERE sp.status = 'active' AND sp.deleted_at IS NULL ");

        List<Object> params = new ArrayList<>();
        if (shopId != null && shopId > 0) {
            sb.append("AND sp.shop_id = ? ");
            params.add(shopId);
        }
        if (billingCycle != null && !billingCycle.trim().isEmpty() && !"all".equalsIgnoreCase(billingCycle)) {
            sb.append("AND sp.billing_cycle = ? ");
            params.add(billingCycle.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (sp.name LIKE ? OR sp.description LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "countActivePlansForCustomer error", e);
            return 0;
        }
    }

    /**
     * Lấy danh sách shop đang active để làm bộ lọc cho khách hàng
     */
    public List<java.util.Map<String, Object>> getActiveShopsForFilter() {
        String sql = "SELECT shop_id, shop_name FROM shops WHERE status = 'active' ORDER BY shop_name ASC";
        List<java.util.Map<String, Object>> shops = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("shopId", rs.getInt("shop_id"));
                map.put("shopName", rs.getString("shop_name"));
                shops.add(map);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getActiveShopsForFilter error", e);
        }
        return shops;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CUSTOMER SUBSCRIPTIONS (Thao tác với bảng subscriptions)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tạo bản ghi đăng ký gói cho khách hàng.
     */
    public int createSubscription(Subscription sub) {
        String sql = "INSERT INTO subscriptions "
                   + "(user_id, variant_id, frequency, next_delivery_date, status, created_at, plan_id, is_custom, price_snapshot, end_date) "
                   + "VALUES (?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sub.getUser_id());
            ps.setInt(2, sub.getVariant_id());
            ps.setString(3, sub.getFrequency());
            ps.setDate(4, new java.sql.Date(sub.getNext_delivery_date().getTime()));
            ps.setString(5, sub.getStatus() != null ? sub.getStatus() : Subscription.STATUS_ACTIVE);
            if (sub.getPlan_id() != null) {
                ps.setInt(6, sub.getPlan_id());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setBoolean(7, sub.isIs_custom());
            ps.setBigDecimal(8, sub.getPrice_snapshot());
            if (sub.getEnd_date() != null) {
                ps.setDate(9, new java.sql.Date(sub.getEnd_date().getTime()));
            } else {
                ps.setNull(9, Types.DATE);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "createSubscription error", e);
        }
        return -1;
    }

    /**
     * Lấy danh sách các gói subscription của khách hàng theo trạng thái (hoặc tất cả).
     */
    public List<Subscription> getSubscriptionsByCustomer(int userId, String status) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT sub.subscription_id, sub.user_id, sub.variant_id, sub.frequency, ");
        sb.append("       sub.next_delivery_date, sub.status, sub.created_at, sub.plan_id, ");
        sb.append("       sub.is_custom, sub.price_snapshot, sub.end_date, ");
        sb.append("       sp.name AS plan_name, sp.description AS plan_description, ");
        sb.append("       sp.duration_days, sp.benefits, sh.shop_name, sh.shop_id, ");
        sb.append("       pv.sku AS variant_name, p.product_name ");
        sb.append("FROM subscriptions sub ");
        sb.append("LEFT JOIN subscription_plans sp ON sp.plan_id = sub.plan_id ");
        sb.append("LEFT JOIN shops sh ON sh.shop_id = sp.shop_id ");
        sb.append("LEFT JOIN product_variants pv ON pv.variant_id = sub.variant_id ");
        sb.append("LEFT JOIN products p ON p.product_id = pv.product_id ");
        sb.append("WHERE sub.user_id = ? ");
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            sb.append("AND sub.status = ? ");
        }
        sb.append("ORDER BY sub.created_at DESC");

        List<Subscription> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setInt(1, userId);
            if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
                ps.setString(2, status.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subscription s = mapSubscriptionRow(rs);
                    if (s.getPlan_id() != null && s.getPlan_id() > 0) {
                        s.setItems(getItemsByPlanId(s.getPlan_id()));
                    }
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getSubscriptionsByCustomer error", e);
        }
        return list;
    }

    /**
     * Lấy chi tiết 1 subscription của khách hàng (bảo mật kiểm tra user_id).
     */
    public Subscription getCustomerSubscriptionById(int subscriptionId, int userId) {
        String sql = "SELECT sub.subscription_id, sub.user_id, sub.variant_id, sub.frequency, "
                   + "       sub.next_delivery_date, sub.status, sub.created_at, sub.plan_id, "
                   + "       sub.is_custom, sub.price_snapshot, sub.end_date, "
                   + "       sp.name AS plan_name, sp.description AS plan_description, "
                   + "       sp.duration_days, sp.benefits, sh.shop_name, sh.shop_id, "
                   + "       pv.sku AS variant_name, p.product_name "
                   + "FROM subscriptions sub "
                   + "LEFT JOIN subscription_plans sp ON sp.plan_id = sub.plan_id "
                   + "LEFT JOIN shops sh ON sh.shop_id = sp.shop_id "
                   + "LEFT JOIN product_variants pv ON pv.variant_id = sub.variant_id "
                   + "LEFT JOIN products p ON p.product_id = pv.product_id "
                   + "WHERE sub.subscription_id = ? AND sub.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Subscription s = mapSubscriptionRow(rs);
                    if (s.getPlan_id() != null && s.getPlan_id() > 0) {
                        s.setItems(getItemsByPlanId(s.getPlan_id()));
                    }
                    return s;
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getCustomerSubscriptionById error", e);
        }
        return null;
    }

    /**
     * Cập nhật trạng thái subscription của khách (Pause, Resume, Cancel).
     */
    public boolean updateSubscriptionStatus(int subscriptionId, int userId, String newStatus) {
        String sql = "UPDATE subscriptions SET status = ? WHERE subscription_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, subscriptionId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "updateSubscriptionStatus error", e);
            return false;
        }
    }

    /**
     * Kích hoạt lại gói và cập nhật ngày giao hàng mới (nếu ngày cũ trong quá khứ).
     */
    public boolean resumeSubscriptionWithNewDate(int subscriptionId, int userId, java.util.Date newDate) {
        String sql = "UPDATE subscriptions SET status = 'active', next_delivery_date = ? WHERE subscription_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(newDate.getTime()));
            ps.setInt(2, subscriptionId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "resumeSubscriptionWithNewDate error", e);
            return false;
        }
    }


    /**
     * Kiểm tra khách hàng đã có gói active của planId này hay chưa.
     */
    public int countActiveSubscriptionsOfUser(int userId, int planId) {
        String sql = "SELECT COUNT(*) FROM subscriptions WHERE user_id = ? AND plan_id = ? AND status = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "countActiveSubscriptionsOfUser error", e);
            return 0;
        }
    }

    /** Helper: map ResultSet → Subscription */
    private Subscription mapSubscriptionRow(ResultSet rs) throws SQLException {
        Subscription s = new Subscription();
        s.setSubscription_id(rs.getInt("subscription_id"));
        s.setUser_id(rs.getInt("user_id"));
        s.setVariant_id(rs.getInt("variant_id"));
        s.setFrequency(rs.getString("frequency"));
        s.setNext_delivery_date(rs.getDate("next_delivery_date"));
        s.setStatus(rs.getString("status"));
        s.setCreated_at(rs.getTimestamp("created_at"));
        int planId = rs.getInt("plan_id");
        if (!rs.wasNull()) s.setPlan_id(planId);
        s.setIs_custom(rs.getBoolean("is_custom"));
        s.setPrice_snapshot(rs.getBigDecimal("price_snapshot"));
        s.setEnd_date(rs.getDate("end_date"));

        // Join fields
        try { s.setPlanName(rs.getString("plan_name")); } catch (SQLException ignored) {}
        try { s.setPlanDescription(rs.getString("plan_description")); } catch (SQLException ignored) {}
        try { s.setShopName(rs.getString("shop_name")); } catch (SQLException ignored) {}
        try { s.setShopId(rs.getInt("shop_id")); } catch (SQLException ignored) {}
        try { s.setDurationDays(rs.getInt("duration_days")); } catch (SQLException ignored) {}
        try { s.setBenefits(rs.getString("benefits")); } catch (SQLException ignored) {}
        try { s.setVariantName(rs.getString("variant_name")); } catch (SQLException ignored) {}
        try { s.setProductName(rs.getString("product_name")); } catch (SQLException ignored) {}
        return s;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER: map ResultSet → SubscriptionPlan
    // ═══════════════════════════════════════════════════════════════════
    private SubscriptionPlan mapRow(ResultSet rs) throws SQLException {
        SubscriptionPlan p = new SubscriptionPlan();
        p.setPlanId(rs.getInt("plan_id"));
        p.setShopId(rs.getInt("shop_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setBillingCycle(rs.getString("billing_cycle"));
        p.setDurationDays(rs.getInt("duration_days"));
        p.setBenefits(rs.getString("benefits"));
        p.setStatus(rs.getString("status"));
        p.setCreatedBy(rs.getInt("created_by"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        p.setDeletedAt(rs.getTimestamp("deleted_at"));
        // Trường phụ (có thể null nếu không JOIN)
        try { p.setShopName(rs.getString("shop_name")); } catch (SQLException ignored) {}
        try { p.setCreatedByName(rs.getString("created_by_name")); } catch (SQLException ignored) {}
        try { p.setSubscriberCount(rs.getInt("subscriber_count")); } catch (SQLException ignored) {}
        return p;
    }
}
