<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

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
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> staff = (List<Map<String, Object>>) request.getAttribute("staff");

    String msg = request.getAttribute("assignMessage") == null
            ? "" : request.getAttribute("assignMessage").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Nhân viên giao hàng - FreshFruit Admin</title>

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

        <a href="<%= contextPath %>/admin/user-management">👥 Người dùng</a>

        <a href="<%= contextPath %>/logout" class="logout">Đăng xuất</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">← Bảng điều khiển</a>
    </p>

    <h1>Nhân viên giao hàng</h1>

    <p class="dashboard-welcome">
        Tài khoản nhân viên giao hàng trên hệ thống, khối lượng
        công việc và tình trạng sẵn sàng hiện tại.
    </p>

    <% if ("blocked".equals(msg)) { %>
        <div class="success-message">✅ Đã khóa tài khoản — shipper không thể đăng nhập nữa.</div>
    <% } else if ("unblocked".equals(msg)) { %>
        <div class="success-message">✅ Đã mở khóa tài khoản.</div>
    <% } else if (!msg.isEmpty() && !"failed".equals(msg)) { %>
        <div class="error-message">Không thể thực hiện thao tác yêu cầu.</div>
    <% } else if ("failed".equals(msg)) { %>
        <div class="error-message">Không thể thực hiện thao tác yêu cầu.</div>
    <% } %>

    <div class="panel">

        <h2>🧑‍✈️ Tài khoản nhân viên</h2>

        <p class="panel-hint">
            <%= staff == null ? 0 : staff.size() %> tài khoản có vai trò DeliveryStaff.
            Có thể xem tiến độ tại
            <a href="<%= contextPath %>/admin/deliveries">Theo dõi giao hàng</a>.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Nhân viên</th>
                        <th>Liên hệ</th>
                        <th>Ngày tham gia</th>
                        <th>Đơn đang giao</th>
                        <th>Tổng đơn giao</th>
                        <th>Trạng thái tài khoản</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>

                <tbody>
                <% if (staff == null || staff.isEmpty()) { %>

                    <tr>
                        <td colspan="7">Chưa có tài khoản nhân viên giao hàng nào.</td>
                    </tr>

                <% } else {
                       for (Map<String, Object> row : staff) {
                           String status = row.get("status") == null
                                   ? "" : row.get("status").toString();
                           boolean active = "active".equals(status);
                %>
                    <tr>
                        <td>
                            <%= esc(row.get("full_name")) %>
                            <br><small>#<%= esc(row.get("user_id")) %></small>
                        </td>

                        <td>
                            <%= esc(row.get("email")) %>
                            <br><small><%= esc(row.get("phone")) %></small>
                        </td>

                        <td><%= esc(row.get("created_at")) %></td>

                        <td><%= esc(row.get("active_deliveries")) %></td>

                        <td><%= esc(row.get("total_deliveries")) %></td>

                        <td>
                            <span class="pill <%= active ? "pill-ok" : "pill-danger" %>">
                                <%= active ? "Hoạt động" : "Đã khóa" %>
                            </span>
                        </td>

                        <td>
                            <form method="post"
                                  action="<%= contextPath %>/admin/delivery-staff"
                                  style="display: inline;">

                                <input type="hidden" name="staffId" value="<%= esc(row.get("user_id")) %>">

                                <% if (active) { %>
                                    <input type="hidden" name="action" value="block">
                                    <button type="submit" class="btn-sm-danger">Khóa</button>
                                <% } else { %>
                                    <input type="hidden" name="action" value="unblock">
                                    <button type="submit" class="btn-sm">Mở khóa</button>
                                <% } %>

                            </form>
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

</section>

</body>
</html>
