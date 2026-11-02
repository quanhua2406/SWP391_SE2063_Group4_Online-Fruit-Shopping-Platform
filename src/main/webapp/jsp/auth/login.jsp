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

    <title>Đăng nhập - FreshFruit</title>

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
            Đăng nhập
        </h2>


        <p class="auth-description">

            Chọn vai trò đăng nhập, sau đó
            nhập thông tin tài khoản của bạn.

        </p>


        <!-- =================================================
             REGISTER SUCCESS
             ================================================= -->

        <% if ("success".equals(request.getParameter("registered"))) { %>

            <div class="success-message">

                ✅ Đăng ký thành công.
                Vui lòng đăng nhập để tiếp tục.

            </div>

        <% } %>


        <!-- =================================================
             RESET PASSWORD SUCCESS
             ================================================= -->

        <% if ("success".equals(request.getParameter("reset"))) { %>

            <div class="success-message">

                ✅ Đặt lại mật khẩu thành công.
                Vui lòng đăng nhập bằng mật khẩu mới.

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
                Đăng nhập với vai trò
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
                            Quản trị viên
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
                    placeholder="Nhập email"
                    autocomplete="email"
                >

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
                    autocomplete="current-password"
                >

            </div>


            <!-- =================================================
                 FORGOT PASSWORD
                 ================================================= -->

            <div class="forgot-password">

                <a href="<%= contextPath %>/forgot-password">

                    Quên mật khẩu?

                </a>

            </div>


            <!-- =================================================
                 LOGIN BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Đăng nhập

            </button>


        </form>


        <!-- =================================================
             REGISTER LINK
             ================================================= -->

        <div class="auth-links">

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
            "Vui lòng chọn vai trò đăng nhập.";

        error.style.display = "block";

        return false;
    }


    // =========================================================
    // EMAIL EMPTY
    // =========================================================

    if (email === "") {

        error.innerHTML =
            "Email không được để trống.";

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
            "Vui lòng nhập địa chỉ email hợp lệ.";

        error.style.display = "block";

        return false;
    }


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
    // VALID
    // =========================================================

    return true;
}

</script>


</body>

</html>
