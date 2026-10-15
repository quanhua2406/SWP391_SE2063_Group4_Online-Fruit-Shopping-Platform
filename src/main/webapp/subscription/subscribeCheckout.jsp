<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Xác nhận đăng ký gói ${plan.name} | FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css"/>
</head>
<body class="subscribe-checkout-page">

<!-- Navigation -->
<header class="cust-navbar">
    <a href="${pageContext.request.contextPath}/home" class="brand">
        🍊 <span>FreshFruit</span>
    </a>
    <nav class="nav-links">
        <a href="${pageContext.request.contextPath}/subscriptions" class="nav-link">← Xem các gói khác</a>
        <c:if test="${not empty sessionScope.user}">
            <a href="${pageContext.request.contextPath}/my-subscriptions" class="nav-link">📋 Gói của tôi</a>
            <span style="font-size:0.9rem; font-weight:700; color:var(--primary-900);">
                👤 ${sessionScope.user.fullName}
            </span>
        </c:if>
    </nav>
</header>

<div class="customer-container">

    <!-- Breadcrumb & Page Title -->
    <div style="margin-bottom:24px;">
        <a href="${pageContext.request.contextPath}/subscriptions" style="color:var(--primary-800); font-weight:700; text-decoration:none; font-size:0.9rem;">
            ← Quay lại danh sách gói
        </a>
        <h1 style="font-size:1.85rem; font-weight:800; color:var(--text-title); margin:8px 0 4px;">
            Xác Nhận Đăng Ký Gói Combo
        </h1>
        <p style="color:var(--text-muted); margin:0;">
            Điền lịch giao hàng và địa chỉ để hoàn tất thủ tục đăng ký gói nhận trái cây định kỳ.
        </p>
    </div>

    <!-- Error Alert if any -->
    <c:if test="${not empty errorMessage}">
        <div style="background:#fee2e2; border:1px solid #f87171; color:#991b1b; padding:16px 20px; border-radius:12px; margin-bottom:24px; font-weight:600;">
            ⚠️ ${errorMessage}
            <c:if test="${not empty errorList}">
                <ul style="margin:8px 0 0; padding-left:20px;">
                    <c:forEach var="err" items="${errorList}">
                        <li>${err}</li>
                    </c:forEach>
                </ul>
            </c:if>
        </div>
    </c:if>

    <!-- Checkout Two-Column Grid -->
    <div class="checkout-grid">

        <!-- Left: Form Input -->
        <div>
            <form action="${pageContext.request.contextPath}/subscription/subscribe" method="POST" id="subscribeForm">
                <input type="hidden" name="planId" value="${plan.planId}"/>

                <!-- 1. Chọn ngày giao hàng đầu tiên -->
                <div class="checkout-card">
                    <h3 class="checkout-card-title">
                        <span>📅</span> 1. Lịch giao hàng định kỳ
                    </h3>
                    <div class="date-picker-box">
                        <label for="deliveryDate">Ngày nhận hàng đợt đầu tiên:</label>
                        <input type="date"
                               id="deliveryDate"
                               name="deliveryDate"
                               value="${not empty selectedDate ? selectedDate : defaultDate}"
                               min="${minDate}"
                               required/>
                        <div style="font-size:0.84rem; color:var(--text-muted); margin-top:6px;">
                            💡 Các đợt giao tiếp theo sẽ tự động diễn ra theo chu kỳ <strong>${plan.billingCycleLabel}</strong>.
                            Bạn có thể tạm dừng hoặc thay đổi lịch bất cứ lúc nào.
                        </div>
                    </div>
                </div>

                <!-- 2. Địa chỉ nhận hàng -->
                <div class="checkout-card">
                    <h3 class="checkout-card-title">
                        <span>📍</span> 2. Địa chỉ nhận hàng
                    </h3>

                    <c:choose>
                        <c:when test="${not empty addresses}">
                            <div style="display:flex; flex-direction:column; gap:10px;">
                                <c:forEach var="addr" items="${addresses}" varStatus="status">
                                    <c:set var="isDef" value="${addr.isDefault or addr['default']}"/>
                                    <label class="address-item-radio ${isDef ? 'selected' : ''}">
                                        <input type="radio"
                                               name="selectedAddressId"
                                               value="${addr.addressId}"
                                               ${isDef or status.first ? 'checked' : ''}/>
                                        <div class="address-info">
                                            <strong>${addr.recipientName} • ${addr.phone}</strong>
                                            <c:if test="${isDef}">
                                                <span style="background:#dcfce7; color:#15803d; font-size:0.75rem; font-weight:700; padding:2px 8px; border-radius:10px; margin-left:6px;">
                                                    Mặc định
                                                </span>
                                            </c:if>
                                            <p>${addr.addressLine}, ${addr.ward}, ${addr.district}, ${addr.province}</p>
                                        </div>
                                    </label>
                                </c:forEach>
                            </div>
                            <div style="margin-top:12px;">
                                <a href="${pageContext.request.contextPath}/addresses" target="_blank" style="color:var(--primary-800); font-size:0.85rem; font-weight:700; text-decoration:none;">
                                    + Thêm hoặc quản lý địa chỉ mới
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="background:#fffbeb; border:1px solid #fde68a; padding:14px; border-radius:8px; color:#92400e; font-size:0.9rem; margin-bottom:14px;">
                                ℹ️ Bạn chưa lưu địa chỉ trong sổ địa chỉ.
                                <a href="${pageContext.request.contextPath}/addresses" target="_blank" style="font-weight:700; color:#b45309;">
                                    Thêm địa chỉ ngay
                                </a>
                            </div>
                            <div class="form-group" style="margin-bottom:12px;">
                                <label style="font-size:0.88rem; font-weight:700; display:block; margin-bottom:4px;">Họ tên người nhận:</label>
                                <input type="text" name="recipientName" value="${sessionScope.user.fullName}" style="width:100%; padding:10px; border:1px solid var(--border-medium); border-radius:8px;" required/>
                            </div>
                            <div class="form-group" style="margin-bottom:12px;">
                                <label style="font-size:0.88rem; font-weight:700; display:block; margin-bottom:4px;">Số điện thoại:</label>
                                <input type="text" name="phone" value="${sessionScope.user.phone}" style="width:100%; padding:10px; border:1px solid var(--border-medium); border-radius:8px;" required/>
                            </div>
                            <div class="form-group">
                                <label style="font-size:0.88rem; font-weight:700; display:block; margin-bottom:4px;">Địa chỉ chi tiết (Số nhà, đường, phường, quận):</label>
                                <input type="text" name="addressLine" placeholder="Vd: 123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1" style="width:100%; padding:10px; border:1px solid var(--border-medium); border-radius:8px;" required/>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- 3. Ghi chú giao hàng -->
                <div class="checkout-card">
                    <h3 class="checkout-card-title">
                        <span>📝</span> 3. Ghi chú cho tài xế
                    </h3>
                    <textarea name="notes"
                              rows="3"
                              placeholder="Vd: Vui lòng bấm chuông cửa hoặc giao trong giờ hành chính..."
                              style="width:100%; padding:12px; border:1px solid var(--border-medium); border-radius:8px; font-family:inherit; font-size:0.9rem; box-sizing:border-box;"><c:out value="${notes}"/></textarea>
                </div>

                <!-- 4. Phương thức thanh toán -->
                <div class="checkout-card">
                    <h3 class="checkout-card-title">
                        <span>💳</span> 4. Phương thức thanh toán
                    </h3>
                    <label class="address-item-radio selected" style="margin-bottom:8px;">
                        <input type="radio" name="paymentMethod" value="COD" checked/>
                        <div class="address-info">
                            <strong>💵 Thanh toán khi nhận hàng mỗi kỳ (COD)</strong>
                            <p>Shipper giao trái cây đến nhà, bạn kiểm tra độ tươi ngon rồi mới thanh toán tiền mặt hoặc quét mã QR.</p>
                        </div>
                    </label>
                    <label class="address-item-radio" style="opacity:0.75;">
                        <input type="radio" name="paymentMethod" value="VNPAY" disabled/>
                        <div class="address-info">
                            <strong>💳 Ví điện tử / Thẻ ngân hàng (Đang thử nghiệm)</strong>
                            <p>Tính năng trừ tiền tự động mỗi kỳ đang được kết nối với cổng thanh toán.</p>
                        </div>
                    </label>
                </div>

                <!-- Điều khoản cam kết -->
                <div style="margin-bottom:24px; padding:0 8px;">
                    <label style="display:flex; align-items:flex-start; gap:10px; cursor:pointer; font-size:0.9rem; color:var(--text-body);">
                        <input type="checkbox" required checked style="margin-top:3px;"/>
                        <span>
                            Tôi đồng ý đăng ký nhận trái cây theo chu kỳ <strong>${plan.billingCycleLabel}</strong>.
                            Tôi hiểu rằng mình có thể <strong>Tạm dừng</strong> hoặc <strong>Hủy gói</strong> bất cứ lúc nào trong trang Quản lý gói.
                        </span>
                    </label>
                </div>

                <!-- Submit Button -->
                <button type="submit" class="btn-subscribe-cta" style="width:100%; justify-content:center; padding:16px; font-size:1.1rem; border-radius:12px;">
                    🔒 Xác Nhận Đăng Ký Gói (${plan.billingCycleLabel})
                </button>
            </form>
        </div>

        <!-- Right: Sticky Order Summary Card -->
        <div>
            <div class="checkout-card summary-sticky">
                <div class="summary-plan-header">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <span class="badge-shop">🏬 ${plan.shopName}</span>
                        <c:if test="${savings != null and savings.savingsPercent > 0}">
                            <span class="badge-save">Tiết kiệm ${savings.savingsPercent}%</span>
                        </c:if>
                    </div>
                    <h3>${plan.name}</h3>
                    <p style="font-size:0.88rem; color:var(--text-muted); margin:0;">
                        ${plan.description}
                    </p>
                </div>

                <!-- Combo fruit items breakdown -->
                <div style="margin-bottom:18px;">
                    <div style="font-size:0.8rem; font-weight:700; color:var(--primary-800); text-transform:uppercase; margin-bottom:8px;">
                        🧺 Danh sách trái cây trong mỗi kỳ giao:
                    </div>
                    <div style="display:flex; flex-direction:column; gap:8px;">
                        <c:forEach var="item" items="${plan.items}">
                            <div class="summary-item-line">
                                <span>• ${item.productName}<c:if test="${not empty item.variantName}"> (${item.variantName})</c:if></span>
                                <span style="font-weight:700; color:var(--text-title);">x${item.quantity}</span>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <!-- Price summary calculation -->
                <div style="border-top:1px solid var(--border-light); padding-top:14px;">
                    <c:if test="${savings != null and savings.originalPrice > 0 and savings.savingsAmount > 0}">
                        <div class="summary-item-line">
                            <span style="color:var(--text-muted);">Tổng giá mua lẻ gốc:</span>
                            <span style="text-decoration:line-through; color:var(--text-light);">
                                <fmt:formatNumber value="${savings.originalPrice}" pattern="#,###"/>đ
                            </span>
                        </div>
                        <div class="summary-item-line" style="color:#d97706; font-weight:700;">
                            <span>Ưu đãi combo đăng ký:</span>
                            <span>-<fmt:formatNumber value="${savings.savingsAmount}" pattern="#,###"/>đ</span>
                        </div>
                    </c:if>
                    <div class="summary-item-line">
                        <span style="color:var(--text-muted);">Phí giao hàng:</span>
                        <span style="color:#15803d; font-weight:700;">Miễn phí (0đ)</span>
                    </div>
                    <div class="summary-item-line">
                        <span style="color:var(--text-muted);">Chu kỳ thanh toán:</span>
                        <span style="font-weight:600;">Mỗi ${plan.billingCycleLabel}</span>
                    </div>

                    <div class="summary-total-line">
                        <span>Số tiền mỗi kỳ:</span>
                        <span style="font-size:1.5rem;">
                            <fmt:formatNumber value="${plan.price}" pattern="#,###"/>đ
                        </span>
                    </div>
                </div>

                <!-- Customer Protection Guarantee -->
                <div style="margin-top:20px; background:#F1F8F2; padding:14px; border-radius:10px; font-size:0.82rem; color:var(--primary-900); line-height:1.45;">
                    🛡️ <strong>Cam kết chất lượng:</strong> 100% trái cây thu hoạch loại 1. Được phép kiểm tra chất lượng khi nhận hàng và đổi trả miễn phí trong 24 giờ.
                </div>
            </div>
        </div>

    </div>

</div>

</body>
</html>
