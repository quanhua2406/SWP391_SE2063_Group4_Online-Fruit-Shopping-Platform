<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thêm Sản Phẩm Mới - FreshFruit</title>
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
        <h1 class="page-title">Thêm Mới Sản Phẩm Trái Cây</h1>
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
            <input type="hidden" name="action" value="add">

            <!-- Tên sản phẩm & Danh mục -->
            <div class="form-grid-2">
                <div class="auth-form-group">
                    <label for="productName">Tên hoa quả <span class="required">*</span></label>
                    <input type="text" id="productName" name="productName" value="${productName}" 
                           placeholder="Vd: Nho Mẫu Đơn Shine Muscat..." required>
                </div>

                <div class="auth-form-group">
                    <label for="categoryId">Danh mục <span class="required">*</span></label>
                    <select id="categoryId" name="categoryId" class="filter-select" style="width: 100%; height: 45px;" required>
                        <option value="">-- Chọn danh mục --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.categoryId}" ${categoryId == cat.categoryId ? 'selected' : ''}>
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
                    <input type="text" id="origin" name="origin" value="${origin}" 
                           placeholder="Vd: Hàn Quốc, Đà Lạt, New Zealand...">
                </div>

                <div class="auth-form-group">
                    <label for="packagingType">Quy cách đóng gói</label>
                    <input type="text" id="packagingType" name="packagingType" value="${packagingType}" 
                           placeholder="Vd: Hộp 500g, Túi 1kg, Thùng 5kg...">
                </div>
            </div>

            <!-- Đơn giá, Đơn vị tính, Số lượng tồn -->
            <div class="form-grid-2" style="grid-template-columns: 1fr 1fr 1fr;">
                <div class="auth-form-group">
                    <label for="price">Giá bán (VNĐ) <span class="required">*</span></label>
                    <input type="number" id="price" name="price" value="${price}" 
                           placeholder="Vd: 250000" min="0" step="1000" required>
                </div>

                <div class="auth-form-group">
                    <label for="unit">Đơn vị tính</label>
                    <select id="unit" name="unit" class="filter-select" style="width: 100%; height: 45px;">
                        <option value="kg" ${unit == 'kg' ? 'selected' : ''}>Kilogram (kg)</option>
                        <option value="box" ${unit == 'box' ? 'selected' : ''}>Hộp (box)</option>
                        <option value="piece" ${unit == 'piece' ? 'selected' : ''}>Trái / Quả (piece)</option>
                    </select>
                </div>

                <div class="auth-form-group">
                    <label for="stock">Tồn kho ban đầu</label>
                    <input type="number" id="stock" name="stock" value="${stock}" 
                           placeholder="Vd: 50" min="0" step="1">
                </div>
            </div>

            <!-- Trạng thái hiển thị -->
            <div class="auth-form-group">
                <label for="status">Trạng thái phát hành</label>
                <select id="status" name="status" class="filter-select" style="width: 100%; height: 45px;">
                    <option value="published">Đang bán (Hiển thị ngay cho khách hàng)</option>
                    <option value="draft">Bản nháp (Chỉ lưu tạm)</option>
                    <option value="hidden">Tạm ẩn</option>
                </select>
            </div>

            <!-- Link ảnh & Preview -->
            <div class="auth-form-group">
                <label for="imageUrl">Đường dẫn hình ảnh (URL)</label>
                <div class="image-preview-container">
                    <input type="text" id="imageUrl" name="imageUrl" value="${imageUrl}" 
                           placeholder="https://images.unsplash.com/..." 
                           oninput="updatePreview(this.value)" style="flex: 1;">
                    <div class="preview-box">
                        <img id="imgPreview" src="${not empty imageUrl ? imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}" 
                             alt="Preview" onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                    </div>
                </div>
                <small style="color: #888;">Để trống sẽ sử dụng hình ảnh trái cây mặc định.</small>
            </div>

            <!-- Mô tả chi tiết -->
            <div class="auth-form-group">
                <label for="description">Mô tả sản phẩm</label>
                <textarea id="description" name="description" rows="4" 
                          style="width: 100%; padding: 12px; border: 1px solid #d9e2d7; border-radius: 8px; outline: none; font-family: inherit; font-size: 14px;"
                          placeholder="Nhập mô tả về độ tươi ngon, hương vị, chứng chỉ an toàn thực phẩm...">${description}</textarea>
            </div>

            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/store-owner/products" class="btn-secondary">Hủy bỏ</a>
                <button type="submit" class="btn-primary" style="padding: 12px 28px; font-size: 15px;">Lưu Vào Database</button>
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
