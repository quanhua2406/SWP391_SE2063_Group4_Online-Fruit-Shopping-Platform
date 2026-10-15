
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String cp = request.getContextPath();
    String error = (String) request.getAttribute("error");

    String fullName = request.getAttribute("fullName") == null
            ? "" : request.getAttribute("fullName").toString();
    String email = request.getAttribute("email") == null
            ? "" : request.getAttribute("email").toString();
    String phone = request.getAttribute("phone") == null
            ? "" : request.getAttribute("phone").toString();
    String role = request.getAttribute("role") == null
            ? "CUSTOMER" : request.getAttribute("role").toString();

    java.util.function.Function<String, String> esc = s -> s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add User | FreshFruit</title>

    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7f4;
            color: #263328;
        }

        .topbar {
            background: #1b5e20;
            color: white;
            padding: 20px 6%;
            font-size: 22px;
            font-weight: bold;
        }

        .container {
            max-width: 760px;
            margin: 35px auto;
            padding: 0 18px;
        }

        .back {
            display: inline-block;
            margin-bottom: 18px;
            color: #2e7d32;
            text-decoration: none;
            font-weight: bold;
        }

        .card {
            background: white;
            padding: 30px;
            border-radius: 14px;
            box-shadow: 0 5px 22px rgba(0, 0, 0, .07);
        }

        h1 {
            margin-top: 0;
            margin-bottom: 8px;
            font-size: 28px;
        }

        .subtitle {
            margin-top: 0;
            margin-bottom: 25px;
            color: #68746a;
        }

        .error {
            padding: 12px 15px;
            margin-bottom: 20px;
            color: #a12626;
            background: #fff0f0;
            border: 1px solid #f0caca;
            border-radius: 7px;
        }

        .field { margin-bottom: 18px; }

        label {
            display: block;
            margin-bottom: 7px;
            font-weight: bold;
        }

        input, select {
            width: 100%;
            padding: 12px;
            border: 1px solid #cbd5cc;
            border-radius: 7px;
            background: white;
            font-size: 15px;
        }

        input:focus, select:focus {
            outline: 2px solid #a5d6a7;
            border-color: #2e7d32;
        }

        .required { color: #c62828; }

        .actions {
            display: flex;
            gap: 12px;
            margin-top: 26px;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 20px;
            border: 0;
            border-radius: 7px;
            font-size: 15px;
            font-weight: bold;
            text-decoration: none;
            cursor: pointer;
        }

        .btn-primary {
            background: #2e7d32;
            color: white;
        }

        .btn-primary:hover { background: #1b5e20; }

        .btn-secondary {
            background: #e8eee8;
            color: #344238;
        }

        .hint {
            margin-top: 6px;
            font-size: 12px;
            color: #68746a;
        }

        @media (max-width: 520px) {
            .card { padding: 20px; }
            .actions { flex-direction: column; }
            .btn { width: 100%; }
        }
    </style>
</head>

<body>
    <header class="topbar">FreshFruit — User Administration</header>

    <main class="container">
        <a class="back" href="<%= cp %>/admin/user-management">
            &#8592; Back to User Management
        </a>

        <section class="card">
            <h1>Add User</h1>
            <p class="subtitle">Create a new account and assign its role.</p>

            <% if (error != null) { %>
                <div class="error"><%= esc.apply(error) %></div>
            <% } %>

            <form action="<%= cp %>/admin/user-add"
                  method="post"
                  accept-charset="UTF-8">

                <div class="field">
                    <label for="fullName">Full Name <span class="required">*</span></label>
                    <input id="fullName" name="fullName" type="text"
                           maxlength="100" required
                           value="<%= esc.apply(fullName) %>">
                </div>

                <div class="field">
                    <label for="email">Email <span class="required">*</span></label>
                    <input id="email" name="email" type="email"
                           maxlength="150" required
                           value="<%= esc.apply(email) %>">
                </div>

                <div class="field">
                    <label for="password">Password <span class="required">*</span></label>
                    <input id="password" name="password" type="password"
                           minlength="8" maxlength="72" required
                           autocomplete="new-password">
                    <div class="hint">At least 8 characters, including a letter and a number.</div>
                </div>

                <div class="field">
                    <label for="confirmPassword">Confirm Password <span class="required">*</span></label>
                    <input id="confirmPassword" name="confirmPassword"
                           type="password" minlength="8" maxlength="72"
                           required autocomplete="new-password">
                    <div id="passwordMessage" class="hint"></div>
                </div>

                <div class="field">
                    <label for="phone">Phone</label>
                    <input id="phone" name="phone" type="tel"
                           pattern="[0-9]{9,11}"
                           maxlength="11"
                           value="<%= esc.apply(phone) %>"
                           placeholder="9–11 digits">
                </div>

                <div class="field">
                    <label for="role">Role <span class="required">*</span></label>
                    <select id="role" name="role" required>
                        <option value="CUSTOMER" <%= "CUSTOMER".equalsIgnoreCase(role) ? "selected" : "" %>>Customer</option>
                        <option value="SHOPOWNER" <%= "SHOPOWNER".equalsIgnoreCase(role) || "SHOP_OWNER".equalsIgnoreCase(role) ? "selected" : "" %>>Shop Owner</option>
                        <option value="DELIVERYSTAFF" <%= "DELIVERYSTAFF".equalsIgnoreCase(role) || "DELIVERY_STAFF".equalsIgnoreCase(role) ? "selected" : "" %>>Delivery Staff</option>
                        <option value="GUEST" <%= "GUEST".equalsIgnoreCase(role) ? "selected" : "" %>>Guest</option>
                        <option value="ADMIN" <%= "ADMIN".equalsIgnoreCase(role) ? "selected" : "" %>>Admin</option>
                    </select>
                </div>

                <div class="actions">
                    <button type="submit" class="btn btn-primary">
                        Create User
                    </button>
                    <a class="btn btn-secondary"
                       href="<%= cp %>/admin/user-management">
                        Cancel
                    </a>
                </div>
            </form>
        </section>
    </main>

    <script>
        const password = document.getElementById("password");
        const confirmPassword = document.getElementById("confirmPassword");
        const passwordMessage = document.getElementById("passwordMessage");

        function checkPasswords() {
            if (!confirmPassword.value) {
                passwordMessage.textContent = "";
                confirmPassword.setCustomValidity("");
                return;
            }

            const matches = password.value === confirmPassword.value;

            passwordMessage.textContent = matches
                ? "Passwords match."
                : "Passwords do not match.";

            passwordMessage.style.color = matches ? "#2e7d32" : "#c62828";
            confirmPassword.setCustomValidity(
                matches ? "" : "Passwords do not match."
            );
        }

        password.addEventListener("input", checkPasswords);
        confirmPassword.addEventListener("input", checkPasswords);
    </script>
</body>
</html>
