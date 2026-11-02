# Sơ đồ hoạt động: Đặt hàng và xử lý đơn

Sơ đồ này bám theo code hiện tại (`OrderDAO`, `StoreOwnerOrderServlet`, `DeliveryRules`, `AdminDeliveryService`).

```mermaid
flowchart TD
  subgraph CUS["Khách hàng"]
    A["Duyệt sản phẩm, thêm vào giỏ"] --> B["Checkout: địa chỉ, phương thức thanh toán, voucher"]
  end

  subgraph SYS["Hệ thống"]
    B --> C{"Phương thức thanh toán"}
    C -->|COD| D["Tạo đơn: status = pending_confirmation, payment_status = Unpaid"]
    C -->|Online| E{"Thanh toán thành công?"}
    E -->|Không| F["Không tạo đơn"]
    E -->|Có| G["Tạo đơn: status = pending_confirmation, payment_status = Paid"]
  end

  subgraph SHOP["Chủ shop"]
    D --> H{"Shop duyệt đơn?"}
    G --> H
    H -->|Từ chối, nhập lý do bắt buộc| I["status = cancelled"]
    H -->|Xác nhận| J["status = preparing: đang chuẩn bị"]
    J --> K["Đóng gói xong, chọn shipper: status = shipping"]
  end

  subgraph DEL["Shipper"]
    K --> L{"Shipper nhận đơn?"}
    L -->|Từ chối, nhập lý do| M["Shipper: Rejected"]
    M --> K
    L -->|Nhận| N["Accepted → PickedUp → Delivering"]
    N -->|Giao thành công| O["Delivered: status = delivered; COD thì payment_status = Paid"]
    N -->|Giao thất bại, nhập lý do| P["Failed"]
  end

  subgraph ADM["Quản trị viên"]
    P --> Q{"Xử lý sự cố"}
    Q -->|Mở lại: status = preparing| J
    Q -->|Hủy| I
  end

  O --> R(["Kết quả: đã giao"])
  I --> S(["Kết quả: không thực hiện, đơn bị hủy"])
  F --> T(["Kết quả: không tạo đơn"])
```

## Khác với sơ đồ cũ

| Sơ đồ cũ | Code hiện tại |
|---|---|
| Trạng thái đơn là `Pending` | Trạng thái đơn là `pending_confirmation` |
| "Cancel Payment (before ship)" và "Cancel Order-refund if paid" | Chưa có chức năng khách tự hủy đơn và chưa có hoàn tiền. Khi shop từ chối, đơn chuyển sang `cancelled` |
| "Payment Successful?" tự động | Chưa có cổng thanh toán. Đơn online chỉ có `Paid` khi được xác nhận thủ công |
| "Prepare order & assign delivery" | Shop chuyển `preparing` → `shipping` (`ready`) và chọn shipper trong cùng bước |
| Không có vai trò admin | Admin xử lý đơn sự cố: mở lại (đưa về hàng chờ giao) hoặc hủy |
| Không có vòng từ chối của shipper | Shipper từ chối thì shop phân công shipper khác |

## Lưu ý

- Luồng checkout từ giỏ hàng chưa tạo đơn thật. `INSERT INTO orders` chỉ có trong `OrderDAO` để tạo dữ liệu mẫu. Cần làm thêm bước này trước khi sơ đồ khách hàng có hiệu lực.
- Với đơn COD, shipper phải xác nhận đã thu tiền trước khi đơn được đánh dấu giao xong (`DeliveryRules.validatePayment`).
- Đơn `cancelled` và `delivered` là trạng thái kết thúc, không đổi được nữa (`OrderDAO.updateOrderStatus`).
