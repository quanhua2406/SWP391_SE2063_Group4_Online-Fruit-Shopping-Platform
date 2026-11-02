<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- Resolving User Role & Dashboard Link cleanly --%>
<c:set var="currentUser" value="${sessionScope.user}" />
<c:set var="userRole" value="${not empty currentUser.roleName ? fn:toUpperCase(fn:trim(currentUser.roleName)) : ''}" />
<c:set var="isShopOwner" value="${fn:contains(userRole, 'SHOP') or fn:contains(userRole, 'STORE') or currentUser.roleID == 2}" />

<c:set var="dashboardUrl" value="" />
<c:set var="dashboardName" value="" />
<c:choose>
    <c:when test="${fn:contains(userRole, 'ADMIN')}">
        <c:set var="dashboardUrl" value="${pageContext.request.contextPath}/admin/dashboard" />
        <c:set var="dashboardName" value="Admin Dashboard" />
    </c:when>
    <c:when test="${isShopOwner}">
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

<header class="navbar">
    <!-- Brand Logo -->
    <div class="logo">
        <a href="${pageContext.request.contextPath}/home" style="display:flex; align-items:center; gap:8px; text-decoration:none; color:inherit;">
            <svg viewBox="0 0 36 36" width="30" height="30" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 6C18 3.5 19 1.5 20 1" stroke="#15803d" stroke-width="2" stroke-linecap="round"/>
                <path d="M18 4C21.5 2 24.5 3.5 23.5 6C21 6.5 19 5.5 18 4Z" fill="#22c55e"/>
                <path d="M18 7C14 7 11 10.5 11 15C11 20 8 22 8 26C8 30 12 33 18 33C24 33 28 30 28 26C28 22 25 20 25 15C25 10.5 22 7 18 7Z" fill="#16a34a"/>
            </svg>
            <span style="font-weight: 800; font-size: 20px; letter-spacing: -0.5px; color: #16a34a;">FreshFruit</span>
        </a>
    </div>

    <!-- Search Bar with Live Suggestions -->
    <c:if test="${empty param.hideSearch or param.hideSearch != 'true'}">
        <div class="search-box" style="position: relative;">
            <form action="${pageContext.request.contextPath}/products" method="GET" style="display: flex; width: 100%;">
                <input type="text" name="keyword" id="globalSearchInput" placeholder="Tìm kiếm trái cây tươi, giống nhập khẩu, giỏ quà..." autocomplete="off" value="${param.keyword}">
                <button type="submit" aria-label="Tìm kiếm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="8"></circle>
                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                </button>
            </form>
            <div class="search-suggestions-dropdown" id="globalSearchDropdown" style="position: absolute; top: 100%; left: 0; right: 0; background: #fff; border: 1px solid #cbd5e1; border-radius: 0 0 10px 10px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); max-height: 380px; overflow-y: auto; z-index: 1000; display: none;"></div>
        </div>
    </c:if>

    <!-- Nav Actions -->
    <div class="nav-actions">
        <!-- Khám phá trái cây -->
        <a href="${pageContext.request.contextPath}/products" class="nav-link" style="color: #16a34a; font-weight: 700;">
            Khám Phá Trái Cây
        </a>

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
                <c:if test="${not isShopOwner and not empty currentUser}">
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
            <c:when test="${not empty currentUser}">
                <div class="nav-dropdown">
                    <button type="button" class="nav-dropdown-toggle" style="border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 9999px; padding: 4px 12px 4px 6px;">
                        <span style="width: 26px; height: 26px; border-radius: 50%; background: #dcfce7; color: #16a34a; display: inline-flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px;">
                            ${fn:toUpperCase(fn:substring(not empty currentUser.fullName ? currentUser.fullName : currentUser.username, 0, 1))}
                        </span>
                        <span style="font-weight: 600; color: #1e293b; max-width: 130px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-size: 13.5px;">
                            ${not empty currentUser.fullName ? currentUser.fullName : currentUser.username}
                        </span>
                        <svg class="chevron-icon" xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="6 9 12 15 18 9"></polyline>
                        </svg>
                    </button>

                    <div class="nav-dropdown-menu" style="min-width: 230px;">
                        <div class="nav-dropdown-header">
                            <div class="nav-dropdown-user-name">${not empty currentUser.fullName ? currentUser.fullName : currentUser.username}</div>
                            <div class="nav-dropdown-user-role">${not empty currentUser.roleName ? currentUser.roleName : 'Thành viên'}</div>
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

<!-- Global Search Autocomplete Script -->
<script>
    (function() {
        const input = document.getElementById('globalSearchInput');
        const dropdown = document.getElementById('globalSearchDropdown');
        if (!input || !dropdown) return;

        let timer = null;
        input.addEventListener('input', function() {
            const q = this.value.trim();
            clearTimeout(timer);

            if (q.length < 2) {
                dropdown.style.display = 'none';
                dropdown.innerHTML = '';
                return;
            }

            timer = setTimeout(function() {
                fetch('${pageContext.request.contextPath}/api/products/search-suggestions?q=' + encodeURIComponent(q))
                    .then(function(r) { return r.json(); })
                    .then(function(data) {
                        if (data && data.length > 0) {
                            dropdown.innerHTML = '';
                            data.forEach(function(item) {
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
                    .catch(function() {
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
