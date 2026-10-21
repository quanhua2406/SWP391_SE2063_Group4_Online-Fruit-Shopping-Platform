package com.mycompany.onlinefruitshopping.shopowner.service;

import com.mycompany.onlinefruitshopping.shopowner.model.Account;

/** Kiểm tra đầu vào dùng chung bên trong tầng Service. */
final class ServiceChecks {
    private ServiceChecks() { }

    static void positiveId(int id) {
        if (id <= 0) throw new IllegalArgumentException("Mã tài khoản hoặc mã dữ liệu không hợp lệ.");
    }

    static void version(int version) {
        if (version < 0) throw new IllegalArgumentException("Phiên bản dữ liệu không hợp lệ.");
    }

    static void requireRole(Account account, String... roles) {
        if (account != null && account.getId() > 0) {
            for (String role : roles) {
                if (role.equals(account.getRole())) return;
            }
        }
        throw new IllegalArgumentException("Bạn không có quyền thực hiện chức năng này.");
    }
}
