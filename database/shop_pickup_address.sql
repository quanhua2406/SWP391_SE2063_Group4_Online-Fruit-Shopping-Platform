-- Run ONCE on the existing OnlineFruitShopping database (SQL Server).
-- Never drops or recreates shops. ShopOwner enters real address in "Quản lý đội shipper".
IF COL_LENGTH('dbo.shops', 'pickup_address') IS NULL
BEGIN
    ALTER TABLE dbo.shops ADD pickup_address NVARCHAR(500) NULL;
END;
GO
