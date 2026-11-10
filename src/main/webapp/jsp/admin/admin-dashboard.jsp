<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Bảng điều khiển quản trị - FreshFruit</title>

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

        .nav-bell-link {
            position: relative;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .nav-bell-badge {
            background: #d32f2f;
            color: #ffffff;
            font-size: 11px;
            font-weight: 700;
            padding: 2px 7px;
            border-radius: 10px;
            line-height: 1;
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

        /* ==================== KPI ROW (FEATURE 4 - SALES DASHBOARD) ==================== */
        .kpi-row {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 35px;
        }

        .kpi-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 22px 24px;
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.04);
            border-left: 5px solid #2e7d32;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .kpi-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
        }

        .kpi-card:nth-child(2) { border-left-color: #3b82f6; }
        .kpi-card:nth-child(3) { border-left-color: #f59e0b; }
        .kpi-card:nth-child(4) { border-left-color: #8b5cf6; }

        .kpi-label {
            font-size: 13px;
            font-weight: 700;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .kpi-value {
            font-size: 26px;
            font-weight: 800;
            color: #1e293b;
            margin-bottom: 6px;
        }

        .kpi-subtext {
            font-size: 13px;
            color: #94a3b8;
        }

        /* ==================== TOP SELLING FRUITS WIDGET ==================== */
        .top-fruits-section {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 25px;
            margin-bottom: 40px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
        }

        .top-fruits-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 18px;
            padding-bottom: 12px;
            border-bottom: 1px solid #f1f5f9;
        }

        .top-fruits-header h2 {
            font-size: 19px;
            color: #1e293b;
            font-weight: 700;
            margin: 0;
        }

        .view-all-link {
            font-size: 14px;
            font-weight: 600;
            color: #2e7d32;
            transition: color 0.2s;
        }

        .view-all-link:hover {
            color: #1b5e20;
            text-decoration: underline;
        }

        .table-container {
            overflow-x: auto;
        }

        .report-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
            text-align: left;
        }

        .report-table th {
            background: #f8fafc;
            color: #475569;
            font-weight: 700;
            padding: 12px 14px;
            border-bottom: 2px solid #e2e8f0;
        }

        .report-table td {
            padding: 12px 14px;
            border-bottom: 1px solid #f1f5f9;
            color: #334155;
        }

        .report-table tr:hover {
            background: #f8fafc;
        }

        .rank-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 24px;
            height: 24px;
            border-radius: 50%;
            background: #e2e8f0;
            font-size: 12px;
            font-weight: 700;
            color: #475569;
        }

        tr:nth-child(1) .rank-badge { background: #fef3c7; color: #b45309; }
        tr:nth-child(2) .rank-badge { background: #f1f5f9; color: #475569; }
        tr:nth-child(3) .rank-badge { background: #ffedd5; color: #c2410c; }

        @media (max-width: 900px) {
            .kpi-row {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        @media (max-width: 600px) {
            .kpi-row {
                grid-template-columns: 1fr;
            }
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
                🏠 Trang chủ
            </a>

            <a href="${pageContext.request.contextPath}/admin/notifications" class="nav-bell-link">
                🔔 Thông báo
                <c:if test="${not empty sessionScope.adminUnreadCount and sessionScope.adminUnreadCount > 0}">
                    <span class="nav-bell-badge">${sessionScope.adminUnreadCount}</span>
                </c:if>
            </a>

            <a href="${pageContext.request.contextPath}/logout"
               class="logout">
                Đăng xuất
            </a>

        </nav>

    </header>

    <!-- ==================== DASHBOARD ==================== -->

    <main class="dashboard">

        <h1>Bảng điều khiển quản trị</h1>

        <p class="dashboard-welcome">
            Chào mừng trở lại, Quản trị viên!
        </p>

        <!-- KPI METRICS SUMMARY BANNER (TASK 26: VIEW SALES DASHBOARD) -->
        <div class="kpi-row">
            <div class="kpi-card">
                <div class="kpi-label">Tổng Doanh Thu Hoàn Tất</div>
                <div class="kpi-value">
                    <fmt:formatNumber value="${not empty kpi.totalRevenue ? kpi.totalRevenue : 0}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                </div>
                <div class="kpi-subtext">Từ ${not empty kpi.completedOrders ? kpi.completedOrders : 0} đơn hàng giao thành công</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-label">Tổng Đơn Hàng Hệ Thống</div>
                <div class="kpi-value">${not empty kpi.totalOrders ? kpi.totalOrders : 0}</div>
                <div class="kpi-subtext"><span style="color:#f59e0b;font-weight:600;">${not empty kpi.pendingOrders ? kpi.pendingOrders : 0}</span> đơn chờ xác nhận</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-label">Cửa Hàng Hoạt Động</div>
                <div class="kpi-value">${not empty kpi.activeShops ? kpi.activeShops : 0}</div>
                <div class="kpi-subtext">Đang kinh doanh trên sàn</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-label">Khách Hàng Khả Dụng</div>
                <div class="kpi-value">${not empty kpi.activeCustomers ? kpi.activeCustomers : 0}</div>
                <div class="kpi-subtext">Tài khoản khách hàng hoạt động</div>
            </div>
        </div>

        <!-- TOP SELLING FRUITS WIDGET (TASK 26: VIEW SALES DASHBOARD) -->
        <c:if test="${not empty topFruits}">
            <div class="top-fruits-section">
                <div class="top-fruits-header">
                    <h2>🍇 Top Hoa Quả Bán Chạy Nhất</h2>
                    <a href="${pageContext.request.contextPath}/admin/reports?tab=sales" class="view-all-link">Xem Báo Cáo Bán Hàng Chi Tiết &rarr;</a>
                </div>
                <div class="table-container">
                    <table class="report-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Tên Hoa Quả</th>
                                <th>Xuất Xứ</th>
                                <th>Danh Mục</th>
                                <th>Cửa Hàng</th>
                                <th style="text-align: right;">Đã Bán</th>
                                <th style="text-align: right;">Doanh Thu (VNĐ)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${topFruits}" varStatus="status">
                                <tr>
                                    <td><span class="rank-badge">${status.index + 1}</span></td>
                                    <td><strong>${item.productName}</strong></td>
                                    <td>${item.origin}</td>
                                    <td>${item.categoryName}</td>
                                    <td>${item.shopName}</td>
                                    <td style="text-align: right; font-weight: 600; color: #2e7d32;">${item.totalQuantity}</td>
                                    <td style="text-align: right; font-weight: 700; color: #1f2937;">
                                        <fmt:formatNumber value="${item.totalSales}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>

        <div class="dashboard-grid">

            <!-- USER MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">👥</div>

                <h3>Quản lý người dùng</h3>

                <p>
                    Quản lý tài khoản khách hàng, chủ shop
                    và nhân viên giao hàng.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/user-management">
                    Quản lý người dùng
                </a>

            </div>

            <!-- SHOP OWNER MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🏪</div>

                <h3>Quản lý chủ shop</h3>

                <p>
                    Xem mọi shop đã đăng ký và khóa hoặc mở khóa
                    chủ shop cùng sản phẩm của họ.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/shops">
                    Quản lý shop
                </a>

            </div>

            <!-- PROMOTION APPROVALS -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🏷️</div>

                <h3>Duyệt khuyến mãi</h3>

                <p>
                    Duyệt hoặc từ chối giá khuyến mãi
                    do chủ shop yêu cầu.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/promotion-approvals">
                    Duyệt khuyến mãi
                </a>

            </div>

            <!-- ORDER MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🛒</div>

                <h3>Quản lý đơn hàng</h3>

                <p>
                    Theo dõi mọi đơn hàng của tất cả shop
                    và xem chi tiết từng đơn.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/orders">
                    Xem đơn hàng
                </a>

            </div>

            <!-- DELIVERY ADMINISTRATION -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🚚</div>

                <h3>Quản lý giao hàng</h3>

                <p>
                    Theo dõi tiến độ giao hàng và quản lý nhân viên giao.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/deliveries">
                    Theo dõi giao hàng
                </a>

            </div>

            <!-- ORDER ISSUES -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🛠️</div>

                <h3>Sự cố đơn hàng</h3>

                <p>
                    Xử lý đơn thất bại, đã hủy và bị trễ
                    và đóng các khiếu nại của khách hàng.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/order-issues">
                    Xử lý sự cố
                </a>

            </div>

            <!-- INAPPROPRIATE REVIEW MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🛡️</div>

                <h3>Quản lý đánh giá không phù hợp</h3>

                <p>
                    Kiểm duyệt đánh giá bị báo cáo, xem minh chứng đính kèm,
                    ẩn hoặc xóa phản hồi không phù hợp của khách hàng.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/reviews?status=reported">
                    Kiểm duyệt đánh giá
                    <c:if test="${not empty reportedReviewsCount && reportedReviewsCount > 0}">
                        (${reportedReviewsCount})
                    </c:if>
                </a>

            </div>

            <!-- COMMISSION REPORT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">💰</div>

                <h3>Báo cáo hoa hồng</h3>

                <p>
                    Xem phí nền tảng thu từ các shop,
                    theo shop và theo % hoa hồng.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/commission-report">
                    Xem hoa hồng
                </a>

            </div>

            <!-- REPORTS -->

            <div class="dashboard-card">

                <div class="dashboard-icon">📊</div>

                <h3>Báo cáo</h3>

                <p>
                    Xem báo cáo bán hàng và báo cáo nền tảng.
                </p>

                <a class="action-btn" href="${pageContext.request.contextPath}/admin/reports">
                    Xem báo cáo
                </a>

            </div>

            <!-- NOTIFICATIONS & SYSTEM ALERTS -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🔔</div>

                <h3>Thông báo &amp; cảnh báo</h3>

                <p>
                    Xem đánh giá bị báo cáo, cảnh báo tồn kho nghiêm trọng,
                    yêu cầu hoàn tiền và sự cố hệ thống.
                </p>

                <a class="action-btn"
                   href="${pageContext.request.contextPath}/admin/notifications">
                    Xem thông báo
                </a>

            </div>

        </div>

    </main>

</body>
</html>