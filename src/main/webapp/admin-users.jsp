<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.AdminUserDAO.UserInfo"%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <title>User Administration</title>

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f5f7fa;
            color: #333;
        }

        .navbar {
            height: 65px;
            background: white;
            border-bottom: 1px solid #ddd;

            display: flex;
            justify-content: space-between;
            align-items: center;

            padding: 0 35px;
        }

        .logo {
            font-size: 22px;
            font-weight: bold;
            color: #f28c28;
        }

        .nav-actions {
            display: flex;
            gap: 20px;
        }

        .nav-actions a {
            text-decoration: none;
            color: #333;
        }

        .logout {
            color: #d9534f !important;
        }

        .container {
            width: 95%;
            max-width: 1500px;
            margin: 35px auto;
        }

        .page-title {
            margin-bottom: 8px;
        }

        .page-description {
            color: #777;
            margin-bottom: 25px;
        }

        /* =========================
           SEARCH / FILTER
           ========================= */

        .search-box {
            background: white;

            padding: 20px;

            border-radius: 8px;

            box-shadow:
                0 2px 8px rgba(0,0,0,0.06);

            display: flex;

            gap: 10px;

            align-items: center;

            margin-bottom: 25px;
        }

        .search-box input {
            flex: 1;

            padding: 11px 14px;

            border: 1px solid #ccc;

            border-radius: 5px;

            font-size: 14px;
        }

        .search-box select {
            width: 200px;

            padding: 11px 12px;

            border: 1px solid #ccc;

            border-radius: 5px;

            background: white;

            font-size: 14px;
        }

        .btn {
            border: none;

            border-radius: 5px;

            padding: 10px 15px;

            cursor: pointer;

            font-size: 13px;

            font-weight: 600;

            white-space: nowrap;
        }

        .btn-search {
            background: #f28c28;
            color: white;
        }

        .btn-search:hover {
            background: #df7815;
        }

        .btn-reset {
            background: #777;
            color: white;

            text-decoration: none;

            display: inline-flex;

            align-items: center;
        }

        /* =========================
           MESSAGE
           ========================= */

        .message {
            padding: 12px 15px;

            border-radius: 5px;

            margin-bottom: 20px;
        }

        .success {
            background: #e8f7ed;
            color: #237a3b;

            border: 1px solid #b9e4c4;
        }

        .error {
            background: #fdecec;
            color: #a33a3a;

            border: 1px solid #efb7b7;
        }

        /* =========================
           TABLE
           ========================= */

        .table-wrapper {
            background: white;

            border-radius: 8px;

            box-shadow:
                0 2px 8px rgba(0,0,0,0.06);

            overflow-x: auto;
        }

        table {
            width: 100%;

            border-collapse: collapse;
        }

        th {
            background: #fafafa;

            padding: 14px;

            text-align: left;

            border-bottom: 1px solid #ddd;

            white-space: nowrap;
        }

        td {
            padding: 14px;

            border-bottom: 1px solid #eee;

            vertical-align: middle;
        }

        tr:hover {
            background: #fafafa;
        }

        .table-input {
            width: 100%;

            min-width: 140px;

            padding: 8px 10px;

            border: 1px solid #ccc;

            border-radius: 4px;
        }

        /* =========================
           ROLE
           ========================= */

        .role-badge {
            display: inline-block;

            padding: 5px 10px;

            border-radius: 15px;

            background: #f0f0f0;

            font-size: 12px;

            font-weight: 600;

            white-space: nowrap;
        }

        /* =========================
           STATUS
           ========================= */

        .status {
            display: inline-block;

            padding: 5px 10px;

            border-radius: 20px;

            font-size: 12px;

            font-weight: bold;

            white-space: nowrap;
        }

        .status-active {
            background: #e7f6ec;
            color: #218838;
        }

        .status-locked {
            background: #fdeaea;
            color: #c62828;
        }

        .status-pending {
            background: #fff4d6;
            color: #9a6700;
        }

        .btn-update {
            background: #3498db;
            color: white;
        }

        .btn-block {
            background: #d9534f;
            color: white;
        }

        .btn-unblock {
            background: #28a745;
            color: white;
        }

        .btn-protected {
            background: #eeeeee;
            color: #777;
            cursor: not-allowed;
        }

        .empty {
            text-align: center;
            padding: 30px;
            color: #777;
        }

    </style>

