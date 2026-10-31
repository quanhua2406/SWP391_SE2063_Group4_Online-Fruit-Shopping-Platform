<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi Tiết & Tiến Trình Đơn Hàng #${order.orderId} | FreshFruit Store</title>
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

        .detail-container {
            max-width: 1000px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 24px;
            margin-bottom: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        /* Order Tracking Stepper (Task 11) */
        .stepper-container {
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: relative;
            margin: 30px 0 20px;
        }

        .stepper-progress-bg {
            position: absolute;
            top: 20px;
            left: 5%;
            right: 5%;
            height: 4px;
            background: #e5e7eb;
            z-index: 1;
        }

        .stepper-progress-bar {
            height: 100%;
            background: var(--primary);
            transition: width 0.4s ease;
        }

        .step-point {
            position: relative;
            z-index: 2;
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 25%;
            text-align: center;
        }

        .step-circle {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #fff;
            border: 3px solid #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            color: #9ca3af;
            transition: all 0.3s;
        }

        .step-point.active .step-circle {
            border-color: var(--primary);
            color: var(--primary);
            background: var(--primary-light);
        }

        .step-point.completed .step-circle {
            border-color: var(--primary);
            background: var(--primary);
            color: #fff;
        }

        .step-label {
            font-size: 13px;
            font-weight: 600;
            margin-top: 8px;
            color: var(--text-dark);
        }

        .step-desc {
            font-size: 11px;
            color: var(--text-muted);
            margin-top: 2px;
        }

        /* Info Grid */
        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }

        @media (max-width: 768px) {
            .info-grid {
                grid-template-columns: 1fr;
            }
        }

        .info-block {
            background: var(--bg-light);
            padding: 16px;
            border-radius: 8px;
            border: 1px solid var(--border);
        }

        .info-title {
            font-size: 14px;
            font-weight: 700;
            color: var(--text-dark);
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .info-text {
            font-size: 14px;
            color: var(--text-dark);
            line-height: 1.5;
        }

        /* Table */
        .items-table {
            width: 100%;
            border-collapse: collapse;
            margin: 16px 0;
        }

        .items-table th {
            text-align: left;
            padding: 12px;
            background: var(--bg-light);
            font-size: 13px;
            font-weight: 600;
            color: var(--text-muted);
            border-bottom: 1px solid var(--border);
        }

        .items-table td {
            padding: 14px 12px;
            border-bottom: 1px solid var(--border);
            font-size: 14px;
        }

        .thumb {
            width: 50px;
            height: 50px;
            border-radius: 6px;
            object-fit: cover;
            border: 1px solid var(--border);
        }

        /* Action Buttons */
        .action-bar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid var(--border);
        }

        .btn-action {
            padding: 10px 20px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            border: 1px solid transparent;
            transition: all 0.2s;
        }

        .btn-back {
            background: #ffffff;
            border-color: var(--border);
            color: var(--text-dark);
        }

        .btn-reorder {
            background: var(--primary);
            color: #ffffff;
        }

        .btn-cancel {
            background: #fee2e2;
            color: var(--danger);
            border-color: #fecaca;
        }
    </style>
