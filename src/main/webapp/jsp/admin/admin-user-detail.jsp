
<%@page import="com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO.UserInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    UserInfo user = (UserInfo) request.getAttribute("user");

    if (user == null) {
        response.sendError(
            HttpServletResponse.SC_NOT_FOUND,
            "User data was not provided by the controller."
        );
        return;
    }

    String cp = request.getContextPath();
    String role = user.getRoleName() == null ? "" : user.getRoleName();
    String status = user.getStatus() == null ? "" : user.getStatus();
    String message = request.getParameter("message");

    boolean isLocked = "locked".equalsIgnoreCase(status);
    boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Detail - FreshFruit</title>

    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            padding: 30px 16px;
            font-family: Arial, sans-serif;
            color: #263238;
            background: #f4f6f9;
        }

        .container {
            max-width: 950px;
            margin: 10px auto;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 14px;
            margin-bottom: 24px;
        }

        h1 {
            margin: 0 0 7px;
            font-size: 28px;
        }

        .muted {
            margin: 0;
            color: #6b7280;
            font-size: 14px;
        }

        .card {
            padding: 28px;
            background: #fff;
            border: 1px solid #e1e5ea;
            border-radius: 12px;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
        }

        .field label {
            display: block;
            margin-bottom: 8px;
            color: #6b7280;
            font-size: 13px;
            font-weight: 600;
        }

        .value {
            min-height: 48px;
            padding: 14px;
            overflow-wrap: anywhere;
            background: #f9fafb;
            border: 1px solid #e5e7eb;
            border-radius: 7px;
            font-size: 15px;
        }

        .badge {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: bold;
        }

        .active {
            color: #166534;
            background: #dcfce7;
        }

        .locked {
            color: #991b1b;
            background: #fee2e2;
        }

        .other {
            color: #374151;
            background: #f3f4f6;
        }

        .notice {
            padding: 14px 16px;
            margin-bottom: 20px;
            border-radius: 7px;
            font-size: 14px;
        }

        .success {
            color: #18733b;
            background: #eaf7ed;
            border: 1px solid #a6e3b5;
        }

        .error {
            color: #b42318;
            background: #fef3f2;
            border: 1px solid #fecdca;
        }

        .actions {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 12px;
            margin-top: 28px;
            padding-top: 24px;
            border-top: 1px solid #e5e7eb;
        }

        .actions form { margin: 0; }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 42px;
            padding: 11px 17px;
            border: 0;
            border-radius: 7px;
            font-size: 14px;
            text-decoration: none;
            cursor: pointer;
        }

        .primary {
            color: #fff;
            background: #1976d2;
        }

        .primary:hover { background: #125eaa; }

        .secondary {
            color: #374151;
            background: #e5e7eb;
        }

        .secondary:hover { background: #d1d5db; }

        .danger {
            color: #fff;
            background: #c62828;
        }

        .danger:hover { background: #a61f1f; }

        .success-btn {
            color: #fff;
            background: #2e7d32;
        }

        .success-btn:hover { background: #256628; }

        @media (max-width: 650px) {
            .grid { grid-template-columns: 1fr; }
            .card { padding: 20px; }
            h1 { font-size: 24px; }
        }
    </style>
</head>

<body>
<main class="container">
    <div class="header">
        <div>
            <h1>User Detail</h1>
            <p class="muted">View and manage this user account.</p>
        </div>

        <a class="btn secondary"
           href="<%=cp%>/admin/user-management">
            Back to User List
        </a>
    </div>

    <% if ("updated".equals(message)) { %>
        <div class="notice success" role="status">
            User updated successfully!
        </div>
    <% } else if ("status-updated".equals(message)) { %>
        <div class="notice success" role="status">
            User status updated successfully!
        </div>
    <% } else if ("status-failed".equals(message)) { %>
        <div class="notice error" role="alert">
            Failed to update user status.
        </div>
    <% } else if ("admin-protected".equals(message)) { %>
        <div class="notice error" role="alert">
            Admin account status cannot be changed.
        </div>
    <% } %>

    <section class="card">
        <div class="grid">
            <div class="field">
                <label>User ID</label>
                <div class="value"><%=user.getUserId()%></div>
            </div>

            <div class="field">
                <label>Role</label>
                <div class="value"><%=escapeHtml(role)%></div>
            </div>

            <div class="field">
                <label>Full Name</label>
                <div class="value"><%=escapeHtml(user.getFullName())%></div>
            </div>

            <div class="field">
                <label>Email</label>
                <div class="value"><%=escapeHtml(user.getEmail())%></div>
            </div>

            <div class="field">
                <label>Phone</label>
                <div class="value"><%=escapeHtml(user.getPhone())%></div>
            </div>

            <div class="field">
                <label>Status</label>
                <div class="value">
                    <span class="badge <%=isLocked ? "locked" :
                        ("active".equalsIgnoreCase(status) ? "active" : "other")%>">
                        <%=escapeHtml(status)%>
                    </span>
                </div>
            </div>
        </div>

        <div class="actions">
            <a class="btn primary"
               href="<%=cp%>/admin/user-management?action=edit&amp;userId=<%=user.getUserId()%>">
                Edit User
            </a>

            <% if (!isAdmin && !isLocked) { %>
                <form method="post"
                      action="<%=cp%>/admin/user-management"
                      onsubmit="return confirm('Are you sure you want to block this user?');">
                    <input type="hidden" name="action" value="block">
                    <input type="hidden" name="userId"
                           value="<%=user.getUserId()%>">
                    <button type="submit" class="btn danger">
                        Block User
                    </button>
                </form>
            <% } else if (!isAdmin && isLocked) { %>
                <form method="post"
                      action="<%=cp%>/admin/user-management"
                      onsubmit="return confirm('Are you sure you want to unblock this user?');">
                    <input type="hidden" name="action" value="unblock">
                    <input type="hidden" name="userId"
                           value="<%=user.getUserId()%>">
                    <button type="submit" class="btn success-btn">
                        Unblock User
                    </button>
                </form>
            <% } %>

            <a class="btn secondary"
               href="<%=cp%>/admin/user-management">
                Back
            </a>
        </div>
    </section>
</main>
</body>
</html>
