-- =============================================================
-- FreshFruit - Pricing by price/kg x quality grade + price history
-- =============================================================
-- 1. products.price_per_kg        : base price per kg, set by the shop owner
-- 2. product_variants.quality_grade: '1' / '2' / '3' (Loai 1 / 2 / 3)
-- 3. quality_grade_factors        : multiplier per grade
--        variant price = price_per_kg x weight_value x factor
-- 4. variant_price_history        : every change of a variant price
-- Idempotent: safe to run more than once.
-- =============================================================

SET NOCOUNT ON;
GO

IF COL_LENGTH('products', 'price_per_kg') IS NULL
    ALTER TABLE products ADD price_per_kg DECIMAL(12,2) NULL;
GO

IF COL_LENGTH('product_variants', 'quality_grade') IS NULL
    ALTER TABLE product_variants
        ADD quality_grade VARCHAR(5) NOT NULL
            CONSTRAINT DF_product_variants_grade DEFAULT ('1');
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name = 'CK_product_variants_grade')
    EXEC('ALTER TABLE product_variants
              ADD CONSTRAINT CK_product_variants_grade
              CHECK (quality_grade IN (''1'', ''2'', ''3''))');
GO

IF OBJECT_ID('quality_grade_factors', 'U') IS NULL
BEGIN
    CREATE TABLE quality_grade_factors (
        grade   VARCHAR(5)     NOT NULL CONSTRAINT PK_quality_grade_factors PRIMARY KEY,
        label   NVARCHAR(50)   NOT NULL,
        factor  DECIMAL(5,3)   NOT NULL,
        CONSTRAINT CK_quality_grade_factors_factor CHECK (factor > 0 AND factor <= 1.5)
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM quality_grade_factors WHERE grade = '1')
    INSERT INTO quality_grade_factors (grade, label, factor)
    VALUES ('1', N'Loại 1', 1.000), ('2', N'Loại 2', 0.850), ('3', N'Loại 3', 0.700);
GO

IF OBJECT_ID('variant_price_history', 'U') IS NULL
BEGIN
    CREATE TABLE variant_price_history (
        history_id  INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_variant_price_history PRIMARY KEY,
        variant_id  INT               NOT NULL,
        old_price   DECIMAL(12,2)     NULL,
        new_price   DECIMAL(12,2)     NOT NULL,
        reason      VARCHAR(30)       NOT NULL,
        changed_by  INT               NULL,
        changed_at  DATETIME2         NOT NULL CONSTRAINT DF_variant_price_history_at DEFAULT (SYSDATETIME()),
        CONSTRAINT FK_variant_price_history_variant
            FOREIGN KEY (variant_id) REFERENCES product_variants (variant_id) ON DELETE CASCADE
    );
END;
GO
