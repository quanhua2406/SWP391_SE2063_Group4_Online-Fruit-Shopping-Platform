
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Payment Result - FreshFruit</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f7f4;
            margin: 0;
            padding: 40px 16px;
            color: #222;
        }
        .container {
            max-width: 650px;
            margin: auto;
            background: white;
            padding: 32px;
            border-radius: 16px;
            box-shadow: 0 8px 30px rgba(0,0,0,.08);
        }
        h1 { color: #07864b; }
        .status {
            padding: 12px 16px;
            border-radius: 10px;
            background: #fff2cc;
            margin: 20px 0;
        }
        a {
            display: inline-block;
            margin-top: 16px;
            color: #07864b;
            text-decoration: none;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>VNPay Payment Result</h1>

    <div class="status">
        <strong>
            <%= Boolean.TRUE.equals(request.getAttribute("paymentSuccess"))
                    ? "Payment successful"
                    : "Payment unsuccessful" %>
        </strong>
    </div>

    <p>
        <%= request.getAttribute("paymentMessage") == null
                ? "Please check your payment status."
                : request.getAttribute("paymentMessage") %>
    </p>

    <a href="${pageContext.request.contextPath}/shop-owner-status">
        Back to Shop Owner Request Status
    </a>
</div>
</body>
</html>
