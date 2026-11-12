-- Commission settlements (monthly shop payment to the platform).
-- Run after database/reference/OnlineFruitShopping_DB_FINAL_MERGED.sql.
-- Idempotent: safe to run more than once.

IF OBJECT_ID('dbo.commission_settlements', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.commission_settlements (
        settlement_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        shop_id         INT NOT NULL,
        period_month    CHAR(7) NOT NULL,
        amount          DECIMAL(12,0) NOT NULL,
        issued_date     DATE NOT NULL,
        due_date        DATE NOT NULL,
        status          NVARCHAR(10) NOT NULL CONSTRAINT DF_commission_settlement_status DEFAULT ('unpaid'),
        shop_locked     BIT NOT NULL CONSTRAINT DF_commission_settlement_locked DEFAULT (0),
        transaction_ref NVARCHAR(100) NULL,
        paid_at         DATETIME2 NULL,
        CONSTRAINT UQ_commission_settlement UNIQUE (shop_id, period_month),
        CONSTRAINT FK_commission_settlement_shop FOREIGN KEY (shop_id) REFERENCES dbo.shops(shop_id),
        CONSTRAINT CK_commission_settlement_status CHECK (status IN ('unpaid', 'paid'))
    );
END;
GO

IF COL_LENGTH('dbo.products', 'hidden_by_commission') IS NULL
    ALTER TABLE dbo.products ADD hidden_by_commission BIT NOT NULL
        CONSTRAINT DF_products_hidden_by_commission DEFAULT (0);
GO
