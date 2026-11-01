package com.mycompany.onlinefruitshopping.service.report;

import com.mycompany.onlinefruitshopping.dao.report.AdminReportDAO;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service tầng nghiệp vụ cho Feature 4: Reporting & Dashboard (Admin).
 */
public class AdminReportService {

    private final AdminReportDAO adminReportDAO;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0");

    public AdminReportService() {
        this.adminReportDAO = new AdminReportDAO();
    }

    public AdminReportService(AdminReportDAO adminReportDAO) {
        this.adminReportDAO = adminReportDAO;
    }

    // 1. KPI Dashboard
    public Map<String, Object> getDashboardKPIs() throws SQLException {
        return adminReportDAO.getDashboardKPIs();
    }

    // 2. Top sản phẩm bán chạy
    public List<Map<String, Object>> getTopSellingFruits(int limit) throws SQLException {
        return adminReportDAO.getTopSellingFruits(limit);
    }

    // 3. Báo cáo bán hàng
    public List<Map<String, Object>> getSalesReport(LocalDate startDate, LocalDate endDate, Integer shopId, Integer categoryId) throws SQLException {
        return adminReportDAO.getSalesReport(startDate, endDate, shopId, categoryId);
    }

    // 4. Báo cáo doanh thu & chiết khấu
    public Map<String, Object> getRevenueReport(LocalDate startDate, LocalDate endDate, Integer shopId) throws SQLException {
        return adminReportDAO.getRevenueReport(startDate, endDate, shopId);
    }

    // 5. Thống kê sản phẩm
    public Map<String, Object> getProductStatistics(int limit) throws SQLException {
        return adminReportDAO.getProductStatistics(limit);
    }

    // 6. Báo cáo tồn kho
    public Map<String, Object> getInventoryReports() throws SQLException {
        return adminReportDAO.getInventoryReports();
    }

    // 7. Thống kê đơn hàng
    public Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate) throws SQLException {
        return adminReportDAO.getOrderStatistics(startDate, endDate);
    }

    // 8. Thống kê khách hàng
    public Map<String, Object> getCustomerStatistics() throws SQLException {
        return adminReportDAO.getCustomerStatistics();
    }

    // Dropdown helpers
    public List<Map<String, Object>> getActiveShops() throws SQLException {
        return adminReportDAO.getActiveShops();
    }

    public List<Map<String, Object>> getAllCategories() throws SQLException {
        return adminReportDAO.getAllCategories();
    }

    // =========================================================================
    // XUẤT CSV BÁO CÁO BÁN HÀNG (EXPORT SALES REPORT)
    // =========================================================================
    public String exportSalesCSV(LocalDate startDate, LocalDate endDate, Integer shopId, Integer categoryId) throws SQLException {
        List<Map<String, Object>> list = getSalesReport(startDate, endDate, shopId, categoryId);
        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM để Excel hiển thị đúng tiếng Việt
        sb.append("\uFEFF");
        sb.append("STT,Mã SP,Tên Hoa Quả,Danh Mục,Cửa Hàng,Quy Cách,Đơn Vị,Số Lượng Bán,Doanh Thu (VNĐ),Số Đơn Hàng\n");

        int index = 1;
        for (Map<String, Object> row : list) {
            sb.append(index++).append(",")
              .append(row.get("productId")).append(",")
              .append(escapeCSV((String) row.get("productName"))).append(",")
              .append(escapeCSV((String) row.get("categoryName"))).append(",")
              .append(escapeCSV((String) row.get("shopName"))).append(",")
              .append(escapeCSV((String) row.get("packagingType"))).append(",")
              .append(escapeCSV((String) row.get("unit"))).append(",")
              .append(row.get("quantitySold")).append(",")
              .append(String.format(Locale.US, "%.0f", (Double) row.get("totalRevenue"))).append(",")
              .append(row.get("totalOrders")).append("\n");
        }
        return sb.toString();
    }

    // =========================================================================
    // XUẤT CSV BÁO CÁO DOANH THU (EXPORT REVENUE REPORT)
    // =========================================================================
    public String exportRevenueCSV(LocalDate startDate, LocalDate endDate, Integer shopId) throws SQLException {
        Map<String, Object> report = getRevenueReport(startDate, endDate, shopId);
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF");
        sb.append("Chỉ Tiêu Tài Chính,Giá Trị (VNĐ)\n");
        sb.append("Tổng Doanh Thu Gộp (Gross Revenue),").append(String.format(Locale.US, "%.0f", (Double) report.get("grossRevenue"))).append("\n");
        sb.append("Tổng Giảm Giá Voucher (Discounts),").append(String.format(Locale.US, "%.0f", (Double) report.get("totalVouchers"))).append("\n");
        sb.append("Hoa Hồng Nền Tảng (Platform Commission),").append(String.format(Locale.US, "%.0f", (Double) report.get("platformCommission"))).append("\n");
        sb.append("Tổng Tiền Hoàn Trả (Refunds),").append(String.format(Locale.US, "%.0f", (Double) report.get("totalRefunds"))).append("\n");
        sb.append("Doanh Thu Thuần Sàn (Net Revenue),").append(String.format(Locale.US, "%.0f", (Double) report.get("netPlatformRevenue"))).append("\n");
        sb.append("Biên Lợi Nhuận (Profit Margin),").append(report.get("profitMargin")).append("%\n\n");

        sb.append("Phương Thức Thanh Toán,Số Đơn Hàng,Tổng Tiền (VNĐ)\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> methods = (List<Map<String, Object>>) report.get("paymentBreakdown");
        if (methods != null) {
            for (Map<String, Object> m : methods) {
                sb.append(escapeCSV((String) m.get("methodName"))).append(",")
                  .append(m.get("orderCount")).append(",")
                  .append(String.format(Locale.US, "%.0f", (Double) m.get("totalAmount"))).append("\n");
            }
        }
        return sb.toString();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
