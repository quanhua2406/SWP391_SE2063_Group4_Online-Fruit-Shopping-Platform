<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.math.RoundingMode"%>
<%@page import="java.util.Locale"%>
<%@page import="java.time.LocalDate"%>
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

    /**
     * Percentage saved by the promotion, e.g. "-10%".
     */
    private String discount(Object price, Object promoPrice) {
        BigDecimal full = dec(price);
        BigDecimal promo = dec(promoPrice);

        if (full == null || promo == null || full.signum() <= 0) {
            return "";
        }

        BigDecimal off = full.subtract(promo)
                .multiply(BigDecimal.valueOf(100))
                .divide(full, 0, RoundingMode.HALF_UP);

        return "-" + off.toPlainString() + "%";
    }

    private String dateValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate().toString();
        }

        return value.toString();
    }

    /**
     * Where the promotion stands in time: "Đang chạy" when today is
     * inside the range, "Chưa bắt đầu" when it starts later, "Đã kết thúc"
     * when it is over and "" when the variant has no promotion.
     */
    private String periodStatus(Object start, Object end) {
        if (!(start instanceof java.sql.Date) || !(end instanceof java.sql.Date)) {
            return "";
        }

        LocalDate from = ((java.sql.Date) start).toLocalDate();
        LocalDate to = ((java.sql.Date) end).toLocalDate();
        LocalDate today = LocalDate.now();

        if (today.isBefore(from)) {
            return "Chưa bắt đầu";
        }

        if (today.isAfter(to)) {
            return "Đã kết thúc";
        }

        return "Đang chạy";
    }

    private String periodPill(String status) {
        if ("Đang chạy".equals(status)) {
            return "pill-ok";
        }

        if ("Chưa bắt đầu".equals(status)) {
            return "pill-warn";
        }

        return "pill-muted";
    }

    /**
     * Approval state decided by the administrator.
     */
    private String approvalLabel(Object status) {
        String value = status == null ? "" : status.toString();

        if ("approved".equals(value)) {
            return "Đã duyệt";
        }

        if ("rejected".equals(value)) {
            return "Từ chối";
        }

        if ("pending".equals(value)) {
            return "Chờ duyệt";
        }

        return "";
    }

    private String approvalPill(String label) {
        if ("Đã duyệt".equals(label)) {
            return "pill-ok";
        }

        if ("Chờ duyệt".equals(label)) {
            return "pill-warn";
        }

        return "pill-danger";
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    Page<Map<String, Object>> variantsPage =
            (Page<Map<String, Object>>) request.getAttribute("variantsPage");

    String pagerUrl = QueryUrl.keep(request,
            contextPath + "/store-owner/promotions", "q", "weight", "approval");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String weight = request.getParameter("weight") == null ? "" : request.getParameter("weight");

    @SuppressWarnings("unchecked")
    java.util.List<String> weightOptions =
            (java.util.List<String>) request.getAttribute("weightOptions");

    String approval = request.getParameter("approval") == null
            ? ""
            : request.getParameter("approval");

    String currentPage = request.getParameter("page") == null
            ? "1"
            : request.getParameter("page");

    LocalDate today = LocalDate.now();
    String defaultStart = today.toString();
    String defaultEnd = today.plusDays(7).toString();

    Object variantCount = request.getAttribute("variantCount");
    Object pendingCount = request.getAttribute("pendingCount");
    Object approvedCount = request.getAttribute("approvedCount");
    Object rejectedCount = request.getAttribute("rejectedCount");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    if ("promo-set".equals(message)) {
        messageText = "Đã gửi khuyến mãi. Khuyến mãi sẽ có hiệu lực sau khi "
                + "quản trị viên duyệt.";
    } else if ("promo-removed".equals(message)) {
        messageText = "Đã xóa khuyến mãi. Biến thể trở lại giá bán thường.";
    } else if ("promo-invalid".equals(message)) {
        level = "error-message";
        messageText = "Vui lòng nhập giá khuyến mãi và khoảng thời gian hợp lệ.";
    } else if ("promo-expired".equals(message)) {
        level = "error-message";
        messageText = "Ngày kết thúc khuyến mãi không được nằm trong quá khứ.";
    } else if ("promo-not-discount".equals(message)) {
        level = "error-message";
        messageText = "Giá khuyến mãi phải thấp hơn giá bán thường.";
    } else if ("invalid".equals(message)) {
        level = "error-message";
        messageText = "Vui lòng chọn một biến thể sản phẩm.";
    } else if ("forbidden".equals(message)) {
        level = "error-message";
        messageText = "Bạn không có quyền thay đổi khuyến mãi này.";
    } else if ("failed".equals(message)) {
        level = "error-message";
        messageText = "Không thể lưu khuyến mãi.";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Quản lý khuyến mãi - FreshFruit</title>

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

    <h1>Quản lý khuyến mãi</h1>

    <p class="dashboard-welcome">
        Yêu cầu giá khuyến mãi có thời hạn cho một biến thể sản phẩm.
        Mọi khuyến mãi đều được quản trị viên xem xét trước khi áp dụng,
        và chỉ chạy trong khoảng thời gian đã đặt.
    </p>

    <% if (messageText != null) { %>

        <div class="<%= level %>">
            <%= esc(messageText) %>
        </div>

    <% } %>

    <div class="summary-grid summary-grid-4">

        <div class="summary-card">
            <span class="summary-label">Biến thể sản phẩm</span>
            <span class="summary-value"><%= esc(qty(variantCount)) %></span>
        </div>

        <div class="summary-card summary-warn">
            <span class="summary-label">Chờ duyệt</span>
            <span class="summary-value"><%= esc(qty(pendingCount)) %></span>
        </div>

        <div class="summary-card summary-accent">
            <span class="summary-label">Đã duyệt</span>
            <span class="summary-value"><%= esc(qty(approvedCount)) %></span>
        </div>

        <div class="summary-card">
            <span class="summary-label">Từ chối</span>
            <span class="summary-value"><%= esc(qty(rejectedCount)) %></span>
        </div>

    </div>

    <form method="get"
          action="<%= contextPath %>/store-owner/promotions"
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

        <select class="filter-select" name="approval">
            <option value="">Tất cả trạng thái duyệt</option>
            <option value="pending" <%= "pending".equals(approval) ? "selected" : "" %>>Chờ duyệt</option>
            <option value="approved" <%= "approved".equals(approval) ? "selected" : "" %>>Đã duyệt</option>
            <option value="rejected" <%= "rejected".equals(approval) ? "selected" : "" %>>Từ chối</option>
        </select>

        <button type="submit" class="btn-sm">Lọc</button>

        <a class="filter-clear"
           href="<%= contextPath %>/store-owner/promotions">Xóa lọc</a>

    </form>

    <!-- ==================== SET PROMOTIONAL PRICE ==================== -->

    <div class="panel" id="promo">

        <h2>🏷️ Đặt giá khuyến mãi</h2>

        <p class="panel-hint">
            Lưu khuyến mãi sẽ gửi yêu cầu duyệt; chỉnh sửa khuyến mãi đã duyệt
            sẽ đưa nó trở lại để xem xét. Giá bán thường được quản lý tại
            <a href="<%= contextPath %>/store-owner/pricing">
                Quản lý giá bán
            </a>.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>SKU</th>
                        <th>Khối lượng</th>
                        <th>Giá bán thường</th>
                        <th>Giá khuyến mãi</th>
                        <th>Thời gian khuyến mãi</th>
                        <th>Duyệt</th>
                        <th>Khuyến mãi</th>
                    </tr>
                </thead>

                <tbody>
                <% if (variantsPage == null || variantsPage.isEmpty()) { %>

                    <tr>
                        <td colspan="8">
                            Không có biến thể sản phẩm nào khớp với tìm kiếm.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> v : variantsPage.getItems()) {

                           Object promoPrice = v.get("promo_price");
                           Object promoStart = v.get("promo_start");
                           Object promoEnd = v.get("promo_end");

                           String period = periodStatus(promoStart, promoEnd);
                           String approvalText = approvalLabel(v.get("promo_status"));
                %>
                    <tr>
                        <td><%= esc(v.get("product_name")) %></td>
                        <td><%= esc(v.get("sku")) %></td>
                        <td><%= esc(VariantWeights.label(v.get("weight_value"), v.get("unit"))) %></td>
                        <td><%= money(v.get("price")) %></td>
                        <td>
                            <% if (promoPrice == null) { %>
                                —
                            <% } else { %>
                                <strong><%= money(promoPrice) %></strong>
                                <span class="pill pill-ok">
                                    <%= esc(discount(v.get("price"), promoPrice)) %>
                                </span>
                            <% } %>
                        </td>
                        <td>
                            <% if (promoPrice == null) { %>
                                —
                            <% } else { %>
                                <%= esc(dateValue(promoStart)) %>
                                →
                                <%= esc(dateValue(promoEnd)) %>
                                <br>
                                <span class="pill <%= periodPill(period) %>">
                                    <%= period %>
                                </span>
                            <% } %>
                        </td>
                        <td>
                            <% if (approvalText.isEmpty()) { %>
                                —
                            <% } else { %>
                                <span class="pill <%= approvalPill(approvalText) %>">
                                    <%= approvalText %>
                                </span>

                                <% if ("Từ chối".equals(approvalText)
                                        && v.get("promo_note") != null) { %>
                                    <span class="row-note">
                                        <%= esc(v.get("promo_note")) %>
                                    </span>
                                <% } %>
                            <% } %>
                        </td>
                        <td>
                            <div class="table-actions">

                                <form method="post"
                                      action="<%= contextPath %>/store-owner/promotions">

                                    <input type="hidden" name="action"
                                           value="setPromo">

                                    <input type="hidden" name="variantId"
                                           value="<%= esc(v.get("variant_id")) %>">

                                    <input type="hidden" name="q"
                                           value="<%= esc(q) %>">

                                    <input type="hidden" name="weight"
                                           value="<%= esc(weight) %>">

                                    <input type="hidden" name="approval"
                                           value="<%= esc(approval) %>">

                                    <input type="hidden" name="page"
                                           value="<%= esc(currentPage) %>">

                                    <input class="qty-input" type="number"
                                           name="promoPrice" min="1000" step="1000"
                                           value="<%= promoPrice == null ? "" : qty(promoPrice) %>"
                                           placeholder="Giá KM đ"
                                           required>

                                    <input class="qty-input date-input" type="date"
                                           name="startDate"
                                           value="<%= promoStart == null ? defaultStart : esc(dateValue(promoStart)) %>"
                                           required>

                                    <input class="qty-input date-input" type="date"
                                           name="endDate"
                                           value="<%= promoEnd == null ? defaultEnd : esc(dateValue(promoEnd)) %>"
                                           required>

                                    <button type="submit" class="btn-sm">
                                        Gửi
                                    </button>

                                </form>

                                <% if (promoPrice != null) { %>

                                    <form method="post"
                                          action="<%= contextPath %>/store-owner/promotions">

                                        <input type="hidden" name="action"
                                               value="removePromo">

                                        <input type="hidden" name="variantId"
                                               value="<%= esc(v.get("variant_id")) %>">

                                        <input type="hidden" name="q"
                                               value="<%= esc(q) %>">

                                        <input type="hidden" name="weight"
                                               value="<%= esc(weight) %>">

                                        <input type="hidden" name="approval"
                                               value="<%= esc(approval) %>">

                                        <input type="hidden" name="page"
                                               value="<%= esc(currentPage) %>">

                                        <button type="submit"
                                                class="btn-sm btn-sm-danger">
                                            Xóa
                                        </button>

                                    </form>

                                <% } %>

                            </div>
                        </td>
                    </tr>
                <%
                       }
                   }
                %>
                </tbody>

            </table>

        </div>

        <%= Pager.render(variantsPage, pagerUrl, "page") %>

    </div>

</section>

</body>
</html>
