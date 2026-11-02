<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bộ Sưu Tập Hình Ảnh - ${product.productName} - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .images-container {
            width: 92%;
            max-width: 1200px;
            margin: 30px auto 60px;
        }
        .images-grid {
            display: grid;
            grid-template-columns: 360px 1fr;
            gap: 25px;
            align-items: start;
        }
        .images-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid #e1ebe0;
            box-shadow: 0 4px 15px rgba(0,0,0,0.03);
            padding: 24px;
        }
        .product-brief {
            display: flex;
            align-items: center;
            gap: 16px;
            background: #f7faf6;
            border: 1px solid #e2ece0;
            border-radius: 10px;
            padding: 14px 18px;
            margin-bottom: 24px;
        }
        .product-brief img {
            width: 60px;
            height: 60px;
            border-radius: 8px;
            object-fit: cover;
            border: 1px solid #ccd8c9;
        }
        .form-group {
            margin-bottom: 16px;
        }
        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-size: 13px;
            font-weight: 600;
            color: #333;
        }
        .form-control {
            width: 100%;
            padding: 9px 12px;
            border: 1px solid #ccd8c9;
            border-radius: 6px;
            font-size: 14px;
            box-sizing: border-box;
            outline: none;
            transition: border 0.2s;
        }
        .form-control:focus {
            border-color: #4caf50;
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
        .alert-error {
            background: #ffebee;
            color: #c62828;
            border: 1px solid #ffcdd2;
        }
        .preview-box {
            width: 100%;
            height: 180px;
            border: 1px dashed #ccd8c9;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #fbfdfb;
            overflow: hidden;
            margin-top: 10px;
        }
        .preview-box img {
            max-width: 100%;
            max-height: 100%;
            object-fit: cover;
        }
        .gallery-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 18px;
        }
        .gallery-item {
            background: #ffffff;
            border: 1px solid #e5ede3;
            border-radius: 10px;
            overflow: hidden;
            transition: all 0.2s;
            display: flex;
            flex-direction: column;
        }
        .gallery-item:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.06);
            border-color: #c8e6c9;
        }
        .gallery-thumb {
            width: 100%;
            height: 160px;
            object-fit: cover;
            background: #f5f5f5;
        }
        .gallery-info {
            padding: 12px;
            display: flex;
            flex-direction: column;
            gap: 8px;
            flex: 1;
            justify-content: space-between;
        }
        .badge-primary-img {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1px solid #c8e6c9;
            font-size: 11px;
            font-weight: bold;
            padding: 3px 8px;
            border-radius: 12px;
            display: inline-block;
            text-align: center;
        }
        .badge-secondary-img {
            background: #f5f5f5;
            color: #666;
            font-size: 11px;
            padding: 3px 8px;
            border-radius: 12px;
            display: inline-block;
            text-align: center;
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
        <a href="${pageContext.request.contextPath}/store-owner/products">Sản phẩm</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </div>
</header>

<div class="images-container">

    <!-- ALERTS -->
    <c:if test="${param.success == 'added'}">
        <div class="alert-box alert-success">
            <span>Đã thêm hình ảnh mới vào bộ sưu tập sản phẩm thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'primary'}">
        <div class="alert-box alert-success">
            <span>Đã đặt hình ảnh làm ảnh đại diện chính thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert-box alert-success">
            <span>Đã xóa hình ảnh khỏi bộ sưu tập thành công!</span>
        </div>
    </c:if>
    <c:if test="${not empty param.errorMsg}">
        <div class="alert-box alert-error">
            <span>${param.errorMsg}</span>
        </div>
    </c:if>

    <!-- HEADER -->
    <div class="page-header" style="margin-bottom: 18px;">
        <div>
            <h1 class="page-title">Quản Lý Thư Viện Hình Ảnh</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Thêm nhiều góc ảnh chất lượng cao cho hoa quả và thiết lập ảnh đại diện chính.</p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/store-owner/products" class="btn-secondary" style="padding: 10px 16px; border-radius: 8px; font-weight: bold;">
                Quay Lại Danh Sách
            </a>
        </div>
    </div>

    <!-- PRODUCT BRIEF BANNER -->
    <div class="product-brief">
        <img src="${not empty product.imageUrl ? product.imageUrl : 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500'}" 
             alt="${product.productName}"
             onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
        <div>
            <h2 style="font-size: 17px; color: #234d25; margin: 0 0 4px;">${product.productName} (#${product.productId})</h2>
            <div style="font-size: 13px; color: #666;">
                Danh mục: <strong>${product.categoryName}</strong> | Tổng số hình ảnh: <strong>${images.size()} ảnh</strong>
            </div>
        </div>
    </div>

    <!-- 2-COLUMN GRID -->
    <div class="images-grid">

        <!-- FORM ADD IMAGE -->
        <div class="images-card">
            <h3 style="font-size: 16px; color: #275629; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid #edf3eb;">
                Thêm Ảnh Mới
            </h3>

            <form action="${pageContext.request.contextPath}/store-owner/product-images" method="POST">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="productId" value="${product.productId}">

                <div class="form-group">
                    <label for="imageUrl">Đường dẫn ảnh (URL) <span style="color: red;">*</span></label>
                    <input type="url" id="imageUrl" name="imageUrl" class="form-control" 
                           placeholder="https://images.unsplash.com/photo-..." required oninput="previewImage(this.value)">
                </div>

                <div class="form-group">
                    <label>Xem trước hình ảnh</label>
                    <div class="preview-box">
                        <img id="imagePreview" src="https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500" 
                             alt="Xem trước ảnh" onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                    </div>
                </div>

                <div class="form-group">
                    <label for="sortOrder">Vai trò hình ảnh</label>
                    <select id="sortOrder" name="sortOrder" class="form-control">
                        <option value="2">Ảnh mô tả phụ (Thứ tự 2)</option>
                        <option value="1">Đặt ngay làm ảnh đại diện chính (Thứ tự 1)</option>
                    </select>
                </div>

                <button type="submit" class="btn-primary" style="width: 100%; padding: 11px; border-radius: 8px; font-weight: bold; margin-top: 10px;">
                    Lưu Hình Ảnh
                </button>
            </form>
        </div>

        <!-- GALLERY OF IMAGES -->
        <div class="images-card">
            <h3 style="font-size: 16px; color: #275629; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid #edf3eb;">
                Bộ Sưu Tập Ảnh Sản Phẩm (${images.size()})
            </h3>

            <div class="gallery-grid">
                <c:choose>
                    <c:when test="${empty images}">
                        <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #888;">
                            Sản phẩm này chưa có hình ảnh nào trong thư viện.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="img" items="${images}">
                            <div class="gallery-item">
                                <img src="${img.imageUrl}" alt="${product.productName}" class="gallery-thumb" 
                                     onerror="this.src='https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';">
                                <div class="gallery-info">
                                    <div style="display: flex; justify-content: space-between; align-items: center;">
                                        <c:choose>
                                            <c:when test="${img.sortOrder == 1}">
                                                <span class="badge-primary-img">Ảnh đại diện</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-secondary-img">Ảnh phụ #${img.sortOrder}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div style="display: flex; gap: 6px; margin-top: 6px;">
                                        <c:if test="${img.sortOrder != 1}">
                                            <a href="${pageContext.request.contextPath}/store-owner/product-images?action=setPrimary&imageId=${img.imageId}&productId=${product.productId}" 
                                               class="btn-sm" style="flex: 1; text-align: center; justify-content: center; background: #e8f5e9; color: #2e7d32; font-size: 11px; padding: 5px 8px;"
                                               title="Đặt làm ảnh đại diện chính của sản phẩm">
                                                Đặt chính
                                            </a>
                                        </c:if>
                                        <a href="${pageContext.request.contextPath}/store-owner/product-images?action=delete&imageId=${img.imageId}&productId=${product.productId}" 
                                           class="btn-sm btn-delete" style="flex: 1; text-align: center; justify-content: center; font-size: 11px; padding: 5px 8px;"
                                           onclick="return confirm('Bạn có chắc chắn muốn xóa ảnh này khỏi bộ sưu tập?');"
                                           title="Xóa hình ảnh">
                                            Xóa ảnh
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

</div>

<script>
    function previewImage(url) {
        var img = document.getElementById('imagePreview');
        if (url && url.trim().length > 5) {
            img.src = url.trim();
        } else {
            img.src = 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=500';
        }
    }
</script>

</body>
</html>
