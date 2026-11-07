<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Xác minh email - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="auth-page">

    <div class="auth-container">

        <!-- Logo -->
        <div class="auth-logo">
            FreshFruit
        </div>

        <!-- Title -->
        <h2>Xác minh email của bạn</h2>

        <!-- Description -->
        <p class="auth-description">
            Nhập mã xác minh gồm 6 chữ số đã gửi đến email của bạn
            để hoàn tất đăng ký.
        </p>

        <!-- Error -->
        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">
                <%= request.getAttribute("error") %>
            </div>

        <% } %>

        <!-- Verify OTP Form -->
        <form action="${pageContext.request.contextPath}/verify-register-otp"
              method="post">

            <div class="auth-form-group">

                <label for="otp">
                    Mã xác minh
                </label>

                <input
                    type="text"
                    id="otp"
                    name="otp"
                    maxlength="6"
                    minlength="6"
                    pattern="[0-9]{6}"
                    inputmode="numeric"
                    autocomplete="one-time-code"
                    placeholder="Nhập mã OTP gồm 6 chữ số"
                    required>

            </div>

            <button type="submit"
                    class="auth-button">
                Xác minh email
            </button>

        </form>

        <!-- Back -->
        <div class="auth-links">

            <a href="${pageContext.request.contextPath}/register">
                Quay lại đăng ký
            </a>

        </div>

    </div>

</div>

</body>
</html>