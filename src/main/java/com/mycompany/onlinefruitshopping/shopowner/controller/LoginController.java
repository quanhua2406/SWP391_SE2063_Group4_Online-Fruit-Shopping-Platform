package com.mycompany.onlinefruitshopping.shopowner.controller;

import com.mycompany.onlinefruitshopping.shopowner.service.AccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;
import com.mycompany.onlinefruitshopping.shopowner.utils.WebUtil;

@WebServlet(urlPatterns = {"/shopowner", "/shopowner/login"})
public class LoginController extends HttpServlet {
    private final AccountService accountService = new AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account user = (Account) request.getSession().getAttribute("shopUser");
        if (user != null) {
            response.sendRedirect(request.getContextPath() + WebUtil.home(user));
            return;
        }
        WebUtil.createCsrfToken(request.getSession());
        request.getRequestDispatcher("/WEB-INF/shopowner/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
            Account user = accountService.login(username, password);
            if (user != null) {
                // Tạo session mới sau đăng nhập; chỉ lưu thông tin cần thiết, không lưu mật khẩu.
                request.getSession().invalidate();
                HttpSession session = request.getSession(true);
                session.setAttribute("shopUser", user);
                WebUtil.createCsrfToken(session);
                response.sendRedirect(request.getContextPath() + WebUtil.home(user));
                return;
            }
            request.setAttribute("error", "Sai tài khoản, mật khẩu hoặc tài khoản không được phép đăng nhập.");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        } catch (SQLException e) {
            log("Lỗi đăng nhập/kết nối SQL Server", e);
            request.setAttribute("error", "Không truy cập được database. Hãy kiểm tra kết nối SQL Server và file utils/DBConnection.java rồi xem log Tomcat.");
        }
        request.getRequestDispatcher("/WEB-INF/shopowner/views/login.jsp").forward(request, response);
    }
}
