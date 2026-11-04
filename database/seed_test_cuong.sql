-- ============================================================
-- ONLINE FRUIT SHOPPING - DỮ LIỆU TÀI KHOẢN TEST CHO CƯỜNG (CuongLD)
-- Phụ trách:
--   1. Shopping Cart Management (Feature 5 - Customer)
--   2. Order Management (Feature 6 - Customer & Shop Owner)
--   3. Payment Management (Feature 7 - Customer, Shop Owner, Admin)
-- Mật khẩu mặc định cho tất cả tài khoản: 123
-- Hash BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- Script an toàn (idempotent), có thể chạy lại nhiều lần mà không trùng lặp.
-- ============================================================

USE OnlineFruitShopping;
GO

SET NOCOUNT ON;

DECLARE @pw NVARCHAR(255) = N'$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'; -- "123"

-- ------------------------------------------------------------
-- 1. TẠO TÀI KHOẢN KHÁCH HÀNG (CUSTOMER) CHO CƯỜNG
-- Email: cuong@fruit.com / cuong.customer@fruit.com
-- Password: 123
-- ------------------------------------------------------------
DECLARE @cuongCustomerId INT;
SELECT @cuongCustomerId = user_id FROM users WHERE email = 'cuong@fruit.com';

IF @cuongCustomerId IS NULL
BEGIN
    INSERT INTO users (full_name, email, phone, password_hash, status, created_at, is_member)
    VALUES (N'Lê Đình Cường', 'cuong@fruit.com', '0988776655', @pw, 'active', GETDATE(), 1);

    SET @cuongCustomerId = SCOPE_IDENTITY();

    DECLARE @customerRoleId INT = (SELECT role_id FROM roles WHERE role_name = 'Customer');
    INSERT INTO user_roles (user_id, role_id) VALUES (@cuongCustomerId, @customerRoleId);

    PRINT N'Đã tạo tài khoản Customer: cuong@fruit.com (Pass: 123)';
END
ELSE
BEGIN
    PRINT N'Tài khoản Customer cuong@fruit.com đã tồn tại.';
END

-- Thêm tài khoản alias cuong.customer@fruit.com (nếu test dùng email này)
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'cuong.customer@fruit.com')
BEGIN
    INSERT INTO users (full_name, email, phone, password_hash, status, created_at, is_member)
    VALUES (N'Lê Đình Cường (Customer)', 'cuong.customer@fruit.com', '0988776655', @pw, 'active', GETDATE(), 1);
    
    DECLARE @aliasCustomerId INT = SCOPE_IDENTITY();
    DECLARE @custRoleId INT = (SELECT role_id FROM roles WHERE role_name = 'Customer');
    INSERT INTO user_roles (user_id, role_id) VALUES (@aliasCustomerId, @custRoleId);
END

-- ------------------------------------------------------------
-- 2. ĐỊA CHỈ NHẬN HÀNG CỦA CƯỜNG (Để test Checkout / Chọn địa chỉ)
-- ------------------------------------------------------------
DECLARE @cuongAddressId1 INT, @cuongAddressId2 INT;

SELECT @cuongAddressId1 = address_id FROM user_addresses 
WHERE user_id = @cuongCustomerId AND is_default = 1;

IF @cuongAddressId1 IS NULL
BEGIN
    INSERT INTO user_addresses (user_id, recipient_name, phone, address_line, ward, district, province, is_default)
    VALUES (@cuongCustomerId, N'Lê Đình Cường', '0988776655', N'Tòa nhà FPT, Phố Duy Tân', N'Dịch Vọng Hậu', N'Cầu Giấy', N'Hà Nội', 1);
    SET @cuongAddressId1 = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = @cuongCustomerId AND is_default = 0)
BEGIN
    INSERT INTO user_addresses (user_id, recipient_name, phone, address_line, ward, district, province, is_default)
    VALUES (@cuongCustomerId, N'Lê Đình Cường (Nhà riêng)', '0912345679', N'Số 68 Đường Cầu Giấy', N'Quan Hoa', N'Cầu Giấy', N'Hà Nội', 0);
    SET @cuongAddressId2 = SCOPE_IDENTITY();
END

-- ------------------------------------------------------------
-- 3. GIỎ HÀNG ACTIVE CỦA CƯỜNG (Để test Giỏ hàng & Checkout ngay)
-- ------------------------------------------------------------
DECLARE @cuongCartId INT;
SELECT @cuongCartId = cart_id FROM carts WHERE user_id = @cuongCustomerId;

