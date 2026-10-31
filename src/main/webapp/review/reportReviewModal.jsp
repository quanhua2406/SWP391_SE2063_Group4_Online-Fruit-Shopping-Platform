<%--
  reportReviewModal.jsp
  Fragment – nhúng vào trang product detail hoặc view review.
  Cách dùng:
    <jsp:include page="/review/reportReviewModal.jsp"/>
  Kích hoạt modal:
    openReportModal(reviewId, redirectUrl)
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
  /* ── Report Modal ── */
  #reportModalOverlay {
    display: none; position: fixed; inset: 0;
    background: rgba(0,0,0,.5); z-index: 2000;
    align-items: center; justify-content: center;
    overflow-y: auto; padding: 20px 0;
  }
  #reportModalOverlay.open { display: flex; }

  #reportModal {
    background: #fff; border-radius: 20px; padding: 32px;
    max-width: 500px; width: 90%;
    box-shadow: 0 24px 60px rgba(0,0,0,.25);
    animation: reportPop .25s ease;
    margin: auto;
  }
  @keyframes reportPop {
    from { transform: translateY(20px); opacity: 0; }
    to   { transform: translateY(0);    opacity: 1; }
  }

  #reportModal h3 {
    font-size: 1.15rem; font-weight: 700; margin-bottom: 6px; color: #1e293b;
  }
  #reportModal .subtitle {
    font-size: 0.85rem; color: #64748b; margin-bottom: 20px;
  }

  .report-reasons { display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px; }
  .reason-option {
    display: flex; align-items: center; gap: 10px;
    padding: 10px 14px; border: 2px solid #e2e8f0; border-radius: 10px;
    cursor: pointer; transition: all .2s; font-size: 0.875rem;
  }
  .reason-option:hover { border-color: #6366f1; background: #f5f3ff; }
  .reason-option input[type="radio"] { accent-color: #6366f1; width: 16px; height: 16px; }
  .reason-option.selected { border-color: #6366f1; background: #ede9fe; }

  #reportNote {
    width: 100%; padding: 10px 14px; border: 2px solid #e2e8f0; border-radius: 10px;
    font-size: 0.875rem; font-family: inherit; resize: vertical; min-height: 80px;
    transition: border-color .2s; box-sizing: border-box;
  }
  #reportNote:focus { outline: none; border-color: #6366f1; }

  .report-note-label {
    font-size: 0.8rem; color: #64748b; margin-bottom: 6px; display: block;
  }

  /* ── Evidence Upload Section ── */
  .evidence-section {
    margin-top: 18px;
    border: 2px dashed #c4b5fd;
    border-radius: 14px;
    padding: 16px;
    background: #faf5ff;
  }
  .evidence-section-title {
    font-size: 0.875rem; font-weight: 700; color: #5b21b6;
    margin: 0 0 4px 0; display: flex; align-items: center; gap: 6px;
  }
  .evidence-section-hint {
    font-size: 0.75rem; color: #7c3aed; margin-bottom: 12px;
  }

  .evidence-drop-zone {
    border: 2px dashed #a78bfa;
    border-radius: 10px; padding: 18px;
    text-align: center; cursor: pointer;
    background: #fff; transition: all .2s;
    position: relative;
  }
  .evidence-drop-zone:hover,
  .evidence-drop-zone.drag-over {
    border-color: #6366f1; background: #ede9fe;
  }
  .evidence-drop-zone input[type="file"] {
    position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; height: 100%;
  }
  .evidence-drop-icon { font-size: 2rem; display: block; margin-bottom: 6px; }
  .evidence-drop-text { font-size: 0.8rem; color: #6b7280; }
  .evidence-drop-text strong { color: #6366f1; }

  /* Preview thumbnails */
  .evidence-previews {
    display: flex; gap: 10px; flex-wrap: wrap; margin-top: 12px;
  }
  .evidence-thumb {
    position: relative; width: 72px; height: 72px;
    border-radius: 8px; overflow: hidden;
    border: 2px solid #c4b5fd;
    flex-shrink: 0;
  }
  .evidence-thumb img {
    width: 100%; height: 100%; object-fit: cover; display: block;
  }
  .evidence-thumb .remove-thumb {
    position: absolute; top: 2px; right: 2px;
    width: 18px; height: 18px; border-radius: 50%;
    background: rgba(239,68,68,.9); color: #fff;
    border: none; cursor: pointer; font-size: 11px;
    display: flex; align-items: center; justify-content: center;
    line-height: 1;
  }
  .evidence-count-label {
    font-size: 0.75rem; color: #64748b; margin-top: 8px; text-align: right;
  }

  .report-modal-actions {
    display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end;
  }
  .btn-cancel-report {
    padding: 10px 20px; border-radius: 10px; border: 2px solid #e2e8f0;
    background: #fff; color: #475569; font-weight: 600; cursor: pointer;
    font-size: 0.875rem; transition: all .2s;
  }
  .btn-cancel-report:hover { background: #f1f5f9; }
  .btn-submit-report {
    padding: 10px 20px; border-radius: 10px; border: none;
    background: linear-gradient(135deg, #ef4444, #dc2626);
    color: #fff; font-weight: 600; cursor: pointer;
    font-size: 0.875rem; transition: all .2s;
  }
  .btn-submit-report:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(239,68,68,.4); }
  .btn-submit-report:disabled { opacity: .5; cursor: not-allowed; transform: none; }
</style>

<!-- Report Modal -->
<div id="reportModalOverlay">
  <div id="reportModal" role="dialog" aria-modal="true" aria-labelledby="reportModalTitle">
    <h3 id="reportModalTitle">🚩 Báo cáo đánh giá</h3>
    <p class="subtitle">Chọn lý do báo cáo phù hợp nhất:</p>

    <%-- enctype multipart/form-data bắt buộc khi upload file --%>
    <form id="reportForm" method="post"
          action="${pageContext.request.contextPath}/review/report"
          enctype="multipart/form-data">
      <input type="hidden" name="reviewId"  id="reportReviewId"/>
      <input type="hidden" name="redirect"  id="reportRedirect"/>

      <div class="report-reasons">
        <label class="reason-option">
          <input type="radio" name="reason" value="spam" required/>
          🗑️ Spam / Quảng cáo
        </label>
        <label class="reason-option">
          <input type="radio" name="reason" value="inappropriate_language"/>
          🤬 Ngôn ngữ không phù hợp
        </label>
        <label class="reason-option">
          <input type="radio" name="reason" value="fake_review"/>
          🤥 Đánh giá giả mạo
        </label>
        <label class="reason-option">
          <input type="radio" name="reason" value="other"/>
          📝 Lý do khác
        </label>
      </div>

      <label class="report-note-label" for="reportNote">Ghi chú thêm (tuỳ chọn):</label>
      <textarea id="reportNote" name="note" placeholder="Mô tả thêm nếu cần..." maxlength="500"></textarea>

      <%-- ── Bằng chứng vi phạm ── --%>
      <div class="evidence-section">
        <p class="evidence-section-title">
          📎 Bằng chứng vi phạm <span style="font-weight:400;color:#7c3aed;">(tuỳ chọn)</span>
        </p>
        <p class="evidence-section-hint">
          Tải ảnh chụp màn hình hoặc bằng chứng khác để hỗ trợ báo cáo.<br/>
          Tối đa 3 ảnh · Mỗi ảnh tối đa 5 MB · Định dạng: JPG, PNG, WEBP
        </p>

        <div class="evidence-drop-zone" id="evidenceDropZone">
          <input type="file" name="evidenceImages" id="evidenceInput"
                 accept="image/jpeg,image/png,image/webp"
                 multiple/>
          <span class="evidence-drop-icon">🖼️</span>
          <div class="evidence-drop-text">
            <strong>Nhấp để chọn</strong> hoặc kéo thả ảnh vào đây
          </div>
        </div>

        <div class="evidence-previews" id="evidencePreviews"></div>
        <div class="evidence-count-label" id="evidenceCountLabel" style="display:none;"></div>
      </div>

      <div class="report-modal-actions">
        <button type="button" class="btn-cancel-report" onclick="closeReportModal()">Hủy</button>
        <button type="submit" class="btn-submit-report" id="reportSubmitBtn">Gửi báo cáo</button>
      </div>
    </form>
  </div>
</div>

<script>
(function () {
  // ── Highlight selected reason ──────────────────────────────────────────
  document.querySelectorAll('.reason-option').forEach(function(label) {
    label.addEventListener('click', function() {
      document.querySelectorAll('.reason-option').forEach(l => l.classList.remove('selected'));
      this.classList.add('selected');
    });
  });

  /**
   * Mở modal báo cáo.
   * @param {number} reviewId   - ID của review cần báo cáo
   * @param {string} redirectUrl - URL quay lại sau khi submit (tuỳ chọn)
   */
  function openReportModal(reviewId, redirectUrl) {
    document.getElementById('reportReviewId').value = reviewId;
    document.getElementById('reportRedirect').value = redirectUrl || window.location.href;
    document.getElementById('reportModalOverlay').classList.add('open');
    // Reset form & previews
    resetReportForm();
  }
  window.openReportModal = openReportModal;

  function closeReportModal() {
    document.getElementById('reportModalOverlay').classList.remove('open');
    resetReportForm();
  }
  window.closeReportModal = closeReportModal;

  function resetReportForm() {
    document.getElementById('reportForm').reset();
    document.querySelectorAll('.reason-option').forEach(l => l.classList.remove('selected'));
    evidenceFiles = [];
    renderPreviews();
    document.getElementById('reportSubmitBtn').disabled = true;
  }

  // Đóng khi click ngoài modal
  document.getElementById('reportModalOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeReportModal();
  });

  // Disable nút submit nếu chưa chọn reason
  document.getElementById('reportForm').addEventListener('change', function() {
    const chosen = this.querySelector('input[name="reason"]:checked');
    document.getElementById('reportSubmitBtn').disabled = !chosen;
  });
  document.getElementById('reportSubmitBtn').disabled = true;

  // ── Evidence Upload Logic ──────────────────────────────────────────────
  const MAX_EVIDENCE = 3;
  let evidenceFiles  = [];          // DataTransfer list phục vụ input

  const dropZone   = document.getElementById('evidenceDropZone');
  const fileInput  = document.getElementById('evidenceInput');
  const previewBox = document.getElementById('evidencePreviews');
  const countLabel = document.getElementById('evidenceCountLabel');

  // Drag-over highlight
  dropZone.addEventListener('dragover', function(e) {
    e.preventDefault();
    this.classList.add('drag-over');
  });
  dropZone.addEventListener('dragleave', function() {
    this.classList.remove('drag-over');
  });
  dropZone.addEventListener('drop', function(e) {
    e.preventDefault();
    this.classList.remove('drag-over');
    addFiles(e.dataTransfer.files);
  });

  // Normal file picker
  fileInput.addEventListener('change', function() {
    addFiles(this.files);
    // Reset value so same file can be re-selected after removal
    this.value = '';
  });

  function addFiles(fileList) {
    const allowed = ['image/jpeg', 'image/png', 'image/webp'];
    for (let i = 0; i < fileList.length; i++) {
      if (evidenceFiles.length >= MAX_EVIDENCE) break;
      const f = fileList[i];
      if (!allowed.includes(f.type)) continue;
      if (f.size > 5 * 1024 * 1024) {
        alert('Ảnh "' + f.name + '" vượt quá 5 MB, vui lòng chọn ảnh nhỏ hơn.');
        continue;
      }
      evidenceFiles.push(f);
    }
    renderPreviews();
    syncInputFiles();
  }

  function removeFile(index) {
    evidenceFiles.splice(index, 1);
    renderPreviews();
    syncInputFiles();
  }

  function renderPreviews() {
    previewBox.innerHTML = '';
    evidenceFiles.forEach(function(f, idx) {
      const reader = new FileReader();
      reader.onload = function(e) {
        const thumb = document.createElement('div');
        thumb.className = 'evidence-thumb';
        thumb.innerHTML =
          '<img src="' + e.target.result + '" alt="bằng chứng ' + (idx+1) + '"/>' +
          '<button type="button" class="remove-thumb" title="Xoá ảnh này" ' +
          'onclick="(function(){removeEvidenceFile(' + idx + ')})()" >✕</button>';
        previewBox.appendChild(thumb);
      };
      reader.readAsDataURL(f);
    });

    if (evidenceFiles.length > 0) {
      countLabel.style.display = 'block';
      countLabel.textContent   = evidenceFiles.length + '/' + MAX_EVIDENCE + ' ảnh đã chọn';
      dropZone.style.display   = evidenceFiles.length >= MAX_EVIDENCE ? 'none' : '';
    } else {
      countLabel.style.display = 'none';
      dropZone.style.display   = '';
    }
  }

  // Expose removeEvidenceFile to inline onclick
  window.removeEvidenceFile = removeFile;

  /** Đồng bộ FileList thực sự vào input[type=file] để form submit đúng */
  function syncInputFiles() {
    const dt = new DataTransfer();
    evidenceFiles.forEach(f => dt.items.add(f));
    fileInput.files = dt.files;
  }
})();
</script>
