<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<fmt:setLocale value="vi_VN"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh Giá Của Tôi | FreshFruit</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css">

    <style>
        .my-reviews-container {
            max-width: 1100px;
            margin: 30px auto 60px;
            padding: 0 20px;
        }

        .breadcrumb {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #64748b;
            margin-bottom: 24px;
        }
        .breadcrumb a {
            color: #0f172a;
            text-decoration: none;
            transition: color 0.2s;
        }
        .breadcrumb a:hover {
            color: #16a34a;
        }

        .page-header-box {
            background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 100%);
            border: 1px solid #d1fae5;
            border-radius: 16px;
            padding: 24px 30px;
            margin-bottom: 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 20px;
        }

        .page-header-title h1 {
            font-size: 24px;
            font-weight: 800;
            color: #065f46;
            margin-bottom: 6px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .page-header-title p {
            color: #047857;
            font-size: 14px;
        }

        .stats-cards-row {
            display: flex;
            gap: 16px;
        }
        .stat-pill {
            background: #ffffff;
            border: 1px solid #a7f3d0;
            padding: 12px 20px;
            border-radius: 12px;
            text-align: center;
            box-shadow: 0 2px 6px rgba(0,0,0,0.03);
        }
        .stat-pill-num {
            font-size: 22px;
            font-weight: 800;
            color: #065f46;
        }
        .stat-pill-label {
            font-size: 12px;
            font-weight: 600;
            color: #047857;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        /* Navigation Tabs */
        .my-review-tabs {
            display: flex;
            gap: 12px;
            border-bottom: 2px solid #e2e8f0;
            margin-bottom: 24px;
        }
        .my-tab-btn {
            padding: 12px 24px;
            font-size: 15px;
            font-weight: 700;
            color: #64748b;
            background: none;
            border: none;
            border-bottom: 3px solid transparent;
            margin-bottom: -2px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.2s;
        }
        .my-tab-btn:hover {
            color: #16a34a;
        }
        .my-tab-btn.active {
            color: #15803d;
            border-bottom-color: #16a34a;
        }
        .tab-badge {
            background: #e2e8f0;
            color: #334155;
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 9999px;
            font-weight: 700;
        }
        .my-tab-btn.active .tab-badge {
            background: #16a34a;
            color: #ffffff;
        }

        /* Review Card */
        .review-card-item {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            padding: 24px;
            margin-bottom: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04);
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .review-card-item:hover {
            box-shadow: 0 6px 16px rgba(0,0,0,0.08);
        }

        .review-card-top {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 16px;
            padding-bottom: 16px;
            border-bottom: 1px solid #f1f5f9;
            margin-bottom: 16px;
        }

        .product-brief {
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .product-brief-img {
            width: 72px;
            height: 72px;
            border-radius: 12px;
            object-fit: cover;
            border: 1px solid #e2e8f0;
        }
        .product-brief-name {
            font-size: 16px;
            font-weight: 700;
            color: #0f172a;
            text-decoration: none;
            display: block;
            margin-bottom: 4px;
        }
        .product-brief-name:hover {
            color: #16a34a;
        }
        .product-brief-order {
            font-size: 13px;
            color: #64748b;
        }

        .review-status-badge {
            font-size: 12px;
            font-weight: 700;
            padding: 4px 10px;
            border-radius: 6px;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }
        .status-published {
            background: #ecfdf5;
            color: #15803d;
        }
        .status-reported {
            background: #fffbeb;
            color: #b45309;
        }
        .status-rejected {
            background: #fef2f2;
            color: #b91c1c;
        }

        .review-card-stars {
            color: #f59e0b;
            font-size: 14px;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 4px;
        }
        .review-card-date {
            color: #94a3b8;
            font-size: 12px;
            margin-left: 8px;
        }

        .review-card-title {
            font-weight: 700;
            font-size: 15px;
            color: #1e293b;
            margin-bottom: 6px;
        }
        .review-card-comment {
            font-size: 14.5px;
            color: #334155;
            line-height: 1.6;
            margin-bottom: 14px;
            white-space: pre-wrap;
        }

        .review-card-images {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 16px;
        }
        .review-card-img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            cursor: pointer;
            transition: transform 0.2s;
        }
        .review-card-img:hover {
            transform: scale(1.05);
        }

        .review-card-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            pt: 12px;
            border-top: 1px solid #f8fafc;
        }
        .review-action-btns {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .btn-action-edit {
            padding: 7px 14px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 700;
            color: #2563eb;
            background: #eff6ff;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: background 0.2s;
        }
        .btn-action-edit:hover {
            background: #dbeafe;
        }

        .btn-action-delete {
            padding: 7px 14px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 700;
            color: #dc2626;
            background: #fef2f2;
            border: none;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: background 0.2s;
        }
        .btn-action-delete:hover {
            background: #fee2e2;
        }

        .btn-view-product {
            font-size: 13px;
            font-weight: 600;
            color: #16a34a;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .btn-view-product:hover {
            text-decoration: underline;
        }

        /* Pending Review Item */
        .pending-item-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            padding: 20px 24px;
            margin-bottom: 16px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 20px;
            flex-wrap: wrap;
        }
        .btn-write-review-now {
            background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
            color: #ffffff;
            font-weight: 700;
            font-size: 14px;
            padding: 10px 20px;
            border-radius: 10px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            box-shadow: 0 4px 10px rgba(22, 163, 74, 0.25);
            transition: all 0.2s;
        }
        .btn-write-review-now:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 14px rgba(22, 163, 74, 0.35);
        }

        .empty-state-box {
            text-align: center;
            padding: 60px 20px;
            background: #ffffff;
            border-radius: 16px;
            border: 1px dashed #cbd5e1;
        }
        .empty-state-icon {
            font-size: 54px;
            color: #cbd5e1;
            margin-bottom: 16px;
        }
        .empty-state-title {
            font-size: 18px;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 8px;
        }
        .empty-state-desc {
            font-size: 14px;
            color: #64748b;
            max-width: 420px;
            margin: 0 auto 20px;
        }
    </style>
