package com.mycompany.onlinefruitshopping.filter;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import com.mycompany.onlinefruitshopping.utils.UserSession;
import com.mycompany.onlinefruitshopping.utils.WebUtil;
import jakarta.servlet.DispatcherType;
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
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

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
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options", "nosniff");

        String path = req.getServletPath();
        String method = req.getMethod();

        // JSP nội bộ phải được mở thông qua controller.
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

        String role = user == null
                ? "" : LoginService.normalizeRole(user.getRoleName());
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
        if (user != null || !publicResource) {
            res.setHeader("Cache-Control", "no-store");
        }

        session = req.getSession(true);
        WebUtil.createCsrfToken(session);
        if (AccessPolicy.requiresCsrf(path, method)) {
            String supplied = req.getHeader("X-CSRF-Token");
            if (supplied == null) {
                supplied = req.getParameter("csrfToken");
            }
            Object expected = session.getAttribute("csrfToken");
            if (!(expected instanceof String token) || !token.equals(supplied)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void requireLogin(HttpServletRequest req, HttpServletResponse res,
                              boolean remember) throws IOException {
        if (!AccessPolicy.isRead(req.getMethod())) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (remember) {
            String target = req.getContextPath() + req.getServletPath();
            if (req.getPathInfo() != null) {
                target += req.getPathInfo();
            }
            if (req.getQueryString() != null) {
                target += "?" + req.getQueryString();
            }
            req.getSession(true).setAttribute("redirectAfterLogin", target);
        }
        res.sendRedirect(req.getContextPath() + "/login");
    }
}
