<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="inventory-common.jspf" %>

<%
    String activePage = "stock";

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> variantsPage =
            (Page<Map<String, Object>>) request.getAttribute("variantsPage");

    @SuppressWarnings("unchecked")
    java.util.List<String> weightOptions =
            (java.util.List<String>) request.getAttribute("weightOptions");

    String vq = request.getParameter("vq") == null ? "" : request.getParameter("vq");
    String vweight = request.getParameter("vweight") == null ? "" : request.getParameter("vweight");

    String stockUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/inventory?",
            "vq", "vweight");
%>

<%@ include file="inventory-nav.jspf" %>

<% if (messageText != null) { %>

    <div class="<%= level %>">
        <%= esc(messageText) %>
    </div>

<% } %>

<!-- ==================== VIEW STOCK ==================== -->

<div class="panel">

    <h2>📦 View Stock Quantity</h2>

    <p class="panel-hint">
        Total active stock per product variant.
    </p>

    <form method="get"
          action="<%= contextPath %>/store-owner/inventory"
          class="filter-form">

        <input type="text" class="filter-input" name="vq"
               value="<%= esc(vq) %>"
               placeholder="Search product or SKU...">

        <select class="filter-select" name="vweight">
            <option value="">All weights</option>
            <% if (weightOptions != null) {
                   for (String w : weightOptions) { %>
                <option value="<%= esc(w) %>"
                    <%= w.equals(vweight) ? "selected" : "" %>>
                    <%= esc(w) %> kg
                </option>
            <%     }
               } %>
        </select>

        <button type="submit" class="btn-sm">Filter</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/inventory">Clear</a>

    </form>

    <table class="data-table">

        <thead>
            <tr>
                <th>Product</th>
                <th>SKU</th>
                <th>Weight</th>
                <th>Packaging</th>
                <th>Price</th>
                <th>Stock</th>
            </tr>
        </thead>

        <tbody>
        <% if (variantsPage == null || variantsPage.isEmpty()) { %>

            <tr>
                <td colspan="6">
                    No product variants in your shop yet.
                </td>
            </tr>

        <% } else {
               for (Map<String, Object> v : variantsPage.getItems()) {
                   double stock = Double.parseDouble(qty(v.get("stock")));
                   double limit = threshold == null
                           ? 0
                           : Double.parseDouble(threshold.toString());
        %>
            <tr>
                <td><%= esc(v.get("product_name")) %></td>
                <td><%= esc(v.get("sku")) %></td>
                <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                <td><%= esc(v.get("packaging_type")) %></td>
                <td><%= money(v.get("price")) %></td>
                <td>
                    <% if (stock <= limit) { %>
                        <span class="pill pill-warn">
                            <%= qty(v.get("stock")) %>
                        </span>
                    <% } else { %>
                        <span class="pill pill-ok">
                            <%= qty(v.get("stock")) %>
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

    <%= Pager.render(variantsPage, stockUrl, "vpage") %>

</div>

</section>

</body>
</html>
