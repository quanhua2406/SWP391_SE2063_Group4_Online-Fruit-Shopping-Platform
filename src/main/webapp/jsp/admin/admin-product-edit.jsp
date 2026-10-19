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

    Map<String, Object> product =
            (Map<String, Object>) request.getAttribute("product");

    List<Map<String, Object>> categories =
            (List<Map<String, Object>>) request.getAttribute("categories");

    String error = request.getAttribute("error") == null
            ? ""
            : request.getAttribute("error").toString();

    if (product == null) {
        response.sendRedirect(
                contextPath + "/admin/product-management");
        return;
    }

    Object categoryId = product.get("category_id");
    String status = product.get("status") == null
            ? "draft"
            : product.get("status").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Edit Product - FreshFruit</title>

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
        <a href="<%= contextPath %>/admin/product-management">
            ← Back to Products
        </a>
    </p>

    <h1>Edit Product</h1>

    <p class="dashboard-welcome">
        Product #<%= esc(product.get("product_id")) %> —
        shop: <strong><%= esc(product.get("shop_name")) %></strong>
    </p>

    <% if (!error.isEmpty()) { %>
        <div class="error-message"><%= esc(error) %></div>
    <% } %>

    <div class="panel">

        <h2>✏️ Product Details</h2>

        <form method="post" action="<%= contextPath %>/admin/product-edit">

            <input type="hidden" name="productId"
                   value="<%= esc(product.get("product_id")) %>">

            <div class="form-grid">

                <div class="form-field" style="grid-column:1/-1">
                    <label for="productName">Product Name *</label>
                    <input type="text" id="productName" name="productName"
                           maxlength="150" required
                           value="<%= esc(product.get("product_name")) %>">
                </div>

                <div class="form-field">
                    <label for="categoryId">Category *</label>
                    <select id="categoryId" name="categoryId" required>
                        <option value="">-- Select category --</option>
                        <% if (categories != null) {
                               for (Map<String, Object> c : categories) { %>
                            <option value="<%= esc(c.get("category_id")) %>"
                                <%= c.get("category_id").equals(categoryId)
                                        ? "selected" : "" %>>
                                <%= esc(c.get("category_name")) %>
                            </option>
                        <%     }
                           } %>
                    </select>
                </div>

                <div class="form-field">
                    <label for="origin">Origin</label>
                    <input type="text" id="origin" name="origin"
                           maxlength="100"
                           value="<%= esc(product.get("origin")) %>">
                </div>

                <div class="form-field">
                    <label for="commissionRate">Commission (%)</label>
                    <input type="number" id="commissionRate"
                           name="commissionRate" min="0" max="100"
                           step="0.01"
                           value="<%= esc(product.get("commission_rate")) %>">
                    <span class="panel-hint">
                        Percentage the platform keeps on each sale.
                    </span>
                </div>

                <div class="form-field">
                    <label for="status">Status *</label>
                    <select id="status" name="status" required>
                        <option value="draft"
                            <%= "draft".equals(status) ? "selected" : "" %>>
                            Draft (not visible)
                        </option>
                        <option value="published"
                            <%= "published".equals(status) ? "selected" : "" %>>
                            Published (approved)
                        </option>
                        <option value="hidden"
                            <%= "hidden".equals(status) ? "selected" : "" %>>
                            Hidden (removed)
                        </option>
                    </select>
                </div>

                <div class="form-field" style="grid-column:1/-1">
                    <label for="description">Description</label>
                    <textarea id="description" name="description"
                              rows="4"
                              style="width:100%; padding:10px 12px; border:1px solid #d9e2d7; border-radius:8px; font-family:inherit; font-size:14px"><%= esc(product.get("description")) %></textarea>
                </div>

            </div>

            <div class="form-actions">
                <button type="submit" class="btn-primary">
                    Save Changes
                </button>

                <a class="btn-sm"
                   style="display:inline-block; margin-left:10px; text-decoration:none"
                   href="<%= contextPath %>/admin/product-variants?productId=<%= esc(product.get("product_id")) %>">
                    Manage Variants
                </a>

                <a href="<%= contextPath %>/admin/product-management"
                   style="margin-left:10px">
                    Cancel
                </a>
            </div>

        </form>

    </div>

</section>

</body>
</html>
