package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.service.promotion.PromotionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.onlinefruitshopping.model.Promotion;
import com.mycompany.onlinefruitshopping.model.ShopOption;
import com.mycompany.onlinefruitshopping.utils.WebUtil;

/** GET để xem; POST để thay đổi dữ liệu. AuthFilter bảo vệ toàn bộ /shop/*. */
@WebServlet("/shopowner/shop/promotions")
public class PromotionController extends HttpServlet {
    private final PromotionService promotionService = new PromotionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int ownerId = WebUtil.user(request).getUserID();

        String action = request.getParameter("action");
        try {
            if (action == null || action.equals("list")) {
                request.setAttribute("promotions", promotionService.findAll(ownerId));
                request.getRequestDispatcher("/jsp/shopowner/views/promotions.jsp").forward(request, response);
            } else if (action.equals("create")) {
                Promotion p = promotionService.createDraft();
                showForm(request, response, p);
            } else if (action.equals("edit") || action.equals("detail")) {
                int id = number(request.getParameter("id"), false);
                Promotion p = promotionService.findById(ownerId, id);
                if (p == null) { response.sendError(404); return; }
                if (action.equals("edit")) {
                    showForm(request, response, p);
                } else {
                    request.setAttribute("promotion", p);
                    request.setAttribute("prices", promotionService.previewPrices(ownerId, id));
                    request.getRequestDispatcher("/jsp/shopowner/views/promotion-detail.jsp").forward(request, response);
                }
            } else {
                // Các action save/activate/deactivate/delete không được chạy bằng GET.
                response.sendError(405);
            }
        } catch (IllegalArgumentException e) {
            response.sendError(400);
        } catch (SQLException e) {
            log("Không tải được khuyến mãi. Kiểm tra database/03_Them_khuyen_mai.sql", e);
            response.sendError(503);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("save".equals(action)) {
            saveForm(request, response);
            return;
        }
        if (!"activate".equals(action) && !"deactivate".equals(action) && !"delete".equals(action)) {
            response.sendError(400);
            return;
        }
        try {
            int id = number(request.getParameter("id"), false);
            int version = number(request.getParameter("version"), true);
            int ownerId = WebUtil.user(request).getUserID();

            if ("delete".equals(action)) {
                promotionService.delete(ownerId, id, version);
                WebUtil.flash(request, "success", "Đã xóa chương trình khỏi danh sách.");
            } else {
                boolean active = "activate".equals(action);
                promotionService.setActive(ownerId, id, version, active);
                WebUtil.flash(request, "success", active
                        ? "Đã bật chương trình. Mức giảm chỉ có hiệu lực trong thời gian đã thiết lập."
                        : "Đã tắt chương trình khuyến mãi.");
            }
        } catch (IllegalArgumentException e) {
            WebUtil.flash(request, "error", e.getMessage());
        } catch (SQLException e) {
            log("Không đổi được trạng thái khuyến mãi", e);
            WebUtil.flash(request, "error", "Chưa lưu được. Hãy tải lại trang và thử lại; nếu còn lỗi, xem log Tomcat.");
        }
        response.sendRedirect(request.getContextPath() + "/shopowner/shop/promotions");
    }

    private void saveForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int ownerId = WebUtil.user(request).getUserID();
        Promotion p = new Promotion();
        try {
            // ID/version sai là form không hợp lệ, không tự biến thành thao tác tạo mới.
            int id;
            int version;
            try {
                id = number(request.getParameter("id"), true);
                version = number(request.getParameter("version"), true);
            } catch (IllegalArgumentException e) { response.sendError(400); return; }
            if (id > 0) {
                p = promotionService.findById(ownerId, id);
                if (p == null) { response.sendError(404); return; }
            }
            p.setId(id);
            p.setVersion(version);
            request.setAttribute("submitted", true);
            try {
                readForm(request, p);
                int savedId = promotionService.save(ownerId, p);
                WebUtil.flash(request, "success", id == 0 ? "Đã tạo chương trình khuyến mãi." : "Đã cập nhật chương trình khuyến mãi.");
                response.sendRedirect(request.getContextPath() + "/shopowner/shop/promotions?action=detail&id=" + savedId);
            } catch (IllegalArgumentException e) {
                response.setStatus(400);
                request.setAttribute("formError", e.getMessage());
                showForm(request, response, p);
            }
        } catch (SQLException e) {
            log("Không lưu được khuyến mãi", e);
            response.sendError(503);
        }
    }

    private void readForm(HttpServletRequest request, Promotion p) {
        p.setName(text(request, "name").trim());
        p.setDescription(text(request, "description").trim());
        p.setDiscountType(text(request, "discountType"));
        p.setActive("on".equals(request.getParameter("active")));
        List<Integer> ids = new ArrayList<>();
        p.setProductIds(ids);
        String[] selected = request.getParameterValues("productId");
        if (selected != null) {
            for (String value : selected) ids.add(number(value, false));
        }
        int shopId = number(request.getParameter("shopId"), false);
        promotionService.validateShopChange(p, shopId);
        p.setShopId(shopId);
        try {
            String value = text(request, "discountValue").trim();
            if (!value.matches("[0-9]{1,16}(\\.[0-9]{1,2})?")) throw new NumberFormatException();
            p.setDiscountValue(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nhập mức giảm bằng số; ví dụ 10 hoặc 20000, tối đa 2 số lẻ.");
        }
        try {
            p.setStartsAt(LocalDateTime.parse(text(request, "startsAt")));
            p.setEndsAt(LocalDateTime.parse(text(request, "endsAt")));
            if (p.getStartsAt().getSecond() != 0 || p.getEndsAt().getSecond() != 0
                    || p.getStartsAt().getNano() != 0 || p.getEndsAt().getNano() != 0)
                throw new IllegalArgumentException("Thời gian chỉ nhập đến phút, không nhập giây.");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Hãy nhập đủ ngày và giờ bắt đầu, kết thúc.");
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Promotion p)
            throws SQLException, ServletException, IOException {

        List<ShopOption> shops = promotionService.findShops(WebUtil.user(request).getUserID());
        if (p.getId() == 0 && p.getShopId() == 0 && !shops.isEmpty()) p.setShopId(shops.get(0).getId());
        request.setAttribute("promotion", p);
        request.setAttribute("shops", shops);
        request.setAttribute("products", promotionService.findProducts(WebUtil.user(request).getUserID()));
        request.getRequestDispatcher("/jsp/shopowner/views/promotion-form.jsp").forward(request, response);
    }

    private String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value;
    }

    private int number(String value, boolean allowZero) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0 || (allowZero && id == 0)) return id;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("Mã chương trình, cửa hàng, sản phẩm hoặc phiên bản không hợp lệ.");
    }
}
