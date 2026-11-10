<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>

<c:choose>

    <c:when test="${requestScope.currentRole == 'DELIVERYSTAFF'}">

        <div class="page-heading">
            <div>
                <p class="eyebrow">NHÂN VIÊN GIAO HÀNG</p>

                <h1>Giao hàng của tôi</h1>

                <p class="muted">
                    Kiểm tra thông tin và mở chi tiết để cập nhật tiến độ.
                </p>
            </div>
        </div>

        <section id="delivery-list"
                 aria-label="Danh sách giao hàng">

            <%@ include file="delivery-filters.jspf" %>

            <div class="shipper-delivery-grid">

                <c:forEach var="d" items="${deliveries}">

                    <article class="shipper-delivery-card">

                        <div class="shipper-card-heading">
                            <div>
                                <h2>Đơn #${d.orderId}</h2>

                                <span class="muted">
                                    Mã lần giao #${d.attemptId}
                                </span>
                            </div>

                            <span class="badge"
                                  data-status="<c:out value='${d.status}'/>">
                                <c:out value="${d.statusLabel}"/>
                            </span>
                        </div>

                        <dl class="shipper-delivery-info">
                            <dt>Cửa hàng</dt>
                            <dd>
                                <c:out value="${d.shopName}"/>
                            </dd>

                            <dt>Người nhận</dt>
                            <dd>
                                <c:out value="${d.receiverName}"/>
                            </dd>

                            <dt>Điện thoại</dt>
                            <dd>
                                <c:out value="${d.receiverPhone}"/>
                            </dd>

                            <dt>Địa chỉ giao</dt>
                            <dd>
                                <c:out value="${d.shippingAddress}"/>
                            </dd>
                        </dl>

                        <div class="shipper-payment">
                            <span>Cần thu COD</span>

                            <strong>
                                <c:choose>
                                    <c:when test="${d.paymentMethod == 'COD'
                                                   and d.paymentStatus != 'Paid'}">

                                        <fmt:formatNumber
                                                value="${d.totalAmount}"
                                                maxFractionDigits="0"/> đ

                                    </c:when>

                                    <c:otherwise>
                                        0 đ
                                    </c:otherwise>
                                </c:choose>
                            </strong>
                        </div>

                        <p class="shipper-payment-note">
                            <c:choose>

                                <c:when test="${d.paymentStatus == 'Paid'}">
                                    Đơn đã thanh toán.
                                </c:when>

                                <c:when test="${d.paymentMethod == 'COD'}">
                                    Thu tiền khi giao hàng thành công.
                                </c:when>

                                <c:otherwise>
                                    Thanh toán online chưa được xác nhận.
                                </c:otherwise>

                            </c:choose>
                        </p>

                        <a class="button shipper-detail-link"
                           href="${ctx}/shopowner/delivery/detail?id=${d.attemptId}">

                            <c:choose>

                                <c:when test="${d.status == 'Assigned'}">
                                    Xem đơn để nhận
                                </c:when>

                                <c:when test="${d.status == 'Accepted'}">
                                    Xem đơn để lấy hàng
                                </c:when>

                                <c:when test="${d.status == 'PickedUp'}">
                                    Xem đơn để bắt đầu giao
                                </c:when>

                                <c:when test="${d.status == 'Delivering'}">
                                    Xem đơn để cập nhật kết quả
                                </c:when>

                                <c:otherwise>
                                    Xem chi tiết
                                </c:otherwise>

                            </c:choose>
                        </a>

                    </article>

                </c:forEach>

            </div>

            <c:if test="${empty deliveries}">

                <div class="card shipper-empty">

                    <c:choose>

                        <c:when test="${deliveryFilter.active}">
                            <h2>Không tìm thấy lần giao phù hợp</h2>

                            <p class="muted">
                                Hãy thay đổi từ khóa hoặc điều kiện lọc.
                            </p>

                            <a class="button secondary"
                               href="${ctx}/shopowner/delivery/list">
                                Xóa bộ lọc
                            </a>
                        </c:when>

                        <c:otherwise>
                            <h2>Chưa có lần giao nào</h2>

                            <p class="muted">
                                Các lần giao được phân công cho bạn
                                sẽ xuất hiện tại đây.
                            </p>
                        </c:otherwise>

                    </c:choose>

                </div>

            </c:if>

        </section>

    </c:when>

    <c:otherwise>

        <p class="eyebrow">THEO DÕI GIAO HÀNG</p>

        <h1>Thông tin giao hàng</h1>

        <p class="muted">
            Mở chi tiết để xem thông tin và tiến độ giao hàng.
        </p>

        <section class="card" id="delivery-list">
            <%@ include file="delivery-table.jspf" %>
        </section>

    </c:otherwise>

</c:choose>

<%@ include file="footer.jspf" %>
