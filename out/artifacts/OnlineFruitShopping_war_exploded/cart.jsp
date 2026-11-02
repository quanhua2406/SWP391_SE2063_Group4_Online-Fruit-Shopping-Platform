<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<fmt:setLocale value="vi_VN"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ Hàng Mua Sắm | FreshFruit</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">

    <style>
        .cart-page-wrapper {
            background-color: #f8fafc;
            min-height: calc(100vh - 180px);
            padding: 30px 0 70px;
        }

        .cart-container {
            max-width: 1240px;
            margin: 0 auto;
            padding: 0 20px;
        }

        .cart-breadcrumb {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #64748b;
            margin-bottom: 24px;
        }

        .cart-breadcrumb a {
            color: #475569;
            text-decoration: none;
            transition: color 0.2s;
        }

        .cart-breadcrumb a:hover {
            color: #16a34a;
        }

        .cart-title-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 26px;
        }

        .cart-title-row h1 {
            font-size: 26px;
            font-weight: 800;
            color: #0f172a;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .cart-title-badge {
            font-size: 14px;
            font-weight: 600;
            color: #16a34a;
            background: #dcfce7;
            padding: 4px 12px;
            border-radius: 9999px;
        }

        /* 2-Column Cart Layout */
        .cart-layout {
            display: grid;
            grid-template-columns: 1fr 380px;
            gap: 28px;
            align-items: start;
        }

        @media (max-width: 992px) {
            .cart-layout {
                grid-template-columns: 1fr;
            }
        }

        /* Warning Alert Box */
        .cart-alert-box {
            background: #fffbeb;
            border: 1px solid #fef3c7;
            border-left: 4px solid #f59e0b;
            padding: 14px 18px;
            border-radius: 10px;
            margin-bottom: 20px;
            color: #92400e;
            font-size: 14px;
            line-height: 1.5;
        }

        .cart-alert-box ul {
            margin: 6px 0 0 18px;
            padding: 0;
        }

        /* Cart Items Card */
        .cart-items-card {
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
            overflow: hidden;
        }

        .cart-table {
            width: 100%;
            border-collapse: collapse;
        }

        .cart-table th {
            text-align: left;
            padding: 16px 20px;
            background: #f8fafc;
            color: #475569;
            font-size: 13px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-bottom: 1px solid #e2e8f0;
        }

        .cart-table td {
            padding: 20px;
            border-bottom: 1px solid #f1f5f9;
            vertical-align: middle;
        }

        .cart-table tr:last-child td {
            border-bottom: none;
        }

        .cart-product-cell {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .cart-thumb-wrap {
            width: 80px;
            height: 80px;
            border-radius: 12px;
            background: #f1f5f9;
            overflow: hidden;
            flex-shrink: 0;
            border: 1px solid #e2e8f0;
        }

        .cart-thumb-wrap img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.25s ease;
        }

        .cart-thumb-wrap:hover img {
            transform: scale(1.06);
        }

        .cart-item-title {
            font-size: 15px;
            font-weight: 700;
            color: #1e293b;
            text-decoration: none;
            display: block;
            margin-bottom: 4px;
            transition: color 0.2s;
        }

        .cart-item-title:hover {
            color: #16a34a;
        }

        .cart-item-meta {
            font-size: 13px;
            color: #64748b;
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
        }

        .meta-pill {
            background: #f1f5f9;
            color: #475569;
            font-weight: 600;
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 6px;
        }

        .stock-warning-tag {
            background: #fee2e2;
            color: #b91c1c;
            font-size: 11.5px;
            font-weight: 700;
            padding: 2px 8px;
            border-radius: 6px;
        }

        .cart-price-cell {
            font-size: 15px;
            font-weight: 700;
            color: #334155;
            white-space: nowrap;
        }

        .cart-total-cell {
            font-size: 16px;
            font-weight: 800;
            color: #16a34a;
            white-space: nowrap;
        }

        /* Quantity controls */
        .cart-qty-control {
            display: inline-flex;
            align-items: center;
            background: #f8fafc;
            border: 1.5px solid #cbd5e1;
            border-radius: 10px;
            overflow: hidden;
        }

        .cart-qty-btn {
            background: transparent;
            border: none;
            width: 32px;
            height: 34px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            font-size: 16px;
            font-weight: 700;
            color: #334155;
            transition: background 0.15s;
        }

        .cart-qty-btn:hover {
            background: #e2e8f0;
        }

        .cart-qty-input {
            width: 44px;
            height: 34px;
            border: none;
            background: transparent;
            text-align: center;
            font-size: 14px;
            font-weight: 700;
            color: #0f172a;
            outline: none;
            -moz-appearance: textfield;
        }

        .cart-qty-input::-webkit-outer-spin-button,
        .cart-qty-input::-webkit-inner-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }

        .btn-remove-item {
            background: transparent;
            border: none;
            color: #94a3b8;
            cursor: pointer;
            padding: 6px;
            border-radius: 8px;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }

        .btn-remove-item:hover {
            color: #ef4444;
            background: #fee2e2;
        }

        /* Cart Footer Navigation */
        .cart-actions-bar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 18px 24px;
            background: #ffffff;
            border-radius: 14px;
            border: 1px solid #e2e8f0;
            margin-top: 20px;
            flex-wrap: wrap;
            gap: 14px;
        }

        .btn-continue-shopping {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: #16a34a;
            font-size: 14px;
            font-weight: 700;
            text-decoration: none;
            padding: 10px 18px;
            border-radius: 10px;
            border: 1.5px solid #16a34a;
            transition: all 0.2s;
        }

        .btn-continue-shopping:hover {
            background: #f0fdf4;
            transform: translateX(-2px);
        }

        .btn-clear-cart {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            color: #64748b;
            font-size: 13.5px;
            font-weight: 600;
            background: transparent;
            border: 1px solid #cbd5e1;
            padding: 9px 16px;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .btn-clear-cart:hover {
            color: #b91c1c;
            border-color: #fca5a5;
            background: #fef2f2;
        }

        /* Order Summary Card (Sticky) */
        .summary-card {
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
            padding: 24px;
            position: sticky;
            top: 20px;
        }

        .summary-title {
            font-size: 18px;
            font-weight: 800;
            color: #0f172a;
            margin: 0 0 18px;
            padding-bottom: 14px;
            border-bottom: 1px solid #f1f5f9;
        }

        /* Free Shipping Progress bar */
        .free-shipping-box {
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            border-radius: 12px;
            padding: 12px 14px;
            margin-bottom: 20px;
            font-size: 13px;
        }

        .free-shipping-text {
            color: #15803d;
            font-weight: 600;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .progress-bar-bg {
            height: 7px;
            background: #e2e8f0;
            border-radius: 9999px;
            overflow: hidden;
        }

        .progress-bar-fill {
            height: 100%;
            background: #22c55e;
            border-radius: 9999px;
            transition: width 0.3s ease;
        }

        .summary-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 14px;
            font-size: 14px;
            color: #475569;
        }

        .summary-row strong {
            color: #0f172a;
            font-weight: 700;
        }

        .summary-divider {
            height: 1px;
            background: #f1f5f9;
            margin: 16px 0;
        }

        .summary-total-row {
            display: flex;
            align-items: baseline;
            justify-content: space-between;
            margin-bottom: 22px;
        }

        .summary-total-row span:first-child {
            font-size: 16px;
            font-weight: 800;
            color: #0f172a;
        }

        .summary-total-price {
            font-size: 24px;
            font-weight: 800;
            color: #16a34a;
        }

        .btn-checkout {
            display: block;
            width: 100%;
            background: #16a34a;
            color: #ffffff;
            font-size: 15px;
            font-weight: 700;
            text-align: center;
            padding: 14px 20px;
            border-radius: 12px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            box-shadow: 0 4px 14px rgba(22, 163, 74, 0.25);
            transition: all 0.25s ease;
            box-sizing: border-box;
        }

        .btn-checkout:hover {
            background: #15803d;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(22, 163, 74, 0.35);
        }

        /* Safe Guarantees */
        .trust-badges {
            margin-top: 22px;
            padding-top: 18px;
            border-top: 1px dashed #e2e8f0;
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .trust-item {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 12.5px;
            color: #64748b;
            font-weight: 600;
        }

        .trust-item svg {
            color: #16a34a;
            flex-shrink: 0;
        }

        /* Empty Cart State */
        .empty-cart-card {
            background: #ffffff;
            border-radius: 20px;
            border: 1px solid #e2e8f0;
            padding: 60px 24px;
            text-align: center;
            max-width: 600px;
            margin: 40px auto;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
        }

        .empty-cart-icon {
            width: 88px;
            height: 88px;
            background: #f0fdf4;
            color: #16a34a;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
        }

        .empty-cart-card h2 {
            font-size: 22px;
            font-weight: 800;
            color: #0f172a;
            margin: 0 0 10px;
        }

        .empty-cart-card p {
            font-size: 14.5px;
            color: #64748b;
            margin: 0 auto 28px;
            max-width: 420px;
            line-height: 1.5;
        }

        .btn-explore-fruits {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: #16a34a;
            color: #ffffff;
            font-size: 15px;
            font-weight: 700;
            padding: 13px 28px;
            border-radius: 9999px;
            text-decoration: none;
            box-shadow: 0 4px 14px rgba(22, 163, 74, 0.25);
            transition: all 0.25s ease;
        }

        .btn-explore-fruits:hover {
            background: #15803d;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(22, 163, 74, 0.35);
        }
    </style>
</head>

<body>

<!-- Header Navigation Bar (Reusable Component) -->
<jsp:include page="/includes/header.jsp" />

<div class="cart-page-wrapper">
    <div class="cart-container">

        <!-- Breadcrumb Navigation -->
        <div class="cart-breadcrumb">
            <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
            <span>/</span>
            <a href="${pageContext.request.contextPath}/products">Khám phá trái cây</a>
            <span>/</span>
            <span style="color: #0f172a; font-weight: 600;">Giỏ hàng của bạn</span>
        </div>

        <c:choose>
            <%-- KHI GIỎ HÀNG TRỐNG --%>
            <c:when test="${empty cartItems}">
                <div class="empty-cart-card">
                    <div class="empty-cart-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="9" cy="21" r="1"></circle>
                            <circle cx="20" cy="21" r="1"></circle>
                            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                        </svg>
                    </div>
                    <h2>Giỏ hàng của bạn đang trống</h2>
                    <p>Hãy dạo quanh cửa hàng và chọn cho mình cùng gia đình những loại trái cây tươi ngon, chuẩn vị tự nhiên nhé!</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn-explore-fruits">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="19" y1="12" x2="5" y2="12"></line>
                            <polyline points="12 19 5 12 12 5"></polyline>
                        </svg>
                        Khám Phá Trái Cây Tươi Ngay
                    </a>
                </div>
            </c:when>

            <%-- KHI GIỎ HÀNG CÓ SẢN PHẨM --%>
            <c:otherwise>
                <div class="cart-title-row">
                    <h1>
                        Giỏ Hàng Mua Sắm
                        <span class="cart-title-badge">${fn:length(cartItems)} loại sản phẩm</span>
                    </h1>
                </div>

                <%-- Hiển thị cảnh báo nếu có --%>
                <c:if test="${not empty cartWarnings}">
                    <div class="cart-alert-box">
                        <strong>Lưu ý về sản phẩm trong giỏ hàng:</strong>
                        <ul>
                            <c:forEach var="w" items="${cartWarnings}">
                                <li>${w}</li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>

                <div class="cart-layout">

                    <!-- CỘT BÊN TRÁI: DANH SÁCH MẶT HÀNG TRONG GIỎ -->
                    <div>
                        <div class="cart-items-card">
                            <table class="cart-table">
                                <thead>
                                    <tr>
                                        <th>Sản phẩm</th>
                                        <th>Đơn giá</th>
                                        <th style="text-align: center;">Số lượng</th>
                                        <th style="text-align: right;">Thành tiền</th>
                                        <th style="width: 40px;"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="item" items="${cartItems}">
                                        <tr id="cartRow_${item.variantId}">
                                            <td>
                                                <div class="cart-product-cell">
                                                    <div class="cart-thumb-wrap">
                                                        <img src="${not empty item.imageUrl ? item.imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}"
                                                             alt="${item.productName}"
                                                             onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                                                    </div>
                                                    <div>
                                                        <a href="${pageContext.request.contextPath}/product?id=${item.productId}" class="cart-item-title">
                                                            ${item.productName}
                                                        </a>
                                                        <div class="cart-item-meta">
                                                            <span class="meta-pill">${item.packagingType}</span>
                                                            <span>•</span>
                                                            <span>Mã: ${item.sku}</span>
                                                            <c:if test="${item.stockQuantity <= 5}">
                                                                <span class="stock-warning-tag">Chỉ còn ${item.stockQuantity} có sẵn</span>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="cart-price-cell">
                                                <fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                            </td>
                                            <td style="text-align: center;">
                                                <div class="cart-qty-control">
                                                    <button type="button" class="cart-qty-btn" onclick="changeQuantity(${item.variantId}, -1)">-</button>
                                                    <input type="number" 
                                                           id="qtyInput_${item.variantId}" 
                                                           class="cart-qty-input" 
                                                           value="${item.quantity}" 
                                                           min="1" 
                                                           max="${item.stockQuantity}"
                                                           onchange="onQtyInputChange(${item.variantId})">
                                                    <button type="button" class="cart-qty-btn" onclick="changeQuantity(${item.variantId}, 1)">+</button>
                                                </div>
                                            </td>
                                            <td class="cart-total-cell" style="text-align: right;" id="lineTotal_${item.variantId}">
                                                <fmt:formatNumber value="${item.itemTotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                            </td>
                                            <td style="text-align: center;">
                                                <form action="${pageContext.request.contextPath}/cart" method="post" style="margin: 0;" onsubmit="return confirm('Bạn có chắc muốn bỏ sản phẩm này khỏi giỏ hàng?');">
                                                    <input type="hidden" name="action" value="remove">
                                                    <input type="hidden" name="variantId" value="${item.variantId}">
                                                    <button type="submit" class="btn-remove-item" title="Xóa khỏi giỏ hàng">
                                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                            <polyline points="3 6 5 6 21 6"></polyline>
                                                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                                            <line x1="10" y1="11" x2="10" y2="17"></line>
                                                            <line x1="14" y1="11" x2="14" y2="17"></line>
                                                        </svg>
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <!-- Thanh điều hướng chân giỏ hàng -->
                        <div class="cart-actions-bar">
                            <a href="${pageContext.request.contextPath}/products" class="btn-continue-shopping">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="15 18 9 12 15 6"></polyline>
                                </svg>
                                Tiếp Tục Chọn Trái Cây
                            </a>

                            <form action="${pageContext.request.contextPath}/cart" method="post" style="margin: 0;" onsubmit="return confirm('Bạn có chắc muốn xóa toàn bộ sản phẩm trong giỏ hàng?');">
                                <input type="hidden" name="action" value="clear">
                                <button type="submit" class="btn-clear-cart">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <polyline points="3 6 5 6 21 6"></polyline>
                                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                    </svg>
                                    Xóa Toàn Bộ Giỏ Hàng
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- CỘT BÊN PHẢI: TỔNG KẾT ĐƠN HÀNG (SUMMARY CARD) -->
                    <div>
                        <div class="summary-card">
                            <h2 class="summary-title">Tóm Tắt Đơn Hàng</h2>

                            <!-- Ưu đãi Miễn Phí Vận Chuyển -->
                            <div class="free-shipping-box">
                                <div class="free-shipping-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <rect x="1" y="3" width="15" height="13"></rect>
                                        <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"></polygon>
                                        <circle cx="5.5" cy="18.5" r="2.5"></circle>
                                        <circle cx="18.5" cy="18.5" r="2.5"></circle>
                                    </svg>
                                    <c:choose>
                                        <c:when test="${freeShippingRemaining <= 0}">
                                            <span>Đơn hàng của bạn đã đủ điều kiện <strong>Miễn phí vận chuyển</strong>!</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span>Mua thêm <strong><fmt:formatNumber value="${freeShippingRemaining}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong> để được Free Ship</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="progress-bar-bg">
                                    <c:set var="percent" value="${(subtotal / freeShippingThreshold) * 100}"/>
                                    <div class="progress-bar-fill" style="width: ${percent > 100 ? 100 : percent}%;"></div>
                                </div>
                            </div>

                            <div class="summary-row">
                                <span>Tạm tính hàng:</span>
                                <strong id="summarySubtotal">
                                    <fmt:formatNumber value="${subtotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </strong>
                            </div>

                            <div class="summary-row">
                                <span>Phí giao hàng dự kiến:</span>
                                <span id="summaryShipping">
                                    <c:choose>
                                        <c:when test="${shippingFee == 0}">
                                            <span style="color: #16a34a; font-weight: 700;">Miễn phí</span>
                                        </c:when>
                                        <c:otherwise>
                                            <strong><fmt:formatNumber value="${shippingFee}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</strong>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>

                            <div class="summary-divider"></div>

                            <div class="summary-total-row">
                                <span>Tổng thanh toán:</span>
                                <span class="summary-total-price" id="summaryGrandTotal">
                                    <fmt:formatNumber value="${grandTotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </span>
                            </div>

                            <c:choose>
                                <c:when test="${empty sessionScope.user}">
                                    <a href="${pageContext.request.contextPath}/login?redirect=/checkout" class="btn-checkout" style="display:block; text-align:center; text-decoration:none;">
                                        Đăng Nhập Để Thanh Toán
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/checkout" class="btn-checkout" style="display:block; text-align:center; text-decoration:none;">
                                        Tiến Hành Đặt Hàng
                                    </a>
                                </c:otherwise>
                            </c:choose>

                            <!-- Trust perks -->
                            <div class="trust-badges">
                                <div class="trust-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                                    </svg>
                                    <span>Cam kết 100% trái cây tươi sạch loại 1</span>
                                </div>
                                <div class="trust-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <polyline points="12 6 12 12 16 14"></polyline>
                                    </svg>
                                    <span>Giao hàng siêu tốc trong 2 giờ tại nội thành</span>
                                </div>
                                <div class="trust-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <polyline points="23 4 23 10 17 10"></polyline>
                                        <polyline points="1 20 1 14 7 14"></polyline>
                                        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                                    </svg>
                                    <span>Đổi trả miễn phí trong 24 giờ nếu dập nát</span>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>

<script>
    function formatCurrency(num) {
        return new Intl.NumberFormat('vi-VN').format(num) + ' ₫';
    }

    function changeQuantity(variantId, delta) {
        const input = document.getElementById('qtyInput_' + variantId);
        if (!input) return;
        let currentVal = parseInt(input.value) || 1;
        let maxVal = parseInt(input.getAttribute('max')) || 9999;
        let newVal = currentVal + delta;

        if (newVal < 1) {
            if (confirm('Bạn có muốn xóa sản phẩm này khỏi giỏ hàng không?')) {
                newVal = 0;
            } else {
                return;
            }
        } else if (newVal > maxVal) {
            alert('Số lượng tối đa có sẵn trong kho là ' + maxVal + ' sản phẩm.');
            newVal = maxVal;
        }

        input.value = newVal;
        sendUpdateQuantity(variantId, newVal);
    }

    function onQtyInputChange(variantId) {
        const input = document.getElementById('qtyInput_' + variantId);
        if (!input) return;
        let val = parseInt(input.value);
        let maxVal = parseInt(input.getAttribute('max')) || 9999;

        if (isNaN(val) || val < 1) {
            val = 1;
            input.value = 1;
        } else if (val > maxVal) {
            alert('Chỉ còn tối đa ' + maxVal + ' sản phẩm trong kho.');
            val = maxVal;
            input.value = maxVal;
        }

        sendUpdateQuantity(variantId, val);
    }

    function sendUpdateQuantity(variantId, qty) {
        const formData = new URLSearchParams();
        formData.append('action', 'update');
        formData.append('variantId', variantId);
        formData.append('quantity', qty);

        fetch('${pageContext.request.contextPath}/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'application/json'
            },
            body: formData.toString()
        })
        .then(res => res.json())
        .then(data => {
            if (qty <= 0) {
                // Remove row if quantity was zero
                const row = document.getElementById('cartRow_' + variantId);
                if (row) row.remove();
                // If cart became empty, reload to show empty state
                if (data.cartCount === 0) {
                    window.location.reload();
                    return;
                }
            } else if (data.adjustedQuantity && data.adjustedQuantity !== qty) {
                const input = document.getElementById('qtyInput_' + variantId);
                if (input) input.value = data.adjustedQuantity;
                if (data.message) alert(data.message);
            }

            // Update line total
            const lineTotalEl = document.getElementById('lineTotal_' + variantId);
            if (lineTotalEl && data.adjustedQuantity) {
                // Calculate line total from price and quantity
                const row = document.getElementById('cartRow_' + variantId);
                if (row) {
                    const priceText = row.querySelector('.cart-price-cell').textContent.replace(/[^\d]/g, '');
                    const unitPrice = parseFloat(priceText) || 0;
                    lineTotalEl.textContent = formatCurrency(unitPrice * data.adjustedQuantity);
                }
            }

            // Update subtotal & grand total
            if (data.subtotal !== undefined) {
                const subtotalEl = document.getElementById('summarySubtotal');
                if (subtotalEl) subtotalEl.textContent = formatCurrency(data.subtotal);
            }
            if (data.shippingFee !== undefined) {
                const shippingEl = document.getElementById('summaryShipping');
                if (shippingEl) {
                    if (data.shippingFee === 0) {
                        shippingEl.innerHTML = '<span style="color: #16a34a; font-weight: 700;">Miễn phí</span>';
                    } else {
                        shippingEl.innerHTML = '<strong>' + formatCurrency(data.shippingFee) + '</strong>';
                    }
                }
            }
            if (data.grandTotal !== undefined) {
                const grandTotalEl = document.getElementById('summaryGrandTotal');
                if (grandTotalEl) grandTotalEl.textContent = formatCurrency(data.grandTotal);
            }
        })
        .catch(err => {
            console.error('Lỗi cập nhật giỏ hàng:', err);
            // Fallback reload if AJAX failed
            window.location.reload();
        });
    }
</script>

<!-- Footer (Reusable Component) -->
<jsp:include page="/includes/footer.jsp" />

</body>
</html>
