<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.onlinefruitshopping.model.Review, java.text.SimpleDateFormat" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh Sửa Đánh Giá – FreshFruit</title>
    <meta name="description" content="Chỉnh sửa đánh giá sản phẩm đã mua tại FreshFruit trong vòng 7 ngày.">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">
</head>
<body>

<%-- ===== Lấy dữ liệu Review từ Servlet ===== --%>
<%
    Review review = (Review) request.getAttribute("review");
    String errorMsg = (String) request.getAttribute("errorMsg");

    // Nếu người dùng truy cập trực tiếp file JSP mà không qua Servlet
    if (review == null) {
        response.sendRedirect(request.getContextPath() + "/home");
        return;
    }

    int ratingVal = (review.getRating() != null) ? review.getRating() : 5;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    String createDateStr = review.getCreate_at() != null ? sdf.format(review.getCreate_at()) : "";
    long remainingDays = review.getRemainingDays();

    // Danh sách ảnh cũ
    String[] oldImages = new String[0];
    if (review.getImageUrls() != null && !review.getImageUrls().trim().isEmpty()) {
        oldImages = review.getImageUrls().split(",");
    }
%>

<%-- ===== BREADCRUMB ===== --%>
<div class="breadcrumb-bar">
    <div class="container">
        <ol class="breadcrumb-list">
            <li><a href="${pageContext.request.contextPath}/home.jsp"><i class="fa-solid fa-house"></i> Trang chủ</a></li>
            <li><span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span></li>
            <li>
                <a href="${pageContext.request.contextPath}/ViewReviewServlet?productId=<%= review.getProduct_id() %>">
                    Đánh giá sản phẩm
                </a>
            </li>
            <li><span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span></li>
            <li class="active">Chỉnh sửa đánh giá</li>
        </ol>
    </div>
</div>

