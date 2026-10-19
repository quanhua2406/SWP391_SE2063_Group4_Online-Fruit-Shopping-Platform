<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>

<%!
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
           href="<%= contextPath %>/admin/commission-report?format=csv">
            ⬇️ Export CSV
        </a>
    </p>

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

</section>

</body>
</html>
