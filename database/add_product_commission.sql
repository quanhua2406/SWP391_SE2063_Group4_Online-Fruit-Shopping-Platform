-- =============================================================
-- FreshFruit - Per-product commission rate
-- =============================================================
-- Adds a single column to dbo.products:
--
--   commission_rate : percentage (0 - 100) that the platform keeps
--                     on each unit sold for that product.
--
-- The column is additive, nullable-safe and idempotent, so it can
-- be run on an existing database without touching any other data.
-- =============================================================

IF NOT EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.products')
      AND name = 'commission_rate'
)
BEGIN
    ALTER TABLE dbo.products
    ADD commission_rate DECIMAL(5, 2) NOT NULL
        CONSTRAINT DF_products_commission_rate DEFAULT 0;
END
GO
