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
    <title>Khám Phá & Tìm Kiếm Trái Cây Tươi | FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">
    <style>
        /* Modern Catalog Layout & Components */
        .catalog-container {
            width: 90%;
            max-width: 1360px;
            margin: 25px auto 60px;
        }

        .catalog-breadcrumb {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #64748b;
            margin-bottom: 24px;
        }
        .catalog-breadcrumb a {
            color: #16a34a;
            font-weight: 500;
        }
        .catalog-breadcrumb a:hover {
            text-decoration: underline;
        }

        .catalog-layout {
            display: grid;
            grid-template-columns: 295px 1fr;
            gap: 32px;
            align-items: start;
        }

        /* Sidebar Styles */
        .catalog-sidebar {
            background: #ffffff;
            border-radius: 14px;
            border: 1px solid #e2e8f0;
            padding: 24px 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.03);
            position: sticky;
            top: 20px;
        }

        .sidebar-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding-bottom: 16px;
            margin-bottom: 20px;
            border-bottom: 1px solid #f1f5f9;
        }
        .sidebar-header h3 {
            font-size: 17px;
            font-weight: 700;
            color: #0f172a;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 8px;
            white-space: nowrap;
        }
        .btn-reset-filters {
            font-size: 12.5px;
            color: #ef4444;
            text-decoration: none;
            font-weight: 600;
            padding: 5px 10px;
            border-radius: 6px;
            background: #fee2e2;
            white-space: nowrap;
            flex-shrink: 0;
            transition: all 0.2s;
        }
        .btn-reset-filters:hover {
            background: #fecaca;
        }

        .filter-section {
            margin-bottom: 24px;
            padding-bottom: 20px;
            border-bottom: 1px solid #f1f5f9;
        }
        .filter-section:last-child {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0;
        }
        .filter-title {
            font-size: 15px;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 14px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        /* Search input in sidebar */
        .sidebar-search-box {
            position: relative;
            margin-bottom: 8px;
        }
        .sidebar-search-input {
            width: 100%;
            padding: 10px 14px 10px 36px;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            font-size: 13.5px;
            outline: none;
            transition: border 0.2s;
        }
        .sidebar-search-input:focus {
            border-color: #16a34a;
            box-shadow: 0 0 0 3px rgba(22,163,74,0.15);
        }
        .sidebar-search-icon {
            position: absolute;
            left: 10px;
            top: 50%;
            transform: translateY(-50%);
            color: #94a3b8;
        }

        /* Category List */
        .category-filter-list {
            list-style: none;
            padding: 0;
            margin: 0;
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .category-filter-item a {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 8px 12px;
            border-radius: 8px;
            color: #334155;
            font-size: 14px;
            transition: all 0.2s;
        }
        .category-filter-item a:hover {
            background: #f1f5f9;
            color: #16a34a;
        }
        .category-filter-item.active a {
            background: #ecfdf5;
            color: #15803d;
            font-weight: 700;
        }
        .category-count {
            font-size: 12px;
            background: #e2e8f0;
            color: #475569;
            padding: 2px 7px;
            border-radius: 10px;
            font-weight: 600;
        }
        .category-filter-item.active .category-count {
            background: #16a34a;
            color: #ffffff;
        }

        /* Checkbox & Radio filters */
        .filter-options {
            display: flex;
            flex-direction: column;
            gap: 10px;
            max-height: 220px;
            overflow-y: auto;
            padding-right: 4px;
        }
        .filter-options::-webkit-scrollbar {
            width: 5px;
        }
        .filter-options::-webkit-scrollbar-thumb {
            background: #cbd5e1;
            border-radius: 4px;
        }
        .filter-checkbox-label {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 13.5px;
            color: #334155;
            cursor: pointer;
            user-select: none;
        }
        .filter-checkbox-label input[type="checkbox"],
        .filter-checkbox-label input[type="radio"] {
            accent-color: #16a34a;
            width: 16px;
            height: 16px;
            cursor: pointer;
        }

        /* Custom Price Range Inputs */
        .price-inputs-row {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-top: 12px;
        }
        .price-input {
            width: 100%;
            padding: 8px 10px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 13px;
            outline: none;
        }
        .price-input:focus {
            border-color: #16a34a;
        }
        .btn-apply-filter {
            width: 100%;
            background: #16a34a;
            color: white;
            border: none;
            padding: 11px;
            border-radius: 8px;
            font-weight: 700;
            font-size: 14px;
            cursor: pointer;
            transition: background 0.2s;
            margin-top: 16px;
        }
        .btn-apply-filter:hover {
            background: #15803d;
        }

        /* Toolbar / Top Bar */
        .catalog-toolbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 14px 20px;
            margin-bottom: 24px;
            flex-wrap: wrap;
            gap: 16px;
        }
        .toolbar-info {
            font-size: 14px;
            color: #475569;
        }
        .toolbar-info strong {
            color: #0f172a;
        }
        .toolbar-actions {
            display: flex;
            align-items: center;
            gap: 14px;
        }
        .sort-select {
            padding: 8px 14px;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            font-size: 13.5px;
            color: #1e293b;
            outline: none;
            background: #fff;
            cursor: pointer;
        }
        .sort-select:focus {
            border-color: #16a34a;
        }

        /* Product Grid */
        .catalog-product-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
            gap: 22px;
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
            height: 200px;
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

        .fruit-badge-stock {
            position: absolute;
            top: 10px;
            right: 10px;
            background: #ecfdf5;
            color: #065f46;
            border: 1px solid #a7f3d0;
            font-size: 11px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 6px;
        }
        .fruit-badge-stock.out {
            background: #fef2f2;
            color: #991b1b;
            border-color: #fecaca;
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
            margin: 0 0 6px;
            line-height: 1.35;
        }
        .fruit-title a {
            color: inherit;
            transition: color 0.2s;
        }
        .fruit-title a:hover {
            color: #16a34a;
        }
        .fruit-desc {
            font-size: 13px;
            color: #64748b;
            line-height: 1.45;
            margin-bottom: 14px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            flex: 1;
        }
        .fruit-variant-meta {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 12px;
            color: #475569;
            background: #f8fafc;
            padding: 6px 10px;
            border-radius: 6px;
            margin-bottom: 14px;
        }
        .fruit-footer {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding-top: 12px;
            border-top: 1px solid #f1f5f9;
        }
        .fruit-price {
            font-size: 18px;
            font-weight: 800;
            color: #15803d;
        }
        .fruit-price-unit {
            font-size: 12px;
            color: #64748b;
            font-weight: 500;
        }
        .btn-view-detail {
            background: #16a34a;
            color: #ffffff;
            border: none;
            padding: 8px 14px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
            text-decoration: none;
            transition: background 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .btn-view-detail:hover {
            background: #15803d;
            color: #ffffff;
        }

        /* Empty State */
        .catalog-empty {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 60px 20px;
            text-align: center;
            color: #64748b;
        }
        .catalog-empty svg {
            margin-bottom: 16px;
            color: #94a3b8;
        }
        .catalog-empty h3 {
            font-size: 20px;
            color: #0f172a;
            margin-bottom: 8px;
        }
        .catalog-empty p {
            font-size: 14px;
            margin-bottom: 20px;
        }

        /* Pagination */
        .catalog-pagination {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-top: 40px;
        }
        .page-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 40px;
            height: 40px;
            padding: 0 12px;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            background: #ffffff;
            color: #334155;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s;
        }
        .page-btn:hover:not(.disabled) {
            border-color: #16a34a;
            color: #16a34a;
            background: #f0fdf4;
        }
        .page-btn.active {
            background: #16a34a;
            border-color: #16a34a;
            color: #ffffff;
        }
        .page-btn.disabled {
            opacity: 0.4;
            cursor: not-allowed;
            pointer-events: none;
        }

        /* Autocomplete dropdown */
        .search-box-wrapper {
            position: relative;
            flex: 1;
            max-width: 520px;
        }
        .search-suggestions-dropdown {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: #ffffff;
            border: 1px solid #cbd5e1;
            border-top: none;
            border-radius: 0 0 10px 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
            max-height: 380px;
            overflow-y: auto;
            z-index: 1000;
            display: none;
        }
        .suggestion-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 14px;
            border-bottom: 1px solid #f1f5f9;
            text-decoration: none;
            color: #1e293b;
            transition: background 0.15s;
        }
        .suggestion-item:last-child {
            border-bottom: none;
        }
        .suggestion-item:hover, .suggestion-item.active {
            background: #f8fafc;
        }
        .suggestion-thumb {
            width: 44px;
            height: 44px;
            border-radius: 6px;
            object-fit: cover;
            background: #f1f5f9;
            flex-shrink: 0;
        }
        .suggestion-info {
            flex: 1;
            min-width: 0;
        }
        .suggestion-name {
            font-size: 14px;
            font-weight: 600;
            color: #0f172a;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .suggestion-meta {
            font-size: 12px;
            color: #64748b;
            display: flex;
            align-items: center;
            gap: 8px;
            margin-top: 2px;
        }
        .suggestion-price {
            font-size: 13.5px;
            font-weight: 700;
            color: #15803d;
            white-space: nowrap;
        }

        @media (max-width: 900px) {
            .catalog-layout {
                grid-template-columns: 1fr;
            }
            .catalog-sidebar {
                position: static;
            }
        }
    </style>
