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

        html.append("<option value=\"\">— Chọn shipper —</option>");

        if (staff != null) {
            for (Map<String, Object> s : staff) {
                Object id = s.get("user_id");
                boolean selected = selectedId != null
                        && id != null
                        && selectedId.toString().equals(id.toString());

                String label = s.get("full_name") + " ("
                        + (s.get("active_deliveries") == null ? "0" : s.get("active_deliveries"))
                        + " đang giao)"
                        + ("active".equals(s.get("status")) ? "" : " — đã khóa");

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

    <title>Phân công giao hàng - FreshFruit Admin</title>

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
        <a href="<%= contextPath %>/admin/dashboard">← Bảng điều khiển</a>
    </p>

    <h1>Quản lý phân công giao hàng</h1>

    <p class="dashboard-welcome">
        Chủ cửa hàng phân shipper khi chuẩn bị đơn hàng.
        Dùng màn hình này để phân lại, hủy hoặc thêm phân công
        khi có sự cố.
    </p>

    <% if (msg != null && !msg.isEmpty()) { %>

        <% if ("assigned".equals(msg)) { %>
            <div class="success-message">✅ Đã phân shipper cho đơn hàng.</div>
        <% } else if ("reassigned".equals(msg)) { %>
            <div class="success-message">✅ Đã phân lại đơn giao cho shipper mới.</div>
        <% } else if ("unassigned".equals(msg)) { %>
            <div class="success-message">✅ Đã hủy phân công — đơn hàng quay lại hàng chờ.</div>
        <% } else if ("conflict".equals(msg)) { %>
            <div class="error-message">
                Đơn hàng đã có đơn giao hoặc không còn có thể phân công.
            </div>
        <% } else if ("invalid-note".equals(msg)) { %>
            <div class="error-message">
                Ghi chú quá dài (tối đa 255 ký tự).
            </div>
        <% } else { %>
            <div class="error-message">Không thể thực hiện thao tác yêu cầu.</div>
        <% } %>

    <% } %>

    <!-- ==================== FILTERS ==================== -->

    <form method="get" action="<%= contextPath %>/admin/delivery-assign" class="filter-form">

        <label for="search">Tìm kiếm</label>

        <input class="filter-input" type="text" id="search" name="search"
               placeholder="Mã đơn, khách hàng, email, SĐT, cửa hàng"
               value="<%= esc(search) %>">

        <button type="submit" class="btn-sm">Áp dụng</button>

        <a class="filter-clear" href="<%= contextPath %>/admin/delivery-assign">Xóa lọc</a>

    </form>

    <!-- ==================== ASSIGNABLE ORDERS ==================== -->

    <div class="panel">

        <h2>🚚 Đơn hàng đang mở</h2>

        <p class="panel-hint">
            <%= orders == null ? 0 : orders.getTotal() %> đơn đang mở — chờ xác nhận,
            đang chuẩn bị hoặc đang giao. Không hiển thị đơn đã giao và đã hủy.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Đơn hàng</th>
                        <th>Khách hàng</th>
                        <th>Cửa hàng</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái đơn</th>
                        <th>Shipper hiện tại</th>
                        <th>Phân công / phân lại</th>
                    </tr>
                </thead>

                <tbody>
                <% if (orders == null || orders.isEmpty()) { %>

                    <tr>
                        <td colspan="7">Hiện không có đơn nào cần phân công.</td>
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
                                <span class="pill pill-muted">Chưa phân công</span>
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
                                           maxlength="255"
                                           placeholder="Ghi chú (không bắt buộc)">

                                    <button type="submit" class="btn-sm">Phân công</button>

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
                                           maxlength="255"
                                           placeholder="Lý do (không bắt buộc)">

                                    <button type="submit" class="btn-sm-info">Phân lại</button>

                                    <button type="submit" class="btn-sm-danger"
                                            name="action" value="unassign"
                                            formnovalidate
                                            title="Hủy phân công này">
                                        Hủy
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
            🚚 Theo dõi giao hàng
        </a>
        <a class="btn-secondary" href="<%= contextPath %>/admin/delivery-staff">
            🧑‍✈️ Nhân viên giao hàng
        </a>
    </p>

</section>

</body>
</html>
