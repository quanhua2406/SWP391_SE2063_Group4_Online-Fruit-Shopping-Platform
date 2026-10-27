# Bản sửa Delivery, ShopDelivery, Coupon và Promotion — 27/10/2026

Bản này được sửa trực tiếp từ ZIP `SWP391_SE2063_Group4_Online-Fruit-Shopping-Platform-main (2)(1).zip` bạn vừa gửi. Không thay toàn bộ source bằng bản project cũ. ZIP đầu vào không có lịch sử Git nên không thể xác minh commit mới nhất trên GitHub hoặc những commit đã nêu trong báo cáo OpenCode.

## Mở project

1. Giải nén vào thư mục mới. Mở thư mục `OnlineFruitShopping` chứa `pom.xml` trong NetBeans/IntelliJ.
2. Dùng JDK 17, Maven và Tomcat 10.1 (Jakarta Servlet 6). Lần build đầu cần mạng để tải dependencies.
3. Cấu hình kết nối SQL Server theo phần dưới, cho đúng tiến trình Tomcat.
4. Chạy `mvn -B clean verify`. WAR nằm ở `target/OnlineFruitShopping-1.0-SNAPSHOT.war`.
5. Undeploy bản cũ, deploy WAR vừa build và đăng nhập lại. Không chép đè các file class riêng lẻ lên bản đang chạy.

Context path tùy cấu hình Tomcat. Nếu đổi tên WAR thành `OnlineFruitShopping.war`, trang đăng nhập là `/OnlineFruitShopping/login` trên host/port Tomcat của bạn.

## Cấu hình database — cần làm trước khi chạy

`DBConnection` vẫn giữ API `getConnection()` chung. Đã bỏ mật khẩu viết trực tiếp trong Java và cách thử một mật khẩu khác khi kết nối lỗi. Class đọc cấu hình theo thứ tự: JVM property → biến môi trường → giá trị mặc định (nếu có).

| Nội dung | Biến môi trường | JVM property | Mặc định |
| --- | --- | --- | --- |
| JDBC URL | `FRUITSHOP_DB_URL` | `fruitshop.db.url` | SQL Server localhost:1433, database OnlineFruitShopping |
| User | `FRUITSHOP_DB_USER` | `fruitshop.db.user` | sa |
| Password | `FRUITSHOP_DB_PASSWORD` | `fruitshop.db.password` | Không có; phải cấu hình |

Ví dụ PowerShell để khởi động Tomcat **test** từ cùng cửa sổ terminal:

```powershell
$env:FRUITSHOP_DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping_Test;encrypt=true;trustServerCertificate=true'
$env:FRUITSHOP_DB_USER = 'sa'
$secureDbPassword = Read-Host 'Mat khau SQL Server test' -AsSecureString
$env:FRUITSHOP_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secureDbPassword).Password
# Chuyển tới thư mục bin của Tomcat test rồi chạy:
.\catalina.bat run
```

Nếu chạy Tomcat qua IDE hoặc Windows service, cấu hình các biến trên cho chính tiến trình đó và khởi động lại. Biến trong một cửa sổ PowerShell khác không tự truyền sang IDE/service đang chạy.

Bản ZIP **không chứa script đầy đủ `OnlineFruitShopping.sql`**. Không tự tạo database nghiệp vụ từ schema H2 trong test. Dùng schema đã thống nhất của nhóm, rồi chạy `tools/check-module-schema.sql` để kiểm tra các cột mà bốn module cần. Đây là script chỉ đọc: kết quả rỗng chỉ chứng minh các cột trong danh sách tồn tại, chưa chứng minh constraints, kiểu dữ liệu, trigger, index hay nghiệp vụ đúng.

Các DAO của bốn module đang dùng `deliveries.shipper_id`, `delivery_status`, `orders.order_status`, `order_items.price`. Một số phần khác vẫn dùng tên cột/trạng thái khác. Chưa đổi schema hoặc sửa `OrderDAO`/`AdminOrderDAO`; nhóm cần đối chiếu với database thực tế trước khi kiểm thử xuyên suốt mua hàng → giao hàng.

## Những phần đã sửa

