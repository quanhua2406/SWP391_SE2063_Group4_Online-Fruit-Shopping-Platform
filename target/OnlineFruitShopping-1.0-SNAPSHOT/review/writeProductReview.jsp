<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.onlinefruitshopping.dao.review.ReviewDAO" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Viết Đánh Giá Sản Phẩm – FreshFruit</title>
    <meta name="description" content="Chia sẻ cảm nhận thực tế về sản phẩm bạn đã mua tại FreshFruit.">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">
</head>
<body>

<%-- ===== Đọc tham số & thông báo lỗi từ ReviewServlet / request param ===== --%>
<%
    String errorMsg   = (String)  request.getAttribute("errorMsg");
    Integer productId = (Integer) request.getAttribute("productId");
    Integer orderId   = (Integer) request.getAttribute("orderId");

    // Nếu không có từ attribute (truy cập trực tiếp), đọc từ query string
    if (productId == null) {
        try { productId = Integer.parseInt(request.getParameter("productId")); }
        catch (Exception e) { productId = 0; }
    }
    if (orderId == null) {
        try { orderId = Integer.parseInt(request.getParameter("orderId")); }
        catch (Exception e) { orderId = 0; }
    }

    // Nếu chưa đăng nhập thì redirect tới login kèm URL quay lại
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        com.mycompany.onlinefruitshopping.model.User u =
                (com.mycompany.onlinefruitshopping.model.User) session.getAttribute("user");
        if (u != null) {
            userId = u.getUserID();
        }
    }
    if (userId == null) {
        String currentUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + java.net.URLEncoder.encode(currentUrl, "UTF-8"));
        return;
    }

    // Tự động tải thông tin sản phẩm thực tế từ Database
    ReviewDAO reviewDAO = new ReviewDAO();
    String productName = reviewDAO.getProductNameById(productId);
    String productImage = reviewDAO.getProductImageById(productId);
    if (productName == null || productName.trim().isEmpty()) {
        productName = "Sản phẩm #" + productId;
    }
    if (productImage == null || productImage.trim().isEmpty()) {
        productImage = "https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=200&q=80";
    }

    // Tự động tìm đơn hàng đã giao thành công của user chứa sản phẩm này (nếu chưa có orderId)
    if ((orderId == null || orderId <= 0) && userId != null) {
        int eligibleOrder = reviewDAO.findDeliveredOrderId(userId, productId);
        if (eligibleOrder > 0) {
            orderId = eligibleOrder;
        }
    }
%>

<%-- ===== BREADCRUMB ===== --%>
<div class="breadcrumb-bar">
    <div class="container">
        <ol class="breadcrumb-list">
            <li><a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-house"></i> Trang chủ</a></li>
            <li><span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span></li>
            <li>
                <a href="${pageContext.request.contextPath}/ViewReviewServlet?productId=<%= productId %>">
                    Đánh giá: <%= productName %>
                </a>
            </li>
            <li><span class="bc-sep"><i class="fa-solid fa-chevron-right"></i></span></li>
            <li class="active">Viết đánh giá</li>
        </ol>
    </div>
</div>

