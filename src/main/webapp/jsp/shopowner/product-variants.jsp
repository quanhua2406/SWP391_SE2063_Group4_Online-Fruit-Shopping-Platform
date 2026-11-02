<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý biến thể - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .variant-layout {
            display: grid;
            grid-template-columns: 1fr;
            gap: 20px;
            align-items: start;
        }
        .variant-layout .panel {
            margin-bottom: 0;
        }
        .variant-form .form-field {
            margin-bottom: 14px;
        }
        .variant-form .form-field small {
            display: block;
            margin-top: 4px;
            color: #888;
            font-size: 12px;
        }
        .variant-form .form-actions {
            margin-top: 6px;
        }
        .detail-modal {
            position: fixed;
            inset: 0;
            background: rgba(0, 0, 0, 0.35);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        }
        .detail-modal[hidden] {
            display: none;
        }
        .detail-box {
            background: #ffffff;
            border-radius: 12px;
            padding: 20px 22px;
            min-width: 300px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        }
        .detail-head {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 14px;
        }
        .detail-title {
            font-size: 16px;
            color: #234d25;
            margin: 0 0 14px;
        }
        .detail-actions {
            justify-content: flex-start;
        }
        .detail-close {
            background: none;
            border: none;
            font-size: 22px;
            cursor: pointer;
            color: #666;
        }
        @media (max-width: 900px) {
            .variant-layout {
                grid-template-columns: 1fr;
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

        <!-- FORM ADD / EDIT (opened in a card) -->
        <div id="formModal" class="detail-modal" hidden
             onclick="if (event.target === this) closeForm();">
        <div class="panel variant-form" style="margin-bottom: 0; min-width: 340px; max-width: 420px;">
            <h2 id="formTitle">Thêm biến thể mới</h2>
            <p class="panel-hint">Mỗi biến thể là một gói bán riêng (hộp, túi, thùng...).</p>

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
                        <small>Nếu sản phẩm đã đặt giá/kg và biến thể có khối lượng, giá được tính tự động.</small>
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
                                            <button type="button" class="btn-sm btn-edit"
                                                    onclick="openDetail(${v.variantId})">
                                                Chi tiết
                                            </button>

                                            <template id="detail-${v.variantId}">
                                                <h3 class="detail-title">${v.sku} - ${v.packagingType}</h3>
                                                <div class="action-btns detail-actions">
                                                    <button type="button" class="btn-sm btn-edit"
                                                            onclick="closeDetail(); editVariant(${v.variantId}, '${v.sku}', '${v.packagingType}', '${v.unit}', '${v.weightValue}', ${v.price}, ${v.stock}, '${v.qualityGrade}', ${v.productId});">
                                                        Sửa
                                                    </button>
                                                    <a href="${pageContext.request.contextPath}/store-owner/variants?action=delete&variantId=${v.variantId}&productId=${v.productId}"
                                                       class="btn-sm btn-delete"
                                                       onclick="event.preventDefault(); closeDetail(); askConfirm(this.href, 'Xóa biến thể ${v.sku} - ${v.packagingType}? Sản phẩm phải có ít nhất 1 biến thể.');">
                                                        Xóa
                                                    </a>
                                                </div>
                                            </template>
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

<script>
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
        document.getElementById('weightValue').value = (weight && weight !== 'null') ? weight : '';
        document.getElementById('price').value = price;
        document.getElementById('stock').value = stock;
        document.getElementById('qualityGrade').value = grade || '1';

        document.getElementById('submitBtn').innerText = 'Lưu thay đổi';
        document.getElementById('cancelEditBtn').style.display = 'inline-block';
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
        document.getElementById('cancelEditBtn').style.display = 'none';
    }
</script>

    <div id="confirmModal" class="detail-modal" hidden
         onclick="if (event.target === this) closeConfirm();">
        <div class="detail-box">
            <div class="detail-head">
                <span>Xác nhận</span>
                <button type="button" class="detail-close" onclick="closeConfirm()">×</button>
            </div>
            <p id="confirmMessage" class="panel-hint"></p>
            <div class="action-btns detail-actions">
                <button type="button" class="btn-secondary" onclick="closeConfirm()">Hủy</button>
                <a id="confirmOk" href="#" class="btn-sm btn-delete">Xóa</a>
            </div>
        </div>
    </div>

    <div id="detailModal" class="detail-modal" hidden
         onclick="if (event.target === this) closeDetail();">
        <div class="detail-box">
            <div class="detail-head">
                <span>Hành động biến thể</span>
                <button type="button" class="detail-close" onclick="closeDetail()">×</button>
            </div>
            <div id="detailBody"></div>
        </div>
    </div>

    <script>
        function openDetail(id) {
            var tpl = document.getElementById('detail-' + id);
            document.getElementById('detailBody').replaceChildren(tpl.content.cloneNode(true));
            document.getElementById('detailModal').hidden = false;
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
    </script>

</body>
</html>
