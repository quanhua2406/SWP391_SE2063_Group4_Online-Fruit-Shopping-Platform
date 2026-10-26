# Cấu hình DB ngoài WAR và runtime test riêng

## Kết nối bắt buộc

`DBConnection.getConnection()` giữ nguyên API, dùng SQL authentication.
Thứ tự chọn file UTF-8:
1. JVM property `-Dfruitshop.db.config=<absolute-path>` nếu không rỗng.
2. Environment `FRUITSHOP_DB_CONFIG` nếu JVM property không được khai báo/rỗng.

File cần `db.url`, `db.user`, `db.password`. URL/user được trim, password giữ
nguyên. Thiếu file/khóa hoặc lỗi kết nối trả SQLException; không fallback về DB,
tài khoản hay mật khẩu cũ. Các DAO và ConnectionProvider vẫn dùng API hiện có.
ReviewDAO bỏ kế thừa DBConnection, tiếp tục gọi static getConnection như trước.

Copy `config/db.properties.example` ra file local và điền thông tin SQL auth.
Không đặt password trong JDBC URL. Không dùng file classpath `src/main/resources/db.properties`
để cấu hình nữa. Maven loại db.properties/db-test.properties khỏi resources/WAR;
đã kiểm tra ZIP WAR không có ba file credential db.properties, db-test.properties,
test-accounts.properties. Không đưa thông tin thật vào file mẫu.

## Môi trường test đã tạo trên máy này

- SQL Server: `localhost:1433`, `@@SERVERNAME = TAI`.
- DB riêng: `OnlineFruitShopping_Test` (đã kiểm tra chưa tồn tại trước khi tạo).
- Login SQL riêng: `fruitshop_runtime_test`, chỉ db_datareader/db_datawriter tại
  DB test; không thêm server role. Windows authentication chỉ dùng để provision
  bằng quyền đã có, không phải cơ chế kết nối của ứng dụng.
- Config thật: `config/db-test.properties` (ignored).
- 8 user fixture: admin; customer.a/b; owner.a/b; delivery.a/b; owner.new,
  tất cả email thuộc `fruitshop.test`. BCrypt sinh bằng thư viện LoginService dùng.
  Mật khẩu ngẫu nhiên ở `config/test-accounts.properties` (ignored), không in ra log.
- Owner A/B có shop, products/variants/batches/orders/items/deliveries riêng.
  Delivery A active, Delivery B inactive. Owner New ban đầu không shop; runtime
  POST đã tạo shop thứ ba. Tra role theo tên chuẩn hóa, không hardcode role ID.
- Tomcat **10.1.17**, JDK **17.0.12**; CATALINA_HOME
  `C:\Program Files\Apache Software Foundation\Tomcat 10.1`.
- CATALINA_BASE: `<repo>/.runtime/tomcat-test/`.
- HTTP bind `127.0.0.1:8081`; shutdown port `-1` (tắt, không tranh cổng).
- URL: **http://127.0.0.1:8081/OnlineFruitShopping/login**.
- Descriptor `conf/Catalina/localhost/OnlineFruitShopping.xml` trỏ tuyệt đối đến
  `<repo>/target/OnlineFruitShopping-1.0-SNAPSHOT.war`, không trỏ Downloads.
  Không copy thêm WAR vào webapps. Tomcat có thể tự unpack từ descriptor.

## Tạo lại trên môi trường mới

Chạy PowerShell ở root repo. Các script provision cố ý dừng nếu database/login
đã tồn tại hoặc users không rỗng; không drop/overwrite DB. Không chạy lại seed trên
DB hiện có. Với máy khác cần xác minh SQL instance, SMO directory, quyền và schema
trước, không dùng nguyên cấu hình máy này cho production.

```powershell
mvn -B clean verify
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/prepare-test-database.ps1
$cp = "target/classes;$env:USERPROFILE\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar;$env:USERPROFILE\.m2\repository\com\microsoft\sqlserver\mssql-jdbc\13.2.0.jre11\mssql-jdbc-13.2.0.jre11.jar"
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -cp $cp scripts/RuntimeTools.java credentials
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/configure-test-fixtures.ps1
& 'C:\Program Files\Java\jdk-17\bin\java.exe' "-Dfruitshop.db.config=$((Resolve-Path config/db-test.properties).Path)" -cp $cp scripts/RuntimeTools.java probe
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-runtime-test.ps1
```

