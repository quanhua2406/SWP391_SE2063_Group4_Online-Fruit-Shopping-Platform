package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.service.commission.CommissionSettlementService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@WebServlet("/admin/commission-settlements")
public class AdminCommissionSettlementServlet extends HttpServlet {

    private final CommissionSettlementService service = new CommissionSettlementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            request.setAttribute("settlements", service.listAll());
            request.setAttribute("today", LocalDate.now());
            request.setAttribute("defaultMonth", YearMonth.now().minusMonths(1).toString());
        } catch (SQLException e) {
            throw new ServletException("Không tải được kỳ thanh toán hoa hồng.", e);
        }

        request.getRequestDispatcher("/jsp/admin/admin-commission-settlements.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String message;

        try {
            if ("confirm".equals(request.getParameter("action"))) {
                int settlementId = Integer.parseInt(request.getParameter("settlementId"));
                message = service.markPaid(settlementId, "BANK_TRANSFER_ADMIN_CONFIRMED")
                        ? "Đã xác nhận nhận chuyển khoản kỳ #" + settlementId + "."
                        : "Kỳ không tồn tại hoặc đã thanh toán.";
            } else {
                YearMonth month = YearMonth.parse(request.getParameter("month"));
                int issued = service.issueSettlements(month, LocalDate.now());
                message = "Đã tạo " + issued + " kỳ thanh toán cho tháng " + month + ".";
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            message = "Dữ liệu không hợp lệ.";
        } catch (SQLException e) {
            throw new ServletException("Không xử lý được kỳ thanh toán.", e);
        }

        response.sendRedirect(request.getContextPath()
                + "/admin/commission-settlements?msg="
                + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
