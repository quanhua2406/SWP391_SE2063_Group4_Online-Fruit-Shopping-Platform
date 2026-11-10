<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán hoa hồng của shop - FreshFruit Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<header class="navbar">
    <div class="logo">🍊 FreshFruit</div>
    <nav class="nav-actions">
        <a href="${pageContext.request.contextPath}/admin/dashboard">📊 Bảng điều khiển</a>
        <a href="${pageContext.request.contextPath}/admin/commission-report">🧾 Báo cáo hoa hồng</a>
        <a href="${pageContext.request.contextPath}/admin/shops">🏪 Quản lý shop</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </nav>
</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="${pageContext.request.contextPath}/admin/commission-report">← Báo cáo hoa hồng</a>
    </p>

    <h1>Thanh toán hoa hồng của shop</h1>

    <p class="dashboard-welcome">
        Mỗi tháng hệ thống tạo kỳ hoa hồng cho từng shop. Shop có hạn 10 ngày để thanh toán,
        quá hạn 1 ngày sẽ được nhắc, quá hạn 2 ngày sẽ bị tạm khóa cho đến khi thanh toán.
    </p>

    <c:if test="${not empty param.msg}">
        <div class="message success"><c:out value="${param.msg}"/></div>
    </c:if>

    <div class="panel">
        <h2>➕ Tạo kỳ thanh toán thủ công</h2>

        <p class="panel-hint">
            Tạo kỳ cho một tháng cụ thể, dùng khi cần test hoặc tạo bù. Kỳ đã tồn tại sẽ không bị tạo lại.
            Chỉ shop có hoa hồng lớn hơn 0 (đơn đã giao và đã thanh toán trong tháng đó) mới được tạo kỳ.
        </p>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/commission-settlements"
              class="filter-form"
              onsubmit="return confirm('Tạo kỳ thanh toán cho tháng đã chọn?');">
            <label>Tháng:
                <input type="month" name="month" value="${defaultMonth}" required>
            </label>
            <button type="submit" class="btn-sm">Tạo kỳ thanh toán</button>
        </form>
    </div>

    <div class="panel">
        <h2>💳 Danh sách kỳ thanh toán</h2>

        <c:choose>
            <c:when test="${empty settlements}">
                <p class="panel-hint">Chưa có kỳ thanh toán nào.</p>
            </c:when>
            <c:otherwise>
                <div class="table-scroll">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>Shop</th>
                            <th>Tháng</th>
                            <th>Số tiền</th>
                            <th>Hạn thanh toán</th>
                            <th>Trạng thái</th>
                            <th>Thanh toán lúc</th>
                            <th>Mã giao dịch</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${settlements}">
                            <tr>
                                <td><c:out value="${s.shopName}"/></td>
                                <td><c:out value="${s.periodMonth}"/></td>
                                <td><fmt:formatNumber value="${s.amount}" type="number" maxFractionDigits="0"/> đ</td>
                                <td>${s.dueDate}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.status == 'paid'}"><span class="pill pill-ok">Đã thanh toán</span></c:when>
                                        <c:when test="${s.dueDate lt today}"><span class="pill pill-danger">Quá hạn</span></c:when>
                                        <c:otherwise><span class="pill pill-warn">Chưa thanh toán</span></c:otherwise>
                                    </c:choose>
                                    <c:if test="${s.shopLocked and s.status == 'unpaid'}">
                                        <span class="pill pill-muted">Shop bị khóa</span>
                                    </c:if>
                                </td>
                                <td>${s.paidAt}</td>
                                <td><c:out value="${s.transactionRef}"/></td>
                                <td>
                                    <c:if test="${s.status == 'unpaid'}">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/commission-settlements"
                                              style="margin:0;"
                                              onsubmit="return confirm('Xác nhận đã nhận đủ chuyển khoản cho kỳ này?');">
                                            <input type="hidden" name="action" value="confirm">
                                            <input type="hidden" name="settlementId" value="${s.settlementId}">
                                            <button type="submit" class="btn-sm">Xác nhận đã nhận tiền</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</section>
</body>
</html>
