# Kiểm thử runtime ShopOwner

## Cập nhật 26/10/2026 — sửa 5 lỗi runtime và chạy lại toàn bộ

Mục này thay thế kết luận ngày 25/10 và bản cập nhật môi trường trước đó; phần cũ
ở dưới giữ làm lịch sử. Nền hiện tại là `fb90284` trên nhánh
`refactor/shopowner-common`, cộng commit sửa 5 lỗi runtime (xem `git log`).

### Môi trường thực tế và artifact

- DB `OnlineFruitShopping_Test` trên `TAI`/localhost:1433; schema-only từ metadata
  DB nguồn rồi áp dụng các migration test được mô tả trong RUNTIME_SETUP.md.
- Probe qua **DBConnection production** trả `server=TAI; database=OnlineFruitShopping_Test`.
  App dùng SQL auth `fruitshop_runtime_test`, không dùng Windows auth.
- 8 tài khoản test và 4 role; Customer/Owner/Shipper A/B cùng admin và owner.new.
  2 shop ban đầu, shop thứ ba được tạo qua POST. Password local ignored, không ghi báo cáo.
- Tomcat 10.1.17/JDK17.0.12, CATALINA_BASE `<repo>/.runtime/tomcat-test`;
  listener **127.0.0.1:8081**, shutdown disabled. PID lần start mới nhất 16100.
- BASE: `http://127.0.0.1:8081/OnlineFruitShopping` (browser cũng dùng hostname localhost).
- Descriptor test deploy đúng `<repo>/target/OnlineFruitShopping-1.0-SNAPSHOT.war`.
  SHA256 cuối: **7462C1256EA3991BED04F21E15F0CCD4BB2DF43E1949DFB9493DF0D698FB4661**
  (392 entry).
- `mvn -B clean verify` cuối: BUILD SUCCESS, XML Surefire **98/0/0/0** (22 test class);
  Jasper **27 JSP**. ZIP WAR không chứa db.properties/db-test.properties/test-accounts.properties.
- Không thêm migration/cột mới cho đợt sửa này: DAO được đổi sang đúng schema thật
  (`orders.order_status`, `orders.order_date`, `order_items.price`,
  `inventory_adjustments.quantity_change/adjusted_by/created_at`).

### Bằng chứng và kết quả thực chạy

HTTP harness `scripts/test-runtime-http.ps1` chạy 69 kiểm tra, **69 PASS / 0 FAIL**
(56 ca cũ giữ nguyên ID + 13 ca bổ sung). Các ca mới không chỉ kiểm tra status mà
đọc SQL/nội dung: doanh thu 200 + fee 20 + earning 180, CSV đúng dòng và BOM
`EF BB BF`/`charset=UTF-8`, filter Completed/Cancelled/ngày, escape dấu phẩy/ngoặc
kép và giữ ký tự UTF-8 (`đ`); batch 100→90 với đúng một log `-10`, restock 7 với đúng một log `+7`, actor
lấy từ session, history hiển thị số lượng/actor/reason, cross-shop bị từ chối và
log insert lỗi thì rollback. Số này là kiểm tra HTTP/SQL, không phải số test JUnit.
Bằng chứng local ignored: `.runtime/http-results.json`,
`tomcat-test/logs/localhost_access_log.*`, `tomcat-test/logs/localhost.*.log`,
`tomcat-test/logs/catalina.*.log` (đã khử credential/password).

