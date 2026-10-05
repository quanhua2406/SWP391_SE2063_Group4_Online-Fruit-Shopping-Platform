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


        <!-- =================================================
             LOGO
             ================================================= -->

        <div class="auth-logo">
            🍊 FreshFruit
        </div>


        <!-- =================================================
             TITLE
             ================================================= -->

        <h2>Login</h2>


        <p class="auth-description">
            Login to your FreshFruit account.
        </p>


        <!-- =================================================
             REGISTER SUCCESS
             ================================================= -->

        <% if ("success".equals(request.getParameter("registered"))) { %>

            <div class="success-message">

                ✅ Registration successful.
                Please login to continue.

            </div>

        <% } %>


        <!-- =================================================
             RESET PASSWORD SUCCESS
             ================================================= -->

        <% if ("success".equals(request.getParameter("reset"))) { %>

            <div class="success-message">

                ✅ Password reset successfully.
                Please login with your new password.

            </div>

        <% } %>


        <!-- =================================================
             SERVER ERROR
             ================================================= -->

        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">

                ❌
                <%= request.getAttribute("error") %>

            </div>

        <% } %>


        <!-- =================================================
             CLIENT ERROR
             ================================================= -->

        <div
            id="clientError"
            class="error-message"
            style="display: none;">
        </div>


        <!-- =================================================
             LOGIN FORM
             ================================================= -->

        <form
            action="${pageContext.request.contextPath}/login"
            method="post"
            onsubmit="return validateLogin();">


            <!-- =================================================
                 EMAIL
                 ================================================= -->

            <div class="auth-form-group">

                <label for="email">
                    Email
                </label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    value="<%= request.getAttribute("email") != null
                            ? request.getAttribute("email")
                            : "" %>"
                    placeholder="Enter your email"
                    autocomplete="email"
                >

            </div>


            <!-- =================================================
                 PASSWORD
                 ================================================= -->

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
                >

            </div>


            <!-- =================================================
                 FORGOT PASSWORD
                 ================================================= -->

            <div
                style="
                    text-align: right;
                    margin-top: -5px;
                    margin-bottom: 18px;
                ">

                <a
                    href="${pageContext.request.contextPath}/forgot-password"
                    style="
                        color: #2e7d32;
                        text-decoration: none;
                        font-size: 14px;
                    ">

                    Forgot Password?

                </a>

            </div>


            <!-- =================================================
                 LOGIN BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Login

            </button>


        </form>


        <!-- =================================================
             REGISTER LINK
             ================================================= -->

        <div class="auth-links">

            <p>

                Don't have an account?

                <a
                    href="${pageContext.request.contextPath}/register">

                    Create Account

                </a>

            </p>

        </div>


    </div>

</div>


<!-- =========================================================
     JAVASCRIPT VALIDATION
     ========================================================= -->

<script>

function validateLogin() {

    const email =
        document.getElementById("email")
            .value
            .trim();

    const password =
        document.getElementById("password")
            .value;

    const error =
        document.getElementById("clientError");


    // =========================================================
    // CLEAR ERROR
    // =========================================================

    error.style.display = "none";
    error.innerHTML = "";


    // =========================================================
    // EMAIL EMPTY
    // =========================================================

    if (email === "") {

        error.innerHTML =
            "Email cannot be empty.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // EMAIL FORMAT
    // =========================================================

    const emailPattern =
        /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;


    if (!emailPattern.test(email)) {

        error.innerHTML =
            "Please enter a valid email address.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD EMPTY
    // =========================================================

    if (password === "") {

        error.innerHTML =
            "Password cannot be empty.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // VALID
    // =========================================================

    return true;
}

</script>


</body>

</html>