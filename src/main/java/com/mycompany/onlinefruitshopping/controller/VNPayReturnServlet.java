
package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ShopOwnerPaymentDAO;
import com.mycompany.onlinefruitshopping.dao.ShopOwnerPaymentDAO.Payment;
import com.mycompany.onlinefruitshopping.utils.VNPayConfig;
import com.mycompany.onlinefruitshopping.utils.VNPayUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/vnpay-return")
public class VNPayReturnServlet extends HttpServlet {

    private final ShopOwnerPaymentDAO paymentDAO =
            new ShopOwnerPaymentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        processReturn(request, response);
    }

    private void processReturn(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            // Step 1: Verify VNPay signature.
            Map<String, String[]> params =
                    request.getParameterMap();

            boolean validSignature =
                    VNPayUtil.verifySignature(
                            params,
                            VNPayConfig.getHashSecret()
                    );

            if (!validSignature) {
                redirectToStatus(
                        request, response, "invalid", 0
                );
                return;
            }

            // Step 2: Verify merchant code.
            String merchantCode =
                    VNPayUtil.first(params, "vnp_TmnCode");

            if (merchantCode == null
                    || !merchantCode.equals(
                            VNPayConfig.getTmnCode())) {

                redirectToStatus(
                        request, response, "invalid", 0
                );
                return;
            }

            // Step 3: Read transaction information.
            String txnRef =
                    VNPayUtil.first(params, "vnp_TxnRef");

            String amountValue =
                    VNPayUtil.first(params, "vnp_Amount");

            String currency =
                    VNPayUtil.first(params, "vnp_CurrCode");

            String responseCode =
                    VNPayUtil.first(params, "vnp_ResponseCode");

            String transactionStatus =
                    VNPayUtil.first(
                            params, "vnp_TransactionStatus"
                    );

            String transactionNo =
                    VNPayUtil.first(params, "vnp_TransactionNo");

            String bankCode =
                    VNPayUtil.first(params, "vnp_BankCode");

            if (txnRef == null
                    || txnRef.isBlank()
                    || amountValue == null
                    || responseCode == null
                    || transactionStatus == null
                    || currency == null) {

                redirectToStatus(
                        request, response, "invalid", 0
                );
                return;
            }

            // Step 4: Find the payment in the database.
            Payment payment =
                    paymentDAO.findByTxnRef(txnRef);

            if (payment == null) {
                redirectToStatus(
                        request, response, "invalid", 0
                );
                return;
            }

            // Step 5: Verify currency and amount.
            long returnedAmount;

            try {
                returnedAmount = Long.parseLong(amountValue);
            } catch (NumberFormatException e) {
                redirectToStatus(
                        request,
                        response,
                        "invalid",
                        payment.requestId
                );
                return;
            }

            // VNPay sends the amount multiplied by 100.
            long expectedAmount = payment.amount * 100L;

            if (!"VND".equals(currency)
                    || payment.amount != VNPayConfig.AMOUNT
                    || returnedAmount != expectedAmount) {

                redirectToStatus(
                        request,
                        response,
                        "invalid",
                        payment.requestId
                );
                return;
            }

            // Step 6: Check the payment result.
            boolean success =
                    "00".equals(responseCode)
                    && "00".equals(transactionStatus);

            if (success) {

                // Handle a repeated successful return.
                if ("SUCCESS".equalsIgnoreCase(payment.status)) {
                    redirectToStatus(
                            request,
                            response,
                            "success",
                            payment.requestId
                    );
                    return;
                }

                // Only pending payments can be updated to success.
                if (!"PENDING".equalsIgnoreCase(payment.status)) {
                    redirectToStatus(
                            request,
                            response,
                            "failed",
                            payment.requestId
                    );
                    return;
                }

                // IMPORTANT: markSuccess has five parameters.
                boolean updated = paymentDAO.markSuccess(
                        txnRef,
                        payment.amount,
                        transactionNo,
                        bankCode,
                        responseCode
                );

                if (updated) {
                    redirectToStatus(
                            request,
                            response,
                            "success",
                            payment.requestId
                    );
                    return;
                }

                // Check whether another request updated it already.
                Payment latestPayment =
                        paymentDAO.findByTxnRef(txnRef);

                if (latestPayment != null
                        && "SUCCESS".equalsIgnoreCase(
                                latestPayment.status)) {

                    redirectToStatus(
                            request,
                            response,
                            "success",
                            latestPayment.requestId
                    );
                } else {
                    redirectToStatus(
                            request,
                            response,
                            "error",
                            payment.requestId
                    );
                }

                return;
            }

            // Step 7: Mark an unsuccessful transaction as failed.
            if ("PENDING".equalsIgnoreCase(payment.status)) {
                paymentDAO.markFailed(
                        txnRef,
                        responseCode
                );
            }

            redirectToStatus(
                    request,
                    response,
                    "failed",
                    payment.requestId
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Cannot update payment in the database.",
                    e
            );
        } catch (Exception e) {
            throw new ServletException(
                    "Cannot verify VNPay response.",
                    e
            );
        }
    }

    private void redirectToStatus(
            HttpServletRequest request,
            HttpServletResponse response,
            String result,
            int requestId
    ) throws IOException {

        StringBuilder url = new StringBuilder(
                request.getContextPath()
                        + "/shop-owner-status?paymentResult="
                        + result
        );

        if (requestId > 0) {
            url.append("&requestId=").append(requestId);
        }

        response.sendRedirect(url.toString());
    }
}
