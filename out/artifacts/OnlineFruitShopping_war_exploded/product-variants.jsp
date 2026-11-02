<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quy Cách Đóng Gói - ${product.productName} - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .variant-container {
            width: 92%;
            max-width: 1200px;
            margin: 30px auto 60px;
        }
        .variant-grid {
            display: grid;
            grid-template-columns: 390px 1fr;
            gap: 25px;
            align-items: start;
        }
        .variant-card {
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
            margin-bottom: 15px;
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
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
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

<div class="variant-container">

    <!-- ALERTS -->
    <c:if test="${param.success == 'added'}">
        <div class="alert-box alert-success">
            <span>Đã thêm quy cách đóng gói mới cho sản phẩm thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert-box alert-success">
            <span>Cập nhật thông tin quy cách đóng gói thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert-box alert-success">
            <span>Đã xóa quy cách đóng gói khỏi hệ thống thành công!</span>
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
            <h1 class="page-title">Quản Lý Quy Cách & Giá Biến Thể</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Cấu hình các phân loại đóng gói khác nhau (Hộp 500g, Túi 1kg, Thùng 5kg) cho sản phẩm.</p>
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
                Danh mục: <strong>${product.categoryName}</strong> | Xuất xứ: <strong>${not empty product.origin ? product.origin : 'Chưa cập nhật'}</strong>
            </div>
        </div>
    </div>

    <!-- 2-COLUMN GRID -->
    <div class="variant-grid">

        <!-- FORM ADD / EDIT VARIANT -->
        <div class="variant-card">
            <h3 id="formTitle" style="font-size: 16px; color: #275629; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid #edf3eb;">
                Thêm Quy Cách Mới
            </h3>

            <form id="variantForm" action="${pageContext.request.contextPath}/store-owner/variants" method="POST">
                <input type="hidden" id="actionInput" name="action" value="add">
                <input type="hidden" name="productId" value="${product.productId}">
                <input type="hidden" id="variantIdInput" name="variantId" value="">

                <div class="form-group">
                    <label for="sku">Mã SKU định danh <span style="color: red;">*</span></label>
                    <input type="text" id="sku" name="sku" class="form-control" 
                           placeholder="Ví dụ: T-ENVY-1KG" required maxlength="50" style="text-transform: uppercase;">
                    <small style="color: #888; font-size: 12px; margin-top: 3px; display: block;">Mã duy nhất, không trùng lặp trong kho.</small>
                </div>

                <div class="form-group">
                    <label for="packagingType">Tên quy cách đóng gói <span style="color: red;">*</span></label>
                    <input type="text" id="packagingType" name="packagingType" class="form-control" 
                           placeholder="Ví dụ: Túi 1kg, Hộp 500g, Thùng 5kg" required maxlength="50">
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="unit">Đơn vị tính <span style="color: red;">*</span></label>
                        <select id="unit" name="unit" class="form-control" required>
                            <option value="kg">kg (Kilogram)</option>
                            <option value="box">box (Hộp / Khay)</option>
                            <option value="piece">piece (Quả / Trái)</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="weightValue">Khối lượng (kg)</label>
                        <input type="number" step="0.01" min="0" id="weightValue" name="weightValue" class="form-control" 
                               placeholder="VD: 1.0">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="price">Giá bán (VNĐ) <span style="color: red;">*</span></label>
                        <input type="number" step="1000" min="0" id="price" name="price" class="form-control" 
                               placeholder="VD: 220000" required>
                    </div>

                    <div class="form-group">
                        <label for="stock">Tồn kho ban đầu</label>
                        <input type="number" step="1" min="0" id="stock" name="stock" class="form-control" 
                               placeholder="VD: 50" value="0">
                    </div>
                </div>

                <div style="display: flex; gap: 10px; margin-top: 18px;">
                    <button type="submit" id="submitBtn" class="btn-primary" style="flex: 1; padding: 11px; border-radius: 8px; font-weight: bold;">
                        Thêm Quy Cách
                    </button>
                    <button type="button" id="cancelEditBtn" class="btn-secondary" style="display: none; padding: 11px 16px; border-radius: 8px;" onclick="resetVariantForm();">
                        Hủy
                    </button>
                </div>
            </form>
        </div>

        <!-- TABLE OF VARIANTS -->
        <div class="variant-card">
            <h3 style="font-size: 16px; color: #275629; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid #edf3eb;">
                Danh Sách Quy Cách (${variants.size()})
            </h3>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>SKU</th>
                            <th>Quy cách</th>
                            <th>Đơn vị</th>
                            <th>Khối lượng</th>
                            <th>Đơn giá</th>
                            <th style="text-align: center;">Tồn kho</th>
                            <th style="text-align: center; width: 130px;">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty variants}">
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 30px; color: #888;">
                                        Sản phẩm này chưa có quy cách đóng gói nào.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="v" items="${variants}">
                                    <tr>
                                        <td>
                                            <strong style="color: #275629; font-size: 13px;">${v.sku}</strong>
                                        </td>
                                        <td>
                                            <strong style="color: #2e532f;">${v.packagingType}</strong>
                                        </td>
                                        <td>${v.unit}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${v.weightValue != null && v.weightValue > 0}">
                                                    ${v.weightValue} kg
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #999;">-</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <strong style="color: #2e7d32;">
                                                <fmt:formatNumber value="${v.price}" pattern="#,###"/> đ
                                            </strong>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="stock-badge ${v.stock <= 5 ? 'stock-low' : ''}">
                                                <fmt:formatNumber value="${v.stock}" pattern="#,###"/>
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <div class="action-btns" style="justify-content: center;">
                                                <button type="button" class="btn-sm btn-edit" 
                                                        onclick="editVariant(${v.variantId}, '${v.sku}', '${v.packagingType}', '${v.unit}', '${v.weightValue}', ${v.price}, ${v.stock});"
                                                        title="Sửa quy cách">
                                                    Sửa
                                                </button>
                                                <a href="${pageContext.request.contextPath}/store-owner/variants?action=delete&variantId=${v.variantId}&productId=${product.productId}" 
                                                   class="btn-sm btn-delete" 
                                                   onclick="return confirm('Bạn có chắc muốn xóa quy cách ${v.sku} - ${v.packagingType}? Lưu ý: Sản phẩm phải có ít nhất 1 quy cách.');" 
                                                   title="Xóa quy cách">
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

    </div>