IF @cuongCartId IS NULL
BEGIN
    INSERT INTO carts (user_id, created_at)
    VALUES (@cuongCustomerId, GETDATE());
    SET @cuongCartId = SCOPE_IDENTITY();
END

-- Thêm các sản phẩm vào giỏ hàng test
IF NOT EXISTS (SELECT 1 FROM cart_items WHERE cart_id = @cuongCartId)
BEGIN
    -- Variant 1: Táo Envy New Zealand (2 hộp)
    -- Variant 2: Xoài Cát Hòa Lộc (3 kg)
    INSERT INTO cart_items (cart_id, variant_id, quantity)
    VALUES 
        (@cuongCartId, 1, 2.00),
        (@cuongCartId, 2, 3.00);
    PRINT N'Đã thêm sản phẩm mẫu vào giỏ hàng của cuong@fruit.com';
END

-- ------------------------------------------------------------
-- 4. TẠO TÀI KHOẢN CHỦ SHOP (SHOP OWNER) CHO CƯỜNG
-- Email: cuong.shop@fruit.com
-- Password: 123
-- Shop: Cường Organic Fruit Store
-- ------------------------------------------------------------
DECLARE @cuongShopOwnerId INT;
SELECT @cuongShopOwnerId = user_id FROM users WHERE email = 'cuong.shop@fruit.com';

IF @cuongShopOwnerId IS NULL
BEGIN
    INSERT INTO users (full_name, email, phone, password_hash, status, created_at, is_member)
    VALUES (N'Lê Đình Cường (Shop Owner)', 'cuong.shop@fruit.com', '0988776699', @pw, 'active', GETDATE(), 0);

    SET @cuongShopOwnerId = SCOPE_IDENTITY();

    DECLARE @shopOwnerRoleId INT = (SELECT role_id FROM roles WHERE role_name = 'ShopOwner');
    INSERT INTO user_roles (user_id, role_id) VALUES (@cuongShopOwnerId, @shopOwnerRoleId);

    PRINT N'Đã tạo tài khoản Shop Owner: cuong.shop@fruit.com (Pass: 123)';
END

-- Tạo Shop cho Cường nếu chưa có
DECLARE @cuongShopId INT;
SELECT @cuongShopId = shop_id FROM shops WHERE owner_id = @cuongShopOwnerId;

IF @cuongShopId IS NULL
BEGIN
    INSERT INTO shops (owner_id, shop_name, description, status, created_at, low_stock_threshold)
    VALUES (@cuongShopOwnerId, N'Cường Organic Fruit Store', N'Cửa hàng trái cây hữu cơ và hoa quả nhập khẩu cao cấp của Cường.', 'active', GETDATE(), 10.00);
    SET @cuongShopId = SCOPE_IDENTITY();
    PRINT N'Đã tạo Shop cho Cường: Cường Organic Fruit Store';
END

-- ------------------------------------------------------------
-- 5. TẠO SẢN PHẨM RIÊNG CHO SHOP CỦA CƯỜNG (Để quản lý đơn & tồn kho)
-- ------------------------------------------------------------
DECLARE @cuongProdId INT, @cuongVarId1 INT, @cuongVarId2 INT;
SELECT @cuongProdId = product_id FROM products WHERE shop_id = @cuongShopId AND product_name = N'Dưa Lưới Huỳnh Long Cường Farm';

IF @cuongProdId IS NULL
BEGIN
    INSERT INTO products (shop_id, category_id, product_name, description, origin, status, created_at, commission_rate, hidden_by_admin)
    VALUES (@cuongShopId, 2, N'Dưa Lưới Huỳnh Long Cường Farm', N'Dưa lưới ruột vàng thơm ngọt mát, canh tác organic an toàn.', N'Việt Nam', 'published', GETDATE(), 5.00, 0);
    SET @cuongProdId = SCOPE_IDENTITY();

    -- Variant 1: Quả 1.5 - 2kg
    INSERT INTO product_variants (product_id, sku, unit, weight_value, packaging_type, price, created_at)
    VALUES (@cuongProdId, 'DUA-HL-QUA', N'Quả', 1.80, N'Quả', 120000.00, GETDATE());
    SET @cuongVarId1 = SCOPE_IDENTITY();

    -- Variant 2: Thùng 3 quả
    INSERT INTO product_variants (product_id, sku, unit, weight_value, packaging_type, price, created_at)
    VALUES (@cuongProdId, 'DUA-HL-THUNG', N'Thùng', 5.50, N'Thùng', 330000.00, GETDATE());
    SET @cuongVarId2 = SCOPE_IDENTITY();

    -- Inventory batch
    INSERT INTO inventory_batches (variant_id, batch_code, quantity, expiry_date, received_date, status)
    VALUES 
        (@cuongVarId1, 'BATCH-DUA-01', 50.00, DATEADD(DAY, 30, GETDATE()), GETDATE(), 'active'),
        (@cuongVarId2, 'BATCH-DUA-02', 30.00, DATEADD(DAY, 30, GETDATE()), GETDATE(), 'active');
