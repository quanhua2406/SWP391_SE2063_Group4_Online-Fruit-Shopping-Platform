<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Register - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<div class="auth-page">

    <div class="auth-container">

        <!-- Logo -->
        <div class="auth-logo">
            🍊 FreshFruit
        </div>

        <!-- Title -->
        <h2>Create Account</h2>

        <p class="auth-description">
            Create your account to start shopping fresh fruits.
        </p>

        <!-- Error message -->
        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">
                <%= request.getAttribute("error") %>
            </div>

        <% } %>

        <!-- Register form -->
        <form action="${pageContext.request.contextPath}/register"
              method="post">

            <!-- Full Name -->
            <div class="auth-form-group">

                <label for="fullName">
                    Full Name
                </label>

                <input type="text"
                       id="fullName"
                       name="fullName"
                       value="<%= request.getAttribute("fullName") != null
                               ? request.getAttribute("fullName")
                               : "" %>"
                       placeholder="Enter your full name"
                       maxlength="100"
                       required>

            </div>


            <!-- Email -->
            <div class="auth-form-group">

                <label for="email">
                    Gmail
                </label>

                <input type="email"
                       id="email"
                       name="email"
                       value="<%= request.getAttribute("email") != null
                               ? request.getAttribute("email")
                               : "" %>"
                       placeholder="example@gmail.com"
                       maxlength="100"
                       required>

            </div>


            <!-- Password -->
            <div class="auth-form-group">

                <label for="password">
                    Password
                </label>

                <input type="password"
                       id="password"
                       name="password"
                       placeholder="At least 8 characters"
                       required>

                <small>
                    At least 8 characters, including letters and numbers.
                </small>

            </div>


            <!-- Confirm Password -->
            <div class="auth-form-group">

                <label for="confirmPassword">
                    Confirm Password
                </label>

                <input type="password"
                       id="confirmPassword"
                       name="confirmPassword"
                       placeholder="Re-enter your password"
                       required>

            </div>


            <!-- Phone -->
            <div class="auth-form-group">

                <label for="phone">
                    Phone Number
                </label>

                <input type="tel"
                       id="phone"
                       name="phone"
                       value="<%= request.getAttribute("phone") != null
                               ? request.getAttribute("phone")
                               : "" %>"
                       placeholder="0912345678"
                       maxlength="11">

            </div>


            <!-- Address -->
            <div class="auth-form-group">

                <label for="address">
                    Address
                </label>

                <input type="text"
                       id="address"
                       name="address"
                       value="<%= request.getAttribute("address") != null
                               ? request.getAttribute("address")
                               : "" %>"
                       placeholder="Enter your address"
                       maxlength="255">

            </div>


            <!-- Register button -->
            <button type="submit"
                    class="auth-button">
                Create Account
            </button>

        </form>


        <!-- Login link -->
        <div class="auth-links">

            <p>
                Already have an account?
                <a href="${pageContext.request.contextPath}/login">
                    Login
                </a>
            </p>

        </div>

    </div>

</div>

</body>
</html>