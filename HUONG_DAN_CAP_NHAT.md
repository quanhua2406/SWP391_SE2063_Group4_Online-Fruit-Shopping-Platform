# Hướng dẫn cập nhật ShopOwner

## 1. Phạm vi và cấu trúc

Ghi nhận source của nhánh `refactor/shopowner-common` sau bước 10, ngày
25/10/2026; đối chiếu thay đổi với commit nền `2e24504`.
Đây là báo cáo refactor và kiểm thử tự động, không khẳng định toàn bộ project hết lỗi.

Module coupon, khuyến mãi và giao hàng chuyển từ package `shopowner.*` sang
`controller`, `dao`, `service` phân theo chức năng. Model dùng chung nằm trong
`model`; dữ liệu dòng sản phẩm giao hàng dùng `dto.delivery.DeliveryOrderItem`,
không thay thế `model.OrderItem` của nghiệp vụ khác.
Tên service thực tế là `service.promotion.PromotionService`, không phải
`CampaignPromotionService`. Kết nối production tiếp tục dùng `utils.DBConnection`.

## 2. Bảng thay đổi Java production

Đường dẫn Java dưới đây tính từ `src/main/java/com/mycompany/onlinefruitshopping/`.
Các dòng gộp tên file áp dụng cho từng file được liệt kê; file mới được đối chiếu
nội dung với file cũ, không xem rename là hai thay đổi độc lập.

