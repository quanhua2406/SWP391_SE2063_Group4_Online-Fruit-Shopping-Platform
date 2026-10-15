<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Gói Trái Cây Định Kỳ | FreshFruit Marketplace</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css"/>
</head>
<body class="customer-plans-page">

<!-- Global Navigation Bar -->
<header class="cust-navbar">
    <a href="${pageContext.request.contextPath}/home" class="brand">
        🍊 <span>FreshFruit</span>
    </a>
    <nav class="nav-links">
        <a href="${pageContext.request.contextPath}/home" class="nav-link">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/subscriptions" class="nav-link active">📦 Gói Trái Cây Định Kỳ</a>
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/my-subscriptions" class="nav-link">📋 Gói của tôi</a>
                <a href="${pageContext.request.contextPath}/profile" class="nav-link">👤 ${sessionScope.user.fullName}</a>
                <a href="${pageContext.request.contextPath}/logout" class="nav-link" style="color:#ef4444;">Đăng xuất</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login" class="btn-nav-primary">Đăng nhập</a>
            </c:otherwise>
        </c:choose>
    </nav>
</header>

<!-- Toast Notifications -->
<c:if test="${not empty sessionScope.toastSuccess}">
    <div class="toast success" id="toast" style="display:block; animation: slideUpToast 0.3s ease;">
        ✅ ${sessionScope.toastSuccess}
    </div>
    <c:remove var="toastSuccess" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.toastError}">
    <div class="toast error" id="toast" style="display:block; animation: slideUpToast 0.3s ease;">
        ⚠️ ${sessionScope.toastError}
    </div>
    <c:remove var="toastError" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.toastInfo}">
    <div class="toast" id="toast" style="display:block; background:#0284c7; color:#fff; animation: slideUpToast 0.3s ease;">
        ℹ️ ${sessionScope.toastInfo}
    </div>
    <c:remove var="toastInfo" scope="session"/>
</c:if>

