<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">DÀNH CHO CHỦ CỬA HÀNG</p>
<div class="page-heading">
    <div><h1>Chương trình khuyến mãi</h1><p class="muted">Quản lý mức giảm, sản phẩm và thời gian áp dụng của cửa hàng.</p></div>
    <a class="button" href="${ctx}/shopowner/shop/promotions?action=create">+ Tạo chương trình</a>
</div>
<c:set var="effectiveCount" value="0"/>
<c:set var="scheduledCount" value="0"/>
<c:forEach var="summaryPromotion" items="${promotions}">
    <c:if test="${summaryPromotion.statusLabel == 'Đang áp dụng'}"><c:set var="effectiveCount" value="${effectiveCount + 1}"/></c:if>
    <c:if test="${summaryPromotion.statusLabel == 'Chưa đến giờ'}"><c:set var="scheduledCount" value="${scheduledCount + 1}"/></c:if>
</c:forEach>
<div class="summary-grid" aria-label="Tổng quan khuyến mãi">
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#gift"/></svg></div><div><strong>${fn:length(promotions)}</strong><span>Chương trình khuyến mãi</span></div></article>
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#check"/></svg></div><div><strong>${effectiveCount}</strong><span>Đang áp dụng</span></div></article>
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#calendar"/></svg></div><div><strong>${scheduledCount}</strong><span>Sắp bắt đầu</span></div></article>
</div>
<section class="card">
    <p class="muted">Một sản phẩm chỉ thuộc một chương trình đang bật trong cùng khoảng thời gian tại mỗi shop. Giờ hiển thị: Việt Nam (UTC+7).</p>
    <div class="table-wrap" tabindex="0" role="region" aria-label="Bảng dữ liệu, cuộn ngang để xem thêm">
        <table class="data-table">
            <thead><tr><th scope="col">Chương trình / Shop</th><th scope="col">Mức giảm</th><th scope="col">Thời gian</th><th scope="col">Trạng thái</th><th scope="col">Thao tác</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${promotions}">
                <tr>
                    <td><strong><c:out value="${p.name}"/></strong><br><c:out value="${p.shopName}"/><br><span class="muted">#${p.id} · ${p.productCount} sản phẩm</span></td>
                    <td class="nowrap"><fmt:formatNumber value="${p.discountValue}" maxFractionDigits="2"/>
                        <c:choose><c:when test="${p.discountType == 'PERCENT'}">%</c:when><c:otherwise>đ / đơn vị</c:otherwise></c:choose>
                    </td>
                    <td class="nowrap">${p.startLabel}<br>đến ${p.endLabel}</td>
                    <td><span class="badge" data-status="<c:out value='${p.statusLabel}'/>"><c:out value="${p.statusLabel}"/></span></td>
                    <td>
                        <div class="actions">
                            <a href="${ctx}/shopowner/shop/promotions?action=detail&amp;id=${p.id}">Chi tiết &amp; giá</a>
                            <a href="${ctx}/shopowner/shop/promotions?action=edit&amp;id=${p.id}">Sửa</a>
                            <form method="post" action="${ctx}/shopowner/shop/promotions">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${p.id}">
                                <input type="hidden" name="version" value="${p.version}">
                                <c:choose>
                                    <c:when test="${p.active}"><button class="secondary" name="action" value="deactivate">Tắt</button></c:when>
                                    <c:otherwise><button name="action" value="activate">Bật</button></c:otherwise>
                                </c:choose>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty promotions}"><tr><td colspan="5" class="empty">Chưa có chương trình khuyến mãi. Bấm “Tạo chương trình” để bắt đầu.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="footer.jspf" %>
