-- Only for the newly created, empty test database. Never run on production.
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
IF EXISTS (SELECT 1 FROM dbo.users) THROW 51001, N'Apply before fixtures only', 1;
-- Existing metadata uses user_id; current CartDAO expects cart_id and decimal quantity.
DROP TABLE dbo.cart_items;
CREATE TABLE dbo.cart_items (
    cart_item_id INT IDENTITY PRIMARY KEY,
    cart_id INT NOT NULL REFERENCES dbo.carts(cart_id) ON DELETE CASCADE,
    variant_id INT NOT NULL REFERENCES dbo.product_variants(variant_id),
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1,
    CONSTRAINT uq_cart_variant UNIQUE(cart_id,variant_id)
);
GO
