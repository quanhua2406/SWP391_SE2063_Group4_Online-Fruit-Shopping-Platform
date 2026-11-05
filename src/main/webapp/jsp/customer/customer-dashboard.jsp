<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển khách hàng - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: Arial, Helvetica, sans-serif;
            background: #f6f8fa;
            color: #333;
        }

        /* =========================
           NAVBAR
           ========================= */

        .navbar {
            width: 100%;
            min-height: 70px;
            background: #ffffff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 60px;
            border-bottom: 1px solid #e5e5e5;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }

        .logo {
            font-size: 24px;
            font-weight: bold;
            color: #2e7d32;
        }

        .nav-actions {
            display: flex;
            align-items: center;
            gap: 25px;
        }

        .nav-actions a {
            text-decoration: none;
            color: #333;
            font-size: 15px;
            font-weight: 500;
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

        .bell-badge {
            background: #d32f2f;
            color: #ffffff;
            font-size: 11px;
            font-weight: 700;
            padding: 2px 7px;
            border-radius: 10px;
            line-height: 1;
            margin-left: 4px;
        }

        /* =========================
           DASHBOARD
           ========================= */

        .dashboard {
            max-width: 1200px;
            margin: 0 auto;
            padding: 60px 30px;
        }

        .dashboard h1 {
            text-align: center;
            font-size: 32px;
            color: #222;
            margin-bottom: 12px;
        }

        .dashboard-welcome {
            text-align: center;
            color: #777;
            font-size: 16px;
            margin-bottom: 45px;
        }

        /* =========================
           DASHBOARD GRID
           ========================= */

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 25px;
            align-items: stretch;
        }

        .dashboard-card {
            background: #ffffff;
            border-radius: 14px;
            padding: 30px 25px;
            min-height: 285px;
            text-align: center;
            border: 1px solid #e8e8e8;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
            transition: transform 0.25s ease,
                        box-shadow 0.25s ease;

            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .dashboard-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.10);
        }

        .dashboard-icon {
            font-size: 42px;
            line-height: 1.3;
            margin-bottom: 18px;
        }

        .dashboard-card h3 {
            font-size: 22px;
            margin-bottom: 12px;
            color: #222;
        }

        .dashboard-card p {
            font-size: 15px;
            line-height: 1.6;
            color: #777;
            min-height: 48px;
            margin-bottom: 22px;
        }

        /* Keep dashboard buttons consistent */
        .dashboard-card .card-button {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 140px;
            min-height: 46px;
            margin-top: auto;
            padding: 12px 22px;

            background: #2e7d32;
            color: #ffffff;
            text-decoration: none;
            border: none;
            border-radius: 7px;

            font-size: 15px;
            font-weight: 700;
            line-height: 1.3;
            cursor: pointer;
            transition: background 0.2s ease;
        }

        .dashboard-card .card-button:hover {
            background: #1b5e20;
            color: #ffffff;
        }

        /* =========================
           RESPONSIVE
           ========================= */

        @media (max-width: 900px) {
            .navbar {
                padding: 0 25px;
            }

            .dashboard-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 600px) {
            .navbar {
                padding: 20px;
                flex-direction: column;
                gap: 15px;
            }

            .nav-actions {
                gap: 15px;
                flex-wrap: wrap;
                justify-content: center;
            }

            .dashboard {
                padding: 40px 20px;
            }

            .dashboard h1 {
                font-size: 26px;
            }

            .dashboard-welcome {
                margin-bottom: 30px;
            }

            .dashboard-grid {
                grid-template-columns: 1fr;
                gap: 20px;
            }

            .dashboard-card {
                min-height: 265px;
                padding: 28px 20px;
            }
        }
    </style>
</head>

<body>

<!-- =========================
     NAVBAR (Reusable Component)
     ========================= -->
<jsp:include page="/includes/header.jsp" />

<!-- =========================
     CUSTOMER DASHBOARD
     ========================= -->

<section class="dashboard">

    <h1>Bảng điều khiển khách hàng</h1>

    <p class="dashboard-welcome">
        Chào mừng trở lại, khách hàng!
    </p>

    <div class="dashboard-grid">

        <!-- Notifications Card -->
        <div class="dashboard-card">

            <div class="dashboard-icon">🔔</div>

            <h3>Thông báo</h3>

            <p>
                Theo dõi cập nhật đơn hàng, trạng thái giao hàng và ưu đãi khuyến mãi.
            </p>

            <a class="card-button"
               href="${pageContext.request.contextPath}/notifications">
                Xem thông báo
            </a>

        </div>

        <!-- Browse Fruits -->
        <div class="dashboard-card">

            <div class="dashboard-icon">🍎</div>

            <h3>Xem trái cây</h3>

            <p>
                Khám phá trái cây tươi và tìm sản phẩm yêu thích.
            </p>

            <a class="card-button"
               href="${pageContext.request.contextPath}/home">
                Mua ngay
            </a>

        </div>

        <!-- My Cart -->
        <div class="dashboard-card">

            <div class="dashboard-icon">🛒</div>

            <h3>Giỏ hàng của tôi</h3>

            <p>
                Xem và quản lý trái cây trong giỏ hàng.
            </p>

            <a class="card-button"
               href="${pageContext.request.contextPath}/cart">
                Xem giỏ hàng
            </a>

        </div>

        <!-- My Orders -->
        <div class="dashboard-card">

            <div class="dashboard-icon">📦</div>

            <h3>Đơn hàng của tôi</h3>

            <p>
                Xem lịch sử và các đơn hàng hiện tại.
            </p>

            <a class="card-button" href="${pageContext.request.contextPath}/orders">
                Đơn hàng của tôi
            </a>

        </div>

        <!-- Track Delivery -->
        <div class="dashboard-card">

            <div class="dashboard-icon">🚚</div>

            <h3>Theo dõi giao hàng</h3>

            <p>
                Kiểm tra trạng thái giao hàng hiện tại.
            </p>

            <a class="card-button"
               href="${pageContext.request.contextPath}/orders?status=shipping">
                Theo dõi đơn
            </a>

        </div>

        <!-- Gói Định Kỳ -->
        <div class="dashboard-card" style="border: 2px solid #4caf50;">
            <div class="dashboard-icon">🎁</div>
            <h3>Gói Định Kỳ</h3>
            <p>
                Theo dõi lịch nhận trái cây định kỳ, tạm dừng hoặc đổi ngày giao hàng.
            </p>
            <a class="card-button" href="${pageContext.request.contextPath}/my-subscriptions" style="background: #2e7d32;">
                Gói Của Tôi →
            </a>
        </div>

        <!-- My Reviews -->
        <div class="dashboard-card" style="border: 2px solid #f59e0b;">
            <div class="dashboard-icon">⭐</div>
            <h3>Đánh giá của tôi</h3>
            <p>
                Quản lý các nhận xét đã gửi và đánh giá các sản phẩm đã mua.
            </p>
            <a class="card-button" href="${pageContext.request.contextPath}/customer/reviews" style="background: #f59e0b;">
                Đánh giá của tôi →
            </a>
        </div>

    </div>

</section>

</body>
</html>
