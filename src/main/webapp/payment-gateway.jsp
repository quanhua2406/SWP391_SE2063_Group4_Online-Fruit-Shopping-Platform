<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cổng Thanh Toán Trực Tuyến - Đơn Hàng #${order.orderId} | FreshFruit Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        :root {
            --primary: #10b981;
            --primary-dark: #059669;
            --primary-light: #ecfdf5;
            --vnpay-blue: #005baa;
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

        .gateway-container {
            max-width: 900px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .header-title {
            text-align: center;
            margin-bottom: 30px;
        }

        .header-title h1 {
            font-size: 26px;
            color: var(--text-dark);
            margin-bottom: 8px;
        }

        .header-title p {
            color: var(--text-muted);
            font-size: 14px;
        }

        .card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 24px;
            margin-bottom: 24px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.04);
        }

        .order-summary-box {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: var(--primary-light);
            border: 1px solid #a7f3d0;
            border-radius: 8px;
            padding: 16px 20px;
            margin-bottom: 24px;
        }

        .summary-label {
            font-size: 14px;
            color: #065f46;
            margin-bottom: 4px;
        }

        .summary-value {
            font-size: 18px;
            font-weight: 700;
            color: #065f46;
        }

        .summary-total {
            font-size: 24px;
            font-weight: 800;
            color: #dc2626;
        }

        .payment-methods-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
        }

        @media (max-width: 768px) {
            .payment-methods-grid {
                grid-template-columns: 1fr;
            }
        }

        .method-card {
            border: 2px solid var(--border);
            border-radius: 12px;
            padding: 24px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            transition: all 0.2s;
        }

        .method-card:hover {
            border-color: var(--primary);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.1);
        }

        .method-header {
            margin-bottom: 16px;
        }

        .method-badge {
            display: inline-block;
            padding: 4px 8px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            border-radius: 4px;
            margin-bottom: 8px;
        }

        .badge-vnpay {
            background-color: #e0f2fe;
            color: var(--vnpay-blue);
        }

        .badge-vietqr {
            background-color: #ecfdf5;
            color: var(--primary-dark);
        }

        .method-title {
            font-size: 18px;
            font-weight: 700;
            margin-bottom: 6px;
        }

        .method-desc {
            font-size: 13px;
            color: var(--text-muted);
            line-height: 1.5;
        }

        .qr-image-container {
            text-align: center;
            margin: 16px 0;
            background: #fff;
            padding: 12px;
            border: 1px dashed var(--border);
            border-radius: 8px;
        }

        .qr-image-container img {
            max-width: 190px;
            height: auto;
            border-radius: 4px;
        }

        .bank-details {
            background: var(--bg-light);
            border-radius: 8px;
            padding: 12px;
            font-size: 13px;
            margin-bottom: 16px;
        }

        .bank-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 6px;
        }

        .bank-row:last-child {
            margin-bottom: 0;
        }

        .bank-label {
            color: var(--text-muted);
        }

        .bank-value {
            font-weight: 600;
            color: var(--text-dark);
        }

        .btn-action {
            display: inline-block;
            text-align: center;
            width: 100%;
            padding: 12px 0;
            font-weight: 600;
            font-size: 15px;
            border-radius: 8px;
            text-decoration: none;
            cursor: pointer;
            border: none;
            box-sizing: border-box;
            transition: background 0.2s;
        }

        .btn-vnpay {
            background: var(--vnpay-blue);
            color: #ffffff;
        }

        .btn-vnpay:hover {
            background: #004a8c;
        }

        .btn-vietqr {
            background: var(--primary);
            color: #ffffff;
        }

        .btn-vietqr:hover {
            background: var(--primary-dark);
        }

        .btn-secondary {
            background: #e5e7eb;
            color: #374151;
            margin-top: 10px;
        }

        .btn-secondary:hover {
            background: #d1d5db;
        }

        .instructions-list {
            margin: 12px 0;
            padding-left: 20px;
            font-size: 13px;
            color: var(--text-muted);
            line-height: 1.6;
        }
    </style>