| File/package cũ | File/package mới | Loại | Lý do và ảnh hưởng |
|---|---|---|---|
| `shopowner/controller/CouponController.java` | `controller/shopowner/CouponController.java` | Di chuyển, sửa | Import chung, User và forward JSP mới |
| `shopowner/controller/PromotionController.java` | `controller/shopowner/PromotionController.java` | Di chuyển, sửa | Service/model chung, forward ba trang promotion |
| `shopowner/controller/ShopDeliveryController.java` | `controller/shopowner/ShopDeliveryController.java` | Di chuyển, sửa | Quản lý giao hàng, forward `shop.jsp` |
| `shopowner/controller/CustomerCouponController.java` | `controller/customer/CustomerCouponController.java` | Di chuyển, sửa | Coupon khách hàng, forward `customer-coupons.jsp` |
| `shopowner/controller/DeliveryController.java` | `controller/delivery/DeliveryController.java` | Di chuyển, sửa | Truyền User xuống service, 404 detail ngoài quyền; POST chỉ DeliveryStaff; forward list/detail mới |
| `shopowner/controller/NotificationController.java` | `controller/notification/NotificationController.java` | Di chuyển, sửa | User chung, forward `notifications.jsp` |
| `shopowner/controller/LoginController.java` | `controller/auth/LegacyLoginRedirectServlet.java` | Xóa cũ, thêm chuyển hướng | Không xử lý đăng nhập riêng |
| `shopowner/controller/LogoutController.java` | `controller/auth/LogoutServlet.java` | Xóa cũ, sửa chung | Hai URL đăng xuất dùng một servlet |
| `shopowner/dao/AccountDAO.java`, `shopowner/service/AccountService.java`, `shopowner/model/Account.java` | `dao/auth/UserDAO.java`, `service/auth/LoginService.java`, `model/User.java` | Xóa cũ, sửa chung | Principal User; xác minh active và role qua DB |
| `shopowner/dao/CouponDAO.java`, `CouponApplicationDAO.java` | `dao/coupon/` cùng tên | Di chuyển, sửa | Dùng model/helper chung |
| `shopowner/dao/AssignmentDAO.java` | `dao/delivery/AssignmentDAO.java` | Di chuyển, sửa | Giữ kiểm tra sở hữu, nhân viên và transaction |
| `shopowner/dao/DeliveryDAO.java` | `dao/delivery/DeliveryDAO.java` | Di chuyển, sửa | ConnectionProvider; scope theo User, chặn đọc chéo items |
| `shopowner/dao/NotificationDAO.java` | `dao/notification/NotificationDAO.java` | Di chuyển, sửa | Import chung |
| `shopowner/dao/PromotionDAO.java` | `dao/promotion/PromotionDAO.java` | Di chuyển, sửa | Import chung |
| `shopowner/model/OrderItem.java` | `dto/delivery/DeliveryOrderItem.java` | Di chuyển, đổi tên | Tách DTO giao hàng khỏi model OrderItem chung |
| `shopowner/model/{Coupon,CouponOrder,CouponQuote,CouponUsage,Delivery,Notification,OrderOption,Promotion,PromotionPrice,PromotionProduct,ShopOption,Staff}.java` | `model/` cùng tên | Di chuyển, sửa | Model dùng chung |
| `shopowner/service/ServiceChecks.java` | `service/common/ServiceChecks.java` | Di chuyển, sửa | Nhận User, so role bằng LoginService |
| `shopowner/service/{CouponService,CouponApplicationService}.java` | `service/coupon/` cùng tên | Di chuyển, sửa | DAO/model chung; giữ constructor kiểm thử |
| `shopowner/service/DeliveryService.java` | `service/delivery/DeliveryService.java` | Di chuyển, sửa | Read cho ba role; assign cho owner, update cho shipper |
| `shopowner/service/NotificationService.java` | `service/notification/NotificationService.java` | Di chuyển, sửa | DAO chung |
| `shopowner/service/PromotionService.java` | `service/promotion/PromotionService.java` | Hợp nhất, sửa, xóa bản cũ | Service hiện tại là nghiệp vụ campaign; API variant promotion trước đây không còn trong class này |
| `shopowner/utils/{CouponRules,DeliveryList,DeliveryRules,PromotionRules}.java` | `utils/` cùng tên | Di chuyển, sửa | Helper dùng chung |
| `shopowner/utils/WebUtil.java` | `utils/WebUtil.java` | Di chuyển, sửa | User và token `csrfToken`; còn fallback `shopUser` kiểu User |
| — | `utils/UserSession.java` | Thêm | current/signIn/refresh; giữ role khi refresh |
| — | `utils/ConnectionProvider.java` | Thêm | Injection kết nối cho UserDAO/DeliveryDAO và H2 test |
| `shopowner/filter/AuthFilter.java` | `filter/AuthorizationFilter.java` | Xóa cũ, sửa chung | Một filter xác thực/phân quyền/CSRF |
| — | `filter/AccessPolicy.java` | Thêm | Helper chính sách URL/method, mặc định từ chối |
| `controller/auth/LoginServlet.java` | Cùng đường dẫn | Sửa | Xử lý exception theo LoginService hiện tại; xem giới hạn session bên dưới |
| `controller/customer/ProfileServlet.java` | Cùng đường dẫn | Sửa | Dùng UserSession.refresh |
| `controller/ProductServlet.java` | Cùng đường dẫn | Sửa | Kiểm tra role qua LoginService |
| `controller/customer/AddressServlet.java`, `controller/subscription/ShopOwnerPlanServlet.java` | Cùng đường dẫn | Sửa | Bổ sung import LoginService |
| `controller/shopowner/StoreOwnerCommissionReportServlet.java` | — | Xóa có sẵn trong đợt refactor | Chưa thấy mapping thay thế `/store-owner/commission-report` |
| `controller/shopowner/StoreOwnerCommissionServlet.java` | — | Xóa có sẵn trong đợt refactor | Chưa thấy mapping thay thế `/store-owner/commission` |
| `controller/shopowner/StoreOwnerCreateShopServlet.java` | — | Xóa có sẵn trong đợt refactor | Chưa thấy mapping thay thế `/store-owner/create-shop` |
| `controller/shopowner/StoreOwnerDashboardServlet.java` | — | Xóa có sẵn trong đợt refactor | Link `/store-owner/dashboard` còn được sử dụng; cần xử lý trước triển khai |
| `controller/shopowner/StoreOwnerInventoryServlet.java` | — | Xóa có sẵn trong đợt refactor | Các route `/store-owner/inventory` thiếu controller cũ |
| `controller/shopowner/StoreOwnerPricingServlet.java` | — | Xóa có sẵn trong đợt refactor | Chưa thấy mapping thay thế `/store-owner/pricing` |
| `controller/shopowner/StoreOwnerPromotionServlet.java` | — | Xóa có sẵn trong đợt refactor | Không tương đương route campaign `/shopowner/shop/promotions` |

Các xóa `StoreOwner*Servlet` trên được ghi nhận trung thực từ diff đã có,
không được mô tả là rename hoặc đã thay thế đầy đủ chức năng.

## 3. View và điều hướng

Tiền tố nguồn `src/main/webapp/WEB-INF/shopowner/views/` chuyển sang
`src/main/webapp/jsp/shopowner/views/`. Đủ 19 file:

