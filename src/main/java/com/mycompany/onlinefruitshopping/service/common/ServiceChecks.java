package com.mycompany.onlinefruitshopping.service.common;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;

/** Kiểm tra đầu vào dùng chung bên trong tầng Service. */
public final class ServiceChecks {
    private ServiceChecks() { }

    public static void positiveId(int id) {
        if (id <= 0) throw new IllegalArgumentException("Mã tài khoản hoặc mã dữ liệu không hợp lệ.");
    }

    public static void version(int version) {
        if (version < 0) throw new IllegalArgumentException("Phiên bản dữ liệu không hợp lệ.");
    }

    public static void requireRole(User account, String... roles) {
        if (account != null && account.getUserID() > 0) {
            for (String role : roles) {
                if (LoginService.roleMatches(account.getRoleName(), role)) return;
            }
        }
        throw new IllegalArgumentException("Bạn không có quyền thực hiện chức năng này.");
    }
}
