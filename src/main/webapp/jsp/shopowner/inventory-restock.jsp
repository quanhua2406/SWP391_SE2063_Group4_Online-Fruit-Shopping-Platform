<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="inventory-common.jspf" %>

<%
    String activePage = "restock";

    @SuppressWarnings("unchecked")
    java.util.List<Map<String, Object>> variants =
            (java.util.List<Map<String, Object>>) request.getAttribute("variants");
%>

<%@ include file="inventory-nav.jspf" %>

<% if (messageText != null) { %>

    <div class="<%= level %>">
        <%= esc(messageText) %>
    </div>

<% } %>

<!-- ==================== RESTOCK ==================== -->

<div class="panel">

    <h2>📥 Restock Products</h2>

    <p class="panel-hint">
        Record a new batch arrival and add it to a variant's
        stock.
    </p>

    <% if (variants == null || variants.isEmpty()) { %>

        <p class="panel-hint">
            Your shop has no product variants yet, so there is
            nothing to restock.
        </p>

    <% } else { %>

        <form method="post"
              action="<%= contextPath %>/store-owner/inventory/restock">

            <input type="hidden" name="action" value="restock">

            <div class="form-grid">

                <div class="form-field">
                    <label for="variantId">Product Variant *</label>
                    <select id="variantId" name="variantId" required>
                        <option value="">-- Select variant --</option>
                        <% for (Map<String, Object> v : variants) { %>
                            <option value="<%= esc(v.get("variant_id")) %>">
                                <%= esc(v.get("product_name")) %>
                                (<%= esc(v.get("sku")) %>, <%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %>)
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="form-field">
                    <label for="batchCode">Batch Code *</label>
                    <input type="text" id="batchCode" name="batchCode"
                           maxlength="50" required
                           placeholder="e.g. BATCH-001">
                </div>

                <div class="form-field">
                    <label for="quantity">Quantity *</label>
                    <input type="number" id="quantity" name="quantity"
                           min="0.01" step="0.01" required>
                </div>

                <div class="form-field">
                    <label for="expiryDate">Expiry Date *</label>
                    <input type="date" id="expiryDate" name="expiryDate"
                           required>
                </div>

                <div class="form-field">
                    <label for="reason">Note</label>
                    <input type="text" id="reason" name="reason"
                           maxlength="255"
                           placeholder="Optional note">
                </div>

            </div>

            <div class="form-actions">
                <button type="submit" class="btn-primary">
                    Add Stock
                </button>
            </div>

        </form>

    <% } %>

</div>

</section>

</body>
</html>
