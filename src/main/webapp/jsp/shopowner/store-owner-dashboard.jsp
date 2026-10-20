<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.math.BigDecimal"%>

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

    private String num(Object value) {
        if (value == null) {
            return "0";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return number.stripTrailingZeros().toPlainString();
    }
%>

<%
    String contextPath = request.getContextPath();

    String ownerName = request.getAttribute("ownerName") == null
            ? "Shop Owner"
            : request.getAttribute("ownerName").toString();

    Boolean hasShopAttr = (Boolean) request.getAttribute("hasShop");
    boolean hasShop = hasShopAttr != null && hasShopAttr;

    String shopName = request.getAttribute("shopName") == null
            ? null
            : request.getAttribute("shopName").toString();

    String shopStatus = request.getAttribute("shopStatus") == null
            ? ""
            : request.getAttribute("shopStatus").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Shop Owner Dashboard - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<!-- ==================== NAVBAR ==================== -->

<header class="navbar">

    <div class="logo">
        ðŸŠ FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/home">
            ðŸ  Home
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Logout
        </a>

    </nav>

</header>

<!-- ==================== DASHBOARD ==================== -->

<section class="dashboard">

    <h1>Shop Owner Dashboard</h1>

    <p class="dashboard-welcome">
        Welcome back, <%= esc(ownerName) %>!
    </p>

    <% if ("created".equals(shopStatus)) { %>

        <div class="success-message">
            âœ… Your shop was created successfully.
        </div>

    <% } else if ("exists".equals(shopStatus)) { %>

        <div class="success-message">
            â„¹ï¸ You already have a shop.
        </div>

    <% } %>

    <% if (!hasShop) { %>

        <div class="error-message">
            You do not have a shop yet. Create your shop first, then
            you can manage its products, inventory and pricing.
        </div>

        <div class="dashboard-grid">

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸª</div>

                <h3>Create Shop</h3>

                <p>
                    Set up your fruit shop to start managing
                    products, inventory and prices.
                </p>

                <a href="<%= contextPath %>/store-owner/create-shop">
                    + Create Shop
                </a>

            </div>

        </div>

    <% } else { %>

        <p class="breadcrumb">
            ðŸª Your shop:
            <strong><%= esc(shopName) %></strong>
        </p>

        <%
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> stats =
                    (java.util.Map<String, Object>) request.getAttribute("stats");

            Object productCount = stats == null ? null : stats.get("productCount");
            Object variantCount = stats == null ? null : stats.get("variantCount");
            Object totalStock = stats == null ? null : stats.get("totalStock");
            Object lowStockCount = stats == null ? null : stats.get("lowStockCount");
            Object outOfStockCount = stats == null ? null : stats.get("outOfStockCount");
        %>

        <div class="summary-grid summary-grid-4">

            <div class="summary-card">
                <span class="summary-label">Products</span>
                <span class="summary-value"><%= esc(num(productCount)) %></span>
            </div>

            <div class="summary-card">
                <span class="summary-label">Variants</span>
                <span class="summary-value"><%= esc(num(variantCount)) %></span>
            </div>

            <div class="summary-card">
                <span class="summary-label">Total Stock</span>
                <span class="summary-value"><%= esc(num(totalStock)) %></span>
            </div>

            <div class="summary-card <%= lowStockCount != null
                    && new BigDecimal(lowStockCount.toString()).signum() > 0
                    ? "summary-warn" : "" %>">
                <span class="summary-label">
                    Low / Out of Stock
                </span>
                <span class="summary-value">
                    <%= esc(num(lowStockCount)) %> /
                    <%= esc(num(outOfStockCount)) %>
                </span>
            </div>

        </div>

        <div class="dashboard-grid">

            <!-- PRODUCT MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸŽ</div>

                <h3>Product Management</h3>

                <p>
                    View your products, their status and the
                    commission the platform keeps on each sale.
                </p>

                <a href="<%= contextPath %>/store-owner/commission">
                    Manage Products
                </a>

            </div>

            <!-- STOCK MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸ“¦</div>

                <h3>Stock Management</h3>

                <p>
                    View and update stock, restock products, watch
                    low stock and see the inventory history.
                </p>

                <a href="<%= contextPath %>/store-owner/inventory">
                    Manage Stock
                </a>

            </div>

            <!-- PRICING MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸ’²</div>

                <h3>Pricing Management</h3>

                <p>
                    Set and edit the selling price of every product
                    variant (package weight in kg).
                </p>

                <a href="<%= contextPath %>/store-owner/pricing">
                    Manage Pricing
                </a>

            </div>

            <!-- COMMISSION REPORT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸ’¼</div>

                <h3>Commission Report</h3>

                <p>
                    See the platform fee you have to pay, broken down
                    by product and by commission %.
                </p>

                <a href="<%= contextPath %>/store-owner/commission-report">
                    View Report
                </a>

            </div>

            <!-- PROMOTION MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸŽ</div>

                <h3>Promotion Management</h3>

                <p>
                    Request limited-time promotional prices for your
                    product variants; an admin approves them before
                    they apply.
                </p>

                <a href="<%= contextPath %>/store-owner/promotions">
                    Manage Promotions
                </a>

            </div>

            <!-- COUPON MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸŽŸï¸</div>

                <h3>Coupon Management</h3>

                <p>
                    Create discount coupons and track their usage.
                </p>

                <a href="#">
                    Manage Coupons
                </a>

            </div>

            <!-- ORDER MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸ“‹</div>

                <h3>Order Management</h3>

                <p>
                    Track and process the orders that contain your
                    products.
                </p>

                <a href="#">
                    Manage Orders
                </a>

            </div>

            <!-- SUBSCRIPTION -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸ”„</div>

                <h3>Subscription</h3>

                <p>
                    Offer recurring fruit boxes and manage
                    subscription plans.
                </p>

                <a href="#">
                    Manage Subscriptions
                </a>

            </div>

            <!-- NOTIFICATIONS -->

            <div class="dashboard-card">

                <div class="dashboard-icon">ðŸ””</div>

                <h3>Notifications</h3>

                <p>
                    Send and review notifications for your
                    customers.
                </p>

                <a href="#">
                    Manage Notifications
                </a>

            </div>

        </div>

    <% } %>

</section>

</body>
</html>
