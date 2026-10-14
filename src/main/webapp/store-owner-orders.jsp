<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quan Ly Don Hang - FreshFruit Store Owner</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .order-container {
            width: 94%;
            max-width: 1200px;
            margin: 30px auto 60px;
        }
        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }
        .page-title {
            font-size: 24px;
            color: #275629;
            margin: 0;
            font-weight: bold;
        }
        .status-tabs {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
            border-bottom: 2px solid #e2ebe0;
            padding-bottom: 8px;
            overflow-x: auto;
        }
        .tab-item {
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 600;
            color: #555;
            background: #f0f4ef;
            transition: all 0.2s;
            white-space: nowrap;
        }
        .tab-item:hover {
            background: #e2ede0;
            color: #275629;
        }
        .tab-item.active {
            background: #275629;
            color: #ffffff;
        }
        .tab-counter {
            background: rgba(255,255,255,0.3);
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 12px;
            margin-left: 6px;
        }
        .tab-item:not(.active) .tab-counter {
            background: #d9e4d6;
            color: #333;
        }
        .search-bar {
            display: flex;
            gap: 12px;
            margin-bottom: 20px;
        }
        .search-input {
            flex: 1;
            max-width: 400px;
            padding: 10px 14px;
            border: 1px solid #ccd8c9;
            border-radius: 6px;
            font-size: 14px;
            outline: none;
        }
        .search-input:focus {
            border-color: #4caf50;
        }
        .btn-search {
            padding: 10px 18px;
            background: #4caf50;
            color: #fff;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
        }
        .btn-search:hover {
            background: #388e3c;
        }
        .orders-table {
            width: 100%;
            border-collapse: collapse;
            background: #ffffff;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 4px 12px rgba(0,0,0,0.03);
            border: 1px solid #e1ebe0;
        }
        .orders-table th {
            background: #f7faf6;
            color: #275629;
            text-align: left;
            padding: 14px 16px;
            font-size: 13px;
            font-weight: 700;
            border-bottom: 1px solid #e1ebe0;
        }
        .orders-table td {
            padding: 14px 16px;
            border-bottom: 1px solid #edf3eb;
            font-size: 13px;
            color: #333;
            vertical-align: middle;
        }
        .orders-table tr:hover {
            background: #fbfdfa;
        }
        .badge {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 14px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.3px;
        }
        .badge-warning {
            background: #fff8e1;
            color: #b78103;
            border: 1px solid #ffe082;
        }
        .badge-info {
            background: #e1f5fe;
            color: #0277bd;
            border: 1px solid #81d4fa;
        }
        .badge-primary {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1px solid #a5d6a7;
        }
        .badge-success {
            background: #e8f8f5;
            color: #00796b;
            border: 1px solid #80cbc4;
        }
        .badge-danger {
            background: #ffebee;
            color: #c62828;
            border: 1px solid #ffcdd2;
        }
        .badge-secondary {
            background: #eceff1;
            color: #546e7a;
            border: 1px solid #cfd8dc;
        }
        .action-btns {
            display: flex;
            gap: 6px;
            flex-wrap: wrap;
        }
        .btn-action {
            padding: 6px 12px;
            font-size: 12px;
            font-weight: 600;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            transition: all 0.2s;
        }
        .btn-detail {
            background: #e0f2f1;
            color: #00695c;
            border: 1px solid #b2dfdb;
        }
        .btn-detail:hover {
            background: #b2dfdb;
        }
        .btn-confirm {
            background: #2e7d32;
            color: #ffffff;
        }
        .btn-confirm:hover {
            background: #1b5e20;
        }
        .btn-ready {
            background: #0288d1;
            color: #ffffff;
        }
        .btn-ready:hover {
            background: #01579b;
        }
        .btn-reject {
            background: #d32f2f;
            color: #ffffff;
        }
        .btn-reject:hover {
            background: #b71c1c;
        }
        .alert {
            padding: 12px 18px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 14px;
            font-weight: 500;
        }
        .alert-success {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1px solid #c8e6c9;
        }
        .alert-error {
            background: #ffebee;
            color: #c62828;
            border: 1px solid #ffcdd2;
        }
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        }
        .modal-card {
            background: #ffffff;
            border-radius: 10px;
            width: 90%;
            max-width: 680px;
            max-height: 85vh;
            overflow-y: auto;
            padding: 26px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.15);
        }
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #edf3eb;
            padding-bottom: 14px;
            margin-bottom: 18px;
        }
        .modal-title {
            font-size: 18px;
            font-weight: bold;
            color: #275629;
            margin: 0;
        }
        .modal-close {
            text-decoration: none;
            font-size: 22px;
            font-weight: bold;
            color: #888;
            line-height: 1;
        }
        .modal-close:hover {
            color: #333;
        }
        .item-detail-row {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 0;
            border-bottom: 1px solid #f2f5f1;
        }
        .item-thumb {
            width: 46px;
            height: 46px;
            object-fit: cover;
            border-radius: 6px;
            border: 1px solid #e1ebe0;
        }
        .timeline-step {
            padding: 8px 12px;
            margin-bottom: 6px;
            background: #f7faf6;
            border-left: 3px solid #4caf50;
            font-size: 12px;
            border-radius: 0 4px 4px 0;
        }
    </style>
