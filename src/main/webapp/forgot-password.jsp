<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Forgot Password - FreshFruit</title>

    <style>

        /* =====================================================
           GLOBAL
           ===================================================== */

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        html,
        body {
            width: 100%;
            min-height: 100%;
        }

        body {
            font-family: Arial, Helvetica, sans-serif;
            color: #333;
        }

        a {
            text-decoration: none;
        }


        /* =====================================================
           AUTH PAGE
           ===================================================== */

        .auth-page {
            min-height: 100vh;

            display: flex;

            justify-content: center;

            align-items: center;

            padding: 30px;

            background:
                linear-gradient(
                    135deg,
                    #f4faef,
                    #e6f4df
                );
        }


        /* =====================================================
           AUTH CONTAINER
           ===================================================== */

        .auth-container {
            width: 100%;

            max-width: 450px;

            background: white;

            padding: 40px;

            border-radius: 16px;

            box-shadow:
                0 10px 35px rgba(0, 0, 0, 0.08);
        }


        /* =====================================================
           LOGO
           ===================================================== */

        .auth-logo {
            text-align: center;

            font-size: 28px;

            color: #3d8b40;

            font-weight: bold;

            margin-bottom: 15px;
        }


        /* =====================================================
           TITLE
           ===================================================== */

        .auth-container h2 {
            text-align: center;

            color: #234d25;

            margin-bottom: 10px;

            font-size: 28px;
        }


        /* =====================================================
           DESCRIPTION
           ===================================================== */

        .auth-description {
            text-align: center;

            color: #777;

            margin-bottom: 25px;

            font-size: 14px;

            line-height: 1.5;
        }


        /* =====================================================
           SUCCESS MESSAGE
           ===================================================== */

        .success-message {
            background: #f0fff2;

            color: #2e7d32;

            border: 1px solid #ccebd0;

            border-radius: 8px;

            padding: 11px 14px;

            margin-bottom: 18px;

            font-size: 14px;

            line-height: 1.5;
        }


        /* =====================================================
           ERROR MESSAGE
           ===================================================== */

        .error-message {
            background: #fff0f0;

            color: #d32f2f;

            border: 1px solid #ffd0d0;

            border-radius: 8px;

            padding: 11px 14px;

            margin-bottom: 18px;

            font-size: 14px;

            line-height: 1.5;
        }


        /* =====================================================
           FORM GROUP
           ===================================================== */

        .auth-form-group {
            margin-bottom: 18px;
        }


        .auth-form-group label {
            display: block;

            margin-bottom: 7px;

            font-weight: bold;

            font-size: 14px;

            color: #333;
        }


        /* =====================================================
           INPUT
           ===================================================== */

        .auth-form-group input {
            width: 100%;

            padding: 12px 14px;

            border: 1px solid #d9e2d7;

            border-radius: 8px;

            outline: none;

            font-size: 14px;

            background: white;

            transition: 0.2s;
        }


        .auth-form-group input:focus {
            border-color: #4caf50;

            box-shadow:
                0 0 0 3px rgba(76, 175, 80, 0.10);
        }


        .auth-form-group input::placeholder {
            color: #999;
        }


        /* =====================================================
           BUTTON
           ===================================================== */

        .auth-button {
            width: 100%;

            border: none;

            border-radius: 8px;

            padding: 13px;

            background: #4caf50;

            color: white;

            font-weight: bold;

            cursor: pointer;

            font-size: 15px;

            transition: 0.2s;
        }


        .auth-button:hover {
            background: #388e3c;

            transform: translateY(-1px);
        }


        .auth-button:active {
            transform: translateY(0);
        }


        /* =====================================================
           LINKS
           ===================================================== */

        .auth-links {
            margin-top: 20px;

            text-align: center;

            font-size: 14px;

            color: #777;
        }


        .auth-links p {
            margin: 8px 0;
        }


        .auth-links a {
            color: #4caf50;

            font-weight: bold;

            font-size: 14px;
        }


        .auth-links a:hover {
            text-decoration: underline;

            color: #388e3c;
        }


        /* =====================================================
           MOBILE
           ===================================================== */

        @media (max-width: 600px) {

            .auth-page {
                padding: 20px;
            }

            .auth-container {
                padding: 28px 22px;

                border-radius: 14px;
            }

            .auth-logo {
                font-size: 24px;
            }

            .auth-container h2 {
                font-size: 24px;
            }
        }

    </style>

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
            Enter your Gmail and we will send you a 6-digit OTP.
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
            action="${pageContext.request.contextPath}/forgot-password"
            method="post"
            onsubmit="return validateForgotPassword();">


            <!-- =================================================
                 EMAIL
                 ================================================= -->

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

                <a
                    href="${pageContext.request.contextPath}/login">

                    Login

                </a>

            </p>


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
            "Please enter your Gmail address.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // CHECK EMAIL FORMAT
    // =========================================================

    const emailPattern =
        /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;


    if (!emailPattern.test(email)) {

        error.innerHTML =
            "Please enter a valid Gmail address.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // CHECK GMAIL DOMAIN
    // =========================================================

    if (!email.toLowerCase().endsWith("@gmail.com")) {

        error.innerHTML =
            "Please enter a valid Gmail address.";

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