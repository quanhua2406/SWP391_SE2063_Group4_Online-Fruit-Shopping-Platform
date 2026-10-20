<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.model.Product"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>

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

    private String money(double value) {
        return String.format(Locale.US, "%,.0f", value) + " \u0111";
    }

    private String percent(double value) {
        return String.format(Locale.US, "%,.2f", value) + " %";
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    Page<Product> productsPage =
            (Page<Product>) request.getAttribute("productsPage");

    String pageUrl = contextPath + "/store-owner/commission?";

    String pagerUrl = QueryUrl.keep(request, pageUrl, "q", "status");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String status = request.getParameter("status") == null ? "" : request.getParameter("status");

    String shopName = request.getAttribute("shopName") == null
            ? ""
            : request.getAttribute("shopName").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Product Management - FreshFruit</title>

    <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">ðŸŠ FreshFruit</div>

    <nav class="nav-actions">
        <a href="<%= contextPath %>/store-owner/dashboard">ðŸ“Š Dashboard</a>
        <a href="<%= contextPath %>/store-owner/inventory">ðŸ“¦ Stock</a>
        <a href="<%= contextPath %>/store-owner/pricing">ðŸ’² Pricing</a>
        <a href="<%= contextPath %>/store-owner/commission-report">ðŸ’¼ Report</a>
        <a href="<%= contextPath %>/logout" class="logout">Logout</a>
    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/store-owner/dashboard">
            â† Shop Owner Dashboard
        </a>
    </p>

    <h1>Product Management</h1>

    <p class="dashboard-welcome">
        ðŸª <%= esc(shopName) %>
        â€” the commission rate is set by the platform admin.
    </p>

    <div class="panel">

        <h2>ðŸ’¼ My Products &amp; Commission</h2>

        <p class="panel-hint">
            "Platform fee" is the amount the platform keeps on each
            unit sold; "You receive" is what is left for your shop.
        </p>

        <form method="get"
              action="<%= contextPath %>/store-owner/commission"
              class="filter-form">

            <input type="text" class="filter-input" name="q"
                   value="<%= esc(q) %>"
                   placeholder="Search product or SKU...">

            <select class="filter-select" name="status">
                <option value="">All statuses</option>
                <option value="published" <%= "published".equals(status) ? "selected" : "" %>>published</option>
                <option value="draft" <%= "draft".equals(status) ? "selected" : "" %>>draft</option>
                <option value="hidden" <%= "hidden".equals(status) ? "selected" : "" %>>hidden</option>
            </select>

            <button type="submit" class="btn-sm">Filter</button>

            <a class="filter-clear"
               href="<%= contextPath %>/store-owner/commission">Clear</a>

        </form>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Status</th>
                    <th>Unit Price</th>
                    <th>Commission</th>
                    <th>Platform Fee / Unit</th>
                    <th>You Receive / Unit</th>
                </tr>
            </thead>

            <tbody>
            <% if (productsPage == null || productsPage.isEmpty()) { %>

                <tr>
                    <td colspan="7">
                        No products in your shop yet. Products are
                        added by the platform admin.
                    </td>
                </tr>

            <% } else {
                   for (Product p : productsPage.getItems()) {
            %>
                <tr>
                    <td><%= esc(p.getProductName()) %></td>
                    <td><%= esc(p.getSku()) %></td>
                    <td>
                        <span class="pill <%= "published".equals(p.getStatus())
                                ? "pill-ok"
                                : ("hidden".equals(p.getStatus())
                                        ? "pill-danger" : "pill-warn") %>">
                            <%= esc(p.getStatus()) %>
                        </span>
                    </td>
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

        <%= Pager.render(productsPage, pagerUrl, "page") %>

    </div>

</section>

</body>
</html>
