<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, com.mycompany.onlinefruitshopping.model.Review, java.text.DecimalFormat, java.text.SimpleDateFormat" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh Giá Sản Phẩm – FreshFruit</title>
    <meta name="description" content="Xem tất cả đánh giá thực tế từ khách hàng đã mua sản phẩm tại FreshFruit.">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">
</head>
<body>

<%-- ===== Lấy dữ liệu từ ViewReviewServlet ===== --%>
<%
    List<Review>  reviewList    = (List<Review>)  request.getAttribute("reviewList");
    double[]      stats         = (double[])       request.getAttribute("stats");
    Integer       productId     = (Integer)        request.getAttribute("productId");
    Integer       currentPage   = (Integer)        request.getAttribute("currentPage");
    Integer       totalPages    = (Integer)        request.getAttribute("totalPages");
    Integer       totalCount    = (Integer)        request.getAttribute("totalCount");
    Integer       filter        = (Integer)        request.getAttribute("filter");
    Boolean       reviewSent    = (Boolean)        request.getAttribute("reviewSent");
    Boolean       reviewUpdated = (Boolean)        request.getAttribute("reviewUpdated");
    Boolean       reviewDeleted = (Boolean)        request.getAttribute("reviewDeleted");
    Boolean       expired       = (Boolean)        request.getAttribute("expired");
    Boolean       authError     = (Boolean)        request.getAttribute("authError");
    Boolean       deleteError   = (Boolean)        request.getAttribute("deleteError");

    Integer sessionUserId = (Integer) session.getAttribute("userId");

    // Giá trị mặc định tránh NullPointerException
    if (reviewList    == null) reviewList    = new java.util.ArrayList<>();
    if (stats         == null) stats         = new double[]{0,0,0,0,0,0,0};
    if (productId     == null) productId     = 0;
    if (currentPage   == null) currentPage   = 1;
    if (totalPages    == null) totalPages    = 1;
    if (totalCount    == null) totalCount    = 0;
    if (filter        == null) filter        = 0;
    if (reviewSent    == null) reviewSent    = false;
    if (reviewUpdated == null) reviewUpdated = false;
    if (reviewDeleted == null) reviewDeleted = false;
    if (expired       == null) expired       = false;
    if (authError     == null) authError     = false;
    if (deleteError   == null) deleteError   = false;

    // Định dạng điểm trung bình
    DecimalFormat df = new DecimalFormat("0.0");
    String avgStr  = df.format(stats[0]);
    int total      = (int) stats[1];
    int c5 = (int) stats[2], c4 = (int) stats[3],
        c3 = (int) stats[4], c2 = (int) stats[5], c1 = (int) stats[6];

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    String baseUrl = request.getContextPath() + "/ViewReviewServlet?productId=" + productId;
    String productName = (String) request.getAttribute("productName");
    String productImage = (String) request.getAttribute("productImage");
    if (productName == null || productName.trim().isEmpty()) {
        productName = "Sản phẩm #" + productId;
    }
%>

<%-- ===== TOAST MESSAGES ===== --%>
<% if (Boolean.TRUE.equals(reviewSent)) { %>
<div id="toastMsg" class="toast-msg toast-success show" role="status" aria-live="polite">
    <i class="fa-solid fa-circle-check"></i>
    <span>Đánh giá của bạn đã được gửi thành công và đang chờ kiểm duyệt!</span>
</div>
<% } else if (Boolean.TRUE.equals(reviewUpdated)) { %>
<div id="toastMsg" class="toast-msg toast-success show" role="status" aria-live="polite">
    <i class="fa-solid fa-circle-check"></i>
    <span>Đánh giá của bạn đã được cập nhật thành công!</span>
</div>
<% } else if (Boolean.TRUE.equals(reviewDeleted)) { %>
<div id="toastMsg" class="toast-msg toast-success show" role="status" aria-live="polite">
    <i class="fa-solid fa-circle-check"></i>
    <span>Đã xóa đánh giá thành công!</span>
</div>
<% } else if (Boolean.TRUE.equals(deleteError)) { %>
<div id="toastMsg" class="toast-msg toast-error show" role="status" aria-live="polite">
    <i class="fa-solid fa-circle-xmark"></i>
    <span>Có lỗi xảy ra khi xóa đánh giá. Vui lòng thử lại sau.</span>
</div>
<% } else if (Boolean.TRUE.equals(expired)) { %>
<div id="toastMsg" class="toast-msg toast-error show" role="status" aria-live="polite">
    <i class="fa-solid fa-triangle-exclamation"></i>
    <span>Đánh giá đã quá thời hạn 7 ngày nên không thể chỉnh sửa.</span>
</div>
<% } else if (Boolean.TRUE.equals(authError)) { %>
<div id="toastMsg" class="toast-msg toast-error show" role="status" aria-live="polite">
    <i class="fa-solid fa-circle-xmark"></i>
    <span>Bạn không có quyền thực hiện thao tác này.</span>
</div>
<% } %>

