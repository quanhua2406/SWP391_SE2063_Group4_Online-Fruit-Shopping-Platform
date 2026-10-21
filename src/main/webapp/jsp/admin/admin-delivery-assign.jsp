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

    private String staffOptions(List<Map<String, Object>> staff, Object selectedId) {

        StringBuilder html = new StringBuilder();

        html.append("<option value=\"\">— select a shipper —</option>");

        if (staff != null) {
            for (Map<String, Object> s : staff) {
                Object id = s.get("user_id");
                boolean selected = selectedId != null
                        && id != null
                        && selectedId.toString().equals(id.toString());

                String label = s.get("full_name") + " ("
                        + (s.get("active_deliveries") == null ? "0" : s.get("active_deliveries"))
                        + " active)"
                        + ("active".equals(s.get("status")) ? "" : " — locked");

                html.append("<option value=\"").append(esc(id)).append("\"")
                    .append(selected ? " selected" : "")
                    .append(">").append(esc(label)).append("</option>");
            }
        }

        return html.toString();
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> orders = (Page<Map<String, Object>>) request.getAttribute("orders");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> staff = (List<Map<String, Object>>) request.getAttribute("staff");

    String search = request.getAttribute("search") == null
            ? "" : request.getAttribute("search").toString();

    String msg = request.getAttribute("assignMessage") == null
            ? "" : request.getAttribute("assignMessage").toString();

    String pagerBase = request.getAttribute("pagerBase") == null
            ? contextPath + "/admin/delivery-assign?" : request.getAttribute("pagerBase").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Manage Assignments - FreshFruit Admin</title>

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
        <a href="<%= contextPath %>/admin/dashboard">← Admin Dashboard</a>
    </p>

    <h1>Manage Delivery Assignments</h1>

    <p class="dashboard-welcome">
        Shop owners assign a shipper when they prepare an order.
        Use this screen to reassign, cancel or add an assignment
        when something goes wrong.
    </p>

    <% if (msg != null && !msg.isEmpty()) { %>

        <% if ("assigned".equals(msg)) { %>
            <div class="success-message">✅ Shipper assigned to the order.</div>
        <% } else if ("reassigned".equals(msg)) { %>
            <div class="success-message">✅ Delivery reassigned to the new shipper.</div>
        <% } else if ("unassigned".equals(msg)) { %>
            <div class="success-message">✅ Assignment cancelled — the order is back in the queue.</div>
        <% } else if ("conflict".equals(msg)) { %>
            <div class="error-message">
                The order already has a delivery, or it can no longer be assigned.
            </div>
        <% } else { %>
            <div class="error-message">The requested action could not be applied.</div>
        <% } %>

    <% } %>

    <!-- ==================== FILTERS ==================== -->

    <form method="get" action="<%= contextPath %>/admin/delivery-assign" class="filter-form">

        <label for="search">Search</label>

        <input class="filter-input" type="text" id="search" name="search"
               placeholder="Order id, customer, email, phone, shop"
               value="<%= esc(search) %>">

        <button type="submit" class="btn-sm">Apply</button>

        <a class="filter-clear" href="<%= contextPath %>/admin/delivery-assign">Clear</a>

    </form>

    <!-- ==================== ASSIGNABLE ORDERS ==================== -->

    <div class="panel">

        <h2>🚚 Open orders</h2>

        <p class="panel-hint">
            <%= orders == null ? 0 : orders.getTotal() %> open order(s) — pending confirmation,
            preparing or shipping. Delivered and cancelled orders are excluded.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Order</th>
                        <th>Customer</th>
                        <th>Shop</th>
                        <th>Total</th>
                        <th>Order status</th>
                        <th>Current shipper</th>
                        <th>Assign / reassign</th>
                    </tr>
                </thead>

                <tbody>
                <% if (orders == null || orders.isEmpty()) { %>

                    <tr>
                        <td colspan="7">No open order to assign right now.</td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : orders.getItems()) {
                           Object deliveryStatus = row.get("delivery_status");
                           Object staffId = row.get("assigned_staff_id");
                           Object orderStatus = row.get("status");
                %>
                    <tr>
                        <td>
                            #<%= esc(row.get("order_id")) %>
                            <br><small><%= esc(row.get("created_at")) %></small>
                        </td>

                        <td>
                            <%= esc(row.get("customer_name")) %>
                            <br><small><%= esc(row.get("customer_phone")) %></small>
                        </td>

                        <td><%= esc(row.get("shop_name")) %></td>

                        <td><%= money(row.get("total_amount")) %></td>

                        <td>
                            <span class="pill <%= "shipping".equals(
                                    orderStatus == null ? "" : orderStatus.toString())
                                    ? "pill-warn" : "pill-muted" %>">
                                <%= esc(orderStatus) %>
                            </span>
                        </td>

                        <td>
                            <% if (staffId == null) { %>
                                <span class="pill pill-muted">Not assigned</span>
                            <% } else { %>
                                <%= esc(row.get("assigned_staff_name")) %>
                                <br><small><%= esc(deliveryStatus) %></small>
                            <% } %>
                        </td>

                        <td>
                            <% if (staffId == null) { %>

                                <form method="post"
                                      action="<%= contextPath %>/admin/delivery-assign"
                                      class="inline-form">

                                    <input type="hidden" name="action" value="assign">
                                    <input type="hidden" name="orderId"
                                           value="<%= esc(row.get("order_id")) %>">

                                    <select name="staffId" class="filter-input" required>
                                        <%= staffOptions(staff, null) %>
                                    </select>

                                    <input type="text" name="note" class="filter-input"
                                           placeholder="Note (optional)">

                                    <button type="submit" class="btn-sm">Assign</button>

                                </form>

                            <% } else { %>

                                <form method="post"
                                      action="<%= contextPath %>/admin/delivery-assign"
                                      class="inline-form">

                                    <input type="hidden" name="action" value="reassign">
                                    <input type="hidden" name="deliveryId"
                                           value="<%= esc(row.get("delivery_id")) %>">

                                    <select name="staffId" class="filter-input" required>
                                        <%= staffOptions(staff, staffId) %>
                                    </select>

                                    <input type="text" name="note" class="filter-input"
                                           placeholder="Reason (optional)">

                                    <button type="submit" class="btn-sm-info">Reassign</button>

                                    <button type="submit" class="btn-sm-danger"
                                            name="action" value="unassign"
                                            formnovalidate
                                            title="Cancel this assignment">
                                        Cancel
                                    </button>

                                </form>

                            <% } %>
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

    <p class="action-row">
        <a class="btn-secondary" href="<%= contextPath %>/admin/deliveries">
            🚚 Delivery monitoring
        </a>
        <a class="btn-secondary" href="<%= contextPath %>/admin/delivery-staff">
            🧑‍✈️ Delivery staff
        </a>
    </p>

</section>

</body>
</html>
