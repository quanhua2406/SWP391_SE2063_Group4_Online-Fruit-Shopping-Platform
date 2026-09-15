<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Customer Dashboard</title>

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

        <a href="#" class="cart-link">
            🛒 Cart
        </a>

        <a href="${pageContext.request.contextPath}/logout"
           class="logout">
            Logout
        </a>
    </div>

</header>

<section class="dashboard">

    <h1>Customer Dashboard</h1>

    <p class="dashboard-welcome">
        Welcome back, Customer!
    </p>

    <div class="dashboard-grid">

        <div class="dashboard-card">
            <div class="dashboard-icon">🍎</div>
            <h3>Browse Fruits</h3>
            <p>Explore fresh fruits and find your favorite products.</p>
            <a href="${pageContext.request.contextPath}/home">
                Shop Now
            </a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">🛒</div>
            <h3>My Cart</h3>
            <p>View and manage the fruits in your shopping cart.</p>
            <a href="#">View Cart</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">📦</div>
            <h3>My Orders</h3>
            <p>View your order history and current orders.</p>
            <a href="#">My Orders</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">🚚</div>
            <h3>Track Delivery</h3>
            <p>Check the current status of your delivery.</p>
            <a href="#">Track Order</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">⭐</div>
            <h3>My Reviews</h3>
            <p>Review the fruits you have purchased.</p>
            <a href="#">My Reviews</a>
        </div>

    </div>

</section>

</body>
</html>