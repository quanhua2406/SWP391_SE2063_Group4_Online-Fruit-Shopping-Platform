package com.mycompany.onlinefruitshopping.controller.subscription;

import com.mycompany.onlinefruitshopping.model.SubscriptionPlan;
import com.mycompany.onlinefruitshopping.model.SubscriptionPlanItem;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.SubscriptionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller Servlet quản lý Subscription Plans cho Shop Owner.
 *
 * Toàn bộ logic nghiệp vụ (Validation, CRUD, quy tắc xóa, thay đổi trạng thái)
 * được ủy quyền xử lý tại SubscriptionService.
 * Servlet chỉ đóng vai trò tiếp nhận HTTP Request, xử lý Session, chuyển đổi tham số và điều hướng.
 *
 * URL Mappings:
 * GET  /store-owner/subscription/plans              → danh sách plans
 * GET  /store-owner/subscription/plans?action=create → form tạo mới
 * POST /store-owner/subscription/plans?action=create → lưu plan mới
 * GET  /store-owner/subscription/plans?action=edit&id=X → form sửa
 * POST /store-owner/subscription/plans?action=edit   → lưu thay đổi
 * POST /store-owner/subscription/plans?action=toggle → activate/deactivate
 * POST /store-owner/subscription/plans?action=delete → soft-delete
 */
