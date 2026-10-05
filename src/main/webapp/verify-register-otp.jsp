<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Verify Email - FreshFruit</title>

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
        <h2>Verify Your Email</h2>

        <!-- Description -->
        <p class="auth-description">
            Enter the 6-digit verification code sent to your email
            to complete your registration.
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
                    Verification Code
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
                    placeholder="Enter 6-digit OTP"
                    required>

            </div>

            <button type="submit"
                    class="auth-button">
                Verify Email
            </button>

        </form>

        <!-- Back -->
        <div class="auth-links">

            <a href="${pageContext.request.contextPath}/register">
                Back to Register
            </a>

        </div>

    </div>

</div>

</body>
</html>