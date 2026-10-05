<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.onlinefruitshopping.dao.RoleDAO.Role" %>
<%@ page import="com.mycompany.onlinefruitshopping.dao.RoleDAO.UserRoleInfo" %>

<!DOCTYPE html>

<html lang="en">

<head>

    <meta charset="UTF-8">

    <title>Role Management - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

    <style>

        /* =====================================================
           PAGE
           ===================================================== */

        .role-page {
            max-width: 1400px;
            margin: 0 auto;
            padding: 40px 30px;
        }

        .role-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }

        .role-header h1 {
            margin: 0;
            font-size: 30px;
            color: #222;
        }

        .role-header p {
            margin-top: 8px;
            color: #777;
            font-size: 15px;
        }


        /* =====================================================
           ALERT
           ===================================================== */

        .role-message {
            padding: 14px 18px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }

        .role-error {
            background: #ffe8e8;
            color: #c62828;
            border: 1px solid #ffcaca;
        }

        .role-success {
            background: #e8f7ed;
            color: #218838;
            border: 1px solid #c7ead2;
        }


        /* =====================================================
           SEARCH
           ===================================================== */

        .role-search-box {
            background: #ffffff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            padding: 22px;
            margin-bottom: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04);
        }

        .role-search-title {
            margin-bottom: 15px;
        }

        .role-search-title h2 {
            margin: 0;
            font-size: 19px;
            color: #333;
        }

        .role-search-title p {
            margin: 6px 0 0;
            font-size: 13px;
            color: #888;
        }

        .role-search-form {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .role-input-wrapper {
            flex: 1;
        }

        .role-input-wrapper input {
            width: 100%;
            height: 44px;
            box-sizing: border-box;
            border: 1px solid #d8d8d8;
            border-radius: 8px;
            padding: 0 15px;
            font-size: 14px;
            outline: none;
        }

        .role-input-wrapper input:focus {
            border-color: #ff7a00;
            box-shadow: 0 0 0 2px rgba(255,122,0,0.1);
        }

        .role-search-btn {
            height: 44px;
            padding: 0 24px;
            border: none;
            border-radius: 8px;
            background: #ff7a00;
            color: white;
            font-weight: 600;
            cursor: pointer;
        }

        .role-search-btn:hover {
            background: #e86d00;
        }

        .role-reset-btn {
            height: 44px;
            display: flex;
            align-items: center;
            padding: 0 20px;
            border-radius: 8px;
            background: #f1f1f1;
            color: #555;
            text-decoration: none;
            font-size: 14px;
        }

        .role-reset-btn:hover {
            background: #e5e5e5;
        }


        /* =====================================================
           USERS TABLE
           ===================================================== */

        .role-users-box {
            background: #ffffff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04);
        }

        .role-users-header {
            padding: 22px 24px;
            border-bottom: 1px solid #eeeeee;
        }

        .role-users-header h2 {
            margin: 0;
            font-size: 19px;
            color: #333;
        }

        .role-users-header p {
            margin: 6px 0 0;
            color: #888;
            font-size: 13px;
        }

        .role-table-wrapper {
            width: 100%;
            overflow-x: auto;
        }

        .role-table {
            width: 100%;
            border-collapse: collapse;
            min-width: 950px;
        }

        .role-table th {
            background: #fafafa;
            padding: 15px 18px;
            text-align: left;
            font-size: 13px;
            color: #666;
            border-bottom: 1px solid #eeeeee;
        }

        .role-table td {
            padding: 16px 18px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 14px;
            color: #444;
            vertical-align: middle;
        }

        .role-table tbody tr:hover {
            background: #fffaf5;
        }


        /* =====================================================
           USER
           ===================================================== */

        .role-user {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .role-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #fff0e3;
            color: #ff7a00;
            display: flex;
            justify-content: center;
            align-items: center;
            font-weight: 700;
        }

        .role-user-name {
            font-weight: 600;
            color: #333;
        }

        .role-user-id {
            margin-top: 3px;
            font-size: 12px;
            color: #999;
        }

        .role-email {
            color: #555;
        }


        /* =====================================================
           STATUS
           ===================================================== */

        .role-status {
            display: inline-flex;
            align-items: center;
            gap: 7px;
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }

        .role-status-dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
        }

        .role-status-active {
            background: #e8f7ed;
            color: #218838;
        }

        .role-status-active .role-status-dot {
            background: #28a745;
        }

        .role-status-inactive {
            background: #f3f3f3;
            color: #777;
        }

        .role-status-inactive .role-status-dot {
            background: #999;
        }


        /* =====================================================
           CURRENT ROLE
           ===================================================== */

        .role-badge {
            display: inline-block;
            padding: 6px 11px;
            border-radius: 6px;
            background: #f1f1f1;
            color: #444;
            font-size: 12px;
            font-weight: 600;
        }


        /* =====================================================
           CHANGE ROLE
           ===================================================== */

        .role-change {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .role-change select {
            height: 36px;
            min-width: 140px;
            border: 1px solid #d8d8d8;
            border-radius: 6px;
            padding: 0 10px;
            background: white;
            font-size: 13px;
            cursor: pointer;
        }

        .role-change select:focus {
            outline: none;
            border-color: #ff7a00;
        }

        .role-change select:disabled {
            background: #f3f3f3;
            color: #888;
            cursor: not-allowed;
        }

        .role-update-btn {
            height: 36px;
            padding: 0 14px;
            border: none;
            border-radius: 6px;
            background: #333;
            color: white;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
        }

        .role-update-btn:hover {
            background: #111;
        }

        .role-update-btn:disabled {
            background: #999;
            color: #eee;
            cursor: not-allowed;
        }


        /* =====================================================
           EMPTY
           ===================================================== */

        .role-empty {
            text-align: center;
            padding: 60px 20px !important;
            color: #888;
        }

        .role-empty-icon {
            font-size: 40px;
            margin-bottom: 10px;
        }

        .role-empty strong {
            display: block;
            font-size: 16px;
            color: #555;
            margin-bottom: 5px;
        }

        .role-empty span {
            font-size: 13px;
        }


        /* =====================================================
           RESPONSIVE
           ===================================================== */

        @media (max-width: 768px) {

            .role-page {
                padding: 25px 15px;
            }

            .role-search-form {
                flex-direction: column;
                align-items: stretch;
            }

            .role-search-btn,
            .role-reset-btn {
                width: 100%;
                justify-content: center;
                box-sizing: border-box;
            }
        }

    </style>

</head>


<body>


<!-- =========================================================
     NAVBAR
     ========================================================= -->

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


<!-- =========================================================
     MAIN
     ========================================================= -->

<main class="role-page">


    <!-- =====================================================
         HEADER
         ===================================================== -->

    <div class="role-header">

        <div>

            <h1>Role Management</h1>

            <p>
                Search user accounts and manage their assigned roles.
            </p>

        </div>

    </div>


    <!-- =====================================================
         ERROR MESSAGE
         ===================================================== -->

    <% if (request.getAttribute("error") != null) { %>

        <div class="role-message role-error">

            <%= request.getAttribute("error") %>

        </div>

    <% } %>


    <!-- =====================================================
         SUCCESS MESSAGE
         ===================================================== -->

    <% if (request.getAttribute("success") != null) { %>

        <div class="role-message role-success">

            <%= request.getAttribute("success") %>

        </div>

    <% } %>


    <!-- =====================================================
         SEARCH USER
         ===================================================== -->

    <section class="role-search-box">

        <div class="role-search-title">

            <h2>Search User</h2>

            <p>
                Enter any keyword from the user's Gmail address.
            </p>

        </div>


        <form
            action="${pageContext.request.contextPath}/admin/roles"
            method="post"
            class="role-search-form">

            <input
                type="hidden"
                name="action"
                value="search">


            <div class="role-input-wrapper">

                <input
                    type="text"
                    name="email"
                    value="<%= request.getAttribute("searchEmail") != null
                            ? request.getAttribute("searchEmail")
                            : "" %>"
                    placeholder="Search by Gmail keyword..."
                    autocomplete="off">

            </div>


            <button
                type="submit"
                class="role-search-btn">

                Search

            </button>


            <a
                href="${pageContext.request.contextPath}/admin/roles"
                class="role-reset-btn">

                Reset

            </a>

        </form>

    </section>


    <!-- =====================================================
         USER LIST
         ===================================================== -->

    <section class="role-users-box">


        <div class="role-users-header">

            <h2>User Accounts</h2>

            <p>
                Select a role and update the account directly.
            </p>

        </div>


        <%

            List<UserRoleInfo> users =
                    (List<UserRoleInfo>)
                    request.getAttribute("users");

            List<Role> roles =
                    (List<Role>)
                    request.getAttribute("roles");

        %>


        <div class="role-table-wrapper">

            <table class="role-table">


                <!-- =================================================
                     TABLE HEADER
                     ================================================= -->

                <thead>

                    <tr>

                        <th>
                            User
                        </th>

                        <th>
                            Email
                        </th>

                        <th>
                            Phone
                        </th>

                        <th>
                            Status
                        </th>

                        <th>
                            Current Role
                        </th>

                        <th>
                            Change Role
                        </th>

                    </tr>

                </thead>


                <!-- =================================================
                     TABLE BODY
                     ================================================= -->

                <tbody>


                <% if (users != null && !users.isEmpty()) { %>


                    <% for (UserRoleInfo user : users) { %>


                        <tr>


                            <!-- =====================================
                                 USER
                                 ===================================== -->

                            <td>

                                <div class="role-user">


                                    <div class="role-avatar">

                                        <%= user.getFullName() != null
                                                && !user.getFullName().isEmpty()
                                                ? user.getFullName()
                                                    .substring(0, 1)
                                                    .toUpperCase()
                                                : "U" %>

                                    </div>


                                    <div>

                                        <div class="role-user-name">

                                            <%= user.getFullName() != null
                                                    ? user.getFullName()
                                                    : "-" %>

                                        </div>


                                        <div class="role-user-id">

                                            ID #<%= user.getUserId() %>

                                        </div>

                                    </div>


                                </div>

                            </td>


                            <!-- =====================================
                                 EMAIL
                                 ===================================== -->

                            <td>

                                <span class="role-email">

                                    <%= user.getEmail() != null
                                            ? user.getEmail()
                                            : "-" %>

                                </span>

                            </td>


                            <!-- =====================================
                                 PHONE
                                 ===================================== -->

                            <td>

                                <%= user.getPhone() != null
                                        && !user.getPhone().isEmpty()
                                        ? user.getPhone()
                                        : "-" %>

                            </td>


                            <!-- =====================================
                                 STATUS
                                 ===================================== -->

                            <td>

                                <%

                                    boolean active =
                                            "active".equalsIgnoreCase(
                                                    user.getStatus()
                                            );

                                %>


                                <span class="role-status
                                    <%= active
                                            ? "role-status-active"
                                            : "role-status-inactive" %>">


                                    <span class="role-status-dot"></span>


                                    <%= user.getStatus() != null
                                            ? user.getStatus()
                                            : "Unknown" %>


                                </span>

                            </td>


                            <!-- =====================================
                                 CURRENT ROLE
                                 ===================================== -->

                            <td>

                                <span class="role-badge">

                                    <%= user.getRoleName() != null
                                            ? user.getRoleName()
                                            : "No Role" %>

                                </span>

                            </td>


                            <!-- =====================================
                                 CHANGE ROLE
                                 ===================================== -->

                            <td>


                                <%

                                    /*
                                     * Admin account is protected.
                                     * If current role is ADMIN:
                                     * - Disable dropdown
                                     * - Show Protected button
                                     * - Do NOT show Update button
                                     */

                                    boolean isAdmin =
                                            user.getRoleName() != null
                                            && "ADMIN".equalsIgnoreCase(
                                                    user.getRoleName()
                                            );

                                %>


                                <form
                                    action="${pageContext.request.contextPath}/admin/roles"
                                    method="post"
                                    class="role-change">


                                    <input
                                        type="hidden"
                                        name="action"
                                        value="update">


                                    <input
                                        type="hidden"
                                        name="userId"
                                        value="<%= user.getUserId() %>">


                                    <!-- ROLE DROPDOWN -->

                                    <select
                                        name="roleId"
                                        <%= isAdmin
                                                ? "disabled"
                                                : "" %>>


                                        <% if (roles != null) { %>


                                            <% for (Role role : roles) { %>


                                                <option
                                                    value="<%= role.getRoleId() %>"
                                                    <%= role.getRoleId()
                                                            == user.getRoleId()
                                                            ? "selected"
                                                            : "" %>>

                                                    <%= role.getRoleName() %>

                                                </option>


                                            <% } %>


                                        <% } %>


                                    </select>


                                    <!-- =================================
                                         ONLY ONE BUTTON
                                         ================================= -->

                                    <% if (isAdmin) { %>


                                        <!-- ADMIN -->

                                        <button
                                            type="button"
                                            class="role-update-btn"
                                            disabled>

                                            Protected

                                        </button>


                                    <% } else { %>


                                        <!-- NORMAL USER -->

                                        <button
                                            type="submit"
                                            class="role-update-btn">

                                            Update

                                        </button>


                                    <% } %>


                                </form>


                            </td>


                        </tr>


                    <% } %>


                <% } else { %>


                    <!-- =========================================
                         NO USER FOUND
                         ========================================= -->

                    <tr>

                        <td
                            colspan="6"
                            class="role-empty">


                            <div class="role-empty-icon">
                                👤
                            </div>


                            <strong>
                                No users found
                            </strong>


                            <span>
                                Try another Gmail keyword.
                            </span>


                        </td>

                    </tr>


                <% } %>


                </tbody>


            </table>

        </div>

    </section>


</main>


</body>

</html>