END
ELSE
BEGIN
    SELECT TOP 1 @cuongVarId1 = variant_id FROM product_variants WHERE product_id = @cuongProdId;
END

-- ------------------------------------------------------------
-- 6. DỮ LIỆU ĐƠN HÀNG MẪU CHO CƯỜNG (CUSTOMER) ĐỂ TEST MỌI TRẠNG THÁI
-- Bao gồm:
--   Đơn 1: pending_confirmation -> Test: Hủy đơn, Theo dõi đơn
--   Đơn 2: preparing            -> Test: Xem chi tiết đơn đang chuẩn bị
--   Đơn 3: shipping             -> Test: Theo dõi đơn hàng (Track Order Progression)
--   Đơn 4: delivered            -> Test: Mua lại (Re-order) & Gửi yêu cầu hoàn tiền (Request Refund)
--   Đơn 5: delivered + refunded -> Test: Xem trạng thái hoàn tiền (Refund Status)
--   Đơn 6: cancelled            -> Test: Lọc đơn đã hủy
-- ------------------------------------------------------------
DECLARE @targetShopId INT = 1; -- Shop Thành Trung
DECLARE @shipperId INT = 5;    -- Shipper Nam
IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = @shipperId)
    SELECT TOP 1 @shipperId = user_id FROM users WHERE email LIKE '%shipper%' OR email LIKE '%delivery%';

-- ĐƠN 1: Chờ xác nhận (pending_confirmation)
IF NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @cuongCustomerId AND status = 'pending_confirmation')
BEGIN
    DECLARE @o1 INT;
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
    VALUES (@cuongCustomerId, @targetShopId, @cuongAddressId1, NULL, 'pending_confirmation', 220000.00, DATEADD(HOUR, -2, GETDATE()));
    SET @o1 = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
    VALUES (@o1, 1, NULL, 1.00, 220000.00);

    INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
    VALUES (@o1, 'cod', 220000.00, 'pending', 'COD-' + CAST(@o1 AS NVARCHAR(20)), NULL);

    INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
    VALUES (@o1, 'pending_confirmation', @cuongCustomerId, DATEADD(HOUR, -2, GETDATE()));
    PRINT N'Đã tạo Đơn 1 (pending_confirmation) cho Cường';
END

-- ĐƠN 2: Đang chuẩn bị (preparing)
IF NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @cuongCustomerId AND status = 'preparing')
BEGIN
    DECLARE @o2 INT;
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
    VALUES (@cuongCustomerId, @targetShopId, @cuongAddressId1, 2, 'preparing', 256500.00, DATEADD(DAY, -1, GETDATE()));
    SET @o2 = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
    VALUES (@o2, 2, NULL, 3.00, 95000.00);

    INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
    VALUES (@o2, 'vnpay', 256500.00, 'completed', 'VNPAY-' + CAST(@o2 AS NVARCHAR(20)), DATEADD(DAY, -1, GETDATE()));

    INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
    VALUES 
        (@o2, 'pending_confirmation', @cuongCustomerId, DATEADD(DAY, -1, GETDATE())),
        (@o2, 'preparing', 1, DATEADD(HOUR, -20, GETDATE()));
    PRINT N'Đã tạo Đơn 2 (preparing) cho Cường';
END

