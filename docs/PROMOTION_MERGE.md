# Gộp promotion vào code chung

## Đường dẫn sau khi gộp

| Chức năng | Đường dẫn |
| --- | --- |
| Danh sách, tạo, sửa, chi tiết, bật/tắt, xóa chương trình | `/store-owner/promotions` |
| Tương thích liên kết chương trình cũ | `/shopowner/shop/promotions` |
| Giá khuyến mãi từng biến thể, gửi admin duyệt | `/store-owner/variant-promotions` |
| Admin duyệt giá biến thể | `/admin/promotion-approvals` |

`PromotionController` xử lý cả URL chung và URL chương trình cũ bằng cùng một service/DAO. Form, redirect, dashboard và thanh điều hướng đều dùng URL chung. `StoreOwnerPromotionServlet` tiếp tục xử lý luồng biến thể ở URL riêng; có liên kết qua lại giữa hai màn hình.

Hai mô hình dữ liệu có nghiệp vụ khác nhau: `promotions` / `promotion_products` là chương trình giảm theo sản phẩm; `variant_promotions` là yêu cầu giá biến thể cần duyệt. Bản gộp không xóa hay chuyển đổi dữ liệu giữa hai mô hình. Bản sửa ngày 12/11/2026 đã nối cả hai mô hình vào giá khách mua; xem PROMOTION_CUSTOMER_PRICING.md.

## Bảo vệ đường dẫn

Hai URL `/store-owner/…promotions` đi qua cùng nhánh kiểm tra session, role và CSRF như module chương trình cũ. GET thiết lập `currentRole`, token; POST bắt buộc token đúng. Form biến thể bổ sung `csrfToken`. Action không hợp lệ trả HTTP 400.

## Chạy dự án

1. Giải nén, mở thư mục chứa `pom.xml`.
2. Dùng database hiện có với các bảng promotion trong `database/reference/OnlineFruitShopping_DB_FINAL_MERGED.sql`. Không cần migration mới cho thay đổi đường dẫn này; không chạy lại script tạo toàn bộ database trên dữ liệu đang dùng.
3. Chạy `mvn clean verify -DskipTests=false` (pom hiện mặc định bỏ qua tests nếu không ghi đè).
4. Deploy WAR mới lên Tomcat 10.1, đăng nhập chủ cửa hàng, chọn Khuyến mãi từ dashboard.
5. Kiểm tra tạo/sửa/bật/tắt chương trình; mở mục “Khuyến mãi từng biến thể · Admin duyệt” để kiểm tra quy trình duyệt cũ.

Các thư mục build cũ `out/`, `target/` không đưa vào ZIP để tránh deploy lại servlet/JSP đã cũ. Cấu hình kết nối database giữ nguyên.
