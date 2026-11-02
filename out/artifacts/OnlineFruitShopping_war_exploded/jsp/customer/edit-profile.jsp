<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="com.mycompany.onlinefruitshopping.model.User"%>

<%
    User user = (User) request.getAttribute("user");

    if (user == null) {
        response.sendRedirect(
                request.getContextPath() + "/login"
        );
        return;
    }

    String error = (String) request.getAttribute("error");
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Edit Profile - FreshFruit</title>

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
        }


        /* =====================================================
           CONTAINER
           ===================================================== */

        .profile-container {
            width: 90%;

            max-width: 550px;
        }


        /* =====================================================
           CARD
           ===================================================== */

        .profile-card {
            background: white;

            padding: 35px;

            border-radius: 16px;

            box-shadow:
                0 5px 20px rgba(0, 0, 0, 0.08);
        }


        /* =====================================================
           HEADER
           ===================================================== */

        .profile-header {
            text-align: center;

            margin-bottom: 30px;
        }

        .profile-header h1 {
            margin: 0 0 8px;

            color: #2e7d32;

            font-size: 28px;
        }

        .profile-header p {
            margin: 0;

            color: #777;

            font-size: 14px;
        }


        /* =====================================================
           ERROR
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
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;

            margin-bottom: 8px;

            font-weight: 600;

            color: #333;
        }

        .form-group input {
            width: 100%;

            padding: 13px 14px;

            border: 1px solid #ddd;

            border-radius: 8px;

            font-size: 15px;

            outline: none;
        }

        .form-group input:focus {
            border-color: #2e7d32;

            box-shadow:
                0 0 0 2px rgba(46, 125, 50, 0.1);
        }


        /* =====================================================
           READ ONLY
           ===================================================== */

        .readonly {
            background: #f1f3f4;

            color: #777;

            cursor: not-allowed;
        }


        /* =====================================================
           BUTTONS
           ===================================================== */

        .button-group {
            display: flex;

            gap: 12px;

            margin-top: 25px;
        }

        .button-group button,
        .button-group a {

            flex: 1;

            padding: 13px;

            border-radius: 8px;

            text-align: center;

            font-size: 15px;

            text-decoration: none;

            cursor: pointer;
        }


        .save-button {

            border: none;

            background: #2e7d32;

            color: white;

            font-weight: 600;
        }

        .save-button:hover {
            opacity: 0.85;
        }


        .cancel-button {

            border: 1px solid #ddd;

            background: white;

            color: #555;
        }

        .cancel-button:hover {
            background: #f5f5f5;
        }


        /* =====================================================
           MOBILE
           ===================================================== */

        @media (max-width: 500px) {

            .profile-container {
                width: 95%;
            }

            .profile-card {
                padding: 25px;
            }

            .button-group {
                flex-direction: column;
            }
        }

    </style>

</head>


<body>


<div class="profile-container">

    <div class="profile-card">


        <!-- =================================================
             HEADER
             ================================================= -->

        <div class="profile-header">

            <h1>
                Edit Profile
            </h1>

            <p>
                Update your personal information
            </p>

        </div>


        <!-- =================================================
             SERVER ERROR
             ================================================= -->

        <% if (error != null && !error.isEmpty()) { %>

            <div class="error-message">

                <%= error %>

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
             FORM
             ================================================= -->

        <form
            method="POST"
            action="<%= request.getContextPath() %>/profile"
            onsubmit="return validateProfileForm();">


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
                    value="<%= user.getFullName() != null
                            ? user.getFullName()
                            : "" %>"
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
                    class="readonly"
                    value="<%= user.getEmail() != null
                            ? user.getEmail()
                            : "" %>"
                    readonly>

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
                    value="<%= user.getPhone() != null
                            ? user.getPhone()
                            : "" %>"
                    placeholder="Enter your phone number"
                    maxlength="11">


            </div>


            <!-- =================================================
                 ROLE
                 ================================================= -->

            <div class="form-group">

                <label for="role">
                    Role
                </label>

                <input
                    type="text"
                    id="role"
                    class="readonly"
                    value="<%= user.getRoleName() != null
                            ? user.getRoleName()
                            : "" %>"
                    readonly>

            </div>


            <!-- =================================================
                 BUTTONS
                 ================================================= -->

            <div class="button-group">

                <a
                    class="cancel-button"
                    href="<%= request.getContextPath() %>/profile">

                    Cancel

                </a>


                <button
                    type="submit"
                    class="save-button">

                    Save Changes

                </button>

            </div>


        </form>


    </div>

</div>


<!-- =========================================================
     JAVASCRIPT VALIDATION
     ========================================================= -->

<script>

function validateProfileForm() {

    const fullName =
        document.getElementById("fullName")
            .value
            .trim();

    const phone =
        document.getElementById("phone")
            .value
            .trim();

    const error =
        document.getElementById("clientError");


    // ---------------------------------------------------------
    // CLEAR OLD ERROR
    // ---------------------------------------------------------

    error.style.display = "none";

    error.innerHTML = "";


    // ---------------------------------------------------------
    // BOTH EMPTY
    // ---------------------------------------------------------

    if (fullName === "" && phone === "") {

        error.innerHTML =
            "Full name and phone number cannot be empty.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // FULL NAME EMPTY
    // ---------------------------------------------------------

    if (fullName === "") {

        error.innerHTML =
            "Full name cannot be empty.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // FULL NAME LENGTH
    // ---------------------------------------------------------

    if (fullName.length < 2) {

        error.innerHTML =
            "Full name must contain at least 2 characters.";

        error.style.display = "block";

        return false;
    }


    if (fullName.length > 100) {

        error.innerHTML =
            "Full name cannot exceed 100 characters.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // FULL NAME MUST CONTAIN LETTER
    // ---------------------------------------------------------

    if (!/\p{L}/u.test(fullName)) {

        error.innerHTML =
            "Full name must contain letters.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // FULL NAME ONLY LETTERS + SPACES
    // ---------------------------------------------------------

    if (!/^[\p{L}\s]+$/u.test(fullName)) {

        error.innerHTML =
            "Full name can contain only letters and spaces.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // PHONE EMPTY
    // ---------------------------------------------------------

    if (phone === "") {

        error.innerHTML =
            "Phone number cannot be empty.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // PHONE ONLY DIGITS
    // ---------------------------------------------------------

    if (!/^[0-9]+$/.test(phone)) {

        error.innerHTML =
            "Phone number can contain only digits.";

        error.style.display = "block";

        return false;
    }


    // ---------------------------------------------------------
    // PHONE LENGTH
    // ---------------------------------------------------------

    if (phone.length < 9 ||
        phone.length > 11) {

        error.innerHTML =
            "Phone number must contain 9 to 11 digits.";

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