</head>
<body>

<!-- Header Navbar -->
<header class="navbar">
    <a href="${pageContext.request.contextPath}/home" class="logo">FreshFruit</a>

    <div class="nav-actions">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <a href="${pageContext.request.contextPath}/products">Khám phá trái cây</a>
        <a href="${pageContext.request.contextPath}/subscriptions">Gói định kỳ</a>
        <a href="${pageContext.request.contextPath}/customer/dashboard" style="color: #16a34a; font-weight: 700;">Dashboard</a>
        <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>
        <a href="${pageContext.request.contextPath}/logout" class="logout">Đăng xuất</a>
    </div>
</header>

<main class="my-reviews-container">

    <!-- Breadcrumb -->
    <div class="breadcrumb">
        <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/customer/dashboard">Customer Dashboard</a>
        <span>/</span>
        <span style="color: #0f172a; font-weight: 600;">Đánh giá của tôi</span>
    </div>

    <%-- Toast Notifications --%>
    <c:if test="${reviewSent}">
        <div class="toast-msg toast-success show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
            <i class="fa-solid fa-circle-check"></i>
            <span>Đánh giá của bạn đã được gửi thành công và đang chờ kiểm duyệt!</span>
        </div>
    </c:if>
    <c:if test="${reviewUpdated}">
        <div class="toast-msg toast-success show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
            <i class="fa-solid fa-circle-check"></i>
            <span>Đánh giá của bạn đã được cập nhật thành công!</span>
        </div>
    </c:if>
    <c:if test="${reviewDeleted}">
        <div class="toast-msg toast-success show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
            <i class="fa-solid fa-circle-check"></i>
            <span>Đã xóa đánh giá thành công!</span>
        </div>
    </c:if>
    <c:if test="${deleteError}">
        <div class="toast-msg toast-error show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
            <i class="fa-solid fa-circle-xmark"></i>
            <span>Có lỗi xảy ra khi xóa đánh giá. Vui lòng thử lại sau.</span>
        </div>
    </c:if>
    <c:if test="${expired}">
        <div class="toast-msg toast-error show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
            <i class="fa-solid fa-triangle-exclamation"></i>
            <span>Đánh giá đã quá thời hạn 7 ngày nên không thể chỉnh sửa.</span>
        </div>
    </c:if>
    <c:if test="${authError}">
        <div class="toast-msg toast-error show" style="margin-bottom: 20px; position: static; display: flex;" role="status">
            <i class="fa-solid fa-circle-xmark"></i>
            <span>Bạn không có quyền thực hiện thao tác này.</span>
        </div>
    </c:if>

    <!-- Header Box -->
    <div class="page-header-box">
        <div class="page-header-title">
            <h1><i class="fa-solid fa-star" style="color: #f59e0b;"></i> Đánh Giá Của Tôi</h1>
            <p>Quản lý các nhận xét đã gửi và chia sẻ cảm nhận về những trái cây tươi bạn đã trải nghiệm.</p>
        </div>
        <div class="stats-cards-row">
            <div class="stat-pill">
                <div class="stat-pill-num">${totalReviewed}</div>
                <div class="stat-pill-label">Đã đánh giá</div>
            </div>
            <div class="stat-pill">
                <div class="stat-pill-num" style="color: #d97706;">${totalPending}</div>
                <div class="stat-pill-label" style="color: #b45309;">Chờ đánh giá</div>
            </div>
        </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="my-review-tabs">
        <a href="${pageContext.request.contextPath}/customer/reviews?tab=reviewed"
           class="my-tab-btn ${currentTab == 'reviewed' ? 'active' : ''}">
            <i class="fa-solid fa-comments"></i>
            <span>Đã Đánh Giá</span>
            <span class="tab-badge">${totalReviewed}</span>
        </a>
        <a href="${pageContext.request.contextPath}/customer/reviews?tab=pending"
           class="my-tab-btn ${currentTab == 'pending' ? 'active' : ''}">
            <i class="fa-solid fa-clock"></i>
            <span>Chờ Đánh Giá</span>
            <span class="tab-badge">${totalPending}</span>
        </a>
    </div>

    <!-- Tab 1: Reviewed List -->
    <c:if test="${currentTab == 'reviewed'}">
        <c:choose>
            <c:when test="${empty reviewedList}">
                <div class="empty-state-box">
                    <div class="empty-state-icon">
                        <i class="fa-regular fa-comment-dots"></i>
                    </div>
                    <div class="empty-state-title">Bạn chưa có đánh giá nào</div>
                    <p class="empty-state-desc">Hãy chia sẻ trải nghiệm thực tế về các sản phẩm trái cây bạn đã nhận để giúp cộng đồng mua sắm tốt hơn!</p>
                    <a href="${pageContext.request.contextPath}/customer/reviews?tab=pending" class="btn-write-review-now">
                        Xem sản phẩm chờ đánh giá (${totalPending})
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach var="r" items="${reviewedList}">
                    <div class="review-card-item">
                        <div class="review-card-top">
                            <div class="product-brief">
                                <img src="${not empty r.productImage ? r.productImage : 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=100'}"
                                     alt="${r.productName}" class="product-brief-img"
                                     onerror="this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=100';">
                                <div>
                                    <a href="${pageContext.request.contextPath}/product?id=${r.product_id}#tab-reviews"
                                       class="product-brief-name">
                                        ${r.productName}
                                    </a>
                                    <div class="product-brief-order">
                                        <i class="fa-solid fa-receipt"></i> Đơn hàng #${r.order_id} &nbsp;|&nbsp; Đã nhận hàng
                                    </div>
                                </div>
                            </div>

                            <!-- Review Status Badge -->
                            <div>
                                <c:choose>
                                    <c:when test="${r.status == 'published' or empty r.status}">
                                        <span class="review-status-badge status-published">
                                            <i class="fa-solid fa-circle-check"></i> Đã xuất bản
                                        </span>
                                    </c:when>
                                    <c:when test="${r.status == 'reported'}">
                                        <span class="review-status-badge status-reported">
                                            <i class="fa-solid fa-triangle-exclamation"></i> Đang kiểm duyệt
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="review-status-badge status-rejected">
                                            <i class="fa-solid fa-circle-xmark"></i> Không được duyệt
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Rating Stars & Date -->
                        <div class="review-card-stars">
                            <c:forEach var="s" begin="1" end="5">
                                <c:choose>
                                    <c:when test="${s <= r.rating}">
                                        <i class="fa-solid fa-star"></i>
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fa-regular fa-star" style="color: #cbd5e1;"></i>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <span class="review-card-date">
                                <i class="fa-regular fa-calendar-check"></i>
                                <fmt:formatDate value="${not empty r.created_at ? r.created_at : r.create_at}" pattern="dd/MM/yyyy HH:mm"/>
                            </span>
                        </div>

                        <!-- Title & Comment -->
                        <c:if test="${not empty r.title}">
                            <div class="review-card-title">${fn:escapeXml(r.title)}</div>
                        </c:if>
                        <div class="review-card-comment">${fn:escapeXml(r.comment)}</div>

                        <!-- Images Gallery -->
                        <c:if test="${not empty r.imageUrls}">
                            <div class="review-card-images">
                                <c:forEach var="img" items="${fn:split(r.imageUrls, ',')}">
                                    <c:set var="trimmed" value="${fn:trim(img)}"/>
                                    <c:if test="${not empty trimmed}">
                                        <img src="${pageContext.request.contextPath}/${trimmed}"
                                             alt="Ảnh đánh giá" class="review-card-img"
                                             onclick="window.open(this.src, '_blank')">
                                    </c:if>
                                </c:forEach>
                            </div>
                        </c:if>

                        <!-- Actions Footer -->
                        <div class="review-card-actions">
                            <a href="${pageContext.request.contextPath}/product?id=${r.product_id}#tab-reviews"
                               class="btn-view-product">
                                Xem trên trang sản phẩm &rarr;
                            </a>

                            <div class="review-action-btns">
                                <c:choose>
                                    <c:when test="${r.editable}">
                                        <a href="${pageContext.request.contextPath}/edit-review?reviewId=${r.review_id}&redirect=/customer/reviews%3Ftab=reviewed"
                                           class="btn-action-edit"
                                           title="Chỉnh sửa đánh giá trong vòng 7 ngày (còn ${r.remainingDays} ngày)">
                                            <i class="fa-solid fa-pen-to-square"></i> Sửa (còn ${r.remainingDays} ngày)
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="font-size: 12px; color: #94a3b8;" title="Đã quá 7 ngày kể từ lúc gửi">
                                            <i class="fa-solid fa-lock"></i> Hết hạn sửa
                                        </span>
                                    </c:otherwise>
                                </c:choose>

                                <button type="button" class="btn-action-delete"
                                        data-review-id="${r.review_id}"
                                        data-product-name="${fn:escapeXml(r.productName)}"
                                        data-rating="${r.rating}"
                                        data-comment="${fn:escapeXml(r.comment)}"
                                        onclick="openDeleteModal(this)">
                                    <i class="fa-solid fa-trash-can"></i> Xóa
                                </button>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <div class="pagination-nav" style="display: flex; justify-content: center; gap: 6px; margin-top: 30px;">
                        <c:if test="${currentPage > 1}">
                            <a href="${pageContext.request.contextPath}/customer/reviews?tab=reviewed&page=${currentPage - 1}"
                               class="review-filter-btn">
                                <i class="fa-solid fa-chevron-left"></i>
                            </a>
                        </c:if>
                        <c:forEach var="p" begin="1" end="${totalPages}">
                            <a href="${pageContext.request.contextPath}/customer/reviews?tab=reviewed&page=${p}"
                               class="review-filter-btn ${p == currentPage ? 'active' : ''}">
                                ${p}
                            </a>
                        </c:forEach>
                        <c:if test="${currentPage < totalPages}">
                            <a href="${pageContext.request.contextPath}/customer/reviews?tab=reviewed&page=${currentPage + 1}"
                               class="review-filter-btn">
                                <i class="fa-solid fa-chevron-right"></i>
                            </a>
                        </c:if>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </c:if>

    <!-- Tab 2: Pending Reviews -->
    <c:if test="${currentTab == 'pending'}">
        <c:choose>
            <c:when test="${empty pendingList}">
                <div class="empty-state-box">
                    <div class="empty-state-icon" style="color: #10b981;">
                        <i class="fa-solid fa-circle-check"></i>
                    </div>
                    <div class="empty-state-title">Tuyệt vời! Bạn đã hoàn thành tất cả đánh giá</div>
                    <p class="empty-state-desc">Hiện không có sản phẩm nào đang chờ nhận xét. Hãy tiếp tục mua sắm các loại trái cây tươi ngon khác nhé!</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn-write-review-now">
                        Khám Phá Trái Cây
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach var="item" items="${pendingList}">
                    <div class="pending-item-card">
                        <div class="product-brief">
                            <img src="${not empty item.productImage ? item.productImage : 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=100'}"
                                 alt="${item.productName}" class="product-brief-img"
                                 onerror="this.src='https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=100';">
                            <div>
                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}" class="product-brief-name">
                                    ${item.productName}
                                </a>
                                <div class="product-brief-order">
                                    <i class="fa-solid fa-receipt"></i> Đơn hàng #${item.orderId}
                                    <span style="color: #cbd5e1; margin: 0 6px;">•</span>
                                    <i class="fa-solid fa-calendar"></i>
                                    <fmt:formatDate value="${item.orderDate}" pattern="dd/MM/yyyy"/>
                                </div>
                            </div>
                        </div>

                        <a href="${pageContext.request.contextPath}/write-review?productId=${item.productId}&orderId=${item.orderId}&redirect=/customer/reviews%3Ftab=reviewed"
                           class="btn-write-review-now">
                            <i class="fa-solid fa-pen-to-square"></i> Viết Đánh Giá
                        </a>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:if>

