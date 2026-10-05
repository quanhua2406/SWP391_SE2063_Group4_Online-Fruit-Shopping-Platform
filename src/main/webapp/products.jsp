<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Sản phẩm - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .table-empty {
            text-align: center;
            padding: 40px !important;
            color: #888;
            font-size: 15px;
        }
        .price-text {
            font-weight: bold;
            color: #2e7d32;
        }
        .stock-badge {
            font-weight: 600;
            padding: 3px 8px;
            border-radius: 6px;
            background: #eef7ee;
            color: #256029;
            font-size: 13px;
        }
        .stock-low {
            background: #ffebee;
            color: #c62828;
        }
        .alert-box {
            padding: 12px 18px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            font-weight: 500;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .alert-success {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1px solid #c8e6c9;
        }
    </style>
</head>
<body>

<header class="navbar">
    <div class="logo">
        FreshFruit
    </div>
    <div class="nav-actions">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <a href="${pageContext.request.contextPath}/store-owner/dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </div>
</header>

<div class="product-management-container">

    <!-- ALERT NOTIFICATIONS -->
    <c:if test="${param.success == 'added'}">
        <div class="alert-box alert-success">
            <span>Đã thêm sản phẩm trái cây mới thành công vào hệ thống!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert-box alert-success">
            <span>Cập nhật thông tin sản phẩm thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert-box alert-success">
            <span>Đã xóa sản phẩm khỏi danh sách thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'status'}">
        <div class="alert-box alert-success">
            <span>Đã cập nhật trạng thái hiển thị của sản phẩm!</span>
        </div>
    </c:if>

    <!-- HEADER & ACTIONS -->
    <div class="page-header">
        <div>
            <h1 class="page-title">Quản Lý Sản Phẩm Trái Cây</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Xem danh sách, cập nhật thông tin giá bán và số lượng tồn kho sản phẩm.</p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/store-owner/products?action=add" class="btn-add">
                Thêm Sản Phẩm Mới
            </a>
        </div>
    </div>

    <!-- FILTER TOOLBAR -->
    <form action="${pageContext.request.contextPath}/store-owner/products" method="GET" class="filter-toolbar">
        <input type="hidden" name="action" value="list">
        
        <div class="filter-group">
            <input type="text" name="keyword" value="${keyword}" placeholder="Tìm theo tên hoặc xuất xứ..." class="filter-input" style="min-width: 250px;">
            
            <select name="categoryId" class="filter-select">
                <option value="">-- Tất cả danh mục --</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.categoryId}" ${selectedCategory == cat.categoryId ? 'selected' : ''}>
                        ${cat.categoryName}
                    </option>
                </c:forEach>
            </select>

            <select name="status" class="filter-select">
                <option value="all" ${selectedStatus == 'all' ? 'selected' : ''}>-- Tất cả trạng thái --</option>
                <option value="published" ${selectedStatus == 'published' ? 'selected' : ''}>Đang bán (Published)</option>
                <option value="hidden" ${selectedStatus == 'hidden' ? 'selected' : ''}>Tạm ẩn (Hidden)</option>
                <option value="draft" ${selectedStatus == 'draft' ? 'selected' : ''}>Bản nháp (Draft)</option>
            </select>

            <button type="submit" class="btn-primary" style="padding: 9px 16px; border-radius: 8px;">Lọc</button>
            <a href="${pageContext.request.contextPath}/store-owner/products" class="btn-secondary" style="padding: 9px 14px; font-size: 13px;">Đặt lại</a>
        </div>

        <div style="font-size: 13px; color: #777;">
            Tổng cộng: <strong>${products != null ? products.size() : 0}</strong> sản phẩm
        </div>
    </form>

    <!-- PRODUCT TABLE -->
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width: 50px;">ID</th>
                    <th style="width: 70px;">Hình ảnh</th>
                    <th>Tên sản phẩm</th>
                    <th>Danh mục</th>
                    <th>Xuất xứ</th>
                    <th>Giá niêm yết</th>
                    <th>Tồn kho</th>
                    <th>Trạng thái</th>
                    <th style="width: 170px; text-align: center;">Hành động</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty products}">
                        <tr>
                            <td colspan="9" class="table-empty">
                                Không tìm thấy sản phẩm trái cây nào phù hợp với bộ lọc.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="p" items="${products}">
                            <tr>
                                <td>#${p.productId}</td>
                                <td>
                                    <img src="${not empty p.imageUrl ? p.imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}" 
                                         alt="${p.productName}" 
                                         class="product-thumb"
                                         onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                                </td>
                                <td>
                                    <strong style="color: #2e532f; font-size: 14px;">${p.productName}</strong>
                                    <div style="font-size: 12px; color: #888;">${p.packagingType} (${p.unit})</div>
                                </td>
                                <td>${p.categoryName}</td>
                                <td>${not empty p.origin ? p.origin : 'Chưa cập nhật'}</td>
                                <td>
                                    <span class="price-text">
                                        <fmt:formatNumber value="${p.price}" pattern="#,###"/> đ
                                    </span>
                                    <span style="font-size: 11px; color: #777;">/${p.unit}</span>
                                </td>
                                <td>
                                    <span class="stock-badge ${p.stock <= 5 ? 'stock-low' : ''}">
                                        <fmt:formatNumber value="${p.stock}" pattern="#,###"/> ${p.unit}
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.status == 'published'}">
                                            <span class="badge badge-published">Đang bán</span>
                                        </c:when>
                                        <c:when test="${p.status == 'hidden'}">
                                            <span class="badge badge-hidden">Đã ẩn</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-draft">Bản nháp</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: center;">
                                    <div class="action-btns" style="justify-content: center;">
                                        <a href="${pageContext.request.contextPath}/store-owner/products?action=edit&id=${p.productId}" 
                                           class="btn-sm btn-edit" title="Sửa thông tin">
                                            Sửa
                                        </a>
                                        <a href="${pageContext.request.contextPath}/store-owner/products?action=delete&id=${p.productId}" 
                                           class="btn-sm btn-delete" 
                                           onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm #${p.productId} - ${p.productName}?');" 
                                           title="Xóa sản phẩm">
                                            Xóa
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>
