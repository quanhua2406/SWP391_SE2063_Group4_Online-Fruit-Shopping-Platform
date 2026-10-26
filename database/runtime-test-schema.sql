-- Generated from SQL Server metadata; schema only, no user data.
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[vouchers](
	[voucher_id] [int] IDENTITY(1,1) NOT NULL,
	[code] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[discount_amount] [decimal](18, 0) NOT NULL,
	[min_order_amount] [decimal](18, 0) NOT NULL,
	[quantity] [int] NOT NULL,
	[start_date] [date] NOT NULL,
	[end_date] [date] NOT NULL,
	[status] [nvarchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED
(
	[voucher_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
UNIQUE NONCLUSTERED
(
	[code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[users](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[full_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[email] [varchar](254) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[password_hash] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[phone] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[status] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[reset_otp] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[reset_otp_expiry] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[username] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[address] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[avatar] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
PRIMARY KEY CLUSTERED
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
UNIQUE NONCLUSTERED
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[categories](
	[category_id] [int] IDENTITY(1,1) NOT NULL,
	[parent_id] [int] NULL,
	[category_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
PRIMARY KEY CLUSTERED
(
	[category_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[roles](
	[role_id] [int] IDENTITY(1,1) NOT NULL,
	[role_name] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
UNIQUE NONCLUSTERED
(
	[role_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[notifications](
	[notification_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NOT NULL,
	[title] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[message] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[is_read] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[notification_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[carts](
	[cart_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[cart_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
UNIQUE NONCLUSTERED
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[user_roles](
	[user_id] [int] NOT NULL,
	[role_id] [int] NOT NULL,
 CONSTRAINT [PK_user_roles] PRIMARY KEY CLUSTERED
(
	[user_id] ASC,
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[user_addresses](
	[address_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NOT NULL,
	[recipient_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[phone] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[address_line] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ward] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[district] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[province] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[is_default] [bit] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[address_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[shops](
	[shop_id] [int] IDENTITY(1,1) NOT NULL,
	[owner_id] [int] NOT NULL,
	[shop_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[pickup_address] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[status] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[low_stock_threshold] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[shop_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[shop_delivery_staff](
	[shop_id] [int] NOT NULL,
	[shipper_id] [int] NOT NULL,
	[status] [nvarchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
 CONSTRAINT [PK_SO2_staff] PRIMARY KEY CLUSTERED
(
	[shop_id] ASC,
	[shipper_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
 CONSTRAINT [UQ_SO2_staff_shipper] UNIQUE NONCLUSTERED
(
	[shipper_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[coupons](
	[coupon_id] [int] IDENTITY(1,1) NOT NULL,
	[shop_id] [int] NOT NULL,
	[code] [varchar](30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[coupon_name] [nvarchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[discount_type] [varchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[discount_value] [decimal](18, 2) NOT NULL,
	[max_discount] [decimal](18, 0) NULL,
	[min_order_amount] [decimal](18, 0) NOT NULL,
	[total_limit] [int] NOT NULL,
	[per_customer_limit] [int] NOT NULL,
	[starts_at] [datetime2](0) NOT NULL,
	[ends_at] [datetime2](0) NOT NULL,
	[is_active] [bit] NOT NULL,
	[is_deleted] [bit] NOT NULL,
	[version] [int] NOT NULL,
	[created_at] [datetime2](0) NOT NULL,
	[updated_at] [datetime2](0) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[coupon_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
 CONSTRAINT [UQ_SO2_coupon_code] UNIQUE NONCLUSTERED
(
	[shop_id] ASC,
	[code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
 CONSTRAINT [UQ_SO2_coupon_shop] UNIQUE NONCLUSTERED
(
	[coupon_id] ASC,
	[shop_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[products](
	[product_id] [int] IDENTITY(1,1) NOT NULL,
	[shop_id] [int] NOT NULL,
	[category_id] [int] NOT NULL,
	[product_name] [nvarchar](200) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[origin] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[status] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[product_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[promotions](
	[promotion_id] [int] IDENTITY(1,1) NOT NULL,
	[shop_id] [int] NOT NULL,
	[promotion_name] [nvarchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[discount_type] [varchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[discount_value] [decimal](18, 2) NOT NULL,
	[starts_at] [datetime2](0) NOT NULL,
	[ends_at] [datetime2](0) NOT NULL,
	[is_active] [bit] NOT NULL,
	[is_deleted] [bit] NOT NULL,
	[version] [int] NOT NULL,
	[created_at] [datetime2](0) NOT NULL,
	[updated_at] [datetime2](0) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[promotion_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[reviews](
	[review_id] [int] IDENTITY(1,1) NOT NULL,
	[product_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[rating] [int] NOT NULL,
	[comment] [nvarchar](1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[review_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[promotion_products](
	[promotion_id] [int] NOT NULL,
	[product_id] [int] NOT NULL,
 CONSTRAINT [PK_SO2_promotion_products] PRIMARY KEY CLUSTERED
(
	[promotion_id] ASC,
	[product_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[product_variants](
	[variant_id] [int] IDENTITY(1,1) NOT NULL,
	[product_id] [int] NOT NULL,
	[sku] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[unit] [nvarchar](30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[weight_value] [decimal](18, 3) NULL,
	[packaging_type] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[price] [decimal](18, 0) NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[variant_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
UNIQUE NONCLUSTERED
(
	[sku] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[product_images](
	[image_id] [int] IDENTITY(1,1) NOT NULL,
	[product_id] [int] NOT NULL,
	[image_url] [nvarchar](1000) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[sort_order] [int] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[image_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[orders](
	[order_id] [int] IDENTITY(1,1) NOT NULL,
	[shop_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[order_date] [datetime2](7) NOT NULL,
	[total_amount] [decimal](18, 0) NOT NULL,
	[voucher_id] [int] NULL,
	[coupon_id] [int] NULL,
	[coupon_discount] [decimal](18, 0) NOT NULL,
	[coupon_subtotal] [decimal](18, 0) NULL,
	[receiver_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[receiver_phone] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[shipping_address] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[note] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[order_status] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[payment_method] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[payment_status] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED
(
	[order_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
 CONSTRAINT [UQ_orders_customer] UNIQUE NONCLUSTERED
(
	[order_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
 CONSTRAINT [UQ_orders_shop] UNIQUE NONCLUSTERED
(
	[order_id] ASC,
	[shop_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[order_items](
	[order_item_id] [int] IDENTITY(1,1) NOT NULL,
	[order_id] [int] NOT NULL,
	[variant_id] [int] NOT NULL,
	[price] [decimal](18, 0) NOT NULL,
	[quantity] [int] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[order_item_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[coupon_usages](
	[usage_id] [int] IDENTITY(1,1) NOT NULL,
	[coupon_id] [int] NOT NULL,
	[shop_id] [int] NOT NULL,
	[order_id] [int] NOT NULL,
	[customer_id] [int] NOT NULL,
	[code_snapshot] [varchar](30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[subtotal] [decimal](18, 0) NOT NULL,
	[discount_amount] [decimal](18, 0) NOT NULL,
	[final_amount] [decimal](18, 0) NOT NULL,
	[used_at] [datetime2](0) NOT NULL,
	[removed_at] [datetime2](0) NULL,
	[usage_status] [varchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED
(
	[usage_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[cart_items](
	[cart_item_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NOT NULL,
	[variant_id] [int] NOT NULL,
	[quantity] [int] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[cart_item_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
 CONSTRAINT [UQ_cart_user_variant] UNIQUE NONCLUSTERED
(
	[user_id] ASC,
	[variant_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[deliveries](
	[delivery_id] [int] IDENTITY(1,1) NOT NULL,
	[shop_id] [int] NOT NULL,
	[order_id] [int] NOT NULL,
	[shipper_id] [int] NULL,
	[delivery_status] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[pickup_time] [datetime2](7) NULL,
	[delivered_time] [datetime2](7) NULL,
	[note] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
PRIMARY KEY CLUSTERED
(
	[delivery_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[inventory_batches](
	[batch_id] [int] IDENTITY(1,1) NOT NULL,
	[variant_id] [int] NOT NULL,
	[batch_code] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[quantity] [decimal](18, 3) NOT NULL,
	[expiry_date] [date] NULL,
	[received_date] [date] NOT NULL,
	[status] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED
(
	[batch_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_NULLS ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET QUOTED_IDENTIFIER ON
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE TABLE [dbo].[inventory_adjustments](
	[adjustment_id] [int] IDENTITY(1,1) NOT NULL,
	[batch_id] [int] NOT NULL,
	[quantity_change] [decimal](18, 3) NOT NULL,
	[reason] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[adjusted_by] [int] NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[adjustment_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_SO2_notification_user] ON [dbo].[notifications]
(
	[user_id] ASC,
	[is_read] ASC,
	[notification_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_addresses_user] ON [dbo].[user_addresses]
(
	[user_id] ASC,
	[is_default] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_PADDING ON

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_shops_owner] ON [dbo].[shops]
(
	[owner_id] ASC,
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_PADDING ON

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_products_shop] ON [dbo].[products]
(
	[shop_id] ASC,
	[category_id] ASC,
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_SO2_promotion_period] ON [dbo].[promotions]
(
	[shop_id] ASC,
	[is_deleted] ASC,
	[is_active] ASC,
	[starts_at] ASC,
	[ends_at] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_order_items_order] ON [dbo].[order_items]
(
	[order_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_order_items_variant] ON [dbo].[order_items]
(
	[variant_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_PADDING ON

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_SO2_usage_quota] ON [dbo].[coupon_usages]
(
	[coupon_id] ASC,
	[usage_status] ASC,
	[customer_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE UNIQUE NONCLUSTERED INDEX [UX_SO2_usage_order] ON [dbo].[coupon_usages]
(
	[order_id] ASC
)
WHERE ([usage_status]='Applied')
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_PADDING ON

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_SO2_delivery_shop] ON [dbo].[deliveries]
(
	[shop_id] ASC,
	[delivery_status] ASC,
	[order_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_PADDING ON

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_SO2_delivery_staff] ON [dbo].[deliveries]
(
	[shipper_id] ASC,
	[delivery_status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
SET ANSI_PADDING ON

GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
CREATE NONCLUSTERED INDEX [IX_batches_variant] ON [dbo].[inventory_batches]
(
	[variant_id] ASC,
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers] ADD  DEFAULT ((0)) FOR [min_order_amount]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers] ADD  DEFAULT ((0)) FOR [quantity]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers] ADD  DEFAULT (N'Active') FOR [status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[users] ADD  DEFAULT ('active') FOR [status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[users] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[notifications] ADD  DEFAULT ((0)) FOR [is_read]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[notifications] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[carts] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[user_addresses] ADD  DEFAULT ((0)) FOR [is_default]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shops] ADD  DEFAULT ('active') FOR [status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shops] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shops] ADD  CONSTRAINT [DF_shops_low_stock_threshold]  DEFAULT ((10)) FOR [low_stock_threshold]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shop_delivery_staff] ADD  DEFAULT (N'Active') FOR [status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] ADD  DEFAULT (N'') FOR [description]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] ADD  DEFAULT ((0)) FOR [min_order_amount]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] ADD  DEFAULT ((0)) FOR [is_active]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] ADD  DEFAULT ((0)) FOR [is_deleted]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] ADD  DEFAULT ((0)) FOR [version]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[products] ADD  DEFAULT ('published') FOR [status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[products] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] ADD  DEFAULT (N'') FOR [description]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] ADD  DEFAULT ((0)) FOR [is_active]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] ADD  DEFAULT ((0)) FOR [is_deleted]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] ADD  DEFAULT ((0)) FOR [version]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[reviews] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_variants] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_images] ADD  DEFAULT ((1)) FOR [sort_order]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] ADD  DEFAULT (getdate()) FOR [order_date]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] ADD  DEFAULT ((0)) FOR [coupon_discount]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] ADD  DEFAULT (N'Pending') FOR [order_status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] ADD  DEFAULT (N'COD') FOR [payment_method]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] ADD  DEFAULT (N'Unpaid') FOR [payment_status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages] ADD  DEFAULT ('Applied') FOR [usage_status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[cart_items] ADD  DEFAULT ((1)) FOR [quantity]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[deliveries] ADD  DEFAULT (N'Assigned') FOR [delivery_status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_batches] ADD  DEFAULT ((0)) FOR [quantity]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_batches] ADD  DEFAULT (CONVERT([date],getdate())) FOR [received_date]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_batches] ADD  DEFAULT ('active') FOR [status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_adjustments] ADD  DEFAULT (getdate()) FOR [created_at]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[categories]  WITH CHECK ADD  CONSTRAINT [FK_categories_parent] FOREIGN KEY([parent_id])
REFERENCES [dbo].[categories] ([category_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[categories] CHECK CONSTRAINT [FK_categories_parent]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [FK_SO2_notification_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [FK_SO2_notification_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[carts]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
ON DELETE CASCADE
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[user_roles]  WITH CHECK ADD  CONSTRAINT [FK_user_roles_role] FOREIGN KEY([role_id])
REFERENCES [dbo].[roles] ([role_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[user_roles] CHECK CONSTRAINT [FK_user_roles_role]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[user_roles]  WITH CHECK ADD  CONSTRAINT [FK_user_roles_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[user_roles] CHECK CONSTRAINT [FK_user_roles_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[user_addresses]  WITH CHECK ADD  CONSTRAINT [FK_user_addresses_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[user_addresses] CHECK CONSTRAINT [FK_user_addresses_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shops]  WITH CHECK ADD  CONSTRAINT [FK_shops_owner] FOREIGN KEY([owner_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shops] CHECK CONSTRAINT [FK_shops_owner]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shop_delivery_staff]  WITH CHECK ADD  CONSTRAINT [FK_SO2_staff_shop] FOREIGN KEY([shop_id])
REFERENCES [dbo].[shops] ([shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shop_delivery_staff] CHECK CONSTRAINT [FK_SO2_staff_shop]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shop_delivery_staff]  WITH CHECK ADD  CONSTRAINT [FK_SO2_staff_user] FOREIGN KEY([shipper_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shop_delivery_staff] CHECK CONSTRAINT [FK_SO2_staff_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons]  WITH CHECK ADD  CONSTRAINT [FK_SO2_coupon_shop] FOREIGN KEY([shop_id])
REFERENCES [dbo].[shops] ([shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] CHECK CONSTRAINT [FK_SO2_coupon_shop]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[products]  WITH CHECK ADD  CONSTRAINT [FK_products_category] FOREIGN KEY([category_id])
REFERENCES [dbo].[categories] ([category_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[products] CHECK CONSTRAINT [FK_products_category]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[products]  WITH CHECK ADD  CONSTRAINT [FK_products_shop] FOREIGN KEY([shop_id])
REFERENCES [dbo].[shops] ([shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[products] CHECK CONSTRAINT [FK_products_shop]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions]  WITH CHECK ADD  CONSTRAINT [FK_SO2_promotion_shop] FOREIGN KEY([shop_id])
REFERENCES [dbo].[shops] ([shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] CHECK CONSTRAINT [FK_SO2_promotion_shop]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[reviews]  WITH CHECK ADD  CONSTRAINT [FK_reviews_product] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([product_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[reviews] CHECK CONSTRAINT [FK_reviews_product]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[reviews]  WITH CHECK ADD  CONSTRAINT [FK_reviews_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[reviews] CHECK CONSTRAINT [FK_reviews_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotion_products]  WITH CHECK ADD  CONSTRAINT [FK_SO2_pp_product] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([product_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotion_products] CHECK CONSTRAINT [FK_SO2_pp_product]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotion_products]  WITH CHECK ADD  CONSTRAINT [FK_SO2_pp_promotion] FOREIGN KEY([promotion_id])
REFERENCES [dbo].[promotions] ([promotion_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotion_products] CHECK CONSTRAINT [FK_SO2_pp_promotion]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_variants]  WITH CHECK ADD  CONSTRAINT [FK_variants_product] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([product_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_variants] CHECK CONSTRAINT [FK_variants_product]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_images]  WITH CHECK ADD  CONSTRAINT [FK_images_product] FOREIGN KEY([product_id])
REFERENCES [dbo].[products] ([product_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_images] CHECK CONSTRAINT [FK_images_product]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [FK_orders_voucher] FOREIGN KEY([voucher_id])
REFERENCES [dbo].[vouchers] ([voucher_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [FK_orders_voucher]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [FK_SO2_order_coupon] FOREIGN KEY([coupon_id], [shop_id])
REFERENCES [dbo].[coupons] ([coupon_id], [shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [FK_SO2_order_coupon]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [FK_SO2_orders_shop] FOREIGN KEY([shop_id])
REFERENCES [dbo].[shops] ([shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [FK_SO2_orders_shop]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [FK_SO2_orders_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [FK_SO2_orders_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[order_items]  WITH CHECK ADD  CONSTRAINT [FK_SO2_details_order] FOREIGN KEY([order_id])
REFERENCES [dbo].[orders] ([order_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[order_items] CHECK CONSTRAINT [FK_SO2_details_order]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[order_items]  WITH CHECK ADD  CONSTRAINT [FK_SO2_details_variant] FOREIGN KEY([variant_id])
REFERENCES [dbo].[product_variants] ([variant_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[order_items] CHECK CONSTRAINT [FK_SO2_details_variant]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages]  WITH CHECK ADD  CONSTRAINT [FK_SO2_usage_coupon] FOREIGN KEY([coupon_id], [shop_id])
REFERENCES [dbo].[coupons] ([coupon_id], [shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages] CHECK CONSTRAINT [FK_SO2_usage_coupon]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages]  WITH CHECK ADD  CONSTRAINT [FK_SO2_usage_order] FOREIGN KEY([order_id], [shop_id])
REFERENCES [dbo].[orders] ([order_id], [shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages] CHECK CONSTRAINT [FK_SO2_usage_order]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages]  WITH CHECK ADD  CONSTRAINT [FK_SO2_usage_user] FOREIGN KEY([customer_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages] CHECK CONSTRAINT [FK_SO2_usage_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages]  WITH CHECK ADD  CONSTRAINT [FK_usage_order_customer] FOREIGN KEY([order_id], [customer_id])
REFERENCES [dbo].[orders] ([order_id], [user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages] CHECK CONSTRAINT [FK_usage_order_customer]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[cart_items]  WITH CHECK ADD  CONSTRAINT [FK_cart_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[cart_items] CHECK CONSTRAINT [FK_cart_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[cart_items]  WITH CHECK ADD  CONSTRAINT [FK_cart_variant] FOREIGN KEY([variant_id])
REFERENCES [dbo].[product_variants] ([variant_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[cart_items] CHECK CONSTRAINT [FK_cart_variant]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[deliveries]  WITH CHECK ADD  CONSTRAINT [FK_SO2_delivery_order] FOREIGN KEY([order_id], [shop_id])
REFERENCES [dbo].[orders] ([order_id], [shop_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[deliveries] CHECK CONSTRAINT [FK_SO2_delivery_order]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[deliveries]  WITH CHECK ADD  CONSTRAINT [FK_SO2_delivery_staff] FOREIGN KEY([shop_id], [shipper_id])
REFERENCES [dbo].[shop_delivery_staff] ([shop_id], [shipper_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[deliveries] CHECK CONSTRAINT [FK_SO2_delivery_staff]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_batches]  WITH CHECK ADD  CONSTRAINT [FK_batches_variant] FOREIGN KEY([variant_id])
REFERENCES [dbo].[product_variants] ([variant_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_batches] CHECK CONSTRAINT [FK_batches_variant]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_adjustments]  WITH CHECK ADD  CONSTRAINT [FK_adjustments_batch] FOREIGN KEY([batch_id])
REFERENCES [dbo].[inventory_batches] ([batch_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_adjustments] CHECK CONSTRAINT [FK_adjustments_batch]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_adjustments]  WITH CHECK ADD  CONSTRAINT [FK_adjustments_user] FOREIGN KEY([adjusted_by])
REFERENCES [dbo].[users] ([user_id])
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_adjustments] CHECK CONSTRAINT [FK_adjustments_user]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers]  WITH CHECK ADD CHECK  (([discount_amount]>(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers]  WITH CHECK ADD CHECK  (([min_order_amount]>=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers]  WITH CHECK ADD CHECK  (([quantity]>=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers]  WITH CHECK ADD CHECK  (([status]=N'Inactive' OR [status]=N'Active'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers]  WITH CHECK ADD  CONSTRAINT [CK_voucher_period] CHECK  (([start_date]<=[end_date]))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[vouchers] CHECK CONSTRAINT [CK_voucher_period]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[users]  WITH CHECK ADD CHECK  (([status]='pending' OR [status]='inactive' OR [status]='blocked' OR [status]='active'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[categories]  WITH CHECK ADD  CONSTRAINT [CK_categories_parent] CHECK  (([parent_id] IS NULL OR [parent_id]<>[category_id]))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[categories] CHECK CONSTRAINT [CK_categories_parent]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shops]  WITH CHECK ADD CHECK  (([status]='inactive' OR [status]='active'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[shop_delivery_staff]  WITH CHECK ADD CHECK  (([status]=N'Inactive' OR [status]=N'Active'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons]  WITH CHECK ADD  CONSTRAINT [CK_SO2_coupon_deleted] CHECK  (([is_deleted]=(0) OR [is_active]=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] CHECK CONSTRAINT [CK_SO2_coupon_deleted]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons]  WITH CHECK ADD  CONSTRAINT [CK_SO2_coupon_discount] CHECK  (([discount_type]='PERCENT' AND [discount_value]>(0) AND [discount_value]<=(100) OR [discount_type]='FIXED' AND [discount_value]>(0) AND [discount_value]=floor([discount_value])))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] CHECK CONSTRAINT [CK_SO2_coupon_discount]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons]  WITH CHECK ADD  CONSTRAINT [CK_SO2_coupon_limits] CHECK  (([min_order_amount]>=(0) AND ([max_discount] IS NULL OR [discount_type]='PERCENT' AND [max_discount]>(0)) AND [total_limit]>(0) AND [per_customer_limit]>(0) AND [per_customer_limit]<=[total_limit]))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] CHECK CONSTRAINT [CK_SO2_coupon_limits]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons]  WITH CHECK ADD  CONSTRAINT [CK_SO2_coupon_period] CHECK  (([starts_at]<[ends_at]))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupons] CHECK CONSTRAINT [CK_SO2_coupon_period]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[products]  WITH CHECK ADD CHECK  (([status]='draft' OR [status]='hidden' OR [status]='published'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions]  WITH CHECK ADD  CONSTRAINT [CK_SO2_promotion_deleted] CHECK  (([is_deleted]=(0) OR [is_active]=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] CHECK CONSTRAINT [CK_SO2_promotion_deleted]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions]  WITH CHECK ADD  CONSTRAINT [CK_SO2_promotion_discount] CHECK  (([discount_type]='PERCENT' AND [discount_value]>(0) AND [discount_value]<=(100) OR [discount_type]='FIXED' AND [discount_value]>(0) AND [discount_value]=floor([discount_value])))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] CHECK CONSTRAINT [CK_SO2_promotion_discount]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions]  WITH CHECK ADD  CONSTRAINT [CK_SO2_promotion_period] CHECK  (([starts_at]<[ends_at]))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[promotions] CHECK CONSTRAINT [CK_SO2_promotion_period]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[reviews]  WITH CHECK ADD CHECK  (([rating]>=(1) AND [rating]<=(5)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_variants]  WITH CHECK ADD CHECK  (([price]>(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_variants]  WITH CHECK ADD CHECK  (([weight_value] IS NULL OR [weight_value]>(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[product_images]  WITH CHECK ADD CHECK  (([sort_order]>=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD CHECK  (([order_status]=N'Cancelled' OR [order_status]=N'Completed' OR [order_status]=N'Shipping' OR [order_status]=N'Confirmed' OR [order_status]=N'Pending'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD CHECK  (([payment_status]=N'Paid' OR [payment_status]=N'Unpaid'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD CHECK  (([total_amount]>=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [CK_SO2_order_coupon] CHECK  (([coupon_id] IS NULL AND [coupon_discount]=(0) AND [coupon_subtotal] IS NULL OR [coupon_id] IS NOT NULL AND [voucher_id] IS NULL AND [coupon_subtotal] IS NOT NULL AND [coupon_discount]>=(0) AND [coupon_subtotal]>=[coupon_discount] AND [total_amount]=([coupon_subtotal]-[coupon_discount])))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [CK_SO2_order_coupon]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[order_items]  WITH CHECK ADD CHECK  (([price]>=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[order_items]  WITH CHECK ADD CHECK  (([quantity]>(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages]  WITH CHECK ADD  CONSTRAINT [CK_SO2_usage_amount] CHECK  (([subtotal]>(0) AND [discount_amount]>(0) AND [final_amount]>=(0) AND [final_amount]=([subtotal]-[discount_amount])))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages] CHECK CONSTRAINT [CK_SO2_usage_amount]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages]  WITH CHECK ADD  CONSTRAINT [CK_SO2_usage_status] CHECK  (([usage_status]='Applied' AND [removed_at] IS NULL OR [usage_status]='Removed' AND [removed_at] IS NOT NULL))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[coupon_usages] CHECK CONSTRAINT [CK_SO2_usage_status]
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[cart_items]  WITH CHECK ADD CHECK  (([quantity]>(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[deliveries]  WITH CHECK ADD CHECK  (([delivery_status]=N'Failed' OR [delivery_status]=N'Delivered' OR [delivery_status]=N'Delivering' OR [delivery_status]=N'PickedUp' OR [delivery_status]=N'Rejected' OR [delivery_status]=N'Accepted' OR [delivery_status]=N'Assigned'))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_batches]  WITH CHECK ADD CHECK  (([quantity]>=(0)))
GO
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
ALTER TABLE [dbo].[inventory_batches]  WITH CHECK ADD CHECK  (([status]='expired' OR [status]='inactive' OR [status]='active'))
GO