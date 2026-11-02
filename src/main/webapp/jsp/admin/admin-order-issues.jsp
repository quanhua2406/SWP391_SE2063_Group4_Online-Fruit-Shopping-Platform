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

    <title>Sự cố đơn hàng - FreshFruit Admin</title>

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

        <a href="<%= contextPath %>/admin/delivery-assign">🚚 Phân công</a>

        <a href="<%= contextPath %>/logout" class="logout">Đăng xuất</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">← Bảng điều khiển</a>
    </p>

    <h1>Sự cố đơn hàng</h1>

    <p class="dashboard-welcome">
        Đơn thất bại và đã hủy, cùng các đơn bị kẹt cùng một giai đoạn
        hơn ba ngày. Mở lại đơn để đưa về xử lý, hoặc hủy đơn
        để đóng khiếu nại.
    </p>

    <% if (msg != null && !msg.isEmpty()) { %>

        <% if ("reopened".equals(msg)) { %>
            <div class="success-message">✅ Đã mở lại đơn — đơn quay lại xử lý.</div>
        <% } else if ("cancelled".equals(msg)) { %>
            <div class="success-message">✅ Đã hủy đơn hàng.</div>
        <% } else if ("terminal".equals(msg)) { %>
            <div class="error-message">
                Đơn hàng này đã ở trạng thái cuối (đã giao hoặc đã hủy)
                nên không thể thay đổi.
            </div>
        <% } else { %>
            <div class="error-message">Không thể thực hiện thao tác yêu cầu.</div>
        <% } %>

    <% } %>

    <!-- ==================== FILTERS ==================== -->

    <form method="get" action="<%= contextPath %>/admin/order-issues" class="filter-form">

        <label for="search">Tìm kiếm</label>

        <input class="filter-input" type="text" id="search" name="search"
               placeholder="Mã đơn, khách hàng, email, SĐT, cửa hàng"
               value="<%= esc(search) %>">

        <button type="submit" class="btn-sm">Áp dụng</button>

        <a class="filter-clear" href="<%= contextPath %>/admin/order-issues">Xóa lọc</a>

    </form>

    <!-- ==================== ISSUE TABLE ==================== -->

    <div class="panel">

        <h2>🛠️ Đơn hàng cần xử lý</h2>

        <p class="panel-hint">
            <%= orders == null ? 0 : orders.getTotal() %> đơn hàng được đánh dấu.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Đơn hàng</th>
                        <th>Khách hàng</th>
                        <th>Cửa hàng</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th>Thời gian chờ</th>
                        <th>Vấn đề</th>
                        <th>Xử lý</th>
                    </tr>
                </thead>

                <tbody>
                <% if (orders == null || orders.isEmpty()) { %>

                    <tr>
                        <td colspan="8">
                            Không có đơn nào cần xử lý — không có đơn thất bại, đã hủy
                            hoặc bị trễ.
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
                                <%= esc(statusLabel(status)) %>
                            </span>
                        </td>

                        <td><%= esc(row.get("age_days")) %> ngày</td>

                        <td><%= esc(row.get("issue_type")) %></td>

                        <td>
                            <form method="post"
                                  action="<%= contextPath %>/admin/order-issues"
                                  class="inline-form">

                                <input type="hidden" name="orderId"
                                       value="<%= esc(row.get("order_id")) %>">

                                <input type="text" name="note" class="filter-input"
                                       maxlength="40"
                                       placeholder="Ghi chú cho lịch sử">

                                <button type="submit" class="btn-sm"
                                        name="resolution" value="reopen">
                                    Mở lại
                                </button>

                                <% if (closable) { %>
                                    <button type="submit" class="btn-sm-danger"
                                            name="resolution" value="cancel">
                                        Hủy đơn
                                    </button>
                                <% } %>

                            </form>

                            <a class="btn-sm-info"
                               href="<%= contextPath %>/admin/order-detail?orderId=<%= esc(row.get("order_id")) %>">
                                Chi tiết
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