<div class="customer-container">

    <!-- Hero Section -->
    <section class="plans-hero">
        <div class="plans-hero-badge">✨ GIẢI PHÁP DINH DƯỠNG THÔNG MINH</div>
        <h1>Gói Combo Trái Cây Định Kỳ</h1>
        <p>
            Trái cây tươi ngon chuẩn sạch từ các nhà vườn uy tín, được giao tận cửa mỗi tuần hoặc mỗi tháng.
            Tiết kiệm chi phí, chủ động thời gian, linh hoạt tạm dừng hoặc đổi lịch giao bất cứ lúc nào!
        </p>
        <div class="plans-hero-perks">
            <div class="hero-perk-item">
                <span>🍇</span> 100% Trái cây tuyển chọn loại 1
            </div>
            <div class="hero-perk-item">
                <span>🚚</span> Miễn phí giao hàng định kỳ
            </div>
            <div class="hero-perk-item">
                <span>💰</span> Tiết kiệm đến 25% so với mua lẻ
            </div>
            <div class="hero-perk-item">
                <span>🔄</span> Tự do tạm dừng hoặc hủy bất kỳ lúc nào
            </div>
        </div>
    </section>

    <!-- Search & Filter Bar -->
    <form action="${pageContext.request.contextPath}/subscriptions" method="GET" class="plans-filter-bar">
        <div class="filter-search-box">
            <span style="margin-right:8px; opacity:0.6;">🔍</span>
            <input type="text" name="q" value="<c:out value='${keyword}'/>" placeholder="Tìm tên combo, loại trái cây mong muốn..."/>
        </div>

        <div class="filter-selects">
            <!-- Filter Shop -->
            <select name="shopId">
                <option value="">🏬 Tất cả Cửa hàng / Nhà vườn</option>
                <c:forEach var="s" items="${shopList}">
                    <option value="${s.shopId}" ${selectedShopId == s.shopId ? 'selected' : ''}>
                        ${s.shopName}
                    </option>
                </c:forEach>
            </select>

            <!-- Filter Billing Cycle -->
            <select name="cycle">
                <option value="">⏱️ Mọi chu kỳ giao</option>
                <option value="weekly" ${selectedCycle == 'weekly' ? 'selected' : ''}>Hàng tuần (Weekly)</option>
                <option value="monthly" ${selectedCycle == 'monthly' ? 'selected' : ''}>Hàng tháng (Monthly)</option>
                <option value="quarterly" ${selectedCycle == 'quarterly' ? 'selected' : ''}>Hàng quý (Quarterly)</option>
            </select>

            <button type="submit" class="filter-btn-apply">Lọc Gói</button>
            <c:if test="${not empty selectedShopId or not empty selectedCycle or not empty keyword}">
                <a href="${pageContext.request.contextPath}/subscriptions"
                   style="color:#64748b; font-size:0.85rem; text-decoration:none; display:flex; align-items:center;">
                    ✖ Xóa lọc
                </a>
            </c:if>
        </div>
    </form>

    <!-- Results Header -->
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
        <h2 style="font-size:1.25rem; font-weight:800; color:var(--text-title); margin:0;">
            Các Gói Combo Đang Mở Đăng Ký (${totalPlans})
        </h2>
        <c:if test="${not empty sessionScope.user}">
            <a href="${pageContext.request.contextPath}/my-subscriptions" style="color:var(--primary-800); font-weight:700; font-size:0.9rem; text-decoration:none;">
                Xem các gói bạn đã đăng ký →
            </a>
        </c:if>
    </div>

    <!-- Combo Plans Cards Grid -->
    <c:choose>
        <c:when test="${empty plans}">
            <div class="cust-empty-state">
                <div class="icon">🍍</div>
                <h3>Không tìm thấy gói combo phù hợp</h3>
                <p>Hiện chưa có gói combo nào khớp với bộ lọc của bạn. Hãy thử từ khóa khác hoặc xóa bộ lọc.</p>
                <a href="${pageContext.request.contextPath}/subscriptions" class="filter-btn-apply" style="text-decoration:none; display:inline-block;">
                    Xem tất cả các gói
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="cust-plans-grid">
                <c:forEach var="p" items="${plans}">
                    <c:set var="sav" value="${savingsMap[p.planId]}"/>
                    <div class="cust-plan-card">
                        <div>
                            <!-- Header Badges -->
                            <div class="cust-plan-badge-top">
                                <span class="badge-shop">🏬 ${p.shopName != null ? p.shopName : 'Shop'}</span>
                                <c:if test="${sav != null and sav.savingsPercent > 0}">
                                    <span class="badge-save">🔥 Tiết kiệm ${sav.savingsPercent}%</span>
                                </c:if>
                            </div>

                            <!-- Plan Title & Description -->
                            <h3 class="cust-plan-title">${p.name}</h3>
                            <p class="cust-plan-desc">${p.description}</p>

                            <!-- Fruit Items inside combo -->
                            <div class="combo-items-box">
                                <div class="combo-items-label">
                                    <span>🧺</span> Trái cây có trong combo (${p.items != null ? p.items.size() : 0} món):
                                </div>
                                <div class="combo-items-list">
                                    <c:choose>
                                        <c:when test="${not empty p.items}">
                                            <c:forEach var="item" items="${p.items}">
                                                <div class="combo-item-row">
                                                    <span>• ${item.productName}<c:if test="${not empty item.variantName}"> (${item.variantName})</c:if></span>
                                                    <span class="qty-badge">x${item.quantity}</span>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="font-size:0.84rem; color:var(--text-muted); font-style:italic;">
                                                Combo đa dạng theo mùa
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <!-- Benefits chips -->
                            <div class="plan-benefits-chips">
                                <span class="benefit-chip">📅 Chu kỳ: ${p.billingCycleLabel}</span>
                                <span class="benefit-chip">⏳ Hiệu lực: ${p.durationDays} ngày</span>
                                <span class="benefit-chip">🚚 Freeship</span>
                            </div>
                        </div>

                        <!-- Pricing & CTA -->
                        <div class="cust-plan-pricing">
                            <div class="price-display-group">
                                <c:if test="${sav != null and sav.originalPrice > 0 and sav.savingsAmount > 0}">
                                    <div class="orig-price">
                                        <fmt:formatNumber value="${sav.originalPrice}" pattern="#,###"/>đ
                                    </div>
                                </c:if>
                                <div class="main-price">
                                    <fmt:formatNumber value="${p.price}" pattern="#,###"/>đ
                                    <span class="cycle">/${p.billingCycleLabel}</span>
                                </div>
                            </div>
                            <div style="display:flex; gap:8px;">
                                <a href="${pageContext.request.contextPath}/subscription/detail?id=${p.planId}"
                                   class="btn-sub-action"
                                   style="background:#f1f5f9; color:#475569; text-decoration:none; padding:10px 12px;">
                                    Chi tiết
                                </a>
                                <a href="${pageContext.request.contextPath}/subscription/subscribe?planId=${p.planId}"
                                   class="btn-subscribe-cta">
                                    Đăng ký ⚡
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- Pagination -->
            <c:if test="${totalPages > 1}">
                <div class="pagination" style="display:flex; justify-content:center; gap:8px; margin-top:30px;">
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <a href="?page=${i}&shopId=${selectedShopId}&cycle=${selectedCycle}&q=${keyword}"
                           class="page-link ${currentPage == i ? 'active' : ''}"
                           style="padding:8px 14px; border-radius:8px; font-weight:700; text-decoration:none;
                                  ${currentPage == i ? 'background:var(--primary-800); color:#fff;' : 'background:#fff; color:var(--text-body); border:1px solid var(--border-medium);'}">
                            ${i}
                        </a>
                    </c:forEach>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>

    <!-- How Subscription Works Section -->
    <section style="margin-top:60px; background:#ffffff; border-radius:var(--radius-lg); padding:36px; border:1px solid var(--border-light); box-shadow:var(--shadow-sm);">
        <h2 style="text-align:center; font-size:1.6rem; font-weight:800; color:var(--text-title); margin:0 0 10px;">
            Trải Nghiệm Nhận Trái Cây Định Kỳ Cực Kỳ Đơn Giản
        </h2>
        <p style="text-align:center; color:var(--text-muted); margin:0 0 36px;">
            Chỉ với 3 bước đơn giản, bạn và gia đình luôn có sẵn nguồn vitamin tươi ngon mỗi ngày.
        </p>
        <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(260px, 1fr)); gap:28px;">
            <div style="text-align:center; padding:16px;">
                <div style="width:64px; height:64px; border-radius:50%; background:#E8F5E9; color:#15803d; font-size:1.8rem; display:flex; align-items:center; justify-content:center; margin:0 auto 16px;">
                    1
                </div>
                <h4 style="font-size:1.1rem; font-weight:800; margin:0 0 8px;">Chọn Gói Combo Yêu Thích</h4>
                <p style="font-size:0.9rem; color:var(--text-muted); line-height:1.5;">
                    Lựa chọn combo trái cây phù hợp khẩu vị và số lượng người dùng từ nhà vườn bạn tin cậy.
                </p>
            </div>
            <div style="text-align:center; padding:16px;">
                <div style="width:64px; height:64px; border-radius:50%; background:#FEF3C7; color:#B45309; font-size:1.8rem; display:flex; align-items:center; justify-content:center; margin:0 auto 16px;">
                    2
                </div>
                <h4 style="font-size:1.1rem; font-weight:800; margin:0 0 8px;">Chọn Lịch Giao & Địa Chỉ</h4>
                <p style="font-size:0.9rem; color:var(--text-muted); line-height:1.5;">
                    Chọn ngày bắt đầu nhận hàng đầu tiên. Hệ thống sẽ tự động lên lịch giao đều đặn theo chu kỳ tuần/tháng.
                </p>
            </div>
            <div style="text-align:center; padding:16px;">
                <div style="width:64px; height:64px; border-radius:50%; background:#E0F2FE; color:#0369A1; font-size:1.8rem; display:flex; align-items:center; justify-content:center; margin:0 auto 16px;">
                    3
                </div>
                <h4 style="font-size:1.1rem; font-weight:800; margin:0 0 8px;">Thư Thái Thưởng Thức</h4>
                <p style="font-size:0.9rem; color:var(--text-muted); line-height:1.5;">
                    Trái cây luôn tươi mới thu hoạch giao tận nhà. Bạn có thể tạm dừng hoặc đổi lịch bất cứ khi nào đi công tác!
                </p>
            </div>
        </div>
    </section>

</div>

<script>
    // Tự động ẩn toast sau 4 giây
    setTimeout(function() {
        const toast = document.getElementById('toast');
        if (toast) toast.style.display = 'none';
    }, 4000);
</script>

</body>
</html>
