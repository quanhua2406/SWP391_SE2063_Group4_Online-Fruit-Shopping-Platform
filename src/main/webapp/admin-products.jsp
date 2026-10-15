
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%!
    private String escapeHtml(Object value) {
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

    String keyword = request.getAttribute("keyword") == null
            ? ""
            : request.getAttribute("keyword").toString();

    List<Map<String, Object>> products =
            (List<Map<String, Object>>) request.getAttribute("products");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Product Management - FreshFruit</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            background: #f5f7fa;
            font-family: Arial, sans-serif;
            color: #263238;
        }

        .navbar {
            min-height: 70px;
            padding: 16px 5%;
            background: #fff;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 20px;
        }

        .logo {
            color: #2e7d32;
            font-size: 25px;
            font-weight: 700;
        }

        .nav-links {
            display: flex;
            align-items: center;
            gap: 28px;
        }

        .nav-links a {
            color: #263238;
            text-decoration: none;
            font-size: 14px;
        }

        .nav-links a:hover {
            color: #2e7d32;
        }

        .container {
            width: 94%;
            max-width: 1450px;
            margin: 40px auto;
        }

        .page-header {
            margin-bottom: 30px;
        }

        .page-header h1 {
            margin: 0 0 10px;
            font-size: 38px;
            color: #263238;
        }

        .page-header p {
            margin: 0;
            color: #777;
            font-size: 16px;
            line-height: 1.6;
        }

        .search-card,
        .table-card {
            background: #fff;
            border: 1px solid #e1e5e9;
            border-radius: 14px;
            box-shadow: 0 3px 10px rgba(0, 0, 0, .04);
            margin-bottom: 30px;
            overflow: hidden;
        }

        .search-card {
            padding: 28px;
        }

        .search-card h2,
        .table-heading h2 {
            margin: 0;
            font-size: 23px;
            color: #263238;
        }

        .search-form {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-top: 22px;
        }

        .search-form input {
            flex: 1;
            min-width: 0;
            height: 56px;
            padding: 0 18px;
            border: 1px solid #d5d5d5;
            border-radius: 10px;
            font-size: 16px;
        }

        .search-form input:focus {
            outline: none;
            border-color: #2e7d32;
            box-shadow: 0 0 0 3px rgba(46, 125, 50, .10);
        }

        .btn {
            min-height: 48px;
            padding: 0 22px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border: none;
            border-radius: 9px;
            font-size: 14px;
            font-weight: 700;
            text-decoration: none;
            white-space: nowrap;
            cursor: pointer;
            transition: background .2s ease;
        }

        .btn-primary {
            background: #2e7d32;
            color: #fff;
        }

        .btn-primary:hover {
            background: #256628;
        }

        .btn-secondary {
            background: #f0f2f4;
            color: #374151;
        }

        .btn-secondary:hover {
            background: #e2e6e9;
        }

        .table-heading {
            padding: 26px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 20px;
            flex-wrap: wrap;
            border-bottom: 1px solid #eee;
        }

        .table-heading p {
            margin: 10px 0 0;
            color: #888;
            font-size: 15px;
        }

        .count {
            color: #2e7d32;
            font-weight: 700;
        }

        .add-button {
            min-height: 48px;
            padding: 0 22px;
            background: #2e7d32;
            color: #fff;
            border-radius: 9px;
            display: inline-flex;
            justify-content: center;
            align-items: center;
            text-decoration: none;
            font-size: 14px;
            font-weight: 700;
            white-space: nowrap;
        }

        .add-button:hover {
            background: #256628;
        }

        .table-scroll {
            width: 100%;
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            white-space: nowrap;
        }

        thead {
            background: #e8f5e9;
        }

        th {
            padding: 19px 22px;
            text-align: left;
            color: #2e7d32;
            font-size: 13px;
            text-transform: uppercase;
        }

        td {
            padding: 18px 22px;
            border-bottom: 1px solid #edf0ed;
            font-size: 14px;
        }

        tbody tr:hover {
            background: #f7fcf7;
        }

        .empty-cell {
            height: 140px;
            text-align: center;
            color: #777;
            white-space: normal;
        }

        .empty-cell p {
            margin: 0;
        }

        .empty-cell a {
            display: inline-block;
            margin-top: 12px;
            color: #2e7d32;
            font-weight: 700;
            text-decoration: none;
        }

        .empty-cell a:hover {
            text-decoration: underline;
        }

        @media (max-width: 700px) {
            .navbar {
                align-items: flex-start;
                flex-direction: column;
            }

            .nav-links {
                gap: 18px;
                flex-wrap: wrap;
            }

            .container {
                width: 95%;
                margin: 25px auto;
            }

            .page-header h1 {
                font-size: 30px;
            }

            .search-card {
                padding: 20px;
            }

            .search-form {
                align-items: stretch;
                flex-direction: column;
            }

            .search-form input {
                width: 100%;
                flex: auto;
            }

            .search-form .btn {
                width: 100%;
            }

            .table-heading {
                padding: 22px;
                align-items: flex-start;
                flex-direction: column;
            }
        }
    </style>
</head>

<body>

<header class="navbar">
    <div class="logo">FreshFruit</div>

    <nav class="nav-links">
        <a href="<%=contextPath%>/admin/dashboard">Dashboard</a>
        <a href="<%=contextPath%>/admin/user-management">Users</a>
        <a href="<%=contextPath%>/logout">Logout</a>
    </nav>
</header>

<main class="container">

    <section class="page-header">
        <h1>Product Management</h1>
        <p>View and manage products in the FreshFruit system.</p>
    </section>

    <!-- SEARCH PRODUCTS -->
    <section class="search-card">
        <h2>Search Products</h2>

        <form class="search-form"
              method="get"
              action="<%=contextPath%>/admin/product-management">

            <input
                type="text"
                name="keyword"
                value="<%=escapeHtml(keyword)%>"
                placeholder="Search product name, description or ID..."
            >

            <button type="submit" class="btn btn-primary">
                Search
            </button>

            <a class="btn btn-secondary"
               href="<%=contextPath%>/admin/product-management">
                Reset
            </a>
        </form>
    </section>

    <!-- PRODUCT LIST -->
    <section class="table-card">

        <div class="table-heading">
            <div>
                <h2>Product List</h2>

                <p>
                    Results:
                    <span class="count">
                        <%=products == null ? 0 : products.size()%>
                    </span>
                </p>
            </div>

            <a class="add-button"
               href="<%=contextPath%>/admin/product-add">
                + Add Product
            </a>
        </div>

        <div class="table-scroll">
            <table>
                <thead>
                    <tr>
                        <% if (products != null && !products.isEmpty()) {
                            for (String column : products.get(0).keySet()) {
                        %>
                            <th><%=escapeHtml(column)%></th>
                        <%
                            }
                        } else {
                        %>
                            <th>Product Information</th>
                        <% } %>
                    </tr>
                </thead>

                <tbody>
                    <% if (products != null && !products.isEmpty()) {
                        for (Map<String, Object> product : products) {
                    %>
                        <tr>
                            <% for (Object value : product.values()) { %>
                                <td><%=escapeHtml(value)%></td>
                            <% } %>
                        </tr>
                    <%
                        }
                    } else {
                    %>
                        <tr>
                            <td class="empty-cell">
                                <p>No products found.</p>
                                <a href="<%=contextPath%>/admin/product-add">
                                    + Add your first product
                                </a>
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
