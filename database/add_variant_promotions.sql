-- =============================================================
-- FreshFruit - add variant_promotions table
-- =============================================================
-- Shop owners can now set a promotional price for a variant:
-- a lower price that only applies inside a start/end date range.
--
-- One active promotion row per variant (UNIQUE on variant_id),
-- so "set promotional price" is an insert-or-replace operation.
--
-- The script is idempotent: it only creates the table when it is
-- missing.
-- =============================================================

SET NOCOUNT ON;
GO

IF OBJECT_ID('variant_promotions', 'U') IS NULL
BEGIN
    CREATE TABLE variant_promotions
    (
        promo_id    INT IDENTITY (1, 1) NOT NULL
            CONSTRAINT PK_variant_promotions PRIMARY KEY,
        variant_id  INT                 NOT NULL,
        promo_price DECIMAL(18, 2)      NOT NULL,
        start_date  DATE                NOT NULL,
        end_date    DATE                NOT NULL,
        created_at  DATETIME2           NOT NULL
            CONSTRAINT DF_variant_promotions_created DEFAULT (SYSDATETIME()),
        updated_at  DATETIME2           NOT NULL
            CONSTRAINT DF_variant_promotions_updated DEFAULT (SYSDATETIME()),
        CONSTRAINT UQ_variant_promotions_variant UNIQUE (variant_id),
        CONSTRAINT FK_variant_promotions_variant
            FOREIGN KEY (variant_id) REFERENCES product_variants (variant_id),
        CONSTRAINT CK_variant_promotions_price CHECK (promo_price > 0),
        CONSTRAINT CK_variant_promotions_dates CHECK (end_date >= start_date)
    );

    PRINT 'Table variant_promotions created.';
END
ELSE
BEGIN
    PRINT 'Table variant_promotions already exists.';
END
GO

SELECT promo_id, variant_id, promo_price, start_date, end_date
FROM variant_promotions;
GO
