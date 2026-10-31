<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Danh Mục Trái Cây - FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .category-management-container {
            width: 92%;
            max-width: 1200px;
            margin: 30px auto 60px;
        }
        .cat-grid {
            display: grid;
            grid-template-columns: 380px 1fr;
            gap: 25px;
            align-items: start;
        }
        .cat-card {
            background: #ffffff;
            border-radius: 12px;
            border: 1px solid #e1ebe0;
            box-shadow: 0 4px 15px rgba(0,0,0,0.03);
            padding: 24px;
        }
        .form-group {
            margin-bottom: 18px;
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
            padding: 10px 12px;
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
        .count-badge {
            display: inline-block;
            padding: 4px 10px;
            background: #e8f5e9;
            color: #2e7d32;
            font-weight: bold;
            border-radius: 12px;
            font-size: 13px;
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

<div class="category-management-container">

    <!-- ALERT NOTIFICATIONS -->
    <c:if test="${param.success == 'added'}">
        <div class="alert-box alert-success">
            <span>Đã thêm danh mục trái cây mới thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert-box alert-success">
            <span>Cập nhật thông tin danh mục thành công!</span>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert-box alert-success">
            <span>Đã xóa danh mục thành công khỏi hệ thống!</span>
        </div>
    </c:if>
    <c:if test="${not empty param.errorMsg}">
        <div class="alert-box alert-error">
            <span>${param.errorMsg}</span>
        </div>
    </c:if>

    <!-- HEADER -->
    <div class="page-header" style="margin-bottom: 25px;">
        <div>
            <h1 class="page-title">Quản Lý Danh Mục Trái Cây</h1>
            <p style="color: #666; font-size: 14px; margin-top: 4px;">Tổ chức cây danh mục, phân loại nguồn gốc trái cây và kiểm tra số lượng sản phẩm liên kết.</p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/store-owner/products" class="btn-secondary" style="padding: 10px 16px; border-radius: 8px; font-weight: bold;">
                Quay Lại Sản Phẩm
            </a>
        </div>
    </div>

    <!-- 2-COLUMN GRID -->
    <div class="cat-grid">

        <!-- FORM ADD / EDIT CATEGORY -->
        <div class="cat-card">
            <h3 id="formTitle" style="font-size: 17px; color: #275629; margin-bottom: 18px; padding-bottom: 10px; border-bottom: 1px solid #edf3eb;">
                Thêm Danh Mục Mới
            </h3>

            <form id="categoryForm" action="${pageContext.request.contextPath}/store-owner/categories" method="POST" onsubmit="return validateCategoryForm();">
                <input type="hidden" id="actionInput" name="action" value="add">
                <input type="hidden" id="categoryIdInput" name="categoryId" value="">

                <div class="form-group">
                    <label for="categoryName">Tên danh mục <span style="color: red;">*</span></label>
                    <input type="text" id="categoryName" name="categoryName" class="form-control" 
                           placeholder="Ví dụ: Trái Cây Nhập Khẩu..." required maxlength="100">
                    <small style="color: #888; font-size: 12px; margin-top: 4px; display: block;">Từ 2 đến 100 ký tự, không được trùng lặp.</small>
                </div>

                <div class="form-group">
                    <label for="parentId">Danh mục cha (Phân cấp tùy chọn)</label>
                    <select id="parentId" name="parentId" class="form-control">
                        <option value="">-- Là danh mục gốc (Không có cha) --</option>
                        <c:forEach var="pCat" items="${parentOptions}">
                            <option value="${pCat.categoryId}">
                                ${pCat.categoryName} (#${pCat.categoryId})
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div style="display: flex; gap: 10px; margin-top: 22px;">
                    <button type="submit" id="submitBtn" class="btn-primary" style="flex: 1; padding: 11px; border-radius: 8px; font-weight: bold;">
                        Thêm Danh Mục
                    </button>
                    <button type="button" id="cancelEditBtn" class="btn-secondary" style="display: none; padding: 11px 16px; border-radius: 8px;" onclick="resetForm();">
                        Hủy
                    </button>
                </div>
            </form>
        </div>

        <!-- TABLE OF CATEGORIES -->
        <div class="cat-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; padding-bottom: 10px; border-bottom: 1px solid #edf3eb;">
                <h3 style="font-size: 17px; color: #275629; margin: 0;">
                    Danh Sách Danh Mục (${categories.size()})
                </h3>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th style="width: 50px;">ID</th>
                            <th>Tên danh mục</th>
                            <th>Danh mục cha</th>
                            <th style="text-align: center; width: 120px;">Số sản phẩm</th>
                            <th style="text-align: center; width: 140px;">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty categories}">
                                <tr>
                                    <td colspan="5" style="text-align: center; padding: 30px; color: #888;">
                                        Chưa có danh mục trái cây nào trong hệ thống.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="c" items="${categories}">
                                    <tr>
                                        <td>#${c.categoryId}</td>
                                        <td>
                                            <strong style="color: #2e532f; font-size: 14px;">${c.categoryName}</strong>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty c.parentName}">
                                                    <span style="color: #555; font-size: 13px;">${c.parentName}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #999; font-style: italic; font-size: 12px;">(Gốc)</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="count-badge">
                                                ${c.productCount} SP
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <div class="action-btns" style="justify-content: center;">
                                                <button type="button" class="btn-sm btn-edit" 
                                                        onclick="editCategory(${c.categoryId}, '${c.categoryName}', '${c.parentId}');"
                                                        title="Sửa danh mục">
                                                    Sửa
                                                </button>
                                                <a href="${pageContext.request.contextPath}/store-owner/categories?action=delete&id=${c.categoryId}" 
                                                   class="btn-sm btn-delete" 
                                                   onclick="return confirm('Bạn có chắc muốn xóa danh mục #${c.categoryId} - ${c.categoryName}? Lưu ý: Không thể xóa nếu đang có sản phẩm liên kết.');" 
                                                   title="Xóa danh mục">
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
    function editCategory(id, name, parentId) {
        document.getElementById('formTitle').innerText = 'Chỉnh Sửa Danh Mục #' + id;
        document.getElementById('actionInput').value = 'edit';
        document.getElementById('categoryIdInput').value = id;
        document.getElementById('categoryName').value = name;
        
        var parentSelect = document.getElementById('parentId');
        if (parentId && parentId !== 'null' && parentId !== '') {
            parentSelect.value = parentId;
        } else {
            parentSelect.value = '';
        }

        document.getElementById('submitBtn').innerText = 'Lưu Thay Đổi';
        document.getElementById('cancelEditBtn').style.display = 'inline-block';
        document.getElementById('categoryName').focus();
    }

    function resetForm() {
        document.getElementById('formTitle').innerText = 'Thêm Danh Mục Mới';
        document.getElementById('actionInput').value = 'add';
        document.getElementById('categoryIdInput').value = '';
        document.getElementById('categoryName').value = '';
        document.getElementById('parentId').value = '';
        document.getElementById('submitBtn').innerText = 'Thêm Danh Mục';
        document.getElementById('cancelEditBtn').style.display = 'none';
    }

    function validateCategoryForm() {
        var name = document.getElementById('categoryName').value.trim();
        if (name.length < 2) {
            alert('Tên danh mục phải có ít nhất 2 ký tự!');
            return false;
        }
        return true;
    }
</script>

</body>
</html>
