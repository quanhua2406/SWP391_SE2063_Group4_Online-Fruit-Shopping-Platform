-- =============================================================
-- FreshFruit - DEMO DATA for the TienPQ features
--   1.0 / 2.0  Auth, Profile, Address, User Admin
--   16.0       Role Management
--   17.0       Product Administration
--   18.0       Shop Owner Management + Commission reports
--   19.0       Order & Delivery Administration
--   24.0       Inventory & Pricing
-- =============================================================
-- Gives every admin / shop-owner screen something meaningful to
-- show during a demo: products in each status, orders in each
-- status, delivery assignments in each status, a couple of extra
-- delivery-staff accounts and an inventory batch that is low.
--
-- SAFE & IDEMPOTENT: it refuses to run twice (it looks for the
-- marker account 'shipper2@freshfruit.com'). See the CLEANUP
-- section at the bottom to remove everything again.
--
-- Run with:
--   sqlcmd -S localhost,1433 -U sa -P 123 -d OnlineFruitShopping -i demo_data.sql
-- =============================================================

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF EXISTS (SELECT 1 FROM users WHERE email = 'shipper2@freshfruit.com')
BEGIN
    PRINT 'Demo data already present - nothing was inserted.';
    RETURN;
END

BEGIN TRANSACTION;

-- -------------------------------------------------------------
-- Resolve the accounts / shop we work with
-- -------------------------------------------------------------
DECLARE @customerId INT = (SELECT user_id FROM users WHERE email = 'customer@freshfruit.com');
DECLARE @ownerId    INT = (SELECT user_id FROM users WHERE email = 'shopowner@freshfruit.com');
DECLARE @staffId    INT = (SELECT user_id FROM users WHERE email = 'delivery@freshfruit.com');
DECLARE @shopId     INT = (SELECT shop_id FROM shops WHERE owner_id = @ownerId);

IF @shopId IS NULL
BEGIN
    RAISERROR('Demo shop not found - run the main seed first.', 16, 1);
    ROLLBACK TRANSACTION;
    RETURN;
END

-- -------------------------------------------------------------
-- 1. Extra delivery-staff accounts (19.0 assign dropdown)
--    password hash = BCrypt of 'Tien123456'
-- -------------------------------------------------------------
DECLARE @pwd NVARCHAR(100) =
    (SELECT TOP 1 password_hash FROM users WHERE email = 'delivery@freshfruit.com');

IF @pwd IS NULL
    SET @pwd = (SELECT TOP 1 password_hash FROM users WHERE email = 'customer@freshfruit.com');

INSERT INTO users (full_name, email, password_hash, phone, status, created_at)
VALUES (N'Nguyen Van Shipper', N'shipper2@freshfruit.com', @pwd, N'0911111111', N'active', SYSDATETIME());

DECLARE @staff2 INT = SCOPE_IDENTITY();

INSERT INTO users (full_name, email, password_hash, phone, status, created_at)
VALUES (N'Tran Thi Shipper', N'shipper3@freshfruit.com', @pwd, N'0922222222', N'active', SYSDATETIME());

DECLARE @staff3 INT = SCOPE_IDENTITY();

INSERT INTO user_roles (user_id, role_id) VALUES (@staff2, 4), (@staff3, 4);

-- -------------------------------------------------------------
-- 2. Extra categories (17.0)
-- -------------------------------------------------------------
INSERT INTO categories (category_name, parent_id)
VALUES (N'Tropical Fruits', NULL),
       (N'Berries', NULL),
       (N'Citrus', NULL);

DECLARE @catTropical INT = (SELECT category_id FROM categories WHERE category_name = N'Tropical Fruits');
DECLARE @catBerries  INT = (SELECT category_id FROM categories WHERE category_name = N'Berries');
DECLARE @catCitrus   INT = (SELECT category_id FROM categories WHERE category_name = N'Citrus');

