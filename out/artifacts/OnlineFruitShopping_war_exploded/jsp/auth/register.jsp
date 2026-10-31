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

    <title>Register - FreshFruit</title>

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
            Create Account
        </h2>

        <p class="auth-description">
            Fill in your details to create a FreshFruit account.
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
                Register as
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
                            Customer
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
                            Shop Owner
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
                            Delivery Staff
                        </span>

                    </span>

                </label>

            </div>


            <!-- =================================================
                 FULL NAME
                 ================================================= -->

            <div class="auth-form-group">

                <label for="fullName">
                    Full Name
                </label>

                <input
                    type="text"
                    id="fullName"
                    name="fullName"
                    value="${fullName}"
                    placeholder="Enter your full name"
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
                    placeholder="Enter your email"
                    autocomplete="email">

            </div>


            <!-- =================================================
                 PASSWORD
                 ================================================= -->

            <div class="auth-form-group">

                <label for="password">
                    Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter your password"
                    autocomplete="new-password">

                <div class="password-note">

                    Minimum 8 characters, including
                    at least one letter and one number.

                </div>

            </div>


            <!-- =================================================
                 CONFIRM PASSWORD
                 ================================================= -->

            <div class="auth-form-group">

                <label for="confirmPassword">
                    Confirm Password
                </label>

                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    placeholder="Confirm your password"
                    autocomplete="new-password">

            </div>


            <!-- =================================================
                 PHONE
                 ================================================= -->

            <div class="auth-form-group">

                <label for="phone">
                    Phone Number
                </label>

                <input
                    type="text"
                    id="phone"
                    name="phone"
                    value="${phone}"
                    placeholder="Enter your phone number"
                    maxlength="11">

            </div>


            <!-- =================================================
                 ADDRESS
                 ================================================= -->

            <div class="auth-form-group">

                <label for="address">
                    Address
                </label>

                <input
                    type="text"
                    id="address"
                    name="address"
                    value="${address}"
                    placeholder="Enter your address"
                    maxlength="255">

            </div>


            <!-- =================================================
                 REGISTER BUTTON
                 ================================================= -->

            <button
                type="submit"
                class="auth-button">

                Create Account

            </button>


        </form>


        <!-- =================================================
             LOGIN LINK
             ================================================= -->

        <div class="auth-links">

            <p>

                Already have an account?

                <a href="<%= contextPath %>/login">

                    Login

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
            "Please choose a role to register as.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // FULL NAME
    // ---------------------------------------------------------

    if (fullName === "") {

        error.innerHTML =
            "Full name cannot be empty.";

        error.style.display = "block";

        return false;
    }


    if (fullName.length < 2) {

        error.innerHTML =
            "Full name must contain at least 2 characters.";

        error.style.display = "block";

        return false;
    }


    if (!/\p{L}/u.test(fullName)) {

        error.innerHTML =
            "Full name must contain letters.";

        error.style.display = "block";

        return false;
    }


    if (!/^[\p{L}\s]+$/u.test(fullName)) {

        error.innerHTML =
            "Full name can contain only letters and spaces.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // EMAIL
    // ---------------------------------------------------------

    if (email === "") {

        error.innerHTML =
            "Email cannot be empty.";

        error.style.display = "block";

        return false;
    }


    const emailPattern =
        /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;

    if (!emailPattern.test(email)) {

        error.innerHTML =
            "Please enter a valid email address.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // PASSWORD
    // ---------------------------------------------------------

    if (password === "") {

        error.innerHTML =
            "Password cannot be empty.";

        error.style.display = "block";

        return false;
    }


    if (password.length < 8) {

        error.innerHTML =
            "Password must contain at least 8 characters.";

        error.style.display = "block";

        return false;
    }


    if (!/[A-Za-z]/.test(password)) {

        error.innerHTML =
            "Password must contain at least one letter.";

        error.style.display = "block";

        return false;
    }


    if (!/[0-9]/.test(password)) {

        error.innerHTML =
            "Password must contain at least one number.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // CONFIRM PASSWORD
    // ---------------------------------------------------------

    if (confirmPassword === "") {

        error.innerHTML =
            "Confirm password cannot be empty.";

        error.style.display = "block";

        return false;
    }


    if (password !== confirmPassword) {

        error.innerHTML =
            "Passwords do not match.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // PHONE
    // ---------------------------------------------------------

    if (phone === "") {

        error.innerHTML =
            "Phone number cannot be empty.";

        error.style.display = "block";

        return false;
    }


    if (!/^[0-9]+$/.test(phone)) {

        error.innerHTML =
            "Phone number can contain only digits.";

        error.style.display = "block";

        return false;
    }


    if (phone.length < 9 ||
        phone.length > 11) {

        error.innerHTML =
            "Phone number must contain 9 to 11 digits.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // ADDRESS
    // ---------------------------------------------------------

    if (address === "") {

        error.innerHTML =
            "Address cannot be empty.";

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
