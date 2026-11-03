-- OnlineFruitShopping - Complete Merged Schema (SQL Server)
-- Merged and cleaned from provided versions
-- Use in SQL Server Management Studio

USE [master];
GO

IF DB_ID(N'OnlineFruitShopping') IS NULL
    EXEC(N'CREATE DATABASE [OnlineFruitShopping] COLLATE Latin1_General_100_CI_AS');
GO

USE [OnlineFruitShopping];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

BEGIN TRY
    BEGIN TRANSACTION;

-- Core tables
CREATE TABLE dbo.[roles] (
    [role_id] int IDENTITY(1,1) NOT NULL,
    [role_name] nvarchar(50) NOT NULL,
    CONSTRAINT PK_roles PRIMARY KEY ([role_id]),
    CONSTRAINT UQ_roles_role_name UNIQUE ([role_name])
);

CREATE TABLE dbo.[users] (
    [user_id] int IDENTITY(1,1) NOT NULL,
    [full_name] nvarchar(100) NOT NULL,
    [email] nvarchar(150) NOT NULL,
    [phone] nvarchar(20) NULL,
    [password_hash] nvarchar(255) NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_users_status DEFAULT ('active'),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_users_created_at DEFAULT (getdate()),
    [reset_otp] nvarchar(10) NULL,
    [reset_otp_expiry] datetime2 NULL,
    [is_member] bit NOT NULL CONSTRAINT DF_users_is_member DEFAULT (0),
    CONSTRAINT PK_users PRIMARY KEY ([user_id]),
    CONSTRAINT UQ_users_email UNIQUE ([email]),
    CONSTRAINT CK_users_status CHECK ([status] IN ('active','locked','pending'))
);

CREATE TABLE dbo.[user_roles] (
    [user_id] int NOT NULL,
    [role_id] int NOT NULL,
    CONSTRAINT PK_user_roles PRIMARY KEY ([user_id], [role_id]),
    CONSTRAINT FK_user_roles_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]) ON DELETE CASCADE,
    CONSTRAINT FK_user_roles_role_id FOREIGN KEY ([role_id]) REFERENCES dbo.[roles]([role_id]) ON DELETE CASCADE
);

CREATE TABLE dbo.[user_addresses] (
    [address_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [recipient_name] nvarchar(100) NOT NULL,
    [phone] nvarchar(20) NOT NULL,
    [address_line] nvarchar(255) NOT NULL,
    [ward] nvarchar(100) NULL,
    [district] nvarchar(100) NULL,
    [province] nvarchar(100) NULL,
    [is_default] bit NOT NULL CONSTRAINT DF_user_addresses_is_default DEFAULT (0),
    CONSTRAINT PK_user_addresses PRIMARY KEY ([address_id]),
    CONSTRAINT FK_user_addresses_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]) ON DELETE CASCADE
);

CREATE TABLE dbo.[registration_verifications] (
    [verification_id] int IDENTITY(1,1) NOT NULL,
    [full_name] nvarchar(100) NOT NULL,
    [email] nvarchar(255) NOT NULL,
    [password_hash] nvarchar(255) NOT NULL,
    [phone] nvarchar(20) NULL,
    [address] nvarchar(500) NULL,
    [otp] nvarchar(10) NOT NULL,
    [otp_expiry] datetime2 NOT NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_registration_verifications_created_at DEFAULT (getdate()),
    CONSTRAINT PK_registration_verifications PRIMARY KEY ([verification_id])
);