| File cũ dưới tiền tố nguồn | File mới dưới tiền tố đích | Loại | Ảnh hưởng |
|---|---|---|---|
| coupon-detail.jsp | coupon-detail.jsp | Di chuyển, sửa | csrfToken |
| coupon-form.jsp | coupon-form.jsp | Di chuyển, sửa | csrfToken |
| coupon-history.jsp | coupon-history.jsp | Di chuyển | Giữ nội dung |
| coupon-usage-table.jspf | coupon-usage-table.jspf | Di chuyển | Include tương đối |
| coupons.jsp | coupons.jsp | Di chuyển, sửa | csrfToken |
| customer-coupons.jsp | customer-coupons.jsp | Di chuyển, sửa | csrfToken |
| deliveries.jsp | deliveries.jsp | Di chuyển, sửa | currentRole, nhãn Customer |
| delivery-filters.jspf | delivery-filters.jspf | Di chuyển, sửa | currentRole chọn route và bộ lọc |
| delivery-table.jspf | delivery-table.jspf | Di chuyển | Include tương đối |
| detail.jsp | detail.jsp | Di chuyển, sửa | Form update chỉ hiển thị DeliveryStaff, csrfToken |
| error.jsp | error.jsp | Di chuyển, sửa | Link đăng nhập chung |
| footer.jspf | footer.jspf | Di chuyển | Include tương đối |
| header.jspf | header.jspf | Di chuyển, sửa | Tên, role, menu Customer, POST logout, sửa `/shopowner//` |
| login.jsp | login.jsp | Di chuyển, thay nội dung | Chuyển hướng `/login`, không còn form login riêng |
| notifications.jsp | notifications.jsp | Di chuyển, sửa | csrfToken |
| promotion-detail.jsp | promotion-detail.jsp | Di chuyển, sửa | csrfToken |
| promotion-form.jsp | promotion-form.jsp | Di chuyển, sửa | csrfToken |
| promotions.jsp | promotions.jsp | Di chuyển, sửa | csrfToken |
| shop.jsp | shop.jsp | Di chuyển, sửa | csrfToken |

Sáu controller đổi forward được liệt kê ở bảng Java. URL servlet `/shopowner/...`
được giữ; link/form gọi servlet thay vì JSP nội bộ. Tên hiển thị dùng
`c:out` với `sessionScope.user.fullName`; điều kiện role dùng
`requestScope.currentRole`. Link dùng `ctx` hoặc `pageContext.request.contextPath`.

| File cũ | File mới | Loại | Ảnh hưởng |
|---|---|---|---|
| `src/main/webapp/customer-dashboard.jsp` | Cùng đường dẫn | Sửa | Cart/icon → `/cart`, Track Order → `/shopowner/delivery/list` |
| `src/main/webapp/jsp/customer/customer-dashboard.jsp` | Cùng đường dẫn | Sửa | Icon Cart → `/cart`; link View Cart/Track Order đã đúng được giữ |

`CustomerDashboardServlet` forward tới bản trong `jsp/customer/`.
Menu header chọn `/store-owner/dashboard`, `/delivery/dashboard` hoặc
`/customer/dashboard` theo role; việc tồn tại mapping đích cần đọc cùng giới hạn ở mục 2.

## 4. Session và xác thực

- Session `user` chứa `model.User`; danh tính lấy từ `getUserID()`, role từ
  `getRoleName()`. `LoginService.normalizeRole/roleMatches` chuẩn hóa role ở
  tầng service/filter; DAO dùng `ValidationUtil.normalizeRoleName`.
- `UserSession.current` không tự tạo session. `signIn` lưu User, dọn
  `shopUser`, `userId`, `roleName`, `shopId`, `shopcsrf`, đổi `csrfToken`, giữ
  giỏ khách. `refresh` chỉ cập nhật cùng ID, giữ role đã xác minh.
- **Đối chiếu thực tế:** ProfileServlet đã gọi `refresh`; LoginServlet hiện
  vẫn ghi `user/userId/roleName` trực tiếp, dọn `shopUser/shopcsrf` và đổi session ID,
  chưa gọi `UserSession.signIn`. Test helper không chứng minh servlet đã dùng helper.
- `LoginService.isSessionValid` gọi `UserDAO.isActiveWithRole`: user active và
  còn đúng role đang dùng. Truy vấn xác minh hỗ trợ nhiều role, không lấy TOP 1.
  Điều này khác truy vấn đăng nhập `findByEmail` hiện lấy dòng đầu tiên; luồng
  chọn role khi đăng nhập tài khoản nhiều role cần kiểm thử riêng.
- `/login` là endpoint chung; forward `/jsp/auth/login.jsp`.
- `LegacyLoginRedirectServlet`: GET `/shopowner`, `/shopowner/login` redirect
  tới contextPath + `/login`; POST trả 303 và header Location cùng đích.
  Servlet không xử lý mật khẩu, DB hoặc session riêng.
