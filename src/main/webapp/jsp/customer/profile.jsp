<%@page import="com.mycompany.onlinefruitshopping.model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    User user = (User) request.getAttribute("user");

    if (user == null) {
        response.sendRedirect(
                request.getContextPath() + "/login"
        );
        return;
    }

    boolean isCustomer =
            user.getRoleName() != null
            && user.getRoleName().equalsIgnoreCase("Customer");
%>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>My Profile - FreshFruit</title>

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
            background: #f5f7fa;
        }

        .profile-container {
            width: 550px;
            max-width: 90%;
            margin: 70px auto;
        }

        .profile-card {
            background: white;
            padding: 35px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
        }

        .profile-title {
            text-align: center;
            margin-bottom: 30px;
            color: #198754;
        }

        .profile-row {
            margin-bottom: 18px;
        }

        .profile-label {
            font-weight: bold;
            color: #555;
            margin-bottom: 5px;
        }

        .profile-value {
            padding: 12px;
            background: #f8f9fa;
            border-radius: 8px;
        }

        .button-container {
            display: flex;
            gap: 10px;
            margin-top: 30px;
            flex-wrap: wrap;
        }

        .button {
            flex: 1;
            min-width: 140px;
            text-align: center;
            text-decoration: none;
            padding: 12px 18px;
            border-radius: 8px;
            font-weight: 600;
            color: white;
            background: #198754;
            transition: 0.2s;
        }

        .button:hover {
            opacity: 0.85;
        }

        .edit-button {
            background: #0d6efd;
        }

        .address-button {
            background: #198754;
        }

    </style>

</head>

<body>

<div class="profile-container">

    <div class="profile-card">

        <h2 class="profile-title">
            My Profile
        </h2>

        <div class="profile-row">

            <div class="profile-label">
                Full Name
            </div>

            <div class="profile-value">
                <%= user.getFullName() %>
            </div>

        </div>

        <div class="profile-row">

            <div class="profile-label">
                Email
            </div>

            <div class="profile-value">
                <%= user.getEmail() %>
            </div>

        </div>

        <div class="profile-row">

            <div class="profile-label">
                Phone
            </div>

            <div class="profile-value">
                <%= user.getPhone() != null
                        ? user.getPhone()
                        : "Not provided" %>
            </div>

        </div>

        <div class="profile-row">

            <div class="profile-label">
                Role
            </div>

            <div class="profile-value">
                <%= user.getRoleName() %>
            </div>

        </div>

        <div class="button-container">

            <a class="button"
               href="<%= request.getContextPath() %>/home">
                Home
            </a>

            <a class="button edit-button"
               href="<%= request.getContextPath() %>/profile?action=edit">
                Edit Profile
            </a>

            <% if (isCustomer) { %>

                <a class="button address-button"
                   href="<%= request.getContextPath() %>/addresses">
                    Manage Addresses
                </a>

            <% } %>

        </div>

    </div>

</div>

</body>
</html>