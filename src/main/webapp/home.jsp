<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<fmt:setLocale value="vi_VN"/>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Online Fruit Shopping</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/quanht/style.css">
    <style>
        .hero-actions {
            display: flex;
            align-items: center;
            gap: 14px;
            margin-top: 30px;
            flex-wrap: wrap;
        }
        .hero-actions a {
            margin: 0 !important;
            padding: 12px 26px !important;
            border-radius: 9999px !important;
            font-size: 15px !important;
            font-weight: 700 !important;
            text-decoration: none !important;
            display: inline-flex !important;
            align-items: center !important;
            justify-content: center !important;
            transition: all 0.25s ease !important;
            box-sizing: border-box !important;
            height: 48px !important;
            line-height: 1 !important;
            white-space: nowrap !important;
        }
        .hero-actions .shop-button {
            background: #ffffff !important;
            color: #15803d !important;
            border: 2px solid #ffffff !important;
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12) !important;
        }
        .hero-actions .shop-button:hover {
            background: #f1f5f9 !important;
            border-color: #f1f5f9 !important;
            transform: translateY(-2px) !important;
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18) !important;
        }
        .hero-actions .hero-sub-btn {
            background: rgba(255, 255, 255, 0.16) !important;
            color: #ffffff !important;
            border: 2px solid rgba(255, 255, 255, 0.85) !important;
            backdrop-filter: blur(4px) !important;
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08) !important;
        }
        .hero-actions .hero-sub-btn:hover {
            background: #ffffff !important;
            color: #15803d !important;
            border-color: #ffffff !important;
            transform: translateY(-2px) !important;
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18) !important;
        }
        .hero-actions .hero-owner-btn {
            background: #15803d !important;
            color: #ffffff !important;
            border: 2px solid rgba(255, 255, 255, 0.5) !important;
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12) !important;
        }
        .hero-actions .hero-owner-btn:hover {
            background: #166534 !important;
            border-color: #ffffff !important;
            transform: translateY(-2px) !important;
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18) !important;
        }
        .category-grid {
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            gap: 16px;
        }
        @media (max-width: 1024px) {
            .category-grid {
                grid-template-columns: repeat(3, 1fr);
            }
        }
        @media (max-width: 600px) {
            .category-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }
        .category-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            padding: 22px 14px 18px;
            text-align: center;
            transition: all 0.25s ease;
            cursor: pointer;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-decoration: none !important;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
        }
        .category-card:hover {
            transform: translateY(-4px);
            border-color: #86efac;
            box-shadow: 0 10px 24px rgba(22, 163, 74, 0.12);
        }
        .category-icon-wrap {
            width: 66px;
            height: 66px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 12px;
            transition: transform 0.25s ease;
        }
        .category-card:hover .category-icon-wrap {
            transform: scale(1.12);
        }
        .category-card h3 {
            font-size: 15px;
            font-weight: 700;
            color: #1e293b;
            margin: 0 0 6px;
            line-height: 1.35;
        }
        .category-card .category-product-count {
            font-size: 12px;
            color: #64748b;
            font-weight: 600;
        }
        .icon-apple { background: #fee2e2; }
        .icon-watermelon { background: #dcfce7; }
        .icon-basket { background: #fef3c7; }
        .icon-organic { background: #ecfccb; }
        .icon-dried { background: #ffedd5; }
        .icon-dried2 { background: #fef9c3; }
        .icon-grapes { background: #f3e8ff; }
    </style>
</head>

<body>

<header class="navbar">

    <div class="logo">
        <a href="${pageContext.request.contextPath}/home" style="color: #3d8b40; text-decoration: none;">FreshFruit</a>
    </div>

    <div class="search-box-wrapper" style="flex: 1; max-width: 520px; position: relative;">
        <form action="${pageContext.request.contextPath}/products" method="get" class="search-box" id="homeSearchForm">
            <input type="text" name="keyword" id="homeSearchInput" placeholder="Tìm kiếm trái cây..." autocomplete="off">
            <button type="submit" aria-label="Tìm kiếm">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
            </button>
        </form>
        <div class="search-suggestions-dropdown" id="homeSearchDropdown" style="position: absolute; top: 100%; left: 0; right: 0; background: #fff; border: 1px solid #cbd5e1; border-radius: 0 0 10px 10px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); max-height: 380px; overflow-y: auto; z-index: 1000; display: none;"></div>
    </div>

    <div class="nav-actions">

        <a href="${pageContext.request.contextPath}/products" style="color: #16a34a; font-weight: 700;">
            Khám Phá Trái Cây
        </a>

        <%
            com.mycompany.onlinefruitshopping.model.User currentUser =
                    (com.mycompany.onlinefruitshopping.model.User)
                    session.getAttribute("user");

            String dashboardUrl = null;
            String dashboardName = null;
            boolean isShopOwner = false;

            if (currentUser != null && currentUser.getRoleName() != null) {

                String role = currentUser.getRoleName()
                        .trim()
                        .toUpperCase();

                if (role.contains("SHOP") || role.contains("OWNER") || currentUser.getRoleID() == 2) {
                    isShopOwner = true;
                }

                switch (role) {

                    case "ADMIN":
                        dashboardUrl =
                                request.getContextPath() + "/admin/dashboard";
                        dashboardName = "Admin Dashboard";
                        break;

                    case "SHOPOWNER":
                    case "SHOP_OWNER":
                    case "SHOP OWNER":
                    case "STORE_OWNER":
                    case "STORE OWNER":
                        dashboardUrl =
                                request.getContextPath() + "/store-owner/dashboard";
                        dashboardName = "Shop Owner Dashboard";
                        break;

                    case "DELIVERYSTAFF":
                    case "DELIVERY_STAFF":
                        dashboardUrl =
                                request.getContextPath() + "/delivery/dashboard";
                        dashboardName = "Delivery Dashboard";
                        break;

                    case "CUSTOMER":
                        dashboardUrl =
                                request.getContextPath() + "/customer/dashboard";
                        dashboardName = "Customer Dashboard";
                        break;
                }
            }
            request.setAttribute("isShopOwner", isShopOwner);
        %>

        <!-- Gói Định Kỳ: cho phép cả khách vãng lai và thành viên xem -->
        <a href="${pageContext.request.contextPath}/subscriptions" class="nav-sub-link">
            Gói Định Kỳ
        </a>

        <!-- Cart: chỉ hiển thị icon giỏ hàng khi khách login -->
        <% if (currentUser != null) { %>
            <a href="${pageContext.request.contextPath}/cart" title="Giỏ hàng" style="display: inline-flex; align-items: center; gap: 6px;">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="9" cy="21" r="1"></circle>
                    <circle cx="20" cy="21" r="1"></circle>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                </svg>
                Giỏ hàng
            </a>
        <c:if test="${not isShopOwner and not empty sessionScope.user}">
            <a href="${pageContext.request.contextPath}/my-subscriptions" class="nav-mysub-link">
                Gói Của Tôi
            </a>
        </c:if>

            <!-- My Account -->
            <a href="${pageContext.request.contextPath}/profile">
                Tài khoản
            </a>
        <% } else { %>
            <a href="${pageContext.request.contextPath}/login">
                Đăng nhập
            </a>
        <% } %>


        <!-- Dashboard -->
        <% if (dashboardUrl != null) { %>

            <a href="<%= dashboardUrl %>">
                <%= dashboardName %>
            </a>

        <% } %>


        <!-- Logout -->
        <% if (currentUser != null) { %>

            <a class="logout"
               href="${pageContext.request.contextPath}/logout">
                Đăng xuất
            </a>

        <% } %>

    </div>

</header>


<nav class="category-bar">

    <a href="${pageContext.request.contextPath}/products" class="all-fruits-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="7" height="7"></rect>
            <rect x="14" y="3" width="7" height="7"></rect>
            <rect x="14" y="14" width="7" height="7"></rect>
            <rect x="3" y="14" width="7" height="7"></rect>
        </svg>
        Tất Cả Trái Cây
    </a>
    <c:forEach var="cat" items="${categories}">
        <c:if test="${cat.productCount > 0}">
            <a href="${pageContext.request.contextPath}/products?category=${cat.categoryId}">
                ${cat.categoryName}
            </a>
        </c:if>
    </c:forEach>
    <a href="${pageContext.request.contextPath}/subscriptions" class="category-sub-badge">
        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
            <line x1="12" y1="22.08" x2="12" y2="12"></line>
        </svg>
        Gói Đăng Ký Định Kỳ
    </a>
    <c:if test="${isShopOwner}">
        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" style="background: linear-gradient(135deg, #16a34a, #15803d); color: #fff; font-weight: 700; border: none; box-shadow: 0 2px 8px rgba(22,163,74,0.3);">
            + Tạo Gói Mới
        </a>
    </c:if>

</nav>


<section class="hero">

    <div class="hero-content">

        <p class="hero-small">
            FRESH • HEALTHY • NATURAL
        </p>

        <h1>
            Fresh Fruits<br>
            Delivered To Your Door
        </h1>

        <p>
            Enjoy fresh and delicious fruits every day.
            Quality products from trusted suppliers.
        </p>

        <div class="hero-actions">
            <a href="${pageContext.request.contextPath}/products" class="shop-button">
                Khám Phá Cửa Hàng
            </a>
            <a href="${pageContext.request.contextPath}/subscriptions" class="hero-sub-btn">
                Đăng Ký Gói Định Kỳ
            </a>
            <c:if test="${isShopOwner}">
                <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" class="hero-owner-btn">
                    + Tạo Gói Mới
                </a>
            </c:if>
        </div>

    </div>

</section>
<!-- ==========================================================================
     SUBSCRIPTION PROMO & COMBO SECTION
     ========================================================================== -->
<section class="section sub-promo-section">

    <div class="subscription-section">
        <div class="subscription-content">
            <span class="sub-badge-cycle">
                Gói Trái Cây Chu Kỳ
            </span>
            <h3>Nhận Trái Cây Tươi Ngon Định Kỳ<br>Giao Tận Cửa Hàng Tuần & Tháng</h3>
            <p>
                Đăng ký một lần để nạp vitamin đều đặn cho cả gia đình. Tiết kiệm chi phí lên đến 30%, nhận ưu đãi freeship và đổi trả 100% nếu trái cây không đạt chuẩn.
            </p>
            <div class="subscription-perks">
                <div class="perk-item">
                    <span class="icon">✓</span>
                    <span>Nguồn trái cây tuyển chọn loại 1 từ các chủ shop uy tín</span>
                </div>
                <div class="perk-item">
                    <span class="icon">✓</span>
                    <span>Tự động giao định kỳ đúng hẹn, linh hoạt đổi ngày hoặc tạm dừng</span>
                </div>
                <div class="perk-item">
                    <span class="icon">✓</span>
                    <span>Hội viên VIP có thể tự do tùy chỉnh danh sách loại quả trong combo</span>
                </div>
            </div>
            <div class="sub-action-group">
                <a href="${pageContext.request.contextPath}/subscriptions" class="btn-sub-explore">
                    Khám Phá Các Gói Combo →
                </a>
                <c:choose>
                    <c:when test="${isShopOwner}">
                        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" class="btn-sub-explore" style="background: linear-gradient(135deg, #16a34a, #15803d);">
                            Tạo Gói Định Kỳ Mới
                        </a>
                        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans" class="btn-sub-manage">
                            Quản Lý Gói Của Shop
                        </a>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${not empty sessionScope.user}">
                            <a href="${pageContext.request.contextPath}/my-subscriptions" class="btn-sub-manage">
                                Quản Lý Gói Của Tôi
                            </a>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Featured Combo Preview Box -->
        <div class="subscription-card-box">
            <c:choose>
                <c:when test="${not empty featuredPlans}">
                    <c:set var="topPlan" value="${featuredPlans[0]}"/>
                    <div class="sub-plan-header">
                        <div>
                            <span class="sub-plan-tag">GÓI COMBO NỔI BẬT</span>
                            <h4 class="sub-top-plan-title">
                                ${topPlan.name}
                            </h4>
                            <span class="sub-top-shop-name">
                                Cửa hàng: <strong>${topPlan.shopName}</strong>
                            </span>
                        </div>
                    </div>
                    <div class="sub-top-price">
                        <fmt:formatNumber value="${topPlan.price}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                        <span class="sub-top-cycle"> / ${topPlan.billingCycleLabel}</span>
                    </div>
                    <p class="sub-top-desc">
                        ${topPlan.description}
                    </p>
                    <c:if test="${not empty topPlan.items}">
                        <div class="sub-top-items-box">
                            <strong class="sub-top-items-title">
                                Combo gồm ${topPlan.itemCount} loại trái cây tươi:
                            </strong>
                            <div class="sub-top-items-list">
                                <c:forEach var="item" items="${topPlan.items}">
                                    <span class="sub-item-chip">
                                        ${item.productName} <c:if test="${not empty item.variantName}">(${item.variantName})</c:if> <span class="sub-item-chip-qty">x${item.quantity}</span>
                                    </span>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/subscription/subscribe?planId=${topPlan.planId}" class="sub-top-btn-cta">
                        Đăng Ký Gói Này Ngay →
                    </a>
                </c:when>
                <c:otherwise>
                    <div class="sub-plan-header">
                        <div>
                            <span class="sub-plan-tag">COMBO GIA ĐÌNH</span>
                            <h4 class="sub-top-plan-title">
                                Combo Trái Cây Gia Đình Tươi Ngon
                            </h4>
                            <span class="sub-top-shop-name">
                                Cửa hàng: Trái Cây Sạch Bình Minh
                            </span>
                        </div>
                    </div>
                    <div class="sub-top-price">
                        199.000 ₫ <span class="sub-top-cycle">/ Tuần</span>
                    </div>
                    <p class="sub-top-desc">
                        Gồm Táo Envy, Cam Sành Tiền Giang, Nho Móng Tay. Giao tận nơi định kỳ vào thứ Hai hàng tuần!
                    </p>
                    <a href="${pageContext.request.contextPath}/subscriptions" class="sub-top-btn-cta">
                        Khám Phá Các Gói Combo →
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Active Combo Plans Grid -->
    <c:if test="${not empty featuredPlans}">
        <div class="sub-grid-wrapper">
            <div class="sub-grid-header">
                <div>
                    <h2 class="sub-grid-title">
                        Gói Combo Đăng Ký Định Kỳ
                    </h2>
                    <p class="sub-grid-subtitle">
                        Lựa chọn combo trái cây tươi theo tuần hoặc tháng phù hợp với nhu cầu gia đình bạn
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/subscriptions" class="sub-grid-more-link">
                    Xem tất cả gói →
                </a>
            </div>

            <div class="sub-plans-grid">
                <c:forEach var="p" items="${featuredPlans}">
                    <div class="sub-plan-card">
                        <div>
                            <div class="sub-card-top">
                                <span class="sub-card-cycle">
                                    ${p.billingCycleLabel}
                                </span>
                                <span class="sub-card-shop">
                                    ${p.shopName}
                                </span>
                            </div>

                            <h3 class="sub-card-title">
                                <a href="${pageContext.request.contextPath}/subscription/detail?planId=${p.planId}">
                                    ${p.name}
                                </a>
                            </h3>

                            <p class="sub-card-desc">
                                ${p.description}
                            </p>

                            <c:if test="${not empty p.items}">
                                <div class="sub-card-items-box">
                                    <div class="sub-card-items-title">
                                        Gồm ${p.itemCount} loại trái cây:
                                    </div>
                                    <div class="sub-card-items-list">
                                        <c:forEach var="it" items="${p.items}">
                                            <span class="sub-card-item-tag">
                                                ${it.productName} <c:if test="${not empty it.variantName}">(${it.variantName})</c:if> <span class="sub-card-item-qty">x${it.quantity}</span>
                                            </span>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:if>
                        </div>

                        <div class="sub-card-footer">
                            <div>
                                <span class="sub-card-price-label">Giá mỗi kỳ</span>
                                <span class="sub-card-price">
                                    <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </span>
                            </div>
                            <a href="${pageContext.request.contextPath}/subscription/subscribe?planId=${p.planId}" class="sub-card-btn-subscribe">
                                Đăng Ký →
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </c:if>

</section>

<!-- Popular Categories -->
<section class="section">

    <h2>Danh Mục Trái Cây Phổ Biến</h2>

    <div class="category-grid">
        <c:choose>
            <c:when test="${not empty categories}">
                <c:forEach var="c" items="${categories}">
                    <a href="${pageContext.request.contextPath}/products?category=${c.categoryId}" class="category-card">
                        <c:choose>
                            <%-- 1. Trái Cây Nhập Khẩu -> Táo Đỏ (Apple) --%>
                            <c:when test="${fn:contains(c.categoryName, 'Nhập Khẩu')}">
                                <div class="category-icon-wrap icon-apple">
                                    <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18 10C18 6.5 19.5 4 21 3.5" stroke="#78350f" stroke-width="2" stroke-linecap="round"/>
                                        <path d="M18 7C22 5.5 25 7 25 9C23 11 19.5 9 18 7Z" fill="#22c55e" stroke="#15803d" stroke-width="1.2"/>
                                        <path d="M18 12C16.2 10.5 13 10 10.5 11.5C6.5 13.8 5 19 7 24C8.8 28.5 12.5 31.5 15.5 31.5C17 31.5 17.5 30.5 18 30.5C18.5 30.5 19 31.5 20.5 31.5C23.5 31.5 27.2 28.5 29 24C31 19 29.5 13.8 25.5 11.5C23 10 19.8 10.5 18 12Z" fill="#ef4444" stroke="#b91c1c" stroke-width="1.5" stroke-linejoin="round"/>
                                        <path d="M10 15C8.8 17 8.5 19.5 9.2 22" stroke="#fca5a5" stroke-width="1.5" stroke-linecap="round"/>
                                    </svg>
                                </div>
                            </c:when>
                            <%-- 2. Trái Cây Nội Địa -> Dưa Hấu (Watermelon slice) --%>
                            <c:when test="${fn:contains(c.categoryName, 'Nội Địa')}">
                                <div class="category-icon-wrap icon-watermelon">
                                    <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M4 14C4 22.8 11.2 30 20 30C28.8 30 36 22.8 36 14H4Z" fill="#15803d" stroke="#166534" stroke-width="1.5"/>
                                        <path d="M6 14C6 21.7 12.3 28 20 28C27.7 28 34 21.7 34 14H6Z" fill="#bbf7d0"/>
                                        <path d="M7.5 14C7.5 20.9 13.1 26.5 20 26.5C26.9 26.5 32.5 20.9 32.5 14H7.5Z" fill="#ef4444"/>
                                        <ellipse cx="14" cy="18" rx="1" ry="1.6" fill="#1e293b" transform="rotate(-15 14 18)"/>
                                        <ellipse cx="20" cy="20" rx="1" ry="1.6" fill="#1e293b"/>
                                        <ellipse cx="26" cy="18" rx="1" ry="1.6" fill="#1e293b" transform="rotate(15 26 18)"/>
                                        <ellipse cx="17" cy="16" rx="0.8" ry="1.3" fill="#1e293b"/>
                                        <ellipse cx="23" cy="16" rx="0.8" ry="1.3" fill="#1e293b"/>
                                    </svg>
                                </div>
                            </c:when>
                            <%-- 3. Giỏ Quà Trái Cây -> Giỏ Quà Hoa Quả (Fruit Gift Basket) --%>
                            <c:when test="${fn:contains(c.categoryName, 'Giỏ Quà')}">
                                <div class="category-icon-wrap icon-basket">
                                    <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M10 16V11C10 6.6 13.6 3 18 3C22.4 3 26 6.6 26 11V16" stroke="#b45309" stroke-width="2" stroke-linecap="round"/>
                                        <circle cx="18" cy="4" r="2.5" fill="#f59e0b" stroke="#d97706" stroke-width="1"/>
                                        <circle cx="13" cy="14" r="4.5" fill="#ef4444" stroke="#b91c1c" stroke-width="1"/>
                                        <circle cx="23" cy="14" r="4.5" fill="#f97316" stroke="#c2410c" stroke-width="1"/>
                                        <circle cx="18" cy="12" r="4" fill="#84cc16" stroke="#4d7c0f" stroke-width="1"/>
                                        <path d="M18 8C20 6 22 7 21 9C19.5 9 18.5 8.5 18 8Z" fill="#22c55e"/>
                                        <path d="M6 16H30L27.5 29.5C27.2 30.9 26 32 24.6 32H11.4C10 32 8.8 30.9 8.5 29.5L6 16Z" fill="#fef3c7" stroke="#b45309" stroke-width="1.8"/>
                                        <path d="M7 21H29M8.5 26.5H27.5" stroke="#d97706" stroke-width="1.2"/>
                                        <path d="M13 16L12 32M18 16V32M23 16L24 32" stroke="#d97706" stroke-width="1.2"/>
                                    </svg>
                                </div>
                            </c:when>
                            <%-- 4. Trái Cây Hữu Cơ (Organic) -> Quả Lê & Mầm Xanh (Organic Sprout) --%>
                            <c:when test="${fn:contains(c.categoryName, 'Hữu Cơ') || fn:contains(c.categoryName, 'Organic')}">
                                <div class="category-icon-wrap icon-organic">
                                    <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18 8C18 5 19 3 20 2" stroke="#78350f" stroke-width="1.8" stroke-linecap="round"/>
                                        <path d="M18 5C22 3 25 5 24 8C21.5 8 19 6.5 18 5Z" fill="#22c55e" stroke="#15803d" stroke-width="1"/>
                                        <path d="M18 6C14 4 12 6 13 8.5C15 8.8 17 7.5 18 6Z" fill="#4ade80" stroke="#15803d" stroke-width="1"/>
                                        <path d="M18 9C15 9 13.5 12 12.5 15C11 19.5 7.5 21.5 7.5 25.5C7.5 29.5 11.5 33 18 33C24.5 33 28.5 29.5 28.5 25.5C28.5 21.5 25 19.5 23.5 15C22.5 12 21 9 18 9Z" fill="#a3e635" stroke="#65a30d" stroke-width="1.5" stroke-linejoin="round"/>
                                        <path d="M15 22C16.5 25 19.5 26 22 25" stroke="#84cc16" stroke-width="1.5" stroke-linecap="round"/>
                                    </svg>
                                </div>
                            </c:when>
                            <%-- 5. Trái Cây Sấy Khô -> Lát Hoa Quả Sấy Giòn --%>
                            <c:when test="${fn:contains(c.categoryName, 'Sấy Khô')}">
                                <div class="category-icon-wrap icon-dried2">
                                    <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <ellipse cx="15" cy="16" rx="9" ry="7" fill="#fde68a" stroke="#d97706" stroke-width="1.5" transform="rotate(-20 15 16)"/>
                                        <ellipse cx="22" cy="20" rx="9" ry="7" fill="#fef08a" stroke="#ca8a04" stroke-width="1.5" transform="rotate(25 22 20)"/>
                                        <path d="M11 16C13 14 17 15 19 17" stroke="#b45309" stroke-width="1.2" stroke-linecap="round"/>
                                        <path d="M18 20C20 18 24 19 26 21" stroke="#a16207" stroke-width="1.2" stroke-linecap="round"/>
                                        <circle cx="15" cy="15" r="1" fill="#b45309"/>
                                        <circle cx="22" cy="19" r="1" fill="#a16207"/>
                                    </svg>
                                </div>
                            </c:when>
                            <%-- 6. Trái Cây Sấy -> Lát Hoa Quả Sấy Dẻo --%>
                            <c:when test="${fn:contains(c.categoryName, 'Sấy')}">
                                <div class="category-icon-wrap icon-dried">
                                    <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="18" cy="18" r="14" fill="#ffedd5" stroke="#ea580c" stroke-width="2"/>
                                        <circle cx="18" cy="18" r="10" fill="#fed7aa" stroke="#c2410c" stroke-width="1.5" stroke-dasharray="2 2"/>
                                        <path d="M18 8V14M18 22V28M8 18H14M22 18H28M11 11L15 15M21 21L25 25M11 25L15 21M21 15L25 11" stroke="#ea580c" stroke-width="1.5" stroke-linecap="round"/>
                                        <circle cx="18" cy="18" r="2.8" fill="#c2410c"/>
                                    </svg>
                                </div>
                            </c:when>
                            <%-- 7. Danh Mục Khác -> Chùm Nho Tươi (Fresh Grapes) --%>
                            <c:otherwise>
                                <div class="category-icon-wrap icon-grapes">
                                    <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M18 3V7" stroke="#78350f" stroke-width="2" stroke-linecap="round"/>
                                        <path d="M18 6C22 4.5 24 6 23 8.5C21 9 19.5 8 18 6Z" fill="#22c55e" stroke="#15803d" stroke-width="1"/>
                                        <circle cx="14" cy="11" r="3.5" fill="#c084fc" stroke="#7e22ce" stroke-width="1.2"/>
                                        <circle cx="22" cy="11" r="3.5" fill="#c084fc" stroke="#7e22ce" stroke-width="1.2"/>
                                        <circle cx="10.5" cy="16.5" r="3.5" fill="#a855f7" stroke="#7e22ce" stroke-width="1.2"/>
                                        <circle cx="18" cy="16.5" r="3.5" fill="#9333ea" stroke="#6b21a8" stroke-width="1.2"/>
                                        <circle cx="25.5" cy="16.5" r="3.5" fill="#a855f7" stroke="#7e22ce" stroke-width="1.2"/>
                                        <circle cx="14" cy="22.5" r="3.5" fill="#9333ea" stroke="#6b21a8" stroke-width="1.2"/>
                                        <circle cx="22" cy="22.5" r="3.5" fill="#9333ea" stroke="#6b21a8" stroke-width="1.2"/>
                                        <circle cx="18" cy="28" r="3" fill="#7e22ce" stroke="#581c87" stroke-width="1.2"/>
                                    </svg>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <h3>${c.categoryName}</h3>
                        <span class="category-product-count">${c.productCount} sản phẩm</span>
                    </a>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/products" class="category-card">
                    <div class="category-icon-wrap icon-apple">
                        <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M18 10C18 6.5 19.5 4 21 3.5" stroke="#78350f" stroke-width="2" stroke-linecap="round"/>
                            <path d="M18 7C22 5.5 25 7 25 9C23 11 19.5 9 18 7Z" fill="#22c55e" stroke="#15803d" stroke-width="1.2"/>
                            <path d="M18 12C16.2 10.5 13 10 10.5 11.5C6.5 13.8 5 19 7 24C8.8 28.5 12.5 31.5 15.5 31.5C17 31.5 17.5 30.5 18 30.5C18.5 30.5 19 31.5 20.5 31.5C23.5 31.5 27.2 28.5 29 24C31 19 29.5 13.8 25.5 11.5C23 10 19.8 10.5 18 12Z" fill="#ef4444" stroke="#b91c1c" stroke-width="1.5" stroke-linejoin="round"/>
                            <path d="M10 15C8.8 17 8.5 19.5 9.2 22" stroke="#fca5a5" stroke-width="1.5" stroke-linecap="round"/>
                        </svg>
                    </div>
                    <h3>Trái Cây Nhập Khẩu</h3>
                    <span class="category-product-count">Khám phá ngay</span>
                </a>
                <a href="${pageContext.request.contextPath}/products" class="category-card">
                    <div class="category-icon-wrap icon-watermelon">
                        <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M4 14C4 22.8 11.2 30 20 30C28.8 30 36 22.8 36 14H4Z" fill="#15803d" stroke="#166534" stroke-width="1.5"/>
                            <path d="M6 14C6 21.7 12.3 28 20 28C27.7 28 34 21.7 34 14H6Z" fill="#bbf7d0"/>
                            <path d="M7.5 14C7.5 20.9 13.1 26.5 20 26.5C26.9 26.5 32.5 20.9 32.5 14H7.5Z" fill="#ef4444"/>
                            <ellipse cx="14" cy="18" rx="1" ry="1.6" fill="#1e293b" transform="rotate(-15 14 18)"/>
                            <ellipse cx="20" cy="20" rx="1" ry="1.6" fill="#1e293b"/>
                            <ellipse cx="26" cy="18" rx="1" ry="1.6" fill="#1e293b" transform="rotate(15 26 18)"/>
                        </svg>
                    </div>
                    <h3>Trái Cây Nội Địa</h3>
                    <span class="category-product-count">Khám phá ngay</span>
                </a>
                <a href="${pageContext.request.contextPath}/products" class="category-card">
                    <div class="category-icon-wrap icon-basket">
                        <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M10 16V11C10 6.6 13.6 3 18 3C22.4 3 26 6.6 26 11V16" stroke="#b45309" stroke-width="2" stroke-linecap="round"/>
                            <circle cx="18" cy="4" r="2.5" fill="#f59e0b" stroke="#d97706" stroke-width="1"/>
                            <circle cx="13" cy="14" r="4.5" fill="#ef4444" stroke="#b91c1c" stroke-width="1"/>
                            <circle cx="23" cy="14" r="4.5" fill="#f97316" stroke="#c2410c" stroke-width="1"/>
                            <circle cx="18" cy="12" r="4" fill="#84cc16" stroke="#4d7c0f" stroke-width="1"/>
                            <path d="M6 16H30L27.5 29.5C27.2 30.9 26 32 24.6 32H11.4C10 32 8.8 30.9 8.5 29.5L6 16Z" fill="#fef3c7" stroke="#b45309" stroke-width="1.8"/>
                        </svg>
                    </div>
                    <h3>Giỏ Quà Cao Cấp</h3>
                    <span class="category-product-count">Khám phá ngay</span>
                </a>
                <a href="${pageContext.request.contextPath}/products" class="category-card">
                    <div class="category-icon-wrap icon-organic">
                        <svg viewBox="0 0 36 36" width="38" height="38" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M18 8C18 5 19 3 20 2" stroke="#78350f" stroke-width="1.8" stroke-linecap="round"/>
                            <path d="M18 5C22 3 25 5 24 8C21.5 8 19 6.5 18 5Z" fill="#22c55e" stroke="#15803d" stroke-width="1"/>
                            <path d="M18 9C15 9 13.5 12 12.5 15C11 19.5 7.5 21.5 7.5 25.5C7.5 29.5 11.5 33 18 33C24.5 33 28.5 29.5 28.5 25.5C28.5 21.5 25 19.5 23.5 15C22.5 12 21 9 18 9Z" fill="#a3e635" stroke="#65a30d" stroke-width="1.5" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <h3>Trái Cây Hữu Cơ</h3>
                    <span class="category-product-count">Khám phá ngay</span>
                </a>
            </c:otherwise>
        </c:choose>
    </div>

</section>


<!-- Featured Fruits -->
<section class="section">

    <div class="section-header">
        <h2>Trái Cây Tươi Nổi Bật</h2>
        <a href="${pageContext.request.contextPath}/products" style="color: #16a34a; font-weight: 700; text-decoration: none;">
            Xem tất cả trái cây →
        </a>
    </div>

    <div class="product-grid">
        <c:choose>
            <c:when test="${not empty featuredProducts}">
                <c:forEach var="p" items="${featuredProducts}">
                    <div class="product-card" style="display: flex; flex-direction: column;">
                        <div class="product-image" style="height: 180px; overflow: hidden; padding: 0; position: relative;">
                            <img src="${p.imageUrl}" alt="${p.productName}" style="width: 100%; height: 100%; object-fit: cover;"
                                 onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500';">
                            <span style="position: absolute; top: 8px; left: 8px; background: rgba(0,0,0,0.7); color: #fff; font-size: 11px; font-weight: 600; padding: 3px 8px; border-radius: 4px;">
                                ${p.origin}
                            </span>
                        </div>

                        <div style="padding: 14px; display: flex; flex-direction: column; flex: 1;">
                            <span style="font-size: 11.5px; color: #64748b; font-weight: 600; text-transform: uppercase;">${p.categoryName}</span>
                            <h3 style="font-size: 16px; font-weight: 700; margin: 4px 0 8px; line-height: 1.3;">
                                <a href="${pageContext.request.contextPath}/product?id=${p.productId}" style="color: #0f172a; text-decoration: none;">
                                    ${p.productName}
                                </a>
                            </h3>

                            <p class="product-desc" style="font-size: 12.5px; color: #64748b; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 12px; flex: 1;">
                                ${p.description}
                            </p>

                            <div class="product-bottom" style="margin-top: auto; display: flex; align-items: center; justify-content: space-between;">
                                <div>
                                    <span class="price" style="font-size: 16px; font-weight: 800; color: #15803d;">
                                        <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </span>
                                    <div style="font-size: 11.5px; color: #64748b;">/ ${p.packagingType}</div>
                                </div>

                                <a href="${pageContext.request.contextPath}/product?id=${p.productId}" class="shop-button" style="padding: 7px 12px; font-size: 12.5px; text-decoration: none; border-radius: 6px;">
                                    Chi tiết
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <p style="grid-column: 1 / -1; text-align: center; color: #64748b; padding: 30px;">
                    Đang nạp dữ liệu trái cây tươi...
                </p>
            </c:otherwise>
        </c:choose>
    </div>

</section>


<footer class="footer">

    <h3>FreshFruit Vietnam</h3>

    <p>
        Trái cây tươi ngon - Nguồn dinh dưỡng tự nhiên mỗi ngày
    </p>

    <p>
        &copy; 2026 Online Fruit Shopping. All rights reserved.
    </p>

</footer>

<!-- Live Autocomplete Script for Home Search -->
<script>
    (function() {
        const input = document.getElementById('homeSearchInput');
        const dropdown = document.getElementById('homeSearchDropdown');
        let timer = null;

        if (!input || !dropdown) return;

        input.addEventListener('input', function() {
            const q = this.value.trim();
            clearTimeout(timer);

            if (q.length < 2) {
                dropdown.style.display = 'none';
                dropdown.innerHTML = '';
                return;
            }

            timer = setTimeout(() => {
                fetch('${pageContext.request.contextPath}/api/products/search-suggestions?q=' + encodeURIComponent(q))
                    .then(r => r.json())
                    .then(data => {
                        if (data && data.length > 0) {
                            dropdown.innerHTML = '';
                            data.forEach(item => {
                                const a = document.createElement('a');
                                a.href = '${pageContext.request.contextPath}/product?id=' + item.id;
                                a.style.display = 'flex';
                                a.style.alignItems = 'center';
                                a.style.gap = '12px';
                                a.style.padding = '10px 14px';
                                a.style.borderBottom = '1px solid #f1f5f9';
                                a.style.textDecoration = 'none';
                                a.style.color = '#1e293b';

                                const formattedPrice = new Intl.NumberFormat('vi-VN').format(item.price) + ' ₫';
                                const img = item.imageUrl || 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500';

                                a.innerHTML = 
                                    '<img src="' + img + '" style="width: 42px; height: 42px; border-radius: 6px; object-fit: cover;">' +
                                    '<div style="flex: 1; min-width: 0;">' +
                                        '<div style="font-size: 13.5px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">' + item.name + '</div>' +
                                        '<div style="font-size: 12px; color: #64748b;">' + item.category + ' • ' + item.origin + '</div>' +
                                    '</div>' +
                                    '<div style="font-size: 13px; font-weight: 700; color: #15803d; white-space: nowrap;">' + formattedPrice + '</div>';

                                dropdown.appendChild(a);
                            });
                            dropdown.style.display = 'block';
                        } else {
                            dropdown.style.display = 'none';
                        }
                    })
                    .catch(() => {
                        dropdown.style.display = 'none';
                    });
            }, 250);
        });

        document.addEventListener('click', function(e) {
            if (!input.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.style.display = 'none';
            }
        });
    })();
</script>

</body>
</html>