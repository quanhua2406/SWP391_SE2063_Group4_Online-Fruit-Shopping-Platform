# SWP391_SE2063_Group4_Online-Fruit-Shopping-Platform
The project for semester 5-SWP391

## Refactor ShopOwner

Module giao hàng, coupon và khuyến mãi sử dụng package chung, session `User`
và chính sách truy cập URL tập trung. View module nằm tại `jsp/shopowner/views/`.

Chi tiết thay đổi, chính sách session/phân quyền, giới hạn còn lại và kết quả
kiểm thử: [Hướng dẫn cập nhật](HUONG_DAN_CAP_NHAT.md).

Chạy kiểm tra tự động (Java 17, Maven):

```bash
mvn -B clean verify
```

Database được cấu hình bằng file UTF-8 bên ngoài WAR qua `fruitshop.db.config`
hoặc `FRUITSHOP_DB_CONFIG`; không còn cấu hình kết nối mặc định.
Xem [thiết lập runtime test riêng](RUNTIME_SETUP.md) và [kết quả runtime](KIEM_THU_RUNTIME.md).
