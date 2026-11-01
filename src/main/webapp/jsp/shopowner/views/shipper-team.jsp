<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<div class="page-heading"><div><p class="eyebrow">SHOP OWNER</p><h1>Quản lý đội shipper</h1><p class="muted">Duyệt yêu cầu, theo dõi tải công việc và kiểm soát phân công.</p></div></div>
<section class="card">
  <h2>Địa chỉ cửa hàng cho shipper</h2>
  <p class="muted">Địa chỉ này xuất hiện trong danh sách cửa hàng để nhân viên giao hàng biết nơi làm việc và xin tham gia.</p>
  <c:if test="${not empty ownerShop}">
    <p><strong><c:out value="${ownerShop.name}"/></strong></p>
    <form method="post" action="${ctx}/shopowner/shop/shipper-team" class="form-grid">
      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
      <input type="hidden" name="shopId" value="${ownerShop.id}">
      <label for="pickupAddress">Địa chỉ lấy hàng / địa chỉ shop</label>
      <input id="pickupAddress" name="pickupAddress" required minlength="5" maxlength="500"
             value="<c:out value='${ownerShop.pickupAddress}'/>" placeholder="Số nhà, đường, phường, quận, tỉnh/thành phố">
      <button type="submit" class="button" name="action" value="savePickupAddress">Lưu địa chỉ</button>
    </form>
  </c:if>
</section>
<section class="card"><h2>Yêu cầu tham gia</h2><div class="table-wrap"><table class="data-table"><thead><tr><th>Shipper</th><th>Mã</th><th>Ngày gửi</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach var="r" items="${requests}"><tr><td><c:out value="${r.shipperName}"/></td><td>${r.shipperId}</td><td><fmt:formatDate value="${r.requestedAt}" pattern="dd/MM/yyyy HH:mm"/></td><td><div class="form-actions"><form method="post" action="${ctx}/shopowner/shop/shipper-team"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="requestId" value="${r.requestId}"><button class="button" name="action" value="approve" type="submit">Duyệt</button></form><form method="post" action="${ctx}/shopowner/shop/shipper-team"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="requestId" value="${r.requestId}"><input type="text" name="reason" maxlength="255" placeholder="Lý do từ chối" required><button class="button secondary" name="action" value="reject" type="submit">Từ chối</button></form></div></td></tr></c:forEach>
<c:if test="${empty requests}"><tr><td colspan="4">Không có yêu cầu chờ duyệt.</td></tr></c:if>
</tbody></table></div></section>
<section class="card"><h2>Nhân viên hiện tại</h2><div class="table-wrap"><table class="data-table"><thead><tr><th>Shipper</th><th>Trạng thái</th><th>Sẵn sàng</th><th>Tải công việc</th><th>Phân công</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach var="s" items="${staff}"><tr><td><c:out value="${s.fullName}"/></td><td><c:choose><c:when test="${s.leaveRequested}">Đang xin nghỉ / ngừng hợp tác</c:when><c:otherwise><c:out value="${s.status}"/></c:otherwise></c:choose></td><td>${s.available ? 'Có' : 'Không'}</td><td>${s.openDeliveryCount}/3 đơn đang giao <c:if test="${s.openDeliveryCount >= 3}"><strong>(Đã đủ tải)</strong></c:if></td><td>${s.assignmentEnabled ? 'Cho phép' : 'Tạm ngưng'}</td><td><c:if test="${s.status == 'Active' and not s.leaveRequested}"><div class="form-actions"><form method="post" action="${ctx}/shopowner/shop/shipper-team"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="shipperId" value="${s.id}"><c:choose><c:when test="${s.assignmentEnabled}"><button class="button secondary" name="action" value="pause">Tạm ngưng</button></c:when><c:otherwise><button class="button" name="action" value="resume">Mở phân công</button></c:otherwise></c:choose></form><form method="post" action="${ctx}/shopowner/shop/shipper-team" onsubmit="return confirm('Ngừng hợp tác với shipper này?');"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="shipperId" value="${s.id}"><button class="button danger" name="action" value="terminate">Ngừng hợp tác</button></form></div></c:if><c:if test="${s.leaveRequested}"><span class="muted">Chờ hoàn tất đơn cũ</span></c:if></td></tr></c:forEach>
<c:if test="${empty staff}"><tr><td colspan="6">Shop chưa có shipper.</td></tr></c:if>
</tbody></table></div></section>
<%@ include file="footer.jspf" %>
