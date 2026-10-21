-- =============================================================
-- 18.0 Shop Owner Management (admin)
--
-- Blocking a shop owner takes the shop off sale: the shop becomes
-- 'suspended' and every published product is hidden. Unblocking
-- must restore exactly the products that the block hid, and leave
-- alone the ones the shop owner had hidden by themselves (or that
-- were never on sale).
--
-- This column marks the products hidden by the administrator so
-- that unblocking can tell the two cases apart.
-- =============================================================

IF COL_LENGTH('dbo.products', 'hidden_by_admin') IS NULL
BEGIN
    ALTER TABLE dbo.products
        ADD hidden_by_admin BIT NOT NULL
            CONSTRAINT DF_products_hidden_by_admin DEFAULT 0;
END
GO
