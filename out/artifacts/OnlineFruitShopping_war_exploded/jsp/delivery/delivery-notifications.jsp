<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông Báo Giao Hàng - FreshFruit Delivery</title>

    <!-- Global CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <!-- Dedicated Delivery Staff Notification CSS (100% Pure CSS, zero inline styles) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/delivery-notification.css">
</head>
<body>

<!-- ======================================================================= -->
<!-- NAVBAR HEADER FOR DELIVERY STAFF -->
<!-- ======================================================================= -->
<header class="navbar">
    <div class="logo">
        <a href="${pageContext.request.contextPath}/delivery/dashboard" class="delivery-bell-link">
            🍊 FreshFruit <span class="badge-tag badge-status">Giao Hàng</span>
        </a>
    </div>

    <nav class="nav-actions">
        <a href="${pageContext.request.contextPath}/home">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/delivery/dashboard">📊 Bảng điều khiển</a>
        <a href="${pageContext.request.contextPath}/shopowner/delivery/list">📦 Danh sách đơn giao</a>

        <!-- Bell Notification Link with Dynamic Unread Badge -->
        <a href="${pageContext.request.contextPath}/delivery/notifications" class="delivery-bell-link">
            🔔 Thông báo
            <c:if test="${not empty unreadCount and unreadCount > 0}">
                <span class="delivery-bell-badge">${unreadCount}</span>
            </c:if>
        </a>

        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </nav>
</header>

