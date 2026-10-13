<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Đặt Hàng Thành Công - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .success-container {
            width: 90%;
            max-width: 680px;
            margin: 50px auto 80px;
        }
        .success-card {
            background: #ffffff;
            border-radius: 14px;
            border: 1px solid #dce8db;
            box-shadow: 0 6px 20px rgba(0,0,0,0.04);
            padding: 40px 36px;
            text-align: center;
        }
        .icon-circle {
            width: 72px;
            height: 72px;
            background: #e8f5e9;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
            color: #2e7d32;
        }
        .success-title {
            font-size: 24px;
            font-weight: bold;
            color: #235425;
            margin-bottom: 10px;
        }
        .success-desc {
            font-size: 14px;
            color: #666;
            line-height: 1.6;
            margin-bottom: 28px;
        }
        .order-info-box {
            background: #fbfdfa;
            border: 1px solid #e5ede3;
            border-radius: 10px;
            padding: 22px 24px;
            text-align: left;
            margin-bottom: 30px;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 9px 0;
            border-bottom: 1px solid #f0f4ef;
            font-size: 14px;
        }
        .info-row:last-child {
            border-bottom: none;
            padding-bottom: 0;
        }
        .info-label {
            color: #777;
            font-weight: 500;
        }
        .info-value {
            color: #222;
            font-weight: 600;
            text-align: right;
        }
        .info-total {
            color: #275629;
            font-size: 16px;
            font-weight: bold;
        }
        .btn-group {
            display: flex;
            gap: 14px;
            justify-content: center;
        }
        .btn-home {
            flex: 1;
            padding: 13px 20px;
            background: #4caf50;
            color: #ffffff;
            font-size: 14px;
            font-weight: bold;
            border-radius: 8px;
            text-decoration: none;
            transition: background 0.2s;
            display: inline-block;
        }
        .btn-home:hover {
            background: #43a047;
        }
        .btn-cart {
            flex: 1;
            padding: 13px 20px;
            background: #ffffff;
            color: #2e7d32;
            border: 1px solid #a5d6a7;
            font-size: 14px;
            font-weight: bold;
            border-radius: 8px;
            text-decoration: none;
            transition: all 0.2s;
            display: inline-block;
        }
        .btn-cart:hover {
            background: #f1f8e9;
        }
    </style>
</head>
<body>

<header class="navbar">
    <div class="logo">
        FreshFruit
    </div>
    <div class="nav-actions">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </div>
</header>

<div class="success-container">
    <div class="success-card">
        <div class="icon-circle">
            <svg xmlns="http://www.w3.org/2000/svg" width="38" height="38" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
        </div>

        <h1 class="success-title">Đặt Hàng Thành Công!</h1>
        <p class="success-desc">
            Cảm ơn bạn đã tin tưởng lựa chọn hoa quả tươi tại <strong>FreshFruit</strong>.<br>
            Đơn hàng của bạn đã được tiếp nhận và nhân viên sẽ liên hệ để giao hàng sớm nhất.
        </p>

        <div class="order-info-box">
            <div class="info-row">
                <span class="info-label">Mã đơn hàng</span>
                <span class="info-value" style="color: #275629; font-weight: bold;">${orderId}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Người nhận hàng</span>
                <span class="info-value">${recipientName}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Số điện thoại</span>
                <span class="info-value">${phone}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Địa chỉ giao nhận</span>
                <span class="info-value" style="max-width: 60%; word-break: break-word;">${address}</span>
            </div>
            <c:if test="${not empty note}">
                <div class="info-row">
                    <span class="info-label">Ghi chú</span>
                    <span class="info-value" style="font-weight: normal; color: #555;">${note}</span>
                </div>
            </c:if>
            <div class="info-row">
                <span class="info-label">Phương thức thanh toán</span>
                <span class="info-value">${paymentMethod}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Tổng số tiền thanh toán</span>
                <span class="info-value info-total">
                    <fmt:formatNumber value="${totalAmount}" pattern="#,###"/> đ
                </span>
            </div>
        </div>

        <div class="btn-group">
            <a href="${pageContext.request.contextPath}/home" class="btn-home">
                Tiếp Tục Mua Sắm
            </a>
            <a href="${pageContext.request.contextPath}/cart" class="btn-cart">
                Xem Lại Giỏ Hàng
            </a>
        </div>
    </div>
</div>

</body>
</html>
