package com.mycompany.onlinefruitshopping.utils;

import com.mycompany.onlinefruitshopping.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class UserSession {

    private UserSession() {
    }

    public static User current(HttpServletRequest request) {
        return current(request.getSession(false));
    }

    public static User current(HttpSession session) {
        Object value = session == null
                ? null
                : session.getAttribute("user");

        return value instanceof User user ? user : null;
    }

    public static void signIn(HttpSession session, User user) {
        if (user == null || user.getUserID() <= 0) {
            throw new IllegalArgumentException(
                    "Người dùng đăng nhập không hợp lệ.");
        }

        session.removeAttribute("shopUser");
        session.removeAttribute("userId");
        session.removeAttribute("roleName");
        session.removeAttribute("shopId");
        session.removeAttribute("shopcsrf");
        session.removeAttribute("csrfToken");

        session.setAttribute("user", user);
        WebUtil.createCsrfToken(session);
    }

    public static void refresh(HttpSession session, User profile) {
        User current = current(session);

        if (current == null
                || profile == null
                || current.getUserID() != profile.getUserID()) {
            throw new IllegalArgumentException(
                    "Phiên đăng nhập không hợp lệ.");
        }

        profile.setRoleID(current.getRoleID());
        profile.setRoleName(current.getRoleName());

        session.setAttribute("user", profile);
    }
}
