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


        <!-- =================================================
             LOGO
             ================================================= -->

        <div class="auth-logo">
            🍊 FreshFruit
        </div>


        <!-- =================================================
             TITLE
             ================================================= -->

        <h2>Verify OTP</h2>


        <p class="auth-description">
            Enter the 6-digit OTP sent to your email.
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
             OTP FORM
             ================================================= -->

        <form
            action="${pageContext.request.contextPath}/verify-otp"
            method="post"
            onsubmit="return validateOTP();">


            <div class="auth-form-group">

                <label for="otp">
                    OTP Code
                </label>

                <input
                    type="text"
                    id="otp"
                    name="otp"
                    placeholder="Enter 6-digit OTP"
                    maxlength="6"
                    inputmode="numeric"
                    autocomplete="one-time-code"
                >

            </div>


            <!-- =================================================
                 VERIFY BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Verify OTP

            </button>


        </form>


        <!-- =================================================
             BACK LINK
             ================================================= -->

        <div class="auth-links">

            <p>

                Didn't receive the OTP?

                <a href="${pageContext.request.contextPath}/forgot-password">

                    Send Again

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


<!-- =========================================================
     JAVASCRIPT VALIDATION
     ========================================================= -->

<script>

function validateOTP() {

    const otp =
        document.getElementById("otp")
            .value
            .trim();

    const error =
        document.getElementById("clientError");


    // =========================================================
    // CLEAR ERROR
    // =========================================================

    error.style.display = "none";
    error.innerHTML = "";


    // =========================================================
    // EMPTY
    // =========================================================

    if (otp === "") {

        error.innerHTML =
            "OTP cannot be empty.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // EXACTLY 6 DIGITS
    // =========================================================

    if (!/^\d{6}$/.test(otp)) {

        error.innerHTML =
            "OTP must contain exactly 6 digits.";

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