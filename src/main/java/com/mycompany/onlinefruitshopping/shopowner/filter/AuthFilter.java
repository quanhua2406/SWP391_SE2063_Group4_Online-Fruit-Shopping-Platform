package com.mycompany.onlinefruitshopping.shopowner.filter;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;
import com.mycompany.onlinefruitshopping.shopowner.service.AccountService;
import com.mycompany.onlinefruitshopping.shopowner.utils.WebUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebFilter("/shopowner/*")
public class AuthFilter implements Filter {
    private final AccountService accountService = new AccountService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Cache-Control", "no-store");

        String path = req.getServletPath();

        // LoginController ở bước 3 sẽ chuyển về trang đăng nhập chung.
        if (path.equals("/shopowner")
                || path.equals("/shopowner/login")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object principal = session == null
                ? null : session.getAttribute("user");

        // Chưa đăng nhập bằng hệ thống chính.
        if (!(principal instanceof User currentUser)) {
            session = req.getSession(true);
            session.removeAttribute("shopUser");

            if ("GET".equals(req.getMethod())) {
                String target = req.getContextPath() + path;
                String query = req.getQueryString();

                if (query != null) {
                    target += "?" + query;
                }

                session.setAttribute("redirectAfterLogin", target);
            }

            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = switch (
                LoginService.normalizeRole(currentUser.getRoleName())) {
            case "SHOPOWNER" -> "ShopOwner";
            case "DELIVERYSTAFF" -> "DeliveryStaff";
            case "CUSTOMER" -> "Customer";
            default -> "";
        };

        if (role.isEmpty()) {
            res.sendError(403);
            return;
        }

        // Lấy tài khoản theo ID của người đang đăng nhập.
        Account account;
        try {
            account = accountService.findActive(currentUser.getUserID());
        } catch (SQLException e) {
            req.getServletContext().log(
                    "Không kiểm tra được tài khoản", e);
            res.sendError(503);
            return;
        }

        // Tài khoản bị khóa hoặc đã thay đổi quyền.
        if (account == null
                || !LoginService.roleMatches(account.getRole(), role)) {
            session.invalidate();
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        account.setRole(role);
        session.setAttribute("shopUser", account);

        boolean denied =
                (path.startsWith("/shopowner/shop/")
                    && !role.equals("ShopOwner"))
                || (path.startsWith("/shopowner/delivery/")
                    && !role.equals("ShopOwner")
                    && !role.equals("DeliveryStaff"))
                || (path.startsWith("/shopowner/customer/")
                    && !role.equals("Customer"));

        if (denied) {
            res.sendError(403);
            return;
        }

        // Giữ kiểm tra CSRF cho các form POST.
        WebUtil.createCsrfToken(session);

        if ("POST".equals(req.getMethod())
                && !session.getAttribute("shopcsrf")
                        .equals(req.getParameter("shopcsrf"))) {
            res.sendError(403);
            return;
        }

        chain.doFilter(request, response);
    }
}
