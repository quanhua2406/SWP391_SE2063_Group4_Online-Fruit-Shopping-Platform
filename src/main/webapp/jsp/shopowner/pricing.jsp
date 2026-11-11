<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Locale"%>
<%@page import="java.text.SimpleDateFormat"%>
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

    private String reasonLabel(Object reason) {
        String code = reason == null ? "" : reason.toString();

        switch (code) {
            case "manual": return "Nhập giá tay";
            case "grade": return "Đổi phẩm cấp";
            case "price_per_kg": return "Giá theo kg";
            case "variant_form": return "Form biến thể";
            default: return code;
        }
    }
%>

<%
    String contextPath = request.getContextPath();

    String activePage = (String) request.getAttribute("activePage");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> variantsPage =
            (Page<Map<String, Object>>) request.getAttribute("variantsPage");

    String pagerUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/pricing/variant", "q", "weight");

    String productPagerUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/pricing/product");

    String historyPagerUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/pricing/history");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String weight = request.getParameter("weight") == null ? "" : request.getParameter("weight");

    @SuppressWarnings("unchecked")
    java.util.List<String> weightOptions =
            (java.util.List<String>) request.getAttribute("weightOptions");

    @SuppressWarnings("unchecked")
    java.util.List<Map<String, Object>> qualityGrades =
            (java.util.List<Map<String, Object>>) request.getAttribute("qualityGrades");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> productsPage =
            (Page<Map<String, Object>>) request.getAttribute("productsPage");

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> historyPage =
            (Page<Map<String, Object>>) request.getAttribute("historyPage");

    String currentPage = request.getParameter("page") == null
            ? "1"
            : request.getParameter("page");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    if ("updated".equals(message)) {
        messageText = "Cập nhật giá thành công.";
    } else if ("kg-saved".equals(message)) {
        messageText = "Đã lưu giá theo kg. Các biến thể có khối lượng được tính lại giá.";
    } else if ("grade-saved".equals(message)) {
        messageText = "Đã cập nhật phẩm cấp.";
    } else if ("invalid".equals(message)) {
        level = "error-message";
        messageText = "Vui lòng nhập giá hợp lệ.";
    } else if ("invalid-kg".equals(message)) {
        level = "error-message";
        messageText = "Vui lòng nhập giá theo kg hợp lệ (lớn hơn 0).";
    } else if ("invalid-grade".equals(message)) {
        level = "error-message";
        messageText = "Phẩm cấp không hợp lệ.";
    } else if ("auto-priced".equals(message)) {
        level = "error-message";
        messageText = "Biến thể này được tính giá tự động từ giá/kg và phẩm cấp. "
                + "Hãy đặt giá/kg của sản phẩm để thay đổi.";
    } else if ("promo-conflict".equals(message)) {
        level = "error-message";
        messageText = "Giá này không cao hơn giá khuyến mãi đang chạy. "
                + "Hãy cập nhật hoặc xóa khuyến mãi đó trước.";
    } else if ("forbidden".equals(message)) {
        level = "error-message";
        messageText = "Bạn không có quyền thay đổi giá này.";
    } else if ("failed".equals(message)) {
        level = "error-message";
        messageText = "Không thể cập nhật giá.";
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Quản lý giá bán - FreshFruit</title>

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
            📊 Bảng điều khiển
        </a>

        <a href="<%= contextPath %>/store-owner/inventory">
            📦 Kho hàng
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Đăng xuất
        </a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/store-owner/dashboard">
            ← Bảng điều khiển chủ cửa hàng
        </a>
    </p>

    <h1>Quản lý giá bán</h1>

    <p class="dashboard-welcome">
        Đặt giá/kg cho từng sản phẩm và phẩm cấp cho từng biến thể.
        Các ưu đãi tạm thời được quản lý tại
        <a href="<%= contextPath %>/store-owner/promotions">
            Quản lý khuyến mãi
        </a>.
    </p>

    <% if (messageText != null) { %>

        <div class="<%= level %>">
            <%= esc(messageText) %>
        </div>

    <% } %>

    <nav class="section-nav">

        <a class="<%= "product".equals(activePage) ? "active" : "" %>"
           href="<%= contextPath %>/store-owner/pricing/product">
            ⚖️ Theo sản phẩm
        </a>

        <a class="<%= "variant".equals(activePage) ? "active" : "" %>"
           href="<%= contextPath %>/store-owner/pricing/variant">
            💲 Theo biến thể
        </a>

        <a class="<%= "history".equals(activePage) ? "active" : "" %>"
           href="<%= contextPath %>/store-owner/pricing/history">
            🕘 Lịch sử
        </a>

    </nav>

    <% if ("product".equals(activePage)) { %>

    <!-- ==================== PRICE PER KG ==================== -->

    <div class="panel" id="kg">

        <h2>⚖️ Giá theo kg của sản phẩm</h2>

        <p class="panel-hint">
            Biến thể có khối lượng được tính giá:
            <strong>giá/kg × khối lượng × hệ số phẩm cấp</strong>.
            Hệ số:
            <% if (qualityGrades != null) {
                   for (Map<String, Object> g : qualityGrades) { %>
                <%= esc(g.get("label")) %> = <%= qty(g.get("factor")) %>;
            <%     }
               } %>
        </p>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Sản phẩm</th>
                    <th>Giá/kg hiện tại</th>
                    <th>Đặt giá/kg (đ)</th>
                </tr>
            </thead>

            <tbody>
            <% if (productsPage == null || productsPage.isEmpty()) { %>

                <tr>
                    <td colspan="3">Cửa hàng chưa có sản phẩm nào.</td>
                </tr>

            <% } else {
                   for (Map<String, Object> p : productsPage.getItems()) { %>
                <tr>
                    <td><%= esc(p.get("product_name")) %></td>
                    <td>
                        <%= p.get("price_per_kg") == null
                                ? "Chưa đặt"
                                : money(p.get("price_per_kg")) + "/kg" %>
                    </td>
                    <td>
                        <form method="post"
                              action="<%= contextPath %>/store-owner/pricing">

                            <input type="hidden" name="action" value="kg">

                            <input type="hidden" name="productId"
                                   value="<%= esc(p.get("product_id")) %>">

                            <input type="hidden" name="ppage"
                                   value="<%= productsPage.getPage() %>">

                            <input class="qty-input" type="number"
                                   name="pricePerKg" min="1" step="1"
                                   value="<%= p.get("price_per_kg") == null ? "" : qty(p.get("price_per_kg")) %>"
                                   required>

                            <button type="submit" class="btn-sm btn-edit">
                                Lưu
                            </button>

                        </form>
                    </td>
                </tr>
            <%     }
               } %>
            </tbody>

        </table>

        <%= Pager.render(productsPage, productPagerUrl, "ppage") %>

    </div>

    <% } %>

    <% if ("variant".equals(activePage)) { %>

    <!-- ==================== FILTER ==================== -->

    <form method="get"
          action="<%= contextPath %>/store-owner/pricing/variant"
          class="filter-form">

        <input type="text" class="filter-input" name="q"
               value="<%= esc(q) %>"
               placeholder="Tìm sản phẩm hoặc SKU...">

        <select class="filter-select" name="weight">
            <option value="">Tất cả khối lượng</option>
            <% if (weightOptions != null) {
                   for (String w : weightOptions) { %>
                <option value="<%= esc(w) %>"
                    <%= w.equals(weight) ? "selected" : "" %>>
                    <%= esc(w) %> kg
                </option>
            <%     }
               } %>
        </select>

        <button type="submit" class="btn-primary" style="padding: 9px 16px; border-radius: 8px;">Lọc</button>

        <a href="<%= contextPath %>/store-owner/pricing/variant" class="btn-secondary" style="padding: 9px 14px; font-size: 13px;">Xóa lọc</a>

    </form>

    <!-- ==================== VARIANT PRICES ==================== -->

    <div class="panel" id="price">

        <h2>💲 Giá và phẩm cấp theo biến thể</h2>

        <p class="panel-hint">
            Biến thể có khối lượng và sản phẩm đã đặt giá/kg được tính giá tự động.
            Các biến thể khác đặt giá bằng tay.
        </p>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Sản phẩm</th>
                    <th>SKU</th>
                    <th>Khối lượng</th>
                    <th>Đóng gói</th>
                    <th>Phẩm cấp</th>
                    <th>Giá hiện tại</th>
                    <th>Giá mới (đ)</th>
                    <th>Tồn kho</th>
                </tr>
            </thead>

            <tbody>
            <% if (variantsPage == null || variantsPage.isEmpty()) { %>

                <tr>
                    <td colspan="8">
                        <% if (productsPage == null || productsPage.isEmpty()) { %>
                        Cửa hàng chưa có sản phẩm nào.
                        <% } else { %>
                        Không có biến thể sản phẩm nào khớp với tìm kiếm.
                        <% } %>
                    </td>
                </tr>

            <% } else {
                   for (Map<String, Object> v : variantsPage.getItems()) {
                       boolean auto = Boolean.TRUE.equals(v.get("auto"));
            %>
                <tr>
                    <td><%= esc(v.get("product_name")) %></td>
                    <td><%= esc(v.get("sku")) %></td>
                    <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                    <td><%= esc(v.get("packaging_type")) %></td>

                    <td>
                        <form method="post"
                              action="<%= contextPath %>/store-owner/pricing">

                            <input type="hidden" name="action" value="grade">

                            <input type="hidden" name="variantId"
                                   value="<%= esc(v.get("variant_id")) %>">

                            <input type="hidden" name="q"
                                   value="<%= esc(q) %>">

                            <input type="hidden" name="weight"
                                   value="<%= esc(weight) %>">

                            <input type="hidden" name="page"
                                   value="<%= esc(currentPage) %>">

                            <select name="grade" class="filter-select">
                                <% if (qualityGrades != null) {
                                       for (Map<String, Object> g : qualityGrades) {
                                           boolean selected = String.valueOf(g.get("grade"))
                                                   .equals(String.valueOf(v.get("quality_grade")));
                                %>
                                    <option value="<%= esc(g.get("grade")) %>"
                                        <%= selected ? "selected" : "" %>>
                                        <%= esc(g.get("label")) %>
                                    </option>
                                <%     }
                                   } %>
                            </select>

                            <button type="submit" class="btn-sm btn-edit">
                                Lưu
                            </button>

                        </form>
                    </td>

                    <td><%= money(v.get("price")) %></td>

                    <td>
                        <% if (auto) { %>
                            <span class="stock-badge">Theo giá/kg</span>
                        <% } else { %>
                            <form method="post"
                                  action="<%= contextPath %>/store-owner/pricing">

                                <input type="hidden" name="action" value="price">

                                <input type="hidden" name="variantId"
                                       value="<%= esc(v.get("variant_id")) %>">

                                <input type="hidden" name="q"
                                       value="<%= esc(q) %>">

                                <input type="hidden" name="weight"
                                       value="<%= esc(weight) %>">

                                <input type="hidden" name="page"
                                       value="<%= esc(currentPage) %>">

                                <input class="qty-input" type="number"
                                       name="price" min="0" step="1"
                                       value="<%= qty(v.get("price")) %>"
                                       required>

                                <button type="submit" class="btn-sm btn-edit">
                                    Lưu
                                </button>

                            </form>
                        <% } %>
                    </td>

                    <td><%= qty(v.get("stock")) %></td>
                </tr>
            <%     }
               } %>
            </tbody>

        </table>

        <%= Pager.render(variantsPage, pagerUrl, "page") %>

    </div>

    <% } %>

    <% if ("history".equals(activePage)) { %>

    <!-- ==================== PRICE HISTORY ==================== -->

    <div class="panel" id="history">

        <h2>🕘 Lịch sử thay đổi giá</h2>

        <p class="panel-hint">Các thay đổi giá, mới nhất trước.</p>

        <table class="data-table">

            <thead>
                <tr>
                    <th>Thời gian</th>
                    <th>Sản phẩm</th>
                    <th>SKU</th>
                    <th>Giá cũ</th>
                    <th>Giá mới</th>
                    <th>Lý do</th>
                </tr>
            </thead>

            <tbody>
            <% if (historyPage == null || historyPage.isEmpty()) { %>

                <tr>
                    <td colspan="6">Chưa có thay đổi giá nào.</td>
                </tr>

            <% } else {
                   for (Map<String, Object> h : historyPage.getItems()) { %>
                <tr>
                    <td><%= h.get("changed_at") == null
                            ? ""
                            : esc(dateFormat.format(h.get("changed_at"))) %></td>
                    <td><%= esc(h.get("product_name")) %></td>
                    <td><%= esc(h.get("sku")) %></td>
                    <td><%= h.get("old_price") == null ? "-" : money(h.get("old_price")) %></td>
                    <td><%= money(h.get("new_price")) %></td>
                    <td><%= esc(reasonLabel(h.get("reason"))) %></td>
                </tr>
            <%     }
               } %>
            </tbody>

        </table>

        <%= Pager.render(historyPage, historyPagerUrl, "hpage") %>

    </div>

    <% } %>

</section>

</body>
</html>
