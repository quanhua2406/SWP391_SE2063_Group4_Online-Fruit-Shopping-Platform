<%@page import="com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO.UserInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    UserInfo user = (UserInfo) request.getAttribute("user");

    if (user == null) {
        response.sendRedirect(
            request.getContextPath() + "/admin/user-management"
        );
        return;
    }

    String contextPath = request.getContextPath();

    String fullName = (String) request.getAttribute("formFullName");
    String email = (String) request.getAttribute("formEmail");
    String phone = (String) request.getAttribute("formPhone");
    String error = (String) request.getAttribute("error");

    if (fullName == null) {
        fullName = user.getFullName() == null ? "" : user.getFullName();
    }

    if (email == null) {
        email = user.getEmail() == null ? "" : user.getEmail();
    }

    if (phone == null) {
        phone = user.getPhone() == null ? "" : user.getPhone();
    }

    String role = user.getRoleName() == null ? "" : user.getRoleName();
    String status = user.getStatus() == null ? "" : user.getStatus();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Sửa người dùng</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 30px 16px;
            font-family: Arial, sans-serif;
            color: #333;
            background: #f4f6f9;
        }

        .container {
            max-width: 900px;
            margin: 20px auto;
            padding: 35px 40px;
            background: white;
            border: 1px solid #ddd;
            border-radius: 14px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
            padding-bottom: 22px;
            border-bottom: 1px solid #eee;
            margin-bottom: 28px;
        }

        h1 {
            margin: 0;
            font-size: 30px;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px 28px;
        }

        .field label {
            display: block;
            margin-bottom: 10px;
            font-weight: 600;
            font-size: 15px;
        }

        .required {
            color: #d32f2f;
        }

        .field input {
            width: 100%;
            min-height: 54px;
            padding: 12px 15px;
            border: 1px solid #d5d5d5;
            border-radius: 8px;
            font-size: 17px;
            background: white;
        }

        .field input:focus {
            outline: none;
            border-color: #1976d2;
            box-shadow: 0 0 0 2px #1976d222;
        }

        .field input[readonly] {
            background: #f4f5f6;
            color: #666;
        }

        .error {
            margin-bottom: 22px;
            padding: 14px 16px;
            color: #b42318;
            background: #fef3f2;
            border: 1px solid #fecdca;
            border-radius: 8px;
        }

        .actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
            border-top: 1px solid #eee;
            margin-top: 30px;
            padding-top: 25px;
        }

        .btn {
            display: inline-block;
            padding: 14px 23px;
            border: 0;
            border-radius: 7px;
            font-size: 16px;
            text-decoration: none;
            cursor: pointer;
        }

        .save {
            color: white;
            background: #1976d2;
        }

        .save:hover {
            background: #125eaa;
        }

        .cancel {
            color: #333;
            background: #eee;
        }

        .cancel:hover {
            background: #ddd;
        }

        @media (max-width: 650px) {
            .container {
                padding: 24px 18px;
            }

            .grid {
                grid-template-columns: 1fr;
            }

            .header {
                align-items: flex-start;
                flex-direction: column;
            }
        }
    </style>
</head>

<body>

