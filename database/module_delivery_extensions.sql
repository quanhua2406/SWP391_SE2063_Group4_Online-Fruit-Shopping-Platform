-- Run AFTER OnlineFruitShopping_DB_FINAL_MERGED.sql on the existing database.
-- Adds module-owned tables only. Does not ALTER/DROP the group's existing tables.
-- Safe to rerun: creates missing tables/indexes and copies only missing rows.
USE [OnlineFruitShopping];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;

BEGIN TRY
    BEGIN TRANSACTION;
    IF OBJECT_ID(N'dbo.shop_delivery_staff', N'U') IS NULL
       OR COL_LENGTH(N'dbo.orders', N'coupon_subtotal') IS NULL
        THROW 51000, N'Install OnlineFruitShopping_DB_FINAL_MERGED.sql first.', 1;

    IF OBJECT_ID(N'dbo.shop_delivery_settings', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.shop_delivery_settings (
            shop_id int NOT NULL,
            shipper_id int NOT NULL,
            is_available bit NOT NULL CONSTRAINT DF_delivery_settings_available DEFAULT (1),
            assignment_enabled bit NOT NULL CONSTRAINT DF_delivery_settings_assignment DEFAULT (1),
            leave_requested bit NOT NULL CONSTRAINT DF_delivery_settings_leave DEFAULT (0),
            CONSTRAINT PK_shop_delivery_settings PRIMARY KEY (shop_id, shipper_id),
            CONSTRAINT FK_delivery_settings_member FOREIGN KEY (shop_id, shipper_id)
                REFERENCES dbo.shop_delivery_staff(shop_id, shipper_id),
            CONSTRAINT CK_delivery_settings_leave CHECK
                (leave_requested = 0 OR (is_available = 0 AND assignment_enabled = 0))
        );
    END;

    IF OBJECT_ID(N'dbo.shop_delivery_requests', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.shop_delivery_requests (
            request_id int IDENTITY(1,1) NOT NULL,
            shipper_id int NOT NULL,
            shop_id int NOT NULL,
            status nvarchar(20) NOT NULL CONSTRAINT DF_delivery_requests_status DEFAULT (N'Pending'),
            requested_at datetime2 NOT NULL CONSTRAINT DF_delivery_requests_requested DEFAULT (SYSDATETIME()),
            reviewed_at datetime2 NULL,
            reviewed_by int NULL,
            review_note nvarchar(255) NULL,
            CONSTRAINT PK_shop_delivery_requests PRIMARY KEY (request_id),
            CONSTRAINT FK_delivery_requests_shipper FOREIGN KEY (shipper_id) REFERENCES dbo.users(user_id),
            CONSTRAINT FK_delivery_requests_shop FOREIGN KEY (shop_id) REFERENCES dbo.shops(shop_id),
            CONSTRAINT FK_delivery_requests_reviewer FOREIGN KEY (reviewed_by) REFERENCES dbo.users(user_id),
            CONSTRAINT CK_delivery_requests_status CHECK (status IN (N'Pending',N'Approved',N'Rejected',N'Withdrawn'))
        );
    END;

    IF OBJECT_ID(N'dbo.delivery_attempts', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.delivery_attempts (
            attempt_id int IDENTITY(1,1) NOT NULL,
            delivery_id int NOT NULL,
            shipper_id int NOT NULL,
            assigned_by int NOT NULL,
            delivery_status nvarchar(20) NOT NULL CONSTRAINT DF_delivery_attempts_status DEFAULT (N'Assigned'),
            assigned_at datetime2 NOT NULL CONSTRAINT DF_delivery_attempts_assigned DEFAULT (SYSDATETIME()),
            accepted_at datetime2 NULL,
            pickup_time datetime2 NULL,
            delivered_time datetime2 NULL,
            closed_at datetime2 NULL,
            receiver_name nvarchar(100) NULL,
            receiver_phone nvarchar(20) NULL,
            shipping_address nvarchar(1000) NULL,
            pickup_address nvarchar(500) NULL,
            note nvarchar(255) NULL,
            proof_image_url nvarchar(255) NULL,
            version int NOT NULL CONSTRAINT DF_delivery_attempts_version DEFAULT (0),
            CONSTRAINT PK_delivery_attempts PRIMARY KEY (attempt_id),
            CONSTRAINT FK_delivery_attempts_delivery FOREIGN KEY (delivery_id) REFERENCES dbo.deliveries(delivery_id),
            CONSTRAINT FK_delivery_attempts_shipper FOREIGN KEY (shipper_id) REFERENCES dbo.users(user_id),
            CONSTRAINT FK_delivery_attempts_owner FOREIGN KEY (assigned_by) REFERENCES dbo.users(user_id),
            CONSTRAINT CK_delivery_attempts_state CHECK (
                (delivery_status IN (N'Assigned',N'Accepted',N'PickedUp',N'Delivering') AND closed_at IS NULL)
                OR (delivery_status IN (N'Rejected',N'Delivered',N'Failed') AND closed_at IS NOT NULL)),
            CONSTRAINT CK_delivery_attempts_version CHECK (version >= 0)
        );
    END;

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.shop_delivery_requests') AND name=N'UX_delivery_requests_pending')
        CREATE UNIQUE INDEX UX_delivery_requests_pending ON dbo.shop_delivery_requests(shipper_id) WHERE status=N'Pending';
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.delivery_attempts') AND name=N'UX_delivery_attempts_open')
        CREATE UNIQUE INDEX UX_delivery_attempts_open ON dbo.delivery_attempts(delivery_id) WHERE closed_at IS NULL;
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.delivery_attempts') AND name=N'IX_delivery_attempts_shipper')
        CREATE INDEX IX_delivery_attempts_shipper ON dbo.delivery_attempts(shipper_id,closed_at,attempt_id DESC);

    -- Preserve old flags when upgrading a previous module installation.
    IF COL_LENGTH(N'dbo.shop_delivery_staff', N'is_available') IS NOT NULL
       AND COL_LENGTH(N'dbo.shop_delivery_staff', N'assignment_enabled') IS NOT NULL
       AND COL_LENGTH(N'dbo.shop_delivery_staff', N'leave_requested') IS NOT NULL
        EXEC(N'INSERT INTO dbo.shop_delivery_settings(shop_id,shipper_id,is_available,assignment_enabled,leave_requested)
            SELECT t.shop_id,t.shipper_id,CASE WHEN t.leave_requested=1 THEN 0 ELSE t.is_available END,
                CASE WHEN t.leave_requested=1 THEN 0 ELSE t.assignment_enabled END,t.leave_requested
            FROM dbo.shop_delivery_staff t
            WHERE NOT EXISTS(SELECT 1 FROM dbo.shop_delivery_settings x WHERE x.shop_id=t.shop_id AND x.shipper_id=t.shipper_id)');
    ELSE
        INSERT INTO dbo.shop_delivery_settings(shop_id,shipper_id)
        SELECT t.shop_id,t.shipper_id FROM dbo.shop_delivery_staff t
        WHERE NOT EXISTS(SELECT 1 FROM dbo.shop_delivery_settings x WHERE x.shop_id=t.shop_id AND x.shipper_id=t.shipper_id);

    -- Import one snapshot for existing assigned deliveries. Never invent a delivered_time.
    -- closed_at is the import time if a terminal legacy delivery has no completion time.
    INSERT INTO dbo.delivery_attempts(delivery_id,shipper_id,assigned_by,delivery_status,
        assigned_at,pickup_time,delivered_time,closed_at,receiver_name,receiver_phone,
        shipping_address,pickup_address,note,proof_image_url)
    SELECT d.delivery_id,COALESCE(d.shipper_id,d.staff_id),s.owner_id,
        CASE d.status WHEN N'assigned' THEN N'Assigned' WHEN N'accepted' THEN N'Accepted'
            WHEN N'rejected' THEN N'Rejected' WHEN N'picked_up' THEN N'PickedUp'
            WHEN N'delivering' THEN N'Delivering' WHEN N'delivered' THEN N'Delivered' ELSE N'Failed' END,
        d.created_at,d.pickup_time,d.delivered_time,
        CASE WHEN d.status IN (N'rejected',N'delivered',N'failed') THEN COALESCE(d.delivered_time,SYSDATETIME()) END,
        COALESCE(NULLIF(o.receiver_name,N''),ua.recipient_name),
        COALESCE(NULLIF(o.receiver_phone,N''),ua.phone),
        COALESCE(NULLIF(o.shipping_address,N''),CONCAT_WS(N', ',NULLIF(ua.address_line,N''),
            NULLIF(ua.ward,N''),NULLIF(ua.district,N''),NULLIF(ua.province,N''))),
        s.pickup_address,d.note,d.proof_image_url
    FROM dbo.deliveries d
    JOIN dbo.orders o ON o.order_id=d.order_id
    JOIN dbo.shops s ON s.shop_id=o.shop_id
    LEFT JOIN dbo.user_addresses ua ON ua.address_id=o.address_id AND ua.user_id=o.user_id
    WHERE COALESCE(d.shipper_id,d.staff_id) IS NOT NULL
      AND NOT EXISTS(SELECT 1 FROM dbo.delivery_attempts a WHERE a.delivery_id=d.delivery_id);

    COMMIT TRANSACTION;
    PRINT N'Delivery extension tables ready. Existing core schema unchanged.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
