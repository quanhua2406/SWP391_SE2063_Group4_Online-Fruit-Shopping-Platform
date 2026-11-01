/*
  Shipper team management migration - SQL Server
  Run once on a BACKUP / test copy of OnlineFruitShopping first.
  Does not add is_busy. Busy is calculated from open delivery_attempts.
  Admin shipper-management workflows are intentionally out of scope.
*/
SET XACT_ABORT ON;
GO

-- Preflight: resolve duplicate active memberships. Multiple open attempts per shipper
-- are now allowed, up to 3; Java enforces the limit inside a transaction.
SELECT shipper_id, COUNT(*) AS active_shop_count
FROM dbo.shop_delivery_staff
WHERE status <> N'Inactive'
GROUP BY shipper_id
HAVING COUNT(*) > 1;

SELECT shipper_id, COUNT(*) AS open_attempt_count
FROM dbo.delivery_attempts
WHERE closed_at IS NULL
GROUP BY shipper_id
HAVING COUNT(*) > 3;
GO

IF COL_LENGTH('dbo.shop_delivery_staff','is_available') IS NULL
    ALTER TABLE dbo.shop_delivery_staff ADD is_available BIT NOT NULL
        CONSTRAINT DF_shop_delivery_staff_is_available DEFAULT (1) WITH VALUES;
GO
IF COL_LENGTH('dbo.shop_delivery_staff','assignment_enabled') IS NULL
    ALTER TABLE dbo.shop_delivery_staff ADD assignment_enabled BIT NOT NULL
        CONSTRAINT DF_shop_delivery_staff_assignment_enabled DEFAULT (1) WITH VALUES;
GO
IF COL_LENGTH('dbo.shop_delivery_staff','leave_requested') IS NULL
    ALTER TABLE dbo.shop_delivery_staff ADD leave_requested BIT NOT NULL
        CONSTRAINT DF_shop_delivery_staff_leave_requested DEFAULT (0) WITH VALUES;
GO

IF OBJECT_ID('dbo.shop_delivery_requests','U') IS NULL
BEGIN
    CREATE TABLE dbo.shop_delivery_requests (
        request_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_shop_delivery_requests PRIMARY KEY,
        shipper_id INT NOT NULL,
        shop_id INT NOT NULL,
        status NVARCHAR(20) NOT NULL CONSTRAINT DF_shop_delivery_requests_status DEFAULT N'Pending',
        requested_at DATETIME2(0) NOT NULL CONSTRAINT DF_shop_delivery_requests_requested_at DEFAULT SYSDATETIME(),
        reviewed_at DATETIME2(0) NULL,
        reviewed_by INT NULL,
        review_note NVARCHAR(255) NULL,
        CONSTRAINT FK_shop_delivery_requests_shipper FOREIGN KEY(shipper_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_shop_delivery_requests_shop FOREIGN KEY(shop_id) REFERENCES dbo.shops(shop_id),
        CONSTRAINT FK_shop_delivery_requests_reviewer FOREIGN KEY(reviewed_by) REFERENCES dbo.users(user_id),
        CONSTRAINT CK_shop_delivery_requests_status CHECK(status IN (N'Pending',N'Approved',N'Rejected',N'Withdrawn'))
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.shop_delivery_requests') AND name='UX_shop_delivery_requests_one_pending')
    CREATE UNIQUE INDEX UX_shop_delivery_requests_one_pending
    ON dbo.shop_delivery_requests(shipper_id) WHERE status=N'Pending';
GO

-- IMPORTANT: this will fail if the preflight query above returned duplicate active memberships.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.shop_delivery_staff') AND name='UX_shop_delivery_staff_one_shop')
    CREATE UNIQUE INDEX UX_shop_delivery_staff_one_shop
    ON dbo.shop_delivery_staff(shipper_id) WHERE status<>N'Inactive';
GO

-- Remove the old rule (one open attempt per shipper). The new limit of 3 is
-- checked transactionally by AssignmentDAO after locking the shipper's users row.
IF EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.delivery_attempts') AND name='UX_delivery_attempts_one_open_per_shipper')
    DROP INDEX UX_delivery_attempts_one_open_per_shipper ON dbo.delivery_attempts;
GO
