-- Full schema of OnlineFruitShopping (exported from the current database).
-- Structure only: contains no data. Run on a fresh SQL Server instance:
--   1. CREATE DATABASE OnlineFruitShopping;
--   2. sqlcmd -S localhost -d OnlineFruitShopping -i database\full_schema_current.sql
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;
GO
CREATE TABLE [dbo].[cart_items] (
    [cart_item_id] int IDENTITY(1,1) NOT NULL,
    [cart_id] int NOT NULL,
    [variant_id] int NOT NULL,
    [quantity] decimal(10,2) NOT NULL CONSTRAINT [DF_cart_items_quantity] DEFAULT ((1))
);
GO
CREATE TABLE [dbo].[carts] (
    [cart_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_carts_created_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[categories] (
    [category_id] int IDENTITY(1,1) NOT NULL,
    [parent_id] int NULL,
    [category_name] nvarchar(100) NOT NULL,
    [shop_id] int NULL
);
GO
CREATE TABLE [dbo].[commission_settlements] (
    [settlement_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [period_month] char(7) NOT NULL,
    [amount] decimal(12,0) NOT NULL,
    [issued_date] date NOT NULL,
    [due_date] date NOT NULL,
    [status] nvarchar(10) NOT NULL CONSTRAINT [DF_commission_settlement_status] DEFAULT ('unpaid'),
    [shop_locked] bit NOT NULL CONSTRAINT [DF_commission_settlement_locked] DEFAULT ((0)),
    [transaction_ref] nvarchar(100) NULL,
    [paid_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[coupon_usages] (
    [usage_id] int IDENTITY(1,1) NOT NULL,
    [coupon_id] int NOT NULL,
    [shop_id] int NOT NULL,
    [order_id] int NOT NULL,
    [customer_id] int NOT NULL,
    [code_snapshot] nvarchar(30) NOT NULL,
    [subtotal] decimal(18,2) NOT NULL,
    [discount_amount] decimal(18,2) NOT NULL,
    [final_amount] decimal(18,2) NOT NULL,
    [used_at] datetime2(7) NOT NULL CONSTRAINT [DF_coupon_usages_used] DEFAULT (sysdatetime()),
    [usage_status] nvarchar(20) NOT NULL CONSTRAINT [DF_coupon_usages_status] DEFAULT ('Applied'),
    [removed_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[coupons] (
    [coupon_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [code] nvarchar(30) NOT NULL,
    [coupon_name] nvarchar(150) NOT NULL,
    [description] nvarchar(500) NOT NULL CONSTRAINT [DF_coupons_description] DEFAULT (''),
    [discount_type] nvarchar(10) NOT NULL,
    [discount_value] decimal(18,2) NOT NULL,
    [max_discount] decimal(18,2) NULL,
    [min_order_amount] decimal(18,2) NOT NULL CONSTRAINT [DF_coupons_min] DEFAULT ((0)),
    [total_limit] int NOT NULL,
    [per_customer_limit] int NOT NULL,
    [starts_at] datetime2(7) NOT NULL,
    [ends_at] datetime2(7) NOT NULL,
    [is_active] bit NOT NULL CONSTRAINT [DF_coupons_active] DEFAULT ((1)),
    [is_deleted] bit NOT NULL CONSTRAINT [DF_coupons_deleted] DEFAULT ((0)),
    [version] int NOT NULL CONSTRAINT [DF_coupons_version] DEFAULT ((0)),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_coupons_created] DEFAULT (sysdatetime()),
    [updated_at] datetime2(7) NOT NULL CONSTRAINT [DF_coupons_updated] DEFAULT (sysdatetime())
);
GO
CREATE TABLE [dbo].[deliveries] (
    [delivery_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [shop_id] int NULL,
    [staff_id] int NULL,
    [shipper_id] int NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_deliveries_status] DEFAULT ('assigned'),
    [delivery_status] nvarchar(20) NULL,
    [proof_image_url] nvarchar(255) NULL,
    [note] nvarchar(255) NULL,
    [pickup_time] datetime2(7) NULL,
    [delivered_time] datetime2(7) NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_deliveries_created_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[delivery_attempts] (
    [attempt_id] int IDENTITY(1,1) NOT NULL,
    [delivery_id] int NOT NULL,
    [shipper_id] int NOT NULL,
    [assigned_by] int NOT NULL,
    [delivery_status] nvarchar(20) NOT NULL CONSTRAINT [DF_delivery_attempts_status] DEFAULT (N'Assigned'),
    [assigned_at] datetime2(7) NOT NULL CONSTRAINT [DF_delivery_attempts_assigned] DEFAULT (sysdatetime()),
    [accepted_at] datetime2(7) NULL,
    [pickup_time] datetime2(7) NULL,
    [delivered_time] datetime2(7) NULL,
    [closed_at] datetime2(7) NULL,
    [receiver_name] nvarchar(100) NULL,
    [receiver_phone] nvarchar(20) NULL,
    [shipping_address] nvarchar(1000) NULL,
    [pickup_address] nvarchar(500) NULL,
    [note] nvarchar(255) NULL,
    [proof_image_url] nvarchar(255) NULL,
    [version] int NOT NULL CONSTRAINT [DF_delivery_attempts_version] DEFAULT ((0))
);
GO
CREATE TABLE [dbo].[delivery_locations] (
    [location_id] int IDENTITY(1,1) NOT NULL,
    [delivery_id] int NOT NULL,
    [latitude] decimal(10,6) NOT NULL,
    [longitude] decimal(10,6) NOT NULL,
    [recorded_at] datetime2(7) NOT NULL CONSTRAINT [DF_delivery_locations_recorded_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[inventory_adjustments] (
    [adjustment_id] int IDENTITY(1,1) NOT NULL,
    [batch_id] int NOT NULL,
    [adjustment_type] nvarchar(20) NOT NULL,
    [quantity] decimal(10,2) NOT NULL,
    [reason] nvarchar(255) NULL,
    [created_by] int NOT NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_inventory_adjustments_created_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[inventory_batches] (
    [batch_id] int IDENTITY(1,1) NOT NULL,
    [variant_id] int NOT NULL,
    [batch_code] nvarchar(50) NULL,
    [quantity] decimal(10,2) NOT NULL CONSTRAINT [DF_inventory_batches_quantity] DEFAULT ((0)),
    [expiry_date] date NULL,
    [received_date] date NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_inventory_batches_status] DEFAULT ('active')
);
GO
CREATE TABLE [dbo].[inventory_discard_requests] (
    [request_id] int IDENTITY(1,1) NOT NULL,
    [batch_id] int NOT NULL,
    [requested_by] int NOT NULL,
    [status] varchar(10) NOT NULL CONSTRAINT [DF_inventory_discard_status] DEFAULT ('pending'),
    [requested_at] datetime2(7) NOT NULL CONSTRAINT [DF_inventory_discard_requested] DEFAULT (sysdatetime()),
    [resolved_by] int NULL,
    [resolved_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[loyalty_points] (
    [point_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [points] int NOT NULL,
    [source_order_id] int NULL,
    [type] nvarchar(10) NOT NULL,
    [created_at] datetime2(7) NULL CONSTRAINT [DF_loyalty_points_created_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[notifications] (
    [notification_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [type] nvarchar(50) NULL,
    [title] nvarchar(200) NULL,
    [content] nvarchar(500) NULL,
    [message] nvarchar(500) NULL,
    [reference_type] nvarchar(50) NULL,
    [reference_id] int NULL,
    [channel] nvarchar(20) NOT NULL CONSTRAINT [DF_notifications_channel] DEFAULT ('in_app'),
    [is_read] bit NOT NULL CONSTRAINT [DF_notifications_is_read] DEFAULT ((0)),
    [is_important] bit NOT NULL CONSTRAINT [DF_notifications_is_important] DEFAULT ((0)),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_notifications_created_at] DEFAULT (getdate()),
    [read_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[order_items] (
    [order_item_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [variant_id] int NOT NULL,
    [batch_id] int NULL,
    [quantity] decimal(10,2) NOT NULL,
    [unit_price] decimal(12,2) NOT NULL
);
GO
CREATE TABLE [dbo].[order_status_history] (
    [history_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [status] nvarchar(255) NOT NULL,
    [changed_by] int NULL,
    [changed_at] datetime2(7) NOT NULL CONSTRAINT [DF_order_status_history_changed_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[orders] (
    [order_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [shop_id] int NOT NULL,
    [address_id] int NOT NULL,
    [voucher_id] int NULL,
    [coupon_id] int NULL,
    [status] nvarchar(30) NOT NULL CONSTRAINT [DF_orders_status] DEFAULT ('pending_confirmation'),
    [order_status] nvarchar(30) NULL,
    [total_amount] decimal(18,2) NOT NULL,
    [coupon_subtotal] decimal(18,2) NULL,
    [coupon_discount] decimal(18,2) NOT NULL CONSTRAINT [DF_orders_coupon_discount] DEFAULT ((0)),
    [receiver_name] nvarchar(100) NULL,
    [receiver_phone] nvarchar(20) NULL,
    [shipping_address] nvarchar(500) NULL,
    [note] nvarchar(1000) NULL,
    [payment_method] nvarchar(20) NULL,
    [payment_status] nvarchar(20) NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_orders_created_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[payments] (
    [payment_id] int IDENTITY(1,1) NOT NULL,
    [order_id] int NOT NULL,
    [method] nvarchar(20) NOT NULL,
    [amount] decimal(12,2) NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_payments_status] DEFAULT ('pending'),
    [transaction_ref] nvarchar(100) NULL,
    [paid_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[product_images] (
    [image_id] int IDENTITY(1,1) NOT NULL,
    [product_id] int NOT NULL,
    [image_url] nvarchar(255) NOT NULL,
    [sort_order] int NOT NULL CONSTRAINT [DF_product_images_sort_order] DEFAULT ((0))
);
GO
CREATE TABLE [dbo].[product_variants] (
    [variant_id] int IDENTITY(1,1) NOT NULL,
    [product_id] int NOT NULL,
    [sku] nvarchar(50) NOT NULL,
    [unit] nvarchar(10) NOT NULL,
    [weight_value] decimal(10,2) NULL,
    [packaging_type] nvarchar(50) NULL,
    [price] decimal(12,2) NOT NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_product_variants_created_at] DEFAULT (getdate()),
    [quality_grade] varchar(5) NOT NULL CONSTRAINT [DF_product_variants_grade] DEFAULT ('1')
);
GO
CREATE TABLE [dbo].[products] (
    [product_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [category_id] int NOT NULL,
    [product_name] nvarchar(150) NOT NULL,
    [description] nvarchar(max) NULL,
    [origin] nvarchar(100) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_products_status] DEFAULT ('draft'),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_products_created_at] DEFAULT (getdate()),
    [commission_rate] decimal(5,2) NOT NULL CONSTRAINT [DF_products_commission_rate] DEFAULT ((0)),
    [hidden_by_admin] bit NOT NULL CONSTRAINT [DF_products_hidden_by_admin] DEFAULT ((0)),
    [price_per_kg] decimal(12,2) NULL,
    [hidden_by_commission] bit NOT NULL CONSTRAINT [DF_products_hidden_by_commission] DEFAULT ((0))
);
GO
CREATE TABLE [dbo].[promotion_products] (
    [promotion_id] int NOT NULL,
    [product_id] int NOT NULL
);
GO
CREATE TABLE [dbo].[promotions] (
    [promotion_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [promotion_name] nvarchar(150) NOT NULL,
    [description] nvarchar(500) NOT NULL CONSTRAINT [DF_promotions_description] DEFAULT (''),
    [discount_type] nvarchar(10) NOT NULL,
    [discount_value] decimal(18,2) NOT NULL,
    [starts_at] datetime2(7) NOT NULL,
    [ends_at] datetime2(7) NOT NULL,
    [is_active] bit NOT NULL CONSTRAINT [DF_promotions_active] DEFAULT ((0)),
    [is_deleted] bit NOT NULL CONSTRAINT [DF_promotions_deleted] DEFAULT ((0)),
    [version] int NOT NULL CONSTRAINT [DF_promotions_version] DEFAULT ((0)),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_promotions_created] DEFAULT (sysdatetime()),
    [updated_at] datetime2(7) NOT NULL CONSTRAINT [DF_promotions_updated] DEFAULT (sysdatetime())
);
GO
CREATE TABLE [dbo].[quality_grade_factors] (
    [grade] varchar(5) NOT NULL,
    [label] nvarchar(50) NOT NULL,
    [factor] decimal(5,3) NOT NULL
);
GO
CREATE TABLE [dbo].[refunds] (
    [refund_id] int IDENTITY(1,1) NOT NULL,
    [payment_id] int NOT NULL,
    [processed_by] int NULL,
    [amount] decimal(12,2) NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_refunds_status] DEFAULT ('requested'),
    [reason] nvarchar(255) NULL,
    [requested_at] datetime2(7) NOT NULL CONSTRAINT [DF_refunds_requested_at] DEFAULT (getdate()),
    [processed_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[registration_verifications] (
    [verification_id] int IDENTITY(1,1) NOT NULL,
    [full_name] nvarchar(100) NOT NULL,
    [email] nvarchar(255) NOT NULL,
    [password_hash] nvarchar(255) NOT NULL,
    [phone] nvarchar(20) NULL,
    [address] nvarchar(500) NULL,
    [otp] nvarchar(10) NOT NULL,
    [otp_expiry] datetime2(7) NOT NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_registration_verifications_created_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[review_reports] (
    [report_id] int IDENTITY(1,1) NOT NULL,
    [review_id] int NOT NULL,
    [reporter_id] int NOT NULL,
    [reason] nvarchar(50) NOT NULL,
    [note] nvarchar(500) NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_review_reports_created_at] DEFAULT (getdate()),
    [evidence_urls] nvarchar(1000) NULL,
    [admin_notes] nvarchar(1000) NULL
);
GO
CREATE TABLE [dbo].[reviews] (
    [review_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [product_id] int NOT NULL,
    [order_id] int NOT NULL,
    [rating] tinyint NOT NULL,
    [title] nvarchar(255) NULL,
    [comment] nvarchar(max) NULL,
    [image_urls] nvarchar(max) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_reviews_status] DEFAULT ('published'),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_reviews_created_at] DEFAULT (getdate()),
    [deleted_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[roles] (
    [role_id] int IDENTITY(1,1) NOT NULL,
    [role_name] nvarchar(50) NOT NULL
);
GO
CREATE TABLE [dbo].[shop_delivery_requests] (
    [request_id] int IDENTITY(1,1) NOT NULL,
    [shipper_id] int NOT NULL,
    [shop_id] int NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_delivery_requests_status] DEFAULT (N'Pending'),
    [requested_at] datetime2(7) NOT NULL CONSTRAINT [DF_delivery_requests_requested] DEFAULT (sysdatetime()),
    [reviewed_at] datetime2(7) NULL,
    [reviewed_by] int NULL,
    [review_note] nvarchar(255) NULL
);
GO
CREATE TABLE [dbo].[shop_delivery_settings] (
    [shop_id] int NOT NULL,
    [shipper_id] int NOT NULL,
    [is_available] bit NOT NULL CONSTRAINT [DF_delivery_settings_available] DEFAULT ((1)),
    [assignment_enabled] bit NOT NULL CONSTRAINT [DF_delivery_settings_assignment] DEFAULT ((1)),
    [leave_requested] bit NOT NULL CONSTRAINT [DF_delivery_settings_leave] DEFAULT ((0))
);
GO
CREATE TABLE [dbo].[shop_delivery_staff] (
    [shop_id] int NOT NULL,
    [shipper_id] int NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_shop_delivery_staff_status] DEFAULT ('Active'),
    [joined_at] datetime2(7) NOT NULL CONSTRAINT [DF_shop_delivery_staff_joined] DEFAULT (sysdatetime())
);
GO
CREATE TABLE [dbo].[shops] (
    [shop_id] int IDENTITY(1,1) NOT NULL,
    [owner_id] int NOT NULL,
    [shop_name] nvarchar(150) NOT NULL,
    [description] nvarchar(max) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_shops_status] DEFAULT ('pending'),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_shops_created_at] DEFAULT (getdate()),
    [low_stock_threshold] decimal(18,2) NOT NULL CONSTRAINT [DF_shops_low_stock_threshold] DEFAULT ((10)),
    [pickup_address] nvarchar(500) NULL,
    [block_reason] nvarchar(255) NULL,
    [blocked_at] datetime2(7) NULL,
    [blocked_by] int NULL
);
GO
CREATE TABLE [dbo].[subscription_plan_items] (
    [item_id] int IDENTITY(1,1) NOT NULL,
    [plan_id] int NOT NULL,
    [variant_id] int NOT NULL,
    [quantity] int NOT NULL CONSTRAINT [DF_subscription_plan_items_quantity] DEFAULT ((1))
);
GO
CREATE TABLE [dbo].[subscription_plans] (
    [plan_id] int IDENTITY(1,1) NOT NULL,
    [shop_id] int NOT NULL,
    [name] nvarchar(100) NOT NULL,
    [description] nvarchar(500) NULL,
    [price] decimal(10,2) NOT NULL,
    [billing_cycle] nvarchar(20) NOT NULL,
    [duration_days] int NOT NULL,
    [benefits] nvarchar(max) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_subscription_plans_status] DEFAULT ('draft'),
    [created_by] int NOT NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_subscription_plans_created_at] DEFAULT (getdate()),
    [updated_at] datetime2(7) NOT NULL CONSTRAINT [DF_subscription_plans_updated_at] DEFAULT (getdate()),
    [deleted_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[subscriptions] (
    [subscription_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [plan_id] int NULL,
    [variant_id] int NULL,
    [is_custom] bit NOT NULL CONSTRAINT [DF_subscriptions_is_custom] DEFAULT ((0)),
    [frequency] nvarchar(10) NOT NULL,
    [price_snapshot] decimal(10,2) NULL,
    [next_delivery_date] date NOT NULL,
    [end_date] date NULL,
    [notes] nvarchar(1000) NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_subscriptions_status] DEFAULT ('active'),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_subscriptions_created_at] DEFAULT (getdate())
);
GO
CREATE TABLE [dbo].[sysdiagrams] (
    [name] sysname NOT NULL,
    [principal_id] int NOT NULL,
    [diagram_id] int IDENTITY(1,1) NOT NULL,
    [version] int NULL,
    [definition] varbinary(max) NULL
);
GO
CREATE TABLE [dbo].[user_addresses] (
    [address_id] int IDENTITY(1,1) NOT NULL,
    [user_id] int NOT NULL,
    [recipient_name] nvarchar(100) NOT NULL,
    [phone] nvarchar(20) NOT NULL,
    [address_line] nvarchar(255) NOT NULL,
    [ward] nvarchar(100) NULL,
    [district] nvarchar(100) NULL,
    [province] nvarchar(100) NULL,
    [is_default] bit NOT NULL CONSTRAINT [DF_user_addresses_is_default] DEFAULT ((0)),
    [latitude] decimal(9,6) NULL,
    [longitude] decimal(9,6) NULL
);
GO
CREATE TABLE [dbo].[user_roles] (
    [user_id] int NOT NULL,
    [role_id] int NOT NULL
);
GO
CREATE TABLE [dbo].[users] (
    [user_id] int IDENTITY(1,1) NOT NULL,
    [full_name] nvarchar(100) NOT NULL,
    [email] nvarchar(150) NOT NULL,
    [phone] nvarchar(20) NULL,
    [password_hash] nvarchar(255) NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_users_status] DEFAULT ('active'),
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_users_created_at] DEFAULT (getdate()),
    [reset_otp] nvarchar(10) NULL,
    [reset_otp_expiry] datetime2(7) NULL,
    [is_member] bit NOT NULL CONSTRAINT [DF_users_is_member] DEFAULT ((0))
);
GO
CREATE TABLE [dbo].[variant_price_history] (
    [history_id] int IDENTITY(1,1) NOT NULL,
    [variant_id] int NOT NULL,
    [old_price] decimal(12,2) NULL,
    [new_price] decimal(12,2) NOT NULL,
    [reason] varchar(30) NOT NULL,
    [changed_by] int NULL,
    [changed_at] datetime2(7) NOT NULL CONSTRAINT [DF_variant_price_history_at] DEFAULT (sysdatetime())
);
GO
CREATE TABLE [dbo].[variant_promotions] (
    [promo_id] int IDENTITY(1,1) NOT NULL,
    [variant_id] int NOT NULL,
    [promo_price] decimal(18,2) NOT NULL,
    [start_date] date NOT NULL,
    [end_date] date NOT NULL,
    [created_at] datetime2(7) NOT NULL CONSTRAINT [DF_variant_promotions_created_at] DEFAULT (sysdatetime()),
    [updated_at] datetime2(7) NOT NULL CONSTRAINT [DF_variant_promotions_updated_at] DEFAULT (sysdatetime()),
    [status] varchar(20) NOT NULL CONSTRAINT [DF_variant_promotions_status] DEFAULT ('pending'),
    [review_note] nvarchar(255) NULL,
    [reviewed_by] int NULL,
    [reviewed_at] datetime2(7) NULL
);
GO
CREATE TABLE [dbo].[vouchers] (
    [voucher_id] int IDENTITY(1,1) NOT NULL,
    [code] nvarchar(50) NOT NULL,
    [discount_type] nvarchar(20) NOT NULL,
    [discount_value] decimal(12,2) NOT NULL,
    [start_date] datetime2(7) NOT NULL,
    [end_date] datetime2(7) NOT NULL,
    [usage_limit] int NOT NULL,
    [status] nvarchar(20) NOT NULL CONSTRAINT [DF_vouchers_status] DEFAULT ('active')
);
GO

ALTER TABLE [dbo].[cart_items] ADD CONSTRAINT [PK_cart_items] PRIMARY KEY ([cart_item_id]);
GO
ALTER TABLE [dbo].[cart_items] ADD CONSTRAINT [UQ_cart_items_cart_variant] UNIQUE ([cart_id], [variant_id]);
GO
ALTER TABLE [dbo].[carts] ADD CONSTRAINT [PK_carts] PRIMARY KEY ([cart_id]);
GO
ALTER TABLE [dbo].[carts] ADD CONSTRAINT [UQ_carts_user_id] UNIQUE ([user_id]);
GO
ALTER TABLE [dbo].[categories] ADD CONSTRAINT [PK_categories] PRIMARY KEY ([category_id]);
GO
ALTER TABLE [dbo].[commission_settlements] ADD CONSTRAINT [PK__commissi__9BB6D7086030DF29] PRIMARY KEY ([settlement_id]);
GO
ALTER TABLE [dbo].[commission_settlements] ADD CONSTRAINT [UQ_commission_settlement] UNIQUE ([shop_id], [period_month]);
GO
ALTER TABLE [dbo].[coupon_usages] ADD CONSTRAINT [PK_coupon_usages] PRIMARY KEY ([usage_id]);
GO
ALTER TABLE [dbo].[coupons] ADD CONSTRAINT [PK_coupons] PRIMARY KEY ([coupon_id]);
GO
ALTER TABLE [dbo].[coupons] ADD CONSTRAINT [UQ_coupons_id_shop] UNIQUE ([coupon_id], [shop_id]);
GO
ALTER TABLE [dbo].[coupons] ADD CONSTRAINT [UQ_coupons_shop_code] UNIQUE ([shop_id], [code]);
GO
ALTER TABLE [dbo].[deliveries] ADD CONSTRAINT [PK_deliveries] PRIMARY KEY ([delivery_id]);
GO
ALTER TABLE [dbo].[delivery_attempts] ADD CONSTRAINT [PK_delivery_attempts] PRIMARY KEY ([attempt_id]);
GO
ALTER TABLE [dbo].[delivery_locations] ADD CONSTRAINT [PK_delivery_locations] PRIMARY KEY ([location_id]);
GO
ALTER TABLE [dbo].[inventory_adjustments] ADD CONSTRAINT [PK_inventory_adjustments] PRIMARY KEY ([adjustment_id]);
GO
ALTER TABLE [dbo].[inventory_batches] ADD CONSTRAINT [PK_inventory_batches] PRIMARY KEY ([batch_id]);
GO
ALTER TABLE [dbo].[inventory_discard_requests] ADD CONSTRAINT [PK_inventory_discard_requests] PRIMARY KEY ([request_id]);
GO
ALTER TABLE [dbo].[loyalty_points] ADD CONSTRAINT [PK_loyalty_points] PRIMARY KEY ([point_id]);
GO
ALTER TABLE [dbo].[notifications] ADD CONSTRAINT [PK_notifications] PRIMARY KEY ([notification_id]);
GO
ALTER TABLE [dbo].[order_items] ADD CONSTRAINT [PK_order_items] PRIMARY KEY ([order_item_id]);
GO
ALTER TABLE [dbo].[order_status_history] ADD CONSTRAINT [PK_order_status_history] PRIMARY KEY ([history_id]);
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [PK_orders] PRIMARY KEY ([order_id]);
GO
ALTER TABLE [dbo].[payments] ADD CONSTRAINT [PK_payments] PRIMARY KEY ([payment_id]);
GO
ALTER TABLE [dbo].[payments] ADD CONSTRAINT [UQ_payments_order_id] UNIQUE ([order_id]);
GO
ALTER TABLE [dbo].[product_images] ADD CONSTRAINT [PK_product_images] PRIMARY KEY ([image_id]);
GO
ALTER TABLE [dbo].[product_variants] ADD CONSTRAINT [PK_product_variants] PRIMARY KEY ([variant_id]);
GO
ALTER TABLE [dbo].[product_variants] ADD CONSTRAINT [UQ_product_variants_sku] UNIQUE ([sku]);
GO
ALTER TABLE [dbo].[products] ADD CONSTRAINT [PK_products] PRIMARY KEY ([product_id]);
GO
ALTER TABLE [dbo].[promotion_products] ADD CONSTRAINT [PK_promotion_products] PRIMARY KEY ([promotion_id], [product_id]);
GO
ALTER TABLE [dbo].[promotions] ADD CONSTRAINT [PK_promotions] PRIMARY KEY ([promotion_id]);
GO
ALTER TABLE [dbo].[quality_grade_factors] ADD CONSTRAINT [PK_quality_grade_factors] PRIMARY KEY ([grade]);
GO
ALTER TABLE [dbo].[refunds] ADD CONSTRAINT [PK_refunds] PRIMARY KEY ([refund_id]);
GO
ALTER TABLE [dbo].[registration_verifications] ADD CONSTRAINT [PK_registration_verifications] PRIMARY KEY ([verification_id]);
GO
ALTER TABLE [dbo].[review_reports] ADD CONSTRAINT [PK_review_reports] PRIMARY KEY ([report_id]);
GO
ALTER TABLE [dbo].[review_reports] ADD CONSTRAINT [UQ_review_reports_per_user] UNIQUE ([review_id], [reporter_id]);
GO
ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [PK_reviews] PRIMARY KEY ([review_id]);
GO
ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [UQ_reviews_order_product] UNIQUE ([order_id], [product_id]);
GO
ALTER TABLE [dbo].[roles] ADD CONSTRAINT [PK_roles] PRIMARY KEY ([role_id]);
GO
ALTER TABLE [dbo].[roles] ADD CONSTRAINT [UQ_roles_role_name] UNIQUE ([role_name]);
GO
ALTER TABLE [dbo].[shop_delivery_requests] ADD CONSTRAINT [PK_shop_delivery_requests] PRIMARY KEY ([request_id]);
GO
ALTER TABLE [dbo].[shop_delivery_settings] ADD CONSTRAINT [PK_shop_delivery_settings] PRIMARY KEY ([shop_id], [shipper_id]);
GO
ALTER TABLE [dbo].[shop_delivery_staff] ADD CONSTRAINT [PK_shop_delivery_staff] PRIMARY KEY ([shop_id], [shipper_id]);
GO
ALTER TABLE [dbo].[shops] ADD CONSTRAINT [PK_shops] PRIMARY KEY ([shop_id]);
GO
ALTER TABLE [dbo].[subscription_plan_items] ADD CONSTRAINT [PK_subscription_plan_items] PRIMARY KEY ([item_id]);
GO
ALTER TABLE [dbo].[subscription_plan_items] ADD CONSTRAINT [UQ_subscription_plan_items_plan_variant] UNIQUE ([plan_id], [variant_id]);
GO
ALTER TABLE [dbo].[subscription_plans] ADD CONSTRAINT [PK_subscription_plans] PRIMARY KEY ([plan_id]);
GO
ALTER TABLE [dbo].[subscriptions] ADD CONSTRAINT [PK_subscriptions] PRIMARY KEY ([subscription_id]);
GO
ALTER TABLE [dbo].[sysdiagrams] ADD CONSTRAINT [PK__sysdiagr__C2B05B614A6B6FF8] PRIMARY KEY ([diagram_id]);
GO
ALTER TABLE [dbo].[sysdiagrams] ADD CONSTRAINT [UK_principal_name] UNIQUE ([principal_id], [name]);
GO
ALTER TABLE [dbo].[user_addresses] ADD CONSTRAINT [PK_user_addresses] PRIMARY KEY ([address_id]);
GO
ALTER TABLE [dbo].[user_roles] ADD CONSTRAINT [PK_user_roles] PRIMARY KEY ([user_id], [role_id]);
GO
ALTER TABLE [dbo].[users] ADD CONSTRAINT [PK_users] PRIMARY KEY ([user_id]);
GO
ALTER TABLE [dbo].[users] ADD CONSTRAINT [UQ_users_email] UNIQUE ([email]);
GO
ALTER TABLE [dbo].[variant_price_history] ADD CONSTRAINT [PK_variant_price_history] PRIMARY KEY ([history_id]);
GO
ALTER TABLE [dbo].[variant_promotions] ADD CONSTRAINT [PK_variant_promotions] PRIMARY KEY ([promo_id]);
GO
ALTER TABLE [dbo].[vouchers] ADD CONSTRAINT [PK_vouchers] PRIMARY KEY ([voucher_id]);
GO
ALTER TABLE [dbo].[vouchers] ADD CONSTRAINT [UQ_vouchers_code] UNIQUE ([code]);
GO
ALTER TABLE [dbo].[cart_items] ADD CONSTRAINT [CK_cart_items_quantity] CHECK ([quantity]>(0) AND [quantity]=floor([quantity]));
GO
ALTER TABLE [dbo].[commission_settlements] ADD CONSTRAINT [CK_commission_settlement_status] CHECK ([status]='paid' OR [status]='unpaid');
GO
ALTER TABLE [dbo].[coupon_usages] ADD CONSTRAINT [CK_coupon_usages_money] CHECK ([subtotal]>(0) AND [discount_amount]>=(0) AND [discount_amount]<=[subtotal] AND [final_amount]=([subtotal]-[discount_amount]));
GO
ALTER TABLE [dbo].[coupon_usages] ADD CONSTRAINT [CK_coupon_usages_state] CHECK ([usage_status]='Applied' AND [removed_at] IS NULL OR [usage_status]='Removed' AND [removed_at] IS NOT NULL);
GO
ALTER TABLE [dbo].[coupons] ADD CONSTRAINT [CK_coupons_deleted] CHECK ([is_deleted]=(0) OR [is_active]=(0));
GO
ALTER TABLE [dbo].[coupons] ADD CONSTRAINT [CK_coupons_policy] CHECK (([discount_type]='FIXED' OR [discount_type]='PERCENT') AND [discount_value]>(0) AND ([discount_type]<>'PERCENT' OR [discount_value]<=(100)) AND ([discount_type]<>'FIXED' OR [discount_value]=floor([discount_value])) AND ([max_discount] IS NULL OR [discount_type]='PERCENT' AND [max_discount]>(0)) AND [min_order_amount]>=(0) AND [total_limit]>(0) AND [per_customer_limit]>(0) AND [per_customer_limit]<=[total_limit] AND [starts_at]<[ends_at] AND [version]>=(0));
GO
ALTER TABLE [dbo].[deliveries] ADD CONSTRAINT [CK_deliveries_status] CHECK ([status]='failed' OR [status]='delivered' OR [status]='delivering' OR [status]='picked_up' OR [status]='rejected' OR [status]='accepted' OR [status]='assigned');
GO
ALTER TABLE [dbo].[delivery_attempts] ADD CONSTRAINT [CK_delivery_attempts_state] CHECK (([delivery_status]=N'Delivering' OR [delivery_status]=N'PickedUp' OR [delivery_status]=N'Accepted' OR [delivery_status]=N'Assigned') AND [closed_at] IS NULL OR ([delivery_status]=N'Failed' OR [delivery_status]=N'Delivered' OR [delivery_status]=N'Rejected') AND [closed_at] IS NOT NULL);
GO
ALTER TABLE [dbo].[delivery_attempts] ADD CONSTRAINT [CK_delivery_attempts_version] CHECK ([version]>=(0));
GO
ALTER TABLE [dbo].[delivery_locations] ADD CONSTRAINT [CK_delivery_locations_coords] CHECK ([latitude]>=(-90) AND [latitude]<=(90) AND ([longitude]>=(-180) AND [longitude]<=(180)));
GO
ALTER TABLE [dbo].[inventory_adjustments] ADD CONSTRAINT [CK_inventory_adjustments_quantity] CHECK ([quantity]>=(0));
GO
ALTER TABLE [dbo].[inventory_adjustments] ADD CONSTRAINT [CK_inventory_adjustments_type] CHECK ([adjustment_type]='discard' OR [adjustment_type]='export' OR [adjustment_type]='import');
GO
ALTER TABLE [dbo].[inventory_batches] ADD CONSTRAINT [CK_inventory_batches_quantity] CHECK ([quantity]>=(0));
GO
ALTER TABLE [dbo].[inventory_batches] ADD CONSTRAINT [CK_inventory_batches_status] CHECK ([status]='depleted' OR [status]='expired' OR [status]='active');
GO
ALTER TABLE [dbo].[inventory_discard_requests] ADD CONSTRAINT [CK_inventory_discard_status] CHECK ([status]='rejected' OR [status]='approved' OR [status]='pending');
GO
ALTER TABLE [dbo].[loyalty_points] ADD CONSTRAINT [CK_loyalty_points_type] CHECK ([type]='redeem' OR [type]='earn');
GO
ALTER TABLE [dbo].[notifications] ADD CONSTRAINT [CK_notifications_channel] CHECK ([channel]='both' OR [channel]='email' OR [channel]='in_app');
GO
ALTER TABLE [dbo].[order_items] ADD CONSTRAINT [CK_order_items_values] CHECK ([quantity]>(0) AND [quantity]=floor([quantity]) AND [unit_price]>=(0));
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [CK_orders_coupon_total] CHECK ([coupon_id] IS NULL AND [coupon_discount]=(0) AND [coupon_subtotal] IS NULL OR [coupon_id] IS NOT NULL AND [coupon_subtotal] IS NOT NULL AND [coupon_subtotal]>=[coupon_discount] AND [total_amount]=([coupon_subtotal]-[coupon_discount]));
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [CK_orders_money] CHECK ([total_amount]>=(0) AND [coupon_discount]>=(0));
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [CK_orders_one_discount] CHECK ([voucher_id] IS NULL OR [coupon_id] IS NULL);
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [CK_orders_status] CHECK ([status]='failed' OR [status]='cancelled' OR [status]='delivered' OR [status]='shipping' OR [status]='preparing' OR [status]='pending_confirmation');
GO
ALTER TABLE [dbo].[payments] ADD CONSTRAINT [CK_payments_amount] CHECK ([amount]>=(0));
GO
ALTER TABLE [dbo].[payments] ADD CONSTRAINT [CK_payments_method] CHECK ([method]='cod' OR [method]='card' OR [method]='momo' OR [method]='vnpay');
GO
ALTER TABLE [dbo].[payments] ADD CONSTRAINT [CK_payments_status] CHECK ([status]='failed' OR [status]='paid' OR [status]='pending');
GO
ALTER TABLE [dbo].[product_variants] ADD CONSTRAINT [CK_product_variants_grade] CHECK ([quality_grade]='3' OR [quality_grade]='2' OR [quality_grade]='1');
GO
ALTER TABLE [dbo].[product_variants] ADD CONSTRAINT [CK_product_variants_price] CHECK ([price]>=(0) AND ([weight_value] IS NULL OR [weight_value]>(0)));
GO
ALTER TABLE [dbo].[product_variants] ADD CONSTRAINT [CK_product_variants_unit] CHECK ([unit]='piece' OR [unit]='box' OR [unit]='kg');
GO
ALTER TABLE [dbo].[products] ADD CONSTRAINT [CK_products_commission_rate] CHECK ([commission_rate]>=(0) AND [commission_rate]<=(100));
GO
ALTER TABLE [dbo].[products] ADD CONSTRAINT [CK_products_status] CHECK ([status]='hidden' OR [status]='published' OR [status]='draft');
GO
ALTER TABLE [dbo].[promotions] ADD CONSTRAINT [CK_promotions_deleted] CHECK ([is_deleted]=(0) OR [is_active]=(0));
GO
ALTER TABLE [dbo].[promotions] ADD CONSTRAINT [CK_promotions_policy] CHECK (([discount_type]='FIXED' OR [discount_type]='PERCENT') AND [discount_value]>(0) AND ([discount_type]<>'PERCENT' OR [discount_value]<=(100)) AND ([discount_type]<>'FIXED' OR [discount_value]=floor([discount_value])) AND [starts_at]<[ends_at] AND [version]>=(0));
GO
ALTER TABLE [dbo].[quality_grade_factors] ADD CONSTRAINT [CK_quality_grade_factors_factor] CHECK ([factor]>(0) AND [factor]<=(1.5));
GO
ALTER TABLE [dbo].[refunds] ADD CONSTRAINT [CK_refunds_amount] CHECK ([amount]>(0));
GO
ALTER TABLE [dbo].[refunds] ADD CONSTRAINT [CK_refunds_status] CHECK ([status]='completed' OR [status]='rejected' OR [status]='approved' OR [status]='requested');
GO
ALTER TABLE [dbo].[review_reports] ADD CONSTRAINT [CK_review_reports_reason] CHECK ([reason]='other' OR [reason]='fake_review' OR [reason]='inappropriate_language' OR [reason]='spam');
GO
ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [CK_reviews_rating] CHECK ([rating]>=(1) AND [rating]<=(5));
GO
ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [CK_reviews_status] CHECK ([status]='deleted' OR [status]='rejected' OR [status]='reported' OR [status]='published');
GO
ALTER TABLE [dbo].[shop_delivery_requests] ADD CONSTRAINT [CK_delivery_requests_status] CHECK ([status]=N'Withdrawn' OR [status]=N'Rejected' OR [status]=N'Approved' OR [status]=N'Pending');
GO
ALTER TABLE [dbo].[shop_delivery_settings] ADD CONSTRAINT [CK_delivery_settings_leave] CHECK ([leave_requested]=(0) OR [is_available]=(0) AND [assignment_enabled]=(0));
GO
ALTER TABLE [dbo].[shop_delivery_staff] ADD CONSTRAINT [CK_shop_delivery_staff_status] CHECK ([status]='Inactive' OR [status]='Active');
GO
ALTER TABLE [dbo].[shops] ADD CONSTRAINT [CK_shops_status] CHECK ([status]='suspended' OR [status]='active' OR [status]='pending');
GO
ALTER TABLE [dbo].[shops] ADD CONSTRAINT [CK_shops_threshold] CHECK ([low_stock_threshold]>=(0));
GO
ALTER TABLE [dbo].[subscription_plan_items] ADD CONSTRAINT [CK_subscription_plan_items_quantity] CHECK ([quantity]>(0));
GO
ALTER TABLE [dbo].[subscription_plans] ADD CONSTRAINT [CK_subscription_plans_billing_cycle] CHECK ([billing_cycle]='yearly' OR [billing_cycle]='quarterly' OR [billing_cycle]='monthly' OR [billing_cycle]='weekly');
GO
ALTER TABLE [dbo].[subscription_plans] ADD CONSTRAINT [CK_subscription_plans_price] CHECK ([price]>=(0));
GO
ALTER TABLE [dbo].[subscription_plans] ADD CONSTRAINT [CK_subscription_plans_status] CHECK ([status]='inactive' OR [status]='active' OR [status]='draft');
GO
ALTER TABLE [dbo].[subscriptions] ADD CONSTRAINT [CK_subscriptions_frequency] CHECK ([frequency]='yearly' OR [frequency]='quarterly' OR [frequency]='monthly' OR [frequency]='weekly');
GO
ALTER TABLE [dbo].[subscriptions] ADD CONSTRAINT [CK_subscriptions_status] CHECK ([status]='cancelled' OR [status]='paused' OR [status]='active');
GO
ALTER TABLE [dbo].[users] ADD CONSTRAINT [CK_users_status] CHECK ([status]='pending' OR [status]='locked' OR [status]='active');
GO
ALTER TABLE [dbo].[variant_promotions] ADD CONSTRAINT [CK_variant_promotions_values] CHECK ([promo_price]>=(0) AND [start_date]<=[end_date] AND ([status]='rejected' OR [status]='approved' OR [status]='pending'));
GO
ALTER TABLE [dbo].[vouchers] ADD CONSTRAINT [CK_vouchers_policy] CHECK ([discount_value]>(0) AND ([discount_type]<>'percent' OR [discount_value]<=(100)) AND [start_date]<[end_date] AND [usage_limit]>=(0));
GO
ALTER TABLE [dbo].[vouchers] ADD CONSTRAINT [CK_vouchers_status] CHECK ([status]='disabled' OR [status]='expired' OR [status]='active');
GO
ALTER TABLE [dbo].[vouchers] ADD CONSTRAINT [CK_vouchers_type] CHECK ([discount_type]='fixed_amount' OR [discount_type]='percent');
GO
CREATE NONCLUSTERED INDEX [IX_coupon_usages_coupon_customer] ON [dbo].[coupon_usages] ([coupon_id], [customer_id], [usage_status]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_one_applied_coupon] ON [dbo].[coupon_usages] ([order_id]) WHERE ([usage_status]='Applied');
GO
CREATE NONCLUSTERED INDEX [IX_coupons_shop_time] ON [dbo].[coupons] ([shop_id], [is_active], [starts_at], [ends_at]);
GO
CREATE NONCLUSTERED INDEX [IX_deliveries_order] ON [dbo].[deliveries] ([order_id], [delivery_id]);
GO
CREATE NONCLUSTERED INDEX [IX_deliveries_shipper_state] ON [dbo].[deliveries] ([shipper_id], [delivery_status], [delivery_id]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_one_active_delivery] ON [dbo].[deliveries] ([order_id]) WHERE ([status] IN ('assigned', 'accepted', 'picked_up', 'delivering'));
GO
CREATE NONCLUSTERED INDEX [IX_delivery_attempts_shipper] ON [dbo].[delivery_attempts] ([shipper_id], [closed_at], [attempt_id]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_delivery_attempts_open] ON [dbo].[delivery_attempts] ([delivery_id]) WHERE ([closed_at] IS NULL);
GO
CREATE NONCLUSTERED INDEX [IX_inventory_variant] ON [dbo].[inventory_batches] ([variant_id], [status], [expiry_date]);
GO
CREATE NONCLUSTERED INDEX [IX_notifications_user] ON [dbo].[notifications] ([user_id], [is_read], [created_at]);
GO
CREATE NONCLUSTERED INDEX [IX_order_items_order] ON [dbo].[order_items] ([order_id]);
GO
CREATE NONCLUSTERED INDEX [IX_order_status_history_order] ON [dbo].[order_status_history] ([order_id], [changed_at]);
GO
CREATE NONCLUSTERED INDEX [IX_orders_shop_state] ON [dbo].[orders] ([shop_id], [status], [created_at]);
GO
CREATE NONCLUSTERED INDEX [IX_orders_user_date] ON [dbo].[orders] ([user_id], [created_at]);
GO
CREATE NONCLUSTERED INDEX [IX_variants_product] ON [dbo].[product_variants] ([product_id]);
GO
CREATE NONCLUSTERED INDEX [IX_products_shop] ON [dbo].[products] ([shop_id], [status]);
GO
CREATE NONCLUSTERED INDEX [IX_promotions_shop_time] ON [dbo].[promotions] ([shop_id], [is_active], [starts_at], [ends_at]);
GO
CREATE UNIQUE NONCLUSTERED INDEX [UX_delivery_requests_pending] ON [dbo].[shop_delivery_requests] ([shipper_id]) WHERE ([status]=N'Pending');
GO
ALTER TABLE [dbo].[cart_items] ADD CONSTRAINT [FK_cart_items_cart_id] FOREIGN KEY ([cart_id]) REFERENCES [dbo].[carts] ([cart_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[cart_items] ADD CONSTRAINT [FK_cart_items_variant_id] FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([variant_id]);
GO
ALTER TABLE [dbo].[carts] ADD CONSTRAINT [FK_carts_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[categories] ADD CONSTRAINT [FK_categories_parent_id] FOREIGN KEY ([parent_id]) REFERENCES [dbo].[categories] ([category_id]);
GO
ALTER TABLE [dbo].[categories] ADD CONSTRAINT [FK_categories_shops] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[commission_settlements] ADD CONSTRAINT [FK_commission_settlement_shop] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[coupon_usages] ADD CONSTRAINT [FK_coupon_usages_coupon] FOREIGN KEY ([coupon_id], [shop_id]) REFERENCES [dbo].[coupons] ([coupon_id], [shop_id]);
GO
ALTER TABLE [dbo].[coupon_usages] ADD CONSTRAINT [FK_coupon_usages_customer] FOREIGN KEY ([customer_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[coupon_usages] ADD CONSTRAINT [FK_coupon_usages_order] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([order_id]);
GO
ALTER TABLE [dbo].[coupons] ADD CONSTRAINT [FK_coupons_shop] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[deliveries] ADD CONSTRAINT [FK_deliveries_order_id] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([order_id]);
GO
ALTER TABLE [dbo].[deliveries] ADD CONSTRAINT [FK_deliveries_shipper] FOREIGN KEY ([shipper_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[deliveries] ADD CONSTRAINT [FK_deliveries_shop] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[deliveries] ADD CONSTRAINT [FK_deliveries_staff_id] FOREIGN KEY ([staff_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[delivery_attempts] ADD CONSTRAINT [FK_delivery_attempts_delivery] FOREIGN KEY ([delivery_id]) REFERENCES [dbo].[deliveries] ([delivery_id]);
GO
ALTER TABLE [dbo].[delivery_attempts] ADD CONSTRAINT [FK_delivery_attempts_owner] FOREIGN KEY ([assigned_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[delivery_attempts] ADD CONSTRAINT [FK_delivery_attempts_shipper] FOREIGN KEY ([shipper_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[delivery_locations] ADD CONSTRAINT [FK_delivery_locations_delivery_id] FOREIGN KEY ([delivery_id]) REFERENCES [dbo].[deliveries] ([delivery_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[inventory_adjustments] ADD CONSTRAINT [FK_inventory_adjustments_batch_id] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[inventory_batches] ([batch_id]);
GO
ALTER TABLE [dbo].[inventory_adjustments] ADD CONSTRAINT [FK_inventory_adjustments_created_by] FOREIGN KEY ([created_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[inventory_batches] ADD CONSTRAINT [FK_inventory_batches_variant_id] FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([variant_id]);
GO
ALTER TABLE [dbo].[inventory_discard_requests] ADD CONSTRAINT [FK_inventory_discard_batch] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[inventory_batches] ([batch_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[loyalty_points] ADD CONSTRAINT [FK_loyalty_points_source_order_id] FOREIGN KEY ([source_order_id]) REFERENCES [dbo].[orders] ([order_id]);
GO
ALTER TABLE [dbo].[loyalty_points] ADD CONSTRAINT [FK_loyalty_points_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[notifications] ADD CONSTRAINT [FK_notifications_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[order_items] ADD CONSTRAINT [FK_order_items_batch_id] FOREIGN KEY ([batch_id]) REFERENCES [dbo].[inventory_batches] ([batch_id]);
GO
ALTER TABLE [dbo].[order_items] ADD CONSTRAINT [FK_order_items_order_id] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([order_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[order_items] ADD CONSTRAINT [FK_order_items_variant_id] FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([variant_id]);
GO
ALTER TABLE [dbo].[order_status_history] ADD CONSTRAINT [FK_order_status_history_changed_by] FOREIGN KEY ([changed_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[order_status_history] ADD CONSTRAINT [FK_order_status_history_order_id] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([order_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [FK_orders_address_id] FOREIGN KEY ([address_id]) REFERENCES [dbo].[user_addresses] ([address_id]);
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [FK_orders_coupon_shop] FOREIGN KEY ([coupon_id], [shop_id]) REFERENCES [dbo].[coupons] ([coupon_id], [shop_id]);
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [FK_orders_shop_id] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [FK_orders_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[orders] ADD CONSTRAINT [FK_orders_voucher_id] FOREIGN KEY ([voucher_id]) REFERENCES [dbo].[vouchers] ([voucher_id]);
GO
ALTER TABLE [dbo].[payments] ADD CONSTRAINT [FK_payments_order_id] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([order_id]);
GO
ALTER TABLE [dbo].[product_images] ADD CONSTRAINT [FK_product_images_product_id] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([product_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[product_variants] ADD CONSTRAINT [FK_product_variants_product_id] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([product_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[product_variants] ADD CONSTRAINT [FK_product_variants_quality_grade] FOREIGN KEY ([quality_grade]) REFERENCES [dbo].[quality_grade_factors] ([grade]);
GO
ALTER TABLE [dbo].[products] ADD CONSTRAINT [FK_products_category_id] FOREIGN KEY ([category_id]) REFERENCES [dbo].[categories] ([category_id]);
GO
ALTER TABLE [dbo].[products] ADD CONSTRAINT [FK_products_shop_id] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[promotion_products] ADD CONSTRAINT [FK_promotion_products_product] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([product_id]);
GO
ALTER TABLE [dbo].[promotion_products] ADD CONSTRAINT [FK_promotion_products_promotion] FOREIGN KEY ([promotion_id]) REFERENCES [dbo].[promotions] ([promotion_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[promotions] ADD CONSTRAINT [FK_promotions_shop] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[refunds] ADD CONSTRAINT [FK_refunds_payment_id] FOREIGN KEY ([payment_id]) REFERENCES [dbo].[payments] ([payment_id]);
GO
ALTER TABLE [dbo].[refunds] ADD CONSTRAINT [FK_refunds_processed_by] FOREIGN KEY ([processed_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[review_reports] ADD CONSTRAINT [FK_review_reports_reporter_id] FOREIGN KEY ([reporter_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[review_reports] ADD CONSTRAINT [FK_review_reports_review_id] FOREIGN KEY ([review_id]) REFERENCES [dbo].[reviews] ([review_id]);
GO
ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [FK_reviews_order_id] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([order_id]);
GO
ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [FK_reviews_product_id] FOREIGN KEY ([product_id]) REFERENCES [dbo].[products] ([product_id]);
GO
ALTER TABLE [dbo].[reviews] ADD CONSTRAINT [FK_reviews_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[shop_delivery_requests] ADD CONSTRAINT [FK_delivery_requests_reviewer] FOREIGN KEY ([reviewed_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[shop_delivery_requests] ADD CONSTRAINT [FK_delivery_requests_shipper] FOREIGN KEY ([shipper_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[shop_delivery_requests] ADD CONSTRAINT [FK_delivery_requests_shop] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[shop_delivery_settings] ADD CONSTRAINT [FK_delivery_settings_member] FOREIGN KEY ([shop_id], [shipper_id]) REFERENCES [dbo].[shop_delivery_staff] ([shop_id], [shipper_id]);
GO
ALTER TABLE [dbo].[shop_delivery_staff] ADD CONSTRAINT [FK_shop_delivery_staff_shop] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[shop_delivery_staff] ADD CONSTRAINT [FK_shop_delivery_staff_user] FOREIGN KEY ([shipper_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[shops] ADD CONSTRAINT [FK_shops_owner_id] FOREIGN KEY ([owner_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[shops] ADD CONSTRAINT [FK_shops_blocked_by] FOREIGN KEY ([blocked_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[subscription_plan_items] ADD CONSTRAINT [FK_subscription_plan_items_plan_id] FOREIGN KEY ([plan_id]) REFERENCES [dbo].[subscription_plans] ([plan_id]);
GO
ALTER TABLE [dbo].[subscription_plan_items] ADD CONSTRAINT [FK_subscription_plan_items_variant_id] FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([variant_id]);
GO
ALTER TABLE [dbo].[subscription_plans] ADD CONSTRAINT [FK_subscription_plans_created_by] FOREIGN KEY ([created_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[subscription_plans] ADD CONSTRAINT [FK_subscription_plans_shop_id] FOREIGN KEY ([shop_id]) REFERENCES [dbo].[shops] ([shop_id]);
GO
ALTER TABLE [dbo].[subscriptions] ADD CONSTRAINT [FK_subscriptions_plan_id] FOREIGN KEY ([plan_id]) REFERENCES [dbo].[subscription_plans] ([plan_id]);
GO
ALTER TABLE [dbo].[subscriptions] ADD CONSTRAINT [FK_subscriptions_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[subscriptions] ADD CONSTRAINT [FK_subscriptions_variant_id] FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([variant_id]);
GO
ALTER TABLE [dbo].[user_addresses] ADD CONSTRAINT [FK_user_addresses_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[user_roles] ADD CONSTRAINT [FK_user_roles_role_id] FOREIGN KEY ([role_id]) REFERENCES [dbo].[roles] ([role_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[user_roles] ADD CONSTRAINT [FK_user_roles_user_id] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[variant_price_history] ADD CONSTRAINT [FK_variant_price_history_variant] FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([variant_id]) ON DELETE CASCADE;
GO
ALTER TABLE [dbo].[variant_promotions] ADD CONSTRAINT [FK_variant_promotions_reviewer] FOREIGN KEY ([reviewed_by]) REFERENCES [dbo].[users] ([user_id]);
GO
ALTER TABLE [dbo].[variant_promotions] ADD CONSTRAINT [FK_variant_promotions_variant] FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([variant_id]);
GO

	CREATE FUNCTION dbo.fn_diagramobjects() 
	RETURNS int
	WITH EXECUTE AS N'dbo'
	AS
	BEGIN
		declare @id_upgraddiagrams		int
		declare @id_sysdiagrams			int
		declare @id_helpdiagrams		int
		declare @id_helpdiagramdefinition	int
		declare @id_creatediagram	int
		declare @id_renamediagram	int
		declare @id_alterdiagram 	int 
		declare @id_dropdiagram		int
		declare @InstalledObjects	int

		select @InstalledObjects = 0

		select 	@id_upgraddiagrams = object_id(N'dbo.sp_upgraddiagrams'),
			@id_sysdiagrams = object_id(N'dbo.sysdiagrams'),
			@id_helpdiagrams = object_id(N'dbo.sp_helpdiagrams'),
			@id_helpdiagramdefinition = object_id(N'dbo.sp_helpdiagramdefinition'),
			@id_creatediagram = object_id(N'dbo.sp_creatediagram'),
			@id_renamediagram = object_id(N'dbo.sp_renamediagram'),
			@id_alterdiagram = object_id(N'dbo.sp_alterdiagram'), 
			@id_dropdiagram = object_id(N'dbo.sp_dropdiagram')

		if @id_upgraddiagrams is not null
			select @InstalledObjects = @InstalledObjects + 1
		if @id_sysdiagrams is not null
			select @InstalledObjects = @InstalledObjects + 2
		if @id_helpdiagrams is not null
			select @InstalledObjects = @InstalledObjects + 4
		if @id_helpdiagramdefinition is not null
			select @InstalledObjects = @InstalledObjects + 8
		if @id_creatediagram is not null
			select @InstalledObjects = @InstalledObjects + 16
		if @id_renamediagram is not null
			select @InstalledObjects = @InstalledObjects + 32
		if @id_alterdiagram  is not null
			select @InstalledObjects = @InstalledObjects + 64
		if @id_dropdiagram is not null
			select @InstalledObjects = @InstalledObjects + 128
		
		return @InstalledObjects 
	END
	
GO

	CREATE PROCEDURE dbo.sp_alterdiagram
	(
		@diagramname 	sysname,
		@owner_id	int	= null,
		@version 	int,
		@definition 	varbinary(max)
	)
	WITH EXECUTE AS 'dbo'
	AS
	BEGIN
		set nocount on
	
		declare @theId 			int
		declare @retval 		int
		declare @IsDbo 			int
		
		declare @UIDFound 		int
		declare @DiagId			int
		declare @ShouldChangeUID	int
	
		if(@diagramname is null)
		begin
			RAISERROR ('Invalid ARG', 16, 1)
			return -1
		end
	
		execute as caller;
		select @theId = DATABASE_PRINCIPAL_ID();	 
		select @IsDbo = IS_MEMBER(N'db_owner'); 
		if(@owner_id is null)
			select @owner_id = @theId;
		revert;
	
		select @ShouldChangeUID = 0
		select @DiagId = diagram_id, @UIDFound = principal_id from dbo.sysdiagrams where principal_id = @owner_id and name = @diagramname 
		
		if(@DiagId IS NULL or (@IsDbo = 0 and @theId <> @UIDFound))
		begin
			RAISERROR ('Diagram does not exist or you do not have permission.', 16, 1);
			return -3
		end
	
		if(@IsDbo <> 0)
		begin
			if(@UIDFound is null or USER_NAME(@UIDFound) is null) -- invalid principal_id
			begin
				select @ShouldChangeUID = 1 ;
			end
		end

		-- update dds data			
		update dbo.sysdiagrams set definition = @definition where diagram_id = @DiagId ;

		-- change owner
		if(@ShouldChangeUID = 1)
			update dbo.sysdiagrams set principal_id = @theId where diagram_id = @DiagId ;

		-- update dds version
		if(@version is not null)
			update dbo.sysdiagrams set version = @version where diagram_id = @DiagId ;

		return 0
	END
	
GO

	CREATE PROCEDURE dbo.sp_creatediagram
	(
		@diagramname 	sysname,
		@owner_id		int	= null, 	
		@version 		int,
		@definition 	varbinary(max)
	)
	WITH EXECUTE AS 'dbo'
	AS
	BEGIN
		set nocount on
	
		declare @theId int
		declare @retval int
		declare @IsDbo	int
		declare @userName sysname
		if(@version is null or @diagramname is null)
		begin
			RAISERROR (N'E_INVALIDARG', 16, 1);
			return -1
		end
	
		execute as caller;
		select @theId = DATABASE_PRINCIPAL_ID(); 
		select @IsDbo = IS_MEMBER(N'db_owner');
		revert; 
		
		if @owner_id is null
		begin
			select @owner_id = @theId;
		end
		else
		begin
			if @theId <> @owner_id
			begin
				if @IsDbo = 0
				begin
					RAISERROR (N'E_INVALIDARG', 16, 1);
					return -1
				end
				select @theId = @owner_id
			end
		end
		-- next 2 line only for test, will be removed after define name unique
		if EXISTS(select diagram_id from dbo.sysdiagrams where principal_id = @theId and name = @diagramname)
		begin
			RAISERROR ('The name is already used.', 16, 1);
			return -2
		end
	
		insert into dbo.sysdiagrams(name, principal_id , version, definition)
				VALUES(@diagramname, @theId, @version, @definition) ;
		
		select @retval = @@IDENTITY 
		return @retval
	END
	
GO

	CREATE PROCEDURE dbo.sp_dropdiagram
	(
		@diagramname 	sysname,
		@owner_id	int	= null
	)
	WITH EXECUTE AS 'dbo'
	AS
	BEGIN
		set nocount on
		declare @theId 			int
		declare @IsDbo 			int
		
		declare @UIDFound 		int
		declare @DiagId			int
	
		if(@diagramname is null)
		begin
			RAISERROR ('Invalid value', 16, 1);
			return -1
		end
	
		EXECUTE AS CALLER;
		select @theId = DATABASE_PRINCIPAL_ID();
		select @IsDbo = IS_MEMBER(N'db_owner'); 
		if(@owner_id is null)
			select @owner_id = @theId;
		REVERT; 
		
		select @DiagId = diagram_id, @UIDFound = principal_id from dbo.sysdiagrams where principal_id = @owner_id and name = @diagramname 
		if(@DiagId IS NULL or (@IsDbo = 0 and @UIDFound <> @theId))
		begin
			RAISERROR ('Diagram does not exist or you do not have permission.', 16, 1)
			return -3
		end
	
		delete from dbo.sysdiagrams where diagram_id = @DiagId;
	
		return 0;
	END
	
GO

	CREATE PROCEDURE dbo.sp_helpdiagramdefinition
	(
		@diagramname 	sysname,
		@owner_id	int	= null 		
	)
	WITH EXECUTE AS N'dbo'
	AS
	BEGIN
		set nocount on

		declare @theId 		int
		declare @IsDbo 		int
		declare @DiagId		int
		declare @UIDFound	int
	
		if(@diagramname is null)
		begin
			RAISERROR (N'E_INVALIDARG', 16, 1);
			return -1
		end
	
		execute as caller;
		select @theId = DATABASE_PRINCIPAL_ID();
		select @IsDbo = IS_MEMBER(N'db_owner');
		if(@owner_id is null)
			select @owner_id = @theId;
		revert; 
	
		select @DiagId = diagram_id, @UIDFound = principal_id from dbo.sysdiagrams where principal_id = @owner_id and name = @diagramname;
		if(@DiagId IS NULL or (@IsDbo = 0 and @UIDFound <> @theId ))
		begin
			RAISERROR ('Diagram does not exist or you do not have permission.', 16, 1);
			return -3
		end

		select version, definition FROM dbo.sysdiagrams where diagram_id = @DiagId ; 
		return 0
	END
	
GO

	CREATE PROCEDURE dbo.sp_helpdiagrams
	(
		@diagramname sysname = NULL,
		@owner_id int = NULL
	)
	WITH EXECUTE AS N'dbo'
	AS
	BEGIN
		DECLARE @user sysname
		DECLARE @dboLogin bit
		EXECUTE AS CALLER;
			SET @user = USER_NAME();
			SET @dboLogin = CONVERT(bit,IS_MEMBER('db_owner'));
		REVERT;
		SELECT
			[Database] = DB_NAME(),
			[Name] = name,
			[ID] = diagram_id,
			[Owner] = USER_NAME(principal_id),
			[OwnerID] = principal_id
		FROM
			sysdiagrams
		WHERE
			(@dboLogin = 1 OR USER_NAME(principal_id) = @user) AND
			(@diagramname IS NULL OR name = @diagramname) AND
			(@owner_id IS NULL OR principal_id = @owner_id)
		ORDER BY
			4, 5, 1
	END
	
GO

	CREATE PROCEDURE dbo.sp_renamediagram
	(
		@diagramname 		sysname,
		@owner_id		int	= null,
		@new_diagramname	sysname
	
	)
	WITH EXECUTE AS 'dbo'
	AS
	BEGIN
		set nocount on
		declare @theId 			int
		declare @IsDbo 			int
		
		declare @UIDFound 		int
		declare @DiagId			int
		declare @DiagIdTarg		int
		declare @u_name			sysname
		if((@diagramname is null) or (@new_diagramname is null))
		begin
			RAISERROR ('Invalid value', 16, 1);
			return -1
		end
	
		EXECUTE AS CALLER;
		select @theId = DATABASE_PRINCIPAL_ID();
		select @IsDbo = IS_MEMBER(N'db_owner'); 
		if(@owner_id is null)
			select @owner_id = @theId;
		REVERT;
	
		select @u_name = USER_NAME(@owner_id)
	
		select @DiagId = diagram_id, @UIDFound = principal_id from dbo.sysdiagrams where principal_id = @owner_id and name = @diagramname 
		if(@DiagId IS NULL or (@IsDbo = 0 and @UIDFound <> @theId))
		begin
			RAISERROR ('Diagram does not exist or you do not have permission.', 16, 1)
			return -3
		end
	
		-- if((@u_name is not null) and (@new_diagramname = @diagramname))	-- nothing will change
		--	return 0;
	
		if(@u_name is null)
			select @DiagIdTarg = diagram_id from dbo.sysdiagrams where principal_id = @theId and name = @new_diagramname
		else
			select @DiagIdTarg = diagram_id from dbo.sysdiagrams where principal_id = @owner_id and name = @new_diagramname
	
		if((@DiagIdTarg is not null) and  @DiagId <> @DiagIdTarg)
		begin
			RAISERROR ('The name is already used.', 16, 1);
			return -2
		end		
	
		if(@u_name is null)
			update dbo.sysdiagrams set [name] = @new_diagramname, principal_id = @theId where diagram_id = @DiagId
		else
			update dbo.sysdiagrams set [name] = @new_diagramname where diagram_id = @DiagId
		return 0
	END
	
GO

	CREATE PROCEDURE dbo.sp_upgraddiagrams
	AS
	BEGIN
		IF OBJECT_ID(N'dbo.sysdiagrams') IS NOT NULL
			return 0;
	
		CREATE TABLE dbo.sysdiagrams
		(
			name sysname NOT NULL,
			principal_id int NOT NULL,	-- we may change it to varbinary(85)
			diagram_id int PRIMARY KEY IDENTITY,
			version int,
	
			definition varbinary(max)
			CONSTRAINT UK_principal_name UNIQUE
			(
				principal_id,
				name
			)
		);


		/* Add this if we need to have some form of extended properties for diagrams */
		/*
		IF OBJECT_ID(N'dbo.sysdiagram_properties') IS NULL
		BEGIN
			CREATE TABLE dbo.sysdiagram_properties
			(
				diagram_id int,
				name sysname,
				value varbinary(max) NOT NULL
			)
		END
		*/

		IF OBJECT_ID(N'dbo.dtproperties') IS NOT NULL
		begin
			insert into dbo.sysdiagrams
			(
				[name],
				[principal_id],
				[version],
				[definition]
			)
			select	 
				convert(sysname, dgnm.[uvalue]),
				DATABASE_PRINCIPAL_ID(N'dbo'),			-- will change to the sid of sa
				0,							-- zero for old format, dgdef.[version],
				dgdef.[lvalue]
			from dbo.[dtproperties] dgnm
				inner join dbo.[dtproperties] dggd on dggd.[property] = 'DtgSchemaGUID' and dggd.[objectid] = dgnm.[objectid]	
				inner join dbo.[dtproperties] dgdef on dgdef.[property] = 'DtgSchemaDATA' and dgdef.[objectid] = dgnm.[objectid]
				
			where dgnm.[property] = 'DtgSchemaNAME' and dggd.[uvalue] like N'_EA3E6268-D998-11CE-9454-00AA00A3F36E_' 
			return 2;
		end
		return 1;
	END
	
GO