</head>
<body>

    <!-- Header Navigation (Reusable Component) -->
    <jsp:include page="/includes/header.jsp" />

    <div class="detail-container">

        <c:if test="${param.success == 'placed'}">
            <div style="background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; padding: 16px; border-radius: 8px; margin-bottom: 24px; font-size: 15px; display: flex; align-items: center; gap: 10px;">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                <div>
                    <strong>Đặt hàng thành công!</strong> Cảm ơn bạn đã lựa chọn hoa quả tươi tại FreshFruit. Mã đơn hàng của bạn là <strong>#${order.orderId}</strong>.
                </div>
            </div>
        </c:if>

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

        <!-- CARD 1: TIẾN TRÌNH THEO DÕI ĐƠN HÀNG (TASK 11: TRACK ORDER STATUS) -->
        <div class="card">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
                <div>
                    <h2 style="margin: 0; font-size: 20px; font-weight: 700;">Hành Trình Đơn Hàng #${order.orderId}</h2>
                    <div style="color: var(--text-muted); font-size: 13px; margin-top: 4px;">
                        Đặt lúc: <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/> | Cửa hàng: <strong>${order.shopName}</strong>
                    </div>
                </div>
                <div>
                    <span class="status-badge ${order.statusBadgeClass}" style="padding: 6px 14px; font-size: 13px;">
                        ${order.statusDisplayName}
                    </span>
                </div>
            </div>

            <!-- Stepper timeline nếu đơn không bị hủy -->
            <c:choose>
                <c:when test="${order.status == 'cancelled'}">
                    <div style="background: #fee2e2; border-left: 4px solid var(--danger); padding: 16px; border-radius: 6px; margin-top: 16px;">
                        <strong style="color: var(--danger);">Đơn hàng đã bị hủy</strong>
                        <div style="color: var(--text-dark); font-size: 14px; margin-top: 4px;">
                            Đơn hàng này không còn hiệu lực thực hiện. Bạn có thể bấm nút "Mua lại" bên dưới để đặt lại các mặt hàng vào giỏ.
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="stepper-container">
                        <div class="stepper-progress-bg">
                            <div class="stepper-progress-bar" style="width: 
                                <c:choose>
                                    <c:when test="${order.status == 'pending_confirmation'}">12%</c:when>
                                    <c:when test="${order.status == 'preparing'}">45%</c:when>
                                    <c:when test="${order.status == 'shipping'}">78%</c:when>
                                    <c:when test="${order.status == 'delivered'}">100%</c:when>
                                    <c:otherwise>0%</c:otherwise>
                                </c:choose>
                            ;"></div>
                        </div>

                        <!-- Step 1 -->
                        <div class="step-point ${order.status == 'pending_confirmation' || order.status == 'preparing' || order.status == 'shipping' || order.status == 'delivered' ? 'completed' : ''}">
                            <div class="step-circle">1</div>
                            <div class="step-label">Đặt Hàng Thành Công</div>
                            <div class="step-desc"><fmt:formatDate value="${order.createdAt}" pattern="HH:mm dd/MM"/></div>
                        </div>

                        <!-- Step 2 -->
                        <div class="step-point ${order.status == 'preparing' || order.status == 'shipping' || order.status == 'delivered' ? 'completed' : (order.status == 'pending_confirmation' ? 'active' : '')}">
                            <div class="step-circle">2</div>
                            <div class="step-label">Duyệt & Đóng Gói</div>
                            <div class="step-desc">Chuẩn bị hoa quả tươi</div>
                        </div>

                        <!-- Step 3 -->
                        <div class="step-point ${order.status == 'shipping' || order.status == 'delivered' ? 'completed' : (order.status == 'preparing' ? 'active' : '')}">
                            <div class="step-circle">3</div>
                            <div class="step-label">Đang Giao Hàng</div>
                            <div class="step-desc">
                                <c:choose>
                                    <c:when test="${not empty order.deliveryStaffName}">
                                        ${order.deliveryStaffName} (${order.deliveryStaffPhone})
                                    </c:when>
                                    <c:otherwise>Shipper vận chuyển</c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Step 4 -->
                        <div class="step-point ${order.status == 'delivered' ? 'completed' : (order.status == 'shipping' ? 'active' : '')}">
                            <div class="step-circle">4</div>
                            <div class="step-label">Giao Thành Công</div>
                            <div class="step-desc">Hoàn tất nhận hàng</div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- CARD 2: CHI TIẾT THÔNG TIN GIAO HÀNG & THANH TOÁN (TASK 8) -->
        <div class="card">
            <h3 style="margin-top:0; font-size:16px; border-bottom:1px solid var(--border); padding-bottom:10px;">
                Thông Tin Giao Nhận & Thanh Toán
            </h3>
            <div class="info-grid">
                <div class="info-block">
                    <div class="info-title">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                            <circle cx="12" cy="10" r="3"></circle>
                        </svg>
                        Địa Chỉ Nhận Hàng
                    </div>
                    <div class="info-text">
                        <strong>${order.recipientName}</strong> | SĐT: ${order.recipientPhone}<br>
                        ${order.recipientAddress}
                    </div>
                </div>

                <div class="info-block">
                    <div class="info-title">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                            <line x1="12" y1="8" x2="12" y2="16"></line>
                        </svg>
                        Hình Thức Thanh Toán & Vận Chuyển
                    </div>
                    <div class="info-text">
                        Thanh toán: <strong>
                            <c:choose>
                                <c:when test="${order.paymentMethod == 'vnpay'}">Thanh toán trực tuyến VNPay / QR</c:when>
                                <c:otherwise>Thanh toán khi nhận hàng (COD)</c:otherwise>
                            </c:choose>
                        </strong><br>
                        Phương thức giao: <strong>${order.deliveryMethod}</strong><br>
                        <c:if test="${not empty order.voucherCode}">
                            Mã ưu đãi đã dùng: <span style="color:var(--primary-dark); font-weight:600;">${order.voucherCode}</span>
                        </c:if>
                    </div>
                </div>
            </div>

            <!-- DANH SÁCH SẢN PHẨM TRONG ĐƠN -->
            <h3 style="margin-top:24px; font-size:16px; border-bottom:1px solid var(--border); padding-bottom:10px;">
                Danh Sách Hoa Quả Đặt Mua
            </h3>
            <table class="items-table">
                <thead>
                    <tr>
                        <th style="width: 70px;">Hình ảnh</th>
                        <th>Loại hoa quả & Quy cách</th>
                        <th style="text-align: right;">Đơn giá</th>
                        <th style="text-align: center;">Số lượng</th>
                        <th style="text-align: right;">Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${order.items}">
                        <tr>
                            <td>
                                <img src="${not empty item.imageUrl ? item.imageUrl : pageContext.request.contextPath.concat('/images/product-placeholder.png')}"
                                     alt="${item.productName}" class="thumb"
                                     onerror="this.src='${pageContext.request.contextPath}/images/product-placeholder.png'">
                            </td>
                            <td>
                                <div style="font-weight:600; color:var(--text-dark);">${item.productName}</div>
                                <div style="font-size:12px; color:var(--text-muted);">Quy cách: ${item.packagingType} | Đơn vị: ${item.unit}</div>
                            </td>
                            <td style="text-align: right;">
                                <fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                            </td>
                            <td style="text-align: center; font-weight: 600;">
                                x${item.quantity}
                            </td>
                            <td style="text-align: right; font-weight: 700; color: var(--text-dark);">
                                <fmt:formatNumber value="${item.itemTotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <!-- BẢNG TỔNG TIỀN -->
            <div style="max-width: 320px; margin-left: auto; margin-top: 20px;">
                <div style="display:flex; justify-content:space-between; margin-bottom:8px; font-size:14px; color:var(--text-muted);">
                    <span>Tổng tiền sản phẩm:</span>
                    <span style="font-weight:600; color:var(--text-dark);">
                        <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </span>
                </div>
                <div style="display:flex; justify-content:space-between; padding-top:12px; border-top:2px dashed var(--border); font-size:18px; font-weight:700;">
                    <span>Tổng thanh toán:</span>
                    <span style="color:#dc2626; font-size:20px;">
                        <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                    </span>
                </div>
            </div>

            <!-- ACTION BAR: HỦY ĐƠN (TASK 9), MUA LẠI (TASK 10) -->
            <div class="action-bar">
                <a href="${pageContext.request.contextPath}/orders" class="btn-action btn-back">
                    &larr; Danh Sách Đơn Hàng
                </a>

                <div style="display: flex; gap: 10px;">
                    <!-- Mua lại (Re-order) -->
                    <form action="${pageContext.request.contextPath}/orders" method="POST" style="margin:0;">
                        <input type="hidden" name="action" value="reorder">
                        <input type="hidden" name="orderId" value="${order.orderId}">
                        <button type="submit" class="btn-action btn-reorder">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="23 4 23 10 17 10"></polyline>
                                <polyline points="1 20 1 14 7 14"></polyline>
                                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                            </svg>
                            Mua Lại Đơn Hàng Này (Re-order)
                        </button>
                    </form>

                    <!-- Thanh toán trực tuyến nếu chưa thanh toán -->
                    <c:if test="${order.paymentStatus == 'pending' && (order.paymentMethod == 'vnpay' || order.paymentMethod == 'online')}">
                        <a href="${pageContext.request.contextPath}/payment/process?orderId=${order.orderId}" class="btn-action btn-reorder" style="background-color: var(--primary); color: #fff; text-decoration: none; text-align: center;">
                            Thanh Toán Ngay (VNPay / VietQR)
                        </a>
                    </c:if>

                    <!-- Yêu cầu hoàn tiền nếu đã thanh toán -->
                    <c:if test="${order.paymentStatus == 'paid'}">
                        <a href="${pageContext.request.contextPath}/refunds/request?orderId=${order.orderId}" class="btn-action" style="background-color: #fef3c7; color: #92400e; border: 1px solid #fde68a; text-decoration: none; text-align: center; font-weight: 600;">
                            Yêu Cầu Hoàn Tiền (Refund)
                        </a>
                    </c:if>

                    <!-- Hủy đơn (Cancel Order) -->
                    <c:if test="${order.cancelable}">
                        <button type="button" class="btn-action btn-cancel" onclick="document.getElementById('cancelModal').style.display='flex'">
                            Hủy Đơn Hàng
                        </button>
                    </c:if>
                </div>
            </div>

        </div>

    </div>

    <!-- Modal Hủy Đơn Hàng -->
    <div id="cancelModal" style="display:none; position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.5); z-index:1000; align-items:center; justify-content:center;">
        <div style="background:#fff; border-radius:12px; max-width:480px; width:90%; padding:24px; box-shadow:0 10px 25px rgba(0,0,0,0.2);">
            <h3 style="margin-top:0; font-size:18px; color:var(--text-dark);">Xác Nhận Hủy Đơn Hàng #${order.orderId}</h3>
            <p style="font-size:14px; color:var(--text-muted);">Bạn có chắc chắn muốn hủy đơn hàng này không? Vui lòng cho biết lý do:</p>
            <form action="${pageContext.request.contextPath}/orders" method="POST">
                <input type="hidden" name="action" value="cancel">
                <input type="hidden" name="orderId" value="${order.orderId}">
                <div style="margin-bottom: 16px;">
                    <textarea name="cancelReason" rows="3" style="width:100%; box-sizing:border-box; padding:10px; border-radius:6px; border:1px solid var(--border);" placeholder="Ví dụ: Đổi ý, muốn đổi sản phẩm khác..."></textarea>
                </div>
                <div style="display:flex; justify-content:flex-end; gap:10px;">
                    <button type="button" class="btn-action btn-back" onclick="document.getElementById('cancelModal').style.display='none'">Đóng</button>
                    <button type="submit" class="btn-action btn-cancel">Xác Nhận Hủy</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Footer (Reusable Component) -->
    <jsp:include page="/includes/footer.jsp" />

</body>
</html>
