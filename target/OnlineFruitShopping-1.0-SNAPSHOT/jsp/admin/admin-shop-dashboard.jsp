<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.shop.AdminShopDAO.ShopStatistics"%>
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
%>

<%
    ShopStatistics stats = (ShopStatistics) request.getAttribute("stats");

    if (stats == null) {
        stats = new ShopStatistics();
    }

    String contextPath = request.getContextPath();

    SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Shop Dashboard - FreshFruit</title>

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
            max-width: 1200px;
            margin: 35px auto;
        }

        .page-header {
            margin-bottom: 26px;
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

        /* STAT CARDS */
        .stat-grid {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 20px;
            margin-bottom: 28px;
        }

        .stat-card {
            padding: 22px;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-left: 5px solid #2e7d32;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
        }

        .stat-card.is-suspended {
            border-left-color: #dc2626;
        }

        .stat-card.is-pending {
            border-left-color: #f59e0b;
        }

        .stat-card.is-products {
            border-left-color: #1976d2;
        }

        .stat-label {
            display: block;
            margin-bottom: 10px;
            color: #888;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: .4px;
        }

        .stat-value {
            color: #222;
            font-size: 34px;
            font-weight: 700;
            line-height: 1;
        }

        .stat-hint {
            display: block;
            margin-top: 8px;
            color: #999;
            font-size: 12px;
        }

        /* SECONDARY PANEL */
        .panel {
            padding: 24px;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
        }

        .panel h2 {
            margin: 0 0 6px;
            color: #333;
            font-size: 19px;
        }

        .panel > p {
            margin: 0 0 20px;
            color: #888;
            font-size: 13px;
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

        .btn-primary {
            background: #ff7a00;
            color: #fff;
        }

        .btn-primary:hover {
            background: #256628;
        }

        .btn-secondary {
            background: #f1f1f1;
            color: #555;
        }

        .btn-secondary:hover {
            background: #e5e5e5;
        }

        .button-row {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }

        @media (max-width: 900px) {
            .stat-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 600px) {
            .navbar {
                align-items: flex-start;
                flex-direction: column;
                padding: 18px 5%;
            }

            .stat-grid {
                grid-template-columns: 1fr;
            }

            .button-row .btn {
                width: 100%;
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
        <h1>Shop Management Dashboard</h1>
        <p>
            An overview of every shop on the platform, taken from the
            shop-owner management screen.
        </p>
    </section>

    <!-- STAT CARDS -->
    <section class="stat-grid">

        <div class="stat-card">
            <span class="stat-label">Total Shops</span>
            <span class="stat-value"><%=stats.getTotalShops()%></span>
            <span class="stat-hint">Registered stores</span>
        </div>

        <div class="stat-card">
            <span class="stat-label">Active Shops</span>
            <span class="stat-value"><%=stats.getActiveShops()%></span>
            <span class="stat-hint">Open and selling</span>
        </div>

        <div class="stat-card is-suspended">
            <span class="stat-label">Suspended Shops</span>
            <span class="stat-value"><%=stats.getSuspendedShops()%></span>
            <span class="stat-hint">Blocked by an admin</span>
        </div>

        <div class="stat-card is-pending">
            <span class="stat-label">Pending Shops</span>
            <span class="stat-value"><%=stats.getPendingShops()%></span>
            <span class="stat-hint">Waiting for review</span>
        </div>

        <div class="stat-card is-products">
            <span class="stat-label">Products On Sale</span>
            <span class="stat-value"><%=stats.getPublishedProducts()%></span>
            <span class="stat-hint">Published across all shops</span>
        </div>

        <div class="stat-card is-suspended">
            <span class="stat-label">Products Hidden By Admin</span>
            <span class="stat-value"><%=stats.getHiddenProducts()%></span>
            <span class="stat-hint">Taken off sale by a block</span>
        </div>

    </section>

    <!-- ACTIONS -->
    <section class="panel">

        <h2>Manage Shops</h2>
        <p>
            Open the shop list to search, view a shop profile, or block
            and unblock a store owner.
        </p>

        <div class="button-row">

            <a class="btn btn-primary"
               href="<%=contextPath%>/admin/shops">
                View Shop List
            </a>

            <a class="btn btn-secondary"
               href="<%=contextPath%>/admin/shops?status=suspended">
                View Blocked Shops
            </a>

        </div>

    </section>

</main>

</body>
</html>