`probe` dùng chính DBConnection production để đọc @@SERVERNAME/DB_NAME(), kiểm
tra tên DB test và không in credentials. Script start kiểm tra port còn trống,
tạo conf/logs/temp/webapps/work/bin, chỉ copy file conf nền từ CATALINA_HOME,
không copy descriptor cũ; đặt CATALINA_HOME/BASE, JAVA_HOME và FRUITSHOP_DB_CONFIG
trước `catalina.bat run`. Đây là foreground process, không cấu hình Windows service.
Dừng bằng Ctrl+C trong terminal start; chỉ dừng PID đã xác minh thuộc instance test
nếu chạy nền. Dừng instance trước `mvn clean verify`, sau đó start lại để deploy WAR mới.

Trong terminal khác:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/test-runtime-http.ps1
```

Script này **ghi fixture DB test**, đặt trigger lỗi tạm thời để kiểm tra rollback,
chạy hai POST đồng thời và lưu `.runtime/http-results.json`; không phải unit test.
Trigger được xóa trong finally; role/status account được khôi phục. Delivery #1
kết thúc ở Accepted, giá/ngưỡng và notification fixture thay đổi sau khi chạy.
Không dùng script trên dữ liệu không phải fixture. Tổng hợp PASS/FAIL trong JSON,
không suy PASS từ exit code PowerShell hoặc từ HTTP redirect đơn lẻ.

## Schema: nguồn và chênh lệch đã xác định

`scripts/prepare-test-database.ps1` dùng SMO đọc metadata của OnlineFruitShopping,
script 24 bảng cùng identity/default/PK/FK/check/unique/index (không dữ liệu),
lưu `database/runtime-test-schema.sql`, tạo DB mới rồi áp dụng từng batch có guard.
Database nguồn không có trigger; không sao chép user/password/order dữ liệu thật.

Các điều chỉnh chỉ áp dụng DB test, được ghi tách khỏi snapshot:
- Cart: snapshot dùng user_id/quantity INT; CartDAO yêu cầu cart_id/DECIMAL(10,2).
  Migration thay bảng cart_items **khi DB test chưa có users**, giữ FK/unique theo
  DDL hiện có trong CartDAO.ensureTablesExist.
- Áp dụng migration repo đã đọc: variant_promotions, approval, commission_rate,
  hidden_by_admin. low_stock_threshold đã có trong snapshot.
- UserDAO.findByEmail ORDER BY assigned_at/user_role_id nhưng nguồn thiếu hai cột:
  `runtime-test-login-schema.sql` thêm datetime2 default UTC và identity int chỉ
  cho test. Đây là bổ sung tương thích được khai báo rõ, không khẳng định nguồn có cột.
- Unique UQ_SO2_staff_shipper chỉ cho shipper thuộc một shop; seed thử cross-shop
  đã rollback. Fixture cuối giữ constraint, B inactive tại chính shop B.
- Account status hỗ trợ active/blocked/inactive/pending; dùng blocked để test khóa,
  không dùng giá trị locked ngoài constraint.

**Chưa giải quyết:** CommissionReportDAO dùng orders.status trong khi metadata
chỉ có order_status; InventoryDAO cần adjustment_type/quantity/created_by trong
khi metadata có quantity_change/adjusted_by. Không thêm cột giả hoặc sửa ngầm
quy tắc signed quantity để làm runtime xanh. Năm ca tương ứng đang FAIL.
Module subscription còn thiếu subscription_plans ngoài phạm vi fixture này.
Cần thống nhất migration/SQL nghiệp vụ các module đó trước khi nghiệm thu toàn bộ.

Kết quả và giới hạn chi tiết: [KIEM_THU_RUNTIME.md](KIEM_THU_RUNTIME.md).
