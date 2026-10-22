-- =============================================================
-- FreshFruit - add shops.low_stock_threshold
-- =============================================================
-- Each shop can now configure its own "low stock" safety
-- threshold instead of the hard-coded 10.
--
-- The script is idempotent: it only adds the column when it is
-- missing.
-- =============================================================

SET NOCOUNT ON;
GO

IF COL_LENGTH('shops', 'low_stock_threshold') IS NULL
BEGIN
    ALTER TABLE shops
        ADD low_stock_threshold DECIMAL(18, 2) NOT NULL
            CONSTRAINT DF_shops_low_stock_threshold DEFAULT (10);

    PRINT 'Column shops.low_stock_threshold added (default 10).';
END
ELSE
BEGIN
    PRINT 'Column shops.low_stock_threshold already exists.';
END
GO

SELECT shop_id, shop_name, low_stock_threshold FROM shops;
GO
