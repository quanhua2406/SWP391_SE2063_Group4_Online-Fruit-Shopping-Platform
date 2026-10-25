package com.mycompany.onlinefruitshopping.utils;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

public class WebUtil {
    public static User user(HttpServletRequest request) {
        User u = UserSession.current(request);
        if (u != null) {
            return u;
        }
        Object legacy = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("shopUser");
        return legacy instanceof User user ? user : null;
    }

    public static String home(User user) {
        if (user == null || user.getRoleName() == null) {
            return "/";
        }
        String p = LoginService.dashboardPath(user.getRoleName());
        if ("/shop".equals(p)) {
            return "/shopowner/shop/deliveries";
        }
        if ("/delivery".equals(p)) {
            return "/shopowner/delivery/list";
        }
        if ("/customer".equals(p)) {
            return "/shopowner/notifications";
        }
        if ("ShopOwner".equals(user.getRoleName())) {
            return "/shopowner/shop/deliveries";
        }
        if ("DeliveryStaff".equals(user.getRoleName()) || "Shipper".equals(user.getRoleName())) {
            return "/shopowner/delivery/list";
        }
        return "/shopowner/notifications";
    }

    public static int positiveId(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) {
            // Cùng trả một thông báo dễ hiểu khi thiếu ID hoặc ID không phải số.
        }
        throw new IllegalArgumentException("Mã đơn hoặc mã nhân viên không hợp lệ.");
    }

    public static void createCsrfToken(HttpSession session) {
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
    }

    public static void flash(HttpServletRequest request, String kind, String text) {
        request.getSession().setAttribute(kind, text);
    }
}
