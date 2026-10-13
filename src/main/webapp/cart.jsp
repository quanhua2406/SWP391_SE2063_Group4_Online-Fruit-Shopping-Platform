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
        .item-chk {
            width: 18px;
            height: 18px;
            cursor: pointer;
            accent-color: #2e7d32;
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
        .btn-checkout {
            display: block;
            width: 100%;
            padding: 14px;
            font-size: 15px;
            font-weight: bold;
            text-align: center;
            background: #4caf50;
            color: #ffffff;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            text-decoration: none;
            box-sizing: border-box;
            transition: background 0.2s;
        }
        .btn-checkout:hover {
            background: #43a047;
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
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Chọn các sản phẩm hoa quả bạn muốn mua và tiến hành đặt hàng.</p>
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
            <form id="cartForm" action="${pageContext.request.contextPath}/checkout" method="GET">
                <div class="cart-grid">
                    
                    <!-- BẢNG SẢN PHẨM TRONG GIỎ (CHỌN SẢN PHẨM ĐẶT HÀNG) -->
                    <div class="cart-card">
                        <table class="cart-table">
                            <thead>
                                <tr>
                                    <th style="width: 40px; text-align: center;">
                                        <input type="checkbox" id="checkAll" checked class="item-chk" onclick="toggleSelectAll(this)" title="Chọn tất cả">
                                    </th>
                                    <th>Sản phẩm</th>
                                    <th>Đơn giá</th>
                                    <th style="width: 100px; text-align: center;">Số lượng</th>
                                    <th>Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${cartItems}">
                                    <tr>
                                        <td style="text-align: center;">
                                            <input type="checkbox" name="selectedVariants" value="${item.variantId}" checked 
                                                   class="item-chk item-box" data-price="${item.itemTotal}" onchange="recalculateCart()">
                                        </td>
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
                            <span>Tạm tính (<span id="selectedCount">3</span> sản phẩm):</span>
                            <strong id="displaySubtotal"><fmt:formatNumber value="${subtotal}" pattern="#,###"/> đ</strong>
                        </div>

                        <div class="summary-row">
                            <span>Phí vận chuyển:</span>
                            <span id="displayShipping">
                                <fmt:formatNumber value="${shippingFee}" pattern="#,###"/> đ
                            </span>
                        </div>

                        <div class="summary-row summary-total">
                            <span>Tổng cộng:</span>
                            <span id="displayGrandTotal" style="color: #275629;"><fmt:formatNumber value="${grandTotal}" pattern="#,###"/> đ</span>
                        </div>

                        <button type="submit" class="btn-checkout" style="margin-top: 18px;" onclick="return validateCheckout();">
                            Tiến Hành Đặt Hàng
                        </button>

                        <div style="text-align: center; margin-top: 15px;">
                            <a href="${pageContext.request.contextPath}/home" style="color: #4caf50; font-size: 13px; text-decoration: underline;">
                                Tiếp tục mua sắm
                            </a>
                        </div>
                    </div>

                </div>
            </form>
        </c:otherwise>
    </c:choose>

</div>

<script>
    function toggleSelectAll(master) {
        var boxes = document.querySelectorAll('.item-box');
        boxes.forEach(function(box) {
            box.checked = master.checked;
        });
        recalculateCart();
    }

    function recalculateCart() {
        var boxes = document.querySelectorAll('.item-box');
        var subtotal = 0;
        var count = 0;
        boxes.forEach(function(box) {
            if (box.checked) {
                subtotal += parseFloat(box.getAttribute('data-price')) || 0;
                count++;
            }
        });

        var shipping = subtotal > 0 ? 25000 : 0;
        var total = subtotal + shipping;

        document.getElementById('selectedCount').innerText = count;
        document.getElementById('displaySubtotal').innerText = subtotal.toLocaleString('vi-VN') + ' đ';
        document.getElementById('displayShipping').innerText = shipping > 0 ? (shipping.toLocaleString('vi-VN') + ' đ') : '0 đ';
        document.getElementById('displayGrandTotal').innerText = total.toLocaleString('vi-VN') + ' đ';

        var master = document.getElementById('checkAll');
        if (master) {
            master.checked = (count === boxes.length && boxes.length > 0);
        }
    }

    function validateCheckout() {
        var checked = document.querySelectorAll('.item-box:checked');
        if (checked.length === 0) {
            alert('Vui lòng chọn ít nhất 1 sản phẩm để tiến hành đặt hàng!');
            return false;
        }
        return true;
    }
</script>

</body>
</html>
