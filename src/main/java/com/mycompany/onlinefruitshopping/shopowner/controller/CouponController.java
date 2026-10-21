package com.mycompany.onlinefruitshopping.shopowner.controller;

import com.mycompany.onlinefruitshopping.shopowner.service.CouponService;
import com.mycompany.onlinefruitshopping.shopowner.service.PromotionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import com.mycompany.onlinefruitshopping.shopowner.model.Coupon;
import com.mycompany.onlinefruitshopping.shopowner.model.ShopOption;
import com.mycompany.onlinefruitshopping.shopowner.utils.WebUtil;

@WebServlet("/shopowner/shop/coupons")
public class CouponController extends HttpServlet {
    private final CouponService couponService = new CouponService();
    private final PromotionService promotionService = new PromotionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int ownerId = WebUtil.user(request).getId();
        String action = request.getParameter("action");
        try {
            if (action == null || action.equals("list")) {
                request.setAttribute("coupons", couponService.findAll(ownerId));
                view(request, response, "coupons.jsp");
            } else if (action.equals("history")) {
                request.setAttribute("usages", couponService.findUsage(ownerId, null));
                view(request, response, "coupon-history.jsp");
            } else if (action.equals("create")) {
                Coupon p = couponService.createDraft();
                form(request, response, p);
            } else if (action.equals("edit") || action.equals("detail")) {
                Coupon p = couponService.findById(ownerId, number(request.getParameter("id"), false));
                if (p == null) { response.sendError(404); return; }
                if (action.equals("edit")) form(request, response, p);
                else {
                    request.setAttribute("coupon", p);
                    request.setAttribute("usages", couponService.findUsage(ownerId, p.getId()));
                    view(request, response, "coupon-detail.jsp");
                }
            } else response.sendError(405);
        } catch (IllegalArgumentException e) { response.sendError(400); }
        catch (SQLException e) {
            log("Không tải được coupon. Kiểm tra database/05_Them_coupon.sql", e);
            response.sendError(503);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("save".equals(action)) { save(request, response); return; }
        if (!"activate".equals(action) && !"deactivate".equals(action) && !"delete".equals(action)) {
            response.sendError(400); return;
        }
        try {
            int id = number(request.getParameter("id"), false);
            int version = number(request.getParameter("version"), true);
            couponService.changeState(WebUtil.user(request).getId(), id, version,
                    "activate".equals(action), "delete".equals(action));
            WebUtil.flash(request, "success", "delete".equals(action) ? "Đã xóa mã khỏi danh sách. Lịch sử sử dụng vẫn được giữ lại."
                    : "activate".equals(action) ? "Đã bật mã giảm giá." : "Đã tắt mã giảm giá.");
        } catch (IllegalArgumentException e) { WebUtil.flash(request, "error", e.getMessage()); }
        catch (SQLException e) {
            log("Không đổi được trạng thái coupon", e);
            WebUtil.flash(request, "error", "Chưa lưu được. Hãy tải lại trang và thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/shopowner/shop/coupons");
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int ownerId = WebUtil.user(request).getId();

        try {
            int id, version;
            try {
                id = number(request.getParameter("id"), true);
                version = number(request.getParameter("version"), true);
            } catch (IllegalArgumentException e) { response.sendError(400); return; }
            Coupon p = id == 0 ? new Coupon() : couponService.findById(ownerId, id);
            if (p == null) { response.sendError(404); return; }
            p.setVersion(version);
            request.setAttribute("submitted", true);
            try {
                readForm(request, p);
                int savedId = couponService.save(ownerId, p);
                WebUtil.flash(request, "success", id == 0 ? "Đã tạo mã giảm giá." : "Đã cập nhật mã giảm giá.");
                response.sendRedirect(request.getContextPath() + "/shopowner/shop/coupons?action=detail&id=" + savedId);
            } catch (IllegalArgumentException e) {
                response.setStatus(400); request.setAttribute("formError", e.getMessage());
                form(request, response, p);
            }
        } catch (SQLException e) { log("Không lưu được coupon", e); response.sendError(503); }
    }

    private void readForm(HttpServletRequest request, Coupon p) {
        p.setName(text(request, "name").trim()); p.setDescription(text(request, "description").trim());
        p.setCode(text(request, "code")); p.setDiscountType(text(request, "discountType"));
        p.setActive("on".equals(request.getParameter("active")));
        int shopId = number(request.getParameter("shopId"), false);
        couponService.validateShopChange(p, shopId);
        p.setShopId(shopId);
        p.setDiscountValue(decimal(request, "discountValue", true));
        p.setMinOrderAmount(decimal(request, "minOrderAmount", false));
        p.setMaxDiscount(text(request, "maxDiscount").isBlank() ? null : decimal(request, "maxDiscount", false));
        p.setTotalLimit(number(request.getParameter("totalLimit"), false));
        p.setPerCustomerLimit(number(request.getParameter("perCustomerLimit"), false));
        try {
            p.setStartsAt(LocalDateTime.parse(text(request, "startsAt")));
            p.setEndsAt(LocalDateTime.parse(text(request, "endsAt")));
            if (p.getStartsAt().getSecond() != 0 || p.getEndsAt().getSecond() != 0
                    || p.getStartsAt().getNano() != 0 || p.getEndsAt().getNano() != 0)
                throw new IllegalArgumentException("Thời gian chỉ nhập đến phút.");
        } catch (DateTimeParseException e) { throw new IllegalArgumentException("Nhập đủ ngày và giờ bắt đầu/kết thúc."); }
    }

    private BigDecimal decimal(HttpServletRequest request, String field, boolean fraction) {
        String value = text(request, field).trim();
        if (!value.matches(fraction ? "[0-9]{1,16}(\\.[0-9]{1,2})?" : "[0-9]{1,18}"))
            throw new IllegalArgumentException("Số tiền/mức giảm không hợp lệ. Nhập số không có dấu phân tách hàng nghìn.");
        return new BigDecimal(value);
    }

    private int number(String value, boolean allowZero) {
        try { int number = Integer.parseInt(value); if (number > 0 || (allowZero && number == 0)) return number; }
        catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("ID, phiên bản hoặc số lượt không hợp lệ.");
    }

    private String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name); return value == null ? "" : value;
    }

    private void form(HttpServletRequest request, HttpServletResponse response, Coupon p)
            throws SQLException, ServletException, IOException {
        List<ShopOption> shops = promotionService.findShops(WebUtil.user(request).getId());
        if (p.getId() == 0 && p.getShopId() == 0 && !shops.isEmpty()) p.setShopId(shops.get(0).getId());
        request.setAttribute("shops", shops); request.setAttribute("coupon", p);
        view(request, response, "coupon-form.jsp");
    }

    private void view(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/shopowner/views/" + jsp).forward(request, response);
    }
}