| Tình huống | Kết quả mong đợi | Kết quả thực tế | PASS/FAIL/CHƯA CHẠY | Bằng chứng |
|---|---|---|---|---|
| Tiến trình/port/context/login | Instance test hoạt động | Tomcat deploy log, listener loopback, GET login 200 | PASS | Log startup + HTTP harness |
| DB ứng dụng | DB test riêng | DBConnection probe trả OnlineFruitShopping_Test | PASS | RuntimeTools probe |
| Login Admin, Customer A/B, Owner A/New, Shipper A/B | Redirect role dashboard | 302 đúng dashboard; GET dashboard các role 200 | PASS | HTTP JSON |
| Logout rồi đổi role cùng client | Xóa session cũ, dùng owner mới, giữ saved redirect | Protected route sau logout về login; login Owner A giữ delivery/list đã lưu, owner page có Owner A | PASS | HTTP JSON |
| Legacy GET/POST | 302/303 về login | Cả hai URL đúng status và Location quan sát được | PASS | HTTP JSON |
| View Cart click | Mở cart | Bấm link thật, trang giỏ hàng render | PASS | Browser snapshot cart |
| Icon Cart click | GET cart 200 | Bấm icon thật; Network document /cart 200 | PASS | Browser network request, access log |
| Track Order/detail click | List chỉ A, detail/items A | Bấm thật, header Customer A; delivery #1/shop A/item FIXTURE-3, qty 2 | PASS | Browser snapshot list/detail |
| Tên Customer Dashboard | User.fullName | Ban đầu chữ Customer cố định; đã sửa c:out, build/deploy lại thấy “Welcome back, Customer A!” | PASS sau sửa | Browser snapshot + HTTP regression name check |
| Customer dashboard Console | Không có JS error | Không có error ở document mới nhất | PASS | browser.console |
| Owner dashboard/commission/list kho/giá/promo | HTTP 200 | HTTP 200; báo cáo/kho đã đối chiếu số liệu ở ca 48-60 | PASS (HTTP + nội dung 5 vùng) | HTTP JSON + SQL; widget khác chưa review |
| Tạo shop | Lưu đúng owner, không trùng | Lần đầu shop=created, DB có 1 shop cho owner.new; chạy lại shop=exists | PASS | HTTP + SQL count |
| Giá/promo biến thể | Persist đúng và promo pending/xóa | Giá 95; promo pending, xóa còn 0 row | PASS | HTTP + SQL |
| Ngưỡng/cảnh báo | Lưu ngưỡng, xử lý request alert | Threshold=150; redirect msg=notified | PASS (request) | HTTP + SQL; chưa xác nhận email đến inbox |
| Báo cáo và CSV | HTTP 200, tổng khớp fixture | 200; HTML section=products có A, revenue 200 đ, fee 20 đ, không có B; CSV đúng dòng `Fixture Shop A Apple,2.00,200.00,10.00,20.00,180.00` | PASS sau sửa | HTTP + SQL fixture; JSON ca 22/23 + ca mới 54-60 |
| Lịch sử kho | HTTP 200, hiển thị đúng | 200; có batch/quantity 7 và 10, actor `Owner A`, reason `Runtime test` | PASS sau sửa | HTTP + SQL; JSON ca 29 + 51 |
| Cập nhật batch/nhập hàng | POST thành công, batch + đúng 1 log | 200→90 với log `-10`; restock tạo batch 7 + log `+7`; actor đúng; cross-shop không đổi; log lỗi rollback (500, batch vẫn 90, 1 log) | PASS sau sửa | HTTP + SQL; JSON ca 46-53 |
| Customer A detail B | 404, không lộ items | 404 | PASS | HTTP JSON |
| Owner A sửa giá B | Từ chối | Redirect msg=forbidden | PASS (phản hồi) | HTTP JSON |
| Shipper A sửa delivery B | Không ghi | POST redirect, SQL delivery B không đổi | PASS | HTTP + SQL |
| Shipper B inactive | Không đọc/cập nhật | Detail 404, POST không đổi trạng thái | PASS | HTTP + SQL |
| Block user/thu hồi role | Request tiếp theo về login | Đúng; fixture được khôi phục sau ca | PASS | HTTP + SQL |
| CSRF thiếu/sai | 403 | Cả hai 403 | PASS | HTTP JSON |
| CSRF đúng/quyền đúng | Ghi trạng thái hợp lệ | accept chuyển Assigned→Accepted và thêm đúng 1 notification | PASS | Ca concurrent accept + SQL |
| JSP trực tiếp/forward | 404 trực tiếp, detail servlet render | 404/200 và browser thấy items | PASS | HTTP + browser |
| Rollback SQL Server | Delivery/order/notification không thay đổi dở dang | Trigger THROW 51009 ở notification; trạng thái và count khớp baseline sau request | PASS | HTTP+SQL, log Runtime rollback probe; trigger đã DROP |
| Hai accept đồng thời/retry | Một transition, một notification | Hai POST qua hai session, Accepted và count tăng 1; retry không tăng thêm | PASS | HTTP+SQL |
| Đồng thời assign, action xung đột, COD hoàn tất | Đúng khóa/thanh toán từng tình huống | Chưa chạy | CHƯA CHẠY | Chỉ concurrent accept đã chạy |
| Toàn bộ menu/EL của Owner/Admin/Shipper bằng browser | Hiển thị đúng fixture | Chỉ xác nhận GET dashboard 200; chưa review mọi widget | CHƯA CHẠY | Không suy từ HTTP 200 |
| CSV content, email inbox, subscription | Nội dung/luồng đúng | CSV content/BOM/escape đã kiểm; email inbox chưa xác minh; subscription thiếu schema | CHƯA CHẠY một phần | JSON ca 56/57/60; email/subscription chưa có bằng chứng |