- Đưa các controller/service/DAO/model của module riêng `onlinefruitshopping.shopowner.*` về các package chung. Không còn `Account`, `AccountDAO`, `AccountService` và `AuthFilter` riêng của module.
- Bốn module dùng `model.User` trong session `user`, qua `UserSession`/`WebUtil`; role được chuẩn hóa. `ServiceChecks` nằm ở `service.common`.
- Chỉ còn một `@WebFilter` là `AuthorizationFilter`. Với `/shopowner/*`, filter kiểm tra tài khoản/role hiện tại trong database, quyền endpoint và CSRF. GET chưa đăng nhập chuyển tới login; POST chưa đăng nhập trả 401; sai role/CSRF trả 403; lỗi kiểm tra tài khoản trả 503.
- Các endpoint ngoài module vẫn dùng chính sách hiện có của bản ZIP mới qua nhánh `legacyFilter`. Không mở rộng chính sách chặn mặc định sang checkout, orders, Admin hay subscription của thành viên khác. Đây không phải kết luận rằng mọi endpoint khác đã được kiểm toán.
- Giữ `userId`/`roleName` do login hiện tại tạo để các caller ngoài phạm vi không bị hỏng. Bốn module mới không dựa vào hai alias này. Login xóa cache `shopId`, principal cũ và đổi CSRF token; vẫn giữ giỏ hàng khách và logic login mới của nhóm.
- Chuyển 19 JSP/JSPF từ `WEB-INF/shopowner/views` sang `src/main/webapp/jsp/shopowner/views`. Trong NetBeans, đó là **Web Pages → jsp → shopowner → views**. Truy cập qua controller; request trực tiếp tới nhóm JSP này trả 404, forward nội bộ vẫn hoạt động.
- Giữ các getter của `model.Delivery` mà Admin đang dùng. Các getter `id`/`shipperId`/`shipperName` của module trỏ tới cùng dữ liệu với `deliveryId`/`staffId`/`staffName` hiện tại; không tạo hai bản giá trị khác nhau.
- Đổi model dòng sản phẩm của giao hàng thành `dto.delivery.DeliveryOrderItem`, và service chương trình khuyến mãi thành `service.promotion.CampaignPromotionService`, tránh đè model/service đã có của nhóm.
- Giữ quyền Customer xem đơn của mình đã có trong ZIP mới. Truy vấn đọc items kiểm tra lại phạm vi Customer/ShopOwner/Shipper tại thời điểm lấy items, tránh lộ dữ liệu nếu phân công đổi giữa hai lần truy vấn.
- Giữ khóa và transaction có sẵn trong phân công/cập nhật trạng thái. Bổ sung rollback cho RuntimeException, kiểm tra số dòng cập nhật đơn, và khả năng truyền connection riêng để kiểm thử.
- Sửa link Track Order còn là `#` trong dashboard dự phòng. Giữ link View Cart và Track Order đã đúng ở dashboard mới.
- Giữ nguyên nghiệp vụ Coupon/Promotion khi chuyển package: validation, sở hữu shop, version, xóa mềm, giới hạn sử dụng, phạm vi đơn của khách và kiểm tra trùng lịch. Không sửa luồng giá sản phẩm/variant promotion/approval của thành viên khác.

Notification được chuyển package cùng module vì phân công/cập nhật giao hàng ghi thông báo trong cùng transaction. Không xây thêm một hệ thống notification mới.

## Đường dẫn sử dụng

Các đường dẫn dưới đây tính từ context path:

| Module | URL | Người sử dụng |
| --- | --- | --- |
| ShopDelivery | `/shopowner/shop/deliveries` | ShopOwner |
| Delivery | `/shopowner/delivery/list`, `/shopowner/delivery/detail?id=...` | Customer, ShopOwner, DeliveryStaff theo quyền dữ liệu |
| Cập nhật giao hàng | POST `/shopowner/delivery/update` | DeliveryStaff được phân công |
| Coupon quản lý | `/shopowner/shop/coupons` | ShopOwner |
| Áp/gỡ coupon | `/shopowner/customer/coupons` | Customer |
| Promotion chương trình | `/shopowner/shop/promotions` | ShopOwner |

## Kết quả kiểm chứng của bản này

