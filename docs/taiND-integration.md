# Tích hợp phần taiND từ main

Base: `3025b3c6d96ff62a9c68f4add2cfb7f23fb74bfc` (main).
Nguồn đối chiếu: `0fef8e0` (taiND).

## Phạm vi

- ShopOwner Delivery, Delivery, Coupon và Promotion chương trình.
- Chuyển package `onlinefruitshopping.shopowner` vào cấu trúc controller/dao/service/model/utils chung; chuyển views sang `jsp/shopowner/views`.
- Xóa implementation cũ đã được thay thế để tránh trùng servlet mapping và hai filter xác thực.
- Notification đi kèm vì transaction phân công/cập nhật giao hàng ghi thông báo.
- `CampaignPromotionService` và `DeliveryOrderItem` có tên riêng để không đè service/model của chức năng khác.

## Điểm kết nối dùng chung

| File | Thay đổi giới hạn |
| --- | --- |
| `controller/auth/LoginServlet.java` | Xóa cache shop và tạo lại CSRF token khi đăng nhập; giữ userId/roleName và logic giỏ hàng hiện có. |
| `controller/auth/LogoutServlet.java` | Thêm alias `/shopowner/logout` cho endpoint cũ của module. |
| `dao/auth/UserDAO.java` | Thêm truy vấn kiểm tra user/role hiện tại của module và connection provider cho kiểm thử truy vấn này. Các phương thức DAO hiện có giữ nguyên. |
| `service/auth/LoginService.java` | Thêm `isSessionValid` cho module. |
| `filter/AuthorizationFilter.java` | Xử lý riêng `/shopowner` và views của module; các route khác tiếp tục chạy logic filter của main. |
| `model/Delivery.java` | Bổ sung trường module và getter alias, giữ getter/setter Admin hiện có. |
| `pom.xml` | Chỉ bổ sung H2/Jasper/Ant ở scope test. |

Không đưa các thay đổi DBConnection, VNPay, email, cart/product/order, dashboard, README chung, cấu hình IDE hoặc binary target từ taiND vào PR này.

## Kiểm chứng

`mvn -B clean verify` trên nhánh tích hợp: **BUILD SUCCESS**, 126 tests, 0 failures/errors/skipped; tạo WAR thành công. Bao gồm kiểm thử quyền truy cập, transaction giao hàng, tương thích model Delivery và biên dịch JSP.

Chưa kiểm thử runtime với SQL Server/Tomcat thật. Có thể chạy script chỉ đọc `tools/check-module-schema.sql` để đối chiếu cột của module với database nhóm. Không áp dụng migration database trong PR này.

## URL module

- ShopOwner Delivery: `/shopowner/shop/deliveries`
- Delivery: `/shopowner/delivery/list`, `/shopowner/delivery/detail`
- Coupon ShopOwner: `/shopowner/shop/coupons`
- Coupon Customer: `/shopowner/customer/coupons`
- Promotion: `/shopowner/shop/promotions`

PR được tạo vào main để nhóm review các điểm kết nối dùng chung trước khi merge.
