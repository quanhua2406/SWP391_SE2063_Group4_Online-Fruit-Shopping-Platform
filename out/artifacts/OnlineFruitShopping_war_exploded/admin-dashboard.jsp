<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Admin Dashboard</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f6faf4; color: #333; }
        .dashboard-welcome { margin-bottom: 24px; color: #666; font-size: 16px; }
        .dashboard-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
        .dashboard-card { background: #fff; padding: 24px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); display: flex; flex-direction: column; }
        .dashboard-icon { width: 55px; height: 55px; display: flex; align-items: center; justify-content: center; background: #edf8ea; border-radius: 12px; font-size: 27px; margin-bottom: 18px; }
        .dashboard-card h3 { color: #2e6b31; font-size: 19px; margin-bottom: 10px; }
        .dashboard-card p { color: #777; font-size: 14px; line-height: 1.6; margin-bottom: 20px; flex: 1; }
        .dashboard-card a { display: inline-block; align-self: flex-start; background: #2e7d32; color: #fff; padding: 10px 17px; border-radius: 7px; font-size: 14px; font-weight: bold; text-decoration: none; transition: background 0.2s; }
        .dashboard-card a:hover { background: #256628; }
    </style>
</head>
<body>

    <h1>Admin Dashboard</h1>

    <p class="dashboard-welcome">
        Welcome back, Admin!
    </p>

    <div class="dashboard-grid">

        <div class="dashboard-card">
            <div class="dashboard-icon">👥</div>
            <h3>User Management</h3>
            <p>Manage customer, store owner and delivery staff accounts.</p>
            <a href="${pageContext.request.contextPath}/admin/user-management">Manage Users</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">🍎</div>
            <h3>Product Management</h3>
            <p>Manage and approve products on the platform.</p>
            <a href="${pageContext.request.contextPath}/admin/product-management">Manage Products</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">🛒</div>
            <h3>Order Management</h3>
            <p>Monitor and manage customer orders.</p>
            <a href="${pageContext.request.contextPath}/admin/orders">View Orders</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">🛡️</div>
            <h3>Inappropriate Review Management</h3>
            <p>Moderate reported reviews, inspect attached evidence, hide or delete inappropriate feedback.</p>
            <a href="${pageContext.request.contextPath}/admin/reviews?status=reported">
                Moderate Reviews
                <c:if test="${not empty reportedReviewsCount && reportedReviewsCount > 0}">
                    (${reportedReviewsCount})
                </c:if>
            </a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">💳</div>
            <h3>Payments</h3>
            <p>Monitor transactions and payment status.</p>
            <a href="#">View Payments</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">📊</div>
            <h3>Reports</h3>
            <p>View sales and platform reports.</p>
            <a href="#">View Reports</a>
        </div>

    </div>

</body>
</html>