</head>
<body>

<header class="navbar">
    <div class="logo">
        FreshFruit
    </div>
    <div class="nav-actions">
        <a href="${pageContext.request.contextPath}/store-owner/dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/store-owner/products">San pham</a>
        <a href="${pageContext.request.contextPath}/store-owner/categories">Danh muc</a>
        <a href="${pageContext.request.contextPath}/store-owner/orders" style="font-weight: bold; color: #fff; background: #275629; padding: 4px 10px; border-radius: 4px;">Don hang</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Dang xuat</a>
    </div>
</header>

<div class="order-container">

    <div class="page-header">
        <div>
            <h1 class="page-title">Quan Ly Don Hang Cua Cua Hang</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Theo doi, xac nhan va cap nhat trang thai don hang trai cay.</p>
        </div>
    </div>

    <!-- THONG BAO ALERT -->
    <c:if test="${not empty param.successMessage}">
        <div class="alert alert-success">
            ${param.successMessage}
        </div>
    </c:if>
    <c:if test="${not empty param.errorMessage}">
        <div class="alert alert-error">
            ${param.errorMessage}
        </div>
    </c:if>

    <!-- STATUS TABS -->
    <div class="status-tabs">
        <a href="${pageContext.request.contextPath}/store-owner/orders?status=all" 
           class="tab-item ${currentStatus == 'all' ? 'active' : ''}">
            Tat ca <span class="tab-counter">${counts.all}</span>
        </a>
        <a href="${pageContext.request.contextPath}/store-owner/orders?status=pending_confirmation" 
           class="tab-item ${currentStatus == 'pending_confirmation' ? 'active' : ''}">
            Cho xac nhan <span class="tab-counter">${counts.pending_confirmation}</span>
        </a>
        <a href="${pageContext.request.contextPath}/store-owner/orders?status=preparing" 
           class="tab-item ${currentStatus == 'preparing' ? 'active' : ''}">
            Dang dong goi <span class="tab-counter">${counts.preparing}</span>
        </a>
        <a href="${pageContext.request.contextPath}/store-owner/orders?status=shipping" 
           class="tab-item ${currentStatus == 'shipping' ? 'active' : ''}">
            Dang giao hang <span class="tab-counter">${counts.shipping}</span>
        </a>
        <a href="${pageContext.request.contextPath}/store-owner/orders?status=delivered" 
           class="tab-item ${currentStatus == 'delivered' ? 'active' : ''}">
            Da giao thanh cong <span class="tab-counter">${counts.delivered}</span>
        </a>
        <a href="${pageContext.request.contextPath}/store-owner/orders?status=cancelled" 
           class="tab-item ${currentStatus == 'cancelled' ? 'active' : ''}">
            Da huy / Tu choi <span class="tab-counter">${counts.cancelled}</span>
        </a>
    </div>

    <!-- SEARCH BAR -->
    <form action="${pageContext.request.contextPath}/store-owner/orders" method="GET" class="search-bar">
        <input type="hidden" name="status" value="${currentStatus}">
        <input type="text" name="q" value="${searchQuery}" class="search-input" 
               placeholder="Tim theo ma don, ten khach hang, SDT...">
        <button type="submit" class="btn-search">Tim kiem</button>
        <c:if test="${not empty searchQuery}">
            <a href="${pageContext.request.contextPath}/store-owner/orders?status=${currentStatus}" 
               style="align-self: center; color: #666; font-size: 13px; text-decoration: underline;">
                Bo loc tim kiem
            </a>
        </c:if>
    </form>

    <!-- BANG DANH SACH DON HANG -->
    <table class="orders-table">
        <thead>
            <tr>
                <th style="width: 70px;">Ma don</th>
                <th style="width: 130px;">Ngay dat</th>
                <th>Khach hang / Nguoi nhan</th>
                <th>San pham</th>
                <th style="width: 120px;">Tong tien</th>
                <th style="width: 130px;">Trang thai</th>
                <th style="width: 220px; text-align: center;">Thao tac</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty orders}">
                    <c:forEach var="o" items="${orders}">
                        <tr>
                            <td><strong>#${o.orderId}</strong></td>
                            <td style="color: #666; font-size: 12px;">
                                <fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </td>
                            <td>
                                <div style="font-weight: bold; color: #275629;">${o.recipientName}</div>
                                <div style="font-size: 12px; color: #666;">SDT: ${o.recipientPhone}</div>
                                <div style="font-size: 12px; color: #888;">${o.recipientAddress}</div>
                            </td>
                            <td>
                                <div style="font-size: 13px;">${o.totalQuantity} san pham</div>
                                <div style="font-size: 12px; color: #888;">
                                    <c:forEach var="item" items="${o.items}" varStatus="status">
                                        <c:if test="${status.index < 2}">
                                            ${item.productName} (${item.packagingType})<c:if test="${!status.last}">, </c:if>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${o.items.size() > 2}">...</c:if>
                                </div>
                            </td>
                            <td>
                                <strong style="color: #275629; font-size: 14px;">
                                    <fmt:formatNumber value="${o.totalAmount}" pattern="#,###"/> d
                                </strong>
                            </td>
                            <td>
                                <span class="badge ${o.statusBadgeClass}">
                                    ${o.statusDisplayName}
                                </span>
                            </td>
                            <td style="text-align: center;">
                                <div class="action-btns" style="justify-content: center;">
                                    <!-- Nut xem chi tiet -->
                                    <a href="${pageContext.request.contextPath}/store-owner/orders?action=detail&orderId=${o.orderId}&status=${currentStatus}" 
                                       class="btn-action btn-detail">
                                        Chi tiet
                                    </a>

                                    <!-- Nut xac nhan don (neu dang cho xac nhan) -->
                                    <c:if test="${o.status == 'pending_confirmation'}">
                                        <form action="${pageContext.request.contextPath}/store-owner/orders" method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="confirm">
                                            <input type="hidden" name="orderId" value="${o.orderId}">
                                            <button type="submit" class="btn-action btn-confirm" 
                                                    onclick="return confirm('Xac nhan duyet don hang #' + ${o.orderId} + '?');">
                                                Duyet don
                                            </button>
                                        </form>

                                        <!-- Nut tu choi don -->
                                        <button type="button" class="btn-action btn-reject" 
                                                onclick="openRejectModal(${o.orderId});">
                                            Tu choi
                                        </button>
                                    </c:if>

                                    <!-- Nut dong goi xong / san sang giao -->
                                    <c:if test="${o.status == 'preparing'}">
                                        <form action="${pageContext.request.contextPath}/store-owner/orders" method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="ready">
                                            <input type="hidden" name="orderId" value="${o.orderId}">
                                            <button type="submit" class="btn-action btn-ready" 
                                                    onclick="return confirm('Xac nhan da dong goi xong va chuyen don #' + ${o.orderId} + ' sang Dang giao?');">
                                                Giao hang
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px; color: #888;">
                            Khong co don hang nao trong muc nay.
                        </td>
                    </tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>