<%-- ===== MAIN CONTENT ===== --%>
<main>
    <div class="container">
        <div class="page-layout-full">

            <%-- ===== ĐIỀU KIỆN BẮT BUỘC ===== --%>
            <div class="notice-box notice-info">
                <span class="notice-icon"><i class="fa-solid fa-circle-info"></i></span>
                <div>
                    <strong>Điều kiện đánh giá:</strong> Chỉ những khách hàng đã đặt hàng và
                    <strong>nhận hàng thành công (status: delivered)</strong> mới có thể gửi đánh giá cho sản phẩm.
                    Đánh giá sẽ được kiểm duyệt trong vòng <strong>24 giờ</strong> trước khi xuất hiện công khai.
                </div>
            </div>

            <%-- ===== FORM CARD ===== --%>
            <div class="page-card">

                <h1 class="page-card-title">
                    <span class="card-title-icon"><i class="fa-solid fa-pen-to-square"></i></span>
                    Viết Đánh Giá Sản Phẩm
                </h1>

                <%-- ===== SẢN PHẨM ĐANG ĐƯỢC ĐÁNH GIÁ ===== --%>
                <div class="product-summary-card">
                    <img class="product-summary-img"
                         src="<%= productImage.startsWith("http") ? productImage : (request.getContextPath() + "/" + productImage) %>"
                         alt="<%= productName %>"
                         onerror="this.src='https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=200&q=80'">
                    <div class="product-summary-info">
                        <div class="product-summary-name"><%= productName %></div>
                        <div class="product-summary-variant">Sản phẩm chính hãng từ nông trại FreshFruit</div>
                        <div class="product-summary-order">
                            <i class="fa-solid fa-receipt"></i>
                            <% if (orderId > 0) { %>
                                Đơn hàng <strong>#<%= orderId %></strong> &nbsp;<span style="color: #10b981; font-weight: 600;"><i class="fa-solid fa-circle-check"></i> Đã nhận hàng</span>
                            <% } else { %>
                                <span style="color: #eab308;"><i class="fa-solid fa-triangle-exclamation"></i> Vui lòng nhập mã đơn hàng đã nhận thành công</span>
                            <% } %>
                        </div>
                    </div>
                </div>

                <%-- ===== HIỂN THỊ LỖI TỪ SERVER (nếu có) ===== --%>
                <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
                <div class="notice-box notice-warning">
                    <span class="notice-icon"><i class="fa-solid fa-triangle-exclamation"></i></span>
                    <span><%= errorMsg %></span>
                </div>
                <% } %>

                <%-- ===== FORM GỬI ĐÁNH GIÁ ===== --%>
                <form id="reviewForm"
                      action="${pageContext.request.contextPath}/ReviewServlet"
                      method="post"
                      enctype="multipart/form-data"
                      novalidate>

                    <%-- Hidden fields --%>
                    <input type="hidden" name="product_id" value="<%= productId %>">
                    <% if (orderId > 0) { %>
                    <input type="hidden" name="order_id"   value="<%= orderId %>">
                    <% } else { %>
                    <div class="form-group" style="margin-bottom: 24px;">
                        <label class="form-label" for="orderIdInput">
                            Mã đơn hàng (Order ID) đã nhận <span class="req">*</span>
                        </label>
                        <input type="number" id="orderIdInput" name="order_id" class="form-input"
                               placeholder="Nhập mã đơn hàng (ví dụ: 1, 2, ...)" required min="1">
                        <small style="color: #64748b; font-size: 0.85rem; display: block; margin-top: 4px;">
                            Bạn chỉ có thể đánh giá cho đơn hàng đã được giao thành công (status: delivered).
                        </small>
                    </div>
                    <% } %>

                    <%-- ===== CHỌN SỐ SAO ===== --%>
                    <div class="star-rating-section">
                        <label class="star-rating-label">
                            Chất lượng sản phẩm <span class="req">*</span>
                        </label>
                        <div class="star-rating-group" role="radiogroup" aria-label="Chọn mức đánh giá sao">
                            <input type="radio" id="star5" name="rating" value="5" required>
                            <label for="star5" title="5 sao – Xuất sắc"><i class="fa-solid fa-star"></i></label>
                            <input type="radio" id="star4" name="rating" value="4">
                            <label for="star4" title="4 sao – Tốt"><i class="fa-solid fa-star"></i></label>
                            <input type="radio" id="star3" name="rating" value="3">
                            <label for="star3" title="3 sao – Bình thường"><i class="fa-solid fa-star"></i></label>
                            <input type="radio" id="star2" name="rating" value="2">
                            <label for="star2" title="2 sao – Không ưng"><i class="fa-solid fa-star"></i></label>
                            <input type="radio" id="star1" name="rating" value="1">
                            <label for="star1" title="1 sao – Rất tệ"><i class="fa-solid fa-star"></i></label>
                        </div>
                        <div class="star-rating-hint" id="starHint">Nhấn vào ngôi sao để chọn mức đánh giá.</div>
                        <div class="form-error-msg" id="ratingError">
                            <i class="fa-solid fa-circle-exclamation"></i> Vui lòng chọn số sao đánh giá.
                        </div>
                    </div>


                    <%-- ===== NỘI DUNG ĐÁNH GIÁ ===== --%>
                    <div class="form-group">
                        <label class="form-label" for="reviewComment">
                            Nội dung đánh giá <span class="req">*</span>
                        </label>
                        <textarea id="reviewComment"
                                  name="comment"
                                  class="form-textarea"
                                  placeholder="Chia sẻ cảm nhận: chất lượng, mùi vị, đóng gói, giao hàng..."
                                  maxlength="1000"
                                  rows="6"
                                  oninput="updateCharCount(this, 'commentCount', 1000)"
                                  required></textarea>
                        <div class="char-count" id="commentCount">0 / 1000 ký tự</div>
                        <div class="form-error-msg" id="commentError">
                            <i class="fa-solid fa-circle-exclamation"></i> Nội dung phải có ít nhất 20 ký tự.
                        </div>
                    </div>

                    <%-- ===== UPLOAD ẢNH (TỐI ĐA 5) ===== --%>
                    <div class="form-group">
                        <label class="form-label">
                            Hình ảnh thực tế
                            <span class="optional-tag">(Không bắt buộc – Tối đa 5 ảnh)</span>
                        </label>
                        <div class="upload-area" id="uploadArea"
                             role="button" aria-label="Khu vực tải ảnh – nhấn hoặc kéo thả ảnh vào đây">
                            <input type="file"
                                   id="reviewImages"
                                   name="reviewImages"
                                   accept="image/jpeg,image/png,image/webp"
                                   multiple
                                   aria-label="Chọn ảnh từ thiết bị">
                            <div class="upload-area-icon"><i class="fa-regular fa-images"></i></div>
                            <div class="upload-area-text">
                                Kéo thả ảnh vào đây hoặc <span>nhấn để chọn ảnh</span>
                            </div>
                            <div class="upload-area-hint">JPG, PNG, WEBP – Tối đa 5 MB / ảnh</div>
                            <div class="upload-limit-badge">
                                <i class="fa-solid fa-camera"></i>
                                <span id="uploadCountBadge">0 / 5 ảnh đã chọn</span>
                            </div>
                        </div>
                        <div class="upload-preview-grid" id="previewGrid"></div>
                        <div class="form-error-msg" id="imageError">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <span id="imageErrorText">Chỉ được tải lên tối đa 5 ảnh.</span>
                        </div>
                        <p class="form-help">
                            <i class="fa-solid fa-lightbulb" style="color:var(--accent-gold);"></i>
                            Ảnh thực tế giúp người mua tin tưởng và tăng mức độ hữu ích của đánh giá!
                        </p>
                    </div>

                    <%-- ===== NÚT HÀNH ĐỘNG ===== --%>
                    <div class="form-actions">
                        <button type="submit" id="submitBtn" class="btn-submit-review">
                            <i class="fa-solid fa-paper-plane"></i> Gửi đánh giá
                        </button>
                        <a href="${pageContext.request.contextPath}/ViewReviewServlet?productId=<%= productId %>"
                           class="btn-cancel-review">
                            <i class="fa-solid fa-xmark"></i> Hủy
                        </a>
                    </div>

                </form>

            </div>
            <%-- /page-card --%>

            <div class="notice-box notice-success" style="margin-top:20px; margin-bottom:0;">
                <span class="notice-icon"><i class="fa-solid fa-leaf"></i></span>
                <span>
                    Đánh giá của bạn <strong>hoàn toàn bảo mật</strong>. FreshFruit cam kết không sử dụng
                    thông tin cá nhân cho mục đích thương mại.
                </span>
            </div>

        </div>
    </div>
