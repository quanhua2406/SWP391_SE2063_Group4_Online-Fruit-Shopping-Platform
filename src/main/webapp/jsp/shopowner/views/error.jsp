<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Không xử lý được yêu cầu - FreshFruit</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/freshfruit-base.css?v=20261014-3">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/freshfruit-management.css?v=20261014-3"></head>
<body class="error-page"><main class="error-card">
    <div class="auth-logo"><svg class="icon" width="30" height="30" aria-hidden="true" focusable="false"><use href="${pageContext.request.contextPath}/assets/icons.svg#fruit"/></svg> FreshFruit</div>
    <div class="error-code"><c:out value="${requestScope['jakarta.servlet.error.status_code']}"/></div>
    <h1>Không xử lý được yêu cầu</h1>
    <c:choose>
        <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 403}"><p>Bạn không có quyền thực hiện hoặc phiên biểu mẫu đã hết hạn. Hãy tải lại trang.</p></c:when>
        <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 404}"><p>Không tìm thấy dữ liệu thuộc quyền truy cập của bạn.</p></c:when>
        <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 503}"><p>Dịch vụ tạm thời chưa sẵn sàng. Vui lòng thử lại sau ít phút.</p></c:when>
        <c:otherwise><p>Yêu cầu không hợp lệ hoặc máy chủ gặp lỗi. Hãy quay về trang chính và thử lại.</p></c:otherwise>
    </c:choose>
    <a class="button" href="${pageContext.request.contextPath}/login">Về trang chính</a>
</main></body></html>
