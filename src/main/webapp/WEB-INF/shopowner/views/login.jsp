<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#3d8b40">
    <title>Đăng nhập - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/freshfruit-base.css?v=20261014-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/freshfruit-management.css?v=20261014-3">
</head>
<body>
<main class="auth-page">
    <section class="auth-container" aria-labelledby="login-title">
        <div class="auth-logo"><svg class="icon" width="30" height="30" aria-hidden="true" focusable="false"><use href="${pageContext.request.contextPath}/assets/icons.svg#fruit"/></svg> FreshFruit</div>
        <h1 id="login-title">Đăng nhập</h1>
        <p class="auth-description">Đăng nhập tài khoản FreshFruit của bạn.</p>
        <c:if test="${not empty error}">
            <p class="error-message" role="alert"><c:out value="${error}"/></p>
        </c:if>
        <form action="${pageContext.request.contextPath}/shopowner/login" method="post">
            <input type="hidden" name="shopcsrf" value="${sessionScope.shopcsrf}">
            <div class="auth-form-group">
                <label for="username">Tài khoản</label>
                <input id="username" name="username" required maxlength="50" autocomplete="username" autofocus placeholder="Nhập tên đăng nhập" value="<c:out value='${param.username}'/>">
            </div>
            <div class="auth-form-group">
                <label for="password">Mật khẩu</label>
                <input id="password" name="password" type="password" required maxlength="255" autocomplete="current-password" placeholder="Nhập mật khẩu">
            </div>
            <button type="submit" class="auth-button">Đăng nhập</button>
        </form>
        <p class="auth-note">Quản lý cửa hàng, giao hàng và ưu đãi của bạn.</p>
    </section>
</main>
</body>
</html>