</div>

<!-- MODAL XEM CHI TIET DON HANG -->
<c:if test="${not empty selectedOrder}">
    <div class="modal-overlay" id="detailModal">
        <div class="modal-card">
            <div class="modal-header">
                <h3 class="modal-title">Chi Tiet Don Hang #${selectedOrder.orderId}</h3>
                <a href="${pageContext.request.contextPath}/store-owner/orders?status=${currentStatus}" class="modal-close">&times;</a>
            </div>

            <!-- Thong tin khach hang & giao nhan -->
            <div style="background: #fbfdfa; border: 1px solid #edf3eb; border-radius: 6px; padding: 14px; margin-bottom: 18px; font-size: 13px;">
                <div style="margin-bottom: 6px;">
                    <strong>Nguoi nhan:</strong> ${selectedOrder.recipientName} - <strong>SDT:</strong> ${selectedOrder.recipientPhone}
                </div>
                <div style="margin-bottom: 6px;">
                    <strong>Dia chi giao:</strong> ${selectedOrder.recipientAddress}
                </div>
                <div style="margin-bottom: 6px;">
                    <strong>Ngay dat hang:</strong> <fmt:formatDate value="${selectedOrder.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/>
                </div>
                <div>
                    <strong>Trang thai:</strong> 
                    <span class="badge ${selectedOrder.statusBadgeClass}">${selectedOrder.statusDisplayName}</span>
                </div>
            </div>

            <!-- Danh sach san pham -->
            <div style="font-weight: bold; margin-bottom: 10px; color: #275629; font-size: 14px;">
                Danh Sach Trai Cay (${selectedOrder.items.size()} loai)
            </div>
            <div style="margin-bottom: 20px;">
                <c:forEach var="item" items="${selectedOrder.items}">
                    <div class="item-detail-row">
                        <img src="${not empty item.imageUrl ? item.imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}" 
                             alt="${item.productName}" class="item-thumb"
                             onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                        <div style="flex: 1;">
                            <div style="font-weight: 600; font-size: 13px; color: #2e532f;">${item.productName}</div>
                            <div style="font-size: 12px; color: #777;">Quy cach: ${item.packagingType} (${item.unit})</div>
                        </div>
                        <div style="font-size: 13px; color: #555;">
                            SL: <strong>${item.quantity}</strong> x <fmt:formatNumber value="${item.unitPrice}" pattern="#,###"/> d
                        </div>
                        <div style="font-size: 13px; font-weight: bold; color: #275629; width: 100px; text-align: right;">
                            <fmt:formatNumber value="${item.lineTotal}" pattern="#,###"/> d
                        </div>
                    </div>
                </c:forEach>
            </div>

            <div style="display: flex; justify-content: space-between; border-top: 1px dashed #ccd8c9; padding-top: 12px; margin-bottom: 20px; font-size: 15px;">
                <strong>Tong Tien Thanh Toan:</strong>
                <strong style="color: #275629; font-size: 17px;"><fmt:formatNumber value="${selectedOrder.totalAmount}" pattern="#,###"/> d</strong>
            </div>

            <!-- Lich su tien trinh trang thai -->
            <c:if test="${not empty selectedOrder.historyList}">
                <div style="font-weight: bold; margin-bottom: 10px; color: #275629; font-size: 14px;">
                    Lich Su Cap Nhat Tien Trinh
                </div>
                <div style="margin-bottom: 20px;">
                    <c:forEach var="h" items="${selectedOrder.historyList}">
                        <div class="timeline-step">
                            <strong>Trang thai:</strong> ${h.status} - 
                            <span style="color: #666;">Boi: ${not empty h.changedByName ? h.changedByName : 'He thong'}</span> - 
                            <span style="color: #888;"><fmt:formatDate value="${h.changedAt}" pattern="dd/MM/yyyy HH:mm:ss"/></span>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <div style="text-align: right;">
                <a href="${pageContext.request.contextPath}/store-owner/orders?status=${currentStatus}" 
                   class="btn-action btn-detail" style="padding: 8px 18px; font-size: 13px;">
                    Dong
                </a>
            </div>
        </div>
    </div>
