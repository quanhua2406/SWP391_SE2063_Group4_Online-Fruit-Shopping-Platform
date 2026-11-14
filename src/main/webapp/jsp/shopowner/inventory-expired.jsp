<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="inventory-common.jspf" %>

<%!
    private String dmy(Object value) {
        if (value == null) {
            return "-";
        }

        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(value);
    }

    private String dmyDate(Object value) {
        if (value == null) {
            return "-";
        }

        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(value);
    }

    private long daysExpired(Object value) {
        if (!(value instanceof java.sql.Date)) {
            return 0;
        }

        java.time.LocalDate expiry = ((java.sql.Date) value).toLocalDate();

        return java.time.temporal.ChronoUnit.DAYS.between(expiry, java.time.LocalDate.now());
    }
%>

<%
    String activePage = "expired";

    @SuppressWarnings("unchecked")
    java.util.List<Map<String, Object>> expiredBatches =
            (java.util.List<Map<String, Object>>) request.getAttribute("expiredBatches");

%>

<%@ include file="inventory-nav.jspf" %>

<% if (messageText != null) { %>

    <div class="<%= level %>">
        <%= esc(messageText) %>
    </div>

<% } %>

<!-- ==================== EXPIRED BATCHES ==================== -->

<div class="panel">

    <h2>⚠️ Lô hàng không đảm bảo chất lượng</h2>

    <p class="panel-hint">
        Các lô còn hàng nhưng đã quá hạn sử dụng. Bấm Bỏ hàng để bỏ toàn bộ
        số lượng còn lại khỏi tồn kho.
    </p>

    <table class="data-table">

        <thead>
            <tr>
                <th>Lô hàng</th>
                <th>Sản phẩm</th>
                <th>SKU</th>
                <th>Số lượng</th>
                <th>Hạn sử dụng</th>
                <th>Quá hạn</th>
                <th>Thao tác</th>
            </tr>
        </thead>

        <tbody>
        <% if (expiredBatches == null || expiredBatches.isEmpty()) { %>

            <tr>
                <td colspan="7">Không có lô nào không đảm bảo chất lượng.</td>
            </tr>

        <% } else {
               for (Map<String, Object> b : expiredBatches) { %>
            <tr>
                <td><%= esc(b.get("batch_code")) %></td>
                <td><%= esc(b.get("product_name")) %></td>
                <td><%= esc(b.get("sku")) %></td>
                <td><%= qty(b.get("quantity")) %></td>
                <td><%= dmyDate(b.get("expiry_date")) %></td>
                <td><%= daysExpired(b.get("expiry_date")) %> ngày</td>
                <td>
                    <button type="button" class="btn-sm btn-delete"
                            onclick="openDiscard(<%= esc(b.get("batch_id")) %>)">
                        Bỏ hàng
                    </button>
                </td>
            </tr>
        <%     }
           } %>
        </tbody>

    </table>

</div>

<style>
    .discard-modal {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.35);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }
    .discard-modal[hidden] {
        display: none;
    }
    .discard-box {
        background: #ffffff;
        border-radius: 12px;
        padding: 20px 22px;
        min-width: 320px;
        max-width: 420px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    }
</style>

<div id="discardModal" class="discard-modal" hidden
     onclick="if (event.target === this) closeDiscard();">
    <div class="discard-box">
        <h3>Bỏ lô không đảm bảo chất lượng</h3>
        <p class="panel-hint">
            Bỏ toàn bộ số lượng còn lại của lô này khỏi tồn kho.
        </p>
        <textarea form="discardForm" name="discardNote" required maxlength="150"
                  rows="3" placeholder="Ghi chú (bắt buộc)"
                  style="width: 100%; box-sizing: border-box;"></textarea>
        <input form="discardForm" type="file" name="discardImage"
               accept="image/png,image/jpeg,image/gif,image/webp"
               style="margin: 8px 0;">
        <div style="display: flex; gap: 10px; justify-content: flex-end;">
            <button type="button" class="btn-secondary" onclick="closeDiscard()">Hủy</button>
            <button type="button" class="btn-sm btn-delete" onclick="submitDiscard()">Bỏ lô</button>
        </div>
    </div>
</div>

<form id="discardForm" method="post" enctype="multipart/form-data"
      action="<%= contextPath %>/store-owner/inventory/update" hidden>
    <input type="hidden" name="action" value="requestDiscard">
    <input type="hidden" id="discardBatchId" name="batchId" value="">
</form>

<script>
    function openDiscard(batchId) {
        document.getElementById('discardBatchId').value = batchId;
        document.getElementById('discardModal').hidden = false;
    }

    function closeDiscard() {
        document.getElementById('discardModal').hidden = true;
    }

    function submitDiscard() {
        document.getElementById('discardForm').submit();
    }
</script>
