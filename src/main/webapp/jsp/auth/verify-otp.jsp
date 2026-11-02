<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Xác minh OTP - FreshFruit</title>

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

        <h2>Xác minh OTP</h2>


        <p class="auth-description">
            Nhập mã OTP gồm 6 chữ số đã gửi đến email của bạn.
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
                    Mã OTP
                </label>

                <input
                    type="text"
                    id="otp"
                    name="otp"
                    placeholder="Nhập mã OTP gồm 6 chữ số"
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

                Xác minh OTP

            </button>


        </form>


        <!-- =================================================
             BACK LINK
             ================================================= -->

        <div class="auth-links">

            <p>

                Chưa nhận được mã OTP?

                <a href="${pageContext.request.contextPath}/forgot-password">

                    Gửi lại

                </a>

            </p>


            <p>

                <a href="${pageContext.request.contextPath}/login">

                    Quay lại đăng nhập

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
            "Mã OTP không được để trống.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // EXACTLY 6 DIGITS
    // =========================================================

    if (!/^\d{6}$/.test(otp)) {

        error.innerHTML =
            "Mã OTP phải gồm đúng 6 chữ số.";

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