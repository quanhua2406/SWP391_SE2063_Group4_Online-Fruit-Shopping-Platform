<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String selectedRole =
            (String) request.getAttribute("selectedRole");

    if (selectedRole == null) {
        selectedRole = "";
    }

    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Login - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">

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

        <h2>
            Login
        </h2>


        <p class="auth-description">

            Choose who you are signing in as, then
            enter your account details.

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
            action="<%= contextPath %>/login"
            method="post"
            onsubmit="return validateLogin();">


            <!-- =================================================
                 ROLE SELECTION
                 ================================================= -->

            <label class="role-label">
                Login as
            </label>


            <div class="role-selector">

                <label class="role-option">

                    <input
                        type="radio"
                        name="role"
                        value="CUSTOMER"
                        <%= "CUSTOMER".equalsIgnoreCase(selectedRole)
                                ? "checked" : "" %>>

                    <span class="role-card">

                        <span class="role-icon">🛒</span>

                        <span class="role-name">
                            Customer
                        </span>

                    </span>

                </label>


                <label class="role-option">

                    <input
                        type="radio"
                        name="role"
                        value="SHOPOWNER"
                        <%= "SHOPOWNER".equalsIgnoreCase(selectedRole)
                                ? "checked" : "" %>>

                    <span class="role-card">

                        <span class="role-icon">🏪</span>

                        <span class="role-name">
                            Shop Owner
                        </span>

                    </span>

                </label>


                <label class="role-option">

                    <input
                        type="radio"
                        name="role"
                        value="DELIVERYSTAFF"
                        <%= "DELIVERYSTAFF".equalsIgnoreCase(selectedRole)
                                ? "checked" : "" %>>

                    <span class="role-card">

                        <span class="role-icon">🚚</span>

                        <span class="role-name">
                            Delivery Staff
                        </span>

                    </span>

                </label>


                <label class="role-option">

                    <input
                        type="radio"
                        name="role"
                        value="ADMIN"
                        <%= "ADMIN".equalsIgnoreCase(selectedRole)
                                ? "checked" : "" %>>

                    <span class="role-card">

                        <span class="role-icon">🛠️</span>

                        <span class="role-name">
                            Admin
                        </span>

                    </span>

                </label>

            </div>


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

            <div class="forgot-password">

                <a href="<%= contextPath %>/forgot-password">

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

                <a href="<%= contextPath %>/register">

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

    const role =
        document.querySelector(
            'input[name="role"]:checked');

    const error =
        document.getElementById("clientError");


    // =========================================================
    // CLEAR ERROR
    // =========================================================

    error.style.display = "none";

    error.innerHTML = "";


    // =========================================================
    // ROLE REQUIRED
    // =========================================================

    if (!role) {

        error.innerHTML =
            "Please choose who you are logging in as.";

        error.style.display = "block";

        return false;
    }


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
