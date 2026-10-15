
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Become a Shop Owner - FreshFruit</title>

    <style>
        :root {
            --green: #27864b;
            --green-dark: #176638;
            --green-light: #eaf7ee;
            --text: #24352a;
            --muted: #718075;
            --border: #dce8df;
            --white: #ffffff;
            --red: #c62828;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            background: #f4f8f4;
            color: var(--text);
        }

        .navbar {
            background: var(--white);
            border-bottom: 1px solid var(--border);
            padding: 17px 7%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
        }

        .logo {
            font-size: 25px;
            font-weight: 800;
            color: var(--green);
        }

        .nav-link {
            color: var(--green-dark);
            text-decoration: none;
            font-weight: 600;
        }

        .page {
            max-width: 900px;
            margin: 40px auto;
            padding: 0 20px 40px;
        }

        .intro {
            text-align: center;
            margin-bottom: 28px;
        }

        .intro h1 {
            font-size: 32px;
            margin-bottom: 12px;
        }

        .intro p {
            color: var(--muted);
            line-height: 1.7;
        }

        .card {
            background: var(--white);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 30px;
            box-shadow: 0 8px 28px rgba(32, 85, 48, 0.06);
            margin-bottom: 22px;
        }

        .card h2 {
            margin-top: 0;
            font-size: 21px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .full-width {
            grid-column: 1 / -1;
        }

        label {
            font-size: 14px;
            font-weight: 700;
        }

        input, textarea {
            width: 100%;
            padding: 12px 13px;
            border: 1px solid #cfddd2;
            border-radius: 9px;
            font-size: 14px;
            font-family: inherit;
            background: white;
        }

        input:focus, textarea:focus {
            outline: 2px solid #b7e2c4;
            border-color: var(--green);
        }

        textarea {
            min-height: 125px;
            resize: vertical;
            line-height: 1.6;
        }

        .hint {
            color: var(--muted);
            font-size: 12px;
        }

        .btn {
            border: 0;
            background: var(--green);
            color: white;
            padding: 13px 22px;
            border-radius: 9px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }

        .btn:hover {
            background: var(--green-dark);
        }

        .alert {
            padding: 14px 16px;
            border-radius: 9px;
            margin-bottom: 20px;
            line-height: 1.6;
        }

        .alert-error {
            color: #a61b1b;
            background: #fff0f0;
            border: 1px solid #ffd2d2;
        }

        .alert-success {
            color: #176638;
            background: var(--green-light);
            border: 1px solid #c5e7ce;
        }

        .status {
            display: inline-block;
            padding: 7px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 800;
        }

        .pending {
            background: #fff2cc;
            color: #8a5a00;
        }

        .approved {
            background: #d9f5df;
            color: #176638;
        }

        .rejected {
            background: #ffe0e0;
            color: #a61b1b;
        }

        .info-row {
            padding: 13px 0;
            border-bottom: 1px solid #edf2ed;
            line-height: 1.6;
        }

        .info-row:last-child {
            border-bottom: 0;
        }

        .info-label {
            display: block;
            color: var(--muted);
            font-size: 13px;
            margin-bottom: 5px;
        }

        .footer-note {
            color: var(--muted);
            font-size: 13px;
            line-height: 1.7;
            margin-top: 20px;
        }

        @media (max-width: 600px) {
            .form-grid {
                grid-template-columns: 1fr;
            }

            .full-width {
                grid-column: auto;
            }

            .card {
                padding: 21px;
            }

            .intro h1 {
                font-size: 26px;
            }

            .navbar {
                padding: 16px 20px;
            }
        }
    </style>
</head>

<body>

<header class="navbar">
    <div class="logo">FreshFruit</div>
    <a class="nav-link"
       href="${pageContext.request.contextPath}/home">
        ← Back to Home
    </a>
</header>

<main class="page">

    <section class="intro">
        <h1>Become a Shop Owner</h1>
        <p>
            Join FreshFruit and grow your business with us.
            Submit your shop information and wait for the administrator
            to review your request.
        </p>
    </section>

    <c:if test="${param.submitted == '1'}">
        <div class="alert alert-success">
            <strong>Request submitted successfully!</strong><br>
            Your request is now pending review by the administrator.
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-error">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <c:if test="${not empty latestRequest}">
        <section class="card">
            <h2>Your Latest Request</h2>

            <div class="info-row">
                <span class="info-label">Proposed Shop Name</span>
                <strong><c:out value="${latestRequest.shopName}"/></strong>
            </div>

            <div class="info-row">
                <span class="info-label">Submitted At</span>
                <c:out value="${latestRequest.submittedAt}"/>
            </div>

            <div class="info-row">
                <span class="info-label">Request Status</span>

                <c:choose>
                    <c:when test="${latestRequest.status == 'PENDING'}">
                        <span class="status pending">PENDING</span>
                        <p>Your request is waiting for administrator review.</p>
                    </c:when>

                    <c:when test="${latestRequest.status == 'APPROVED'}">
                        <span class="status approved">APPROVED</span>
                        <p>Your request has been approved.</p>
                    </c:when>

                    <c:when test="${latestRequest.status == 'REJECTED'}">
                        <span class="status rejected">REJECTED</span>
                        <p>
                            Your request was rejected.
                            You may submit a new request with updated information.
                        </p>
                    </c:when>
                </c:choose>
            </div>

            <c:if test="${not empty latestRequest.adminNote}">
                <div class="info-row">
                    <span class="info-label">Administrator's Note</span>
                    <c:out value="${latestRequest.adminNote}"/>
                </div>
            </c:if>
        </section>
    </c:if>

    <c:if test="${empty latestRequest
                  or latestRequest.status == 'REJECTED'}">

        <section class="card">
            <h2>Shop Information</h2>

            <p style="color: #718075; line-height: 1.7;">
                Please provide accurate information about the shop
                you want to register.
            </p>

            <form method="post"
                  action="${pageContext.request.contextPath}/shop-owner-request">

                <div class="form-grid">

                    <div class="form-group full-width">
                        <label for="shopName">Shop Name *</label>
                        <input type="text"
                               id="shopName"
                               name="shopName"
                               maxlength="150"
                               required
                               placeholder="Enter your proposed shop name">
                    </div>

                    <div class="form-group">
                        <label for="contactPhone">Contact Phone *</label>
                        <input type="tel"
                               id="contactPhone"
                               name="contactPhone"
                               maxlength="20"
                               required
                               placeholder="Enter contact phone">
                    </div>

                    <div class="form-group">
                        <label for="address">Shop Address *</label>
                        <input type="text"
                               id="address"
                               name="address"
                               maxlength="300"
                               required
                               placeholder="Enter proposed shop address">
                    </div>

                    <div class="form-group full-width">
                        <label for="description">Business Description *</label>
                        <textarea id="description"
                                  name="description"
                                  maxlength="1000"
                                  required
                                  placeholder="Describe your shop, products and business plan"></textarea>
                        <span class="hint">
                            Maximum 1000 characters.
                        </span>
                    </div>

                    <div class="form-group full-width">
                        <button type="submit" class="btn">
                            Submit Shop Owner Request
                        </button>
                    </div>

                </div>
            </form>

            <p class="footer-note">
                Submitting this form does not automatically grant Shop Owner
                permissions. Your request must be reviewed by an administrator.
            </p>
        </section>

    </c:if>

</main>

</body>
</html>
