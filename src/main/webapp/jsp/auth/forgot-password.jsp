<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Forgot Password - FreshFruit</title>

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
            Forgot Password
        </h2>


        <p class="auth-description">
            Enter your email address and we will send you
            a 6-digit OTP.
        </p>


        <!-- =================================================
             SUCCESS MESSAGE
             ================================================= -->

        <% if (request.getAttribute("success") != null) { %>

            <div class="success-message">

                ✅
                <%= request.getAttribute("success") %>

            </div>

        <% } %>


        <!-- =================================================
             SERVER ERROR MESSAGE
             ================================================= -->

        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">

                ❌
                <%= request.getAttribute("error") %>

            </div>

        <% } %>


        <!-- =================================================
             CLIENT ERROR MESSAGE
             ================================================= -->

        <div
            id="clientError"
            class="error-message"
            style="display: none;">
        </div>


        <!-- =================================================
             FORGOT PASSWORD FORM
             ================================================= -->

        <form
            action="<%= contextPath %>/forgot-password"
            method="post"
            onsubmit="return validateForgotPassword();">


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
                 SEND OTP BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Send OTP

            </button>


        </form>


        <!-- =================================================
             LINKS
             ================================================= -->

        <div class="auth-links">


            <p>

                Remember your password?

                <a href="<%= contextPath %>/login">

                    Login

                </a>

            </p>


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

function validateForgotPassword() {

    const email =
        document.getElementById("email")
            .value
            .trim();

    const error =
        document.getElementById("clientError");


    // =========================================================
    // CLEAR OLD ERROR
    // =========================================================

    error.style.display = "none";
    error.innerHTML = "";


    // =========================================================
    // CHECK EMPTY
    // =========================================================

    if (email === "") {

        error.innerHTML =
            "Please enter your email address.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // CHECK EMAIL FORMAT
    //
    // Any email domain is accepted, matching the
    // server-side validation.
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
    // VALID
    // =========================================================

    return true;
}

</script>


</body>

</html>
