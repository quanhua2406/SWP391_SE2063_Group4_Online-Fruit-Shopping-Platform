<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${mode == 'edit' ? 'Chỉnh sửa' : 'Tạo'} Gói Đăng Ký | Shop Owner</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css"/>
</head>
<body class="plan-management-page">

<div class="topbar">
  <h1>${mode == 'edit' ? '✏️ Chỉnh sửa Gói Đăng Ký' : '➕ Tạo Gói Đăng Ký Mới'}</h1>
  <a href="${pageContext.request.contextPath}/store-owner/subscription/plans">← Quay lại danh sách</a>
</div>

<div class="container container-form">

  <!-- Errors -->
  <c:if test="${not empty errors}">
    <div class="error-box">
      <ul>
        <c:forEach var="e" items="${errors}"><li>${e}</li></c:forEach>
      </ul>
    </div>
  </c:if>

  <form method="post"
        action="${pageContext.request.contextPath}/store-owner/subscription/plans?action=${mode}"
        id="planForm">

    <c:if test="${mode == 'edit'}">
      <input type="hidden" name="planId" value="${plan.planId}"/>
    </c:if>

    <div class="form-card">
      <h2>${mode == 'edit' ? 'Chỉnh sửa thông tin gói' : 'Thông tin gói đăng ký'}</h2>

      <!-- ── Thông tin cơ bản ── -->
      <div class="form-section">
        <div class="section-title">📋 Thông tin cơ bản</div>

        <div class="form-group">
          <label for="name">Tên gói <span class="req">*</span></label>
          <input type="text" id="name" name="name" maxlength="100"
                 value="${plan.name}" placeholder="VD: Combo Trái Cây Tuần" required/>
        </div>

        <div class="form-group">
          <label for="description">Mô tả</label>
          <textarea id="description" name="description" placeholder="Mô tả ngắn về gói...">${plan.description}</textarea>
        </div>

        <div class="form-group">
          <label for="benefits">Quyền lợi / Benefits</label>
          <textarea id="benefits" name="benefits"
                    placeholder='VD: ["Freeship","Ưu tiên giao hàng","Giảm 5% mỗi đơn"]'>${plan.benefits}</textarea>
          <div class="hint">Nhập dạng JSON array hoặc danh sách phân cách bằng dấu phẩy</div>
        </div>
      </div>

      <!-- ── Giá & Chu kỳ ── -->
      <div class="form-section">
        <div class="section-title">💰 Giá & Chu kỳ</div>
        <div class="form-row">
          <div class="form-group">
            <label for="price">Giá (VNĐ) <span class="req">*</span></label>
            <input type="number" id="price" name="price" min="0" step="1000"
                   value="${plan.price}" placeholder="VD: 150000" required/>
          </div>
          <div class="form-group">
            <label for="billingCycle">Chu kỳ thanh toán <span class="req">*</span></label>
            <select id="billingCycle" name="billingCycle" required>
              <option value="">-- Chọn chu kỳ --</option>
              <option value="weekly"    ${plan.billingCycle == 'weekly'    ? 'selected' : ''}>Hàng tuần</option>
              <option value="monthly"   ${plan.billingCycle == 'monthly'   ? 'selected' : ''}>Hàng tháng</option>
              <option value="quarterly" ${plan.billingCycle == 'quarterly' ? 'selected' : ''}>Hàng quý (3 tháng)</option>
              <option value="yearly"    ${plan.billingCycle == 'yearly'    ? 'selected' : ''}>Hàng năm</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="durationDays">Số ngày hiệu lực <span class="req">*</span></label>
            <input type="number" id="durationDays" name="durationDays" min="1"
                   value="${plan.durationDays > 0 ? plan.durationDays : ''}" placeholder="VD: 30"/>
            <div class="hint">Hiệu lực của mỗi kỳ đăng ký (tính bằng ngày)</div>
          </div>
          <div class="form-group">
            <label>Trạng thái</label>
            <div class="status-group">
              <label class="status-opt">
                <input type="radio" name="status" value="draft"
                       ${empty plan.status || plan.status == 'draft' ? 'checked' : ''}/>
                <span>📝 Bản nháp</span>
              </label>
              <label class="status-opt">
                <input type="radio" name="status" value="active"
                       ${plan.status == 'active' ? 'checked' : ''}/>
                <span>✅ Active</span>
              </label>
              <label class="status-opt">
                <input type="radio" name="status" value="inactive"
                       ${plan.status == 'inactive' ? 'checked' : ''}/>
                <span>⏸️ Inactive</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Sản phẩm trong combo ── -->
      <div class="form-section">
        <div class="items-header">
          <div class="section-title" style="margin-bottom:0">🛒 Sản phẩm trong combo</div>
          <button type="button" class="btn-add-item" onclick="addItem()">+ Thêm sản phẩm</button>
        </div>

        <c:if test="${empty availableVariants}">
          <div class="hint" style="color:#d97706; margin-bottom: 10px;">
            ⚠️ Shop của bạn chưa có sản phẩm hoạt động nào. Hãy thêm sản phẩm trước khi tạo gói combo.
          </div>
        </c:if>

        <!-- Hộp thống kê giá trị Combo trực quan -->
        <div class="combo-summary-box" id="comboSummaryBox">
          <div class="summary-item">
            <span class="summary-label">Tổng giá trị lẻ gốc</span>
            <span class="summary-val" id="comboTotalOriginal">0 đ</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Giá bán gói</span>
            <span class="summary-val" id="comboPlanPrice">0 đ</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Ưu đãi / Tiết kiệm</span>
            <span class="summary-val diff-saving" id="comboDiff">0 đ</span>
          </div>
        </div>

        <div id="itemsContainer">
          <c:choose>
            <c:when test="${not empty plan.items}">
              <c:forEach var="item" items="${plan.items}" varStatus="st">
                <div class="item-row" id="item-${st.index}">
                  <select name="variantId" required onchange="updateComboTotals()">
                    <option value="">-- Chọn sản phẩm --</option>
                    <c:forEach var="v" items="${availableVariants}">
                      <option value="${v.variantId}" data-price="${v.unitPrice}" ${v.variantId == item.variantId ? 'selected' : ''}>
                        ${v.productName} - ${v.variantName} (<fmt:formatNumber value="${v.unitPrice}" pattern="#,###"/> đ)
                      </option>
                    </c:forEach>
                  </select>
                  <input type="number" name="quantity" value="${item.quantity}" min="1" placeholder="SL" oninput="updateComboTotals()"/>
                  <span class="item-subtotal">
                    <fmt:formatNumber value="${item.unitPrice * item.quantity}" pattern="#,###"/> đ
                  </span>
                  <button type="button" class="btn-rm-item" onclick="removeItem(this)">✕</button>
                </div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="no-items" id="noItemsMsg">Chưa có sản phẩm nào. Nhấn "+ Thêm sản phẩm".</div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <!-- ── Actions ── -->
      <div class="form-actions">
        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans" class="btn-cancel">Hủy</a>
        <button type="submit" class="btn-save">
          ${mode == 'edit' ? '💾 Lưu thay đổi' : '🚀 Tạo gói'}
        </button>
      </div>
    </div>
  </form>