</head>
<body>

    <jsp:include page="/includes/header.jsp" />

    <div class="gateway-container">

        <div class="header-title">
            <h1>Cổng Thanh Toán Trực Tuyến</h1>
            <p>Hệ thống hỗ trợ cổng thanh toán quốc gia VNPay Sandbox & Quét mã VietQR tiện lợi</p>
        </div>

        <div class="card">
            <div class="order-summary-box">
                <div>
                    <div class="summary-label">Mã đơn hàng:</div>
                    <div class="summary-value">#${order.orderId}</div>
                    <div style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">
                        Khách hàng: <strong>${order.recipientName}</strong> | SĐT: ${order.recipientPhone}
                    </div>
                </div>
                <div style="text-align: right;">
                    <div class="summary-label">Số tiền cần thanh toán:</div>
                    <div class="summary-total">
                        <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                    </div>
                </div>
            </div>

            <div class="payment-methods-grid">

                <!-- PHƯƠNG THỨC 1: VNPAY SANDBOX (TASK 1, 2) -->
                <div class="method-card">
                    <div>
                        <div class="method-header">
                            <span class="method-badge badge-vnpay">Cổng Điện Tử VNPay</span>
                            <div class="method-title">Cổng Thanh Toán VNPay</div>
                            <div class="method-desc">
                                Hỗ trợ hơn 34 ngân hàng nội địa (Thẻ ATM), Thẻ quốc tế Visa/Mastercard và quét mã QR ứng dụng ngân hàng.
                            </div>
                        </div>

                        <ul class="instructions-list">
                            <li>Chuyển tiếp trực tiếp sang cổng bảo mật VNPay Sandbox.</li>
                            <li>Sử dụng thông tin thẻ thử nghiệm để xác thực giao dịch.</li>
                            <li>Hệ thống tự động kích hoạt trạng thái đơn hàng khi thành công.</li>
                        </ul>
                    </div>

                    <div>
                        <a href="${vnpayUrl}" class="btn-action btn-vnpay" id="btn-vnpay-direct">
                            Thanh Toán Ngay Qua VNPay
                        </a>
                        <a href="${pageContext.request.contextPath}/payment/confirm-simulated?orderId=${order.orderId}" class="btn-action btn-secondary">
                            Mô phỏng VNPay Thành Công (Sandbox Demo)
                        </a>
                    </div>
                </div>

                <!-- PHƯƠNG THỨC 2: VIETQR CHUYỂN KHOẢN TRỰC TIẾP (TASK 1, 2) -->
                <div class="method-card">
                    <div>
                        <div class="method-header">
                            <span class="method-badge badge-vietqr">Chuyển Khoản 24/7</span>
                            <div class="method-title">Quét Mã VietQR Ngân Hàng</div>
                            <div class="method-desc">
                                Mở ứng dụng ngân hàng bất kỳ để quét mã QR và xác nhận giao dịch tức thì.
                            </div>
                        </div>

                        <div class="qr-image-container">
                            <img src="https://img.vietqr.io/image/970422-999888666888-compact2.png?amount=${order.totalAmount}&addInfo=${qrContent}&accountName=CONG%20TY%20CP%20HOA%20QUA%20SACH%20ONLINE%20FRUIT"
                                 alt="VietQR Payment Code"
                                 onerror="this.src='https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${qrContent}';">
                        </div>

                        <div class="bank-details">
                            <div class="bank-row">
                                <span class="bank-label">Ngân hàng:</span>
                                <span class="bank-value">${bankName}</span>
                            </div>
                            <div class="bank-row">
                                <span class="bank-label">Số tài khoản:</span>
                                <span class="bank-value">${bankAccountNumber}</span>
                            </div>
                            <div class="bank-row">
                                <span class="bank-label">Chủ tài khoản:</span>
                                <span class="bank-value">${bankAccountName}</span>
                            </div>
                            <div class="bank-row">
                                <span class="bank-label">Nội dung chuyển khoản:</span>
                                <span class="bank-value" style="color: #dc2626; font-weight: 700;">${qrContent}</span>
                            </div>
                        </div>
                    </div>

                    <div>
                        <a href="${pageContext.request.contextPath}/payment/confirm-simulated?orderId=${order.orderId}" class="btn-action btn-vietqr">
                            Xác Nhận Đã Chuyển Khoản VietQR
                        </a>
                        <a href="${pageContext.request.contextPath}/orders/detail?id=${order.orderId}" class="btn-action btn-secondary">
                            Xem Lại Chi Tiết Đơn Hàng
                        </a>
                    </div>
                </div>

            </div>
        </div>

    </div>

    <jsp:include page="/includes/footer.jsp" />

</body>
</html>
