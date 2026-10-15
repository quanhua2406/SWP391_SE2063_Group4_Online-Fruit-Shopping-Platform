<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Online Fruit Shopping</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/quanht/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        FreshFruit
    </div>

    <div class="search-box">
        <input type="text" placeholder="Search fresh fruits...">
        <button>Search</button>
    </div>

    <div class="nav-actions">

        <%
            com.mycompany.onlinefruitshopping.model.User currentUser =
                    (com.mycompany.onlinefruitshopping.model.User)
                    session.getAttribute("user");

            String dashboardUrl = null;
            String dashboardName = null;

            if (currentUser != null && currentUser.getRoleName() != null) {

                String role = currentUser.getRoleName()
                        .trim()
                        .toUpperCase();

                switch (role) {

                    case "ADMIN":
                        dashboardUrl =
                                request.getContextPath() + "/admin/dashboard";
                        dashboardName = "Admin Dashboard";
                        break;

                    case "SHOPOWNER":
                    case "SHOP_OWNER":
                        dashboardUrl =
                                request.getContextPath() + "/store-owner/dashboard";
                        dashboardName = "Shop Owner Dashboard";
                        break;

                    case "DELIVERYSTAFF":
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
        %>


        <!-- Gói Định Kỳ: cho phép cả khách vãng lai và thành viên xem -->
        <a href="${pageContext.request.contextPath}/subscriptions" class="nav-sub-link">
            📦 Gói Định Kỳ
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
            <c:if test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/my-subscriptions" class="nav-mysub-link">
                    📋 Gói Của Tôi
                </a>
            </c:if>

            <!-- My Account -->
            <a href="${pageContext.request.contextPath}/profile">
                My Account
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
                Logout
            </a>

        <% } %>

    </div>

</header>


<nav class="category-bar">

    <a href="#">All Fruits</a>
    <a href="#">Tropical Fruits</a>
    <a href="#">Citrus Fruits</a>
    <a href="#">Berries</a>
    <a href="#">Seasonal Fruits</a>
    <a href="${pageContext.request.contextPath}/subscriptions" class="category-sub-badge">
        📦 Gói Đăng Ký Định Kỳ (Combo)
    </a>

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
            <button class="shop-button">
                Shop Now
            </button>
            <a href="${pageContext.request.contextPath}/subscriptions" class="hero-sub-btn">
                🎁 Đăng Ký Gói Định Kỳ
            </a>
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
                ✨ Gói Trái Cây Chu Kỳ
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
                <c:if test="${not empty sessionScope.user}">
                    <a href="${pageContext.request.contextPath}/my-subscriptions" class="btn-sub-manage">
                        📋 Quản Lý Gói Của Tôi
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Featured Combo Preview Box -->
        <div class="subscription-card-box">
            <c:choose>
                <c:when test="${not empty featuredPlans}">
                    <c:set var="topPlan" value="${featuredPlans[0]}"/>
                    <div class="sub-plan-header">
                        <div>
                            <span class="sub-plan-tag">🔥 GÓI COMBO NỔI BẬT</span>
                            <h4 class="sub-top-plan-title">
                                ${topPlan.name}
                            </h4>
                            <span class="sub-top-shop-name">
                                🏪 Cửa hàng: <strong>${topPlan.shopName}</strong>
                            </span>
                        </div>
                    </div>
                    <div class="sub-top-price">
                        <fmt:formatNumber value="${topPlan.price}" pattern="#,###"/> ₫
                        <span class="sub-top-cycle"> / ${topPlan.billingCycleLabel}</span>
                    </div>
                    <p class="sub-top-desc">
                        ${topPlan.description}
                    </p>
                    <c:if test="${not empty topPlan.items}">
                        <div class="sub-top-items-box">
                            <strong class="sub-top-items-title">
                                🧺 Combo gồm ${topPlan.itemCount} loại trái cây tươi:
                            </strong>
                            <div class="sub-top-items-list">
                                <c:forEach var="item" items="${topPlan.items}">
                                    <span class="sub-item-chip">
                                        🍎 ${item.productName} <c:if test="${not empty item.variantName}">(${item.variantName})</c:if> <span class="sub-item-chip-qty">x${item.quantity}</span>
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
                            <span class="sub-plan-tag">🔥 COMBO GIA ĐÌNH</span>
                            <h4 class="sub-top-plan-title">
                                Combo Trái Cây Gia Đình Tươi Ngon
                            </h4>
                            <span class="sub-top-shop-name">
                                🏪 Cửa hàng: Trái Cây Sạch Bình Minh
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
                        📦 Gói Combo Đăng Ký Định Kỳ
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
                                    🏪 ${p.shopName}
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
                                        🧺 Gồm ${p.itemCount} loại trái cây:
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
                                    <fmt:formatNumber value="${p.price}" pattern="#,###"/> ₫
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

<section class="section">

    <h2>Popular Categories</h2>

    <div class="category-grid">

        <div class="category-card">
            🍎
            <h3>Apples</h3>
        </div>

        <div class="category-card">
            🍊
            <h3>Oranges</h3>
        </div>

        <div class="category-card">
            🍌
            <h3>Bananas</h3>
        </div>

        <div class="category-card">
            🍉
            <h3>Watermelon</h3>
        </div>

        <div class="category-card">
            🍇
            <h3>Grapes</h3>
        </div>

        <div class="category-card">
            🥭
            <h3>Mango</h3>
        </div>

    </div>

</section>


<section class="section">

    <div class="section-header">

        <h2>Featured Fruits</h2>

        <a href="#">
            View All →
        </a>

    </div>


    <div class="product-grid">


        <!-- Product 1 -->
        <div class="product-card">

            <div class="product-image">
                🍎
            </div>

            <h3>Fresh Red Apple</h3>

            <p class="product-desc">
                Sweet and crispy apples
            </p>

            <div class="product-bottom">

                <span class="price">
                    $3.99 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


        <!-- Product 2 -->
        <div class="product-card">

            <div class="product-image">
                🍊
            </div>

            <h3>Fresh Orange</h3>

            <p class="product-desc">
                Juicy and rich in vitamin C
            </p>

            <div class="product-bottom">

                <span class="price">
                    $2.99 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


        <!-- Product 3 -->
        <div class="product-card">

            <div class="product-image">
                🍇
            </div>

            <h3>Premium Grapes</h3>

            <p class="product-desc">
                Fresh seedless grapes
            </p>

            <div class="product-bottom">

                <span class="price">
                    $5.49 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


        <!-- Product 4 -->
        <div class="product-card">

            <div class="product-image">
                🥭
            </div>

            <h3>Fresh Mango</h3>

            <p class="product-desc">
                Sweet tropical mango
            </p>

            <div class="product-bottom">

                <span class="price">
                    $4.49 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


    </div>

</section>


<footer class="footer">

    <h3>🍊 FreshFruit</h3>

    <p>
        Fresh fruits. Healthy life.
    </p>

    <p>
        © 2026 Online Fruit Shopping
    </p>

</footer>


</body>
</html>