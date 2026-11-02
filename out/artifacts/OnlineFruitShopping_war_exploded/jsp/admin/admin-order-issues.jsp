<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>

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

    private String pillClass(String status) {

        if ("failed".equals(status) || "cancelled".equals(status)) {
            return "pill-danger";
        }

        return "pill-warn";
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> orders = (Page<Map<String, Object>>) request.getAttribute("orders");

    String search = request.getAttribute("search") == null
            ? "" : request.getAttribute("search").toString();

    String msg = request.getAttribute("issueMessage") == null
            ? "" : request.getAttribute("issueMessage").toString();

    String pagerBase = request.getAttribute("pagerBase") == null
            ? contextPath + "/admin/order-issues?" : request.getAttribute("pagerBase").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Order Issues - FreshFruit Admin</title>

    <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/admin/dashboard">📊 Dashboard</a>

        <a href="<%= contextPath %>/admin/orders">🛒 Orders</a>

        <a href="<%= contextPath %>/admin/delivery-assign">🚚 Assignments</a>

        <a href="<%= contextPath %>/logout" class="logout">Logout</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">← Admin Dashboard</a>
    </p>

    <h1>Order Issues</h1>

    <p class="dashboard-welcome">
        Failed and cancelled orders, plus orders stuck in the same
        stage for more than three days. Reopen an order to push it
        back into processing, or cancel it to close the dispute.
    </p>

    <% if (msg != null && !msg.isEmpty()) { %>

        <% if ("reopened".equals(msg)) { %>
            <div class="success-message">✅ Order reopened — it is back in processing.</div>
        <% } else if ("cancelled".equals(msg)) { %>
            <div class="success-message">✅ Order cancelled.</div>
        <% } else if ("terminal".equals(msg)) { %>
            <div class="error-message">
                This order already reached a final state (delivered or cancelled)
                and cannot be changed.
            </div>
        <% } else { %>
            <div class="error-message">The requested action could not be applied.</div>
        <% } %>

    <% } %>

    <!-- ==================== FILTERS ==================== -->

    <form method="get" action="<%= contextPath %>/admin/order-issues" class="filter-form">

        <label for="search">Search</label>

        <input class="filter-input" type="text" id="search" name="search"
               placeholder="Order id, customer, email, phone, shop"
               value="<%= esc(search) %>">

        <button type="submit" class="btn-sm">Apply</button>

        <a class="filter-clear" href="<%= contextPath %>/admin/order-issues">Clear</a>

    </form>

    <!-- ==================== ISSUE TABLE ==================== -->

    <div class="panel">

        <h2>🛠️ Orders needing attention</h2>

        <p class="panel-hint">
            <%= orders == null ? 0 : orders.getTotal() %> order(s) flagged.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Order</th>
                        <th>Customer</th>
                        <th>Shop</th>
                        <th>Total</th>
                        <th>Status</th>
                        <th>Age</th>
                        <th>Issue</th>
                        <th>Handle</th>
                    </tr>
                </thead>

                <tbody>
                <% if (orders == null || orders.isEmpty()) { %>

                    <tr>
                        <td colspan="8">
                            No order needs attention — nothing failed, cancelled
                            or delayed.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : orders.getItems()) {
                           String status = row.get("status") == null
                                   ? "" : row.get("status").toString();
                           boolean closable = !"cancelled".equals(status);
                %>
                    <tr>
                        <td>#<%= esc(row.get("order_id")) %></td>

                        <td>
                            <%= esc(row.get("customer_name")) %>
                            <br><small><%= esc(row.get("customer_email")) %></small>
                        </td>

                        <td><%= esc(row.get("shop_name")) %></td>

                        <td><%= money(row.get("total_amount")) %></td>

                        <td>
                            <span class="pill <%= pillClass(status) %>">
                                <%= esc(status) %>
                            </span>
                        </td>

                        <td><%= esc(row.get("age_days")) %> day(s)</td>

                        <td><%= esc(row.get("issue_type")) %></td>

                        <td>
                            <form method="post"
                                  action="<%= contextPath %>/admin/order-issues"
                                  class="inline-form">

                                <input type="hidden" name="orderId"
                                       value="<%= esc(row.get("order_id")) %>">

                                <input type="text" name="note" class="filter-input"
                                       maxlength="40"
                                       placeholder="Note for the history">

                                <button type="submit" class="btn-sm"
                                        name="resolution" value="reopen">
                                    Reopen
                                </button>

                                <% if (closable) { %>
                                    <button type="submit" class="btn-sm-danger"
                                            name="resolution" value="cancel">
                                        Cancel order
                                    </button>
                                <% } %>

                            </form>

                            <a class="btn-sm-info"
                               href="<%= contextPath %>/admin/order-detail?orderId=<%= esc(row.get("order_id")) %>">
                                Details
                            </a>
                        </td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

            </table>

        </div>

        <%= Pager.render(orders, pagerBase, "page") %>

    </div>

</section>

</body>
</html>
