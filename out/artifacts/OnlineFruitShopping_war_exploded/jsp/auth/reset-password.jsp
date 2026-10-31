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


        <!-- =================================================
             LOGO
             ================================================= -->

        <div class="auth-logo">
            🍊 FreshFruit
        </div>


        <!-- =================================================
             TITLE
             ================================================= -->

        <h2>Reset Password</h2>


        <p class="auth-description">
            Create a new password for your account.
        </p>


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
             RESET PASSWORD FORM
             ================================================= -->

        <form
            action="${pageContext.request.contextPath}/reset-password"
            method="post"
            onsubmit="return validateResetPassword();">


            <!-- =================================================
                 NEW PASSWORD
                 ================================================= -->

            <div class="auth-form-group">

                <label for="password">
                    New Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter your new password"
                    autocomplete="new-password"
                >

                <div class="password-note">
                    Minimum 8 characters, including
                    at least one letter and one number.
                </div>

            </div>


            <!-- =================================================
                 CONFIRM PASSWORD
                 ================================================= -->

            <div class="auth-form-group">

                <label for="confirmPassword">
                    Confirm New Password
                </label>

                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    placeholder="Confirm your new password"
                    autocomplete="new-password"
                >

            </div>


            <!-- =================================================
                 RESET BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Reset Password

            </button>


        </form>


        <!-- =================================================
             LOGIN LINK
             ================================================= -->

        <div class="auth-links">

            <p>

                Remember your password?

                <a href="${pageContext.request.contextPath}/login">

                    Login

                </a>

            </p>

        </div>


    </div>

</div>


<!-- =========================================================
     JAVASCRIPT VALIDATION
     ========================================================= -->

<script>

function validateResetPassword() {

    const password =
        document.getElementById("password")
            .value;

    const confirmPassword =
        document.getElementById("confirmPassword")
            .value;

    const error =
        document.getElementById("clientError");


    // =========================================================
    // CLEAR ERROR
    // =========================================================

    error.style.display = "none";
    error.innerHTML = "";


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
    // PASSWORD LENGTH
    // =========================================================

    if (password.length < 8) {

        error.innerHTML =
            "Password must contain at least 8 characters.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD LETTER
    // =========================================================

    if (!/[A-Za-z]/.test(password)) {

        error.innerHTML =
            "Password must contain at least one letter.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD NUMBER
    // =========================================================

    if (!/[0-9]/.test(password)) {

        error.innerHTML =
            "Password must contain at least one number.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // CONFIRM PASSWORD EMPTY
    // =========================================================

    if (confirmPassword === "") {

        error.innerHTML =
            "Confirm password cannot be empty.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD MATCH
    // =========================================================

    if (password !== confirmPassword) {

        error.innerHTML =
            "Passwords do not match.";

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