<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Store Owner Dashboard</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/quanht/style.css">
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

        <a href="${pageContext.request.contextPath}/store-owner/products">
            🍎 Sản Phẩm
        </a>

        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" style="color: #16a34a; font-weight: 700;">
            ➕ Tạo Gói Mới
        </a>

        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans">
            📦 Gói Combo
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

    <c:if test="${not empty error}">
        <div style="background: #fee2e2; border: 1px solid #ef4444; color: #b91c1c; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px; font-weight: 600;">
            ⚠️ ${error}
        </div>
    </c:if>

    <!-- Quick Action Bar -->
    <div style="display: flex; gap: 12px; margin-bottom: 28px; flex-wrap: wrap;">
        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" 
           style="background: linear-gradient(135deg, #16a34a, #15803d); color: #fff; padding: 10px 20px; border-radius: 8px; font-weight: 600; text-decoration: none; box-shadow: 0 4px 12px rgba(22,163,74,0.3); display: inline-flex; align-items: center; gap: 8px;">
            ➕ Tạo Gói Định Kỳ Mới
        </a>
        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans" 
           style="background: #eef7ee; color: #166534; border: 1px solid #bbf7d0; padding: 10px 18px; border-radius: 8px; font-weight: 600; text-decoration: none; display: inline-flex; align-items: center; gap: 8px;">
            📦 Quản Lý Gói Combo Của Shop
        </a>
        <a href="${pageContext.request.contextPath}/products?action=add" 
           style="background: #f3f4f6; color: #374151; border: 1px solid #e5e7eb; padding: 10px 18px; border-radius: 8px; font-weight: 600; text-decoration: none; display: inline-flex; align-items: center; gap: 8px;">
            🍎 Thêm Sản Phẩm Mới
        </a>
    </div>

    <div class="dashboard-grid">

        <div class="dashboard-card">
            <div class="dashboard-icon">🍎</div>
            <h3>My Products</h3>
            <p>Add, update and manage your fruit products.</p>
            <a href="${pageContext.request.contextPath}/store-owner/products">Manage Products</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">📦</div>
            <h3>Inventory</h3>
            <p>Monitor stock quantity and product availability.</p>
            <a href="#">Manage Inventory</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">🛒</div>
            <h3>Orders</h3>
            <p>View and process orders containing your products.</p>
            <a href="#">View Orders</a>
        </div>

        <div class="dashboard-card">
            <div class="dashboard-icon">💰</div>
            <h3>Sales</h3>
            <p>Review your sales and business performance.</p>
            <a href="#">View Sales</a>
        </div>

        <div class="dashboard-card" style="border: 2px solid #22c55e; background: #f0fdf4;">
            <div class="dashboard-icon">🎁</div>
            <h3 style="color: #15803d;">Gói Đăng Ký (Combo Plans)</h3>
            <p>Tạo mới & quản lý các gói combo trái cây định kỳ cho khách hàng đăng ký theo tuần/tháng.</p>
            <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 14px;">
                <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" 
                   style="background: #16a34a; color: #ffffff !important; padding: 9px 14px; border-radius: 6px; text-decoration: none; font-weight: 700; text-align: center; display: block; box-shadow: 0 2px 6px rgba(22,163,74,0.25);">
                    ➕ Tạo Gói Định Kỳ Mới
                </a>
                <a href="${pageContext.request.contextPath}/store-owner/subscription/plans" 
                   style="color: #15803d; font-weight: 600; text-decoration: none; text-align: center; font-size: 13.5px; padding: 4px;">
                    📋 Quản Lý Danh Sách Gói Combo →
                </a>
            </div>
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