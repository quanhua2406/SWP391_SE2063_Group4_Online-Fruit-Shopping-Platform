<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chỉnh Sửa Sản Phẩm #${product.productId} - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .image-preview-container {
            display: flex;
            align-items: center;
            gap: 15px;
            margin-top: 8px;
        }
        .preview-box {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            border: 2px dashed #ccd9ca;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            background: #fafdf9;
        }
        .preview-box img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .required {
            color: #d32f2f;
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
        <a href="${pageContext.request.contextPath}/store-owner/products">Danh sách sản phẩm</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </div>
</header>

<div class="product-management-container">

    <div class="page-header" style="max-width: 800px; margin: 0 auto 20px;">
        <div>
            <h1 class="page-title">Chỉnh Sửa Sản Phẩm #${product.productId}</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Cập nhật thông tin hoa quả, điều chỉnh đơn giá và số lượng kho.</p>
        </div>
        <a href="${pageContext.request.contextPath}/store-owner/products" class="btn-secondary" style="font-size: 14px;">
            ← Quay lại danh sách
        </a>
    </div>

    <div class="form-card">
        <c:if test="${not empty error}">
            <div class="error-message">
                ${error}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/store-owner/products" method="POST">
            <input type="hidden" name="action" value="edit">
            <input type="hidden" name="id" value="${product.productId}">

            <!-- Tên sản phẩm & Danh mục -->
            <div class="form-grid-2">
                <div class="auth-form-group">
                    <label for="productName">Tên hoa quả <span class="required">*</span></label>
                    <input type="text" id="productName" name="productName" value="${product.productName}" required>
                </div>

                <div class="auth-form-group">
                    <label for="categoryId">Danh mục <span class="required">*</span></label>
                    <select id="categoryId" name="categoryId" class="filter-select" style="width: 100%; height: 45px;" required>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.categoryId}" ${product.categoryId == cat.categoryId ? 'selected' : ''}>
                                ${cat.categoryName}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <!-- Xuất xứ & Quy cách đóng gói -->
            <div class="form-grid-2">
                <div class="auth-form-group">
                    <label for="origin">Nguồn gốc / Xuất xứ</label>
                    <input type="text" id="origin" name="origin" value="${product.origin}">
                </div>

                <div class="auth-form-group">
                    <label for="packagingType">Quy cách đóng gói</label>
                    <input type="text" id="packagingType" name="packagingType" value="${product.packagingType}">
                </div>
            </div>

            <!-- Đơn giá, Đơn vị tính, Số lượng tồn -->
            <div class="form-grid-2" style="grid-template-columns: 1fr 1fr 1fr;">
                <div class="auth-form-group">
                    <label for="price">Giá bán (VNĐ) <span class="required">*</span></label>
                    <input type="number" id="price" name="price" value="${product.price}" min="0" step="1000" required>
                </div>

                <div class="auth-form-group">
                    <label for="unit">Đơn vị tính</label>
                    <select id="unit" name="unit" class="filter-select" style="width: 100%; height: 45px;">
                        <option value="kg" ${product.unit == 'kg' ? 'selected' : ''}>Kilogram (kg)</option>
                        <option value="box" ${product.unit == 'box' ? 'selected' : ''}>Hộp (box)</option>
                        <option value="piece" ${product.unit == 'piece' ? 'selected' : ''}>Trái / Quả (piece)</option>
                    </select>
                </div>

                <div class="auth-form-group">
                    <label for="stock">Tồn kho</label>
                    <input type="number" id="stock" name="stock" value="${product.stock}" min="0" step="1">
                </div>
            </div>

            <!-- Trạng thái hiển thị -->
            <div class="auth-form-group">
                <label for="status">Trạng thái phát hành</label>
                <select id="status" name="status" class="filter-select" style="width: 100%; height: 45px;">
                    <option value="published" ${product.status == 'published' ? 'selected' : ''}>Đang bán (Published)</option>
                    <option value="hidden" ${product.status == 'hidden' ? 'selected' : ''}>Tạm ẩn (Hidden)</option>
                    <option value="draft" ${product.status == 'draft' ? 'selected' : ''}>Bản nháp (Draft)</option>
                </select>
            </div>

            <!-- Link ảnh & Preview -->
            <div class="auth-form-group">
                <label for="imageUrl">Đường dẫn hình ảnh (URL)</label>
                <div class="image-preview-container">
                    <input type="text" id="imageUrl" name="imageUrl" value="${product.imageUrl}" 
                           oninput="updatePreview(this.value)" style="flex: 1;">
                    <div class="preview-box">
                        <img id="imgPreview" src="${not empty product.imageUrl ? product.imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}" 
                             alt="Preview" onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                    </div>
                </div>
            </div>

            <!-- Mô tả chi tiết -->
            <div class="auth-form-group">
                <label for="description">Mô tả sản phẩm</label>
                <textarea id="description" name="description" rows="4" 
                          style="width: 100%; padding: 12px; border: 1px solid #d9e2d7; border-radius: 8px; outline: none; font-family: inherit; font-size: 14px;">${product.description}</textarea>
            </div>

            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/store-owner/products" class="btn-secondary">Hủy bỏ</a>
                <button type="submit" class="btn-primary" style="padding: 12px 28px; font-size: 15px;">Cập Nhật Sản Phẩm</button>
            </div>

        </form>
    </div>

</div>

<script>
    function updatePreview(url) {
        var preview = document.getElementById('imgPreview');
        if (url && url.trim().length > 0) {
            preview.src = url.trim();
        } else {
            preview.src = 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';
        }
    }
</script>

</body>
</html>
