<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">DÀNH CHO CHỦ CỬA HÀNG</p>
<div class="page-heading">
    <div><h1>Mã giảm giá</h1><p class="muted">Tạo ưu đãi theo đơn hàng và theo dõi lượt dùng của khách.</p></div>
    <div class="actions"><a href="${ctx}/shopowner/shop/coupons?action=history">Lịch sử sử dụng</a><a class="button" href="${ctx}/shopowner/shop/coupons?action=create">+ Tạo mã giảm giá</a></div>
</div>
<c:set var="effectiveCouponCount" value="0"/>
<c:set var="couponUseCount" value="0"/>
<c:forEach var="summaryCoupon" items="${coupons}">
    <c:if test="${summaryCoupon.statusLabel == 'Đang áp dụng'}"><c:set var="effectiveCouponCount" value="${effectiveCouponCount + 1}"/></c:if>
    <c:set var="couponUseCount" value="${couponUseCount + summaryCoupon.usedCount}"/>
</c:forEach>
<div class="summary-grid" aria-label="Tổng quan các mã trong danh sách">
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#ticket"/></svg></div><div><strong>${fn:length(coupons)}</strong><span>Mã giảm giá trong danh sách</span></div></article>
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#check"/></svg></div><div><strong>${effectiveCouponCount}</strong><span>Đang áp dụng</span></div></article>
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#cart"/></svg></div><div><strong>${couponUseCount}</strong><span>Lượt dùng của các mã trên</span></div></article>
</div>
<section class="card"><div class="table-wrap" tabindex="0" role="region" aria-label="Bảng dữ liệu, cuộn ngang để xem thêm"><table class="data-table">
    <thead><tr><th scope="col">Mã / Cửa hàng</th><th scope="col">Ưu đãi</th><th scope="col">Thời gian</th><th scope="col">Lượt dùng</th><th scope="col">Trạng thái / Thao tác</th></tr></thead>
    <tbody>
    <c:forEach var="p" items="${coupons}">
        <tr>
            <td><strong><c:out value="${p.code}"/></strong><br><c:out value="${p.name}"/><br><span class="muted"><c:out value="${p.shopName}"/></span></td>
            <td>Giảm <fmt:formatNumber value="${p.discountValue}" maxFractionDigits="2"/> ${p.discountType == 'PERCENT' ? '%' : 'đ'}
                <c:if test="${not empty p.maxDiscount}"><br>Tối đa <fmt:formatNumber value="${p.maxDiscount}" maxFractionDigits="0"/> đ</c:if>
                <br>Đơn từ <fmt:formatNumber value="${p.minOrderAmount}" maxFractionDigits="0"/> đ
            </td>
            <td class="nowrap">${p.startLabel}<br>đến ${p.endLabel}<br><span class="muted">Giờ Việt Nam</span></td>
            <td>${p.usedCount} / ${p.totalLimit}<br><span class="muted">${p.perCustomerLimit} lượt / khách</span></td>
            <td><span class="badge" data-status="<c:out value='${p.statusLabel}'/>"><c:out value="${p.statusLabel}"/></span>
                <div class="actions">
                    <a href="${ctx}/shopowner/shop/coupons?action=detail&amp;id=${p.id}">Chi tiết</a>
                    <a href="${ctx}/shopowner/shop/coupons?action=edit&amp;id=${p.id}">Sửa</a>
                    <form method="post" action="${ctx}/shopowner/shop/coupons">
                        <input type="hidden" name="shopcsrf" value="${sessionScope.shopcsrf}"><input type="hidden" name="id" value="${p.id}"><input type="hidden" name="version" value="${p.version}">
                        <button class="secondary" name="action" value="${p.active ? 'deactivate' : 'activate'}">${p.active ? 'Tắt' : 'Bật'}</button>
                    </form>
                </div>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty coupons}"><tr><td colspan="5" class="empty">Chưa có mã giảm giá. Bấm “Tạo mã giảm giá” để bắt đầu.</td></tr></c:if>
    </tbody>
</table></div></section>
<p class="muted">Mỗi đơn dùng tối đa một mã. Lượt đã gỡ hoặc thuộc đơn đã hủy không tính vào hạn mức.</p>
<%@ include file="footer.jspf" %>
