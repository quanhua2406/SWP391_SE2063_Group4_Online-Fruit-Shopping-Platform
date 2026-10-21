<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Delivery Personal Dashboard</title>

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

        <a href="${pageContext.request.contextPath}/shopowner/delivery/list" style="color: #16a34a; font-weight: 700;">Danh s&#225;ch giao h&#224;ng</a>


        <a href="${pageContext.request.contextPath}/logout"
           class="logout">
            Logout
        </a>
    </div>

</header>

<section class="dashboard">

    <h1>Delivery Personal Dashboard</h1>

    <p class="dashboard-welcome">
        Welcome back, Delivery Personal!
    </p>

    <div class="dashboard-grid">

        <div class="dashboard-card">
            <div class="dashboard-icon">📦</div>
            <h3>Assigned Orders</h3>
            <p>View orders assigned to you for delivery.</p>
            <a href="${pageContext.request.contextPath}/shopowner/delivery/list?status=Assigned">View Orders</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">🚚</div>
            <h3>Delivery Tasks</h3>
            <p>Manage your current delivery tasks.</p>
            <a href="${pageContext.request.contextPath}/shopowner/delivery/list">My Deliveries</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">📍</div>
            <h3>Delivery Status</h3>
            <p>Update the status of your deliveries.</p>
            <a href="${pageContext.request.contextPath}/shopowner/delivery/list">Update Status</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">✅</div>
            <h3>Completed Deliveries</h3>
            <p>View your completed delivery history.</p>
            <a href="${pageContext.request.contextPath}/shopowner/delivery/list?status=Delivered">View History</a>
        </div>

    </div>

</section>

</body>
</html>