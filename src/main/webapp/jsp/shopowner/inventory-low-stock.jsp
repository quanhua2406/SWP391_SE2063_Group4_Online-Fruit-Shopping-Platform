<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="inventory-common.jspf" %>

<%
    String activePage = "lowstock";

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> lowStockPage =
            (Page<Map<String, Object>>) request.getAttribute("lowStockPage");

    String lq = request.getParameter("lq") == null ? "" : request.getParameter("lq");

    String lowUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/inventory/low-stock?",
            "lq");
%>

<%@ include file="inventory-nav.jspf" %>

<% if (messageText != null) { %>

    <div class="<%= level %>">
        <%= esc(messageText) %>
    </div>

<% } %>

<!-- ==================== LOW STOCK ==================== -->

<div class="panel">

    <h2>⚠️ Theo dõi tồn kho thấp</h2>

    <p class="panel-hint">
        Các biến thể có tồn kho bằng hoặc thấp hơn ngưỡng an toàn
        <strong><%= esc(thresholdText) %></strong> đơn vị.
    </p>

    <form method="post"
          action="<%= contextPath %>/store-owner/inventory/low-stock"
          class="inline-form">

        <input type="hidden" name="action" value="setThreshold">

        <label for="threshold">Ngưỡng an toàn</label>

        <input class="qty-input" type="number" id="threshold"
               name="threshold" min="0" step="1"
               value="<%= esc(thresholdText) %>" required>

        <button type="submit" class="btn-sm">
            Lưu ngưỡng
        </button>

    </form>

    <form method="post"
          action="<%= contextPath %>/store-owner/inventory/low-stock"
          class="inline-form">

        <input type="hidden" name="action" value="notifyLowStock">

        <button type="submit" class="btn-sm btn-sm-warning">
            ✉️ Gửi cảnh báo email
        </button>

        <span class="hint">
            Gửi danh sách bên dưới tới email tài khoản của bạn.
        </span>

    </form>

    <form method="get"
          action="<%= contextPath %>/store-owner/inventory/low-stock"
          class="filter-form">

        <input type="text" class="filter-input" name="lq"
               value="<%= esc(lq) %>"
               placeholder="Tìm sản phẩm hoặc SKU...">

        <button type="submit" class="btn-sm">Lọc</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/inventory/low-stock">Xóa lọc</a>

    </form>

    <table class="data-table">

        <thead>
            <tr>
                <th>Sản phẩm</th>
                <th>SKU</th>
                <th>Khối lượng</th>
                <th>Đóng gói</th>
                <th>Giá</th>
                <th>Tồn kho</th>
            </tr>
        </thead>

        <tbody>
        <% if (lowStockPage == null || lowStockPage.isEmpty()) { %>

            <tr>
                <td colspan="6">
                    ✅ Tồn kho đều ổn định. Hiện không có sản phẩm nào
                    sắp hết.
                </td>
            </tr>

        <% } else {
               for (Map<String, Object> v : lowStockPage.getItems()) {
        %>
            <tr>
                <td><%= esc(v.get("product_name")) %></td>
                <td><%= esc(v.get("sku")) %></td>
                <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                <td><%= esc(v.get("packaging_type")) %></td>
                <td><%= money(v.get("price")) %></td>
                <td>
                    <span class="pill pill-warn">
                        <%= qty(v.get("stock")) %>
                    </span>
                </td>
            </tr>
        <%
               }
           }
        %>
        </tbody>

    </table>

    <%= Pager.render(lowStockPage, lowUrl, "lpage") %>

</div>

</section>

</body>
</html>
