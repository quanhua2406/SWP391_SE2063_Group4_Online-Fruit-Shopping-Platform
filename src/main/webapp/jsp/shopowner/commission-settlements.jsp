<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thanh toán hoa hồng - FreshFruit</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f8fafc; color: #1e293b; margin: 0; padding: 24px; }
        .container { max-width: 960px; margin: 0 auto; }
        h1 { font-size: 22px; margin-bottom: 8px; }
        .hint { color: #64748b; font-size: 14px; margin-bottom: 16px; }
        .notice { padding: 12px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 14px; }
        .notice.ok { background: #dcfce7; color: #166534; }
        .notice.error { background: #fee2e2; color: #991b1b; }
        .notice.warn { background: #fef9c3; color: #854d0e; }
        table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; }
        th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        th { background: #f1f5f9; }
        .badge { padding: 2px 8px; border-radius: 999px; font-size: 12px; font-weight: 600; }
        .badge.paid { background: #dcfce7; color: #166534; }
        .badge.unpaid { background: #fef9c3; color: #854d0e; }
        .badge.overdue { background: #fee2e2; color: #991b1b; }
        button { background: #16a34a; color: #fff; border: none; border-radius: 6px; padding: 6px 12px; cursor: pointer; }
    </style>
</head>
<body>
<div class="container">

    <h1>Thanh toán hoa hồng cho nền tảng</h1>
    <p class="hint">Hoa hồng tháng trước được tính trên đơn đã giao và đã thanh toán. Hạn thanh toán là 10 ngày kể từ ngày phát hành kỳ.</p>

    <c:if test="${not empty param.msg}">
        <div class="notice ok"><c:out value="${param.msg}"/></div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="notice error"><c:out value="${param.error}"/></div>
    </c:if>

    <c:if test="${shopLocked}">
        <div class="notice error">Shop đang bị tạm khóa do chưa thanh toán hoa hồng. Thanh toán kỳ quá hạn để mở lại.</div>
    </c:if>
    <c:if test="${not shopLocked and not empty overdueIds}">
        <div class="notice warn">Có kỳ thanh toán quá hạn. Shop sẽ bị tạm khóa nếu chưa thanh toán sau 1 ngày.</div>
    </c:if>

    <c:choose>
        <c:when test="${empty settlements}">
            <p>Chưa có kỳ thanh toán hoa hồng nào.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr>
                    <th>Kỳ</th>
                    <th>Số tiền</th>
                    <th>Ngày phát hành</th>
                    <th>Hạn thanh toán</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${settlements}">
                    <tr>
                        <td><c:out value="${s.periodMonth}"/></td>
                        <td><fmt:formatNumber value="${s.amount}" type="number" maxFractionDigits="0"/> ₫</td>
                        <td>${s.issuedDate}</td>
                        <td>${s.dueDate}</td>
                        <td>
                            <c:choose>
                                <c:when test="${s.status == 'paid'}"><span class="badge paid">Đã thanh toán</span></c:when>
                                <c:when test="${overdueIds.contains(s.settlementId)}"><span class="badge overdue">Quá hạn</span></c:when>
                                <c:otherwise><span class="badge unpaid">Chưa thanh toán</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:if test="${s.status == 'unpaid'}">
                                <form method="post" action="${pageContext.request.contextPath}/store-owner/commission/pay" style="margin:0;">
                                    <input type="hidden" name="settlementId" value="${s.settlementId}">
                                    <button type="submit">Thanh toán VNPay</button>
                                </form>

                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

</div>
</body>
</html>
