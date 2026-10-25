<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">CHỦ CỬA HÀNG · CHI TIẾT MÃ GIẢM GIÁ</p>
<div class="page-heading"><div><h1><c:out value="${coupon.code}"/></h1><p><c:out value="${coupon.name}"/></p></div><a class="button" href="${ctx}/shopowner/shop/coupons?action=edit&amp;id=${coupon.id}">Sửa mã</a></div>
<section class="card">
    <dl>
        <dt>Cửa hàng</dt><dd><c:out value="${coupon.shopName}"/></dd>
        <dt>Trạng thái</dt><dd><span class="badge" data-status="<c:out value='${coupon.statusLabel}'/>"><c:out value="${coupon.statusLabel}"/></span></dd>
        <dt>Mức giảm</dt><dd><fmt:formatNumber value="${coupon.discountValue}" maxFractionDigits="2"/> ${coupon.discountType == 'PERCENT' ? '%' : 'đ / đơn'}
            <c:if test="${not empty coupon.maxDiscount}">, tối đa <fmt:formatNumber value="${coupon.maxDiscount}" maxFractionDigits="0"/> đ</c:if>
        </dd>
        <dt>Đơn tối thiểu</dt><dd><fmt:formatNumber value="${coupon.minOrderAmount}" maxFractionDigits="0"/> đ</dd>
        <dt>Thời gian</dt><dd>${coupon.startLabel} → ${coupon.endLabel} (UTC+7)</dd>
        <dt>Lượt sử dụng</dt><dd>${coupon.usedCount} / ${coupon.totalLimit}; còn ${coupon.remaining} lượt</dd>
        <dt>Mỗi khách</dt><dd>Tối đa ${coupon.perCustomerLimit} lượt</dd>
        <dt>Mô tả</dt><dd class="preserve-lines"><c:out value="${empty coupon.description ? 'Chưa có mô tả.' : coupon.description}"/></dd>
    </dl>
    <div class="actions">
        <form method="post" action="${ctx}/shopowner/shop/coupons">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${coupon.id}"><input type="hidden" name="version" value="${coupon.version}">
            <button class="secondary" name="action" value="${coupon.active ? 'deactivate' : 'activate'}">${coupon.active ? 'Tắt mã' : 'Bật mã'}</button>
        </form>
        <a href="${ctx}/shopowner/shop/coupons">Về danh sách</a>
    </div>
</section>
<section class="card"><h2>Lịch sử sử dụng mã này</h2><%@ include file="coupon-usage-table.jspf" %></section>
<section class="card"><h2>Xóa mã giảm giá</h2><p class="muted">Khách không thể dùng mã cho đơn mới. Ưu đãi đã lưu vào đơn và lịch sử vẫn được giữ lại.</p>
    <form method="post" action="${ctx}/shopowner/shop/coupons" class="stack">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${coupon.id}"><input type="hidden" name="version" value="${coupon.version}">
        <label class="check"><input type="checkbox" required> Tôi muốn xóa mã này.</label><button class="danger" name="action" value="delete">Xóa mã</button>
    </form>
</section>
<%@ include file="footer.jspf" %>