-- ĐƠN 3: Đang giao hàng (shipping)
IF NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @cuongCustomerId AND status = 'shipping')
BEGIN
    DECLARE @o3 INT;
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
    VALUES (@cuongCustomerId, @targetShopId, @cuongAddressId1, NULL, 'shipping', 450000.00, DATEADD(DAY, -2, GETDATE()));
    SET @o3 = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
    VALUES (@o3, 3, NULL, 1.00, 450000.00);

    INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
    VALUES (@o3, 'vnpay', 450000.00, 'completed', 'VNPAY-' + CAST(@o3 AS NVARCHAR(20)), DATEADD(DAY, -2, GETDATE()));

    IF @shipperId IS NOT NULL
    BEGIN
        INSERT INTO deliveries (order_id, staff_id, status, proof_image_url, note, created_at)
        VALUES (@o3, @shipperId, 'shipping', NULL, N'Đang giao trong chiều nay', DATEADD(HOUR, -4, GETDATE()));
    END

    INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
    VALUES 
        (@o3, 'pending_confirmation', @cuongCustomerId, DATEADD(DAY, -2, GETDATE())),
        (@o3, 'preparing', 1, DATEADD(DAY, -2, DATEADD(HOUR, 2, GETDATE()))),
        (@o3, 'shipping', @shipperId, DATEADD(HOUR, -4, GETDATE()));
    PRINT N'Đã tạo Đơn 3 (shipping) cho Cường';
END

-- ĐƠN 4: Đã hoàn thành (delivered) - Dùng test Re-order và Yêu cầu hoàn tiền
IF NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @cuongCustomerId AND status = 'delivered' AND total_amount = 650000.00)
BEGIN
    DECLARE @o4 INT;
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
    VALUES (@cuongCustomerId, @targetShopId, @cuongAddressId1, NULL, 'delivered', 650000.00, DATEADD(DAY, -5, GETDATE()));
    SET @o4 = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
    VALUES (@o4, 4, NULL, 1.00, 650000.00);

    INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
    VALUES (@o4, 'vnpay', 650000.00, 'completed', 'VNPAY-' + CAST(@o4 AS NVARCHAR(20)), DATEADD(DAY, -5, GETDATE()));

    IF @shipperId IS NOT NULL
    BEGIN
        INSERT INTO deliveries (order_id, staff_id, status, proof_image_url, note, created_at)
        VALUES (@o4, @shipperId, 'delivered', N'/assets/images/proof_delivery.jpg', N'Đã giao thành công cho khách', DATEADD(DAY, -4, GETDATE()));
    END

    INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
    VALUES 
        (@o4, 'pending_confirmation', @cuongCustomerId, DATEADD(DAY, -5, GETDATE())),
        (@o4, 'preparing', 1, DATEADD(DAY, -5, DATEADD(HOUR, 2, GETDATE()))),
        (@o4, 'shipping', @shipperId, DATEADD(DAY, -4, GETDATE())),
        (@o4, 'delivered', @shipperId, DATEADD(DAY, -4, DATEADD(HOUR, 3, GETDATE())));
    PRINT N'Đã tạo Đơn 4 (delivered) cho Cường (để test Re-order / Hoàn tiền)';
END

-- ĐƠN 5: Đã giao và ĐANG CÓ YÊU CẦU HOÀN TIỀN (requested refund)
IF NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @cuongCustomerId AND status = 'delivered' AND total_amount = 285000.00)
BEGIN
    DECLARE @o5 INT, @pay5 INT;
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
    VALUES (@cuongCustomerId, @targetShopId, @cuongAddressId1, NULL, 'delivered', 285000.00, DATEADD(DAY, -7, GETDATE()));
    SET @o5 = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
    VALUES (@o5, 2, NULL, 3.00, 95000.00);

    INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
    VALUES (@o5, 'vnpay', 285000.00, 'completed', 'VNPAY-' + CAST(@o5 AS NVARCHAR(20)), DATEADD(DAY, -7, GETDATE()));
    SET @pay5 = SCOPE_IDENTITY();

    INSERT INTO refunds (payment_id, processed_by, amount, status, reason, requested_at, processed_at)
    VALUES (@pay5, NULL, 285000.00, 'requested', N'Quả xoài bị dập và hỏng 1 phần trong quá trình giao hàng, yêu cầu hoàn tiền.', DATEADD(DAY, -1, GETDATE()), NULL);

    PRINT N'Đã tạo Đơn 5 kèm Yêu cầu hoàn tiền (Refund requested) cho Cường';
END

