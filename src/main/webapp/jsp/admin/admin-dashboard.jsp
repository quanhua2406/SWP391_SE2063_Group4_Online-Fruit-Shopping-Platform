<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Admin Dashboard - FreshFruit</title>

    <style>
        /* ==================== GLOBAL ==================== */

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: Arial, Helvetica, sans-serif;
            background: #f6faf4;
            color: #333;
        }

        a {
            text-decoration: none;
        }

        /* ==================== NAVBAR ==================== */

        .navbar {
            min-height: 70px;
            background: #ffffff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 50px;
            border-bottom: 1px solid #e5e5e5;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }

        .logo {
            font-size: 25px;
            font-weight: bold;
            color: #3d8b40;
        }

        .nav-actions {
            display: flex;
            align-items: center;
            gap: 25px;
        }

        .nav-actions a {
            color: #333;
            font-size: 14px;
            font-weight: 600;
            transition: color 0.2s;
        }

        .nav-actions a:hover {
            color: #2e7d32;
        }

        .nav-actions .logout {
            color: #d32f2f;
        }

        .nav-actions .logout:hover {
            color: #b71c1c;
        }

        /* ==================== DASHBOARD ==================== */

        .dashboard {
            max-width: 1200px;
            margin: 0 auto;
            padding: 55px 30px 70px;
        }

        .dashboard h1 {
            text-align: center;
            font-size: 34px;
            color: #234d25;
            margin-bottom: 10px;
        }

        .dashboard-welcome {
            text-align: center;
            color: #777;
            font-size: 15px;
            margin-bottom: 40px;
        }

        /* ==================== DASHBOARD GRID ==================== */

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 25px;
        }

        /* ==================== DASHBOARD CARD ==================== */

        .dashboard-card {
            background: #ffffff;
            border: 1px solid #edf2eb;
            border-radius: 14px;
            padding: 28px;
            min-height: 250px;

            display: flex;
            flex-direction: column;

            box-shadow: 0 5px 18px rgba(0, 0, 0, 0.07);

            transition: transform 0.2s, box-shadow 0.2s;
        }

        .dashboard-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.10);
        }

        /* ==================== CARD ICON ==================== */

        .dashboard-icon {
            width: 55px;
            height: 55px;

            display: flex;
            align-items: center;
            justify-content: center;

            background: #edf8ea;
            border-radius: 12px;

            font-size: 27px;
            margin-bottom: 18px;
        }

        /* ==================== CARD TITLE ==================== */

        .dashboard-card h3 {
            color: #2e6b31;
            font-size: 19px;
            margin-bottom: 10px;
        }

        /* ==================== CARD DESCRIPTION ==================== */

        .dashboard-card p {
            color: #777;
            font-size: 14px;
            line-height: 1.6;
            margin-bottom: 20px;
            flex: 1;
        }

        /* ==================== CARD BUTTON ==================== */

        .dashboard-card a.action-btn {
            display: inline-block;
            align-self: flex-start;
            width: fit-content;

            background: #2e7d32;
            color: #ffffff;

            padding: 10px 17px;
            border-radius: 7px;

            font-size: 14px;
            font-weight: bold;

            transition: background 0.2s, transform 0.2s;
        }

        .dashboard-card a.action-btn:hover {
            background: #256628;
            transform: translateY(-1px);
        }

        /* ==================== RESPONSIVE ==================== */

        @media (max-width: 900px) {
            .dashboard-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }

            .navbar {
                padding: 0 25px;
            }
        }

        @media (max-width: 600px) {
            .navbar {
                padding: 18px 20px;
                flex-direction: column;
                gap: 15px;
            }

            .nav-actions {
                gap: 15px;
                flex-wrap: wrap;
                justify-content: center;
            }

            .dashboard {
                padding: 35px 20px 50px;
            }

            .dashboard h1 {
                font-size: 28px;
            }

            .dashboard-grid {
                grid-template-columns: 1fr;
            }

            .dashboard-card {
                min-height: 220px;
            }
        }
    </style>

</head>

<body>

    <!-- ==================== NAVBAR ==================== -->

    <header class="navbar">

        <div class="logo">
            🍊 FreshFruit
        </div>

        <nav class="nav-actions">

            <a href="${pageContext.request.contextPath}/home">
                🏠 Home
            </a>

            <a href="${pageContext.request.contextPath}/logout"
               class="logout">
                Logout
            </a>

        </nav>

    </header>

    <!-- ==================== DASHBOARD ==================== -->

    <main class="dashboard">

        <h1>Admin Dashboard</h1>

        <p class="dashboard-welcome">
            Welcome back, Admin!
        </p>

        <div class="dashboard-grid">

            <!-- USER MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">👥</div>

                <h3>User Management</h3>

                <p>
                    Manage customer, store owner
                    and delivery staff accounts.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/user-management">
                    Manage Users
                </a>

            </div>

            <!-- ROLE MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🔐</div>

                <h3>Role Management</h3>

                <p>
                    Search users by email and
                    change their system role.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/roles">
                    Manage Roles
                </a>

            </div>

            <!-- PRODUCT MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🍎</div>

                <h3>Product Management</h3>

                <p>
                    Manage and approve products
                    on the platform.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/product-management">
                    Manage Products
                </a>

            </div>

            <!-- CATEGORY MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">📂</div>

                <h3>Category Management</h3>

                <p>
                    Create and organise product categories
                    used when adding products.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/category-management">
                    Manage Categories
                </a>

            </div>

            <!-- PROMOTION APPROVALS -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🏷️</div>

                <h3>Promotion Approvals</h3>

                <p>
                    Approve or refuse the promotional prices
                    requested by shop owners.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/promotion-approvals">
                    Review Promotions
                </a>

            </div>

            <!-- ORDER MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🛒</div>

                <h3>Order Management</h3>

                <p>
                    Monitor and manage customer orders.
                </p>

                <a class="action-btn" href="#">
                    View Orders
                </a>

            </div>

            <!-- PAYMENT MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">💳</div>

                <h3>Payments</h3>

                <p>
                    Monitor transactions and payment status.
                </p>

                <a class="action-btn" href="#">
                    View Payments
                </a>

            </div>

            <!-- COMMISSION REPORT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">💰</div>

                <h3>Commission Report</h3>

                <p>
                    View the platform fee collected from shops,
                    by shop and by commission %.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/commission-report">
                    View Commission
                </a>

            </div>

            <!-- REPORTS -->

            <div class="dashboard-card">

                <div class="dashboard-icon">📊</div>

                <h3>Reports</h3>

                <p>
                    View sales and platform reports.
                </p>

                <a class="action-btn" href="#">
                    View Reports
                </a>

            </div>

        </div>

    </main>

</body>
</html>