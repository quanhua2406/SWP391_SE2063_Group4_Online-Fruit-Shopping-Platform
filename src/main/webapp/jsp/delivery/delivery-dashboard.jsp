<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Delivery Personal Dashboard - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/quanht/delivery-notification.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">
        <a href="${pageContext.request.contextPath}/home">
            🏠 Home
        </a>

        <a href="${pageContext.request.contextPath}/shopowner/delivery/list">
            Danh sách giao hàng
        </a>

        <!-- Bell Notification Icon with Unread Badge -->
        <a href="${pageContext.request.contextPath}/delivery/notifications" class="delivery-bell-link">
            🔔 Thông báo
            <c:if test="${not empty sessionScope.deliveryUnreadCount and sessionScope.deliveryUnreadCount > 0}">
                <span class="delivery-bell-badge">${sessionScope.deliveryUnreadCount}</span>
            </c:if>
        </a>

        <a href="${pageContext.request.contextPath}/logout"
           class="logout">
            Logout
        </a>
    </nav>

</header>

<section class="dashboard">

    <h1>Delivery Personal Dashboard</h1>

    <p class="dashboard-welcome">
        Welcome back, Delivery Personal!
    </p>

    <div class="dashboard-grid">

        <!-- NOTIFICATIONS CARD -->
        <div class="dashboard-card">
            <div class="dashboard-icon">🔔</div>
            <h3>Notifications &amp; Alerts</h3>
            <p>Check newly assigned deliveries and real-time status updates.</p>
            <a href="${pageContext.request.contextPath}/delivery/notifications">View Notifications</a>
        </div>

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