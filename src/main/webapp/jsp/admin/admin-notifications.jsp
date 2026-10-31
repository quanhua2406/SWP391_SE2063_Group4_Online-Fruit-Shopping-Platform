<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông Báo Hệ Thống - FreshFruit Admin</title>

    <!-- Global CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">
    <!-- Admin Dedicated Notification CSS (100% CSS thuần, zero inline styles) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/admin-notification.css">
</head>
<body>

<!-- ======================================================================= -->
<!-- NAVBAR HEADER FOR ADMIN -->
<!-- ======================================================================= -->
<header class="navbar">
    <div class="logo">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-logo-link">
            🍊 FreshFruit <span class="card-type-tag">Quản Trị</span>
        </a>
    </div>

    <div class="nav-actions nav-actions-group">
        <a href="${pageContext.request.contextPath}/admin/dashboard">📊 Bảng điều khiển</a>
        <a href="${pageContext.request.contextPath}/admin/reviews">🚩 Kiểm duyệt Review</a>
        <a href="${pageContext.request.contextPath}/admin/products">🍎 Kho sản phẩm</a>
        <a href="${pageContext.request.contextPath}/admin/orders">📦 Đơn hàng</a>

        <!-- Bell Icon with Unread Badge for Admin -->
        <a href="${pageContext.request.contextPath}/admin/notifications" class="nav-bell-link" title="Thông báo hệ thống">
            🔔 Thông báo
            <c:if test="${unreadCount > 0}">
                <span class="nav-bell-badge">${unreadCount}</span>
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
        <a href="${pageContext.request.contextPath}/admin/dashboard">Bảng điều khiển</a> &gt; 
        <span>Trung tâm thông báo &amp; cảnh báo hệ thống</span>
    </div>

    <!-- Flash Success Message (chuẩn PRG) -->
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="alert-flash">
            <span>✓</span> ${sessionScope.flashMessage}
        </div>
        <c:remove var="flashMessage" scope="session" />
    </c:if>

    <div class="notif-layout">

        <!-- =================================================================== -->
        <!-- LEFT SIDEBAR: FILTERS FOR ADMIN -->
        <!-- =================================================================== -->
        <aside class="notif-sidebar">
            <h3 class="sidebar-title">Danh mục cảnh báo</h3>

            <nav class="filter-nav">
                <!-- Tất cả -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=all&pageSize=${pageSize}&filterRead=${filterRead}&filterImportant=${filterImportant}" 
                   class="filter-link ${empty currentTab or currentTab == 'all' ? 'active' : ''}">
                    <span class="filter-label">🔔 Tất cả</span>
                    <c:if test="${unreadCount > 0}">
                        <span class="filter-badge has-unread">${unreadCount}</span>
                    </c:if>
                </a>

                <!-- Review bị báo cáo -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=reported_review&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'reported_review' ? 'active' : ''}">
                    <span class="filter-label">🚩 Review bị báo cáo</span>
                    <c:if test="${not empty unreadPerGroup['reported_review'] and unreadPerGroup['reported_review'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['reported_review']}</span>
                    </c:if>
                </a>

                <!-- Tồn kho nghiêm trọng -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=critical_low_stock&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'critical_low_stock' ? 'active' : ''}">
                    <span class="filter-label">⚠️ Tồn kho nghiêm trọng</span>
                    <c:if test="${not empty unreadPerGroup['critical_low_stock'] and unreadPerGroup['critical_low_stock'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['critical_low_stock']}</span>
                    </c:if>
                </a>

                <!-- Hoàn tiền -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=refund&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'refund' ? 'active' : ''}">
                    <span class="filter-label">🔄 Yêu cầu hoàn tiền</span>
                    <c:if test="${not empty unreadPerGroup['refund'] and unreadPerGroup['refund'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['refund']}</span>
                    </c:if>
                </a>

                <!-- Đơn bị hủy -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=cancelled&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'cancelled' ? 'active' : ''}">
                    <span class="filter-label">❌ Đơn hàng bị hủy</span>
                    <c:if test="${not empty unreadPerGroup['cancelled'] and unreadPerGroup['cancelled'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['cancelled']}</span>
                    </c:if>
                </a>

                <!-- Cửa hàng mới -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=new_shop&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'new_shop' ? 'active' : ''}">
                    <span class="filter-label">🏪 Cửa hàng mới</span>
                    <c:if test="${not empty unreadPerGroup['new_shop'] and unreadPerGroup['new_shop'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['new_shop']}</span>
                    </c:if>
                </a>

                <!-- Cảnh báo hệ thống -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=system_alert&pageSize=${pageSize}&filterRead=${filterRead}" 
                   class="filter-link ${currentTab == 'system_alert' ? 'active' : ''}">
                    <span class="filter-label">📢 Cảnh báo hệ thống</span>
                    <c:if test="${not empty unreadPerGroup['system_alert'] and unreadPerGroup['system_alert'] > 0}">
                        <span class="filter-badge has-unread">${unreadPerGroup['system_alert']}</span>
                    </c:if>
                </a>

                <div class="sidebar-divider"></div>

                <!-- Lọc riêng tin khẩn cấp (is_important = true) -->
                <a href="${pageContext.request.contextPath}/admin/notifications?tab=all&pageSize=${pageSize}&filterImportant=${filterImportant == 'important' ? 'all' : 'important'}" 
                   class="filter-link filter-important-link ${filterImportant == 'important' ? 'active' : ''}">
                    <span class="filter-label">⚡ Tin khẩn cấp</span>
                    <c:if test="${importantUnreadCount > 0}">
                        <span class="filter-badge badge-important-count">${importantUnreadCount}</span>
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
                            <c:when test="${filterImportant == 'important'}">⚡ Thông Báo Khẩn Cấp Cần Xử Lý</c:when>
                            <c:when test="${currentTab == 'reported_review'}">🚩 Đánh Giá Bị Báo Cáo Cần Duyệt</c:when>
                            <c:when test="${currentTab == 'critical_low_stock'}">⚠️ Cảnh Báo Tồn Kho Nghiêm Trọng</c:when>
                            <c:when test="${currentTab == 'refund'}">🔄 Yêu Cầu Hoàn Tiền Chờ Phê Duyệt</c:when>
                            <c:when test="${currentTab == 'cancelled'}">❌ Theo Dõi Đơn Hàng Bị Hủy</c:when>
                            <c:when test="${currentTab == 'new_shop'}">🏪 Hồ Sơ Đăng Ký Cửa Hàng Mới</c:when>
                            <c:when test="${currentTab == 'system_alert'}">📢 Cảnh Báo Hệ Thống &amp; Bảo Trì</c:when>
                            <c:otherwise>🔔 Tất cả thông báo &amp; cảnh báo</c:otherwise>
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
                        <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=all&pageSize=${pageSize}&filterImportant=${filterImportant}" 
                           class="sub-filter-btn ${empty filterRead or filterRead == 'all' ? 'active' : ''}">
                           Tất cả
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=unread&pageSize=${pageSize}&filterImportant=${filterImportant}" 
                           class="sub-filter-btn ${filterRead == 'unread' ? 'active' : ''}">
                           Chưa đọc
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=read&pageSize=${pageSize}&filterImportant=${filterImportant}" 
                           class="sub-filter-btn ${filterRead == 'read' ? 'active' : ''}">
                           Đã đọc
                        </a>
                    </div>

                    <!-- Nút Đánh Dấu Tất Cả Đã Đọc (Pure HTML Form POST) -->
                    <c:if test="${unreadCount > 0}">
                        <form action="${pageContext.request.contextPath}/admin/notifications" method="POST" class="form-inline">
                            <input type="hidden" name="action" value="readAll">
                            <input type="hidden" name="tab" value="${currentTab}">
                            <input type="hidden" name="pageSize" value="${pageSize}">
                            <input type="hidden" name="filterRead" value="${filterRead}">
                            <input type="hidden" name="filterImportant" value="${filterImportant}">
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
                            <!-- Card thông báo: Phân biệt Đã đọc/Chưa đọc và Visual Highlight cho tin Khẩn cấp -->
                            <article class="notif-card ${item.read ? 'card-read' : 'card-unread'} ${item.important ? 'card-important' : ''}">
                                
                                <!-- Icon Box -->
                                <div class="card-icon-box">
                                    ${item.icon}
                                </div>

                                <!-- Nội dung chính -->
                                <div class="card-body">
                                    <div class="card-title-row">
                                        <!-- Chấm đỏ chưa đọc -->
                                        <c:if test="${!item.read}">
                                            <span class="unread-dot" title="Chưa đọc"></span>
                                        </c:if>

                                        <!-- Badge Khẩn Cấp -->
                                        <c:if test="${item.important}">
                                            <span class="badge-important">⚡ KHẨN CẤP</span>
                                        </c:if>

                                        <a href="${pageContext.request.contextPath}/admin/notifications?action=read&id=${item.notificationId}" 
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
                                        <c:if test="${not empty item.referenceId}">
                                            <span>•</span>
                                            <span>Tham chiếu: #${item.referenceId}</span>
                                        </c:if>
                                    </div>
                                </div>

                                <!-- Nút Hành Động (Click -> Đánh dấu đọc -> Điều hướng đến màn hình quản trị) -->
                                <div class="card-action-box">
                                    <a href="${pageContext.request.contextPath}/admin/notifications?action=read&id=${item.notificationId}" 
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

                            <form action="${pageContext.request.contextPath}/admin/notifications" method="GET" class="page-size-form">
                                <input type="hidden" name="tab" value="${currentTab}">
                                <input type="hidden" name="filterRead" value="${filterRead}">
                                <input type="hidden" name="filterImportant" value="${filterImportant}">
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

                        <!-- Các nút chuyển trang (Sliding Window Pagination) -->
                        <c:if test="${totalPages > 1}">
                            <div class="pagination-links">
                                <!-- Nút Trang đầu / Trang trước -->
                                <c:if test="${currentPage > 1}">
                                    <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=${filterRead}&filterImportant=${filterImportant}&pageSize=${pageSize}&page=1" 
                                       class="page-btn" title="Trang đầu">
                                       « Đầu
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=${filterRead}&filterImportant=${filterImportant}&pageSize=${pageSize}&page=${currentPage - 1}" 
                                       class="page-btn" title="Trang trước">
                                       ‹ Trước
                                    </a>
                                </c:if>

                                <!-- Dải số trang hiển thị linh hoạt (startPage đến endPage) -->
                                <c:forEach begin="${startPage}" end="${endPage}" var="p">
                                    <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=${filterRead}&filterImportant=${filterImportant}&pageSize=${pageSize}&page=${p}" 
                                       class="page-btn ${p == currentPage ? 'active' : ''}">
                                        ${p}
                                    </a>
                                </c:forEach>

                                <!-- Nút Trang sau / Trang cuối -->
                                <c:if test="${currentPage < totalPages}">
                                    <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=${filterRead}&filterImportant=${filterImportant}&pageSize=${pageSize}&page=${currentPage + 1}" 
                                       class="page-btn" title="Trang tiếp theo">
                                       Sau ›
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/notifications?tab=${currentTab}&filterRead=${filterRead}&filterImportant=${filterImportant}&pageSize=${pageSize}&page=${totalPages}" 
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
                        <h3>Chưa có cảnh báo nào</h3>
                        <p>Hệ thống hiện tại đang vận hành ổn định, không có báo cáo hay cảnh báo nào trong mục này.</p>
                    </div>
                </c:otherwise>
            </c:choose>

        </section>

    </div>

</main>

</body>
</html>
