<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn Hàng Của Tôi | FreshFruit Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        :root {
            --primary: #10b981;
            --primary-dark: #059669;
            --primary-light: #ecfdf5;
            --text-dark: #1f2937;
            --text-muted: #6b7280;
            --border: #e5e7eb;
            --bg-light: #f9fafb;
            --danger: #ef4444;
            --warning: #f59e0b;
            --info: #3b82f6;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-light);
            color: var(--text-dark);
            margin: 0;
            padding: 0;
        }

        .orders-container {
            max-width: 1100px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .page-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
        }

        .page-title {
            font-size: 24px;
            font-weight: 700;
            color: var(--text-dark);
            margin: 0;
        }

        /* Filter Tabs */
        .status-tabs {
            display: flex;
            background: #ffffff;
            border: 1px solid var(--border);
            border-radius: 10px;
            overflow-x: auto;
            margin-bottom: 20px;
            box-shadow: 0 1px 2px rgba(0,0,0,0.04);
        }

        .status-tab {
            padding: 12px 20px;
            text-decoration: none;
            color: var(--text-muted);
            font-size: 14px;
            font-weight: 500;
            white-space: nowrap;
            border-bottom: 2px solid transparent;
            transition: all 0.2s;
        }

        .status-tab:hover {
            color: var(--primary);
        }

        .status-tab.active {
            color: var(--primary-dark);
            border-bottom-color: var(--primary);
            font-weight: 600;
            background: var(--primary-light);
        }

        /* Search Bar */
        .search-box {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
        }

        .search-input {
            flex: 1;
            padding: 10px 14px;
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 14px;
            background: #fff;
        }

        .btn-search {
            background: var(--text-dark);
            color: #fff;
            border: none;
            border-radius: 8px;
            padding: 0 20px;
            font-weight: 600;
            cursor: pointer;
        }

        /* Order Cards */
        .order-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            margin-bottom: 20px;
            overflow: hidden;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            transition: box-shadow 0.2s;
        }

        .order-card:hover {
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.08);
        }

        .order-card-header {
            padding: 16px 20px;
            background: #fafafa;
            border-bottom: 1px solid var(--border);
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 14px;
        }

        .order-meta-left {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .order-code {
            font-weight: 700;
            color: var(--text-dark);
        }

        .order-date {
            color: var(--text-muted);
            font-size: 13px;
        }

        .order-shop {
            color: var(--primary-dark);
            font-weight: 500;
            font-size: 13px;
        }

        .status-badge {
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 600;
        }

        .badge-warning { background: #fef3c7; color: #92400e; }
        .badge-info { background: #dbeafe; color: #1e40af; }
        .badge-primary { background: #e0e7ff; color: #3730a3; }
        .badge-success { background: #d1fae5; color: #065f46; }
        .badge-danger { background: #fee2e2; color: #991b1b; }
        .badge-secondary { background: #f3f4f6; color: #4b5563; }

        .order-card-body {
            padding: 16px 20px;
        }

        .order-item-line {
            display: flex;
            align-items: center;
            gap: 14px;
            padding: 10px 0;
            border-bottom: 1px solid #f3f4f6;
        }

        .order-item-line:last-child {
            border-bottom: none;
        }

        .item-thumb {
            width: 50px;
            height: 50px;
            border-radius: 6px;
            object-fit: cover;
            border: 1px solid var(--border);
        }

        .item-details {
            flex: 1;
        }

        .item-name {
            font-weight: 600;
            font-size: 14px;
            color: var(--text-dark);
        }

        .item-subtext {
            font-size: 13px;
            color: var(--text-muted);
        }

        .item-price-col {
            text-align: right;
            font-weight: 600;
            font-size: 14px;
        }

        .order-card-footer {
            padding: 14px 20px;
            background: #fafafa;
            border-top: 1px solid var(--border);
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .order-total-block {
            display: flex;
            align-items: baseline;
            gap: 8px;
        }

        .order-total-label {
            font-size: 14px;
            color: var(--text-muted);
        }

        .order-total-price {
            font-size: 18px;
            font-weight: 700;
            color: #dc2626;
        }

        .order-actions {
            display: flex;
            gap: 10px;
        }

        .btn-action {
            padding: 8px 16px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            border: 1px solid transparent;
            transition: all 0.2s;
        }

        .btn-view {
            background: #ffffff;
            border-color: var(--border);
            color: var(--text-dark);
        }

        .btn-view:hover {
            border-color: var(--primary);
            color: var(--primary);
        }

        .btn-reorder {
            background: var(--primary);
            color: #ffffff;
        }

        .btn-reorder:hover {
            background: var(--primary-dark);
        }

        .btn-cancel {
            background: #fee2e2;
            color: var(--danger);
            border-color: #fecaca;
        }

        .btn-cancel:hover {
            background: #fecaca;
        }

        .empty-orders {
            text-align: center;
            padding: 60px 20px;
            background: #fff;
            border-radius: 12px;
            border: 1px solid var(--border);
        }

        /* Modal Hủy Đơn */
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0,0,0,0.5);
            z-index: 1000;
            align-items: center;
            justify-content: center;
        }

        .modal-content {
            background: #fff;
            border-radius: 12px;
            max-width: 480px;
            width: 90%;
            padding: 24px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
        }
    </style>
</head>
<body>

    <!-- Header Navigation (Reusable Component) -->
    <jsp:include page="/includes/header.jsp" />

    <div class="orders-container">
        <div class="page-header">
            <h1 class="page-title">Đơn Hàng Của Tôi (My Orders)</h1>
            <a href="${pageContext.request.contextPath}/products" class="btn-action btn-reorder">
                Mua Thêm Hoa Quả Mới
            </a>
        </div>

        <c:if test="${not empty param.msg}">
            <div style="background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px;">
                ${param.msg}
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div style="background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px;">
                ${param.error}
            </div>
        </c:if>

        <!-- Status Filter Tabs -->
        <div class="status-tabs">
            <a href="${pageContext.request.contextPath}/orders?status=all" class="status-tab ${empty currentStatus || currentStatus == 'all' ? 'active' : ''}">Tất cả</a>
            <a href="${pageContext.request.contextPath}/orders?status=pending_confirmation" class="status-tab ${currentStatus == 'pending_confirmation' ? 'active' : ''}">Chờ xác nhận</a>
            <a href="${pageContext.request.contextPath}/orders?status=preparing" class="status-tab ${currentStatus == 'preparing' ? 'active' : ''}">Đang đóng gói</a>
            <a href="${pageContext.request.contextPath}/orders?status=shipping" class="status-tab ${currentStatus == 'shipping' ? 'active' : ''}">Đang giao hàng</a>
            <a href="${pageContext.request.contextPath}/orders?status=delivered" class="status-tab ${currentStatus == 'delivered' ? 'active' : ''}">Đã giao</a>
            <a href="${pageContext.request.contextPath}/orders?status=cancelled" class="status-tab ${currentStatus == 'cancelled' ? 'active' : ''}">Đã hủy</a>
        </div>

        <!-- Search Bar -->
        <form action="${pageContext.request.contextPath}/orders" method="GET" class="search-box">
            <input type="hidden" name="status" value="${currentStatus}">
            <input type="text" name="q" class="search-input" placeholder="Tìm theo mã đơn hàng (#101), tên shop hoặc tên loại hoa quả..." value="${searchQuery}">
            <button type="submit" class="btn-search">Tìm Kiếm</button>
        </form>

        <!-- Danh sách đơn hàng (Task 7: View Order History) -->
        <c:choose>
            <c:when test="${not empty orders}">
                <c:forEach var="ord" items="${orders}">
                    <div class="order-card">
                        <div class="order-card-header">
                            <div class="order-meta-left">
                                <span class="order-code">Đơn hàng #${ord.orderId}</span>
                                <span class="order-date">
                                    <fmt:formatDate value="${ord.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                </span>
                                <span class="order-shop">${ord.shopName}</span>
                            </div>
                            <div>
                                <span class="status-badge ${ord.statusBadgeClass}">
                                    ${ord.statusDisplayName}
                                </span>
                            </div>
                        </div>

                        <div class="order-card-body">
                            <c:forEach var="item" items="${ord.items}">
                                <div class="order-item-line">
                                    <img src="${not empty item.imageUrl ? item.imageUrl : pageContext.request.contextPath.concat('/images/product-placeholder.png')}"
                                         alt="${item.productName}" class="item-thumb"
                                         onerror="this.src='${pageContext.request.contextPath}/images/product-placeholder.png'">
                                    <div class="item-details">
                                        <div class="item-name">${item.productName}</div>
                                        <div class="item-subtext">Quy cách: ${item.packagingType} | Số lượng: x${item.quantity}</div>
                                    </div>
                                    <div class="item-price-col">
                                        <fmt:formatNumber value="${item.itemTotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="order-card-footer">
                            <div class="order-total-block">
                                <span class="order-total-label">Tổng thanh toán:</span>
                                <span class="order-total-price">
                                    <fmt:formatNumber value="${ord.totalAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                </span>
                            </div>

                            <div class="order-actions">
                                <!-- Xem chi tiết & Theo dõi tiến trình (Task 8 & 11) -->
                                <a href="${pageContext.request.contextPath}/orders/detail?id=${ord.orderId}" class="btn-action btn-view">
                                    Chi Tiết & Tiến Trình
                                </a>

                                <!-- Mua lại (Task 10: Re-order) -->
                                <form action="${pageContext.request.contextPath}/orders" method="POST" style="margin:0;">
                                    <input type="hidden" name="action" value="reorder">
                                    <input type="hidden" name="orderId" value="${ord.orderId}">
                                    <button type="submit" class="btn-action btn-reorder">
                                        Mua Lại
                                    </button>
                                </form>

                                <!-- Hủy đơn hàng (Task 9: Cancel Order) -->
                                <c:if test="${ord.cancelable}">
                                    <button type="button" class="btn-action btn-cancel" onclick="openCancelModal(${ord.orderId})">
                                        Hủy Đơn
                                    </button>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty-orders">
                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom: 12px;">
                        <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                        <line x1="3" y1="6" x2="21" y2="6"></line>
                        <path d="M16 10a4 4 0 0 1-8 0"></path>
                    </svg>
                    <h3 style="margin: 0 0 8px; font-size: 18px; color: var(--text-dark);">Chưa có đơn hàng nào trong mục này</h3>
                    <p style="color: var(--text-muted); font-size: 14px; margin-bottom: 20px;">Hãy dạo qua cửa hàng để chọn mua những loại hoa quả tươi ngon nhất!</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn-action btn-reorder" style="display:inline-flex;">
                        Khám Phá Cửa Hàng Ngay
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Modal Hủy Đơn Hàng -->
    <div id="cancelModal" class="modal-overlay">
        <div class="modal-content">
            <h3 style="margin-top:0; font-size:18px; color:var(--text-dark);">Xác Nhận Hủy Đơn Hàng</h3>
            <p style="font-size:14px; color:var(--text-muted);">Bạn có chắc chắn muốn hủy đơn hàng này không? Vui lòng cho biết lý do:</p>
            <form action="${pageContext.request.contextPath}/orders" method="POST">
                <input type="hidden" name="action" value="cancel">
                <input type="hidden" name="orderId" id="cancelModalOrderId" value="">
                <div style="margin-bottom: 16px;">
                    <textarea name="cancelReason" rows="3" class="form-control" style="width:100%; box-sizing:border-box; padding:10px; border-radius:6px; border:1px solid var(--border);" placeholder="Ví dụ: Đổi ý, muốn thay đổi địa chỉ hoặc thêm sản phẩm khác..."></textarea>
                </div>
                <div style="display:flex; justify-content:flex-end; gap:10px;">
                    <button type="button" class="btn-action btn-view" onclick="closeCancelModal()">Đóng</button>
                    <button type="submit" class="btn-action btn-cancel">Xác Nhận Hủy</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openCancelModal(orderId) {
            document.getElementById('cancelModalOrderId').value = orderId;
            document.getElementById('cancelModal').style.display = 'flex';
        }

        function closeCancelModal() {
            document.getElementById('cancelModal').style.display = 'none';
        }
    </script>
    <!-- Footer (Reusable Component) -->
    <jsp:include page="/includes/footer.jsp" />

</body>
</html>
