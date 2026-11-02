
<%@page import="java.util.List"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.user.RoleDAO.Role"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.user.RoleDAO.UserRoleInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    List<UserRoleInfo> users =
        (List<UserRoleInfo>) request.getAttribute("users");

    List<Role> roles =
        (List<Role>) request.getAttribute("roles");

    String searchEmail = request.getAttribute("searchEmail") == null
        ? "" : String.valueOf(request.getAttribute("searchEmail"));

    String error = request.getAttribute("error") == null
        ? "" : String.valueOf(request.getAttribute("error"));

    String message = request.getParameter("message");
    if (message == null) {
        message = request.getAttribute("success") == null
            ? "" : String.valueOf(request.getAttribute("success"));
    }

    String cp = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Role Management - FreshFruit</title>

    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            color: #263238;
            background: #f5f7fa;
        }

        .navbar {
            min-height: 68px;
            padding: 16px 5%;
            background: #fff;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
        }

        .logo {
            color: #2e7d32;
            font-size: 24px;
            font-weight: bold;
        }

        .nav-links {
            display: flex;
            gap: 20px;
        }

        a { text-decoration: none; }

        .nav-links a {
            color: #374151;
            font-size: 14px;
        }

        .container {
            width: 94%;
            max-width: 1400px;
            margin: 32px auto;
        }

        .page-header { margin-bottom: 24px; }

        h1 {
            margin: 0 0 8px;
            font-size: 28px;
        }

        .muted {
            color: #6b7280;
            font-size: 14px;
        }

        .panel {
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 10px;
            margin-bottom: 22px;
            overflow: hidden;
        }

        .filters { padding: 20px; }

        .filter-form {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }

        input, select {
            min-height: 42px;
            padding: 10px 12px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            background: white;
            font-size: 14px;
        }

        .filter-form input {
            flex: 1;
            min-width: 220px;
        }

        .filter-form select { min-width: 175px; }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 40px;
            padding: 10px 16px;
            border: 0;
            border-radius: 6px;
            font-size: 14px;
            cursor: pointer;
            white-space: nowrap;
        }

        .btn-primary {
            color: white;
            background: #2e7d32;
        }

        .btn-primary:hover { background: #256628; }

        .btn-secondary {
            color: #374151;
            background: #e5e7eb;
        }

        .btn-secondary:hover { background: #d1d5db; }

        .btn-update {
            color: white;
            background: #2e7d32;
            padding: 9px 12px;
            font-size: 12px;
        }

        .btn-update:hover { background: #256628; }

        .btn-protected {
            color: #6b7280;
            background: #e5e7eb;
            padding: 9px 12px;
            font-size: 12px;
            cursor: not-allowed;
        }

        .notice {
            padding: 13px 16px;
            margin-bottom: 18px;
            border-radius: 7px;
            font-size: 14px;
        }

        .notice-error {
            color: #b42318;
            background: #fef3f2;
            border: 1px solid #fecdca;
        }

        .notice-success {
            color: #18733b;
            background: #eaf7ed;
            border: 1px solid #a6e3b5;
        }

        .panel-heading {
            padding: 20px;
            border-bottom: 1px solid #e5e7eb;
        }

        .panel-heading h2 {
            margin: 0 0 6px;
            font-size: 18px;
        }

        .table-scroll { overflow-x: auto; }

        table {
            width: 100%;
            min-width: 950px;
            border-collapse: collapse;
        }

        th, td {
            padding: 15px 16px;
            text-align: left;
            border-bottom: 1px solid #edf0f2;
            font-size: 13px;
        }

        th {
            color: #4b5563;
            background: #f9fafb;
            font-size: 12px;
            text-transform: uppercase;
        }

        tbody tr:hover { background: #f9fcf8; }

        .badge {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 5px;
            font-size: 11px;
            font-weight: bold;
            white-space: nowrap;
        }

        .role-badge {
            color: #1d4ed8;
            background: #eff6ff;
        }

        .admin-badge {
            color: #4b5563;
            background: #f3f4f6;
        }

        .active {
            color: #166534;
            background: #dcfce7;
        }

        .locked {
            color: #991b1b;
            background: #fee2e2;
        }

        .other {
            color: #4b5563;
            background: #f3f4f6;
        }

        .role-form {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .role-select {
            min-width: 140px;
            max-width: 180px;
            min-height: 40px;
            padding: 9px 10px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            background: white;
            font-size: 13px;
        }

        .role-select:focus {
            outline: none;
            border-color: #2e7d32;
            box-shadow: 0 0 0 2px #2e7d3222;
        }

        .role-select:disabled {
            background: #f3f4f6;
            color: #6b7280;
        }

        .empty {
            padding: 40px 20px;
            color: #6b7280;
            text-align: center;
        }

        @media (max-width: 650px) {
            .container { width: 95%; margin: 22px auto; }
            .filter-form { flex-direction: column; }
            .filter-form input,
            .filter-form select,
            .filter-form .btn { width: 100%; }
            .navbar { flex-wrap: wrap; }
            h1 { font-size: 24px; }
            .role-form { flex-wrap: wrap; }
        }
    </style>
</head>

<body>

<header class="navbar">
    <div class="logo">FreshFruit</div>

    <nav class="nav-links">
        <a href="<%=cp%>/admin/dashboard">Dashboard</a>
        <a href="<%=cp%>/logout">Logout</a>
    </nav>
</header>

<main class="container">

    <div class="page-header">
        <h1>Role Management</h1>
        <p class="muted">
            Search users and manage their system roles.
        </p>
    </div>

    <% if (!error.isBlank()) { %>
        <div class="notice notice-error" role="alert">
            <%=escapeHtml(error)%>
        </div>
    <% } %>

    <% if (!message.isBlank()) { %>
        <div class="notice notice-success" role="status">
            <%=escapeHtml(message)%>
        </div>
    <% } %>

    <!-- SEARCH PANEL -->
    <section class="panel filters">
        <form class="filter-form"
              method="post"
              action="<%=cp%>/admin/roles">

            <input type="hidden" name="action" value="search">

            <input type="text"
                   name="email"
                   value="<%=escapeHtml(searchEmail)%>"
                   placeholder="Search by email...">

            <button class="btn btn-primary" type="submit">
                Search
            </button>

            <a class="btn btn-secondary"
               href="<%=cp%>/admin/roles">
                Reset
            </a>
        </form>
    </section>

    <!-- ROLE LIST PANEL -->
    <section class="panel">

        <div class="panel-heading">
            <h2>User Role List</h2>
            <p class="muted">
                Select a role and choose Update to change a user's role.
            </p>
        </div>

        <div class="table-scroll">
            <table>
                <thead>
                    <tr>
                        <th>User ID</th>
                        <th>Full Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Current Role</th>
                        <th>Change Role</th>
                        <th>Action</th>
                    </tr>
                </thead>

                <tbody>
                <% if (users != null && !users.isEmpty()) {
                    for (UserRoleInfo user : users) {
                        String roleName = user.getRoleName() == null
                            ? "" : user.getRoleName();

                        String fullName = user.getFullName() == null
                            ? "" : user.getFullName();

                        boolean isAdmin =
                            "ADMIN".equalsIgnoreCase(roleName);
                %>
                    <tr>
                        <td><%=user.getUserId()%></td>

                        <td><%=escapeHtml(fullName)%></td>

                        <td><%=escapeHtml(user.getEmail())%></td>

                        <td><%=escapeHtml(user.getPhone())%></td>

                        <td>
                            <span class="badge <%=isAdmin
                                ? "admin-badge"
                                : "role-badge"%>">
                                <%=escapeHtml(roleName)%>
                            </span>
                        </td>

                        <td>
                            <% if (isAdmin) { %>
                                <select class="role-select" disabled>
                                    <option>ADMIN</option>
                                </select>
                            <% } else { %>
                                <form class="role-form"
                                      id="roleForm<%=user.getUserId()%>"
                                      method="post"
                                      action="<%=cp%>/admin/roles"
                                      onsubmit="return confirmRoleUpdate();">

                                    <input type="hidden"
                                           name="action"
                                           value="update">

                                    <input type="hidden"
                                           name="userId"
                                           value="<%=user.getUserId()%>">

                                    <select class="role-select"
                                            name="roleId"
                                            required>

                                        <% if (roles != null) {
                                            for (Role role : roles) {
                                                boolean selected =
                                                    role.getRoleId()
                                                    == user.getRoleId();
                                        %>
                                            <option
                                                value="<%=role.getRoleId()%>"
                                                <%=selected
                                                    ? "selected"
                                                    : ""%>>
                                                <%=escapeHtml(role.getRoleName())%>
                                            </option>
                                        <%  }
                                           }
                                        %>
                                    </select>
                                </form>
                            <% } %>
                        </td>

                        <td>
                            <% if (isAdmin) { %>
                                <button type="button"
                                        class="btn btn-protected"
                                        disabled>
                                    Protected
                                </button>
                            <% } else { %>
                                <button type="submit"
                                        form="roleForm<%=user.getUserId()%>"
                                        class="btn btn-update">
                                    Update
                                </button>
                            <% } %>
                        </td>
                    </tr>

                <%  }
                   } else { %>
                    <tr>
                        <td colspan="7" class="empty">
                            No users found.
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>

</main>

<script>
    function confirmRoleUpdate() {
        return confirm(
            "Are you sure you want to update this user's role?"
        );
    }
</script>

</body>
</html>
