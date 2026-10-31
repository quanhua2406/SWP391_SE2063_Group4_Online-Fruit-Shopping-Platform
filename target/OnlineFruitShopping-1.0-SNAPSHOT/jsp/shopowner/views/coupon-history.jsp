<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">CHỦ CỬA HÀNG · MÃ GIẢM GIÁ</p>
<div class="page-heading"><h1>Lịch sử sử dụng mã</h1><a href="${ctx}/shopowner/shop/coupons">Về danh sách mã</a></div>
<p class="muted">Lịch sử thuộc các shop của bạn, bao gồm mã đã xóa và các lần đã gỡ. Số tiền hiển thị là tại lần áp dụng.</p>
<section class="card"><%@ include file="coupon-usage-table.jspf" %></section>
<%@ include file="footer.jspf" %>
