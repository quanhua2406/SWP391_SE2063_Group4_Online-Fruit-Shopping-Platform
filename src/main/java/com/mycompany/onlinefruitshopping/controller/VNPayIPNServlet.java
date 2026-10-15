
package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ShopOwnerPaymentDAO;
import com.mycompany.onlinefruitshopping.dao.ShopOwnerPaymentDAO.Payment;
import com.mycompany.onlinefruitshopping.utils.VNPayConfig;
import com.mycompany.onlinefruitshopping.utils.VNPayUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebServlet("/vnpay-ipn")
public class VNPayIPNServlet extends HttpServlet {

    private final ShopOwnerPaymentDAO dao =
            new ShopOwnerPaymentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        processIPN(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        processIPN(request, response);
    }

    private void processIPN(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        try {
            Map<String, String[]> params =
                    request.getParameterMap();

            // 1. Verify VNPay signature.
            boolean validSignature =
                    VNPayUtil.verifySignature(
                            params,
                            VNPayConfig.getHashSecret()
                    );

            if (!validSignature) {
                reply(response, "97", "Invalid signature");
                return;
            }

            // 2. Verify merchant code.
            String tmnCode =
                    VNPayUtil.first(params, "vnp_TmnCode");

            if (tmnCode == null
                    || !VNPayConfig.getTmnCode().equals(tmnCode)) {

                reply(response, "97", "Invalid merchant");
                return;
            }

            // 3. Read transaction information.
            String txnRef =
                    VNPayUtil.first(params, "vnp_TxnRef");

            String amountText =
                    VNPayUtil.first(params, "vnp_Amount");

            String currency =
                    VNPayUtil.first(params, "vnp_CurrCode");

            String responseCode =
                    VNPayUtil.first(params, "vnp_ResponseCode");

            String transactionStatus =
                    VNPayUtil.first(
                            params,
                            "vnp_TransactionStatus"
                    );

            String transactionNo =
                    VNPayUtil.first(
                            params,
                            "vnp_TransactionNo"
                    );

            String bankCode =
                    VNPayUtil.first(params, "vnp_BankCode");

            if (txnRef == null
                    || txnRef.isBlank()
                    || amountText == null
                    || responseCode == null
                    || transactionStatus == null
                    || currency == null) {

                reply(response, "99", "Missing parameters");
                return;
            }

            // 4. Find the payment record.
            Payment payment = dao.findByTxnRef(txnRef);

            if (payment == null) {
                reply(response, "01", "Order not found");
                return;
            }

            // 5. Check currency and amount.
            long vnpAmount;

            try {
                vnpAmount = Long.parseLong(amountText);
            } catch (NumberFormatException e) {
                reply(response, "04", "Invalid amount");
                return;
            }

            // Database stores VND; VNPay sends amount x 100.
            long expectedAmount = payment.amount * 100L;

            if (!"VND".equals(currency)
                    || payment.amount != VNPayConfig.AMOUNT
                    || vnpAmount != expectedAmount) {

                reply(response, "04", "Invalid amount");
                return;
            }

            // 6. Handle repeated notifications.
            if ("SUCCESS".equalsIgnoreCase(payment.status)) {
                reply(response, "02", "Order already confirmed");
                return;
            }

            if ("FAILED".equalsIgnoreCase(payment.status)) {
                reply(response, "02", "Order already processed");
                return;
            }

            if (!"PENDING".equalsIgnoreCase(payment.status)) {
                reply(response, "99", "Invalid payment status");
                return;
            }

            // 7. Update the payment result.
            boolean transactionSucceeded =
                    "00".equals(responseCode)
                    && "00".equals(transactionStatus);

            if (transactionSucceeded) {

                // Must pass all five parameters to markSuccess().
                boolean updated = dao.markSuccess(
                        txnRef,
                        payment.amount,
                        transactionNo,
                        bankCode,
                        responseCode
                );

                if (updated) {
                    reply(response, "00", "Confirm Success");
                    return;
                }

                // Check whether another request updated it first.
                Payment refreshed = dao.findByTxnRef(txnRef);

                if (refreshed != null
                        && "SUCCESS".equalsIgnoreCase(
                                refreshed.status)) {

                    reply(response, "02", "Order already confirmed");
                } else {
                    reply(response, "99", "Cannot update payment");
                }

                return;
            }

            // 8. The signature is valid, but the payment failed.
            dao.markFailed(txnRef, responseCode);

            // Acknowledge that the notification was processed.
            reply(response, "00", "Confirm Success");

        } catch (Exception e) {
            // Log the actual exception for debugging.
            getServletContext().log(
                    "Error processing VNPay IPN",
                    e
            );

            reply(response, "99", "Unknown error");
        }
    }

    private void reply(
            HttpServletResponse response,
            String code,
            String message
    ) throws IOException {

        response.getWriter().write(
                "{\"RspCode\":\"" + code
                + "\",\"Message\":\"" + message + "\"}"
        );
    }
}
