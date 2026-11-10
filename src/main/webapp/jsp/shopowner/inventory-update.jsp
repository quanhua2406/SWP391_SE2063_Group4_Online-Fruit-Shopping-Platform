<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="inventory-common.jspf" %>

<%!
    private String batchStatusLabel(Object value) {
        if (value == null) {
            return "";
        }

        switch (value.toString()) {
            case "active":
                return "Đang hoạt động";
            case "expired":
                return "Không đảm bảo chất lượng";
            case "depleted":
                return "Đã hết";
            default:
                return value.toString();
        }
    }
%>

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

    <h2>✏️ Cập nhật số lượng tồn kho</h2>

    <p class="panel-hint">
        Điều chỉnh số lượng của một lô hàng cụ thể. Mọi thay đổi đều được ghi
        lại tại trang
        <a href="<%= contextPath %>/store-owner/inventory/history">
            Lịch sử kho
        </a>.
    </p>

    <form method="get"
          action="<%= contextPath %>/store-owner/inventory/update"
          class="filter-form">

        <input type="text" class="filter-input" name="bq"
               value="<%= esc(bq) %>"
               placeholder="Tìm lô hàng, sản phẩm hoặc SKU...">

        <select class="filter-select" name="bstatus">
            <option value="">Tất cả trạng thái</option>
            <option value="active" <%= "active".equals(bstatus) ? "selected" : "" %>>Đang hoạt động</option>
            <option value="expired" <%= "expired".equals(bstatus) ? "selected" : "" %>>Không đảm bảo chất lượng</option>
            <option value="depleted" <%= "depleted".equals(bstatus) ? "selected" : "" %>>Đã hết</option>
        </select>

        <button type="submit" class="btn-sm">Lọc</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/inventory/update">Xóa lọc</a>

    </form>

    <table class="data-table">

        <thead>
            <tr>
                <th>Lô hàng</th>
                <th>Sản phẩm</th>
                <th>SKU</th>
                <th>Số lượng</th>
                <th>Hạn sử dụng</th>
                <th>Ngày nhập</th>
                <th>Trạng thái</th>
                <th>Cập nhật</th>
            </tr>
        </thead>

        <tbody>
        <% if (batchesPage == null || batchesPage.isEmpty()) { %>

            <tr>
                <td colspan="8">
                    Chưa có lô hàng nào.
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
                        <%= esc(batchStatusLabel(b.get("status"))) %>
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
                               placeholder="Lý do">

                        <button type="submit" class="btn-sm">
                            Lưu
                        </button>

                    </form>

                    <form method="post"
                          enctype="multipart/form-data"
                          action="<%= contextPath %>/store-owner/inventory/update">

                        <input type="hidden" name="action" value="discardQuality">
                        <input type="hidden" name="batchId"
                               value="<%= esc(b.get("batch_id")) %>">

                        <input class="qty-input" type="number"
                               name="discardQuantity" min="0.01" step="0.01"
                               placeholder="SL bỏ" required>

                        <input class="qty-input" type="text"
                               name="discardReason" maxlength="255"
                               placeholder="Lý do (bắt buộc)" required>

                        <input class="qty-input" type="file"
                               name="discardImage"
                               accept="image/png,image/jpeg,image/gif,image/webp">

                        <button type="submit" class="btn-sm">
                            Bỏ hàng lỗi
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