</main>

<!-- Delete Modal -->
<div id="deleteReviewModal" class="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalTitle"
     style="display:none; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 2100; align-items: center; justify-content: center;">
    <div class="delete-modal-box" style="background: #ffffff; border-radius: 16px; padding: 24px; max-width: 440px; width: 90%; box-shadow: 0 20px 40px rgba(0,0,0,0.2);">
        <div class="delete-modal-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div style="display: flex; align-items: center; gap: 10px;">
                <div class="delete-modal-icon-wrap" style="color: #ef4444; font-size: 20px;">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                </div>
                <div class="delete-modal-title" id="modalTitle" style="font-weight: 700; font-size: 16px; color: #1e293b;">Xác nhận xóa đánh giá</div>
            </div>
            <button type="button" class="delete-modal-close" onclick="closeDeleteModal()" aria-label="Đóng popup"
                    style="background: none; border: none; font-size: 18px; cursor: pointer; color: #94a3b8;">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>
        <div class="delete-modal-body" style="margin-bottom: 20px;">
            <p style="font-size: 13.5px; color: #64748b; line-height: 1.5; margin-bottom: 12px;">
                Bạn có chắc chắn muốn xóa đánh giá cho <strong id="modalProductName"></strong>? Hành động này không thể hoàn tác.
            </p>
            <div style="background: #f8fafc; padding: 12px; border-radius: 10px; border-left: 3px solid #ef4444;">
                <div id="modalReviewStars" style="color: #f59e0b; margin-bottom: 4px; font-size: 13px;"></div>
                <div id="modalReviewExcerpt" style="font-size: 13px; color: #334155; font-style: italic;"></div>
            </div>
        </div>
        <form id="deleteReviewForm" action="${pageContext.request.contextPath}/delete-review" method="post">
            <input type="hidden" name="review_id" id="deleteReviewId" value="">
            <input type="hidden" name="redirect" value="/customer/reviews?tab=reviewed">
            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" onclick="closeDeleteModal()"
                        style="padding: 8px 16px; border-radius: 8px; border: 1px solid #cbd5e1; background: #ffffff; color: #475569; font-weight: 600; cursor: pointer;">
                    Hủy
                </button>
                <button type="submit"
                        style="padding: 8px 16px; border-radius: 8px; border: none; background: #ef4444; color: #ffffff; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: 6px;">
                    <i class="fa-solid fa-trash-can"></i> Xóa
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    function openDeleteModal(btn) {
        const rId = btn.getAttribute('data-review-id');
        const pName = btn.getAttribute('data-product-name') || 'sản phẩm này';
        const rating = parseInt(btn.getAttribute('data-rating')) || 5;
        const comment = btn.getAttribute('data-comment') || '';

        document.getElementById('deleteReviewId').value = rId;
        document.getElementById('modalProductName').textContent = pName;

        let starsHtml = '';
        for (let i = 1; i <= 5; i++) {
            starsHtml += (i <= rating) ? '<i class="fa-solid fa-star"></i> ' : '<i class="fa-regular fa-star"></i> ';
        }
        document.getElementById('modalReviewStars').innerHTML = starsHtml;

        const excerpt = comment.length > 90 ? comment.substring(0, 90) + '...' : comment;
        document.getElementById('modalReviewExcerpt').textContent = '"' + excerpt + '"';

        document.getElementById('deleteReviewModal').style.display = 'flex';
    }

    function closeDeleteModal() {
        document.getElementById('deleteReviewModal').style.display = 'none';
    }
</script>

</body>
</html>