</head>
<body>

<!-- Header / Navigation -->
<header class="navbar">
    <a href="${pageContext.request.contextPath}/home" class="logo">FreshFruit</a>

    <div class="search-box-wrapper">
        <form action="${pageContext.request.contextPath}/products" method="get" class="search-box" id="topSearchForm">
            <input type="text" name="keyword" id="topSearchInput" placeholder="Tìm kiếm trái cây..." 
                   value="${fn:escapeXml(selectedKeyword)}" autocomplete="off">
            <button type="submit" aria-label="Tìm kiếm">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
            </button>
        </form>
        <div class="search-suggestions-dropdown" id="topSearchDropdown"></div>
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

<main class="catalog-container">
    <!-- Breadcrumb -->
    <div class="catalog-breadcrumb">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <span>/</span>
        <span>Khám phá sản phẩm</span>
        <c:if test="${not empty selectedKeyword}">
            <span>/</span>
            <span>Kết quả cho: "<strong>${fn:escapeXml(selectedKeyword)}</strong>"</span>
        </c:if>
    </div>

    <div class="catalog-layout">
        <!-- Sidebar Filter Form -->
        <aside class="catalog-sidebar">
            <div class="sidebar-header">
                <h3>
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
                    </svg>
                    Bộ Lọc Tìm Kiếm
                </h3>
                <a href="${pageContext.request.contextPath}/products" class="btn-reset-filters">Xóa lọc</a>
            </div>

            <form action="${pageContext.request.contextPath}/products" method="get" id="filterForm">
                <!-- Multi-field search box -->
                <div class="filter-section">
                    <label class="filter-title" for="sidebarKeyword">Từ khóa tìm kiếm</label>
                    <div class="sidebar-search-box">
                        <svg class="sidebar-search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                        <input type="text" name="keyword" id="sidebarKeyword" class="sidebar-search-input" 
                               placeholder="Tên quả, xuất xứ, loại..." value="${fn:escapeXml(selectedKeyword)}">
                    </div>
                </div>

                <!-- Category Filter -->
                <div class="filter-section">
                    <div class="filter-title">Danh Mục Trái Cây</div>
                    <ul class="category-filter-list">
                        <li class="category-filter-item ${empty selectedCategoryId ? 'active' : ''}">
                            <a href="${pageContext.request.contextPath}/products?${fn:replace(baseQueryString, '&category=', '&prevCat=')}">
                                <span>Tất cả danh mục</span>
                                <span class="category-count">${totalProducts}</span>
                            </a>
                        </li>
                        <c:forEach var="cat" items="${categories}">
                            <li class="category-filter-item ${selectedCategoryId == cat.categoryId ? 'active' : ''}">
                                <a href="${pageContext.request.contextPath}/products?category=${cat.categoryId}&keyword=${fn:escapeXml(selectedKeyword)}&sortBy=${selectedSortBy}">
                                    <span>${cat.categoryName}</span>
                                    <span class="category-count">${cat.productCount}</span>
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                    <input type="hidden" name="category" value="${selectedCategoryId}">
                </div>

                <!-- Origin / Xuất xứ Filter -->
                <div class="filter-section">
                    <div class="filter-title">Nguồn Gốc & Xuất Xứ</div>
                    <div class="filter-options">
                        <c:forEach var="orig" items="${distinctOrigins}">
                            <label class="filter-checkbox-label">
                                <input type="checkbox" name="origin" value="${orig}" 
                                       <c:if test="${not empty selectedOrigins && selectedOrigins.contains(orig)}">checked</c:if>>
                                <span>${orig}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <!-- Price Range Presets -->
                <div class="filter-section">
                    <div class="filter-title">Khoảng Giá Tuyển Chọn</div>
                    <div class="filter-options">
                        <label class="filter-checkbox-label">
                            <input type="radio" name="priceRange" value="" ${empty selectedPriceRange ? 'checked' : ''}>
                            <span>Tất cả mức giá</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="priceRange" value="under100" ${selectedPriceRange == 'under100' ? 'checked' : ''}>
                            <span>Dưới 100.000 ₫</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="priceRange" value="100to300" ${selectedPriceRange == '100to300' ? 'checked' : ''}>
                            <span>100.000 ₫ - 300.000 ₫</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="priceRange" value="300to600" ${selectedPriceRange == '300to600' ? 'checked' : ''}>
                            <span>300.000 ₫ - 600.000 ₫</span>
                        </label>
                        <label class="filter-checkbox-label">
                            <input type="radio" name="priceRange" value="above600" ${selectedPriceRange == 'above600' ? 'checked' : ''}>
                            <span>Trên 600.000 ₫</span>
                        </label>
                    </div>

                    <div style="margin-top: 14px; font-size: 13px; font-weight: 600; color: #475569;">Tự nhập khoảng giá (₫):</div>
                    <div class="price-inputs-row">
                        <input type="number" name="minPrice" class="price-input" placeholder="Từ" value="${selectedMinPrice}" min="0" step="5000">
                        <span style="color: #94a3b8;">-</span>
                        <input type="number" name="maxPrice" class="price-input" placeholder="Đến" value="${selectedMaxPrice}" min="0" step="5000">
                    </div>
                </div>

                <!-- Stock Availability -->
                <div class="filter-section">
                    <div class="filter-title">Tình Trạng Kho</div>
                    <label class="filter-checkbox-label">
                        <input type="checkbox" name="inStock" value="true" ${selectedInStock ? 'checked' : ''}>
                        <span>Chỉ hiển thị sản phẩm còn hàng</span>
                    </label>
                </div>

                <!-- Hidden sort field to preserve when submitting sidebar -->
                <input type="hidden" name="sortBy" value="${selectedSortBy}" id="sidebarSortBy">

                <button type="submit" class="btn-apply-filter">Áp Dụng Bộ Lọc</button>
            </form>
        </aside>

        <!-- Main Product Results Area -->
        <section class="catalog-main">
            <!-- Toolbar -->
            <div class="catalog-toolbar">
                <div class="toolbar-info">
                    Tìm thấy <strong>${totalProducts}</strong> sản phẩm trái cây chất lượng cao
                </div>

                <div class="toolbar-actions">
                    <label for="topSortSelect" style="font-size: 13.5px; color: #475569; font-weight: 500;">Sắp xếp theo:</label>
                    <select id="topSortSelect" class="sort-select" onchange="applySorting(this.value)">
                        <option value="newest" ${selectedSortBy == 'newest' ? 'selected' : ''}>Mới nhất</option>
                        <option value="price_asc" ${selectedSortBy == 'price_asc' ? 'selected' : ''}>Giá: Thấp đến cao</option>
                        <option value="price_desc" ${selectedSortBy == 'price_desc' ? 'selected' : ''}>Giá: Cao đến thấp</option>
                        <option value="name_asc" ${selectedSortBy == 'name_asc' ? 'selected' : ''}>Tên: A đến Z</option>
                        <option value="stock_desc" ${selectedSortBy == 'stock_desc' ? 'selected' : ''}>Tồn kho nhiều nhất</option>
                    </select>
                </div>
            </div>

            <!-- Product Cards Grid -->
            <c:choose>
                <c:when test="${not empty products}">
                    <div class="catalog-product-grid">
                        <c:forEach var="p" items="${products}">
                            <article class="fruit-card">
                                <div class="fruit-image-wrap">
                                    <img src="${p.imageUrl}" alt="${p.productName}" loading="lazy"
                                         onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500';">
                                    
                                    <span class="fruit-badge-origin" title="${p.origin}">
                                        ${p.origin}
                                    </span>

                                    <c:choose>
                                        <c:when test="${p.stock > 0}">
                                            <span class="fruit-badge-stock">Còn hàng</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="fruit-badge-stock out">Tạm hết</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="fruit-body">
                                    <span class="fruit-category">${p.categoryName}</span>
                                    <h4 class="fruit-title">
                                        <a href="${pageContext.request.contextPath}/product?id=${p.productId}">
                                            ${p.productName}
                                        </a>
                                    </h4>
                                    <p class="fruit-desc">${p.description}</p>

                                    <div class="fruit-variant-meta">
                                        <span>Quy cách: <strong>${p.packagingType}</strong></span>
                                        <span>•</span>
                                        <span>Đơn vị: <strong>${p.unit}</strong></span>
                                    </div>

                                    <div class="fruit-footer">
                                        <div>
                                            <div class="fruit-price">
                                                <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                            </div>
                                            <span class="fruit-price-unit">/ ${p.packagingType}</span>
                                        </div>

                                        <a href="${pageContext.request.contextPath}/product?id=${p.productId}" class="btn-view-detail">
                                            Xem chi tiết
                                        </a>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="catalog-pagination">
                            <c:set var="prevPage" value="${currentPage - 1}"/>
                            <c:set var="nextPage" value="${currentPage + 1}"/>

                            <a href="${pageContext.request.contextPath}/products?page=${prevPage}${baseQueryString}" 
                               class="page-btn ${currentPage <= 1 ? 'disabled' : ''}">
                                &larr; Trước
                            </a>

                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <a href="${pageContext.request.contextPath}/products?page=${i}${baseQueryString}" 
                                   class="page-btn ${currentPage == i ? 'active' : ''}">
                                    ${i}
                                </a>
                            </c:forEach>

                            <a href="${pageContext.request.contextPath}/products?page=${nextPage}${baseQueryString}" 
                               class="page-btn ${currentPage >= totalPages ? 'disabled' : ''}">
                                Sau &rarr;
                            </a>
                        </div>
                    </c:if>

                </c:when>
                <c:otherwise>
                    <!-- Empty State -->
                    <div class="catalog-empty">
                        <svg xmlns="http://www.w3.org/2000/svg" width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                            <line x1="8" y1="11" x2="14" y2="11"></line>
                        </svg>
                        <h3>Không tìm thấy sản phẩm phù hợp</h3>
                        <p>Vui lòng thử tìm kiếm bằng từ khóa khác hoặc điều chỉnh lại các tiêu chí bộ lọc giá, xuất xứ.</p>
                        <a href="${pageContext.request.contextPath}/products" class="btn-view-detail" style="padding: 10px 20px;">
                            Xem Tất Cả Trái Cây
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>

