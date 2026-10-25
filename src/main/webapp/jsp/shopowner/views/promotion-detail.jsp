<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">SHOP OWNER · CHI TIẾT KHUYẾN MÃI</p>
<div class="page-heading">
    <div><h1><c:out value="${promotion.name}"/></h1><p class="muted"><c:out value="${promotion.shopName}"/> · Mã #${promotion.id}</p></div>
    <a class="button" href="${ctx}/shopowner/shop/promotions?action=edit&amp;id=${promotion.id}">Sửa chương trình</a>
</div>
<section class="card">
    <dl>
        <dt>Trạng thái</dt><dd><span class="badge" data-status="<c:out value='${promotion.statusLabel}'/>"><c:out value="${promotion.statusLabel}"/></span></dd>
        <dt>Mức giảm</dt><dd><fmt:formatNumber value="${promotion.discountValue}" maxFractionDigits="2"/> ${promotion.discountType == 'PERCENT' ? '%' : 'đ / đơn vị'}</dd>
        <dt>Bắt đầu</dt><dd>${promotion.startLabel}</dd>
        <dt>Kết thúc</dt><dd>${promotion.endLabel} (UTC+7)</dd>
        <dt>Sản phẩm</dt><dd>${promotion.productCount} sản phẩm, bao gồm các biến thể</dd>
        <dt>Mô tả</dt><dd class="preserve-lines"><c:out value="${empty promotion.description ? 'Chưa có mô tả.' : promotion.description}"/></dd>
    </dl>
    <div class="actions">
        <form method="post" action="${ctx}/shopowner/shop/promotions">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="id" value="${promotion.id}">
            <input type="hidden" name="version" value="${promotion.version}">
            <button class="secondary" name="action" value="${promotion.active ? 'deactivate' : 'activate'}">${promotion.active ? 'Tắt chương trình' : 'Bật chương trình'}</button>
        </form>
        <a href="${ctx}/shopowner/shop/promotions">Về danh sách</a>
    </div>
</section>

<section class="card">
    <h2>Xem thử giá sau giảm</h2>
    <p class="muted">Bảng tính thử dùng giá gốc của từng biến thể, kể cả khi chương trình đang tắt hoặc chưa đến giờ. Số tiền được làm tròn đến đồng; giá sau giảm thấp nhất là 0 đ.</p>
    <div class="table-wrap" tabindex="0" role="region" aria-label="Bảng dữ liệu, cuộn ngang để xem thêm"><table class="data-table">
        <thead><tr><th scope="col">Sản phẩm</th><th scope="col">Biến thể</th><th scope="col">Giá gốc</th><th scope="col">Tiền giảm</th><th scope="col">Giá sau giảm</th></tr></thead>
        <tbody>
            <c:forEach var="row" items="${prices}">
                <tr>
                    <td><c:out value="${row.productName}"/><c:if test="${not row.productActive}"><br><span class="text-error">Đã ngừng bán</span></c:if></td>
                    <td><c:out value="${row.variantName}"/></td>
                    <td class="nowrap"><fmt:formatNumber value="${row.originalPrice}" maxFractionDigits="0"/> đ</td>
                    <td class="nowrap"><fmt:formatNumber value="${row.discountAmount}" maxFractionDigits="0"/> đ</td>
                    <td class="nowrap"><strong><fmt:formatNumber value="${row.finalPrice}" maxFractionDigits="0"/> đ</strong></td>
                </tr>
            </c:forEach>
            <c:if test="${empty prices}"><tr><td colspan="5" class="empty">Chưa có biến thể để xem giá.</td></tr></c:if>
        </tbody>
    </table></div>
</section>
<section class="card">
    <h2>Xóa chương trình</h2>
    <p class="muted">Chương trình sẽ ngừng áp dụng và được gỡ khỏi danh sách quản lý.</p>
    <form method="post" action="${ctx}/shopowner/shop/promotions" class="stack">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="id" value="${promotion.id}">
        <input type="hidden" name="version" value="${promotion.version}">
        <label class="check"><input type="checkbox" required> Tôi muốn xóa chương trình này.</label>
        <button class="danger" type="submit" name="action" value="delete">Xóa chương trình</button>
    </form>
</section>
<%@ include file="footer.jspf" %>
