
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.ShopOwnerRequestDAO.AdminRequestInfo"%>

<%!
    // Escape HTML to prevent user-provided text from being interpreted as HTML.
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
    List<AdminRequestInfo> requests =
            (List<AdminRequestInfo>) request.getAttribute("requests");

    String message = (String) session.getAttribute("adminReviewMessage");

    if (message != null) {
        session.removeAttribute("adminReviewMessage");
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Shop Owner Requests - Admin</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 30px;
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            color: #222;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
        }

        h1 {
            margin-bottom: 8px;
        }

        .subtitle {
            color: #666;
            margin-bottom: 25px;
        }

        .message {
            padding: 14px;
            margin-bottom: 20px;
            background: #e8f4ff;
            border: 1px solid #b8dcff;
            border-radius: 6px;
        }

        .table-wrapper {
            overflow-x: auto;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 1100px;
        }

        th, td {
            padding: 14px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid #e5e5e5;
        }

        th {
            background: #202b3c;
            color: white;
            white-space: nowrap;
        }

        tr:hover td {
            background: #f9fbfd;
        }

        .status {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 5px;
            font-size: 12px;
            font-weight: bold;
        }

        .pending {
            background: #fff0c2;
            color: #805b00;
        }

        .approved {
            background: #d8f5df;
            color: #17652a;
        }

        .rejected {
            background: #ffe0e0;
            color: #a11b1b;
        }

        .review-form {
            min-width: 200px;
        }

        .review-form textarea {
            width: 100%;
            min-width: 180px;
            min-height: 65px;
            padding: 8px;
            margin-bottom: 8px;
            resize: vertical;
        }

        .button-group {
            display: flex;
            gap: 8px;
        }

        button {
            padding: 8px 12px;
            border: none;
            border-radius: 5px;
            color: white;
            cursor: pointer;
            font-weight: bold;
        }

        .approve {
            background: #198754;
        }

        .reject {
            background: #dc3545;
        }

        button:hover {
            opacity: 0.85;
        }

        .reviewed {
            color: #777;
            font-style: italic;
        }

        .empty {
            padding: 30px;
            text-align: center;
            color: #777;
        }

        @media (max-width: 768px) {
            body {
                padding: 15px;
            }
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Shop Owner Requests</h1>

    <p class="subtitle">
        Review and manage customer applications to become shop owners.
    </p>

    <% if (message != null) { %>
        <div class="message">
            <%= escapeHtml(message) %>
        </div>
    <% } %>

    <div class="table-wrapper">

        <table>
            <thead>
                <tr>
                    <th>Request ID</th>
                    <th>Customer</th>
                    <th>Email</th>
                    <th>Shop Name</th>
                    <th>Description</th>
                    <th>Phone</th>
                    <th>Address</th>
                    <th>Submitted At</th>
                    <th>Status</th>
                    <th>Admin Note</th>
                    <th>Action</th>
                </tr>
            </thead>

            <tbody>

            <% if (requests == null || requests.isEmpty()) { %>

                <tr>
                    <td colspan="11" class="empty">
                        No shop owner requests found.
                    </td>
                </tr>

            <% } else { %>

                <% for (AdminRequestInfo item : requests) { %>

                    <tr>
                        <td>
                            <%= item.getRequestId() %>
                        </td>

                        <td>
                            <%= escapeHtml(item.getFullName()) %>
                        </td>

                        <td>
                            <%= escapeHtml(item.getEmail()) %>
                        </td>

                        <td>
                            <strong>
                                <%= escapeHtml(item.getShopName()) %>
                            </strong>
                        </td>

                        <td>
                            <%= escapeHtml(item.getDescription()) %>
                        </td>

                        <td>
                            <%= escapeHtml(item.getContactPhone()) %>
                        </td>

                        <td>
                            <%= escapeHtml(item.getAddress()) %>
                        </td>

                        <td>
                            <%= escapeHtml(item.getSubmittedAt()) %>
                        </td>

                        <td>
                            <%
                                String status = item.getStatus() == null
                                        ? ""
                                        : item.getStatus().toUpperCase();

                                String statusClass = "pending";

                                if ("APPROVED".equals(status)) {
                                    statusClass = "approved";
                                } else if ("REJECTED".equals(status)) {
                                    statusClass = "rejected";
                                }
                            %>

                            <span class="status <%= statusClass %>">
                                <%= escapeHtml(status) %>
                            </span>
                        </td>

                        <td>
                            <%= escapeHtml(item.getAdminNote()) %>
                        </td>

                        <td>
                            <% if ("PENDING".equals(status)) { %>

                                <form class="review-form"
                                      method="post"
                                      action="<%= request.getContextPath() %>/admin/review-requests"
                                      onsubmit="return confirmReview(event);">

                                    <input type="hidden"
                                           name="requestId"
                                           value="<%= item.getRequestId() %>">

                                    <textarea name="adminNote"
                                              maxlength="1000"
                                              placeholder="Enter an optional review note"></textarea>

                                    <div class="button-group">
                                        <button type="submit"
                                                name="action"
                                                value="APPROVE"
                                                class="approve">
                                            Approve
                                        </button>

                                        <button type="submit"
                                                name="action"
                                                value="REJECT"
                                                class="reject">
                                            Reject
                                        </button>
                                    </div>
                                </form>

                            <% } else { %>

                                <span class="reviewed">
                                    Already reviewed
                                </span>

                            <% } %>
                        </td>
                    </tr>

                <% } %>

            <% } %>

            </tbody>
        </table>

    </div>

</div>

<script>
    function confirmReview(event) {
        const clickedButton = event.submitter;
        const action = clickedButton ? clickedButton.value : "";

        if (action === "APPROVE") {
            return confirm(
                "Are you sure you want to approve this shop owner request?"
            );
        }

        if (action === "REJECT") {
            return confirm(
                "Are you sure you want to reject this shop owner request?"
            );
        }

        return false;
    }
</script>

</body>
</html>
