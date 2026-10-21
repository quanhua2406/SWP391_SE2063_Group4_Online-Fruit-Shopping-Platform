package com.mycompany.onlinefruitshopping.shopowner.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;

public class WebUtil {
    public static Account user(HttpServletRequest request) {
        return (Account) request.getSession().getAttribute("shopUser");
    }

    public static String home(Account user) {
        if ("ShopOwner".equals(user.getRole())) return "/shopowner/shop/deliveries";
        if ("DeliveryStaff".equals(user.getRole())) return "/shopowner/delivery/list";
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
        if (session.getAttribute("shopcsrf") == null) {
            session.setAttribute("shopcsrf", UUID.randomUUID().toString());
        }
    }

    public static void flash(HttpServletRequest request, String kind, String text) {
        request.getSession().setAttribute(kind, text);
    }
}
