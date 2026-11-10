<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý biến thể - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .variant-layout {
            display: block;
        }
        .variant-layout .panel {
            margin-bottom: 0;
        }
        .modal-overlay {
            position: fixed !important;
            inset: 0 !important;
            top: 0 !important;
            left: 0 !important;
            width: 100vw !important;
            height: 100vh !important;
            background: rgba(15, 23, 42, 0.75) !important;
            backdrop-filter: blur(5px);
            -webkit-backdrop-filter: blur(5px);
            display: flex !important;
            align-items: center;
            justify-content: center;
            z-index: 99999 !important;
            padding: 16px;
            box-sizing: border-box;
        }
        .modal-overlay[hidden] {
            display: none !important;
        }
        .modal-card {
            background-color: #ffffff !important;
            color: #1e293b !important;
            border-radius: 16px;
            padding: 24px 28px;
            width: 100%;
            max-width: 480px;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5) !important;
            border: 1px solid #e2e8f0;
            position: relative;
            z-index: 100000;
        }
        .modal-card-sm {
            max-width: 380px;
        }
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 1px solid #f1f5f9;
        }
        .modal-title {
            font-size: 18px;
            font-weight: 700;
            color: #234d25;
            margin: 0 0 4px 0;
        }
        .modal-subtitle {
            font-size: 13px;
            color: #64748b;
            margin: 0;
        }
        .modal-close {
            background: transparent;
            border: none;
            font-size: 24px;
            line-height: 1;
            color: #64748b;
            cursor: pointer;
            padding: 2px 6px;
            border-radius: 6px;
            margin-left: 12px;
        }
        .modal-close:hover {
            color: #0f172a;
            background: #f1f5f9;
        }
        .modal-card .form-field {
            margin-bottom: 14px;
        }
        .modal-card .form-field label {
            display: block;
            margin-bottom: 5px;
            font-size: 13px;
            font-weight: 600;
            color: #334155;
        }
        .modal-card .form-field input,
        .modal-card .form-field select {
            width: 100%;
            padding: 9px 12px;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            font-size: 14px;
            background-color: #ffffff;
            color: #0f172a;
            box-sizing: border-box;
        }
        .modal-card .form-field input:focus,
        .modal-card .form-field select:focus {
            outline: none;
            border-color: #2e7d32;
            box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.15);
        }
        .modal-card .form-field small {
            display: block;
            margin-top: 4px;
            color: #64748b;
            font-size: 12px;
        }
        .modal-card .form-grid-2 {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }
        .modal-card .form-actions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            margin-top: 20px;
            padding-top: 14px;
            border-top: 1px solid #f1f5f9;
        }
        @media (prefers-color-scheme: dark) {
            .modal-card {
                background-color: #1e293b !important;
                color: #f8fafc !important;
                border-color: #334155 !important;
            }
            .modal-header {
                border-bottom-color: #334155 !important;
            }
            .modal-title {
                color: #4ade80 !important;
            }
            .modal-subtitle {
                color: #94a3b8 !important;
            }
            .modal-close {
                color: #94a3b8 !important;
            }
            .modal-close:hover {
                color: #f8fafc !important;
                background: #334155 !important;
            }
            .modal-card .form-field label {
                color: #cbd5e1 !important;
            }
            .modal-card .form-field input,
            .modal-card .form-field select {
                background-color: #0f172a !important;
                color: #f8fafc !important;
                border-color: #475569 !important;
            }
            .modal-card .form-field small {
                color: #94a3b8 !important;
            }
            .modal-card .form-actions {
                border-top-color: #334155 !important;
            }
        }
    </style>
</head>
<body>

<header class="navbar">
    <div class="logo">
        🍊 FreshFruit
    </div>
    <nav class="nav-actions">
        <a href="${pageContext.request.contextPath}/store-owner/dashboard">📊 Bảng điều khiển</a>
        <a href="${pageContext.request.contextPath}/store-owner/products">🍎 Sản phẩm</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </nav>
</header>

