<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Giỏ Hàng - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .cart-container {
            width: 92%;
            max-width: 1200px;
            margin: 35px auto 60px;
        }
        .cart-grid {
            display: grid;
            grid-template-columns: 1fr 360px;
            gap: 25px;
            align-items: start;
        }
        .cart-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid #e1ebe0;
            box-shadow: 0 4px 15px rgba(0,0,0,0.03);
            padding: 24px;
        }
        .cart-table {
            width: 100%;
            border-collapse: collapse;
        }
        .cart-table th {
            text-align: left;
            padding: 12px 14px;
            border-bottom: 2px solid #edf3eb;
            color: #2c592e;
            font-size: 14px;
        }
        .cart-table td {
            padding: 16px 14px;
            border-bottom: 1px solid #edf3eb;
            vertical-align: middle;
        }
        .cart-item-info {
            display: flex;
            align-items: center;
            gap: 14px;
        }
        .cart-thumb {
            width: 60px;
            height: 60px;
            object-fit: cover;
            border-radius: 8px;
            border: 1px solid #e1ebe0;
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 14px;
            font-size: 14px;
            color: #555;
        }
        .summary-total {
            border-top: 1px dashed #d5ded3;
            padding-top: 15px;
            margin-top: 15px;
            font-size: 18px;
            font-weight: bold;
            color: #275629;
        }
        .empty-cart-box {
            text-align: center;
            padding: 50px 20px;
            background: white;
            border-radius: 12px;
            border: 1px solid #e1ebe0;
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
        <a href="${pageContext.request.contextPath}/cart" style="font-weight: bold; color: #2e7d32;">Giỏ hàng</a>
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>

<div class="cart-container">

    <div class="page-header">
        <div>
            <h1 class="page-title">Giỏ Hàng</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Khung giao diện giỏ hàng mẫu dành cho khách hàng đã đăng nhập.</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty cartItems}">
            <div class="empty-cart-box">
                <h3 style="color: #444; margin-bottom: 10px;">Giỏ hàng của bạn đang trống</h3>
                <p style="color: #777; margin-bottom: 25px; font-size: 14px;">Hãy khám phá các loại trái cây tươi ngon và chọn mua nhé!</p>
                <a href="${pageContext.request.contextPath}/home" class="btn-primary" style="padding: 12px 26px;">
                    Mua sắm ngay
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="cart-grid">
                
                <!-- BẢNG SẢN PHẨM TRONG GIỎ (KHUNG HIỂN THỊ MẪU) -->
                <div class="cart-card">
                    <table class="cart-table">
                        <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Đơn giá</th>
                                <th style="width: 100px; text-align: center;">Số lượng</th>
                                <th>Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${cartItems}">
                                <tr>
                                    <td>
                                        <div class="cart-item-info">
                                            <img src="${not empty item.imageUrl ? item.imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}" 
                                                 alt="${item.productName}" 
                                                 class="cart-thumb"
                                                 onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                                            <div>
                                                <strong style="color: #2e532f; font-size: 14px;">${item.productName}</strong>
                                                <div style="font-size: 12px; color: #888;">${item.packagingType} (${item.unit})</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.unitPrice}" pattern="#,###"/> đ
                                    </td>
                                    <td style="text-align: center;">
                                        <span style="display: inline-block; font-weight: 600; padding: 4px 14px; background: #eef5ec; border-radius: 6px; color: #2e7d32;">
                                            ${item.quantity}
                                        </span>
                                    </td>
                                    <td>
                                        <strong style="color: #275629;">
                                            <fmt:formatNumber value="${item.itemTotal}" pattern="#,###"/> đ
                                        </strong>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- TỔNG KẾT ĐƠN HÀNG (SUMMARY) -->
                <div class="cart-card">
                    <h3 style="font-size: 17px; color: #275629; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #edf3eb;">
                        Tóm Tắt Đơn Hàng
                    </h3>

                    <div class="summary-row">
                        <span>Tạm tính:</span>
                        <strong><fmt:formatNumber value="${subtotal}" pattern="#,###"/> đ</strong>
                    </div>

                    <div class="summary-row">
                        <span>Phí vận chuyển:</span>
                        <span>
                            <c:choose>
                                <c:when test="${shippingFee == 0}">
                                    <span style="color: #2e7d32; font-weight: bold;">Miễn phí</span>
                                </c:when>
                                <c:otherwise>
                                    <fmt:formatNumber value="${shippingFee}" pattern="#,###"/> đ
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="summary-row summary-total">
                        <span>Tổng cộng:</span>
                        <span><fmt:formatNumber value="${grandTotal}" pattern="#,###"/> đ</span>
                    </div>

                    <button class="btn-primary" style="width: 100%; padding: 13px; font-size: 14px; margin-top: 15px; opacity: 0.85; cursor: pointer;" onclick="alert('Khung giao diện giỏ hàng mẫu - Chức năng đặt hàng đang được hoàn thiện!');">
                        Tiến Hành Đặt Hàng
                    </button>

                    <div style="text-align: center; margin-top: 15px;">
                        <a href="${pageContext.request.contextPath}/home" style="color: #4caf50; font-size: 13px; text-decoration: underline;">
                            Tiếp tục mua sắm
                        </a>
                    </div>
                </div>

            </div>
        </c:otherwise>
    </c:choose>

</div>

</body>
</html>