</div>

<script>
  let itemCount = ${not empty plan.items ? plan.items.size() : 0};

  // Danh sách biến thể nạp từ server
  const availableVariants = [
    <c:forEach var="v" items="${availableVariants}" varStatus="vs">
      {
        variantId: ${v.variantId},
        productName: "${v.productName}",
        variantName: "${v.variantName}",
        unitPrice: ${v.unitPrice}
      }${!vs.last ? ',' : ''}
    </c:forEach>
  ];

  function getVariantOptionsHtml() {
    let html = '<option value="">-- Chọn biến thể sản phẩm --</option>';
    for (const v of availableVariants) {
      const priceFmt = Number(v.unitPrice).toLocaleString('vi-VN');
      html += `<option value="${v.variantId}" data-price="${v.unitPrice}">\${v.productName} - \${v.variantName} (\${priceFmt} đ)</option>`;
    }
    return html;
  }

  function addItem() {
    document.getElementById('noItemsMsg')?.remove();
    const container = document.getElementById('itemsContainer');
    const div = document.createElement('div');
    div.className = 'item-row';
    div.id = 'item-' + itemCount++;
    div.innerHTML = `
      <select name="variantId" required onchange="updateComboTotals()">
        ` + getVariantOptionsHtml() + `
      </select>
      <input type="number" name="quantity" value="1" min="1" placeholder="SL" oninput="updateComboTotals()"/>
      <span class="item-subtotal">0 đ</span>
      <button type="button" class="btn-rm-item" onclick="removeItem(this)">✕</button>
    `;
    container.appendChild(div);
    updateComboTotals();
  }

  function removeItem(btn) {
    btn.closest('.item-row').remove();
    if (document.querySelectorAll('.item-row').length === 0) {
      const container = document.getElementById('itemsContainer');
      container.innerHTML = '<div class="no-items" id="noItemsMsg">Chưa có sản phẩm nào. Nhấn "+ Thêm sản phẩm".</div>';
    }
    updateComboTotals();
  }

  function updateComboTotals() {
    let totalOriginal = 0;
    const selectedVariantIds = new Set();
    const rows = document.querySelectorAll('.item-row');

    rows.forEach(row => {
      const select = row.querySelector('select[name="variantId"]');
      const qtyInput = row.querySelector('input[name="quantity"]');
      const subtotalEl = row.querySelector('.item-subtotal');

      const variantId = select ? select.value : '';
      const qty = qtyInput ? (parseInt(qtyInput.value, 10) || 0) : 0;

      // Tìm giá của variant
      let unitPrice = 0;
      if (select && select.selectedOptions && select.selectedOptions[0]) {
        unitPrice = parseFloat(select.selectedOptions[0].getAttribute('data-price')) || 0;
      }
      if (!unitPrice && variantId) {
        const found = availableVariants.find(v => String(v.variantId) === String(variantId));
        if (found) unitPrice = found.unitPrice;
      }

      const lineTotal = unitPrice * qty;
      totalOriginal += lineTotal;

      if (subtotalEl) {
        subtotalEl.textContent = lineTotal.toLocaleString('vi-VN') + ' đ';
      }

      // Đánh dấu nếu chọn trùng sản phẩm
      if (variantId) {
        if (selectedVariantIds.has(variantId)) {
          row.style.borderColor = '#f59e0b';
        } else {
          selectedVariantIds.add(variantId);
          row.style.borderColor = '#e2e8f0';
        }
      }
    });

    // Lấy giá bán gói từ input giá
    const planPriceInput = document.getElementById('price');
    const planPrice = planPriceInput ? (parseFloat(planPriceInput.value) || 0) : 0;

    const elTotal = document.getElementById('comboTotalOriginal');
    const elPlanPrice = document.getElementById('comboPlanPrice');
    const elDiff = document.getElementById('comboDiff');

    if (elTotal) elTotal.textContent = totalOriginal.toLocaleString('vi-VN') + ' đ';
    if (elPlanPrice) elPlanPrice.textContent = planPrice.toLocaleString('vi-VN') + ' đ';

    if (elDiff) {
      const diff = totalOriginal - planPrice;
      if (diff > 0) {
        const pct = totalOriginal > 0 ? Math.round((diff / totalOriginal) * 100) : 0;
        elDiff.textContent = 'Tiết kiệm ' + diff.toLocaleString('vi-VN') + ' đ (' + pct + '%)';
        elDiff.style.color = '#15803d';
      } else if (diff < 0) {
        elDiff.textContent = 'Cao hơn giá gốc ' + Math.abs(diff).toLocaleString('vi-VN') + ' đ';
        elDiff.style.color = '#b91c1c';
      } else {
        elDiff.textContent = 'Bằng giá gốc lẻ (0 đ)';
        elDiff.style.color = '#64748b';
      }
    }
  }

  // Lắng nghe khi giá gói thay đổi để cập nhật mức tiết kiệm
  document.getElementById('price')?.addEventListener('input', updateComboTotals);

  // Validate trước khi submit
  document.getElementById('planForm').addEventListener('submit', function(e) {
    const price = parseFloat(document.getElementById('price').value);
    if (isNaN(price) || price < 0) {
      e.preventDefault();
      alert('Giá không hợp lệ!');
      return;
    }

    // Kiểm tra trùng sản phẩm
    const selectedIds = [];
    const selects = document.querySelectorAll('select[name="variantId"]');
    for (const sel of selects) {
      if (sel.value) {
        if (selectedIds.includes(sel.value)) {
          e.preventDefault();
          alert('Có sản phẩm bị chọn trùng trong combo! Vui lòng tăng số lượng thay vì chọn trùng.');
          return;
        }
        selectedIds.push(sel.value);
      }
    }
  });

  // Tính toán lần đầu khi mở form
  document.addEventListener('DOMContentLoaded', updateComboTotals);
  updateComboTotals();
</script>
</body>
</html>
