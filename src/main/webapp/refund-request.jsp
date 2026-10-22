<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yêu Cầu Hoàn Tiền (Refunds) | FreshFruit Store</title>
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

        .refund-container {
            max-width: 850px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 28px;
            margin-bottom: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        .page-title {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .page-desc {
            color: var(--text-muted);
            font-size: 14px;
            margin-bottom: 24px;
        }

        .alert-error {
            background: #fee2e2;
            color: #991b1b;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }

        .alert-success {
            background: #d1fae5;
            color: #065f46;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }

        .order-info-box {
            background: var(--bg-light);
            border: 1px solid var(--border);
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 24px;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            font-size: 14px;
            margin-bottom: 8px;
        }

        .info-row:last-child {
            margin-bottom: 0;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 8px;
            color: var(--text-dark);
        }

        .form-control {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 14px;
            box-sizing: border-box;
            outline: none;
        }

        .form-control:focus {
            border-color: var(--primary);
        }

        textarea.form-control {
            resize: vertical;
            min-height: 100px;
        }

        .btn-submit {
            background: var(--primary);
            color: #ffffff;
            padding: 12px 24px;
            font-size: 15px;
            font-weight: 600;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            width: 100%;
            transition: background 0.2s;
        }

        .btn-submit:hover {
            background: var(--primary-dark);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }

        th {
            background: var(--bg-light);
            padding: 12px 14px;
            text-align: left;
            font-weight: 600;
            color: var(--text-muted);
            border-bottom: 1px solid var(--border);
        }

        td {
            padding: 14px;
            border-bottom: 1px solid var(--border);
            vertical-align: middle;
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
    </style>
</head>
<body>

    <jsp:include page="/includes/header.jsp" />

    <div class="refund-container">

        <c:if test="${param.success == 'requested'}">
            <div class="alert-success">
                Yêu cầu hoàn tiền của bạn đã được gửi thành công! Quản trị viên và chủ cửa hàng sẽ xem xét và phản hồi trong thời gian sớm nhất.
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert-error">
                ${errorMessage}
            </div>
        </c:if>

        <!-- NẾU CÓ THÔNG TIN ORDER ĐỂ GỬI YÊU CẦU HOÀN TIỀN -->
        <c:if test="${not empty order}">
            <div class="card">
                <h1 class="page-title">Gửi Yêu Cầu Hoàn Tiền - Đơn Hàng #${order.orderId}</h1>
                <p class="page-desc">Chính sách bảo vệ người tiêu dùng tại FreshFruit hỗ trợ hoàn tiền nhanh chóng khi đơn hàng gặp sự cố.</p>

                <div class="order-info-box">
                    <div class="info-row">
                        <span style="color: var(--text-muted);">Mã đơn hàng:</span>
                        <strong>#${order.orderId}</strong>
                    </div>
                    <div class="info-row">
                        <span style="color: var(--text-muted);">Số tiền đã thanh toán:</span>
                        <strong style="color: #dc2626;">
                            <fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                        </strong>
                    </div>
                    <div class="info-row">
                        <span style="color: var(--text-muted);">Phương thức đã thanh toán:</span>
                        <span>${payment.methodDisplayName}</span>
                    </div>
                    <div class="info-row">
                        <span style="color: var(--text-muted);">Trạng thái giao dịch:</span>
                        <span style="color: #065f46; font-weight: 600;">Đã thanh toán (PAID)</span>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/refunds/request" method="POST">
                    <input type="hidden" name="orderId" value="${order.orderId}">

                    <div class="form-group">
                        <label for="reasonCategory">Lý do yêu cầu hoàn tiền (*)</label>
                        <select id="reasonCategory" name="reasonCategory" class="form-control" required>
                            <option value="">-- Vui lòng chọn lý do hoàn tiền --</option>
                            <option value="Đơn hàng bị hủy do hết hàng">Đơn hàng bị hủy do hết hàng tại kho</option>
                            <option value="Sản phẩm dập nát / không đúng chất lượng">Sản phẩm hoa quả dập nát / không đúng chất lượng cam kết</option>
                            <option value="Giao sai loại mặt hàng">Giao sai loại mặt hàng đã đặt</option>
                            <option value="Giao trễ quá thời gian cam kết">Giao trễ quá thời gian cam kết</option>
                            <option value="Khác">Lý do khác</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="reasonNote">Mô tả chi tiết sự cố (*)</label>
                        <textarea id="reasonNote" name="reasonNote" class="form-control" placeholder="Mô tả cụ thể tình trạng hoa quả hoặc lý do để bộ phận hỗ trợ xử lý nhanh chóng..." required></textarea>
                    </div>

                    <div class="form-group">
                        <label>Thông tin số tài khoản nhận tiền hoàn lại (Tùy chọn)</label>
                        <input type="text" name="bankNote" class="form-control" placeholder="Ví dụ: MB Bank - 0987654321 - NGUYEN VAN A">
                        <span style="font-size: 12px; color: var(--text-muted); margin-top: 4px; display: block;">Nếu thanh toán bằng VNPay, hệ thống có thể hoàn trực tiếp về thẻ/ngân hàng nguồn.</span>
                    </div>

                    <button type="submit" class="btn-submit">Xác Nhận Gửi Yêu Cầu Hoàn Tiền</button>
                    <div style="text-align: center; margin-top: 14px;">
                        <a href="${pageContext.request.contextPath}/orders/detail?id=${order.orderId}" style="color: var(--text-muted); font-size: 14px;">Quay lại chi tiết đơn hàng</a>
                    </div>
                </form>
            </div>
        </c:if>

        <!-- DANH SÁCH YÊU CẦU HOÀN TIỀN CỦA KHÁCH HÀNG -->
        <c:if test="${empty order}">
            <div class="card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <div>
                        <h1 class="page-title" style="margin: 0;">Danh Sách Yêu Cầu Hoàn Tiền</h1>
                        <p class="page-desc" style="margin: 4px 0 0;">Theo dõi tiến độ xét duyệt các khoản hoàn tiền của bạn</p>
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/payments" class="btn-sm" style="background: var(--bg-light); border: 1px solid var(--border); padding: 8px 14px; text-decoration: none; color: var(--text-dark); border-radius: 6px; font-weight: 600;">
                            Lịch Sử Giao Dịch
                        </a>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty refundList}">
                        <div style="text-align: center; padding: 40px 20px; color: var(--text-muted);">
                            Bạn chưa có yêu cầu hoàn tiền nào trong hệ thống.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table>
                            <thead>
                                <tr>
                                    <th>Mã Yêu Cầu</th>
                                    <th>Đơn Hàng</th>
                                    <th>Số Tiền</th>
                                    <th>Lý Do</th>
                                    <th>Ngày Gửi</th>
                                    <th>Trạng Thái</th>
                                    <th>Thao Tác</th>
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
                                        <td style="font-weight: 700; color: #dc2626;">
                                            <fmt:formatNumber value="${rf.amount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                        </td>
                                        <td style="max-width: 250px; font-size: 13px;">${rf.reason}</td>
                                        <td>
                                            <fmt:formatDate value="${rf.requestedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                        <td>
                                            <span class="badge ${rf.statusBadgeClass}">${rf.statusDisplayName}</span>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/orders/detail?id=${rf.orderId}" style="font-size: 12px; color: var(--primary); text-decoration: underline;">
                                                Xem đơn
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

    </div>

    <jsp:include page="/includes/footer.jsp" />

</body>
</html>
