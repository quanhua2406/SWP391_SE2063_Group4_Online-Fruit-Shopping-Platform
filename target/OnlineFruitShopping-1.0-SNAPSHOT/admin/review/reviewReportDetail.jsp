<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Báo cáo #RV-${detail.reviewId} | Admin Moderation</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: #f8fafc;
      color: #1e293b;
      min-height: 100vh;
      padding: 24px 32px 60px 32px;
    }

    /* ── Header & Breadcrumbs ── */
    .top-nav {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 24px;
    }
    .breadcrumbs {
      font-size: 0.875rem; color: #64748b; margin-bottom: 8px;
    }
    .breadcrumbs a {
      color: #64748b; text-decoration: none; transition: color .2s;
    }
    .breadcrumbs a:hover { color: #2563eb; }
    .breadcrumbs span { margin: 0 6px; color: #cbd5e1; }

    .page-title-row {
      display: flex; align-items: center; gap: 14px;
    }
    .page-title {
      font-size: 1.75rem; font-weight: 700; color: #0f172a;
    }
    .status-badge {
      display: inline-flex; align-items: center;
      padding: 5px 14px; border-radius: 9999px;
      font-size: 0.8rem; font-weight: 600;
    }
    .status-badge.pending {
      background: #ffedd5; color: #c2410c; border: 1px solid #fed7aa;
    }
    .status-badge.published {
      background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0;
    }
    .status-badge.rejected {
      background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca;
    }
    .status-badge.deleted {
      background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1;
    }

    .top-actions {
      display: flex; align-items: center; gap: 12px;
    }
    .btn-header {
      display: inline-flex; align-items: center; gap: 6px;
      padding: 8px 16px; background: #fff; border: 1px solid #e2e8f0;
      border-radius: 8px; font-size: 0.875rem; font-weight: 500;
      color: #334155; cursor: pointer; text-decoration: none;
      transition: all .2s; box-shadow: 0 1px 2px rgba(0,0,0,.05);
    }
    .btn-header:hover { background: #f8fafc; border-color: #cbd5e1; }
    .btn-bell {
      width: 38px; height: 38px; display: inline-flex; align-items: center; justify-content: center;
      background: #fff; border: 1px solid #e2e8f0; border-radius: 8px;
      color: #334155; cursor: pointer; font-size: 1.1rem; box-shadow: 0 1px 2px rgba(0,0,0,.05);
    }

    /* ── Toast notification ── */
    .toast {
      padding: 12px 20px; border-radius: 10px; margin-bottom: 20px;
      font-size: 0.9rem; font-weight: 500; animation: fadeIn .3s ease;
    }
    .toast.success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
    .toast.error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(-6px); } to { opacity: 1; transform: translateY(0); } }

    /* ── Layout Grid ── */
    .grid-top {
      display: grid;
      grid-template-columns: 1.15fr 1.15fr 1fr;
      gap: 20px;
      margin-bottom: 20px;
    }
    .grid-bottom {
      display: grid;
      grid-template-columns: 1fr 1.5fr;
      gap: 20px;
    }

    @media (max-width: 1080px) {
      .grid-top { grid-template-columns: 1fr; }
      .grid-bottom { grid-template-columns: 1fr; }
    }

    /* ── Card Styling ── */
    .card {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 1px 3px rgba(0,0,0,.03);
      display: flex; flex-direction: column;
    }
    .card-header {
      display: flex; align-items: center; justify-content: space-between;
      margin-bottom: 18px;
    }
    .card-title {
      font-size: 1.05rem; font-weight: 700; color: #0f172a;
      display: flex; align-items: center; gap: 8px;
    }
    .card-title-icon { font-size: 1.15rem; }

    /* ── Card 1: Report Info ── */
    .info-list { display: flex; flex-direction: column; gap: 14px; margin-bottom: 18px; }
    .info-row { display: flex; font-size: 0.875rem; }
    .info-label { width: 130px; color: #64748b; font-weight: 500; flex-shrink: 0; }
    .info-value { color: #0f172a; font-weight: 500; flex: 1; }
    .info-value.danger { color: #ef4444; font-weight: 600; }

    .seller-desc-title {
      font-size: 0.875rem; font-weight: 600; color: #1e293b; margin-bottom: 8px;
    }
    .seller-desc-box {
      background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px;
      padding: 14px; font-size: 0.875rem; color: #334155; line-height: 1.5;
      margin-bottom: 20px;
    }

    .evidence-title {
      font-size: 0.875rem; font-weight: 600; color: #1e293b; margin-bottom: 10px;
    }
    .evidence-gallery {
      display: flex; gap: 12px; flex-wrap: wrap;
    }
    .evidence-item {
      position: relative; width: 84px; height: 84px;
      border-radius: 10px; overflow: hidden;
      border: 1px solid #cbd5e1; cursor: pointer;
      background: #f1f5f9; transition: transform .15s, box-shadow .15s;
    }
    .evidence-item:hover {
      transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.12);
    }
    .evidence-item img {
      width: 100%; height: 100%; object-fit: cover; display: block;
    }
    .evidence-item .badge-num {
      position: absolute; bottom: 4px; left: 4px;
      background: rgba(0,0,0,.75); color: #fff;
      font-size: 0.7rem; font-weight: 700; width: 18px; height: 18px;
      border-radius: 4px; display: flex; align-items: center; justify-content: center;
    }

    /* ── Card 2: Original Review ── */
    .review-stars {
      display: flex; gap: 2px; font-size: 1.5rem; margin-bottom: 12px;
    }
    .star-filled { color: #ef4444; }
    .star-empty { color: #cbd5e1; }

    .review-quote {
      font-size: 1.2rem; font-weight: 700; color: #0f172a; line-height: 1.45;
      margin-bottom: 24px; word-break: break-word;
    }

    .review-meta-divider {
      height: 1px; background: #e2e8f0; margin-bottom: 16px;
    }

    .review-images-row {
      display: flex; gap: 12px; flex-wrap: wrap; margin-top: 10px;
    }
    .review-img-box {
      width: 90px; height: 90px; border-radius: 10px; overflow: hidden;
      border: 1px solid #e2e8f0; cursor: pointer; background: #f8fafc;
      transition: transform .15s;
    }
    .review-img-box:hover { transform: scale(1.04); }
    .review-img-box img { width: 100%; height: 100%; object-fit: cover; }

    /* ── Card 3: Actions ── */
    .action-buttons {
      display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px;
    }
    .btn-act {
      width: 100%; padding: 11px 16px; border-radius: 10px; font-size: 0.9rem;
      font-weight: 600; cursor: pointer; transition: all .2s; border: none;
      display: flex; align-items: center; justify-content: center; gap: 8px;
    }
    .btn-act.hide-review {
      background: #ef4444; color: #fff;
    }
    .btn-act.hide-review:hover { background: #dc2626; box-shadow: 0 4px 12px rgba(239,68,68,.3); }

    .btn-act.delete-review {
      background: #dc2626; color: #fff;
    }
    .btn-act.delete-review:hover { background: #b91c1c; box-shadow: 0 4px 12px rgba(220,38,38,.3); }

    .btn-act.reject-report {
      background: #ffffff; color: #1e293b; border: 1.5px solid #cbd5e1;
    }
    .btn-act.reject-report:hover { background: #f8fafc; border-color: #94a3b8; }

    .btn-act.request-evidence {
      background: #2563eb; color: #fff;
    }
    .btn-act.request-evidence:hover { background: #1d4ed8; box-shadow: 0 4px 12px rgba(37,99,235,.3); }

    .internal-notes-sec {
      margin-bottom: 20px;
    }
    .internal-notes-label {
      font-size: 0.85rem; font-weight: 600; color: #1e293b; margin-bottom: 6px; display: block;
    }
    .internal-notes-area {
      width: 100%; border: 1.5px solid #e2e8f0; border-radius: 10px; padding: 10px 12px;
      font-family: inherit; font-size: 0.85rem; color: #1e293b; resize: vertical; min-height: 70px;
      outline: none; transition: border-color .2s;
    }
    .internal-notes-area:focus { border-color: #2563eb; }
    .notes-counter-row {
      display: flex; justify-content: space-between; align-items: center; margin-top: 4px;
    }
    .char-count { font-size: 0.75rem; color: #94a3b8; }
    .btn-save-note {
      background: #f1f5f9; border: 1px solid #cbd5e1; border-radius: 6px;
      padding: 3px 10px; font-size: 0.75rem; font-weight: 600; cursor: pointer; color: #334155;
    }
    .btn-save-note:hover { background: #e2e8f0; }

    /* Timeline Log */
    .history-log-title {
      font-size: 0.85rem; font-weight: 600; color: #1e293b; margin-bottom: 12px;
    }
    .timeline {
      display: flex; flex-direction: column; gap: 12px;
    }
    .timeline-item {
      display: flex; align-items: flex-start; justify-content: space-between;
      font-size: 0.8rem; line-height: 1.35;
    }
    .timeline-left {
      display: flex; align-items: flex-start; gap: 8px;
    }
    .timeline-dot {
      width: 8px; height: 8px; border-radius: 50%; margin-top: 4px; flex-shrink: 0;
    }
    .timeline-dot.blue { background: #2563eb; }
    .timeline-dot.orange { background: #f97316; }
    .timeline-text { color: #334155; }
    .timeline-text strong { display: block; color: #0f172a; font-weight: 500; }
    .timeline-author { color: #64748b; font-size: 0.75rem; }

    /* ── Card 4: Order Details ── */
    .order-table {
      width: 100%; border-collapse: collapse; font-size: 0.875rem;
    }
    .order-table tr { border-bottom: 1px solid #f1f5f9; }
    .order-table tr:last-child { border-bottom: none; }
    .order-table td {
      padding: 10px 0; vertical-align: middle;
    }
    .order-table td.lbl {
      color: #64748b; font-weight: 500; width: 140px;
    }
    .order-table td.val {
      color: #0f172a; font-weight: 500; text-align: right;
    }
    .order-id-link {
      color: #2563eb; font-weight: 600; text-decoration: none;
    }
    .order-id-link:hover { text-decoration: underline; }
    .badge-delivered {
      background: #dcfce7; color: #15803d; font-size: 0.75rem;
      font-weight: 600; padding: 3px 8px; border-radius: 6px;
    }

    /* ── Card 5: Chat History Preview ── */
    .view-chat-link {
      color: #2563eb; font-size: 0.85rem; font-weight: 600; text-decoration: none;
    }
    .view-chat-link:hover { text-decoration: underline; }

    .chat-preview-list {
      display: flex; flex-direction: column; gap: 14px; margin-top: 4px;
    }
    .chat-bubble-row {
      display: flex; flex-direction: column; gap: 4px;
      font-size: 0.875rem;
    }
    .chat-sender-header {
      display: flex; align-items: center; gap: 8px; font-size: 0.8rem;
    }
    .sender-dot {
      width: 8px; height: 8px; border-radius: 50%;
    }
    .sender-dot.shop { background: #10b981; }
    .sender-dot.user { background: #3b82f6; }
    .sender-name { font-weight: 600; color: #0f172a; }
    .sender-time { color: #94a3b8; font-size: 0.75rem; }
    .chat-message-text {
      color: #334155; margin-left: 16px; line-height: 1.45;
    }

    /* ── Lightbox ── */
    #imageLightbox {
      display: none; position: fixed; inset: 0;
      background: rgba(0,0,0,.85); z-index: 99999;
      align-items: center; justify-content: center;
    }
    #imageLightbox.open { display: flex; }
    #imageLightbox img {
      max-width: 90vw; max-height: 85vh; border-radius: 12px;
      box-shadow: 0 10px 40px rgba(0,0,0,.5);
    }
    #imageLightbox .close-lb {
      position: absolute; top: 20px; right: 28px;
      color: #fff; font-size: 2.2rem; cursor: pointer; line-height: 1;
    }
  </style>
</head>
<body>

  <!-- ── Top Nav & Header ── -->
  <div class="top-nav">
    <div>
      <div class="breadcrumbs">
        <a href="${pageContext.request.contextPath}/admin/reviews">Reports Queue</a>
        <span>/</span>
        <strong>Report #RV-2026-${detail.reviewId}</strong>
      </div>
      <div class="page-title-row">
        <h1 class="page-title">Report #RV-2026-${detail.reviewId}</h1>
        <c:choose>
          <c:when test="${detail.reviewStatus == 'reported'}">
            <span class="status-badge pending">Pending Review</span>
          </c:when>
          <c:when test="${detail.reviewStatus == 'published'}">
            <span class="status-badge published">Published / Active</span>
          </c:when>
          <c:when test="${detail.reviewStatus == 'rejected'}">
            <span class="status-badge rejected">Hidden</span>
          </c:when>
          <c:when test="${detail.reviewStatus == 'deleted'}">
            <span class="status-badge deleted">Deleted</span>
          </c:when>
          <c:otherwise>
            <span class="status-badge pending">${detail.reviewStatus}</span>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="top-actions">
      <a href="?reviewId=${detail.reviewId}" class="btn-header">
        🔄 Refresh
      </a>
      <button class="btn-bell" title="Thông báo">🔔</button>
    </div>
  </div>

  <!-- ── Toast thông báo ── -->
  <c:if test="${not empty sessionScope.toast_success}">
    <div class="toast success">${sessionScope.toast_success}</div>
    <c:remove var="toast_success" scope="session"/>
  </c:if>
  <c:if test="${not empty sessionScope.toast_error}">
    <div class="toast error">${sessionScope.toast_error}</div>
    <c:remove var="toast_error" scope="session"/>
  </c:if>

  <!-- ── Top Grid (3 Cards) ── -->
  <div class="grid-top">

    <!-- ── CARD 1: Report Info ── -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          <span class="card-title-icon">📄</span> Report Info
        </h3>
      </div>

      <div class="info-list">
        <div class="info-row">
          <span class="info-label">Reporter</span>
          <span class="info-value">${detail.reporterName}</span>
        </div>
        <div class="info-row">
          <span class="info-label">Submitted</span>
          <span class="info-value">
            <fmt:formatDate value="${detail.reportDate}" pattern="dd/MM/yyyy HH:mm"/>
          </span>
        </div>
        <div class="info-row">
          <span class="info-label">Reason selected</span>
          <span class="info-value danger">${detail.reasonLabel}</span>
        </div>
      </div>

      <div class="seller-desc-title">Seller description</div>
      <div class="seller-desc-box">
        <c:choose>
          <c:when test="${not empty detail.sellerDescription}">
            ${detail.sellerDescription}
          </c:when>
          <c:otherwise>
            Review này có từ ngữ thô tục, thiếu tôn trọng và ảnh hưởng đến hình ảnh shop. Mong đội ngũ kiểm tra và xử lý.
          </c:otherwise>
        </c:choose>
      </div>

      <div class="evidence-title">
        Attached evidence (${detail.evidenceImages.size()})
      </div>
      <div class="evidence-gallery">
        <c:choose>
          <c:when test="${not empty detail.evidenceImages}">
            <c:forEach var="img" items="${detail.evidenceImages}" varStatus="loop">
              <div class="evidence-item" onclick="openLightbox('${pageContext.request.contextPath}/${img}')">
                <img src="${pageContext.request.contextPath}/${img}" alt="evidence ${loop.index + 1}"/>
                <span class="badge-num">${loop.index + 1}</span>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <span style="font-size: 0.85rem; color: #94a3b8; font-style: italic;">
              Không có ảnh bằng chứng đính kèm.
            </span>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- ── CARD 2: Original Review ── -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          <span class="card-title-icon">💬</span> Original Review
        </h3>
      </div>

      <!-- Rating stars -->
      <div class="review-stars">
        <c:forEach begin="1" end="${detail.rating}" var="s">
          <span class="star-filled">★</span>
        </c:forEach>
        <c:forEach begin="${detail.rating + 1}" end="5" var="s">
          <span class="star-empty">★</span>
        </c:forEach>
      </div>

      <!-- Review comment text -->
      <div class="review-quote">
        “${detail.reviewComment}”
      </div>

      <div class="review-meta-divider"></div>

      <div class="info-list" style="margin-bottom: 18px;">
        <div class="info-row">
          <span class="info-label">Reviewer</span>
          <span class="info-value">${detail.reviewerName}</span>
        </div>
        <div class="info-row">
          <span class="info-label">Date</span>
          <span class="info-value">
            <fmt:formatDate value="${detail.reviewDate}" pattern="dd/MM/yyyy"/>
          </span>
        </div>
      </div>

      <div class="evidence-title">
        Product images (${detail.reviewImages.size()})
      </div>
      <div class="review-images-row">
        <c:choose>
          <c:when test="${not empty detail.reviewImages}">
            <c:forEach var="pImg" items="${detail.reviewImages}">
              <div class="review-img-box" onclick="openLightbox('${pImg.startsWith('http') ? pImg : pageContext.request.contextPath.concat('/').concat(pImg)}')">
                <img src="${pImg.startsWith('http') ? pImg : pageContext.request.contextPath.concat('/').concat(pImg)}" alt="ảnh sản phẩm"/>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <span style="font-size: 0.85rem; color: #94a3b8; font-style: italic;">
              Khách hàng không đính kèm ảnh chụp.
            </span>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- ── CARD 3: Actions ── -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          <span class="card-title-icon">🛡️</span> Actions
        </h3>
      </div>

      <div class="action-buttons">
        <!-- Nút 1: Hide Review -->
        <form method="post" action="${pageContext.request.contextPath}/admin/reviews/hide"
              onsubmit="return confirm('Bạn có chắc chắn muốn ẩn đánh giá này khỏi trang sản phẩm?');">
          <input type="hidden" name="reviewId" value="${detail.reviewId}"/>
          <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/admin/reviews/detail?reviewId=${detail.reviewId}"/>
          <button type="submit" class="btn-act hide-review">
            👁 Hide Review
          </button>
        </form>

        <!-- Nút 2: Delete Review -->
        <form method="post" action="${pageContext.request.contextPath}/admin/reviews/delete"
              onsubmit="return confirm('Bạn có chắc chắn muốn xóa đánh giá này vĩnh viễn?');">
          <input type="hidden" name="reviewId" value="${detail.reviewId}"/>
          <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/admin/reviews/detail?reviewId=${detail.reviewId}"/>
          <button type="submit" class="btn-act delete-review">
            🗑 Delete Review
          </button>
        </form>

        <!-- Nút 3: Reject Report -->
        <form method="post" action="${pageContext.request.contextPath}/admin/reviews/reject-report"
              onsubmit="return confirm('Bác bỏ báo cáo và giữ lại đánh giá bình thường?');">
          <input type="hidden" name="reviewId" value="${detail.reviewId}"/>
          <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/admin/reviews/detail?reviewId=${detail.reviewId}"/>
          <button type="submit" class="btn-act reject-report">
            ⮹ Reject Report
          </button>
        </form>

        <!-- Nút 4: Request More Evidence -->
        <form method="post" action="${pageContext.request.contextPath}/admin/reviews/request-evidence"
              onsubmit="return confirm('Gửi yêu cầu cung cấp thêm bằng chứng vi phạm tới người báo cáo?');">
          <input type="hidden" name="reviewId" value="${detail.reviewId}"/>
          <input type="hidden" name="reporterId" value="${detail.reporterId}"/>
          <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/admin/reviews/detail?reviewId=${detail.reviewId}"/>
          <button type="submit" class="btn-act request-evidence">
            💬 Request More Evidence
          </button>
        </form>
      </div>

      <!-- Internal notes -->
      <form method="post" action="${pageContext.request.contextPath}/admin/reviews/save-notes" class="internal-notes-sec">
        <input type="hidden" name="reviewId" value="${detail.reviewId}"/>
        <input type="hidden" name="reportId" value="${detail.reportId}"/>
        <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/admin/reviews/detail?reviewId=${detail.reviewId}"/>

        <label class="internal-notes-label">Internal notes</label>
        <textarea name="internalNotes" id="internalNotesInput" class="internal-notes-area"
                  maxlength="500" placeholder="Add private notes for internal team only...">${detail.internalNotes}</textarea>
        <div class="notes-counter-row">
          <span class="char-count" id="notesCounter">0/500</span>
          <button type="submit" class="btn-save-note">Lưu ghi chú</button>
        </div>
      </form>

      <!-- History log -->
      <div class="history-log-title">History log</div>
      <div class="timeline">
        <div class="timeline-item">
          <div class="timeline-left">
            <span class="timeline-dot blue"></span>
            <div class="timeline-text">
              <strong>
                <fmt:formatDate value="${detail.reportDate}" pattern="dd/MM/yyyy HH:mm"/>
              </strong>
              Report submitted
            </div>
          </div>
          <span class="timeline-author">System</span>
        </div>

        <div class="timeline-item">
          <div class="timeline-left">
            <span class="timeline-dot orange"></span>
            <div class="timeline-text">
              <strong>
                <fmt:formatDate value="${detail.reviewDate}" pattern="dd/MM/yyyy HH:mm"/>
              </strong>
              Review posted
            </div>
          </div>
          <span class="timeline-author">System</span>
        </div>

        <div class="timeline-item">
          <div class="timeline-left">
            <span class="timeline-dot blue"></span>
            <div class="timeline-text">
              <strong>
                <fmt:formatDate value="${detail.deliveredDate}" pattern="dd/MM/yyyy HH:mm"/>
              </strong>
              Order delivered
            </div>
          </div>
          <span class="timeline-author">System</span>
        </div>
      </div>
    </div>

  </div>

  <!-- ── Bottom Grid (2 Cards) ── -->
  <div class="grid-bottom">

    <!-- ── CARD 4: Order Details ── -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          <span class="card-title-icon">🛒</span> Order Details
        </h3>
      </div>

      <table class="order-table">
        <tbody>
          <tr>
            <td class="lbl">Order ID</td>
            <td class="val">
              <a href="${pageContext.request.contextPath}/admin/orders?search=${detail.orderId}" class="order-id-link">
                ${detail.orderFormattedId}
              </a>
            </td>
          </tr>
          <tr>
            <td class="lbl">Product</td>
            <td class="val"><strong>${detail.productName}</strong></td>
          </tr>
          <tr>
            <td class="lbl">Variant</td>
            <td class="val">${detail.variant}</td>
          </tr>
          <tr>
            <td class="lbl">Price</td>
            <td class="val">
              <fmt:formatNumber value="${detail.price}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
            </td>
          </tr>
          <tr>
            <td class="lbl">Order Date</td>
            <td class="val">
              <fmt:formatDate value="${detail.orderDate}" pattern="dd/MM/yyyy HH:mm"/>
            </td>
          </tr>
          <tr>
            <td class="lbl">Status</td>
            <td class="val">
              <span class="badge-delivered">
                <c:choose>
                  <c:when test="${not empty detail.orderStatus}">${detail.orderStatus}</c:when>
                  <c:otherwise>Delivered</c:otherwise>
                </c:choose>
              </span>
            </td>
          </tr>
          <tr>
            <td class="lbl">Delivered Date</td>
            <td class="val">
              <fmt:formatDate value="${detail.deliveredDate}" pattern="dd/MM/yyyy HH:mm"/>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── CARD 5: Chat History Preview ── -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          <span class="card-title-icon">💬</span> Chat History Preview (Latest 3)
        </h3>
        <a href="javascript:void(0)" onclick="alert('Đang mở toàn bộ đoạn chat giữa khách hàng và người bán...')" class="view-chat-link">
          View full chat
        </a>
      </div>

      <div class="chat-preview-list">
        <c:forEach var="chat" items="${detail.chatHistory}">
          <div class="chat-bubble-row">
            <div class="chat-sender-header">
              <span class="sender-dot ${chat.senderType}"></span>
              <span class="sender-name">${chat.sender}</span>
              <span class="sender-time">${chat.time}</span>
            </div>
            <div class="chat-message-text">
              ${chat.message}
            </div>
          </div>
        </c:forEach>
      </div>
    </div>

  </div>

  <!-- ── Lightbox Preview ── -->
  <div id="imageLightbox" onclick="closeLightbox()">
    <span class="close-lb">&times;</span>
    <img id="lightboxImage" src="" alt="phóng to bằng chứng"/>
  </div>

  <script>
    // ── Internal notes character counter ──
    const notesInput = document.getElementById('internalNotesInput');
    const notesCounter = document.getElementById('notesCounter');
    function updateCounter() {
      notesCounter.textContent = notesInput.value.length + '/500';
    }
    if (notesInput) {
      notesInput.addEventListener('input', updateCounter);
      updateCounter();
    }

    // ── Lightbox functions ──
    function openLightbox(src) {
      const lb = document.getElementById('imageLightbox');
      const img = document.getElementById('lightboxImage');
      img.src = src;
      lb.classList.add('open');
    }
    function closeLightbox() {
      const lb = document.getElementById('imageLightbox');
      lb.classList.remove('open');
      document.getElementById('lightboxImage').src = '';
    }
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') closeLightbox();
    });
  </script>

</body>
</html>

