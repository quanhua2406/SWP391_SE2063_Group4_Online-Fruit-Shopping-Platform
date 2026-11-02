package com.mycompany.onlinefruitshopping.service.report;

import com.mycompany.onlinefruitshopping.dao.report.AdminReportDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AdminReportServiceTest {

    private AdminReportService reportService;
    private StubAdminReportDAO stubDAO;

    static class StubAdminReportDAO extends AdminReportDAO {
        @Override
        public List<Map<String, Object>> getSalesReport(LocalDate startDate, LocalDate endDate, Integer shopId, Integer categoryId) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productId", 1);
            row.put("productName", "Táo Envy New Zealand");
            row.put("categoryName", "Trái Cây Nhập Khẩu");
            row.put("shopName", "FreshFruit Store");
            row.put("packagingType", "Hộp 1kg");
            row.put("unit", "kg");
            row.put("quantitySold", 25.0);
            row.put("totalRevenue", 3750000.0);
            row.put("totalOrders", 15);
            return List.of(row);
        }

        @Override
        public Map<String, Object> getRevenueReport(LocalDate startDate, LocalDate endDate, Integer shopId) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("grossRevenue", 10000000.0);
            map.put("totalVouchers", 500000.0);
            map.put("platformCommission", 800000.0);
            map.put("totalRefunds", 100000.0);
            map.put("netPlatformRevenue", 700000.0);
            map.put("profitMargin", 7.0);

            Map<String, Object> p1 = new LinkedHashMap<>();
            p1.put("methodKey", "vnpay");
            p1.put("methodName", "VNPay Sandbox");
            p1.put("orderCount", 20);
            p1.put("totalAmount", 6000000.0);

            map.put("paymentBreakdown", List.of(p1));
            return map;
        }

        @Override
        public Map<String, Object> getDashboardKPIs() {
            Map<String, Object> kpis = new LinkedHashMap<>();
            kpis.put("totalRevenue", 15000000.0);
            kpis.put("totalOrders", 45);
            kpis.put("completedOrders", 38);
            kpis.put("pendingOrders", 4);
            kpis.put("activeShops", 3);
            kpis.put("activeCustomers", 25);
            kpis.put("publishedProducts", 12);
            kpis.put("totalStock", 450.0);
            return kpis;
        }
    }

    @BeforeEach
    void setUp() {
        stubDAO = new StubAdminReportDAO();
        reportService = new AdminReportService(stubDAO);
    }

    @Test
    void testGetDashboardKPIs() throws SQLException {
        Map<String, Object> kpis = reportService.getDashboardKPIs();
        assertNotNull(kpis);
        assertEquals(15000000.0, kpis.get("totalRevenue"));
        assertEquals(45, kpis.get("totalOrders"));
        assertEquals(3, kpis.get("activeShops"));
        assertEquals(25, kpis.get("activeCustomers"));
    }

    @Test
    void testExportSalesCSV() throws SQLException {
        String csv = reportService.exportSalesCSV(LocalDate.now().minusDays(30), LocalDate.now(), null, null);
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF")); // UTF-8 BOM
        assertTrue(csv.contains("Táo Envy New Zealand"));
        assertTrue(csv.contains("Trái Cây Nhập Khẩu"));
        assertTrue(csv.contains("3750000"));
    }

    @Test
    void testExportRevenueCSV() throws SQLException {
        String csv = reportService.exportRevenueCSV(LocalDate.now().minusDays(30), LocalDate.now(), null);
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF")); // UTF-8 BOM
        assertTrue(csv.contains("Tổng Doanh Thu Gộp (Gross Revenue)"));
        assertTrue(csv.contains("10000000"));
        assertTrue(csv.contains("VNPay Sandbox"));
        assertTrue(csv.contains("6000000"));
    }
}