<%-- ===== MAIN CONTENT ===== --%>
<main>
    <div class="container">
        <div class="page-layout-full">

            <%-- ===== HỘP THÔNG BÁO THỜI HẠN 7 NGÀY ===== --%>
            <div class="notice-countdown">
                <div class="notice-countdown-icon">
                    <i class="fa-solid fa-stopwatch"></i>
                </div>
                <div class="notice-countdown-content">
                    <strong>Quy định chỉnh sửa:</strong> Bạn chỉ có thể chỉnh sửa đánh giá trong vòng <strong>7 ngày</strong> kể từ ngày đăng.
                    (Đăng lúc: <%= createDateStr %>).
                </div>
                <div class="notice-countdown-badge">
                    <i class="fa-solid fa-clock"></i> Còn <%= remainingDays %> ngày
                </div>
            </div>

            <%-- ===== CARD CHÍNH CỦA FORM ===== --%>
            <div class="page-card">

                <h1 class="page-card-title">
                    <span class="card-title-icon"><i class="fa-solid fa-pen-to-square"></i></span>
                    Chỉnh Sửa Đánh Giá Sản Phẩm
                </h1>

                <%-- ===== THÔNG TIN SẢN PHẨM ĐANG ĐƯỢC ĐÁNH GIÁ ===== --%>
                <div class="edit-product-header">
                    <img class="edit-product-thumb"
                         src="<%= review.getProductImage() != null ? review.getProductImage() : "https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=200&q=80" %>"
                         alt="<%= review.getProductName() %>"
                         onerror="this.src='${pageContext.request.contextPath}/images/no-image.png'">
                    <div class="edit-product-info">
                        <div class="edit-product-title"><%= review.getProductName() %></div>
                        <div class="edit-product-meta">
                            <span class="edit-product-order">
                                <i class="fa-solid fa-receipt"></i> Đơn hàng #<%= review.getOrder_id() %>
                            </span>
                            <span><i class="fa-solid fa-user-check"></i> Người đánh giá: <%= review.getUserName() != null ? review.getUserName() : "Tôi" %></span>
                        </div>
                    </div>
                </div>

                <%-- ===== THÔNG BÁO LỖI (NẾU CÓ) ===== --%>
                <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
                <div class="notice-box notice-warning">
                    <span class="notice-icon"><i class="fa-solid fa-triangle-exclamation"></i></span>
                    <span><%= errorMsg %></span>
                </div>
                <% } %>

                <%-- ===== FORM CHỈNH SỬA ===== --%>
                <form id="editReviewForm"
                      action="${pageContext.request.contextPath}/EditReviewServlet"
                      method="post"
                      enctype="multipart/form-data"
                      novalidate>

                    <%-- Hidden ID params --%>
                    <input type="hidden" name="review_id"  value="<%= review.getReview_id() %>">
                    <input type="hidden" name="product_id" value="<%= review.getProduct_id() %>">

                    <%-- ===== 1. SỐ SAO ĐÁNH GIÁ (PRE-FILL) ===== --%>
                    <div class="star-rating-section">
                        <label class="star-rating-label">
                            Mức độ hài lòng của bạn <span class="req">*</span>
                        </label>
                        <div class="star-rating-group" role="radiogroup" aria-label="Thay đổi mức sao đánh giá">
                            <input type="radio" id="star5" name="rating" value="5" <%= ratingVal == 5 ? "checked" : "" %> required>
                            <label for="star5" title="5 sao – Xuất sắc"><i class="fa-solid fa-star"></i></label>

                            <input type="radio" id="star4" name="rating" value="4" <%= ratingVal == 4 ? "checked" : "" %>>
                            <label for="star4" title="4 sao – Tốt"><i class="fa-solid fa-star"></i></label>

                            <input type="radio" id="star3" name="rating" value="3" <%= ratingVal == 3 ? "checked" : "" %>>
                            <label for="star3" title="3 sao – Bình thường"><i class="fa-solid fa-star"></i></label>

                            <input type="radio" id="star2" name="rating" value="2" <%= ratingVal == 2 ? "checked" : "" %>>
                            <label for="star2" title="2 sao – Không ưng"><i class="fa-solid fa-star"></i></label>

                            <input type="radio" id="star1" name="rating" value="1" <%= ratingVal == 1 ? "checked" : "" %>>
                            <label for="star1" title="1 sao – Rất tệ"><i class="fa-solid fa-star"></i></label>
                        </div>
                        <div class="star-rating-hint" id="starHint">
                            <%= ratingVal == 5 ? "5 sao – Xuất sắc, rất hài lòng!" :
                                ratingVal == 4 ? "4 sao – Tốt, khá hài lòng" :
                                ratingVal == 3 ? "3 sao – Bình thường" :
                                ratingVal == 2 ? "2 sao – Không ưng lắm" : "1 sao – Rất tệ, không hài lòng" %>
                        </div>
                        <div class="form-error-msg" id="ratingError">
                            <i class="fa-solid fa-circle-exclamation"></i> Vui lòng chọn mức sao đánh giá.
                        </div>
                    </div>

                    <%-- ===== 2. TIÊU ĐỀ ĐÁNH GIÁ (PRE-FILL) ===== --%>
                    <div class="form-group">
                        <label class="form-label" for="reviewTitle">
                            Tiêu đề đánh giá
                            <span class="optional-tag">(Tùy chọn)</span>
                        </label>
                        <input type="text"
                               id="reviewTitle"
                               name="title"
                               class="form-input"
                               value="<%= review.getTitle() != null ? review.getTitle() : "" %>"
                               placeholder="Tóm tắt ngắn gọn cảm nhận của bạn (ví dụ: Trái cây tươi ngon, đóng gói rất kỹ)..."
                               maxlength="150">
                        <p class="form-help">Tiêu đề giúp nhận xét của bạn nổi bật và người mua khác dễ theo dõi hơn.</p>
                    </div>

                    <%-- ===== 3. NỘI DUNG ĐÁNH GIÁ (PRE-FILL) ===== --%>
                    <div class="form-group">
                        <label class="form-label" for="reviewComment">
                            Nội dung đánh giá chi tiết <span class="req">*</span>
                        </label>
                        <textarea id="reviewComment"
                                  name="comment"
                                  class="form-textarea"
                                  placeholder="Chia sẻ trải nghiệm của bạn về hương vị, độ tươi, độ ngọt, quy cách đóng gói và thời gian giao hàng..."
                                  maxlength="1000"
                                  rows="6"
                                  required><%= review.getComment() != null ? review.getComment() : "" %></textarea>
                        <div class="char-count" id="commentCount">0 / 1000 ký tự</div>
                        <div class="form-error-msg" id="commentError">
                            <i class="fa-solid fa-circle-exclamation"></i> Nội dung đánh giá phải có ít nhất 10 ký tự.
                        </div>
                    </div>

                    <%-- ===== 4. QUẢN LÝ HÌNH ẢNH: ẢNH CŨ & ẢNH MỚI ===== --%>
                    <div class="form-group image-manager-section">
                        <label class="form-label">
                            Hình ảnh sản phẩm
                            <span class="optional-tag">(Tối đa 5 ảnh tổng cộng)</span>
                        </label>

                        <%-- Danh sách ảnh cũ (nếu có) --%>
                        <% if (oldImages.length > 0) { %>
                        <div class="existing-images-header">
                            <span class="existing-images-title">
                                <i class="fa-solid fa-clock-rotate-left"></i> Ảnh đã tải lên trước đây:
                            </span>
                            <span class="existing-images-count" id="existingCountLabel">
                                Đang giữ <%= oldImages.length %> ảnh cũ
                            </span>
                        </div>

                        <div class="existing-images-grid" id="existingGrid">
                            <%
                                for (int i = 0; i < oldImages.length; i++) {
                                    String imgPath = oldImages[i].trim();
                                    if (imgPath.isEmpty()) continue;
                            %>
                            <div class="existing-image-card" id="card-<%= i %>">
                                <img src="${pageContext.request.contextPath}/<%= imgPath %>"
                                     alt="Ảnh đánh giá cũ"
                                     loading="lazy">
                                <input type="hidden"
                                       name="keptImages"
                                       id="keptInput-<%= i %>"
                                       value="<%= imgPath %>">
                                <button type="button"
                                        class="btn-delete-existing"
                                        onclick="markDeleteOldImage(<%= i %>)"
                                        title="Xóa ảnh này">
                                    <i class="fa-solid fa-trash-can"></i>
                                </button>
                                <div class="badge-delete-mark">
                                    <span class="badge-delete-text"><i class="fa-solid fa-circle-xmark"></i> Sẽ xóa</span>
                                    <button type="button"
                                            class="btn-undo-delete"
                                            onclick="undoDeleteOldImage(<%= i %>)">
                                        <i class="fa-solid fa-rotate-left"></i> Khôi phục
                                    </button>
                                </div>
                            </div>
                            <% } %>
                        </div>
                        <% } %>

                        <%-- Khu vực Upload thêm ảnh mới --%>
                        <div class="upload-area" id="uploadArea"
                             role="button"
                             aria-label="Khu vực tải thêm ảnh mới">
                            <input type="file"
                                   id="newImages"
                                   name="newImages"
                                   accept="image/jpeg,image/png,image/webp"
                                   multiple
                                   aria-label="Chọn ảnh bổ sung">
                            <div class="upload-area-icon"><i class="fa-solid fa-cloud-arrow-up"></i></div>
                            <div class="upload-area-text">
                                Kéo thả ảnh mới vào đây hoặc <span>chọn ảnh từ máy tính</span>
                            </div>
                            <div class="upload-area-hint">Hỗ trợ JPG, PNG, WEBP &nbsp;|&nbsp; Tối đa 5 MB / ảnh</div>
                            <div class="upload-limit-badge">
                                <i class="fa-solid fa-camera"></i>
                                <span id="totalImageBadge"><%= oldImages.length %> / 5 ảnh</span>
                            </div>
                        </div>

                        <%-- Grid hiển thị ảnh mới chuẩn bị thêm --%>
                        <div class="upload-preview-grid" id="newPreviewGrid"></div>

                        <div class="form-error-msg" id="imageError">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <span id="imageErrorText">Tổng số ảnh không được vượt quá 5 ảnh.</span>
                        </div>
                    </div>

                    <%-- ===== 5. HAI NÚT HÀNH ĐỘNG RÕ RÀNG ===== --%>
                    <div class="form-actions">
                        <button type="submit" id="saveChangesBtn" class="btn-save-changes">
                            <i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi
                        </button>
                        <a href="${pageContext.request.contextPath}/ViewReviewServlet?productId=<%= review.getProduct_id() %>"
                           class="btn-cancel-edit">
                            <i class="fa-solid fa-arrow-left"></i> Hủy
                        </a>
                    </div>

                </form>

            </div>
            <%-- /page-card --%>

        </div>
    </div>