### Năm lỗi FAIL cũ: nguyên nhân → nơi sửa → kết quả

| # | Ca FAIL cũ | Nguyên nhân gốc | Nơi sửa | Kết quả chạy lại |
|---|---|---|---|---|
| 1 | `owner GET /store-owner/commission-report` (ca 22) | SQL dùng `o.status`, `o.created_at`, `oi.unit_price`; schema thật là `order_status`, `order_date`, `price` → `Invalid column name 'status'` | `dao/report/CommissionReportDAO.java`: các SELECT/scope, `STATUSES`, `DELIVERED_STATUS=Completed`, `REFUND_STATUSES`; `service/report/CommissionReportService.java` nhãn CSV; JSP shopowner/admin dùng status thật | 200; ca 54 PASS: A Completed 2×100 → revenue 200 đ, fee 20 đ; không lộ B |
| 2 | `owner GET ...commission-report?format=csv` (ca 23) | Cùng SQL sai cột; nhãn filter còn ghi “Delivered” trong khi basis là `Completed` | `CommissionReportDAO` + `CommissionReportService` (nhãn dùng `DELIVERED_STATUS`); CSV dùng chung `loadOwnerReport`/filter với HTML | 200; ca 56/57/60 PASS: đúng dòng 2.00/200.00/10.00/20.00/180.00, BOM `EF BB BF`, charset UTF-8, escape `"`/`,` và giữ `đ` |
| 3 | `owner GET /store-owner/inventory/history` (ca 29) | SELECT đọc `adjustment_type`, `quantity`, `created_by`; schema thật chỉ có `quantity_change`, `adjusted_by`, `created_at`, `reason` | `dao/inventory/InventoryDAO.java`: history SELECT dùng `quantity_change`/`adjusted_by` + `LEFT JOIN users`, suy ra `adjustment_type`/`quantity` cho JSP; `jsp/shopowner/inventory-history.jsp` thêm cột Actor | 200; ca 51 PASS: thấy batch RUNTIME-RESTOCK, quantity 7/10, actor `Owner A`, reason `Runtime test` |
| 4 | `POST /store-owner/inventory/update` (ca 46) | INSERT log dùng cột không tồn tại và ghi số tuyệt đối thay vì delta; không khóa batch/transaction | `InventoryDAO.setBatchQuantity`: `UPDLOCK, ROWLOCK`, UPDATE batch, INSERT `(batch_id, quantity_change, reason, adjusted_by, created_at)` với delta, cùng connection; rollback khi log lỗi | 302; ca 48/49 PASS: 100→90, đúng 1 log `-10`, actor = user session, reason đúng; ca 52 cross-shop bị chặn, không đổi dữ liệu; ca 53 trigger lỗi → 500, batch vẫn 90, vẫn 1 log |
| 5 | `POST /store-owner/inventory/restock` (ca 47) | Cùng lỗi cột audit; batch mới và log phải atomic | `InventoryDAO.restock`: batch mới quantity 7, log `+7`, `adjusted_by` từ session, cùng transaction | 302; ca 50 PASS: batch 7, đúng 1 log `+7`, actor đúng |