</main>

<%-- ===== JAVASCRIPT: Star hints, Char count, Image preview, Client Validation ===== --%>
<script>
    /* -------- STAR HINTS -------- */
    var starLabels = {
        '1': '1 sao – Rất tệ, không hài lòng',
        '2': '2 sao – Không ưng lắm',
        '3': '3 sao – Bình thường',
        '4': '4 sao – Tốt, khá hài lòng',
        '5': '5 sao – Xuất sắc, rất hài lòng!'
    };
    document.querySelectorAll('.star-rating-group input[type="radio"]').forEach(function(r) {
        r.addEventListener('change', function() {
            document.getElementById('starHint').textContent = starLabels[this.value] || '';
            document.getElementById('ratingError').classList.remove('visible');
        });
    });

    /* -------- CHARACTER COUNT -------- */
    function updateCharCount(el, countId, max) {
        var len  = el.value.length;
        var span = document.getElementById(countId);
        span.textContent = len + ' / ' + max + ' ký tự';
        span.classList.toggle('warn', len > max * 0.85);
    }

    /* -------- IMAGE UPLOAD PREVIEW -------- */
    var selectedFiles = [];
    var MAX_FILES     = 5;
    var MAX_MB        = 5;

    document.getElementById('reviewImages').addEventListener('change', function(e) {
        handleFiles(Array.from(e.target.files));
    });

    /* Drag & Drop */
    var uploadArea = document.getElementById('uploadArea');
    uploadArea.addEventListener('dragover', function(e) {
        e.preventDefault();
        uploadArea.classList.add('drag-over');
    });
    uploadArea.addEventListener('dragleave', function() {
        uploadArea.classList.remove('drag-over');
    });
    uploadArea.addEventListener('drop', function(e) {
        e.preventDefault();
        uploadArea.classList.remove('drag-over');
        handleFiles(Array.from(e.dataTransfer.files));
    });

    function handleFiles(files) {
        var imgError = document.getElementById('imageError');
        var imgErrTx = document.getElementById('imageErrorText');
        imgError.classList.remove('visible');

        files.forEach(function(file) {
            if (selectedFiles.length >= MAX_FILES) {
                imgErrTx.textContent = 'Chỉ được tải lên tối đa ' + MAX_FILES + ' ảnh.';
                imgError.classList.add('visible');
                return;
            }
            if (!file.type.match(/image\/(jpeg|png|webp)/)) {
                imgErrTx.textContent = '"' + file.name + '" không đúng định dạng (JPG/PNG/WEBP).';
                imgError.classList.add('visible');
                return;
            }
            if (file.size > MAX_MB * 1024 * 1024) {
                imgErrTx.textContent = '"' + file.name + '" vượt quá ' + MAX_MB + ' MB.';
                imgError.classList.add('visible');
                return;
            }
            selectedFiles.push(file);
        });

        renderPreviews();
        document.getElementById('uploadCountBadge').textContent =
            selectedFiles.length + ' / ' + MAX_FILES + ' ảnh đã chọn';
    }

    function renderPreviews() {
        var grid = document.getElementById('previewGrid');
        grid.innerHTML = '';
        selectedFiles.forEach(function(file, idx) {
            var reader = new FileReader();
            reader.onload = function(e) {
                var item = document.createElement('div');
                item.className = 'upload-preview-item';
                item.innerHTML =
                    '<img src="' + e.target.result + '" alt="Ảnh xem trước ' + (idx + 1) + '">' +
                    '<button type="button" class="upload-preview-remove" ' +
                    '        onclick="removeImage(' + idx + ')" title="Xoá ảnh này">' +
                    '    <i class="fa-solid fa-xmark"></i>' +
                    '</button>';
                grid.appendChild(item);
            };
            reader.readAsDataURL(file);
        });
    }

    function removeImage(idx) {
        selectedFiles.splice(idx, 1);
        renderPreviews();
        document.getElementById('uploadCountBadge').textContent =
            selectedFiles.length + ' / ' + MAX_FILES + ' ảnh đã chọn';
        if (selectedFiles.length < MAX_FILES) {
            document.getElementById('imageError').classList.remove('visible');
        }
    }

    /* -------- CLIENT-SIDE VALIDATION -------- */
    document.getElementById('reviewForm').addEventListener('submit', function(e) {
        var valid = true;

        // Validate sao
        if (!document.querySelector('.star-rating-group input[type="radio"]:checked')) {
            document.getElementById('ratingError').classList.add('visible');
            valid = false;
        }

        // Validate nội dung
        var comment = document.getElementById('reviewComment');
        if (comment.value.trim().length < 20) {
            comment.classList.add('input-error');
            document.getElementById('commentError').classList.add('visible');
            valid = false;
        } else {
            comment.classList.remove('input-error');
            document.getElementById('commentError').classList.remove('visible');
        }

        if (!valid) {
            e.preventDefault();
            var firstErr = document.querySelector('.form-error-msg.visible, .input-error');
            if (firstErr) firstErr.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return;
        }

        // Vô hiệu hoá nút tránh double submit
        var btn = document.getElementById('submitBtn');
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang gửi...';
    });

    /* -------- XOÁ ERROR KHI USER GÕ LẠI -------- */
    document.getElementById('reviewComment').addEventListener('input', function() {
        this.classList.remove('input-error');
        document.getElementById('commentError').classList.remove('visible');
    });
</script>

</body>
</html>
