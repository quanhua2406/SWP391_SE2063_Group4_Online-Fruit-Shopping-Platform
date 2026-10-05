<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <title>Admin Dashboard</title>

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

    <h1>Admin Dashboard</h1>

    <p class="dashboard-welcome">
        Welcome back, Admin!
    </p>


    <div class="dashboard-grid">


        <!-- USER MANAGEMENT -->

        <div class="dashboard-card">

            <div class="dashboard-icon">
                👥
            </div>

            <h3>
                User Management
            </h3>

            <p>
                Manage customer, store owner
                and delivery staff accounts.
            </p>

            <a href="${pageContext.request.contextPath}/admin/user-management">
                Manage Users
            </a>

        </div>


        <!-- ROLE MANAGEMENT -->

        <div class="dashboard-card">

            <div class="dashboard-icon">
                🔐
            </div>

            <h3>
                Role Management
            </h3>

            <p>
                Search users by Gmail and
                change their system role.
            </p>

            <a href="${pageContext.request.contextPath}/admin/roles">
                Manage Roles
            </a>

        </div>


        <!-- PRODUCT MANAGEMENT -->

        <div class="dashboard-card">

            <div class="dashboard-icon">
                🍎
            </div>

            <h3>
                Product Management
            </h3>

            <p>
                Manage and approve products
                on the platform.
            </p>

            <a href="#">
                Manage Products
            </a>

        </div>


        <!-- ORDER MANAGEMENT -->

        <div class="dashboard-card">

            <div class="dashboard-icon">
                🛒
            </div>

            <h3>
                Order Management
            </h3>

            <p>
                Monitor and manage
                customer orders.
            </p>

            <a href="#">
                View Orders
            </a>

        </div>


        <!-- PAYMENT -->

        <div class="dashboard-card">

            <div class="dashboard-icon">
                💳
            </div>

            <h3>
                Payments
            </h3>

            <p>
                Monitor transactions
                and payment status.
            </p>

            <a href="#">
                View Payments
            </a>

        </div>


        <!-- REPORT -->

        <div class="dashboard-card">

            <div class="dashboard-icon">
                📊
            </div>

            <h3>
                Reports
            </h3>

            <p>
                View sales and
                platform reports.
            </p>

            <a href="#">
                View Reports
            </a>

        </div>


    </div>

</section>

</body>

</html>