<!-- ======================================================================= -->
<!-- MAIN CONTAINER -->
<!-- ======================================================================= -->
<main class="delivery-notif-container">

    <!-- Breadcrumb -->
    <div class="delivery-breadcrumb">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <span class="delivery-breadcrumb-sep">/</span>
        <a href="${pageContext.request.contextPath}/delivery/dashboard">Bàn giao việc</a>
        <span class="delivery-breadcrumb-sep">/</span>
        <span>Thông báo giao hàng</span>
    </div>

    <!-- Layout: Sidebar / Pills & Main Content -->
    <div class="delivery-layout">

        <!-- =================================================================== -->
        <!-- FILTER SIDEBAR (Responsive: converts to scrollable pills on mobile) -->
        <!-- =================================================================== -->
        <aside class="delivery-sidebar">
            <div class="sidebar-heading">Loại thông báo</div>

            <!-- Primary Type Filters -->
            <div class="filter-nav">
                <!-- Tất cả -->
                <a href="${pageContext.request.contextPath}/delivery/notifications?type=all&filterRead=${filterRead}&pageSize=${pageSize}"
                   class="filter-link <c:if test="${currentType == 'all'}">active</c:if>">
                    <span>📋 Tất cả</span>
                    <c:if test="${unreadCount > 0}">
                        <span class="filter-badge">${unreadCount}</span>
                    </c:if>
                </a>

                <!-- Đơn được phân công -->
                <a href="${pageContext.request.contextPath}/delivery/notifications?type=new_delivery_assignment&filterRead=${filterRead}&pageSize=${pageSize}"
                   class="filter-link <c:if test="${currentType == 'new_delivery_assignment'}">active</c:if>">
                    <span>🚚 Đơn phân công</span>
                    <c:set var="assignmentCnt" value="${unreadPerGroup['new_delivery_assignment']}" />
                    <c:if test="${not empty assignmentCnt and assignmentCnt > 0}">
                        <span class="filter-badge filter-badge-urgent">${assignmentCnt}</span>
                    </c:if>
                </a>

                <!-- Cập nhật trạng thái -->
                <a href="${pageContext.request.contextPath}/delivery/notifications?type=delivery_status_update&filterRead=${filterRead}&pageSize=${pageSize}"
                   class="filter-link <c:if test="${currentType == 'delivery_status_update'}">active</c:if>">
                    <span>📍 Cập nhật trạng thái</span>
                    <c:set var="statusCnt" value="${unreadPerGroup['delivery_status_update']}" />
                    <c:if test="${not empty statusCnt and statusCnt > 0}">
                        <span class="filter-badge">${statusCnt}</span>
                    </c:if>
                </a>
            </div>

            <div class="sidebar-divider"></div>

            <div class="sidebar-heading">Trạng thái đọc</div>

            <!-- Secondary Read Status Filters -->
            <div class="subfilter-nav">
                <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=all&pageSize=${pageSize}"
                   class="subfilter-link <c:if test="${filterRead == 'all'}">active</c:if>">
                    Tất cả trạng thái
                </a>
                <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=unread&pageSize=${pageSize}"
                   class="subfilter-link <c:if test="${filterRead == 'unread'}">active</c:if>">
                    Chưa đọc (${unreadCount})
                </a>
                <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=read&pageSize=${pageSize}"
                   class="subfilter-link <c:if test="${filterRead == 'read'}">active</c:if>">
                    Đã đọc
                </a>
            </div>
        </aside>

        <!-- =================================================================== -->
        <!-- MAIN CONTENT LIST -->
        <!-- =================================================================== -->
        <section class="delivery-main">

            <!-- Toolbar Header -->
            <div class="delivery-toolbar">
                <div class="toolbar-title-wrap">
                    <h1 class="toolbar-title">
                        <c:choose>
                            <c:when test="${currentType == 'new_delivery_assignment'}">🚚 Đơn phân công mới</c:when>
                            <c:when test="${currentType == 'delivery_status_update'}">📍 Cập nhật trạng thái đơn</c:when>
                            <c:otherwise>🔔 Tất cả thông báo</c:otherwise>
                        </c:choose>
                    </h1>
                    <span class="toolbar-count">(${totalItems} tin)</span>
                </div>

                <div class="toolbar-actions">
                    <!-- Dynamic Page Size Selector Form -->
                    <form action="${pageContext.request.contextPath}/delivery/notifications" method="GET" class="pagesize-form">
                        <input type="hidden" name="type" value="${currentType}">
                        <input type="hidden" name="filterRead" value="${filterRead}">
                        <input type="hidden" name="page" value="1">
                        <label for="pageSizeSelect">Hiển thị:</label>
                        <select name="pageSize" id="pageSizeSelect" class="pagesize-select">
                            <c:forEach items="${pageSizeOptions}" var="sizeOpt">
                                <option value="${sizeOpt}" <c:if test="${pageSize == sizeOpt}">selected</c:if>>${sizeOpt} / trang</option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="pagesize-btn">Áp dụng</button>
                    </form>

                    <!-- Mark All As Read Button (Pure POST form) -->
                    <c:if test="${unreadCount > 0}">
                        <form action="${pageContext.request.contextPath}/delivery/notifications" method="POST">
                            <input type="hidden" name="action" value="readAll">
                            <input type="hidden" name="type" value="${currentType}">
                            <input type="hidden" name="filterRead" value="${filterRead}">
                            <input type="hidden" name="pageSize" value="${pageSize}">
                            <button type="submit" class="btn-read-all">
                                ✓ Đánh dấu tất cả đã đọc
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>

            <!-- List of Notification Cards -->
            <div class="notif-list">
                <c:choose>
                    <c:when test="${not empty notifications}">
                        <c:forEach items="${notifications}" var="noti">
                            <article class="notif-card <c:if test="${!noti.read}">unread</c:if> <c:if test="${noti.important}">important</c:if>">
                                <!-- Icon Box -->
                                <div class="notif-icon-box">
                                    <c:out value="${noti.icon}" />
                                </div>

                                <!-- Card Body -->
                                <div class="notif-body">
                                    <div class="notif-header-line">
                                        <div class="notif-tag-wrap">
                                            <c:if test="${noti.important}">
                                                <span class="badge-tag badge-urgent">🚨 Đơn Mới</span>
                                            </c:if>
                                            <c:if test="${!noti.important}">
                                                <span class="badge-tag badge-status">Cập Nhật</span>
                                            </c:if>
                                            <c:if test="${!noti.read}">
                                                <span class="unread-dot" title="Chưa đọc"></span>
                                            </c:if>
                                        </div>
                                        <span class="notif-time">${noti.timeAgo}</span>
                                    </div>

                                    <!-- Title -->
                                    <h2 class="notif-title">
                                        <c:out value="${noti.title}" />
                                    </h2>

                                    <!-- Content (Order info & Address) -->
                                    <p class="notif-content">
                                        <c:out value="${noti.content}" />
                                    </p>

                                    <!-- Footer Actions -->
                                    <div class="notif-footer-actions">
                                        <!-- Primary Action: Click to read & navigate directly to delivery detail -->
                                        <a href="${pageContext.request.contextPath}/delivery/notifications?action=read&id=${noti.notificationId}&redirect=${noti.actionUrl}"
                                           class="btn-action-primary <c:if test="${noti.important}">btn-action-urgent</c:if>">
                                            ${noti.actionText} →
                                        </a>

                                        <!-- Mark as read only (if unread) -->
                                        <c:if test="${!noti.read}">
                                            <a href="${pageContext.request.contextPath}/delivery/notifications?action=read&id=${noti.notificationId}"
                                               class="btn-action-secondary">
                                                ✓ Đã đọc
                                            </a>
                                        </c:if>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <!-- Empty State -->
                        <div class="notif-empty">
                            <div class="notif-empty-icon">📭</div>
                            <h3 class="notif-empty-title">Không có thông báo nào</h3>
                            <p class="notif-empty-desc">
                                Bạn hiện không có thông báo giao hàng nào trong danh mục hoặc trạng thái này.
                            </p>
                            <c:if test="${currentType != 'all' or filterRead != 'all'}">
                                <a href="${pageContext.request.contextPath}/delivery/notifications?type=all&filterRead=all&pageSize=${pageSize}"
                                   class="btn-empty-reset">
                                    Xem tất cả thông báo
                                </a>
                            </c:if>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- =============================================================== -->
            <!-- DYNAMIC PAGINATION -->
            <!-- =============================================================== -->
            <c:if test="${totalPages > 1}">
                <div class="pagination-wrap">
                    <div class="pagination-info">
                        Hiển thị <strong>${fromItem} - ${toItem}</strong> trên <strong>${totalItems}</strong> thông báo
                    </div>

                    <div class="pagination-pages">
                        <!-- First Page -->
                        <c:choose>
                            <c:when test="${currentPage > 1}">
                                <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=${filterRead}&pageSize=${pageSize}&page=1"
                                   class="page-btn" title="Trang đầu">««</a>
                                <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=${filterRead}&pageSize=${pageSize}&page=${currentPage - 1}"
                                   class="page-btn" title="Trang trước">«</a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-btn disabled">««</span>
                                <span class="page-btn disabled">«</span>
                            </c:otherwise>
                        </c:choose>

                        <!-- Dynamic Page Numbers (Sliding Window) -->
                        <c:forEach begin="${startPage}" end="${endPage}" var="p">
                            <c:choose>
                                <c:when test="${p == currentPage}">
                                    <span class="page-btn active">${p}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=${filterRead}&pageSize=${pageSize}&page=${p}"
                                       class="page-btn">${p}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <!-- Next & Last Page -->
                        <c:choose>
                            <c:when test="${currentPage < totalPages}">
                                <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=${filterRead}&pageSize=${pageSize}&page=${currentPage + 1}"
                                   class="page-btn" title="Trang sau">»</a>
                                <a href="${pageContext.request.contextPath}/delivery/notifications?type=${currentType}&filterRead=${filterRead}&pageSize=${pageSize}&page=${totalPages}"
                                   class="page-btn" title="Trang cuối">»»</a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-btn disabled">»</span>
                                <span class="page-btn disabled">»»</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

        </section>
    </div>

</main>

</body>
</html>