Ngoài 5 ca: `deductStockForOrder` (chưa được gọi) đã bỏ `order_items.batch_id`
(không có trong schema) và lấy FIFO batch của variant, log `quantity_change` âm;
`takeFromBatch`/`lockedBatchSql` không dùng đã xóa. Không có migration mới.

### Còn FAIL / BLOCKED / CHƯA CHẠY

- **FAIL:** không còn trong 69 ca harness.
- **BLOCKED:** subscription/home vẫn log `Invalid object name 'subscription_plans'`;
  schema test không có bảng này, ngoài phạm vi 5 lỗi.
- **CHƯA CHẠY:** đồng thời assign/action xung đột/COD hoàn tất; review browser toàn
  bộ widget Owner/Admin/Shipper; email đến inbox; trang báo cáo phía Admin (dùng
  chung DAO nhưng servlet Admin chưa được harness gọi); `deductStockForOrder` chưa
  có nơi gọi.
- **Rủi ro schema còn mở:** `OrderDAO`, `AdminOrderDAO`, `database/sample_orders.sql`
  vẫn dùng `status`, `created_at`, `unit_price`, `batch_id`; không được harness chạy
  nên không kết luận đạt. Cần xử lý riêng trước khi nghiệm thu toàn bộ.
- Các ca cũ chỉ kiểm tra status/redirect vẫn giữ nguyên tiêu chí; 13 ca mới bổ sung
  kiểm tra nội dung/SQL cho đúng 5 vùng lỗi. Không suy PASS toàn bộ chức năng từ 69 ca.

## Lịch sử 25/10 (giữ nguyên)

## Phạm vi và kết luận

- Nhánh `refactor/shopowner-common`, HEAD `3923639e5d9a82327706697b1d24952cd1e6f65b`.
- Working tree sạch trước đợt kiểm tra. Đợt này chỉ bổ sung tài liệu.
- **Chưa chạy được kiểm thử nghiệp vụ runtime:** server chưa phục vụ URL cấu hình;
  deployment descriptor trỏ bản khác; WAR ghi cứng database chưa được xác nhận là DB test riêng.
- Không có kết quả PASS cho đăng nhập, nút Customer, handler, phân quyền hoặc transaction.
  88 unit/integration tests và 27 JSP compile của lần trước không thay thế các ca này.
- Không deploy WAR lên Tomcat dùng chung, không sửa DB/tài khoản hiện có, không chạy script seed.
  Chỉ đọc metadata database bằng Windows authentication; không thử đoán mật khẩu ứng dụng.

## Môi trường và bằng chứng

