<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.model.OrderItem"%>
<%@page import="com.mycompany.onlinefruitshopping.model.OrderStatusHistory"%>

<%!
    private String statusLabel(Object value) {
        String status = value == null ? "" : value.toString();

        switch (status) {
            case "pending_confirmation":
                return "Chờ xác nhận";
            case "preparing":
                return "Đang chuẩn bị";
            case "shipping":
            case "delivering":
                return "Đang giao";
            case "delivered":
                return "Đã giao";
            case "cancelled":
                return "Đã hủy";
            case "failed":
                return "Thất bại";
            case "assigned":
                return "Đã phân công";
            case "picked_up":
                return "Đã lấy hàng";
            default:
                return status;
        }
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

    <title>Đơn hàng #<%= esc(order.get("order_id")) %> - FreshFruit Admin</title>

    <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/admin/dashboard">📊 Bảng điều khiển</a>

        <a href="<%= contextPath %>/admin/orders">🛒 Đơn hàng</a>

        <a href="<%= contextPath %>/admin/deliveries">🚚 Giao hàng</a>

        <a href="<%= contextPath %>/logout" class="logout">Đăng xuất</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/orders">← Theo dõi đơn hàng</a>
    </p>

    <h1>Đơn hàng #<%= esc(order.get("order_id")) %></h1>

    <p class="dashboard-welcome">
        Đặt lúc <%= esc(order.get("created_at")) %>
        &nbsp;•&nbsp;
        <span class="pill <%= pillClass(status) %>"><%= esc(statusLabel(status)) %></span>
    </p>

    <!-- ==================== SUMMARY ==================== -->

    <div class="summary-grid">

        <div class="summary-card">
            <span class="summary-label">Khách hàng</span>
            <span class="summary-value"><%= esc(order.get("customer_name")) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Cửa hàng</span>
            <span class="summary-value"><%= esc(order.get("shop_name")) %></span>
        </div>

        <div class="summary-card summary-accent">
            <span class="summary-label">Tổng tiền</span>
            <span class="summary-value"><%= money(order.get("total_amount")) %></span>
        </div>

    </div>

    <!-- ==================== CUSTOMER & DELIVERY ==================== -->

    <div class="panel">

        <h2>👤 Khách hàng &amp; Giao hàng</h2>

        <table class="data-table">
            <tbody>
                <tr>
                    <th>Khách hàng</th>
                    <td>
                        <%= esc(order.get("customer_name")) %>
                        (<%= esc(order.get("customer_email")) %>)
                    </td>
                </tr>
                <tr>
                    <th>SĐT liên hệ</th>
                    <td><%= esc(order.get("customer_phone")) %></td>
                </tr>
                <tr>
                    <th>Người nhận</th>
                    <td>
                        <%= esc(order.get("recipient_name")) %>
                        <%= order.get("recipient_phone") == null
                                ? "" : " — " + esc(order.get("recipient_phone")) %>
                    </td>
                </tr>
                <tr>
                    <th>Địa chỉ giao hàng</th>
                    <td><%= address.length() == 0 ? "—" : esc(address.toString()) %></td>
                </tr>
                <tr>
                    <th>Shipper (chủ cửa hàng phân công)</th>
                    <td>
                        <% if (delivery == null) { %>
                            <span class="pill pill-muted">Chưa có shipper</span>
                        <% } else { %>
                            <%= esc(delivery.get("staff_name")) %>
                            (<%= esc(delivery.get("staff_phone")) %>)
                            <span class="pill <%= pillClass(delivery.get("status") == null
                                    ? "" : delivery.get("status").toString()) %>">
                                <%= esc(statusLabel(delivery.get("status"))) %>
                            </span>
                        <% } %>
                    </td>
                </tr>
            </tbody>
        </table>

    </div>

    <!-- ==================== ITEMS ==================== -->

    <div class="panel">

        <h2>📦 Sản phẩm trong đơn</h2>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Đóng gói</th>
                        <th>Đơn giá</th>
                        <th>Số lượng</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>

                <tbody>
                <% if (items == null || items.isEmpty()) { %>

                    <tr>
                        <td colspan="5">Đơn hàng này không có sản phẩm nào.</td>
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
                        <td colspan="4">Tổng đơn hàng</td>
                        <td><strong><%= money(order.get("total_amount")) %></strong></td>
                    </tr>
                </tfoot>

            </table>

        </div>

    </div>

    <!-- ==================== PAYMENTS ==================== -->

    <div class="panel">

        <h2>💳 Thanh toán</h2>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>#</th>
                        <th>Phương thức</th>
                        <th>Số tiền</th>
                        <th>Trạng thái</th>
                        <th>Mã giao dịch</th>
                        <th>Thời gian thanh toán</th>
                    </tr>
                </thead>

                <tbody>
                <% if (payments == null || payments.isEmpty()) { %>

                    <tr>
                        <td colspan="6">Chưa ghi nhận thanh toán cho đơn hàng này.</td>
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

        <h2>🕘 Lịch sử trạng thái</h2>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Thời gian</th>
                        <th>Trạng thái</th>
                        <th>Người thay đổi</th>
                    </tr>
                </thead>

                <tbody>
                <% if (history == null || history.isEmpty()) { %>

                    <tr>
                        <td colspan="3">Chưa có thay đổi trạng thái nào.</td>
                    </tr>

                <% } else {
                       for (OrderStatusHistory entry : history) {
                %>
                    <tr>
                        <td><%= esc(entry.getChangedAt()) %></td>
                        <td><%= esc(statusLabel(entry.getStatus())) %></td>
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
           href="<%= contextPath %>/admin/order-issues?search=<%= esc(order.get("order_id")) %>">
            🛠️ Xử lý như sự cố
        </a>

    </p>

</section>

</body>
</html>
