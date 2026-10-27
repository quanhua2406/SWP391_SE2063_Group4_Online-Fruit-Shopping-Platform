package com.mycompany.onlinefruitshopping.filter;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import com.mycompany.onlinefruitshopping.utils.UserSession;
import com.mycompany.onlinefruitshopping.utils.WebUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

/** One authentication/authorization boundary for all servlet modules. */
@WebFilter(urlPatterns = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class AuthorizationFilter implements Filter {
    private final LoginService loginService;

    public AuthorizationFilter() {
        this(new LoginService());
    }

    AuthorizationFilter(LoginService loginService) {
        this.loginService = Objects.requireNonNull(loginService);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        // Use the container's servlet path, which also handles URL-based session IDs.
        String path = req.getServletPath();
        String method = req.getMethod();
        if (!AccessPolicy.isModulePath(path)) {
            legacyFilter(req, res, chain);
            return;
        }
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options", "nosniff");
        if (AccessPolicy.isInternalView(path)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (AccessPolicy.isStatic(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        boolean publicResource = AccessPolicy.isPublic(path, method);
        Set<String> roles = AccessPolicy.allowedRoles(path, method);
        if (!publicResource && roles.isEmpty()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = UserSession.current(session);
        if (user != null) {
            try {
                if (!loginService.isSessionValid(user)) {
                    session.invalidate();
                    requireLogin(req, res, false);
                    return;
                }
            } catch (SQLException e) {
                req.getServletContext().log("Không kiểm tra được tài khoản", e);
                res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
        }

        String role = user == null ? "" : LoginService.normalizeRole(user.getRoleName());
        if (!publicResource) {
            if (user == null) {
                requireLogin(req, res, true);
                return;
            }
            if (!roles.contains(role)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        req.setAttribute("currentRole", role);
        if (user != null || !publicResource) res.setHeader("Cache-Control", "no-store");

        // Make a token available to GET-rendered forms, including the shared logout.
        session = req.getSession(true);
        WebUtil.createCsrfToken(session);
        if (AccessPolicy.requiresCsrf(path, method)) {
            String supplied = req.getHeader("X-CSRF-Token");
            if (supplied == null) supplied = req.getParameter("csrfToken");
            Object expected = session.getAttribute("csrfToken");
            if (!(expected instanceof String token) || !token.equals(supplied)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void requireLogin(HttpServletRequest req, HttpServletResponse res, boolean remember)
            throws IOException {
        if (!AccessPolicy.isRead(req.getMethod())) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (remember) {
            String target = req.getContextPath() + req.getServletPath();
            if (req.getPathInfo() != null) target += req.getPathInfo();
            if (req.getQueryString() != null) target += "?" + req.getQueryString();
            req.getSession(true).setAttribute("redirectAfterLogin", target);
        }
        res.sendRedirect(req.getContextPath() + "/login");
    }
    private void legacyFilter(ServletRequest request,
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
        // 1. PUBLIC RESOURCE
        // =========================================

        if (isPublicResource(path)) {

            chain.doFilter(
                    request,
                    response
            );

            return;
        }

        // =========================================
        // 2. REQUIRED ROLE (null = open resource)
        // =========================================

        String requiredRole = requiredRole(path);

        if (requiredRole == null) {

            // Not public and not role protected (e.g. static
            // or callback endpoints) -> let it through.
            chain.doFilter(
                    request,
                    response
            );

            return;
        }

        // =========================================
        // 3. GET CURRENT USER
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

        // Guest cannot access protected resources
        if (user == null) {

            // Remember where the user wanted to go
            // (used after a successful login)
            session = httpRequest.getSession(true);
            session.setAttribute(
                    "redirectAfterLogin",
                    requestURI
            );

            httpResponse.sendRedirect(
                    contextPath + "/login"
            );

            return;
        }

        // =========================================
        // 4. CHECK AUTHORIZATION
        // =========================================

        if (!LoginService.roleMatches(
                user.getRoleName(),
                requiredRole)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied"
            );

            return;
        }

        // =========================================
        // 5. CONTINUE
        // =========================================

        chain.doFilter(
                request,
                response
        );
    }


    private boolean isPublicResource(String path) {

        if (path == null || path.isBlank()) {
            return true;
        }

        return path.equals("/")
                || path.equals("/index.html")
                || path.equals("/index.jsp")

                // Home (storefront)
                || path.equals("/home")
                || path.equals("/home.jsp")
                || path.startsWith("/jsp/home/")

                // Direct JSP pages kept by the storefront
                || path.equals("/login.jsp")
                || path.equals("/register.jsp")
                || path.equals("/forgot-password.jsp")
                || path.equals("/verify-otp.jsp")
                || path.equals("/reset-password.jsp")
                || path.equals("/product-details.jsp")
                || path.equals("/product-catalog.jsp")

                // Guest cart (browsing only; actions
                // check the session themselves)
                || path.equals("/cart")
                || path.equals("/cart.jsp")
                || path.startsWith("/cart/")

                // Authentication
                || path.equals("/login")
                || path.equals("/register")
                || path.equals("/logout")

                // Registration Email Verification
                || path.equals("/verify-register-otp")

                // Forgot Password
                || path.equals("/forgot-password")
                || path.equals("/verify-otp")
                || path.equals("/reset-password")

                // Auth view pages (views only render, data
                // comes from the matching servlet)
                || path.startsWith("/jsp/auth/")

                // Static resources
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/assets/")
                || path.startsWith("/favicon");
    }

    // =================================================
    // REQUIRED ROLE FOR A PATH
    // =================================================

    /**
     * @return the role required to access the path, or
     *         {@code null} when the path is not role protected.
     */
    private String requiredRole(String path) {

        if (path.startsWith("/admin/")
                || path.startsWith("/jsp/admin/")) {

            return "ADMIN";
        }

        if (path.startsWith("/customer/")
                || path.startsWith("/jsp/customer/")) {

            return "CUSTOMER";
        }

        if (path.startsWith("/store-owner/")
                || path.startsWith("/jsp/shopowner/")) {

            return "SHOPOWNER";
        }

        if (path.startsWith("/delivery/")
                || path.startsWith("/jsp/delivery/")) {

            return "DELIVERYSTAFF";
        }

        return null;
    }
}
