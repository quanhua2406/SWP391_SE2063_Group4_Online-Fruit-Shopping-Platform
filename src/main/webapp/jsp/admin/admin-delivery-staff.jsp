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

    <title>Delivery Staff - FreshFruit Admin</title>

    <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/admin/dashboard">📊 Dashboard</a>

        <a href="<%= contextPath %>/admin/deliveries">🚚 Deliveries</a>

        <a href="<%= contextPath %>/admin/user-management">👥 Users</a>

        <a href="<%= contextPath %>/logout" class="logout">Logout</a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">← Admin Dashboard</a>
    </p>

    <h1>Delivery Staff</h1>

    <p class="dashboard-welcome">
        Delivery-staff accounts on the platform, their current
        workload and availability.
    </p>

    <% if ("blocked".equals(msg)) { %>
        <div class="success-message">✅ Staff account locked — the shipper can no longer sign in.</div>
    <% } else if ("unblocked".equals(msg)) { %>
        <div class="success-message">✅ Staff account unlocked.</div>
    <% } else if (!msg.isEmpty() && !"failed".equals(msg)) { %>
        <div class="error-message">The requested action could not be applied.</div>
    <% } else if ("failed".equals(msg)) { %>
        <div class="error-message">The requested action could not be applied.</div>
    <% } %>

    <div class="panel">

        <h2>🧑‍✈️ Staff accounts</h2>

        <p class="panel-hint">
            <%= staff == null ? 0 : staff.size() %> account(s) hold the DeliveryStaff role.
            Assignments are viewed through
            <a href="<%= contextPath %>/admin/deliveries">Delivery Monitoring</a>.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Staff</th>
                        <th>Contact</th>
                        <th>Joined</th>
                        <th>Active deliveries</th>
                        <th>Total deliveries</th>
                        <th>Account status</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                <% if (staff == null || staff.isEmpty()) { %>

                    <tr>
                        <td colspan="7">No delivery-staff account exists yet.</td>
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
                                <%= active ? "Active" : "Locked" %>
                            </span>
                        </td>

                        <td>
                            <form method="post"
                                  action="<%= contextPath %>/admin/delivery-staff"
                                  style="display: inline;">

                                <input type="hidden" name="staffId" value="<%= esc(row.get("user_id")) %>">

                                <% if (active) { %>
                                    <input type="hidden" name="action" value="block">
                                    <button type="submit" class="btn-sm-danger">Block</button>
                                <% } else { %>
                                    <input type="hidden" name="action" value="unblock">
                                    <button type="submit" class="btn-sm">Unblock</button>
                                <% } %>

                            </form>

                            <a class="btn-sm-info"
                               href="<%= contextPath %>/admin/delivery-assign?search=<%= esc(row.get("user_id")) %>"
                               title="Deliveries assigned to this shipper">
                                Assignments
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

    </div>

</section>

</body>
</html>
