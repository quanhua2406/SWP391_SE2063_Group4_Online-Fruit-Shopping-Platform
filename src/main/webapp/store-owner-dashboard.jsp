<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Store Owner Dashboard</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <div class="nav-actions">
        <a href="${pageContext.request.contextPath}/home">
            🏠 Home
        </a>

        <a href="${pageContext.request.contextPath}/logout"
           class="logout">
            Logout
        </a>
    </div>

</header>

<section class="dashboard">

    <h1>Store Owner Dashboard</h1>

    <p class="dashboard-welcome">
        Welcome back, Store Owner!
    </p>

    <div class="dashboard-grid">

        <div class="dashboard-card">
            <div class="dashboard-icon">🍎</div>
            <h3>My Products</h3>
            <p>Add, update and manage your fruit products.</p>
            <a href="${pageContext.request.contextPath}/store-owner/products">Manage Products</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">📁</div>
            <h3>Categories</h3>
            <p>Organize fruit categories, origins and hierarchy.</p>
            <a href="${pageContext.request.contextPath}/store-owner/categories">Manage Categories</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">📦</div>
            <h3>Inventory</h3>
            <p>Monitor stock quantity and product availability.</p>
            <a href="#">Manage Inventory</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">Orders</div>
            <h3>Orders</h3>
            <p>View and process orders containing your products.</p>
            <a href="${pageContext.request.contextPath}/store-owner/orders">View Orders</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">💰</div>
            <h3>Sales</h3>
            <p>Review your sales and business performance.</p>
            <a href="#">View Sales</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">📋</div>
            <h3>Product Approval</h3>
            <p>Check product approval status.</p>
            <a href="#">View Status</a>
        </div>

    </div>

</section>

</body>
</html>