<%-- ===== BREADCRUMB ===== --%>
<div class="breadcrumb-bar">
    <div class="container">
        <ol class="breadcrumb-list">
            <li><a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-house"></i> Trang chủ</a></li>
            <li><span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span></li>
            <li><a href="${pageContext.request.contextPath}/products">Trái cây</a></li>
            <li><span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span></li>
            <li><a href="${pageContext.request.contextPath}/product?id=<%= productId %>"><%= productName %></a></li>
            <li><span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span></li>
            <li class="active">Đánh giá</li>
        </ol>
    </div>
</div>

<%-- ===== MAIN CONTENT ===== --%>
<main>
    <div class="container">
        <div class="page-layout">

            <%-- ===== LEFT: REVIEW SUMMARY + LIST ===== --%>
            <div class="review-main-col">

                <%-- ===== THÔNG TIN SẢN PHẨM ĐANG XEM ĐÁNH GIÁ ===== --%>
                <div class="page-card" style="margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 16px 24px;">
                    <div style="display: flex; align-items: center; gap: 16px;">
                        <% if (productImage != null && !productImage.isEmpty()) { %>
                            <img src="<%= productImage.startsWith("http") ? productImage : (request.getContextPath() + "/" + productImage) %>"
                                 alt="<%= productName %>"
                                 style="width: 70px; height: 70px; object-fit: cover; border-radius: 12px; border: 1px solid #e2e8f0;"
                                 onerror="this.src='https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=200&q=80'">
                        <% } else { %>
                            <img src="https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=200&q=80"
                                 alt="<%= productName %>"
                                 style="width: 70px; height: 70px; object-fit: cover; border-radius: 12px; border: 1px solid #e2e8f0;">
                        <% } %>
                        <div>
                            <h2 style="font-size: 1.25rem; font-weight: 700; color: #1e293b; margin: 0 0 6px 0;">
                                <a href="${pageContext.request.contextPath}/product?id=<%= productId %>" style="color: inherit; text-decoration: none;">
                                    <%= productName %>
                                </a>
                            </h2>
                            <div style="font-size: 0.9rem; color: #64748b;">
                                <i class="fa-solid fa-circle-check" style="color: #10b981; margin-right: 4px;"></i> Sản phẩm chính hãng • Nông sản sạch từ nông trại
                            </div>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/product?id=<%= productId %>" class="review-filter-btn" style="white-space: nowrap;">
                        <i class="fa-solid fa-arrow-left"></i> Xem chi tiết sản phẩm
                    </a>
                </div>

                <%-- ===== RATING SUMMARY CHART ===== --%>
                <div class="review-summary-box">
                    <div class="review-avg-score">
                        <div class="review-avg-number"><%= avgStr %></div>
                        <div class="review-avg-stars">
                            <%
                                double avg = stats[0];
                                for (int s = 1; s <= 5; s++) {
                                    if (avg >= s) {
                            %>
                                <i class="fa-solid fa-star"></i>
                            <%  } else if (avg >= s - 0.5) { %>
                                <i class="fa-solid fa-star-half-stroke"></i>
                            <%  } else { %>
                                <i class="fa-regular fa-star"></i>
                            <%  } } %>
                        </div>
                        <div class="review-avg-count"><%= total %> lượt đánh giá</div>
                    </div>

                    <div class="review-bar-list">
                        <%
                            int[][] bars = {{5, c5}, {4, c4}, {3, c3}, {2, c2}, {1, c1}};
                            for (int[] bar : bars) {
                                int pct = total > 0 ? bar[1] * 100 / total : 0;
                        %>
                        <div class="review-bar-item">
                            <span class="review-bar-label">
                                <i class="fa-solid fa-star star-gold"></i> <%= bar[0] %>
                            </span>
                            <div class="review-bar-track">
                                <div class="review-bar-fill" data-pct="<%= pct %>"></div>
                            </div>
                            <span class="review-bar-count"><%= bar[1] %></span>
                        </div>
                        <% } %>
                    </div>
                </div>

                <%-- ===== FILTER TABS ===== --%>
                <div class="review-filter-bar" role="group" aria-label="Lọc đánh giá">
                    <a href="<%= baseUrl %>&filter=0"  class="review-filter-btn <%= filter == 0  ? "active" : "" %>">Tất cả (<%= total %>)</a>
                    <a href="<%= baseUrl %>&filter=5"  class="review-filter-btn <%= filter == 5  ? "active" : "" %>"><i class="fa-solid fa-star star-gold"></i> 5 sao (<%= c5 %>)</a>
                    <a href="<%= baseUrl %>&filter=4"  class="review-filter-btn <%= filter == 4  ? "active" : "" %>"><i class="fa-solid fa-star star-gold"></i> 4 sao (<%= c4 %>)</a>
                    <a href="<%= baseUrl %>&filter=3"  class="review-filter-btn <%= filter == 3  ? "active" : "" %>"><i class="fa-solid fa-star star-gold"></i> 3 sao (<%= c3 %>)</a>
                    <a href="<%= baseUrl %>&filter=2"  class="review-filter-btn <%= filter == 2  ? "active" : "" %>"><i class="fa-solid fa-star star-gold"></i> 2 sao (<%= c2 %>)</a>
                    <a href="<%= baseUrl %>&filter=1"  class="review-filter-btn <%= filter == 1  ? "active" : "" %>"><i class="fa-solid fa-star star-gold"></i> 1 sao (<%= c1 %>)</a>
                    <a href="<%= baseUrl %>&filter=-1" class="review-filter-btn <%= filter == -1 ? "active" : "" %>"><i class="fa-regular fa-image"></i> Có hình ảnh</a>
                </div>

                <%-- ===== REVIEW LIST ===== --%>
                <div class="review-list">
                    <% if (reviewList.isEmpty()) { %>
                    <div class="notice-box notice-info">
                        <span class="notice-icon"><i class="fa-solid fa-circle-info"></i></span>
                        <span>Chưa có đánh giá nào cho bộ lọc này. Hãy là người đầu tiên đánh giá!</span>
                    </div>
                    <% } else {
                        for (Review r : reviewList) {
                            String uName     = (r.getUserName() != null && !r.getUserName().trim().isEmpty())
                                               ? r.getUserName().trim() : "Khách hàng";
                            String initials  = uName.substring(0, 1).toUpperCase();
                            java.util.Date rDate = r.getCreated_at() != null ? r.getCreated_at() : r.getCreate_at();
                            String dateStr   = (rDate != null) ? sdf.format(rDate) : "";
                            boolean isOwner  = (sessionUserId != null && sessionUserId.equals(r.getUser_id()));
                    %>
                    <div class="review-item-card">
                        <div class="review-item-header">
                            <div class="review-item-user">
                                <div class="review-item-avatar"><%= initials %></div>
                                <div class="review-item-user-info">
                                    <div class="user-name">
                                        <%= uName %>
                                        <% if (isOwner) { %>
                                            <span class="user-order">(Đánh giá của bạn)</span>
                                        <% } %>
                                    </div>
                                    <div class="user-order">
                                        <i class="fa-solid fa-receipt"></i>
                                        <% if (r.getOrder_id() > 0) { %>
                                            Đơn hàng #<%= r.getOrder_id() %> &nbsp;|&nbsp; Đã nhận hàng
                                        <% } else { %>
                                            Đã mua hàng thực tế
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                            <div>
                                <div class="review-item-stars">
                                    <%
                                        int ratingVal = r.getRating() != null ? r.getRating() : 0;
                                        for (int s = 1; s <= 5; s++) {
                                    %>
                                        <% if (s <= ratingVal) { %>
                                            <i class="fa-solid fa-star"></i>
                                        <% } else { %>
                                            <i class="fa-regular fa-star"></i>
                                        <% } %>
                                    <% } %>
                                </div>
                                <div class="review-item-date"><%= dateStr %></div>
                            </div>
                        </div>

                        <%-- Tiêu đề đánh giá (nếu có) --%>
                        <% if (r.getTitle() != null && !r.getTitle().trim().isEmpty()) { %>
                        <div class="review-item-title"><%= r.getTitle() %></div>
                        <% } %>

                        <%-- Nội dung đánh giá --%>
                        <div class="review-item-comment"><%= r.getComment() %></div>

                        <%-- Hiển thị ảnh nếu có --%>
                        <% if (r.getImageUrls() != null && !r.getImageUrls().isEmpty()) {
                               String[] imgArr = r.getImageUrls().split(",");
                        %>
                        <div class="review-item-images">
                            <% for (String imgUrl : imgArr) {
                                   imgUrl = imgUrl.trim();
                                   if (!imgUrl.isEmpty()) {
                            %>
                            <img class="review-item-img"
                                 src="${pageContext.request.contextPath}/<%= imgUrl %>"
                                 alt="Ảnh đánh giá sản phẩm"
                                 loading="lazy">
                            <% } } %>
                        </div>
                        <% } %>

                        <div class="review-item-footer">
                            <div class="review-helpful">
                                <span>Hữu ích không?</span>
                                <button class="btn-helpful" onclick="markHelpful(this)" title="Đánh dấu hữu ích">
                                    <i class="fa-regular fa-thumbs-up"></i> <span>0</span>
                                </button>

                                <%-- Các nút "Sửa" và "Xóa" nếu người dùng là tác giả --%>
                                <% if (isOwner) {
                                       if (r.isEditable()) {
                                %>
                                <a href="${pageContext.request.contextPath}/EditReviewServlet?reviewId=<%= r.getReview_id() %>"
                                   class="btn-edit-review"
                                   title="Chỉnh sửa đánh giá trong vòng 7 ngày (còn <%= r.getRemainingDays() %> ngày)">
                                    <i class="fa-solid fa-pen-to-square"></i> Sửa
                                </a>
                                <%     } else { %>
                                <span class="review-item-date" title="Đã quá 7 ngày kể từ ngày đăng">
                                    <i class="fa-solid fa-lock"></i> Hết hạn sửa
                                </span>
                                <%     } %>

                                <%-- Nút "Xóa" hiển thị popup xác nhận --%>
                                <button type="button"
                                        class="btn-delete-review"
                                        data-review-id="<%= r.getReview_id() %>"
                                        data-product-id="<%= r.getProduct_id() %>"
                                        data-rating="<%= r.getRating() %>"
                                        data-comment="<%= r.getComment() != null ? r.getComment().replace("\"", "&quot;").replace("\n", " ").replace("\r", "") : "" %>"
                                        onclick="openDeleteModal(this)"
                                        title="Xóa đánh giá này">
                                    <i class="fa-solid fa-trash-can"></i> Xóa
                                </button>
                                <% } %>

                                <%-- Nút "Báo cáo" – chỉ hiển thị nếu KHÔNG phải tác giả và đã đăng nhập --%>
                                <% if (!isOwner && sessionUserId != null) { %>
                                <button type="button"
                                        class="btn-report-review"
                                        onclick="openReportModal(<%= r.getReview_id() %>, window.location.href)"
                                        title="Báo cáo đánh giá không phù hợp">
                                    <i class="fa-solid fa-flag"></i> Báo cáo
                                </button>
                                <% } %>
                            </div>
                            <span class="review-verified-badge">
                                <i class="fa-solid fa-shield-check"></i> Đã xác thực mua hàng
                            </span>
                        </div>
                    </div>
                    <%  } // end for
                       } // end else %>
                </div>
                <%-- /review-list --%>

                <%-- ===== PHÂN TRANG ===== --%>
                <% if (totalPages > 1) { %>
                <div class="pagination-nav">
                    <% if (currentPage > 1) { %>
                    <a href="<%= baseUrl %>&filter=<%= filter %>&page=<%= currentPage - 1 %>"
                       class="review-filter-btn">
                        <i class="fa-solid fa-chevron-left"></i>
                    </a>
                    <% } %>

                    <% for (int p = 1; p <= totalPages; p++) { %>
                    <a href="<%= baseUrl %>&filter=<%= filter %>&page=<%= p %>"
                       class="review-filter-btn <%= p == currentPage ? "active" : "" %>">
                        <%= p %>
                    </a>
                    <% } %>

                    <% if (currentPage < totalPages) { %>
                    <a href="<%= baseUrl %>&filter=<%= filter %>&page=<%= currentPage + 1 %>"
                       class="review-filter-btn">
                        Tiếp <i class="fa-solid fa-chevron-right"></i>
                    </a>
                    <% } %>
                </div>
                <% } %>

            </div>
            <%-- /review-main-col --%>

            <%-- ===== RIGHT: SIDEBAR ===== --%>
            <aside>
                <div class="review-sidebar-cta">
                    <i class="fa-solid fa-pen-to-square sidebar-cta-icon"></i>
                    <h4>Bạn đã mua sản phẩm này?</h4>
                    <p>Chia sẻ cảm nhận thực tế của bạn để giúp những khách hàng khác mua sắm tốt hơn.</p>
                    <a href="${pageContext.request.contextPath}/write-review?productId=<%= productId %>"
                       class="btn-submit-review btn-cta-submit">
                        <i class="fa-solid fa-star"></i> Viết đánh giá của bạn
                    </a>
                </div>

                <div class="page-card">
                    <div class="page-card-title">
                        <span class="card-title-icon"><i class="fa-solid fa-shield-halved"></i></span>
                        Cam kết đánh giá
                    </div>
                    <ul class="sidebar-commitments-list">
                        <li class="sidebar-commitment-item">
                            <i class="fa-solid fa-circle-check"></i>
                            <span>100% đánh giá đến từ khách hàng đã mua và nhận hàng thực tế.</span>
                        </li>
                        <li class="sidebar-commitment-item">
                            <i class="fa-solid fa-circle-check"></i>
                            <span>FreshFruit không chỉnh sửa hay xoá đánh giá trung thực.</span>
                        </li>
                        <li class="sidebar-commitment-item">
                            <i class="fa-solid fa-circle-check"></i>
                            <span>Khách hàng có thể chỉnh sửa đánh giá trong vòng <strong>7 ngày</strong> kể từ khi đăng.</span>
                        </li>
                        <li class="sidebar-commitment-item">
                            <i class="fa-solid fa-circle-check"></i>
                            <span>Đánh giá được kiểm duyệt trong vòng 24h trước khi xuất hiện.</span>
                        </li>
                    </ul>
                </div>
            </aside>

        </div>
    </div>