<div class="container">

    <div class="header">
        <h1>Sửa người dùng</h1>

        <a class="btn cancel"
           href="<%= contextPath %>/admin/user-management?action=detail&amp;userId=<%= user.getUserId() %>">
            Quay lại chi tiết
        </a>
    </div>

    <% if (error != null && !error.isBlank()) { %>
        <div class="error" role="alert">
            <%= escapeHtml(error) %>
        </div>
    <% } %>

    <form id="editForm"
          method="post"
          action="<%= contextPath %>/admin/user-management"
          novalidate>

        <input type="hidden" name="action" value="update">

        <input type="hidden"
               name="userId"
               value="<%= user.getUserId() %>">

        <div class="grid">

            <div class="field">
                <label for="userIdDisplay">Mã người dùng</label>
                <input id="userIdDisplay"
                       value="<%= user.getUserId() %>"
                       readonly>
            </div>

            <div class="field">
                <label for="role">Vai trò</label>
                <input id="role"
                       value="<%= escapeHtml(role) %>"
                       readonly>
            </div>

            <div class="field">
                <label for="fullName">
                    Họ và tên <span class="required">*</span>
                </label>
                <input id="fullName"
                       name="fullName"
                       type="text"
                       value="<%= escapeHtml(fullName) %>"
                       maxlength="100"
                       autocomplete="name"
                       required>
            </div>

            <div class="field">
                <label for="email">
                    Email <span class="required">*</span>
                </label>
                <input id="email"
                       name="email"
                       type="email"
                       value="<%= escapeHtml(email) %>"
                       maxlength="150"
                       autocomplete="email"
                       required>
            </div>

            <div class="field">
                <label for="phone">
                    Số điện thoại <span class="required">*</span>
                </label>
                <input id="phone"
                       name="phone"
                       type="tel"
                       value="<%= escapeHtml(phone) %>"
                       maxlength="11"
                       autocomplete="tel"
                       required>
            </div>

            <div class="field">
                <label for="status">Trạng thái</label>
                <input id="status"
                       value="<%= escapeHtml(status) %>"
                       readonly>
            </div>

        </div>

        <div class="actions">
            <button type="submit" class="btn save">
                Lưu thay đổi
            </button>

            <a class="btn cancel"
               href="<%= contextPath %>/admin/user-management?action=detail&amp;userId=<%= user.getUserId() %>">
                Hủy
            </a>
        </div>

    </form>
</div>

<script>
    const form = document.getElementById("editForm");

    const fullName = document.getElementById("fullName");
    const email = document.getElementById("email");
    const phone = document.getElementById("phone");

    function showError(input, message) {
        input.setCustomValidity(message);
        input.reportValidity();
        input.focus();
    }

    function validateFullName() {
        const value = fullName.value.trim();

        if (value.length === 0) {
            showError(fullName, "Vui lòng nhập họ và tên.");
            return false;
        }

        if (value.length < 2 || value.length > 100) {
            showError(
                fullName,
                "Họ và tên phải có từ 2 đến 100 ký tự."
            );
            return false;
        }

        if (!/^[\p{L}\p{M} .'-]+$/u.test(value)) {
            showError(
                fullName,
                "Họ và tên chứa ký tự không hợp lệ."
            );
            return false;
        }

        fullName.setCustomValidity("");
        return true;
    }

    function validateEmail() {
        const value = email.value.trim();

        if (value.length === 0) {
            showError(email, "Vui lòng nhập email.");
            return false;
        }

        if (value.length > 150 ||
            !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
            showError(email, "Vui lòng nhập địa chỉ email hợp lệ.");
            return false;
        }

        email.setCustomValidity("");
        return true;
    }

    function validatePhone() {
        const value = phone.value.trim();

        if (value.length === 0) {
            showError(phone, "Vui lòng nhập số điện thoại.");
            return false;
        }

        if (!/^0[0-9]{9,10}$/.test(value)) {
            showError(
                phone,
                "Số điện thoại phải bắt đầu bằng số 0 và có 10 hoặc 11 chữ số."
            );
            return false;
        }

        phone.setCustomValidity("");
        return true;
    }

    fullName.addEventListener("input", function () {
        fullName.setCustomValidity("");
    });

    email.addEventListener("input", function () {
        email.setCustomValidity("");
    });

    phone.addEventListener("input", function () {
        phone.setCustomValidity("");
    });

    form.addEventListener("submit", function (event) {
        fullName.value = fullName.value.trim();
        email.value = email.value.trim();
        phone.value = phone.value.trim();

        if (!validateFullName()) {
            event.preventDefault();
            return;
        }

        if (!validateEmail()) {
            event.preventDefault();
            return;
        }

        if (!validatePhone()) {
            event.preventDefault();
        }
    });
</script>

</body>
</html>

<%!
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>