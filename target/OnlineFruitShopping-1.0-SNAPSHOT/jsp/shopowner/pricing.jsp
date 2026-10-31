<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.VariantWeights"%>

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

    private BigDecimal dec(Object value) {
        if (value == null) {
            return null;
        }

        return value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());
    }

    private String qty(Object value) {
        BigDecimal number = dec(value);

        return number == null
                ? "0"
                : number.stripTrailingZeros().toPlainString();
    }

    private String money(Object value) {
        BigDecimal number = dec(value);

        return number == null
                ? "0 \u0111"
                : String.format(Locale.US, "%,.0f", number) + " \u0111";
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> variantsPage =
            (Page<Map<String, Object>>) request.getAttribute("variantsPage");

    String pagerUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/pricing", "q", "weight");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String weight = request.getParameter("weight") == null ? "" : request.getParameter("weight");

    @SuppressWarnings("unchecked")
    java.util.List<String> weightOptions =
            (java.util.List<String>) request.getAttribute("weightOptions");

    String currentPage = request.getParameter("page") == null
            ? "1"
            : request.getParameter("page");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    if ("updated".equals(message)) {
        messageText = "Price updated successfully.";
    } else if ("invalid".equals(message)) {
        level = "error-message";
        messageText = "Please enter a valid price.";
    } else if ("promo-conflict".equals(message)) {
        level = "error-message";
        messageText = "This price is not higher than the running promotional price. "
                + "Update or remove that promotion first.";
    } else if ("forbidden".equals(message)) {
        level = "error-message";
        messageText = "You are not allowed to change this price.";
    } else if ("failed".equals(message)) {
        level = "error-message";
        messageText = "The price could not be updated.";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Pricing Management - FreshFruit</title>

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

        <a href="<%= contextPath %>/store-owner/inventory">
            📦 Inventory
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

    <h1>Pricing Management</h1>

    <p class="dashboard-welcome">
        Set or edit the normal selling price of every variant in your
        shop. Temporary offers are managed in
        <a href="<%= contextPath %>/store-owner/promotions">
            Promotion Management
        </a>.
    </p>

    <% if (messageText != null) { %>

        <div class="<%= level %>">
            <%= esc(messageText) %>
        </div>

    <% } %>

    <form method="get"
          action="<%= contextPath %>/store-owner/pricing"
          class="filter-form">

        <input type="text" class="filter-input" name="q"
               value="<%= esc(q) %>"
               placeholder="Search product or SKU...">

        <select class="filter-select" name="weight">
            <option value="">All weights</option>
            <% if (weightOptions != null) {
                   for (String w : weightOptions) { %>
                <option value="<%= esc(w) %>"
                    <%= w.equals(weight) ? "selected" : "" %>>
                    <%= esc(w) %> kg
                </option>
            <%     }
               } %>
        </select>

        <button type="submit" class="btn-sm">Filter</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/pricing">Clear</a>

    </form>

    <!-- ==================== SET & EDIT PRICE ==================== -->

    <div class="panel" id="price">

        <h2>💲 Set &amp; Edit Price by Variant</h2>

        <p class="panel-hint">
            Prices are per package. A variant is one selling package
            (0.5 kg, 1 kg, 2 kg, 5 kg ...).
        </p>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Weight</th>
                    <th>Packaging</th>
                    <th>Current Price</th>
                    <th>Stock</th>
                    <th>New Price (đ)</th>
                </tr>
            </thead>

            <tbody>
            <% if (variantsPage == null || variantsPage.isEmpty()) { %>

                <tr>
                    <td colspan="7">
                        No product variants match your search.
                    </td>
                </tr>

            <% } else {
                   for (Map<String, Object> v : variantsPage.getItems()) {
            %>
                <tr>
                    <td><%= esc(v.get("product_name")) %></td>
                    <td><%= esc(v.get("sku")) %></td>
                    <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                    <td><%= esc(v.get("packaging_type")) %></td>
                    <td><%= money(v.get("price")) %></td>
                    <td><%= qty(v.get("stock")) %></td>
                    <td>
                        <form method="post"
                              action="<%= contextPath %>/store-owner/pricing">

                            <input type="hidden" name="variantId"
                                   value="<%= esc(v.get("variant_id")) %>">

                            <input type="hidden" name="q"
                                   value="<%= esc(q) %>">

                            <input type="hidden" name="weight"
                                   value="<%= esc(weight) %>">

                            <input type="hidden" name="page"
                                   value="<%= esc(currentPage) %>">

                            <input class="qty-input" type="number"
                                   name="price" min="0" step="1000"
                                   value="<%= qty(v.get("price")) %>"
                                   required>

                            <button type="submit" class="btn-sm">
                                Save
                            </button>

                        </form>
                    </td>
                </tr>
            <%
                   }
               }
            %>
            </tbody>

        </table>

        <%= Pager.render(variantsPage, pagerUrl, "page") %>

    </div>

</section>

</body>
</html>
