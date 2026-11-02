<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">DÀNH CHO CHỦ CỬA HÀNG</p>
<h1>Quản lý giao hàng</h1>
<p class="muted">Phân công nhân viên và theo dõi từng lần giao của cửa hàng.</p>
<div class="summary-grid" aria-label="Tổng quan giao hàng">
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#box"/></svg></div><div><strong>${fn:length(orders)}</strong><span>Đơn chờ phân công</span></div></article>
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#truck"/></svg></div><div><strong>${fn:length(staff)}</strong><span>Nhân viên đang hoạt động</span></div></article>
    <article class="summary-card"><div class="dashboard-icon" aria-hidden="true"><svg class="icon" width="28" height="28" aria-hidden="true" focusable="false"><use href="${ctx}/assets/icons.svg#clipboard"/></svg></div><div><strong>${deliveryTotal}</strong><span>Tổng số lần giao</span></div></article>
</div>
<section class="card">
    <h2>Đơn chờ phân công</h2>
    <p class="muted">Gồm đơn đã xác nhận chưa có người giao, đơn bị từ chối và đơn cần giao lại sau thất bại.</p>
    <div class="table-wrap" tabindex="0" role="region" aria-label="Bảng dữ liệu, cuộn ngang để xem thêm">
    <table class="data-table">
        <thead><tr><th scope="col">Đơn / Cửa hàng</th><th scope="col">Người nhận / Địa chỉ</th><th scope="col">Tổng tiền</th><th scope="col">Phân công</th></tr></thead>
        <tbody>
        <c:forEach var="o" items="${orders}">
            <tr>
                <td><strong>#${o.id}</strong><br><c:out value="${o.shopName}"/></td>
                <td><c:out value="${o.receiverName}"/><br><span class="muted"><c:out value="${o.shippingAddress}"/></span></td>
                <td class="nowrap"><fmt:formatNumber value="${o.totalAmount}" maxFractionDigits="0"/> đ</td>
                <td>
                    <c:set var="hasStaff" value="false"/>
                    <form method="post" action="${ctx}/shopowner/shop/deliveries" class="inline-form">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="orderId" value="${o.id}">
                        <select name="shipperId" required aria-label="Nhân viên giao đơn ${o.id}">
                            <option value="">Chọn nhân viên</option>
                            <c:forEach var="s" items="${staff}">
                                <c:if test="${s.shopId == o.shopId}">
                                    <c:set var="hasStaff" value="true"/>
                                    <option value="${s.id}"><c:out value="${s.fullName}"/></option>
                                </c:if>
                            </c:forEach>
                        </select>
                        <c:if test="${hasStaff}"><button type="submit">Phân công</button></c:if>
                        <c:if test="${not hasStaff}"><span class="muted">Shop chưa có nhân viên hoạt động.</span></c:if>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty orders}"><tr><td colspan="4" class="empty">Không có đơn chờ phân công.</td></tr></c:if>
        </tbody>
    </table>
    </div>
</section>
<section class="card" id="delivery-list">
    <h2>Danh sách các lần giao</h2>
    <p class="muted">Các lần giao bị từ chối hoặc thất bại được giữ lại để tra cứu.</p>
    <%@ include file="delivery-table.jspf" %>
</section>
<%@ include file="footer.jspf" %>