- `LogoutServlet` xử lý `/logout`, `/shopowner/logout`, invalidate và về login.
- Một số thông báo/comment đăng nhập trong source hiện có lỗi ký tự; kiểm thử
  logic đăng nhập không xác minh chất lượng hiển thị các chuỗi này.

## 5. Phân quyền URL và CSRF

`AccessPolicy` là helper, không đăng ký filter. Nó phân quyền theo route và HTTP
method; route không có chính sách bị từ chối. `AuthorizationFilter` chỉ đăng ký
dispatcher REQUEST. Request trực tiếp `/jsp/shopowner/views` và các đường dẫn
bên dưới bị trả 404 trước nhánh static/public/role; forward nội bộ sau kiểm tra
quyền không chạy lại filter. `web.xml` không đăng ký trùng filter.

Filter kiểm tra session có User bằng `isSessionValid`, invalid session bị hủy;
lỗi SQL khi xác minh trả 503. Guest GET route cần login được redirect; request
ghi cần login trả 401. Role sai hoặc CSRF sai trả 403.

Phạm vi **chính xác** của `requiresCsrf`: method không phải GET/HEAD và
path bằng `/logout`, hoặc bắt đầu `/shopowner/` nhưng không bằng
`/shopowner/login`. `/shopowner` không có dấu `/` cuối cũng không thuộc phạm vi
này. Các method không được chính sách cho phép có thể bị từ chối trước bước CSRF.
Đây không phải kiểm tra mọi POST trong toàn ứng dụng; giỏ khách vẫn public.

Form được bảo vệ gửi `name="csrfToken" value="${sessionScope.csrfToken}"`;
filter nhận header `X-CSRF-Token` trước, nếu không có thì đọc tham số form.
Token được tạo trong helper, không tạo trong JSP. Header module dùng POST `/logout`.
GET `/logout` vẫn tồn tại theo source hiện tại.

## 6. Phân quyền dữ liệu giao hàng

| Role | Scope đọc |
|---|---|
| CUSTOMER | `orders.user_id` bằng ID đăng nhập |
| SHOPOWNER | `shops.owner_id` bằng ID đăng nhập |
| DELIVERYSTAFF | `deliveries.shipper_id` bằng ID đăng nhập và quan hệ nhân viên/shop Active |

Delivery nối order theo cả order_id và shop_id; shop phải active. Giá trị ID bind
qua PreparedStatement; scope chỉ nối đoạn SQL cố định. User null, ID không hợp lệ
hoặc role khác bị từ chối. `findItems` gọi `findById` kiểm tra quyền trước khi lấy
items theo orderId của delivery đó.

`AssignmentDAO.assign` giữ kiểm tra chủ shop và nhân viên active thuộc đúng shop
trong transaction. `DeliveryDAO.updateStatus` giữ kiểm tra người được giao,
trạng thái hoạt động, trạng thái đơn, thanh toán, UPDLOCK/HOLDLOCK, commit/rollback
và thông báo. CUSTOMER/SHOPOWNER không được POST cập nhật trạng thái giao hàng.
H2 test chỉ xác nhận truy vấn đọc, không xác nhận khóa SQL Server.

## 7. Test và cấu hình build

Đường dẫn test dưới đây tính từ `src/test/java/com/mycompany/onlinefruitshopping/`.

