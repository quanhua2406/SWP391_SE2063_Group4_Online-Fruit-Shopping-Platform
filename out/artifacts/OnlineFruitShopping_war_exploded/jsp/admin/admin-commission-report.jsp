<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.report.CommissionReportDAO"%>

<%!
    private String statusOptions(String selected) {

        StringBuilder html = new StringBuilder();

        html.append("<option value=\"\"")
            .append(selected.isEmpty() ? " selected" : "")
            .append(">Đã giao (tính phí)</option>");

        String[][] statuses = {
            {"pending_confirmation", "Chờ xác nhận"},
            {"preparing", "Đang chuẩn bị"},
            {"shipping", "Đang giao"},
            {"delivered", "Đã giao"},
            {"cancelled", "Đã hủy"},
            {"failed", "Thất bại"}
        };

        for (String[] s : statuses) {
            html.append("<option value=\"").append(s[0]).append("\"")
                .append(s[0].equals(selected) ? " selected" : "")
                .append(">").append(s[1]).append("</option>");
        }

        return html.toString();
    }

    private String secUrl(String base, String section, String from, String to, String status) {

        StringBuilder u = new StringBuilder(base).append("?section=").append(section);

        if (from != null && !from.isEmpty()) {
            u.append("&from=").append(from);
        }

        if (to != null && !to.isEmpty()) {
            u.append("&to=").append(to);
        }

        if (status != null && !status.isEmpty()) {
            u.append("&status=").append(status);
        }

        return u.toString();
    }

    private String esc(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String money(Object value) {
        if (value == null) {
            return "0 \u0111";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return String.format(Locale.US, "%,.0f", number) + " \u0111";
    }

    private String percent(Object value) {
        if (value == null) {
            return "0 %";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return number.stripTrailingZeros().toPlainString() + " %";
    }

    private String qty(Object value) {
        if (value == null) {
            return "0";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return number.stripTrailingZeros().toPlainString();
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> shopReport =
            (List<Map<String, Object>>) request.getAttribute("shopReport");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> rateReport =
            (List<Map<String, Object>>) request.getAttribute("rateReport");

    Object totalRevenue = request.getAttribute("totalRevenue");
    Object totalFee = request.getAttribute("totalFee");
    Object totalEarning = request.getAttribute("totalEarning");
    Object serviceFee = request.getAttribute("serviceFee");
    Object netPayout = request.getAttribute("netPayout");

    @SuppressWarnings("unchecked")
    Map<String, Object> breakdown =
            (Map<String, Object>) request.getAttribute("breakdown");

    String from = request.getParameter("from") == null
            ? ""
            : request.getParameter("from");

    String to = request.getParameter("to") == null
            ? ""
            : request.getParameter("to");

    String status = request.getAttribute("status") == null
            ? ""
            : request.getAttribute("status").toString();

    boolean hasSales = false;

    if (shopReport != null) {
        for (Map<String, Object> row : shopReport) {
            Object sold = row.get("sold_qty");

            if (sold != null
                    && new BigDecimal(sold.toString()).signum() > 0) {
                hasSales = true;
                break;
            }
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Báo cáo hoa hồng - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/admin/dashboard">
            📊 Bảng điều khiển
        </a>

        <a href="<%= contextPath %>/admin/product-management">
            🍎 Sản phẩm
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Đăng xuất
        </a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">
            ← Bảng điều khiển
        </a>
    </p>

    <h1>Báo cáo hoa hồng</h1>

    <p class="dashboard-welcome">
        Phí nền tảng thu từ chủ cửa hàng, tính theo
        tỷ lệ hoa hồng&nbsp;% trên mỗi đơn bán.
    </p>

    <p class="action-row">
        <a class="btn-secondary"
           href="<%= QueryUrl.keep(request, contextPath + "/admin/commission-report", "from", "to", "status") %>format=csv">
            ⬇️ Xuất CSV
        </a>
    </p>

    <%
        String section = request.getAttribute("section") == null
                ? "overview"
                : request.getAttribute("section").toString();

        String reportBase = contextPath + "/admin/commission-report";
        String secOverview = secUrl(reportBase, "overview", from, to, status);
        String secShops    = secUrl(reportBase, "shops", from, to, status);
        String secRates    = secUrl(reportBase, "rates", from, to, status);
    %>

    <!-- Section navigation: one small page per report view -->

    <nav class="section-nav">

        <a class="<%= "overview".equals(section) ? "active" : "" %>"
           href="<%= secOverview %>">
            📊 Tổng quan
        </a>

        <a class="<%= "shops".equals(section) ? "active" : "" %>"
           href="<%= secShops %>">
            🏪 Theo cửa hàng
        </a>

        <a class="<%= "rates".equals(section) ? "active" : "" %>"
           href="<%= secRates %>">
            📈 Theo tỷ lệ
        </a>

    </nav>

    <!-- ==================== REPORT FILTERS ==================== -->

    <form method="get"
          action="<%= contextPath %>/admin/commission-report"
          class="filter-form">

        <input type="hidden" name="section" value="<%= esc(section) %>">

        <label for="from">Từ ngày</label>

        <input class="filter-input" type="date" id="from"
               name="from" value="<%= esc(from) %>">

        <label for="to">Đến ngày</label>

        <input class="filter-input" type="date" id="to"
               name="to" value="<%= esc(to) %>">

        <label for="status">Trạng thái đơn</label>

        <select class="filter-input" id="status" name="status">
            <%= statusOptions(status) %>
        </select>

        <button type="submit" class="btn-sm">Áp dụng</button>

        <a class="filter-clear"
           href="<%= secUrl(reportBase, section, "", "", "") %>">Xóa lọc</a>

    </form>

    <% if ("overview".equals(section)) { %>

    <!-- ==================== SUMMARY ==================== -->

    <div class="summary-grid">

        <div class="summary-card">
            <span class="summary-label">Tổng doanh thu</span>
            <span class="summary-value"><%= money(totalRevenue) %></span>
        </div>

        <div class="summary-card summary-accent">
            <span class="summary-label">Phí nền tảng đã thu</span>
            <span class="summary-value"><%= money(totalFee) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Đã trả cửa hàng</span>
            <span class="summary-value"><%= money(totalEarning) %></span>
        </div>

    </div>

    <% if (!hasSales) { %>

        <div class="success-message">
            ℹ️ Chưa có doanh số — các số liệu bên dưới sẽ khác 0 ngay khi
            sản phẩm của cửa hàng được đặt mua.
        </div>

    <% } %>

    <!-- ==================== FEE BREAKDOWN ==================== -->

    <div class="panel">

        <h2>🧾 Chi tiết phí</h2>

        <p class="panel-hint">
            Quyết toán nền tảng trong kỳ đã chọn, trên tất cả
            cửa hàng. Hoa hồng chỉ tính trên đơn <em>đã giao</em>;
            đơn đã hủy và đơn thất bại được ghi nhận
            là hoàn tiền.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Mục</th>
                        <th>Số tiền</th>
                    </tr>
                </thead>

                <tbody>
                    <tr>
                        <td>Tổng doanh số (tất cả đơn trong kỳ)</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("gross_sales")) %></td>
                    </tr>
                    <tr>
                        <td>Giảm giá khuyến mãi (&minus;)</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("promotional_discount")) %></td>
                    </tr>
                    <tr>
                        <td>Đơn hoàn tiền / đã hủy</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("refunds")) %></td>
                    </tr>
                    <tr>
                        <td><strong>Doanh thu thực thu (cơ sở hoa hồng)</strong></td>
                        <td><strong><%= money(totalRevenue) %></strong></td>
                    </tr>
                    <tr>
                        <td>Phí dịch vụ (<%= CommissionReportDAO.SERVICE_FEE_RATE.toPlainString() %> %)</td>
                        <td><%= money(serviceFee) %></td>
                    </tr>
                    <tr>
                        <td>Hoa hồng nền tảng (theo tỷ lệ từng sản phẩm)</td>
                        <td><%= money(totalFee) %></td>
                    </tr>
                    <tr>
                        <td><strong>Thực trả cửa hàng (ròng)</strong></td>
                        <td><strong><%= money(netPayout) %></strong></td>
                    </tr>
                </tbody>

            </table>

        </div>

    </div>

    <% } else if ("shops".equals(section)) { %>

    <!-- ==================== FEE BY SHOP ==================== -->

    <div class="panel">

        <h2>🏪 Phí nền tảng theo cửa hàng</h2>

        <p class="panel-hint">
            Số phí nền tảng thu được từ từng cửa hàng.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Cửa hàng</th>
                        <th>Sản phẩm</th>
                        <th>Số lượng bán</th>
                        <th>Doanh thu</th>
                        <th>Hoa hồng TB</th>
                        <th>Phí nền tảng</th>
                        <th>Thực trả cửa hàng</th>
                    </tr>
                </thead>

                <tbody>
                <% if (shopReport == null || shopReport.isEmpty()) { %>

                    <tr>
                        <td colspan="7">
                            Chưa có cửa hàng để báo cáo.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : shopReport) {
                %>
                    <tr>
                        <td><%= esc(row.get("shop_name")) %></td>
                        <td><%= qty(row.get("product_count")) %></td>
                        <td><%= qty(row.get("sold_qty")) %></td>
                        <td><%= money(row.get("revenue")) %></td>
                        <td><%= percent(row.get("average_rate")) %></td>
                        <td><%= money(row.get("platform_fee")) %></td>
                        <td><%= money(row.get("shop_earning")) %></td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

                <tfoot>
                    <tr>
                        <td colspan="3">Tổng cộng</td>
                        <td><%= money(totalRevenue) %></td>
                        <td></td>
                        <td><%= money(totalFee) %></td>
                        <td><%= money(totalEarning) %></td>
                    </tr>
                </tfoot>

            </table>

        </div>

    </div>

    <% } else if ("rates".equals(section)) { %>

    <!-- ==================== FEE BY RATE ==================== -->

    <div class="panel">

        <h2>📊 Phí nền tảng theo tỷ lệ hoa hồng</h2>

        <p class="panel-hint">
            Phí thu được, nhóm theo tỷ lệ hoa hồng đặt
            cho sản phẩm.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Tỷ lệ hoa hồng</th>
                        <th>Số lượng bán</th>
                        <th>Doanh thu</th>
                        <th>Phí nền tảng</th>
                    </tr>
                </thead>

                <tbody>
                <% if (rateReport == null || rateReport.isEmpty()) { %>

                    <tr>
                        <td colspan="4">
                            Chưa có tỷ lệ hoa hồng để báo cáo.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : rateReport) {
                %>
                    <tr>
                        <td>
                            <span class="pill pill-ok">
                                <%= percent(row.get("commission_rate")) %>
                            </span>
                        </td>
                        <td><%= qty(row.get("sold_qty")) %></td>
                        <td><%= money(row.get("revenue")) %></td>
                        <td><%= money(row.get("platform_fee")) %></td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

            </table>

        </div>

    </div>

    <% } %>

</section>

</body>
</html>