- `mvn -B clean verify`: build Java, test và WAR thành công; số liệu chi tiết trong `KET_QUA_KIEM_THU.txt`.
- 126 test đạt, 0 thất bại, 0 lỗi, 0 bỏ qua. Đây là tổng unit/integration test cục bộ, gồm cả helper, session, filter và kiểm tra tương thích; không phải 126 ca HTTP runtime.
- Jasper biên dịch thành công 20 JSP (14 trang module + 2 dashboard Customer + dashboard ShopOwner/DeliveryStaff + 2 trang Admin Delivery), cùng các include JSPF.
- SQL của test dùng H2 riêng trong bộ nhớ. Không kết nối hay sửa dữ liệu SQL Server thật, không gọi thanh toán/email.

| Phạm vi | Đã chạy đạt | Chưa xác nhận trên SQL Server/Tomcat thật |
| --- | --- | --- |
| Delivery | Quy tắc trạng thái/thanh toán; quyền đọc A/B; truy vấn items khi đổi phân công; COD đổi Delivered/Completed/Paid; gửi lại không thêm thông báo; rollback khi thông báo thứ hai lỗi | Request trạng thái chạy đồng thời; dữ liệu/triggers thực tế; thao tác trình duyệt |
| ShopDelivery | Validation service; chủ shop/shipper hợp lệ; phân công một lần; gửi lại không tạo phân công/thông báo trùng; rollback khi ghi notification lỗi | Hai request phân công thực sự chồng nhau; lịch sử và khóa trên SQL Server |
| Coupon | Validation mã/tiền/thời hạn/quota ở rule/service; quyền service; JSP biên dịch | CRUD/áp/gỡ thực tế, quota đồng thời, version/lịch sử trên SQL Server |
| Promotion | Validation mức giảm/thời gian/sản phẩm ở rule/service; quyền service; JSP biên dịch | CRUD/overlap trên SQL Server, tích hợp giá cuối tại checkout của nhóm |

Test `acceptedAssignmentCannotThenBeRejected` là hai thao tác tuần tự, **không phải kiểm thử đồng thời**. Test H2 không xác nhận tác dụng khóa `UPDLOCK/HOLDLOCK` của SQL Server. Các phần runtime trên đang BLOCKED bởi thiếu database, fixture A/B và Tomcat test tương ứng; không lấy số 69/69 hay 98 test từ báo cáo cũ làm kết quả của ZIP này.

Trước bàn giao, chạy ba tình huống trên DB test: hai request phân công cùng đơn; accept/reject cùng đơn Assigned; hoàn tất COD rồi gửi lại, đối chiếu deliveries/orders/notifications và gây lỗi giữa transaction. Tiếp tục kiểm tra chủ shop → shipper → customer và CRUD Coupon/Promotion bằng tài khoản A/B. Không tính ca chưa chạy thành PASS.

## Cấu hình riêng không nằm trong gói

ZIP không kèm `.git`, `target`, `out`, IDE settings, log hay mật khẩu SMTP. `pom.xml` không đóng gói `mail.properties` vào WAR. Giữ cấu hình email riêng ở môi trường của người phụ trách; có mẫu không chứa mật khẩu tại `config/mail.properties.example`. Có thể đặt tài nguyên `mail.properties` trên classpath dùng chung của Tomcat theo cấu hình triển khai nhóm đang sử dụng. Bốn module này dùng thông báo database, không cần gửi email để thực hiện các test đã chạy.

Khóa ký VNPay từng viết trực tiếp trong `VNPayConfig` được thay bằng biến `VNPAY_HASH_SECRET` hoặc JVM property `fruitshop.vnpay.hashSecret` để không gửi khóa trong source/WAR. Chỉ thay nơi lấy cấu hình, không sửa thuật toán/luồng thanh toán. Người phụ trách thanh toán cần cấp lại khóa qua môi trường; chưa kiểm thử VNPay.

## Đưa vào repository của nhóm

Đây là source ZIP đã sửa, chưa commit/push lên remote của nhóm. Không giải nén chồng trực tiếp vào working tree đang có thay đổi chưa lưu.

Dùng bản này làm thư mục đối chiếu, xem `DANH_SACH_THAY_DOI.txt`. Trên máy bạn, kiểm tra `git status --short --branch`, fetch cập nhật của nhóm, rồi đưa các thay đổi phù hợp vào nhánh `refactor/shopowner-common`. Lưu ý **xóa các class/package và view cũ đã được thay thế**, tránh cùng tồn tại hai servlet mapping. Giữ cấu hình cá nhân ngoài Git. Sau tích hợp, chạy build/test lại trước khi commit/push; không force push và không ghi đè công việc của thành viên khác.
