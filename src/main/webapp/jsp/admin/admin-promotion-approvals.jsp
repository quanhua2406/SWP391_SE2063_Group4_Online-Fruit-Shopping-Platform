<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.math.RoundingMode"%>
<%@page import="java.util.Locale"%>
<%@page import="java.time.LocalDate"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Page"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.Pager"%>
<%@page import="com.mycompany.onlinefruitshopping.utils.QueryUrl"%>

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

    private String periodStatus(Object start, Object end) {
        if (!(start instanceof java.sql.Date) || !(end instanceof java.sql.Date)) {
            return "";
        }

        LocalDate from = ((java.sql.Date) start).toLocalDate();
        LocalDate to = ((java.sql.Date) end).toLocalDate();
        LocalDate today = LocalDate.now();

        if (today.isBefore(from)) {
            return "Sắp diễn ra";
        }

        if (today.isAfter(to)) {
            return "Đã kết thúc";
        }

        return "Đang diễn ra";
    }

    private String periodPill(String status) {
        if ("Đang diễn ra".equals(status)) {
            return "pill-ok";
        }

        if ("Sắp diễn ra".equals(status)) {
            return "pill-warn";
        }

        return "pill-muted";
    }

    private String statusLabel(Object status) {
        String value = status == null ? "" : status.toString();

        if ("approved".equals(value)) {
            return "Đã duyệt";
        }

        if ("rejected".equals(value)) {
            return "Từ chối";
        }

        return "Chờ duyệt";
    }

    private String statusPill(String label) {
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
    Page<Map<String, Object>> promotionsPage =
            (Page<Map<String, Object>>) request.getAttribute("promotionsPage");

    String pagerUrl = QueryUrl.keep(request,
            contextPath + "/admin/promotion-approvals", "status", "q");

    String q = request.getParameter("q") == null ? "" : request.getParameter("q");
    String status = request.getParameter("status") == null
            ? ""
            : request.getParameter("status");

    String currentPage = request.getParameter("page") == null
            ? "1"
            : request.getParameter("page");

    Object pendingCount = request.getAttribute("pendingCount");
    Object approvedCount = request.getAttribute("approvedCount");
    Object rejectedCount = request.getAttribute("rejectedCount");
    Object totalCount = request.getAttribute("totalCount");

    String message = request.getAttribute("message") == null
            ? ""
            : request.getAttribute("message").toString();

    String level = "success-message";
    String messageText = null;

    if ("approved".equals(message)) {
        messageText = "Đã duyệt khuyến mãi. Giá khuyến mãi có hiệu lực "
                + "trong khoảng thời gian đã đặt.";
    } else if ("rejected".equals(message)) {
        messageText = "Đã từ chối khuyến mãi. Chủ shop sẽ thấy lý do.";
    } else if ("cancelled".equals(message)) {
        messageText = "Đã hủy duyệt. Khuyến mãi quay về trạng thái chờ duyệt và không còn áp dụng.";
    } else if ("not-approved".equals(message)) {
        level = "error-message";
        messageText = "Chỉ hủy duyệt được khuyến mãi đã duyệt.";
    } else if ("approve-expired".equals(message)) {
        level = "error-message";
        messageText = "Khuyến mãi này đã kết thúc nên không thể "
                + "duyệt. Chủ shop cần gửi yêu cầu mới.";
    } else if ("approve-not-discount".equals(message)) {
        level = "error-message";
        messageText = "Giá khuyến mãi không còn thấp hơn giá gốc "
                + "nên không thể duyệt.";
    } else if ("not-pending".equals(message)) {
        level = "error-message";
        messageText = "Khuyến mãi này đã được xử lý trước đó.";
    } else if ("invalid".equals(message)) {
        level = "error-message";
        messageText = "Không tìm thấy khuyến mãi.";
    } else if ("failed".equals(message)) {
        level = "error-message";
        messageText = "Không lưu được quyết định.";
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Duyệt khuyến mãi - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/admin/dashboard">
            📊 Bảng điều khiển
        </a>

        <a href="<%= contextPath %>/admin/product-management">
            🍎 Sản phẩm
        </a>

        <a href="<%= contextPath %>/logout"
           class="logout">
            Đăng xuất
        </a>

    </nav>

</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="<%= contextPath %>/admin/dashboard">
            ← Bảng điều khiển
        </a>
    </p>

    <h1>Duyệt khuyến mãi</h1>

    <p class="dashboard-welcome">
        Xem các giá khuyến mãi do chủ shop gửi lên.
        Khuyến mãi chỉ có hiệu lực sau khi được duyệt tại đây.
    </p>

    <% if (messageText != null) { %>

        <div class="<%= level %>">
            <%= esc(messageText) %>
        </div>

    <% } %>

    <div class="summary-grid summary-grid-4">

        <div class="summary-card">
            <span class="summary-label">Tổng yêu cầu</span>
            <span class="summary-value"><%= esc(qty(totalCount)) %></span>
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
          action="<%= contextPath %>/admin/promotion-approvals"
          class="filter-form">

        <input type="text" class="filter-input" name="q"
               value="<%= esc(q) %>"
               placeholder="Tìm sản phẩm, SKU hoặc shop...">

        <select class="filter-select" name="status">
            <option value="">Tất cả trạng thái</option>
            <option value="pending" <%= "pending".equals(status) ? "selected" : "" %>>Chờ duyệt</option>
            <option value="approved" <%= "approved".equals(status) ? "selected" : "" %>>Đã duyệt</option>
            <option value="rejected" <%= "rejected".equals(status) ? "selected" : "" %>>Từ chối</option>
        </select>

        <button type="submit" class="btn-sm">Lọc</button>

        <a class="filter-clear"
           href="<%= contextPath %>/admin/promotion-approvals">Xóa lọc</a>

    </form>

    <div class="panel">

        <h2>🏷️ Yêu cầu khuyến mãi</h2>

        <p class="panel-hint">
            Yêu cầu đang chờ duyệt được hiển thị trước.
        </p>

        <div class="table-scroll">

            <table class="data-table">

                <thead>
                    <tr>
                        <th>Cửa hàng</th>
                        <th>Sản phẩm</th>
                        <th>SKU</th>
                        <th>Giá gốc</th>
                        <th>Giá khuyến mãi</th>
                        <th>Thời gian</th>
                        <th>Trạng thái</th>
                        <th>Quyết định</th>
                    </tr>
                </thead>

                <tbody>
                <% if (promotionsPage == null || promotionsPage.isEmpty()) { %>

                    <tr>
                        <td colspan="8">
                            Không có yêu cầu khuyến mãi nào khớp với tìm kiếm.
                        </td>
                    </tr>

                <% } else {
                       for (Map<String, Object> p : promotionsPage.getItems()) {

                           String label = statusLabel(p.get("status"));
                           String period = periodStatus(
                                   p.get("start_date"), p.get("end_date"));
                           boolean pending = "Chờ duyệt".equals(label);
                %>
                    <tr>
                        <td><%= esc(p.get("shop_name")) %></td>
                        <td><%= esc(p.get("product_name")) %></td>
                        <td><%= esc(p.get("sku")) %></td>
                        <td><%= money(p.get("price")) %></td>
                        <td>
                            <strong><%= money(p.get("promo_price")) %></strong>
                            <span class="pill pill-ok">
                                <%= esc(discount(p.get("price"), p.get("promo_price"))) %>
                            </span>
                        </td>
                        <td>
                            <%= esc(dateValue(p.get("start_date"))) %>
                            →
                            <%= esc(dateValue(p.get("end_date"))) %>
                            <br>
                            <span class="pill <%= periodPill(period) %>">
                                <%= period %>
                            </span>
                        </td>
                        <td>
                            <span class="pill <%= statusPill(label) %>">
                                <%= label %>
                            </span>

                            <% if ("Từ chối".equals(label)
                                    && p.get("review_note") != null) { %>
                                <span class="row-note">
                                    <%= esc(p.get("review_note")) %>
                                </span>
                            <% } %>
                        </td>
                        <td>
                            <% if (pending) { %>

                                <div class="table-actions">

                                    <form method="post"
                                          action="<%= contextPath %>/admin/promotion-approvals">

                                        <input type="hidden" name="action"
                                               value="approve">

                                        <input type="hidden" name="promoId"
                                               value="<%= esc(p.get("promo_id")) %>">

                                        <input type="hidden" name="status"
                                               value="<%= esc(status) %>">

                                        <input type="hidden" name="q"
                                               value="<%= esc(q) %>">

                                        <input type="hidden" name="page"
                                               value="<%= esc(currentPage) %>">

                                        <button type="submit" class="btn-sm">
                                            Duyệt
                                        </button>

                                    </form>

                                    <form method="post"
                                          action="<%= contextPath %>/admin/promotion-approvals">

                                        <input type="hidden" name="action"
                                               value="reject">

                                        <input type="hidden" name="promoId"
                                               value="<%= esc(p.get("promo_id")) %>">

                                        <input type="hidden" name="status"
                                               value="<%= esc(status) %>">

                                        <input type="hidden" name="q"
                                               value="<%= esc(q) %>">

                                        <input type="hidden" name="page"
                                               value="<%= esc(currentPage) %>">

                                        <input class="qty-input note-input" type="text"
                                               name="reviewNote" maxlength="255"
                                               placeholder="Lý do (không bắt buộc)">

                                        <button type="submit"
                                                class="btn-sm btn-sm-danger">
                                            Từ chối
                                        </button>

                                    </form>

                                </div>

                            <% } else if ("Đã duyệt".equals(label)) { %>

                                <div class="table-actions">

                                    <form method="post"
                                          action="<%= contextPath %>/admin/promotion-approvals"
                                          onsubmit="return confirm('Hủy duyệt khuyến mãi này? Giá khuyến mãi sẽ ngừng áp dụng.');">

                                        <input type="hidden" name="action"
                                               value="cancel">

                                        <input type="hidden" name="promoId"
                                               value="<%= esc(p.get("promo_id")) %>">

                                        <input type="hidden" name="status"
                                               value="<%= esc(status) %>">

                                        <input type="hidden" name="q"
                                               value="<%= esc(q) %>">

                                        <input type="hidden" name="page"
                                               value="<%= esc(currentPage) %>">

                                        <button type="submit" class="btn-sm btn-sm-secondary">
                                            Hủy duyệt
                                        </button>

                                    </form>

                                </div>

                            <% } else { %>

                                <span class="muted-note">
                                    Đã xử lý
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

        </div>

        <%= Pager.render(promotionsPage, pagerUrl, "page") %>

    </div>

</section>

</body>
</html>
