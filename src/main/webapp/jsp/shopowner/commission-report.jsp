<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.model.Product"%>

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

    private String statusLabel(Object value) {
        if (value == null) {
            return "";
        }

        switch (value.toString().toLowerCase()) {
            case "pending_confirmation":
                return "Pending confirmation";
            case "preparing":
                return "Preparing";
            case "shipping":
                return "Shipping";
            case "delivered":
                return "Delivered";
            case "cancelled":
                return "Cancelled";
            case "failed":
                return "Failed";
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

        <a href="<%= contextPath %>/store-owner/dashboard">
            📊 Dashboard
        </a>

        <a href="<%= contextPath %>/store-owner/products">
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
        <a href="<%= contextPath %>/store-owner/dashboard">
            ← Shop Owner Dashboard
        </a>
    </p>

    <h1>Commission Report</h1>

    <p class="dashboard-welcome">
        🏪 <%= esc(shopName) %>
        — the platform fee is <strong>commission&nbsp;%</strong>
        of every sale.
    </p>

    <p class="action-row">
        <a class="btn-primary"
           href="<%= contextPath %>/store-owner/commission-report?format=csv">
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
            <span class="summary-label">Platform Fee To Pay</span>
            <span class="summary-value"><%= money(totalFee) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Your Earnings</span>
            <span class="summary-value"><%= money(totalEarning) %></span>
        </div>

    </div>

    <% if (!hasSales) { %>

        <div class="success-message">
            ℹ️ No sales yet — the figures below become non-zero as
            soon as your products are ordered.
        </div>

    <% } %>

    <!-- ==================== FEE BY PRODUCT ==================== -->

    <div class="panel">

        <h2>💼 Platform Fee by Product</h2>

        <p class="panel-hint">
            Fee = revenue &times; commission %. This is the amount
            your shop has to pay the platform.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Quantity Sold</th>
                        <th>Revenue</th>
                        <th>Commission</th>
                        <th>Platform Fee</th>
                        <th>You Receive</th>
                    </tr>
                </thead>

                <tbody>
                <% if (productReport == null || productReport.isEmpty()) { %>

                    <tr>
                        <td colspan="6">
                            No products in your shop yet.
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
                        <td colspan="2">Total</td>
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
            The same fee, grouped by the commission percentage the
            admin set for the products.
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

    <!-- ==================== REVENUE BY ORDER STATUS ==================== -->

    <div class="panel">

        <h2>🚚 Fee by Order Status</h2>

        <p class="panel-hint">
            Fee counts every order except
            <em>cancelled</em>/<em>failed</em>. Delivered orders are
            the money actually due; the other statuses are still
            being processed.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Order Status</th>
                        <th>Quantity Sold</th>
                        <th>Revenue</th>
                        <th>Platform Fee</th>
                    </tr>
                </thead>

                <tbody>
                <% if (statusReport == null || statusReport.isEmpty()) { %>

                    <tr>
                        <td colspan="4">
                            No orders yet.
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

    <!-- ==================== CURRENT FEE SCHEDULE ==================== -->

    <div class="panel">

        <h2>🏷️ Current Fee Schedule (listed price)</h2>

        <p class="panel-hint">
            The commission applied to each product's listed price,
            per unit.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Product</th>
                        <th>SKU</th>
                        <th>Unit Price</th>
                        <th>Commission</th>
                        <th>Fee / Unit</th>
                        <th>You Receive / Unit</th>
                    </tr>
                </thead>

                <tbody>
                <% if (products == null || products.isEmpty()) { %>

                    <tr>
                        <td colspan="6">
                            No products in your shop yet.
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

</section>

</body>
</html>
