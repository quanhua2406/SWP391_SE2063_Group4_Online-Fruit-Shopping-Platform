<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.model.Product"%>
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
            html.append("<option value=\"").append(s[0]).append('"')
                .append(s[0].equals(selected) ? " selected" : "")
                .append('>').append(s[1]).append("</option>");
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

    private String statusLabel(Object value) {
        if (value == null) {
            return "";
        }

        switch (value.toString().toLowerCase()) {
            case "pending_confirmation":
                return "Chờ xác nhận";
            case "preparing":
                return "Đang chuẩn bị";
            case "shipping":
                return "Đang giao";
            case "delivered":
                return "Đã giao";
            case "cancelled":
                return "Đã hủy";
            case "failed":
                return "Thất bại";
            default:
                return value.toString();
        }
    }

    private String statusPill(Object value) {
        if (value == null) {
            return "pill-ok";
        }

        switch (value.toString().toLowerCase()) {
            case "delivered":
                return "pill-ok";
            case "cancelled":
            case "failed":
                return "pill-danger";
            default:
                return "pill-warn";
        }
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> productReport =
            (List<Map<String, Object>>) request.getAttribute("productReport");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> rateReport =
            (List<Map<String, Object>>) request.getAttribute("rateReport");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> statusReport =
            (List<Map<String, Object>>) request.getAttribute("statusReport");

    @SuppressWarnings("unchecked")
    List<Product> products = (List<Product>) request.getAttribute("products");

    String shopName = request.getAttribute("shopName") == null
            ? ""
            : request.getAttribute("shopName").toString();

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

    if (productReport != null) {
        for (Map<String, Object> row : productReport) {
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

        <a href="<%= contextPath %>/store-owner/dashboard">
            📊 Bảng điều khiển
        </a>

        <a href="<%= contextPath %>/store-owner/commission">
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
        <a href="<%= contextPath %>/store-owner/dashboard">
            ← Bảng điều khiển chủ cửa hàng
        </a>
    </p>

    <h1>Báo cáo hoa hồng</h1>

    <p class="dashboard-welcome">
        🏪 <%= esc(shopName) %>
        — phí nền tảng là <strong>hoa hồng&nbsp;%</strong>
        trên mọi đơn bán ra.
    </p>

    <p class="action-row">
        <a class="btn-secondary"
           href="<%= QueryUrl.keep(request, contextPath + "/store-owner/commission-report", "from", "to", "status") %>format=csv">
            ⬇️ Xuất CSV
        </a>
    </p>

    <%
        String section = request.getAttribute("section") == null
                ? "overview"
                : request.getAttribute("section").toString();

        String reportBase = contextPath + "/store-owner/commission-report";
        String secOverview = secUrl(reportBase, "overview", from, to, status);
        String secProducts = secUrl(reportBase, "products", from, to, status);
        String secSchedule = secUrl(reportBase, "schedule", from, to, status);
        String secRates    = secUrl(reportBase, "rates", from, to, status);
        String secStatus   = secUrl(reportBase, "status", from, to, status);
    %>

    <!-- Section navigation: one small page per report view -->

    <nav class="section-nav">

        <a class="<%= "overview".equals(section) ? "active" : "" %>"
           href="<%= secOverview %>">
            📊 Tổng quan
        </a>

        <a class="<%= "products".equals(section) ? "active" : "" %>"
           href="<%= secProducts %>">
            💼 Theo sản phẩm
        </a>

        <a class="<%= "schedule".equals(section) ? "active" : "" %>"
           href="<%= secSchedule %>">
            🏷️ Bảng phí
        </a>

        <a class="<%= "rates".equals(section) ? "active" : "" %>"
           href="<%= secRates %>">
            📈 Theo tỷ lệ
        </a>

        <a class="<%= "status".equals(section) ? "active" : "" %>"
           href="<%= secStatus %>">
            🚚 Theo trạng thái đơn
        </a>

    </nav>

    <!-- ==================== REPORT FILTERS ==================== -->

    <form method="get"
          action="<%= contextPath %>/store-owner/commission-report"
          class="filter-form">

        <input type="hidden" name="section" value="<%= esc(section) %>">

        <label for="from">Từ</label>

        <input class="filter-input" type="date" id="from"
               name="from" value="<%= esc(from) %>">

        <label for="to">Đến</label>

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
            <span class="summary-label">Phí nền tảng phải trả</span>
            <span class="summary-value"><%= money(totalFee) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Thu nhập của bạn</span>
            <span class="summary-value"><%= money(totalEarning) %></span>
        </div>

    </div>

    <% if (!hasSales) { %>

        <div class="success-message">
            ℹ️ Chưa có đơn bán nào — các số liệu bên dưới sẽ khác 0 ngay khi
            sản phẩm của bạn được đặt mua.
        </div>

    <% } %>

    <!-- ==================== FEE BREAKDOWN ==================== -->

    <div class="panel">

        <h2>🧾 Chi tiết phí</h2>

        <p class="panel-hint">
            Quyết toán cho khoảng thời gian đã chọn. Hoa hồng chỉ tính trên
            đơn <em>đã giao</em>; đơn đã hủy và đơn thất bại được tính là hoàn
            tiền. Dòng giảm giá so sánh giá bán thực tế với giá niêm yết hiện
            tại.
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
                        <td>Doanh số gộp (tất cả đơn trong kỳ)</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("gross_sales")) %></td>
                    </tr>
                    <tr>
                        <td>Giảm giá khuyến mãi (&minus;)</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("promotional_discount")) %></td>
                    </tr>
                    <tr>
                        <td>Hoàn tiền / đơn đã hủy</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("refunds")) %></td>
                    </tr>
                    <tr>
                        <td><strong>Doanh thu tính hoa hồng</strong></td>
                        <td><strong><%= money(totalRevenue) %></strong></td>
                    </tr>
                    <tr>
                        <td>Phí dịch vụ (<%= CommissionReportDAO.SERVICE_FEE_RATE.toPlainString() %> %)</td>
                        <td><%= money(serviceFee) %></td>
                    </tr>
                    <tr>
                        <td>Hoa hồng nền tảng (theo tỷ lệ sản phẩm)</td>
                        <td><%= money(totalFee) %></td>
                    </tr>
                    <tr>
                        <td><strong>Số tiền thực nhận của cửa hàng</strong></td>
                        <td><strong><%= money(netPayout) %></strong></td>
                    </tr>
                </tbody>

            </table>

        </div>

    </div>

    <% } else if ("products".equals(section)) { %>

    <!-- ==================== FEE BY PRODUCT ==================== -->

    <div class="panel">

        <h2>💼 Phí nền tảng theo sản phẩm</h2>

        <p class="panel-hint">
            Phí = doanh thu &times; hoa hồng %. Đây là số tiền cửa hàng bạn
            phải trả cho nền tảng.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Số lượng đã bán</th>
                        <th>Doanh thu</th>
                        <th>Hoa hồng</th>
                        <th>Phí nền tảng</th>
                        <th>Bạn nhận</th>
                    </tr>
                </thead>

                <tbody>
                <% if (productReport == null || productReport.isEmpty()) { %>

                    <tr>
                        <td colspan="6">
                            Cửa hàng của bạn chưa có sản phẩm nào.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : productReport) {
                %>
                    <tr>
                        <td><%= esc(row.get("product_name")) %></td>
                        <td><%= qty(row.get("sold_qty")) %></td>
                        <td><%= money(row.get("revenue")) %></td>
                        <td><%= percent(row.get("commission_rate")) %></td>
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
                        <td colspan="2">Tổng cộng</td>
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
            Cùng khoản phí, được nhóm theo tỷ lệ hoa hồng (%) mà quản trị viên
            đã đặt cho các sản phẩm.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Tỷ lệ hoa hồng</th>
                        <th>Số lượng đã bán</th>
                        <th>Doanh thu</th>
                        <th>Phí nền tảng</th>
                    </tr>
                </thead>

                <tbody>
                <% if (rateReport == null || rateReport.isEmpty()) { %>

                    <tr>
                        <td colspan="4">
                            Chưa có tỷ lệ hoa hồng nào để báo cáo.
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

    <% } else if ("status".equals(section)) { %>

    <!-- ==================== REVENUE BY ORDER STATUS ==================== -->

    <div class="panel">

        <h2>🚚 Phí theo trạng thái đơn</h2>

        <p class="panel-hint">
            Hoa hồng chỉ tính trên đơn <em>đã giao</em> — số tiền thực sự phải
            thanh toán. Các trạng thái khác được liệt kê ở đây để tham khảo;
            dùng bộ lọc trạng thái phía trên để xem riêng một trạng thái.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Trạng thái đơn</th>
                        <th>Số lượng đã bán</th>
                        <th>Doanh thu</th>
                        <th>Phí nền tảng</th>
                    </tr>
                </thead>

                <tbody>
                <% if (statusReport == null || statusReport.isEmpty()) { %>

                    <tr>
                        <td colspan="4">
                            Chưa có đơn hàng nào.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : statusReport) {
                %>
                    <tr>
                        <td>
                            <span class="pill <%= statusPill(row.get("status")) %>">
                                <%= esc(statusLabel(row.get("status"))) %>
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

    <% } else if ("schedule".equals(section)) { %>

    <!-- ==================== CURRENT FEE SCHEDULE ==================== -->

    <div class="panel">

        <h2>🏷️ Bảng phí hiện tại (giá niêm yết)</h2>

        <p class="panel-hint">
            Hoa hồng áp dụng cho giá niêm yết của từng sản phẩm, tính trên mỗi
            đơn vị.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>SKU</th>
                        <th>Đơn giá</th>
                        <th>Hoa hồng</th>
                        <th>Phí / đơn vị</th>
                        <th>Bạn nhận / đơn vị</th>
                    </tr>
                </thead>

                <tbody>
                <% if (products == null || products.isEmpty()) { %>

                    <tr>
                        <td colspan="6">
                            Cửa hàng của bạn chưa có sản phẩm nào.
                        </td>
                    </tr>

                <% } else {
                       for (Product p : products) {
                %>
                    <tr>
                        <td><%= esc(p.getProductName()) %></td>
                        <td><%= esc(p.getSku()) %></td>
                        <td><%= money(p.getPrice()) %></td>
                        <td><%= percent(p.getCommissionRate()) %></td>
                        <td><%= money(p.getPlatformCommission()) %></td>
                        <td><%= money(p.getShopEarning()) %></td>
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
