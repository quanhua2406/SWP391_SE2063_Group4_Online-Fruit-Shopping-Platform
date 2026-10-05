<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Register - FreshFruit</title>

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f5f7fa;
            min-height: 100vh;

            display: flex;
            align-items: center;
            justify-content: center;

            padding: 30px 0;
        }


        /* =====================================================
           CONTAINER
           ===================================================== */

        .register-container {
            width: 90%;
            max-width: 500px;
        }


        /* =====================================================
           CARD
           ===================================================== */

        .register-card {
            background: white;

            padding: 35px;

            border-radius: 16px;

            box-shadow:
                0 5px 20px rgba(0, 0, 0, 0.08);
        }


        /* =====================================================
           HEADER
           ===================================================== */

        .register-header {
            text-align: center;

            margin-bottom: 25px;
        }

        .register-header h1 {
            margin: 0 0 8px;

            color: #2e7d32;

            font-size: 30px;
        }

        .register-header p {
            margin: 0;

            color: #777;

            font-size: 14px;
        }


        /* =====================================================
           ERROR MESSAGE
           ===================================================== */

        .error-message {
            background: #ffebee;

            color: #c62828;

            border: 1px solid #ffcdd2;

            padding: 12px 15px;

            border-radius: 8px;

            margin-bottom: 20px;

            font-size: 14px;
        }


        /* =====================================================
           FORM
           ===================================================== */

        .form-group {
            margin-bottom: 17px;
        }

        .form-group label {
            display: block;

            margin-bottom: 7px;

            font-weight: 600;

            color: #333;

            font-size: 14px;
        }

        .form-group input {
            width: 100%;

            padding: 12px 13px;

            border: 1px solid #ddd;

            border-radius: 8px;

            font-size: 15px;

            outline: none;

            transition: 0.2s;
        }

        .form-group input:focus {
            border-color: #2e7d32;

            box-shadow:
                0 0 0 2px rgba(46, 125, 50, 0.1);
        }


        /* =====================================================
           PASSWORD NOTE
           ===================================================== */

        .password-note {
            margin-top: 6px;

            color: #888;

            font-size: 12px;
        }


        /* =====================================================
           BUTTON
           ===================================================== */

        .register-button {
            width: 100%;

            padding: 13px;

            margin-top: 8px;

            border: none;

            border-radius: 8px;

            background: #2e7d32;

            color: white;

            font-size: 16px;

            font-weight: 600;

            cursor: pointer;

            transition: 0.2s;
        }

        .register-button:hover {
            opacity: 0.85;
        }


        /* =====================================================
           LINKS
           ===================================================== */

        .login-link {
            text-align: center;

            margin-top: 20px;

            color: #777;

            font-size: 14px;
        }

        .login-link a {
            color: #2e7d32;

            text-decoration: none;

            font-weight: 600;
        }

        .login-link a:hover {
            text-decoration: underline;
        }


        /* =====================================================
           MOBILE
           ===================================================== */

        @media (max-width: 500px) {

            .register-container {
                width: 95%;
            }

            .register-card {
                padding: 25px;
            }
        }

    </style>

</head>


<body>


<div class="register-container">

    <div class="register-card">


        <!-- =================================================
             HEADER
             ================================================= -->

        <div class="register-header">

            <h1>
                FreshFruit
            </h1>

            <p>
                Create your account
            </p>

        </div>


        <!-- =================================================
             SERVER ERROR
             ================================================= -->

        <% if (request.getAttribute("error") != null) { %>

            <div class="error-message">

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
            action="${pageContext.request.contextPath}/register"
            onsubmit="return validateRegisterForm();">


            <!-- =================================================
                 FULL NAME
                 ================================================= -->

            <div class="form-group">

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

            <div class="form-group">

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

            <div class="form-group">

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

            <div class="form-group">

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

            <div class="form-group">

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

            <div class="form-group">

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
                class="register-button">

                Create Account

            </button>


        </form>


        <!-- =================================================
             LOGIN LINK
             ================================================= -->

        <div class="login-link">

            Already have an account?

            <a
                href="${pageContext.request.contextPath}/login">

                Login

            </a>

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

    const error =
        document.getElementById("clientError");


    // ---------------------------------------------------------
    // CLEAR ERROR
    // ---------------------------------------------------------

    error.style.display = "none";

    error.innerHTML = "";


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