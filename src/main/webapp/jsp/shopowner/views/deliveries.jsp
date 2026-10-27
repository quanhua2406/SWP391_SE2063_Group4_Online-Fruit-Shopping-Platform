<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">NHÂN VIÊN GIAO HÀNG</p>
<h1>Giao hàng của tôi</h1>
<p class="muted">Mở chi tiết để xem đơn hàng, địa chỉ và cập nhật tiến độ.</p>
<section class="card" id="delivery-list"><%@ include file="delivery-table.jspf" %></section>
<%@ include file="footer.jspf" %>
