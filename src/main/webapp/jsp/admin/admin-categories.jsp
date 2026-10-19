<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>

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

    List<Map<String, Object>> categories =
            (List<Map<String, Object>>) request.getAttribute("categories");

    Page<Map<String, Object>> categoriesPage =
            (Page<Map<String, Object>>) request.getAttribute("categoriesPage");

    String pageUrl = contextPath + "/admin/categories?";

    String pagerUrl = QueryUrl.keep(request, pageUrl, "q");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    switch (message) {
        case "created":
            messageText = "Category added successfully.";
            break;
        case "updated":
            messageText = "Category updated successfully.";
            break;
        case "deleted":
            messageText = "Category deleted.";
            break;
        case "duplicate":
            level = "error-message";
            messageText = "A category with that name already exists.";
            break;
        case "in_use":
            level = "error-message";
            messageText = "This category still has products and cannot be deleted.";
            break;
        case "has_children":
            level = "error-message";
            messageText = "This category has sub-categories and cannot be deleted.";
            break;
        case "invalid":
            level = "error-message";
            messageText = "Please enter a valid category name.";
            break;
        case "failed":
            level = "error-message";
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

    <title>Manage Categories - FreshFruit</title>

    <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">🍊 FreshFruit</div>

    <nav class="nav-actions">
        <a href="<%= contextPath %>/admin/dashboard">📊 Dashboard</a>
        <a href="<%= contextPath %>/admin/product-management">🍎 Products</a>
        <a href="<%= contextPath %>/logout" class="logout">Logout</a>
    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">
            ← Admin Dashboard
        </a>
    </p>

    <h1>Manage Categories</h1>

    <p class="dashboard-welcome">
        Categories group products. Products need a category before
        they can be created.
    </p>

    <% if (messageText != null) { %>
        <div class="<%= level %>"><%= esc(messageText) %></div>
    <% } %>

    <!-- ==================== ADD ==================== -->

    <div class="panel">

        <h2>➕ Add Category</h2>

        <p class="panel-hint">Create a new product category.</p>

        <form method="post" action="<%= contextPath %>/admin/categories">

            <input type="hidden" name="action" value="add">

            <div class="form-grid">

                <div class="form-field">
                    <label for="categoryName">Category Name *</label>
                    <input type="text" id="categoryName" name="categoryName"
                           maxlength="100" required
                           placeholder="e.g. Fresh Fruits">
                </div>

                <div class="form-field">
                    <label for="parentId">Parent Category</label>
                    <select id="parentId" name="parentId">
                        <option value="">-- None (top level) --</option>
                        <% if (categories != null) {
                               for (Map<String, Object> c : categories) { %>
                            <option value="<%= esc(c.get("category_id")) %>">
                                <%= esc(c.get("category_name")) %>
                            </option>
                        <%     }
                           } %>
                    </select>
                </div>

            </div>

            <div class="form-actions">
                <button type="submit" class="btn-primary">
                    Add Category
                </button>
            </div>

        </form>

    </div>

    <!-- ==================== LIST ==================== -->

    <div class="panel">

        <h2>📂 Category List</h2>

        <form method="get"
              action="<%= contextPath %>/admin/categories"
              class="filter-form">

            <input type="text" class="filter-input" name="q"
                   value="<%= esc(q) %>"
                   placeholder="Search category or parent name...">

            <button type="submit" class="btn-sm">Search</button>

            <a class="filter-clear"
               href="<%= contextPath %>/admin/categories">Clear</a>

        </form>

        <table class="data-table">

            <thead>
                <tr>
                    <th>#</th>
                    <th>Name</th>
                    <th>Parent</th>
                    <th>Products</th>
                    <th>Sub-categories</th>
                    <th>Actions</th>
                </tr>
            </thead>

            <tbody>
            <% if (categoriesPage == null || categoriesPage.isEmpty()) { %>

                <tr>
                    <td colspan="6">No categories yet.</td>
                </tr>

            <% } else {
                   for (Map<String, Object> c : categoriesPage.getItems()) {
            %>
                <tr>
                    <td><%= esc(c.get("category_id")) %></td>

                    <td>
                        <form method="post"
                              action="<%= contextPath %>/admin/categories"
                              class="table-actions">

                            <input type="hidden" name="action" value="edit">
                            <input type="hidden" name="categoryId"
                                   value="<%= esc(c.get("category_id")) %>">

                            <input class="qty-input" type="text"
                                   name="categoryName" maxlength="100"
                                   value="<%= esc(c.get("category_name")) %>"
                                   required
                                   style="width:180px">

                            <button type="submit" class="btn-sm">
                                Save
                            </button>

                        </form>
                    </td>

                    <td>
                        <form method="post"
                              action="<%= contextPath %>/admin/categories"
                              class="table-actions">

                            <input type="hidden" name="action" value="edit">
                            <input type="hidden" name="categoryId"
                                   value="<%= esc(c.get("category_id")) %>">
                            <input type="hidden" name="categoryName"
                                   value="<%= esc(c.get("category_name")) %>">

                            <select name="parentId" class="qty-input"
                                    style="width:150px">
                                <option value="">-- None --</option>
                                <% for (Map<String, Object> p : categories) {
                                       if (p.get("category_id")
                                               .equals(c.get("category_id"))) {
                                           continue;
                                       } %>
                                    <option value="<%= esc(p.get("category_id")) %>">
                                        <%= esc(p.get("category_name")) %>
                                    </option>
                                <% } %>
                            </select>

                            <button type="submit" class="btn-sm">
                                Set
                            </button>

                        </form>

                        <small>
                            <%= c.get("parent_name") == null
                                    ? "top level"
                                    : esc(c.get("parent_name")) %>
                        </small>
                    </td>

                    <td><%= esc(c.get("product_count")) %></td>
                    <td><%= esc(c.get("child_count")) %></td>

                    <td>
                        <form method="post"
                              action="<%= contextPath %>/admin/categories"
                              class="table-actions"
                              onsubmit="return confirm('Delete this category?');">

                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="categoryId"
                                   value="<%= esc(c.get("category_id")) %>">

                            <button type="submit" class="btn-sm btn-sm-danger">
                                Delete
                            </button>

                        </form>
                    </td>
                </tr>
            <%
                   }
               }
            %>
            </tbody>

        </table>

        <%= Pager.render(categoriesPage, pagerUrl, "page") %>

    </div>

</section>

</body>
</html>
