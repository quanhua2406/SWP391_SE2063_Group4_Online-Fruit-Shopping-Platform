package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Small shared helpers for the shop-owner controllers.
 */
final class ShopOwnerSupport {

    private ShopOwnerSupport() {
    }

    /**
     * Returns the logged-in user, or sends the guest to the login
     * page and returns {@code null}.
     */
    static User currentUser(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("user") instanceof User) {

            User user = (User) session.getAttribute("user");
            if (!LoginService.roleMatches(user.getRoleName(), "SHOPOWNER")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
            return user;
        }

        response.sendRedirect(
                request.getContextPath() + "/login"
        );

        return null;
    }
}
