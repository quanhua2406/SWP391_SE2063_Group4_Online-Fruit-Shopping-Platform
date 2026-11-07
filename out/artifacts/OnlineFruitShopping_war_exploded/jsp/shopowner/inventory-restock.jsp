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

    <h2>📥 Nhập hàng cho sản phẩm</h2>

    <p class="panel-hint">
        Ghi nhận một lô hàng mới về và cộng vào tồn kho của một biến thể
        sản phẩm.
    </p>

    <% if (variants == null || variants.isEmpty()) { %>

        <p class="panel-hint">
            Cửa hàng của bạn chưa có biến thể sản phẩm nào nên chưa có gì để
            nhập.
        </p>

    <% } else { %>

        <form method="post"
              action="<%= contextPath %>/store-owner/inventory/restock">

            <input type="hidden" name="action" value="restock">

            <div class="form-grid">

                <div class="form-field">
                    <label for="variantId">Biến thể sản phẩm *</label>
                    <select id="variantId" name="variantId" required>
                        <option value="">-- Chọn biến thể --</option>
                        <% for (Map<String, Object> v : variants) { %>
                            <option value="<%= esc(v.get("variant_id")) %>">
                                <%= esc(v.get("product_name")) %>
                                (<%= esc(v.get("sku")) %>, <%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %>)
                            </option>
                        <% } %>
                    </select>
                </div>

                <div class="form-field">
                    <label for="batchCode">Mã lô *</label>
                    <input type="text" id="batchCode" name="batchCode"
                           maxlength="50" required
                           placeholder="VD: BATCH-001">
                </div>

                <div class="form-field">
                    <label for="quantity">Số lượng *</label>
                    <input type="number" id="quantity" name="quantity"
                           min="0.01" step="0.01" required>
                </div>

                <div class="form-field">
                    <label for="expiryDate">Hạn sử dụng *</label>
                    <input type="date" id="expiryDate" name="expiryDate"
                           required>
                </div>

                <div class="form-field">
                    <label for="reason">Ghi chú</label>
                    <input type="text" id="reason" name="reason"
                           maxlength="255"
                           placeholder="Ghi chú (không bắt buộc)">
                </div>

            </div>

            <div class="form-actions">
                <button type="submit" class="btn-primary">
                    Thêm vào kho
                </button>
            </div>

        </form>

    <% } %>

</div>

</section>

</body>
</html>
