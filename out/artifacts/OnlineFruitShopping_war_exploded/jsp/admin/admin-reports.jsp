<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo Cáo & Thống Kê Quản Trị (Admin Reports) | FreshFruit</title>
    <style>
        :root {
            --primary: #2e7d32;
            --primary-dark: #1b5e20;
            --primary-light: #edf8ea;
            --text-dark: #1e293b;
            --text-muted: #64748b;
            --border: #e2e8f0;
            --bg-light: #f8fafc;
            --danger: #ef4444;
            --warning: #f59e0b;
            --info: #3b82f6;
            --purple: #8b5cf6;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background: #f1f5f9;
            color: var(--text-dark);
            line-height: 1.5;
        }

        a {
            text-decoration: none;
            color: inherit;
        }

        /* NAVBAR */
        .navbar {
            min-height: 65px;
            background: #ffffff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 40px;
            border-bottom: 1px solid var(--border);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        }

        .logo {
            font-size: 22px;
            font-weight: 800;
            color: var(--primary);
        }

        .nav-actions {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .nav-actions a {
            font-size: 14px;
            font-weight: 600;
            color: var(--text-muted);
            transition: color 0.2s;
        }

        .nav-actions a:hover {
            color: var(--primary);
        }

        .nav-actions .logout {
            color: var(--danger);
        }

        /* CONTAINER */
        .main-container {
            max-width: 1250px;
            margin: 30px auto 60px;
            padding: 0 20px;
        }

        .page-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 25px;
            flex-wrap: wrap;
            gap: 15px;
        }

        .page-title {
            font-size: 26px;
            font-weight: 800;
            color: var(--text-dark);
        }

        .page-subtitle {
            font-size: 14px;
            color: var(--text-muted);
            margin-top: 4px;
        }

        .btn-back {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: #ffffff;
            border: 1px solid var(--border);
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            color: var(--text-dark);
            transition: all 0.2s;
        }

        .btn-back:hover {
            background: #f8fafc;
            border-color: #cbd5e1;
        }

        /* TABS */
        .tabs-nav {
            display: flex;
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 6px;
            gap: 6px;
            margin-bottom: 25px;
            overflow-x: auto;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
        }

        .tab-btn {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 10px 18px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            color: var(--text-muted);
            white-space: nowrap;
            transition: all 0.2s;
        }

        .tab-btn:hover {
            color: var(--primary);
            background: var(--primary-light);
        }

        .tab-btn.active {
            background: var(--primary);
            color: #ffffff;
        }

        /* FILTER CARD */
        .filter-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 18px 22px;
            margin-bottom: 25px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
        }

        .filter-form {
            display: flex;
            align-items: flex-end;
            gap: 15px;
            flex-wrap: wrap;
        }

        .filter-group {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .filter-label {
            font-size: 12px;
            font-weight: 700;
            color: var(--text-muted);
            text-transform: uppercase;
        }

        .filter-input, .filter-select {
            padding: 9px 12px;
            border-radius: 7px;
            border: 1px solid var(--border);
            font-size: 14px;
            color: var(--text-dark);
            background: #ffffff;
            outline: none;
            min-width: 140px;
        }

        .filter-input:focus, .filter-select:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.15);
        }

        .btn-filter {
            background: var(--primary);
            color: #ffffff;
            border: none;
            padding: 10px 18px;
            border-radius: 7px;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
            transition: background 0.2s;
        }

        .btn-filter:hover {
            background: var(--primary-dark);
        }

        .btn-export {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: #ffffff;
            color: #059669;
            border: 1.5px solid #059669;
            padding: 9px 16px;
            border-radius: 7px;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;
            margin-left: auto;
        }

        .btn-export:hover {
            background: #ecfdf5;
        }

        /* METRIC CARDS */
        .metric-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 18px;
            margin-bottom: 25px;
        }

        .metric-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
            border-left: 4px solid var(--primary);
        }

        .metric-title {
            font-size: 12px;
            font-weight: 700;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .metric-val {
            font-size: 24px;
            font-weight: 800;
            color: var(--text-dark);
            margin-bottom: 4px;
        }

        .metric-desc {
            font-size: 12px;
            color: var(--text-muted);
        }

        /* CONTENT CARD */
        .content-card {
            background: #ffffff;
            border-radius: 14px;
            border: 1px solid var(--border);
            padding: 24px;
            margin-bottom: 25px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
        }

        .card-header-bar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 18px;
            padding-bottom: 12px;
            border-bottom: 1px solid #f1f5f9;
        }

        .card-heading {
            font-size: 18px;
            font-weight: 700;
            color: var(--text-dark);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        /* DATA TABLE */
        .table-wrap {
            overflow-x: auto;
        }

        .data-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
            text-align: left;
        }

        .data-table th {
            background: #f8fafc;
            color: #475569;
            font-weight: 700;
            padding: 12px 14px;
            border-bottom: 2px solid var(--border);
            white-space: nowrap;
        }

        .data-table td {
            padding: 12px 14px;
            border-bottom: 1px solid #f1f5f9;
            color: #334155;
            vertical-align: middle;
        }

        .data-table tr:hover {
            background: #f8fafc;
        }

        /* BADGES */
        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
        }

        .badge-success { background: #dcfce7; color: #166534; }
        .badge-warning { background: #fef3c7; color: #92400e; }
        .badge-danger { background: #fee2e2; color: #991b1b; }
        .badge-info { background: #e0f2fe; color: #075985; }
        .badge-purple { background: #f3e8ff; color: #6b21a8; }
        .badge-gray { background: #f1f5f9; color: #475569; }

        .empty-notice {
            text-align: center;
            padding: 40px 20px;
            color: var(--text-muted);
            font-size: 14px;
        }

        @media (max-width: 768px) {
            .navbar { padding: 0 20px; }
            .filter-form { flex-direction: column; align-items: stretch; }
            .btn-export { margin-left: 0; justify-content: center; }
        }
    </style>
</head>
<body>

    <!-- NAVBAR -->
    <header class="navbar">
        <div class="logo">🍊 FreshFruit Admin</div>
        <nav class="nav-actions">
            <a href="${pageContext.request.contextPath}/admin/dashboard">🏠 Dashboard</a>
            <a href="${pageContext.request.contextPath}/admin/notifications">🔔 Thông báo</a>
            <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
        </nav>
    </header>

    <div class="main-container">

        <!-- HEADER -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Hệ Thống Báo Cáo &amp; Thống Kê Nền Tảng</h1>
                <p class="page-subtitle">Phân hệ theo dõi chỉ số bán hàng, doanh thu, sản phẩm, tồn kho và khách hàng (Admin Analytics)</p>
            </div>
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-back">
                &larr; Về Admin Dashboard
            </a>
        </div>

        <!-- TABS NAVIGATION -->
        <div class="tabs-nav">
            <a href="${pageContext.request.contextPath}/admin/reports?tab=sales" class="tab-btn ${currentTab == 'sales' ? 'active' : ''}">
                🛒 Báo Cáo Bán Hàng
            </a>
            <a href="${pageContext.request.contextPath}/admin/reports?tab=revenue" class="tab-btn ${currentTab == 'revenue' ? 'active' : ''}">
                💵 Báo Cáo Doanh Thu
            </a>
            <a href="${pageContext.request.contextPath}/admin/reports?tab=products" class="tab-btn ${currentTab == 'products' ? 'active' : ''}">
                🍎 Thống Kê Sản Phẩm
            </a>
            <a href="${pageContext.request.contextPath}/admin/reports?tab=inventory" class="tab-btn ${currentTab == 'inventory' ? 'active' : ''}">
                📦 Báo Cáo Tồn Kho
            </a>
            <a href="${pageContext.request.contextPath}/admin/reports?tab=orders" class="tab-btn ${currentTab == 'orders' ? 'active' : ''}">
                📋 Thống Kê Đơn Hàng
            </a>
            <a href="${pageContext.request.contextPath}/admin/reports?tab=customers" class="tab-btn ${currentTab == 'customers' ? 'active' : ''}">
                👥 Thống Kê Khách Hàng
            </a>
        </div>

        <!-- TAB 1: BÁO CÁO BÁN HÀNG (TASK 27: SALES REPORTS) -->
        <c:if test="${currentTab == 'sales'}">
            <div class="filter-card">
                <form action="${pageContext.request.contextPath}/admin/reports" method="GET" class="filter-form">
                    <input type="hidden" name="tab" value="sales">
                    <div class="filter-group">
                        <label class="filter-label">Từ Ngày</label>
                        <input type="date" name="startDate" value="${startDate}" class="filter-input">
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Đến Ngày</label>
                        <input type="date" name="endDate" value="${endDate}" class="filter-input">
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Cửa Hàng</label>
                        <select name="shopId" class="filter-select">
                            <option value="">-- Tất cả cửa hàng --</option>
                            <c:forEach var="s" items="${shops}">
                                <option value="${s.shopId}" ${shopId == s.shopId ? 'selected' : ''}>${s.shopName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Danh Mục</label>
                        <select name="categoryId" class="filter-select">
                            <option value="">-- Tất cả danh mục --</option>
                            <c:forEach var="c" items="${categories}">
                                <option value="${c.categoryId}" ${categoryId == c.categoryId ? 'selected' : ''}>${c.categoryName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn-filter">Lọc Dữ Liệu</button>
                    <a href="${pageContext.request.contextPath}/admin/reports/export?type=sales&startDate=${startDate}&endDate=${endDate}&shopId=${shopId}&categoryId=${categoryId}" class="btn-export">
                        📥 Xuất File CSV
                    </a>
                </form>
            </div>

            <div class="content-card">
                <div class="card-header-bar">
                    <h2 class="card-heading">🛒 Chi Tiết Báo Cáo Bán Hàng Hoa Quả</h2>
                    <span style="font-size: 13px; color: var(--text-muted);">Dữ liệu tính trên các đơn hàng giao thành công</span>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Mã SP</th>
                                <th>Tên Hoa Quả</th>
                                <th>Danh Mục</th>
                                <th>Cửa Hàng</th>
                                <th>Quy Cách</th>
                                <th>Đơn Vị</th>
                                <th style="text-align: right;">Số Lượng Bán</th>
                                <th style="text-align: right;">Tổng Doanh Thu (VNĐ)</th>
                                <th style="text-align: right;">Số Đơn Hàng</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty salesReport}">
                                    <c:forEach var="item" items="${salesReport}" varStatus="loop">
                                        <tr>
                                            <td>${loop.index + 1}</td>
                                            <td><span class="badge badge-gray">#${item.productId}</span></td>
                                            <td><strong>${item.productName}</strong></td>
                                            <td>${item.categoryName}</td>
                                            <td>${item.shopName}</td>
                                            <td>${item.packagingType}</td>
                                            <td>${item.unit}</td>
                                            <td style="text-align: right; font-weight: 600; color: var(--primary);">${item.quantitySold}</td>
                                            <td style="text-align: right; font-weight: 700; color: #1e293b;">
                                                <fmt:formatNumber value="${item.totalRevenue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                            </td>
                                            <td style="text-align: right;">${item.totalOrders}</td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="10" class="empty-notice">Không tìm thấy dữ liệu bán hàng trong khoảng thời gian này.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

        <!-- TAB 2: BÁO CÁO DOANH THU & CHIẾT KHẤU (TASK 28: REVENUE REPORTS) -->
        <c:if test="${currentTab == 'revenue'}">
            <div class="filter-card">
                <form action="${pageContext.request.contextPath}/admin/reports" method="GET" class="filter-form">
                    <input type="hidden" name="tab" value="revenue">
                    <div class="filter-group">
                        <label class="filter-label">Từ Ngày</label>
                        <input type="date" name="startDate" value="${startDate}" class="filter-input">
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Đến Ngày</label>
                        <input type="date" name="endDate" value="${endDate}" class="filter-input">
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Cửa Hàng</label>
                        <select name="shopId" class="filter-select">
                            <option value="">-- Tất cả cửa hàng --</option>
                            <c:forEach var="s" items="${shops}">
                                <option value="${s.shopId}" ${shopId == s.shopId ? 'selected' : ''}>${s.shopName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn-filter">Lọc Dữ Liệu</button>
                    <a href="${pageContext.request.contextPath}/admin/reports/export?type=revenue&startDate=${startDate}&endDate=${endDate}&shopId=${shopId}" class="btn-export">
                        📥 Xuất File CSV
                    </a>
                </form>
            </div>

            <!-- CHỈ SỐ DOANH THU & PHÍ SÀN -->
            <div class="metric-grid">
                <div class="metric-card" style="border-left-color: #10b981;">
                    <div class="metric-title">Doanh Thu Gộp (Gross Revenue)</div>
                    <div class="metric-val">
                        <fmt:formatNumber value="${revenueReport.grossRevenue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </div>
                    <div class="metric-desc">Tổng giá trị đơn hàng hoàn thành</div>
                </div>
                <div class="metric-card" style="border-left-color: #3b82f6;">
                    <div class="metric-title">Hoa Hồng Sàn Thu Được</div>
                    <div class="metric-val">
                        <fmt:formatNumber value="${revenueReport.platformCommission}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </div>
                    <div class="metric-desc">Phí nền tảng thu từ các cửa hàng</div>
                </div>
                <div class="metric-card" style="border-left-color: #f59e0b;">
                    <div class="metric-title">Chiết Khấu Khuyến Mãi (Voucher)</div>
                    <div class="metric-val">
                        <fmt:formatNumber value="${revenueReport.totalVouchers}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </div>
                    <div class="metric-desc">Tổng số tiền giảm giá đã áp dụng</div>
                </div>
                <div class="metric-card" style="border-left-color: #ef4444;">
                    <div class="metric-title">Tổng Tiền Hoàn Trả (Refunds)</div>
                    <div class="metric-val">
                        <fmt:formatNumber value="${revenueReport.totalRefunds}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </div>
                    <div class="metric-desc">Khoản hoàn tiền đơn hàng đã duyệt</div>
                </div>
                <div class="metric-card" style="border-left-color: #8b5cf6;">
                    <div class="metric-title">Doanh Thu Thuần Sàn (Net Revenue)</div>
                    <div class="metric-val">
                        <fmt:formatNumber value="${revenueReport.netPlatformRevenue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </div>
                    <div class="metric-desc">Sau khi trừ các khoản bồi hoàn</div>
                </div>
                <div class="metric-card" style="border-left-color: #06b6d4;">
                    <div class="metric-title">Biên Lợi Nhuận (Profit Margin)</div>
                    <div class="metric-val">${revenueReport.profitMargin}%</div>
                    <div class="metric-desc">Tỷ lệ lợi nhuận thuần trên tổng doanh thu</div>
                </div>
            </div>

            <!-- PHÂN BỔ THEO PHƯƠNG THỨC THANH TOÁN -->
            <div class="content-card">
                <div class="card-header-bar">
                    <h2 class="card-heading">💳 Phân Bổ Giao Dịch Theo Cổng Thanh Toán</h2>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Cổng / Phương Thức Thanh Toán</th>
                                <th>Mã Ký Hiệu</th>
                                <th style="text-align: right;">Số Đơn Hàng</th>
                                <th style="text-align: right;">Tổng Tiền Giao Dịch (VNĐ)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${revenueReport.paymentBreakdown}" varStatus="loop">
                                <tr>
                                    <td>${loop.index + 1}</td>
                                    <td><strong>${p.methodName}</strong></td>
                                    <td><span class="badge badge-gray">${p.methodKey}</span></td>
                                    <td style="text-align: right; font-weight: 600;">${p.orderCount}</td>
                                    <td style="text-align: right; font-weight: 700; color: #1e293b;">
                                        <fmt:formatNumber value="${p.totalAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

        <!-- TAB 3: THỐNG KÊ SẢN PHẨM & ĐÁNH GIÁ (TASK 29: PRODUCT STATISTICS) -->
        <c:if test="${currentTab == 'products'}">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 25px; margin-bottom: 25px;">
                <!-- TOP BÁN CHẠY -->
                <div class="content-card" style="margin-bottom: 0;">
                    <div class="card-header-bar">
                        <h2 class="card-heading">🔥 Top Sản Phẩm Bán Chạy Nhất</h2>
                    </div>
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Tên Hoa Quả</th>
                                    <th>Cửa Hàng</th>
                                    <th style="text-align: right;">SL Đã Bán</th>
                                    <th style="text-align: right;">Doanh Thu</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${productStats.topSelling}" varStatus="loop">
                                    <tr>
                                        <td><span class="badge badge-success">${loop.index + 1}</span></td>
                                        <td><strong>${item.productName}</strong></td>
                                        <td>${item.shopName}</td>
                                        <td style="text-align: right; font-weight: 700; color: var(--primary);">${item.totalQuantity}</td>
                                        <td style="text-align: right; font-weight: 700;">
                                            <fmt:formatNumber value="${item.totalSales}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- BÁN CHẬM / CẦN THÚC ĐẨY -->
                <div class="content-card" style="margin-bottom: 0;">
                    <div class="card-header-bar">
                        <h2 class="card-heading">🐢 Sản Phẩm Bán Chậm (Cần Thúc Đẩy)</h2>
                    </div>
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Tên Hoa Quả</th>
                                    <th>Danh Mục</th>
                                    <th style="text-align: right;">Đã Bán</th>
                                    <th style="text-align: right;">Tồn Kho Hiện Tại</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${productStats.slowMoving}" varStatus="loop">
                                    <tr>
                                        <td>${loop.index + 1}</td>
                                        <td><strong>${item.productName}</strong></td>
                                        <td>${item.categoryName}</td>
                                        <td style="text-align: right; font-weight: 600; color: var(--text-muted);">${item.totalSold}</td>
                                        <td style="text-align: right; font-weight: 700; color: #f59e0b;">${item.currentStock}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- ĐÁNH GIÁ VÀ XẾP HẠNG SAO -->
            <div class="content-card">
                <div class="card-header-bar">
                    <h2 class="card-heading">⭐ Thống Kê Đánh Giá &amp; Điểm Rating Khách Hàng</h2>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Mã SP</th>
                                <th>Tên Hoa Quả</th>
                                <th>Đánh Giá Trung Bình</th>
                                <th style="text-align: right;">Tổng Số Lượt Đánh Giá</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${productStats.ratings}" varStatus="loop">
                                <tr>
                                    <td>${loop.index + 1}</td>
                                    <td><span class="badge badge-gray">#${r.productId}</span></td>
                                    <td><strong>${r.productName}</strong></td>
                                    <td>
                                        <span style="color: #f59e0b; font-weight: 800; font-size: 15px;">⭐ ${r.avgRating} / 5.0</span>
                                    </td>
                                    <td style="text-align: right; font-weight: 600;">${r.totalReviews} lượt đánh giá</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

        <!-- TAB 4: BÁO CÁO TỒN KHO & CẢNH BÁO (TASK 30: INVENTORY REPORTS) -->
        <c:if test="${currentTab == 'inventory'}">
            <div class="metric-grid">
                <div class="metric-card" style="border-left-color: #3b82f6;">
                    <div class="metric-title">Tổng Số Quy Cách / Biến Thể</div>
                    <div class="metric-val">${inventoryReport.totalVariants}</div>
                    <div class="metric-desc">Đang quản lý trên toàn hệ thống</div>
                </div>
                <div class="metric-card" style="border-left-color: #10b981;">
                    <div class="metric-title">Tổng Khối Lượng Tồn Kho Khả Dụng</div>
                    <div class="metric-val">
                        <fmt:formatNumber value="${inventoryReport.totalStockQuantity}" type="number" groupingUsed="true" maxFractionDigits="1"/>
                    </div>
                    <div class="metric-desc">Tổng tồn kho thực tế từ các lô nhập</div>
                </div>
                <div class="metric-card" style="border-left-color: #f59e0b;">
                    <div class="metric-title">Mặt Hàng Sắp Hết (Cảnh Báo)</div>
                    <div class="metric-val" style="color: #f59e0b;">${not empty inventoryReport.lowStockItems ? inventoryReport.lowStockItems.size() : 0}</div>
                    <div class="metric-desc">Tồn kho khả dụng &le; Ngưỡng an toàn</div>
                </div>
                <div class="metric-card" style="border-left-color: #ef4444;">
                    <div class="metric-title">Mặt Hàng Đã Hết Tồn Kho</div>
                    <div class="metric-val" style="color: #ef4444;">${not empty inventoryReport.outOfStockItems ? inventoryReport.outOfStockItems.size() : 0}</div>
                    <div class="metric-desc">Tồn kho bằng 0 cần nhập thêm hàng</div>
                </div>
            </div>

            <!-- CẢNH BÁO SẮP HẾT HÀNG -->
            <div class="content-card">
                <div class="card-header-bar">
                    <h2 class="card-heading" style="color: #b45309;">⚠️ Cảnh Báo Sản Phẩm Sắp Hết Hàng (Low Stock Alerts)</h2>
                    <span class="badge badge-warning">Cần nhập thêm</span>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Mã SKU</th>
                                <th>Tên Hoa Quả</th>
                                <th>Quy Cách Đóng Gói</th>
                                <th>Cửa Hàng</th>
                                <th style="text-align: right;">Ngưỡng An Toàn</th>
                                <th style="text-align: right;">Tồn Kho Hiện Tại</th>
                                <th>Trạng Thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty inventoryReport.lowStockItems}">
                                    <c:forEach var="item" items="${inventoryReport.lowStockItems}">
                                        <tr>
                                            <td><span class="badge badge-gray">${item.sku}</span></td>
                                            <td><strong>${item.productName}</strong></td>
                                            <td>${item.packagingType} (${item.unit})</td>
                                            <td>${item.shopName}</td>
                                            <td style="text-align: right;">${item.threshold}</td>
                                            <td style="text-align: right; font-weight: 800; color: #d97706;">${item.currentStock}</td>
                                            <td><span class="badge badge-warning">Sắp hết hàng</span></td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" class="empty-notice">Hiện không có mặt hàng nào rơi vào tình trạng sắp hết hàng.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- LÔ HÀNG CẬN HẠN SỬ DỤNG -->
            <div class="content-card">
                <div class="card-header-bar">
                    <h2 class="card-heading">⏳ Lô Hàng Cận Hạn Sử Dụng (Trong 14 Ngày Tới)</h2>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Mã Lô (Batch Code)</th>
                                <th>Tên Sản Phẩm</th>
                                <th>Quy Cách</th>
                                <th style="text-align: right;">Số Lượng</th>
                                <th>Hạn Sử Dụng</th>
                                <th style="text-align: right;">Số Ngày Còn Lại</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty inventoryReport.expiringBatches}">
                                    <c:forEach var="b" items="${inventoryReport.expiringBatches}">
                                        <tr>
                                            <td><span class="badge badge-gray">${b.batchCode}</span></td>
                                            <td><strong>${b.productName}</strong></td>
                                            <td>${b.packagingType}</td>
                                            <td style="text-align: right; font-weight: 600;">${b.quantity}</td>
                                            <td><fmt:formatDate value="${b.expiryDate}" pattern="dd/MM/yyyy"/></td>
                                            <td style="text-align: right; font-weight: 700; color: #ef4444;">${b.daysLeft} ngày</td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" class="empty-notice">Không có lô hàng nào sắp hết hạn trong 14 ngày tới.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

        <!-- TAB 5: THỐNG KÊ ĐƠN HÀNG (TASK 31: ORDER STATISTICS) -->
        <c:if test="${currentTab == 'orders'}">
            <div class="filter-card">
                <form action="${pageContext.request.contextPath}/admin/reports" method="GET" class="filter-form">
                    <input type="hidden" name="tab" value="orders">
                    <div class="filter-group">
                        <label class="filter-label">Từ Ngày</label>
                        <input type="date" name="startDate" value="${startDate}" class="filter-input">
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Đến Ngày</label>
                        <input type="date" name="endDate" value="${endDate}" class="filter-input">
                    </div>
                    <button type="submit" class="btn-filter">Lọc Thống Kê</button>
                </form>
            </div>

            <div class="metric-grid">
                <div class="metric-card" style="border-left-color: #3b82f6;">
                    <div class="metric-title">Tổng Đơn Hàng Phát Sinh</div>
                    <div class="metric-val">${orderStats.totalOrders}</div>
                    <div class="metric-desc">Trong khoảng thời gian đã chọn</div>
                </div>
                <div class="metric-card" style="border-left-color: #10b981;">
                    <div class="metric-title">Tỷ Lệ Giao Thành Công</div>
                    <div class="metric-val" style="color: #10b981;">${orderStats.completionRate}%</div>
                    <div class="metric-desc">${orderStats.completedCount} đơn đã giao thành công</div>
                </div>
                <div class="metric-card" style="border-left-color: #ef4444;">
                    <div class="metric-title">Tỷ Lệ Hủy Đơn Hàng</div>
                    <div class="metric-val" style="color: #ef4444;">${orderStats.cancellationRate}%</div>
                    <div class="metric-desc">${orderStats.cancelledCount} đơn đã bị hủy</div>
                </div>
            </div>

            <div class="content-card">
                <div class="card-header-bar">
                    <h2 class="card-heading">📋 Phân Bố Đơn Hàng Theo Trạng Thái (Order Status Breakdown)</h2>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Trạng Thái Đơn Hàng</th>
                                <th>Mã Trạng Thái (System Code)</th>
                                <th style="text-align: right;">Số Lượng Đơn</th>
                                <th style="text-align: right;">Tổng Tiền (VNĐ)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="st" items="${orderStats.statusList}" varStatus="loop">
                                <tr>
                                    <td>${loop.index + 1}</td>
                                    <td><strong>${st.statusName}</strong></td>
                                    <td><span class="badge badge-gray">${st.status}</span></td>
                                    <td style="text-align: right; font-weight: 700;">${st.count}</td>
                                    <td style="text-align: right; font-weight: 700; color: #1e293b;">
                                        <fmt:formatNumber value="${st.amount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

        <!-- TAB 6: THỐNG KÊ KHÁCH HÀNG & HỘI VIÊN (TASK 32: CUSTOMER STATISTICS) -->
        <c:if test="${currentTab == 'customers'}">
            <div class="metric-grid">
                <div class="metric-card" style="border-left-color: #3b82f6;">
                    <div class="metric-title">Tổng Khách Hàng</div>
                    <div class="metric-val">${customerStats.totalCustomers}</div>
                    <div class="metric-desc">Tài khoản khách hàng trên hệ thống</div>
                </div>
                <div class="metric-card" style="border-left-color: #10b981;">
                    <div class="metric-title">Khách Mới Đăng Ký (7 Ngày)</div>
                    <div class="metric-val" style="color: #10b981;">+${customerStats.newCustomers7d}</div>
                    <div class="metric-desc">Trong 30 ngày: +${customerStats.newCustomers30d} người dùng mới</div>
                </div>
                <div class="metric-card" style="border-left-color: #8b5cf6;">
                    <div class="metric-title">Hội Viên Thân Thiết (Member)</div>
                    <div class="metric-val" style="color: #8b5cf6;">${customerStats.memberCustomers}</div>
                    <div class="metric-desc">Khách hàng sở hữu gói thành viên</div>
                </div>
                <div class="metric-card" style="border-left-color: #f59e0b;">
                    <div class="metric-title">Điểm Tích Lũy (Loyalty Points)</div>
                    <div class="metric-val" style="color: #f59e0b;">
                        <fmt:formatNumber value="${customerStats.totalPointsEarned}" type="number" groupingUsed="true"/>
                    </div>
                    <div class="metric-desc">Đã đổi: <fmt:formatNumber value="${customerStats.totalPointsRedeemed}" type="number" groupingUsed="true"/> điểm</div>
                </div>
            </div>

            <!-- TOP KHÁCH HÀNG CHI TIÊU NHIỀU NHẤT -->
            <div class="content-card">
                <div class="card-header-bar">
                    <h2 class="card-heading">🏆 Top Khách Hàng Thân Thiết Chi Tiêu Nhiều Nhất</h2>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Họ &amp; Tên Khách Hàng</th>
                                <th>Email</th>
                                <th>Số Điện Thoại</th>
                                <th>Hạng Hội Viên</th>
                                <th style="text-align: right;">Số Đơn Đã Mua</th>
                                <th style="text-align: right;">Tổng Chi Tiêu (VNĐ)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${customerStats.topCustomers}" varStatus="loop">
                                <tr>
                                    <td><span class="badge badge-purple">${loop.index + 1}</span></td>
                                    <td><strong>${c.fullName}</strong></td>
                                    <td>${c.email}</td>
                                    <td>${c.phone}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.isMember}"><span class="badge badge-purple">⭐ Hội Viên VIP</span></c:when>
                                            <c:otherwise><span class="badge badge-gray">Khách Tiêu Chuẩn</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right; font-weight: 600;">${c.orderCount} đơn</td>
                                    <td style="text-align: right; font-weight: 800; color: #1e293b;">
                                        <fmt:formatNumber value="${c.totalSpent}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

    </div>

</body>
</html>
