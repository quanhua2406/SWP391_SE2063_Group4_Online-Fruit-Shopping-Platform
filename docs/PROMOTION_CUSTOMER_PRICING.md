# Sửa giá khuyến mãi phía khách mua — 12/11/2026

## Nguyên nhân

Bản trước chỉ gộp màn hình quản lý. Các truy vấn sản phẩm/giỏ hàng vẫn dùng product_variants.price, chưa áp dụng promotions hoặc variant_promotions.

## Luồng sau khi sửa

- PromotionPricing.customerVariants() tạo nguồn giá bán dùng chung: giá gốc, giá chương trình còn hiệu lực, giá biến thể đã approved trong hạn; chọn mức thấp nhất, không cộng dồn hai loại khuyến mãi.
- Chương trình theo sản phẩm: bắt đầu <= hiện tại < kết thúc, đang bật, chưa xóa, đúng shop và sản phẩm; dùng giờ Việt Nam. Giảm phần trăm làm tròn số tiền giảm về đồng; giảm cố định không làm giá âm.
- Khuyến mãi biến thể: chỉ approved, bao gồm cả ngày kết thúc. Pending/rejected không áp dụng.
- ProductDAO: danh sách, tìm kiếm/gợi ý, sản phẩm nổi bật/liên quan dùng giá mới ngay trước lọc giá, sắp xếp và phân trang.
- Trang chi tiết và các nút chọn quy cách dùng giá mới. Màn chỉnh sửa của chủ shop vẫn đọc giá gốc để tránh vô tình lưu đè giá khuyến mãi vào giá gốc.
- CartDAO: giá mới cho giỏ đăng nhập, làm mới giỏ khách vãng lai, trang thanh toán.
- OrderDAO: xác nhận lại giá bằng cùng connection trong transaction SERIALIZABLE trước khi tạo đơn. Nếu giá thay đổi, yêu cầu tải lại trang thanh toán. Order items và payments lưu giá đã xác nhận; thay đổi khuyến mãi sau đó không sửa lịch sử đơn hàng.
- Coupon nhập lúc thanh toán tiếp tục tính trên tổng hàng sau promotion, theo logic coupon hiện có. Coupon không tự thay đổi giá trên trang sản phẩm.

## Cách chạy

1. Dùng source trong ZIP này thay cho bản promotion trước; mở đúng thư mục chứa pom.xml.
2. Clean and Build, deploy lại WAR trên Tomcat 10.1. Nếu còn bản deploy cũ, undeploy bản đó trước khi deploy bản mới.
3. Không cần đổi giá gốc hoặc chạy migration mới. Database phải đã có promotions, promotion_products, variant_promotions theo schema đi kèm.
4. Tạo chương trình giảm 10% cho sản phẩm giá 100.000đ, chọn đúng shop/sản phẩm, bật và đặt thời gian bao gồm hiện tại.
5. Tải lại trang sản phẩm/giỏ hàng: đơn giá phải là 90.000đ. Mua 2 đơn vị: tiền hàng 180.000đ, cộng phí giao hàng và trừ coupon nếu có.
6. Tắt chương trình rồi tải lại: giá trở về 100.000đ. Đơn đã đặt trước đó vẫn giữ đơn giá 90.000đ.

## Kiểm tra

- 22 test promotion đạt, bao gồm 8 test mới về giá bán/giỏ hàng/lưu đơn/thanh toán, mốc thời gian, shop, duyệt biến thể và giá đã thay đổi.
- Toàn bộ: 173 tests, 163 đạt, 10 failures ở delivery đã có từ trước, không phát sinh lỗi test mới.
- Biên dịch Java/JSP đạt. Test DB dùng H2; chưa chạy thực tế trên SQL Server + Tomcat của bạn, chưa đo hiệu năng dữ liệu lớn hay xác nhận locking trên SQL Server.
- File promotion-customer-pricing.patch chứa thay đổi từ bản promotion đã gửi trước đó.
