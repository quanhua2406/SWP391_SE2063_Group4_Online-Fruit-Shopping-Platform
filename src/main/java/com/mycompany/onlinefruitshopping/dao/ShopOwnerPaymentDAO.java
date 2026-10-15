
package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopOwnerPaymentDAO {

    public static class Payment {
        public long paymentId;
        public int requestId;
        public String txnRef;
        public long amount;
        public String status;
    }

    /**
     * Create a pending payment for an existing pending shop owner request.
     */
    public Payment createPayment(int requestId) throws SQLException {

        String txnRef = "SOR-" + requestId + "-"
                + System.currentTimeMillis();

        String sql = """
            INSERT INTO dbo.shop_owner_payments
                (
                    request_id,
                    user_id,
                    txn_ref,
                    amount,
                    currency,
                    payment_status,
                    created_at
                )
            OUTPUT INSERTED.payment_id
            SELECT
                r.request_id,
                r.user_id,
                ?,
                ?,
                'VND',
                'PENDING',
                SYSDATETIME()
            FROM dbo.shop_owner_requests r
            WHERE r.request_id = ?
              AND r.status = 'PENDING'
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txnRef);
            ps.setLong(2, 200000L);
            ps.setInt(3, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Shop owner request was not found "
                            + "or is not pending."
                    );
                }

                Payment payment = new Payment();
                payment.paymentId = rs.getLong("payment_id");
                payment.requestId = requestId;
                payment.txnRef = txnRef;
                payment.amount = 200000L;
                payment.status = "PENDING";

                return payment;
            }
        }
    }

    /**
     * Find a payment using its VNPay transaction reference.
     */
    public Payment findByTxnRef(String txnRef)
            throws SQLException {

        String sql = """
            SELECT
                payment_id,
                request_id,
                txn_ref,
                amount,
                payment_status
            FROM dbo.shop_owner_payments
            WHERE txn_ref = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txnRef);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Payment payment = new Payment();
                payment.paymentId =
                        rs.getLong("payment_id");
                payment.requestId =
                        rs.getInt("request_id");
                payment.txnRef =
                        rs.getString("txn_ref");
                payment.amount =
                        rs.getLong("amount");
                payment.status =
                        rs.getString("payment_status");

                return payment;
            }
        }
    }

    /**
     * Mark payment successful only if it is still pending
     * and the transaction reference and amount match.
     */
    public boolean markSuccess(
            String txnRef,
            long amount,
            String transactionNo,
            String bankCode,
            String responseCode
    ) throws SQLException {

        String sql = """
            UPDATE dbo.shop_owner_payments
            SET payment_status = 'SUCCESS',
                vnp_transaction_no = ?,
                bank_code = ?,
                vnp_response_code = ?,
                paid_at = SYSDATETIME()
            WHERE txn_ref = ?
              AND amount = ?
              AND currency = 'VND'
              AND payment_status = 'PENDING'
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionNo);
            ps.setString(2, bankCode);
            ps.setString(3, responseCode);
            ps.setString(4, txnRef);
            ps.setLong(5, amount);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Mark a pending payment as failed.
     */
    public void markFailed(
            String txnRef,
            String responseCode
    ) throws SQLException {

        String sql = """
            UPDATE dbo.shop_owner_payments
            SET payment_status = 'FAILED',
                vnp_response_code = ?
            WHERE txn_ref = ?
              AND payment_status = 'PENDING'
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, responseCode);
            ps.setString(2, txnRef);

            ps.executeUpdate();
        }
    }
}
