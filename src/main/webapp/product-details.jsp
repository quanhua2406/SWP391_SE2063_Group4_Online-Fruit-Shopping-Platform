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
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
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
        <a href="${pageContext.request.contextPath}/products" style="color: #16a34a; font-weight: 700;">Khám Phá Trái Cây</a>
        <a href="${pageContext.request.contextPath}/subscriptions">Gói Định Kỳ</a>

        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/cart" title="Giỏ hàng" style="display: inline-flex; align-items: center; gap: 6px;">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="9" cy="21" r="1"></circle>
                        <circle cx="20" cy="21" r="1"></circle>
                        <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                    </svg>
                    Giỏ hàng
                    <span id="navCartBadge" style="background: #ef4444; color: #fff; font-size: 11px; font-weight: 700; padding: 1px 6px; border-radius: 9999px; ${sessionScope.cartCount > 0 ? '' : 'display:none;'}">
                        ${sessionScope.cartCount}
                    </span>
                </a>
                <a href="${pageContext.request.contextPath}/profile">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
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
                <div class="product-shop-row" style="margin-bottom: 8px;">
                    <span>Nhà cung cấp: <strong class="shop-name-badge">${product.shopName}</strong></span>
                    <span>•</span>
                    <span>Xuất xứ: <strong>${product.origin}</strong></span>
                </div>
                <!-- Star Rating Badge -->
                <div class="product-rating-row" style="display: flex; align-items: center; gap: 8px;">
                    <a href="#tab-reviews" onclick="activateReviewsTab(event)" style="display: inline-flex; align-items: center; gap: 6px; text-decoration: none; color: inherit; cursor: pointer;">
                        <span style="font-size: 15px; font-weight: 700; color: #f59e0b; display: inline-flex; align-items: center; gap: 4px;">
                            <fmt:formatNumber value="${avgRating}" pattern="0.0"/>
                            <i class="fa-solid fa-star" style="color: #f59e0b; font-size: 14px;"></i>
                        </span>
                        <span style="color: #64748b; font-size: 13.5px; text-decoration: underline;">
                            (${totalReviews} đánh giá)
                        </span>
                    </a>
                    <span>•</span>
                    <span style="color: #10b981; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                        <i class="fa-solid fa-shield-check"></i> Đã kiểm định chất lượng
                    </span>
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
            <button type="button" class="tab-btn active" id="tabBtnDesc" onclick="switchTab('desc', this)">Mô Tả Sản Phẩm</button>
            <button type="button" class="tab-btn" id="tabBtnStorage" onclick="switchTab('storage', this)">Bảo Quản & Sử Dụng</button>
            <button type="button" class="tab-btn" id="tabBtnSupplier" onclick="switchTab('supplier', this)">Thông Tin Xuất Xứ</button>
            <button type="button" class="tab-btn" id="tabBtnReviews" onclick="switchTab('reviews', this)">
                Đánh Giá & Nhận Xét (${totalReviews})
            </button>
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

        <!-- Reviews & Ratings Tab Content -->
        <div id="tab-reviews" class="tab-content">
            <%-- Toast notifications --%>
            <c:if test="${reviewSent}">
                <div class="toast-msg toast-success show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
                    <i class="fa-solid fa-circle-check"></i>
                    <span>Đánh giá của bạn đã được gửi thành công và đang chờ kiểm duyệt!</span>
                </div>
            </c:if>
            <c:if test="${reviewUpdated}">
                <div class="toast-msg toast-success show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
                    <i class="fa-solid fa-circle-check"></i>
                    <span>Đánh giá của bạn đã được cập nhật thành công!</span>
                </div>
            </c:if>
            <c:if test="${reviewDeleted}">
                <div class="toast-msg toast-success show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
                    <i class="fa-solid fa-circle-check"></i>
                    <span>Đã xóa đánh giá thành công!</span>
                </div>
            </c:if>
            <c:if test="${deleteError}">
                <div class="toast-msg toast-error show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
                    <i class="fa-solid fa-circle-xmark"></i>
                    <span>Có lỗi xảy ra khi xóa đánh giá. Vui lòng thử lại sau.</span>
                </div>
            </c:if>
            <c:if test="${expired}">
                <div class="toast-msg toast-error show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    <span>Đánh giá đã quá thời hạn 7 ngày nên không thể chỉnh sửa.</span>
                </div>
            </c:if>
            <c:if test="${authError}">
                <div class="toast-msg toast-error show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
                    <i class="fa-solid fa-circle-xmark"></i>
                    <span>Bạn không có quyền thực hiện thao tác này.</span>
                </div>
            </c:if>

            <%-- Review Summary Section --%>
            <div class="review-summary-box" style="margin-bottom: 24px;">
                <!-- Average Score Column -->
                <div class="review-avg-score">
                    <div class="review-avg-number">
                        <fmt:formatNumber value="${avgRating}" pattern="0.0"/>
                    </div>
                    <div class="review-avg-stars">
                        <c:forEach var="star" begin="1" end="5">
                            <c:choose>
                                <c:when test="${avgRating >= star}">
                                    <i class="fa-solid fa-star"></i>
                                </c:when>
                                <c:when test="${avgRating >= star - 0.5}">
                                    <i class="fa-solid fa-star-half-stroke"></i>
                                </c:when>
                                <c:otherwise>
                                    <i class="fa-regular fa-star"></i>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>
                    <div class="review-avg-count">${totalReviews} lượt đánh giá</div>
                </div>

                <!-- Star Rating Distribution Bars -->
                <div class="review-bar-list">
                    <c:set var="c5" value="${stats[2] != null ? stats[2] : 0}"/>
                    <c:set var="c4" value="${stats[3] != null ? stats[3] : 0}"/>
                    <c:set var="c3" value="${stats[4] != null ? stats[4] : 0}"/>
                    <c:set var="c2" value="${stats[5] != null ? stats[5] : 0}"/>
                    <c:set var="c1" value="${stats[6] != null ? stats[6] : 0}"/>

                    <div class="review-bar-item">
                        <span class="review-bar-label"><i class="fa-solid fa-star star-gold"></i> 5</span>
                        <div class="review-bar-track">
                            <div class="review-bar-fill" style="width: ${totalReviews > 0 ? (c5 * 100 / totalReviews) : 0}%;"></div>
                        </div>
                        <span class="review-bar-count"><fmt:formatNumber value="${c5}" maxFractionDigits="0"/></span>
                    </div>
                    <div class="review-bar-item">
                        <span class="review-bar-label"><i class="fa-solid fa-star star-gold"></i> 4</span>
                        <div class="review-bar-track">
                            <div class="review-bar-fill" style="width: ${totalReviews > 0 ? (c4 * 100 / totalReviews) : 0}%;"></div>
                        </div>
                        <span class="review-bar-count"><fmt:formatNumber value="${c4}" maxFractionDigits="0"/></span>
                    </div>
                    <div class="review-bar-item">
                        <span class="review-bar-label"><i class="fa-solid fa-star star-gold"></i> 3</span>
                        <div class="review-bar-track">
                            <div class="review-bar-fill" style="width: ${totalReviews > 0 ? (c3 * 100 / totalReviews) : 0}%;"></div>
                        </div>
                        <span class="review-bar-count"><fmt:formatNumber value="${c3}" maxFractionDigits="0"/></span>
                    </div>
                    <div class="review-bar-item">
                        <span class="review-bar-label"><i class="fa-solid fa-star star-gold"></i> 2</span>
                        <div class="review-bar-track">
                            <div class="review-bar-fill" style="width: ${totalReviews > 0 ? (c2 * 100 / totalReviews) : 0}%;"></div>
                        </div>
                        <span class="review-bar-count"><fmt:formatNumber value="${c2}" maxFractionDigits="0"/></span>
                    </div>
                    <div class="review-bar-item">
                        <span class="review-bar-label"><i class="fa-solid fa-star star-gold"></i> 1</span>
                        <div class="review-bar-track">
                            <div class="review-bar-fill" style="width: ${totalReviews > 0 ? (c1 * 100 / totalReviews) : 0}%;"></div>
                        </div>
                        <span class="review-bar-count"><fmt:formatNumber value="${c1}" maxFractionDigits="0"/></span>
                    </div>
                </div>

                <!-- Call to action write review -->
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: flex-start; gap: 12px; padding: 16px 20px; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; min-width: 230px;">
                    <div style="font-size: 14px; font-weight: 700; color: #1e293b;">Bạn đã mua sản phẩm này?</div>
                    <c:choose>
                        <c:when test="${not empty sessionScope.user or not empty currentUserId}">
                            <c:choose>
                                <c:when test="${canReview}">
                                    <a href="${pageContext.request.contextPath}/write-review?productId=${product.productId}&orderId=${eligibleOrderId}"
                                       class="btn-submit-review"
                                       style="padding: 10px 16px; border-radius: 8px; font-size: 13.5px; font-weight: 700; display: inline-flex; align-items: center; gap: 6px; text-decoration: none;">
                                        <i class="fa-solid fa-pen-to-square"></i> Viết đánh giá
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <div style="font-size: 12.5px; color: #64748b; line-height: 1.4;">
                                        <i class="fa-solid fa-circle-info" style="color: #3b82f6;"></i>
                                        Chỉ khách hàng đã nhận hàng thành công mới có thể gửi đánh giá.
                                    </div>
                                    <a href="${pageContext.request.contextPath}/write-review?productId=${product.productId}"
                                       style="font-size: 12.5px; color: #16a34a; font-weight: 600; text-decoration: underline;">
                                        Nhập mã đơn hàng thủ công
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/login?redirect=${pageContext.request.contextPath}/product?id=${product.productId}%23tab-reviews"
                               class="btn-submit-review"
                               style="padding: 9px 15px; border-radius: 8px; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; text-decoration: none;">
                                <i class="fa-solid fa-arrow-right-to-bracket"></i> Đăng nhập để đánh giá
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <%-- Filter Buttons --%>
            <c:set var="baseFilterUrl" value="${pageContext.request.contextPath}/product?id=${product.productId}"/>
            <div class="review-filter-bar" role="group" aria-label="Lọc đánh giá" style="margin-bottom: 20px;">
                <a href="${baseFilterUrl}&filter=0#tab-reviews" class="review-filter-btn ${reviewFilter == 0 ? 'active' : ''}">
                    Tất cả (${totalReviews})
                </a>
                <a href="${baseFilterUrl}&filter=5#tab-reviews" class="review-filter-btn ${reviewFilter == 5 ? 'active' : ''}">
                    <i class="fa-solid fa-star star-gold"></i> 5 sao (<fmt:formatNumber value="${c5}" maxFractionDigits="0"/>)
                </a>
                <a href="${baseFilterUrl}&filter=4#tab-reviews" class="review-filter-btn ${reviewFilter == 4 ? 'active' : ''}">
                    <i class="fa-solid fa-star star-gold"></i> 4 sao (<fmt:formatNumber value="${c4}" maxFractionDigits="0"/>)
                </a>
                <a href="${baseFilterUrl}&filter=3#tab-reviews" class="review-filter-btn ${reviewFilter == 3 ? 'active' : ''}">
                    <i class="fa-solid fa-star star-gold"></i> 3 sao (<fmt:formatNumber value="${c3}" maxFractionDigits="0"/>)
                </a>
                <a href="${baseFilterUrl}&filter=2#tab-reviews" class="review-filter-btn ${reviewFilter == 2 ? 'active' : ''}">
                    <i class="fa-solid fa-star star-gold"></i> 2 sao (<fmt:formatNumber value="${c2}" maxFractionDigits="0"/>)
                </a>
                <a href="${baseFilterUrl}&filter=1#tab-reviews" class="review-filter-btn ${reviewFilter == 1 ? 'active' : ''}">
                    <i class="fa-solid fa-star star-gold"></i> 1 sao (<fmt:formatNumber value="${c1}" maxFractionDigits="0"/>)
                </a>
                <a href="${baseFilterUrl}&filter=-1#tab-reviews" class="review-filter-btn ${reviewFilter == -1 ? 'active' : ''}">
                    <i class="fa-regular fa-image"></i> Có hình ảnh
                </a>
            </div>

            <%-- Reviews List --%>
            <div class="review-list">
                <c:choose>
                    <c:when test="${empty reviewList}">
                        <div class="notice-box notice-info" style="margin: 16px 0;">
                            <span class="notice-icon"><i class="fa-solid fa-circle-info"></i></span>
                            <span>Chưa có đánh giá nào cho bộ lọc này. Hãy là người đầu tiên chia sẻ cảm nhận!</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="r" items="${reviewList}">
                            <c:set var="uName" value="${not empty r.userName ? r.userName : 'Khách hàng'}"/>
                            <c:set var="initial" value="${fn:toUpperCase(fn:substring(uName, 0, 1))}"/>
                            <c:set var="isOwner" value="${not empty currentUserId and currentUserId == r.user_id}"/>

                            <div class="review-item-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 20px; margin-bottom: 16px;">
                                <div class="review-item-header" style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                                    <div class="review-item-user" style="display: flex; align-items: center; gap: 12px;">
                                        <div class="review-item-avatar" style="width: 42px; height: 42px; border-radius: 50%; background: #e0f2fe; color: #0284c7; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px;">
                                            ${initial}
                                        </div>
                                        <div class="review-item-user-info">
                                            <div class="user-name" style="font-weight: 700; font-size: 14.5px; color: #1e293b;">
                                                ${uName}
                                                <c:if test="${isOwner}">
                                                    <span style="font-size: 12px; color: #16a34a; font-weight: 600; margin-left: 4px;">(Đánh giá của bạn)</span>
                                                </c:if>
                                            </div>
                                            <div class="user-order" style="font-size: 12px; color: #64748b; margin-top: 2px;">
                                                <i class="fa-solid fa-receipt"></i>
                                                <c:choose>
                                                    <c:when test="${r.order_id > 0}">
                                                        Đơn hàng #${r.order_id} &nbsp;|&nbsp; Đã nhận hàng
                                                    </c:when>
                                                    <c:otherwise>
                                                        Đã mua hàng thực tế
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </div>
                                    <div style="text-align: right;">
                                        <div class="review-item-stars" style="color: #f59e0b; font-size: 13px;">
                                            <c:forEach var="s" begin="1" end="5">
                                                <c:choose>
                                                    <c:when test="${s <= r.rating}">
                                                        <i class="fa-solid fa-star"></i>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <i class="fa-regular fa-star" style="color: #cbd5e1;"></i>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </div>
                                        <div class="review-item-date" style="font-size: 12px; color: #94a3b8; margin-top: 4px;">
                                            <fmt:formatDate value="${not empty r.created_at ? r.created_at : r.create_at}" pattern="dd/MM/yyyy"/>
                                        </div>
                                    </div>
                                </div>

                                <c:if test="${not empty r.title}">
                                    <div class="review-item-title" style="font-weight: 700; font-size: 15px; color: #0f172a; margin-bottom: 6px;">
                                        ${fn:escapeXml(r.title)}
                                    </div>
                                </c:if>

                                <div class="review-item-comment" style="font-size: 14px; color: #334155; line-height: 1.6; margin-bottom: 12px; white-space: pre-wrap;">${fn:escapeXml(r.comment)}</div>

                                <c:if test="${not empty r.imageUrls}">
                                    <div class="review-item-images" style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 14px;">
                                        <c:forEach var="img" items="${fn:split(r.imageUrls, ',')}">
                                            <c:set var="trimmedImg" value="${fn:trim(img)}"/>
                                            <c:if test="${not empty trimmedImg}">
                                                <img class="review-item-img"
                                                     src="${pageContext.request.contextPath}/${trimmedImg}"
                                                     alt="Ảnh đánh giá"
                                                     style="width: 72px; height: 72px; object-fit: cover; border-radius: 8px; border: 1px solid #e2e8f0; cursor: pointer;"
                                                     onclick="window.open(this.src, '_blank')">
                                            </c:if>
                                        </c:forEach>
                                    </div>
                                </c:if>

                                <div class="review-item-footer" style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f1f5f9; padding-top: 10px;">
                                    <div class="review-helpful" style="display: flex; align-items: center; gap: 10px; font-size: 13px;">
                                        <span style="color: #64748b;">Hữu ích?</span>
                                        <button type="button" class="btn-helpful" onclick="markHelpful(this)" style="background: none; border: 1px solid #e2e8f0; padding: 4px 10px; border-radius: 6px; cursor: pointer; color: #475569; display: inline-flex; align-items: center; gap: 5px;">
                                            <i class="fa-regular fa-thumbs-up"></i> <span>0</span>
                                        </button>

                                        <c:if test="${isOwner}">
                                            <c:choose>
                                                <c:when test="${r.editable}">
                                                    <a href="${pageContext.request.contextPath}/edit-review?reviewId=${r.review_id}"
                                                       class="btn-edit-review"
                                                       style="color: #2563eb; font-weight: 600; font-size: 13px; text-decoration: none; display: inline-flex; align-items: center; gap: 4px;"
                                                       title="Chỉnh sửa đánh giá trong vòng 7 ngày (còn ${r.remainingDays} ngày)">
                                                        <i class="fa-solid fa-pen-to-square"></i> Sửa
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-size: 12px; color: #94a3b8;" title="Đã quá thời hạn 7 ngày kể từ lúc đăng">
                                                        <i class="fa-solid fa-lock"></i> Hết hạn sửa
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>

                                            <button type="button"
                                                    class="btn-delete-review"
                                                    data-review-id="${r.review_id}"
                                                    data-product-id="${r.product_id}"
                                                    data-rating="${r.rating}"
                                                    data-comment="${fn:escapeXml(r.comment)}"
                                                    onclick="openDeleteModal(this)"
                                                    style="background: none; border: none; color: #ef4444; font-size: 13px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: 4px;"
                                                    title="Xóa đánh giá này">
                                                <i class="fa-solid fa-trash-can"></i> Xóa
                                            </button>
                                        </c:if>

                                        <c:if test="${not isOwner and not empty currentUserId}">
                                            <button type="button"
                                                    class="btn-report-review"
                                                    onclick="openReportModal(${r.review_id}, window.location.href)"
                                                    style="background: none; border: none; color: #94a3b8; font-size: 12.5px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px;"
                                                    title="Báo cáo đánh giá">
                                                <i class="fa-solid fa-flag"></i> Báo cáo
                                            </button>
                                        </c:if>
                                    </div>

                                    <span class="review-verified-badge" style="font-size: 12px; color: #16a34a; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                                        <i class="fa-solid fa-shield-check"></i> Đã xác thực mua hàng
                                    </span>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- Pagination --%>
            <c:if test="${totalPages > 1}">
                <div class="pagination-nav" style="display: flex; justify-content: center; gap: 6px; margin-top: 24px;">
                    <c:if test="${currentPage > 1}">
                        <a href="${baseFilterUrl}&filter=${reviewFilter}&page=${currentPage - 1}#tab-reviews"
                           class="review-filter-btn" style="padding: 6px 12px;">
                            <i class="fa-solid fa-chevron-left"></i>
                        </a>
                    </c:if>
                    <c:forEach var="p" begin="1" end="${totalPages}">
                        <a href="${baseFilterUrl}&filter=${reviewFilter}&page=${p}#tab-reviews"
                           class="review-filter-btn ${p == currentPage ? 'active' : ''}" style="padding: 6px 12px;">
                            ${p}
                        </a>
                    </c:forEach>
                    <c:if test="${currentPage < totalPages}">
                        <a href="${baseFilterUrl}&filter=${reviewFilter}&page=${currentPage + 1}#tab-reviews"
                           class="review-filter-btn" style="padding: 6px 12px;">
                            <i class="fa-solid fa-chevron-right"></i>
                        </a>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>

    <!-- Delete Review Modal -->
    <div id="deleteReviewModal" class="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalTitle" style="display:none; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 2100; align-items: center; justify-content: center;">
        <div class="delete-modal-box" style="background: #ffffff; border-radius: 16px; padding: 24px; max-width: 440px; width: 90%; box-shadow: 0 20px 40px rgba(0,0,0,0.2);">
            <div class="delete-modal-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <div class="delete-modal-icon-wrap" style="color: #ef4444; font-size: 20px;">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                    </div>
                    <div class="delete-modal-title" id="modalTitle" style="font-weight: 700; font-size: 16px; color: #1e293b;">Xác nhận xóa đánh giá</div>
                </div>
                <button type="button" class="delete-modal-close" onclick="closeDeleteModal()" aria-label="Đóng popup" style="background: none; border: none; font-size: 18px; cursor: pointer; color: #94a3b8;">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>
            <div class="delete-modal-body" style="margin-bottom: 20px;">
                <p class="delete-modal-desc" style="font-size: 13.5px; color: #64748b; line-height: 1.5; margin-bottom: 12px;">
                    Bạn có chắc chắn muốn xóa đánh giá này không? <strong>Hành động này không thể hoàn tác.</strong>
                </p>
                <div class="delete-review-summary" style="background: #f8fafc; padding: 12px; border-radius: 10px; border-left: 3px solid #ef4444;">
                    <div class="delete-review-stars" id="modalReviewStars" style="color: #f59e0b; margin-bottom: 4px; font-size: 13px;"></div>
                    <div class="delete-review-excerpt" id="modalReviewExcerpt" style="font-size: 13px; color: #334155; font-style: italic;"></div>
                </div>
            </div>
            <form id="deleteReviewForm" action="${pageContext.request.contextPath}/delete-review" method="post">
                <input type="hidden" name="review_id" id="deleteReviewId" value="">
                <input type="hidden" name="product_id" id="deleteProductId" value="">
                <div class="delete-modal-actions" style="display: flex; justify-content: flex-end; gap: 10px;">
                    <button type="button" class="btn-modal-cancel" onclick="closeDeleteModal()" style="padding: 8px 16px; border-radius: 8px; border: 1px solid #cbd5e1; background: #ffffff; color: #475569; font-weight: 600; cursor: pointer;">
                        Hủy
                    </button>
                    <button type="submit" id="btnConfirmDelete" class="btn-modal-delete" style="padding: 8px 16px; border-radius: 8px; border: none; background: #ef4444; color: #ffffff; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: 6px;">
                        <i class="fa-solid fa-trash-can"></i> Xóa
                    </button>
                </div>
            </form>
        </div>
    </div>
    <jsp:include page="/review/reportReviewModal.jsp"/>

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

        if (btn) {
            btn.classList.add('active');
        } else {
            const defaultBtn = document.getElementById('tabBtn' + tabId.charAt(0).toUpperCase() + tabId.slice(1));
            if (defaultBtn) defaultBtn.classList.add('active');
        }

        const target = document.getElementById('tab-' + tabId);
        if (target) target.classList.add('active');
    }

    function activateReviewsTab(e) {
        if (e) e.preventDefault();
        const btn = document.getElementById('tabBtnReviews');
        switchTab('reviews', btn);
        const el = document.getElementById('tab-reviews');
        if (el) el.scrollIntoView({ behavior: 'smooth' });
    }

    // Auto-switch to reviews tab on load if specified in URL
    window.addEventListener('DOMContentLoaded', function() {
        const urlParams = new URLSearchParams(window.location.search);
        const hasReviewParam = urlParams.has('reviewSent') || urlParams.has('reviewUpdated') || 
                               urlParams.has('reviewDeleted') || urlParams.has('filter') || 
                               urlParams.has('page') || urlParams.has('expired') || 
                               urlParams.has('authError') || urlParams.has('deleteError');

        if (window.location.hash === '#tab-reviews' || hasReviewParam) {
            const btn = document.getElementById('tabBtnReviews');
            switchTab('reviews', btn);
            if (hasReviewParam || window.location.hash === '#tab-reviews') {
                const el = document.getElementById('tab-reviews');
                if (el) {
                    setTimeout(() => {
                        el.scrollIntoView({ behavior: 'smooth' });
                    }, 200);
                }
            }
        }
    });

    // Mark review helpful
    function markHelpful(btn) {
        if (btn.dataset.voted) return;
        btn.dataset.voted = '1';
        btn.style.color = '#16a34a';
        btn.style.borderColor = '#16a34a';
        const span = btn.querySelector('span');
        if (span) {
            span.textContent = (parseInt(span.textContent) || 0) + 1;
        }
    }

    // Delete review modal controls
    function openDeleteModal(btn) {
        const rId = btn.getAttribute('data-review-id');
        const pId = btn.getAttribute('data-product-id');
        const rating = parseInt(btn.getAttribute('data-rating')) || 5;
        const comment = btn.getAttribute('data-comment') || '';

        document.getElementById('deleteReviewId').value = rId;
        document.getElementById('deleteProductId').value = pId;

        // Render stars in modal
        let starsHtml = '';
        for (let i = 1; i <= 5; i++) {
            starsHtml += (i <= rating) ? '<i class="fa-solid fa-star"></i> ' : '<i class="fa-regular fa-star"></i> ';
        }
        document.getElementById('modalReviewStars').innerHTML = starsHtml;

        const excerpt = comment.length > 90 ? comment.substring(0, 90) + '...' : comment;
        document.getElementById('modalReviewExcerpt').textContent = '"' + excerpt + '"';

        const modal = document.getElementById('deleteReviewModal');
        if (modal) modal.style.display = 'flex';
    }

    function closeDeleteModal() {
        const modal = document.getElementById('deleteReviewModal');
        if (modal) modal.style.display = 'none';
    }
</script>

</body>
</html>
