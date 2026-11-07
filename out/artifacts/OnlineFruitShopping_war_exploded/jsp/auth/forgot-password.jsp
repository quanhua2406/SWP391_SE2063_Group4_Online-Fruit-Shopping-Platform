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

    <title>Quên mật khẩu - FreshFruit</title>

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
            Quên mật khẩu
        </h2>


        <p class="auth-description">
            Nhập địa chỉ email, chúng tôi sẽ gửi cho bạn
            mã OTP gồm 6 chữ số.
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
                    placeholder="Nhập email"
                    autocomplete="email"
                >

            </div>


            <!-- =================================================
                 SEND OTP BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Gửi mã OTP

            </button>


        </form>


        <!-- =================================================
             LINKS
             ================================================= -->

        <div class="auth-links">


            <p>

                Nhớ mật khẩu rồi?

                <a href="<%= contextPath %>/login">

                    Đăng nhập

                </a>

            </p>


            <p>

                Chưa có tài khoản?

                <a href="<%= contextPath %>/register">

                    Tạo tài khoản

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
            "Vui lòng nhập địa chỉ email.";

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
            "Vui lòng nhập địa chỉ email hợp lệ.";

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
