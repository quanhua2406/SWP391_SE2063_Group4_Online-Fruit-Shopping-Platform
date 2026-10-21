<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">SHOP OWNER · KHUYẾN MÃI</p>
<h1>${promotion.id == 0 ? 'Tạo chương trình khuyến mãi' : 'Sửa chương trình khuyến mãi'}</h1>
<p class="muted">Giảm giá trên từng đơn vị sản phẩm được chọn. Ngày giờ nhập theo Việt Nam (UTC+7).</p>
<c:if test="${not empty formError}"><p class="alert error" role="alert"><c:out value="${formError}"/></p></c:if>
<c:if test="${empty shops}"><p class="alert error">Tài khoản của bạn chưa có cửa hàng đang hoạt động.</p></c:if>

<%-- Nếu nhập sai, giữ lại các nội dung đã nhập để người dùng sửa tiếp. --%>
<c:set var="nameValue" value="${submitted ? param.name : promotion.name}"/>
<c:set var="descriptionValue" value="${submitted ? param.description : promotion.description}"/>
<c:set var="typeValue" value="${submitted ? param.discountType : promotion.discountType}"/>
<c:set var="discountValue" value="${submitted ? param.discountValue : promotion.discountValue}"/>
<c:set var="startValue" value="${submitted ? param.startsAt : promotion.startInput}"/>
<c:set var="endValue" value="${submitted ? param.endsAt : promotion.endInput}"/>

<form method="post" action="${ctx}/shopowner/shop/promotions" class="card stack management-form">
    <input type="hidden" name="shopcsrf" value="${sessionScope.shopcsrf}">
    <input type="hidden" name="action" value="save">
    <input type="hidden" name="id" value="${promotion.id}">
    <input type="hidden" name="version" value="${promotion.version}">

    <h2 class="form-section-title">Thông tin cơ bản</h2>
    <label for="shopId">Cửa hàng *</label>
    <c:choose>
        <c:when test="${promotion.id > 0}">
            <input type="hidden" name="shopId" value="${promotion.shopId}">
            <input id="shopId" value="<c:out value='${promotion.shopName}'/>" readonly>
        </c:when>
        <c:otherwise>
            <select id="shopId" name="shopId" required>
                <c:forEach var="shop" items="${shops}">
                    <option value="${shop.id}" ${shop.id == promotion.shopId ? 'selected' : ''}><c:out value="${shop.name}"/></option>
                </c:forEach>
            </select>
        </c:otherwise>
    </c:choose>

    <label for="name">Tên chương trình *</label>
    <input id="name" name="name" maxlength="150" required value="<c:out value='${nameValue}'/>" placeholder="Ví dụ: Tuần lễ trái cây tươi">
    <label for="description">Mô tả</label>
    <textarea id="description" name="description" maxlength="500" rows="3"><c:out value="${descriptionValue}"/></textarea>

    <h2 class="form-section-title">Điều kiện ưu đãi</h2>
    <div class="grid">
        <div class="stack">
            <label for="discountType">Loại giảm giá *</label>
            <select id="discountType" name="discountType" required>
                <option value="PERCENT" ${typeValue == 'PERCENT' ? 'selected' : ''}>Theo phần trăm (%)</option>
                <option value="FIXED" ${typeValue == 'FIXED' ? 'selected' : ''}>Theo số tiền (đ / đơn vị)</option>
            </select>
        </div>
        <div class="stack">
            <label for="discountValue">Mức giảm *</label>
            <input id="discountValue" name="discountValue" type="number" min="0.01" step="0.01" required value="<c:out value='${discountValue}'/>" aria-describedby="discountHint">
        </div>
    </div>
    <p id="discountHint" class="muted">Ví dụ: nhập 10 để giảm 10%; nhập 20000 để giảm 20.000 đ cho mỗi đơn vị. Phần trăm tối đa 100%; số tiền phải là số nguyên đồng.</p>

    <h2 class="form-section-title">Thời gian &amp; sản phẩm áp dụng</h2>
    <div class="grid">
        <div class="stack">
            <label for="startsAt">Bắt đầu *</label>
            <input id="startsAt" name="startsAt" type="datetime-local" min="2000-01-01T00:00" max="9999-12-31T23:59" step="60" required value="<c:out value='${startValue}'/>">
        </div>
        <div class="stack">
            <label for="endsAt">Kết thúc *</label>
            <input id="endsAt" name="endsAt" type="datetime-local" min="2000-01-01T00:00" max="9999-12-31T23:59" step="60" required value="<c:out value='${endValue}'/>">
        </div>
    </div>
    <p class="muted">Chương trình hết hiệu lực ngay tại giờ kết thúc. Muốn chạy hết ngày, chọn 00:00 ngày tiếp theo.</p>

    <fieldset class="product-picker">
        <legend>Sản phẩm áp dụng *</legend>
        <p class="muted">Chọn ít nhất một sản phẩm trong danh mục. Mức giảm áp dụng cho tất cả biến thể của sản phẩm tại cửa hàng đã chọn.</p>
        <div class="product-options">
            <c:forEach var="product" items="${products}">
                <c:if test="${product.active or promotion.productIds.contains(product.id)}">
                    <label class="check">
                        <input type="checkbox" name="productId" value="${product.id}" ${promotion.productIds.contains(product.id) ? 'checked' : ''}>
                        <span><c:out value="${product.name}"/> <span class="muted">#${product.id}</span>
                            <c:if test="${not product.active}"><span class="text-error"> — Đã ngừng bán; hãy bỏ chọn</span></c:if>
                        </span>
                    </label>
                </c:if>
            </c:forEach>
        </div>
        <c:if test="${empty products}"><p>Chưa có sản phẩm để chọn.</p></c:if>
    </fieldset>

    <label class="check"><input type="checkbox" name="active" ${promotion.active ? 'checked' : ''}> Bật chương trình sau khi lưu</label>
    <p class="muted">Bỏ chọn để lưu chương trình ở trạng thái tắt. Khi bật, chương trình phải còn hạn và không trùng lịch trên cùng sản phẩm.</p>
    <div class="actions">
        <c:if test="${not empty shops}"><button type="submit">Lưu chương trình</button></c:if>
        <a class="button secondary" href="${ctx}/shopowner/shop/promotions">Về danh sách</a>
        <c:if test="${promotion.id > 0}"><a href="${ctx}/shopowner/shop/promotions?action=edit&amp;id=${promotion.id}">Tải lại dữ liệu đã lưu</a></c:if>
    </div>
</form>
<%@ include file="footer.jspf" %>
