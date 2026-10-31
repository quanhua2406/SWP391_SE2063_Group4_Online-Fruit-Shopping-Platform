<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">DÀNH CHO KHÁCH HÀNG</p>
<h1>Đơn hàng &amp; mã giảm giá</h1>
<p class="muted">Áp dụng một mã của cửa hàng cho đơn đang chờ xác nhận và chưa thanh toán. Kiểm tra mã để xem mức giảm trước khi áp dụng.</p>
<c:if test="${not empty formError}"><p class="alert error" role="alert"><c:out value="${formError}"/></p></c:if>
<c:if test="${not empty quote}">
    <section class="card">
        <h2>Mã hợp lệ cho đơn #${selectedOrderId}: <c:out value="${quote.code}"/></h2>
        <p>Trước giảm: <fmt:formatNumber value="${quote.subtotal}" maxFractionDigits="0"/> đ · Giảm: <fmt:formatNumber value="${quote.discountAmount}" maxFractionDigits="0"/> đ</p>
        <p><strong>Sau giảm: <fmt:formatNumber value="${quote.finalAmount}" maxFractionDigits="0"/> đ</strong></p>
        <p class="muted">Chưa thay đổi đơn hoặc giữ lượt. Bấm “Áp dụng” ở đơn bên dưới; mã sẽ được kiểm tra lại lúc lưu.</p>
    </section>
</c:if>
<section class="card"><div class="table-wrap" tabindex="0" role="region" aria-label="Bảng dữ liệu, cuộn ngang để xem thêm"><table class="data-table">
    <thead><tr><th scope="col">Đơn / Shop</th><th scope="col">Trạng thái</th><th scope="col">Tổng tiền hiện tại</th><th scope="col">Mã giảm giá</th></tr></thead>
    <tbody>
    <c:forEach var="o" items="${orders}">
        <tr>
            <td><strong>Đơn #${o.id}</strong><br><c:out value="${o.shopName}"/></td>
            <td><c:out value="${o.orderStatus}"/><br><c:out value="${o.paymentStatus}"/></td>
            <td class="nowrap"><strong><fmt:formatNumber value="${o.totalAmount}" maxFractionDigits="0"/> đ</strong>
                <c:if test="${not empty o.couponId}"><br><span class="muted">Đã giảm <fmt:formatNumber value="${o.couponDiscount}" maxFractionDigits="0"/> đ</span></c:if>
            </td>
            <td>
                <c:choose>
                    <c:when test="${not empty o.couponId}">
                        <p><strong><c:out value="${o.couponCode}"/></strong></p>
                        <c:if test="${o.editable}"><form method="post" action="${ctx}/shopowner/customer/coupons">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="orderId" value="${o.id}">
                            <button class="secondary" name="action" value="remove">Gỡ mã</button>
                        </form></c:if>
                    </c:when>
                    <c:when test="${not empty o.voucherId}"><span class="muted">Đơn đã có voucher cũ; không cộng dồn mã.</span></c:when>
                    <c:when test="${o.editable}">
                        <form method="post" action="${ctx}/shopowner/customer/coupons" class="stack">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="orderId" value="${o.id}">
                            <label for="code-${o.id}">Nhập mã của cửa hàng</label>
                            <input id="code-${o.id}" name="code" required minlength="3" maxlength="30" pattern="[A-Za-z0-9_-]{3,30}" placeholder="Ví dụ: SHOP20K" value="<c:out value='${selectedOrderId == o.id ? param.code : ""}'/>">
                            <div class="actions"><button class="secondary" name="action" value="preview">Kiểm tra mã</button><button name="action" value="apply">Áp dụng</button></div>
                        </form>
                    </c:when>
                    <c:otherwise><span class="muted">Đơn này không còn được thêm hoặc gỡ mã.</span></c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty orders}"><tr><td colspan="4" class="empty">Bạn chưa có đơn tại cửa hàng đang hoạt động.</td></tr></c:if>
    </tbody>
</table></div></section>
<%@ include file="footer.jspf" %>
