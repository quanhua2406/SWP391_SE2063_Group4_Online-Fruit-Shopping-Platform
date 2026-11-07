<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch Sử Giao Dịch Thanh Toán | FreshFruit Store</title>
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
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-light);
            color: var(--text-dark);
            margin: 0;
            padding: 0;
        }

        .history-container {
            max-width: 1100px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
        }

        .page-title {
            font-size: 24px;
            font-weight: 700;
            color: var(--text-dark);
            margin: 0;
        }

        .filter-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 16px 20px;
            margin-bottom: 20px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        .filter-form {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
        }

        .filter-input {
            padding: 8px 12px;
            border: 1px solid var(--border);
            border-radius: 6px;
            font-size: 14px;
            outline: none;
        }

        .filter-input:focus {
            border-color: var(--primary);
        }

        .btn-filter {
            background-color: var(--primary);
            color: #ffffff;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s;
        }

        .btn-filter:hover {
            background-color: var(--primary-dark);
        }

        .table-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            overflow: hidden;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
            text-align: left;
        }

        thead {
            background-color: var(--bg-light);
            border-bottom: 1px solid var(--border);
        }

        th {
            padding: 12px 16px;
            font-weight: 600;
            color: var(--text-muted);
            text-transform: uppercase;
            font-size: 12px;
            letter-spacing: 0.5px;
        }

        td {
            padding: 14px 16px;
            border-bottom: 1px solid var(--border);
            vertical-align: middle;
        }

        tr:last-child td {
            border-bottom: none;
        }

        tr:hover {
            background-color: #fafafa;
        }

        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
        }

        .badge-success {
            background-color: #d1fae5;
            color: #065f46;
        }

        .badge-warning {
            background-color: #fef3c7;
            color: #92400e;
        }

        .badge-danger {
            background-color: #fee2e2;
            color: #991b1b;
        }

        .badge-secondary {
            background-color: #f3f4f6;
            color: #4b5563;
        }

        .amount-text {
            font-weight: 700;
            color: #111827;
        }

        .txn-ref {
            font-family: monospace;
            font-size: 12px;
            color: var(--text-muted);
        }

        .btn-sm {
            padding: 6px 12px;
            font-size: 12px;
            font-weight: 600;
            border-radius: 6px;
            text-decoration: none;
            display: inline-block;
            cursor: pointer;
            border: none;
        }

        .btn-pay {
            background-color: var(--primary);
            color: #ffffff;
        }

        .btn-pay:hover {
            background-color: var(--primary-dark);
        }

        .btn-refund {
            background-color: #fef3c7;
            color: #92400e;
            border: 1px solid #fde68a;
        }

        .btn-refund:hover {
            background-color: #fde68a;
        }

        .btn-detail {
            background-color: #f3f4f6;
            color: #374151;
        }

        .btn-detail:hover {
            background-color: #e5e7eb;
        }

        .empty-box {
            padding: 48px 20px;
            text-align: center;
            color: var(--text-muted);
        }
    </style>