</div>

<script>
    function editVariant(id, sku, pkg, unit, weight, price, stock) {
        document.getElementById('formTitle').innerText = 'Chỉnh Sửa Quy Cách: ' + sku;
        document.getElementById('actionInput').value = 'edit';
        document.getElementById('variantIdInput').value = id;
        document.getElementById('sku').value = sku;
        document.getElementById('packagingType').value = pkg;
        document.getElementById('unit').value = unit;
        document.getElementById('weightValue').value = (weight && weight !== 'null') ? weight : '';
        document.getElementById('price').value = price;
        document.getElementById('stock').value = stock;

        document.getElementById('submitBtn').innerText = 'Lưu Thay Đổi';
        document.getElementById('cancelEditBtn').style.display = 'inline-block';
        document.getElementById('packagingType').focus();
    }

    function resetVariantForm() {
        document.getElementById('formTitle').innerText = 'Thêm Quy Cách Mới';
        document.getElementById('actionInput').value = 'add';
        document.getElementById('variantIdInput').value = '';
        document.getElementById('sku').value = '';
        document.getElementById('packagingType').value = '';
        document.getElementById('unit').value = 'kg';
        document.getElementById('weightValue').value = '';
        document.getElementById('price').value = '';
        document.getElementById('stock').value = '0';
        document.getElementById('submitBtn').innerText = 'Thêm Quy Cách';
        document.getElementById('cancelEditBtn').style.display = 'none';
    }
</script>

</body>
</html>
