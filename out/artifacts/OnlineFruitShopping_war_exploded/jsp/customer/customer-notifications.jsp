<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông Báo Khách Hàng - FreshFruit</title>

    <!-- Global CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">
    <!-- Notification Custom CSS (Toàn bộ style nằm ở đây, không inline trong JSP) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/notification.css">
</head>
<body>

<!-- ======================================================================= -->
<!-- NAVBAR HEADER -->
<!-- ======================================================================= -->
<header class="navbar">
    <div class="logo">
        <a href="${pageContext.request.contextPath}/home" class="nav-logo-link">
            🍊 FreshFruit
        </a>
    </div>

    <div class="nav-actions nav-actions-group">
        <a href="${pageContext.request.contextPath}/home">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/customer/dashboard">👤 Bảng điều khiển</a>

        <!-- Bell Icon with Unread Badge -->
        <a href="${pageContext.request.contextPath}/notifications" class="nav-bell" title="Thông báo của bạn">
            🔔 Thông báo
            <c:if test="${unreadCount > 0}">
                <span class="bell-badge">${unreadCount}</span>
            </c:if>
        </a>

        <a href="${pageContext.request.contextPath}/logout" class="logout nav-logout-btn">Đăng xuất</a>
    </div>
</header>

<!-- ======================================================================= -->
<!-- MAIN CONTENT -->
<!-- ======================================================================= -->
<main class="notif-container">

    <!-- Breadcrumb -->
    <div class="notif-breadcrumb">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a> &gt; 
        <a href="${pageContext.request.contextPath}/customer/dashboard">Khách hàng</a> &gt; 
        <span>Trung tâm thông báo</span>
    </div>

    <!-- Flash Success Message -->
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="alert-flash">
            <span>✓</span> ${sessionScope.flashMessage}
        </div>
        <c:remove var="flashMessage" scope="session" />
    </c:if>

    <div class="notif-layout">

        <!-- =================================================================== -->
        <!-- LEFT SIDEBAR: FILTERS -->
        <!-- =================================================================== -->
        <aside class="notif-sidebar">
            <h3 class="sidebar-title">Danh mục thông báo</h3>

            <nav class="filter-nav">
                <!-- Tất cả -->
                <a href="${pageContext.request.contextPath}/notifications?tab=all&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${empty currentTab or currentTab == 'all' ? 'active' : ''}">
                    <span class="filter-label">🔔 Tất cả</span>
                    <c:if test="${unreadCount > 0}">
                        <span class="filter-badge has-unread">${unreadCount}</span>
                    </c:if>
                </a>

                <!-- Đơn hàng -->
                <a href="${pageContext.request.contextPath}/notifications?tab=order&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'order' ? 'active' : ''}">
                    <span class="filter-label">📦 Đơn hàng</span>
                    <c:if test="${not empty unreadPerGroup['order'] and unreadPerGroup['order'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['order']}</span>
                    </c:if>
                </a>

                <!-- Giao hàng -->
                <a href="${pageContext.request.contextPath}/notifications?tab=delivery&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'delivery' ? 'active' : ''}">
                    <span class="filter-label">🚚 Giao hàng</span>
                    <c:if test="${not empty unreadPerGroup['delivery'] and unreadPerGroup['delivery'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['delivery']}</span>
                    </c:if>
                </a>

                <!-- Thanh toán -->
                <a href="${pageContext.request.contextPath}/notifications?tab=payment&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'payment' ? 'active' : ''}">
                    <span class="filter-label">💳 Thanh toán</span>
                    <c:if test="${not empty unreadPerGroup['payment'] and unreadPerGroup['payment'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['payment']}</span>
                    </c:if>
                </a>

                <!-- Khuyến mãi -->
                <a href="${pageContext.request.contextPath}/notifications?tab=promotion&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'promotion' ? 'active' : ''}">
                    <span class="filter-label">🏷️ Khuyến mãi</span>
                    <c:if test="${not empty unreadPerGroup['promotion'] and unreadPerGroup['promotion'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['promotion']}</span>
                    </c:if>
                </a>

                <!-- Đăng ký gói -->
                <a href="${pageContext.request.contextPath}/notifications?tab=subscription&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'subscription' ? 'active' : ''}">
                    <span class="filter-label">🔄 Gói định kỳ</span>
                    <c:if test="${not empty unreadPerGroup['subscription'] and unreadPerGroup['subscription'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['subscription']}</span>
                    </c:if>
                </a>
            </nav>
        </aside>

        <!-- =================================================================== -->
        <!-- RIGHT CONTENT AREA -->
        <!-- =================================================================== -->
        <section class="notif-content">

            <!-- Toolbar Header -->
            <div class="content-header">
                <div class="header-left">
                    <h2>
                        <c:choose>
                            <c:when test="${currentTab == 'order'}">📦 Thông báo Đơn hàng</c:when>
                            <c:when test="${currentTab == 'delivery'}">🚚 Trạng thái Giao hàng</c:when>
                            <c:when test="${currentTab == 'payment'}">💳 Thông tin Thanh toán</c:when>
                            <c:when test="${currentTab == 'promotion'}">🏷️ Ưu đãi & Khuyến mãi</c:when>
                            <c:when test="${currentTab == 'subscription'}">🔄 Gói Trái Cây Định Kỳ</c:when>
                            <c:otherwise>🔔 Tất cả thông báo</c:otherwise>
                        </c:choose>
                    </h2>
                    <div class="count-info">
                        Tổng cộng <strong>${totalItems}</strong> thông báo
                        <c:if test="${unreadCount > 0}">
                            • <span class="count-unread-badge">${unreadCount} chưa đọc</span>
                        </c:if>
                    </div>
                </div>

                <div class="header-right">
                    <!-- Lọc Trạng Thái Đọc / Chưa Đọc -->
                    <div class="sub-filter-group">
                        <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=all&pageSize=${pageSize}" 
                           class="sub-filter-btn ${empty filterRead or filterRead == 'all' ? 'active' : ''}">
                           Tất cả
                        </a>
                        <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=unread&pageSize=${pageSize}" 
                           class="sub-filter-btn ${filterRead == 'unread' ? 'active' : ''}">
                           Chưa đọc
                        </a>
                        <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=read&pageSize=${pageSize}" 
                           class="sub-filter-btn ${filterRead == 'read' ? 'active' : ''}">
                           Đã đọc
                        </a>
                    </div>

                    <!-- Nút Đánh Dấu Tất Cả Đã Đọc (Pure HTML Form POST) -->
                    <c:if test="${unreadCount > 0}">
                        <form action="${pageContext.request.contextPath}/notifications" method="POST" class="form-inline">
                            <input type="hidden" name="action" value="readAll">
                            <input type="hidden" name="tab" value="${currentTab}">
                            <input type="hidden" name="pageSize" value="${pageSize}">
                            <button type="submit" class="btn-mark-all" title="Đánh dấu toàn bộ là đã đọc">
                                ✓ Đánh dấu tất cả đã đọc
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>

            <!-- Danh Sách Notification Cards -->
            <c:choose>
                <c:when test="${not empty notifications}">
                    <div class="notif-list">
                        <c:forEach items="${notifications}" var="item">
                            <!-- Card thông báo -->
                            <article class="notif-card ${item.read ? 'card-read' : 'card-unread'}">
                                
                                <!-- Icon / Image Box -->
                                <div class="card-icon-box">
                                    <c:choose>
                                        <c:when test="${not empty item.productImageUrl}">
                                            <img src="${item.productImageUrl}" alt="Ảnh sản phẩm" class="card-product-img">
                                        </c:when>
                                        <c:otherwise>
                                            ${item.icon}
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Nội dung chính -->
                                <div class="card-body">
                                    <div class="card-title-row">
                                        <c:if test="${!item.read}">
                                            <span class="unread-dot" title="Chưa đọc"></span>
                                        </c:if>
                                        <a href="${pageContext.request.contextPath}/notifications?action=read&id=${item.notificationId}" 
                                           class="card-title">
                                            ${item.title}
                                        </a>
                                    </div>

                                    <p class="card-content">
                                        ${item.content}
                                    </p>

                                    <div class="card-meta">
                                        <span class="card-type-tag">${item.typeDisplayName}</span>
                                        <span>•</span>
                                        <span>${item.timeAgo}</span>
                                    </div>
                                </div>

                                <!-- Nút Hành Động -->
                                <div class="card-action-box">
                                    <a href="${pageContext.request.contextPath}/notifications?action=read&id=${item.notificationId}" 
                                       class="card-action-btn">
                                        ${item.actionText} →
                                    </a>
                                </div>
                            </article>
                        </c:forEach>
                    </div>

                    <!-- Phân Trang Linh Hoạt (Dynamic Pagination) -->
                    <div class="pagination-wrapper">

                        <!-- Toolbar chọn số lượng hiển thị (Dynamic Page Size) -->
                        <div class="pagination-toolbar">
                            <div class="pagination-info">
                                Hiển thị <strong>${fromItem}</strong> - <strong>${toItem}</strong> trong <strong>${totalItems}</strong> thông báo
                            </div>

                            <form action="${pageContext.request.contextPath}/notifications" method="GET" class="page-size-form">
                                <input type="hidden" name="tab" value="${currentTab}">
                                <input type="hidden" name="filterRead" value="${filterRead}">
                                <input type="hidden" name="page" value="1">
                                <label for="pageSizeSelect" class="page-size-label">Hiển thị mỗi trang:</label>
                                <select name="pageSize" id="pageSizeSelect" class="page-size-select">
                                    <c:forEach items="${pageSizeOptions}" var="opt">
                                        <option value="${opt}" ${pageSize == opt ? 'selected' : ''}>${opt} thông báo</option>
                                    </c:forEach>
                                </select>
                                <button type="submit" class="page-size-btn">Đổi</button>
                            </form>
                        </div>

                        <!-- Các nút chuyển trang -->
                        <c:if test="${totalPages > 1}">
                            <div class="pagination-links">
                                <!-- Nút Trang đầu -->
                                <c:if test="${currentPage > 1}">
                                    <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=${filterRead}&pageSize=${pageSize}&page=1" 
                                       class="page-btn" title="Trang đầu">
                                       « Đầu
                                    </a>
                                    <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=${filterRead}&pageSize=${pageSize}&page=${currentPage - 1}" 
                                       class="page-btn" title="Trang trước">
                                       ‹ Trước
                                    </a>
                                </c:if>

                                <!-- Dải số trang hiển thị linh hoạt (Sliding Window: startPage đến endPage) -->
                                <c:forEach begin="${startPage}" end="${endPage}" var="p">
                                    <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=${filterRead}&pageSize=${pageSize}&page=${p}" 
                                       class="page-btn ${p == currentPage ? 'active' : ''}">
                                        ${p}
                                    </a>
                                </c:forEach>

                                <!-- Nút Trang sau / Trang cuối -->
                                <c:if test="${currentPage < totalPages}">
                                    <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=${filterRead}&pageSize=${pageSize}&page=${currentPage + 1}" 
                                       class="page-btn" title="Trang tiếp theo">
                                       Sau ›
                                    </a>
                                    <a href="${pageContext.request.contextPath}/notifications?tab=${currentTab}&filterRead=${filterRead}&pageSize=${pageSize}&page=${totalPages}" 
                                       class="page-btn" title="Trang cuối">
                                       Cuối »
                                    </a>
                                </c:if>
                            </div>
                        </c:if>

                    </div>

                </c:when>

                <c:otherwise>
                    <!-- Empty State khi không có thông báo nào -->
                    <div class="notif-empty">
                        <div class="empty-icon">📭</div>
                        <h3>Chưa có thông báo nào</h3>
                        <p>Bạn không có thông báo nào trong danh mục này hoặc tất cả đã được xử lý.</p>
                    </div>
                </c:otherwise>
            </c:choose>

        </section>

    </div>

</main>

</body>
</html>
