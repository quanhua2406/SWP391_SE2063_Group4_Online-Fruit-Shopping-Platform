-- ============================================================
-- Sample orders for the remaining order statuses
--
-- Purpose: demo the "Fee by Order Status" table and the order
-- status filter of the commission report. The two original
-- sample orders stay delivered, so the verified report figures
-- (1.250.000 VND revenue / 187.500 VND platform fee) do not
-- change: commission is counted on delivered orders only.
--
-- Rerunning this script is safe: every insert is guarded.
-- ============================================================

USE OnlineFruitShopping;
GO

SET NOCOUNT ON;

DECLARE @oid INT;

-- ------------------------------------------------------------
-- Pending confirmation: two 0.5 kg packages
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM orders
               WHERE user_id = 2
                 AND status = 'pending_confirmation'
                 AND total_amount = 56000.00)
BEGIN
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id,
                        status, total_amount, created_at)
    VALUES (2, 1, 1, NULL, 'pending_confirmation',
            56000.00, '2026-10-18 09:15:00');

    SET @oid = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id,
                             quantity, unit_price)
    VALUES (@oid, 17, NULL, 2.00, 28000.00);
END

-- ------------------------------------------------------------
-- Preparing: three 1 kg packages sold at the approved promo
-- price (45.000 instead of 50.000) - this is what feeds the
-- "promotional discount" line of the fee breakdown.
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM orders
               WHERE user_id = 2
                 AND status = 'preparing'
                 AND total_amount = 135000.00)
BEGIN
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id,
                        status, total_amount, created_at)
    VALUES (2, 1, 1, NULL, 'preparing',
            135000.00, '2026-10-18 14:30:00');

    SET @oid = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id,
                             quantity, unit_price)
    VALUES (@oid, 1, NULL, 3.00, 45000.00);
END

-- ------------------------------------------------------------
-- Shipping: one 2 kg package
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM orders
               WHERE user_id = 2
                 AND status = 'shipping'
                 AND total_amount = 95000.00)
BEGIN
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id,
                        status, total_amount, created_at)
    VALUES (2, 1, 1, NULL, 'shipping',
            95000.00, '2026-10-19 08:00:00');

    SET @oid = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id,
                             quantity, unit_price)
    VALUES (@oid, 18, NULL, 1.00, 95000.00);
END

-- ------------------------------------------------------------
-- Cancelled: reported as a refund, never counted as fee
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM orders
               WHERE user_id = 2
                 AND status = 'cancelled'
                 AND total_amount = 100000.00)
BEGIN
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id,
                        status, total_amount, created_at)
    VALUES (2, 1, 1, NULL, 'cancelled',
            100000.00, '2026-10-16 11:00:00');

    SET @oid = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id,
                             quantity, unit_price)
    VALUES (@oid, 1, NULL, 2.00, 50000.00);
END

-- ------------------------------------------------------------
-- Failed: reported as a refund, never counted as fee
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM orders
               WHERE user_id = 2
                 AND status = 'failed'
                 AND total_amount = 250000.00)
BEGIN
    INSERT INTO orders (user_id, shop_id, address_id, voucher_id,
                        status, total_amount, created_at)
    VALUES (2, 1, 1, NULL, 'failed',
            250000.00, '2026-10-15 16:45:00');

    SET @oid = SCOPE_IDENTITY();

    INSERT INTO order_items (order_id, variant_id, batch_id,
                             quantity, unit_price)
    VALUES (@oid, 2, NULL, 1.00, 250000.00);
END

-- ------------------------------------------------------------
-- Overview
-- ------------------------------------------------------------
SELECT o.order_id, o.status, o.total_amount, o.created_at
FROM orders o
ORDER BY o.order_id;
GO
