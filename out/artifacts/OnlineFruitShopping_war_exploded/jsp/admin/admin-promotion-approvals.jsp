<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.math.RoundingMode"%>
<%@page import="java.util.Locale"%>
<%@page import="java.time.LocalDate"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>

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

    private BigDecimal dec(Object value) {
        if (value == null) {
            return null;
        }

        return value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());
    }

    private String qty(Object value) {
        BigDecimal number = dec(value);

        return number == null
                ? "0"
                : number.stripTrailingZeros().toPlainString();
    }

    private String money(Object value) {
        BigDecimal number = dec(value);

        return number == null
                ? "0 \u0111"
                : String.format(Locale.US, "%,.0f", number) + " \u0111";
    }

    private String discount(Object price, Object promoPrice) {
        BigDecimal full = dec(price);
        BigDecimal promo = dec(promoPrice);

        if (full == null || promo == null || full.signum() <= 0) {
            return "";
        }

        BigDecimal off = full.subtract(promo)
                .multiply(BigDecimal.valueOf(100))
                .divide(full, 0, RoundingMode.HALF_UP);

        return "-" + off.toPlainString() + "%";
    }

    private String dateValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate().toString();
        }

        return value.toString();
    }

    private String periodStatus(Object start, Object end) {
        if (!(start instanceof java.sql.Date) || !(end instanceof java.sql.Date)) {
            return "";
        }

        LocalDate from = ((java.sql.Date) start).toLocalDate();
        LocalDate to = ((java.sql.Date) end).toLocalDate();
        LocalDate today = LocalDate.now();

        if (today.isBefore(from)) {
            return "Scheduled";
        }

        if (today.isAfter(to)) {
            return "Ended";
        }

        return "Running";
    }

    private String periodPill(String status) {
        if ("Running".equals(status)) {
            return "pill-ok";
        }

        if ("Scheduled".equals(status)) {
            return "pill-warn";
        }

        return "pill-muted";
    }

    private String statusLabel(Object status) {
        String value = status == null ? "" : status.toString();

        if ("approved".equals(value)) {
            return "Approved";
        }

        if ("rejected".equals(value)) {
            return "Rejected";
        }

        return "Pending";
    }

    private String statusPill(String label) {
        if ("Approved".equals(label)) {
            return "pill-ok";
        }

        if ("Pending".equals(label)) {
            return "pill-warn";
        }

        return "pill-danger";
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> promotionsPage =
            (Page<Map<String, Object>>) request.getAttribute("promotionsPage");

    String pagerUrl = QueryUrl.keep(request,
            contextPath + "/admin/promotion-approvals", "status", "q");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String status = request.getParameter("status") == null
            ? ""
            : request.getParameter("status");

    String currentPage = request.getParameter("page") == null
            ? "1"
            : request.getParameter("page");

    Object pendingCount = request.getAttribute("pendingCount");
    Object approvedCount = request.getAttribute("approvedCount");
    Object rejectedCount = request.getAttribute("rejectedCount");
    Object totalCount = request.getAttribute("totalCount");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    if ("approved".equals(message)) {
        messageText = "Promotion approved. The promotional price is "
                + "effective inside its date range.";
    } else if ("rejected".equals(message)) {
        messageText = "Promotion refused. The shop owner sees the reason.";
    } else if ("approve-expired".equals(message)) {
        level = "error-message";
        messageText = "This promotion already ended, so it cannot be "
                + "approved. The shop owner has to submit a new one.";
    } else if ("approve-not-discount".equals(message)) {
        level = "error-message";
        messageText = "The promotional price is not below the normal "
                + "price any more, so it cannot be approved.";
    } else if ("not-pending".equals(message)) {
        level = "error-message";
        messageText = "That promotion has already been reviewed.";
    } else if ("invalid".equals(message)) {
        level = "error-message";
        messageText = "The promotion could not be found.";
    } else if ("failed".equals(message)) {
        level = "error-message";
        messageText = "The decision could not be saved.";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Promotion Approvals - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/admin/dashboard">
            📊 Dashboard
        </a>

        <a href="<%= contextPath %>/admin/product-management">
            🍎 Products
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Logout
        </a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">
            ← Admin Dashboard
        </a>
    </p>

    <h1>Promotion Approvals</h1>

    <p class="dashboard-welcome">
        Review the promotional prices requested by shop owners.
        A promotion only applies once it is approved here.
    </p>

    <% if (messageText != null) { %>

        <div class="<%= level %>">
            <%= esc(messageText) %>
        </div>

    <% } %>

    <div class="summary-grid summary-grid-4">

        <div class="summary-card">
            <span class="summary-label">Total Requests</span>
            <span class="summary-value"><%= esc(qty(totalCount)) %></span>
        </div>

        <div class="summary-card summary-warn">
            <span class="summary-label">Waiting For Review</span>
            <span class="summary-value"><%= esc(qty(pendingCount)) %></span>
        </div>

        <div class="summary-card summary-accent">
            <span class="summary-label">Approved</span>
            <span class="summary-value"><%= esc(qty(approvedCount)) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Rejected</span>
            <span class="summary-value"><%= esc(qty(rejectedCount)) %></span>
        </div>

    </div>

    <form method="get"
          action="<%= contextPath %>/admin/promotion-approvals"
          class="filter-form">

        <input type="text" class="filter-input" name="q"
               value="<%= esc(q) %>"
               placeholder="Search product, SKU or shop...">

        <select class="filter-select" name="status">
            <option value="">All states</option>
            <option value="pending" <%= "pending".equals(status) ? "selected" : "" %>>Pending</option>
            <option value="approved" <%= "approved".equals(status) ? "selected" : "" %>>Approved</option>
            <option value="rejected" <%= "rejected".equals(status) ? "selected" : "" %>>Rejected</option>
        </select>

        <button type="submit" class="btn-sm">Filter</button>

        <a class="filter-clear"
           href="<%= contextPath %>/admin/promotion-approvals">Clear</a>

    </form>

    <div class="panel">

        <h2>🏷️ Promotion Requests</h2>

        <p class="panel-hint">
            Requests waiting for review are listed first.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Shop</th>
                        <th>Product</th>
                        <th>SKU</th>
                        <th>Normal Price</th>
                        <th>Promo Price</th>
                        <th>Period</th>
                        <th>Status</th>
                        <th>Decision</th>
                    </tr>
                </thead>

                <tbody>
                <% if (promotionsPage == null || promotionsPage.isEmpty()) { %>

                    <tr>
                        <td colspan="8">
                            No promotion request matches your search.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> p : promotionsPage.getItems()) {

                           String label = statusLabel(p.get("status"));
                           String period = periodStatus(
                                   p.get("start_date"), p.get("end_date"));
                           boolean pending = "Pending".equals(label);
                %>
                    <tr>
                        <td><%= esc(p.get("shop_name")) %></td>
                        <td><%= esc(p.get("product_name")) %></td>
                        <td><%= esc(p.get("sku")) %></td>
                        <td><%= money(p.get("price")) %></td>
                        <td>
                            <strong><%= money(p.get("promo_price")) %></strong>
                            <span class="pill pill-ok">
                                <%= esc(discount(p.get("price"), p.get("promo_price"))) %>
                            </span>
                        </td>
                        <td>
                            <%= esc(dateValue(p.get("start_date"))) %>
                            →
                            <%= esc(dateValue(p.get("end_date"))) %>
                            <br>
                            <span class="pill <%= periodPill(period) %>">
                                <%= period %>
                            </span>
                        </td>
                        <td>
                            <span class="pill <%= statusPill(label) %>">
                                <%= label %>
                            </span>

                            <% if ("Rejected".equals(label)
                                    && p.get("review_note") != null) { %>
                                <span class="row-note">
                                    <%= esc(p.get("review_note")) %>
                                </span>
                            <% } %>
                        </td>
                        <td>
                            <% if (pending) { %>

                                <div class="table-actions">

                                    <form method="post"
                                          action="<%= contextPath %>/admin/promotion-approvals">

                                        <input type="hidden" name="action"
                                               value="approve">

                                        <input type="hidden" name="promoId"
                                               value="<%= esc(p.get("promo_id")) %>">

                                        <input type="hidden" name="status"
                                               value="<%= esc(status) %>">

                                        <input type="hidden" name="q"
                                               value="<%= esc(q) %>">

                                        <input type="hidden" name="page"
                                               value="<%= esc(currentPage) %>">

                                        <button type="submit" class="btn-sm">
                                            Approve
                                        </button>

                                    </form>

                                    <form method="post"
                                          action="<%= contextPath %>/admin/promotion-approvals">

                                        <input type="hidden" name="action"
                                               value="reject">

                                        <input type="hidden" name="promoId"
                                               value="<%= esc(p.get("promo_id")) %>">

                                        <input type="hidden" name="status"
                                               value="<%= esc(status) %>">

                                        <input type="hidden" name="q"
                                               value="<%= esc(q) %>">

                                        <input type="hidden" name="page"
                                               value="<%= esc(currentPage) %>">

                                        <input class="qty-input note-input" type="text"
                                               name="reviewNote" maxlength="255"
                                               placeholder="Reason (optional)">

                                        <button type="submit"
                                                class="btn-sm btn-sm-danger">
                                            Reject
                                        </button>

                                    </form>

                                </div>

                            <% } else { %>

                                <span class="muted-note">
                                    Reviewed
                                </span>

                            <% } %>
                        </td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

            </table>

        </div>

        <%= Pager.render(promotionsPage, pagerUrl, "page") %>

    </div>

</section>

</body>
</html>
