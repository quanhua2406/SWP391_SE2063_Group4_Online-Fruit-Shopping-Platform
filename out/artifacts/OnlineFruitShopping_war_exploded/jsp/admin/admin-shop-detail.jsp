<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.shop.AdminShopDAO.ShopInfo"%>
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

    private String messageText(String code) {
        if (code == null) {
            return "";
        }

        switch (code) {
            case "blocked":
                return "Shop blocked. The owner is locked and its products are hidden.";
            case "unblocked":
                return "Shop unblocked. The owner is active and its products are published again.";
            case "already-blocked":
                return "This shop is already blocked.";
            case "not-blocked":
                return "This shop is not blocked.";
            case "notfound":
                return "Shop not found.";
            case "failed":
                return "The shop status could not be changed. Please try again.";
            default:
                return "";
        }
    }
%>

<%
    ShopInfo shop = (ShopInfo) request.getAttribute("shop");

    String message = messageText(
            request.getAttribute("message") == null
                    ? ""
                    : String.valueOf(request.getAttribute("message"))
    );

    String contextPath = request.getContextPath();

    SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm");

    String shopStatus = shop.getStatus() == null
            ? ""
            : shop.getStatus();

    String ownerStatus = shop.getOwnerStatus() == null
            ? ""
            : shop.getOwnerStatus();

    boolean suspended = "suspended".equalsIgnoreCase(shopStatus);

    String shopStatusClass = suspended
            ? "status-suspended"
            : "active".equalsIgnoreCase(shopStatus)
                ? "status-active"
                : "status-other";

    String ownerStatusClass = "active".equalsIgnoreCase(ownerStatus)
            ? "status-active"
            : "locked".equalsIgnoreCase(ownerStatus)
                ? "status-locked"
                : "status-other";

    String created = shop.getCreatedAt() == null
            ? "-"
            : dateFormat.format(shop.getCreatedAt());
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Shop Profile - FreshFruit</title>

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
            max-width: 1000px;
            margin: 35px auto;
        }

        .page-header {
            margin-bottom: 24px;
        }

        .page-header a {
            display: inline-block;
            margin-bottom: 14px;
            color: #ff7a00;
            text-decoration: none;
            font-size: 14px;
            font-weight: 600;
        }

        .page-header a:hover {
            text-decoration: underline;
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

        /* NOTICE */
        .notice {
            padding: 14px 18px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-size: 14px;
        }

        .notice-success {
            background: #e8f7ed;
            color: #218838;
            border: 1px solid #c7ead2;
        }

        /* CARD */
        .card {
            padding: 26px;
            margin-bottom: 24px;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
        }

        .card h2 {
            margin: 0 0 18px;
            color: #333;
            font-size: 19px;
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px 28px;
        }

        .info-item .label {
            display: block;
            margin-bottom: 6px;
            color: #888;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
        }

        .info-item .value {
            color: #333;
            font-size: 15px;
            word-break: break-word;
        }

        .span-2 {
            grid-column: 1 / -1;
        }

        /* STATUS BADGES */
        .status-badge {
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

        .status-suspended {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-locked {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-other {
            background: #f3f3f3;
            color: #777;
        }

        /* BUTTONS */
        .btn {
            min-height: 44px;
            padding: 0 20px;
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

        .btn-block {
            background: #dc2626;
            color: #fff;
        }

        .btn-block:hover {
            background: #b91c1c;
        }

        .btn-unblock {
            background: #16a34a;
            color: #fff;
        }

        .btn-unblock:hover {
            background: #15803d;
        }

        .action-row {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
            margin-top: 22px;
        }

        .hint {
            color: #888;
            font-size: 13px;
        }

        @media (max-width: 700px) {
            .info-grid {
                grid-template-columns: 1fr;
            }

            .navbar {
                align-items: flex-start;
                flex-direction: column;
                padding: 18px 5%;
            }
        }
    </style>
</head>

<body>

<header class="navbar">
    <div class="logo">🍊 FreshFruit</div>

    <nav class="nav-actions">
        <a href="<%=contextPath%>/admin/dashboard">Dashboard</a>
        <a href="<%=contextPath%>/admin/shops">Shops</a>
        <a href="<%=contextPath%>/logout">Logout</a>
    </nav>
</header>

<main class="container">

    <section class="page-header">
        <a href="<%=contextPath%>/admin/shops">&larr; Back to shop list</a>

        <h1>Shop Profile</h1>
        <p>Profile of a registered shop owner store.</p>
    </section>

    <% if (!message.isEmpty()) { %>
        <div class="notice notice-success" role="status">
            <%=escapeHtml(message)%>
        </div>
    <% } %>

    <!-- SHOP INFO -->
    <section class="card">

        <h2>Shop Information</h2>

        <div class="info-grid">

            <div class="info-item">
                <span class="label">Shop ID</span>
                <span class="value">#<%=shop.getShopId()%></span>
            </div>

            <div class="info-item">
                <span class="label">Shop Name</span>
                <span class="value"><%=escapeHtml(shop.getShopName())%></span>
            </div>

            <div class="info-item">
                <span class="label">Shop Status</span>
                <span class="value">
                    <span class="status-badge <%=shopStatusClass%>">
                        <%=shopStatus.isBlank()
                                ? "Unknown"
                                : escapeHtml(shopStatus)%>
                    </span>
                </span>
            </div>

            <div class="info-item">
                <span class="label">Created At</span>
                <span class="value"><%=escapeHtml(created)%></span>
            </div>

            <div class="info-item span-2">
                <span class="label">Description</span>
                <span class="value">
                    <%=shop.getDescription() == null
                            || shop.getDescription().isBlank()
                                ? "-"
                                : escapeHtml(shop.getDescription())%>
                </span>
            </div>

            <div class="info-item">
                <span class="label">Products</span>
                <span class="value">
                    <%=shop.getPublishedCount()%> published /
                    <%=shop.getProductCount()%> total
                </span>
            </div>

        </div>

    </section>

    <!-- OWNER INFO -->
    <section class="card">

        <h2>Owner Information</h2>

        <div class="info-grid">

            <div class="info-item">
                <span class="label">Owner ID</span>
                <span class="value">#<%=shop.getOwnerId()%></span>
            </div>

            <div class="info-item">
                <span class="label">Full Name</span>
                <span class="value"><%=escapeHtml(shop.getOwnerName())%></span>
            </div>

            <div class="info-item">
                <span class="label">Email</span>
                <span class="value"><%=escapeHtml(shop.getOwnerEmail())%></span>
            </div>

            <div class="info-item">
                <span class="label">Account Status</span>
                <span class="value">
                    <span class="status-badge <%=ownerStatusClass%>">
                        <%=ownerStatus.isBlank()
                                ? "Unknown"
                                : escapeHtml(ownerStatus)%>
                    </span>
                </span>
            </div>

        </div>

        <!-- BLOCK / UNBLOCK -->
        <div class="action-row">

            <% if (suspended) { %>

                <form method="post"
                      action="<%=contextPath%>/admin/shops"
                      onsubmit="return confirm('Unblock this shop and publish its products again?');">

                    <input type="hidden" name="action" value="unblock">
                    <input type="hidden" name="shopId"
                           value="<%=shop.getShopId()%>">

                    <button type="submit" class="btn btn-unblock">
                        Unblock Shop
                    </button>

                </form>

                <span class="hint">
                    Unblocking reactivates the owner and publishes the
                    products that the block hid.
                </span>

            <% } else { %>

                <form method="post"
                      action="<%=contextPath%>/admin/shops"
                      onsubmit="return confirm('Block this shop? Its owner is locked and its products are hidden.');">

                    <input type="hidden" name="action" value="block">
                    <input type="hidden" name="shopId"
                           value="<%=shop.getShopId()%>">

                    <button type="submit" class="btn btn-block">
                        Block Shop
                    </button>

                </form>

                <span class="hint">
                    Blocking locks the owner account, suspends the shop and
                    takes every published product off sale.
                </span>

            <% } %>

        </div>

    </section>

</main>

</body>
</html>
