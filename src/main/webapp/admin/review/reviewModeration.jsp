<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Quản lý Đánh giá | Admin</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css"/>
  <style>
    /* ── Evidence thumbnails in admin table ── */
    .evidence-imgs { display: flex; gap: 4px; flex-wrap: wrap; }
    .evidence-imgs img {
      width: 44px; height: 44px; object-fit: cover; border-radius: 6px;
      border: 1.5px solid #c4b5fd; cursor: pointer; transition: transform .15s;
    }
    .evidence-imgs img:hover { transform: scale(1.1); }
    .no-evidence { color: #94a3b8; font-size: 0.75rem; }

    /* ── Lightbox ── */
    #evidenceLightbox {
      display: none; position: fixed; inset: 0;
      background: rgba(0,0,0,.85); z-index: 9999;
      align-items: center; justify-content: center;
    }
    #evidenceLightbox.open { display: flex; }
    #evidenceLightbox img {
      max-width: 90vw; max-height: 85vh;
      border-radius: 12px; box-shadow: 0 8px 40px rgba(0,0,0,.6);
    }
    #evidenceLightbox .lb-close {
      position: absolute; top: 16px; right: 24px;
      font-size: 2rem; color: #fff; cursor: pointer; line-height: 1;
    }
    .btn-detail {
      background: #4f46e5; color: #fff; text-decoration: none;
      display: inline-flex; align-items: center; gap: 4px;
      padding: 6px 12px; border-radius: 6px; font-size: 0.8rem; font-weight: 600;
      transition: background .15s;
    }
    .btn-detail:hover { background: #4338ca; }
  </style>
</head>
<body class="review-mod-page">

<!-- ── Top Bar ── -->
<div class="topbar">
  <div>
    <h1>🛡️ Quản lý Đánh giá</h1>
    <span>Review Moderation Dashboard</span>
  </div>
</div>

<!-- ── Toast thông báo ── -->
<c:if test="${not empty sessionScope.toast_success}">
  <div class="toast success" id="toastMsg">${sessionScope.toast_success}</div>
  <c:remove var="toast_success" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.toast_error}">
  <div class="toast error" id="toastMsg">${sessionScope.toast_error}</div>
  <c:remove var="toast_error" scope="session"/>
</c:if>

<div class="container">

  <!-- ── Filter tabs ── -->
  <div class="filter-bar">
    <a href="?status=" class="tab all ${empty param.status ? 'active' : ''}">Tất cả (${totalAll})</a>
    <a href="?status=reported"  class="tab reported ${param.status == 'reported'  ? 'active' : ''}">⚠️ Bị báo cáo</a>
    <a href="?status=rejected"  class="tab rejected ${param.status == 'rejected'  ? 'active' : ''}">🚫 Đã từ chối</a>
    <a href="?status=deleted"   class="tab deleted  ${param.status == 'deleted'   ? 'active' : ''}">🗑️ Đã xóa</a>
  </div>

  <!-- ── Table ── -->
  <div class="card">
    <c:choose>
      <c:when test="${empty reviews}">
        <div class="empty">
          <div class="icon">📋</div>
          <p>Không có đánh giá nào trong danh mục này.</p>
        </div>
      </c:when>
      <c:otherwise>
        <table>
          <thead>
            <tr>
              <th>#ID</th>
              <th>Sản phẩm</th>
              <th>Người dùng</th>
              <th>Sao</th>
              <th>Nội dung</th>
              <th>Trạng thái</th>
              <th>Báo cáo</th>
              <th>Bằng chứng</th>
              <th>Ngày tạo</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="r" items="${reviews}">
              <tr>
                <td>
                  <a href="${pageContext.request.contextPath}/admin/reviews/detail?reviewId=${r.review_id}"
                     style="color: #4f46e5; font-weight: 700; text-decoration: none;"
                     title="Nhấp để mở bảng kiểm duyệt báo cáo chi tiết">
                    #${r.review_id}
                  </a>
                </td>
                <td>${r.productName}</td>
                <td>${r.userName}</td>
                <td>
                  <span class="stars">
                    <c:forEach begin="1" end="${r.rating}" var="s">★</c:forEach>
                    <c:forEach begin="${r.rating + 1}" end="5" var="s">☆</c:forEach>
                  </span>
                </td>
                <td><div class="comment-preview">${r.comment}</div></td>
                <td>
                  <span class="badge ${r.status}">
                    <c:choose>
                      <c:when test="${r.status == 'published'}">✅ Đã đăng</c:when>
                      <c:when test="${r.status == 'reported'}">⚠️ Bị báo cáo</c:when>
                      <c:when test="${r.status == 'rejected'}">🚫 Từ chối</c:when>
                      <c:when test="${r.status == 'deleted'}">🗑️ Đã xóa</c:when>
                    </c:choose>
                  </span>
                </td>
                <td><span class="report-count">${r.reportCount}</span></td>

                <%-- ── Cột Bằng chứng ── --%>
                <td>
                  <c:choose>
                    <c:when test="${not empty r.evidenceUrls}">
                      <div class="evidence-imgs">
                        <%-- Split CSV và render từng ảnh --%>
                        <c:forTokens var="imgUrl" items="${r.evidenceUrls}" delims=",">
                          <img src="${pageContext.request.contextPath}/${imgUrl}"
                               alt="bằng chứng"
                               onclick="openLightbox('${pageContext.request.contextPath}/${imgUrl}')"
                               title="Nhấp để xem ảnh đầy đủ"/>
                        </c:forTokens>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <span class="no-evidence">—</span>
                    </c:otherwise>
                  </c:choose>
                </td>

                <td>
                  <fmt:formatDate value="${r.created_at}" pattern="dd/MM/yyyy"/>
                </td>
                <td>
                  <div class="actions">
                    <a href="${pageContext.request.contextPath}/admin/reviews/detail?reviewId=${r.review_id}"
                       class="btn-detail"
                       title="Xem xét chi tiết báo cáo vi phạm">
                      🔍 Xem xét
                    </a>
                    <c:if test="${r.status != 'published'}">
                      <button class="btn btn-approve"
                              onclick="confirmAction('approve', ${r.review_id}, 'duyệt')">
                        Duyệt
                      </button>
                    </c:if>
                    <c:if test="${r.status != 'rejected'}">
                      <button class="btn btn-reject"
                              onclick="confirmAction('reject', ${r.review_id}, 'từ chối')">
                        Từ chối
                      </button>
                    </c:if>
                    <c:if test="${r.status != 'deleted'}">
                      <button class="btn btn-delete"
                              onclick="confirmAction('delete', ${r.review_id}, 'xóa')">
                        Xóa
                      </button>
                    </c:if>
                  </div>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>

  <!-- ── Phân trang ── -->
  <c:if test="${totalPages > 1}">
    <div class="pagination">
      <c:if test="${page > 1}">
        <a href="?status=${param.status}&page=${page - 1}" class="page-btn">← Trước</a>
      </c:if>
      <c:forEach begin="1" end="${totalPages}" var="p">
        <a href="?status=${param.status}&page=${p}"
           class="page-btn ${p == page ? 'active' : ''}">${p}</a>
      </c:forEach>
      <c:if test="${page < totalPages}">
        <a href="?status=${param.status}&page=${page + 1}" class="page-btn">Sau →</a>
      </c:if>
    </div>
  </c:if>

