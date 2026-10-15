
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.ShopOwnerRegistrationDAO.CustomerInfo"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.ShopOwnerRegistrationDAO.RequestInfo"%>

<%
    CustomerInfo customer =
        (CustomerInfo) request.getAttribute("customer");

    RequestInfo latest =
        (RequestInfo) request.getAttribute("latestRequest");

    String error = (String) request.getAttribute("error");
    String contextPath = request.getContextPath();

    boolean hasOpenRequest = latest != null
        && ("PENDING_PAYMENT".equals(latest.getStatus())
        || "PENDING_APPROVAL".equals(latest.getStatus()));
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register as Shop Owner - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">

    <style>
        .shop-register-page {
            min-height: 100vh;
            padding: 40px 16px;
            background: #f4f8f4;
        }

        .shop-register-card {
            max-width: 760px;
            margin: auto;
            padding: 32px;
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 6px 24px rgba(0, 0, 0, .08);
        }

        .shop-register-card h1 {
            color: #198754;
            margin-bottom: 8px;
        }

        .shop-register-card .subtitle {
            color: #667085;
            margin-bottom: 24px;
        }

        .shop-form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
        }

        .shop-form-group {
            display: flex;
            flex-direction: column;
            gap: 7px;
        }

        .shop-form-group.full-width {
            grid-column: 1 / -1;
        }

        .shop-form-group label {
            font-weight: 600;
            color: #344054;
        }

        .shop-form-group input,
        .shop-form-group textarea,
        .shop-form-group select {
            width: 100%;
            padding: 11px 12px;
            border: 1px solid #d0d5dd;
            border-radius: 8px;
            font: inherit;
            box-sizing: border-box;
        }

        .shop-form-group textarea {
            min-height: 110px;
            resize: vertical;
        }

        .shop-form-group input:focus,
        .shop-form-group textarea:focus,
        .shop-form-group select:focus {
            outline: 2px solid #a6e3bd;
            border-color: #198754;
        }

        .shop-register-button {
            width: 100%;
            padding: 13px;
            margin-top: 22px;
            border: 0;
            border-radius: 8px;
            background: #198754;
            color: white;
            font-size: 16px;
            font-weight: 700;
            cursor: pointer;
        }

        .shop-register-button:hover {
            background: #146c43;
        }

        .shop-alert {
            padding: 12px 14px;
            margin-bottom: 18px;
            border-radius: 8px;
        }

        .shop-alert.error {
            background: #fff0f0;
            color: #b42318;
        }

        .shop-alert.info {
            background: #eef8f0;
            color: #166534;
        }

        .shop-back-link {
            display: inline-block;
            margin-top: 18px;
            color: #198754;
            text-decoration: none;
        }

        @media (max-width: 600px) {
            .shop-register-card {
                padding: 22px 16px;
            }

            .shop-form-grid {
                grid-template-columns: 1fr;
            }

            .shop-form-group.full-width {
                grid-column: auto;
            }
        }
    </style>
</head>

<body>
<div class="shop-register-page">
    <main class="shop-register-card">
        <h1>Register as Shop Owner</h1>

        <p class="subtitle">
            Submit your shop information for review by our admin team.
        </p>

        <% if (error != null) { %>
            <div class="shop-alert error">
                <%= error %>
            </div>
        <% } %>

        <% if (customer != null) { %>
            <div class="shop-alert info">
                <strong>Account:</strong>
                <%= customer.getEmail() %>
            </div>
        <% } %>

        <% if (hasOpenRequest) { %>
            <div class="shop-alert info">
                You already have an active registration request:
                <strong><%= latest.getShopName() %></strong>.
                Current status:
                <strong><%= latest.getStatus() %></strong>.
                <p>
                    <a href="<%= contextPath %>/shop-owner-status">
                        View request status
                    </a>
                </p>
            </div>
        <% } else { %>

            <form action="<%= contextPath %>/register-shop-owner"
                  method="post">

                <div class="shop-form-grid">
                    <div class="shop-form-group">
                        <label for="shopName">Shop Name *</label>
                        <input type="text"
                               id="shopName"
                               name="shopName"
                               maxlength="150"
                               required>
                    </div>

                    <div class="shop-form-group">
                        <label for="businessType">Business Type *</label>
                        <select id="businessType"
                                name="businessType"
                                required>
                            <option value="">Select business type</option>
                            <option value="Fruit">Fruit</option>
                            <option value="Vegetables">Vegetables</option>
                            <option value="Organic Food">Organic Food</option>
                            <option value="Other">Other</option>
                        </select>
                    </div>

                    <div class="shop-form-group">
                        <label for="shopPhone">Shop Phone *</label>
                        <input type="tel"
                               id="shopPhone"
                               name="shopPhone"
                               maxlength="20"
                               required>
                    </div>

                    <div class="shop-form-group">
                        <label for="shopEmail">Shop Email</label>
                        <input type="email"
                               id="shopEmail"
                               name="shopEmail"
                               maxlength="254">
                    </div>

                    <div class="shop-form-group full-width">
                        <label for="shopAddress">Shop Address *</label>
                        <input type="text"
                               id="shopAddress"
                               name="shopAddress"
                               maxlength="300"
                               required>
                    </div>

                    <div class="shop-form-group">
                        <label for="city">City / Province *</label>
                        <input type="text"
                               id="city"
                               name="city"
                               maxlength="100"
                               required>
                    </div>

                    <div class="shop-form-group full-width">
                        <label for="description">Shop Description *</label>
                        <textarea id="description"
                                  name="description"
                                  maxlength="1000"
                                  required></textarea>
                    </div>
                </div>

                <button type="submit" class="shop-register-button">
                    Submit Registration
                </button>
            </form>

        <% } %>

        <a class="shop-back-link"
           href="<%= contextPath %>/">
            Back to Home
        </a>
    </main>
</div>
</body>
</html>