<section class="dashboard">

    <p class="breadcrumb">
        <a href="${pageContext.request.contextPath}/store-owner/products">← Danh sách sản phẩm</a>
    </p>

    <h1>Quản lý biến thể</h1>

    <c:choose>
        <c:when test="${not empty product}">
            <p class="dashboard-welcome">
                <strong>${product.productName}</strong> (#${product.productId}) ·
                Danh mục: ${product.categoryName} ·
                Xuất xứ: ${not empty product.origin ? product.origin : 'Chưa cập nhật'}
            </p>
        </c:when>
        <c:otherwise>
            <p class="dashboard-welcome">
                Tất cả biến thể của các sản phẩm trong cửa hàng.
            </p>
        </c:otherwise>
    </c:choose>

    <c:if test="${param.success == 'added'}">
        <div class="success-message">Đã thêm biến thể mới cho sản phẩm.</div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="success-message">Đã cập nhật biến thể.</div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="success-message">Đã xóa biến thể.</div>
    </c:if>
    <c:if test="${not empty param.errorMsg}">
        <div class="error-message">${param.errorMsg}</div>
    </c:if>

    <div class="variant-layout">

        <!-- TABLE -->
        <div class="panel">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                <h2>Danh sách biến thể (${variants.size()})</h2>
                <button type="button" class="btn-primary" onclick="openAddForm()">+ Thêm biến thể</button>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>SKU</th>
                            <th>Quy cách</th>
                            <th>Phẩm cấp</th>
                            <th>Đơn vị</th>
                            <th>Khối lượng</th>
                            <th>Giá bán</th>
                            <th style="text-align: center;">Tồn kho</th>
                            <th style="text-align: center;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty variants}">
                                <tr>
                                    <td colspan="9">Chưa có biến thể nào.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="v" items="${variants}">
                                    <tr>
                                        <td>${v.productName}</td>
                                        <td><strong>${v.sku}</strong></td>
                                        <td>${v.packagingType}</td>
                                        <td>Loại ${v.qualityGrade}</td>
                                        <td>${v.unit}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${v.weightValue != null && v.weightValue > 0}">
                                                    ${v.weightValue} kg
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${v.price}" pattern="#,###"/> đ
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="stock-badge ${v.stock <= 5 ? 'stock-low' : ''}">
                                                <fmt:formatNumber value="${v.stock}" pattern="#,###"/>
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <div style="display: flex; gap: 6px; justify-content: center;">
                                                <button type="button" class="btn-sm btn-edit"
                                                        data-id="${v.variantId}"
                                                        data-sku="${fn:escapeXml(v.sku)}"
                                                        data-pkg="${fn:escapeXml(v.packagingType)}"
                                                        data-unit="${fn:escapeXml(v.unit)}"
                                                        data-weight="${v.weightValue}"
                                                        data-price="<fmt:formatNumber value="${v.price}" pattern="0"/>"
                                                        data-stock="<fmt:formatNumber value="${v.stock}" pattern="0"/>"
                                                        data-grade="${v.qualityGrade}"
                                                        data-product-id="${v.productId}"
                                                        onclick="handleEditBtn(this)">
                                                    Sửa
                                                </button>
                                                <a href="${pageContext.request.contextPath}/store-owner/variants?action=delete&variantId=${v.variantId}&productId=${v.productId}"
                                                   class="btn-sm btn-delete"
                                                   onclick="return confirm('Bạn có chắc muốn xóa biến thể ${fn:escapeXml(v.sku)} - ${fn:escapeXml(v.packagingType)}? Sản phẩm phải có ít nhất 1 biến thể.');">
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

</section>

    <!-- MODAL ADD / EDIT VARIANT -->
    <div id="formModal" class="modal-overlay" hidden onclick="if (event.target === this) closeForm();">
        <div class="modal-card">
            <div class="modal-header">
                <div>
                    <h2 id="formTitle" class="modal-title">Thêm biến thể mới</h2>
                    <p class="modal-subtitle">Mỗi biến thể là một quy cách đóng gói (hộp, túi, thùng...).</p>
                </div>
                <button type="button" class="modal-close" onclick="closeForm()" title="Đóng">&times;</button>
            </div>

            <form id="variantForm" action="${pageContext.request.contextPath}/store-owner/variants" method="POST">
                <input type="hidden" id="actionInput" name="action" value="add">
                <c:choose>
                    <c:when test="${not empty product}">
                        <input type="hidden" name="productId" value="${product.productId}">
                    </c:when>
                    <c:otherwise>
                        <div class="form-field">
                            <label for="productSelect">Sản phẩm *</label>
                            <select id="productSelect" name="productId" required>
                                <c:forEach var="sp" items="${shopProducts}">
                                    <option value="${sp.product_id}">${sp.product_name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:otherwise>
                </c:choose>
                <input type="hidden" id="variantIdInput" name="variantId" value="">

                <div class="form-field">
                    <label for="sku">Mã SKU *</label>
                    <input type="text" id="sku" name="sku" placeholder="VD: T-ENVY-1KG"
                           required maxlength="50" style="text-transform: uppercase;">
                    <small>Mã duy nhất, không trùng trong cửa hàng.</small>
                </div>

                <div class="form-field">
                    <label for="packagingType">Tên quy cách đóng gói *</label>
                    <input type="text" id="packagingType" name="packagingType"
                           placeholder="VD: Túi 1kg, Hộp 500g" required maxlength="50">
                </div>

                <div class="form-grid-2">
                    <div class="form-field">
                        <label for="unit">Đơn vị tính *</label>
                        <select id="unit" name="unit" required>
                            <option value="kg">kg</option>
                            <option value="box">hộp</option>
                            <option value="piece">quả</option>
                        </select>
                    </div>

                    <div class="form-field">
                        <label for="weightValue">Khối lượng (kg)</label>
                        <input type="number" step="0.01" min="0" id="weightValue" name="weightValue"
                               placeholder="VD: 1.0">
                    </div>
                </div>

                <div class="form-field">
                    <label for="qualityGrade">Phẩm cấp *</label>
                    <select id="qualityGrade" name="qualityGrade" required>
                        <c:forEach var="g" items="${qualityGrades}">
                            <option value="${g.grade}">${g.label}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-grid-2">
                    <div class="form-field">
                        <label for="price">Giá bán (đ) *</label>
                        <input type="number" step="1" min="0" id="price" name="price"
                               placeholder="VD: 220000" required>
                        <small>Nếu sản phẩm có giá/kg, giá tính tự động.</small>
                    </div>

                    <div class="form-field">
                        <label for="stock">Tồn kho ban đầu</label>
                        <input type="number" step="1" min="0" id="stock" name="stock"
                               placeholder="VD: 50" value="0">
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" id="cancelEditBtn" class="btn-secondary"
                            style="display: none;" onclick="closeForm();">
                        Hủy
                    </button>
                    <button type="submit" id="submitBtn" class="btn-primary">
                        Thêm biến thể
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- CONFIRM MODAL -->
    <div id="confirmModal" class="modal-overlay" hidden
         onclick="if (event.target === this) closeConfirm();">
        <div class="modal-card modal-card-sm">
            <div class="modal-header">
                <h3 class="modal-title">Xác nhận</h3>
                <button type="button" class="modal-close" onclick="closeConfirm()">&times;</button>
            </div>
            <p id="confirmMessage" class="modal-subtitle" style="margin-bottom: 16px;"></p>
            <div class="form-actions" style="margin-top: 12px; padding-top: 10px;">
                <button type="button" class="btn-secondary" onclick="closeConfirm()">Hủy</button>
                <a id="confirmOk" href="#" class="btn-sm btn-delete" style="text-decoration: none; padding: 6px 14px; border-radius: 6px;">Xóa</a>
            </div>
        </div>
    </div>

    <!-- DETAIL MODAL -->
    <div id="detailModal" class="modal-overlay" hidden
         onclick="if (event.target === this) closeDetail();">
        <div class="modal-card modal-card-sm">
            <div class="modal-header">
                <h3 class="modal-title">Hành động biến thể</h3>
                <button type="button" class="modal-close" onclick="closeDetail()">&times;</button>
            </div>
            <div id="detailBody"></div>
        </div>
    </div>

    <script>
        function handleEditBtn(btn) {
            var id = btn.getAttribute('data-id');
            var sku = btn.getAttribute('data-sku');
            var pkg = btn.getAttribute('data-pkg');
            var unit = btn.getAttribute('data-unit');
            var weight = btn.getAttribute('data-weight');
            var price = btn.getAttribute('data-price');
            var stock = btn.getAttribute('data-stock');
            var grade = btn.getAttribute('data-grade');
            var productId = btn.getAttribute('data-product-id');
            editVariant(id, sku, pkg, unit, weight, price, stock, grade, productId);
        }

        function editVariant(id, sku, pkg, unit, weight, price, stock, grade, productId) {
            var productSelect = document.getElementById('productSelect');
            if (productSelect) {
                productSelect.value = productId;
            }
            document.getElementById('formTitle').innerText = 'Sửa biến thể: ' + sku;
            document.getElementById('actionInput').value = 'edit';
            document.getElementById('variantIdInput').value = id;
            document.getElementById('sku').value = sku;
            document.getElementById('packagingType').value = pkg;
            document.getElementById('unit').value = unit;

            if (weight && weight !== 'null' && weight !== '') {
                var w = parseFloat(weight);
                document.getElementById('weightValue').value = isNaN(w) ? '' : w;
            } else {
                document.getElementById('weightValue').value = '';
            }

            if (price !== undefined && price !== null && price !== '') {
                var p = parseFloat(price);
                document.getElementById('price').value = isNaN(p) ? '' : Math.round(p);
            } else {
                document.getElementById('price').value = '';
            }

            if (stock !== undefined && stock !== null && stock !== '') {
                var s = parseFloat(stock);
                document.getElementById('stock').value = isNaN(s) ? '0' : Math.round(s);
            } else {
                document.getElementById('stock').value = '0';
            }

            document.getElementById('qualityGrade').value = grade || '1';

            document.getElementById('submitBtn').innerText = 'Lưu thay đổi';
            var cancelBtn = document.getElementById('cancelEditBtn');
            if (cancelBtn) cancelBtn.style.display = 'inline-block';
            document.getElementById('formModal').hidden = false;
            document.getElementById('packagingType').focus();
        }

        function openAddForm() {
            resetVariantForm();
            document.getElementById('formModal').hidden = false;
            document.getElementById('packagingType').focus();
        }

        function closeForm() {
            resetVariantForm();
            document.getElementById('formModal').hidden = true;
        }

        function resetVariantForm() {
            document.getElementById('formTitle').innerText = 'Thêm biến thể mới';
            document.getElementById('actionInput').value = 'add';
            document.getElementById('variantIdInput').value = '';
            document.getElementById('sku').value = '';
            document.getElementById('packagingType').value = '';
            document.getElementById('unit').value = 'kg';
            document.getElementById('weightValue').value = '';
            document.getElementById('price').value = '';
            document.getElementById('stock').value = '0';
            document.getElementById('qualityGrade').value = '1';
            document.getElementById('submitBtn').innerText = 'Thêm biến thể';
            var cancelBtn = document.getElementById('cancelEditBtn');
            if (cancelBtn) cancelBtn.style.display = 'none';
        }

        function openDetail(id) {
            var tpl = document.getElementById('detail-' + id);
            if (tpl) {
                document.getElementById('detailBody').replaceChildren(tpl.content.cloneNode(true));
                document.getElementById('detailModal').hidden = false;
            }
        }

        function closeDetail() {
            document.getElementById('detailModal').hidden = true;
        }

        function askConfirm(url, message) {
            document.getElementById('confirmMessage').innerText = message;
            document.getElementById('confirmOk').href = url;
            document.getElementById('confirmModal').hidden = false;
        }

        function closeConfirm() {
            document.getElementById('confirmModal').hidden = true;
        }

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                var formM = document.getElementById('formModal');
                if (formM && !formM.hidden) closeForm();
                var confM = document.getElementById('confirmModal');
                if (confM && !confM.hidden) closeConfirm();
                var detM = document.getElementById('detailModal');
                if (detM && !detM.hidden) closeDetail();
            }
        });
    </script>

</body>
</html>
