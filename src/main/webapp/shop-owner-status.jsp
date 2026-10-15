
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.ShopOwnerRegistrationDAO.CustomerInfo"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.ShopOwnerRegistrationDAO.RequestInfo"%>

<%
    CustomerInfo customer =
        (CustomerInfo) request.getAttribute("customer");

    RequestInfo latest =
        (RequestInfo) request.getAttribute("latestRequest");

    String contextPath = request.getContextPath();
    String submitted = request.getParameter("submitted");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Shop Owner Request Status - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">

    <style>
        .status-page {
            min-height: 100vh;
            padding: 40px 16px;
            background: #f4f8f4;
        }

        .status-card {
            max-width: 620px;
            margin: auto;
            padding: 32px;
            background: white;
            border-radius: 14px;
            box-shadow: 0 6px 24px rgba(0, 0, 0, .08);
        }

        .status-card h1 {
            color: #198754;
        }

        .status-label {
            display: inline-block;
            padding: 7px 12px;
            border-radius: 20px;
            background: #fff3cd;
            color: #664d03;
            font-weight: 700;
        }

        .status-link {
            display: inline-block;
            margin-top: 20px;
            color: #198754;
        }
    </style>
</head>

<body>
<main class="status-page">
    <section class="status-card">
        <h1>Shop Owner Request Status</h1>

        <% if ("1".equals(submitted)) { %>
            <p>Your registration request has been saved.</p>
        <% } %>

        <% if (latest == null) { %>
            <p>You have not submitted a shop registration request.</p>
        <% } else { %>

            <p>
                <strong>Shop Name:</strong>
                <%= latest.getShopName() %>
            </p>

            <p>
                <strong>Request ID:</strong>
                <%= latest.getRequestId() %>
            </p>

            <p>
                <strong>Status:</strong>
                <span class="status-label">
                    <%= latest.getStatus() %>
                </span>
            </p>

            <% if ("PENDING_PAYMENT".equals(latest.getStatus())) { %>
                <p>
                    Your request is waiting for subscription payment.
                    Payment processing will be connected in the next step.
                </p>
            <% } else if ("PENDING_APPROVAL".equals(latest.getStatus())) { %>
                <p>
                    Payment has been recorded. Your request is waiting
                    for admin approval.
                </p>
            <% } else if ("APPROVED".equals(latest.getStatus())) { %>
                <p>Your shop registration has been approved.</p>
            <% } else if ("REJECTED".equals(latest.getStatus())) { %>
                <p>Your request was rejected.</p>

                <% if (latest.getAdminNote() != null
                        && !latest.getAdminNote().isBlank()) { %>
                    <p>
                        <strong>Admin Note:</strong>
                        <%= latest.getAdminNote() %>
                    </p>
                <% } %>
            <% } %>

        <% } %>

        <a class="status-link"
           href="<%= contextPath %>/register-shop-owner">
            Back to Registration
        </a>
    </section>
</main>
</body>
</html>