-- -------------------------------------------------------------
-- 3. Extra products + variants (17.0)
--    One published, one draft (to approve), one hidden.
-- -------------------------------------------------------------
DECLARE @pBanana INT, @pStrawberry INT, @pOrange INT;

INSERT INTO products (shop_id, category_id, product_name, description, origin, status, created_at, commission_rate)
VALUES (@shopId, @catTropical, N'Banana', N'Sweet ripe bananas', N'Vietnam', N'published', SYSDATETIME(), 5.00);
SET @pBanana = SCOPE_IDENTITY();

INSERT INTO products (shop_id, category_id, product_name, description, origin, status, created_at, commission_rate)
VALUES (@shopId, @catBerries, N'Strawberry', N'Fresh Da Lat strawberries', N'Vietnam', N'draft', SYSDATETIME(), 8.00);
SET @pStrawberry = SCOPE_IDENTITY();

INSERT INTO products (shop_id, category_id, product_name, description, origin, status, created_at, commission_rate)
VALUES (@shopId, @catCitrus, N'Orange', N'Juicy imported oranges', N'Australia', N'hidden', SYSDATETIME(), 6.00);
SET @pOrange = SCOPE_IDENTITY();

INSERT INTO product_variants (product_id, sku, unit, weight_value, packaging_type, price, created_at)
VALUES
    (@pBanana,     N'BANANA-1KG', N'kg', 1.00, N'Bag 1kg',   30000, SYSDATETIME()),
    (@pBanana,     N'BANANA-3KG', N'kg', 3.00, N'Bag 3kg',   85000, SYSDATETIME()),
    (@pStrawberry, N'STRAW-500G', N'kg', 0.50, N'Box 500g',  60000, SYSDATETIME()),
    (@pOrange,     N'ORANGE-2KG', N'kg', 2.00, N'Bag 2kg',   70000, SYSDATETIME());

DECLARE @vBanana1 INT = (SELECT variant_id FROM product_variants WHERE sku = 'BANANA-1KG');
DECLARE @vBanana3 INT = (SELECT variant_id FROM product_variants WHERE sku = 'BANANA-3KG');
DECLARE @vStraw   INT = (SELECT variant_id FROM product_variants WHERE sku = 'STRAW-500G');
DECLARE @vOrange  INT = (SELECT variant_id FROM product_variants WHERE sku = 'ORANGE-2KG');

-- -------------------------------------------------------------
-- 4. Inventory batches (24.0)
--    Give the new variants stock; make ORANGE-2KG low so the
--    "low stock" screen has something to show.
-- -------------------------------------------------------------
INSERT INTO inventory_batches (variant_id, batch_code, quantity, expiry_date, received_date, status)
VALUES
    (@vBanana1, N'BATCH-BAN-01', 120, DATEADD(MONTH, 2, CAST(SYSDATETIME() AS DATE)), CAST(SYSDATETIME() AS DATE), N'active'),
    (@vBanana3, N'BATCH-BAN-02', 40,  DATEADD(MONTH, 2, CAST(SYSDATETIME() AS DATE)), CAST(SYSDATETIME() AS DATE), N'active'),
    (@vStraw,   N'BATCH-STR-01', 15,  DATEADD(DAY, 5, CAST(SYSDATETIME() AS DATE)), CAST(SYSDATETIME() AS DATE), N'active'),
    (@vOrange,  N'BATCH-ORA-01', 3,   DATEADD(MONTH, 1, CAST(SYSDATETIME() AS DATE)), CAST(SYSDATETIME() AS DATE), N'active');

DECLARE @bBanana1 INT = (SELECT batch_id FROM inventory_batches WHERE batch_code = 'BATCH-BAN-01');
DECLARE @bOrange  INT = (SELECT batch_id FROM inventory_batches WHERE batch_code = 'BATCH-ORA-01');

