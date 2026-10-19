<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>
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

    private String qty(Object value) {
        if (value == null) {
            return "0";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return number.stripTrailingZeros().toPlainString();
    }

    private String money(Object value) {
        if (value == null) {
            return "0 \u0111";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return String.format(Locale.US, "%,.0f", number) + " \u0111";
    }

    private String date(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String typeLabel(Object value) {
        if (value == null) {
            return "";
        }

        String type = value.toString().toLowerCase();

        switch (type) {
            case "import":
                return "Restock";
            case "export":
                return "Stock Out";
            case "discard":
                return "Discard";
            default:
                return value.toString();
        }
    }

    private String typePill(Object value) {
        if (value == null) {
            return "pill-ok";
        }

        String type = value.toString().toLowerCase();

        switch (type) {
            case "export":
                return "pill-warn";
            case "discard":
                return "pill-danger";
            default:
                return "pill-ok";
        }
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> variants =
            (List<Map<String, Object>>) request.getAttribute("variants");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> variantsPage =
            (Page<Map<String, Object>>) request.getAttribute("variantsPage");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> batchesPage =
            (Page<Map<String, Object>>) request.getAttribute("batchesPage");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> lowStockPage =
            (Page<Map<String, Object>>) request.getAttribute("lowStockPage");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> adjustmentsPage =
            (Page<Map<String, Object>>) request.getAttribute("adjustmentsPage");

    String pageUrl = contextPath + "/store-owner/inventory?";

    // One pager URL per table so each keeps its own filters.
    String stockUrl = QueryUrl.keep(request, pageUrl, "vq", "vweight");
    String batchUrl = QueryUrl.keep(request, pageUrl, "bq", "bstatus");
    String lowUrl = QueryUrl.keep(request, pageUrl, "lq");
    String historyUrl = QueryUrl.keep(request, pageUrl, "hq", "htype");

    // Current filter values, echoed back into the filter forms.
    String vq = request.getParameter("vq") == null ? "" : request.getParameter("vq");
    String vweight = request.getParameter("vweight") == null ? "" : request.getParameter("vweight");

    @SuppressWarnings("unchecked")
    java.util.List<String> weightOptions =
            (java.util.List<String>) request.getAttribute("weightOptions");
    String bq = request.getParameter("bq") == null ? "" : request.getParameter("bq");
    String bstatus = request.getParameter("bstatus") == null ? "" : request.getParameter("bstatus");
    String lq = request.getParameter("lq") == null ? "" : request.getParameter("lq");
    String hq = request.getParameter("hq") == null ? "" : request.getParameter("hq");
    String htype = request.getParameter("htype") == null ? "" : request.getParameter("htype");

    String shopName = request.getAttribute("shopName") == null
            ? ""
            : request.getAttribute("shopName").toString();

    Object threshold = request.getAttribute("threshold");
    String thresholdText = threshold == null ? "0" : threshold.toString();

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    if ("restocked".equals(message)) {
        messageText = "Product restocked successfully.";
    } else if ("updated".equals(message)) {
        messageText = "Stock quantity updated.";
    } else if ("threshold-saved".equals(message)) {
        messageText = "Low stock threshold updated.";
    } else if ("duplicate".equals(message)) {
        level = "error-message";
        messageText = "That batch code already exists for this variant.";
    } else if ("expired".equals(message)) {
        level = "error-message";
        messageText = "The expiry date cannot be in the past.";
    } else if ("invalid".equals(message)) {
        level = "error-message";
        messageText = "Please enter valid stock information.";
    } else if ("forbidden".equals(message)) {
        level = "error-message";
        messageText = "You are not allowed to modify this item.";
    } else if ("failed".equals(message)) {
        level = "error-message";
        messageText = "The action could not be completed.";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Stock Management - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/store-owner/dashboard">
            📊 Dashboard
        </a>

        <a href="<%= contextPath %>/store-owner/pricing">
            💲 Pricing
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Logout
        </a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/store-owner/dashboard">
            ← Shop Owner Dashboard
        </a>
    </p>

    <h1>Stock Management</h1>

    <p class="dashboard-welcome">
        🏪 <%= esc(shopName) %>
    </p>

    <!-- Grouped navigation: every stock function lives on this page -->

    <nav class="section-nav">

        <a href="#restock">📥 Restock</a>
        <a href="#stock">📦 View &amp; Update Stock</a>
        <a href="#low-stock">⚠️ Low Stock</a>
        <a href="#history">🕓 History</a>

    </nav>

    <% if (messageText != null) { %>

        <div class="<%= level %>">
            <%= esc(messageText) %>
        </div>

    <% } %>

    <!-- ==================== RESTOCK ==================== -->

    <div class="panel" id="restock">

        <h2>📥 Restock Products</h2>

        <p class="panel-hint">
            Record a new batch arrival and add it to a variant's
            stock.
        </p>

        <% if (variants == null || variants.isEmpty()) { %>

            <p class="panel-hint">
                Your shop has no product variants yet, so there is
                nothing to restock.
            </p>

        <% } else { %>

            <form method="post"
                  action="<%= contextPath %>/store-owner/inventory">

                <input type="hidden" name="action" value="restock">

                <div class="form-grid">

                    <div class="form-field">
                        <label for="variantId">Product Variant *</label>
                        <select id="variantId" name="variantId" required>
                            <option value="">-- Select variant --</option>
                            <% for (Map<String, Object> v : variants) { %>
                                <option value="<%= esc(v.get("variant_id")) %>">
                                    <%= esc(v.get("product_name")) %>
                                    (<%= esc(v.get("sku")) %>, <%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %>)
                                </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-field">
                        <label for="batchCode">Batch Code *</label>
                        <input type="text" id="batchCode" name="batchCode"
                               maxlength="50" required
                               placeholder="e.g. BATCH-001">
                    </div>

                    <div class="form-field">
                        <label for="quantity">Quantity *</label>
                        <input type="number" id="quantity" name="quantity"
                               min="0.01" step="0.01" required>
                    </div>

                    <div class="form-field">
                        <label for="expiryDate">Expiry Date *</label>
                        <input type="date" id="expiryDate" name="expiryDate"
                               required>
                    </div>

                    <div class="form-field">
                        <label for="reason">Note</label>
                        <input type="text" id="reason" name="reason"
                               maxlength="255"
                               placeholder="Optional note">
                    </div>

                </div>

                <div class="form-actions">
                    <button type="submit" class="btn-primary">
                        Add Stock
                    </button>
                </div>

            </form>

        <% } %>

    </div>

    <!-- ==================== VIEW STOCK ==================== -->

    <div class="panel" id="stock">

        <h2>📦 View Stock Quantity</h2>

        <p class="panel-hint">
            Total active stock per product variant.
        </p>

        <form method="get"
              action="<%= contextPath %>/store-owner/inventory#stock"
              class="filter-form">

            <input type="text" class="filter-input" name="vq"
                   value="<%= esc(vq) %>"
                   placeholder="Search product or SKU...">

            <select class="filter-select" name="vweight">
                <option value="">All weights</option>
                <% if (weightOptions != null) {
                       for (String w : weightOptions) { %>
                    <option value="<%= esc(w) %>"
                        <%= w.equals(vweight) ? "selected" : "" %>>
                        <%= esc(w) %> kg
                    </option>
                <%     }
                   } %>
            </select>

            <button type="submit" class="btn-sm">Filter</button>

            <a class="filter-clear"
               href="<%= contextPath %>/store-owner/inventory#stock">Clear</a>

        </form>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Weight</th>
                    <th>Packaging</th>
                    <th>Price</th>
                    <th>Stock</th>
                </tr>
            </thead>

            <tbody>
            <% if (variantsPage == null || variantsPage.isEmpty()) { %>

                <tr>
                    <td colspan="6">
                        No product variants in your shop yet.
                    </td>
                </tr>

            <% } else {
                   for (Map<String, Object> v : variantsPage.getItems()) {
                       double stock = Double.parseDouble(qty(v.get("stock")));
                       double limit = threshold == null
                               ? 0
                               : Double.parseDouble(threshold.toString());
            %>
                <tr>
                    <td><%= esc(v.get("product_name")) %></td>
                    <td><%= esc(v.get("sku")) %></td>
                    <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                    <td><%= esc(v.get("packaging_type")) %></td>
                    <td><%= money(v.get("price")) %></td>
                    <td>
                        <% if (stock <= limit) { %>
                            <span class="pill pill-warn">
                                <%= qty(v.get("stock")) %>
                            </span>
                        <% } else { %>
                            <span class="pill pill-ok">
                                <%= qty(v.get("stock")) %>
                            </span>
                        <% } %>
                    </td>
                </tr>
            <%
                   }
               }
            %>
            </tbody>

        </table>

        <%= Pager.render(variantsPage, stockUrl, "vpage") %>

    </div>

    <!-- ==================== UPDATE STOCK ==================== -->

    <div class="panel" id="update">

        <h2>✏️ Update Stock Quantity</h2>

        <p class="panel-hint">
            Adjust the quantity of a specific stock batch. Every
            change is recorded in the inventory history below.
        </p>

        <form method="get"
              action="<%= contextPath %>/store-owner/inventory#update"
              class="filter-form">

            <input type="text" class="filter-input" name="bq"
                   value="<%= esc(bq) %>"
                   placeholder="Search batch, product or SKU...">

            <select class="filter-select" name="bstatus">
                <option value="">All statuses</option>
                <option value="active" <%= "active".equals(bstatus) ? "selected" : "" %>>active</option>
                <option value="expired" <%= "expired".equals(bstatus) ? "selected" : "" %>>expired</option>
                <option value="depleted" <%= "depleted".equals(bstatus) ? "selected" : "" %>>depleted</option>
            </select>

            <button type="submit" class="btn-sm">Filter</button>

            <a class="filter-clear"
               href="<%= contextPath %>/store-owner/inventory#update">Clear</a>

        </form>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Batch</th>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Quantity</th>
                    <th>Expiry</th>
                    <th>Received</th>
                    <th>Status</th>
                    <th>Update</th>
                </tr>
            </thead>

            <tbody>
            <% if (batchesPage == null || batchesPage.isEmpty()) { %>

                <tr>
                    <td colspan="8">
                        No stock batches yet.
                    </td>
                </tr>

            <% } else {
                   for (Map<String, Object> b : batchesPage.getItems()) {
            %>
                <tr>
                    <td><%= esc(b.get("batch_code")) %></td>
                    <td><%= esc(b.get("product_name")) %></td>
                    <td><%= esc(b.get("sku")) %></td>
                    <td><%= qty(b.get("quantity")) %></td>
                    <td><%= date(b.get("expiry_date")) %></td>
                    <td><%= date(b.get("received_date")) %></td>
                    <td>
                        <span class="pill <%= "active".equals(b.get("status"))
                                ? "pill-ok" : "pill-danger" %>">
                            <%= esc(b.get("status")) %>
                        </span>
                    </td>
                    <td>
                        <form method="post"
                              action="<%= contextPath %>/store-owner/inventory">

                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="batchId"
                                   value="<%= esc(b.get("batch_id")) %>">

                            <input class="qty-input" type="number"
                                   name="quantity" min="0" step="0.01"
                                   value="<%= qty(b.get("quantity")) %>"
                                   required>

                            <input class="qty-input" type="text"
                                   name="reason" maxlength="255"
                                   placeholder="Reason">

                            <button type="submit" class="btn-sm">
                                Save
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

        <%= Pager.render(batchesPage, batchUrl, "bpage") %>

    </div>

    <!-- ==================== LOW STOCK ==================== -->

    <div class="panel" id="low-stock">

        <h2>⚠️ Low Stock Monitoring</h2>

        <p class="panel-hint">
            Variants at or below the safety threshold of
            <strong><%= esc(thresholdText) %></strong> units.
        </p>

        <form method="post"
              action="<%= contextPath %>/store-owner/inventory"
              class="inline-form">

            <input type="hidden" name="action" value="setThreshold">

            <label for="threshold">Safety threshold</label>

            <input class="qty-input" type="number" id="threshold"
                   name="threshold" min="0" step="1"
                   value="<%= esc(thresholdText) %>" required>

            <button type="submit" class="btn-sm">
                Save Threshold
            </button>

        </form>

        <form method="get"
              action="<%= contextPath %>/store-owner/inventory#low-stock"
              class="filter-form">

            <input type="text" class="filter-input" name="lq"
                   value="<%= esc(lq) %>"
                   placeholder="Search product or SKU...">

            <button type="submit" class="btn-sm">Filter</button>

            <a class="filter-clear"
               href="<%= contextPath %>/store-owner/inventory#low-stock">Clear</a>

        </form>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Weight</th>
                    <th>Packaging</th>
                    <th>Price</th>
                    <th>Stock</th>
                </tr>
            </thead>

            <tbody>
            <% if (lowStockPage == null || lowStockPage.isEmpty()) { %>

                <tr>
                    <td colspan="6">
                        ✅ Everything is well stocked. No low stock
                        items right now.
                    </td>
                </tr>

            <% } else {
                   for (Map<String, Object> v : lowStockPage.getItems()) {
            %>
                <tr>
                    <td><%= esc(v.get("product_name")) %></td>
                    <td><%= esc(v.get("sku")) %></td>
                    <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                    <td><%= esc(v.get("packaging_type")) %></td>
                    <td><%= money(v.get("price")) %></td>
                    <td>
                        <span class="pill pill-warn">
                            <%= qty(v.get("stock")) %>
                        </span>
                    </td>
                </tr>
            <%
                   }
               }
            %>
            </tbody>

        </table>

        <%= Pager.render(lowStockPage, lowUrl, "lpage") %>

    </div>

    <!-- ==================== HISTORY ==================== -->

    <div class="panel" id="history">

        <h2>🕓 Inventory History</h2>

        <p class="panel-hint">
            Every stock change recorded for your shop.
        </p>

        <form method="get"
              action="<%= contextPath %>/store-owner/inventory#history"
              class="filter-form">

            <input type="text" class="filter-input" name="hq"
                   value="<%= esc(hq) %>"
                   placeholder="Search product, SKU, batch or reason...">

            <select class="filter-select" name="htype">
                <option value="">All types</option>
                <option value="import" <%= "import".equals(htype) ? "selected" : "" %>>Restock</option>
                <option value="export" <%= "export".equals(htype) ? "selected" : "" %>>Stock Out</option>
                <option value="discard" <%= "discard".equals(htype) ? "selected" : "" %>>Discard</option>
            </select>

            <button type="submit" class="btn-sm">Filter</button>

            <a class="filter-clear"
               href="<%= contextPath %>/store-owner/inventory#history">Clear</a>

        </form>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Date</th>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Batch</th>
                    <th>Type</th>
                    <th>Quantity</th>
                    <th>Reason</th>
                </tr>
            </thead>

            <tbody>
            <% if (adjustmentsPage == null || adjustmentsPage.isEmpty()) { %>

                <tr>
                    <td colspan="7">
                        No inventory changes yet.
                    </td>
                </tr>

            <% } else {
                   for (Map<String, Object> a : adjustmentsPage.getItems()) {
            %>
                <tr>
                    <td><%= esc(a.get("created_at")) %></td>
                    <td><%= esc(a.get("product_name")) %></td>
                    <td><%= esc(a.get("sku")) %></td>
                    <td><%= esc(a.get("batch_code")) %></td>
                    <td>
                        <span class="pill <%= typePill(a.get("adjustment_type")) %>">
                            <%= esc(typeLabel(a.get("adjustment_type"))) %>
                        </span>
                    </td>
                    <td><%= qty(a.get("quantity")) %></td>
                    <td><%= esc(a.get("reason")) %></td>
                </tr>
            <%
                   }
               }
            %>
            </tbody>

        </table>

        <%= Pager.render(adjustmentsPage, historyUrl, "hpage") %>

    </div>

</section>

</body>
</html>
