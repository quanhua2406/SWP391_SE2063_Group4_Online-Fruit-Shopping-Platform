<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../shopowner/views/header.jspf" %>
<div class="page-heading"><div><p class="eyebrow">SHIPPER</p><h1>Đội giao hàng</h1><p class="muted">Gửi yêu cầu tham gia shop, theo dõi kết quả và quản lý trạng thái nhận đơn.</p></div></div>
<section class="card">
  <h2>Danh sách cửa hàng đang hoạt động</h2>
  <p class="muted">Tra cứu tên và địa chỉ shop trước khi gửi yêu cầu tham gia.</p>
  <div class="table-wrap"><table class="data-table">
    <thead><tr><th>Cửa hàng</th><th>Địa chỉ</th></tr></thead>
    <tbody>
      <c:forEach var="shop" items="${shops}"><tr>
        <td><c:out value="${shop.name}"/></td>
        <td><c:choose><c:when test="${not empty shop.pickupAddress}"><c:out value="${shop.pickupAddress}"/></c:when><c:otherwise><span class="muted">Shop chưa cập nhật địa chỉ</span></c:otherwise></c:choose></td>
      </tr></c:forEach>
      <c:if test="${empty shops}"><tr><td colspan="2">Hiện chưa có cửa hàng đang hoạt động.</td></tr></c:if>
    </tbody>
  </table></div>
</section>
<section class="card">
  <c:choose>
    <c:when test="${not empty membership}">
      <h2>Shop hiện tại</h2>
      <p><strong><c:out value="${membership.shopName}"/></strong> · Trạng thái: <c:out value="${membership.leaveRequested ? 'Đang xin nghỉ' : membership.status}"/></p>
      <p class="muted">Đơn đang giao: ${membership.openDeliveryCount}/3 · Sẵn sàng: ${membership.available ? 'Có' : 'Không'}</p>
    </c:when>
    <c:otherwise>
      <c:choose>
        <c:when test="${not empty myRequests and myRequests[0].status == 'Pending'}">
          <h2>Yêu cầu đang chờ duyệt</h2><p>Hãy chờ shop xử lý yêu cầu hiện tại trước khi gửi yêu cầu mới.</p>
        </c:when>
        <c:otherwise>
      <h2>Xin tham gia cửa hàng</h2>
      <form method="post" action="${ctx}/delivery/team/apply" class="form-grid">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <label>Cửa hàng<select name="shopId" required><option value="">Chọn cửa hàng</option><c:forEach var="s" items="${shops}"><option value="${s.id}"><c:out value="${s.name}"/><c:if test="${not empty s.pickupAddress}"> — <c:out value="${s.pickupAddress}"/></c:if></option></c:forEach></select></label>
        <button class="button" type="submit">Gửi yêu cầu</button>
      </form>
      <p class="muted">Mỗi shipper chỉ được thuộc một shop tại một thời điểm.</p>
        </c:otherwise>
      </c:choose>
    </c:otherwise>
  </c:choose>
</section>
<section class="card">
  <h2>Lịch sử yêu cầu</h2>
  <div class="table-wrap"><table class="data-table"><thead><tr><th>Cửa hàng</th><th>Ngày gửi</th><th>Kết quả</th><th>Ghi chú</th><th>Thao tác</th></tr></thead><tbody>
  <c:forEach var="r" items="${myRequests}"><tr><td><c:out value="${r.shopName}"/></td><td><fmt:formatDate value="${r.requestedAt}" pattern="dd/MM/yyyy HH:mm"/></td><td><c:out value="${r.status}"/></td><td><c:out value="${r.reviewNote}"/></td><td><c:if test="${r.status == 'Pending'}"><form method="post" action="${ctx}/delivery/team/withdraw"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="requestId" value="${r.requestId}"><button class="button secondary" type="submit">Rút yêu cầu</button></form></c:if></td></tr></c:forEach>
  <c:if test="${empty myRequests}"><tr><td colspan="5">Bạn chưa gửi yêu cầu tham gia shop.</td></tr></c:if>
  </tbody></table></div>
</section>
<c:if test="${not empty membership and membership.status == 'Active' and not membership.leaveRequested}">
<section class="card">
  <h2>Trạng thái nhận đơn</h2>
  <p class="muted">Tắt sẵn sàng không ảnh hưởng đến đơn bạn đã nhận.</p>
  <form method="post" action="${ctx}/delivery/team/availability" class="form-actions"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><button class="button" name="available" value="true" type="submit">Bật sẵn sàng</button><button class="button secondary" name="available" value="false" type="submit">Tắt sẵn sàng</button></form>
  <form method="post" action="${ctx}/delivery/team/leave" onsubmit="return confirm('Bạn muốn xin nghỉ shop? Đơn đang giao vẫn phải hoàn thành.');"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><button class="button danger" type="submit">Xin nghỉ shop</button></form>
</section>
</c:if>
<c:if test="${not empty membership and membership.leaveRequested}"><section class="card"><h2>Đang chờ kết thúc hợp tác</h2><p>Bạn không nhận đơn mới. Hãy hoàn tất đơn đang mở; hệ thống sẽ tự kết thúc tư cách thành viên sau đó.</p></section></c:if>
<%@ include file="../shopowner/views/footer.jspf" %>