| Mã | Kiểm tra thực tế | Kết quả |
|---|---|---|
| E1 | `git branch --show-current`, `git rev-parse HEAD`, `git status --short` | Đúng nhánh/HEAD trên, ban đầu sạch |
| E2 | `target/OnlineFruitShopping-1.0-SNAPSHOT.war` | Có file 13.694.999 byte, LastWriteTime 25/10/2026 19:16:24; SHA256 `55E13F7AC47A2B10E1F7B4EBBA0D0A99A2227ECA1783C37345E63F5B532196C3` |
| E3 | `nb-configuration.xml`, `nbactions.xml` | NetBeans chọn Tomcat, deploy sau package, client URL `/home` |
| E4 | `C:\Program Files\Apache Software Foundation\Tomcat 10.1\conf\server.xml` | Connector HTTP port 8080, host localhost; 8443 chỉ là redirectPort, không có connector HTTPS đang bật |
| E5 | `src/main/webapp/META-INF/context.xml` và `conf/Catalina/localhost/OnlineFruitShopping.xml` của Tomcat | Context `/OnlineFruitShopping`; descriptor thực tế có docBase `C:\Users\Admin\Downloads\OnlineFruitShopping_refactored\OnlineFruitShopping\target\OnlineFruitShopping-1.0-SNAPSHOT`, thư mục tồn tại nhưng không phải WAR mục E2 |
| E6 | Process/listener và HTTP từ máy local | Không thấy process Java/Tomcat, không có listener 8080. `Invoke-WebRequest http://localhost:8080/OnlineFruitShopping/login -TimeoutSec 5` trả `Unable to connect to the remote server` |
| E7 | Công cụ browser mở cùng URL | Trả `502 Bad Gateway` qua đường truy cập browser; chưa nhận trang login. Đây không phải bằng chứng ứng dụng tự trả HTTP 502 |
| E8 | Công cụ local | Có Java/JDK 17, Maven, sqlcmd, Tomcat 10.1; không tìm thấy docker trên PATH. Browser tool hoạt động nhưng chưa tới được ứng dụng |
| E9 | `DBConnection.java` và `javap -c -p` trên class build | URL ghi cứng `jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping;encrypt=true;trustServerCertificate=true`; user SQL `sa`; không đọc db.properties, không có override DB test |
| E10 | `src/main/resources/db.properties` (file local ignored, không công bố mật khẩu) | URL chỉ tới `FruitShoppingShopOwnerDB`, khác E9; sửa riêng file này không chuyển database mà DBConnection dùng |
| E11 | SQL Server PID 5516/listener 1433; sqlcmd Windows authentication | Truy vấn chỉ đọc `SELECT name,state_desc FROM sys.databases WHERE database_id > 4` thành công; có `FruitShoppingDB`, `FruitShoppingShopOwnerDB`, `OnlineFruitShopping` ở trạng thái ONLINE. Tên DB không chứng minh đó là môi trường kiểm thử riêng |

URL lấy từ cấu hình: **`BASE = http://localhost:8080/OnlineFruitShopping`**.
Đây là URL chuẩn bị kiểm tra, chưa phải deployment đang chạy. Không suy context
path từ tên WAR vì descriptor hiện có chỉ định `/OnlineFruitShopping`.

### Các thông tin cần xác nhận để tiếp tục

1. DB/instance kiểm thử riêng, schema/fixture chuẩn và thông tin kết nối dành cho test.
   Cần cơ chế để WAR thực sự dùng DB đó: DBConnection hiện ghi cứng tên DB/host.
   Nếu dùng cùng instance với DB tên khác thì phải sửa cấu hình kết nối, build lại
   và ghi hash WAR mới; không thể tuyên bố đã dùng nguyên WAR E2 với DB khác.
2. Tài khoản ứng dụng đã xác nhận cho Customer A/B, Owner A/B, shipper A/B,
   owner chưa có shop, ADMIN nếu kiểm tra role này; fixture ID order/delivery/shop/variant.
   Không đồng nhất tài khoản SQL `sa` với tài khoản đăng nhập web.
3. CATALINA_BASE dành riêng cho test và descriptor trỏ đúng artifact; cấu hình
   hiện có trỏ bản Downloads. Xác nhận port/context mới nếu môi trường test khác E4/E5.
4. DB test cho phép thay role/status, tạo dữ liệu, gây lỗi transaction và chạy
   đồng thời; có baseline để khôi phục giữa các ca. Không áp dụng trên DB chưa xác nhận.

## Bảng kết quả

`CHƯA CHẠY` nghĩa là chưa thực thi hành vi nghiệp vụ, không phải PASS hoặc FAIL.
FAIL ở bước tiền kiểm chỉ mô tả trạng thái môi trường, chưa kết luận lỗi refactor.

