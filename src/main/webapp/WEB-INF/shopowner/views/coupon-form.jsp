<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<c:set var="locked" value="${coupon.historyCount > 0}"/>
<p class="eyebrow">CHỦ CỬA HÀNG · MÃ GIẢM GIÁ</p>
<h1>${coupon.id == 0 ? 'Tạo mã giảm giá' : 'Sửa mã giảm giá'}</h1>
<c:if test="${not empty formError}"><p class="alert error" role="alert"><c:out value="${formError}"/></p></c:if>
<c:if test="${locked}"><p class="alert success">Mã đã có lịch sử sử dụng. Bạn có thể sửa tên, mô tả và bật/tắt; hãy tạo mã mới nếu muốn đổi điều kiện ưu đãi.</p></c:if>
<c:if test="${empty shops}"><p class="alert error">Bạn chưa có cửa hàng đang hoạt động.</p></c:if>
<form method="post" action="${ctx}/shopowner/shop/coupons" class="card stack management-form">
    <input type="hidden" name="shopcsrf" value="${sessionScope.shopcsrf}"><input type="hidden" name="action" value="save">
    <input type="hidden" name="id" value="${coupon.id}"><input type="hidden" name="version" value="${coupon.version}">
    <h2 class="form-section-title">Thông tin cơ bản</h2>
    <label for="shopId">Cửa hàng *</label>
    <c:choose>
        <c:when test="${coupon.id > 0}"><input type="hidden" name="shopId" value="${coupon.shopId}"><input id="shopId" readonly value="<c:out value='${coupon.shopName}'/>"></c:when>
        <c:otherwise><select id="shopId" name="shopId" required><c:forEach var="s" items="${shops}"><option value="${s.id}" ${s.id == coupon.shopId ? 'selected' : ''}><c:out value="${s.name}"/></option></c:forEach></select></c:otherwise>
    </c:choose>
    <label for="code">Mã giảm giá *</label>
    <input id="code" name="code" required minlength="3" maxlength="30" pattern="[A-Za-z0-9_-]{3,30}" placeholder="Ví dụ: SHOP20K" value="<c:out value='${submitted ? param.code : coupon.code}'/>" ${locked ? 'readonly' : ''}>
    <p class="muted">Dùng chữ A–Z, số, dấu - hoặc _. Mã sẽ được lưu thành chữ in hoa và không được trùng trong cùng shop.</p>
    <label for="name">Tên ưu đãi *</label>
    <input id="name" name="name" required maxlength="150" value="<c:out value='${submitted ? param.name : coupon.name}'/>" placeholder="Ví dụ: Giảm 20.000 đ cho đơn từ 100.000 đ">
    <label for="description">Mô tả</label>
    <textarea id="description" name="description" maxlength="500" rows="3"><c:out value="${submitted ? param.description : coupon.description}"/></textarea>

    <h2 class="form-section-title">Điều kiện ưu đãi</h2>
    <div class="grid">
        <div class="stack"><label for="discountType">Loại giảm giá *</label>
            <c:set var="discountType" value="${submitted ? param.discountType : coupon.discountType}"/>
            <select id="discountType" name="discountType" ${locked ? 'disabled' : ''}>
                <option value="FIXED" ${discountType == 'FIXED' ? 'selected' : ''}>Số tiền (đ / đơn)</option>
                <option value="PERCENT" ${discountType == 'PERCENT' ? 'selected' : ''}>Phần trăm (%)</option>
            </select>
            <c:if test="${locked}"><input type="hidden" name="discountType" value="<c:out value='${coupon.discountType}'/>"></c:if>
        </div>
        <div class="stack"><label for="discountValue">Mức giảm *</label>
            <input id="discountValue" name="discountValue" type="number" min="0.01" step="0.01" required value="<c:out value='${submitted ? param.discountValue : coupon.discountValue}'/>" ${locked ? 'readonly' : ''}>
        </div>
    </div>
    <p class="muted">Nhập 20000 để giảm 20.000 đ cho cả đơn hoặc 10 để giảm 10%. Phần trăm không quá 100%; số tiền là số nguyên đồng.</p>
    <div class="grid">
        <div class="stack"><label for="minOrderAmount">Giá trị đơn tối thiểu (đ) *</label>
            <input id="minOrderAmount" name="minOrderAmount" type="number" min="0" step="1" required value="<c:out value='${submitted ? param.minOrderAmount : coupon.minOrderAmount}'/>" ${locked ? 'readonly' : ''}>
        </div>
        <div class="stack"><label for="maxDiscount">Giảm tối đa (đ, chỉ dành cho %)</label>
            <input id="maxDiscount" name="maxDiscount" type="number" min="1" step="1" value="<c:out value='${submitted ? param.maxDiscount : coupon.maxDiscount}'/>" ${locked ? 'readonly' : ''}>
        </div>
    </div>
    <p class="muted">Bỏ trống “Giảm tối đa” nếu không đặt trần. Với mã giảm theo số tiền, để trống ô này.</p>
    <div class="grid">
        <div class="stack"><label for="totalLimit">Tổng số lượt *</label><input id="totalLimit" name="totalLimit" type="number" min="1" max="2147483647" step="1" required value="<c:out value='${submitted ? param.totalLimit : coupon.totalLimit}'/>" ${locked ? 'readonly' : ''}></div>
        <div class="stack"><label for="perCustomerLimit">Số lượt mỗi khách *</label><input id="perCustomerLimit" name="perCustomerLimit" type="number" min="1" max="2147483647" step="1" required value="<c:out value='${submitted ? param.perCustomerLimit : coupon.perCustomerLimit}'/>" ${locked ? 'readonly' : ''}></div>
    </div>
    <h2 class="form-section-title">Thời gian áp dụng</h2>
    <div class="grid">
        <div class="stack"><label for="startsAt">Bắt đầu *</label><input id="startsAt" name="startsAt" type="datetime-local" step="60" min="2000-01-01T00:00" max="9999-12-31T23:59" required value="<c:out value='${submitted ? param.startsAt : coupon.startInput}'/>" ${locked ? 'readonly' : ''}></div>
        <div class="stack"><label for="endsAt">Kết thúc *</label><input id="endsAt" name="endsAt" type="datetime-local" step="60" min="2000-01-01T00:00" max="9999-12-31T23:59" required value="<c:out value='${submitted ? param.endsAt : coupon.endInput}'/>" ${locked ? 'readonly' : ''}></div>
    </div>
    <p class="muted">Giờ Việt Nam (UTC+7). Mã hết hiệu lực ngay tại giờ kết thúc; muốn hết một ngày, chọn 00:00 ngày tiếp theo.</p>
    <label class="check"><input type="checkbox" name="active" ${coupon.active ? 'checked' : ''}> Bật mã sau khi lưu</label>
    <div class="actions">
        <c:if test="${not empty shops}"><button type="submit">Lưu mã giảm giá</button></c:if>
        <a class="button secondary" href="${ctx}/shopowner/shop/coupons">Về danh sách</a>
        <c:if test="${coupon.id > 0}"><a href="${ctx}/shopowner/shop/coupons?action=edit&amp;id=${coupon.id}">Tải lại dữ liệu đã lưu</a></c:if>
    </div>
</form>
<%@ include file="footer.jspf" %>