-- ĐƠN 6: Đã hủy (cancelled)
IF NOT EXISTS (SELECT 1 FROM orders WHERE user_id = @cuongCustomerId AND status = 'cancelled')
BEGIN
    DECLARE @o6 INT;
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
    VALUES (@cuongCustomerId, @targetShopId, @cuongAddressId1, NULL, 'cancelled', 180000.00, DATEADD(DAY, -10, GETDATE()));
    SET @o6 = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
    VALUES (@o6, 5, NULL, 4.00, 45000.00);

    INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
    VALUES (@o6, 'cod', 180000.00, 'cancelled', 'COD-' + CAST(@o6 AS NVARCHAR(20)), NULL);

    INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
    VALUES 
        (@o6, 'pending_confirmation', @cuongCustomerId, DATEADD(DAY, -10, GETDATE())),
        (@o6, 'cancelled', @cuongCustomerId, DATEADD(DAY, -10, DATEADD(MINUTE, 30, GETDATE())));
    PRINT N'Đã tạo Đơn 6 (cancelled) cho Cường';
END

-- ------------------------------------------------------------
-- 7. ĐƠN HÀNG GỬI ĐẾN SHOP CỦA CƯỜNG (Để test màn hình Shop Owner)
-- Để Cường đăng nhập shopowner (cuong.shop@fruit.com) test:
--   - Xem đơn chờ duyệt -> Bấm Xác nhận đơn (Confirm) hoặc Từ chối (Reject)
--   - Chuẩn bị hàng -> Đánh dấu Ready / Giao Shipper
-- ------------------------------------------------------------
IF @cuongShopId IS NOT NULL
BEGIN
    -- Đơn khách hàng 3 (customer@fruit.com) đặt tới shop Cường: pending_confirmation
    IF NOT EXISTS (SELECT 1 FROM orders WHERE shop_id = @cuongShopId AND status = 'pending_confirmation')
    BEGIN
        DECLARE @so1 INT;
        INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
        VALUES (3, @cuongShopId, 1, NULL, 'pending_confirmation', 240000.00, DATEADD(MINUTE, -45, GETDATE()));
        SET @so1 = SCOPE_IDENTITY();

        INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
        VALUES (@so1, ISNULL(@cuongVarId1, 1), NULL, 2.00, 120000.00);

        INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
        VALUES (@so1, 'cod', 240000.00, 'pending', 'COD-' + CAST(@so1 AS NVARCHAR(20)), NULL);

        INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
        VALUES (@so1, 'pending_confirmation', 3, DATEADD(MINUTE, -45, GETDATE()));
        PRINT N'Đã tạo đơn khách đặt đến Shop của Cường (trạng thái pending_confirmation để test Xác nhận/Từ chối)';
    END

    -- Đơn khách đặt tới shop Cường: preparing
    IF NOT EXISTS (SELECT 1 FROM orders WHERE shop_id = @cuongShopId AND status = 'preparing')
    BEGIN
        DECLARE @so2 INT;
        INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
        VALUES (3, @cuongShopId, 1, NULL, 'preparing', 330000.00, DATEADD(HOUR, -3, GETDATE()));
        SET @so2 = SCOPE_IDENTITY();

        INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
        VALUES (@so2, ISNULL(@cuongVarId2, 2), NULL, 1.00, 330000.00);

        INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
        VALUES (@so2, 'vnpay', 330000.00, 'completed', 'VNPAY-' + CAST(@so2 AS NVARCHAR(20)), DATEADD(HOUR, -3, GETDATE()));

        INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
        VALUES 
            (@so2, 'pending_confirmation', 3, DATEADD(HOUR, -3, GETDATE())),
            (@so2, 'preparing', @cuongShopOwnerId, DATEADD(HOUR, -2, GETDATE()));
        PRINT N'Đã tạo đơn đang chuẩn bị tại Shop của Cường (trạng thái preparing để test Bàn giao Shipper)';
    END
END

-- ------------------------------------------------------------
-- 8. KẾT QUẢ TỔNG QUAN SAU KHI INSERT
-- ------------------------------------------------------------
PRINT N'============================================================';
PRINT N'TỔNG KẾT TÀI KHOẢN VÀ DỮ LIỆU TEST CHO CƯỜNG:';
PRINT N'============================================================';

SELECT u.user_id, u.full_name, u.email, r.role_name, u.status
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
WHERE u.email LIKE '%cuong%';

SELECT o.order_id, o.user_id, s.shop_name, o.status, o.total_amount, p.method AS pay_method, p.status AS pay_status, o.created_at
FROM orders o
JOIN shops s ON o.shop_id = s.shop_id
LEFT JOIN payments p ON o.order_id = p.order_id
WHERE o.user_id = @cuongCustomerId OR o.shop_id = @cuongShopId
ORDER BY o.order_id DESC;
GO