</head>


<body>


<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <div class="nav-actions">

        <a href="${pageContext.request.contextPath}/admin/dashboard">
            Dashboard
        </a>

        <a href="${pageContext.request.contextPath}/logout"
           class="logout">
            Logout
        </a>

    </div>

</header>


<div class="container">


    <h1 class="page-title">
        User Administration
    </h1>


    <p class="page-description">
        View, search, filter, edit, block and unblock user accounts.
    </p>


    <!-- MESSAGE -->

    <%
        String success =
                (String) request.getAttribute("success");

        String error =
                (String) request.getAttribute("error");
    %>


    <% if (success != null) { %>

        <div class="message success">
            <%= success %>
        </div>

    <% } %>


    <% if (error != null) { %>

        <div class="message error">
            <%= error %>
        </div>

    <% } %>


    <!-- =====================================================
         SEARCH + FILTER
         ===================================================== -->

    <%
        String keyword =
                (String) request.getAttribute("keyword");

        String selectedRole =
                (String) request.getAttribute("selectedRole");

        if (keyword == null) {
            keyword = "";
        }

        if (selectedRole == null
                || selectedRole.isBlank()) {

            selectedRole = "ALL";
        }
    %>


    <form method="post"
          action="${pageContext.request.contextPath}/admin/user-management"
          class="search-box">


        <input type="hidden"
               name="action"
               value="search">


        <!-- SEARCH EMAIL -->

        <input type="text"
               name="keyword"
               value="<%= keyword %>"
               placeholder="Search by Gmail / Email...">


        <!-- ROLE FILTER -->

        <select name="role">


            <option value="ALL"
                <%= "ALL".equals(selectedRole)
                        ? "selected"
                        : "" %>>

                All Roles

            </option>


            <option value="CUSTOMER"
                <%= "CUSTOMER".equals(selectedRole)
                        ? "selected"
                        : "" %>>

                Customer

            </option>


            <option value="SHOPOWNER"
                <%= "SHOPOWNER".equals(selectedRole)
                        ? "selected"
                        : "" %>>

                Store Owner

            </option>


            <option value="DELIVERYSTAFF"
                <%= "DELIVERYSTAFF".equals(selectedRole)
                        ? "selected"
                        : "" %>>

                Delivery Staff

            </option>


            <option value="ADMIN"
                <%= "ADMIN".equals(selectedRole)
                        ? "selected"
                        : "" %>>

                Admin

            </option>


            


        </select>


        <button type="submit"
                class="btn btn-search">

            🔍 Search

        </button>


        <a href="${pageContext.request.contextPath}/admin/user-management"
           class="btn btn-reset">

            Reset

        </a>


    </form>


    <!-- =====================================================
         TABLE
         ===================================================== -->

    <%
        List<UserInfo> users =
                (List<UserInfo>)
                request.getAttribute("users");
    %>


    <div class="table-wrapper">

        <table>

            <thead>

                <tr>

                    <th>User ID</th>

                    <th>Full Name</th>

                    <th>Email</th>

                    <th>Phone</th>

                    <th>Role</th>

                    <th>Status</th>

                    <th>Update</th>

                    <th>Account</th>

                </tr>

            </thead>


            <tbody>


            <% if (users == null || users.isEmpty()) { %>

                <tr>

                    <td colspan="8"
                        class="empty">

                        No users found.

                    </td>

                </tr>


            <% } else { %>


                <% for (UserInfo user : users) { %>


                    <%
                        String status =
                                user.getStatus() == null
                                ? ""
                                : user.getStatus()
                                      .toLowerCase();

                        String dbRole =
                                user.getRoleName() == null
                                ? "GUEST"
                                : user.getRoleName()
                                  .toUpperCase();

                        String displayRole;

                        if ("SHOPOWNER".equals(dbRole)) {

                            displayRole = "Store Owner";

                        } else if ("DELIVERYSTAFF".equals(dbRole)) {

                            displayRole = "Delivery Staff";

                        } else if ("CUSTOMER".equals(dbRole)) {

                            displayRole = "Customer";

                        } else if ("ADMIN".equals(dbRole)) {

                            displayRole = "Admin";

                        } else {

                            displayRole = "Guest";
                        }

                        boolean isAdmin =
                                "ADMIN".equals(dbRole);
                    %>


                    <tr>


                        <!-- USER ID -->

                        <td>

                            <%= user.getUserId() %>

                        </td>


                        <!-- FULL NAME -->

                        <td>

                            <form method="post"
                                  action="${pageContext.request.contextPath}/admin/user-management">

                                <input type="hidden"
                                       name="action"
                                       value="update">

                                <input type="hidden"
                                       name="userId"
                                       value="<%= user.getUserId() %>">


                                <input type="text"
                                       class="table-input"
                                       name="fullName"
                                       value="<%= user.getFullName() != null
                                               ? user.getFullName()
                                               : "" %>">

                        </td>


                        <!-- EMAIL -->

                        <td>

                            <input type="email"
                                   class="table-input"
                                   name="email"
                                   value="<%= user.getEmail() != null
                                           ? user.getEmail()
                                           : "" %>">

                        </td>


                        <!-- PHONE -->

                        <td>

                            <input type="text"
                                   class="table-input"
                                   name="phone"
                                   value="<%= user.getPhone() != null
                                           ? user.getPhone()
                                           : "" %>">

                        </td>


                        <!-- ROLE -->

                        <td>

                            <span class="role-badge">

                                <%= displayRole %>

                            </span>

                        </td>


                        <!-- STATUS -->

                        <td>


                            <% if ("active".equals(status)) { %>

                                <span class="status status-active">
                                    Active
                                </span>


                            <% } else if ("locked".equals(status)) { %>

                                <span class="status status-locked">
                                    Locked
                                </span>


                            <% } else if ("pending".equals(status)) { %>

                                <span class="status status-pending">
                                    Pending
                                </span>


                            <% } else { %>

                                <span class="status">
                                    <%= user.getStatus() %>
                                </span>

                            <% } %>


                        </td>


                        <!-- UPDATE -->

                        <td>

                                <button type="submit"
                                        class="btn btn-update">

                                    Update

                                </button>

                            </form>

                        </td>


                        <!-- ACCOUNT -->

                        <td>


                            <% if (isAdmin) { %>


                                <button type="button"
                                        class="btn btn-protected"
                                        disabled>

                                    🔒 Protected

                                </button>


                            <% } else if ("active".equals(status)) { %>


                                <form method="post"
                                      action="${pageContext.request.contextPath}/admin/user-management"
                                      style="display:inline;">

                                    <input type="hidden"
                                           name="action"
                                           value="block">

                                    <input type="hidden"
                                           name="userId"
                                           value="<%= user.getUserId() %>">


                                    <button type="submit"
                                            class="btn btn-block"
                                            onclick="return confirm('Are you sure you want to block this account?');">

                                        🔒 Block Account

                                    </button>

                                </form>


                            <% } else if ("locked".equals(status)) { %>


                                <form method="post"
                                      action="${pageContext.request.contextPath}/admin/user-management"
                                      style="display:inline;">

                                    <input type="hidden"
                                           name="action"
                                           value="unblock">

                                    <input type="hidden"
                                           name="userId"
                                           value="<%= user.getUserId() %>">


                                    <button type="submit"
                                            class="btn btn-unblock"
                                            onclick="return confirm('Are you sure you want to unblock this account?');">

                                        🔓 Unblock Account

                                    </button>

                                </form>


                            <% } else if ("pending".equals(status)) { %>


                                <button type="button"
                                        class="btn btn-protected"
                                        disabled>

                                    Pending Approval

                                </button>


                            <% } %>


                        </td>


                    </tr>


                <% } %>


            <% } %>


            </tbody>

        </table>

    </div>


</div>


</body>

</html>