CREATE TABLE dbo.[shops] (
    [shop_id] int IDENTITY(1,1) NOT NULL,
    [owner_id] int NOT NULL,
    [shop_name] nvarchar(150) NOT NULL,
    [description] nvarchar(MAX) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_shops_status DEFAULT ('pending'),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_shops_created_at DEFAULT (getdate()),
    [low_stock_threshold] decimal(18,2) NOT NULL CONSTRAINT DF_shops_low_stock_threshold DEFAULT (10),
    [pickup_address] nvarchar(500) NULL,
    CONSTRAINT PK_shops PRIMARY KEY ([shop_id]),
    CONSTRAINT FK_shops_owner_id FOREIGN KEY ([owner_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_shops_status CHECK ([status] IN ('pending','active','suspended')),
    CONSTRAINT CK_shops_threshold CHECK ([low_stock_threshold] >= 0)
);

CREATE TABLE dbo.[shop_delivery_staff] (
    [shop_id] int NOT NULL,
    [shipper_id] int NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_shop_delivery_staff_status DEFAULT ('Active'),
    [joined_at] datetime2 NOT NULL CONSTRAINT DF_shop_delivery_staff_joined DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_shop_delivery_staff PRIMARY KEY ([shop_id], [shipper_id]),
    CONSTRAINT FK_shop_delivery_staff_shop FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([shop_id]),
    CONSTRAINT FK_shop_delivery_staff_user FOREIGN KEY ([shipper_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_shop_delivery_staff_status CHECK ([status] IN ('Active','Inactive'))
);

CREATE TABLE dbo.[categories] (
    [category_id] int IDENTITY(1,1) NOT NULL,
    [parent_id] int NULL,
    [category_name] nvarchar(100) NOT NULL,
    CONSTRAINT PK_categories PRIMARY KEY ([category_id]),
    CONSTRAINT FK_categories_parent_id FOREIGN KEY ([parent_id]) REFERENCES dbo.[categories]([category_id])
);

CREATE TABLE dbo.[products] (
    [product_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [category_id] int NOT NULL,
    [product_name] nvarchar(150) NOT NULL,
    [description] nvarchar(MAX) NULL,
    [origin] nvarchar(100) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_products_status DEFAULT ('draft'),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_products_created_at DEFAULT (getdate()),
    [commission_rate] decimal(5,2) NOT NULL CONSTRAINT DF_products_commission_rate DEFAULT (0),
    [hidden_by_admin] bit NOT NULL CONSTRAINT DF_products_hidden_by_admin DEFAULT (0),
    CONSTRAINT PK_products PRIMARY KEY ([product_id]),
    CONSTRAINT FK_products_shop_id FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([shop_id]),
    CONSTRAINT FK_products_category_id FOREIGN KEY ([category_id]) REFERENCES dbo.[categories]([category_id]),
    CONSTRAINT CK_products_status CHECK ([status] IN ('draft','published','hidden')),
    CONSTRAINT CK_products_commission_rate CHECK ([commission_rate] BETWEEN 0 AND 100)
);

CREATE TABLE dbo.[product_images] (
    [image_id] int IDENTITY(1,1) NOT NULL,
    [product_id] int NOT NULL,
    [image_url] nvarchar(255) NOT NULL,
    [sort_order] int NOT NULL CONSTRAINT DF_product_images_sort_order DEFAULT (0),
    CONSTRAINT PK_product_images PRIMARY KEY ([image_id]),
    CONSTRAINT FK_product_images_product_id FOREIGN KEY ([product_id]) REFERENCES dbo.[products]([product_id]) ON DELETE CASCADE
);

CREATE TABLE dbo.[product_variants] (
    [variant_id] int IDENTITY(1,1) NOT NULL,
    [product_id] int NOT NULL,
    [sku] nvarchar(50) NOT NULL,
    [unit] nvarchar(10) NOT NULL,
    [weight_value] decimal(10,2) NULL,
    [packaging_type] nvarchar(50) NULL,
    [price] decimal(12,2) NOT NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_product_variants_created_at DEFAULT (getdate()),
    CONSTRAINT PK_product_variants PRIMARY KEY ([variant_id]),
    CONSTRAINT UQ_product_variants_sku UNIQUE ([sku]),
    CONSTRAINT FK_product_variants_product_id FOREIGN KEY ([product_id]) REFERENCES dbo.[products]([product_id]) ON DELETE CASCADE,
    CONSTRAINT CK_product_variants_unit CHECK ([unit] IN ('kg','box','piece')),
    CONSTRAINT CK_product_variants_price CHECK ([price] >= 0 AND ([weight_value] IS NULL OR [weight_value] > 0))
);

CREATE TABLE dbo.[inventory_batches] (
    [batch_id] int IDENTITY(1,1) NOT NULL,
    [variant_id] int NOT NULL,
    [batch_code] nvarchar(50) NULL,
    [quantity] decimal(10,2) NOT NULL CONSTRAINT DF_inventory_batches_quantity DEFAULT (0),
    [expiry_date] date NULL,
    [received_date] date NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_inventory_batches_status DEFAULT ('active'),
    CONSTRAINT PK_inventory_batches PRIMARY KEY ([batch_id]),
    CONSTRAINT FK_inventory_batches_variant_id FOREIGN KEY ([variant_id]) REFERENCES dbo.[product_variants]([variant_id]),
    CONSTRAINT CK_inventory_batches_status CHECK ([status] IN ('active','expired','depleted')),
    CONSTRAINT CK_inventory_batches_quantity CHECK ([quantity] >= 0)
);

CREATE TABLE dbo.[inventory_adjustments] (
    [adjustment_id] int IDENTITY(1,1) NOT NULL,
    [batch_id] int NOT NULL,
    [adjustment_type] nvarchar(20) NOT NULL,
    [quantity] decimal(10,2) NOT NULL,
    [reason] nvarchar(255) NULL,
    [created_by] int NOT NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_inventory_adjustments_created_at DEFAULT (getdate()),
    CONSTRAINT PK_inventory_adjustments PRIMARY KEY ([adjustment_id]),
    CONSTRAINT FK_inventory_adjustments_batch_id FOREIGN KEY ([batch_id]) REFERENCES dbo.[inventory_batches]([batch_id]),
    CONSTRAINT FK_inventory_adjustments_created_by FOREIGN KEY ([created_by]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_inventory_adjustments_type CHECK ([adjustment_type] IN ('import','export','discard')),
    CONSTRAINT CK_inventory_adjustments_quantity CHECK ([quantity] >= 0)
);

CREATE TABLE dbo.[vouchers] (
    [voucher_id] int IDENTITY(1,1) NOT NULL,
    [code] nvarchar(50) NOT NULL,
    [discount_type] nvarchar(20) NOT NULL,
    [discount_value] decimal(12,2) NOT NULL,
    [start_date] datetime2 NOT NULL,
    [end_date] datetime2 NOT NULL,
    [usage_limit] int NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_vouchers_status DEFAULT ('active'),
    CONSTRAINT PK_vouchers PRIMARY KEY ([voucher_id]),
    CONSTRAINT UQ_vouchers_code UNIQUE ([code]),
    CONSTRAINT CK_vouchers_type CHECK ([discount_type] IN ('percent','fixed_amount')),
    CONSTRAINT CK_vouchers_status CHECK ([status] IN ('active','expired','disabled')),
    CONSTRAINT CK_vouchers_policy CHECK ([discount_value] > 0 AND ([discount_type] <> 'percent' OR [discount_value] <= 100) AND [start_date] < [end_date] AND [usage_limit] >= 0)
);

CREATE TABLE dbo.[coupons] (
    [coupon_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [code] nvarchar(30) NOT NULL,
    [coupon_name] nvarchar(150) NOT NULL,
    [description] nvarchar(500) NOT NULL CONSTRAINT DF_coupons_description DEFAULT (''),
    [discount_type] nvarchar(10) NOT NULL,
    [discount_value] decimal(18,2) NOT NULL,
    [max_discount] decimal(18,2) NULL,
    [min_order_amount] decimal(18,2) NOT NULL CONSTRAINT DF_coupons_min DEFAULT (0),
    [total_limit] int NOT NULL,
    [per_customer_limit] int NOT NULL,
    [starts_at] datetime2 NOT NULL,
    [ends_at] datetime2 NOT NULL,
    [is_active] bit NOT NULL CONSTRAINT DF_coupons_active DEFAULT (1),
    [is_deleted] bit NOT NULL CONSTRAINT DF_coupons_deleted DEFAULT (0),
    [version] int NOT NULL CONSTRAINT DF_coupons_version DEFAULT (0),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_coupons_created DEFAULT (SYSDATETIME()),
    [updated_at] datetime2 NOT NULL CONSTRAINT DF_coupons_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_coupons PRIMARY KEY ([coupon_id]),
    CONSTRAINT UQ_coupons_shop_code UNIQUE ([shop_id], [code]),
    CONSTRAINT UQ_coupons_id_shop UNIQUE ([coupon_id], [shop_id]),
    CONSTRAINT FK_coupons_shop FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([shop_id]),
    CONSTRAINT CK_coupons_policy CHECK ([discount_type] IN ('PERCENT','FIXED') AND [discount_value] > 0 AND ([discount_type] <> 'PERCENT' OR [discount_value] <= 100) AND ([discount_type] <> 'FIXED' OR [discount_value] = FLOOR([discount_value])) AND ([max_discount] IS NULL OR ([discount_type] = 'PERCENT' AND [max_discount] > 0)) AND [min_order_amount] >= 0 AND [total_limit] > 0 AND [per_customer_limit] > 0 AND [per_customer_limit] <= [total_limit] AND [starts_at] < [ends_at] AND [version] >= 0),
    CONSTRAINT CK_coupons_deleted CHECK ([is_deleted] = 0 OR [is_active] = 0)
);

CREATE TABLE dbo.[orders] (
    [order_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [shop_id] int NOT NULL,
    [address_id] int NOT NULL,
    [voucher_id] int NULL,
    [coupon_id] int NULL,
    [status] nvarchar(30) NOT NULL CONSTRAINT DF_orders_status DEFAULT ('pending_confirmation'),
    [order_status] nvarchar(30) NULL,
    [total_amount] decimal(18,2) NOT NULL,
    [coupon_subtotal] decimal(18,2) NULL,
    [coupon_discount] decimal(18,2) NOT NULL CONSTRAINT DF_orders_coupon_discount DEFAULT (0),
    [receiver_name] nvarchar(100) NULL,
    [receiver_phone] nvarchar(20) NULL,
    [shipping_address] nvarchar(500) NULL,
    [note] nvarchar(1000) NULL,
    [payment_method] nvarchar(20) NULL,
    [payment_status] nvarchar(20) NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_orders_created_at DEFAULT (getdate()),
    CONSTRAINT PK_orders PRIMARY KEY ([order_id]),
    CONSTRAINT FK_orders_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT FK_orders_shop_id FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([shop_id]),
    CONSTRAINT FK_orders_address_id FOREIGN KEY ([address_id]) REFERENCES dbo.[user_addresses]([address_id]),
    CONSTRAINT FK_orders_voucher_id FOREIGN KEY ([voucher_id]) REFERENCES dbo.[vouchers]([voucher_id]),
    CONSTRAINT FK_orders_coupon_shop FOREIGN KEY ([coupon_id], [shop_id]) REFERENCES dbo.[coupons]([coupon_id], [shop_id]),
    CONSTRAINT CK_orders_status CHECK ([status] IN ('pending_confirmation','preparing','shipping','delivered','cancelled','failed')),
    CONSTRAINT CK_orders_money CHECK ([total_amount] >= 0 AND [coupon_discount] >= 0),
    CONSTRAINT CK_orders_one_discount CHECK ([voucher_id] IS NULL OR [coupon_id] IS NULL),
    CONSTRAINT CK_orders_coupon_total CHECK (([coupon_id] IS NULL AND [coupon_discount] = 0 AND [coupon_subtotal] IS NULL) OR ([coupon_id] IS NOT NULL AND [coupon_subtotal] IS NOT NULL AND [coupon_subtotal] >= [coupon_discount] AND [total_amount] = [coupon_subtotal] - [coupon_discount]))
);

CREATE TABLE dbo.[order_items] (
    [order_item_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [variant_id] int NOT NULL,
    [batch_id] int NULL,
    [quantity] decimal(10,2) NOT NULL,
    [unit_price] decimal(12,2) NOT NULL,
    CONSTRAINT PK_order_items PRIMARY KEY ([order_item_id]),
    CONSTRAINT FK_order_items_order_id FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([order_id]) ON DELETE CASCADE,
    CONSTRAINT FK_order_items_variant_id FOREIGN KEY ([variant_id]) REFERENCES dbo.[product_variants]([variant_id]),
    CONSTRAINT FK_order_items_batch_id FOREIGN KEY ([batch_id]) REFERENCES dbo.[inventory_batches]([batch_id]),
    CONSTRAINT CK_order_items_values CHECK ([quantity] > 0 AND [quantity] = FLOOR([quantity]) AND [unit_price] >= 0)
);

CREATE TABLE dbo.[order_status_history] (
    [history_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [status] nvarchar(255) NOT NULL,
    [changed_by] int NULL,
    [changed_at] datetime2 NOT NULL CONSTRAINT DF_order_status_history_changed_at DEFAULT (getdate()),
    CONSTRAINT PK_order_status_history PRIMARY KEY ([history_id]),
    CONSTRAINT FK_order_status_history_order_id FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([order_id]) ON DELETE CASCADE,
    CONSTRAINT FK_order_status_history_changed_by FOREIGN KEY ([changed_by]) REFERENCES dbo.[users]([user_id])
);

CREATE TABLE dbo.[payments] (
    [payment_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [method] nvarchar(20) NOT NULL,
    [amount] decimal(12,2) NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_payments_status DEFAULT ('pending'),
    [transaction_ref] nvarchar(100) NULL,
    [paid_at] datetime2 NULL,
    CONSTRAINT PK_payments PRIMARY KEY ([payment_id]),
    CONSTRAINT UQ_payments_order_id UNIQUE ([order_id]),
    CONSTRAINT FK_payments_order_id FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([order_id]),
    CONSTRAINT CK_payments_method CHECK ([method] IN ('vnpay','momo','card','cod')),
    CONSTRAINT CK_payments_status CHECK ([status] IN ('pending','paid','failed')),
    CONSTRAINT CK_payments_amount CHECK ([amount] >= 0)
);

CREATE TABLE dbo.[refunds] (
    [refund_id] int IDENTITY(1,1) NOT NULL,
    [payment_id] int NOT NULL,
    [processed_by] int NULL,
    [amount] decimal(12,2) NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_refunds_status DEFAULT ('requested'),
    [reason] nvarchar(255) NULL,
    [requested_at] datetime2 NOT NULL CONSTRAINT DF_refunds_requested_at DEFAULT (getdate()),
    [processed_at] datetime2 NULL,
    CONSTRAINT PK_refunds PRIMARY KEY ([refund_id]),
    CONSTRAINT FK_refunds_payment_id FOREIGN KEY ([payment_id]) REFERENCES dbo.[payments]([payment_id]),
    CONSTRAINT FK_refunds_processed_by FOREIGN KEY ([processed_by]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_refunds_status CHECK ([status] IN ('requested','approved','rejected','completed')),
    CONSTRAINT CK_refunds_amount CHECK ([amount] > 0)
);

CREATE TABLE dbo.[deliveries] (
    [delivery_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [shop_id] int NULL,
    [staff_id] int NULL,
    [shipper_id] int NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_deliveries_status DEFAULT ('assigned'),
    [delivery_status] nvarchar(20) NULL,
    [proof_image_url] nvarchar(255) NULL,
    [note] nvarchar(255) NULL,
    [pickup_time] datetime2 NULL,
    [delivered_time] datetime2 NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_deliveries_created_at DEFAULT (getdate()),
    CONSTRAINT PK_deliveries PRIMARY KEY ([delivery_id]),
    CONSTRAINT FK_deliveries_order_id FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([order_id]),
    CONSTRAINT FK_deliveries_staff_id FOREIGN KEY ([staff_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT FK_deliveries_shipper FOREIGN KEY ([shipper_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT FK_deliveries_shop FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([shop_id]),
    CONSTRAINT CK_deliveries_status CHECK ([status] IN ('assigned','accepted','rejected','picked_up','delivering','delivered','failed'))
);

CREATE TABLE dbo.[delivery_locations] (
    [location_id] int IDENTITY(1,1) NOT NULL,
    [delivery_id] int NOT NULL,
    [latitude] decimal(10,6) NOT NULL,
    [longitude] decimal(10,6) NOT NULL,
    [recorded_at] datetime2 NOT NULL CONSTRAINT DF_delivery_locations_recorded_at DEFAULT (getdate()),
    CONSTRAINT PK_delivery_locations PRIMARY KEY ([location_id]),
    CONSTRAINT FK_delivery_locations_delivery_id FOREIGN KEY ([delivery_id]) REFERENCES dbo.[deliveries]([delivery_id]) ON DELETE CASCADE,
    CONSTRAINT CK_delivery_locations_coords CHECK ([latitude] BETWEEN -90 AND 90 AND [longitude] BETWEEN -180 AND 180)
);

CREATE TABLE dbo.[coupon_usages] (
    [usage_id] int IDENTITY(1,1) NOT NULL,
    [coupon_id] int NOT NULL,
    [shop_id] int NOT NULL,
    [order_id] int NOT NULL,
    [customer_id] int NOT NULL,
    [code_snapshot] nvarchar(30) NOT NULL,
    [subtotal] decimal(18,2) NOT NULL,
    [discount_amount] decimal(18,2) NOT NULL,
    [final_amount] decimal(18,2) NOT NULL,
    [used_at] datetime2 NOT NULL CONSTRAINT DF_coupon_usages_used DEFAULT (SYSDATETIME()),
    [usage_status] nvarchar(20) NOT NULL CONSTRAINT DF_coupon_usages_status DEFAULT ('Applied'),
    [removed_at] datetime2 NULL,
    CONSTRAINT PK_coupon_usages PRIMARY KEY ([usage_id]),
    CONSTRAINT FK_coupon_usages_coupon FOREIGN KEY ([coupon_id], [shop_id]) REFERENCES dbo.[coupons]([coupon_id], [shop_id]),
    CONSTRAINT FK_coupon_usages_order FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([order_id]),
    CONSTRAINT FK_coupon_usages_customer FOREIGN KEY ([customer_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_coupon_usages_money CHECK ([subtotal] > 0 AND [discount_amount] >= 0 AND [discount_amount] <= [subtotal] AND [final_amount] = [subtotal] - [discount_amount]),
    CONSTRAINT CK_coupon_usages_state CHECK (([usage_status] = 'Applied' AND [removed_at] IS NULL) OR ([usage_status] = 'Removed' AND [removed_at] IS NOT NULL))
);

CREATE TABLE dbo.[promotions] (
    [promotion_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [promotion_name] nvarchar(150) NOT NULL,
    [description] nvarchar(500) NOT NULL CONSTRAINT DF_promotions_description DEFAULT (''),
    [discount_type] nvarchar(10) NOT NULL,
    [discount_value] decimal(18,2) NOT NULL,
    [starts_at] datetime2 NOT NULL,
    [ends_at] datetime2 NOT NULL,
    [is_active] bit NOT NULL CONSTRAINT DF_promotions_active DEFAULT (0),
    [is_deleted] bit NOT NULL CONSTRAINT DF_promotions_deleted DEFAULT (0),
    [version] int NOT NULL CONSTRAINT DF_promotions_version DEFAULT (0),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_promotions_created DEFAULT (SYSDATETIME()),
    [updated_at] datetime2 NOT NULL CONSTRAINT DF_promotions_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_promotions PRIMARY KEY ([promotion_id]),
    CONSTRAINT FK_promotions_shop FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([shop_id]),
    CONSTRAINT CK_promotions_policy CHECK ([discount_type] IN ('PERCENT','FIXED') AND [discount_value] > 0 AND ([discount_type] <> 'PERCENT' OR [discount_value] <= 100) AND ([discount_type] <> 'FIXED' OR [discount_value] = FLOOR([discount_value])) AND [starts_at] < [ends_at] AND [version] >= 0),
    CONSTRAINT CK_promotions_deleted CHECK ([is_deleted] = 0 OR [is_active] = 0)
);

CREATE TABLE dbo.[promotion_products] (
    [promotion_id] int NOT NULL,
    [product_id] int NOT NULL,
    CONSTRAINT PK_promotion_products PRIMARY KEY ([promotion_id], [product_id]),
    CONSTRAINT FK_promotion_products_promotion FOREIGN KEY ([promotion_id]) REFERENCES dbo.[promotions]([promotion_id]) ON DELETE CASCADE,
    CONSTRAINT FK_promotion_products_product FOREIGN KEY ([product_id]) REFERENCES dbo.[products]([product_id])
);

CREATE TABLE dbo.[variant_promotions] (
    [promo_id] int IDENTITY(1,1) NOT NULL,
    [variant_id] int NOT NULL,
    [promo_price] decimal(18,2) NOT NULL,
    [start_date] date NOT NULL,
    [end_date] date NOT NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_variant_promotions_created_at DEFAULT (SYSDATETIME()),
    [updated_at] datetime2 NOT NULL CONSTRAINT DF_variant_promotions_updated_at DEFAULT (SYSDATETIME()),
    [status] varchar(20) NOT NULL CONSTRAINT DF_variant_promotions_status DEFAULT ('pending'),
    [review_note] nvarchar(255) NULL,
    [reviewed_by] int NULL,
    [reviewed_at] datetime2 NULL,
    CONSTRAINT PK_variant_promotions PRIMARY KEY ([promo_id]),
    CONSTRAINT FK_variant_promotions_variant FOREIGN KEY ([variant_id]) REFERENCES dbo.[product_variants]([variant_id]),
    CONSTRAINT FK_variant_promotions_reviewer FOREIGN KEY ([reviewed_by]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_variant_promotions_values CHECK ([promo_price] >= 0 AND [start_date] <= [end_date] AND [status] IN ('pending','approved','rejected'))
);

CREATE TABLE dbo.[loyalty_points] (
    [point_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [points] int NOT NULL,
    [source_order_id] int NULL,
    [type] nvarchar(10) NOT NULL,
    [created_at] datetime2 NULL CONSTRAINT DF_loyalty_points_created_at DEFAULT (getdate()),
    CONSTRAINT PK_loyalty_points PRIMARY KEY ([point_id]),
    CONSTRAINT FK_loyalty_points_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT FK_loyalty_points_source_order_id FOREIGN KEY ([source_order_id]) REFERENCES dbo.[orders]([order_id]),
    CONSTRAINT CK_loyalty_points_type CHECK ([type] IN ('earn','redeem'))
);

CREATE TABLE dbo.[notifications] (
    [notification_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [type] nvarchar(50) NULL,
    [title] nvarchar(200) NULL,
    [content] nvarchar(500) NULL,
    [message] nvarchar(500) NULL,
    [reference_type] nvarchar(50) NULL,
    [reference_id] int NULL,
    [channel] nvarchar(20) NOT NULL CONSTRAINT DF_notifications_channel DEFAULT ('in_app'),
    [is_read] bit NOT NULL CONSTRAINT DF_notifications_is_read DEFAULT (0),
    [is_important] bit NOT NULL CONSTRAINT DF_notifications_is_important DEFAULT (0),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_notifications_created_at DEFAULT (getdate()),
    [read_at] datetime2 NULL,
    CONSTRAINT PK_notifications PRIMARY KEY ([notification_id]),
    CONSTRAINT FK_notifications_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]) ON DELETE CASCADE,
    CONSTRAINT CK_notifications_channel CHECK ([channel] IN ('in_app','email','both'))
);

CREATE TABLE dbo.[reviews] (
    [review_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [product_id] int NOT NULL,
    [order_id] int NOT NULL,
    [rating] tinyint NOT NULL,
    [title] nvarchar(255) NULL,
    [comment] nvarchar(MAX) NULL,
    [image_urls] nvarchar(MAX) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_reviews_status DEFAULT ('published'),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_reviews_created_at DEFAULT (getdate()),
    [deleted_at] datetime2 NULL,
    CONSTRAINT PK_reviews PRIMARY KEY ([review_id]),
    CONSTRAINT UQ_reviews_order_product UNIQUE ([order_id], [product_id]),
    CONSTRAINT FK_reviews_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT FK_reviews_product_id FOREIGN KEY ([product_id]) REFERENCES dbo.[products]([product_id]),
    CONSTRAINT FK_reviews_order_id FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([order_id]),
    CONSTRAINT CK_reviews_rating CHECK ([rating] BETWEEN 1 AND 5),
    CONSTRAINT CK_reviews_status CHECK ([status] IN ('published','reported','rejected','deleted'))
);

CREATE TABLE dbo.[review_reports] (
    [report_id] int IDENTITY(1,1) NOT NULL,
    [review_id] int NOT NULL,
    [reporter_id] int NOT NULL,
    [reason] nvarchar(50) NOT NULL,
    [note] nvarchar(500) NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_review_reports_created_at DEFAULT (getdate()),
    CONSTRAINT PK_review_reports PRIMARY KEY ([report_id]),
    CONSTRAINT UQ_review_reports_per_user UNIQUE ([review_id], [reporter_id]),
    CONSTRAINT FK_review_reports_review_id FOREIGN KEY ([review_id]) REFERENCES dbo.[reviews]([review_id]),
    CONSTRAINT FK_review_reports_reporter_id FOREIGN KEY ([reporter_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_review_reports_reason CHECK ([reason] IN ('spam','inappropriate_language','fake_review','other'))
);

CREATE TABLE dbo.[subscription_plans] (
    [plan_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [name] nvarchar(100) NOT NULL,
    [description] nvarchar(500) NULL,
    [price] decimal(10,2) NOT NULL,
    [billing_cycle] nvarchar(20) NOT NULL,
    [duration_days] int NOT NULL,
    [benefits] nvarchar(MAX) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_subscription_plans_status DEFAULT ('draft'),
    [created_by] int NOT NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_subscription_plans_created_at DEFAULT (getdate()),
    [updated_at] datetime2 NOT NULL CONSTRAINT DF_subscription_plans_updated_at DEFAULT (getdate()),
    [deleted_at] datetime2 NULL,
    CONSTRAINT PK_subscription_plans PRIMARY KEY ([plan_id]),
    CONSTRAINT FK_subscription_plans_shop_id FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([shop_id]),
    CONSTRAINT FK_subscription_plans_created_by FOREIGN KEY ([created_by]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT CK_subscription_plans_billing_cycle CHECK ([billing_cycle] IN ('weekly','monthly','quarterly','yearly')),
    CONSTRAINT CK_subscription_plans_price CHECK ([price] >= 0),
    CONSTRAINT CK_subscription_plans_status CHECK ([status] IN ('draft','active','inactive'))
);

CREATE TABLE dbo.[subscription_plan_items] (
    [item_id] int IDENTITY(1,1) NOT NULL,
    [plan_id] int NOT NULL,
    [variant_id] int NOT NULL,
    [quantity] int NOT NULL CONSTRAINT DF_subscription_plan_items_quantity DEFAULT (1),
    CONSTRAINT PK_subscription_plan_items PRIMARY KEY ([item_id]),
    CONSTRAINT UQ_subscription_plan_items_plan_variant UNIQUE ([plan_id], [variant_id]),
    CONSTRAINT FK_subscription_plan_items_plan_id FOREIGN KEY ([plan_id]) REFERENCES dbo.[subscription_plans]([plan_id]),
    CONSTRAINT FK_subscription_plan_items_variant_id FOREIGN KEY ([variant_id]) REFERENCES dbo.[product_variants]([variant_id]),
    CONSTRAINT CK_subscription_plan_items_quantity CHECK ([quantity] > 0)
);

CREATE TABLE dbo.[subscriptions] (
    [subscription_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [plan_id] int NULL,
    [variant_id] int NULL,
    [is_custom] bit NOT NULL CONSTRAINT DF_subscriptions_is_custom DEFAULT (0),
    [frequency] nvarchar(10) NOT NULL,
    [price_snapshot] decimal(10,2) NULL,
    [next_delivery_date] date NOT NULL,
    [end_date] date NULL,
    [notes] nvarchar(1000) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT DF_subscriptions_status DEFAULT ('active'),
    [created_at] datetime2 NOT NULL CONSTRAINT DF_subscriptions_created_at DEFAULT (getdate()),
    CONSTRAINT PK_subscriptions PRIMARY KEY ([subscription_id]),
    CONSTRAINT FK_subscriptions_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]),
    CONSTRAINT FK_subscriptions_plan_id FOREIGN KEY ([plan_id]) REFERENCES dbo.[subscription_plans]([plan_id]),
    CONSTRAINT FK_subscriptions_variant_id FOREIGN KEY ([variant_id]) REFERENCES dbo.[product_variants]([variant_id]),
    CONSTRAINT CK_subscriptions_frequency CHECK ([frequency] IN ('weekly','monthly','quarterly','yearly')),
    CONSTRAINT CK_subscriptions_status CHECK ([status] IN ('active','paused','cancelled'))
);

CREATE TABLE dbo.[carts] (
    [cart_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [created_at] datetime2 NOT NULL CONSTRAINT DF_carts_created_at DEFAULT (getdate()),
    CONSTRAINT PK_carts PRIMARY KEY ([cart_id]),
    CONSTRAINT UQ_carts_user_id UNIQUE ([user_id]),
    CONSTRAINT FK_carts_user_id FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([user_id]) ON DELETE CASCADE
);

CREATE TABLE dbo.[cart_items] (
    [cart_item_id] int IDENTITY(1,1) NOT NULL,
    [cart_id] int NOT NULL,
    [variant_id] int NOT NULL,
    [quantity] decimal(10,2) NOT NULL CONSTRAINT DF_cart_items_quantity DEFAULT (1),
    CONSTRAINT PK_cart_items PRIMARY KEY ([cart_item_id]),
    CONSTRAINT UQ_cart_items_cart_variant UNIQUE ([cart_id], [variant_id]),
    CONSTRAINT FK_cart_items_cart_id FOREIGN KEY ([cart_id]) REFERENCES dbo.[carts]([cart_id]) ON DELETE CASCADE,
    CONSTRAINT FK_cart_items_variant_id FOREIGN KEY ([variant_id]) REFERENCES dbo.[product_variants]([variant_id]),
    CONSTRAINT CK_cart_items_quantity CHECK ([quantity] > 0 AND [quantity] = FLOOR([quantity]))
);

-- Indexes
CREATE UNIQUE INDEX UX_one_active_delivery ON dbo.[deliveries]([order_id]) WHERE [status] IN ('assigned','accepted','picked_up','delivering');
CREATE UNIQUE INDEX UX_one_applied_coupon ON dbo.[coupon_usages]([order_id]) WHERE [usage_status] = 'Applied';
CREATE INDEX IX_deliveries_shipper_state ON dbo.[deliveries]([shipper_id], [delivery_status], [delivery_id] DESC);
CREATE INDEX IX_deliveries_order ON dbo.[deliveries]([order_id], [delivery_id] DESC);
CREATE INDEX IX_orders_user_date ON dbo.[orders]([user_id], [created_at] DESC);
CREATE INDEX IX_orders_shop_state ON dbo.[orders]([shop_id], [status], [created_at] DESC);
CREATE INDEX IX_order_items_order ON dbo.[order_items]([order_id]);
CREATE INDEX IX_inventory_variant ON dbo.[inventory_batches]([variant_id], [status], [expiry_date]);
CREATE INDEX IX_products_shop ON dbo.[products]([shop_id], [status]);
CREATE INDEX IX_variants_product ON dbo.[product_variants]([product_id]);
CREATE INDEX IX_notifications_user ON dbo.[notifications]([user_id], [is_read], [created_at] DESC);
CREATE INDEX IX_order_status_history_order ON dbo.[order_status_history]([order_id], [changed_at] DESC);
CREATE INDEX IX_coupon_usages_coupon_customer ON dbo.[coupon_usages]([coupon_id], [customer_id], [usage_status]);
CREATE INDEX IX_promotions_shop_time ON dbo.[promotions]([shop_id], [is_active], [starts_at], [ends_at]);
CREATE INDEX IX_coupons_shop_time ON dbo.[coupons]([shop_id], [is_active], [starts_at], [ends_at]);

COMMIT TRANSACTION;
PRINT N'Schema merged successfully created.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