| File/package cũ | File/package mới | Loại | Lý do/ảnh hưởng |
|---|---|---|---|
| `shopowner/utils/CouponRulesTest.java` | `utils/CouponRulesTest.java` | Di chuyển, sửa import | Giữ assertion coupon |
| `shopowner/utils/DeliveryRulesTest.java` | `utils/DeliveryRulesTest.java` | Di chuyển | Giữ assertion trạng thái/thanh toán |
| `shopowner/utils/DeliveryFilterTest.java` | `utils/DeliveryFilterTest.java` | Di chuyển, sửa import | Giữ assertion lọc/tìm kiếm |
| `shopowner/utils/PromotionRulesTest.java` | `utils/PromotionRulesTest.java` | Di chuyển, sửa import | Giữ assertion khuyến mãi |
| `shopowner/service/ServiceValidationTest.java` | `service/coupon/CouponServiceTest.java` | Tách | Quota, chuẩn hóa code, version, không chuyển shop |
| Cùng test cũ | `service/coupon/CouponApplicationServiceTest.java` | Tách | Validation, customer scope, SQL failure không retry |
| Cùng test cũ | `service/delivery/DeliveryServiceTest.java` | Tách | User fixture, role/action/reason, actor/COD, quyền phân công |
| Cùng test cũ | `service/notification/NotificationServiceTest.java` | Tách | ID và scope tài khoản |
| Cùng test cũ | `service/promotion/PromotionServiceTest.java` | Tách | Discount, deduplicate sản phẩm, owner ID, không chuyển shop |
| Cùng test cũ | `service/auth/LoginServiceTest.java` | Tách, sửa hợp đồng | LoginResult.Status, trim email nhưng giữ password, khóa/role sai |
| — | `support/ServletHarness.java` | Thêm | Fake HTTP boundary, gọi production thật |
| — | `filter/AccessPolicyTest.java` | Thêm | Ma trận role/route/method, view nội bộ, CSRF |
| — | `filter/AuthorizationFilterTest.java` | Thêm | Status/redirect/chain, session, CSRF, SQL failure, REQUEST |
| — | `utils/UserSessionTest.java` | Thêm | Dọn session, đổi token, giữ cart/role, không đổi identity |
| — | `dao/auth/UserSessionValidationTest.java` | Thêm | H2, active/nhiều role/thu hồi role |
| — | `dao/delivery/DeliveryOwnershipTest.java` | Thêm | H2, scope customer/owner/shipper, items, shop mismatch |
| — | `controller/auth/LegacyLoginRedirectServletTest.java` | Thêm | Mapping, GET redirect, POST 303, không tạo session |
| — | `view/JspCompilationTest.java` | Thêm | Jasper compile và kiểm tra đồ thị include |
| `pom.xml` (gốc repo) | Cùng đường dẫn | Sửa | H2 2.5.252, Jasper 10.1.61, Ant 1.10.18 scope test; giữ JUnit/Surefire, Java 17/Jakarta EE 10 |

ServiceValidationTest cũ được xóa sau khi tách. Test chung coupon/promotion được
tách thành hai test giữ đủ assertion. Không khôi phục AccountService/AccountDAO,
không đổi constructor package-private thành public để phục vụ test.

## 8. Kết quả tại thời điểm refactor

Lần chạy bước 10: `mvn -B clean verify`, ngày 25/10/2026: **BUILD SUCCESS**.
Đọc XML `target/surefire-reports`: **78 tests, 0 failures, 0 errors, 0 skipped**.
Không sử dụng tùy chọn bỏ test hay bỏ assertion.

JspCompilationTest thực sự chạy: **16 JSP** (14 JSP module + 2 Customer Dashboard).
Đủ 5 JSPF qua include: `header.jspf`, `footer.jspf`, `delivery-table.jspf`,
`delivery-filters.jspf`, `coupon-usage-table.jspf`. Biên dịch JSP không xác minh
dữ liệu EL khi render hoặc route đã được deploy.

WAR `target/OnlineFruitShopping-1.0-SNAPSHOT.war` đã kiểm tra:
- Có 19 view dưới `jsp/shopowner/views/`.
- Không còn `WEB-INF/shopowner/views/` hay class trong package `shopowner.*` cũ.
- Không có JUnit, H2, Jasper/Ant hoặc dependency test bổ sung lọt vào `WEB-INF/lib`.
- `git diff --check` và `git diff --cached --check` đạt; không còn tham chiếu
  `com.mycompany.onlinefruitshopping.shopowner.` trong source Java/test.

Bước tài liệu không sửa Java/JSP/test/pom sau kết quả verify này nên không chạy
lại toàn bộ test chỉ vì Markdown. Artifact build không đưa vào commit refactor.
Repo đã theo dõi một số file trong target từ trước; thay đổi binary sinh ra khi
build được giữ ngoài commit. `.gitignore` đã có target/class/WAR/log.

## 9. Kiểm thử runtime còn lại

| Tình huống trên server/database thật | Trạng thái |
|---|---|
| Đăng nhập theo từng role, tài khoản nhiều role | Chưa thực hiện |
| View Cart và Track Order | Chưa thực hiện |
| Tên/menu, dữ liệu EL, chuỗi tiếng Việt | Chưa thực hiện |
| Truy cập chéo tài khoản/shop | Chưa thực hiện |
| Thu hồi role/khóa tài khoản | Chưa thực hiện |
| Shipper ngừng hoạt động | Chưa thực hiện |
| Form thiếu/sai/đúng CSRF | Chưa thực hiện |
| JSP trực tiếp bị chặn, forward hợp lệ | Chưa thực hiện |
| Phân công/cập nhật giao hàng, khóa/transaction SQL Server | Chưa thực hiện |
| POST đăng xuất, legacy redirect sau deploy | Chưa thực hiện |

Cần giải quyết các chênh lệch source nêu ở mục 2 và 4 trước khi coi bản refactor
là sẵn sàng triển khai. Build xanh không thay thế kiểm thử đầy đủ ứng dụng.