</c:if>

<!-- MODAL TU CHOI DON HANG -->
<div class="modal-overlay" id="rejectModal" style="display: none;">
    <div class="modal-card" style="max-width: 480px;">
        <div class="modal-header">
            <h3 class="modal-title">Tu Choi Don Hang</h3>
            <span class="modal-close" style="cursor: pointer;" onclick="closeRejectModal()">&times;</span>
        </div>

        <form action="${pageContext.request.contextPath}/store-owner/orders" method="POST">
            <input type="hidden" name="action" value="reject">
            <input type="hidden" name="orderId" id="rejectOrderId" value="">

            <div style="margin-bottom: 16px;">
                <p style="font-size: 13px; color: #555; margin-top: 0;">
                    Vui long nhap ly do tu choi don hang de thong bao cho khach hang:
                </p>
                <textarea name="reason" id="rejectReason" rows="3" class="search-input" style="width: 100%; box-sizing: border-box; resize: vertical;" 
                          placeholder="Vi du: Mat hang tam thoi het trai tuoi ngon, khong dap ung giao ngay..." required></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" class="btn-action btn-detail" onclick="closeRejectModal()">Huy bo</button>
                <button type="submit" class="btn-action btn-reject" style="padding: 8px 16px;">Xac Nhan Tu Choi</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openRejectModal(orderId) {
        document.getElementById('rejectOrderId').value = orderId;
        document.getElementById('rejectReason').value = '';
        document.getElementById('rejectModal').style.display = 'flex';
    }

    function closeRejectModal() {
        document.getElementById('rejectModal').style.display = 'none';
    }
</script>

</body>
</html>
