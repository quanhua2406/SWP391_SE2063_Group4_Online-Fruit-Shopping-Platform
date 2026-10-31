<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh Toán & Đặt Hàng | FreshFruit Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        :root {
            --primary: #10b981;
            --primary-dark: #059669;
            --primary-light: #ecfdf5;
            --text-dark: #1f2937;
            --text-muted: #6b7280;
            --border: #e5e7eb;
            --bg-light: #f9fafb;
            --danger: #ef4444;
            --warning: #f59e0b;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-light);
            color: var(--text-dark);
            margin: 0;
            padding: 0;
            line-height: 1.5;
        }

        .checkout-container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .breadcrumb {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: var(--text-muted);
            margin-bottom: 24px;
        }

        .breadcrumb a {
            color: var(--text-muted);
            text-decoration: none;
        }

        .breadcrumb a:hover {
            color: var(--primary);
        }

        .checkout-grid {
            display: grid;
            grid-template-columns: 1.5fr 1fr;
            gap: 30px;
        }

        @media (max-width: 900px) {
            .checkout-grid {
                grid-template-columns: 1fr;
            }
        }

        .checkout-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 24px;
            margin-bottom: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        .checkout-card-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 20px;
            padding-bottom: 12px;
            border-bottom: 1px solid var(--border);
        }

        .step-number {
            width: 28px;
            height: 28px;
            background: var(--primary);
            color: #ffffff;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 14px;
        }

        .checkout-card-title {
            font-size: 18px;
            font-weight: 600;
            margin: 0;
            color: var(--text-dark);
        }

        /* Address styles */
        .address-item {
            border: 1px solid var(--border);
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 12px;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: flex-start;
            gap: 12px;
        }

        .address-item:hover {
            border-color: var(--primary);
        }

        .address-item.selected {
            border-color: var(--primary);
            background: var(--primary-light);
        }

        .address-radio {
            margin-top: 4px;
            accent-color: var(--primary);
        }

        .address-info {
            flex: 1;
        }

        .address-name {
            font-weight: 600;
            color: var(--text-dark);
        }

        .address-phone {
            color: var(--text-muted);
            font-size: 14px;
            margin-left: 8px;
        }

        .address-detail {
            color: var(--text-muted);
            font-size: 14px;
            margin-top: 4px;
        }

        .badge-default {
            display: inline-block;
            background: #e0e7ff;
            color: #4338ca;
            font-size: 11px;
            padding: 2px 8px;
            border-radius: 999px;
            font-weight: 600;
            margin-left: 8px;
        }

        .new-address-toggle {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            color: var(--primary-dark);
            background: none;
            border: none;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            padding: 8px 0;
        }

        .new-address-form {
            background: var(--bg-light);
            border: 1px dashed var(--border);
            border-radius: 8px;
            padding: 16px;
            margin-top: 12px;
            display: none;
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
            margin-bottom: 12px;
        }

        .form-group {
            margin-bottom: 12px;
        }

        .form-group label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 4px;
            color: var(--text-dark);
        }

        .form-control {
            width: 100%;
            padding: 9px 12px;
            border: 1px solid var(--border);
            border-radius: 6px;
            font-size: 14px;
            box-sizing: border-box;
        }

        .form-control:focus {
            outline: none;
            border-color: var(--primary);
        }

        /* Delivery and Payment options */
        .option-box {
            border: 1px solid var(--border);
            border-radius: 8px;
            padding: 14px 16px;
            margin-bottom: 10px;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .option-box:hover {
            border-color: var(--primary);
        }

        .option-box.selected {
            border-color: var(--primary);
            background: var(--primary-light);
        }

        .option-left {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .option-title {
            font-weight: 600;
            font-size: 14px;
            color: var(--text-dark);
        }

        .option-desc {
            font-size: 13px;
            color: var(--text-muted);
        }

        .option-price {
            font-weight: 700;
            color: var(--primary-dark);
            font-size: 14px;
        }

        /* Order Review item list */
        .order-item-row {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 0;
            border-bottom: 1px solid var(--border);
        }

        .order-item-img {
            width: 52px;
            height: 52px;
            border-radius: 8px;
            object-fit: cover;
            border: 1px solid var(--border);
            background: #fff;
        }

        .order-item-info {
            flex: 1;
        }

        .order-item-name {
            font-weight: 600;
            font-size: 14px;
            color: var(--text-dark);
        }

        .order-item-meta {
            font-size: 12px;
            color: var(--text-muted);
        }

        .order-item-total {
            font-weight: 600;
            font-size: 14px;
            color: var(--text-dark);
            white-space: nowrap;
        }

        /* Voucher Box */
        .voucher-section {
            margin: 20px 0;
            padding: 16px;
            background: var(--bg-light);
            border-radius: 8px;
            border: 1px solid var(--border);
        }

        .voucher-input-group {
            display: flex;
            gap: 8px;
        }

        .btn-apply-voucher {
            background: var(--primary);
            color: #ffffff;
            border: none;
            border-radius: 6px;
            padding: 9px 16px;
            font-weight: 600;
            cursor: pointer;
            font-size: 14px;
        }

        .btn-apply-voucher:hover {
            background: var(--primary-dark);
        }

        .voucher-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 10px;
        }

        .voucher-tag {
            font-size: 12px;
            background: #ffffff;
            border: 1px dashed var(--primary);
            color: var(--primary-dark);
            padding: 3px 8px;
            border-radius: 4px;
            cursor: pointer;
            font-weight: 500;
        }

        .voucher-tag:hover {
            background: var(--primary-light);
        }

        /* Summary breakdown */
        .summary-row {
            display: flex;
            justify-content: space-between;
            font-size: 14px;
            margin-bottom: 10px;
            color: var(--text-muted);
        }

        .summary-row.total {
            font-size: 18px;
            font-weight: 700;
            color: var(--text-dark);
            padding-top: 14px;
            border-top: 2px dashed var(--border);
            margin-top: 14px;
        }

        .total-highlight {
            color: #dc2626;
            font-size: 22px;
        }

        .btn-place-order {
            width: 100%;
            background: #10b981;
            color: #ffffff;
            border: none;
            border-radius: 8px;
            padding: 14px;
            font-size: 16px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;
            margin-top: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .btn-place-order:hover {
            background: #059669;
            transform: translateY(-1px);
            box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3);
        }

        .alert-error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }

        .alert-success {
            background: #d1fae5;
            color: #065f46;
            border: 1px solid #a7f3d0;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }
    </style>
</head>
<body>

    <!-- Header Navigation (Reusable Component) -->
    <jsp:include page="/includes/header.jsp" />

    <div class="checkout-container">
        <!-- Breadcrumb -->
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
            <span>/</span>
            <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>
            <span>/</span>
            <span style="color: var(--text-dark); font-weight: 600;">Thanh toán & Đặt hàng</span>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert-error">${errorMessage}</div>
        </c:if>

        <form id="checkoutForm" action="${pageContext.request.contextPath}/checkout" method="POST">
            <input type="hidden" name="action" value="place">

            <div class="checkout-grid">
                <!-- CỘT TRÁI: ĐỊA CHỈ, VẬN CHUYỂN, THANH TOÁN -->
                <div class="checkout-col-left">

                    <!-- BƯỚC 1: CHỌN ĐỊA CHỈ GIAO HÀNG (TASK 1) -->
                    <div class="checkout-card">
                        <div class="checkout-card-header">
                            <span class="step-number">1</span>
                            <h2 class="checkout-card-title">Địa Chỉ Nhận Hàng (Select Delivery Address)</h2>
                        </div>

                        <c:choose>
                            <c:when test="${not empty addresses}">
                                <c:forEach var="addr" items="${addresses}">
                                    <label class="address-item ${addr.isDefault ? 'selected' : ''}" onclick="selectAddress(this)">
                                        <input type="radio" name="addressId" value="${addr.addressId}" class="address-radio"
                                               ${addr.isDefault ? 'checked' : ''} onchange="onAddressChanged(this)">
                                        <div class="address-info">
                                            <div>
                                                <span class="address-name">${addr.recipientName}</span>
                                                <span class="address-phone">${addr.phone}</span>
                                                <c:if test="${addr.isDefault}">
                                                    <span class="badge-default">Mặc định</span>
                                                </c:if>
                                            </div>
                                            <div class="address-detail">
                                                ${addr.addressLine}, ${addr.ward}, ${addr.district}, ${addr.province}
                                            </div>
                                        </div>
                                    </label>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div style="color: var(--text-muted); font-size: 14px; margin-bottom: 12px;">
                                    Bạn chưa có địa chỉ nhận hàng nào được lưu. Vui lòng nhập địa chỉ bên dưới:
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <!-- Tùy chọn nhập địa chỉ mới -->
                        <button type="button" class="new-address-toggle" onclick="toggleNewAddressForm()">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="8" x2="12" y2="16"></line>
                                <line x1="8" y1="12" x2="16" y2="12"></line>
                            </svg>
                            Giao đến địa chỉ khác / Thêm địa chỉ mới
                        </button>

                        <div id="newAddressForm" class="new-address-form" style="${empty addresses ? 'display:block;' : 'display:none;'}">
                            <input type="radio" name="addressId" id="radioNewAddress" value="new" style="display:none;" ${empty addresses ? 'checked' : ''}>
                            <div class="form-row">
                                <div class="form-group">
                                    <label>Họ và tên người nhận (*)</label>
                                    <input type="text" name="newRecipientName" class="form-control" placeholder="Ví dụ: Nguyễn Văn A" value="${sessionScope.user.fullName}">
                                </div>
                                <div class="form-group">
                                    <label>Số điện thoại (*)</label>
                                    <input type="tel" name="newRecipientPhone" class="form-control" placeholder="Ví dụ: 0987654321" value="${sessionScope.user.phone}">
                                </div>
                            </div>
                            <div class="form-group">
                                <label>Địa chỉ chi tiết (Số nhà, tên đường) (*)</label>
                                <input type="text" name="newAddressLine" class="form-control" placeholder="Ví dụ: Số 25 Ngõ 12 Đường Cầu Giấy">
                            </div>
                            <div class="form-row">
                                <div class="form-group">
                                    <label>Tỉnh / Thành phố (*)</label>
                                    <input type="text" name="newProvince" class="form-control" placeholder="Hà Nội / TP. HCM" value="Hà Nội">
                                </div>
                                <div class="form-group">
                                    <label>Quận / Huyện (*)</label>
                                    <input type="text" name="newDistrict" class="form-control" placeholder="Quận Cầu Giấy">
                                </div>
                            </div>
                            <div class="form-group">
                                <label>Phường / Xã</label>
                                <input type="text" name="newWard" class="form-control" placeholder="Phường Dịch Vọng">
                            </div>
                        </div>
                    </div>

                    <!-- BƯỚC 2: PHƯƠNG THỨC GIAO HÀNG (TASK 2) -->
                    <div class="checkout-card">
                        <div class="checkout-card-header">
                            <span class="step-number">2</span>
                            <h2 class="checkout-card-title">Phương Thức Giao Hàng (Select Delivery Method)</h2>
                        </div>

                        <label class="option-box ${deliveryMethod == 'standard' ? 'selected' : ''}" onclick="selectDeliveryMethod(this, 'standard')">
                            <div class="option-left">
                                <input type="radio" name="deliveryMethod" value="standard" class="address-radio"
                                       ${deliveryMethod == 'standard' ? 'checked' : ''} onchange="updateDeliveryFee('standard')">
                                <div>
                                    <div class="option-title">Giao Hàng Tiêu Chuẩn (Standard Delivery)</div>
                                    <div class="option-desc">Giao trong ngày hoặc sáng mai. Miễn phí vận chuyển cho đơn từ 500.000 ₫</div>
                                </div>
                            </div>
                            <div class="option-price">
                                <c:choose>
                                    <c:when test="${summary.baseShippingFee == 0}">
                                        <span style="color: var(--primary);">Miễn phí</span>
                                    </c:when>
                                    <c:otherwise>25.000 ₫</c:otherwise>
                                </c:choose>
                            </div>
                        </label>

                        <label class="option-box ${deliveryMethod == 'express' ? 'selected' : ''}" onclick="selectDeliveryMethod(this, 'express')">
                            <div class="option-left">
                                <input type="radio" name="deliveryMethod" value="express" class="address-radio"
                                       ${deliveryMethod == 'express' ? 'checked' : ''} onchange="updateDeliveryFee('express')">
                                <div>
                                    <div class="option-title">Giao Hỏa Tốc 2 Giờ (Express 2H)</div>
                                    <div class="option-desc">Nhận hoa quả tươi ướp lạnh tận tay trong vòng 120 phút</div>
                                </div>
                            </div>
                            <div class="option-price">
                                <c:choose>
                                    <c:when test="${summary.baseShippingFee == 0}">
                                        30.000 ₫
                                    </c:when>
                                    <c:otherwise>55.000 ₫</c:otherwise>
                                </c:choose>
                            </div>
                        </label>
                    </div>

                    <!-- BƯỚC 3: PHƯƠNG THỨC THANH TOÁN (TASK 3) -->
                    <div class="checkout-card">
                        <div class="checkout-card-header">
                            <span class="step-number">3</span>
                            <h2 class="checkout-card-title">Phương Thức Thanh Toán (Select Payment Method)</h2>
                        </div>

                        <label class="option-box selected" onclick="selectPaymentMethod(this)">
                            <div class="option-left">
                                <input type="radio" name="paymentMethod" value="cod" class="address-radio" checked>
                                <div>
                                    <div class="option-title">Thanh Toán Khi Nhận Hàng (COD)</div>
                                    <div class="option-desc">Kiểm tra hoa quả tươi ngon trước khi thanh toán tiền mặt cho shipper</div>
                                </div>
                            </div>
                            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                                <line x1="12" y1="8" x2="12" y2="16"></line>
                                <line x1="8" y1="12" x2="16" y2="12"></line>
                            </svg>
                        </label>

                        <label class="option-box" onclick="selectPaymentMethod(this)">
                            <div class="option-left">
                                <input type="radio" name="paymentMethod" value="vnpay" class="address-radio">
                                <div>
                                    <div class="option-title">Thanh Toán Trực Tuyến VNPay / Thẻ ATM / QR Code</div>
                                    <div class="option-desc">Quét mã VietQR hoặc thanh toán qua cổng điện tử bảo mật</div>
                                </div>
                            </div>
                            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                                <line x1="1" y1="10" x2="23" y2="10"></line>
                            </svg>
                        </label>
                    </div>

                    <!-- GHI CHÚ GIAO HÀNG -->
                    <div class="checkout-card">
                        <div class="form-group" style="margin-bottom:0;">
                            <label>Ghi chú cho Shipper / Shop (Tùy chọn)</label>
                            <input type="text" name="orderNote" class="form-control" placeholder="Ví dụ: Giao giờ hành chính, gọi trước khi đến 15 phút...">
                        </div>
                    </div>

                </div>

                <!-- CỘT PHẢI: XEM LẠI ĐƠN HÀNG, ÁP VOUCHER, NÚT ĐẶT HÀNG (TASKS 4, 5, 6) -->
                <div class="checkout-col-right">

                    <div class="checkout-card">
                        <div class="checkout-card-header">
                            <h2 class="checkout-card-title">Xem Lại Đơn Hàng (${summary.totalItems} SP)</h2>
                            <a href="${pageContext.request.contextPath}/cart" style="margin-left:auto; font-size:13px; color:var(--primary); text-decoration:none;">Sửa</a>
                        </div>

                        <!-- Danh sách mặt hàng (Task 4: Review Order) -->
                        <div class="order-items-list">
                            <c:forEach var="item" items="${cartItems}">
                                <div class="order-item-row">
                                    <img src="${not empty item.imageUrl ? item.imageUrl : pageContext.request.contextPath.concat('/images/product-placeholder.png')}"
                                         alt="${item.productName}" class="order-item-img"
                                         onerror="this.src='${pageContext.request.contextPath}/images/product-placeholder.png'">
                                    <div class="order-item-info">
                                        <div class="order-item-name">${item.productName}</div>
                                        <div class="order-item-meta">
                                            Quy cách: ${item.packagingType} | SL: x${item.quantity}
                                        </div>
                                    </div>
                                    <div class="order-item-total">
                                        <fmt:formatNumber value="${item.itemTotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <!-- Áp dụng mã giảm giá (Task 5: Apply Promotion) -->
                        <div class="voucher-section">
                            <label style="display:block; font-size:13px; font-weight:600; margin-bottom:6px;">Mã Giảm Giá / Voucher (Apply Promotion)</label>
                            <div class="voucher-input-group">
                                <input type="text" id="voucherInput" name="voucherCode" class="form-control"
                                       placeholder="Nhập mã voucher (VD: FRUIT10)" value="${param.voucherCode}">
                                <button type="button" class="btn-apply-voucher" onclick="applyVoucherAJAX()">Áp Dụng</button>
                            </div>
                            <div id="voucherStatusMsg" style="font-size:12px; margin-top:6px; color:var(--primary-dark); font-weight:500;">
                                ${summary.voucherMessage}
                            </div>

                            <c:if test="${not empty activeVouchers}">
                                <div style="font-size:11px; color:var(--text-muted); margin-top:8px;">Gợi ý mã khuyến mãi dành cho bạn:</div>
                                <div class="voucher-tags">
                                    <c:forEach var="v" items="${activeVouchers}">
                                        <span class="voucher-tag" onclick="quickApplyVoucher('${v.code}')">
                                            ${v.code}
                                            <c:choose>
                                                <c:when test="${v.discountType == 'percent'}">(-${v.discountValue}%)</c:when>
                                                <c:otherwise>(-<fmt:formatNumber value="${v.discountValue}" type="number" maxFractionDigits="0"/>₫)</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </div>

                        <!-- Tóm tắt chi phí thanh toán -->
                        <div class="summary-breakdown">
                            <div class="summary-row">
                                <span>Tạm tính tiền hàng:</span>
                                <span id="displaySubtotal">
                                    <fmt:formatNumber value="${summary.subtotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </span>
                            </div>
                            <div class="summary-row">
                                <span>Phí vận chuyển:</span>
                                <span id="displayShipping">
                                    <c:choose>
                                        <c:when test="${summary.shippingFee == 0}">Miễn phí</c:when>
                                        <c:otherwise><fmt:formatNumber value="${summary.shippingFee}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="summary-row" id="rowDiscount" style="${summary.discount > 0 ? '' : 'display:none;'}">
                                <span style="color: #dc2626;">Giảm giá Voucher:</span>
                                <span style="color: #dc2626; font-weight:600;" id="displayDiscount">
                                    -<fmt:formatNumber value="${summary.discount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </span>
                            </div>
                            <div class="summary-row total">
                                <span>Tổng cộng:</span>
                                <span class="total-highlight" id="displayTotal">
                                    <fmt:formatNumber value="${summary.totalAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </span>
                            </div>
                        </div>

                        <!-- Nút Đặt Hàng (Task 6: Confirm & Place Order) -->
                        <button type="submit" class="btn-place-order" id="btnPlaceOrder">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                                <line x1="3" y1="6" x2="21" y2="6"></line>
                                <path d="M16 10a4 4 0 0 1-8 0"></path>
                            </svg>
                            Xác Nhận Đặt Hàng (Place Order)
                        </button>

                        <div style="font-size:12px; color:var(--text-muted); text-align:center; margin-top:12px;">
                            Bằng cách đặt hàng, bạn đồng ý với Điều khoản dịch vụ và Chính sách hoa quả tươi sạch của FreshFruit Store.
                        </div>
                    </div>

                </div>
            </div>
        </form>
    </div>

    <script>
        function selectAddress(element) {
            document.querySelectorAll('.address-item').forEach(el => el.classList.remove('selected'));
            element.classList.add('selected');
            const radio = element.querySelector('.address-radio');
            if (radio) radio.checked = true;
            document.getElementById('newAddressForm').style.display = 'none';
        }

        function onAddressChanged(radio) {
            document.getElementById('newAddressForm').style.display = 'none';
        }

        function toggleNewAddressForm() {
            const form = document.getElementById('newAddressForm');
            const radioNew = document.getElementById('radioNewAddress');
            if (form.style.display === 'none' || form.style.display === '') {
                form.style.display = 'block';
                radioNew.checked = true;
                document.querySelectorAll('.address-item').forEach(el => el.classList.remove('selected'));
            } else {
                form.style.display = 'none';
            }
        }

        function selectDeliveryMethod(element, method) {
            document.querySelectorAll('label[onclick*="selectDeliveryMethod"]').forEach(el => el.classList.remove('selected'));
            element.classList.add('selected');
            const radio = element.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
            updateDeliveryFee(method);
        }

        function selectPaymentMethod(element) {
            document.querySelectorAll('label[onclick*="selectPaymentMethod"]').forEach(el => el.classList.remove('selected'));
            element.classList.add('selected');
            const radio = element.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        }

        function updateDeliveryFee(method) {
            applyVoucherAJAX();
        }

        function quickApplyVoucher(code) {
            document.getElementById('voucherInput').value = code;
            applyVoucherAJAX();
        }

        function applyVoucherAJAX() {
            const code = document.getElementById('voucherInput').value.trim();
            const methodRadio = document.querySelector('input[name="deliveryMethod"]:checked');
            const deliveryMethod = methodRadio ? methodRadio.value : 'standard';

            const url = '${pageContext.request.contextPath}/checkout/apply-voucher';
            const params = new URLSearchParams();
            params.append('voucherCode', code);
            params.append('deliveryMethod', deliveryMethod);

            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: params.toString()
            })
            .then(res => res.json())
            .then(data => {
                const msgEl = document.getElementById('voucherStatusMsg');
                msgEl.innerText = data.message;
                msgEl.style.color = data.success ? 'var(--primary-dark)' : 'var(--danger)';

                // Update UI numbers
                if (data.shippingFee === 0) {
                    document.getElementById('displayShipping').innerText = 'Miễn phí';
                } else {
                    document.getElementById('displayShipping').innerText = new Intl.NumberFormat('vi-VN').format(data.shippingFee) + ' ₫';
                }

                if (data.discount > 0) {
                    document.getElementById('rowDiscount').style.display = 'flex';
                    document.getElementById('displayDiscount').innerText = '-' + new Intl.NumberFormat('vi-VN').format(data.discount) + ' ₫';
                } else {
                    document.getElementById('rowDiscount').style.display = 'none';
                }

                document.getElementById('displayTotal').innerText = new Intl.NumberFormat('vi-VN').format(data.totalAmount) + ' ₫';
            })
            .catch(err => {
                console.error("Voucher calculation error:", err);
            });
        }
    </script>
    <!-- Footer (Reusable Component) -->
    <jsp:include page="/includes/footer.jsp" />

</body>
</html>
