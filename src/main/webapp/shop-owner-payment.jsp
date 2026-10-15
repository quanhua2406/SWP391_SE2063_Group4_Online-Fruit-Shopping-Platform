<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    // Escape HTML output to prevent user-provided values from being
    // interpreted as HTML or JavaScript.
    public String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
%>

<%
    Object requestIdValue = request.getAttribute("requestId");

    int requestId = 0;

    if (requestIdValue instanceof Number) {
        requestId = ((Number) requestIdValue).intValue();
    } else if (requestIdValue != null) {
        try {
            requestId = Integer.parseInt(
                requestIdValue.toString()
            );
        } catch (NumberFormatException ex) {
            requestId = 0;
        }
    }

    String shopName = (String) request.getAttribute("shopName");
    String error = (String) request.getAttribute("error");

    boolean validRequestId = requestId > 0;
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Shop Owner Payment - FreshFruit</title>

    <style>
        /* ==================== GLOBAL ==================== */

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 0;
            font-family: Arial, Helvetica, sans-serif;
            background: #f6faf4;
            color: #333;
        }

        a {
            text-decoration: none;
        }

        /* ==================== NAVBAR ==================== */

        .navbar {
            min-height: 70px;
            padding: 0 50px;
            background: #ffffff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid #e5e5e5;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }

        .logo {
            color: #3d8b40;
            font-size: 25px;
            font-weight: bold;
        }

        .nav-link {
            color: #2e6b31;
            font-size: 14px;
            font-weight: 600;
        }

        .nav-link:hover {
            color: #4caf50;
        }

        /* ==================== PAYMENT PAGE ==================== */

        .page-container {
            min-height: calc(100vh - 70px);
            display: flex;
            justify-content: center;
            align-items: flex-start;
            padding: 55px 18px;
        }

        .payment-card {
            width: 100%;
            max-width: 580px;
            padding: 36px;
            background: #ffffff;
            border: 1px solid #e4ece1;
            border-radius: 16px;
            box-shadow: 0 8px 25px rgba(46, 104, 45, 0.08);
        }

        .payment-icon {
            width: 62px;
            height: 62px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
            border-radius: 14px;
            background: #edf8ea;
            font-size: 32px;
        }

        h1 {
            margin: 0 0 10px;
            color: #234d25;
            font-size: 28px;
        }

        .description {
            margin: 0 0 28px;
            color: #777;
            font-size: 14px;
            line-height: 1.7;
        }

        /* ==================== REQUEST INFORMATION ==================== */

        .section-title {
            margin: 0 0 15px;
            color: #2e6b31;
            font-size: 17px;
        }

        .request-info {
            margin-bottom: 25px;
            padding: 18px;
            background: #f8fbf6;
            border: 1px solid #e5eee1;
            border-radius: 10px;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 16px;
            padding: 10px 0;
            border-bottom: 1px solid #e8eee5;
            font-size: 14px;
        }

        .info-row:last-child {
            border-bottom: none;
        }

        .info-label {
            color: #777;
        }

        .info-value {
            color: #333;
            font-weight: 600;
            text-align: right;
            overflow-wrap: anywhere;
        }

        /* ==================== PAYMENT AMOUNT ==================== */

        .amount-box {
            margin-bottom: 22px;
            padding: 22px;
            background: #edf8ea;
            border: 1px solid #d8ebd2;
            border-radius: 12px;
            text-align: center;
        }

        .amount-label {
            margin-bottom: 8px;
            color: #547052;
            font-size: 14px;
        }

        .amount {
            color: #23753a;
            font-size: 32px;
            font-weight: bold;
        }

        .currency {
            margin-left: 5px;
            font-size: 15px;
        }

        /* ==================== PAYMENT NOTE ==================== */

        .notice {
            margin-bottom: 22px;
            padding: 14px;
            background: #f8fbf6;
            border-left: 4px solid #4caf50;
            border-radius: 5px;
            color: #526650;
            font-size: 13px;
            line-height: 1.7;
        }

        .error-message {
            margin-bottom: 20px;
            padding: 13px 15px;
            background: #fff0f0;
            border: 1px solid #f1caca;
            border-radius: 7px;
            color: #a12626;
            font-size: 14px;
            line-height: 1.5;
        }

        /* ==================== BUTTONS ==================== */

        .pay-button {
            width: 100%;
            min-height: 48px;
            padding: 13px 20px;
            border: none;
            border-radius: 8px;
            background: #4caf50;
            color: #ffffff;
            font-size: 15px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.2s, transform 0.2s;
        }

        .pay-button:hover {
            background: #388e3c;
            transform: translateY(-1px);
        }

        .back-link {
            display: block;
            margin-top: 18px;
            color: #4b7048;
            font-size: 14px;
            text-align: center;
        }

        .back-link:hover {
            color: #2e6b31;
            text-decoration: underline;
        }

        .invalid-message {
            padding: 15px;
            background: #fff8e7;
            border: 1px solid #f0dfac;
            border-radius: 8px;
            color: #775d20;
            font-size: 14px;
            line-height: 1.7;
        }

        /* ==================== RESPONSIVE ==================== */

        @media (max-width: 600px) {
            .navbar {
                padding: 18px 20px;
            }

            .page-container {
                padding: 30px 15px;
            }

            .payment-card {
                padding: 24px 20px;
            }

            h1 {
                font-size: 24px;
            }

            .amount {
                font-size: 28px;
            }

            .info-row {
                gap: 10px;
            }
        }
    </style>
</head>

<body>

    <!-- ==================== NAVBAR ==================== -->

    <header class="navbar">

        <div class="logo">
            🍊 FreshFruit
        </div>

        <a class="nav-link"
           href="${pageContext.request.contextPath}/home">
            🏠 Home
        </a>

    </header>

    <!-- ==================== PAYMENT CONTENT ==================== -->

    <main class="page-container">

        <section class="payment-card">

            <div class="payment-icon">
                💳
            </div>

            <h1>Shop Owner Registration</h1>

            <p class="description">
                Complete the registration fee payment to continue
                with your shop owner application.
            </p>

            <% if (error != null && !error.trim().isEmpty()) { %>

                <div class="error-message">
                    <%= escapeHtml(error) %>
                </div>

            <% } %>

            <% if (validRequestId) { %>

                <h2 class="section-title">
                    Request Information
                </h2>

                <div class="request-info">

                    <div class="info-row">

                        <span class="info-label">
                            Request ID
                        </span>

                        <span class="info-value">
                            #<%= requestId %>
                        </span>

                    </div>

                    <div class="info-row">

                        <span class="info-label">
                            Shop Name
                        </span>

                        <span class="info-value">
                            <%= escapeHtml(shopName) %>
                        </span>

                    </div>

                    <div class="info-row">

                        <span class="info-label">
                            Request Type
                        </span>

                        <span class="info-value">
                            Shop Owner Registration
                        </span>

                    </div>

                    <div class="info-row">

                        <span class="info-label">
                            Payment Status
                        </span>

                        <span class="info-value">
                            Awaiting Payment
                        </span>

                    </div>

                </div>

                <div class="amount-box">

                    <div class="amount-label">
                        Registration Fee
                    </div>

                    <div class="amount">
                        200,000
                        <span class="currency">VND</span>
                    </div>

                </div>

                <div class="notice">
                    <strong>Important:</strong>
                    Your application is not yet approved.
                    After payment is verified, your request can proceed
                    to Admin review. Payment does not guarantee approval.
                </div>

                <form method="post"
                      action="${pageContext.request.contextPath}/shop-owner-payment">

                    <input type="hidden"
                           name="requestId"
                           value="<%= requestId %>">

                    <button type="submit" class="pay-button">
                        Pay 200,000 VND with VNPay
                    </button>

                </form>

            <% } else { %>

                <div class="invalid-message">
                    <strong>Request information is missing or invalid.</strong>
                    Please open the payment page from your shop registration
                    process, or return to registration and check your request.
                </div>

                <a class="back-link"
                   href="${pageContext.request.contextPath}/register-shop-owner">
                    ← Back to Shop Registration
                </a>

            <% } %>

            <a class="back-link"
               href="${pageContext.request.contextPath}/shop-owner-status">
                View Shop Owner Request Status
            </a>

        </section>

    </main>

</body>
</html>