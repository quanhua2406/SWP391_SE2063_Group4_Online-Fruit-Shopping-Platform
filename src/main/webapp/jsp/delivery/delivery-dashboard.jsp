<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../shopowner/views/header.jspf" %>

<div class="page-heading">
    <div>
        <p class="eyebrow">KHÔNG GIAN LÀM VIỆC</p>

        <h1>
            Xin chào, <c:out value="${sessionScope.user.fullName}"/>
        </h1>

        <p class="muted">
            Theo dõi đơn được phân công và cập nhật tiến độ giao hàng.
        </p>
    </div>

    <a class="button"
       href="${ctx}/shopowner/delivery/list">
        Xem tất cả lần giao
    </a>
</div>

<section class="shipper-overview"
         aria-label="Truy cập nhanh theo trạng thái">

    <a class="shipper-shortcut"
       href="${ctx}/shopowner/delivery/list?status=Assigned">

        <span class="shipper-shortcut-label">CHỜ XÁC NHẬN</span>

        <h2>Chờ nhận đơn</h2>

        <p>
            Kiểm tra đơn mới và xác nhận nhận hoặc từ chối.
        </p>

        <span class="shipper-shortcut-link">
            Xem danh sách →
        </span>
    </a>

    <a class="shipper-shortcut"
       href="${ctx}/shopowner/delivery/list?status=Accepted">

        <span class="shipper-shortcut-label">TẠI CỬA HÀNG</span>

        <h2>Chờ lấy hàng</h2>

        <p>
            Các đơn đã nhận và đang chờ lấy hàng tại shop.
        </p>

        <span class="shipper-shortcut-link">
            Xem danh sách →
        </span>
    </a>

    <a class="shipper-shortcut"
       href="${ctx}/shopowner/delivery/list?status=PickedUp">

        <span class="shipper-shortcut-label">SẴN SÀNG GIAO</span>

        <h2>Đã lấy hàng</h2>

        <p>
            Các đơn đã lấy hàng và chờ bắt đầu giao.
        </p>

        <span class="shipper-shortcut-link">
            Xem danh sách →
        </span>
    </a>

    <a class="shipper-shortcut"
       href="${ctx}/shopowner/delivery/list?status=Delivering">

        <span class="shipper-shortcut-label">ĐANG THỰC HIỆN</span>

        <h2>Đang giao hàng</h2>

        <p>
            Xem thông tin người nhận và cập nhật kết quả giao.
        </p>

        <span class="shipper-shortcut-link">
            Xem danh sách →
        </span>
    </a>

    <a class="shipper-shortcut"
       href="${ctx}/shopowner/delivery/list?status=Delivered">

        <span class="shipper-shortcut-label">LỊCH SỬ</span>

        <h2>Giao thành công</h2>

        <p>
            Tra cứu các lần giao đã hoàn tất.
        </p>

        <span class="shipper-shortcut-link">
            Xem lịch sử →
        </span>
    </a>

    <a class="shipper-shortcut"
       href="${ctx}/shopowner/delivery/list?status=Failed">

        <span class="shipper-shortcut-label">KẾT QUẢ KHÁC</span>

        <h2>Giao thất bại</h2>

        <p>
            Xem lại các lần giao không thành công và lý do.
        </p>

        <span class="shipper-shortcut-link">
            Xem danh sách →
        </span>
    </a>

</section>

<section class="card">
    <h2>Trình tự cập nhật</h2>

    <p class="muted">
        Nhận đơn → Lấy hàng tại shop → Bắt đầu giao → Cập nhật kết quả.
    </p>

    <p class="muted">
        Với đơn COD, kiểm tra số tiền cần thu trước khi xác nhận
        giao thành công. Nếu từ chối hoặc giao thất bại,
        hãy ghi rõ lý do.
    </p>
</section>

<%@ include file="../shopowner/views/footer.jspf" %>
