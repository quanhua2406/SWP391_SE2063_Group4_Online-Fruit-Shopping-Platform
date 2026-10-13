<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thanh Toán Đơn Hàng - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .checkout-container {
            width: 92%;
            max-width: 1100px;
            margin: 35px auto 60px;
        }
        .checkout-grid {
            display: grid;
            grid-template-columns: 1fr 400px;
            gap: 30px;
            align-items: start;
        }
        .checkout-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid #e1ebe0;
            box-shadow: 0 4px 15px rgba(0,0,0,0.03);
            padding: 26px;
        }
        .section-title {
            font-size: 17px;
            color: #275629;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid #edf3eb;
            font-weight: bold;
        }
        .form-group {
            margin-bottom: 16px;
        }
        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-size: 13px;
            font-weight: 600;
            color: #333;
        }
        .form-control {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #ccd8c9;
            border-radius: 6px;
            font-size: 14px;
            box-sizing: border-box;
            outline: none;
            transition: border 0.2s;
        }
        .form-control:focus {
            border-color: #4caf50;
        }
        .payment-option {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 14px;
            border: 1px solid #e0eae0;
            border-radius: 8px;
            margin-bottom: 10px;
            cursor: pointer;
            transition: all 0.2s;
        }
        .payment-option:hover {
            border-color: #81c784;
            background: #fbfdfb;
        }
        .payment-option input {
            accent-color: #2e7d32;
            width: 16px;
            height: 16px;
        }
        .item-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 0;
            border-bottom: 1px solid #f0f4ef;
        }
        .item-info {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .item-thumb {
            width: 48px;
            height: 48px;
            object-fit: cover;
            border-radius: 6px;
            border: 1px solid #e1ebe0;
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;
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
        .btn-confirm-order {
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
            margin-top: 20px;
            transition: background 0.2s;
        }
        .btn-confirm-order:hover {
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
        <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </div>
</header>

<div class="checkout-container">

    <div class="page-header" style="margin-bottom: 25px;">
        <div>
            <h1 class="page-title">Xác Nhận & Thanh Toán Đơn Hàng</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Vui lòng kiểm tra địa chỉ giao nhận và phương thức thanh toán.</p>
        </div>
    </div>

    <form action="${pageContext.request.contextPath}/checkout" method="POST">
        <input type="hidden" name="totalAmount" value="${grandTotal}">

        <div class="checkout-grid">

            <!-- THÔNG TIN GIAO HÀNG & PHƯƠNG THỨC THANH TOÁN -->
            <div class="checkout-card">
                <div class="section-title">1. Thông Tin Nhận Hàng</div>

                <div class="form-group">
                    <label for="recipientName">Họ và tên người nhận <span style="color: red;">*</span></label>
                    <input type="text" id="recipientName" name="recipientName" class="form-control" 
                           value="${not empty sessionScope.user.fullName ? sessionScope.user.fullName : 'Nguyễn Văn An'}" required>
                </div>

                <div class="form-group">
                    <label for="phone">Số điện thoại liên hệ <span style="color: red;">*</span></label>
                    <input type="text" id="phone" name="phone" class="form-control" 
                           value="${not empty sessionScope.user.phone ? sessionScope.user.phone : '0988888888'}" required>
                </div>

                <div class="form-group">
                    <label for="address">Địa chỉ giao hàng chi tiết <span style="color: red;">*</span></label>
                    <input type="text" id="address" name="address" class="form-control" 
                           value="Số 123 Đường Nguyễn Trãi, Phường Bến Thành, Quận 1, TP.HCM" required>
                </div>

                <div class="form-group">
                    <label for="note">Ghi chú giao hàng (Tùy chọn)</label>
                    <input type="text" id="note" name="note" class="form-control" 
                           placeholder="Ví dụ: Giao giờ hành chính, gọi trước khi đến...">
                </div>

                <div class="section-title" style="margin-top: 30px;">2. Phương Thức Thanh Toán</div>

                <label class="payment-option">
                    <input type="radio" name="paymentMethod" value="Thanh toán khi nhận hàng (COD)" checked>
                    <div>
                        <strong>Thanh toán khi nhận hàng (COD)</strong>
                        <div style="font-size: 12px; color: #777;">Thanh toán tiền mặt cho nhân viên giao hàng khi nhận hoa quả.</div>
                    </div>
                </label>

                <label class="payment-option">
                    <input type="radio" name="paymentMethod" value="Chuyển khoản ngân hàng / VNPay">
                    <div>
                        <strong>Chuyển khoản ngân hàng / VNPay</strong>
                        <div style="font-size: 12px; color: #777;">Quét mã QR qua ứng dụng ngân hàng hoặc ví điện tử.</div>
                    </div>
                </label>
            </div>

            <!-- TỔNG KẾT CÁC SẢN PHẨM ĐÃ CHỌN -->
            <div class="checkout-card">
                <div class="section-title">Đơn Hàng Của Bạn (${checkoutItems.size()} sản phẩm)</div>

                <div style="margin-bottom: 20px;">
                    <c:forEach var="item" items="${checkoutItems}">
                        <div class="item-row">
                            <div class="item-info">
                                <img src="${not empty item.imageUrl ? item.imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}" 
                                     alt="${item.productName}" class="item-thumb"
                                     onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                                <div>
                                    <div style="font-size: 13px; font-weight: bold; color: #2e532f;">${item.productName}</div>
                                    <div style="font-size: 12px; color: #888;">SL: ${item.quantity} x <fmt:formatNumber value="${item.unitPrice}" pattern="#,###"/> đ</div>
                                </div>
                            </div>
                            <strong style="font-size: 13px; color: #275629;">
                                <fmt:formatNumber value="${item.itemTotal}" pattern="#,###"/> đ
                            </strong>
                        </div>
                    </c:forEach>
                </div>

                <div class="summary-row">
                    <span>Tạm tính:</span>
                    <strong><fmt:formatNumber value="${subtotal}" pattern="#,###"/> đ</strong>
                </div>

                <div class="summary-row">
                    <span>Phí vận chuyển:</span>
                    <span><fmt:formatNumber value="${shippingFee}" pattern="#,###"/> đ</span>
                </div>

                <div class="summary-row summary-total">
                    <span>Tổng thanh toán:</span>
                    <span style="color: #275629;"><fmt:formatNumber value="${grandTotal}" pattern="#,###"/> đ</span>
                </div>

                <button type="submit" class="btn-confirm-order">
                    Xác Nhận Đặt Hàng
                </button>

                <div style="text-align: center; margin-top: 15px;">
                    <a href="${pageContext.request.contextPath}/cart" style="color: #4caf50; font-size: 13px; text-decoration: underline;">
                        Quay lại giỏ hàng
                    </a>
                </div>
            </div>

        </div>
    </form>

</div>

</body>
</html>
