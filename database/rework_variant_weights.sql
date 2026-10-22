-- =============================================================
-- FreshFruit - variants become weight packages
-- =============================================================
-- A variant is no longer "1KG versus BOX". It is one selling
-- package defined by its weight, so a fruit like Apple is sold as
--   0.5 kg (half a kilo), 1 kg, 2 kg and a 5 kg carton.
--
--   unit          -> always 'kg', the unit the weight is measured in
--   weight_value  -> weight of the package (0.5, 1, 2, 5 ...)
--   packaging_type -> only the label of the package ("Bag 1kg")
--   sku           -> product + weight, e.g. APPLE-0.5KG
--
-- The script is idempotent: it only renames / inserts what is
-- still missing.
-- =============================================================

SET NOCOUNT ON;
GO

-- ---- 1 kg bag (the variant that already exists) ----------------

UPDATE product_variants
SET unit = 'kg',
    weight_value = 1.00,
    packaging_type = 'Bag 1kg'
WHERE sku = 'APPLE-1KG';
GO

-- ---- the old "box" was a 5 kg carton ---------------------------

UPDATE product_variants
SET unit = 'kg',
    weight_value = 5.00,
    packaging_type = 'Carton 5kg'
WHERE sku = 'APPLE-BOX';
GO

IF NOT EXISTS (SELECT 1 FROM product_variants WHERE sku = 'APPLE-5KG')
BEGIN
    UPDATE product_variants
    SET sku = 'APPLE-5KG'
    WHERE sku = 'APPLE-BOX';
END
GO

-- ---- the missing weight options -------------------------------

INSERT INTO product_variants
    (product_id, sku, unit, weight_value, packaging_type, price)
SELECT v.product_id, 'APPLE-0.5KG', 'kg', 0.50, 'Bag 500g', 28000
FROM product_variants v
WHERE v.sku = 'APPLE-1KG'
  AND NOT EXISTS (SELECT 1 FROM product_variants x
                  WHERE x.sku = 'APPLE-0.5KG');
GO

INSERT INTO product_variants
    (product_id, sku, unit, weight_value, packaging_type, price)
SELECT v.product_id, 'APPLE-2KG', 'kg', 2.00, 'Bag 2kg', 95000
FROM product_variants v
WHERE v.sku = 'APPLE-1KG'
  AND NOT EXISTS (SELECT 1 FROM product_variants x
                  WHERE x.sku = 'APPLE-2KG');
GO

-- ---- stock for the new packages -------------------------------

INSERT INTO inventory_batches
    (variant_id, batch_code, quantity, received_date, expiry_date, status)
SELECT v.variant_id, 'BATCH-005', 40, '2026-10-15', '2026-12-31', 'active'
FROM product_variants v
WHERE v.sku = 'APPLE-0.5KG'
  AND NOT EXISTS (SELECT 1 FROM inventory_batches b
                  WHERE b.batch_code = 'BATCH-005');
GO

INSERT INTO inventory_batches
    (variant_id, batch_code, quantity, received_date, expiry_date, status)
SELECT v.variant_id, 'BATCH-006', 25, '2026-10-15', '2026-12-20', 'active'
FROM product_variants v
WHERE v.sku = 'APPLE-2KG'
  AND NOT EXISTS (SELECT 1 FROM inventory_batches b
                  WHERE b.batch_code = 'BATCH-006');
GO

-- ---- a package weight is always positive ----------------------

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name = 'CK_product_variants_weight')
BEGIN
    ALTER TABLE product_variants
        ADD CONSTRAINT CK_product_variants_weight
            CHECK (weight_value IS NULL OR weight_value > 0);
END
GO

SELECT variant_id, product_id, sku, unit, weight_value,
       packaging_type, price
FROM product_variants
ORDER BY product_id, weight_value;
GO

SELECT b.batch_id, b.variant_id, v.sku, b.batch_code,
       b.quantity, b.expiry_date, b.status
FROM inventory_batches b
JOIN product_variants v ON v.variant_id = b.variant_id
ORDER BY b.batch_id;
GO
