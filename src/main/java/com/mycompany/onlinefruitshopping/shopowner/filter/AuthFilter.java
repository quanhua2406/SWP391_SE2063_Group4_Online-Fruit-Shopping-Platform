package com.mycompany.onlinefruitshopping.shopowner.filter;

import com.mycompany.onlinefruitshopping.shopowner.service.AccountService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;
import com.mycompany.onlinefruitshopping.shopowner.utils.WebUtil;

/** Filter chạy trước Servlet: UTF-8, đăng nhập, phân quyền và kiểm tra form. */
@WebFilter("/shopowner/*")
public class AuthFilter implements Filter {
    private final AccountService accountService = new AccountService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options", "nosniff");
        String path = req.getServletPath();
        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }
        res.setHeader("Cache-Control", "no-store");
        boolean publicPage = path.equals("/shopowner/login") || path.equals("/index.jsp")
                || path.equals("/") || path.isEmpty();
        HttpSession session = req.getSession();
        if (!publicPage) {
            Account user = (Account) session.getAttribute("shopUser");
            if (user == null) {
                res.sendRedirect(req.getContextPath() + "/shopowner/login");
                return;
            }
            try {
                user = accountService.findActive(user.getId());
            } catch (SQLException e) {
                req.getServletContext().log("Không kiểm tra được tài khoản", e);
                res.sendError(503);
                return;
            }
            if (user == null) {
                session.invalidate();
                res.sendRedirect(req.getContextPath() + "/shopowner/login");
                return;
            }
            session.setAttribute("shopUser", user);
            boolean wrongShopRole = path.startsWith("/shopowner/shop/") && !"ShopOwner".equals(user.getRole());
            boolean wrongDeliveryRole = path.startsWith("/shopowner/delivery/")
                    && !"ShopOwner".equals(user.getRole()) && !"DeliveryStaff".equals(user.getRole());
            boolean wrongCustomerRole = path.startsWith("/shopowner/customer/") && !"Customer".equals(user.getRole());
            if (wrongShopRole || wrongDeliveryRole || wrongCustomerRole) {
                res.sendError(403);
                return;
            }
        }
        WebUtil.createCsrfToken(session);
        // Form POST phải có mã thuộc đúng phiên đăng nhập này.
        if ("POST".equals(req.getMethod()) && !session.getAttribute("shopcsrf").equals(req.getParameter("shopcsrf"))) {
            res.sendError(403);
            return;
        }
        chain.doFilter(request, response);
    }
}
