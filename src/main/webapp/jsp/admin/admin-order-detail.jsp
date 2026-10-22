<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.model.OrderItem"%>
<%@page import="com.mycompany.onlinefruitshopping.model.OrderStatusHistory"%>

<%!
    private String pillClass(String status) {

        if ("delivered".equals(status)) {
            return "pill-ok";
        }

        if ("cancelled".equals(status) || "failed".equals(status)) {
            return "pill-danger";
        }

        if ("pending_confirmation".equals(status)) {
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
    Map<String, Object> order = (Map<String, Object>) request.getAttribute("order");

    @SuppressWarnings("unchecked")
    List<OrderItem> items = (List<OrderItem>) request.getAttribute("items");

    @SuppressWarnings("unchecked")
    List<OrderStatusHistory> history = (List<OrderStatusHistory>) request.getAttribute("history");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> payments = (List<Map<String, Object>>) request.getAttribute("payments");

    @SuppressWarnings("unchecked")
    Map<String, Object> delivery = (Map<String, Object>) request.getAttribute("delivery");

    String status = order.get("status") == null ? "" : order.get("status").toString();

    StringBuilder address = new StringBuilder();

    for (String key : new String[]{"address_line", "ward", "district", "province"}) {
        Object part = order.get(key);

        if (part != null && !part.toString().isBlank()) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(part.toString().trim());
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Order #<%= esc(order.get("order_id")) %> - FreshFruit Admin</title>

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

        <a href="<%= contextPath %>/admin/deliveries">🚚 Deliveries</a>

        <a href="<%= contextPath %>/logout" class="logout">Logout</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/orders">← Order Monitoring</a>
    </p>

    <h1>Order #<%= esc(order.get("order_id")) %></h1>

    <p class="dashboard-welcome">
        Placed on <%= esc(order.get("created_at")) %>
        &nbsp;•&nbsp;
        <span class="pill <%= pillClass(status) %>"><%= esc(status) %></span>
    </p>

    <!-- ==================== SUMMARY ==================== -->

    <div class="summary-grid">

        <div class="summary-card">
            <span class="summary-label">Customer</span>
            <span class="summary-value"><%= esc(order.get("customer_name")) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Shop</span>
            <span class="summary-value"><%= esc(order.get("shop_name")) %></span>
        </div>

        <div class="summary-card summary-accent">
            <span class="summary-label">Total</span>
            <span class="summary-value"><%= money(order.get("total_amount")) %></span>
        </div>

    </div>

    <!-- ==================== CUSTOMER & DELIVERY ==================== -->

    <div class="panel">

        <h2>👤 Customer &amp; Delivery</h2>

        <table class="data-table">
            <tbody>
                <tr>
                    <th>Customer</th>
                    <td>
                        <%= esc(order.get("customer_name")) %>
                        (<%= esc(order.get("customer_email")) %>)
                    </td>
                </tr>
                <tr>
                    <th>Contact phone</th>
                    <td><%= esc(order.get("customer_phone")) %></td>
                </tr>
                <tr>
                    <th>Recipient</th>
                    <td>
                        <%= esc(order.get("recipient_name")) %>
                        <%= order.get("recipient_phone") == null
                                ? "" : " — " + esc(order.get("recipient_phone")) %>
                    </td>
                </tr>
                <tr>
                    <th>Delivery address</th>
                    <td><%= address.length() == 0 ? "—" : esc(address.toString()) %></td>
                </tr>
                <tr>
                    <th>Shipper</th>
                    <td>
                        <% if (delivery == null) { %>
                            <span class="pill pill-muted">Not assigned</span>
                        <% } else { %>
                            <%= esc(delivery.get("staff_name")) %>
                            (<%= esc(delivery.get("staff_phone")) %>)
                            <span class="pill <%= pillClass(delivery.get("status") == null
                                    ? "" : delivery.get("status").toString()) %>">
                                <%= esc(delivery.get("status")) %>
                            </span>
                        <% } %>
                    </td>
                </tr>
            </tbody>
        </table>

    </div>

    <!-- ==================== ITEMS ==================== -->

    <div class="panel">

        <h2>📦 Items</h2>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Packaging</th>
                        <th>Unit price</th>
                        <th>Quantity</th>
                        <th>Line total</th>
                    </tr>
                </thead>

                <tbody>
                <% if (items == null || items.isEmpty()) { %>

                    <tr>
                        <td colspan="5">This order has no line items.</td>
                    </tr>

                <% } else {
                       for (OrderItem item : items) {
                %>
                    <tr>
                        <td>
                            <% if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) { %>
                                <img src="<%= esc(item.getImageUrl()) %>"
                                     alt="<%= esc(item.getProductName()) %>"
                                     style="width: 42px; height: 42px; object-fit: cover;
                                            border-radius: 8px; vertical-align: middle;
                                            margin-right: 8px;">
                            <% } %>
                            <%= esc(item.getProductName()) %>
                        </td>
                        <td>
                            <%= esc(item.getPackagingType()) %>
                            <%= item.getWeightValue() > 0
                                    ? " (" + qty(item.getWeightValue()) + " " + esc(item.getUnit()) + ")"
                                    : "" %>
                        </td>
                        <td><%= money(item.getUnitPrice()) %></td>
                        <td><%= qty(item.getQuantity()) %></td>
                        <td><%= money(item.getUnitPrice() * item.getQuantity()) %></td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

                <tfoot>
                    <tr>
                        <td colspan="4">Order total</td>
                        <td><strong><%= money(order.get("total_amount")) %></strong></td>
                    </tr>
                </tfoot>

            </table>

        </div>

    </div>

    <!-- ==================== PAYMENTS ==================== -->

    <div class="panel">

        <h2>💳 Payments</h2>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>#</th>
                        <th>Method</th>
                        <th>Amount</th>
                        <th>Status</th>
                        <th>Reference</th>
                        <th>Paid at</th>
                    </tr>
                </thead>

                <tbody>
                <% if (payments == null || payments.isEmpty()) { %>

                    <tr>
                        <td colspan="6">No payment recorded for this order yet.</td>
                    </tr>

                <% } else {
                       for (Map<String, Object> pay : payments) {
                %>
                    <tr>
                        <td>#<%= esc(pay.get("payment_id")) %></td>
                        <td><%= esc(pay.get("method")) %></td>
                        <td><%= money(pay.get("amount")) %></td>
                        <td><%= esc(pay.get("status")) %></td>
                        <td><%= esc(pay.get("transaction_ref")) %></td>
                        <td><%= esc(pay.get("paid_at")) %></td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

            </table>

        </div>

    </div>

    <!-- ==================== STATUS HISTORY ==================== -->

    <div class="panel">

        <h2>🕘 Status History</h2>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>When</th>
                        <th>Status</th>
                        <th>Changed by</th>
                    </tr>
                </thead>

                <tbody>
                <% if (history == null || history.isEmpty()) { %>

                    <tr>
                        <td colspan="3">No status changes recorded.</td>
                    </tr>

                <% } else {
                       for (OrderStatusHistory entry : history) {
                %>
                    <tr>
                        <td><%= esc(entry.getChangedAt()) %></td>
                        <td><%= esc(entry.getStatus()) %></td>
                        <td>
                            <%= entry.getChangedByName() == null
                                    ? "#" + entry.getChangedBy()
                                    : esc(entry.getChangedByName()) %>
                        </td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

            </table>

        </div>

    </div>

    <p class="action-row">

        <a class="btn-secondary"
           href="<%= contextPath %>/admin/delivery-assign?search=<%= esc(order.get("order_id")) %>">
            🚚 Manage assignment
        </a>

        <a class="btn-secondary"
           href="<%= contextPath %>/admin/order-issues?search=<%= esc(order.get("order_id")) %>">
            🛠️ Handle as issue
        </a>

    </p>

</section>

</body>
</html>
