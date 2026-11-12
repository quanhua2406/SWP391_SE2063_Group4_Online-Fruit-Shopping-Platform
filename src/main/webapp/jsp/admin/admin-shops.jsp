<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.mycompany.onlinefruitshopping.dao.shop.AdminShopDAO.ShopInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    private String statusLabel(String status) {
        if ("active".equalsIgnoreCase(status)) {
            return "Hoạt động";
        }

        if ("suspended".equalsIgnoreCase(status)) {
            return "Bị khóa";
        }

        if ("locked".equalsIgnoreCase(status)) {
            return "Đã khóa";
        }

        if ("pending".equalsIgnoreCase(status)) {
            return "Chờ duyệt";
        }

        return status;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    List<ShopInfo> shops =
            (List<ShopInfo>) request.getAttribute("shops");

    String keyword = request.getAttribute("keyword") == null
            ? ""
            : String.valueOf(request.getAttribute("keyword"));

    String selectedStatus =
            request.getAttribute("selectedStatus") == null
                    ? "ALL"
                    : String.valueOf(request.getAttribute("selectedStatus"));

    String error = request.getAttribute("error") == null
            ? ""
            : String.valueOf(request.getAttribute("error"));

    String message = request.getAttribute("message") == null
            ? ""
            : String.valueOf(request.getAttribute("message"));

    String contextPath = request.getContextPath();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Quản lý chủ cửa hàng - FreshFruit</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            color: #263238;
            background: #f5f7fa;
        }

        /* NAVBAR */
        .navbar {
            min-height: 70px;
            padding: 16px 5%;
            background: #fff;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
        }

        .logo {
            color: #2e7d32;
            font-size: 25px;
            font-weight: 700;
        }

        .nav-actions {
            display: flex;
            align-items: center;
            gap: 22px;
        }

        .nav-actions a {
            color: #374151;
            text-decoration: none;
            font-size: 14px;
        }

        .nav-actions a:hover {
            color: #2E7D32;
        }

        /* PAGE */
        .container {
            width: 94%;
            max-width: 1400px;
            margin: 35px auto;
        }

        .page-header {
            margin-bottom: 28px;
        }

        .page-header h1 {
            margin: 0 0 8px;
            color: #222;
            font-size: 30px;
        }

        .page-header p {
            margin: 0;
            color: #777;
            font-size: 14px;
        }

        /* COMMON BUTTON */
        .btn {
            min-height: 40px;
            padding: 0 16px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border: 0;
            border-radius: 7px;
            font-family: inherit;
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            white-space: nowrap;
        }

        .btn-search {
            min-height: 44px;
            padding: 0 24px;
            background: #ff7a00;
            color: #fff;
        }

        .btn-search:hover {
            background: #256628;
        }

        .btn-reset {
            min-height: 44px;
            background: #f1f1f1;
            color: #555;
        }

        .btn-reset:hover {
            background: #e5e5e5;
        }

        .btn-detail {
            background: #1976d2;
            color: #fff;
        }

        .btn-detail:hover {
            background: #1565c0;
        }

        .btn-block {
            background: #fee2e2;
            color: #991b1b;
        }

        .btn-block:hover {
            background: #fecaca;
        }

        .btn-unblock {
            background: #dcfce7;
            color: #166534;
        }

        .btn-unblock:hover {
            background: #bbf7d0;
        }

        /* NOTICES */
        .notice {
            padding: 14px 18px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-size: 14px;
        }

        .notice-error {
            background: #ffe8e8;
            color: #c62828;
            border: 1px solid #ffcaca;
        }

        .notice-success {
            background: #e8f7ed;
            color: #218838;
            border: 1px solid #c7ead2;
        }

        /* SEARCH CARD */
        .filter-card {
            padding: 22px;
            margin-bottom: 25px;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
        }

        .filter-heading {
            margin-bottom: 16px;
        }

        .filter-heading h2 {
            margin: 0;
            color: #333;
            font-size: 19px;
        }

        .filter-heading p {
            margin: 6px 0 0;
            color: #888;
            font-size: 13px;
        }

        .filter-form {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
        }

        .filter-form input,
        .filter-form select {
            height: 44px;
            padding: 0 14px;
            border: 1px solid #d8d8d8;
            border-radius: 8px;
            background: #fff;
            font-family: inherit;
            font-size: 14px;
        }

        .filter-form input {
            flex: 1;
            min-width: 220px;
        }

        .filter-form select {
            min-width: 180px;
        }

        .filter-form input:focus,
        .filter-form select:focus {
            outline: none;
            border-color: #ff7a00;
            box-shadow: 0 0 0 2px rgba(255, 122, 0, .1);
        }

        /* TABLE CARD */
        .table-card {
            overflow: hidden;
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
        }

        .table-heading {
            padding: 22px 24px;
            border-bottom: 1px solid #eee;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 15px;
        }

        .table-heading h2 {
            margin: 0;
            color: #333;
            font-size: 19px;
        }

        .table-heading p {
            margin: 6px 0 0;
            color: #888;
            font-size: 13px;
        }

        .table-scroll {
            width: 100%;
            overflow-x: auto;
        }

        table {
            width: 100%;
            min-width: 1050px;
            border-collapse: collapse;
        }

        thead {
            background: #fafafa;
        }

        th {
            padding: 15px 16px;
            color: #666;
            text-align: left;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            white-space: nowrap;
            border-bottom: 1px solid #eee;
        }

        td {
            padding: 16px;
            color: #444;
            font-size: 13px;
            vertical-align: middle;
            border-bottom: 1px solid #f0f0f0;
        }

        tbody tr:hover {
            background: #fffaf5;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        /* SHOP CELL */
        .shop-cell {
            display: flex;
            align-items: center;
            gap: 11px;
            min-width: 170px;
        }

        .avatar {
            width: 40px;
            height: 40px;
            flex: 0 0 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: #fff0e3;
            color: #ff7a00;
            font-size: 15px;
            font-weight: 700;
        }

        .shop-name {
            color: #333;
            font-weight: 600;
        }

        .shop-id {
            margin-top: 4px;
            color: #999;
            font-size: 12px;
        }

        .owner-email {
            color: #555;
        }

        /* STATUS BADGES */
        .status-badge {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 700;
            white-space: nowrap;
        }

        .status-active {
            background: #e8f7ed;
            color: #218838;
        }

        .status-suspended {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-locked {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-other {
            background: #f3f3f3;
            color: #777;
        }

        /* ACTIONS */
        .actions {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 7px;
        }

        .action-form {
            display: inline;
            margin: 0;
        }

        .empty-message {
            padding: 60px 20px !important;
            text-align: center;
            color: #888;
        }

        .empty-message strong {
            display: block;
            margin-bottom: 6px;
            color: #555;
            font-size: 16px;
        }

        .empty-message span {
            font-size: 13px;
        }

        /* RESPONSIVE */
        @media (max-width: 700px) {
            .navbar {
                align-items: flex-start;
                flex-direction: column;
                padding: 18px 5%;
            }

            .nav-actions {
                gap: 16px;
            }

            .container {
                width: 95%;
                margin: 25px auto;
            }

            .page-header h1 {
                font-size: 25px;
            }

            .filter-form {
                align-items: stretch;
                flex-direction: column;
            }

            .filter-form input,
            .filter-form select,
            .filter-form .btn {
                width: 100%;
            }

            .table-heading {
                align-items: flex-start;
                flex-direction: column;
            }
        }
    </style>
</head>

<body>

<header class="navbar">
    <div class="logo">🍊 FreshFruit</div>

    <nav class="nav-actions">
        <a href="<%=contextPath%>/admin/dashboard">Bảng điều khiển</a>
        <a href="<%=contextPath%>/admin/shops?action=dashboard">Bảng điều khiển cửa hàng</a>
        <a href="<%=contextPath%>/admin/shops">Cửa hàng</a>
        <a href="<%=contextPath%>/admin/user-management">Người dùng</a>
        <a href="<%=contextPath%>/logout">Đăng xuất</a>
    </nav>
</header>

<main class="container">

    <section class="page-header">
        <h1>Quản lý chủ cửa hàng</h1>
        <p>Xem các cửa hàng đã đăng ký và khóa hoặc mở khóa cửa hàng của chủ cửa hàng.</p>
    </section>

    <% if (!error.isEmpty()) { %>
        <div class="notice notice-error" role="alert">
            <%=escapeHtml(error)%>
        </div>
    <% } %>

    <% if (!message.isEmpty()) { %>
        <div class="notice notice-success" role="status">
            <%=escapeHtml(message)%>
        </div>
    <% } %>

    <!-- SEARCH AND FILTER -->
    <section class="filter-card">

        <div class="filter-heading">
            <h2>Tìm kiếm cửa hàng</h2>
            <p>Tìm theo tên cửa hàng hoặc email chủ cửa hàng và lọc theo trạng thái cửa hàng.</p>
        </div>

        <form class="filter-form"
              method="get"
              action="<%=contextPath%>/admin/shops">

            <input type="hidden" name="action" value="search">

            <input type="text"
                   name="keyword"
                   value="<%=escapeHtml(keyword)%>"
                   placeholder="Tìm theo tên cửa hàng hoặc email chủ cửa hàng..."
                   aria-label="Tìm cửa hàng">

            <select name="status" aria-label="Lọc theo trạng thái cửa hàng">

                <option value="ALL"
                    <%= "ALL".equalsIgnoreCase(selectedStatus)
                            || selectedStatus.isEmpty() ? "selected" : "" %>>
                    Tất cả trạng thái
                </option>

                <option value="active"
                    <%= "ACTIVE".equalsIgnoreCase(selectedStatus)
                            ? "selected" : "" %>>
                    Đang hoạt động
                </option>

                <option value="suspended"
                    <%= "SUSPENDED".equalsIgnoreCase(selectedStatus)
                            ? "selected" : "" %>>
                    Bị khóa
                </option>

                <option value="pending"
                    <%= "PENDING".equalsIgnoreCase(selectedStatus)
                            ? "selected" : "" %>>
                    Chờ duyệt
                </option>

            </select>

            <button type="submit" class="btn btn-search">
                Tìm kiếm
            </button>

            <a class="btn btn-reset"
               href="<%=contextPath%>/admin/shops">
                Đặt lại
            </a>

        </form>
    </section>

    <!-- SHOP LIST -->
    <section class="table-card">

        <div class="table-heading">
            <div>
                <h2>Danh sách cửa hàng</h2>
                <p>Xem hồ sơ cửa hàng hoặc khóa và mở khóa toàn bộ cửa hàng.</p>
            </div>
        </div>

        <div class="table-scroll">

            <table>

                <thead>
                    <tr>
                        <th>Cửa hàng</th>
                        <th>Chủ cửa hàng</th>
                        <th>Sản phẩm</th>
                        <th>Ngày tạo</th>
                        <th>Trạng thái cửa hàng</th>
                        <th>Trạng thái chủ</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>

                <tbody>

                <%
                    if (shops != null && !shops.isEmpty()) {

                        for (ShopInfo shop : shops) {

                            String shopStatus = shop.getStatus() == null
                                    ? ""
                                    : shop.getStatus();

                            String ownerStatus = shop.getOwnerStatus() == null
                                    ? ""
                                    : shop.getOwnerStatus();

                            boolean suspended =
                                    "suspended".equalsIgnoreCase(shopStatus);

                            String shopStatusClass = suspended
                                    ? "status-suspended"
                                    : "active".equalsIgnoreCase(shopStatus)
                                        ? "status-active"
                                        : "status-other";

                            String ownerStatusClass =
                                    "active".equalsIgnoreCase(ownerStatus)
                                        ? "status-active"
                                        : "locked".equalsIgnoreCase(ownerStatus)
                                            ? "status-locked"
                                            : "status-other";

                            String shopName = shop.getShopName() == null
                                    || shop.getShopName().isBlank()
                                        ? "Cửa hàng"
                                        : shop.getShopName();

                            String avatar = shopName
                                    .substring(0, 1)
                                    .toUpperCase(Locale.ROOT);

                            String created = shop.getCreatedAt() == null
                                    ? "-"
                                    : dateFormat.format(shop.getCreatedAt());
                %>

                    <tr>

                        <!-- SHOP -->
                        <td>
                            <div class="shop-cell">

                                <div class="avatar">
                                    <%=escapeHtml(avatar)%>
                                </div>

                                <div>
                                    <div class="shop-name">
                                        <%=escapeHtml(shopName)%>
                                    </div>

                                    <div class="shop-id">
                                        Mã #<%=shop.getShopId()%>
                                    </div>
                                </div>

                            </div>
                        </td>

                        <!-- OWNER -->
                        <td>
                            <div class="shop-name">
                                <%=escapeHtml(shop.getOwnerName())%>
                            </div>

                            <div class="owner-email">
                                <%=escapeHtml(shop.getOwnerEmail())%>
                            </div>
                        </td>

                        <!-- PRODUCTS -->
                        <td>
                            <%=shop.getPublishedCount()%> đã đăng /
                            <%=shop.getProductCount()%> tổng
                        </td>

                        <!-- CREATED -->
                        <td><%=escapeHtml(created)%></td>

                        <!-- SHOP STATUS -->
                        <td>
                            <span class="status-badge <%=shopStatusClass%>">
                                <%=shopStatus.isBlank()
                                        ? "Không rõ"
                                        : escapeHtml(statusLabel(shopStatus))%>
                            </span>
                        </td>

                        <!-- OWNER STATUS -->
                        <td>
                            <span class="status-badge <%=ownerStatusClass%>">
                                <%=ownerStatus.isBlank()
                                        ? "Không rõ"
                                        : escapeHtml(statusLabel(ownerStatus))%>
                            </span>
                        </td>

                        <!-- ACTIONS -->
                        <td>
                            <div class="actions">

                                <a class="btn btn-detail"
                                   href="<%=contextPath%>/admin/shops?action=detail&amp;shopId=<%=shop.getShopId()%>">
                                    Xem
                                </a>

                                <% if (suspended) { %>

                                    <form class="action-form"
                                          method="post"
                                          action="<%=contextPath%>/admin/shops"
                                          onsubmit="return confirm('Mở khóa cửa hàng này và đăng lại sản phẩm?');">

                                        <input type="hidden" name="action" value="unblock">
                                        <input type="hidden" name="shopId"
                                               value="<%=shop.getShopId()%>">

                                        <button type="submit" class="btn btn-unblock">
                                            Mở khóa
                                        </button>

                                    </form>

                                <% } else { %>

                                    <form class="action-form"
                                          method="post"
                                          action="<%=contextPath%>/admin/shops"
                                          onsubmit="return askBlockReason(this);">

                                        <input type="hidden" name="action" value="block">
                                        <input type="hidden" name="shopId"
                                               value="<%=shop.getShopId()%>">
                                        <input type="hidden" name="reason" value="">

                                        <button type="submit" class="btn btn-block">
                                            Khóa
                                        </button>

                                    </form>

                                <% } %>

                            </div>
                        </td>

                    </tr>

                <%
                        }

                    } else {
                %>

                    <tr>
                        <td colspan="7" class="empty-message">

                            <strong>Không tìm thấy cửa hàng</strong>

                            <span>
                                Thử từ khóa hoặc bộ lọc trạng thái cửa hàng khác.
                            </span>

                        </td>
                    </tr>

                <%
                    }
                %>

                </tbody>

            </table>

        </div>
    </section>

</main>

<script>
    function askBlockReason(form) {
        var reason = window.prompt("Lý do khóa cửa hàng (bắt buộc, tối đa 255 ký tự):", "");

        if (reason === null) {
            return false;
        }

        reason = reason.trim();

        if (reason === "") {
            window.alert("Vui lòng nhập lý do khóa cửa hàng.");
            return false;
        }

        if (reason.length > 255) {
            window.alert("Lý do khóa tối đa 255 ký tự.");
            return false;
        }

        if (!window.confirm("Khóa cửa hàng này? Chủ cửa hàng sẽ bị khóa và sản phẩm bị ẩn.")) {
            return false;
        }

        form.elements.reason.value = reason;
        return true;
    }
</script>

</body>
</html>
