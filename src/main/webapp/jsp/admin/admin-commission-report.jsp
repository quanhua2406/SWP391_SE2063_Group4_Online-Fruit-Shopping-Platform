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
            .append(">Delivered (fee basis)</option>");

        String[][] statuses = {
            {"pending_confirmation", "Pending confirmation"},
            {"preparing", "Preparing"},
            {"shipping", "Shipping"},
            {"delivered", "Delivered"},
            {"cancelled", "Cancelled"},
            {"failed", "Failed"}
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

    <title>Commission Report - FreshFruit</title>

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
            📊 Dashboard
        </a>

        <a href="<%= contextPath %>/admin/product-management">
            🍎 Products
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Logout
        </a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">
            ← Admin Dashboard
        </a>
    </p>

    <h1>Commission Report</h1>

    <p class="dashboard-welcome">
        Platform fee collected from shop owners, calculated as the
        commission&nbsp;% of each sale.
    </p>

    <p class="action-row">
        <a class="btn-primary"
           href="<%= QueryUrl.keep(request, contextPath + "/admin/commission-report", "from", "to", "status") %>format=csv">
            ⬇️ Export CSV
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
            📊 Overview
        </a>

        <a class="<%= "shops".equals(section) ? "active" : "" %>"
           href="<%= secShops %>">
            🏪 By Shop
        </a>

        <a class="<%= "rates".equals(section) ? "active" : "" %>"
           href="<%= secRates %>">
            📈 By Rate
        </a>

    </nav>

    <!-- ==================== REPORT FILTERS ==================== -->

    <form method="get"
          action="<%= contextPath %>/admin/commission-report"
          class="filter-form">

        <input type="hidden" name="section" value="<%= esc(section) %>">

        <label for="from">From</label>

        <input class="filter-input" type="date" id="from"
               name="from" value="<%= esc(from) %>">

        <label for="to">To</label>

        <input class="filter-input" type="date" id="to"
               name="to" value="<%= esc(to) %>">

        <label for="status">Order Status</label>

        <select class="filter-input" id="status" name="status">
            <%= statusOptions(status) %>
        </select>

        <button type="submit" class="btn-sm">Apply</button>

        <a class="filter-clear"
           href="<%= secUrl(reportBase, section, "", "", "") %>">Clear</a>

    </form>

    <% if ("overview".equals(section)) { %>

    <!-- ==================== SUMMARY ==================== -->

    <div class="summary-grid">

        <div class="summary-card">
            <span class="summary-label">Total Revenue</span>
            <span class="summary-value"><%= money(totalRevenue) %></span>
        </div>

        <div class="summary-card summary-accent">
            <span class="summary-label">Platform Fee Collected</span>
            <span class="summary-value"><%= money(totalFee) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Paid To Shops</span>
            <span class="summary-value"><%= money(totalEarning) %></span>
        </div>

    </div>

    <% if (!hasSales) { %>

        <div class="success-message">
            ℹ️ No sales yet — the figures below become non-zero as
            soon as shop products are ordered.
        </div>

    <% } %>

    <!-- ==================== FEE BREAKDOWN ==================== -->

    <div class="panel">

        <h2>🧾 Fee Breakdown</h2>

        <p class="panel-hint">
            Platform settlement for the selected period, across all
            shops. Commission is counted on <em>delivered</em>
            orders only; cancelled and failed orders are reported
            as refunds.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Line</th>
                        <th>Amount</th>
                    </tr>
                </thead>

                <tbody>
                    <tr>
                        <td>Gross sales (all orders in period)</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("gross_sales")) %></td>
                    </tr>
                    <tr>
                        <td>Promotional discount (&minus;)</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("promotional_discount")) %></td>
                    </tr>
                    <tr>
                        <td>Refunded / cancelled orders</td>
                        <td><%= money(breakdown == null ? null : breakdown.get("refunds")) %></td>
                    </tr>
                    <tr>
                        <td><strong>Paid revenue (commission basis)</strong></td>
                        <td><strong><%= money(totalRevenue) %></strong></td>
                    </tr>
                    <tr>
                        <td>Service fee (<%= CommissionReportDAO.SERVICE_FEE_RATE.toPlainString() %> %)</td>
                        <td><%= money(serviceFee) %></td>
                    </tr>
                    <tr>
                        <td>Platform commission (per product rate)</td>
                        <td><%= money(totalFee) %></td>
                    </tr>
                    <tr>
                        <td><strong>Paid to shops (net)</strong></td>
                        <td><strong><%= money(netPayout) %></strong></td>
                    </tr>
                </tbody>

            </table>

        </div>

    </div>

    <% } else if ("shops".equals(section)) { %>

    <!-- ==================== FEE BY SHOP ==================== -->

    <div class="panel">

        <h2>🏪 Platform Fee by Shop</h2>

        <p class="panel-hint">
            How much fee the platform collects from each shop.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Shop</th>
                        <th>Products</th>
                        <th>Quantity Sold</th>
                        <th>Revenue</th>
                        <th>Avg. Commission</th>
                        <th>Platform Fee</th>
                        <th>Paid To Shop</th>
                    </tr>
                </thead>

                <tbody>
                <% if (shopReport == null || shopReport.isEmpty()) { %>

                    <tr>
                        <td colspan="7">
                            No shops to report yet.
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
                        <td colspan="3">Total</td>
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

        <h2>📊 Platform Fee by Commission Rate</h2>

        <p class="panel-hint">
            Fee collected, grouped by the commission percentage set
            for the products.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Commission Rate</th>
                        <th>Quantity Sold</th>
                        <th>Revenue</th>
                        <th>Platform Fee</th>
                    </tr>
                </thead>

                <tbody>
                <% if (rateReport == null || rateReport.isEmpty()) { %>

                    <tr>
                        <td colspan="4">
                            No commission rates to report yet.
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
