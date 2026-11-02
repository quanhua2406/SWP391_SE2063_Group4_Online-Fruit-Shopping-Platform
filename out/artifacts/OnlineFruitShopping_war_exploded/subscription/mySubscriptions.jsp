<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Gói Đăng Ký Của Tôi | FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css"/>
</head>
<body class="my-subscriptions-page">

<!-- Global Header (Reusable Component) -->
<jsp:include page="/includes/header.jsp" />

<!-- Toast Notifications -->
<c:if test="${not empty sessionScope.toastSuccess}">
    <div class="toast success" id="toast" style="display:block; animation: slideUpToast 0.3s ease;">
        ${sessionScope.toastSuccess}
    </div>
    <c:remove var="toastSuccess" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.toastError}">
    <div class="toast error" id="toast" style="display:block; animation: slideUpToast 0.3s ease;">
        ${sessionScope.toastError}
    </div>
    <c:remove var="toastError" scope="session"/>
</c:if>

<div class="customer-container">

    <!-- Page Header -->
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:24px; flex-wrap:wrap; gap:16px;">
        <div>
            <h1 style="font-size:1.85rem; font-weight:800; color:var(--text-title); margin:0 0 6px;">
                Quản Lý Gói Đăng Ký Định Kỳ
            </h1>
            <p style="color:var(--text-muted); margin:0;">
                Theo dõi các đợt giao trái cây sắp tới, tạm dừng hoặc hủy gói bất cứ lúc nào.
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/subscriptions" class="btn-subscribe-cta" style="font-size:0.9rem; padding:10px 18px;">
            + Đăng Ký Thêm Gói Mới
        </a>
    </div>

    <!-- Statistics Overview Cards -->
    <div class="my-subs-stats">
        <div class="my-sub-stat-card active">
            <div class="val">${countActive}</div>
            <div class="lbl">Gói đang nhận trái cây</div>
        </div>
        <div class="my-sub-stat-card">
            <div class="val" style="font-size:1.25rem; color:#15803d; line-height:1.4;">
                <c:choose>
                    <c:when test="${not empty nextDeliveryDate}">
                        <fmt:formatDate value="${nextDeliveryDate}" pattern="dd/MM/yyyy"/>
                    </c:when>
                    <c:otherwise>Chưa có lịch</c:otherwise>
                </c:choose>
            </div>
            <div class="lbl">Đợt giao gần nhất</div>
        </div>
        <div class="my-sub-stat-card paused">
            <div class="val">${countPaused}</div>
            <div class="lbl">Gói đang tạm dừng</div>
        </div>
        <div class="my-sub-stat-card">
            <div class="val">${totalCount}</div>
            <div class="lbl">Tổng số gói đã đăng ký</div>
        </div>
    </div>

    <!-- Filter Tabs -->
    <div class="subs-tab-filter">
        <a href="?status=all" class="subs-tab ${currentStatus == 'all' ? 'active' : ''}">
            Tất cả (${totalCount})
        </a>
        <a href="?status=active" class="subs-tab ${currentStatus == 'active' ? 'active' : ''}">
            Đang hoạt động (${countActive})
        </a>
        <a href="?status=paused" class="subs-tab ${currentStatus == 'paused' ? 'active' : ''}">
            Đang tạm dừng (${countPaused})
        </a>
        <a href="?status=cancelled" class="subs-tab ${currentStatus == 'cancelled' ? 'active' : ''}">
            Đã hủy (${countCancelled})
        </a>
    </div>

    <!-- Subscriptions List -->
    <c:choose>
        <c:when test="${empty subscriptions}">
            <div class="cust-empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px;">
                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                </svg>
                <h3>Chưa có gói đăng ký nào ở mục này</h3>
                <p>Khám phá các combo trái cây tươi ngon theo tuần hoặc tháng để nhận ưu đãi tiết kiệm tới 25%.</p>
                <a href="${pageContext.request.contextPath}/subscriptions" class="btn-subscribe-cta">
                    Khám Phá Gói Combo Ngay
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach var="s" items="${subscriptions}">
                <div class="my-sub-card ${s.status}">

                    <!-- Left: Plan Info & Combo Fruit Items -->
                    <div>
                        <div style="display:flex; align-items:center; gap:10px; margin-bottom:8px;">
                            <span class="${s.statusBadgeClass}">${s.statusLabel}</span>
                            <span class="badge-shop">${s.shopName != null ? s.shopName : 'Shop'}</span>
                            <span style="font-size:0.82rem; color:var(--text-muted);">
                                Mã đơn: #${s.subscription_id}
                            </span>
                        </div>

                        <h3 style="font-size:1.3rem; font-weight:800; color:var(--text-title); margin:0 0 6px;">
                            ${s.planName != null ? s.planName : 'Gói Combo Trái Cây'}
                        </h3>
                        <p style="font-size:0.88rem; color:var(--text-muted); margin:0 0 14px;">
                            ${s.planDescription}
                        </p>

                        <!-- Items in combo -->
                        <div class="combo-items-box" style="margin-bottom:0;">
                            <div class="combo-items-label">
                                Trái cây nhận mỗi kỳ:
                            </div>
                            <div class="combo-items-list">
                                <c:choose>
                                    <c:when test="${not empty s.items}">
                                        <c:forEach var="it" items="${s.items}">
                                            <div class="combo-item-row">
                                                <span>• ${it.productName}<c:if test="${not empty it.variantName}"> (${it.variantName})</c:if></span>
                                                <span class="qty-badge">x${it.quantity}</span>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="combo-item-row">
                                            <span>• ${s.productName}<c:if test="${not empty s.variantName}"> (${s.variantName})</c:if></span>
                                            <span class="qty-badge">x1</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Customer preferences & notes -->
                        <c:if test="${not empty s.notes}">
                            <div style="margin-top:12px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px 14px; font-size:0.83rem; color:#334155;">
                                <div style="font-weight:700; color:#0f172a; margin-bottom:4px;">
                                    Tùy chọn & Ghi chú đã đăng ký:
                                </div>
                                <div style="white-space:pre-line; line-height:1.5;"><c:out value="${s.notes}"/></div>
                            </div>
                        </c:if>
                    </div>

                    <!-- Middle: Schedule Timeline Box -->
                    <div>
                        <div class="sub-schedule-box">
                            <div style="font-size:0.8rem; font-weight:700; color:var(--text-muted); text-transform:uppercase;">
                                Đợt giao hàng tiếp theo:
                            </div>
                            <div class="sub-schedule-date">
                                <c:choose>
                                    <c:when test="${s.active and not empty s.next_delivery_date}">
                                        <fmt:formatDate value="${s.next_delivery_date}" pattern="dd/MM/yyyy (EEEE)"/>
                                    </c:when>
                                    <c:when test="${s.paused}">
                                        <span style="color:#d97706; font-size:0.95rem;">Đang tạm dừng nhận hàng</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color:#94a3b8; font-size:0.95rem;">Đã ngừng lịch giao</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div style="margin-top:12px; font-size:0.85rem; color:var(--text-body); border-top:1px dashed #c8e6c9; padding-top:8px;">
                                <div><strong>Chu kỳ:</strong> ${s.frequencyLabel}</div>
                                <div><strong>Số tiền mỗi kỳ:</strong>
                                    <span style="color:#15803d; font-weight:800;">
                                        <fmt:formatNumber value="${s.price_snapshot}" pattern="#,###"/>đ
                                    </span>
                                </div>
                                <c:if test="${not empty s.end_date}">
                                    <div style="color:var(--text-muted); font-size:0.8rem; margin-top:2px;">
                                        Hạn kỳ hiện tại: <fmt:formatDate value="${s.end_date}" pattern="dd/MM/yyyy"/>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <!-- Right: Action Buttons (Pause / Resume / Cancel) -->
                    <div class="sub-card-actions">
                        <c:choose>
                            <c:when test="${s.active}">
                                <!-- Nút Tạm Dừng -->
                                <form action="${pageContext.request.contextPath}/my-subscriptions" method="POST"
                                      onsubmit="return confirm('Bạn có chắc chắn muốn TẠM DỪNG nhận trái cây cho gói này không? Bạn có thể bật lại bất cứ lúc nào.');">
                                    <input type="hidden" name="action" value="pause"/>
                                    <input type="hidden" name="id" value="${s.subscription_id}"/>
                                    <button type="submit" class="btn-sub-action btn-sub-pause" style="width:100%;">
                                        Tạm Dừng Gói
                                    </button>
                                </form>

                                <!-- Nút Hủy Gói -->
                                <form action="${pageContext.request.contextPath}/my-subscriptions" method="POST"
                                      onsubmit="return confirm('Bạn có chắc chắn muốn HỦY gói đăng ký này không? Bạn sẽ không nhận thêm đợt giao trái cây nào nữa.');">
                                    <input type="hidden" name="action" value="cancel"/>
                                    <input type="hidden" name="id" value="${s.subscription_id}"/>
                                    <button type="submit" class="btn-sub-action btn-sub-cancel" style="width:100%;">
                                        Hủy Gói
                                    </button>
                                </form>
                            </c:when>

                            <c:when test="${s.paused}">
                                <!-- Nút Tiếp Tục Nhận Hàng -->
                                <form action="${pageContext.request.contextPath}/my-subscriptions" method="POST"
                                      onsubmit="return confirm('KÍCH HOẠT LẠI gói: Lịch giao trái cây định kỳ sẽ được tiếp tục diễn ra bình thường.');">
                                    <input type="hidden" name="action" value="resume"/>
                                    <input type="hidden" name="id" value="${s.subscription_id}"/>
                                    <button type="submit" class="btn-sub-action btn-sub-resume" style="width:100%;">
                                        Tiếp Tục Nhận
                                    </button>
                                </form>

                                <!-- Nút Hủy Gói -->
                                <form action="${pageContext.request.contextPath}/my-subscriptions" method="POST"
                                      onsubmit="return confirm('Bạn có chắc chắn muốn HỦY hoàn toàn gói đăng ký này?');">
                                    <input type="hidden" name="action" value="cancel"/>
                                    <input type="hidden" name="id" value="${s.subscription_id}"/>
                                    <button type="submit" class="btn-sub-action btn-sub-cancel" style="width:100%;">
                                        Hủy Gói
                                    </button>
                                </form>
                            </c:when>

                            <c:otherwise>
                                <span style="font-size:0.84rem; color:var(--text-light); text-align:center;">
                                    Gói đã đóng
                                </span>
                                <c:if test="${not empty s.plan_id}">
                                    <a href="${pageContext.request.contextPath}/subscription/subscribe?planId=${s.plan_id}"
                                       class="btn-sub-action"
                                       style="background:var(--primary-100); color:var(--primary-900); text-decoration:none;">
                                        Đăng ký lại
                                    </a>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>

</div>

<script>
    setTimeout(function() {
        const toast = document.getElementById('toast');
        if (toast) toast.style.display = 'none';
    }, 4000);
</script>

<!-- Footer (Reusable Component) -->
<jsp:include page="/includes/footer.jsp" />

</body>
</html>
