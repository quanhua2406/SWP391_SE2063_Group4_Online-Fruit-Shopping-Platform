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
    <title>${fn:escapeXml(product.productName)} | FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">
    <style>
        .detail-wrapper {
            width: 90%;
            max-width: 1280px;
            margin: 25px auto 60px;
        }

        .breadcrumb {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #64748b;
            margin-bottom: 24px;
        }
        .breadcrumb a {
            color: #16a34a;
            font-weight: 500;
        }
        .breadcrumb a:hover {
            text-decoration: underline;
        }

        .product-detail-card {
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 4px 20px rgba(0,0,0,0.04);
            padding: 36px;
            display: grid;
            grid-template-columns: 480px 1fr;
            gap: 48px;
            margin-bottom: 40px;
        }

        /* Gallery */
        .gallery-container {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }
        .main-image-frame {
            position: relative;
            width: 100%;
            height: 420px;
            border-radius: 12px;
            overflow: hidden;
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .main-image-frame img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.3s ease;
        }
        .main-image-frame:hover img {
            transform: scale(1.03);
        }
        .badge-origin-overlay {
            position: absolute;
            top: 14px;
            left: 14px;
            background: rgba(15, 23, 42, 0.8);
            backdrop-filter: blur(4px);
            color: #ffffff;
            font-size: 12px;
            font-weight: 600;
            padding: 6px 12px;
            border-radius: 8px;
        }

        .thumbnail-strip {
            display: flex;
            gap: 12px;
            overflow-x: auto;
            padding-bottom: 4px;
        }
        .thumb-item {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            overflow: hidden;
            border: 2px solid transparent;
            cursor: pointer;
            background: #f1f5f9;
            flex-shrink: 0;
            transition: all 0.2s;
        }
        .thumb-item img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .thumb-item:hover, .thumb-item.active {
            border-color: #16a34a;
        }

        /* Product Info */
        .product-meta-header {
            margin-bottom: 16px;
        }
        .product-category-tag {
            display: inline-block;
            background: #ecfdf5;
            color: #15803d;
            font-size: 12px;
            font-weight: 700;
            padding: 4px 10px;
            border-radius: 6px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }
        .product-main-title {
            font-size: 28px;
            font-weight: 800;
            color: #0f172a;
            line-height: 1.3;
            margin-bottom: 10px;
        }
        .product-shop-row {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 13.5px;
            color: #64748b;
        }
        .shop-name-badge {
            color: #0f172a;
            font-weight: 600;
        }

        /* Price Area */
        .price-box {
            background: #f8fafc;
            border-radius: 12px;
            padding: 16px 20px;
            margin: 20px 0;
            display: flex;
            align-items: baseline;
            gap: 12px;
        }
        .live-price {
            font-size: 32px;
            font-weight: 800;
            color: #15803d;
        }
        .live-unit {
            font-size: 14px;
            color: #64748b;
            font-weight: 500;
        }

        /* Variant Options */
        .variant-group-title {
            font-size: 14.5px;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .variant-options-list {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 24px;
        }
        .variant-btn {
            padding: 10px 18px;
            border-radius: 10px;
            border: 1.5px solid #cbd5e1;
            background: #ffffff;
            color: #334155;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 2px;
        }
        .variant-btn:hover {
            border-color: #16a34a;
            color: #16a34a;
            background: #f0fdf4;
        }
        .variant-btn.active {
            border-color: #16a34a;
            background: #ecfdf5;
            color: #15803d;
            box-shadow: 0 0 0 2px rgba(22,163,74,0.2);
        }
        .variant-btn-price {
            font-size: 12px;
            color: #64748b;
            font-weight: 500;
        }
        .variant-btn.active .variant-btn-price {
            color: #16a34a;
            font-weight: 600;
        }

        /* Stock & SKU row */
        .stock-sku-row {
            display: flex;
            align-items: center;
            gap: 20px;
            font-size: 13.5px;
            color: #64748b;
            margin-bottom: 24px;
        }
        .stock-badge-live {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            border-radius: 6px;
            font-weight: 700;
            font-size: 13px;
        }
        .stock-badge-live.in-stock {
            background: #ecfdf5;
            color: #065f46;
        }
        .stock-badge-live.out-stock {
            background: #fef2f2;
            color: #991b1b;
        }

        /* Quantity & Add to Cart */
        .purchase-actions-form {
            display: flex;
            flex-direction: column;
            gap: 20px;
        }
        .quantity-row {
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .quantity-selector {
            display: inline-flex;
            align-items: center;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            overflow: hidden;
            background: #ffffff;
        }
        .qty-btn {
            width: 38px;
            height: 38px;
            background: #f8fafc;
            border: none;
            font-size: 18px;
            font-weight: 700;
            color: #334155;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background 0.15s;
        }
        .qty-btn:hover {
            background: #e2e8f0;
        }
        .qty-input {
            width: 50px;
            height: 38px;
            border: none;
            border-left: 1px solid #cbd5e1;
            border-right: 1px solid #cbd5e1;
            text-align: center;
            font-size: 15px;
            font-weight: 700;
            color: #0f172a;
            outline: none;
        }

        .action-buttons-group {
            display: flex;
            gap: 14px;
        }
        .btn-add-cart {
            flex: 1;
            background: #ffffff;
            color: #16a34a;
            border: 2px solid #16a34a;
            padding: 13px 20px;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }
        .btn-add-cart:hover {
            background: #ecfdf5;
        }
        .btn-buy-now {
            flex: 1;
            background: #16a34a;
            color: #ffffff;
            border: 2px solid #16a34a;
            padding: 13px 20px;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }
        .btn-buy-now:hover {
            background: #15803d;
            border-color: #15803d;
        }

        /* Trust Perks Box */
        .trust-perks-box {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 14px;
            background: #f8fafc;
            border-radius: 12px;
            padding: 18px;
            margin-top: 24px;
        }
        .perk-point {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 13px;
            color: #334155;
            font-weight: 500;
        }
        .perk-point svg {
            color: #16a34a;
            flex-shrink: 0;
        }

        /* Tabs Section */
        .tabs-card {
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid #e2e8f0;
            overflow: hidden;
            margin-bottom: 48px;
        }
        .tabs-header {
            display: flex;
            border-bottom: 1px solid #e2e8f0;
            background: #f8fafc;
        }
        .tab-btn {
            padding: 16px 28px;
            font-size: 15px;
            font-weight: 700;
            color: #64748b;
            border: none;
            background: transparent;
            cursor: pointer;
            transition: all 0.2s;
            border-bottom: 3px solid transparent;
        }
        .tab-btn:hover {
            color: #16a34a;
        }
        .tab-btn.active {
            color: #16a34a;
            background: #ffffff;
            border-bottom-color: #16a34a;
        }
        .tab-content {
            padding: 28px 36px;
            display: none;
            color: #334155;
            font-size: 15px;
            line-height: 1.7;
        }
        .tab-content.active {
            display: block;
        }

        /* Related Products */
        .related-section {
            margin-top: 40px;
        }
        .section-headline {
            font-size: 22px;
            font-weight: 800;
            color: #0f172a;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .catalog-product-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
            gap: 20px;
        }
        .fruit-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
            position: relative;
        }
        .fruit-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 24px rgba(0,0,0,0.07);
            border-color: #cbd5e1;
        }
        .fruit-image-wrap {
            position: relative;
            width: 100%;
            height: 180px;
            background: #f8fafc;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .fruit-image-wrap img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            transition: transform 0.35s ease;
        }
        .fruit-card:hover .fruit-image-wrap img {
            transform: scale(1.05);
        }
        .fruit-badge-origin {
            position: absolute;
            top: 10px;
            left: 10px;
            background: rgba(15, 23, 42, 0.75);
            backdrop-filter: blur(4px);
            color: #ffffff;
            font-size: 11.5px;
            font-weight: 600;
            padding: 4px 9px;
            border-radius: 6px;
            max-width: 80%;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .fruit-body {
            padding: 16px;
            display: flex;
            flex-direction: column;
            flex: 1;
        }
        .fruit-category {
            font-size: 12px;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-weight: 600;
            margin-bottom: 4px;
        }
        .fruit-title {
            font-size: 16px;
            font-weight: 700;
            color: #0f172a;
            margin: 0 0 10px;
            line-height: 1.35;
        }
        .fruit-title a {
            color: inherit;
            text-decoration: none;
            transition: color 0.2s;
        }
        .fruit-title a:hover {
            color: #16a34a;
        }
        .fruit-footer {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding-top: 12px;
            border-top: 1px solid #f1f5f9;
            margin-top: auto;
        }
        .fruit-price {
            font-size: 17px;
            font-weight: 800;
            color: #15803d;
        }
        .btn-view-detail {
            background: #16a34a;
            color: #ffffff;
            border: none;
            padding: 7px 14px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
            text-decoration: none;
            transition: background 0.2s;
            display: inline-flex;
            align-items: center;
        }
        .btn-view-detail:hover {
            background: #15803d;
            color: #ffffff;
        }

        @media (max-width: 900px) {
            .product-detail-card {
                grid-template-columns: 1fr;
                padding: 24px;
            }
            .main-image-frame {
                height: 320px;
            }
            .trust-perks-box {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>

<!-- Header -->
<header class="navbar">
    <a href="${pageContext.request.contextPath}/home" class="logo">FreshFruit</a>

    <div class="search-box-wrapper" style="flex: 1; max-width: 520px; position: relative;">
        <form action="${pageContext.request.contextPath}/products" method="get" class="search-box">
            <input type="text" name="keyword" placeholder="Tìm kiếm trái cây..." autocomplete="off">
            <button type="submit" aria-label="Tìm kiếm">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
            </button>
        </form>
    </div>

    <div class="nav-actions">
        <a href="${pageContext.request.contextPath}/products" class="nav-link" style="color: #16a34a; font-weight: 700;">Khám Phá Trái Cây</a>

        <c:set var="userRole" value="${sessionScope.user.roleName != null ? fn:toUpperCase(sessionScope.user.roleName) : ''}" />
        <c:set var="dashboardUrl" value="" />
        <c:set var="dashboardName" value="" />
        <c:choose>
            <c:when test="${fn:contains(userRole, 'ADMIN')}">
                <c:set var="dashboardUrl" value="${pageContext.request.contextPath}/admin/dashboard" />
                <c:set var="dashboardName" value="Admin Dashboard" />
            </c:when>
            <c:when test="${fn:contains(userRole, 'SHOP') || fn:contains(userRole, 'STORE')}">
                <c:set var="dashboardUrl" value="${pageContext.request.contextPath}/store-owner/dashboard" />
                <c:set var="dashboardName" value="Shop Owner Dashboard" />
            </c:when>
            <c:when test="${fn:contains(userRole, 'DELIVERY')}">
                <c:set var="dashboardUrl" value="${pageContext.request.contextPath}/delivery/dashboard" />
                <c:set var="dashboardName" value="Delivery Dashboard" />
            </c:when>
            <c:when test="${fn:contains(userRole, 'CUSTOMER')}">
                <c:set var="dashboardUrl" value="${pageContext.request.contextPath}/customer/dashboard" />
                <c:set var="dashboardName" value="Customer Dashboard" />
            </c:when>
        </c:choose>

        <!-- Dropdown Gói Định Kỳ -->
        <div class="nav-dropdown">
            <a href="${pageContext.request.contextPath}/subscriptions" class="nav-dropdown-toggle">
                <span>Gói Định Kỳ</span>
                <svg class="chevron-icon" xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
            </a>
            <div class="nav-dropdown-menu">
                <a href="${pageContext.request.contextPath}/subscriptions" class="nav-dropdown-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                        <polyline points="2 17 12 22 22 17"></polyline>
                        <polyline points="2 12 12 17 22 12"></polyline>
                    </svg>
                    Tất cả các gói định kỳ
                </a>
                <c:if test="${not fn:contains(userRole, 'SHOP') and not empty sessionScope.user}">
                    <a href="${pageContext.request.contextPath}/my-subscriptions" class="nav-dropdown-item">
                        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                            <polyline points="22 4 12 14.01 9 11.01"></polyline>
                        </svg>
                        Gói đăng ký của tôi
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Giỏ hàng -->
        <a href="${pageContext.request.contextPath}/cart" title="Giỏ hàng" class="nav-link" style="display: inline-flex; align-items: center; gap: 6px;">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="9" cy="21" r="1"></circle>
                <circle cx="20" cy="21" r="1"></circle>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
            </svg>
            <span>Giỏ hàng</span>
            <span id="navCartBadge" style="background: #ef4444; color: #fff; font-size: 11px; font-weight: 700; padding: 1px 6px; border-radius: 9999px; ${sessionScope.cartCount > 0 ? '' : 'display:none;'}">
                ${sessionScope.cartCount}
            </span>
        </a>

        <!-- Dropdown Tài khoản Người dùng -->
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <div class="nav-dropdown">
                    <button type="button" class="nav-dropdown-toggle" style="border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 9999px; padding: 4px 12px 4px 6px;">
                        <span style="width: 26px; height: 26px; border-radius: 50%; background: #dcfce7; color: #16a34a; display: inline-flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px;">
                            ${fn:toUpperCase(fn:substring(sessionScope.user.fullName != null ? sessionScope.user.fullName : sessionScope.user.username, 0, 1))}
                        </span>
                        <span style="font-weight: 600; color: #1e293b; max-width: 130px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-size: 13.5px;">
                            ${sessionScope.user.fullName != null ? sessionScope.user.fullName : sessionScope.user.username}
                        </span>
                        <svg class="chevron-icon" xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="6 9 12 15 18 9"></polyline>
                        </svg>
                    </button>

                    <div class="nav-dropdown-menu" style="min-width: 230px;">
                        <div class="nav-dropdown-header">
                            <div class="nav-dropdown-user-name">${sessionScope.user.fullName != null ? sessionScope.user.fullName : sessionScope.user.username}</div>
                            <div class="nav-dropdown-user-role">${sessionScope.user.roleName != null ? sessionScope.user.roleName : 'Thành viên'}</div>
                        </div>

                        <!-- Dashboard theo vai trò -->
                        <c:if test="${not empty dashboardUrl}">
                            <a href="${dashboardUrl}" class="nav-dropdown-item">
                                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <rect x="3" y="3" width="7" height="7"></rect>
                                    <rect x="14" y="3" width="7" height="7"></rect>
                                    <rect x="14" y="14" width="7" height="7"></rect>
                                    <rect x="3" y="14" width="7" height="7"></rect>
                                </svg>
                                ${dashboardName}
                            </a>
                        </c:if>

                        <!-- Đơn hàng & Thanh toán -->
                        <a href="${pageContext.request.contextPath}/orders" class="nav-dropdown-item">
                            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                                <line x1="3" y1="6" x2="21" y2="6"></line>
                                <path d="M16 10a4 4 0 0 1-8 0"></path>
                            </svg>
                            Đơn hàng của tôi
                        </a>

                        <a href="${pageContext.request.contextPath}/payments" class="nav-dropdown-item">
                            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                                <line x1="12" y1="8" x2="12" y2="16"></line>
                                <line x1="8" y1="12" x2="16" y2="12"></line>
                            </svg>
                            Lịch sử thanh toán
                        </a>

                        <a href="${pageContext.request.contextPath}/refunds" class="nav-dropdown-item">
                            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="1 4 1 10 7 10"></polyline>
                                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
                            </svg>
                            Yêu cầu hoàn tiền
                        </a>

                        <div class="nav-dropdown-divider"></div>

                        <!-- Thông tin cá nhân & Địa chỉ -->
                        <a href="${pageContext.request.contextPath}/profile" class="nav-dropdown-item">
                            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            Thông tin tài khoản
                        </a>

                        <a href="${pageContext.request.contextPath}/addresses" class="nav-dropdown-item">
                            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                <circle cx="12" cy="10" r="3"></circle>
                            </svg>
                            Sổ địa chỉ nhận hàng
                        </a>

                        <div class="nav-dropdown-divider"></div>

                        <!-- Đăng xuất -->
                        <a href="${pageContext.request.contextPath}/logout" class="nav-dropdown-item logout-item">
                            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                                <polyline points="16 17 21 12 16 7"></polyline>
                                <line x1="21" y1="12" x2="9" y2="12"></line>
                            </svg>
                            Đăng xuất
                        </a>
                    </div>
                </div>
            </c:when>

            <c:otherwise>
                <!-- Khách vãng lai -->
                <a href="${pageContext.request.contextPath}/login" class="nav-link" style="font-weight: 600;">
                    Đăng nhập
                </a>
                <a href="${pageContext.request.contextPath}/register" class="nav-link" style="background: #16a34a; color: #fff; padding: 6px 14px; border-radius: 6px; font-weight: 600;">
                    Đăng ký
                </a>
            </c:otherwise>
        </c:choose>
    </div>
</header>

<main class="detail-wrapper">
    <!-- Breadcrumb -->
    <div class="breadcrumb">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/products">Khám phá trái cây</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/products?category=${product.categoryId}">${product.categoryName}</a>
        <span>/</span>
        <span>${product.productName}</span>
    </div>

    <!-- Product Main Card -->
    <div class="product-detail-card">
        <!-- Gallery Left -->
        <div class="gallery-container">
            <div class="main-image-frame">
                <img id="mainFruitImg" src="${product.imageUrl}" alt="${product.productName}"
                     onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=600';">
                
                <span class="badge-origin-overlay">
                    Xuất xứ: ${product.origin}
                </span>
            </div>

            <!-- Thumbnail Strip -->
            <div class="thumbnail-strip">
                <div class="thumb-item active" onclick="switchMainImage('${product.imageUrl}', this)">
                    <img src="${product.imageUrl}" alt="Main image"
                         onerror="this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=100';">
                </div>
                <c:forEach var="img" items="${images}">
                    <c:if test="${img.imageUrl != product.imageUrl}">
                        <div class="thumb-item" onclick="switchMainImage('${img.imageUrl}', this)">
                            <img src="${img.imageUrl}" alt="Gallery thumb"
                                 onerror="this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=100';">
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>

        <!-- Product Details Right -->
        <div class="product-info-column">
            <div class="product-meta-header">
                <span class="product-category-tag">${product.categoryName}</span>
                <h1 class="product-main-title">${product.productName}</h1>
                <div class="product-shop-row">
                    <span>Nhà cung cấp: <strong class="shop-name-badge">${product.shopName}</strong></span>
                    <span>•</span>
                    <span>Xuất xứ: <strong>${product.origin}</strong></span>
                </div>
            </div>

            <!-- Price Display -->
            <div class="price-box">
                <span class="live-price" id="displayPrice">
                    <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                </span>
                <span class="live-unit" id="displayUnit">/ ${product.packagingType}</span>
            </div>

            <form action="${pageContext.request.contextPath}/cart" method="post" class="purchase-actions-form" id="addToCartForm">
                <input type="hidden" name="action" value="add" id="formAction">
                <input type="hidden" name="productId" value="${product.productId}">
                
                <!-- Packaging Variants Selector -->
                <div>
                    <div class="variant-group-title">
                        <span>Chọn quy cách đóng gói:</span>
                        <span id="selectedVariantLabel" style="font-size: 13px; color: #16a34a; font-weight: 600;">${product.packagingType}</span>
                    </div>

                    <div class="variant-options-list">
                        <c:forEach var="v" items="${variants}" varStatus="status">
                            <button type="button" 
                                    class="variant-btn ${status.first ? 'active' : ''}"
                                    data-variant-id="${v.variantId}"
                                    data-price="${v.price}"
                                    data-unit="${v.unit}"
                                    data-packaging="${v.packagingType}"
                                    data-stock="${v.stock}"
                                    data-sku="${v.sku}"
                                    onclick="selectVariant(this)">
                                <span>${v.packagingType}</span>
                                <span class="variant-btn-price">
                                    <fmt:formatNumber value="${v.price}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </span>
                            </button>
                        </c:forEach>
                    </div>

                    <c:choose>
                        <c:when test="${not empty variants}">
                            <input type="hidden" name="variantId" id="selectedVariantId" value="${variants[0].variantId}">
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="variantId" id="selectedVariantId" value="0">
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Stock & SKU Live Status -->
                <div class="stock-sku-row">
                    <div>
                        Mã SKU: <strong id="displaySku">${not empty variants ? variants[0].sku : product.sku}</strong>
                    </div>
                    <span>•</span>
                    <div>
                        <c:set var="initialStock" value="${not empty variants ? variants[0].stock : product.stock}"/>
                        <span class="stock-badge-live ${initialStock > 0 ? 'in-stock' : 'out-stock'}" id="stockBadge">
                            <c:choose>
                                <c:when test="${initialStock > 0}">
                                    Còn hàng (${initialStock} có sẵn)
                                </c:when>
                                <c:otherwise>
                                    Tạm hết hàng
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>

                <!-- Quantity & Actions -->
                <div class="quantity-row">
                    <span style="font-size: 14px; font-weight: 600; color: #334155;">Số lượng:</span>
                    <div class="quantity-selector">
                        <button type="button" class="qty-btn" onclick="updateQty(-1)">-</button>
                        <input type="number" name="quantity" id="quantityInput" class="qty-input" value="1" min="1" max="${initialStock}">
                        <button type="button" class="qty-btn" onclick="updateQty(1)">+</button>
                    </div>
                </div>

                <div class="action-buttons-group">
                    <button type="submit" class="btn-add-cart" id="btnAddToCart">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="9" cy="21" r="1"></circle>
                            <circle cx="20" cy="21" r="1"></circle>
                            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                        </svg>
                        Thêm Vào Giỏ Hàng
                    </button>

                    <button type="button" class="btn-buy-now" onclick="buyNow()">
                        Mua Ngay
                    </button>
                </div>
            </form>

            <!-- Trust Perks -->
            <div class="trust-perks-box">
                <div class="perk-point">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    </svg>
                    <span>100% Trái cây tươi sạch tự nhiên</span>
                </div>
                <div class="perk-point">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="1" y="3" width="15" height="13"></rect>
                        <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"></polygon>
                        <circle cx="5.5" cy="18.5" r="2.5"></circle>
                        <circle cx="18.5" cy="18.5" r="2.5"></circle>
                    </svg>
                    <span>Giao hàng siêu tốc trong 2 giờ</span>
                </div>
                <div class="perk-point">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="23 4 23 10 17 10"></polyline>
                        <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                    </svg>
                    <span>Đổi trả 1-1 nếu dập nát hư hỏng</span>
                </div>
                <div class="perk-point">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 14 14"></polyline>
                    </svg>
                    <span>Đảm bảo an toàn vệ sinh thực phẩm</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Details Tab Section -->
    <div class="tabs-card">
        <div class="tabs-header">
            <button type="button" class="tab-btn active" onclick="switchTab('desc', this)">Mô Tả Sản Phẩm</button>
            <button type="button" class="tab-btn" onclick="switchTab('storage', this)">Bảo Quản & Sử Dụng</button>
            <button type="button" class="tab-btn" onclick="switchTab('supplier', this)">Thông Tin Xuất Xứ</button>
        </div>

        <div id="tab-desc" class="tab-content active">
            <p>${product.description}</p>
            <br>
            <p>Trái cây được tuyển chọn kỹ lưỡng trực tiếp từ nhà vườn và các đối tác nhập khẩu chính ngạch, đảm bảo giữ trọn vẹn hương vị tự nhiên và hàm lượng vitamin cao nhất.</p>
        </div>

        <div id="tab-storage" class="tab-content">
            <p><strong>Hướng dẫn bảo quản:</strong></p>
            <ul style="padding-left: 20px; margin-top: 8px;">
                <li>Bảo quản trong ngăn mát tủ lạnh ở nhiệt độ từ 4°C - 8°C để giữ trái cây tươi giòn lâu nhất.</li>
                <li>Chỉ nên rửa trái cây ngay trước khi ăn, tránh rửa trước khi cho vào tủ lạnh để hạn chế úng vỏ.</li>
                <li>Đối với các loại trái cây họ cam quýt, có thể để nơi khô ráo, thoáng mát tránh ánh nắng trực tiếp.</li>
            </ul>
        </div>

        <div id="tab-supplier" class="tab-content">
            <p><strong>Nguồn gốc xuất xứ:</strong> ${product.origin}</p>
            <p><strong>Cửa hàng phân phối:</strong> ${product.shopName}</p>
            <p><strong>Tiêu chuẩn canh tác:</strong> VietGAP / GlobalGAP / Canh tác hữu cơ organic an toàn, đạt chuẩn xuất nhập khẩu.</p>
        </div>
    </div>

    <!-- Related Fruits Section -->
    <c:if test="${not empty relatedProducts}">
        <section class="related-section">
            <div class="section-headline">
                <span>Trái Cây Cùng Loại Gợi Ý Cho Bạn</span>
                <a href="${pageContext.request.contextPath}/products?category=${product.categoryId}" style="font-size: 14px; color: #16a34a; font-weight: 600; text-decoration: none;">
                    Xem tất cả &rarr;
                </a>
            </div>

            <div class="catalog-product-grid">
                <c:forEach var="rp" items="${relatedProducts}">
                    <article class="fruit-card">
                        <div class="fruit-image-wrap" style="height: 180px;">
                            <img src="${rp.imageUrl}" alt="${rp.productName}" loading="lazy"
                                 onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500';">
                            <span class="fruit-badge-origin">${rp.origin}</span>
                        </div>
                        <div class="fruit-body">
                            <span class="fruit-category">${rp.categoryName}</span>
                            <h4 class="fruit-title">
                                <a href="${pageContext.request.contextPath}/product?id=${rp.productId}">
                                    ${rp.productName}
                                </a>
                            </h4>
                            <div class="fruit-footer" style="margin-top: auto;">
                                <div class="fruit-price">
                                    <fmt:formatNumber value="${rp.price}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </div>
                                <a href="${pageContext.request.contextPath}/product?id=${rp.productId}" class="btn-view-detail">
                                    Chi tiết
                                </a>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </section>
    </c:if>
</main>

<footer class="footer">
    <h3>FreshFruit Vietnam</h3>
    <p>Trái cây tươi ngon - Nguồn dinh dưỡng tự nhiên mỗi ngày</p>
    <p>&copy; 2026 Online Fruit Shopping. All rights reserved.</p>
</footer>

<script>
    // Image thumbnail switcher
    function switchMainImage(url, el) {
        document.getElementById('mainFruitImg').src = url;
        document.querySelectorAll('.thumb-item').forEach(item => item.classList.remove('active'));
        if (el) el.classList.add('active');
    }

    // Dynamic Variant selector
    let currentMaxStock = ${initialStock};

    function selectVariant(btn) {
        document.querySelectorAll('.variant-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        const variantId = btn.getAttribute('data-variant-id');
        const price = parseFloat(btn.getAttribute('data-price')) || 0;
        const packaging = btn.getAttribute('data-packaging') || '';
        const unit = btn.getAttribute('data-unit') || '';
        const stock = parseFloat(btn.getAttribute('data-stock')) || 0;
        const sku = btn.getAttribute('data-sku') || '';

        // Update Hidden Input
        document.getElementById('selectedVariantId').value = variantId;

        // Update Labels
        document.getElementById('selectedVariantLabel').innerText = packaging;
        document.getElementById('displayPrice').innerText = new Intl.NumberFormat('vi-VN').format(price) + ' ₫';
        document.getElementById('displayUnit').innerText = '/ ' + packaging;
        document.getElementById('displaySku').innerText = sku;

        // Update Stock status
        currentMaxStock = stock;
        const stockBadge = document.getElementById('stockBadge');
        const qtyInput = document.getElementById('quantityInput');
        const btnAdd = document.getElementById('btnAddToCart');

        if (stock > 0) {
            stockBadge.className = 'stock-badge-live in-stock';
            stockBadge.innerText = 'Còn hàng (' + stock + ' có sẵn)';
            qtyInput.max = stock;
            if (parseInt(qtyInput.value) > stock) {
                qtyInput.value = stock;
            }
            if (btnAdd) btnAdd.disabled = false;
        } else {
            stockBadge.className = 'stock-badge-live out-stock';
            stockBadge.innerText = 'Tạm hết hàng';
            qtyInput.max = 0;
            qtyInput.value = 0;
            if (btnAdd) btnAdd.disabled = true;
        }
    }

    // Quantity update
    function updateQty(delta) {
        const input = document.getElementById('quantityInput');
        let current = parseInt(input.value) || 1;
        current += delta;
        if (current < 1) current = 1;
        if (currentMaxStock > 0 && current > currentMaxStock) {
            current = currentMaxStock;
        }
        input.value = current;
    }

    // Buy now action
    function buyNow() {
        const form = document.getElementById('addToCartForm');
        document.getElementById('formAction').value = 'add';
        let buyNowInput = document.getElementById('buyNowInput');
        if (!buyNowInput) {
            buyNowInput = document.createElement('input');
            buyNowInput.type = 'hidden';
            buyNowInput.name = 'buyNow';
            buyNowInput.id = 'buyNowInput';
            buyNowInput.value = 'true';
            form.appendChild(buyNowInput);
        }
        form.submit();
    }

    // AJAX Add to Cart
    const cartForm = document.getElementById('addToCartForm');
    if (cartForm) {
        cartForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const variantId = document.getElementById('selectedVariantId').value;
            const qty = document.getElementById('quantityInput').value;

            const formData = new URLSearchParams();
            formData.append('action', 'add');
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
                showToast(data.message, data.success);
                if (data.success && data.cartCount !== undefined) {
                    updateNavCartBadge(data.cartCount);
                }
            })
            .catch(err => {
                cartForm.submit();
            });
        });
    }

    function showToast(message, isSuccess) {
        let toast = document.getElementById('cartToastNotification');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'cartToastNotification';
            toast.style.position = 'fixed';
            toast.style.bottom = '28px';
            toast.style.right = '28px';
            toast.style.padding = '16px 22px';
            toast.style.borderRadius = '14px';
            toast.style.boxShadow = '0 10px 30px rgba(0,0,0,0.18)';
            toast.style.zIndex = '9999';
            toast.style.fontWeight = '600';
            toast.style.fontSize = '14px';
            toast.style.display = 'flex';
            toast.style.alignItems = 'center';
            toast.style.gap = '14px';
            toast.style.transition = 'all 0.3s ease';
            document.body.appendChild(toast);
        }

        toast.style.background = isSuccess ? '#16a34a' : '#ef4444';
        toast.style.color = '#ffffff';
        toast.innerHTML = '<span>' + message + '</span>' + 
            (isSuccess ? '<a href="${pageContext.request.contextPath}/cart" style="color: #ffffff; text-decoration: underline; font-weight: 700; padding-left: 8px; border-left: 1px solid rgba(255,255,255,0.4);">Xem Giỏ Hàng</a>' : '');

        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
        toast.style.display = 'flex';

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            setTimeout(() => { toast.style.display = 'none'; }, 300);
        }, 4000);
    }

    function updateNavCartBadge(count) {
        const badge = document.getElementById('navCartBadge');
        if (badge) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-block' : 'none';
        }
    }

    // Tab switcher
    function switchTab(tabId, btn) {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

        btn.classList.add('active');
        const target = document.getElementById('tab-' + tabId);
        if (target) target.classList.add('active');
    }
</script>

</body>
</html>