-- -------------------------------------------------------------
-- 5. A second delivery address for the customer (2.0)
-- -------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = @customerId AND recipient_name = N'Customer (Office)')
    INSERT INTO user_addresses (user_id, recipient_name, phone, address_line, ward, district, province, is_default)
    VALUES (@customerId, N'Customer (Office)', N'0900000001', N'456 Le Loi', N'Ben Nghe', N'District 1', N'Ho Chi Minh', 0);

DECLARE @addrId INT = (SELECT TOP 1 address_id FROM user_addresses WHERE user_id = @customerId ORDER BY address_id);

-- -------------------------------------------------------------
-- 6. Demo orders in every status (19.0)
--    New statuses alongside the existing seed: pending_confirmation,
--    preparing, shipping, failed  (delivered/cancelled already exist)
-- -------------------------------------------------------------
DECLARE @oPending INT, @oPreparing INT, @oShipping INT, @oFailed INT;

-- pending_confirmation
INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
VALUES (@customerId, @shopId, @addrId, NULL, N'pending_confirmation', 90000, DATEADD(HOUR, -3, SYSDATETIME()));
SET @oPending = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
VALUES (@oPending, @vBanana1, @bBanana1, 3, 30000);

-- preparing
INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
VALUES (@customerId, @shopId, @addrId, NULL, N'preparing', 170000, DATEADD(HOUR, -8, SYSDATETIME()));
SET @oPreparing = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
VALUES (@oPreparing, @vBanana3, NULL, 2, 85000);

-- shipping (with a delivery in progress)
INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
VALUES (@customerId, @shopId, @addrId, NULL, N'shipping', 100000, DATEADD(DAY, -1, SYSDATETIME()));
SET @oShipping = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
VALUES (@oShipping, @vBanana1, @bBanana1, 1, 30000),
       (@oShipping, @vOrange,  @bOrange,  1, 70000);

-- failed (so "Handle Order Issues" has a candidate)
INSERT INTO orders (user_id, shop_id, address_id, voucher_id, status, total_amount, created_at)
VALUES (@customerId, @shopId, @addrId, NULL, N'failed', 85000, DATEADD(DAY, -4, SYSDATETIME()));
SET @oFailed = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price)
VALUES (@oFailed, @vBanana3, NULL, 1, 85000);

-- -------------------------------------------------------------
-- 7. Payments for the demo orders (19.0 order detail)
-- -------------------------------------------------------------
INSERT INTO payments (order_id, method, amount, status, transaction_ref, paid_at)
VALUES
    (@oPending,   N'cod',  90000,  N'pending', NULL, NULL),
    (@oPreparing, N'momo', 170000, N'paid',    N'MOMO-DEMO-001', DATEADD(HOUR, -7, SYSDATETIME())),
    (@oShipping,  N'vnpay',100000, N'paid',    N'VNPAY-DEMO-002', DATEADD(DAY, -1, SYSDATETIME())),
    (@oFailed,    N'card', 85000,  N'failed',  N'CARD-DEMO-003', NULL);

-- -------------------------------------------------------------
-- 8. Status history for the demo orders (19.0 order detail)
-- -------------------------------------------------------------
INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
VALUES
    (@oPending,   N'pending_confirmation', @customerId, DATEADD(HOUR, -3, SYSDATETIME())),
    (@oPreparing, N'pending_confirmation', @customerId, DATEADD(HOUR, -8, SYSDATETIME())),
    (@oPreparing, N'preparing',            @ownerId,    DATEADD(HOUR, -6, SYSDATETIME())),
    (@oShipping,  N'pending_confirmation', @customerId, DATEADD(DAY, -1, SYSDATETIME())),
    (@oShipping,  N'preparing',            @ownerId,    DATEADD(HOUR, -20, SYSDATETIME())),
    (@oShipping,  N'shipping',             @ownerId,    DATEADD(HOUR, -18, SYSDATETIME())),
    (@oFailed,    N'pending_confirmation', @customerId, DATEADD(DAY, -4, SYSDATETIME())),
    (@oFailed,    N'failed',               @ownerId,    DATEADD(DAY, -4, SYSDATETIME()));

