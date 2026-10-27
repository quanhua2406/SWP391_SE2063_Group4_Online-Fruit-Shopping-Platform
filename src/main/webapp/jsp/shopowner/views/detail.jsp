<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<c:choose>
    <c:when test="${requestScope.currentRole == 'SHOPOWNER'}"><a href="${ctx}/shopowner/shop/deliveries">← Quản lý giao hàng</a></c:when>
    <c:otherwise><a href="${ctx}/shopowner/delivery/list">← Giao hàng của tôi</a></c:otherwise>
</c:choose>
<h1>Lần giao #${delivery.id} <span class="badge" data-status="<c:out value='${delivery.status}'/>"><c:out value="${delivery.statusLabel}"/></span></h1>
<p class="muted">Đơn hàng #${delivery.orderId} · <c:out value="${delivery.shopName}"/></p>
<div class="grid">
    <section class="card">
        <h2>Thông tin giao hàng</h2>
        <dl>
            <dt>Nơi lấy hàng</dt><dd><c:out value="${delivery.pickupAddress}"/></dd>
            <dt>Người nhận</dt><dd><c:out value="${delivery.receiverName}"/></dd>
            <dt>Điện thoại</dt><dd><c:out value="${delivery.receiverPhone}"/></dd>
            <dt>Nơi giao hàng</dt><dd><c:out value="${delivery.shippingAddress}"/></dd>
            <dt>Ghi chú đơn</dt><dd><c:out value="${delivery.orderNote}" default="Không có"/></dd>
        </dl>
    </section>
    <section class="card">
        <h2>Tiến độ và thanh toán</h2>
        <dl>
            <dt>Nhân viên</dt><dd><c:out value="${delivery.shipperName}" default="Chưa phân công"/></dd>
            <dt>Thanh toán</dt><dd><c:out value="${delivery.paymentMethod}"/> · ${delivery.paymentStatus == 'Paid' ? 'Đã thanh toán' : 'Chưa thanh toán'}</dd>
            <dt>Tổng tiền</dt><dd><strong><fmt:formatNumber value="${delivery.totalAmount}" maxFractionDigits="0"/> đ</strong></dd>
            <dt>Cần thu COD</dt>
            <dd><c:choose><c:when test="${delivery.paymentMethod == 'COD' and delivery.paymentStatus != 'Paid'}"><fmt:formatNumber value="${delivery.totalAmount}" maxFractionDigits="0"/> đ</c:when><c:otherwise>0 đ</c:otherwise></c:choose></dd>
            <dt>Đã lấy lúc</dt><dd><fmt:formatDate value="${delivery.pickupTime}" pattern="dd/MM/yyyy HH:mm"/><c:if test="${empty delivery.pickupTime}">Chưa lấy hàng</c:if></dd>
            <dt>Đã giao lúc</dt><dd><fmt:formatDate value="${delivery.deliveredTime}" pattern="dd/MM/yyyy HH:mm"/><c:if test="${empty delivery.deliveredTime}">Chưa hoàn tất</c:if></dd>
            <dt>Ghi chú lần giao</dt><dd><c:out value="${delivery.note}" default="Không có"/></dd>
        </dl>
    </section>
</div>
<section class="card">
    <h2>Sản phẩm trong đơn</h2>
    <div class="table-wrap" tabindex="0" role="region" aria-label="Bảng dữ liệu, cuộn ngang để xem thêm">
    <table class="data-table">
        <thead><tr><th scope="col">Sản phẩm</th><th scope="col">Quy cách</th><th scope="col">Đơn giá</th><th scope="col">Số lượng</th><th scope="col">Thành tiền</th></tr></thead>
        <tbody>
        <c:forEach var="item" items="${items}">
            <tr><td><c:out value="${item.productName}"/></td><td><c:out value="${item.variantName}"/></td>
                <td><fmt:formatNumber value="${item.price}" maxFractionDigits="0"/> đ</td><td>${item.quantity}</td>
                <td><fmt:formatNumber value="${item.subtotal}" maxFractionDigits="0"/> đ</td></tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
    <p class="muted">Tổng tiền đơn phía trên đã tính giảm giá theo dữ liệu của cửa hàng.</p>
</section>
<c:if test="${requestScope.currentRole == 'DELIVERYSTAFF'}">
<section class="card">
    <h2>Cập nhật tiến độ</h2>
    <c:choose>
        <c:when test="${delivery.status == 'Assigned'}">
            <p>Kiểm tra địa chỉ và sản phẩm trước khi nhận đơn.</p>
            <form action="${ctx}/shopowner/delivery/update" method="post" class="action-form">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="id" value="${delivery.id}">
                <button name="action" value="accept" type="submit">Nhận đơn</button>
            </form>
            <form action="${ctx}/shopowner/delivery/update" method="post" class="stack narrow">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="id" value="${delivery.id}">
                <label for="rejectReason">Lý do từ chối</label>
                <textarea id="rejectReason" name="reason" required maxlength="255" rows="2" placeholder="Ví dụ: Không thể nhận đơn trong ca này"></textarea>
                <button class="danger" name="action" value="reject" type="submit">Từ chối đơn</button>
            </form>
        </c:when>
        <c:when test="${delivery.status == 'Accepted'}">
            <p>Đến cửa hàng nhận đủ hàng rồi xác nhận bên dưới.</p>
            <form action="${ctx}/shopowner/delivery/update" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="id" value="${delivery.id}">
                <button name="action" value="pickup" type="submit">Đã lấy hàng tại shop</button>
            </form>
        </c:when>
        <c:when test="${delivery.status == 'PickedUp'}">
            <p>Bắt đầu di chuyển đến địa chỉ người nhận.</p>
            <form action="${ctx}/shopowner/delivery/update" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="id" value="${delivery.id}">
                <button name="action" value="start" type="submit">Bắt đầu giao hàng</button>
            </form>
        </c:when>
        <c:when test="${delivery.status == 'Delivering'}">
            <form action="${ctx}/shopowner/delivery/update" method="post" class="stack narrow action-form">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="id" value="${delivery.id}">
                <c:if test="${delivery.paymentMethod == 'COD' and delivery.paymentStatus != 'Paid'}">
                    <label class="check"><input type="checkbox" name="collected" value="yes" required> Tôi đã thu đủ <fmt:formatNumber value="${delivery.totalAmount}" maxFractionDigits="0"/> đ tiền COD.</label>
                </c:if>
                <button name="action" value="delivered" type="submit">Giao thành công</button>
            </form>
            <form action="${ctx}/shopowner/delivery/update" method="post" class="stack narrow">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="id" value="${delivery.id}">
                <label for="failedReason">Lý do giao thất bại</label>
                <textarea id="failedReason" name="reason" required maxlength="255" rows="2" placeholder="Ví dụ: Gọi nhiều lần nhưng không liên lạc được với khách"></textarea>
                <button class="danger" name="action" value="failed" type="submit">Giao thất bại</button>
            </form>
        </c:when>
        <c:otherwise><p>Lần giao đã kết thúc. Chủ shop sẽ phân công lần mới nếu cần giao lại.</p></c:otherwise>
    </c:choose>
</section>
</c:if>
<%@ include file="footer.jspf" %>
