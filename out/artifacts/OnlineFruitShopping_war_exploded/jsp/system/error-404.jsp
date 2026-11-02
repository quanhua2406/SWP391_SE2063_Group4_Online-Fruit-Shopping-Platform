<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Page Not Found - FreshFruit</title>

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

    <h1>🔎 Page not found</h1>

    <p class="dashboard-welcome">
        The page you are looking for does not exist or has been
        moved.
    </p>

    <p class="action-row">
        <a class="btn-primary" href="<%= contextPath %>/home">
            🏠 Back to Home
        </a>
    </p>

</section>

</body>
</html>