@WebServlet("/store-owner/subscription/plans")
public class ShopOwnerPlanServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private final SubscriptionService subscriptionService = new SubscriptionService();

    // ─────────────────────────────────────────────────────────────────────
    // GET
    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User owner = getShopOwner(request, response);
        if (owner == null) return;

        int shopId = getShopId(request, owner.getUserID());
        if (shopId <= 0) {
            request.setAttribute("error", "Bạn chưa có shop. Vui lòng tạo shop trước.");
            request.getRequestDispatcher("/store-owner-dashboard.jsp").forward(request, response);
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "create":
                showCreateForm(request, response, shopId);
                break;
            case "edit":
                showEditForm(request, response, shopId);
                break;
            default:
                showList(request, response, shopId);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST
    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User owner = getShopOwner(request, response);
        if (owner == null) return;

        int shopId = getShopId(request, owner.getUserID());
        if (shopId <= 0) {
            response.sendRedirect(request.getContextPath() + "/store-owner/dashboard");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "create": handleCreate(request, response, shopId, owner.getUserID()); break;
            case "edit":   handleEdit(request, response, shopId);   break;
            case "toggle": handleToggle(request, response, shopId); break;
            case "delete": handleDelete(request, response, shopId); break;
            default:
                response.sendRedirect(request.getContextPath() + "/store-owner/subscription/plans");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Hiển thị danh sách plans
    // ─────────────────────────────────────────────────────────────────────
    private void showList(HttpServletRequest req, HttpServletResponse resp, int shopId)
            throws ServletException, IOException {

        String status  = req.getParameter("status");
        int    page    = parseSafe(req.getParameter("page"), 1);
        if (page < 1) page = 1;

        List<SubscriptionPlan> plans = subscriptionService.getPlansByShop(shopId, status, page, PAGE_SIZE);
        int total      = subscriptionService.countPlansByShop(shopId, status);
        int totalPages = subscriptionService.calculateTotalPages(total, PAGE_SIZE);

        req.setAttribute("plans",      plans);
        req.setAttribute("total",      total);
        req.setAttribute("page",       page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("status",     status);

        req.getRequestDispatcher("/subscription/planList.jsp").forward(req, resp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Hiển thị form tạo mới
    // ─────────────────────────────────────────────────────────────────────
    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp, int shopId)
            throws ServletException, IOException {
        List<SubscriptionPlanItem> variants = subscriptionService.getAvailableVariantsByShop(shopId);
        req.setAttribute("availableVariants", variants);
        req.setAttribute("mode",   "create");
        req.setAttribute("shopId", shopId);
        req.getRequestDispatcher("/subscription/planForm.jsp").forward(req, resp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Hiển thị form sửa
    // ─────────────────────────────────────────────────────────────────────
    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, int shopId)
            throws ServletException, IOException {
        int planId = parseSafe(req.getParameter("id"), -1);
        if (planId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
            return;
        }

        SubscriptionPlan plan = subscriptionService.getPlanDetails(planId, shopId);
        if (plan == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Gói đăng ký không tồn tại hoặc không thuộc quyền quản lý của bạn.");
            return;
        }

        List<SubscriptionPlanItem> variants = subscriptionService.getAvailableVariantsByShop(shopId);
        req.setAttribute("availableVariants", variants);
        req.setAttribute("plan",   plan);
        req.setAttribute("mode",   "edit");
        req.setAttribute("shopId", shopId);
        req.getRequestDispatcher("/subscription/planForm.jsp").forward(req, resp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Xử lý tạo plan mới
    // ─────────────────────────────────────────────────────────────────────
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp,
                              int shopId, int userId) throws IOException, ServletException {
        SubscriptionPlan plan = buildPlanFromRequest(req, shopId, userId);
        List<SubscriptionPlanItem> items = parseItemsFromRequest(req);

        SubscriptionService.ServiceResult<Integer> result = subscriptionService.createPlan(plan, items);
        if (!result.isSuccess()) {
            req.setAttribute("availableVariants", subscriptionService.getAvailableVariantsByShop(shopId));
            plan.setItems(items);
            req.setAttribute("plan",   plan);
            req.setAttribute("mode",   "create");
            req.setAttribute("errors", result.getErrors());
            req.getRequestDispatcher("/subscription/planForm.jsp").forward(req, resp);
            return;
        }

        req.getSession().setAttribute("toast_success", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Xử lý sửa plan
    // ─────────────────────────────────────────────────────────────────────
    private void handleEdit(HttpServletRequest req, HttpServletResponse resp, int shopId)
            throws IOException, ServletException {
        int planId = parseSafe(req.getParameter("planId"), -1);
        if (planId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
            return;
        }

        SubscriptionPlan updated = buildPlanFromRequest(req, shopId, 0);
        List<SubscriptionPlanItem> items = parseItemsFromRequest(req);

        SubscriptionService.ServiceResult<Void> result = subscriptionService.updatePlan(planId, shopId, updated, items);
        if (!result.isSuccess()) {
            req.setAttribute("availableVariants", subscriptionService.getAvailableVariantsByShop(shopId));
            updated.setPlanId(planId);
            updated.setItems(items);
            req.setAttribute("plan",   updated);
            req.setAttribute("mode",   "edit");
            req.setAttribute("errors", result.getErrors());
            req.getRequestDispatcher("/subscription/planForm.jsp").forward(req, resp);
            return;
        }

        req.getSession().setAttribute("toast_success", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Activate / Deactivate
    // ─────────────────────────────────────────────────────────────────────
    private void handleToggle(HttpServletRequest req, HttpServletResponse resp, int shopId)
            throws IOException {
        int planId = parseSafe(req.getParameter("id"), -1);
        if (planId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
            return;
        }

        SubscriptionService.ServiceResult<String> result = subscriptionService.togglePlanStatus(planId, shopId);
        req.getSession().setAttribute(result.isSuccess() ? "toast_success" : "toast_error", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Soft Delete
    // ─────────────────────────────────────────────────────────────────────
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, int shopId)
            throws IOException {
        int planId = parseSafe(req.getParameter("id"), -1);
        if (planId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
            return;
        }

        SubscriptionService.ServiceResult<Void> result = subscriptionService.deletePlan(planId, shopId);
        req.getSession().setAttribute(result.isSuccess() ? "toast_success" : "toast_error", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/store-owner/subscription/plans");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────

    /** Đọc params từ form và build SubscriptionPlan object */
    private SubscriptionPlan buildPlanFromRequest(HttpServletRequest req, int shopId, int userId) {
        SubscriptionPlan p = new SubscriptionPlan();
        p.setShopId(shopId);
        p.setCreatedBy(userId);
        p.setName(trim(req.getParameter("name")));
        p.setDescription(trim(req.getParameter("description")));
        String cycle = trim(req.getParameter("billingCycle"));
        p.setBillingCycle(cycle);
        int durationDays = parseSafe(req.getParameter("durationDays"), 0);
        if (durationDays <= 0 && cycle != null) {
            switch (cycle.toLowerCase()) {
                case "weekly": durationDays = 7; break;
                case "quarterly": durationDays = 90; break;
                case "yearly": durationDays = 365; break;
                default: durationDays = 30; break;
            }
        }
        p.setDurationDays(durationDays);
        p.setBenefits(trim(req.getParameter("benefits")));
        p.setStatus(trim(req.getParameter("status")));

        String priceStr = trim(req.getParameter("price"));
        try {
            if (priceStr != null) p.setPrice(new BigDecimal(priceStr));
        } catch (NumberFormatException ignored) {}

        return p;
    }

    /** Parse danh sách sản phẩm từ form (multi-value params) */
    private List<SubscriptionPlanItem> parseItemsFromRequest(HttpServletRequest req) {
        String[] variantIds = req.getParameterValues("variantId");
        String[] quantities = req.getParameterValues("quantity");
        List<SubscriptionPlanItem> items = new ArrayList<>();
        if (variantIds == null) return items;
        for (int i = 0; i < variantIds.length; i++) {
            try {
                if (variantIds[i] == null || variantIds[i].trim().isEmpty()) continue;
                int vid = Integer.parseInt(variantIds[i].trim());
                int qty = (quantities != null && i < quantities.length)
                        ? parseSafe(quantities[i], 1) : 1;
                if (vid > 0 && qty > 0) items.add(new SubscriptionPlanItem(0, vid, qty));
            } catch (NumberFormatException ignored) {}
        }
        return items;
    }

    /** Kiểm tra quyền Shop Owner, trả về User hoặc null nếu không hợp lệ */
    private User getShopOwner(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        String role = user.getRoleName() != null ? user.getRoleName().trim().toUpperCase() : "";
        boolean isOwner = role.contains("SHOP") || role.contains("OWNER") || user.getRoleID() == 2;
        if (!isOwner) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Shop Owner mới có quyền truy cập.");
            return null;
        }
        return user;
    }

    /** Lấy shop_id từ session hoặc qua service theo owner_id */
    private int getShopId(HttpServletRequest req, int ownerId) {
        HttpSession session = req.getSession(false);
        Object cached = session != null ? session.getAttribute("shopId") : null;
        if (cached instanceof Integer) return (Integer) cached;

        int shopId = subscriptionService.getShopIdByOwner(ownerId);
        if (shopId > 0 && session != null) {
            session.setAttribute("shopId", shopId);
        }
        return shopId;
    }

    private String trim(String s) { return s != null ? s.trim() : null; }

    private int parseSafe(String s, int def) {
        try { return s != null ? Integer.parseInt(s.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }
}
