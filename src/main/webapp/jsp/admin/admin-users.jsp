<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO.UserInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    List<UserInfo> users =
            (List<UserInfo>) request.getAttribute("users");

    String keyword = request.getAttribute("keyword") == null
            ? ""
            : String.valueOf(request.getAttribute("keyword"));

    String selectedRole =
            request.getAttribute("selectedRole") == null
                    ? "ALL"
                    : String.valueOf(request.getAttribute("selectedRole"));

    String error = request.getAttribute("error") == null
            ? ""
            : String.valueOf(request.getAttribute("error"));

    String message = request.getAttribute("message") == null
            ? ""
            : String.valueOf(request.getAttribute("message"));

    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>User Management - FreshFruit</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            color: #263238;
            background: #f5f7fa;
        }

        /* NAVBAR */
        .navbar {
            min-height: 70px;
            padding: 16px 5%;
            background: #fff;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
        }

        .logo {
            color: #2e7d32;
            font-size: 25px;
            font-weight: 700;
        }

        .nav-actions {
            display: flex;
            align-items: center;
            gap: 22px;
        }

        .nav-actions a {
            color: #374151;
            text-decoration: none;
            font-size: 14px;
        }

        .nav-actions a:hover {
            color: #2E7D32;
        }

        /* PAGE */
        .container {
            width: 94%;
            max-width: 1400px;
            margin: 35px auto;
        }

        .page-header {
            margin-bottom: 28px;
        }

        .page-header h1 {
            margin: 0 0 8px;
            color: #222;
            font-size: 30px;
        }

        .page-header p {
            margin: 0;
            color: #777;
            font-size: 14px;
        }

        /* COMMON BUTTON */
        .btn {
            min-height: 40px;
            padding: 0 16px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border: 0;
            border-radius: 7px;
            font-family: inherit;
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            white-space: nowrap;
        }

        /* SEARCH BUTTON */
        .btn-search {
            min-height: 44px;
            padding: 0 24px;
            background: #ff7a00;
            color: #fff;
        }

        .btn-search:hover {
            background: #256628;
        }

        /* RESET BUTTON */
        .btn-reset {
            min-height: 44px;
            background: #f1f1f1;
            color: #555;
        }

        .btn-reset:hover {
            background: #e5e5e5;
        }

        /* DETAIL BUTTON */
        .btn-detail {
            background: #1976d2;
            color: #fff;
        }

        .btn-detail:hover {
            background: #1565c0;
        }

        /* EDIT BUTTON */
        .btn-edit {
            background: #E8F5E9;
            color: #e86d00;
        }

        .btn-edit:hover {
            background: #C8E6C9;
        }

        /* BLOCK BUTTON */
        .btn-block {
            background: #fee2e2;
            color: #991b1b;
        }

        .btn-block:hover {
            background: #fecaca;
        }

        /* UNBLOCK BUTTON */
        .btn-unblock {
            background: #dcfce7;
            color: #166534;
        }

        .btn-unblock:hover {
            background: #bbf7d0;
        }

        /* NOTICES */
        .notice {
            padding: 14px 18px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-size: 14px;
        }

        .notice-error {
            background: #ffe8e8;
            color: #c62828;
            border: 1px solid #ffcaca;
        }

        .notice-success {
            background: #e8f7ed;
            color: #218838;
            border: 1px solid #c7ead2;
        }

        /* SEARCH CARD */
        .filter-card {
            padding: 22px;
            margin-bottom: 25px;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
        }

        .filter-heading {
            margin-bottom: 16px;
        }

        .filter-heading h2 {
            margin: 0;
            color: #333;
            font-size: 19px;
        }

        .filter-heading p {
            margin: 6px 0 0;
            color: #888;
            font-size: 13px;
        }

        .filter-form {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
        }

        .filter-form input,
        .filter-form select {
            height: 44px;
            padding: 0 14px;
            border: 1px solid #d8d8d8;
            border-radius: 8px;
            background: #fff;
            font-family: inherit;
            font-size: 14px;
        }

        .filter-form input {
            flex: 1;
            min-width: 220px;
        }

        .filter-form select {
            min-width: 180px;
        }

        .filter-form input:focus,
        .filter-form select:focus {
            outline: none;
            border-color: #ff7a00;
            box-shadow: 0 0 0 2px rgba(255, 122, 0, .1);
        }

        /* TABLE CARD */
        .table-card {
            overflow: hidden;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
        }

        .table-heading {
            padding: 22px 24px;
            border-bottom: 1px solid #eee;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 15px;
        }

        .table-heading h2 {
            margin: 0;
            color: #333;
            font-size: 19px;
        }

        .table-heading p {
            margin: 6px 0 0;
            color: #888;
            font-size: 13px;
        }

        .table-scroll {
            width: 100%;
            overflow-x: auto;
        }

        table {
            width: 100%;
            min-width: 1150px;
            border-collapse: collapse;
        }

        thead {
            background: #fafafa;
        }

        th {
            padding: 15px 16px;
            color: #666;
            text-align: left;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            white-space: nowrap;
            border-bottom: 1px solid #eee;
        }

        td {
            padding: 16px;
            color: #444;
            font-size: 13px;
            vertical-align: middle;
            border-bottom: 1px solid #f0f0f0;
        }

        tbody tr:hover {
            background: #fffaf5;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        /* USER CELL */
        .user-cell {
            display: flex;
            align-items: center;
            gap: 11px;
            min-width: 170px;
        }

        .avatar {
            width: 40px;
            height: 40px;
            flex: 0 0 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: #fff0e3;
            color: #ff7a00;
            font-size: 15px;
            font-weight: 700;
        }

        .user-name {
            color: #333;
            font-weight: 600;
        }

        .user-id {
            margin-top: 4px;
            color: #999;
            font-size: 12px;
        }

        .user-email {
            color: #555;
        }

        /* STATUS AND ROLE */
        .status-badge,
        .role-badge {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 700;
            white-space: nowrap;
        }

        .status-active {
            background: #e8f7ed;
            color: #218838;
        }

        .status-locked {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-other {
            background: #f3f3f3;
            color: #777;
        }

        .role-badge {
            background: #eff6ff;
            color: #1d4ed8;
        }

        /* ACTIONS */
        .actions {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 7px;
        }

        .action-form {
            display: inline;
            margin: 0;
        }

        .empty-message {
            padding: 60px 20px !important;
            text-align: center;
            color: #888;
        }

        .empty-message strong {
            display: block;
            margin-bottom: 6px;
            color: #555;
            font-size: 16px;
        }

        .empty-message span {
            font-size: 13px;
        }

        /* RESPONSIVE */
        @media (max-width: 700px) {
            .navbar {
                align-items: flex-start;
                flex-direction: column;
                padding: 18px 5%;
            }

            .nav-actions {
                gap: 16px;
            }

            .container {
                width: 95%;
                margin: 25px auto;
            }

            .page-header h1 {
                font-size: 25px;
            }

            .filter-form {
                align-items: stretch;
                flex-direction: column;
            }

            .filter-form input,
            .filter-form select,
            .filter-form .btn {
                width: 100%;
            }

            .table-heading {
                align-items: flex-start;
                flex-direction: column;
            }

            .btn-add {
                width: 100%;
            }
        }
    </style>
</head>

<body>

<header class="navbar">
    <div class="logo">🍊 FreshFruit</div>

    <nav class="nav-actions">
        <a href="<%=contextPath%>/admin/dashboard">Dashboard</a>
        <a href="<%=contextPath%>/logout">Logout</a>
    </nav>
</header>

<main class="container">

    <section class="page-header">
        <h1>User Management</h1>
        <p>Search, filter, edit, and manage user account status.</p>
    </section>

    <% if (!error.isEmpty()) { %>
        <div class="notice notice-error" role="alert">
            <%=escapeHtml(error)%>
        </div>
    <% } %>

    <% if (!message.isEmpty()) { %>
        <div class="notice notice-success" role="status">
            <%=escapeHtml(message)%>
        </div>
    <% } %>

    <!-- SEARCH AND FILTER -->
    <section class="filter-card">

        <div class="filter-heading">
            <h2>Search Users</h2>
            <p>Search by email and filter accounts by role.</p>
        </div>

        <form class="filter-form"
              method="get"
              action="<%=contextPath%>/admin/user-management">

            <input type="hidden" name="action" value="search">

            <input type="text"
                   name="keyword"
                   value="<%=escapeHtml(keyword)%>"
                   placeholder="Search by email..."
                   aria-label="Search by email">

            <select name="role" aria-label="Filter by role">

                <option value="ALL"
                    <%= "ALL".equalsIgnoreCase(selectedRole)
                            || selectedRole.isEmpty() ? "selected" : "" %>>
                    All Roles
                </option>

                <option value="CUSTOMER"
                    <%= "CUSTOMER".equalsIgnoreCase(selectedRole)
                            ? "selected" : "" %>>
                    Customer
                </option>

                <option value="SHOPOWNER"
                    <%= "SHOPOWNER".equalsIgnoreCase(selectedRole)
                            ? "selected" : "" %>>
                    Shop Owner
                </option>

                <option value="DELIVERYSTAFF"
                    <%= "DELIVERYSTAFF".equalsIgnoreCase(selectedRole)
                            ? "selected" : "" %>>
                    Delivery Staff
                </option>

                <option value="ADMIN"
                    <%= "ADMIN".equalsIgnoreCase(selectedRole)
                            ? "selected" : "" %>>
                    Admin
                </option>

                <option value="GUEST"
                    <%= "GUEST".equalsIgnoreCase(selectedRole)
                            ? "selected" : "" %>>
                    Guest
                </option>

            </select>

            <button type="submit" class="btn btn-search">
                Search
            </button>

            <a class="btn btn-reset"
               href="<%=contextPath%>/admin/user-management">
                Reset
            </a>

        </form>
    </section>

    <!-- USER LIST -->
    <section class="table-card">

        <div class="table-heading">

            <div>
                <h2>User List</h2>
                <p>View details, edit accounts, or block and unblock users.</p>
            </div>

        </div>

        <div class="table-scroll">

            <table>

                <thead>
                    <tr>
                        <th>User</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>

                <%
                    if (users != null && !users.isEmpty()) {

                        for (UserInfo user : users) {

                            String role = user.getRoleName() == null
                                    ? "GUEST"
                                    : user.getRoleName();

                            String status = user.getStatus() == null
                                    ? ""
                                    : user.getStatus();

                            boolean isAdmin =
                                    "ADMIN".equalsIgnoreCase(role);

                            boolean isActive =
                                    "active".equalsIgnoreCase(status);

                            boolean isLocked =
                                    "locked".equalsIgnoreCase(status);

                            String statusClass = isActive
                                    ? "status-active"
                                    : isLocked
                                        ? "status-locked"
                                        : "status-other";

                            String displayName =
                                    user.getFullName() == null
                                    || user.getFullName().isBlank()
                                        ? "User"
                                        : user.getFullName();

                            String avatar = displayName
                                    .substring(0, 1)
                                    .toUpperCase(Locale.ROOT);
                %>

                    <tr>

                        <!-- USER -->
                        <td>
                            <div class="user-cell">

                                <div class="avatar">
                                    <%=escapeHtml(avatar)%>
                                </div>

                                <div>
                                    <div class="user-name">
                                        <%=escapeHtml(displayName)%>
                                    </div>

                                    <div class="user-id">
                                        ID #<%=user.getUserId()%>
                                    </div>
                                </div>

                            </div>
                        </td>

                        <!-- EMAIL -->
                        <td class="user-email">
                            <%=escapeHtml(user.getEmail())%>
                        </td>

                        <!-- PHONE -->
                        <td>
                            <%=user.getPhone() == null
                                    || user.getPhone().isBlank()
                                    ? "-"
                                    : escapeHtml(user.getPhone())%>
                        </td>

                        <!-- ROLE -->
                        <td>
                            <span class="role-badge">
                                <%=escapeHtml(role)%>
                            </span>
                        </td>

                        <!-- STATUS -->
                        <td>
                            <span class="status-badge <%=statusClass%>">
                                <%=status.isBlank()
                                        ? "Unknown"
                                        : escapeHtml(status)%>
                            </span>
                        </td>

                        <!-- ACTIONS -->
                        <td>
                            <div class="actions">

                                <!-- VIEW -->
                                <a class="btn btn-detail"
                                   href="<%=contextPath%>/admin/user-management?action=detail&amp;userId=<%=user.getUserId()%>">
                                    View
                                </a>

                                <!-- EDIT -->
                                <a class="btn btn-edit"
                                   href="<%=contextPath%>/admin/user-management?action=edit&amp;userId=<%=user.getUserId()%>">
                                    Edit
                                </a>

                                <% if (!isAdmin && isActive) { %>

                                    <!-- BLOCK -->
                                    <form class="action-form"
                                          method="post"
                                          action="<%=contextPath%>/admin/user-management"
                                          onsubmit="return confirm('Are you sure you want to block this account?');">

                                        <input type="hidden"
                                               name="action"
                                               value="block">

                                        <input type="hidden"
                                               name="userId"
                                               value="<%=user.getUserId()%>">

                                        <button type="submit"
                                                class="btn btn-block">
                                            Block
                                        </button>

                                    </form>

                                <% } else if (!isAdmin && isLocked) { %>

                                    <!-- UNBLOCK -->
                                    <form class="action-form"
                                          method="post"
                                          action="<%=contextPath%>/admin/user-management"
                                          onsubmit="return confirm('Are you sure you want to unblock this account?');">

                                        <input type="hidden"
                                               name="action"
                                               value="unblock">

                                        <input type="hidden"
                                               name="userId"
                                               value="<%=user.getUserId()%>">

                                        <button type="submit"
                                                class="btn btn-unblock">
                                            Unblock
                                        </button>

                                    </form>

                                <% } else if (isAdmin) { %>

                                    <span class="status-badge status-other">
                                        Protected
                                    </span>

                                <% } %>

                            </div>
                        </td>

                    </tr>

                <%
                        }

                    } else {
                %>

                    <tr>
                        <td colspan="6" class="empty-message">

                            <strong>No users found</strong>

                            <span>
                                Try another email keyword or role filter.
                            </span>

                        </td>
                    </tr>

                <%
                    }
                %>

                </tbody>

            </table>

        </div>
    </section>

</main>

</body>
</html>