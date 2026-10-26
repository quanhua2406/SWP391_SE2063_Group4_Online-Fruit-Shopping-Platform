<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="inventory-common.jspf" %>

<%
    String activePage = "history";

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> adjustmentsPage =
            (Page<Map<String, Object>>) request.getAttribute("adjustmentsPage");

    String hq = request.getParameter("hq") == null ? "" : request.getParameter("hq");
    String htype = request.getParameter("htype") == null ? "" : request.getParameter("htype");

    String historyUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/inventory/history?",
            "hq", "htype");
%>

<%@ include file="inventory-nav.jspf" %>

<% if (messageText != null) { %>

    <div class="<%= level %>">
        <%= esc(messageText) %>
    </div>

<% } %>

<!-- ==================== INVENTORY HISTORY ==================== -->

<div class="panel">

    <h2>🕓 Inventory History</h2>

    <p class="panel-hint">
        Every stock change recorded for your shop.
    </p>

    <form method="get"
          action="<%= contextPath %>/store-owner/inventory/history"
          class="filter-form">

        <input type="text" class="filter-input" name="hq"
               value="<%= esc(hq) %>"
               placeholder="Search product, SKU, batch or reason...">

        <select class="filter-select" name="htype">
            <option value="">All types</option>
            <option value="import" <%= "import".equals(htype) ? "selected" : "" %>>Restock</option>
            <option value="export" <%= "export".equals(htype) ? "selected" : "" %>>Stock Out</option>
            <option value="discard" <%= "discard".equals(htype) ? "selected" : "" %>>Discard</option>
        </select>

        <button type="submit" class="btn-sm">Filter</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/inventory/history">Clear</a>

    </form>

    <table class="data-table">

        <thead>
            <tr>
                <th>Date</th>
                <th>Product</th>
                <th>SKU</th>
                <th>Batch</th>
                <th>Type</th>
                <th>Quantity</th>
                <th>Actor</th>
                <th>Reason</th>
            </tr>
        </thead>

        <tbody>
        <% if (adjustmentsPage == null || adjustmentsPage.isEmpty()) { %>

            <tr>
                <td colspan="8">
                    No inventory changes yet.
                </td>
            </tr>

        <% } else {
               for (Map<String, Object> a : adjustmentsPage.getItems()) {
        %>
            <tr>
                <td><%= esc(a.get("created_at")) %></td>
                <td><%= esc(a.get("product_name")) %></td>
                <td><%= esc(a.get("sku")) %></td>
                <td><%= esc(a.get("batch_code")) %></td>
                <td>
                    <span class="pill <%= typePill(a.get("adjustment_type")) %>">
                        <%= esc(typeLabel(a.get("adjustment_type"))) %>
                    </span>
                </td>
                <td><%= qty(a.get("quantity")) %></td>
                <td><%= esc(a.get("actor")) %></td>
                <td><%= esc(a.get("reason")) %></td>
            </tr>
        <%
               }
           }
        %>
        </tbody>

    </table>

    <%= Pager.render(adjustmentsPage, historyUrl, "hpage") %>

</div>

</section>

</body>
</html>
