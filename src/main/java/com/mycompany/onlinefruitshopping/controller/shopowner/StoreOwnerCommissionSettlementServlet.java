package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.CommissionSettlement;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.commission.CommissionSettlementService;
import com.mycompany.onlinefruitshopping.utils.VNPayConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shop-owner page for the monthly commission settlements. The shop pays
 * the platform through VNPay; the return URL verifies the signature and
 * marks the settlement as paid.
 */
@WebServlet({
        "/store-owner/commission/settlements",
        "/store-owner/commission/pay",
        "/store-owner/commission/vnpay-return"
})
public class StoreOwnerCommissionSettlementServlet extends HttpServlet {

    private final CommissionSettlementService service = new CommissionSettlementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            Integer shopId = service.findShopIdByOwner(user.getUserID());

            if (shopId == null) {
                response.sendRedirect(request.getContextPath() + "/store-owner/create-shop");
                return;
            }

            if (request.getServletPath().endsWith("/vnpay-return")) {
                handleReturn(request, response, shopId);
                return;
            }

            List<CommissionSettlement> settlements = service.listForShop(shopId);
            LocalDate today = LocalDate.now();
            Set<Integer> overdueIds = new HashSet<>();
            boolean locked = false;

            for (CommissionSettlement s : settlements) {
                if ("unpaid".equals(s.getStatus()) && today.isAfter(s.getDueDate())) {
                    overdueIds.add(s.getSettlementId());
                }
                if ("unpaid".equals(s.getStatus()) && s.isShopLocked()) {
                    locked = true;
                }
            }

            request.setAttribute("settlements", settlements);
            request.setAttribute("overdueIds", overdueIds);
            request.setAttribute("shopLocked", locked);
            request.setAttribute("today", today);

            request.getRequestDispatcher("/jsp/shopowner/commission-settlements.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Không tải được kỳ thanh toán hoa hồng.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            Integer shopId = service.findShopIdByOwner(user.getUserID());

            if (shopId == null) {
                response.sendRedirect(request.getContextPath() + "/store-owner/create-shop");
                return;
            }

            int settlementId = parseId(request.getParameter("settlementId"));
            CommissionSettlement settlement = service.findForShop(settlementId, shopId);

            if (settlement == null || !"unpaid".equals(settlement.getStatus())) {
                redirect(request, response, "error", "Kỳ thanh toán không hợp lệ hoặc đã thanh toán.");
                return;
            }

            String returnUrl = baseUrl(request) + request.getContextPath()
                    + "/store-owner/commission/vnpay-return";

            String url = VNPayConfig.buildPaymentUrl(
                    returnUrl,
                    CommissionSettlementService.txnRefOf(settlement.getSettlementId()),
                    settlement.getAmount().doubleValue(),
                    "Thanh toan hoa hong ky " + settlement.getPeriodMonth(),
                    clientIp(request));

            response.sendRedirect(url);

        } catch (SQLException e) {
            throw new ServletException("Không tạo được thanh toán hoa hồng.", e);
        }
    }

    private void handleReturn(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws SQLException, IOException {

        String txnRef = request.getParameter("vnp_TxnRef");
        int settlementId = CommissionSettlementService.settlementIdOf(txnRef);
        CommissionSettlement settlement = service.findForShop(settlementId, shopId);

        if (settlement == null) {
            redirect(request, response, "error", "Không tìm thấy kỳ thanh toán.");
            return;
        }

        if (!VNPayConfig.verifyCallback(request.getParameterMap())) {
            redirect(request, response, "error", "Chữ ký VNPay không hợp lệ.");
            return;
        }

        String amountStr = request.getParameter("vnp_Amount");
        if (amountStr == null || !amountStr.matches("\\d+")
                || new BigDecimal(amountStr).divide(BigDecimal.valueOf(100))
                        .compareTo(settlement.getAmount()) != 0) {
            redirect(request, response, "error", "Số tiền thanh toán không khớp kỳ hoa hồng.");
            return;
        }

        if (!"00".equals(request.getParameter("vnp_ResponseCode"))) {
            redirect(request, response, "error", "Thanh toán hoa hồng chưa thành công.");
            return;
        }

        String transactionNo = request.getParameter("vnp_TransactionNo");
        service.markPaid(settlementId, transactionNo != null ? transactionNo : txnRef);
        redirect(request, response, "msg", "Đã thanh toán hoa hồng kỳ " + settlement.getPeriodMonth() + ".");
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String key, String message) throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/store-owner/commission/settlements?" + key + "="
                + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    private static int parseId(String value) {

        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String baseUrl(HttpServletRequest request) {

        int port = request.getServerPort();
        boolean standardPort = port == 80 || port == 443;

        return request.getScheme() + "://" + request.getServerName()
                + (standardPort ? "" : ":" + port);
    }

    private static String clientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-FORWARDED-FOR");

        return ip == null || ip.isBlank() ? request.getRemoteAddr() : ip;
    }
}
