<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>Login - FreshFruit</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">

</head>

<body>

<div class="auth-page">

    <div class="auth-container">

        <!-- =========================
             LOGO
             ========================= -->

        <div class="auth-logo">
            🍊 FreshFruit
        </div>


        <!-- =========================
             TITLE
             ========================= -->

        <h2>Welcome Back</h2>

        <p class="auth-description">
            Login to your FreshFruit account.
        </p>


        <!-- =========================
             SUCCESS MESSAGE
             ========================= -->

        <%
            String successMessage =
                    (String) request.getAttribute("success");

            String logout =
                    request.getParameter("logout");

            if ("success".equals(logout)) {

                successMessage =
                        "You have been logged out successfully.";
            }
        %>

        <% if (successMessage != null
                && !successMessage.isBlank()) { %>

            <div class="success-message">

                ✅ <%= successMessage %>

            </div>

        <% } %>


        <!-- =========================
             ERROR MESSAGE
             ========================= -->

        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">

                ❌ <%= request.getAttribute("error") %>

            </div>

        <% } %>


        <!-- =========================
             LOGIN FORM
             ========================= -->

        <form action="${pageContext.request.contextPath}/login"
              method="post">


            <!-- Gmail -->

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
                    maxlength="100"
                    required
                >

            </div>


            <!-- Password -->

            <div class="auth-form-group">

                <label for="password">
                    Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter your password"
                    autocomplete="current-password"
                    required
                >

            </div>


            <!-- Login button -->

            <button
                type="submit"
                class="auth-button"
            >
                Login
            </button>


        </form>


        <!-- =========================
             LINKS
             ========================= -->

        <div class="auth-links">


            <!-- Forgot Password -->

            <p>

                <a href="${pageContext.request.contextPath}/forgot-password">

                    Forgot Password?

                </a>

            </p>


            <!-- Register -->

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