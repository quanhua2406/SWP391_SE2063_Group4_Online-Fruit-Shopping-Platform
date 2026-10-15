<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${plan.name} | Chi tiết gói Combo FreshFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/quanht/style.css"/>
</head>
<body class="customer-plans-page">

<!-- Global Navigation Bar -->
<header class="cust-navbar">
    <a href="${pageContext.request.contextPath}/home" class="brand">
        🍊 <span>FreshFruit</span>
    </a>
    <nav class="nav-links">
        <a href="${pageContext.request.contextPath}/subscriptions" class="nav-link">← Danh sách gói</a>
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/my-subscriptions" class="nav-link">📋 Gói của tôi</a>
                <span style="font-size:0.9rem; font-weight:700; color:var(--primary-900);">
                    👤 ${sessionScope.user.fullName}
                </span>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login" class="btn-nav-primary">Đăng nhập</a>
            </c:otherwise>
        </c:choose>
    </nav>
</header>

<div class="customer-container" style="max-width:960px;">

    <!-- Breadcrumb -->
    <div style="margin-bottom:20px;">
        <a href="${pageContext.request.contextPath}/subscriptions" style="color:var(--primary-800); font-weight:700; text-decoration:none; font-size:0.9rem;">
            ← Quay lại khám phá các gói combo
        </a>
    </div>

    <!-- Main Detail Card -->
    <div style="background:#ffffff; border-radius:var(--radius-lg); padding:36px; border:1px solid var(--border-light); box-shadow:var(--shadow-sm); margin-bottom:30px;">
        
        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:16px; margin-bottom:16px;">
            <div>
                <span class="badge-shop" style="font-size:0.85rem; padding:6px 14px;">🏬 Cửa hàng / Nhà vườn: ${plan.shopName}</span>
                <h1 style="font-size:2.2rem; font-weight:800; color:var(--text-title); margin:12px 0 8px;">
                    ${plan.name}
                </h1>
                <div style="display:flex; gap:10px; flex-wrap:wrap;">
                    <span class="benefit-chip">📅 Giao định kỳ: <strong>${plan.billingCycleLabel}</strong></span>
                    <span class="benefit-chip">⏳ Hiệu lực mỗi kỳ: <strong>${plan.durationDays} ngày</strong></span>
                    <span class="benefit-chip">🚚 Miễn phí giao hàng</span>
                </div>
            </div>
            
            <div style="text-align:right;">
                <c:if test="${savings != null and savings.savingsPercent > 0}">
                    <span class="badge-save" style="font-size:0.9rem; padding:6px 14px; margin-bottom:6px; display:inline-block;">
                        🔥 Tiết kiệm ${savings.savingsPercent}%
                    </span>
                </c:if>
                <div style="font-size:2.2rem; font-weight:800; color:#15803d; line-height:1;">
                    <fmt:formatNumber value="${plan.price}" pattern="#,###"/>đ
                </div>
                <div style="color:var(--text-muted); font-size:0.88rem; margin-top:4px;">
                    Thanh toán mỗi ${plan.billingCycleLabel}
                </div>
            </div>
        </div>

        <p style="font-size:1.05rem; line-height:1.6; color:var(--text-body); margin:0 0 28px; border-top:1px solid var(--border-light); padding-top:18px;">
            ${plan.description}
        </p>

        <!-- Fruits in combo list -->
        <div style="margin-bottom:32px;">
            <h3 style="font-size:1.25rem; font-weight:800; color:var(--text-title); margin:0 0 16px; display:flex; align-items:center; gap:8px;">
                <span>🧺</span> Thành phần trái cây tuyển chọn trong combo
            </h3>

            <div style="border:1px solid var(--border-light); border-radius:12px; overflow:hidden;">
                <table style="width:100%; border-collapse:collapse; font-size:0.95rem;">
                    <thead>
                        <tr style="background:#F1F8F2; text-align:left; color:var(--primary-900);">
                            <th style="padding:12px 16px;">Sản phẩm trái cây</th>
                            <th style="padding:12px 16px;">Quy cách / Biến thể</th>
                            <th style="padding:12px 16px; text-align:center;">Số lượng</th>
                            <th style="padding:12px 16px; text-align:right;">Giá gốc tham khảo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty plan.items}">
                                <c:forEach var="it" items="${plan.items}">
                                    <tr style="border-bottom:1px solid var(--border-light);">
                                        <td style="padding:14px 16px; font-weight:700; color:var(--text-title);">
                                            🍎 ${it.productName}
                                        </td>
                                        <td style="padding:14px 16px; color:var(--text-muted);">
                                            ${it.variantName}
                                        </td>
                                        <td style="padding:14px 16px; text-align:center;">
                                            <span class="qty-badge" style="font-size:0.85rem; padding:4px 10px;">
                                                x${it.quantity}
                                            </span>
                                        </td>
                                        <td style="padding:14px 16px; text-align:right; font-weight:600;">
                                            <fmt:formatNumber value="${it.unitPrice * it.quantity}" pattern="#,###"/>đ
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="4" style="padding:20px; text-align:center; color:var(--text-muted);">
                                        Combo trái cây chọn lọc theo mùa của nhà vườn.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                    <tfoot style="background:#FAFBF9; font-weight:700;">
                        <c:if test="${savings != null and savings.originalPrice > 0}">
                            <tr>
                                <td colspan="3" style="padding:12px 16px; text-align:right; color:var(--text-muted);">
                                    Tổng giá trị mua lẻ:
                                </td>
                                <td style="padding:12px 16px; text-align:right; text-decoration:line-through; color:var(--text-light);">
                                    <fmt:formatNumber value="${savings.originalPrice}" pattern="#,###"/>đ
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <td colspan="3" style="padding:14px 16px; text-align:right; color:var(--primary-900); font-size:1.05rem;">
                                Giá combo đăng ký trọn gói:
                            </td>
                            <td style="padding:14px 16px; text-align:right; color:#15803d; font-size:1.3rem;">
                                <fmt:formatNumber value="${plan.price}" pattern="#,###"/>đ
                            </td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>

        <!-- Big CTA -->
        <div style="background:#F1F8F2; border-radius:14px; padding:24px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:16px; border:1px solid #C8E6C9;">
            <div>
                <h4 style="margin:0 0 4px; font-size:1.15rem; font-weight:800; color:var(--primary-900);">
                    Sẵn sàng nhận trái cây tươi mỗi ${plan.billingCycleLabel}?
                </h4>
                <p style="margin:0; font-size:0.9rem; color:var(--text-body);">
                    Miễn phí giao hàng tận nơi. Linh hoạt dừng hoặc hủy gói bất kỳ lúc nào.
                </p>
            </div>
            <a href="${pageContext.request.contextPath}/subscription/subscribe?planId=${plan.planId}"
               class="btn-subscribe-cta"
               style="font-size:1.1rem; padding:14px 28px;">
                Đăng Ký Gói Ngay 🚀
            </a>
        </div>

    </div>

</div>

</body>
</html>
