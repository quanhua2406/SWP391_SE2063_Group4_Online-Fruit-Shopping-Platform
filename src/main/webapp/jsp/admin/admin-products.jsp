
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.TreeSet"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>

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

    private String statusPill(Object value) {
        String s = value == null ? "" : value.toString();

        switch (s) {
            case "published":
                return "pill-ok";
            case "hidden":
                return "pill-danger";
            default:
                return "pill-warn";
        }
    }
%>

<%
    String contextPath = request.getContextPath();

    String keyword = request.getAttribute("keyword") == null
            ? ""
            : request.getAttribute("keyword").toString();

    String statusFilter = request.getParameter("status") == null
            ? ""
            : request.getParameter("status");

    String categoryFilter = request.getParameter("category") == null
            ? ""
            : request.getParameter("category");

    TreeSet<String> categoryOptions =
            (TreeSet<String>) request.getAttribute("categoryOptions");

    Page<Map<String, Object>> productsPage =
            (Page<Map<String, Object>>) request.getAttribute("productsPage");

    String pagerUrl = request.getAttribute("pagerUrl") == null
            ? contextPath + "/admin/product-management?"
            : request.getAttribute("pagerUrl").toString();

    String message = request.getParameter("message") == null
            ? ""
            : request.getParameter("message").trim();

    String messageLevel = "success-message";
    String messageText = null;

    switch (message) {
        case "created":
            messageText = "Product created successfully (status: draft).";
            break;
        case "updated":
            messageText = "Product updated successfully.";
            break;
        case "deleted":
            messageText = "Product deleted.";
            break;
        case "has_orders":
            messageLevel = "error-message";
            messageText = "This product is used in orders and cannot be deleted. Hide it instead.";
            break;
        case "status_published":
            messageText = "Product approved and published.";
            break;
        case "status_hidden":
            messageText = "Product hidden from the storefront.";
            break;
        case "status_draft":
            messageText = "Product moved back to draft.";
            break;
        case "notfound":
            messageLevel = "error-message";
            messageText = "Product not found.";
            break;
        case "invalid":
            messageLevel = "error-message";
            messageText = "Invalid request.";
            break;
        case "failed":
            messageLevel = "error-message";
            messageText = "The action could not be completed.";
            break;
        default:
            break;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Product Management - FreshFruit</title>

    <link rel="stylesheet"
          href="<%=contextPath%>/css/style.css">

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

        .search-form select {
            height: 56px;
            padding: 0 14px;
            border: 1px solid #d5d5d5;
            border-radius: 10px;
            background: #fff;
            color: #263238;
            font-size: 15px;
        }

        .search-form select:focus {
            outline: none;
            border-color: #2e7d32;
            box-shadow: 0 0 0 3px rgba(46, 125, 50, .10);
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
            background: #fff;
            color: #374151;
            border: 1px solid #cfd8cb;
        }

        .btn-secondary:hover {
            background: #f1f5f0;
            border-color: #b3c1b0;
            color: #2e7d32;
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

        .pager {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 14px;
            flex-wrap: wrap;
            margin: 0 30px 26px;
            padding-top: 16px;
            border-top: 1px solid #edf0ed;
        }

        .page-info {
            font-size: 13px;
            color: #6b7a6b;
        }

        .pager-links {
            display: flex;
            align-items: center;
            gap: 6px;
            flex-wrap: wrap;
        }

        .pager-links a,
        .pager-current,
        .pager-disabled {
            min-width: 34px;
            padding: 6px 10px;
            border: 1px solid #dbe5d8;
            border-radius: 8px;
            font-size: 13px;
            line-height: 1;
            text-align: center;
            text-decoration: none;
        }

        .pager-links a {
            color: #2e7d32;
            background: #fff;
        }

        .pager-links a:hover {
            background: #eef8eb;
            border-color: #bcdcb4;
        }

        .pager-current {
            background: #2e7d32;
            border-color: #2e7d32;
            color: #fff;
            font-weight: 700;
        }

        .pager-disabled {
            color: #b3bdb1;
            background: #f7f9f6;
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

            .search-form select {
                width: 100%;
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
        <a href="<%=contextPath%>/admin/category-management">Categories</a>
        <a href="<%=contextPath%>/logout">Logout</a>
    </nav>
</header>

<main class="container">

    <section class="page-header">
        <h1>Product Management</h1>
        <p>View and manage products in the FreshFruit system.</p>
    </section>

    <% if (messageText != null) { %>
        <div class="<%=messageLevel%>" style="margin-bottom:24px">
            <%=escapeHtml(messageText)%>
        </div>
    <% } %>

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

            <select name="status">
                <option value="">All statuses</option>
                <option value="draft"
                        <%= "draft".equals(statusFilter) ? "selected" : "" %>>
                    draft
                </option>
                <option value="published"
                        <%= "published".equals(statusFilter) ? "selected" : "" %>>
                    published
                </option>
                <option value="hidden"
                        <%= "hidden".equals(statusFilter) ? "selected" : "" %>>
                    hidden
                </option>
            </select>

            <select name="category">
                <option value="">All categories</option>
                <% if (categoryOptions != null) {
                       for (String name : categoryOptions) { %>
                    <option value="<%=escapeHtml(name)%>"
                            <%= name.equals(categoryFilter) ? "selected" : "" %>>
                        <%=escapeHtml(name)%>
                    </option>
                <%     }
                   } %>
            </select>

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
                        <%=productsPage == null ? 0 : productsPage.getTotal()%>
                    </span>
                </p>
            </div>

            <div style="display:flex; gap:10px; flex-wrap:wrap">
                <a class="btn btn-secondary"
                   href="<%=contextPath%>/admin/category-management">
                    ðŸ“‚ Categories
                </a>

                <a class="btn btn-primary"
                   href="<%=contextPath%>/admin/product-add">
                    + Add Product
                </a>
            </div>
        </div>

        <div class="table-scroll">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Category</th>
                        <th>Shop</th>
                        <th>Origin</th>
                        <th>Status</th>
                        <th>Commission %</th>
                        <th>Price</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <% if (productsPage != null && !productsPage.isEmpty()) {
                        for (Map<String, Object> product : productsPage.getItems()) {

                            Object productId = product.get("product_id");
                            String status = product.get("status") == null
                                    ? "draft"
                                    : product.get("status").toString();
                    %>
                        <tr>
                            <td><%=escapeHtml(productId)%></td>
                            <td><%=escapeHtml(product.get("product_name"))%></td>
                            <td><%=escapeHtml(product.get("category_name"))%></td>
                            <td><%=escapeHtml(product.get("shop_name"))%></td>
                            <td><%=escapeHtml(product.get("origin"))%></td>
                            <td>
                                <span class="pill <%=statusPill(status)%>">
                                    <%=escapeHtml(status)%>
                                </span>
                            </td>
                            <td><%=escapeHtml(product.get("commission_rate"))%></td>
                            <td><%=escapeHtml(product.get("price"))%></td>
                            <td>
                                <div class="table-actions">

                                    <a class="btn-sm btn-sm-secondary"
                                       href="<%=contextPath%>/admin/product-edit?id=<%=escapeHtml(productId)%>">
                                        Edit
                                    </a>

                                    <a class="btn-sm btn-sm-info"
                                       href="<%=contextPath%>/admin/product-variants?productId=<%=escapeHtml(productId)%>">
                                        Variants
                                    </a>

                                    <% if (!"published".equals(status)) { %>
                                        <form method="post"
                                              action="<%=contextPath%>/admin/product-status">
                                            <input type="hidden" name="productId"
                                                   value="<%=escapeHtml(productId)%>">
                                            <input type="hidden" name="status"
                                                   value="published">
                                            <button type="submit" class="btn-sm">
                                                Approve
                                            </button>
                                        </form>
                                    <% } %>

                                    <% if (!"hidden".equals(status)) { %>
                                        <form method="post"
                                              action="<%=contextPath%>/admin/product-status">
                                            <input type="hidden" name="productId"
                                                   value="<%=escapeHtml(productId)%>">
                                            <input type="hidden" name="status"
                                                   value="hidden">
                                            <button type="submit"
                                                    class="btn-sm btn-sm-warning">
                                                Hide
                                            </button>
                                        </form>
                                    <% } %>

                                    <form method="post"
                                          action="<%=contextPath%>/admin/product-delete"
                                          onsubmit="return confirm('Delete this product and its variants/stock?');">
                                        <input type="hidden" name="productId"
                                               value="<%=escapeHtml(productId)%>">
                                        <button type="submit"
                                                class="btn-sm btn-sm-danger">
                                            Delete
                                        </button>
                                    </form>

                                </div>
                            </td>
                        </tr>
                    <%
                        }
                    } else {
                    %>
                        <tr>
                            <td class="empty-cell" colspan="9">
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

        <%=Pager.render(productsPage, pagerUrl, "page")%>

    </section>

</main>

</body>
</html>
