<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Quản lý Gói Đăng Ký | Shop Owner</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css"/>
</head>
<body class="plan-management-page">

<!-- Top Bar -->
<div class="topbar">
  <div>
    <h1>Quản lý Gói Đăng Ký</h1>
  </div>
  <div style="display: flex; gap: 10px; align-items: center;">
    <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" class="btn-create" style="margin: 0;">
      + Tạo Gói Mới
    </a>
    <a href="${pageContext.request.contextPath}/store-owner/dashboard" class="back-link">← Dashboard</a>
  </div>
</div>

<!-- Toast -->
<c:if test="${not empty sessionScope.toast_success}">
  <div class="toast success" id="toast">${sessionScope.toast_success}</div>
  <c:remove var="toast_success" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.toast_error}">
  <div class="toast error" id="toast">${sessionScope.toast_error}</div>
  <c:remove var="toast_error" scope="session"/>
</c:if>

<div class="container">

  <!-- Toolbar -->
  <div class="toolbar">
    <div class="filter-bar">
      <a href="?" class="tab all  ${empty param.status ? 'selected' : ''}">Tất cả (${total})</a>
      <a href="?status=active"   class="tab active   ${param.status == 'active'   ? 'selected' : ''}">Active</a>
      <a href="?status=draft"    class="tab draft    ${param.status == 'draft'    ? 'selected' : ''}">Draft</a>
      <a href="?status=inactive" class="tab inactive ${param.status == 'inactive' ? 'selected' : ''}">Inactive</a>
    </div>
    <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" class="btn-create">+ Tạo gói mới</a>
  </div>

  <!-- Plan Cards -->
  <c:choose>
    <c:when test="${empty plans}">
      <div class="empty">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px;">
          <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
          <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
        </svg>
        <p>Chưa có gói đăng ký nào.<br>Bắt đầu tạo gói combo trái cây định kỳ đầu tiên cho shop của bạn!</p>
        <div style="margin-top: 16px;">
          <a href="${pageContext.request.contextPath}/store-owner/subscription/plans?action=create" class="btn-create">
            + Tạo gói mới ngay
          </a>
        </div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="plan-grid">
        <c:forEach var="p" items="${plans}">
          <div class="plan-card ${p.status}">
            <!-- Badge status -->
            <span class="plan-badge badge-${p.status}">${p.statusLabel}</span>

            <div class="plan-name">${p.name}</div>
            <div class="plan-desc">${not empty p.description ? p.description : 'Chưa có mô tả'}</div>

            <div class="plan-meta">
              <span class="meta-chip">Chu kỳ: ${p.billingCycleLabel}</span>
              <span class="meta-chip">Thời hạn: ${p.durationDays} ngày</span>
            </div>

            <div class="plan-price">
              <fmt:formatNumber value="${p.price}" pattern="#,###"/> đ
            </div>
            <div class="plan-cycle">/ ${p.billingCycleLabel}</div>

            <div class="subscriber-info" style="margin-top:12px;">
              <strong>${p.subscriberCount}</strong> khách đang đăng ký
            </div>

            <div class="plan-actions">
              <!-- Sửa -->
              <a href="?action=edit&id=${p.planId}" class="btn-sm btn-edit">Sửa</a>

              <!-- Toggle Active/Inactive -->
              <c:choose>
                <c:when test="${p.status == 'active'}">
                  <button class="btn-sm btn-toggle-off"
                          onclick="confirmToggle(${p.planId}, 'deactivate', '${p.name}')">
                    Tạm dừng
                  </button>
                </c:when>
                <c:otherwise>
                  <button class="btn-sm btn-toggle-on"
                          onclick="confirmToggle(${p.planId}, 'activate', '${p.name}')">
                    Kích hoạt
                  </button>
                </c:otherwise>
              </c:choose>

              <!-- Xóa -->
              <button class="btn-sm btn-del"
                      onclick="confirmDelete(${p.planId}, '${p.name}', ${p.subscriberCount})">
                Xóa
              </button>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>

  <!-- Pagination -->
  <c:if test="${totalPages > 1}">
    <div class="pagination">
      <c:if test="${page > 1}">
        <a href="?status=${param.status}&page=${page-1}" class="pg-btn">← Trước</a>
      </c:if>
      <c:forEach begin="1" end="${totalPages}" var="pg">
        <a href="?status=${param.status}&page=${pg}"
           class="pg-btn ${pg == page ? 'cur' : ''}">${pg}</a>
      </c:forEach>
      <c:if test="${page < totalPages}">
        <a href="?status=${param.status}&page=${page+1}" class="pg-btn">Sau →</a>
      </c:if>
    </div>
  </c:if>

</div>

<!-- Confirm Modal -->
<div class="modal-overlay" id="confirmModal">
  <div class="modal">
    <h3 id="modalTitle">Xác nhận</h3>
    <p  id="modalDesc"></p>
    <div class="modal-actions">
      <button class="btn-cancel" onclick="closeModal()">Hủy</button>
      <form id="modalForm" method="post">
        <input type="hidden" name="id" id="modalId"/>
        <button type="submit" class="btn-confirm" id="modalBtn">Xác nhận</button>
      </form>
    </div>
  </div>
</div>

<script>
  const ctx = '${pageContext.request.contextPath}';

  function confirmDelete(id, name, subCount) {
    if (subCount > 0) {
      alert('Không thể xóa! Gói "' + name + '" đang có ' + subCount + ' khách đăng ký.');
      return;
    }
    document.getElementById('modalTitle').textContent = 'Xóa gói đăng ký';
    document.getElementById('modalDesc').textContent  = 'Bạn có chắc muốn xóa gói "' + name + '"? Hành động này không thể hoàn tác.';
    document.getElementById('modalId').value           = id;
    document.getElementById('modalForm').action        = ctx + '/store-owner/subscription/plans?action=delete';
    const btn = document.getElementById('modalBtn');
    btn.textContent = 'Xóa';
    btn.className   = 'btn-confirm danger';
    document.getElementById('confirmModal').classList.add('open');
  }

  function confirmToggle(id, action, name) {
    const isActivate = action === 'activate';
    document.getElementById('modalTitle').textContent = isActivate ? 'Kích hoạt gói' : 'Tạm dừng gói';
    document.getElementById('modalDesc').textContent  = isActivate
      ? 'Kích hoạt gói "' + name + '"? Khách hàng sẽ thấy và đăng ký được.'
      : 'Tạm dừng gói "' + name + '"? Khách mới sẽ không đăng ký được, khách cũ không ảnh hưởng.';
    document.getElementById('modalId').value = id;
    document.getElementById('modalForm').action = ctx + '/store-owner/subscription/plans?action=toggle';
    const btn = document.getElementById('modalBtn');
    btn.textContent = isActivate ? 'Kích hoạt' : 'Tạm dừng';
    btn.className   = 'btn-confirm ' + (isActivate ? 'warning' : 'warning');
    document.getElementById('confirmModal').classList.add('open');
  }

  function closeModal() {
    document.getElementById('confirmModal').classList.remove('open');
  }
  document.getElementById('confirmModal').addEventListener('click', e => {
    if (e.target === document.getElementById('confirmModal')) closeModal();
  });

  // Auto-hide toast
  const toast = document.getElementById('toast');
  if (toast) setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity .5s'; }, 3500);
</script>
</body>
</html>
