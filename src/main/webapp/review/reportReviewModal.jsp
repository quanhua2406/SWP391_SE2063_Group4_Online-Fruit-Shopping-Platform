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
  }
  #reportModalOverlay.open { display: flex; }

  #reportModal {
    background: #fff; border-radius: 20px; padding: 32px;
    max-width: 460px; width: 90%;
    box-shadow: 0 24px 60px rgba(0,0,0,.25);
    animation: reportPop .25s ease;
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
    transition: border-color .2s;
  }
  #reportNote:focus { outline: none; border-color: #6366f1; }

  .report-note-label {
    font-size: 0.8rem; color: #64748b; margin-bottom: 6px; display: block;
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

    <form id="reportForm" method="post" action="${pageContext.request.contextPath}/review/report">
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

      <div class="report-modal-actions">
        <button type="button" class="btn-cancel-report" onclick="closeReportModal()">Hủy</button>
        <button type="submit" class="btn-submit-report" id="reportSubmitBtn">Gửi báo cáo</button>
      </div>
    </form>
  </div>
</div>

<script>
  // Highlight selected reason
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
    // Reset form
    document.getElementById('reportForm').reset();
    document.querySelectorAll('.reason-option').forEach(l => l.classList.remove('selected'));
  }

  function closeReportModal() {
    document.getElementById('reportModalOverlay').classList.remove('open');
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
</script>
