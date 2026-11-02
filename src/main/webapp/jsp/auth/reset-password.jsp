<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Đặt lại mật khẩu - FreshFruit</title>

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

        <h2>Đặt lại mật khẩu</h2>


        <p class="auth-description">
            Tạo mật khẩu mới cho tài khoản của bạn.
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
                    Mật khẩu mới
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Nhập mật khẩu mới"
                    autocomplete="new-password"
                >

                <div class="password-note">
                    Tối thiểu 8 ký tự, bao gồm
                    ít nhất một chữ cái và một chữ số.
                </div>

            </div>


            <!-- =================================================
                 CONFIRM PASSWORD
                 ================================================= -->

            <div class="auth-form-group">

                <label for="confirmPassword">
                    Xác nhận mật khẩu mới
                </label>

                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    placeholder="Nhập lại mật khẩu mới"
                    autocomplete="new-password"
                >

            </div>


            <!-- =================================================
                 RESET BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Đặt lại mật khẩu

            </button>


        </form>


        <!-- =================================================
             LOGIN LINK
             ================================================= -->

        <div class="auth-links">

            <p>

                Nhớ mật khẩu rồi?

                <a href="${pageContext.request.contextPath}/login">

                    Đăng nhập

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
            "Mật khẩu không được để trống.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD LENGTH
    // =========================================================

    if (password.length < 8) {

        error.innerHTML =
            "Mật khẩu phải có ít nhất 8 ký tự.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD LETTER
    // =========================================================

    if (!/[A-Za-z]/.test(password)) {

        error.innerHTML =
            "Mật khẩu phải chứa ít nhất một chữ cái.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD NUMBER
    // =========================================================

    if (!/[0-9]/.test(password)) {

        error.innerHTML =
            "Mật khẩu phải chứa ít nhất một chữ số.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // CONFIRM PASSWORD EMPTY
    // =========================================================

    if (confirmPassword === "") {

        error.innerHTML =
            "Xác nhận mật khẩu không được để trống.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // PASSWORD MATCH
    // =========================================================

    if (password !== confirmPassword) {

        error.innerHTML =
            "Mật khẩu xác nhận không khớp.";

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