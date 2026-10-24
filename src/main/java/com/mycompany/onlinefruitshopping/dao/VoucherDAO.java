package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.Voucher;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    public VoucherDAO() {
        ensureVouchersTableAndSeeds();
    }

    public Voucher getVoucherByCode(String code) throws SQLException {
        if (code == null || code.isBlank()) return null;

        String sql = "SELECT voucher_id, code, discount_type, discount_value, start_date, end_date, usage_limit, status "
                + "FROM vouchers WHERE UPPER(code) = UPPER(?) AND status = 'active'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Voucher v = new Voucher();
                    v.setVoucherId(rs.getInt("voucher_id"));
                    v.setCode(rs.getString("code"));
                    v.setDiscountType(rs.getString("discount_type"));
                    v.setDiscountValue(rs.getDouble("discount_value"));
                    v.setStartDate(rs.getTimestamp("start_date"));
                    v.setEndDate(rs.getTimestamp("end_date"));
                    int limit = rs.getInt("usage_limit");
                    if (!rs.wasNull()) {
                        v.setUsageLimit(limit);
                    }
                    v.setStatus(rs.getString("status"));
                    return v;
                }
            }
        }
        return null;
    }

    public List<Voucher> getActiveVouchers() throws SQLException {
        List<Voucher> list = new ArrayList<>();
        String sql = "SELECT voucher_id, code, discount_type, discount_value, start_date, end_date, usage_limit, status "
                + "FROM vouchers WHERE status = 'active' AND (end_date IS NULL OR end_date >= GETDATE())";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Voucher v = new Voucher();
                v.setVoucherId(rs.getInt("voucher_id"));
                v.setCode(rs.getString("code"));
                v.setDiscountType(rs.getString("discount_type"));
                v.setDiscountValue(rs.getDouble("discount_value"));
                v.setStartDate(rs.getTimestamp("start_date"));
                v.setEndDate(rs.getTimestamp("end_date"));
                int limit = rs.getInt("usage_limit");
                if (!rs.wasNull()) {
                    v.setUsageLimit(limit);
                }
                v.setStatus(rs.getString("status"));
                list.add(v);
            }
        }
        return list;
    }

    public void decrementUsageLimit(int voucherId) throws SQLException {
        String sql = "UPDATE vouchers SET usage_limit = usage_limit - 1 WHERE voucher_id = ? AND usage_limit > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            ps.executeUpdate();
        }
    }

    private void ensureVouchersTableAndSeeds() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            // Tạo bảng nếu chưa tồn tại
            String createTableSql = """
                IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'vouchers')
                BEGIN
                    CREATE TABLE vouchers (
                        voucher_id      INT IDENTITY(1,1) PRIMARY KEY,
                        code            NVARCHAR(50) NOT NULL UNIQUE,
                        discount_type   NVARCHAR(20) NOT NULL,
                        discount_value  DECIMAL(12,2) NOT NULL,
                        start_date      DATETIME2 NOT NULL DEFAULT GETDATE(),
                        end_date        DATETIME2 NOT NULL DEFAULT DATEADD(year, 1, GETDATE()),
                        usage_limit     INT DEFAULT 100,
                        status          NVARCHAR(20) NOT NULL DEFAULT 'active'
                    );
                END
                """;
            st.execute(createTableSql);

            // Kiểm tra và seed các mã khuyến mãi phổ biến
            String checkCount = "SELECT COUNT(*) FROM vouchers";
            try (ResultSet rs = st.executeQuery(checkCount)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String seedSql = """
                        INSERT INTO vouchers (code, discount_type, discount_value, start_date, end_date, usage_limit, status)
                        VALUES 
                        ('FREESHIP', 'fixed_amount', 25000, GETDATE(), DATEADD(month, 3, GETDATE()), 500, 'active'),
                        ('FRUIT10', 'percent', 10, GETDATE(), DATEADD(month, 3, GETDATE()), 200, 'active'),
                        ('GIAM50K', 'fixed_amount', 50000, GETDATE(), DATEADD(month, 3, GETDATE()), 100, 'active'),
                        ('WELCOME', 'percent', 15, GETDATE(), DATEADD(month, 6, GETDATE()), 300, 'active');
                        """;
                    st.execute(seedSql);
                }
            }

        } catch (SQLException e) {
            // Log nhưng không làm gián đoạn ứng dụng
            System.err.println("Warning: Unable to ensure vouchers table: " + e.getMessage());
        }
    }
}
