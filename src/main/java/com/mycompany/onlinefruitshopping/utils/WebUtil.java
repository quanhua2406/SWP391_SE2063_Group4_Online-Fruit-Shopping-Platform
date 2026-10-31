package com.mycompany.onlinefruitshopping.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;

public class WebUtil {
    public static User user(HttpServletRequest request) {
        return UserSession.current(request);
    }

    public static String home(User user) {
        return LoginService.dashboardPath(user.getRoleName());
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
