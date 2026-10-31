<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    private String esc(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    String contextPath = request.getContextPath();

    String error = request.getAttribute("error") == null
            ? ""
            : request.getAttribute("error").toString();

    String shopName = request.getAttribute("shopName") == null
            ? ""
            : request.getAttribute("shopName").toString();

    String description = request.getAttribute("description") == null
            ? ""
            : request.getAttribute("description").toString();
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Create Shop - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">

</head>

<body>

<div class="auth-page">

    <div class="auth-container">

        <div class="auth-logo">
            🍊 FreshFruit
        </div>

        <h2>
            Create Your Shop
        </h2>

        <p class="auth-description">
            Set up your store to start selling fruits on FreshFruit.
        </p>

        <% if (!error.isEmpty()) { %>

            <div class="error-message">

                ❌
                <%= esc(error) %>

            </div>

        <% } %>

        <form
            method="POST"
            action="<%= contextPath %>/store-owner/create-shop">

            <div class="auth-form-group">

                <label for="shopName">
                    Shop Name
                </label>

                <input
                    type="text"
                    id="shopName"
                    name="shopName"
                    value="<%= esc(shopName) %>"
                    placeholder="e.g. Fresh Fruit Garden"
                    maxlength="150"
                    required>

            </div>

            <div class="auth-form-group">

                <label for="description">
                    Description
                </label>

                <input
                    type="text"
                    id="description"
                    name="description"
                    value="<%= esc(description) %>"
                    placeholder="A short description of your shop"
                    maxlength="255">

                <div class="password-note">
                    Optional. Maximum 255 characters.
                </div>

            </div>

            <button
                type="submit"
                class="auth-button">

                Create Shop

            </button>

        </form>

        <div class="auth-links">

            <p>

                <a href="<%= contextPath %>/store-owner/dashboard">

                    ← Back to Dashboard

                </a>

            </p>

        </div>

    </div>

</div>

</body>

</html>
