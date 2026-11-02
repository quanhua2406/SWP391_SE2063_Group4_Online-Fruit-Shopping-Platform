<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.math.RoundingMode"%>
<%@page import="java.util.Locale"%>
<%@page import="java.time.LocalDate"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.VariantWeights"%>

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

    /**
     * Percentage saved by the promotion, e.g. "-10%".
     */
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

    /**
     * Where the promotion stands in time: "Running" when today is
     * inside the range, "Scheduled" when it starts later, "Ended"
     * when it is over and "" when the variant has no promotion.
     */
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

    /**
     * Approval state decided by the administrator.
     */
    private String approvalLabel(Object status) {
        String value = status == null ? "" : status.toString();

        if ("approved".equals(value)) {
            return "Approved";
        }

        if ("rejected".equals(value)) {
            return "Rejected";
        }

        if ("pending".equals(value)) {
            return "Pending";
        }

        return "";
    }

    private String approvalPill(String label) {
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
    Page<Map<String, Object>> variantsPage =
            (Page<Map<String, Object>>) request.getAttribute("variantsPage");

    String pagerUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/promotions", "q", "weight", "approval");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String weight = request.getParameter("weight") == null ? "" : request.getParameter("weight");

    @SuppressWarnings("unchecked")
    java.util.List<String> weightOptions =
            (java.util.List<String>) request.getAttribute("weightOptions");

    String approval = request.getParameter("approval") == null
            ? ""
            : request.getParameter("approval");

    String currentPage = request.getParameter("page") == null
            ? "1"
            : request.getParameter("page");

    LocalDate today = LocalDate.now();
    String defaultStart = today.toString();
    String defaultEnd = today.plusDays(7).toString();

    Object variantCount = request.getAttribute("variantCount");
    Object pendingCount = request.getAttribute("pendingCount");
    Object approvedCount = request.getAttribute("approvedCount");
    Object rejectedCount = request.getAttribute("rejectedCount");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    if ("promo-set".equals(message)) {
        messageText = "Promotion submitted. It becomes effective once an "
                + "administrator approves it.";
    } else if ("promo-removed".equals(message)) {
        messageText = "Promotion removed. The variant is back to its normal price.";
    } else if ("promo-invalid".equals(message)) {
        level = "error-message";
        messageText = "Please enter a valid promotional price and a valid date range.";
    } else if ("promo-expired".equals(message)) {
        level = "error-message";
        messageText = "The promotion end date cannot be in the past.";
    } else if ("promo-not-discount".equals(message)) {
        level = "error-message";
        messageText = "The promotional price must be lower than the normal price.";
    } else if ("invalid".equals(message)) {
        level = "error-message";
        messageText = "Please choose a product variant.";
    } else if ("forbidden".equals(message)) {
        level = "error-message";
        messageText = "You are not allowed to change this promotion.";
    } else if ("failed".equals(message)) {
        level = "error-message";
        messageText = "The promotion could not be saved.";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Promotion Management - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/store-owner/dashboard">
            📊 Dashboard
        </a>

        <a href="<%= contextPath %>/store-owner/inventory">
            📦 Inventory
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Logout
        </a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/store-owner/dashboard">
            ← Shop Owner Dashboard
        </a>
    </p>

    <h1>Promotion Management</h1>

    <p class="dashboard-welcome">
        Request a limited-time promotional price for a product variant.
        Every promotion is reviewed by an administrator before it
        applies, and it only runs inside its date range.
    </p>

    <% if (messageText != null) { %>

        <div class="<%= level %>">
            <%= esc(messageText) %>
        </div>

    <% } %>

    <div class="summary-grid summary-grid-4">

        <div class="summary-card">
            <span class="summary-label">Product Variants</span>
            <span class="summary-value"><%= esc(qty(variantCount)) %></span>
        </div>

        <div class="summary-card summary-warn">
            <span class="summary-label">Pending Approval</span>
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
          action="<%= contextPath %>/store-owner/promotions"
          class="filter-form">

        <input type="text" class="filter-input" name="q"
               value="<%= esc(q) %>"
               placeholder="Search product or SKU...">

        <select class="filter-select" name="weight">
            <option value="">All weights</option>
            <% if (weightOptions != null) {
                   for (String w : weightOptions) { %>
                <option value="<%= esc(w) %>"
                    <%= w.equals(weight) ? "selected" : "" %>>
                    <%= esc(w) %> kg
                </option>
            <%     }
               } %>
        </select>

        <select class="filter-select" name="approval">
            <option value="">All approval states</option>
            <option value="pending" <%= "pending".equals(approval) ? "selected" : "" %>>Pending</option>
            <option value="approved" <%= "approved".equals(approval) ? "selected" : "" %>>Approved</option>
            <option value="rejected" <%= "rejected".equals(approval) ? "selected" : "" %>>Rejected</option>
        </select>

        <button type="submit" class="btn-sm">Filter</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/promotions">Clear</a>

    </form>

    <!-- ==================== SET PROMOTIONAL PRICE ==================== -->

    <div class="panel" id="promo">

        <h2>🏷️ Set Promotional Price</h2>

        <p class="panel-hint">
            Saving a promotion submits it for approval; editing an
            approved promotion sends it back for review. Normal prices
            are managed in
            <a href="<%= contextPath %>/store-owner/pricing">
                Pricing Management
            </a>.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Product</th>
                        <th>SKU</th>
                        <th>Weight</th>
                        <th>Normal Price</th>
                        <th>Promo Price</th>
                        <th>Promo Period</th>
                        <th>Approval</th>
                        <th>Promotion</th>
                    </tr>
                </thead>

                <tbody>
                <% if (variantsPage == null || variantsPage.isEmpty()) { %>

                    <tr>
                        <td colspan="8">
                            No product variants match your search.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> v : variantsPage.getItems()) {

                           Object promoPrice = v.get("promo_price");
                           Object promoStart = v.get("promo_start");
                           Object promoEnd = v.get("promo_end");

                           String period = periodStatus(promoStart, promoEnd);
                           String approvalText = approvalLabel(v.get("promo_status"));
                %>
                    <tr>
                        <td><%= esc(v.get("product_name")) %></td>
                        <td><%= esc(v.get("sku")) %></td>
                        <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                        <td><%= money(v.get("price")) %></td>
                        <td>
                            <% if (promoPrice == null) { %>
                                —
                            <% } else { %>
                                <strong><%= money(promoPrice) %></strong>
                                <span class="pill pill-ok">
                                    <%= esc(discount(v.get("price"), promoPrice)) %>
                                </span>
                            <% } %>
                        </td>
                        <td>
                            <% if (promoPrice == null) { %>
                                —
                            <% } else { %>
                                <%= esc(dateValue(promoStart)) %>
                                →
                                <%= esc(dateValue(promoEnd)) %>
                                <br>
                                <span class="pill <%= periodPill(period) %>">
                                    <%= period %>
                                </span>
                            <% } %>
                        </td>
                        <td>
                            <% if (approvalText.isEmpty()) { %>
                                —
                            <% } else { %>
                                <span class="pill <%= approvalPill(approvalText) %>">
                                    <%= approvalText %>
                                </span>

                                <% if ("Rejected".equals(approvalText)
                                        && v.get("promo_note") != null) { %>
                                    <span class="row-note">
                                        <%= esc(v.get("promo_note")) %>
                                    </span>
                                <% } %>
                            <% } %>
                        </td>
                        <td>
                            <div class="table-actions">

                                <form method="post"
                                      action="<%= contextPath %>/store-owner/promotions">

                                    <input type="hidden" name="action"
                                           value="setPromo">

                                    <input type="hidden" name="variantId"
                                           value="<%= esc(v.get("variant_id")) %>">

                                    <input type="hidden" name="q"
                                           value="<%= esc(q) %>">

                                    <input type="hidden" name="weight"
                                           value="<%= esc(weight) %>">

                                    <input type="hidden" name="approval"
                                           value="<%= esc(approval) %>">

                                    <input type="hidden" name="page"
                                           value="<%= esc(currentPage) %>">

                                    <input class="qty-input" type="number"
                                           name="promoPrice" min="1000" step="1000"
                                           value="<%= promoPrice == null ? "" : qty(promoPrice) %>"
                                           placeholder="Promo đ"
                                           required>

                                    <input class="qty-input date-input" type="date"
                                           name="startDate"
                                           value="<%= promoStart == null ? defaultStart : esc(dateValue(promoStart)) %>"
                                           required>

                                    <input class="qty-input date-input" type="date"
                                           name="endDate"
                                           value="<%= promoEnd == null ? defaultEnd : esc(dateValue(promoEnd)) %>"
                                           required>

                                    <button type="submit" class="btn-sm">
                                        Submit
                                    </button>

                                </form>

                                <% if (promoPrice != null) { %>

                                    <form method="post"
                                          action="<%= contextPath %>/store-owner/promotions">

                                        <input type="hidden" name="action"
                                               value="removePromo">

                                        <input type="hidden" name="variantId"
                                               value="<%= esc(v.get("variant_id")) %>">

                                        <input type="hidden" name="q"
                                               value="<%= esc(q) %>">

                                        <input type="hidden" name="weight"
                                               value="<%= esc(weight) %>">

                                        <input type="hidden" name="approval"
                                               value="<%= esc(approval) %>">

                                        <input type="hidden" name="page"
                                               value="<%= esc(currentPage) %>">

                                        <button type="submit"
                                                class="btn-sm btn-sm-danger">
                                            Remove
                                        </button>

                                    </form>

                                <% } %>

                            </div>
                        </td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

            </table>

        </div>

        <%= Pager.render(variantsPage, pagerUrl, "page") %>

    </div>

</section>

</body>
</html>
