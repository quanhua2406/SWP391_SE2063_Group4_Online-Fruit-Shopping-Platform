package com.mycompany.onlinefruitshopping.dao.commission;

import com.mycompany.onlinefruitshopping.model.CommissionSettlement;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommissionSettlementDAO {

    private static final String SELECT_SQL = """
            SELECT cs.settlement_id, cs.shop_id, s.shop_name, cs.period_month,
                   cs.amount, cs.issued_date, cs.due_date, cs.status,
                   cs.shop_locked, cs.transaction_ref, cs.paid_at
            FROM commission_settlements cs
            JOIN shops s ON s.shop_id = cs.shop_id
            """;

    public Integer findShopIdByOwner(int ownerId) throws SQLException {

        String sql = "SELECT TOP 1 shop_id FROM shops WHERE owner_id = ? ORDER BY shop_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public boolean insertIfAbsent(int shopId, String periodMonth, BigDecimal amount,
                                  LocalDate issuedDate, LocalDate dueDate) throws SQLException {

        String sql = """
                INSERT INTO commission_settlements
                    (shop_id, period_month, amount, issued_date, due_date)
                SELECT ?, ?, ?, ?, ?
                WHERE NOT EXISTS (SELECT 1 FROM commission_settlements
                                  WHERE shop_id = ? AND period_month = ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, shopId);
            ps.setString(2, periodMonth);
            ps.setBigDecimal(3, amount);
            ps.setDate(4, Date.valueOf(issuedDate));
            ps.setDate(5, Date.valueOf(dueDate));
            ps.setInt(6, shopId);
            ps.setString(7, periodMonth);

            return ps.executeUpdate() > 0;
        }
    }

    public List<CommissionSettlement> listByShop(int shopId) throws SQLException {

        return query(SELECT_SQL + " WHERE cs.shop_id = ? ORDER BY cs.period_month DESC", shopId);
    }

    public List<CommissionSettlement> listAll() throws SQLException {

        return query(SELECT_SQL + " ORDER BY cs.period_month DESC, s.shop_name ASC", null);
    }

    public CommissionSettlement findById(int settlementId) throws SQLException {

        List<CommissionSettlement> rows = query(SELECT_SQL + " WHERE cs.settlement_id = ?", settlementId);

        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Unpaid settlements whose due date is before the cutoff (already past the grace day). */
    public List<CommissionSettlement> findOverdueUnlocked(LocalDate dueBefore) throws SQLException {

        String sql = SELECT_SQL + " WHERE cs.status = 'unpaid' AND cs.shop_locked = 0 AND cs.due_date < ?";

        List<CommissionSettlement> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(dueBefore));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    public boolean markPaid(int settlementId, String transactionRef) throws SQLException {

        String sql = """
                UPDATE commission_settlements
                SET status = 'paid', transaction_ref = ?, paid_at = SYSDATETIME()
                WHERE settlement_id = ? AND status = 'unpaid'
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionRef);
            ps.setInt(2, settlementId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean markShopLocked(int settlementId) throws SQLException {

        String sql = "UPDATE commission_settlements SET shop_locked = 1 WHERE settlement_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, settlementId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Takes the shop off sale and hides its published products. Returns
     * false when the shop was not active.
     */
    public boolean suspendShop(int shopId) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE shops SET status = 'suspended' WHERE shop_id = ? AND status = 'active'")) {
                    ps.setInt(1, shopId);

                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET status = 'hidden', hidden_by_commission = 1 "
                                + "WHERE shop_id = ? AND status = 'published'")) {
                    ps.setInt(1, shopId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean hasUnpaidShopLock(int shopId) throws SQLException {

        String sql = "SELECT COUNT(*) FROM commission_settlements "
                + "WHERE shop_id = ? AND shop_locked = 1 AND status = 'unpaid'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, shopId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Puts a commission-suspended shop back on sale once nothing is left
     * unpaid. Shops whose owner account is locked (admin block) are kept.
     */
    public boolean reactivateShop(int shopId) throws SQLException {

        String reopen = """
                UPDATE s SET s.status = 'active'
                FROM shops s
                JOIN users u ON u.user_id = s.owner_id
                WHERE s.shop_id = ?
                  AND s.status = 'suspended'
                  AND u.status = 'active'
                  AND NOT EXISTS (SELECT 1 FROM commission_settlements cs
                                  WHERE cs.shop_id = s.shop_id
                                    AND cs.shop_locked = 1
                                    AND cs.status = 'unpaid')
                """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(reopen)) {
                    ps.setInt(1, shopId);

                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET status = 'published', hidden_by_commission = 0 "
                                + "WHERE shop_id = ? AND status = 'hidden' AND hidden_by_commission = 1")) {
                    ps.setInt(1, shopId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean hasUnpaidShopLockForOwner(int ownerId) throws SQLException {

        String sql = "SELECT COUNT(*) FROM commission_settlements cs "
                + "JOIN shops s ON s.shop_id = cs.shop_id "
                + "WHERE s.owner_id = ? AND cs.shop_locked = 1 AND cs.status = 'unpaid'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private List<CommissionSettlement> query(String sql, Integer param) throws SQLException {

        List<CommissionSettlement> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param != null) {
                ps.setInt(1, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    private CommissionSettlement map(ResultSet rs) throws SQLException {

        CommissionSettlement s = new CommissionSettlement();

        s.setSettlementId(rs.getInt("settlement_id"));
        s.setShopId(rs.getInt("shop_id"));
        s.setShopName(rs.getString("shop_name"));
        s.setPeriodMonth(rs.getString("period_month").trim());
        s.setAmount(rs.getBigDecimal("amount"));
        s.setIssuedDate(rs.getDate("issued_date").toLocalDate());
        s.setDueDate(rs.getDate("due_date").toLocalDate());
        s.setStatus(rs.getString("status"));
        s.setShopLocked(rs.getBoolean("shop_locked"));
        s.setTransactionRef(rs.getString("transaction_ref"));

        Timestamp paid = rs.getTimestamp("paid_at");
        s.setPaidAt(paid == null ? null : paid.toLocalDateTime());

        return s;
    }
}
