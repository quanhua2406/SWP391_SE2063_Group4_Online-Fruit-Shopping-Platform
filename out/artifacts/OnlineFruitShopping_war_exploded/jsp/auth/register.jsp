<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String contextPath = request.getContextPath();

    String selectedRole =
            (String) request.getAttribute("role");

    if (selectedRole == null) {
        selectedRole = "";
    }
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Đăng ký - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">

</head>


<body>


<div class="auth-page">

    <div class="auth-container">


        <!-- =================================================
             LOGO + TITLE
             ================================================= -->

        <div class="auth-logo">
            🍊 FreshFruit
        </div>

        <h2>
            Tạo tài khoản
        </h2>

        <p class="auth-description">
            Nhập thông tin để tạo tài khoản FreshFruit.
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
             REGISTER FORM
             ================================================= -->

        <form
            method="POST"
            action="<%= contextPath %>/register"
            onsubmit="return validateRegisterForm();">


            <!-- =================================================
                 ROLE SELECTION
                 (Admin is never selectable here)
                 ================================================= -->

            <label class="role-label">
                Đăng ký với vai trò
            </label>


            <div class="role-selector">

                <label class="role-option">

                    <input
                        type="radio"
                        name="role"
                        value="CUSTOMER"
                        <%= !"SHOPOWNER".equalsIgnoreCase(selectedRole)
                                && !"DELIVERYSTAFF".equalsIgnoreCase(selectedRole)
                                ? "checked" : "" %>>

                    <span class="role-card">

                        <span class="role-icon">🛒</span>

                        <span class="role-name">
                            Khách hàng
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
                            Chủ cửa hàng
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
                            Nhân viên giao hàng
                        </span>

                    </span>

                </label>

            </div>


            <!-- =================================================
                 FULL NAME
                 ================================================= -->

            <div class="auth-form-group">

                <label for="fullName">
                    Họ và tên
                </label>

                <input
                    type="text"
                    id="fullName"
                    name="fullName"
                    value="${fullName}"
                    placeholder="Nhập họ và tên"
                    maxlength="100">

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
                    value="${email}"
                    placeholder="Nhập email"
                    autocomplete="email">

            </div>


            <!-- =================================================
                 PASSWORD
                 ================================================= -->

            <div class="auth-form-group">

                <label for="password">
                    Mật khẩu
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Nhập mật khẩu"
                    autocomplete="new-password">

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
                    Xác nhận mật khẩu
                </label>

                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    placeholder="Nhập lại mật khẩu"
                    autocomplete="new-password">

            </div>


            <!-- =================================================
                 PHONE
                 ================================================= -->

            <div class="auth-form-group">

                <label for="phone">
                    Số điện thoại
                </label>

                <input
                    type="text"
                    id="phone"
                    name="phone"
                    value="${phone}"
                    placeholder="Nhập số điện thoại"
                    maxlength="11">

            </div>


            <!-- =================================================
                 ADDRESS
                 ================================================= -->

            <div class="auth-form-group">

                <label for="address">
                    Địa chỉ
                </label>

                <input
                    type="text"
                    id="address"
                    name="address"
                    value="${address}"
                    placeholder="Nhập địa chỉ"
                    maxlength="255">

            </div>


            <!-- =================================================
                 REGISTER BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Tạo tài khoản

            </button>


        </form>


        <!-- =================================================
             LOGIN LINK
             ================================================= -->

        <div class="auth-links">

            <p>

                Đã có tài khoản?

                <a href="<%= contextPath %>/login">

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

function validateRegisterForm() {

    const fullName =
        document.getElementById("fullName")
            .value
            .trim();

    const email =
        document.getElementById("email")
            .value
            .trim();

    const password =
        document.getElementById("password")
            .value;

    const confirmPassword =
        document.getElementById("confirmPassword")
            .value;

    const phone =
        document.getElementById("phone")
            .value
            .trim();

    const address =
        document.getElementById("address")
            .value
            .trim();

    const roleChecked =
        document.querySelector('input[name="role"]:checked');

    const error =
        document.getElementById("clientError");


    // ---------------------------------------------------------
    // CLEAR ERROR
    // ---------------------------------------------------------

    error.style.display = "none";

    error.innerHTML = "";


    // ---------------------------------------------------------
    // ROLE
    // ---------------------------------------------------------

    if (!roleChecked) {

        error.innerHTML =
            "Vui lòng chọn vai trò đăng ký.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // FULL NAME
    // ---------------------------------------------------------

    if (fullName === "") {

        error.innerHTML =
            "Họ tên không được để trống.";

        error.style.display = "block";

        return false;
    }


    if (fullName.length < 2) {

        error.innerHTML =
            "Họ tên phải có ít nhất 2 ký tự.";

        error.style.display = "block";

        return false;
    }


    if (!/\p{L}/u.test(fullName)) {

        error.innerHTML =
            "Họ tên phải chứa chữ cái.";

        error.style.display = "block";

        return false;
    }


    if (!/^[\p{L}\s]+$/u.test(fullName)) {

        error.innerHTML =
            "Họ tên chỉ được chứa chữ cái và khoảng trắng.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // EMAIL
    // ---------------------------------------------------------

    if (email === "") {

        error.innerHTML =
            "Email không được để trống.";

        error.style.display = "block";

        return false;
    }


    const emailPattern =
        /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;

    if (!emailPattern.test(email)) {

        error.innerHTML =
            "Vui lòng nhập địa chỉ email hợp lệ.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // PASSWORD
    // ---------------------------------------------------------

    if (password === "") {

        error.innerHTML =
            "Mật khẩu không được để trống.";

        error.style.display = "block";

        return false;
    }


    if (password.length < 8) {

        error.innerHTML =
            "Mật khẩu phải có ít nhất 8 ký tự.";

        error.style.display = "block";

        return false;
    }


    if (!/[A-Za-z]/.test(password)) {

        error.innerHTML =
            "Mật khẩu phải chứa ít nhất một chữ cái.";

        error.style.display = "block";

        return false;
    }


    if (!/[0-9]/.test(password)) {

        error.innerHTML =
            "Mật khẩu phải chứa ít nhất một chữ số.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // CONFIRM PASSWORD
    // ---------------------------------------------------------

    if (confirmPassword === "") {

        error.innerHTML =
            "Xác nhận mật khẩu không được để trống.";

        error.style.display = "block";

        return false;
    }


    if (password !== confirmPassword) {

        error.innerHTML =
            "Mật khẩu xác nhận không khớp.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // PHONE
    // ---------------------------------------------------------

    if (phone === "") {

        error.innerHTML =
            "Số điện thoại không được để trống.";

        error.style.display = "block";

        return false;
    }


    if (!/^[0-9]+$/.test(phone)) {

        error.innerHTML =
            "Số điện thoại chỉ được chứa chữ số.";

        error.style.display = "block";

        return false;
    }


    if (phone.length < 9 ||
        phone.length > 11) {

        error.innerHTML =
            "Số điện thoại phải có từ 9 đến 11 chữ số.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // ADDRESS
    // ---------------------------------------------------------

    if (address === "") {

        error.innerHTML =
            "Địa chỉ không được để trống.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // VALID
    // ---------------------------------------------------------

    return true;
}

</script>


</body>

</html>