</main>

<%-- ===== JAVASCRIPT: Logic Pre-fill, Character Count, Image Manager & Validation ===== --%>
<script>
    /* -------- 1. TƯƠNG TÁC CHỌN SAO -------- */
    var starLabels = {
        '1': '1 sao – Rất tệ, không hài lòng',
        '2': '2 sao – Không ưng lắm',
        '3': '3 sao – Bình thường',
        '4': '4 sao – Tốt, khá hài lòng',
        '5': '5 sao – Xuất sắc, rất hài lòng!'
    };
    document.querySelectorAll('.star-rating-group input[type="radio"]').forEach(function(radio) {
        radio.addEventListener('change', function() {
            var hint = document.getElementById('starHint');
            if (hint) hint.textContent = starLabels[this.value] || '';
            var ratingErr = document.getElementById('ratingError');
            if (ratingErr) ratingErr.classList.remove('visible');
        });
    });

    /* -------- 2. BỘ ĐẾM KÝ TỰ -------- */
    var commentEl = document.getElementById('reviewComment');
    var commentCountEl = document.getElementById('commentCount');
    function updateCommentCount() {
        if (!commentEl || !commentCountEl) return;
        var len = commentEl.value.length;
        commentCountEl.textContent = len + ' / 1000 ký tự';
        commentCountEl.classList.toggle('warn', len > 850);
    }
    if (commentEl) {
        commentEl.addEventListener('input', updateCommentCount);
        updateCommentCount(); // Chạy lúc khởi tạo để tính ký tự pre-fill
    }

    /* -------- 3. QUẢN LÝ ẢNH CŨ (XÓA / HOÀN TÁC) -------- */
    var totalOldImages = <%= oldImages.length %>;
    var currentKeptOldCount = <%= oldImages.length %>;
    var newFilesList = []; // Danh sách file ảnh mới chọn

    function markDeleteOldImage(index) {
        var card = document.getElementById('card-' + index);
        var input = document.getElementById('keptInput-' + index);
        if (card && input) {
            card.classList.add('marked-delete');
            input.disabled = true; // Không gửi ảnh này lên server
            currentKeptOldCount--;
            updateTotalImageCount();
        }
    }

    function undoDeleteOldImage(index) {
        var card = document.getElementById('card-' + index);
        var input = document.getElementById('keptInput-' + index);
        if (card && input) {
            card.classList.remove('marked-delete');
            input.disabled = false; // Phục hồi giữ ảnh
            currentKeptOldCount++;
            updateTotalImageCount();
        }
    }

    function updateTotalImageCount() {
        var total = currentKeptOldCount + newFilesList.length;
        var badge = document.getElementById('totalImageBadge');
        if (badge) badge.textContent = total + ' / 5 ảnh';

        var label = document.getElementById('existingCountLabel');
        if (label) label.textContent = 'Đang giữ ' + currentKeptOldCount + ' ảnh cũ';

        var errEl = document.getElementById('imageError');
        var errText = document.getElementById('imageErrorText');
        if (total > 5) {
            if (errEl && errText) {
                errText.textContent = 'Tổng số ảnh (ảnh cũ + ảnh mới) đang là ' + total + '. Vượt quá giới hạn 5 ảnh!';
                errEl.classList.add('visible');
            }
        } else {
            if (errEl) errEl.classList.remove('visible');
        }
    }

    /* -------- 4. QUẢN LÝ UPLOAD ẢNH MỚI & PREVIEW -------- */
    var fileInput = document.getElementById('newImages');
    var uploadArea = document.getElementById('uploadArea');
    var newPreviewGrid = document.getElementById('newPreviewGrid');

    if (uploadArea && fileInput) {
        ['dragenter', 'dragover'].forEach(function(evt) {
            uploadArea.addEventListener(evt, function(e) {
                e.preventDefault();
                uploadArea.classList.add('drag-over');
            });
        });
        ['dragleave', 'drop'].forEach(function(evt) {
            uploadArea.addEventListener(evt, function(e) {
                e.preventDefault();
                uploadArea.classList.remove('drag-over');
            });
        });
        uploadArea.addEventListener('drop', function(e) {
            if (e.dataTransfer && e.dataTransfer.files) {
                handleIncomingFiles(e.dataTransfer.files);
            }
        });
        fileInput.addEventListener('change', function() {
            handleIncomingFiles(this.files);
        });
    }

    function handleIncomingFiles(files) {
        var errEl = document.getElementById('imageError');
        var errText = document.getElementById('imageErrorText');

        for (var i = 0; i < files.length; i++) {
            var file = files[i];

            // Kiểm tra dung lượng < 5MB
            if (file.size > 5 * 1024 * 1024) {
                if (errEl && errText) {
                    errText.textContent = 'Ảnh "' + file.name + '" vượt quá dung lượng 5 MB.';
                    errEl.classList.add('visible');
                }
                continue;
            }

            // Kiểm tra định dạng
            if (!file.type.match(/image\/(jpeg|png|webp)/)) {
                if (errEl && errText) {
                    errText.textContent = 'Chỉ chấp nhận định dạng JPG, PNG hoặc WEBP.';
                    errEl.classList.add('visible');
                }
                continue;
            }

            // Kiểm tra giới hạn 5 ảnh
            if (currentKeptOldCount + newFilesList.length >= 5) {
                if (errEl && errText) {
                    errText.textContent = 'Tối đa chỉ được tải lên tổng cộng 5 ảnh.';
                    errEl.classList.add('visible');
                }
                break;
            }

            newFilesList.push(file);
        }

        renderNewPreviews();
        syncDataTransfer();
        updateTotalImageCount();
    }

    function renderNewPreviews() {
        if (!newPreviewGrid) return;
        newPreviewGrid.innerHTML = '';

        newFilesList.forEach(function(file, idx) {
            var item = document.createElement('div');
            item.className = 'upload-preview-item';

            var img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.alt = 'Ảnh mới upload';

            var removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'upload-preview-remove';
            removeBtn.title = 'Bỏ ảnh này';
            removeBtn.innerHTML = '<i class="fa-solid fa-xmark"></i>';
            removeBtn.onclick = function() {
                newFilesList.splice(idx, 1);
                renderNewPreviews();
                syncDataTransfer();
                updateTotalImageCount();
            };

            item.appendChild(img);
            item.appendChild(removeBtn);
            newPreviewGrid.appendChild(item);
        });
    }

    function syncDataTransfer() {
        try {
            var dt = new DataTransfer();
            newFilesList.forEach(function(f) { dt.items.add(f); });
            if (fileInput) fileInput.files = dt.files;
        } catch (e) {
            // Trình duyệt cũ không hỗ trợ DataTransfer constructor
        }
    }

    /* -------- 5. VALIDATE FORM TRƯỚC KHI GỬI -------- */
    var form = document.getElementById('editReviewForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            var valid = true;

            // Kiểm tra số sao
            var ratingChecked = document.querySelector('.star-rating-group input[type="radio"]:checked');
            var ratingErr = document.getElementById('ratingError');
            if (!ratingChecked) {
                if (ratingErr) ratingErr.classList.add('visible');
                valid = false;
            } else {
                if (ratingErr) ratingErr.classList.remove('visible');
            }

            // Kiểm tra nội dung
            var commentVal = commentEl ? commentEl.value.trim() : '';
            var commentErr = document.getElementById('commentError');
            if (commentVal.length < 10) {
                if (commentErr) commentErr.classList.add('visible');
                if (commentEl) commentEl.classList.add('input-error');
                valid = false;
            } else {
                if (commentErr) commentErr.classList.remove('visible');
                if (commentEl) commentEl.classList.remove('input-error');
            }

            // Kiểm tra tổng số ảnh <= 5
            var total = currentKeptOldCount + newFilesList.length;
            if (total > 5) {
                var errEl = document.getElementById('imageError');
                var errText = document.getElementById('imageErrorText');
                if (errEl && errText) {
                    errText.textContent = 'Tổng số ảnh không được vượt quá 5 ảnh.';
                    errEl.classList.add('visible');
                }
                valid = false;
            }

            if (!valid) {
                e.preventDefault();
                return false;
            }

            // Hiệu ứng đang lưu thay đổi
            var saveBtn = document.getElementById('saveChangesBtn');
            if (saveBtn) {
                saveBtn.disabled = true;
                saveBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';
            }
        });
    }
</script>

</body>
</html>
