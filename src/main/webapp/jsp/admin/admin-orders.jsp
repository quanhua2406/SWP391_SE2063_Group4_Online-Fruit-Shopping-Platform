<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>

<%!
    private static final String[][] ORDER_STATUSES = {
        {"pending_confirmation", "Chờ xác nhận"},
        {"preparing", "Đang chuẩn bị"},
        {"shipping", "Đang giao"},
        {"delivered", "Đã giao"},
        {"cancelled", "Đã hủy"},
        {"failed", "Thất bại"}
    };

    private String statusLabel(String status) {

        for (String[] s : ORDER_STATUSES) {
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

    private String when(Object value) {
        return value == null ? "" : value.toString();
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
    Page<Map<String, Object>> orders = (Page<Map<String, Object>>) request.getAttribute("orders");

    @SuppressWarnings("unchecked")
    Map<String, Integer> statusCounts = (Map<String, Integer>) request.getAttribute("statusCounts");

    String status = request.getAttribute("status") == null
            ? "" : request.getAttribute("status").toString();

    String search = request.getAttribute("search") == null
            ? "" : request.getAttribute("search").toString();

    String pagerBase = request.getAttribute("pagerBase") == null
            ? contextPath + "/admin/orders?" : request.getAttribute("pagerBase").toString();

    String msg = request.getParameter("msg") == null ? "" : request.getParameter("msg");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Đơn hàng - FreshFruit Admin</title>

    <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/admin/dashboard">📊 Bảng điều khiển</a>

        <a href="<%= contextPath %>/admin/deliveries">🚚 Giao hàng</a>

        <a href="<%= contextPath %>/admin/order-issues">🛠️ Sự cố đơn hàng</a>

        <a href="<%= contextPath %>/logout" class="logout">Đăng xuất</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">← Bảng điều khiển</a>
    </p>

    <h1>Theo dõi đơn hàng</h1>

    <p class="dashboard-welcome">
        Tất cả đơn hàng trên hệ thống, trên mọi cửa hàng, kèm
        trạng thái hiện tại và phân công giao hàng.
    </p>

    <% if ("notfound".equals(msg)) { %>
        <div class="error-message">Không tìm thấy đơn hàng.</div>
    <% } else if ("badorder".equals(msg)) { %>
        <div class="error-message">Mã đơn hàng không hợp lệ.</div>
    <% } %>

    <!-- ==================== STATUS COUNTS ==================== -->

    <div class="summary-grid">

        <% for (String[] s : ORDER_STATUSES) {
               Integer count = statusCounts == null ? null : statusCounts.get(s[0]);
               int value = count == null ? 0 : count;
        %>
        <a class="summary-card<%= "delivered".equals(s[0]) ? " summary-accent" : "" %>"
           href="<%= contextPath %>/admin/orders?status=<%= s[0] %>">
            <span class="summary-label"><%= s[1] %></span>
            <span class="summary-value"><%= value %></span>
        </a>
        <% } %>

    </div>

    <!-- ==================== FILTERS ==================== -->

    <form method="get" action="<%= contextPath %>/admin/orders" class="filter-form">

        <label for="search">Tìm kiếm</label>

        <input class="filter-input" type="text" id="search" name="search"
               placeholder="Mã đơn, khách hàng, email, SĐT, cửa hàng"
               value="<%= esc(search) %>">

        <label for="status">Trạng thái</label>

        <select class="filter-input" id="status" name="status">
            <option value=""<%= status.isEmpty() ? " selected" : "" %>>Tất cả trạng thái</option>
            <% for (String[] s : ORDER_STATUSES) { %>
                <option value="<%= s[0] %>"<%= s[0].equals(status) ? " selected" : "" %>>
                    <%= s[1] %>
                </option>
            <% } %>
        </select>

        <button type="submit" class="btn-sm">Áp dụng</button>

        <a class="filter-clear" href="<%= contextPath %>/admin/orders">Xóa lọc</a>

    </form>

    <!-- ==================== ORDER TABLE ==================== -->

    <div class="panel">

        <h2>🛒 Đơn hàng</h2>

        <p class="panel-hint">
            <%= orders == null ? 0 : orders.getTotal() %> đơn hàng phù hợp.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>#</th>
                        <th>Khách hàng</th>
                        <th>Cửa hàng</th>
                        <th>Ngày đặt</th>
                        <th>Số món</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th>Shipper</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>

                <tbody>
                <% if (orders == null || orders.isEmpty()) { %>

                    <tr>
                        <td colspan="9">Không có đơn hàng nào phù hợp bộ lọc.</td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : orders.getItems()) {
                           Object st = row.get("status");
                           String statusText = st == null ? "" : st.toString();
                %>
                    <tr>
                        <td>#<%= esc(row.get("order_id")) %></td>

                        <td>
                            <%= esc(row.get("customer_name")) %>
                            <br><small><%= esc(row.get("customer_email")) %></small>
                        </td>

                        <td><%= esc(row.get("shop_name")) %></td>

                        <td><%= esc(when(row.get("created_at"))) %></td>

                        <td><%= esc(row.get("item_count")) %></td>

                        <td><%= money(row.get("total_amount")) %></td>

                        <td>
                            <span class="pill <%= pillClass(statusText) %>">
                                <%= statusLabel(statusText) %>
                            </span>
                        </td>

                        <td><%= esc(row.get("assigned_staff_name")) %></td>

                        <td>
                            <a class="btn-sm-info"
                               href="<%= contextPath %>/admin/order-detail?orderId=<%= esc(row.get("order_id")) %>">
                                Xem
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
