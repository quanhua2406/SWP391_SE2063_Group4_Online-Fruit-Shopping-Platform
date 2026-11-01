package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.service.report.AdminReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Controller điều phối toàn bộ các báo cáo & thống kê dành cho Admin (Feature 4).
 * Hỗ trợ 6 phân hệ báo cáo:
 * 1. Báo cáo bán hàng (Sales Reports)
 * 2. Báo cáo doanh thu & chiết khấu (Revenue Reports)
 * 3. Thống kê sản phẩm & đánh giá (Product Statistics)
 * 4. Báo cáo tồn kho & cảnh báo (Inventory Reports)
 * 5. Thống kê đơn hàng (Order Statistics)
 * 6. Thống kê khách hàng & hội viên (Customer Statistics)
 * Kèm xuất dữ liệu CSV.
 */
@WebServlet(urlPatterns = {"/admin/reports", "/admin/reports/export"})
public class AdminReportServlet extends HttpServlet {

    private AdminReportService reportService;

    @Override
    public void init() {
        this.reportService = new AdminReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Xử lý xuất file CSV
        if ("/admin/reports/export".equals(path)) {
            handleExport(request, response);
            return;
        }

        // Mặc định tab = sales nếu không truyền
        String tab = request.getParameter("tab");
        if (tab == null || tab.isBlank()) {
            tab = "sales";
        }
        tab = tab.trim().toLowerCase();

        // Xử lý bộ lọc ngày
        String startParam = request.getParameter("startDate");
        String endParam = request.getParameter("endDate");
        LocalDate startDate = parseDate(startParam);
        LocalDate endDate = parseDate(endParam);

        // Nếu người dùng chưa chọn ngày, mặc định lấy 30 ngày gần nhất
        if (startDate == null && endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        // Xử lý lọc shop & category
        Integer shopId = parseInteger(request.getParameter("shopId"));
        Integer categoryId = parseInteger(request.getParameter("categoryId"));

        try {
            // Nạp danh sách dropdown cho bộ lọc
            List<Map<String, Object>> shops = reportService.getActiveShops();
            List<Map<String, Object>> categories = reportService.getAllCategories();
            request.setAttribute("shops", shops);
            request.setAttribute("categories", categories);

            request.setAttribute("currentTab", tab);
            request.setAttribute("startDate", startDate != null ? startDate.toString() : "");
            request.setAttribute("endDate", endDate != null ? endDate.toString() : "");
            request.setAttribute("shopId", shopId);
            request.setAttribute("categoryId", categoryId);

            // Nạp dữ liệu theo từng tab
            switch (tab) {
                case "revenue":
                    Map<String, Object> revData = reportService.getRevenueReport(startDate, endDate, shopId);
                    request.setAttribute("revenueReport", revData);
                    break;
                case "products":
                    Map<String, Object> prodData = reportService.getProductStatistics(10);
                    request.setAttribute("productStats", prodData);
                    break;
                case "inventory":
                    Map<String, Object> invData = reportService.getInventoryReports();
                    request.setAttribute("inventoryReport", invData);
                    break;
                case "orders":
                    Map<String, Object> ordData = reportService.getOrderStatistics(startDate, endDate);
                    request.setAttribute("orderStats", ordData);
                    break;
                case "customers":
                    Map<String, Object> custData = reportService.getCustomerStatistics();
                    request.setAttribute("customerStats", custData);
                    break;
                case "sales":
                default:
                    List<Map<String, Object>> salesData = reportService.getSalesReport(startDate, endDate, shopId, categoryId);
                    request.setAttribute("salesReport", salesData);
                    break;
            }

        } catch (SQLException e) {
            request.setAttribute("errorMessage", "Không thể nạp dữ liệu báo cáo: " + e.getMessage());
        }

        request.getRequestDispatcher("/jsp/admin/admin-reports.jsp").forward(request, response);
    }

    private void handleExport(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getParameter("type");
        if (type == null) type = "sales";

        String startParam = request.getParameter("startDate");
        String endParam = request.getParameter("endDate");
        LocalDate startDate = parseDate(startParam);
        LocalDate endDate = parseDate(endParam);
        Integer shopId = parseInteger(request.getParameter("shopId"));
        Integer categoryId = parseInteger(request.getParameter("categoryId"));

        try {
            String csvContent;
            String fileName;

            if ("revenue".equalsIgnoreCase(type)) {
                csvContent = reportService.exportRevenueCSV(startDate, endDate, shopId);
                fileName = "BaoCao_DoanhThu_" + LocalDate.now() + ".csv";
            } else {
                csvContent = reportService.exportSalesCSV(startDate, endDate, shopId, categoryId);
                fileName = "BaoCao_BanHang_" + LocalDate.now() + ".csv";
            }

            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            try (OutputStream os = response.getOutputStream()) {
                os.write(csvContent.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi xuất báo cáo: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return LocalDate.parse(val.trim());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private Integer parseInteger(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
