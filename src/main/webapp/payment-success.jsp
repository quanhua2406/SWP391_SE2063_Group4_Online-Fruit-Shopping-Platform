<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác Nhận Thanh Toán Thành Công | FreshFruit Store</title>
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
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-light);
            color: var(--text-dark);
            margin: 0;
            padding: 0;
        }

        .success-container {
            max-width: 680px;
            margin: 50px auto;
            padding: 0 20px;
        }

        .receipt-card {
            background: #ffffff;
            border-radius: 16px;
            border: 1px solid var(--border);
            padding: 36px;
            text-align: center;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
        }

        .icon-circle {
            width: 72px;
            height: 72px;
            background-color: #d1fae5;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            color: var(--primary);
        }

        .receipt-title {
            font-size: 24px;
            font-weight: 700;
            color: var(--text-dark);
            margin-bottom: 8px;
        }

        .receipt-subtitle {
            font-size: 14px;
            color: var(--text-muted);
            margin-bottom: 28px;
            line-height: 1.5;
        }

        .receipt-details {
            background: var(--bg-light);
            border-radius: 12px;
            padding: 20px;
            text-align: left;
            margin-bottom: 28px;
            border: 1px solid var(--border);
        }

        .receipt-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px dashed var(--border);
            font-size: 14px;
        }

        .receipt-row:last-child {
            border-bottom: none;
            padding-bottom: 0;
        }

        .receipt-row:first-child {
            padding-top: 0;
        }

        .receipt-label {
            color: var(--text-muted);
        }

        .receipt-value {
            font-weight: 600;
            color: var(--text-dark);
        }

        .receipt-amount {
            font-size: 18px;
            font-weight: 700;
            color: var(--primary-dark);
        }

        .badge-success {
            background-color: #d1fae5;
            color: #065f46;
            padding: 3px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
        }

        .action-buttons {
            display: flex;
            gap: 12px;
            justify-content: center;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s;
            border: none;
            cursor: pointer;
        }

        .btn-primary {
            background-color: var(--primary);
            color: #ffffff;
        }

        .btn-primary:hover {
            background-color: var(--primary-dark);
        }

        .btn-outline {
            background-color: #ffffff;
            color: var(--text-dark);
            border: 1px solid var(--border);
        }

        .btn-outline:hover {
            background-color: var(--bg-light);
        }
    </style>
</head>
<body>

    <jsp:include page="/includes/header.jsp" />

    <div class="success-container">
        <div class="receipt-card">

            <div class="icon-circle">
                <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
            </div>

            <h1 class="receipt-title">Thanh Toán Hoàn Tất!</h1>
            <p class="receipt-subtitle">
                Giao dịch của bạn đã được cổng thanh toán xác nhận thành công.<br>
                Cửa hàng đã nhận được thanh toán và đang chuẩn bị đơn hàng cho bạn.
            </p>

            <div class="receipt-details">
                <div class="receipt-row">
                    <span class="receipt-label">Mã đơn hàng</span>
                    <span class="receipt-value">#${order.orderId}</span>
                </div>
                <div class="receipt-row">
                    <span class="receipt-label">Mã giao dịch (TxnRef)</span>
                    <span class="receipt-value" style="font-family: monospace;">${txnRef != null ? txnRef : payment.transactionRef}</span>
                </div>
                <div class="receipt-row">
                    <span class="receipt-label">Số tiền thanh toán</span>
                    <span class="receipt-value receipt-amount">
                        <fmt:formatNumber value="${payment.amount > 0 ? payment.amount : order.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                    </span>
                </div>
                <div class="receipt-row">
                    <span class="receipt-label">Cổng / Phương thức</span>
                    <span class="receipt-value">${bankCode != null ? bankCode : payment.paymentMethod.toUpperCase()}</span>
                </div>
                <div class="receipt-row">
                    <span class="receipt-label">Trạng thái thanh toán</span>
                    <span class="receipt-value">
                        <span class="badge-success">Đã Thanh Toán (PAID)</span>
                    </span>
                </div>
                <div class="receipt-row">
                    <span class="receipt-label">Trạng thái đơn hàng</span>
                    <span class="receipt-value">Đang Chuẩn Bị (Preparing)</span>
                </div>
            </div>

            <div class="action-buttons">
                <a href="${pageContext.request.contextPath}/orders/detail?id=${order.orderId}" class="btn btn-primary">
                    Xem Chi Tiết & Tiến Trình Đơn Hàng
                </a>
                <a href="${pageContext.request.contextPath}/payments" class="btn btn-outline">
                    Lịch Sử Giao Dịch
                </a>
                <a href="${pageContext.request.contextPath}/home" class="btn btn-outline">
                    Tiếp Tục Mua Sắm
                </a>
            </div>

        </div>
    </div>

    <jsp:include page="/includes/footer.jsp" />

</body>
</html>
