<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.math.BigDecimal"%>

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

    private String num(Object value) {
        if (value == null) {
            return "0";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return number.stripTrailingZeros().toPlainString();
    }
%>

<%
    String contextPath = request.getContextPath();

    String ownerName = request.getAttribute("ownerName") == null
            ? "Shop Owner"
            : request.getAttribute("ownerName").toString();

    Boolean hasShopAttr = (Boolean) request.getAttribute("hasShop");
    boolean hasShop = hasShopAttr != null && hasShopAttr;

    String shopName = request.getAttribute("shopName") == null
            ? null
            : request.getAttribute("shopName").toString();

    String shopStatus = request.getAttribute("shopStatus") == null
            ? ""
            : request.getAttribute("shopStatus").toString();
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Bảng điều khiển chủ shop - FreshFruit</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css">
</head>

<body>

<!-- ==================== NAVBAR ==================== -->

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>

    <nav class="nav-actions">

        <a href="<%= contextPath %>/home">
            🏠 Trang chủ
        </a>

        <a href="<%= contextPath %>/shopowner/shop/deliveries" style="color: #16a34a; font-weight: 700;">Giao hàng &amp; Mã giảm giá</a>


        <a href="<%= contextPath %>/logout"
           class="logout">
            Đăng xuất
        </a>

    </nav>

</header>

<!-- ==================== DASHBOARD ==================== -->

<section class="dashboard">

    <h1>Bảng điều khiển chủ shop</h1>

    <p class="dashboard-welcome">
        Xin chào, <%= esc(ownerName) %>!
    </p>

    <% if ("created".equals(shopStatus)) { %>

        <div class="success-message">
            ✅ Cửa hàng của bạn đã được tạo thành công.
        </div>

    <% } else if ("exists".equals(shopStatus)) { %>

        <div class="success-message">
            ℹ️ Bạn đã có cửa hàng.
        </div>

    <% } %>

    <% if (!hasShop) { %>

        <div class="error-message">
            Bạn chưa có cửa hàng. Hãy tạo cửa hàng trước, sau đó
            bạn có thể quản lý sản phẩm, kho và giá bán.
        </div>

        <div class="dashboard-grid">

            <div class="dashboard-card">

                <div class="dashboard-icon">🏪</div>

                <h3>Tạo cửa hàng</h3>

                <p>
                    Thiết lập cửa hàng để bắt đầu quản lý
                    sản phẩm, kho và giá bán.
                </p>

                <a href="<%= contextPath %>/store-owner/create-shop">
                    + Tạo cửa hàng
                </a>

            </div>

        </div>

    <% } else { %>

        <p class="breadcrumb">
            🏪 Cửa hàng của bạn:
            <strong><%= esc(shopName) %></strong>
        </p>

        <%
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> stats =
                    (java.util.Map<String, Object>) request.getAttribute("stats");

            Object productCount = stats == null ? null : stats.get("productCount");
            Object variantCount = stats == null ? null : stats.get("variantCount");
            Object totalStock = stats == null ? null : stats.get("totalStock");
            Object lowStockCount = stats == null ? null : stats.get("lowStockCount");
            Object outOfStockCount = stats == null ? null : stats.get("outOfStockCount");
        %>

        <div class="summary-grid summary-grid-4">

            <div class="summary-card">
                <span class="summary-label">Sản phẩm</span>
                <span class="summary-value"><%= esc(num(productCount)) %></span>
            </div>

            <div class="summary-card">
                <span class="summary-label">Biến thể</span>
                <span class="summary-value"><%= esc(num(variantCount)) %></span>
            </div>

            <div class="summary-card">
                <span class="summary-label">Tổng tồn kho</span>
                <span class="summary-value"><%= esc(num(totalStock)) %></span>
            </div>

            <div class="summary-card <%= lowStockCount != null
                    && new BigDecimal(lowStockCount.toString()).signum() > 0
                    ? "summary-warn" : "" %>">
                <span class="summary-label">
                    Sắp hết / Hết hàng
                </span>
                <span class="summary-value">
                    <%= esc(num(lowStockCount)) %> /
                    <%= esc(num(outOfStockCount)) %>
                </span>
            </div>

        </div>

        <div class="dashboard-grid">

            <div class="dashboard-card">
                <div class="dashboard-icon">🚚</div>
                <h3>Quản lý giao hàng</h3>
                <p>Phân công nhân viên và theo dõi trạng thái giao hàng.</p>

                <a href="${pageContext.request.contextPath}/shopowner/shop/deliveries">
                    Mở quản lý giao hàng
                </a>
            </div>

            <!-- PRODUCT MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🍎</div>

                <h3>Quản lý sản phẩm</h3>

                <p>
                    Xem sản phẩm, trạng thái và phần hoa hồng nền tảng thu
                    trên mỗi đơn bán.
                </p>

                <a href="<%= contextPath %>/store-owner/commission">
                    Quản lý sản phẩm
                </a>

            </div>

            <!-- VARIANT MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">📏</div>

                <h3>Quản lý biến thể</h3>

                <p>
                    Thêm, sửa và xóa biến thể (quy cách đóng gói, SKU, khối lượng)
                    của từng sản phẩm.
                </p>

                <a href="<%= contextPath %>/store-owner/variants">
                    Quản lý biến thể
                </a>

            </div>

            <!-- STOCK MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">📦</div>

                <h3>Quản lý kho</h3>

                <p>
                    Xem và cập nhật tồn kho, nhập hàng, theo dõi
                    hàng sắp hết và xem lịch sử kho.
                </p>

                <a href="<%= contextPath %>/store-owner/inventory">
                    Quản lý kho
                </a>

            </div>

            <!-- PRICING MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">💲</div>

                <h3>Quản lý giá bán</h3>

                <p>
                    Đặt và sửa giá bán của từng
                    biến thể sản phẩm (khối lượng gói tính theo kg).
                </p>

                <a href="<%= contextPath %>/store-owner/pricing">
                    Quản lý giá bán
                </a>

            </div>

            <!-- COMMISSION REPORT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">💼</div>

                <h3>Báo cáo hoa hồng</h3>

                <p>
                    Xem phí nền tảng bạn phải trả, chia theo
                    từng sản phẩm và tỷ lệ hoa hồng.
                </p>

                <a href="<%= contextPath %>/store-owner/commission-report">
                    Xem báo cáo
                </a>

            </div>

            <!-- PROMOTION MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🎁</div>

                <h3>Quản lý khuyến mãi</h3>

                <p>Quản lý chương trình khuyến mãi cho sản phẩm của cửa hàng.</p>

                <a href="${pageContext.request.contextPath}/shopowner/shop/promotions">
                    Quản lý khuyến mãi
                </a>

            </div>

            <!-- COUPON MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🎟️</div>

                <h3>Quản lý mã giảm giá</h3>

                <p>
                    Tạo mã giảm giá và theo dõi lượt sử dụng.
                </p>

                <a href="${pageContext.request.contextPath}/shopowner/shop/coupons">
                    Quản lý mã giảm giá
                </a>

            </div>

            <!-- ORDER MANAGEMENT -->

            <div class="dashboard-card">

                <div class="dashboard-icon">📋</div>

                <h3>Quản lý đơn hàng</h3>

                <p>
                    Theo dõi và xử lý các đơn có
                    sản phẩm của bạn.
                </p>

                <a href="#">
                    Quản lý đơn hàng
                </a>

            </div>

            <!-- SUBSCRIPTION -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🔄</div>

                <h3>Gói đăng ký</h3>

                <p>
                    Cung cấp gói trái cây định kỳ và quản lý
                    các gói đăng ký.
                </p>

                <a href="#">
                    Quản lý gói đăng ký
                </a>

            </div>

            <!-- NOTIFICATIONS -->

            <div class="dashboard-card">

                <div class="dashboard-icon">🔔</div>

                <h3>Thông báo</h3>

                <p>
                    Gửi và xem thông báo cho
                    khách hàng.
                </p>

                <a href="#">
                    Quản lý thông báo
                </a>

            </div>

        </div>

    <% } %>

</section>

</body>
</html>
