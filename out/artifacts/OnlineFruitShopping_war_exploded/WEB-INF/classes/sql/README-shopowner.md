# Database module ShopOwner (giao hàng / khuyến mãi / coupon)

Từ khi tích hợp, module **dùng chung 1 database `OnlineFruitShopping` và 1 class
`com.mycompany.onlinefruitshopping.utils.DBConnection`** với phần còn lại của
SWP391. Không còn database `FruitShoppingShopOwnerDB` riêng.

## Tạo database

Chạy script của nhóm `OnlineFruitShopping.sql` (tạo mới toàn bộ database OnlineFruitShopping,
gồm cả bảng và dữ liệu mẫu của module). File này đã chứa đủ:

- Bảng module: `coupons`, `coupon_usages`, `promotions`, `promotion_products`,
  `deliveries`, `notifications`, `shop_delivery_staff`.
- Tài khoản demo dùng chung: `admin` / `shop` / `shipper` / `customer`
  (mật khẩu `123456`, BCrypt cost 12; đăng nhập bằng **username hoặc email**).
- Vai trò: module dùng `ShopOwner`, `DeliveryStaff` (tên cũ trong module là
  `Shipper`), `Customer`. Tài khoản `shop` = chủ shop 1, `shipper` = đội shop 1.
- Dữ liệu thử: đơn #25/#26 chờ áp coupon, đơn #27 đã áp XANH10, đơn #17/#24
  chờ phân công shipper, đơn #15 giao thất bại chờ giao lại.

## Đối chiếu cột module -> database chung (đã sửa trong code)

| Cũ (DB riêng của module) | Mới (OnlineFruitShopping) |
| --- | --- |
| `Accounts` + `Roles` (role_id trực tiếp) | `users` + `user_roles` + `roles` |
| `Accounts.password` (ro) | `users.password_hash` (BCrypt) |
| `Orders.account_id` | `orders.user_id` |
| `Shops.owner_account_id` | `shops.owner_id` |
| `Notifications.account_id` | `notifications.user_id` |
| `OrderDetails` | `order_items` |
| role `Shipper` | role `DeliveryStaff` |
| `Products.status = 'Active'` | `products.status = 'published'` |
| `ProductVariants.variant_name` | `product_variants.sku` (đổi tên hiển thị) |

Lưu ý ràng buộc của database chung: đơn chỉ được **hoặc voucher, hoặc coupon**
(không cộng dồn) và `orders.total_amount` phải bằng `coupon_subtotal - coupon_discount`
khi có coupon — `CouponApplicationDAO` đã kiểm tra và cập nhật đúng các điều này.