| Tình huống | Kết quả mong đợi | Kết quả thực tế | PASS/FAIL/CHƯA CHẠY | Bằng chứng |
|---|---|---|---|---|
| Nhánh và artifact | Đúng commit, có WAR | Đúng HEAD, có WAR/hash | PASS | E1–E2, chỉ tiền kiểm |
| URL server sẵn sàng | Nhận trang login từ WAR mục E2 | Không kết nối; descriptor trỏ bản khác | FAIL | E5–E7, môi trường |
| Kết nối SQL metadata | SQL Server truy cập được | Truy vấn chỉ đọc thành công | PASS | E11, không xác nhận app/DB test |
| Cách ly database test | WAR dùng DB test riêng đã xác nhận | Ghi cứng OnlineFruitShopping; chưa có xác nhận DB/fixture | CHƯA CHẠY | E9–E11 |
| Login CUSTOMER/SHOPOWNER/DELIVERYSTAFF/ADMIN có tài khoản test | Dashboard đúng role, đúng tên/menu | Chưa đăng nhập | CHƯA CHẠY | Thiếu môi trường và tài khoản xác nhận |
| Logout rồi login role khác | Xóa principal cũ, token mới, menu đúng role mới | Chưa thao tác | CHƯA CHẠY | E6 |
| GET/POST hai URL legacy | GET 302, POST 303; Location tới BASE/login | Chưa gửi request tới app chạy | CHƯA CHẠY | E6 |
| Bấm View Cart từ Customer Dashboard | Chuyển tới BASE/cart, hiển thị giỏ đúng | Chưa mở được dashboard để bấm | CHƯA CHẠY | E6–E7 |
| Bấm icon giỏ hàng | Cùng đích cart, không bị overlay/JS chặn | Chưa bấm | CHƯA CHẠY | E6–E7 |
| Bấm Track Order | Mở delivery list theo customer | Chưa bấm | CHƯA CHẠY | E6–E7 |
| Bấm detail giao hàng của chính mình | Detail/items đúng fixture, forward render được | Chưa có session/fixture | CHƯA CHẠY | E9–E11 |
| Owner dashboard/thống kê | Tên/shop/số liệu khớp baseline | Chưa render | CHƯA CHẠY | E6, thiếu fixture |
| Sản phẩm/hoa hồng | Đúng shop, filter và phân trang đúng | Chưa đọc | CHƯA CHẠY | E6 |
| Báo cáo/CSV | Tổng khớp DB, CSV tải được và giữ filter | Chưa tải CSV | CHƯA CHẠY | E6 |
| Tạo shop | Validation đúng, gắn owner hiện tại, không tạo trùng | Chưa POST | CHƯA CHẠY | Thiếu owner chưa có shop/DB test |
| Kho/nhập hàng | Batch, tồn kho, lịch sử thay đổi đúng | Chưa POST | CHƯA CHẠY | Thiếu fixture/DB test |
| Ngưỡng/cảnh báo kho | Lưu ngưỡng, danh sách thấp và thông báo đúng | Chưa POST | CHƯA CHẠY | Thiếu fixture/DB test |
| Giá biến thể | Chỉ variant của owner được cập nhật | Chưa POST | CHƯA CHẠY | Thiếu fixture/DB test |
| Khuyến mãi biến thể | Yêu cầu dated promo, trạng thái chờ duyệt/xóa đúng | Chưa POST | CHƯA CHẠY | Thiếu fixture/DB test |
| Customer A đọc delivery B | Detail 404, không lộ items của B | Chưa gửi request có session A | CHƯA CHẠY | Thiếu A/B và delivery IDs |
| Owner A sửa shop B | Bị từ chối, DB B không đổi | Chưa POST | CHƯA CHẠY | Thiếu A/B và variant/order IDs |
| Shipper sửa delivery của người khác | Không đổi trạng thái/notification | Chưa POST | CHƯA CHẠY | Thiếu fixture |
| Shipper ngừng hoạt động | Không đọc/cập nhật delivery theo policy | Chưa đổi trạng thái quan hệ shop/shipper | CHƯA CHẠY | Thiếu DB test |
| Khóa user/thu hồi role | Request bảo vệ tiếp theo vô hiệu session, không thực hiện nghiệp vụ | Chưa sửa fixture/account | CHƯA CHẠY | Thiếu DB test |
| CSRF thiếu/sai | 403 cho POST thuộc scope, không ghi DB | Chưa gửi request có session hợp lệ | CHƯA CHẠY | E6 |
| CSRF đúng/quyền đúng | Nghiệp vụ hợp lệ hoàn tất, dữ liệu đúng | Chưa POST | CHƯA CHẠY | Thiếu fixture/DB test |
| JSP nội bộ trực tiếp | 404 tại /jsp/shopowner/views/* | Chưa gọi app chạy | CHƯA CHẠY | E6 |
| Servlet forward hợp lệ | Render JSP/EL đúng sau kiểm tra quyền | Chưa render | CHƯA CHẠY | E6 |
| Rollback SQL Server | Không còn thay đổi dở dang/notification khi bước sau lỗi | Chưa gây lỗi transaction | CHƯA CHẠY | Thiếu DB test riêng |
| Đồng thời cùng order/delivery | Trạng thái hợp lệ, không thông báo/ghi nhận trùng trái quy tắc | Chưa chạy concurrent requests | CHƯA CHẠY | Thiếu DB test riêng |

## Checklist thao tác thủ công sau khi môi trường sẵn sàng

### Login và Customer — bắt buộc thao tác bấm

1. Xác nhận log startup ghi đúng context/artifact, kiểm tra DB_NAME() của kết nối
   ứng dụng là DB test. Ghi BASE thực tế nếu khác URL cấu hình trên.
2. Mở `BASE/login`, đăng nhập bằng tài khoản test được cung cấp, lưu ảnh tên/menu,
   URL cuối và request login (ẩn password/cookie/token trong bằng chứng chia sẻ).
3. CUSTOMER phải tới `BASE/customer/dashboard`. Mở DevTools Console và Network,
   bật Preserve log. **Bấm View Cart**, ghi request/response và URL cuối `/cart`.
   Quay lại dashboard, **bấm icon giỏ hàng** và ghi riêng kết quả.
4. Quay lại dashboard, **bấm Track Order**, xác nhận `/shopowner/delivery/list`;
   **bấm detail** của delivery trong fixture A. URL detail có `?id=<deliveryA>`.
   Items được lấy trong handler detail, không có endpoint items riêng trong DeliveryController.
5. Nếu click không phát request: xem href thực tế, overlay/pointer-events,
   listener preventDefault và lỗi Console. Nếu có request: lưu status, redirect chain,
   response body, timestamp và log Tomcat cùng thời điểm. Không thay click bằng gọi URL trực tiếp.
6. Logout bằng form có token rồi login role khác; SHOPOWNER tới
   `/store-owner/dashboard`, DELIVERYSTAFF tới `/delivery/dashboard`, ADMIN tới
   dashboard theo LoginService. Đối chiếu không còn tên/menu/principal role trước.
7. Kiểm tra riêng GET và POST `BASE/shopowner`, `BASE/shopowner/login`;
   tắt auto-follow redirect khi ghi status/Location. GET 302, POST 303 về `BASE/login`.

### ShopOwner — URL và ghi dữ liệu

| Trang | Đường dẫn nối BASE | Thao tác |
|---|---|---|
| Dashboard | `/store-owner/dashboard` | Mở qua login/menu, so thống kê với baseline |
| Hoa hồng | `/store-owner/commission` | Lọc q/status/page; kiểm tra sản phẩm của đúng owner |
| Báo cáo | `/store-owner/commission-report` | Chọn from/to/status/section, bấm tải CSV (`format=csv`), mở file và so tổng |
| Tạo shop | `/store-owner/create-shop` | Dùng owner chưa có shop; gửi shopName/description; kiểm tra invalid input và lần tạo thứ hai |
| Kho | `/store-owner/inventory` | Bấm các tab thay vì chỉ gọi URL |
| Cập nhật batch | `/store-owner/inventory/update` | POST batchId/quantity/reason và action từ form, kiểm tra stock/history |
| Nhập hàng | `/store-owner/inventory/restock` | POST action=restock, variantId/quantity/batchCode/expiryDate/reason |
| Ngưỡng/cảnh báo | `/store-owner/inventory/low-stock` | POST action=setThreshold với threshold; action=notifyLowStock; kiểm tra notification |
| Lịch sử | `/store-owner/inventory/history` | Lọc và so các thay đổi vừa tạo |
| Giá | `/store-owner/pricing` | Sửa variantId/price; xác nhận filter/page còn giữ sau redirect |
| Promo biến thể | `/store-owner/promotions` | Lưu variantId/promoPrice/startDate/endDate, kiểm tra pending; action=removePromo để xóa |

### Quyền và CSRF

- Với session A, thay ID bằng fixture B trong request thật; chụp kết quả và so DB
  trước/sau. Không coi một redirect là cập nhật thành công: handler delivery có thể
  redirect kèm flash lỗi; kiểm tra cả dữ liệu và notification.
- Owner phân công qua POST `/shopowner/shop/deliveries` (`orderId`, `shipperId`,
  `csrfToken`). Shipper cập nhật qua POST `/shopowner/delivery/update`
  (`id`, `action`, `reason`, `collected` theo form, `csrfToken`).
- Dùng hai phiên browser độc lập cho A/B. Với shipper inactive, cập nhật quan hệ
  `shop_delivery_staff` trong fixture, giữ session cũ rồi gửi lại request.
- Khóa user hoặc bỏ role đang đăng nhập trong DB test; request bảo vệ tiếp theo
  phải bị chặn. Khôi phục fixture sau từng ca trước khi kiểm tra tiếp.
- CSRF: dùng session/role hợp lệ, cùng payload hợp lệ; lần lượt bỏ token, token sai,
  token đúng. Scope hiện tại là non-GET/HEAD `/logout` và `/shopowner/*` ngoại trừ
  exact `/shopowner/login`; **không** gồm `/store-owner/*`.
- GET `BASE/jsp/shopowner/views/detail.jsp` phải 404; mở detail qua servlet
  với ID thuộc quyền phải render được. Không đánh đồng hai loại request.

### Rollback và đồng thời trên SQL Server test

1. Chụp baseline delivery/order/payment/inventory và các notification liên quan.
   Dùng các ID fixture riêng cho từng ca; ghi request payload đã ẩn token.
2. Rollback: trên DB test, đặt lỗi có kiểm soát ở bước ghi sau khi đã cập nhật
   trạng thái (ví dụ trigger test THROW ở insert notification đúng fixture).
   Gửi request update/assign; lấy exception/log và đọc lại bằng connection khác.
   PASS chỉ khi mọi thay đổi trong transaction đã rollback. Bỏ trigger test rồi
   gửi lại payload hợp lệ để chứng minh đường thành công vẫn chạy.
3. Đồng thời: dùng hai HTTP client/session có quyền, barrier cùng lúc gửi assign
   cùng order hoặc cùng action update cùng delivery. Lưu cả response/timestamp,
   SQL error/deadlock nếu có. So trạng thái cuối và số notification trước/sau.
4. Kiểm tra hai action xung đột và retry request đã hoàn tất; đối chiếu transition
   cho phép trong DeliveryRules. Không lấy hai HTTP 302 làm bằng chứng transaction đúng.
5. Khôi phục baseline, ghi SQL/log và kết luận từng ca riêng. Chưa có bằng chứng
   thực thi nào cho các bước này trong đợt hiện tại.

Log cần thu sau deploy: `CATALINA_BASE/logs/catalina.*`, `localhost.*`, access log,
Console browser, Network/HAR và SQL trước/sau (ẩn secrets/dữ liệu không thuộc fixture).

## Thay đổi và kiểm tra cuối đợt

Chỉ thêm báo cáo này và liên kết trong HUONG_DAN_CAP_NHAT.md. Không sửa production,
không có lỗi nghiệp vụ runtime đã tái hiện để viết test hồi quy. Không chạy lại
Maven chỉ vì Markdown, không commit/push trong đợt này.
