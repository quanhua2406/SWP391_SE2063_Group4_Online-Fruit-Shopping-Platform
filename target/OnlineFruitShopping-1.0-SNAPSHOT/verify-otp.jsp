<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Verify OTP - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

</head>

<body>

<div class="auth-page">

    <div class="auth-container">

        <div class="auth-logo">
            🍊 FreshFruit
        </div>

        <h2>Verify OTP</h2>

        <p class="auth-description">
            Enter the 6-digit OTP sent to your Gmail.
        </p>


        <!-- ERROR -->

        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">

                ❌
                <%= request.getAttribute("error") %>

            </div>

        <% } %>


        <!-- FORM -->

        <form action="${pageContext.request.contextPath}/verify-otp"
              method="post">

            <div class="auth-form-group">

                <label for="otp">
                    OTP
                </label>

                <input
                    type="text"
                    id="otp"
                    name="otp"
                    placeholder="Enter 6-digit OTP"
                    maxlength="6"
                    inputmode="numeric"
                    pattern="[0-9]{6}"
                    autocomplete="one-time-code"
                    required
                >

            </div>


            <button
                type="submit"
                class="auth-button"
            >
                Verify OTP
            </button>

        </form>


        <div class="auth-links">

            <p>

                Didn't receive the OTP?

                <a href="${pageContext.request.contextPath}/forgot-password">
                    Request a new one
                </a>

            </p>

            <p>

                <a href="${pageContext.request.contextPath}/login">
                    Back to Login
                </a>

            </p>

        </div>

    </div>

</div>

</body>
</html>