-- -------------------------------------------------------------
-- 9. Deliveries in several statuses (19.0 deliveries / assign)
--    A fresh Db   value so the demo has data.
-- -------------------------------------------------------------
INSERT INTO deliveries (order_id, staff_id, status, note, created_at)
VALUES
    (@oShipping,  @staffId, N'delivering', N'Out for delivery - demo',   DATEADD(HOUR, -18, SYSDATETIME())),
    (@oPreparing, @staff2,  N'assigned',   N'Assigned by shop owner',    DATEADD(HOUR, -4, SYSDATETIME()));

-- Delivered delivery for a finished order (shows the whole lifecycle).
DECLARE @oDelivered INT = (SELECT TOP 1 order_id FROM orders WHERE status = N'delivered' ORDER BY order_id);
IF NOT EXISTS (SELECT 1 FROM deliveries WHERE order_id = @oDelivered)
    INSERT INTO deliveries (order_id, staff_id, status, note, proof_image_url, created_at)
    VALUES (@oDelivered, @staffId, N'delivered', N'Delivered successfully', N'/uploads/proof/demo-proof.jpg', DATEADD(DAY, -5, SYSDATETIME()));

-- -------------------------------------------------------------
-- 10. One failed delivery (19.0 deliveries status mix)
-- -------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM deliveries WHERE order_id = @oFailed)
    INSERT INTO deliveries (order_id, staff_id, status, note, created_at)
    VALUES (@oFailed, @staff3, N'failed', N'Customer not reachable', DATEADD(DAY, -4, SYSDATETIME()));

COMMIT TRANSACTION;

PRINT 'Demo data inserted.';
SELECT 'users' AS entity, COUNT(*) AS total FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'products', COUNT(*) FROM products
UNION ALL SELECT 'variants', COUNT(*) FROM product_variants
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'deliveries', COUNT(*) FROM deliveries
UNION ALL SELECT 'batches', COUNT(*) FROM inventory_batches;


-- =============================================================
-- CLEANUP - run this to remove every row this script added
-- =============================================================
--  DECLARE @c INT = (SELECT user_id FROM users WHERE email='customer@freshfruit.com');
--  DECLARE @s2 INT = (SELECT user_id FROM users WHERE email='shipper2@freshfruit.com');
--  DECLARE @s3 INT = (SELECT user_id FROM users WHERE email='shipper3@freshfruit.com');
--  DELETE FROM order_status_history WHERE order_id IN (SELECT order_id FROM orders WHERE total_amount IN (90000,170000,100000,85000) AND user_id=@c);
--  DELETE FROM payments            WHERE order_id IN (SELECT order_id FROM orders WHERE total_amount IN (90000,170000,100000,85000) AND user_id=@c);
--  DELETE FROM deliveries          WHERE staff_id IN (@s2,@s3);
--  DELETE FROM order_items         WHERE order_id IN (SELECT order_id FROM orders WHERE total_amount IN (90000,170000,100000,85000) AND user_id=@c);
--  DELETE FROM orders              WHERE total_amount IN (90000,170000,100000,85000) AND user_id=@c;
--  DELETE FROM inventory_batches   WHERE batch_code IN ('BATCH-BAN-01','BATCH-BAN-02','BATCH-STR-01','BATCH-ORA-01');
--  DELETE FROM product_variants    WHERE sku IN ('BANANA-1KG','BANANA-3KG','STRAW-500G','ORANGE-2KG');
--  DELETE FROM products            WHERE product_name IN ('Banana','Strawberry','Orange');
--  DELETE FROM categories          WHERE category_name IN ('Tropical Fruits','Berries','Citrus');
--  DELETE FROM user_addresses      WHERE recipient_name = 'Customer (Office)';
--  DELETE FROM user_roles          WHERE user_id IN (@s2,@s3);
--  DELETE FROM users               WHERE user_id IN (@s2,@s3);
-- =============================================================
