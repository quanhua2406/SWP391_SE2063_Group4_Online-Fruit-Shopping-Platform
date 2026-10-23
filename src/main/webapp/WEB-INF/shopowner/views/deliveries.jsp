<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>

<p class="eyebrow">
    ${sessionScope.shopUser.role == 'Customer'
      ? 'KHÁCH HÀNG' : 'GIAO HÀNG'}
</p>

<h1>Giao hàng của tôi</h1>

<p class="muted">
    Xem trạng thái và thông tin từng lần giao hàng.
</p>

<c:if test="${sessionScope.shopUser.role == 'Customer'}">
    <a class="button secondary"
       href="${ctx}/customer/dashboard">
        ← Về Customer Dashboard
    </a>
</c:if>

<section class="card" id="delivery-list">
    <%@ include file="delivery-table.jspf" %>
</section>

<%@ include file="footer.jspf" %>
