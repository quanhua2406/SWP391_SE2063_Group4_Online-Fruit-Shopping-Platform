package com.mycompany.onlinefruitshopping.filter;

import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        String contextPath =
                httpRequest.getContextPath();

        String requestURI =
                httpRequest.getRequestURI();

        // Remove context path
        String path =
                requestURI.substring(
                        contextPath.length()
                );

        // =========================================
        // 1. Allow public resources
        // =========================================

        if (isPublicResource(path)) {

            chain.doFilter(
                    request,
                    response
            );

            return;
        }

        // =========================================
        // 2. Get session
        // =========================================

        HttpSession session =
                httpRequest.getSession(false);

        User user = null;

        if (session != null) {

            Object userObject =
                    session.getAttribute("user");

            if (userObject instanceof User) {

                user = (User) userObject;
            }
        }

        // =========================================
        // 3. Protected URL
        // =========================================

        if (isProtectedResource(path)) {

            // Not logged in
            if (user == null) {

                httpResponse.sendRedirect(
                        contextPath + "/login"
                );

                return;
            }

            // =========================================
            // 4. Get role
            // =========================================

            String role =
                    user.getRoleName();

            if (role == null || role.isBlank()) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access Denied"
                );

                return;
            }

            role = role.toUpperCase();

            // =========================================
            // 5. Check authorization
            // =========================================

            if (!isAuthorized(path, role)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access Denied"
                );

                return;
            }
        }

        // =========================================
        // 6. Continue
        // =========================================

        chain.doFilter(
                request,
                response
        );
    }

    // =================================================
    // PUBLIC RESOURCE
    // =================================================

    private boolean isPublicResource(String path) {

        if (path == null || path.isBlank()) {
            return true;
        }

        return path.equals("/")
                || path.equals("/index.jsp")
                || path.equals("/login")
                || path.equals("/login.jsp")
                || path.equals("/register")
                || path.equals("/register.jsp")
                || path.equals("/forgot-password")
                || path.equals("/forgot-password.jsp")
                || path.equals("/verify-otp")
                || path.equals("/verify-otp.jsp")
                || path.equals("/reset-password")
                || path.equals("/reset-password.jsp")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/assets/")
                || path.startsWith("/favicon");
    }

    // =================================================
    // PROTECTED RESOURCE
    // =================================================

    private boolean isProtectedResource(String path) {

        return path.startsWith("/customer/")
                || path.startsWith("/store-owner/")
                || path.startsWith("/delivery/")
                || path.startsWith("/admin/");
    }

    // =================================================
    // AUTHORIZATION
    // =================================================

    private boolean isAuthorized(String path,
                                 String role) {

        // CUSTOMER
        if (path.startsWith("/customer/")) {

            return role.equals("CUSTOMER");
        }

        // SHOP OWNER
        if (path.startsWith("/store-owner/")) {

            return role.equals("SHOP_OWNER");
        }

        // DELIVERY STAFF
        if (path.startsWith("/delivery/")) {

            return role.equals("DELIVERY_STAFF");
        }

        // ADMIN
        if (path.startsWith("/admin/")) {

            return role.equals("ADMIN");
        }

        return false;
    }
}