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

    <h2>🕓 Lịch sử kho</h2>

    <p class="panel-hint">
        Mọi thay đổi tồn kho được ghi nhận cho cửa hàng của bạn.
    </p>

    <form method="get"
          action="<%= contextPath %>/store-owner/inventory/history"
          class="filter-form">

        <input type="text" class="filter-input" name="hq"
               value="<%= esc(hq) %>"
               placeholder="Tìm sản phẩm, SKU, lô hàng hoặc lý do...">

        <select class="filter-select" name="htype">
            <option value="">Tất cả loại</option>
            <option value="import" <%= "import".equals(htype) ? "selected" : "" %>>Nhập hàng</option>
            <option value="export" <%= "export".equals(htype) ? "selected" : "" %>>Xuất kho</option>
            <option value="discard" <%= "discard".equals(htype) ? "selected" : "" %>>Hủy hàng</option>
        </select>

        <button type="submit" class="btn-sm">Lọc</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/inventory/history">Xóa lọc</a>

    </form>

    <table class="data-table">

        <thead>
            <tr>
                <th>Ngày</th>
                <th>Sản phẩm</th>
                <th>SKU</th>
                <th>Lô hàng</th>
                <th>Loại</th>
                <th>Số lượng</th>
                <th>Lý do</th>
            </tr>
        </thead>

        <tbody>
        <% if (adjustmentsPage == null || adjustmentsPage.isEmpty()) { %>

            <tr>
                <td colspan="7">
                    Chưa có thay đổi tồn kho nào.
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