</div>

<!-- ── Confirm Modal ── -->
<div class="modal-overlay" id="confirmModal">
  <div class="modal">
    <h3 id="modalTitle">Xác nhận hành động</h3>
    <p id="modalDesc">Bạn có chắc chắn muốn thực hiện hành động này không?</p>
    <div class="modal-actions">
      <button class="btn btn-delete" onclick="closeModal()">Hủy</button>
      <form id="actionForm" method="post">
        <input type="hidden" name="reviewId" id="modalReviewId"/>
        <button type="submit" class="btn" id="modalConfirmBtn">Xác nhận</button>
      </form>
    </div>
  </div>
</div>

<!-- ── Evidence Lightbox ── -->
<div id="evidenceLightbox">
  <span class="lb-close" onclick="closeLightbox()">✕</span>
  <img id="lightboxImg" src="" alt="bằng chứng đầy đủ"/>
</div>

<script>
  // ── Toast auto-hide ──
  const toast = document.getElementById('toastMsg');
  if (toast) setTimeout(() => toast.style.opacity = '0', 3500);

  // ── Confirm modal ──
  const ctx = '${pageContext.request.contextPath}';

  function confirmAction(action, reviewId, label) {
    document.getElementById('modalTitle').textContent = 'Xác nhận ' + label + ' đánh giá';
    document.getElementById('modalDesc').textContent  =
      'Bạn có chắc chắn muốn ' + label + ' đánh giá #' + reviewId + ' không?';
    document.getElementById('modalReviewId').value = reviewId;
    document.getElementById('actionForm').action    = ctx + '/admin/reviews/' + action;

    const btn = document.getElementById('modalConfirmBtn');
    btn.className = 'btn ' + (action === 'approve' ? 'btn-approve' : action === 'reject' ? 'btn-reject' : 'btn-delete');
    btn.textContent = action === 'approve' ? 'Duyệt' : action === 'reject' ? 'Từ chối' : 'Xóa';

    document.getElementById('confirmModal').classList.add('open');
  }

  function closeModal() {
    document.getElementById('confirmModal').classList.remove('open');
  }

  // Đóng modal khi click ngoài
  document.getElementById('confirmModal').addEventListener('click', function(e) {
    if (e.target === this) closeModal();
  });

  // ── Evidence Lightbox ──
  function openLightbox(src) {
    document.getElementById('lightboxImg').src = src;
    document.getElementById('evidenceLightbox').classList.add('open');
  }

  function closeLightbox() {
    document.getElementById('evidenceLightbox').classList.remove('open');
    document.getElementById('lightboxImg').src = '';
  }

  document.getElementById('evidenceLightbox').addEventListener('click', function(e) {
    if (e.target === this) closeLightbox();
  });
</script>
</body>
</html>
