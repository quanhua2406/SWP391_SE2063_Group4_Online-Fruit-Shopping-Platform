<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Reset Password - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

</head>

<body>

<div class="auth-page">

    <div class="auth-container">

        <div class="auth-logo">
            🍊 FreshFruit
        </div>

        <h2>Reset Password</h2>

        <p class="auth-description">
            Create a new password for your account.
        </p>


        <!-- ERROR -->

        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">

                ❌
                <%= request.getAttribute("error") %>

            </div>

        <% } %>


        <!-- FORM -->

        <form action="${pageContext.request.contextPath}/reset-password"
              method="post">


            <!-- EMAIL -->

            <div class="auth-form-group">

                <label for="email">
                    Gmail
                </label>

                <input
                    type="email"
                    id="email"
                    value="<%= request.getAttribute("email") != null
                            ? request.getAttribute("email")
                            : "" %>"
                    readonly
                >

            </div>


            <!-- NEW PASSWORD -->

            <div class="auth-form-group">

                <label for="newPassword">
                    New Password
                </label>

                <input
                    type="password"
                    id="newPassword"
                    name="newPassword"
                    placeholder="At least 8 characters"
                    autocomplete="new-password"
                    required
                >

                <small>
                    At least 8 characters, including letters and numbers.
                </small>

            </div>


            <!-- CONFIRM PASSWORD -->

            <div class="auth-form-group">

                <label for="confirmPassword">
                    Confirm Password
                </label>

                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    placeholder="Re-enter your password"
                    autocomplete="new-password"
                    required
                >

            </div>


            <!-- BUTTON -->

            <button
                type="submit"
                class="auth-button"
            >
                Reset Password
            </button>

        </form>


        <div class="auth-links">

            <a href="${pageContext.request.contextPath}/login">
                Back to Login
            </a>

        </div>

    </div>

</div>

</body>
</html>