<footer class="footer">
    <h3>FreshFruit Vietnam</h3>
    <p>Trái cây tươi ngon - Nguồn dinh dưỡng tự nhiên mỗi ngày</p>
    <p>&copy; 2026 Online Fruit Shopping. All rights reserved.</p>
</footer>

<!-- Autocomplete & Filter Scripts -->
<script>
    // Live Autocomplete for Header Search
    (function() {
        const input = document.getElementById('topSearchInput');
        const dropdown = document.getElementById('topSearchDropdown');
        let debounceTimer = null;

        if (!input || !dropdown) return;

        input.addEventListener('input', function() {
            const query = this.value.trim();
            clearTimeout(debounceTimer);

            if (query.length < 2) {
                dropdown.style.display = 'none';
                dropdown.innerHTML = '';
                return;
            }

            debounceTimer = setTimeout(() => {
                fetch('${pageContext.request.contextPath}/api/products/search-suggestions?q=' + encodeURIComponent(query))
                    .then(res => res.json())
                    .then(data => {
                        if (data && data.length > 0) {
                            renderSuggestions(data);
                        } else {
                            dropdown.style.display = 'none';
                        }
                    })
                    .catch(err => {
                        console.error('Autocomplete error:', err);
                        dropdown.style.display = 'none';
                    });
            }, 250);
        });

        function renderSuggestions(items) {
            dropdown.innerHTML = '';
            items.forEach(item => {
                const a = document.createElement('a');
                a.className = 'suggestion-item';
                a.href = '${pageContext.request.contextPath}/product?id=' + item.id;
                
                const formattedPrice = new Intl.NumberFormat('vi-VN').format(item.price) + ' ₫';
                const imgUrl = item.imageUrl || 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500';

                a.innerHTML = 
                    '<img class="suggestion-thumb" src="' + imgUrl + '" alt="' + item.name + '" onerror="this.src=\'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500\'">' +
                    '<div class="suggestion-info">' +
                        '<div class="suggestion-name">' + item.name + '</div>' +
                        '<div class="suggestion-meta">' +
                            '<span>' + item.category + '</span>' +
                            '<span>•</span>' +
                            '<span>' + item.origin + '</span>' +
                        '</div>' +
                    '</div>' +
                    '<div class="suggestion-price">' + formattedPrice + '</div>';

                dropdown.appendChild(a);
            });
            dropdown.style.display = 'block';
        }

        // Close dropdown when clicking outside
        document.addEventListener('click', function(e) {
            if (!input.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.style.display = 'none';
            }
        });
    })();

    // Sorting change handler
    function applySorting(sortByValue) {
        const form = document.getElementById('filterForm');
        const sortInput = document.getElementById('sidebarSortBy');
        if (form && sortInput) {
            sortInput.value = sortByValue;
            form.submit();
        } else {
            const url = new URL(window.location.href);
            url.searchParams.set('sortBy', sortByValue);
            url.searchParams.set('page', '1');
            window.location.href = url.toString();
        }
    }
</script>

</body>
</html>
