package com.mycompany.onlinefruitshopping.filter;

import com.mycompany.onlinefruitshopping.utils.ValidationUtil;
import java.util.Set;

final class AccessPolicy {

    private AccessPolicy() {
    }

    private static final Set<String> ALL_ROLES = Set.of(
            "ADMIN",
            "CUSTOMER",
            "SHOPOWNER",
            "DELIVERYSTAFF"
    );

    private static final Set<String> DELIVERY_READERS = Set.of(
            "CUSTOMER",
            "SHOPOWNER",
            "DELIVERYSTAFF"
    );

    private static final Set<String> PUBLIC_READ = Set.of(
            "/",
            "/index.html",
            "/index.jsp",
            "/home",
            "/home.jsp",
            "/products",
            "/catalog",
            "/product",
            "/product-detail",
            "/product-details.jsp",
            "/product-catalog.jsp",
            "/api/products/search-suggestions",
            "/subscriptions",
            "/subscription/plans",
            "/subscription/detail",
            "/ViewReviewServlet",
            "/view-review",
            "/reviews",
            "/review",
            "/login.jsp",
            "/register.jsp",
            "/forgot-password.jsp",
            "/verify-otp.jsp",
            "/reset-password.jsp",
            "/cart.jsp"
    );

    private static final Set<String> AUTH_ROUTES = Set.of(
            "/login",
            "/register",
            "/verify-register-otp",
            "/forgot-password",
            "/verify-otp",
            "/reset-password",
            "/logout",
            "/shopowner",
            "/shopowner/login"
    );

    private static final Set<String> CART_ROUTES = Set.of(
            "/cart",
            "/cart/add",
            "/cart/update",
            "/cart/remove",
            "/cart/clear"
    );

    static boolean isRead(String method) {
        return "GET".equals(method) || "HEAD".equals(method);
    }

    static boolean isInternalView(String path) {
        return path.equals("/jsp/shopowner/views")
                || path.startsWith("/jsp/shopowner/views/");
    }

    static boolean isStatic(String path, String method) {
        if (!isRead(method)) {
            return false;
        }

        if (path.startsWith("/uploads/reviews/")) {
            return path.matches(
                    "/uploads/reviews/[^/]+\\.(?i:png|jpe?g|gif|webp)"
            );
        }

        boolean directory = path.startsWith("/assets/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");

        return path.equals("/favicon.ico")
                || (directory && path.matches(
                        ".*\\.(?i:css|js|map|png|jpe?g|gif|webp|svg|ico|woff2?|ttf|eot)"
                ));
    }

    static boolean isPublic(String path, String method) {
        if (isInternalView(path)) {
            return false;
        }

        if (isStatic(path, method)) {
            return true;
        }

        if (AUTH_ROUTES.contains(path) || CART_ROUTES.contains(path)) {
            return isRead(method) || "POST".equals(method);
        }

        return isRead(method)
                && (PUBLIC_READ.contains(path)
                || path.startsWith("/jsp/auth/")
                || path.startsWith("/jsp/home/"));
    }

    static Set<String> allowedRoles(String path, String method) {
        if (isInternalView(path)) {
            return Set.of();
        }

        if (!isRead(method) && !"POST".equals(method)) {
            return Set.of();
        }

        // Các vai trò được đọc dữ liệu giao hàng.
        // DAO vẫn phải giới hạn dữ liệu theo người đăng nhập.
        if (path.equals("/shopowner/delivery/list")
                || path.equals("/shopowner/delivery/detail")) {

            return isRead(method) ? DELIVERY_READERS : Set.of();
        }

        // Chỉ nhân viên giao hàng được cập nhật trạng thái.
        if (path.equals("/shopowner/delivery/update")) {
            return "POST".equals(method)
                    ? Set.of("DELIVERYSTAFF")
                    : Set.of();
        }

        if (path.startsWith("/shopowner/shop/")) {
            return Set.of("SHOPOWNER");
        }

        if (path.startsWith("/shopowner/customer/")) {
            return Set.of("CUSTOMER");
        }

        if (path.equals("/shopowner/notifications")) {
            return DELIVERY_READERS;
        }

        if (path.equals("/shopowner/logout")) {
            return "POST".equals(method)
                    ? ALL_ROLES
                    : Set.of();
        }

        if (path.startsWith("/admin/")
                || path.startsWith("/jsp/admin/")) {
            return Set.of("ADMIN");
        }

        if (path.startsWith("/store-owner/")
                || path.startsWith("/jsp/shopowner/")) {
            return Set.of("SHOPOWNER");
        }

        if (path.startsWith("/customer/")
                || path.startsWith("/jsp/customer/")) {
            return Set.of("CUSTOMER");
        }

        if (path.startsWith("/delivery/")
                || path.startsWith("/jsp/delivery/")) {
            return Set.of("DELIVERYSTAFF");
        }

        return switch (path) {
            case "/profile" -> ALL_ROLES;

            case "/addresses",
                    "/my-subscriptions",
                    "/subscription/subscribe",
                    "/ReviewServlet",
                    "/write-review",
                    "/EditReviewServlet",
                    "/edit-review" ->
                    Set.of("CUSTOMER");

            case "/DeleteReviewServlet", "/delete-review" ->
                    "POST".equals(method)
                            ? Set.of("CUSTOMER")
                            : Set.of();

            case "/review/report" ->
                    "POST".equals(method)
                            ? Set.of("CUSTOMER", "SHOPOWNER")
                            : Set.of();

            case "/test-db" -> Set.of("ADMIN");

            default -> Set.of();
        };
    }

    static boolean permits(
            String path,
            String method,
            String role) {

        String normalizedRole =
                ValidationUtil.normalizeRoleName(role);

        return allowedRoles(path, method).contains(normalizedRole);
    }

    static boolean requiresCsrf(String path, String method) {
        return !isRead(method)
                && (path.equals("/logout")
                || (path.startsWith("/shopowner/")
                && !path.equals("/shopowner/login")));
    }
}