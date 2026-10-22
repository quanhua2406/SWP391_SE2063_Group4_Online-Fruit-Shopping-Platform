-- =============================================================
-- FreshFruit - SAMPLE ORDERS (for testing the commission report)
-- =============================================================
-- Creates two small orders for the demo shop so the
-- "Commission Report" pages (shop owner + admin) show real
-- numbers.
--
-- Safe to run: it does nothing when the orders table already
-- has rows. See the CLEANUP section at the bottom to remove
-- the sample data again.
-- =============================================================

SET NOCOUNT ON;

IF EXISTS (SELECT 1 FROM orders)
BEGIN
    PRINT 'Orders already exist - sample orders were NOT inserted.';
    RETURN;
END

-- -------------------------------------------------------------
-- Resolve the accounts / shop / products involved
-- -------------------------------------------------------------
DECLARE @customerId INT =
    (SELECT user_id FROM users WHERE email = 'customer@freshfruit.com');

DECLARE @ownerId INT =
    (SELECT user_id FROM users WHERE email = 'shopowner@freshfruit.com');

DECLARE @shopId INT =
    (SELECT shop_id FROM shops WHERE owner_id = @ownerId);

DECLARE @kgVariant INT =
    (SELECT variant_id FROM product_variants WHERE sku = 'APPLE-1KG');

DECLARE @boxVariant INT =
    (SELECT variant_id FROM product_variants WHERE sku = 'APPLE-BOX');

DECLARE @batchId INT =
    (SELECT batch_id FROM inventory_batches WHERE batch_code = 'BATCH-001');

-- -------------------------------------------------------------
-- A delivery address for the customer (orders.address_id is
-- mandatory)
-- -------------------------------------------------------------
DECLARE @addressId INT =
    (SELECT TOP 1 address_id FROM user_addresses
     WHERE user_id = @customerId ORDER BY address_id);

IF @addressId IS NULL
BEGIN
    INSERT INTO user_addresses
        (user_id, recipient_name, phone, address_line,
         ward, district, province, is_default)
    VALUES
        (@customerId, N'Customer', N'0900000000',
         N'123 Nguyen Trai', N'Ben Thanh', N'District 1',
         N'Ho Chi Minh', 1);

    SET @addressId = SCOPE_IDENTITY();
END

-- -------------------------------------------------------------
-- Order 1 - delivered
--   10 x APPLE-1KG (50,000) + 2 x APPLE-BOX (250,000)
--   = 1,000,000
-- -------------------------------------------------------------
DECLARE @order1 INT;

INSERT INTO orders
    (user_id, shop_id, address_id, voucher_id, status,
     total_amount, created_at)
VALUES
    (@customerId, @shopId, @addressId, NULL, 'delivered',
     1000000, DATEADD(DAY, -6, SYSDATETIME()));

SET @order1 = SCOPE_IDENTITY();

INSERT INTO order_items
    (order_id, variant_id, batch_id, quantity, unit_price)
VALUES
    (@order1, @kgVariant,  @batchId, 10, 50000),
    (@order1, @boxVariant, NULL,      2, 250000);

-- -------------------------------------------------------------
-- Order 2 - delivered
--   5 x APPLE-1KG (50,000) = 250,000
-- -------------------------------------------------------------
DECLARE @order2 INT;

INSERT INTO orders
    (user_id, shop_id, address_id, voucher_id, status,
     total_amount, created_at)
VALUES
    (@customerId, @shopId, @addressId, NULL, 'delivered',
     250000, DATEADD(DAY, -2, SYSDATETIME()));

SET @order2 = SCOPE_IDENTITY();

INSERT INTO order_items
    (order_id, variant_id, batch_id, quantity, unit_price)
VALUES
    (@order2, @kgVariant, @batchId, 5, 50000);

PRINT 'Sample orders inserted.';
SELECT order_id, shop_id, status, total_amount, created_at
FROM orders ORDER BY order_id;


-- =============================================================
-- CLEANUP (run this to remove the sample orders again)
-- =============================================================
--  DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id = (SELECT user_id FROM users WHERE email='customer@freshfruit.com'));
--  DELETE FROM orders      WHERE user_id = (SELECT user_id FROM users WHERE email='customer@freshfruit.com');
--  DELETE FROM user_addresses WHERE user_id = (SELECT user_id FROM users WHERE email='customer@freshfruit.com');
-- =============================================================
