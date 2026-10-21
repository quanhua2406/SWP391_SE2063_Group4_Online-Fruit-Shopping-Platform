package com.mycompany.onlinefruitshopping.shopowner.service;

import com.mycompany.onlinefruitshopping.shopowner.dao.AccountDAO;
import com.mycompany.onlinefruitshopping.shopowner.model.Account;
import java.sql.SQLException;

/** Xử lý yêu cầu đăng nhập và kiểm tra tài khoản đang hoạt động. */
public class AccountService {
    private final AccountDAO accountDAO;

    public AccountService() {
        this(new AccountDAO());
    }

    AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public Account login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || username.length() > 50
                || password == null || password.isEmpty() || password.length() > 255) {
            throw new IllegalArgumentException("Vui lòng nhập tài khoản và mật khẩu hợp lệ.");
        }
        // Giữ nguyên mật khẩu; chỉ loại bỏ khoảng trắng ở hai đầu tên đăng nhập.
        return accountDAO.login(username.trim(), password);
    }

    public Account findActive(int accountId) throws SQLException {
        if (accountId <= 0) return null;
        return accountDAO.findActive(accountId);
    }
}
