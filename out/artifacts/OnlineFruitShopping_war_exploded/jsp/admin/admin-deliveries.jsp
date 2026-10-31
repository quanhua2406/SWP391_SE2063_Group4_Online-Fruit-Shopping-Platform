<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>

<%!
    private static final String[][] DELIVERY_STATUSES = {
        {"assigned", "Assigned"},
        {"picked_up", "Picked up"},
        {"delivering", "Delivering"},
        {"delivered", "Delivered"},
        {"failed", "Failed"}
    };

    private String statusLabel(String status) {

        for (String[] s : DELIVERY_STATUSES) {
            if (s[0].equals(status)) {
                return s[1];
            }
        }

        return status == null ? "" : status;
    }

    private String pillClass(String status) {

        if ("delivered".equals(status)) {
            return "pill-ok";
        }

        if ("failed".equals(status)) {
            return "pill-danger";
        }

        if ("assigned".equals(status)) {
            return "pill-muted";
        }

        return "pill-warn";
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

    private String address(Map<String, Object> row) {

        StringBuilder text = new StringBuilder();

        for (String key : new String[]{"address_line", "ward", "district", "province"}) {
            Object part = row.get(key);

            if (part != null && !part.toString().isBlank()) {
                if (text.length() > 0) {
                    text.append(", ");
                }
                text.append(part.toString().trim());
            }
        }

        return text.length() == 0 ? "—" : text.toString();
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> deliveries = (Page<Map<String, Object>>) request.getAttribute("deliveries");

    @SuppressWarnings("unchecked")
    Map<String, Integer> statusCounts = (Map<String, Integer>) request.getAttribute("statusCounts");

    String status = request.getAttribute("status") == null
            ? "" : request.getAttribute("status").toString();

    String search = request.getAttribute("search") == null
            ? "" : request.getAttribute("search").toString();

    String pagerBase = request.getAttribute("pagerBase") == null
            ? contextPath + "/admin/deliveries?" : request.getAttribute("pagerBase").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Deliveries - FreshFruit Admin</title>

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

        <a href="<%= contextPath %>/admin/delivery-staff">🧑‍✈️ Delivery Staff</a>

        <a href="<%= contextPath %>/logout" class="logout">Logout</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">← Admin Dashboard</a>
    </p>

    <h1>Delivery Monitoring</h1>

    <p class="dashboard-welcome">
        Delivery progress across the platform and the shipper
        assigned to each order.
    </p>

    <!-- ==================== STATUS COUNTS ==================== -->

    <div class="summary-grid">

        <% for (String[] s : DELIVERY_STATUSES) {
               Integer count = statusCounts == null ? null : statusCounts.get(s[0]);
               int value = count == null ? 0 : count;
        %>
        <a class="summary-card<%= "delivered".equals(s[0]) ? " summary-accent" : "" %>"
           href="<%= contextPath %>/admin/deliveries?status=<%= s[0] %>">
            <span class="summary-label"><%= s[1] %></span>
            <span class="summary-value"><%= value %></span>
        </a>
        <% } %>

    </div>

    <!-- ==================== FILTERS ==================== -->

    <form method="get" action="<%= contextPath %>/admin/deliveries" class="filter-form">

        <label for="search">Search</label>

        <input class="filter-input" type="text" id="search" name="search"
               placeholder="Delivery id, order id, shipper, customer, shop"
               value="<%= esc(search) %>">

        <label for="status">Status</label>

        <select class="filter-input" id="status" name="status">
            <option value=""<%= status.isEmpty() ? " selected" : "" %>>All statuses</option>
            <% for (String[] s : DELIVERY_STATUSES) { %>
                <option value="<%= s[0] %>"<%= s[0].equals(status) ? " selected" : "" %>>
                    <%= s[1] %>
                </option>
            <% } %>
        </select>

        <button type="submit" class="btn-sm">Apply</button>

        <a class="filter-clear" href="<%= contextPath %>/admin/deliveries">Clear</a>

    </form>

    <!-- ==================== DELIVERY TABLE ==================== -->

    <div class="panel">

        <h2>🚚 Deliveries</h2>

        <p class="panel-hint">
            <%= deliveries == null ? 0 : deliveries.getTotal() %> delivery record(s) matched.
            Assignments are created by the shop owner; use
            <a href="<%= contextPath %>/admin/delivery-assign">Manage Assignments</a>
            to step in.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Delivery</th>
                        <th>Order</th>
                        <th>Shipper</th>
                        <th>Customer</th>
                        <th>Address</th>
                        <th>Status</th>
                        <th>Note</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                <% if (deliveries == null || deliveries.isEmpty()) { %>

                    <tr>
                        <td colspan="8">
                            No deliveries yet. A delivery appears here once an
                            order has shipping status and a shipper assigned.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : deliveries.getItems()) {
                           Object st = row.get("status");
                           String statusText = st == null ? "" : st.toString();
                %>
                    <tr>
                        <td>#<%= esc(row.get("delivery_id")) %></td>

                        <td>
                            #<%= esc(row.get("order_id")) %>
                            <br><small><%= money(row.get("total_amount")) %></small>
                        </td>

                        <td>
                            <%= esc(row.get("staff_name")) %>
                            <br><small><%= esc(row.get("staff_phone")) %></small>
                        </td>

                        <td><%= esc(row.get("customer_name")) %></td>

                        <td>
                            <%= esc(address(row)) %>
                            <br><small><%= esc(row.get("shop_name")) %></small>
                        </td>

                        <td>
                            <span class="pill <%= pillClass(statusText) %>">
                                <%= statusLabel(statusText) %>
                            </span>
                        </td>

                        <td><%= esc(row.get("note")) %></td>

                        <td>
                            <a class="btn-sm-info"
                               href="<%= contextPath %>/admin/order-detail?orderId=<%= esc(row.get("order_id")) %>">
                                Order
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

        <%= Pager.render(deliveries, pagerBase, "page") %>

    </div>

</section>

</body>
</html>
