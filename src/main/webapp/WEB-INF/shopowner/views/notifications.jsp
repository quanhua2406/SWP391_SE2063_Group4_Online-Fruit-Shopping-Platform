<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">HỘP THÔNG BÁO</p>
<h1>Thông báo của tôi</h1>
<p class="muted">Hiển thị 100 thông báo gần nhất. Tải lại trang để nhận cập nhật mới.</p>
<c:forEach var="n" items="${notifications}">
    <article class="card notification ${n.read ? '' : 'is-unread'}">
        <div>
            <p class="muted"><fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/> · ${n.read ? 'Đã đọc' : 'Chưa đọc'}</p>
            <h2><c:out value="${n.title}"/></h2>
            <p><c:out value="${n.message}"/></p>
        </div>
        <c:if test="${not n.read}">
            <form action="${ctx}/shopowner/notifications" method="post">
                <input type="hidden" name="shopcsrf" value="${sessionScope.shopcsrf}">
                <input type="hidden" name="id" value="${n.id}">
                <button class="secondary" type="submit">Đánh dấu đã đọc</button>
            </form>
        </c:if>
    </article>
</c:forEach>
<c:if test="${empty notifications}"><p class="card empty">Bạn chưa có thông báo.</p></c:if>
<%@ include file="footer.jspf" %>
