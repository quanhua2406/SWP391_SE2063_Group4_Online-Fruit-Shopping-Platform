<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.VariantWeights"%>

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

    private String money(Object value) {
        if (value == null) {
            return "0";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return String.format(Locale.US, "%,.0f", number);
    }
%>

<%
    String contextPath = request.getContextPath();

    Map<String, Object> product =
            (Map<String, Object>) request.getAttribute("product");

    List<Map<String, Object>> variants =
            (List<Map<String, Object>>) request.getAttribute("variants");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> variantsPage =
            (Page<Map<String, Object>>) request.getAttribute("variantsPage");

    if (product == null) {
        response.sendRedirect(
                contextPath + "/admin/product-management");
        return;
    }

    String productId = esc(product.get("product_id"));

    String pageUrl = contextPath
            + "/admin/product-variants?productId=" + productId + "&";

    // The servlet provides a URL that keeps the active filters.
    if (request.getAttribute("pagerUrl") != null) {
        pageUrl = request.getAttribute("pagerUrl").toString();
    }

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String weight = request.getParameter("weight") == null ? "" : request.getParameter("weight");

    @SuppressWarnings("unchecked")
    java.util.List<String> weightOptions =
            (java.util.List<String>) request.getAttribute("weightOptions");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    switch (message) {
        case "created":
            messageText = "Variant added successfully.";
            break;
        case "updated":
            messageText = "Variant updated successfully.";
            break;
        case "deleted":
            messageText = "Variant deleted.";
            break;
        case "duplicate":
            level = "error-message";
            messageText = "That SKU is already used by another variant.";
            break;
        case "has_orders":
            level = "error-message";
            messageText = "This variant is used in orders and cannot be deleted.";
            break;
        case "invalid":
            level = "error-message";
            messageText = "Please check the SKU, weight and price.";
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

    <title>Product Variants - FreshFruit</title>

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

    <h1>Manage Product Variants</h1>

    <p class="dashboard-welcome">
        <%= esc(product.get("product_name")) %>
        (product #<%= productId %>)
    </p>

    <% if (messageText != null) { %>
        <div class="<%= level %>"><%= esc(messageText) %></div>
    <% } %>

    <!-- ==================== ADD VARIANT ==================== -->

    <div class="panel">

        <h2>➕ Add Variant</h2>

        <p class="panel-hint">
            Each variant is one selling package, defined by its
            weight (0.5 kg = half a kilo, 1 kg, 2 kg ...). Every
            package has its own SKU and price.
        </p>

        <form method="post" action="<%= contextPath %>/admin/product-variants">

            <input type="hidden" name="action" value="add">
            <input type="hidden" name="productId" value="<%= productId %>">

            <div class="form-grid">

                <div class="form-field">
                    <label for="sku">SKU *</label>
                    <input type="text" id="sku" name="sku"
                           maxlength="50" required
                           placeholder="e.g. APPLE-0.5KG">
                </div>

                <div class="form-field">
                    <label for="weightValue">Weight (kg) *</label>
                    <input type="number" id="weightValue" name="weightValue"
                           min="0.01" step="0.01" required
                           placeholder="0.5 = half a kilo">
                </div>

                <div class="form-field">
                    <label for="price">Price (đ) *</label>
                    <input type="number" id="price" name="price"
                           min="0.01" step="0.01" required>
                </div>

                <div class="form-field">
                    <label for="packagingType">Packaging Label</label>
                    <input type="text" id="packagingType"
                           name="packagingType" maxlength="50"
                           placeholder="e.g. Bag 500g">
                </div>

            </div>

            <div class="form-actions">
                <button type="submit" class="btn-primary">
                    Add Variant
                </button>
            </div>

        </form>

    </div>

    <!-- ==================== VARIANT LIST ==================== -->

    <div class="panel">

        <h2>📦 Variant List</h2>

        <form method="get"
              action="<%= contextPath %>/admin/product-variants"
              class="filter-form">

            <input type="hidden" name="productId" value="<%= productId %>">

            <input type="text" class="filter-input" name="q"
                   value="<%= esc(q) %>"
                   placeholder="Search SKU or packaging...">

            <select class="filter-select" name="weight">
                <option value="">All weights</option>
                <% if (weightOptions != null) {
                       for (String w : weightOptions) { %>
                    <option value="<%= esc(w) %>"
                        <%= w.equals(weight) ? "selected" : "" %>>
                        <%= esc(w) %> kg
                    </option>
                <%     }
                   } %>
            </select>

            <button type="submit" class="btn-sm">Filter</button>

            <a class="filter-clear"
               href="<%= contextPath %>/admin/product-variants?productId=<%= productId %>">
                Clear
            </a>

        </form>

        <table class="data-table">

            <thead>
                <tr>
                    <th>#</th>
                    <th>SKU</th>
                    <th>Weight</th>
                    <th>Packaging</th>
                    <th>Price (đ)</th>
                    <th>Actions</th>
                </tr>
            </thead>

            <tbody>
            <% if (variantsPage == null || variantsPage.isEmpty()) { %>

                <tr>
                    <td colspan="6">No variants for this product yet.</td>
                </tr>

            <% } else {
                   for (Map<String, Object> v : variantsPage.getItems()) {
            %>
                <tr>
                    <td><%= esc(v.get("variant_id")) %></td>

                    <td>
                        <form method="post"
                              action="<%= contextPath %>/admin/product-variants"
                              class="table-actions">

                            <input type="hidden" name="action" value="edit">
                            <input type="hidden" name="productId" value="<%= productId %>">
                            <input type="hidden" name="variantId"
                                   value="<%= esc(v.get("variant_id")) %>">

                            <input class="qty-input" type="text" name="sku"
                                   maxlength="50" required
                                   value="<%= esc(v.get("sku")) %>"
                                   style="width:130px">

                            <input class="qty-input" type="number"
                                   name="weightValue" min="0.01" step="0.01"
                                   required placeholder="kg"
                                   value="<%= esc(v.get("weight_value")) %>"
                                   style="width:80px">

                            <input class="qty-input" type="number" name="price"
                                   min="0.01" step="0.01" required
                                   value="<%= esc(v.get("price")) %>"
                                   style="width:110px">

                            <input class="qty-input" type="text"
                                   name="packagingType" maxlength="50"
                                   placeholder="packaging"
                                   value="<%= esc(v.get("packaging_type")) %>"
                                   style="width:120px">

                            <button type="submit" class="btn-sm">Save</button>

                        </form>
                    </td>

                    <td><%= esc(VariantWeights.label(
                                v.get("weight_value"), v.get("unit"))) %></td>
                    <td><%= esc(v.get("packaging_type")) %></td>
                    <td><%= money(v.get("price")) %></td>

                    <td>
                        <form method="post"
                              action="<%= contextPath %>/admin/product-variants"
                              class="table-actions"
                              onsubmit="return confirm('Delete this variant and its stock?');">

                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="productId" value="<%= productId %>">
                            <input type="hidden" name="variantId"
                                   value="<%= esc(v.get("variant_id")) %>">

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

        <%= Pager.render(variantsPage, pageUrl, "page") %>

    </div>

</section>

</body>
</html>