</main>

<%-- ===== MODAL XÁC NHẬN XÓA ĐÁNH GIÁ ===== --%>
<div id="deleteReviewModal" class="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
    <div class="delete-modal-box">
        <div class="delete-modal-header">
            <div class="delete-modal-icon-wrap">
                <i class="fa-solid fa-triangle-exclamation"></i>
            </div>
            <div class="delete-modal-title" id="modalTitle">Xác nhận xóa đánh giá</div>
            <button type="button" class="delete-modal-close" onclick="closeDeleteModal()" aria-label="Đóng popup">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>
        <div class="delete-modal-body">
            <p class="delete-modal-desc">
                Bạn có chắc chắn muốn xóa đánh giá này không? <strong>Hành động này không thể hoàn tác.</strong>
            </p>
            <div class="delete-review-summary">
                <div class="delete-review-stars" id="modalReviewStars"></div>
                <div class="delete-review-excerpt" id="modalReviewExcerpt"></div>
            </div>
        </div>
        <form id="deleteReviewForm" action="${pageContext.request.contextPath}/DeleteReviewServlet" method="post">
            <input type="hidden" name="review_id" id="deleteReviewId" value="">
            <input type="hidden" name="product_id" id="deleteProductId" value="">
            <div class="delete-modal-actions">
                <button type="button" class="btn-modal-cancel" onclick="closeDeleteModal()">
                    Hủy
                </button>
                <button type="submit" id="btnConfirmDelete" class="btn-modal-delete">
                    <i class="fa-solid fa-trash-can"></i> Xóa
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    /* -------- Tự động ẩn Toast & Render thanh thống kê sao -------- */
    window.addEventListener('DOMContentLoaded', function() {
        // Render thanh tiến trình sao
        document.querySelectorAll('.review-bar-fill[data-pct]').forEach(function(bar) {
            bar.style.width = (bar.getAttribute('data-pct') || '0') + '%';
        });

        // Toast thông báo
        var toast = document.getElementById('toastMsg');
        if (toast && toast.classList.contains('show')) {
            setTimeout(function() {
                toast.classList.remove('show');
            }, 5000);
        }
    });

    /* -------- MARK HELPFUL (client-side only) -------- */
    function markHelpful(btn) {
        if (btn.dataset.voted) return;
        btn.dataset.voted = '1';
        var countEl = btn.querySelector('span');
        countEl.textContent = parseInt(countEl.textContent || '0') + 1;
        var icon = btn.querySelector('i');
        if (icon) icon.classList.replace('fa-regular', 'fa-solid');
        btn.classList.add('voted');
    }

    /* -------- MODAL XÁC NHẬN XÓA ĐÁNH GIÁ -------- */
    function openDeleteModal(btn) {
        var reviewId  = btn.getAttribute('data-review-id');
        var productId = btn.getAttribute('data-product-id');
        var rating    = parseInt(btn.getAttribute('data-rating') || '5', 10);
        var comment   = btn.getAttribute('data-comment') || '';

        var idInput = document.getElementById('deleteReviewId');
        var prodInput = document.getElementById('deleteProductId');
        if (idInput) idInput.value = reviewId;
        if (prodInput) prodInput.value = productId;

        // Render các ngôi sao
        var starsHtml = '';
        for (var s = 1; s <= 5; s++) {
            starsHtml += (s <= rating)
                ? '<i class="fa-solid fa-star"></i>'
                : '<i class="fa-regular fa-star"></i>';
        }
        var starsEl = document.getElementById('modalReviewStars');
        if (starsEl) starsEl.innerHTML = starsHtml;

        // Render tóm tắt nội dung 1-2 dòng
        var excerptEl = document.getElementById('modalReviewExcerpt');
        if (excerptEl) {
            excerptEl.textContent = comment ? ('“' + comment + '”') : '“Không có nội dung bình luận”';
        }

        // Hiển thị modal
        var modal = document.getElementById('deleteReviewModal');
        if (modal) modal.classList.add('show');
    }

    function closeDeleteModal() {
        var modal = document.getElementById('deleteReviewModal');
        if (modal) modal.classList.remove('show');
    }

    // Đóng khi click ra ngoài backdrop
    window.addEventListener('click', function(e) {
        var modal = document.getElementById('deleteReviewModal');
        if (e.target === modal) {
            closeDeleteModal();
        }
    });

    // Đóng khi nhấn phím Escape
    window.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeDeleteModal();
        }
    });
</script>

</body>

<%-- ===== REPORT REVIEW MODAL ===== --%>
<jsp:include page="/review/reportReviewModal.jsp"/>

<style>
  /* Nút báo cáo */
  .btn-report-review {
    display: inline-flex; align-items: center; gap: 5px;
    padding: 5px 12px; border: 1.5px solid #fca5a5; border-radius: 8px;
    background: transparent; color: #ef4444; font-size: 0.8rem;
    font-weight: 500; cursor: pointer; transition: all .2s;
    font-family: inherit;
  }
  .btn-report-review:hover {
    background: #fee2e2; border-color: #ef4444;
  }
</style>

</html>
