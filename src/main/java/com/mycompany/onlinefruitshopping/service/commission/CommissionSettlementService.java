package com.mycompany.onlinefruitshopping.service.commission;

import com.mycompany.onlinefruitshopping.dao.commission.CommissionSettlementDAO;
import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.model.CommissionSettlement;
import com.mycompany.onlinefruitshopping.service.report.CommissionReportService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Monthly commission settlement: each shop pays the platform the
 * commission of the previous month. The due date is 10 days after the
 * settlement is issued; one day after that the shop is notified, and
 * one more day after that the shop is suspended until it pays.
 */
public class CommissionSettlementService {

    public static final int PAYMENT_DAYS = 10;

    private final CommissionSettlementDAO settlementDAO = new CommissionSettlementDAO();
    private final CommissionReportService reportService = new CommissionReportService();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /** Creates the settlement of {@code month} for every shop with a commission. Safe to repeat. */
    public int issueSettlements(YearMonth month, LocalDate today) throws SQLException {

        String from = month.atDay(1).toString();
        String to = month.atEndOfMonth().toString();
        int issued = 0;

        for (int ownerId : inventoryDAO.getActiveShopOwnerIds()) {

            CommissionReportService.OwnerReport report =
                    reportService.loadOwnerReport(ownerId, from, to, "", "overview");

            if (report == null) {
                continue;
            }

            BigDecimal fee = report.getTotalFee().setScale(0, RoundingMode.HALF_UP);

            if (fee.signum() <= 0) {
                continue;
            }

            Integer shopId = settlementDAO.findShopIdByOwner(ownerId);

            if (shopId == null) {
                continue;
            }

            if (settlementDAO.insertIfAbsent(shopId, month.toString(), fee,
                    today, today.plusDays(PAYMENT_DAYS))) {
                issued++;
            }
        }

        return issued;
    }

    /** Suspends shops whose settlement has been overdue for more than one day. */
    public int suspendOverdueShops(LocalDate today) throws SQLException {

        int suspended = 0;

        for (CommissionSettlement s : settlementDAO.findOverdueUnlocked(today.minusDays(1))) {

            settlementDAO.suspendShop(s.getShopId());
            settlementDAO.markShopLocked(s.getSettlementId());
            suspended++;
        }

        return suspended;
    }

    public Integer findShopIdByOwner(int ownerId) throws SQLException {

        return settlementDAO.findShopIdByOwner(ownerId);
    }

    public List<CommissionSettlement> listForShop(int shopId) throws SQLException {

        return settlementDAO.listByShop(shopId);
    }

    public List<CommissionSettlement> listAll() throws SQLException {

        return settlementDAO.listAll();
    }

    /** The settlement when it belongs to the shop, otherwise {@code null}. */
    public CommissionSettlement findForShop(int settlementId, int shopId) throws SQLException {

        CommissionSettlement s = settlementDAO.findById(settlementId);

        return s == null || s.getShopId() != shopId ? null : s;
    }

    public static final String TXN_PREFIX = "CS";

    public static String txnRefOf(int settlementId) {

        return TXN_PREFIX + settlementId + "_" + System.currentTimeMillis();
    }

    public static boolean isSettlementTxnRef(String txnRef) {

        return txnRef != null && txnRef.startsWith(TXN_PREFIX);
    }

    /** The settlement id encoded in a txn ref, or 0 when the ref is not a settlement ref. */
    public static int settlementIdOf(String txnRef) {

        if (!isSettlementTxnRef(txnRef)) {
            return 0;
        }

        try {
            return Integer.parseInt(txnRef.substring(TXN_PREFIX.length()).split("_")[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Marks the settlement paid and reopens the shop once nothing is left unpaid. */
    public boolean markPaid(int settlementId, String transactionRef) throws SQLException {

        CommissionSettlement s = settlementDAO.findById(settlementId);

        if (s == null || !settlementDAO.markPaid(settlementId, transactionRef)) {
            return false;
        }

        settlementDAO.reactivateShop(s.getShopId());
        return true;
    }
}
