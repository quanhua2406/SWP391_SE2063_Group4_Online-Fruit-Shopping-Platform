<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="inventory-common.jspf" %>

<%
    String activePage = "update";

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> batchesPage =
            (Page<Map<String, Object>>) request.getAttribute("batchesPage");

    String bq = request.getParameter("bq") == null ? "" : request.getParameter("bq");
    String bstatus = request.getParameter("bstatus") == null ? "" : request.getParameter("bstatus");

    String batchUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/inventory/update?",
            "bq", "bstatus");
%>

<%@ include file="inventory-nav.jspf" %>

<% if (messageText != null) { %>

    <div class="<%= level %>">
        <%= esc(messageText) %>
    </div>

<% } %>

<!-- ==================== UPDATE STOCK ==================== -->

<div class="panel">

    <h2>✏️ Update Stock Quantity</h2>

    <p class="panel-hint">
        Adjust the quantity of a specific stock batch. Every
        change is recorded in the
        <a href="<%= contextPath %>/store-owner/inventory/history">
            Inventory History
        </a> page.
    </p>

    <form method="get"
          action="<%= contextPath %>/store-owner/inventory/update"
          class="filter-form">

        <input type="text" class="filter-input" name="bq"
               value="<%= esc(bq) %>"
               placeholder="Search batch, product or SKU...">

        <select class="filter-select" name="bstatus">
            <option value="">All statuses</option>
            <option value="active" <%= "active".equals(bstatus) ? "selected" : "" %>>active</option>
            <option value="expired" <%= "expired".equals(bstatus) ? "selected" : "" %>>expired</option>
            <option value="depleted" <%= "depleted".equals(bstatus) ? "selected" : "" %>>depleted</option>
        </select>

        <button type="submit" class="btn-sm">Filter</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/inventory/update">Clear</a>

    </form>

    <table class="data-table">

        <thead>
            <tr>
                <th>Batch</th>
                <th>Product</th>
                <th>SKU</th>
                <th>Quantity</th>
                <th>Expiry</th>
                <th>Received</th>
                <th>Status</th>
                <th>Update</th>
            </tr>
        </thead>

        <tbody>
        <% if (batchesPage == null || batchesPage.isEmpty()) { %>

            <tr>
                <td colspan="8">
                    No stock batches yet.
                </td>
            </tr>

        <% } else {
               for (Map<String, Object> b : batchesPage.getItems()) {
        %>
            <tr>
                <td><%= esc(b.get("batch_code")) %></td>
                <td><%= esc(b.get("product_name")) %></td>
                <td><%= esc(b.get("sku")) %></td>
                <td><%= qty(b.get("quantity")) %></td>
                <td><%= date(b.get("expiry_date")) %></td>
                <td><%= date(b.get("received_date")) %></td>
                <td>
                    <span class="pill <%= "active".equals(b.get("status"))
                            ? "pill-ok" : "pill-danger" %>">
                        <%= esc(b.get("status")) %>
                    </span>
                </td>
                <td>
                    <form method="post"
                          action="<%= contextPath %>/store-owner/inventory/update">

                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="batchId"
                               value="<%= esc(b.get("batch_id")) %>">

                        <input class="qty-input" type="number"
                               name="quantity" min="0" step="0.01"
                               value="<%= qty(b.get("quantity")) %>"
                               required>

                        <input class="qty-input" type="text"
                               name="reason" maxlength="255"
                               placeholder="Reason">

                        <button type="submit" class="btn-sm">
                            Save
                        </button>

                    </form>
                </td>
            </tr>
        <%
               }
           }
        %>
        </tbody>

    </table>

    <%= Pager.render(batchesPage, batchUrl, "bpage") %>

</div>

</section>

</body>
</html>
