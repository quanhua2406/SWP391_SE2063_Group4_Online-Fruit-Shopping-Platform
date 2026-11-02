<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String contextPath = request.getContextPath();

    Integer statusCode =
            (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    String message =
            (String) request.getAttribute("jakarta.servlet.error.message");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Something Went Wrong - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

</header>

<section class="dashboard" style="text-align:center;">

    <h1>⚠️ Something went wrong</h1>

    <p class="dashboard-welcome">
        Sorry - the server could not complete this request.
        Please try again in a moment.
    </p>

    <% if (statusCode != null) { %>
        <p class="panel-hint">Error code: <%= statusCode %></p>
    <% } %>

    <% if (message != null && !message.trim().isEmpty()
            && message.length() < 200) { %>
        <p class="panel-hint"><%= message.replace("&", "&amp;")
                                          .replace("<", "&lt;")
                                          .replace(">", "&gt;") %></p>
    <% } %>

    <p class="action-row">
        <a class="btn-primary" href="<%= contextPath %>/home">
            🏠 Back to Home
        </a>
    </p>

</section>

</body>
</html>