</head>
<body>

    <jsp:include page="/includes/header.jsp" />

    <div class="history-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">
                    <c:choose>
                        <c:when test="${isStaffOrAdmin}">Quản Lý Toàn Bộ Giao Dịch Thanh Toán</c:when>
                        <c:otherwise>Lịch Sử Giao Dịch Thanh Toán Của Tôi</c:otherwise>
                    </c:choose>
                </h1>
                <p style="color: var(--text-muted); font-size: 14px; margin: 4px 0 0;">
                    Theo dõi trạng thái thanh toán, biên nhận giao dịch VNPay và yêu cầu hoàn tiền
                </p>
            </div>
            <div>
                <c:if test="${!isStaffOrAdmin}">
                    <a href="${pageContext.request.contextPath}/refunds" class="btn-sm btn-refund" style="padding: 9px 14px; font-size: 13px;">
                        Danh Sách Yêu Cầu Hoàn Tiền
                    </a>
                </c:if>
                <c:if test="${isStaffOrAdmin}">
                    <a href="${pageContext.request.contextPath}/admin/refunds" class="btn-sm btn-refund" style="padding: 9px 14px; font-size: 13px;">
                        Quản Lý Hoàn Tiền (Refunds)
                    </a>
                </c:if>
            </div>
        </div>

        <c:if test="${isStaffOrAdmin}">
            <div class="filter-card">
                <form action="${pageContext.request.contextPath}/payments" method="GET" class="filter-form">
                    <select name="status" class="filter-input">
                        <option value="all" ${statusFilter == 'all' ? 'selected' : ''}>-- Tất cả trạng thái --</option>
                        <option value="paid" ${statusFilter == 'paid' ? 'selected' : ''}>Đã thanh toán (Paid)</option>
                        <option value="pending" ${statusFilter == 'pending' ? 'selected' : ''}>Chờ thanh toán (Pending)</option>
                        <option value="failed" ${statusFilter == 'failed' ? 'selected' : ''}>Thất bại (Failed)</option>
                    </select>

                    <select name="method" class="filter-input">
                        <option value="all" ${methodFilter == 'all' ? 'selected' : ''}>-- Tất cả phương thức --</option>
                        <option value="vnpay" ${methodFilter == 'vnpay' ? 'selected' : ''}>VNPay Sandbox</option>
                        <option value="cod" ${methodFilter == 'cod' ? 'selected' : ''}>Tiền mặt (COD)</option>
                    </select>

                    <input type="text" name="q" value="${searchQuery}" placeholder="Mã đơn hàng, tên khách hàng..." class="filter-input" style="flex: 1; min-width: 200px;">

                    <button type="submit" class="btn-filter">Lọc Giao Dịch</button>
                    <a href="${pageContext.request.contextPath}/payments" style="font-size: 13px; color: var(--text-muted); text-decoration: underline;">Đặt lại</a>
                </form>
            </div>
        </c:if>

        <div class="table-card">
            <c:choose>
                <c:when test="${empty paymentList}">
                    <div class="empty-box">
                        <div style="font-size: 16px; font-weight: 600; margin-bottom: 6px;">Không tìm thấy giao dịch nào</div>
                        <p style="font-size: 14px; margin: 0;">Bạn chưa có giao dịch thanh toán nào phù hợp với bộ lọc.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th>Mã GD</th>
                                <th>Đơn Hàng</th>
                                <c:if test="${isStaffOrAdmin}">
                                    <th>Khách Hàng</th>
                                </c:if>
                                <th>Phương Thức</th>
                                <th>Số Tiền</th>
                                <th>Trạng Thái</th>
                                <th>Mã Tham Chiếu (TxnRef)</th>
                                <th>Thời Gian</th>
                                <th style="text-align: right;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${paymentList}">
                                <tr>
                                    <td><strong>#${p.paymentId}</strong></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/orders/detail?id=${p.orderId}" style="color: var(--primary); font-weight: 600; text-decoration: none;">
                                            #${p.orderId}
                                        </a>
                                        <c:if test="${not empty p.shopName}">
                                            <div style="font-size: 11px; color: var(--text-muted);">${p.shopName}</div>
                                        </c:if>
                                    </td>
                                    <c:if test="${isStaffOrAdmin}">
                                        <td>
                                            <div style="font-weight: 600;">${p.customerName}</div>
                                            <div style="font-size: 12px; color: var(--text-muted);">${p.customerPhone}</div>
                                        </td>
                                    </c:if>
                                    <td>
                                        <span class="badge badge-secondary">${p.methodDisplayName}</span>
                                    </td>
                                    <td class="amount-text">
                                        <fmt:formatNumber value="${p.amount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                    </td>
                                    <td>
                                        <span class="badge ${p.statusBadgeClass}">${p.statusDisplayName}</span>
                                    </td>
                                    <td>
                                        <span class="txn-ref">${not empty p.transactionRef ? p.transactionRef : 'Chưa có'}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty p.paidAt}">
                                                <fmt:formatDate value="${p.paidAt}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:when>
                                            <c:otherwise>
                                                <fmt:formatDate value="${p.orderCreatedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right; white-space: nowrap;">
                                        <c:if test="${p.status == 'pending' && (p.method == 'vnpay' || p.method == 'online')}">
                                            <a href="${pageContext.request.contextPath}/payment/process?orderId=${p.orderId}" class="btn-sm btn-pay" style="margin-right: 4px;">
                                                Thanh toán
                                            </a>
                                        </c:if>
                                        <c:if test="${p.status == 'paid' && !isStaffOrAdmin}">
                                            <a href="${pageContext.request.contextPath}/refunds/request?orderId=${p.orderId}" class="btn-sm btn-refund" style="margin-right: 4px;">
                                                Hoàn tiền
                                            </a>
                                        </c:if>
                                        <a href="${pageContext.request.contextPath}/orders/detail?id=${p.orderId}" class="btn-sm btn-detail">
                                            Xem Đơn
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>

    </div>

    <jsp:include page="/includes/footer.jsp" />

</body>
</html>
