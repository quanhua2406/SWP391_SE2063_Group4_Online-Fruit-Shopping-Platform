package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ShopOwnerPaymentDAO;
import com.mycompany.onlinefruitshopping.dao.ShopOwnerPaymentDAO.Payment;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.DBConnection;
import com.mycompany.onlinefruitshopping.utils.VNPayConfig;
import com.mycompany.onlinefruitshopping.utils.VNPayUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.SortedMap;
import java.util.TreeMap;

@WebServlet("/shop-owner-payment")
public class ShopOwnerPaymentServlet extends HttpServlet {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ShopOwnerPaymentDAO dao =
            new ShopOwnerPaymentDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        // Step 1: Check login.
        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("user") instanceof User)) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            // Step 2: Validate request ID.
            String requestIdValue =
                    request.getParameter("requestId");

            if (requestIdValue == null
                    || requestIdValue.isBlank()) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Missing request ID."
                );
                return;
            }

            int requestId;

            try {
                requestId = Integer.parseInt(requestIdValue);
            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid request ID."
                );
                return;
            }

            if (requestId <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid request ID."
                );
                return;
            }

            // Step 3: Verify that the request belongs to this user.
            String checkSql = """
                SELECT r.request_id
                FROM dbo.shop_owner_requests r
                INNER JOIN dbo.users u
                    ON u.user_id = r.user_id
                WHERE r.request_id = ?
                  AND u.email = ?
                  AND r.status = 'PENDING'
                """;

            boolean ownsRequest = false;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps =
                         conn.prepareStatement(checkSql)) {

                ps.setInt(1, requestId);
                ps.setString(2, user.getEmail());

                try (ResultSet rs = ps.executeQuery()) {
                    ownsRequest = rs.next();
                }
            }

            if (!ownsRequest) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Request not found, not owned by this account, "
                                + "or not pending."
                );
                return;
            }

            // Step 4: Create a payment record in SQL Server.
            Payment payment = dao.createPayment(requestId);

            if (payment == null
                    || payment.txnRef == null
                    || payment.amount <= 0) {

                throw new ServletException(
                        "Cannot create a valid payment record."
                );
            }

            if (payment.amount != VNPayConfig.AMOUNT) {
                throw new ServletException(
                        "Payment amount does not match VNPay configuration."
                );
            }

            // Step 5: Build VNPay parameters.
            SortedMap<String, String> params = new TreeMap<>();

            LocalDateTime now = LocalDateTime.now();

            params.put("vnp_Version", VNPayConfig.VERSION);
            params.put("vnp_Command", VNPayConfig.COMMAND);
            params.put("vnp_TmnCode", VNPayConfig.getTmnCode());

            // VNPay requires the amount multiplied by 100.
            params.put(
                    "vnp_Amount",
                    String.valueOf(payment.amount * 100L)
            );

            params.put("vnp_CurrCode", VNPayConfig.CURR_CODE);
            params.put("vnp_TxnRef", payment.txnRef);

            params.put(
                    "vnp_OrderInfo",
                    "Shop Owner registration " + requestId
            );

            params.put("vnp_OrderType", VNPayConfig.ORDER_TYPE);
            params.put("vnp_Locale", "vn");

            // Use the public HTTPS URL instead of localhost.
            params.put(
                    "vnp_ReturnUrl",
                    VNPayConfig.getReturnUrl()
            );

            params.put("vnp_IpAddr", getClientIp(request));

            params.put(
                    "vnp_CreateDate",
                    now.format(VNPAY_DATE_FORMAT)
            );

            params.put(
                    "vnp_ExpireDate",
                    now.plusMinutes(15)
                            .format(VNPAY_DATE_FORMAT)
            );

            // Step 6: Generate the VNPay signature.
            String hashData = VNPayUtil.buildHashData(params);

            String secureHash = VNPayUtil.hmacSHA512(
                    VNPayConfig.getHashSecret(),
                    hashData
            );

            // Step 7: Build the payment URL and redirect.
            String paymentUrl =
                    VNPayConfig.PAYMENT_URL
                    + "?"
                    + VNPayUtil.buildQuery(params)
                    + "&vnp_SecureHash="
                    + secureHash;

            response.sendRedirect(paymentUrl);
            // Temporary debugging: do not print the full payment URL.
System.out.println("===== VNPAY DEBUG =====");
System.out.println("TMN Code: " + params.get("vnp_TmnCode"));
System.out.println("Endpoint: " + VNPayConfig.PAYMENT_URL);
System.out.println("Amount: " + params.get("vnp_Amount"));
System.out.println("Return URL: " + params.get("vnp_ReturnUrl"));
System.out.println("=======================");
        } catch (ServletException e) {
            throw e;

        } catch (Exception e) {
            throw new ServletException(
                    "Cannot create VNPay payment.",
                    e
            );
        }
    }

    /**
     * Return the direct remote address.
     * Do not trust forwarding headers unless a trusted proxy
     * has been explicitly configured.
     */
    private String getClientIp(HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        if (ip == null || ip.isBlank()) {
            return "127.0.0.1";
        }

        return ip;
    }
}