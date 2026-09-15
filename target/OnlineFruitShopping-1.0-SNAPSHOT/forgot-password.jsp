<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Forgot Password - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

</head>

<body>

<div class="auth-page">

    <div class="auth-container">

        <div class="auth-logo">
            🍊 FreshFruit
        </div>

        <h2>Forgot Password</h2>

        <p class="auth-description">
            Enter your Gmail and we will send you a 6-digit OTP.
        </p>


        <!-- SUCCESS -->

        <% if (request.getAttribute("success") != null) { %>

            <div class="success-message">

                ✅
                <%= request.getAttribute("success") %>

            </div>

        <% } %>


        <!-- ERROR -->

        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">

                ❌
                <%= request.getAttribute("error") %>

            </div>

        <% } %>


        <!-- FORM -->

        <form action="${pageContext.request.contextPath}/forgot-password"
              method="post">

            <div class="auth-form-group">

                <label for="email">
                    Gmail
                </label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    value="<%= request.getAttribute("email") != null
                            ? request.getAttribute("email")
                            : "" %>"
                    placeholder="example@gmail.com"
                    autocomplete="email"
                    required
                >

            </div>


            <button
                type="submit"
                class="auth-button"
            >
                Send OTP
            </button>

        </form>


        <div class="auth-links">

            <p>
                Remember your password?

                <a href="${pageContext.request.contextPath}/login">
                    Login
                </a>
            </p>

            <p>
                Don't have an account?

                <a href="${pageContext.request.contextPath}/register">
                    Create Account
                </a>
            </p>

        </div>

    </div>

</div>

</body>
</html>