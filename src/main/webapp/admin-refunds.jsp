<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Phê Duyệt Hoàn Tiền | FreshFruit Admin</title>
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

        .admin-container {
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
        }

        .filter-select {
            padding: 8px 12px;
            border: 1px solid var(--border);
            border-radius: 6px;
            font-size: 14px;
            outline: none;
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
            padding: 12px 14px;
            font-weight: 600;
            color: var(--text-muted);
            text-transform: uppercase;
            font-size: 12px;
        }

        td {
            padding: 14px;
            border-bottom: 1px solid var(--border);
            vertical-align: middle;
        }

        tr:last-child td {
            border-bottom: none;
        }

        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
        }

        .badge-warning {
            background: #fef3c7;
            color: #92400e;
        }

        .badge-info {
            background: #dbeafe;
            color: #1e40af;
        }

        .badge-success {
            background: #d1fae5;
            color: #065f46;
        }

        .badge-danger {
            background: #fee2e2;
            color: #991b1b;
        }

        .btn-action {
            display: inline-block;
            padding: 6px 12px;
            font-size: 12px;
            font-weight: 600;
            border-radius: 6px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: opacity 0.2s;
        }

        .btn-approve {
            background-color: var(--primary);
            color: #ffffff;
        }

        .btn-reject {
            background-color: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }

        .btn-approve:hover, .btn-reject:hover {
            opacity: 0.85;
        }

        .alert-success {
            background: #d1fae5;
            color: #065f46;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }
    </style>
</head>
<body>

    <jsp:include page="/includes/header.jsp" />

    <div class="admin-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">Quản Lý Xét Duyệt Hoàn Tiền</h1>
                <p style="color: var(--text-muted); font-size: 14px; margin: 4px 0 0;">
                    Xét duyệt và xử lý các khoản hoàn tiền trực tuyến cho khách hàng
                </p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/payments" class="btn-action" style="background: var(--bg-light); border: 1px solid var(--border); padding: 8px 14px; color: var(--text-dark);">
                    Xem Lịch Sử Giao Dịch
                </a>
            </div>
        </div>

        <c:if test="${param.success == 'processed'}">
            <div class="alert-success">
                Thao tác xét duyệt yêu cầu hoàn tiền đã được cập nhật thành công vào hệ thống.
            </div>
        </c:if>

        <div class="filter-card">
            <form action="${pageContext.request.contextPath}/admin/refunds" method="GET" class="filter-form">
                <label style="font-size: 14px; font-weight: 600;">Lọc theo trạng thái:</label>
                <select name="status" class="filter-select">
                    <option value="all" ${statusFilter == 'all' ? 'selected' : ''}>-- Tất cả yêu cầu --</option>
                    <option value="requested" ${statusFilter == 'requested' ? 'selected' : ''}>Chờ xét duyệt (Requested)</option>
                    <option value="approved" ${statusFilter == 'approved' ? 'selected' : ''}>Đã chấp thuận (Approved)</option>
                    <option value="rejected" ${statusFilter == 'rejected' ? 'selected' : ''}>Đã từ chối (Rejected)</option>
                </select>
                <button type="submit" class="btn-filter">Lọc</button>
            </form>
        </div>

        <div class="table-card">
            <c:choose>
                <c:when test="${empty refundList}">
                    <div style="text-align: center; padding: 48px 20px; color: var(--text-muted);">
                        Không có yêu cầu hoàn tiền nào trong danh sách.
                    </div>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th>Mã Refund</th>
                                <th>Đơn Hàng</th>
                                <th>Khách Hàng</th>
                                <th>Phương Thức</th>
                                <th>Số Tiền</th>
                                <th>Lý Do Hoàn</th>
                                <th>Ngày Gửi</th>
                                <th>Trạng Thái</th>
                                <th style="text-align: right;">Xét Duyệt</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="rf" items="${refundList}">
                                <tr>
                                    <td><strong>#${rf.refundId}</strong></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/orders/detail?id=${rf.orderId}" style="color: var(--primary); font-weight: 600; text-decoration: none;">
                                            #${rf.orderId}
                                        </a>
                                    </td>
                                    <td>
                                        <div style="font-weight: 600;">${rf.customerName}</div>
                                        <div style="font-size: 12px; color: var(--text-muted);">${rf.customerPhone}</div>
                                    </td>
                                    <td>${rf.paymentMethod.toUpperCase()}</td>
                                    <td style="font-weight: 700; color: #dc2626;">
                                        <fmt:formatNumber value="${rf.amount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                    </td>
                                    <td style="max-width: 220px; font-size: 13px;">${rf.reason}</td>
                                    <td>
                                        <fmt:formatDate value="${rf.requestedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </td>
                                    <td>
                                        <span class="badge ${rf.statusBadgeClass}">${rf.statusDisplayName}</span>
                                    </td>
                                    <td style="text-align: right; white-space: nowrap;">
                                        <c:choose>
                                            <c:when test="${rf.status == 'requested'}">
                                                <form action="${pageContext.request.contextPath}/admin/refunds" method="POST" style="display: inline-block; margin-right: 4px;">
                                                    <input type="hidden" name="action" value="approve">
                                                    <input type="hidden" name="refundId" value="${rf.refundId}">
                                                    <button type="submit" class="btn-action btn-approve" onclick="return confirm('Bạn có chắc chắn muốn chấp thuận hoàn lại khoản tiền này không?');">
                                                        Duyệt hoàn tiền
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/admin/refunds" method="POST" style="display: inline-block;">
                                                    <input type="hidden" name="action" value="reject">
                                                    <input type="hidden" name="refundId" value="${rf.refundId}">
                                                    <button type="submit" class="btn-action btn-reject" onclick="return confirm('Bạn có chắc chắn muốn từ chối yêu cầu này không?');">
                                                        Từ chối
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="font-size: 12px; color: var(--text-muted);">
                                                    Đã xử lý bởi <strong>${not empty rf.processedByName ? rf.processedByName : 'Admin'}</strong>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
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
