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
  <style>
    /* Styling cho thanh tìm kiếm biến thể sản phẩm */
    .variant-search-container {
      position: relative;
      margin-bottom: 16px;
    }
    .search-input-box {
      position: relative;
      display: flex;
      align-items: center;
    }
    .search-input-box .search-icon {
      position: absolute;
      left: 14px;
      font-size: 1rem;
      color: #64748b;
      pointer-events: none;
    }
    .search-input-box input {
      width: 100%;
      padding: 12px 40px 12px 42px;
      border: 2px solid #cbd5e1;
      border-radius: 12px;
      font-size: 0.95rem;
      transition: all 0.2s ease;
      background: #ffffff;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    }
    .search-input-box input:focus {
      outline: none;
      border-color: #10b981;
      box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.15);
    }
    .btn-clear-search {
      position: absolute;
      right: 12px;
      background: #e2e8f0;
      border: none;
      border-radius: 50%;
      width: 26px;
      height: 26px;
      cursor: pointer;
      font-size: 0.8rem;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #475569;
      transition: background 0.15s;
    }
    .btn-clear-search:hover {
      background: #cbd5e1;
    }
    .search-dropdown-menu {
      position: absolute;
      top: calc(100% + 6px);
      left: 0;
      right: 0;
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.15), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
      max-height: 320px;
      overflow-y: auto;
      z-index: 100;
    }
    .search-res-header {
      padding: 10px 16px;
      background: #f8fafc;
      border-bottom: 1px solid #e2e8f0;
      font-size: 0.75rem;
      font-weight: 700;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .search-res-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 11px 16px;
      border-bottom: 1px solid #f1f5f9;
      cursor: pointer;
      transition: background 0.15s ease;
    }
    .search-res-item:last-child {
      border-bottom: none;
    }
    .search-res-item:hover {
      background: #f0fdf4;
    }
    .search-res-item.is-in-combo {
      background: #fbfcfe;
    }
    .search-res-item.is-in-combo:hover {
      background: #f0fdf4;
    }
    .res-item-left {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .res-item-name {
      font-weight: 600;
      color: #1e293b;
      font-size: 0.92rem;
    }
    .res-item-variant {
      font-size: 0.8rem;
      color: #64748b;
    }
    .res-item-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .res-item-price {
      font-weight: 700;
      color: #15803d;
      font-size: 0.92rem;
      white-space: nowrap;
    }
    .btn-quick-add {
      padding: 6px 14px;
      background: #10b981;
      color: #ffffff;
      border: none;
      border-radius: 8px;
      font-size: 0.82rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s ease;
      white-space: nowrap;
    }
    .btn-quick-add:hover {
      background: #059669;
      transform: translateY(-1px);
    }
    .res-badge-added {
      padding: 5px 12px;
      background: #e2e8f0;
      color: #334155;
      border-radius: 8px;
      font-size: 0.78rem;
      font-weight: 600;
      white-space: nowrap;
    }
    .no-search-res {
      padding: 20px;
      text-align: center;
      color: #94a3b8;
      font-size: 0.9rem;
    }
  </style>
</head>
<body class="plan-management-page">

<div class="topbar">
  <h1>${mode == 'edit' ? 'Chỉnh sửa Gói Đăng Ký' : 'Tạo Gói Đăng Ký Mới'}</h1>
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
        <div class="section-title">Thông tin cơ bản</div>

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
        <div class="section-title">Giá & Chu kỳ</div>
        <div class="form-row">
          <div class="form-group">
            <label for="price">Giá (VNĐ) <span class="req">*</span></label>
            <input type="number" id="price" name="price" min="0" step="1000"
                   value="${plan.price}" placeholder="VD: 150000" required/>
          </div>
          <div class="form-group">
            <label for="billingCycle">Chu kỳ thanh toán <span class="req">*</span></label>
            <select id="billingCycle" name="billingCycle" required onchange="syncDurationDays()">
              <option value="weekly"    ${plan.billingCycle == 'weekly'    ? 'selected' : ''}>Hàng tuần (Weekly - 7 ngày)</option>
              <option value="monthly"   ${empty plan.billingCycle || plan.billingCycle == 'monthly'   ? 'selected' : ''}>Hàng tháng (Monthly - 30 ngày)</option>
              <option value="quarterly" ${plan.billingCycle == 'quarterly' ? 'selected' : ''}>Hàng quý (Quarterly - 90 ngày)</option>
              <option value="yearly"    ${plan.billingCycle == 'yearly'    ? 'selected' : ''}>Hàng năm (Yearly - 365 ngày)</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="durationDays">Thời hạn hiệu lực (ngày) <span class="req">*</span></label>
            <input type="number" id="durationDays" name="durationDays" min="1"
                   value="${plan.durationDays > 0 ? plan.durationDays : (plan.billingCycle == 'weekly' ? 7 : (plan.billingCycle == 'quarterly' ? 90 : (plan.billingCycle == 'yearly' ? 365 : 30)))}" required/>
            <div class="hint" id="durationHint">Tự động đồng bộ theo chu kỳ (Tuần: 7 ngày, Tháng: 30 ngày, Quý: 90 ngày, Năm: 365 ngày)</div>
          </div>
          <div class="form-group">
            <label>Trạng thái</label>
            <div class="status-options">
              <label class="status-opt">
                <input type="radio" name="status" value="draft"
                       ${empty plan.status || plan.status == 'draft' ? 'checked' : ''}/>
                <span>Draft</span>
              </label>
              <label class="status-opt">
                <input type="radio" name="status" value="active"
                       ${plan.status == 'active' ? 'checked' : ''}/>
                <span>Active</span>
              </label>
              <label class="status-opt">
                <input type="radio" name="status" value="inactive"
                       ${plan.status == 'inactive' ? 'checked' : ''}/>
                <span>Inactive</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Sản phẩm trong combo ── -->
      <div class="form-section">
        <div class="items-header">
          <div class="section-title" style="margin-bottom:0">Sản phẩm trong combo</div>
          <button type="button" class="btn-add-item" onclick="addItem()">+ Thêm dòng sản phẩm</button>
        </div>

        <c:if test="${empty availableVariants}">
          <div class="hint" style="color:#d97706; margin-bottom: 10px;">
            Shop của bạn chưa có sản phẩm hoạt động nào. Hãy thêm sản phẩm trước khi tạo gói combo.
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

        <!-- Thanh tìm kiếm nhanh & thêm sản phẩm vào combo -->
        <div class="variant-search-container" id="variantSearchContainer">
          <div class="search-input-box">
            <span class="search-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color:#94a3b8;"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
            </span>
            <input type="text" id="variantSearchInput"
                   placeholder="Nhập tên trái cây hoặc SKU để tìm nhanh (VD: Nho, Táo, Cam)..."
                   autocomplete="off"
                   oninput="searchVariants(this.value)"
                   onfocus="searchVariants(this.value)"/>
            <button type="button" class="btn-clear-search" id="btnClearSearch" onclick="clearVariantSearch()" style="display:none;" title="Xóa tìm kiếm">✕</button>
          </div>
          <!-- Menu kết quả tìm kiếm thả xuống -->
          <div class="search-dropdown-menu" id="searchDropdownMenu" style="display:none;"></div>
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
                  <input type="number" name="quantity" value="${item.quantity > 0 ? item.quantity : 1}" min="1" placeholder="SL" oninput="updateComboTotals()"/>
                  <span class="item-subtotal">
                    <fmt:formatNumber value="${item.unitPrice * (item.quantity > 0 ? item.quantity : 1)}" pattern="#,###"/> đ
                  </span>
                  <button type="button" class="btn-rm-item" onclick="removeItem(this)">✕</button>
                </div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="no-items" id="noItemsMsg">Chưa có sản phẩm nào. Dùng thanh tìm kiếm ở trên hoặc nhấn "+ Thêm dòng sản phẩm".</div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <!-- ── Actions ── -->
      <div class="form-actions">
        <a href="${pageContext.request.contextPath}/store-owner/subscription/plans" class="btn-cancel">Hủy</a>
        <button type="submit" class="btn-save">
          ${mode == 'edit' ? 'Lưu thay đổi' : 'Tạo gói'}
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
        productName: "<c:out value="${v.productName}"/>",
        variantName: "<c:out value="${v.variantName}"/>",
        unitPrice: ${v.unitPrice}
      }${!vs.last ? ',' : ''}
    </c:forEach>
  ];

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Lấy tập hợp các variantId đã được chọn trong các hàng combo hiện tại
  function getCurrentSelectedVariantIds() {
    const set = new Set();
    const selects = document.querySelectorAll('select[name="variantId"]');
    selects.forEach(sel => {
      if (sel.value && sel.value.trim() !== '') {
        set.add(String(sel.value.trim()));
      }
    });
    return set;
  }

  // Tìm hàng chứa variantId
  function findRowByVariantId(variantId) {
    const rows = document.querySelectorAll('.item-row');
    for (const row of rows) {
      const select = row.querySelector('select[name="variantId"]');
      if (select && String(select.value) === String(variantId)) {
        return row;
      }
    }
    return null;
  }

  // Tạo HTML các option cho thẻ <select> bằng phép nối chuỗi thuần túy (tránh lỗi EL của JSP)
  function getVariantOptionsHtml(selectedId) {
    let html = '<option value="">-- Chọn biến thể sản phẩm --</option>';
    for (let i = 0; i < availableVariants.length; i++) {
      const v = availableVariants[i];
      const priceFmt = Number(v.unitPrice).toLocaleString('vi-VN');
      const isSel = (selectedId && Number(selectedId) === Number(v.variantId)) ? ' selected' : '';
      html += '<option value="' + v.variantId + '" data-price="' + v.unitPrice + '"' + isSel + '>'
            + escapeHtml(v.productName) + ' - ' + escapeHtml(v.variantName) + ' (' + priceFmt + ' đ)</option>';
    }
    return html;
  }

  // Thêm trực tiếp biến thể từ kết quả tìm kiếm vào combo
  function addVariantDirectly(variantId) {
    const existingRow = findRowByVariantId(variantId);
    if (existingRow) {
      // Đã có trong combo -> Tăng số lượng lên 1
      const qtyInput = existingRow.querySelector('input[name="quantity"]');
      if (qtyInput) {
        qtyInput.value = (parseInt(qtyInput.value, 10) || 0) + 1;
        existingRow.style.borderColor = '#10b981';
        existingRow.style.backgroundColor = '#ecfdf5';
        setTimeout(() => {
          existingRow.style.borderColor = '#e2e8f0';
          existingRow.style.backgroundColor = '';
        }, 800);
      }
      updateComboTotals();
      clearVariantSearch();
      return;
    }

    // Chưa có -> Tạo hàng mới và chọn biến thể này
    document.getElementById('noItemsMsg')?.remove();
    const container = document.getElementById('itemsContainer');
    const div = document.createElement('div');
    div.className = 'item-row';
    div.id = 'item-' + (itemCount++);
    div.innerHTML = 
      '<select name="variantId" required onchange="updateComboTotals()">' +
        getVariantOptionsHtml(variantId) +
      '</select>' +
      '<input type="number" name="quantity" value="1" min="1" placeholder="SL" oninput="updateComboTotals()"/>' +
      '<span class="item-subtotal">0 đ</span>' +
      '<button type="button" class="btn-rm-item" onclick="removeItem(this)">✕</button>';
    container.appendChild(div);

    div.style.borderColor = '#10b981';
    div.style.backgroundColor = '#ecfdf5';
    setTimeout(() => {
      div.style.borderColor = '#e2e8f0';
      div.style.backgroundColor = '';
    }, 800);

    updateComboTotals();
    clearVariantSearch();
  }

  // Tìm kiếm sản phẩm theo từ khóa
  function searchVariants(keyword) {
    const dropdown = document.getElementById('searchDropdownMenu');
    const btnClear = document.getElementById('btnClearSearch');
    if (!dropdown) return;

    if (!keyword || keyword.trim() === '') {
      btnClear.style.display = 'none';
      renderSearchResults(availableVariants.slice(0, 10), false);
      dropdown.style.display = 'block';
      return;
    }

    btnClear.style.display = 'flex';
    const q = keyword.trim().toLowerCase();
    const matched = availableVariants.filter(v => {
      const full = (v.productName + ' ' + v.variantName).toLowerCase();
      return full.includes(q);
    });

    renderSearchResults(matched, true);
    dropdown.style.display = 'block';
  }

  function renderSearchResults(list, isFiltered) {
    const dropdown = document.getElementById('searchDropdownMenu');
    if (!dropdown) return;

    if (!list || list.length === 0) {
      dropdown.innerHTML = '<div class="no-search-res">Không tìm thấy sản phẩm nào khớp với từ khóa.</div>';
      return;
    }

    const currentVariantIds = getCurrentSelectedVariantIds();
    let html = '';
    if (!isFiltered) {
      html += '<div class="search-res-header">Gợi ý sản phẩm nổi bật (hoặc gõ tên để tìm nhanh)</div>';
    } else {
      html += '<div class="search-res-header">Kết quả tìm kiếm (' + list.length + ' sản phẩm)</div>';
    }

    for (let i = 0; i < list.length; i++) {
      const v = list[i];
      const isAdded = currentVariantIds.has(String(v.variantId));
      const priceFmt = Number(v.unitPrice).toLocaleString('vi-VN');

      html += '<div class="search-res-item ' + (isAdded ? 'is-in-combo' : '') + '" onclick="addVariantDirectly(' + v.variantId + ')">' +
                '<div class="res-item-left">' +
                  '<div class="res-item-name">' + escapeHtml(v.productName) + '</div>' +
                  '<div class="res-item-variant">Biến thể: ' + escapeHtml(v.variantName) + '</div>' +
                '</div>' +
                '<div class="res-item-right">' +
                  '<span class="res-item-price">' + priceFmt + ' đ</span>' +
                  (isAdded 
                    ? '<span class="res-badge-added">✓ Đã có (+1)</span>' 
                    : '<button type="button" class="btn-quick-add">+ Thêm</button>') +
                '</div>' +
              '</div>';
    }
    dropdown.innerHTML = html;
  }

  function clearVariantSearch() {
    const input = document.getElementById('variantSearchInput');
    const dropdown = document.getElementById('searchDropdownMenu');
    const btnClear = document.getElementById('btnClearSearch');
    if (input) input.value = '';
    if (dropdown) dropdown.style.display = 'none';
    if (btnClear) btnClear.style.display = 'none';
  }

  // Đóng dropdown khi bấm ra ngoài
  document.addEventListener('click', function(e) {
    const searchContainer = document.getElementById('variantSearchContainer');
    if (searchContainer && !searchContainer.contains(e.target)) {
      const dropdown = document.getElementById('searchDropdownMenu');
      if (dropdown) dropdown.style.display = 'none';
    }
  });

  // Nút thêm sản phẩm thủ công
  function addItem() {
    document.getElementById('noItemsMsg')?.remove();
    const container = document.getElementById('itemsContainer');
    const div = document.createElement('div');
    div.className = 'item-row';
    div.id = 'item-' + (itemCount++);
    div.innerHTML = 
      '<select name="variantId" required onchange="updateComboTotals()">' +
        getVariantOptionsHtml(null) +
      '</select>' +
      '<input type="number" name="quantity" value="1" min="1" placeholder="SL" oninput="updateComboTotals()"/>' +
      '<span class="item-subtotal">0 đ</span>' +
      '<button type="button" class="btn-rm-item" onclick="removeItem(this)">✕</button>';
    container.appendChild(div);
    updateComboTotals();
  }

  function removeItem(btn) {
    btn.closest('.item-row').remove();
    if (document.querySelectorAll('.item-row').length === 0) {
      const container = document.getElementById('itemsContainer');
      container.innerHTML = '<div class="no-items" id="noItemsMsg">Chưa có sản phẩm nào. Dùng thanh tìm kiếm ở trên hoặc nhấn "+ Thêm dòng sản phẩm".</div>';
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

      // Tìm giá của variant từ option hoặc từ mảng availableVariants
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

      // Đánh dấu cảnh báo nếu chọn trùng sản phẩm
      if (variantId) {
        if (selectedVariantIds.has(variantId)) {
          row.style.borderColor = '#f59e0b';
          row.style.backgroundColor = '#fffbeb';
        } else {
          selectedVariantIds.add(variantId);
          row.style.borderColor = '#e2e8f0';
          row.style.backgroundColor = '';
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

  // Đồng bộ số ngày hiệu lực theo chu kỳ
  function syncDurationDays() {
    const cycle = document.getElementById('billingCycle')?.value;
    const durationInput = document.getElementById('durationDays');
    if (!durationInput || !cycle) return;
    switch (cycle) {
      case 'weekly':
        durationInput.value = 7;
        break;
      case 'monthly':
        durationInput.value = 30;
        break;
      case 'quarterly':
        durationInput.value = 90;
        break;
      case 'yearly':
        durationInput.value = 365;
        break;
    }
  }

  // Lắng nghe khi giá gói thay đổi để cập nhật mức tiết kiệm
  document.getElementById('price')?.addEventListener('input', updateComboTotals);

  // Validate trước khi submit
  document.getElementById('planForm').addEventListener('submit', function(e) {
    const price = parseFloat(document.getElementById('price').value);
    if (isNaN(price) || price < 0) {
      e.preventDefault();
      alert('Vui lòng nhập giá gói hợp lệ!');
      return;
    }

    // Kiểm tra tính nhất quán giữa chu kỳ và số ngày hiệu lực
    const cycle = document.getElementById('billingCycle')?.value;
    const days = parseInt(document.getElementById('durationDays')?.value, 10);
    if (cycle === 'weekly' && days !== 7) {
      e.preventDefault();
      alert('Gói chu kỳ "Hàng tuần" phải có thời hạn hiệu lực là 7 ngày!');
      document.getElementById('durationDays').value = 7;
      return;
    } else if (cycle === 'monthly' && (days < 28 || days > 31)) {
      e.preventDefault();
      alert('Gói chu kỳ "Hàng tháng" phải có thời hạn hiệu lực là 30 ngày (28-31 ngày)!');
      document.getElementById('durationDays').value = 30;
      return;
    } else if (cycle === 'quarterly' && (days < 89 || days > 93)) {
      e.preventDefault();
      alert('Gói chu kỳ "Hàng quý" phải có thời hạn hiệu lực là 90 ngày!');
      document.getElementById('durationDays').value = 90;
      return;
    } else if (cycle === 'yearly' && (days < 365 || days > 366)) {
      e.preventDefault();
      alert('Gói chu kỳ "Hàng năm" phải có thời hạn hiệu lực là 365 ngày!');
      document.getElementById('durationDays').value = 365;
      return;
    }

    const selectedIds = [];
    const selects = document.querySelectorAll('select[name="variantId"]');
    for (const sel of selects) {
      if (sel.value && sel.value.trim() !== '') {
        if (selectedIds.includes(sel.value)) {
          e.preventDefault();
          alert('Có sản phẩm bị chọn trùng trong combo! Vui lòng tăng số lượng thay vì chọn trùng.');
          return;
        }
        selectedIds.push(sel.value);
      }
    }

    if (selectedIds.length === 0) {
      e.preventDefault();
      alert('Gói đăng ký định kỳ phải chứa ít nhất 1 biến thể sản phẩm. Vui lòng thêm sản phẩm vào combo!');
      return;
    }
  });

  // Tính toán lần đầu khi mở form
  document.addEventListener('DOMContentLoaded', updateComboTotals);
  updateComboTotals();
</script>
</body>
</html>
